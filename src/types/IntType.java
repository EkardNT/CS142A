package types;

public class IntType extends Type 
{
    @Override
    public String toString() {
        return "int";
    }

    @Override
    public Type add(Type that)
    {
        return equivalent(that)
        	? new IntType()
        	: super.add(that);
    }
    
    @Override
    public Type sub(Type that)
    {
        return equivalent(that)
        	? new IntType()
        	: super.sub(that);
    }
    
    @Override
    public Type mul(Type that)
    {
        return equivalent(that)
        	? new IntType()
        	: super.mul(that);
    }
    
    @Override
    public Type div(Type that)
    {
        return equivalent(that)
        	? new IntType()
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
    		? new IntType()
    		: super.assign(source);
    }
    
    @Override
    public boolean equivalent(Type that)
    {
    	return that instanceof IntType
    		|| (that instanceof FuncType && equivalent(((FuncType)that).returnType()));
    }
}
