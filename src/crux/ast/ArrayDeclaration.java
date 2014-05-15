package crux.ast;

public class ArrayDeclaration extends Command implements Declaration
{
	public ArrayDeclaration(int lineNumber, int charPosition) 
	{
		super(lineNumber, charPosition);
	}

	@Override
	public void accept(Visitor visitor) 
	{
		
	}	
}
