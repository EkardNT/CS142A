package crux.ast;

public class Assignment extends Command implements Statement {

	public Assignment(int lineNumber, int charPosition) {
		super(lineNumber, charPosition);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(Visitor visitor) {
		// TODO Auto-generated method stub

	}

}
