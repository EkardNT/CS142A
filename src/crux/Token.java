package crux;

public class Token 
{
	public static enum Kind
	{
		ERROR,
		EOF,
		AND,
		OR,
		NOT,
		LET,
		VAR,
		ARRAY,
		FUNC,
		IF,
		ELSE,
		WHILE,
		TRUE,
		FALSE,
		RETURN,
		OPEN_PAREN,
		CLOSE_PAREN,
		OPEN_BRACE,
		CLOSE_BRACE,
		OPEN_BRACKET,
		CLOSE_BRACKET,
		ADD,
		SUB,
		MUL,
		DIV,
		GREATER_EQUAL,
		LESSER_EQUAL,
		NOT_EQUAL,
		EQUAL,
		GREATER_THAN,
		LESS_THAN,
		ASSIGN,
		COMMA,
		SEMICOLON,
		COLON,
		CALL,
		INTEGER,
		FLOAT,
		IDENTIFIER
	}
	
	private final Kind kind;
	private final String lexeme;
	private final int lineNumber, charPos;
	
	public Token(Kind kind, String lexeme, int lineNumber, int charPos)
	{
		this.kind = kind;
		this.lexeme = lexeme;
		this.lineNumber = lineNumber;
		this.charPos = charPos;
	}
	
	public Kind getKind() { return kind; }
	
	public String getLexeme() { return lexeme; }
	
	public int getLineNumber() { return lineNumber; }
	
	public int getCharPos() { return charPos; }
}