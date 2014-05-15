package crux.ast;

public interface Visitor 
{
	public void visit(Addition command);
	public void visit(AddressOf command);
	public void visit(ArrayDeclaration command);
	public void visit(Assignment command);
	public void visit(Call command);
	public void visit(Comparison command);
	public void visit(Dereference command);
	public void visit(Division command);
	public void visit(Error command);
	public void visit(FunctionDeclaration command);
	public void visit(IfElseBranch command);
	public void visit(Index command);
	public void visit(LiteralBoolean command);
	public void visit(LiteralFloat command);
	public void visit(LiteralInteger command);
	public void visit(LogicalAnd command);
	public void visit(LogicalNot command);
	public void visit(LogicalOr command);
	public void visit(Multiplication command);
	public void visit(Return command);
	public void visit(Subtraction command);
	public void visit(VariableDeclaration command);
	public void visit(WhileLoop command);
}
