/** 
 * Copyright (c) 2005-2006 Xue Yong Zhi. All rights reserved.
 */

package com.xruby.core.builtin;

import com.xruby.core.lang.*;
import com.xruby.core.value.*;

class Array_length extends RubyMethod {
	public Array_length() {
		super(0);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue value = (ArrayValue)receiver.getValue();
		return ObjectFactory.createFixnum(value.size());
	}
}

class Array_to_s extends RubyMethod {
	public Array_to_s() {
		super(0);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue value = (ArrayValue)receiver.getValue();
		return value.to_s();
	}
}

class Array_to_a extends RubyMethod {
	public Array_to_a() {
		super(0);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		return receiver;
	}
}

class Array_array_access extends RubyMethod {
	public Array_array_access() {
		super(-1);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue value = (ArrayValue)receiver.getValue();
		if (1 == args.size()) {
			Object argValue = args.get(0).getValue();
			if (argValue instanceof IntegerValue) {
				IntegerValue index = (IntegerValue)argValue;
				return value.get(index.intValue());
			} else if (argValue instanceof RangeValue) {				
				RangeValue range = (RangeValue)argValue;
				return value.get(range);
			}
		}
		
		//TODO
		throw new RubyException("not implemented");
	}
}

class Array_array_set extends RubyMethod {
	public Array_array_set() {
		super(-1);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		//TODO index can be range, -1 etc
		ArrayValue value = (ArrayValue)receiver.getValue();
		IntegerValue index = (IntegerValue)args.get(0).getValue();
		return value.set(index.intValue(), args.get(1));
	}
}

class Array_array_equal extends RubyMethod {
	public Array_array_equal() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue left = (ArrayValue)receiver.getValue();
		Object right = args.get(0).getValue();
		if (!(right instanceof ArrayValue)) {
			return ObjectFactory.falseValue;
		}
		return left.equals((ArrayValue)right);
	}
}

class Array_concat extends RubyMethod {
	public Array_concat() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue left = (ArrayValue)receiver.getValue();
		left.concat(args.get(0));
		return receiver;
	}
}

class Array_plus extends RubyMethod {
	public Array_plus() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue left = (ArrayValue)receiver.getValue();
		ArrayValue right = (ArrayValue)args.get(0).getValue();
		ArrayValue resultArray = left.plus(right);
		return ObjectFactory.createArray(resultArray);
	}
}

class Array_times extends RubyMethod {
	public Array_times() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, ArrayValue args, RubyBlock block) throws RubyException {
		ArrayValue array = (ArrayValue)receiver.getValue();
		IntegerValue times = (IntegerValue)args.get(0).getValue();
		ArrayValue resultArray = array.times(times.intValue());		
		
		return ObjectFactory.createArray(resultArray);
	}
}

public class ArrayClassBuilder {
	
	public static RubyClass create() {
		RubyClass c = RubyRuntime.GlobalScope.defineNewClass("Array", RubyRuntime.ObjectClass);
		c.defineMethod("length", new Array_length());
		c.defineMethod("to_s", new Array_to_s());
		c.defineMethod("to_a", new Array_to_a());
		c.defineMethod("[]", new Array_array_access());
		c.defineMethod("[]=", new Array_array_set());
		c.defineMethod("==", new Array_array_equal());
		c.defineMethod("concat", new Array_concat());
		c.defineMethod("+", new Array_plus());
		c.defineMethod("*", new Array_times());
		return c;
	}
}

