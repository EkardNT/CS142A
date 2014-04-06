package crux;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import crux.Token.Kind;
import crux.scanning.Scanner;

public class Program 
{
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner();
		System.out.println(System.getProperty("user.dir"));
		
		
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
					System.out.printf(
						"%-12s%-10s%-3s%-3s\n",
						token.getKind(),
						token.getLexeme(),
						token.getLineNumber(),
						token.getCharPos());
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