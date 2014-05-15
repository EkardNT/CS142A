package crux.ast;

public class Addition extends Command implements Expression {

	public Addition(int lineNumber, int charPosition) {
		super(lineNumber, charPosition);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(Visitor visitor) {
		// TODO Auto-generated method stub

	}

}
