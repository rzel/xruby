/** 
 * Copyright 2005-2007 Ye Zheng
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.lang;

public class RubySingletonClass extends RubyClass {

	public RubySingletonClass() {
		super("", null, null);
	}

	public boolean isSingleton() {
		return true;
	}

	public boolean isReal() {
		return false;
	}
}
