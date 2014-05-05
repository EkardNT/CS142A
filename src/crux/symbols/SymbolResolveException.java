package crux.symbols;

import crux.Token;

// Thrown when the parser encounters an attempt to use an identifier
// not defined in the current scope.
public class SymbolResolveException extends Exception 
{
	public final Token UnresolvableToken;
	
	public SymbolResolveException(Token unresolvableToken)
	{
		UnresolvableToken = unresolvableToken;
	}
}
