package crux.scanning;

import crux.Token.Kind;

public class NumberState implements State
{
	private static final NumberState inst = new NumberState();
	public static NumberState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isDigit())
		{
			context.pushChar();
			return this;
		}
		if(context.isDecimalSeparator())
		{
			context.pushChar();
			return FloatState.instance();
		}
		context.emit(Kind.INTEGER);
		return StartState.instance().transition(context);
	}
}