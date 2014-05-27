package types;

public class ArrayType extends Type {
    
	private Type element;
	private int length;
	
	public ArrayType(int length, Type element)
	{
	    this.length = length;
	    this.element = element;
	}
	
	public int arrayLength()
	{
	    return length;
	}
	
	public Type elementType()
	{
	    return element;
	}
	
	@Override
	public String toString()
	{
	    return "array[" + length + "," + element + "]";
	}
	
	@Override
	public Type index(Type that)
	{
	    return that instanceof IntType
	    	? element
	    	: super.index(that);
	}
	
	@Override
	public Type assign(Type source)
	{
	    return equivalent(source)
	    	? new ArrayType(length, element)
	    	: super.assign(source);
	}
	
	@Override
	public boolean equivalent(Type that)
	{
	    if (!(that instanceof ArrayType))
	        return false;        
	    ArrayType aType = (ArrayType)that;
	    return this.length == aType.length && element.equivalent(aType.element);
	}
	
	@Override
	public boolean isPrimitive() {
		return false;
	}
}
