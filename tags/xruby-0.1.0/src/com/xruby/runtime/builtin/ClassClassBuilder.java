/** 
 * Copyright 2005-2007 Xue Yong Zhi
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.builtin;

import com.xruby.runtime.lang.*;
import com.xruby.runtime.value.*;

class Class_new extends RubyMethod {
	public Class_new() {
		super(0);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {		
		RubyClass r = (RubyClass)receiver;
		RubyValue v = r.invokeAllocMethod(receiver, args, block);
		callInitializeMethod(v, args, block);
		return v;
	}

	private void callInitializeMethod(RubyValue v, RubyArray args, RubyBlock block) {
		RubyMethod m = v.findMethod("initialize");
		if (m != null) {
			m.invoke(v, args, block);
		}
	}
}

public class ClassClassBuilder {

	static RubyMethod class_new_ = new Class_new();
	
	public static void initialize() {
		RubyClass c = RubyRuntime.ClassClass;
		c.defineMethod("new", class_new_);
	}
}