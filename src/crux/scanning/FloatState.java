package crux.scanning;

import crux.Token.Kind;

public class FloatState implements State
{
	private static final FloatState inst = new FloatState();
	public static FloatState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isEof() || context.isWhitespace())
		{
			context.emit(Kind.FLOAT);
			return StartState.instance();
		}
		if(context.isDigit())
		{
			context.accumulate();
			return this;
		}
		if(context.isSymbol())
		{
			context.emit(Kind.FLOAT);
			context.accumulate();
			return SymbolState.instance();
		}
		context.accumulate();
		context.emit(Kind.ERROR);
		return StartState.instance();
	}		
}