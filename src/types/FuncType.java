package types;

public class FuncType extends Type
{
	private TypeList args;
	private Type ret;
   
   public FuncType(TypeList args, Type returnType)
   {
      this.args = args;
      this.ret = returnType;
   }
   
   public Type returnType()
   {
      return ret;
   }
   
   public TypeList arguments()
   {
      return args;
   }
   
   @Override
   public String toString()
   {
      return "func(" + args + "):" + ret;
   }

   @Override
   public Type add(Type that)
   {
       return ret.add(that);
   }

   @Override
   public Type sub(Type that)
   {
       return ret.sub(that);
   }

   @Override
   public Type mul(Type that)
   {
       return ret.mul(that);
   }

   @Override
   public Type div(Type that)
   {
       return ret.div(that);
   }

   @Override
   public Type and(Type that)
   {
       return ret.and(that);
   }

   @Override
   public Type or(Type that)
   {
       return ret.or(that);
   }

   @Override
   public Type not()
   {
       return ret.not();
   }

   @Override
   public Type compare(Type that)
   {
       return ret.compare(that);
   }

   @Override
   public Type deref()
   {
	   return ret.deref();
   }

   @Override
   public Type call(Type args)
   {
	   return this.args.equivalent(args) ? ret : super.call(args);
   }
   
   @Override
   public boolean equivalent(Type that)
   {
      if (!(that instanceof FuncType))
         return false;
      FuncType thatType = (FuncType)that;
      return this.ret.equivalent(thatType.ret) && this.args.equivalent(thatType.args);
   }
   
   @Override
	public boolean isPrimitive() {
		return false;
	}
}
