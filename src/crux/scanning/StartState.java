package crux.scanning;

import crux.Token.Kind;

public class StartState implements State
{
	private static final StartState inst = new StartState();
	public static StartState instance() { return inst; }
	
	@Override
	public State transition(TransitionContext context) 
	{
		if(context.isEof())
		{
			context.emit(Kind.EOF);
			return this;
		}
		if(context.isDigit())
		{
			context.accumulate();
			return NumberState.instance();
		}
		if(context.value() == '.')
		{
			context.accumulate();
			return FloatState.instance();
		}
		if(context.isWhitespace())
		{
			return this;
		}
		if(context.isLetter() || context.value() == '_')
		{
			context.accumulate();
			return WordState.instance();
		}
		if(context.isSymbol())
		{
			context.accumulate();
			return SymbolState.instance();
		}
		context.accumulate();
		context.emit(Kind.ERROR);
		return this;
	}		
}