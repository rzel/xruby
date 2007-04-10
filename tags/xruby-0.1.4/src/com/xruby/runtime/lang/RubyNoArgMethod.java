/** 
 * Copyright 2007 Ye Zheng
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.lang;

import com.xruby.runtime.value.RubyArray;

/**
 * 
 * Ruby method with no arg.
 *
 */
public abstract class RubyNoArgMethod extends RubyMethod {
	public RubyNoArgMethod() {
		super(0, false, 0);
	}

	protected abstract RubyValue run(RubyValue receiver, RubyBlock block);
	
	protected RubyValue run(RubyValue receiver, RubyValue arg, RubyArray args, RubyBlock block) {
		return this.run(receiver, block);
	}
}
