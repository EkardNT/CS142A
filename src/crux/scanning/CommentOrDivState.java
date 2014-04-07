package crux.scanning;

import crux.Token.Kind;

public class CommentOrDivState implements State
{
	private static final CommentOrDivState inst = new CommentOrDivState();
	public static CommentOrDivState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.value() == '/')
		{
			return DrainLineState.instance();
		}
		context.emit(Kind.DIV);
		return StartState.instance().transition(context);
	}
}