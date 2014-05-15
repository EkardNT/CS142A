package crux.ast;

public class FunctionDeclaration extends Command implements Declaration {

	public FunctionDeclaration(int lineNumber, int charPosition) {
		super(lineNumber, charPosition);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(Visitor visitor) {
		// TODO Auto-generated method stub

	}

}
