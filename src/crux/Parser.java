package crux;

import java.util.ArrayList;
import java.util.Stack;

import ast.*;
import crux.parsing.FirstSetUnsatisfiedException;
import crux.parsing.LL1Reader;
import crux.parsing.ParseNode;
import crux.parsing.RequiredTokenException;

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
		rootTable.define("readInt", 0, 0);
		rootTable.define("readFloat", 0, 0);
		rootTable.define("printBool", 0, 0);
		rootTable.define("printInt", 0, 0);
		rootTable.define("printFloat", 0, 0);
		rootTable.define("println", 0, 0);
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
		catch(RequiredTokenException e)
		{
			errorReport.append(String.format("SyntaxError(%d,%d)[Expected %s but got %s.]", e.ActualToken.getLineNumber(), e.ActualToken.getCharPos(), e.ExpectedKind, e.ActualToken.getKind()));
			errorReport.append(String.format("SyntaxError(%d,%d)[Could not complete parsing.]", e.ActualToken.getLineNumber(), e.ActualToken.getCharPos()));
			return new ast.Error(0, 0);
		}
		catch(FirstSetUnsatisfiedException e)
		{
			errorReport.append(String.format("First set unsatisfied for non-terminal of kind \"%s\".", e.Unsatisfied.toString()));
			return new ast.Error(0, 0);
		}
	}
	
	private Command program() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.PROGRAM);
		DeclarationList declarationList = declaration_list();
		exitRule();
		return declarationList;
	}
	
	private StatementList statement_block(boolean suppressNewScope) throws RequiredTokenException, FirstSetUnsatisfiedException
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
	
	private StatementList statement_list(int fallbackLineNumber, int fallbackCharPosition) throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.STATEMENT_LIST);
		ArrayList<Command> statements = new ArrayList<Command>();
		while(firstSetSatisfied(NonTerminal.STATEMENT))
			statements.add(statement());
		exitRule();
		return new StatementList(
			statements.size() > 0 ? statements.get(0).getLineNumber() : fallbackLineNumber,
			statements.size() > 0 ? statements.get(0).getCharPosition() : fallbackCharPosition,
			statements);
	}
	
	private Command statement() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.STATEMENT);
		Command statement = null;
		if(firstSetSatisfied(NonTerminal.VARIABLE_DECLARATION))
			statement = variable_declaration();
		else if(firstSetSatisfied(NonTerminal.CALL_STATEMENT))
			statement = call_statement();
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
			// TODO: return error?
			statement = null;
			throw new FirstSetUnsatisfiedException(NonTerminal.STATEMENT);
		}
		exitRule();
		return statement;
	}
	
	private Return return_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.RETURN_STATEMENT);
		Token returnToken = require(Token.Kind.RETURN);
		Command returnExpression = expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new Return(returnToken.getLineNumber(), returnToken.getCharPos(), returnExpression);
	}
	
	private WhileLoop while_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.WHILE_STATEMENT);
		Token whileToken = require(Token.Kind.WHILE);
		Command continuationExpression = expression0();
		StatementList bodyStatements = statement_block(false);
		exitRule();
		return new WhileLoop(
			whileToken.getLineNumber(),
			whileToken.getCharPos(),
			continuationExpression,
			bodyStatements);
	}
	
	private IfElseBranch if_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.IF_STATEMENT);
		Token ifToken = require(Token.Kind.IF);
		Command conditionalExpression = expression0();
		StatementList ifBodyStatements = statement_block(false);
		StatementList elseBodyStatements = accept(Token.Kind.ELSE) ? statement_block(false) : null;
		exitRule(); 
		return new IfElseBranch(
				ifToken.getLineNumber(), 
				ifToken.getCharPos(), 
				conditionalExpression, 
				ifBodyStatements, 
				elseBodyStatements);
	}
	
	private Call call_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.CALL_STATEMENT);
		Call callExpression = call_expression();
		require(Token.Kind.SEMICOLON);
		exitRule();
		return callExpression;
	}
	
	private Assignment assignment_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
		Token letToken = require(Token.Kind.LET);
		designator();
		require(Token.Kind.ASSIGN);
		expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private DeclarationList declaration_list(int fallbackLineNumber, int fallbackCharPosition) throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.DECLARATION_LIST);
		ArrayList<Command> declarations = new ArrayList<Command>();
		while(firstSetSatisfied(NonTerminal.DECLARATION))
		{
			declarations.add(declaration());			
		}
		require(Token.Kind.EOF);
		exitRule();
		return new DeclarationList(
			declarations.size() > 0 ? declarations.get(0).getLineNumber() : fallbackLineNumber,
			declarations.size() > 0 ? declarations.get(0).getCharPosition() : fallbackCharPosition,
			declarations);
	}
	
	private Command declaration() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.DECLARATION);
		Command declaration = null;
		if(firstSetSatisfied(NonTerminal.VARIABLE_DECLARATION))
			declaration = variable_declaration();
		else if(firstSetSatisfied(NonTerminal.ARRAY_DECLARATION))
			declaration = array_declaration();
		else if(firstSetSatisfied(NonTerminal.FUNCTION_DEFINITION))
			declaration = function_definition();
		else
		{
			// TODO:
			declaration = new ast.Error(0, 0);
			throw new FirstSetUnsatisfiedException(NonTerminal.DECLARATION);
		}
		exitRule();
		return declaration;
	}
	
	private FunctionDeclaration function_definition() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.FUNCTION_DEFINITION);
		Token funcToken = require(Token.Kind.FUNC);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		enterScope();
		require(Token.Kind.OPEN_PAREN);
		ArrayList<Parameter> parameters = parameter_list();
		require(Token.Kind.CLOSE_PAREN);
		require(Token.Kind.COLON);
		Token returnTypeToken = type();
		StatementList bodyStatements = statement_block(true);
		exitScope();
		exitRule();
		return new FunctionDeclaration(
				funcToken.getLineNumber(),
				funcToken.getCharPos(),
				nameSymbol.getName(),
				returnTypeToken.getLexeme(),
				parameters,
				bodyStatements);
	}
	
	private ArrayDeclaration array_declaration() throws RequiredTokenException
	{
		enterRule(NonTerminal.ARRAY_DECLARATION);
		Token arrayToken = require(Token.Kind.ARRAY);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		Token typeToken = type();
		require(Token.Kind.OPEN_BRACKET);
		ArrayList<String> dimensions = new ArrayList<String>();
		dimensions.add(emitTerminal(require(Token.Kind.INTEGER)).getLexeme());
		require(Token.Kind.CLOSE_BRACKET);
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			dimensions.add(emitTerminal(require(Token.Kind.INTEGER)).getLexeme());
			require(Token.Kind.CLOSE_BRACKET);
		}
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new ArrayDeclaration(
				arrayToken.getLineNumber(), 
				arrayToken.getCharPos(),
				nameSymbol.getName(),
				typeToken.getLexeme(),
				dimensions);
	}
	
	private VariableDeclaration variable_declaration() throws RequiredTokenException
	{
		enterRule(NonTerminal.VARIABLE_DECLARATION);
		Token varToken = require(Token.Kind.VAR);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		Token typeToken = type();
		require(Token.Kind.SEMICOLON);
		exitRule();
		return new VariableDeclaration(varToken.getLineNumber(), varToken.getCharPos(), nameSymbol.getName(), typeToken.getLexeme());
	}
	
	private ArrayList<Parameter> parameter_list() throws RequiredTokenException
	{
		enterRule(NonTerminal.PARAMETER_LIST);
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		if(firstSetSatisfied(NonTerminal.PARAMETER))
		{
			parameters.add(parameter());
			while(accept(Token.Kind.COMMA))
				parameters.add(parameter());
		}
		exitRule();
		return parameters;
	}
	
	private Parameter parameter() throws RequiredTokenException
	{
		enterRule(NonTerminal.PARAMETER);
		Symbol nameSymbol = defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		Token typeToken = type();
		exitRule();
		return new Parameter(
			nameSymbol.getLineNumber(),
			nameSymbol.getCharPosition(),
			nameSymbol.getName(),
			typeToken.getLexeme());
	}
	
	private ExpressionList expression_list(int fallbackLineNumber, int fallbackCharPosition) throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.EXPRESSION_LIST);
		ArrayList<Command> expressions = new ArrayList<Command>();
		if(firstSetSatisfied(NonTerminal.EXPRESSION0))
		{
			expressions.add(expression0());
			while(accept(Token.Kind.COMMA))
				expressions.add(expression0());
		}
		exitRule();
		return new ExpressionList(
			expressions.size() > 0 ? expressions.get(0).getLineNumber() : fallbackLineNumber,
			expressions.size() > 0 ? expressions.get(0).getCharPosition() : fallbackCharPosition,
			expressions);
	}
	
	private Call call_expression() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.CALL_EXPRESSION);
		Token callToken = require(Token.Kind.CALL);
		Symbol functionNameSymbol = resolveSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.OPEN_PAREN);
		ExpressionList argumentExpressions = expression_list(callToken.getLineNumber(), callToken.getCharPos());
		require(Token.Kind.CLOSE_PAREN);
		exitRule();
		return new Call(
			callToken.getLineNumber(),
			callToken.getCharPos(),
			functionNameSymbol.getName(),
			argumentExpressions);
	}
	
	private void expression3() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.EXPRESSION3);
		if(accept(Token.Kind.NOT))
			expression3();
		else if(accept(Token.Kind.OPEN_PAREN))
		{
			expression0();
			require(Token.Kind.CLOSE_PAREN);
		}
		else if(firstSetSatisfied(NonTerminal.DESIGNATOR))
			designator();
		else if(firstSetSatisfied(NonTerminal.CALL_EXPRESSION))
			call_expression();
		else if(firstSetSatisfied(NonTerminal.LITERAL))
			literal();
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.EXPRESSION3);
		exitRule();
	}
	
	private void expression2() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.EXPRESSION2);
		expression3();
		while(firstSetSatisfied(NonTerminal.OP2))
		{
			op2();
			expression3();
		}
		exitRule();
	}
	
	private void expression1() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.EXPRESSION1);
		expression2();
		while(firstSetSatisfied(NonTerminal.OP1))
		{
			op1();
			expression2();
		}
		exitRule();
	}
	
	private Command expression0() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.EXPRESSION0);
		expression1();
		if(firstSetSatisfied(NonTerminal.OP0))
		{
			op0();
			expression1();
		}
		exitRule();
	}
	
	private void op2() throws FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.OP2);
		if(firstSetSatisfied(NonTerminal.OP2))
		{
			emitTerminal(reader.token());
			reader.advance();
		}
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.OP2);
		exitRule();
	}
	
	private void op1() throws FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.OP1);
		if(firstSetSatisfied(NonTerminal.OP1))
		{
			emitTerminal(reader.token());
			reader.advance();
		}
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.OP1);
		exitRule();
	}
	
	private void op0() throws FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.OP0);
		if(firstSetSatisfied(NonTerminal.OP0))
		{
			emitTerminal(reader.token());
			reader.advance();
		}
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.OP0);
		exitRule();
	}
	
	private Token type() throws RequiredTokenException
	{
		enterRule(NonTerminal.TYPE);
		Token typeToken = emitTerminal(require(Token.Kind.IDENTIFIER));
		exitRule();
		return typeToken;
	}
	
	private Dereference designator() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.DESIGNATOR);
		Symbol dereferencedSymbol = resolveSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			expression0();
			require(Token.Kind.CLOSE_BRACKET);
		}
		exitRule();
	}
	
	private void literal() throws FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.LITERAL);
		if(firstSetSatisfied(NonTerminal.LITERAL))
		{
			emitTerminal(reader.token());
			reader.advance();
		}
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.LITERAL);
		exitRule();
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
	
	private Symbol resolveSymbol(Token token)
	{
		if(symbolTables.peek().containsSymbol(token.getLexeme(), true))
			return symbolTables.peek().lookup(token.getLexeme());
		errorReport.append(String.format("ResolveSymbolError(%d,%d)[Could not find %s.]\n", token.getLineNumber(), token.getCharPos(), token.getLexeme()));
		appendSymbolHistory(errorReport, symbolTables.peek());
		errorReport.append("\n");
		throw new RuntimeException("Failed to resolve symbol: " + token.getLexeme());
	}
	
	private Symbol defineSymbol(Token token)
	{
		// Define the symbol.
		if(symbolTables.peek().containsSymbol(token.getLexeme(), false))
		{
			errorReport.append(String.format("DeclareSymbolError(%d,%d)[%s already exists.]\n", token.getLineNumber(), token.getCharPos(), token.getLexeme()));
			appendSymbolHistory(errorReport, symbolTables.peek());
			errorReport.append("\n");
			return symbolTables.peek().lookup(token.getLexeme());
		}
		else
		{
			return symbolTables.peek().define(token.getLexeme(), token.getLineNumber(), token.getCharPos());
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