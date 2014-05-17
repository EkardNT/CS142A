package ast;

import java.util.Iterator;
import java.util.List;

public class DeclarationList extends Command implements Iterable<Declaration> {
	
	private List<Declaration> list;
	
	public DeclarationList(int lineNum, int charPos, List<Declaration> list)
	{
		super(lineNum, charPos);
		this.list = list;
	}

	@Override
	public Iterator<Declaration> iterator() {
		return list.iterator();
	}

	@Override
	public void accept(CommandVisitor visitor) {
		visitor.visit(this);
	}
}
