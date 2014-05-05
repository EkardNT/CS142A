package crux;

public class Symbol
{
	private final String identifier;
	
	public Symbol(String name)
	{
		this.identifier = name;
	}
	
	public String getName() { return identifier; }
}
