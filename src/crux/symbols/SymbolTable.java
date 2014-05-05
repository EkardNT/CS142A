package crux.symbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import crux.Symbol;

public class SymbolTable
{
	private final SymbolTable parent;
	private final Map<String, Symbol> definedSymbols;
	private final ArrayList<Symbol> declarationOrder;
	
	public SymbolTable(SymbolTable parent)
	{
		this.parent = parent;
		definedSymbols = new HashMap<String, Symbol>();
		declarationOrder = new ArrayList<Symbol>();
	}
	
	public Iterable<Symbol> getDeclarationOrder() {	return declarationOrder; }
	
	public SymbolTable getParent() { return parent; }
	
	public boolean containsSymbol(String identifier, boolean checkParentScopes)
	{
		return definedSymbols.containsKey(identifier) 
			|| (checkParentScopes && parent != null && parent.containsSymbol(identifier, true));
	}
	
	public void define(String identifier)
	{
		Symbol symbol = new Symbol(identifier);
		definedSymbols.put(identifier, symbol);
		declarationOrder.add(symbol);
	}
	
	public Symbol lookup(String identifier)
	{
		return definedSymbols.containsKey(identifier)
			? definedSymbols.get(identifier)
			: (parent != null ? parent.lookup(identifier) : null);
	}
}