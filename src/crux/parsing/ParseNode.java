package crux.parsing;

import crux.NonTerminal;
import crux.Token;

// A node in the abstract syntax tree produced by the parser.
public class ParseNode
{
	public ParseNode Parent, Sibling, FirstChild;
	public final NonTerminal ProductionRule;
	public final Token Terminal;
	
	public ParseNode(NonTerminal productionRule, Token terminal)
	{
		ProductionRule = productionRule;
		Terminal = terminal;
	}
}
