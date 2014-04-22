package crux.parsing;

import crux.Token;

public class RequiredTokenException extends Exception 
{
	public final Token.Kind ExpectedKind;
	public final Token ActualToken;
	
	public RequiredTokenException(Token.Kind requiredKind, Token actualToken)
	{
		ExpectedKind = requiredKind;
		ActualToken = actualToken;
	}
}
