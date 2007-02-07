/** 
 * Copyright 2005-2007 Xue Yong Zhi
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.builtin;

import com.xruby.runtime.lang.*;
import com.xruby.runtime.value.*;

class Struct_new extends RubyVarArgMethod {
	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		if (!(args.get(0) instanceof RubyString)) {
			//TODO It is almost impossible to implement this:
			//Customer = Struct.new(:name, :address)
			//a = Customer.new("Dave", "123 Main")
			//print a.class
			throw new RubyException("not implemented!");
		} else {
			RubyClass c = RubyRuntime.StructClass.defineClass(((RubyString)args.get(0)).toString(), RubyRuntime.ObjectClass);
			addMethods(c, args);
			return c;
		}
	}

	private void addMethods(RubyClass c, final RubyArray super_args) {
		for (int i = 1; i < super_args.size(); ++i) {
			String s = RubyTypesUtil.convertToSymbol(super_args.get(i)).toString();
			c.defineMethod(s, new AttrReader(s));
			c.defineMethod(s + "=", new AttrWriter(s));
		}
		
		c.defineAllocMethod(new RubyMethod(super_args.size() - 1) {
				protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
					RubyValue v = new RubyObject((RubyClass)receiver);
					for (int i = 0; i < args.size(); ++i) {
						v.setInstanceVariable(args.get(i), "@" + RubyTypesUtil.convertToSymbol(super_args.get(i + 1)).toString());
					}
					return v;
				}
				
			}
		);

		c.defineMethod("[]", new RubyMethod(1) {
				protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
					String name;
					if (args.get(0) instanceof RubyString) {
						name = ((RubyString)args.get(0)).toString();
					} else if (args.get(0) instanceof RubySymbol) {
						name = ((RubySymbol)args.get(0)).toString();
					} else if (args.get(0) instanceof RubyFixnum) {
						int i = ((RubyFixnum)args.get(0)).intValue();
						name = RubyTypesUtil.convertToSymbol(super_args.get(i + 1)).toString();
					} else {
						throw new RubyException(RubyRuntime.TypeErrorClass, "can't convert " + args.get(0).getRubyClass().getName() + " into Integer");
					}

					return receiver.getInstanceVariable("@" + name);
				}
			}
		);

		c.defineMethod("[]=", new RubyMethod(2) {
				protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
					String name;
					if (args.get(0) instanceof RubyString) {
						name = ((RubyString)args.get(0)).toString();
					} else if (args.get(0) instanceof RubySymbol) {
						name = ((RubySymbol)args.get(0)).toString();
					} else if (args.get(0) instanceof RubyFixnum) {
						int i = ((RubyFixnum)args.get(0)).intValue();
						name = RubyTypesUtil.convertToSymbol(super_args.get(i + 1)).toString();
					} else {
						throw new RubyException(RubyRuntime.TypeErrorClass, "can't convert " + args.get(0).getRubyClass().getName() + " into Integer");
					}

					return receiver.setInstanceVariable(args.get(1), "@" + name);
				}
			}
		);

		c.defineMethod("to_a", new RubyMethod(0) {
				protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
					RubyArray a = new RubyArray(super_args.size() - 1);
					for (int i = 1; i < super_args.size(); ++i) {
						String name = RubyTypesUtil.convertToSymbol(super_args.get(i)).toString();
						a.add(receiver.getInstanceVariable("@" + name));
					}
					return a;
				}
			}
		);
	}
}

public class StructClassBuilder {
	public static void initialize() {
		RubyClass c = RubyRuntime.StructClass;
		c.defineAllocMethod(new Struct_new());
		//c.defineMethod("to_s", new Struct_to_s());
	}
}
