package crux.scanning;

public class SymbolState implements State
{
	private static final SymbolState inst = new SymbolState();
	public static SymbolState instance() { return inst; }
	
	
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isEof() || context.isWhitespace())
		{
			String str = context.accumulated();
			if(context.getSymbols().containsKey(str))
			{
				context.emit(context.getSymbols().get(str));
				return StartState.instance();
			}
			context.accumulate();
			return DrainErrorState.instance();
		}
		if(context.isSymbol())
		{
			context.accumulate();
			return this;
		}
		String str = context.accumulated();
		if(context.getSymbols().containsKey(str))
		{
			context.emit(context.getSymbols().get(str));
			return StartState.instance().transition(context);
		}
		context.accumulate();
		return DrainErrorState.instance();
	}
}
