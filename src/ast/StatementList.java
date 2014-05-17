package ast;

import java.util.Iterator;
import java.util.List;

public class StatementList extends Command implements Iterable<Statement> {
	
	private List<Statement> list;
	
	public StatementList(int lineNum, int charPos, List<Statement> list)
	{
		super(lineNum, charPos);
		this.list = list;
	}

	@Override
	public Iterator<Statement> iterator() {
		return list.iterator();
	}

	@Override
	public void accept(CommandVisitor visitor) {
		visitor.visit(this);
	}
}
