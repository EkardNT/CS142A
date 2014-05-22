package types;

public class VoidType extends Type {
    @Override
    public String toString()
    {
        return "void";
    }
    
    @Override
    public boolean equivalent(Type that)
    {
        return that instanceof VoidType
        	|| (that instanceof FuncType && equivalent(((FuncType)that).returnType()));
    }
}
