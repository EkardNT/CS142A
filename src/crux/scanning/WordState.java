package crux.scanning;

import crux.Token.Kind;

public class WordState implements State
{
	private static final WordState inst = new WordState();
	public static WordState instance() { return inst; }
			
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isLetter() || context.value() == '_' || context.isDigit())
		{
			context.pushChar();
			return this;
		}
		String word = context.accumulated();
		context.emit(context.getKeywords().containsKey(word)
			? context.getKeywords().get(word)
			: Kind.IDENTIFIER);
		return StartState.instance().transition(context);
	}
}