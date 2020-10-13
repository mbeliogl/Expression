package com.maxim;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.Arrays;

/**
 * A class representing an abstract arithmetic expression
 */
public abstract class Expression {

   /**
    * Creates a tree from an expression in postfix notation
    * @param postfix an array of Strings representing a postfix arithmetic expression
    * @return a new Expression that represents postfix
    */
   public static Expression expressionFromPostfix(String[] postfix) {
       Stack<Expression> stack = new Stack<Expression>();
       
       Expression var1;
       Expression var2;
       Expression exp = null;

       for (String s : postfix) {

           if (!(s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/"))) { // checking if operator
               if (Character.isLetter(s.charAt(0))) {
                   stack.push(new VariableOperand(s));
               } else {
                   stack.push(new IntegerOperand(Integer.parseInt(s)));
               }
           } else {

               if (!stack.empty()) {    // performing the postfix eval. algorithm
                   var2 = stack.pop();
                   var1 = stack.pop();
                   exp = makeExpression(var2, var1, s);
                   stack.push(exp);
               }
           }
       }
       return exp;
   }

   /**
    * Creates a tree from an expression in infix notation
    * @param infix an array of Strings representing a infix arithmetic expression
    * @return a new Expression that represents infix
    */
   public static Expression expressionFromInfix(String[] infix) {
       Stack<String> yardStack = new Stack<String>();
       Stack<Expression> stack = new Stack<Expression>();

       HashMap<String, Integer> opPrec = new HashMap<String, Integer>();
       opPrec.put("*", 3);
       opPrec.put("/", 3);
       opPrec.put("+", 2);
       opPrec.put("-", 2);
       opPrec.put("(", 1);
       opPrec.put(")", 0);

       Expression exp = null;
       Expression var1;
       Expression var2;

       for(int i = 0; i < infix.length; i++) {
	       char c = infix[i].charAt(0);
	       String c1 = Character.toString(c);
	       
	       //put all ints and vars straight to exp stack
	       if(Character.isLetter(infix[i].charAt(0))) {
		       stack.push(new VariableOperand(infix[i]));
		   }
	       if(infix[i].charAt(0) == '-' && infix[i].length() > 1) {
		      stack.push(new IntegerOperand(Integer.parseInt(infix[i])));
	       	  }  
	       if(Character.isDigit(infix[i].charAt(0))) {
	       	       stack.push(new IntegerOperand(Integer.parseInt(infix[i]))); 
	       	   }
	       
	       if(infix[i].equals("+") || infix[i].equals("-") || infix[i].equals("*") || infix[i].equals("/")) {

		       while(! yardStack.empty() && opPrec.get(yardStack.peek()) >= opPrec.get(c1)) {
			       var1 = stack.pop();
			       var2 = stack.pop();
			       stack.push(makeExpression(var2, var1, yardStack.pop()));			      
			   }
		       yardStack.push(infix[i]);
	       }

	       else if(infix[i].equals("(")) {
		       yardStack.push(infix[i]);
		   }

	       else if(infix[i].equals(")")) {
		       while(! yardStack.peek().equals("(")) {
			       var1 = stack.pop();
			       var2 = stack.pop();
			       stack.push(makeExpression(var2, var1, yardStack.pop()));
			   }
		       //pop from exp stack twice, push once
		       yardStack.pop();
		   }
	   }

       while(! yardStack.empty()) {

	       var1 = stack.pop();
	       var2 = stack.pop();
	       exp = makeExpression(var2, var1, yardStack.pop());
	       stack.push(exp);
	   }
       return exp;
   }
      
   /**
    * @return a String that represents this expression in prefix notation.
    */
   public abstract String toPrefix();

   /**
    * @return a String that represents this expression in infix notation.
    */  
   public abstract String toInfix();

   /**
    * @return a String that represents this expression in postfix notation.
    */  
   public abstract String toPostfix();

   /**
    * @return a String that represents the expression in infix notation
    */
   @Override
   public String toString() { return toInfix(); }
   
   /**
    * @return a new Expression mathematically equivalent to this one, but simplified.
    */  
   public abstract Expression simplify();

   /**
    * Evaluates the expression given assignments of values to variables.
    * @param assignments a HashMap from Strings (variable names) to Integers (values).
    * @return the result of evaluating the expression with the given variable assignments
    */
   public abstract int evaluate(HashMap<String, Integer> assignments);

   /**
    * @return a Set of the variables contained in this expression
    */
   public abstract Set<String> getVariables();

    /**
     * @param obj Expression object
     * @return compares Expressions, taking commutativity of + and * into account
     */
   @Override
   public abstract boolean equals(Object obj);





   /**
    * Prints the expression as a tree in DOT format for visualization
    * @param filename the name of the output file
    */
   public void drawExpression(String filename) throws IOException {

      BufferedWriter bw = null;
      FileWriter fw = new FileWriter(filename);
      bw = new BufferedWriter(fw);
      
      bw.write("graph Expression {\n");
      
      drawExprHelper(bw);
      
      bw.write("}\n");
      
      bw.close();
      fw.close();     
   }

   /**
    * Recursively prints the vertices and edges of the expression tree for visualization
    * @param bw the BufferedWriter to write to
    */
    protected abstract void drawExprHelper(BufferedWriter bw) throws IOException;
    
    //Helper method to make appropriate expressions
    public static Expression makeExpression(Expression var1, Expression var2, String op) {
	    Expression exp = null;

	    if(op.equals("+")) {
		    exp = new SumExpression(var1, var2); // instead of evaluating, make a new expression
	    }

	    if(op.equals("-")) {
		    exp = new DifferenceExpression(var1,var2);
	    }

	    if(op.equals("*")) {
		    exp = new ProductExpression(var1, var2);
	    }

	    if(op.equals("/")) {
		    exp = new QuotientExpression(var1, var2);
	    }
   
	    return exp;
    }
   
}




/**
 * A class representing an abstract operand
 */
abstract class Operand extends Expression
{
}

/**
 * A class representing an expression containing only a single integer value
 */
class IntegerOperand extends Operand {
   protected int operand;

   /**
    * Create the expression
    * @param operand the integer value this expression represents
    */
   public IntegerOperand(int operand)
   {
      this.operand = operand;
   }

   /**
    * @return a String that represents this expression in prefix notation
    */   
   public String toPrefix() {
       return Integer.toString(operand);
   }

   /**
    * @return a String that represents this expression in postfix notation
    */  
   public String toPostfix() {
       return Integer.toString(operand);
   }   

   /**
    * @return a String that represents the expression in infix notation
    */
   public String toInfix() {
       return Integer.toString(operand);
   }

   /**
    * @return a new Expression mathematically equivalent to this one, but simplified
    */  
   public Expression simplify() {
       return new IntegerOperand(operand);
   }   

   /**
    * Evaluates the expression given assignments of values to variables
    * @param assignments a HashMap from Strings (variable names) to Integers (values)
    * @return the result of evaluating the expression with the given variable assignments
    */
   public int evaluate(HashMap<String, Integer> assignments) {
       return operand;
   }

   /**
    * @return a Set of the variables contained in this expression
    */
   public Set<String> getVariables() {
       TreeSet<String> vars = new TreeSet<String>();
       return vars;
   }

   /**
    * @param obj and Object to compare to
    * @return true if obj is an IntegerOperand with the same associated value
    */
   @Override
   public boolean equals(Object obj) {
       if(obj == null || !((obj) instanceof IntegerOperand)) { // if is an integer operand at all!
	       return false;
	   }
       IntegerOperand op = (IntegerOperand) obj; // compare the integer values of the operands
       
       return op.operand == this.operand;
   }   

   /**
    * Recursively prints the vertices and edges of the expression tree for visualization
    * @param bw the BufferedWriter to write to
    */
   protected void drawExprHelper(BufferedWriter bw) throws IOException {
      bw.write("\tnode"+hashCode()+"[label="+operand+"];\n");
   }
}




/**
 * A class representing an expression containing only a single variable
 */
class VariableOperand extends Operand {
   protected String variable;

   /**
    * Create the expression
    * @param variable the variable name contained with this expression
    */
   public VariableOperand(String variable)
   {
      this.variable = variable;
   }

   /**
    * @return a String that represents this expression in prefix notation
    */   
   public String toPrefix() {
       return variable;
   }
    
    /**
     * @return a String that represents this expression in postfix notation
     */  
    public String toPostfix() {
        return variable;
    }   

    /**
     * @return a String that represents the expression in infix notation
     */
    public String toInfix() {
        return variable;
    }

   /**
    * @return a new Expression mathematically equivalent to this one, but simplified
    */  
   public Expression simplify() {
       return new VariableOperand(variable);
   }   

   /**
    * Evaluates the expression given assignments of values to variables
    * @param assignments a HashMap from Strings (variable names) to Integers (values)
    * @return the result of evaluating the expression with the given variable assignments
    */
   public int evaluate(HashMap<String, Integer> assignments) {
       return assignments.get(variable);
   }

   /**
    * @return a Set of the variables contained in this expression
    */
   public Set<String> getVariables() {
       TreeSet<String> vars = new TreeSet<String>();
       vars.add(variable);
       return vars;
   }

   /**
    * @param obj and Object to compare to
    * @return true if obj is an IntegerOperand with the same associated value
    */
   @Override
   public boolean equals(Object obj) {
       if(obj == null || !((obj) instanceof VariableOperand)) {
	       return false;
	   }
       VariableOperand var = (VariableOperand) obj;
       
       return var.variable.equals(this.variable); //same as with integer but comparing vars
   }   

   /**
    * Recursively prints the vertices and edges of the expression tree for visualization
    * @param bw the BufferedWriter to write to
    */
   protected void drawExprHelper(BufferedWriter bw) throws IOException {
      bw.write("\tnode"+hashCode()+"[label="+variable+"];\n");
   }   
}




/**
 * A class representing an expression involving an operator
 */
abstract class OperatorExpression extends Expression
{
   protected Expression left;
   protected Expression right;

   /**
    * Create the expression
    * @param left the expression representing the left operand
    * @param right the expression representing the right operand
    */
   public OperatorExpression(Expression left, Expression right) {
      this.left = left;
      this.right = right;
   }

   /**
    * @return a string representing the operator
    */
   protected abstract String getOperator();     
   
   /**
    * Recursively prints the vertices and edges of the expression tree for visualization
    * @param bw the BufferedWriter to write to
    */
   protected void drawExprHelper(BufferedWriter bw) throws IOException {
      String rootID = "\tnode"+hashCode();
      bw.write(rootID+"[label=\""+getOperator()+"\"];\n");

      bw.write(rootID + " -- node" + left.hashCode() + ";\n");
      bw.write(rootID + " -- node" + right.hashCode() + ";\n");
      left.drawExprHelper(bw);
      right.drawExprHelper(bw);
   }   
    
    public Set<String> getVariables() {
	    TreeSet<String> vars = new TreeSet<String>();
	    vars.addAll(this.left.getVariables());
	    vars.addAll(this.right.getVariables());  //make a new tree and add all the lefts and rights
	    return vars;
    }
}




/**
 * A class representing an expression involving a sum
 */
class SumExpression extends OperatorExpression {

   /**
    * Create the expression
    * @param left the expression representing the left operand
    * @param right the expression representing the right operand
    */
   public SumExpression(Expression left, Expression right)
   {
      super(left, right);
   }

   /**
    * @return a string representing the operand
    */
   protected String getOperator()
   {
      return "+";
   }
   
   /**
    * @return a String that represents this expression in prefix notation.
    */   
   public String toPrefix() {
       return this.getOperator() + this.left.toPrefix() + this.right.toPrefix();
   }

   /**
    * @return a String that represents this expression in postfix notation.
    */  
   public String toPostfix() {
       return this.left.toPostfix() + this.right.toPostfix() + this.getOperator();
   }   

   /**
    * @return a String that represents the expression in infix notation
    */
   public String toInfix() {
       return "(" + this.left.toInfix() + this.getOperator() + this.right.toInfix() + ")";
   }

   /**
    * @return a new Expression mathematically equivalent to this one, but simplified.
    */  
   public Expression simplify() {

       Expression myLeft = left.simplify();
       Expression myRight = right.simplify(); //keep track of the proper Expression

       // if both are integerOps, simply add the values in a new IntOp
       if(myLeft instanceof IntegerOperand && myRight instanceof IntegerOperand) {
	       IntegerOperand op1 = (IntegerOperand) myLeft;
	       IntegerOperand op2 = (IntegerOperand) myRight;
	       
	       return new IntegerOperand(op1.operand + op2.operand);
	   }

       // left int and right expression
       else if(myLeft instanceof IntegerOperand && myRight instanceof Expression) {
	       IntegerOperand a = (IntegerOperand) myLeft;
	       Expression expr = (Expression) myRight;
	       if(a.operand != 0)
	           return new SumExpression(a.simplify(), expr.simplify()); // if not zero return the sumExpression

	       else return expr.simplify(); // if 0 return the other
	   }

       // right int  and left expression
       else if(myRight instanceof IntegerOperand && myLeft instanceof Expression) {
	       IntegerOperand b = (IntegerOperand) myRight;
	       Expression expr1 = (Expression) myLeft;
	       if(b.operand != 0)
	           return new SumExpression(b.simplify(), expr1.simplify());

	       else return expr1.simplify();
	   }

       return new SumExpression(myLeft.simplify(), myRight.simplify());
   }   

   /**
    * Evaluates the expression given assignments of values to variables.
    * @param assignments a HashMap from Strings (variable names) to Integers (values).
    * @return the result of evaluating the expression with the given variable assignments
    */
   public int evaluate(HashMap<String, Integer> assignments) {
       return this.left.evaluate(assignments) + this.right.evaluate(assignments); // simply add
   }

   /**
    * @return a Set of the variables contained in this expression
    */
   public Set<String> getVariables() {
       TreeSet<String> vars = new TreeSet<String>();
       vars.addAll(this.left.getVariables());
       vars.addAll(this.right.getVariables());  //make a new tree and add all the lefts and rights
       return vars;
   }
 

   /**
    * @param obj and Object to compare to
    * @return true if obj is an IntegerOperand with the same associated value
    */
   @Override
   public boolean equals(Object obj) {
       if(obj == null || (!(obj instanceof SumExpression)))
           return false;
      
       SumExpression sum = (SumExpression) obj;

       // if both left and right are equal
       return this.left.equals(sum.left) && this.right.equals(sum.right) || sum.left.equals(this.left) && sum.right.equals(this.right);
   }   
}




/**
 * A class representing an expression involving an difference
 */
class DifferenceExpression extends OperatorExpression {

   /**
    * Create the expression
    * @param left the expression representing the left operand
    * @param right the expression representing the right operand
    */
   public DifferenceExpression(Expression left, Expression right)
   {
      super(left, right);
   }

   /**
    * @return a string representing the operand
    */
   protected String getOperator()
   {
      return "-";
   }

   /**
    * @return a String that represents this expression in prefix notation
    */   
   public String toPrefix() {
      return this.getOperator() + this.left.toPrefix() + this.right.toPrefix();
   }

   /**
    * @return a String that represents this expression in postfix notation.
    */  
   public String toPostfix() {
       return this.left.toPostfix() + this.right.toPostfix() + this.getOperator();
   }   

   /**
    * @return a String that represents the expression in infix notation
    */
   public String toInfix() {
       return "(" + this.left.toInfix() + this.getOperator() + this.right.toInfix() + ")";
   }

   /**
    * @return a new Expression mathematically equivalent to this one, but simplified.
    */  
   public Expression simplify() {
       Expression myLeft = left.simplify();
       Expression myRight = right.simplify();

       if(left instanceof IntegerOperand && right instanceof IntegerOperand) {
	       IntegerOperand op1 = (IntegerOperand) left;
	       IntegerOperand op2 = (IntegerOperand) right;
	       
	       return new IntegerOperand(op1.operand - op2.operand);
	   }

       else if(myLeft instanceof IntegerOperand && myRight instanceof Expression) {
	       IntegerOperand a = (IntegerOperand) myLeft;
	       Expression expr = (Expression) myRight;

	       if(a.operand == 0) {
		       return expr.simplify();
		   }
	   }

       else if(myRight instanceof IntegerOperand && myLeft instanceof Expression) {
	       IntegerOperand b = (IntegerOperand) myRight;
	       Expression expr1 = (Expression) myLeft;

	       if(b.operand == 0) {
		       return myLeft.simplify();
		   }
	   }

       // special case if both are variables
       else if(left instanceof VariableOperand && right instanceof VariableOperand) {
	       VariableOperand var1 = (VariableOperand) myLeft;
	       VariableOperand var2 = (VariableOperand) myRight;
	       
	       if(var1.equals(var2)) {
		       return new IntegerOperand(0); // if so, then x - x returns a new 0 IntOp
		   }
	   }

       return new DifferenceExpression(myLeft.simplify(), myRight.simplify());
     
   }   

   /**
    * Evaluates the expression given assignments of values to variables.
    * @param assignments a HashMap from Strings (variable names) to Integers (values).
    * @return the result of evaluating the expression with the given variable assignments
    */
   public int evaluate(HashMap<String, Integer> assignments) {
       return this.left.evaluate(assignments) - this.right.evaluate(assignments); // simply subtract
   }

   /**
    * @return a Set of the variables contained in this expression
    */
   public Set<String> getVariables() {
       TreeSet<String> vars = new TreeSet<String>();
       vars.addAll(this.left.getVariables());
       vars.addAll(this.right.getVariables());  //make a new tree and add all the lefts and rights
       return vars;
   }

   /**
    * @param obj and Object to compare to
    * @return true if obj is an IntegerOperand with the same associated value
    */
   @Override
   public boolean equals(Object obj) {
       if(obj == null || (!(obj instanceof DifferenceExpression)))
           return false;
      
      DifferenceExpression sum = (DifferenceExpression) obj;

       return this.left.equals(sum.left) && this.right.equals(sum.right);
   }      
}




/**
 * A class representing an expression involving a product
 */
class ProductExpression extends OperatorExpression {

   /**
    * Create the expression
    * @param left the expression representing the left operand
    * @param right the expression representing the right operand
    */
   public ProductExpression(Expression left, Expression right)
   {
      super(left, right);
   }

   /**
    * @return a string representing the operand
    */
   protected String getOperator()
   {
      return "*";
   }

   /**
    * @return a String that represents this expression in prefix notation.
    */   
   public String toPrefix()
   {
       return this.getOperator() + this.left.toPrefix() + this.right.toPrefix();
   }

   /**
    * @return a String that represents this expression in postfix notation.
    */  
   public String toPostfix()
   {
       return this.left.toPostfix() + this.right.toPostfix() + this.getOperator();
   }   

   /**
    * @return a String that represents the expression in infix notation
    */
   public String toInfix()
   {
       return "(" + this.left.toInfix() + this.getOperator() + this.right.toInfix() + ")";
   }

   /**
    * @return a new Expression mathematically equivalent to this one, but simplified
    */  
   public Expression simplify() {
       Expression myLeft = left.simplify();
       Expression myRight = right.simplify();

       if(left instanceof IntegerOperand && right instanceof IntegerOperand) {
	       IntegerOperand op1 = (IntegerOperand) left;
	       IntegerOperand op2 = (IntegerOperand) right;
	       
	       return new IntegerOperand(op1.operand * op2.operand);
	   }

       else if(myLeft instanceof IntegerOperand && myRight instanceof Expression) {
	       IntegerOperand a = (IntegerOperand) myLeft;
	       Expression expr = (Expression) myRight;

	       if(a.operand == 1) {
		       return expr.simplify(); // if left is 1
		   }

	       if(a.operand == 0) {
		       return new IntegerOperand(0); // if right is 0 simply return 0
		   }
	   }

       else if(myRight instanceof IntegerOperand && myLeft instanceof Expression) {
	       IntegerOperand b = (IntegerOperand) myRight;
	       Expression expr1 = (Expression) myLeft;

	       if(b.operand == 1) {
		       return myLeft.simplify();
		   }

	       if(b.operand == 0) {
		       return new IntegerOperand(0); // same but in reverse
		   }
	   }

       return new ProductExpression(myLeft.simplify(), myRight.simplify());
   }   

   /**
    * Evaluates the expression given assignments of values to variables
    * @param assignments a HashMap from Strings (variable names) to Integers (values)
    * @return the result of evaluating the expression with the given variable assignments
    */
   public int evaluate(HashMap<String, Integer> assignments) {
       return this.left.evaluate(assignments) * this.right.evaluate(assignments);
   }

   /**
    * @return a Set of the variables contained in this expression
    */
   public Set<String> getVariables() {
       TreeSet<String> vars = new TreeSet<String>();
       vars.addAll(this.left.getVariables());
       vars.addAll(this.right.getVariables());  //make a new tree and add all the lefts and rights
       return vars;
   }

   /**
    * @param obj and Object to compare to
    * @return true if obj is an IntegerOperand with the same associated value
    */
   @Override
   public boolean equals(Object obj) {
       if(obj == null || (!(obj instanceof ProductExpression)))
           return false;
      
       ProductExpression sum = (ProductExpression) obj;

       //check if ALL are equal
       return this.left.equals(sum.left) && this.right.equals(sum.right) || sum.left.equals(this.left) && sum.right.equals(this.right);
   }
}




/**
 * A class representing an expression involving a division
 */
class QuotientExpression extends OperatorExpression {

   /**
    * Create the expression
    * @param left the expression representing the left operand
    * @param right the expression representing the right operand
    */
   public QuotientExpression(Expression left, Expression right)
   {
      super(left, right);
   }

   /**
    * @return a string representing the operand
    */
   protected String getOperator()
   {
      return "/";
   }

   /**
    * @return a String that represents this expression in prefix notation.
    */   
   public String toPrefix()
   {
       return this.getOperator() + this.left.toPrefix() + this.right.toPrefix();
   }

   /**
    * @return a String that represents this expression in postfix notation.
    */  
   public String toPostfix()
   {
       return this.left.toPostfix() + this.right.toPostfix() + this.getOperator();
   }   

   /**
    * @return a String that represents the expression in infix notation
    */
   public String toInfix()
   {
       return "(" + this.left.toInfix() + this.getOperator() + this.right.toInfix() + ")";
   }

   /**
    * @return a new Expression mathematically equivalent to this one, but simplified.
    */

   public Expression simplify() {
       Expression myLeft = left.simplify();
       Expression myRight = right.simplify();

       if(left instanceof IntegerOperand && right instanceof IntegerOperand) {
	       IntegerOperand op1 = (IntegerOperand) left;
	       IntegerOperand op2 = (IntegerOperand) right;
	       
	       return new IntegerOperand(op1.operand / op2.operand);
	   }

       else if(myLeft instanceof IntegerOperand && myRight instanceof Expression) {
	       IntegerOperand a = (IntegerOperand) myLeft;
	       Expression expr = (Expression) myRight;

	       if(a.operand == 0) {
		       return new IntegerOperand(0); // if left is 0 return 0
		   }
	   }

       else if(myRight instanceof IntegerOperand && myLeft instanceof Expression) {
	       IntegerOperand b = (IntegerOperand) myRight;
	       Expression expr1 = (Expression) myLeft;

	       if(b.operand == 1) {
		       return myLeft.simplify(); // if right is 1 call recursively 
		   }
	   }
       
       else if(left instanceof VariableOperand && right instanceof VariableOperand) {
	       VariableOperand var1 = (VariableOperand) myLeft;
	       VariableOperand var2 = (VariableOperand) myRight;
	       
	       if(var1.equals(var2)) {
		       return new IntegerOperand(1); // if both are same (even if variables) return 1
		   }
	   }
       return new QuotientExpression(myLeft.simplify(), myRight.simplify());
   }   
    
   /**
    * Evaluates the expression given assignments of values to variables.
    * @param assignments a HashMap from Strings (variable names) to Integers (values).
    * @return the result of evaluating the expression with the given variable assignments
    */
   public int evaluate(HashMap<String, Integer> assignments) {
       return this.left.evaluate(assignments) / this.right.evaluate(assignments);
   }

   /**
    * @return a Set of the variables contained in this expression
    */
   public Set<String> getVariables() {
       TreeSet<String> vars = new TreeSet<String>();
       vars.addAll(this.left.getVariables());
       vars.addAll(this.right.getVariables());  //make a new tree and add all the lefts and rights
       return vars;
   }

   /**
    * @param obj and Object to compare to
    * @return true if obj is an IntegerOperand with the same associated value
    */
   @Override
   public boolean equals(Object obj) {
       if(obj == null || (!(obj instanceof QuotientExpression)))
           return false;
      
       QuotientExpression sum = (QuotientExpression) obj;

       // same as difference Expression
       return this.left.equals(sum.left) && this.right.equals(sum.right);
   }
}
