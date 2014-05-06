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
			context.startToken();
			context.emit(Kind.EOF);
			return this;
		}
		if(context.isDigit())
		{
			context.startToken();
			context.pushChar();
			return NumberState.instance();
		}
		if(context.isWhitespace())
		{
			return this;
		}
		if(context.isLetter() || context.value() == '_')
		{
			context.startToken();
			context.pushChar();
			return WordState.instance();
		}
		if(context.value() == '/')
		{
			context.startToken();
			context.pushChar();
			return CommentOrDivState.instance();
		}
		if(context.isSymbol())
		{
			context.startToken();
			context.pushChar();
			return SymbolState.instance();
		}
		context.startToken();
		context.pushChar();
		context.emit(Kind.ERROR);
		return this;
	}		
}