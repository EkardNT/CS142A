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
			context.accumulate();
			return this;
		}
		if(context.isWhitespace())
		{
			emit(context);
			return StartState.instance();
		}
		if(context.isSymbol())
		{
			emit(context);
			context.accumulate();
			return SymbolState.instance();
		}
		context.accumulate();
		return DrainErrorState.instance();
	}
	
	private void emit(TransitionContext context)
	{
		String word = context.accumulated();
		context.emit(context.getKeywords().containsKey(word)
			? context.getKeywords().get(word)
			: Kind.IDENTIFIER);
	}
}