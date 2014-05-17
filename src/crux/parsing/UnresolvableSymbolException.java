package crux.parsing;

import crux.Token;

public class UnresolvableSymbolException extends Exception {
	public final Token UnresolvedToken;
	
	public UnresolvableSymbolException(Token unresolvedToken) {
		UnresolvedToken = unresolvedToken;
	}

}
