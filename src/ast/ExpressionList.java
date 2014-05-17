package ast;

import java.util.Iterator;
import java.util.List;

public class ExpressionList extends Command implements Iterable<Expression> {
	
	private List<Expression> list;
	
	public ExpressionList(int lineNum, int charPos, List<Expression> expressions)
	{
		super(lineNum, charPos);
		list = expressions;
	}
		
	public int size()
	{
		return list.size();
	}

	@Override
	public Iterator<Expression> iterator() {
		return list.iterator();
	}

	@Override
	public void accept(CommandVisitor visitor) {
		visitor.visit(this);
	}
}
