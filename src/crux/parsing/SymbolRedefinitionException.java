package crux.parsing;

import crux.Token;

public class SymbolRedefinitionException extends Exception {
	public final Token DuplicateToken;
	
	public SymbolRedefinitionException(Token duplicateToken) {
		DuplicateToken = duplicateToken;
	}

}
