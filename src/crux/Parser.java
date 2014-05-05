package crux;

import java.util.ArrayList;
import java.util.Stack;

import crux.parsing.FirstSetUnsatisfiedException;
import crux.parsing.LL1Reader;
import crux.parsing.ParseNode;
import crux.parsing.RequiredTokenException;
import crux.symbols.SymbolRedefinitionException;
import crux.symbols.SymbolResolveException;
import crux.symbols.SymbolTable;

public class Parser 
{	
	public static String studentName = "Drake Tetreault";
    public static String studentID = "35571095";
    public static String uciNetID = "dtetreau";
	
	private final LL1Reader reader;
	private final ArrayList<String> errors;
	private final Stack<SymbolTable> symbolTables;
	private ParseNode root, current;
	
	public Parser(Scanner scanner)
	{
		this.reader = new LL1Reader(scanner);
		this.errors = new ArrayList<String>();
		this.symbolTables = new Stack<SymbolTable>();
		root = current = null;
		SymbolTable rootTable = new SymbolTable(null);
		rootTable.define("readInt");
		rootTable.define("readFloat");
		rootTable.define("printBool");
		rootTable.define("printInt");
		rootTable.define("printFloat");
		rootTable.define("println");
		symbolTables.push(rootTable);
	}
	
	public void error(String errorMessage)
	{
		errors.add(errorMessage);
	}
	
	public boolean hasError()
	{
		return errors.size() > 0;
	}
	
	public String errorReport()
	{
		StringBuilder b = new StringBuilder();
		for(String error : errors)
			b.append(String.format("%s\n", error));
		return b.toString();
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
	
	public void parse()
	{
		try
		{
			program();
		}
		catch(SymbolRedefinitionException e)
		{
			error(String.format("Symbol redefinition error: %s\n", e.RedefiningToken.getLexeme()));
		}
		catch(SymbolResolveException e)
		{
			error(String.format("Symbol resolve error: %s\n", e.UnresolvableToken.getLexeme()));
		}
		catch(RequiredTokenException e)
		{
			error(String.format("SyntaxError(%d,%d)[Expected %s but got %s.]", e.ActualToken.getLineNumber(), e.ActualToken.getCharPos(), e.ExpectedKind, e.ActualToken.getKind()));
			error(String.format("SyntaxError(%d,%d)[Could not complete parsing.]", e.ActualToken.getLineNumber(), e.ActualToken.getCharPos()));
		}
		catch(FirstSetUnsatisfiedException e)
		{
			error(String.format("First set unsatisfied for non-terminal of kind \"%s\".\n", e.Unsatisfied.toString()));
		}
	}
	
	private void program() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.PROGRAM);
		declaration_list();
		exitRule();
	}
	
	private void statement_block(boolean suppressNewScope) throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.STATEMENT_BLOCK);
		if(!suppressNewScope)
			enterScope();
		require(Token.Kind.OPEN_BRACE);
		statement_list();
		require(Token.Kind.CLOSE_BRACE);
		if(!suppressNewScope)
			exitScope();
		exitRule();
	}
	
	private void statement_list() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.STATEMENT_LIST);
		while(firstSetSatisfied(NonTerminal.STATEMENT))
			statement();
		exitRule();
	}
	
	private void statement() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.STATEMENT);
		if(firstSetSatisfied(NonTerminal.VARIABLE_DECLARATION))
			variable_declaration();
		else if(firstSetSatisfied(NonTerminal.CALL_STATEMENT))
			call_statement();
		else if(firstSetSatisfied(NonTerminal.ASSIGNMENT_STATEMENT))
			assignment_statement();
		else if(firstSetSatisfied(NonTerminal.IF_STATEMENT))
			if_statement();
		else if(firstSetSatisfied(NonTerminal.WHILE_STATEMENT))
			while_statement();
		else if(firstSetSatisfied(NonTerminal.RETURN_STATEMENT))
			return_statement();
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.STATEMENT);
		exitRule();
	}
	
	private void return_statement() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.RETURN_STATEMENT);
		require(Token.Kind.RETURN);
		expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void while_statement() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.WHILE_STATEMENT);
		require(Token.Kind.WHILE);
		expression0();
		statement_block(false);
		exitRule();
	}
	
	private void if_statement() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.IF_STATEMENT);
		require(Token.Kind.IF);
		expression0();
		statement_block(false);
		if(accept(Token.Kind.ELSE))
			statement_block(false);
		exitRule(); 
	}
	
	private void call_statement() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.CALL_STATEMENT);
		call_expression();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void assignment_statement() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
		require(Token.Kind.LET);
		designator();
		require(Token.Kind.ASSIGN);
		expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void declaration_list() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.DECLARATION_LIST);
		while(firstSetSatisfied(NonTerminal.DECLARATION))
			declaration();
		require(Token.Kind.EOF);
		exitRule();
	}
	
	private void declaration() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.DECLARATION);
		if(firstSetSatisfied(NonTerminal.VARIABLE_DECLARATION))
			variable_declaration();
		else if(firstSetSatisfied(NonTerminal.ARRAY_DECLARATION))
			array_declaration();
		else if(firstSetSatisfied(NonTerminal.FUNCTION_DEFINITION))
			function_definition();
		else
			throw new FirstSetUnsatisfiedException(NonTerminal.DECLARATION);
		exitRule();
	}
	
	private void function_definition() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.FUNCTION_DEFINITION);
		require(Token.Kind.FUNC);
		defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		enterScope();
		require(Token.Kind.OPEN_PAREN);
		parameter_list();
		require(Token.Kind.CLOSE_PAREN);
		require(Token.Kind.COLON);
		type();
		statement_block(true);
		exitScope();
		exitRule();
	}
	
	private void array_declaration() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.ARRAY_DECLARATION);
		require(Token.Kind.ARRAY);
		defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		type();
		require(Token.Kind.OPEN_BRACKET);
		emitTerminal(require(Token.Kind.INTEGER));
		require(Token.Kind.CLOSE_BRACKET);
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			emitTerminal(require(Token.Kind.INTEGER));
			require(Token.Kind.CLOSE_BRACKET);
		}
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void variable_declaration() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.VARIABLE_DECLARATION);
		require(Token.Kind.VAR);
		defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		type();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void parameter_list() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.PARAMETER_LIST);
		if(firstSetSatisfied(NonTerminal.PARAMETER))
		{
			parameter();
			while(accept(Token.Kind.COMMA))
				parameter();
		}
		exitRule();
	}
	
	private void parameter() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.PARAMETER);
		defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		type();
		exitRule();
	}
	
	private void expression_list() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.EXPRESSION_LIST);
		if(firstSetSatisfied(NonTerminal.EXPRESSION0))
		{
			expression0();
			while(accept(Token.Kind.COMMA))
				expression0();
		}
		exitRule();
	}
	
	private void call_expression() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.CALL_EXPRESSION);
		require(Token.Kind.CALL);
		resolveSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.OPEN_PAREN);
		expression_list();
		require(Token.Kind.CLOSE_PAREN);
		exitRule();
	}
	
	private void expression3() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
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
	
	private void expression2() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
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
	
	private void expression1() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
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
	
	private void expression0() throws FirstSetUnsatisfiedException, RequiredTokenException, SymbolRedefinitionException, SymbolResolveException
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
	
	private void op2() throws FirstSetUnsatisfiedException, SymbolRedefinitionException
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
	
	private void op1() throws FirstSetUnsatisfiedException, SymbolRedefinitionException
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
	
	private void op0() throws FirstSetUnsatisfiedException, SymbolRedefinitionException
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
	
	private void type() throws RequiredTokenException, SymbolRedefinitionException
	{
		enterRule(NonTerminal.TYPE);
		emitTerminal(require(Token.Kind.IDENTIFIER));
		exitRule();
	}
	
	private void designator() throws RequiredTokenException, FirstSetUnsatisfiedException, SymbolRedefinitionException, SymbolResolveException
	{
		enterRule(NonTerminal.DESIGNATOR);
		resolveSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		while(accept(Token.Kind.OPEN_BRACKET))
		{
			expression0();
			require(Token.Kind.CLOSE_BRACKET);
		}
		exitRule();
	}
	
	private void literal() throws FirstSetUnsatisfiedException, SymbolRedefinitionException
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
	
	private Token emitTerminal(Token terminal) throws SymbolRedefinitionException
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
	
	private void resolveSymbol(Token token) throws SymbolResolveException
	{
		if(!symbolTables.peek().containsSymbol(token.getLexeme(), true))
			throw new SymbolResolveException(token);
	}
	
	private void defineSymbol(Token token) throws SymbolRedefinitionException
	{
		// Define the symbol.
		if(symbolTables.peek().containsSymbol(token.getLexeme(), false))
			throw new SymbolRedefinitionException(token);
		symbolTables.peek().define(token.getLexeme());
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