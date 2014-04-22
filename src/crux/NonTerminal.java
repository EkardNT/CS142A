package crux;

import java.util.HashSet;
import java.util.Set;

import crux.Token.Kind;

public enum NonTerminal 
{
	LITERAL(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.INTEGER);
			add(Token.Kind.FLOAT);
			add(Token.Kind.TRUE);
			add(Token.Kind.FALSE);
		}
	}),
	DESIGNATOR(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.IDENTIFIER);
		}
	}),
	TYPE(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.IDENTIFIER);
		}
	}),
	OP0(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.GREATER_EQUAL);
			add(Token.Kind.LESSER_EQUAL);
			add(Token.Kind.NOT_EQUAL);
			add(Token.Kind.EQUAL);
			add(Token.Kind.GREATER_THAN);
			add(Token.Kind.LESS_THAN);
		}
	}),
	OP1(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.ADD);
			add(Token.Kind.SUB);
			add(Token.Kind.OR);
		}
	}),
	OP2(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.MUL);
			add(Token.Kind.DIV);
			add(Token.Kind.AND);
		}
	}),
	CALL_EXPRESSION(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.CALL);
		}
	}),
	EXPRESSION3(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.NOT);
			add(Token.Kind.OPEN_PAREN);
			addAll(DESIGNATOR.FirstSet);
			addAll(CALL_EXPRESSION.FirstSet);
			addAll(LITERAL.FirstSet);
		}
	}),
	EXPRESSION2(new HashSet<Token.Kind>() {
		{
			addAll(EXPRESSION3.FirstSet);
		}
	}),
	EXPRESSION1(new HashSet<Token.Kind>() {
		{
			addAll(EXPRESSION2.FirstSet);
		}
	}),
	EXPRESSION0(new HashSet<Token.Kind>() {
		{
			addAll(EXPRESSION1.FirstSet);
		}
	}),
	EXPRESSION_LIST(new HashSet<Token.Kind>() {
		{
			addAll(EXPRESSION0.FirstSet);
		}
	}),
	PARAMETER(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.IDENTIFIER);
		}
	}),
	PARAMETER_LIST(new HashSet<Token.Kind>() {
		{
			addAll(PARAMETER.FirstSet);
		}
	}),
	VARIABLE_DECLARATION(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.VAR);
		}
	}),
	ARRAY_DECLARATION(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.ARRAY);
		}
	}),
	FUNCTION_DEFINITION(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.FUNC);
		}
	}),
	DECLARATION(new HashSet<Token.Kind>() {
		{
			addAll(VARIABLE_DECLARATION.FirstSet);
			addAll(ARRAY_DECLARATION.FirstSet);
			addAll(FUNCTION_DEFINITION.FirstSet);	
		}
	}),
	DECLARATION_LIST(new HashSet<Token.Kind>() {
		{
			addAll(DECLARATION.FirstSet);
		}
	}),
	ASSIGNMENT_STATEMENT(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.LET);
		}
	}),
	CALL_STATEMENT(new HashSet<Token.Kind>() {
		{
			addAll(CALL_EXPRESSION.FirstSet);
		}
	}),
	IF_STATEMENT(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.IF);
		}
	}),
	WHILE_STATEMENT(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.WHILE);
		}
	}),
	RETURN_STATEMENT(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.RETURN);
		}
	}),
	STATEMENT(new HashSet<Token.Kind>() {
		{
			addAll(VARIABLE_DECLARATION.FirstSet);
			addAll(CALL_STATEMENT.FirstSet);
			addAll(ASSIGNMENT_STATEMENT.FirstSet);
			addAll(IF_STATEMENT.FirstSet);
			addAll(WHILE_STATEMENT.FirstSet);
			addAll(RETURN_STATEMENT.FirstSet);
		}
	}),
	STATEMENT_LIST(new HashSet<Token.Kind>() {
		{
			addAll(STATEMENT.FirstSet);
		}
	}),
	STATEMENT_BLOCK(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.OPEN_BRACE);
		}
	}),
	PROGRAM(new HashSet<Token.Kind>() {
		{
			add(Token.Kind.EOF);
			addAll(DECLARATION_LIST.FirstSet);
		}
	});	
	
	public Set<Kind> FirstSet;
	
	private NonTerminal(Set<Kind> firstSet)
	{
		FirstSet = firstSet;
	}
}