package com.xruby.codedom;

public class NextExpression extends Expression {

	private ReturnArguments arguments_;
	
	public NextExpression(ReturnArguments arguments){
		arguments_ = arguments;
	}
	
	public void accept(CodeVisitor visitor) {
		visitor.visitNextBegin();
		
		if (null != arguments_) { 
			arguments_.accept(visitor);
		} else {
			visitor.visitNilExpression();
		}

		visitor.visitNextEnd();
	}
}
