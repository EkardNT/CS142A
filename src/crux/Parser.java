package crux;

import java.util.Stack;

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
		rootTable.define("readInt");
		rootTable.define("readFloat");
		rootTable.define("printBool");
		rootTable.define("printInt");
		rootTable.define("printFloat");
		rootTable.define("println");
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
	
	public void parse()
	{
		try
		{
			program();
		}
		catch(RequiredTokenException e)
		{
			errorReport.append(String.format("SyntaxError(%d,%d)[Expected %s but got %s.]", e.ActualToken.getLineNumber(), e.ActualToken.getCharPos(), e.ExpectedKind, e.ActualToken.getKind()));
			errorReport.append(String.format("SyntaxError(%d,%d)[Could not complete parsing.]", e.ActualToken.getLineNumber(), e.ActualToken.getCharPos()));
		}
		catch(FirstSetUnsatisfiedException e)
		{
			errorReport.append(String.format("First set unsatisfied for non-terminal of kind \"%s\".", e.Unsatisfied.toString()));
		}
	}
	
	private void program() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.PROGRAM);
		declaration_list();
		exitRule();
	}
	
	private void statement_block(boolean suppressNewScope) throws RequiredTokenException, FirstSetUnsatisfiedException
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
	
	private void statement_list() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.STATEMENT_LIST);
		while(firstSetSatisfied(NonTerminal.STATEMENT))
			statement();
		exitRule();
	}
	
	private void statement() throws FirstSetUnsatisfiedException, RequiredTokenException
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
	
	private void return_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.RETURN_STATEMENT);
		require(Token.Kind.RETURN);
		expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void while_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.WHILE_STATEMENT);
		require(Token.Kind.WHILE);
		expression0();
		statement_block(false);
		exitRule();
	}
	
	private void if_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.IF_STATEMENT);
		require(Token.Kind.IF);
		expression0();
		statement_block(false);
		if(accept(Token.Kind.ELSE))
			statement_block(false);
		exitRule(); 
	}
	
	private void call_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.CALL_STATEMENT);
		call_expression();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void assignment_statement() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
		require(Token.Kind.LET);
		designator();
		require(Token.Kind.ASSIGN);
		expression0();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void declaration_list() throws FirstSetUnsatisfiedException, RequiredTokenException
	{
		enterRule(NonTerminal.DECLARATION_LIST);
		while(firstSetSatisfied(NonTerminal.DECLARATION))
			declaration();
		require(Token.Kind.EOF);
		exitRule();
	}
	
	private void declaration() throws FirstSetUnsatisfiedException, RequiredTokenException
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
	
	private void function_definition() throws RequiredTokenException, FirstSetUnsatisfiedException
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
	
	private void array_declaration() throws RequiredTokenException
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
	
	private void variable_declaration() throws RequiredTokenException
	{
		enterRule(NonTerminal.VARIABLE_DECLARATION);
		require(Token.Kind.VAR);
		defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		type();
		require(Token.Kind.SEMICOLON);
		exitRule();
	}
	
	private void parameter_list() throws RequiredTokenException
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
	
	private void parameter() throws RequiredTokenException
	{
		enterRule(NonTerminal.PARAMETER);
		defineSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.COLON);
		type();
		exitRule();
	}
	
	private void expression_list() throws FirstSetUnsatisfiedException, RequiredTokenException
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
	
	private void call_expression() throws RequiredTokenException, FirstSetUnsatisfiedException
	{
		enterRule(NonTerminal.CALL_EXPRESSION);
		require(Token.Kind.CALL);
		resolveSymbol(emitTerminal(require(Token.Kind.IDENTIFIER)));
		require(Token.Kind.OPEN_PAREN);
		expression_list();
		require(Token.Kind.CLOSE_PAREN);
		exitRule();
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
	
	private void expression0() throws FirstSetUnsatisfiedException, RequiredTokenException
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
	
	private void type() throws RequiredTokenException
	{
		enterRule(NonTerminal.TYPE);
		emitTerminal(require(Token.Kind.IDENTIFIER));
		exitRule();
	}
	
	private void designator() throws RequiredTokenException, FirstSetUnsatisfiedException
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
	
	private void resolveSymbol(Token token)
	{
		if(symbolTables.peek().containsSymbol(token.getLexeme(), true))
			return;
		errorReport.append(String.format("ResolveSymbolError(%d,%d)[Could not find %s.]\n", token.getLineNumber(), token.getCharPos(), token.getLexeme()));
		appendSymbolHistory(errorReport, symbolTables.peek());
		errorReport.append("\n");
	}
	
	private void defineSymbol(Token token)
	{
		// Define the symbol.
		if(symbolTables.peek().containsSymbol(token.getLexeme(), false))
		{
			errorReport.append(String.format("DeclareSymbolError(%d,%d)[%s already exists.]\n", token.getLineNumber(), token.getCharPos(), token.getLexeme()));
			appendSymbolHistory(errorReport, symbolTables.peek());
			errorReport.append("\n");
		}
		else
		{
			symbolTables.peek().define(token.getLexeme());
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