package crux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import crux.Token.Kind;
import crux.scanning.StartState;
import crux.scanning.State;
import crux.scanning.TransitionContext;

// Converts input source into a stream of tokens.
public class Scanner 
{
	// The source of input that is currently being read from,
	// or null if no input is currently being read from.
	private BufferedReader input;
	
	private StringBuilder accumulator;	
	private int nextReadLineNumber, nextReadColumnNumber, tokenStartLineNumber, tokenStartColumnNumber;
	private boolean eof;	
	private char value;
	private State currentState;
	private TransitionContext context;
	private Queue<Token> emittedTokens;
	
	public Scanner(FileReader source)
	{
		input = new BufferedReader(source);
		accumulator = new StringBuilder();
		context = new ScannerTransitionContext();
		currentState = StartState.instance();
		emittedTokens = new LinkedList<Token>();
		nextReadLineNumber = nextReadColumnNumber = 1;
		eof = false;
	}
	
	public Token next()
	{
		if(eof)
			return new Token(Kind.EOF, "", nextReadLineNumber, nextReadColumnNumber - 1);
		
		while(emittedTokens.size() == 0)
		{
			int rawValue = readChar();
			
			if(rawValue < 0)
			{
				eof = true;
				value = Character.MIN_VALUE;
			}
			else
			{
				value = (char)rawValue;
			}
			
			currentState = currentState.transition(context);
		}
				
		return emittedTokens.remove();
	}
	
	private int readChar()
	{
		try
		{
			int readVal;
			while(true)
			{
				readVal = input.read();
				//if(readVal == (int)'\n')
				//	continue;
				nextReadColumnNumber++;
				if(readVal == (int)'\n')
				{
					nextReadLineNumber++;
					nextReadColumnNumber = 1;					
				}
				return readVal;
			}
		}
		catch (IOException e)
		{
			return -1;
		}
	}
	
	private class ScannerTransitionContext implements TransitionContext
	{
		private final HashMap<String, Kind> keywords;
		private final HashSet<Character> symbolChars;
		private final HashMap<String, Kind> symbols;
		
		public ScannerTransitionContext()
		{
			keywords = new HashMap<String, Kind>();
			keywords.put("and", Kind.AND);
			keywords.put("or", Kind.OR);
			keywords.put("not", Kind.NOT);
			keywords.put("let", Kind.LET);
			keywords.put("var", Kind.VAR);
			keywords.put("array", Kind.ARRAY);
			keywords.put("func", Kind.FUNC);
			keywords.put("if", Kind.IF);
			keywords.put("else", Kind.ELSE);
			keywords.put("while", Kind.WHILE);
			keywords.put("true", Kind.TRUE);
			keywords.put("false", Kind.FALSE);
			keywords.put("return", Kind.RETURN);
			
			symbolChars = new HashSet<Character>();
			symbolChars.add(new Character('('));
			symbolChars.add(new Character(')'));
			symbolChars.add(new Character('{'));
			symbolChars.add(new Character('}'));
			symbolChars.add(new Character('['));
			symbolChars.add(new Character(']'));
			symbolChars.add(new Character('+'));
			symbolChars.add(new Character('-'));
			symbolChars.add(new Character('*'));
			symbolChars.add(new Character('/'));
			symbolChars.add(new Character('>'));
			symbolChars.add(new Character('<'));
			symbolChars.add(new Character('='));
			symbolChars.add(new Character('!'));
			symbolChars.add(new Character(','));
			symbolChars.add(new Character(';'));
			symbolChars.add(new Character(':'));
			
			symbols = new HashMap<String, Kind>();
			symbols.put("(", Kind.OPEN_PAREN);
			symbols.put(")", Kind.CLOSE_PAREN);
			symbols.put("{", Kind.OPEN_BRACE);
			symbols.put("}", Kind.CLOSE_BRACE);
			symbols.put("[", Kind.OPEN_BRACKET);
			symbols.put("]", Kind.CLOSE_BRACKET);
			symbols.put("+", Kind.ADD);
			symbols.put("-", Kind.SUB);
			symbols.put("*", Kind.MUL);
			symbols.put("/", Kind.DIV);
			symbols.put(">=", Kind.GREATER_EQUAL);
			symbols.put("<=", Kind.LESSER_EQUAL);
			symbols.put(">", Kind.GREATER_THAN);
			symbols.put("<", Kind.LESS_THAN);
			symbols.put("!=", Kind.NOT_EQUAL);
			symbols.put("==", Kind.EQUAL);
			symbols.put("=", Kind.ASSIGN);
			symbols.put(",", Kind.COMMA);
			symbols.put(";", Kind.SEMICOLON);
			symbols.put(":", Kind.COLON);
			symbols.put("::", Kind.CALL);
		}
		
		public String accumulated()
		{
			return accumulator.toString();
		}
		
		public char value()
		{
			return eof ? Character.MIN_VALUE : value;
		}
		
		public void pushChar() 
		{
			accumulator.append(value);
		}
		
		public char popChar()
		{
			if(accumulator.length() > 0)
			{
				char val = accumulator.charAt(accumulator.length() - 1);
				accumulator.setLength(accumulator.length() - 1);
				return val;
			}
			return Character.MIN_VALUE;
		}
		
		public void emit(Token.Kind kind)
		{
			emittedTokens.add(new Token(kind, accumulator.toString(), tokenStartLineNumber, tokenStartColumnNumber));
			accumulator.setLength(0);
		}
		
		public void startToken()
		{
			tokenStartLineNumber = nextReadLineNumber;
			tokenStartColumnNumber = nextReadColumnNumber - 1;
		}
		
		public boolean isDecimalSeparator()
		{
			return !eof && value == '.';
		}
		
		public boolean isDigit()
		{
			return !eof && Character.isDigit(value);
		}
		
		public boolean isLetter()
		{
			return !eof && ((value >= 'a' && value <= 'z') || (value >= 'A' && value <= 'Z'));			
		}
		
		public boolean isWhitespace()
		{
			return !eof && Character.isWhitespace(value);
		}
		
		public boolean isEof()
		{
			return eof;
		}

		@Override
		public Map<String, Kind> getKeywords() 
		{
			return keywords;
		}

		@Override
		public Map<String, Kind> getSymbols() 
		{
			return symbols;
		}

		@Override
		public boolean isSymbol() 
		{
			return !eof && symbolChars.contains(new Character(value));
		}

		@Override
		public boolean isNewline() 
		{
			return !eof && (value == '\n' || value == '\r');
		}

		@Override
		public void popAllChars() 
		{
			accumulator.setLength(0);
		}

		@Override
		public boolean existsSymbolWithAccumulatedAsPrefix()
		{
			// Should use a trie or other prefix tree here,
			// but n is small and deadlines loom, so settle
			// for this O(n) linear search.
			String prefix = accumulated();
			for(String symbolString : symbols.keySet())
				if(symbolString.startsWith(prefix))
					return true;
			return false;
		}
	}
}