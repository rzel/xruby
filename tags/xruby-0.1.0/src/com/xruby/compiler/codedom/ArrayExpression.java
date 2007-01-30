/** 
 * Copyright 2005-2007 Xue Yong Zhi
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.compiler.codedom;

import java.util.*;

public class ArrayExpression extends Expression {
	private ArrayList<Expression> elements_;
	private Expression asterisk_element_;
	private boolean notSingleAsterisk_;
	
	public ArrayExpression() {
		elements_ = new ArrayList<Expression>();
		asterisk_element_ = null;
		notSingleAsterisk_ = true;
	}

	ArrayExpression(ArrayList<Expression> elements, Expression asterisk_element) {
		elements_ = elements;
		asterisk_element_ = asterisk_element;
		notSingleAsterisk_ = elements.size() > 0;
	}
	
	public void addElement(Expression e) {
		elements_.add(e);
	}

	public void setAsteriskElement(Expression e) {
		asterisk_element_ = e;
	}

	public ArrayList<Expression> getElements() {
		return elements_;
	}
	
	public void accept(CodeVisitor visitor) {
		visitor.visitArrayBegin(elements_.size(), notSingleAsterisk_);
		
		for (Expression e : elements_) {
			e.accept(visitor);
			visitor.visitArrayElement(false, e instanceof MethodCallExpression || e instanceof YieldExpression);
		}

		if (null != asterisk_element_) {
			asterisk_element_.accept(visitor);
			visitor.visitArrayElement(true, asterisk_element_ instanceof MethodCallExpression || asterisk_element_ instanceof YieldExpression);
		}
		
		visitor.visitArrayEnd();
	}
	
}
