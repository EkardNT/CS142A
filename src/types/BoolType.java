package types;

public class BoolType extends Type 
{
	@Override
	public String toString()
	{
	    return "bool";
	}
	
	@Override
	public Type and(Type that)
	{
		return equivalent(that)
	        	? new BoolType()
	        	: super.and(that);
	}
	
	@Override
	public Type or(Type that)
	{
		return equivalent(that)
	    	? new BoolType()
	    	: super.or(that);
	}
	
	@Override
	public Type not()
	{
	    return new BoolType();
	}
	
	@Override
	public Type compare(Type that)
	{
	    return equivalent(that)
	    	? new BoolType()
	    	: super.compare(that);
	}
	
	@Override
	public Type assign(Type source)
	{
		return equivalent(source)
			? new BoolType()
			: super.assign(source);
	}
	
	@Override
	public boolean equivalent(Type that)
	{
	    return that instanceof BoolType
	    	|| (that instanceof FuncType && equivalent(((FuncType)that).returnType()));
	}
	
	@Override
	public boolean isPrimitive() {
		return true;
	}
}    
