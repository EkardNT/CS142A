package crux;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import crux.Token.Kind;

public class Compiler 
{
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner();
		
		for(String path : args)
		{
			try 
			{
				BufferedReader reader = Files.newBufferedReader(Paths.get(path), Charset.forName("UTF-8"));
				scanner.beginReadFrom(reader);
				Token token = null;
				do
				{
					token = scanner.next();
					switch(token.getKind())
					{
						case IDENTIFIER:
						case FLOAT:
						case INTEGER:
						case ERROR:
							System.out.printf(
								"%s(%s)(lineNum:%s, charPos:%s)\n",
								token.getKind(),
								token.getLexeme(),
								token.getLineNumber(),
								token.getCharPos());
							break;
						default:
							System.out.printf(
								"%s(lineNum:%s, charPos:%s)\n",
								token.getKind(),
								token.getLineNumber(),
								token.getCharPos());
							break;
					}
				}
				while(token.getKind() != Kind.EOF);
				reader.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}