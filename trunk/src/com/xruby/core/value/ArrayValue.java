/** 
 * Copyright (c) 2005-2006 Xue Yong Zhi. All rights reserved.
 */

package com.xruby.core.builtin;

import java.util.*;

import com.xruby.core.lang.*;

/**
 * @breif Internal representation of a ruby array 
 * 
 */
public class ArrayValue {
	private ArrayList<RubyValue> values_;

	public ArrayValue(int size) {
		values_ = new ArrayList<RubyValue>(size);
	}
	
	public void add(RubyValue v) {
		values_.add(v);
	}
	
	public int size() {
		return values_.size();
	}

	public RubyValue[] toArray() {
		if (values_.isEmpty()) {
			return null;
		} else {
			return values_.toArray(new RubyValue[values_.size()]);
		}
	}
	
	public RubyValue[] toArray2() {
		//def f; ar = [1, 2,3,4]; yield ar; end; f {|a,b,*c| print c}
		if (values_.size() == 1 && values_.get(0).getValue() instanceof ArrayValue) {
			return ((ArrayValue)values_.get(0).getValue()).toArray();
		} else {
			return toArray();
		}
	}

	public RubyValue set(int index, RubyValue value) throws RubyException {
		if (index < 0) {
			index = values_.size() + index;
		}

		if (index < 0) {
			//FIXME throw IndexError
			throw new RubyException("IndexError");
		}
	
		if (index < values_.size()) {
			values_.set(index, value);
		} else {
			values_.ensureCapacity(index + 1);
			for (int i = values_.size(); i < index; ++i) {
				values_.add(ObjectFactory.nilValue);
			}
			values_.add(value);
		}

		return value;
	}
	
	public RubyValue get(int index) {
		if (index < 0) {
			index = values_.size() + index;
		}
		
		if (index < 0 || index >= size()) {
			return ObjectFactory.nilValue;
		} else {
			return values_.get(index);
		}
	}

	public RubyValue equals(ArrayValue that) throws RubyException {
		if (values_.size() != that.size()) {
			return ObjectFactory.falseValue;
		}

		for (int i = 0; i < values_.size(); ++i) {
			if (RubyRuntime.callPublicMethod(this.get(i), that.get(i), "==") == ObjectFactory.falseValue) {
				return ObjectFactory.falseValue;
			}
		}

		return ObjectFactory.trueValue;
	}

	public RubyValue to_s() throws RubyException {
		StringBuilder r = new StringBuilder();
		
		for (RubyValue v : values_) {
			RubyValue s = RubyRuntime.callPublicMethod(v, null, "to_s");
			r.append((StringBuilder)s.getValue());
		}
		
		return ObjectFactory.createString(r);
	}
	
	ArrayList<RubyValue> getInternal() {
		return values_;
	}
	
	public void concat(RubyValue v) throws RubyException {
		Object o = v.getValue();
		if (v.getRubyClass() != RubyRuntime.ArrayClass) {//TODO use RuyRuntime.testInstanceOf() ?
			throw new RubyException(RubyRuntime.TypeErrorClass,
					"can't convert " + v.getRubyClass().toString() + " into Array");
		}

		values_.addAll(((ArrayValue)o).getInternal());
	}

	public void expand(RubyValue v) {
		Object o = v.getValue();
		if (o instanceof ArrayValue) {
			//[5,6,*[1, 2]]
			values_.addAll(((ArrayValue)o).getInternal());	
		} else {
			//[5,6,*1], [5,6,*nil]
			values_.add(v);
		}
	}

	public static RubyValue expandArrayIfThereIsZeroOrOneValue(ArrayValue a) {
		if (a.size() <= 1) {
			return a.get(0);
		} else {
			return ObjectFactory.createArray(a);
		}
	}
	
	public static ArrayValue expandArrayIfThereIsOnlyOneArrayValue(ArrayValue a) {
		if (a.size() == 1 &&
				a.get(0).getValue() instanceof ArrayValue) {
			return (ArrayValue)a.get(0).getValue();
		} else {
			return a;
		}
	}

	//create a new Array containing every element from index to the end
	public RubyValue collect(int index) {
		assert(index >= 0);

		final int size = values_.size() - index;
		if (size < 0) {
			return ObjectFactory.createEmptyArray();
		}

		ArrayValue v = new ArrayValue(size);
		for (int i = index; i < values_.size(); ++i) {
			v.add(values_.get(i));
		}

		return ObjectFactory.createArray(v);
	}
}

