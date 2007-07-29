/**
 * Copyright 2007 Ye Zheng
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.lang.util;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.xruby.compiler.codegen.CgUtil;
import com.xruby.runtime.lang.RubyBlock;
import com.xruby.runtime.lang.RubyValue;
import com.xruby.runtime.value.RubyArray;

public class VarArgRunMethodHelper extends RunMethodHelper {
	protected String getRunName() {
		return CgUtil.getMethodName("run", RubyValue.class, 
				RubyValue.class, RubyArray.class, RubyBlock.class);
	}
	
	protected void loadBlock(GeneratorAdapter mg) {
		mg.loadArg(2);
	}
	
	protected Class[] getParams(boolean block) {
		return block ? new Class[] {RubyArray.class, RubyBlock.class} : new Class[] {RubyArray.class};
	}

	protected void loadArgs(GeneratorAdapter mg) {
		mg.loadArg(1);
	}

}