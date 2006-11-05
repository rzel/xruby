/** 
 * Copyright (c) 2005-2006 Xue Yong Zhi. All rights reserved.
 */

package com.xruby.compiler.codedom;

public class WhileExpression extends Expression {
	private final Expression condition_;
	private final CompoundStatement body_;
	boolean is_until_;
	
	public WhileExpression(Expression condition, CompoundStatement body, boolean is_until) {
		assert(null != condition);
		condition_ = condition;
		body_ = body;
		is_until_ = is_until;
	}
	
	public boolean will_not_be_executed() {
		if (!is_until_ && conditionIsAlwayFalse(condition_)) {
			//while (false)
			return true;
		} else if (is_until_ && conditionIsAlwayTrue(condition_)) {
			//while (true)
			return true;
		} else {
			return false;
		}
	}
	
	public void accept(CodeVisitor visitor) {
		//We used to generate code for body first, then condition (javac does this as well, it will help to create a little more efficient code).
		//But later we changed it back, because local variable may be first assigned in while expression's condition,  and we are replying
		//codegen backend to to maintain a symbol table -- so we should not change the consequence of visiting.

		visitor.visitWhileConditionBegin();
		condition_.accept(visitor);
		visitor.visitWhileConditionEnd(is_until_);

		if (null != body_) {
			body_.accept(visitor);
		}
		
		visitor.visitWhileBodyEnd();
	}

}
