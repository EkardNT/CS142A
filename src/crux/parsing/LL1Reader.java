package crux.parsing;

import crux.Scanner;
import crux.Token;

public class LL1Reader
{
	private final Scanner scanner;
	private Token token;
	
	public LL1Reader(Scanner scanner)
	{
		this.scanner = scanner;
		token = scanner.next();
	}
	
	public Token token() { return token; }
	
	public Token.Kind kind() { return token.getKind(); }
	
	public void advance() { token = scanner.next(); }
}