package crux.scanning;

import crux.Token.Kind;

public class FloatState implements State
{
	private static final FloatState inst = new FloatState();
	public static FloatState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isDigit())
		{
			context.pushChar();
			return this;
		}
		context.emit(Kind.FLOAT);
		return StartState.instance().transition(context);
	}
}