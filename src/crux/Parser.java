package crux;

import java.util.ArrayList;

import crux.parsing.LL1Reader;

public class Parser 
{
	private static class ParserState
	{
		private final LL1Reader reader;
		private final ArrayList<String> errors;
		
		public ParserState(LL1Reader reader)
		{
			this.reader = reader;
			this.errors = new ArrayList<String>();
		}
		
		public boolean accept(Token.Kind tokenKind)
		{
			if(reader.kind().equals(tokenKind))
			{
				reader.advance();
				return true;
			}
			return false;
		}
		
		public boolean require(Token.Kind tokenKind)
		{
			if(reader.kind().equals(tokenKind))
			{
				reader.advance();
				return true;
			}
			return error(String.format("Required Token.Kind \"%s\" not found, \"%s\" found instead.", tokenKind, reader.kind()));
		}
		
		public Token.Kind tokenKind() { return reader.kind(); }
		
		public boolean error(String errorMessage)
		{
			errors.add(errorMessage);
			return false;
		}
		
		public Iterable<String> errors() { return errors; }
	}
	
	public void parse(LL1Reader reader)
	{
		ParserState state = new ParserState(reader);
		if(program(state))
		{
			System.out.println("Program recognized.");
		}
		else
		{
			System.out.println("Program not recognized.");
			for(String error : state.errors())
				System.out.printf("\t%s\n", error);
		}
	}
	
	private boolean program(ParserState state)
	{
		System.out.println("program");
		declaration_list(state);
		return state.accept(Token.Kind.EOF);
	}
	
	private boolean statement_block(ParserState state)
	{
		System.out.println("statement_block");
		return state.require(Token.Kind.OPEN_BRACE)
			&& statement_list(state)
			&& state.require(Token.Kind.CLOSE_BRACE);
	}
	
	private boolean statement_list(ParserState state)
	{
		System.out.println("statement_list");
		while(statement(state)) {}
		return true;
	}
	
	private boolean statement(ParserState state)
	{
		System.out.println("statement");
		switch(state.tokenKind())
		{
			case VAR: return variable_declaration(state);
			case CALL: return call_statement(state);
			case LET: return assignment_statement(state);
			case IF: return if_statement(state);
			case WHILE: return while_statement(state);
			case RETURN: return return_statement(state);
			default: return state.error(String.format("Unexpected token \"%s\" encountered in statement.", state.tokenKind()));
		}
	}
	
	private boolean return_statement(ParserState state)
	{
		System.out.println("return_statement");
		return state.require(Token.Kind.RETURN)
			&& expression0(state)
			&& state.require(Token.Kind.SEMICOLON);
	}
	
	private boolean while_statement(ParserState state)
	{
		System.out.println("while_statement");
		return state.require(Token.Kind.WHILE)
			&& expression0(state)
			&& statement_block(state);
	}
	
	private boolean if_statement(ParserState state)
	{
		System.out.println("if_statement");
		return state.require(Token.Kind.IF)
			&& expression0(state)
			&& statement_block(state)
			&& (state.accept(Token.Kind.ELSE)? statement_block(state) : true); 
	}
	
	private boolean call_statement(ParserState state)
	{
		System.out.println("call_statement");
		return call_expression(state) 
			&& state.require(Token.Kind.SEMICOLON);
	}
	
	private boolean assignment_statement(ParserState state)
	{
		System.out.println("assignment_statement");
		return state.require(Token.Kind.LET)
			&& designator(state)
			&& state.require(Token.Kind.ASSIGN)
			&& expression0(state)
			&& state.require(Token.Kind.SEMICOLON);
	}
	
	private boolean declaration_list(ParserState state)
	{
		System.out.println("declaration_list");
		while(declaration(state)) {}
		return true;
	}
	
	private boolean declaration(ParserState state)
	{
		System.out.println("declaration");
		switch(state.tokenKind())
		{
			case VAR: return variable_declaration(state);
			case ARRAY: return array_declaration(state);
			case FUNC: return function_definition(state);
			default: return state.error(String.format("Unexpected token \"%s\" encountered in declaration.", state.tokenKind()));
		}
	}
	
	private boolean function_definition(ParserState state)
	{
		System.out.println("function_definition");
		return state.require(Token.Kind.FUNC)
			&& state.require(Token.Kind.IDENTIFIER)
			&& state.require(Token.Kind.OPEN_PAREN)
			&& parameter_list(state)
			&& state.require(Token.Kind.CLOSE_PAREN)
			&& state.require(Token.Kind.COLON)
			&& type(state)
			&& statement_block(state);
	}
	
	private boolean array_declaration(ParserState state)
	{
		System.out.println("array_declaration");
		if(!state.require(Token.Kind.ARRAY)
			|| !state.require(Token.Kind.IDENTIFIER)
			|| !state.require(Token.Kind.COLON)
			|| !type(state)
			|| !state.require(Token.Kind.OPEN_BRACKET)
			|| !state.require(Token.Kind.INTEGER)
			|| !state.require(Token.Kind.CLOSE_BRACKET))
			return false;
		while(state.accept(Token.Kind.OPEN_BRACKET))
		{
			if(!state.require(Token.Kind.INTEGER)
				|| !state.require(Token.Kind.CLOSE_BRACKET))
				return false;
		}
		return state.require(Token.Kind.SEMICOLON);
	}
	
	private boolean variable_declaration(ParserState state)
	{
		System.out.println("variable_declaration");
		return state.require(Token.Kind.VAR)
			&& state.require(Token.Kind.IDENTIFIER)
			&& state.require(Token.Kind.COLON)
			&& type(state)
			&& state.require(Token.Kind.SEMICOLON);
	}
	
	private boolean parameter_list(ParserState state)
	{
		System.out.println("parameter_list");
		if(parameter(state))
		{
			while(state.accept(Token.Kind.COMMA))
			{
				if(!parameter(state))
					return false;
			}
		}
		return true;
	}
	
	private boolean parameter(ParserState state)
	{
		System.out.println("parameter");
		return state.require(Token.Kind.IDENTIFIER)
			&& state.require(Token.Kind.COLON)
			&& type(state);
	}
	
	private boolean expression_list(ParserState state)
	{
		System.out.println("expression_list");
		if(expression0(state))
		{
			while(state.accept(Token.Kind.COMMA))
			{
				if(!expression0(state))
					return false;
			}
		}
		return false;
	}
	
	private boolean call_expression(ParserState state)
	{
		System.out.println("call_expression");
		return state.require(Token.Kind.CALL)
			&& state.require(Token.Kind.IDENTIFIER)
			&& state.require(Token.Kind.OPEN_PAREN)
			&& expression_list(state)
			&& state.require(Token.Kind.CLOSE_PAREN);
	}
	
	private boolean expression3(ParserState state)
	{
		System.out.println("expression3");
		switch(state.tokenKind())
		{
			case NOT: return state.require(Token.Kind.NOT) && expression3(state);
			case OPEN_PAREN: return state.require(Token.Kind.OPEN_PAREN) && expression0(state) && state.require(Token.Kind.CLOSE_PAREN);
			case IDENTIFIER: return designator(state);
			case CALL: return call_expression(state);
			case INTEGER:
			case FLOAT:
			case TRUE:
			case FALSE:
				return literal(state);
			default: return state.error(String.format("Unexpected token \"%s\" encountered in expression3.", state.tokenKind()));
		}
	}
	
	private boolean expression2(ParserState state)
	{
		System.out.println("expression2");
		if(!expression3(state))
			return false;
		while(op2(state))
			if(!expression3(state))
				return false;
		return true;
	}
	
	private boolean expression1(ParserState state)
	{
		System.out.println("expression1");
		if(!expression2(state))
			return false;
		while(op1(state))
			if(!expression2(state))
				return false;
		return true;
	}
	
	private boolean expression0(ParserState state)
	{
		System.out.println("expression0");
		if(!expression1(state))
			return false;
		if(op0(state) && !expression1(state))
			return false;
		return true;
	}
	
	private boolean op2(ParserState state)
	{
		System.out.println("op2");
		return state.accept(Token.Kind.MUL)
			|| state.accept(Token.Kind.DIV)
			|| state.accept(Token.Kind.AND)
			|| state.error(String.format("Unexpected token \"%s\" encountered in op2.", state.tokenKind()));
	}
	
	private boolean op1(ParserState state)
	{
		System.out.println("op1");
		return state.accept(Token.Kind.ADD)
			|| state.accept(Token.Kind.SUB)
			|| state.accept(Token.Kind.OR)
			|| state.error(String.format("Unexpected token \"%s\" encountered in op1.", state.tokenKind()));
	}
	
	private boolean op0(ParserState state)
	{
		System.out.println("op0");
		return state.accept(Token.Kind.GREATER_EQUAL)
			|| state.accept(Token.Kind.LESSER_EQUAL)
			|| state.accept(Token.Kind.NOT_EQUAL)
			|| state.accept(Token.Kind.EQUAL)
			|| state.accept(Token.Kind.GREATER_THAN)
			|| state.accept(Token.Kind.LESS_THAN)
			|| state.error(String.format("Unexpected token \"%s\" encountered in op0.", state.tokenKind()));
	}
	
	private boolean type(ParserState state)
	{
		System.out.println("type");
		return state.require(Token.Kind.IDENTIFIER);
	}
	
	private boolean designator(ParserState state)
	{
		System.out.println("designator");
		if(!state.require(Token.Kind.IDENTIFIER))
			return false;
		while(state.accept(Token.Kind.OPEN_BRACKET))
			if(!expression0(state) || !state.require(Token.Kind.CLOSE_BRACKET))
				return false;
		return true;
	}
	
	private boolean literal(ParserState state)
	{
		System.out.println("literal");
		return state.accept(Token.Kind.INTEGER)
			|| state.accept(Token.Kind.FLOAT)
			|| state.accept(Token.Kind.TRUE)
			|| state.accept(Token.Kind.FALSE)
			|| state.error(String.format("Unexpected token \"%s\" encountered in literal.", state.tokenKind()));
	}
}