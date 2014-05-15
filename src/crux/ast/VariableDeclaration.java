package crux.ast;

public class VariableDeclaration extends Command implements Declaration, Statement
{
	public VariableDeclaration(int lineNumber, int charPosition) 
	{
		super(lineNumber, charPosition);
	}

	@Override
	public void accept(Visitor visitor) 
	{
		
	}
}