package crux.scanning;

import java.util.Map;
import crux.Token;
import crux.Token.Kind;

public interface TransitionContext
{
	public Map<String, Kind> getKeywords();
	public Map<String, Kind> getSymbols();
	public String accumulated();
	public char value();
	public void pushChar();
	public char popChar();
	public void emit(Token.Kind kind);
	public void popAllChars();
	public boolean isDecimalSeparator();
	public boolean isDigit();
	public boolean isLetter();	
	public boolean isWhitespace();	
	public boolean isEof();
	public boolean isSymbol();
	public boolean isNewline();
}