package crux.ast;

public class Error extends Command implements Declaration, Expression, Statement {

	public Error(int lineNumber, int charPosition) {
		super(lineNumber, charPosition);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(Visitor visitor) {
		// TODO Auto-generated method stub

	}

}
