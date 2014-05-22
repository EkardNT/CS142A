package crux;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import ast.*;
import ast.Error;
import crux.parsing.*;
import types.*;

public class Parser 
{	
	public static String studentName = "Drake Tetreault";
    public static String studentID = "35571095";
    public static String uciNetID = "dtetreau";
	
	private final LL1Reader reader;
	private final StringBuilder errorReport;
	private final Stack<SymbolTable> symbolTables;
	private ParseNode root, current;
	
	public Parser(Scanner scanner)
	{
		this.reader = new LL1Reader(scanner);
		this.errorReport = new StringBuilder();
		this.symbolTables = new Stack<SymbolTable>();
		root = current = null;
		SymbolTable rootTable = new SymbolTable(null);
		rootTable.define("readInt", 0, 0, new IntType());
		rootTable.define("readFloat", 0, 0, new FloatType());
		rootTable.define("printBool", 0, 0, new BoolType());
		rootTable.define("printInt", 0, 0, new VoidType());
		rootTable.define("printFloat", 0, 0, new VoidType());
		rootTable.define("println", 0, 0, new VoidType());
		symbolTables.push(rootTable);
	}
		
	public boolean hasError()
	{
		return errorReport.length() > 0;
	}
	
	public String errorReport()
	{
		return errorReport.toString();
	}
	
	public String parseTreeReport()
	{
		StringBuilder b = new StringBuilder();
		buildReport(root, 0, b);
		return b.toString();
	}
	
	private void buildReport(ParseNode current, int indent, StringBuilder b)
	{
		while(current != null)
		{
			if(current.Terminal == null)
			{
				for(int i = 0; i < indent; i++)
					b.append("  "); // 2 spaces
				b.append(current.ProductionRule.toString());
				b.append('\n');
				// Children
				buildReport(current.FirstChild, indent + 1, b);
			}
			// Siblings
			current = current.Sibling;
		}
	}
	
	public Command parse()
	{
			try 
			{
				return program();
			} 
			catch (RequiredTokenException e)
			{
				// According to instructions, if the parser encounters a syntax
				// error it returns an Error node.
				return new Error(
					e.ActualToken.getLineNumber(), 
					e.ActualToken.getCharPos(), 
					String.format("A token of kind \"%s\" was expected but the token \"%\" of kind \"%s\" was found instead.", e.ExpectedKind, e.ActualToken.getLexeme(), e.ActualToken.getKind()));
			}
	}
	
	private Command program() throws RequiredTokenException
	{
		enterRule(NonTerminal.PROGRAM);
		DeclarationList declarationList = declaration_list(1, 1);
		exitRule();
		return declarationList;
	}
	
	private StatementList statement_block(boolean suppressNewScope) throws RequiredTokenException 
	{
		enterRule(NonTerminal.STATEMENT_BLOCK);
		if(!suppressNewScope)
			enterScope();
		Token openBraceToken = require(Token.Kind.OPEN_BRACE);
		StatementList statementList = statement_list(openBraceToken.getLineNumber(), openBraceToken.getCharPos());		
		require(Token.Kind.CLOSE_BRACE);
		if(!suppressNewScope)
			exitScope();
		exitRule();
		return statementList;
	}
	
	private StatementList statement_list(int fallbackLineNumber, int fallbackCharPosition) throws RequiredTokenException 
	{
		enterRule(NonTerminal.STATEMENT_LIST);
		List<Statement> statements = new ArrayList<Statement>();
		while(firstSetSatisfied(NonTerminal.STATEMENT))
			statements.add(statement());
		exitRule();
		return new StatementList(
			statements.size() > 0 ? ((Command)statements.get(0)).lineNumber() : fallbackLineNumber,
			statements.size() > 0 ? ((Command)statements.get(0)).charPosition() : fallbackCharPosition,
			statements);
	}
	
	private Statement statement() throws RequiredTokenException 
	{
		enterRule(NonTerminal.STATEMENT);
		Statement statement = null;
		if(firstSetSatisfied(NonTerminal.VARIABLE_DECLARATION))
			try 
			{
				statement = variable_declaration();
			} 
			catch (SymbolRedefinitionException e) 
			{
				statement = new Error(
					e.DuplicateToken.getLineNumber(),
					e.DuplicateToken.getCharPos(),
					String.format("Attempted to redefine symbol \"%s\".", e.DuplicateToken.getLexeme()));
			}
		else if(firstSetSatisfied(NonTerminal.CALL_STATEMENT))
			try 
			{
				statement = call_statement();
			} 
			catch (UnresolvableSymbolException e) 
			{
				statement = new Error(
					e.UnresolvedToken.getLineNumber(),
					e.UnresolvedToken.getCharPos(),
					String.format("Unable to resolve symbol \"%s\".", e.UnresolvedToken.getLexeme()));
			}
		else if(firstSetSatisfied(NonTerminal.ASSIGNMENT_STATEMENT))
			statement = assignment_statement();
		else if(firstSetSatisfied(NonTerminal.IF_STATEMENT))
			statement = if_statement();
		else if(firstSetSatisfied(NonTerminal.WHILE_STATEMENT))
			statement = while_statement();
		else if(firstSetSatisfied(NonTerminal.RETURN_STATEMENT))
			statement = return_statement();
		else
		{
			statement = new Error(
				reader.token().getLineNumber(), 
				reader.token().getCharPos(),
				String.format("First set unsatisfied for non-terminal of kind \"%s\".", reader.token().getKind()));
		}
		exitRule();
		return statement;
	}
	
	private Return return_statement() throws RequiredTokenException 
	{
		enterRule(NonTerminal.RETURN_STATEMENT);
		Token returnToken = require(Token.Kind.RETURN);
		Expression arg = expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new Return(returnToken.getLineNumber(), returnToken.getCharPos(), arg);
	}
	
	private WhileLoop while_statement() throws RequiredTokenException 
	{
		enterRule(NonTerminal.WHILE_STATEMENT);
		Token whileToken = require(Token.Kind.WHILE);
		Expression conditional = expression0();
		StatementList body = statement_block(false);
		exitRule();
		return new WhileLoop(
			whileToken.getLineNumber(),
			whileToken.getCharPos(),
			conditional,
			body);
	}
	
	private IfElseBranch if_statement() throws RequiredTokenException 
	{
		enterRule(NonTerminal.IF_STATEMENT);
		Token ifToken = require(Token.Kind.IF);
		Expression conditional = expression0();
		StatementList thenBlock = statement_block(false);
		StatementList elseBlock = accept(Token.Kind.ELSE) ? statement_block(false) : new StatementList(reader.token().getLineNumber(), reader.token().getCharPos(), new ArrayList<Statement>());
		exitRule(); 
		return new IfElseBranch(
				ifToken.getLineNumber(), 
				ifToken.getCharPos(), 
				conditional, 
				thenBlock, 
				elseBlock);
	}
	
	private Call call_statement() throws RequiredTokenException, UnresolvableSymbolException 
	{
		enterRule(NonTerminal.CALL_STATEMENT);
		Call callExpression = call_expression();
		require(Token.Kind.SEMICOLON);
		exitRule();
		return callExpression;
	}
	
	private Assignment assignment_statement() throws RequiredTokenException 
	{
		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
		Token letToken = require(Token.Kind.LET);
		Expression destination = designator(true);
		require(Token.Kind.ASSIGN);
		Expression source = expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new Assignment(
			letToken.getLineNumber(),
			letToken.getCharPos(),
			destination,
			source);			
	}
	
	private DeclarationList declaration_list(int fallbackLineNumber, int fallbackCharPosition) throws RequiredTokenException 
	{
		enterRule(NonTerminal.DECLARATION_LIST);
		ArrayList<Declaration> declarations = new ArrayList<Declaration>();
		while(firstSetSatisfied(NonTerminal.DECLARATION))
		{
			declarations.add(declaration());			
		}
		require(Token.Kind.EOF);
		exitRule();
		return new DeclarationList(
			declarations.size() > 0 ? ((Command)declarations.get(0)).lineNumber() : fallbackLineNumber,
			declarations.size() > 0 ? ((Command)declarations.get(0)).charPosition() : fallbackCharPosition,
			declarations);
	}
	
	private Declaration declaration() throws RequiredTokenException 
	{
		enterRule(NonTerminal.DECLARATION);
		Declaration declaration = null;
		try
		{
			if(firstSetSatisfied(NonTerminal.VARIABLE_DECLARATION))
				declaration = variable_declaration();
			else if(firstSetSatisfied(NonTerminal.ARRAY_DECLARATION))
				declaration = array_declaration();
			else if(firstSetSatisfied(NonTerminal.FUNCTION_DEFINITION))
				declaration = function_definition();
			else
			{
				declaration = new Error(
					reader.token().getLineNumber(), 
					reader.token().getCharPos(), 
					String.format("First sets unsatisfied by token of kind \"%s\".", reader.token().getKind()));
			}
		}
		catch(SymbolRedefinitionException e)
		{
			declaration = new Error(
				e.DuplicateToken.getLineNumber(),
				e.DuplicateToken.getCharPos(),
				String.format("Attempted to redefine symbol \"%s\".", e.DuplicateToken.getLexeme()));
		}
		exitRule();
		return declaration;
	}
	
	private FunctionDefinition function_definition() throws RequiredTokenException, SymbolRedefinitionException 
	{
		enterRule(NonTerminal.FUNCTION_DEFINITION);
		Token funcToken = require(Token.Kind.FUNC);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)), null);
		enterScope();
		require(Token.Kind.OPEN_PAREN);
		ArrayList<Symbol> parameters = parameter_list();
		TypeList paramTypes = new TypeList();
		for(Symbol param : parameters)
			paramTypes.add(param.getType());
		require(Token.Kind.CLOSE_PAREN);
		require(Token.Kind.COLON);
		Type returnType = Type.getBaseType(type().getLexeme());
		nameSymbol.setType(new FuncType(paramTypes, returnType));
		StatementList bodyStatements = statement_block(true);				
		exitScope();
		exitRule();
		return new FunctionDefinition(
				funcToken.getLineNumber(),
				funcToken.getCharPos(),
				nameSymbol,
				parameters,
				bodyStatements);
	}
	
	private ArrayDeclaration array_declaration() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.ARRAY_DECLARATION);
		Token arrayToken = require(Token.Kind.ARRAY);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)), null);
		require(Token.Kind.COLON);
		Type elementType = Type.getBaseType(type().getLexeme());
		require(Token.Kind.OPEN_BRACKET);
		Stack<String> dimensions = new Stack<String>();
		dimensions.push(emitTerminal(require(Token.Kind.INTEGER)).getLexeme());
		require(Token.Kind.CLOSE_BRACKET);		
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			dimensions.push(emitTerminal(require(Token.Kind.INTEGER)).getLexeme());
			require(Token.Kind.CLOSE_BRACKET);
		}
		// Have to reverse the order of dimensions, because
		// int arr[8][5][3]
		//		   is -> array(8, array(5, array(3, int)))
		// instead of -> array(3, array(5, array(8, int)))
		Type arrayType = new types.ArrayType(Integer.parseInt(dimensions.pop()), elementType);
		while(!dimensions.isEmpty())
		{
			arrayType = new types.ArrayType(Integer.parseInt(dimensions.pop()), arrayType);
		}		
		nameSymbol.setType(arrayType);
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new ArrayDeclaration(
				arrayToken.getLineNumber(), 
				arrayToken.getCharPos(),
				nameSymbol);
	}
	
	private VariableDeclaration variable_declaration() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.VARIABLE_DECLARATION);
		Token varToken = require(Token.Kind.VAR);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)), null);
		require(Token.Kind.COLON);
		Type varType = Type.getBaseType(type().getLexeme());
		nameSymbol.setType(varType);
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new VariableDeclaration(
			varToken.getLineNumber(), 
			varToken.getCharPos(), 
			nameSymbol);
	}
	
	private ArrayList<Symbol> parameter_list() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.PARAMETER_LIST);
		ArrayList<Symbol> parameters = new ArrayList<Symbol>();
		if(firstSetSatisfied(NonTerminal.PARAMETER))
		{
			parameters.add(parameter());
			while(accept(Token.Kind.COMMA))
				parameters.add(parameter());
		}
		exitRule();
		return parameters;
	}
	
	private Symbol parameter() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.PARAMETER);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)), null);
		require(Token.Kind.COLON);
		Type paramType = Type.getBaseType(type().getLexeme());
		nameSymbol.setType(paramType);
		exitRule();
		return nameSymbol;
	}
	
	private ExpressionList expression_list(int fallbackLineNumber, int fallbackCharPosition) throws RequiredTokenException 
	{
		enterRule(NonTerminal.EXPRESSION_LIST);
		ArrayList<Expression> expressions = new ArrayList<Expression>();
		if(firstSetSatisfied(NonTerminal.EXPRESSION0))
		{
			expressions.add(expression0());
			while(accept(Token.Kind.COMMA))
				expressions.add(expression0());
		}
		exitRule();
		return new ExpressionList(
			expressions.size() > 0 ? ((Command)expressions.get(0)).lineNumber() : fallbackLineNumber,
			expressions.size() > 0 ? expressions.get(0).getLeftmostCharPos(): fallbackCharPosition,
			expressions);
	}
	
	private Call call_expression() throws RequiredTokenException, UnresolvableSymbolException 
	{
		enterRule(NonTerminal.CALL_EXPRESSION);
		Token callToken = require(Token.Kind.CALL);
		Symbol functionNameSymbol = resolveSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		Token openParenToken = require(Token.Kind.OPEN_PAREN);
		ExpressionList argumentExpressions = expression_list(openParenToken.getLineNumber(), openParenToken.getCharPos() + 1);
		require(Token.Kind.CLOSE_PAREN);
		exitRule();
		return new Call(
			callToken.getLineNumber(),
			callToken.getCharPos(),
			functionNameSymbol,
			argumentExpressions);
	}
	
	private Expression expression3() throws RequiredTokenException 
	{
		enterRule(NonTerminal.EXPRESSION3);
		Expression expression = null;
		Token startToken = reader.token();
		if(accept(Token.Kind.NOT))
		{
			expression = new LogicalNot(
				startToken.getLineNumber(),
				startToken.getCharPos(),
				expression3());
		}
		else if(accept(Token.Kind.OPEN_PAREN))
		{
			expression = expression0();
			require(Token.Kind.CLOSE_PAREN);
		}
		else if(firstSetSatisfied(NonTerminal.DESIGNATOR))
			expression = designator(false);
		else if(firstSetSatisfied(NonTerminal.CALL_EXPRESSION))
			try 
			{
				expression = call_expression();
			} catch (UnresolvableSymbolException e) 
			{
				expression = new Error(
					e.UnresolvedToken.getLineNumber(),
					e.UnresolvedToken.getCharPos(),
					String.format("Unable to resolve symbol \"%s\".", e.UnresolvedToken.getLexeme()));
			}
		else if(firstSetSatisfied(NonTerminal.LITERAL))
			expression = literal();
		else
			expression = new Error(
				startToken.getLineNumber(),
				startToken.getCharPos(),
				String.format("First set unsatisfied by token of kind \"%s\".", startToken.getKind()));
		exitRule();
		return expression;
	}
	
	private Expression expression2() throws RequiredTokenException 
	{
		enterRule(NonTerminal.EXPRESSION2);
		Expression lhs = expression3();
		while(firstSetSatisfied(NonTerminal.OP2))
		{
			Token op = op2();
			lhs = Command.newExpression(lhs, op, expression3());
		}
		exitRule();
		return lhs;
	}
	
	private Expression expression1() throws RequiredTokenException 
	{
		enterRule(NonTerminal.EXPRESSION1);
		Expression lhs = expression2();
		while(firstSetSatisfied(NonTerminal.OP1))
		{
			Token op;
			try 
			{
				op = op1();
				lhs = Command.newExpression(lhs, op, expression2());
			} 
			catch (FirstSetUnsatisfiedException e) 
			{
				lhs = new Error(
					((Command)lhs).lineNumber(),
					((Command)lhs).charPosition(),
					String.format("First set unsatisfied for nonterminal of type \"%s\".", e.Unsatisfied));
			}
		}
		exitRule();
		return lhs;
	}
	
	private Expression expression0() throws RequiredTokenException 
	{
		enterRule(NonTerminal.EXPRESSION0);
		Expression lhs = expression1();
		if(firstSetSatisfied(NonTerminal.OP0))
		{
			Token op = null;
			try 
			{
				op = op0();
				lhs = Command.newExpression(lhs, op, expression1());
			} 
			catch (FirstSetUnsatisfiedException e) 
			{
				lhs = new Error(
					((Command)lhs).lineNumber(),
					((Command)lhs).charPosition(),
					String.format("First set unsatisfied for nonterminal of type \"%s\".", e.Unsatisfied));
			}
		}
		exitRule();
		return lhs;
	}
	
	private Token op2()
	{
		enterRule(NonTerminal.OP2);
		Token token = null;
		firstSetSatisfied(NonTerminal.OP2);
		token = emitTerminal(reader.token());
		reader.advance();
		exitRule();
		return token;
	}
	
	private Token op1() throws FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.OP1);
		Token token = null;
		requireFirstSetSatisfied(NonTerminal.OP1);
		token = emitTerminal(reader.token());
		reader.advance();
		exitRule();
		return token;
	}
	
	private Token op0() throws FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.OP0);
		Token token = null;
		requireFirstSetSatisfied(NonTerminal.OP0);
		token = emitTerminal(reader.token());
		reader.advance();		
		exitRule();
		return token;
	}
	
	private Token type() throws RequiredTokenException
	{
		enterRule(NonTerminal.TYPE);
		Token typeToken = emitTerminal(require(Token.Kind.IDENTIFIER));
		exitRule();
		return typeToken;
	}
	
	private Expression designator(boolean acceptAddressOf) throws RequiredTokenException 
	{
		enterRule(NonTerminal.DESIGNATOR);
		Token dereferencedToken = emitTerminal(require(Token.Kind.IDENTIFIER));
		Symbol dereferencedSymbol = null;
		try 
		{
			dereferencedSymbol = resolveSymbol(dereferencedToken);
		}
		catch (UnresolvableSymbolException e) 
		{
			exitRule();
			return new Error(
				dereferencedToken.getLineNumber(),
				dereferencedToken.getCharPos(),
				String.format("Unable to resolve symbol \"%s\".", dereferencedToken.getLexeme()));
		}
		Index prevIndex = null;
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			Expression indexExpression = expression0();
			require(Token.Kind.CLOSE_BRACKET);
			
			if(prevIndex == null)
			{
				prevIndex = new Index(
					((Command)indexExpression).lineNumber(), 
					((Command)indexExpression).charPosition(), 
					new AddressOf(
						dereferencedToken.getLineNumber(),
						dereferencedToken.getCharPos(),
						dereferencedSymbol),
					indexExpression);
			}
			else
			{
				prevIndex = new Index(
					((Command)indexExpression).lineNumber(),
					((Command)indexExpression).charPosition(),
					prevIndex,
					indexExpression);
			}
		}
		exitRule();
		if(acceptAddressOf && prevIndex == null)
		{
			return new AddressOf(
				dereferencedToken.getLineNumber(),
				dereferencedToken.getCharPos(),
				dereferencedSymbol);
		}
		return new Dereference(
			dereferencedToken.getLineNumber(),
			dereferencedToken.getCharPos(),
			prevIndex != null
				? prevIndex
				: new AddressOf(
					dereferencedToken.getLineNumber(),
					dereferencedToken.getCharPos(),
					dereferencedSymbol));
	}
	
	private Expression literal()
	{
		enterRule(NonTerminal.LITERAL);
		Expression literal = firstSetSatisfied(NonTerminal.LITERAL)
			? Command.newLiteral(emitTerminal(reader.token()))
			: new Error(
					reader.token().getLineNumber(),
					reader.token().getCharPos(),
					String.format("First set unsatisfied by token of kind \"%s\".", reader.token().getKind()));
		reader.advance();
		exitRule();
		return literal;
	}
	
	private Token emitTerminal(Token terminal)
	{
		if(root == null)
		{
			root = current = new ParseNode(null, terminal);
			return terminal;
		}
		ParseNode newNode = new ParseNode(null, terminal);
		newNode.Parent = current;
		if(current.FirstChild == null)
			current.FirstChild = newNode;
		else
		{
			ParseNode lastChild = current.FirstChild;
			while(lastChild.Sibling != null)
				lastChild = lastChild.Sibling;
			lastChild.Sibling = newNode;
		}
		return terminal;
	}
			
	private void enterRule(NonTerminal productionRule)
	{
		if(root == null)
		{
			root = current = new ParseNode(productionRule, null);
			return;
		}
		ParseNode newNode = new ParseNode(productionRule, null);
		newNode.Parent = current;
		if(current.FirstChild == null)
			current.FirstChild = newNode;
		else
		{
			ParseNode lastChild = current.FirstChild;
			while(lastChild.Sibling != null)
				lastChild = lastChild.Sibling;
			lastChild.Sibling = newNode;
		}
		current = newNode;
	}
	
	private void exitRule()
	{
		current = current.Parent;
	}
		
	private boolean firstSetSatisfied(NonTerminal nonTerminal)
	{
		return nonTerminal.FirstSet.contains(reader.kind());
	}
	
	private void requireFirstSetSatisfied(NonTerminal nonTerminal) throws FirstSetUnsatisfiedException
	{
		if(!firstSetSatisfied(nonTerminal))
			throw new FirstSetUnsatisfiedException(nonTerminal);
	}
	
	private Token require(Token.Kind kind) throws RequiredTokenException
	{
		if(reader.kind().equals(kind))
		{
			Token token = reader.token();
			reader.advance();
			return token;
		}
		else
			throw new RequiredTokenException(kind, reader.token());
	}
	
	private boolean accept(Token.Kind kind)
	{
		if(reader.kind().equals(kind))
		{
			reader.advance();
			return true;
		}
		return false;
	}
	
	private Symbol resolveSymbol(Token token) throws UnresolvableSymbolException
	{
		if(symbolTables.peek().containsSymbol(token.getLexeme(), true))
			return symbolTables.peek().lookup(token.getLexeme());
		errorReport.append(String.format("ResolveSymbolError(%d,%d)[Could not find %s.]\n", token.getLineNumber(), token.getCharPos(), token.getLexeme()));
		appendSymbolHistory(errorReport, symbolTables.peek());
		errorReport.append("\n");
		throw new UnresolvableSymbolException(token);
	}
	
	private Symbol defineSymbol(Token token, Type type) throws SymbolRedefinitionException
	{
		// Define the symbol.
		if(symbolTables.peek().containsSymbol(token.getLexeme(), false))
		{
			errorReport.append(String.format("DeclareSymbolError(%d,%d)[%s already exists.]\n", token.getLineNumber(), token.getCharPos(), token.getLexeme()));
			appendSymbolHistory(errorReport, symbolTables.peek());
			errorReport.append("\n");
			throw new SymbolRedefinitionException(token);
		}
		else
		{
			return symbolTables.peek().define(token.getLexeme(), token.getLineNumber(), token.getCharPos(), type);
		}
	}
	
	private int appendSymbolHistory(StringBuilder b, SymbolTable current)
	{
		if(current == null)
			return 0;
		int indent = appendSymbolHistory(b, current.getParent());
		for(Symbol s : current.getDeclarationOrder())
		{
			// Two spaces per indent.
			for(int i = 0; i < indent; i++)
				b.append("  ");
			b.append(String.format("Symbol(%s)\n", s.getName()));
		}
		return indent + 1;
	}
	
	private void enterScope()
	{
		symbolTables.push(new SymbolTable(symbolTables.peek()));
	}
	
	private void exitScope()
	{
		symbolTables.pop();
	}
}