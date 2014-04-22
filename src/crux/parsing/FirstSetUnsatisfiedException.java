package crux.parsing;

import crux.NonTerminal;

public class FirstSetUnsatisfiedException extends Exception {
	public final NonTerminal Unsatisfied;
	
	public FirstSetUnsatisfiedException(NonTerminal unsatisfied)
	{
		Unsatisfied = unsatisfied;
	}
}
