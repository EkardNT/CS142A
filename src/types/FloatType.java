package types;

public class FloatType extends Type 
{    
    @Override
    public String toString()
    {
        return "float";
    }
       
    @Override
    public Type add(Type that)
    {
        return equivalent(that)
        	? new FloatType()
        	: super.add(that);
    }
    
    @Override
    public Type sub(Type that)
    {
        return equivalent(that)
        	? new FloatType()
        	: super.sub(that);
    }
    
    @Override
    public Type mul(Type that)
    {
        return equivalent(that)
        	? new FloatType()
        	: super.mul(that);
    }
    
    @Override
    public Type div(Type that)
    {
        return equivalent(that)
        	? new FloatType()
        	: super.div(that);
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
    		? new FloatType()
    		: super.assign(source);
    }
    
    @Override
    public boolean equivalent(Type that)
    {
    	return that instanceof FloatType
    		|| (that instanceof FuncType && equivalent(((FuncType)that).returnType()));
    }
    
    @Override
	public boolean isPrimitive() {
		return true;
	}
}
