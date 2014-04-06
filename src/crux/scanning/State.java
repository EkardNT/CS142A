package crux.scanning;

public interface State 
{
	State transition(TransitionContext context);
}