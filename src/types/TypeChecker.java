package types;

import java.util.HashMap;
import java.util.Stack;

import crux.Symbol;
import ast.*;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;
    private Symbol currentFunctionSymbol = null;
    private Stack<Boolean> returnFound;

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */

    public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
        returnFound = new Stack<Boolean>();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
    	if(type == null)
    		throw new RuntimeException("Null type.");
        if (type instanceof ErrorType) {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Command node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    @Override
    public void visit(ExpressionList node) {
    	for(Expression e : node)
    		((Command)e).accept(this);
    }

    @Override
    public void visit(DeclarationList node) {
    	for(Declaration d : node)
    		((Command)d).accept(this);
    }

    @Override
    public void visit(StatementList node) {
        for(Statement s : node)
        	((Command)s).accept(this);
    }

    @Override
    public void visit(AddressOf node) {
    	put(node, new AddressType(node.symbol().getType()));
    }

    @Override
    public void visit(LiteralBool node) {
    	put(node, new BoolType());
    }

    @Override
    public void visit(LiteralFloat node) {
        put(node, new FloatType());
    }

    @Override
    public void visit(LiteralInt node) {
    	put(node, new IntType());
    }

    @Override
    public void visit(VariableDeclaration node) {
    	Type symbolType = node.symbol().getType();
    	if(!symbolType.isPrimitive())
    		reportError(node.lineNumber(), node.charPosition(), "Variable " + node.symbol().getName() + " has invalid type " + symbolType + ".");
    }

    @Override
    public void visit(ArrayDeclaration node) {
        Type elementType = ((ArrayType)node.symbol().getType()).elementType();
        // Descend possible nested array declarations to get at the root element type.
        while(elementType instanceof ArrayType)
        	elementType = ((ArrayType)elementType).elementType();
        if(!elementType.isPrimitive())
    		reportError(node.lineNumber(), node.charPosition(), "Array " + node.symbol().getName() + " has invalid base type " + elementType + ".");
    }

    @Override
    public void visit(FunctionDefinition node) {
    	currentFunctionSymbol = node.function();    	
    	// Recurse down tree.
        node.body().accept(this);
        currentFunctionSymbol = null;
        
        VoidType type = new VoidType();
        
        // Make sure no arguments have void or error types.
        for(int i = 0; i < node.arguments().size(); i++)
        {
        	Symbol arg = node.arguments().get(i);
        	if(type.equivalent(arg.getType()))
        		reportError(node.lineNumber(), node.charPosition(), "Function " + node.function().getName() + " has a void argument in position " + i + ".");
        	else if(arg.getType() instanceof ErrorType)
        		reportError(node.lineNumber(), node.charPosition(), "Function " + node.function().getName() + " has an error in argument in position " + i + ": " + ((ErrorType)arg.getType()).getMessage());
        }
        
        // Make sure "main" function has void return type and no arguments.
        if(node.function().getName().equals("main")
        	&& (node.arguments().size() > 0 
    			|| !((FuncType)node.function().getType()).returnType().equivalent(new VoidType())))
        {
        	reportError(node.lineNumber(), node.charPosition(), "Function main has invalid signature.");
        }
    }

    @Override
    public void visit(Comparison node) {
    	node.leftSide().accept(this);
    	node.rightSide().accept(this);
    	
    	Type intType = new IntType(),
            	floatType = new FloatType(),
            	leftType = getType((Command)node.leftSide()),
            	rightType = getType((Command)node.rightSide());
    	if(!leftType.equivalent(rightType))
        	put(node, new ErrorType("Cannot compare " + leftType + " with " + rightType + "."));
    	else if(leftType.equivalent(intType) || leftType.equivalent(floatType))
    		put(node, new BoolType());
    	else
    		put(node, new ErrorType("Cannot compare " + leftType + " with " + rightType + "."));    		
    }
    
    private void visitBinaryArithmetic(Command node, Command leftSide, Command rightSide, String name)
    {
    	leftSide.accept(this);
        rightSide.accept(this);
        
        Type intType = new IntType(),
        	floatType = new FloatType(),
        	leftType = getType(leftSide),
        	rightType = getType(rightSide);
        if(!leftType.equivalent(rightType))
        	put(node, new ErrorType("Cannot add " + leftType + " with " + rightType + "."));
        else if(leftType.equivalent(intType))
        	put(node, intType);
        else if(leftType.equivalent(floatType))
        	put(node, floatType);
        else
        	put(node, new ErrorType("Cannot add " + leftType + " with " + rightType + "."));
    }
    
    @Override
    public void visit(Addition node) {
    	visitBinaryArithmetic(node, (Command)node.leftSide(), (Command)node.rightSide(), "Addition");
    }
    
    @Override
    public void visit(Subtraction node) {
    	visitBinaryArithmetic(node, (Command)node.leftSide(), (Command)node.rightSide(), "Subtraction");
    }
    
    @Override
    public void visit(Multiplication node) {
    	visitBinaryArithmetic(node, (Command)node.leftSide(), (Command)node.rightSide(), "Multiplication");
    }
    
    @Override
    public void visit(Division node) {
    	visitBinaryArithmetic(node, (Command)node.leftSide(), (Command)node.rightSide(), "Division");
    }
    
    @Override
    public void visit(LogicalAnd node) {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        BoolType boolType = new BoolType();
        Type leftType = getType((Command)node.leftSide()),
        	rightType = getType((Command)node.rightSide());
        if(!boolType.equivalent(leftType))
        	put(node, new ErrorType("Cannot compute " + leftType + " and " + rightType + "."));
        else if(!boolType.equivalent(rightType))
        	put(node, new ErrorType("Cannot compute " + leftType + " and " + rightType + "."));
        else
        	put(node, new BoolType());
    }

    @Override
    public void visit(LogicalOr node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        BoolType boolType = new BoolType();
        Type leftType = getType((Command)node.leftSide()),
        	rightType = getType((Command)node.rightSide());
        if(!boolType.equivalent(leftType))
        	put(node, new ErrorType("Cannot compute " + leftType + " or " + rightType + "."));
        else if(!boolType.equivalent(rightType))
        	put(node, new ErrorType("Cannot compute " + leftType + " or " + rightType + "."));
        else
        	put(node, new BoolType());
    }

    @Override
    public void visit(LogicalNot node) {
    	node.expression().accept(this);
    	Type expressionType = getType((Command)node.expression());
    	if(!(new BoolType()).equivalent(expressionType))
    		put(node, new ErrorType("Cannot negate " + expressionType + "."));
    	else
    		put(node, new BoolType());
    }
    
    @Override
    public void visit(Dereference node) {
    	node.expression().accept(this);
    	
    	Type expressionType = getType((Command)node.expression());
    	if(!(expressionType instanceof AddressType))
    		put(node, new ErrorType("Cannot dereference " + expressionType)); // No period in provided test output.
    	else
    		put(node, ((AddressType)expressionType).base());
    }

    @Override
    public void visit(Index node) {
        node.amount().accept(this);
        node.base().accept(this);
        
        Type baseType = getType((Command)node.base()),
        	amountType = getType((Command)node.amount());
        if(!(baseType instanceof AddressType))
        	put(node, new ErrorType("Cannot index " + baseType + " with " + amountType + "."));
        else if(!(((AddressType)baseType).base() instanceof ArrayType))
        	put(node, new ErrorType("Cannot index " + baseType + " with " + amountType + "."));
        else if(!(amountType instanceof IntType))
        	put(node, new ErrorType("Can only index with IntType, not with " + amountType + "."));
        else
        	put(node, new AddressType(((ArrayType)((AddressType)baseType).base()).elementType()));
    }

    @Override
    public void visit(Assignment node) {
    	node.destination().accept(this);
    	node.source().accept(this);
    	put(node, getType((Command)node.destination()));
    	
    	Type destType = getType((Command)node.destination()),
    		sourceType = getType((Command)node.source());
    	
    	if(destType instanceof AddressType && ((AddressType)destType).base().equivalent(sourceType))
    	{
    		// Ignore this.
    	}
    	else if(!destType.equivalent(sourceType))
    		reportError(node.lineNumber(), node.charPosition(), "Cannot assign " + sourceType + " to " + destType + ".");
    }

    @Override
    public void visit(Call node) {
    	node.arguments().accept(this);
    	TypeList argList = new TypeList();
    	for(Expression arg : node.arguments())
    		argList.append(getType((Command)arg));
    	FuncType calledFuncType = ((FuncType)node.function().getType());
    	if(!argList.equivalent(calledFuncType.arguments()))
    		put(node, new ErrorType("Cannot call " + calledFuncType + " using " + argList + "."));
    	else
    		put(node, node.function().getType());
    }

    @Override
    public void visit(IfElseBranch node) {
    	node.condition().accept(this);
    	node.thenBlock().accept(this);
    	node.elseBlock().accept(this);
    	
    	if(!(new BoolType()).equivalent(getType((Command)node.condition())))
    		reportError(node.lineNumber(), node.charPosition(), "IfElseBranch requires bool condition not " + getType((Command)node.condition()) + ".");
    }

    @Override
    public void visit(WhileLoop node) {
    	node.condition().accept(this);
    	node.body().accept(this);
    	
    	if(!(new BoolType()).equivalent(getType((Command)node.condition())))
    		reportError(node.lineNumber(), node.charPosition(), "WhileLoop requires bool condition not " + getType((Command)node.condition()) + ".");
    }

    @Override
    public void visit(Return node) {
        node.argument().accept(this);
        
        Type argType = getType((Command)node.argument()),
        	expectedRetType = ((FuncType)currentFunctionSymbol.getType()).returnType();
        if(!expectedRetType.equivalent(argType))
        	reportError(node.lineNumber(), node.charPosition(), "Function " + currentFunctionSymbol.getName() + " returns " + expectedRetType + " not " + argType + ".");        	
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }
}
