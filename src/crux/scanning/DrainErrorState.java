package crux.scanning;

import crux.Token.Kind;

public class DrainErrorState implements State
{
	private static final DrainErrorState inst = new DrainErrorState();
	public static DrainErrorState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isEof() || context.isWhitespace())
		{
			context.emit(Kind.ERROR);
			return StartState.instance();
		}
		context.accumulate();
		return this;
	}

}