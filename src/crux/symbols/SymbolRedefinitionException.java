package crux.symbols;

import crux.Token;

// Thrown when the parser encounters an attempt to define
// a symbol with an identifier that already exists in
// the current scope.
public class SymbolRedefinitionException extends Exception
{
	public final Token RedefiningToken;
	
	public SymbolRedefinitionException(Token redefiningToken)
	{
		RedefiningToken = redefiningToken;
	}
}
