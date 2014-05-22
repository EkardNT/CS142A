package crux;

import types.Type;

public class Symbol
{
	private final String identifier;
	private final int lineNumber, charPosition;
	private Type type;
	
	public Symbol(String name, int lineNumber, int charPosition, Type type)
	{
		this.identifier = name;
		this.lineNumber = lineNumber;
		this.charPosition = charPosition;
		this.type = type;
	}
	
	public String getName() { return identifier; }
	public int getLineNumber() { return lineNumber; }
	public int getCharPosition() { return charPosition; }
	public Type getType() { return type; }
	
	@Override
	public String toString()
	{
		return "Symbol(" + identifier + ":" + type + ")";
	}
}
