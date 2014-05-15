package crux.ast;

public abstract class Command 
{
	private final int lineNumber, charPosition;
	
	public Command(int lineNumber, int charPosition)
	{
		this.lineNumber = lineNumber;
		this.charPosition = charPosition;
	}
	
	public abstract void accept(Visitor visitor);
}