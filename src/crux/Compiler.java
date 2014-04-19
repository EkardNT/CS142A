package crux;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import crux.Token.Kind;
import crux.parsing.LL1Reader;

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
		Parser parser = new Parser();
		
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
				System.out.printf("Compiling file \"%s\":\n", path);
				scanner.beginReadFrom(reader);
				parser.parse(new LL1Reader(scanner));
				reader.close();
				System.out.println();
			}
			catch (IOException e) 
			{
				System.out.printf("IO error while compiling file \"%s\", details follow.", path);
				e.printStackTrace();
			}
		}
	}
}