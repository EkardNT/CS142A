package crux.scanning;

public class DrainLineState implements State
{
	private static final DrainLineState inst = new DrainLineState();
	public static DrainLineState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context) 
	{
		context.popAllChars();
		return (context.isEof() || context.isNewline()) ? StartState.instance().transition(context) : this;
	}
}