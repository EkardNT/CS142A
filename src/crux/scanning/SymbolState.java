package crux.scanning;

import crux.Token.Kind;

public class SymbolState implements State
{
	private static final SymbolState inst = new SymbolState();
	public static SymbolState instance() { return inst; }
		
	@Override
	public State transition(TransitionContext context)
	{
		if(context.isSymbol())
		{
			// Test to see whether accumulating this char
			// would result in any valid symbols remaining.
			// If yes then we keep building up the symbol.
			context.pushChar();
			if(context.existsSymbolWithAccumulatedAsPrefix())
				return this;
			// Otherwise undo the push and emit a symbol token
			// or error token depending on whether the accumulated
			// value is a valid symbol string.
			context.popChar();
			String potentialSymbol = context.accumulated();
			context.emit(context.getSymbols().containsKey(potentialSymbol)
				? context.getSymbols().get(potentialSymbol)
				: Kind.ERROR);
			// Finally, we defer handling of this potential new symbol
			// to the start state, because it might be a comment.
			return StartState.instance().transition(context);
		}
		// If the value is not a symbol, then we transition
		// away from this state, emitting the correct symbol
		// token or error token depending on whether the
		// accumulated value is a valid symbol string or not.
		String potentialSymbol = context.accumulated();
		context.emit(context.getSymbols().containsKey(potentialSymbol)
			? context.getSymbols().get(potentialSymbol)
			: Kind.ERROR);
		// Because the value is not a symbol, defer its 
		// handling to the StartState.
		return StartState.instance().transition(context);
	}
}
