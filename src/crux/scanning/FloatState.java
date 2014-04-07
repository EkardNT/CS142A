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
		// One of the test cases specifically calls out a second '.' as an error,
		// even though it could be interpreted as the start of a new float.
		if(context.value() == '.')
		{
			context.emit(Kind.FLOAT);
			context.pushChar();
			context.emit(Kind.ERROR);
			return StartState.instance();
		}
		context.emit(Kind.FLOAT);
		return StartState.instance().transition(context);
	}
}