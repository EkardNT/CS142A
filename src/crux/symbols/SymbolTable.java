package crux.symbols;

import java.util.HashMap;
import java.util.Map;

import crux.Symbol;

public class SymbolTable
{
	private final SymbolTable parent;
	private final Map<String, Symbol> definedSymbols;
	
	public SymbolTable(SymbolTable parent)
	{
		this.parent = parent;
		definedSymbols = new HashMap<String, Symbol>();
	}
	
	public boolean containsSymbol(String identifier, boolean checkParentScopes)
	{
		return definedSymbols.containsKey(identifier) 
			|| (checkParentScopes && parent != null && parent.containsSymbol(identifier, true));
	}
	
	public void define(String identifier)
	{
		definedSymbols.put(identifier, new Symbol(identifier));
	}
	
	public Symbol lookup(String identifier)
	{
		return definedSymbols.containsKey(identifier)
			? definedSymbols.get(identifier)
			: (parent != null ? parent.lookup(identifier) : null);
	}
}