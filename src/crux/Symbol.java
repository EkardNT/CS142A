package crux;

public class Symbol
{
	private final String identifier;
	private final int lineNumber, charPosition;
	
	public Symbol(String name, int lineNumber, int charPosition)
	{
		this.identifier = name;
		this.lineNumber = lineNumber;
		this.charPosition = charPosition;
	}
	
	public String getName() { return identifier; }
	public int getLineNumber() { return lineNumber; }
	public int getCharPosition() { return charPosition; }
}
