package crux.scanning;

import crux.Token.Kind;

public class NumberState implements State
{
	private static final NumberState inst = new NumberState();
	public static NumberState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isEof() || context.isWhitespace())
		{
			context.emit(Kind.INTEGER);
			return StartState.instance();
		}
		if(context.isDigit())
		{
			context.accumulate();
			return this;
		}
		if(context.isDecimalSeparator())
		{
			context.accumulate();
			return FloatState.instance();
		}
		if(context.isSymbol())
		{
			context.emit(Kind.INTEGER);
			context.accumulate();
			return SymbolState.instance();
		}
		context.accumulate();
		context.emit(Kind.ERROR);
		return StartState.instance();
	}
}