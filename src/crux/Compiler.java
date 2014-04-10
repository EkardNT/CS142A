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
		if(args.length == 0)
		{
			System.out.println("Specify at least one input file to read.");
			return;
		}
		
		Scanner scanner = new Scanner();
		
		for(String path : args)
		{
			BufferedReader reader = null;
			
			try 
			{
				reader = Files.newBufferedReader(Paths.get(path), Charset.forName("UTF-8"));
			}
			catch(IOException e)
			{
				System.out.printf("Unable to open file \"%s\".\n", path);
				continue;
			}
			
			try
			{
				System.out.printf("Scanning file \"%s\":\n", path);
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
				System.out.println();
			}
			catch (IOException e) 
			{
				System.out.printf("Error while scanning file \"%s\", details follow.", path);
				e.printStackTrace();
			}
		}
	}
}