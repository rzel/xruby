/**
 * Copyright 2005-2007 Xue Yong Zhi, Ye Zheng
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.lang;

import java.util.HashMap;
import java.util.Map;

import com.xruby.runtime.builtin.AttrReader;
import com.xruby.runtime.builtin.AttrWriter;
import com.xruby.runtime.builtin.ObjectFactory;
import com.xruby.runtime.builtin.RubyArray;
import com.xruby.runtime.builtin.RubyKernelModule;
import com.xruby.runtime.builtin.RubyProc;
import com.xruby.runtime.builtin.RubyString;
import com.xruby.runtime.builtin.RubyTypesUtil;
import com.xruby.runtime.lang.annotation.DummyMethod;
import com.xruby.runtime.lang.annotation.RubyLevelClass;
import com.xruby.runtime.lang.annotation.RubyLevelMethod;

@RubyLevelClass(name="Module", superclass="Object", dummy={
		@DummyMethod(name="included", privateMethod=true),
		@DummyMethod(name="extended", privateMethod=true),
		@DummyMethod(name="method_added", privateMethod=true),
		@DummyMethod(name="method_removed", privateMethod=true),
	    @DummyMethod(name="method_undefined", privateMethod=true)
})
public class RubyModule extends MethodCollection {
    private RubyModule owner_ = null;//owner is where is the module is defined under.
    protected RubyClass superclass_;
    private int current_access_mode_ = RubyMethod.PUBLIC;
    protected Map<RubyID, RubyValue> instance_varibles_ = null;

    public RubyModule(String name, RubyModule owner) {
        super(null);
        super.name_ = name;
        owner_ = owner;
    }
    void setScope(RubyModule owner) {
        this.owner_ = owner;
    }

    public boolean isRealModule() {
        return true;
    }

    public boolean isRealClass() {
        return false;
    }
    
    public void setAccessPublic() {
        current_access_mode_ = RubyMethod.PUBLIC;
    }

    public void setAccessPrivate() {
        current_access_mode_ = RubyMethod.PRIVATE;
    }

    public void setAccessMode(int access) {
        current_access_mode_ = access;
    }

    public RubyValue defineMethod(String name, RubyMethod m) {
        return addMethod(RubyID.intern(name), m, this.current_access_mode_);
    }
    
    public RubyValue defineMethod(RubyID mid, RubyMethod m) {
        return addMethod(mid, m, this.current_access_mode_);
    }
    
    public RubyValue definePrivateMethod(String name, RubyMethod m) {
    	return addMethod(RubyID.intern(name), m, RubyMethod.PRIVATE);
    }
    
    public void defineModuleMethod(String name, RubyMethod m) {
    	this.definePrivateMethod(name, m);
    	this.getSingletonClass().defineMethod(name, m.clone());
    }

    protected RubyValue addMethod(RubyID id, RubyMethod m, int attribute) {
    	m.setScope(this);
    	
        RubyValue v = super.addMethod(id, m, attribute);
        if (RubyRuntime.running && id != RubyID.ID_ALLOCATOR) {
            RubyAPI.callOneArgMethod(this, id.toSymbol(), null, RubyID.methodAddedID);
        }
        return v;
    }
    
    public boolean isMethodBound(RubyID id, boolean check) {
    	RubyMethod m = this.findOwnMethod(id);
    	if (null != m && !UndefMethod.isUndef(m)) {
    		return true;
    	}
    	
    	return false;
    }
    
    public boolean isKindOf(RubyModule klass) {
		RubyModule m = this;
		
		while (m != null) {
			if (m == klass || m.methods_ == klass.methods_) {
				return true;
			}
			
			m = m.superclass_;
		}
		
		return false;
	}

    /// This method is only used by java classes in package 'com.xruby.runtime.builtin'.
    /// It has less overhead than 'defineClass' (no hash table lookup).
    /// This method is NOT used by classes compiled from ruby script.
    public RubyClass defineNewClass(String name, RubyClass parent) {
        if (parent == null) {
            parent = RubyRuntime.ObjectClass;
        }

        RubyClass klass = ClassFactory.makeRubyClass(parent);
        if (null != name) {
            //Class can be nameless when they are created by Struct.new (e.g. Struct.new(:name, :address))
            klass.setScope(this);
            klass.setName(name);
            constants_.put(name, klass);
        }
        ClassFactory.inheritedClass(parent, klass);

        ObjectSpace.add(klass);
        return klass;
    }

    private RubyClass defineClass(String name, RubyClass parent) {
        RubyValue v = null;
        if (null != name) {
            v = constants_.get(name);
        }
        if (null == v) {
            return defineNewClass(name, parent);
        }

        if (!(v instanceof RubyClass)) {
            throw new RubyException(RubyRuntime.TypeErrorClass, name + " is not a class");
        }

        RubyClass c = (RubyClass)v;

        if (null != parent) {
            if (!c.isMyParent(parent)){
                throw new RubyException(RubyRuntime.TypeErrorClass, "superclass mismatch for class "+ name);
            }
        }

        c.setAccessPublic();
        return c;
    }

    /// define a new class or get a old one
    public RubyClass defineClass(String name, RubyValue parent) {
        if (null != parent && !(parent instanceof RubyClass)) {
            throw new RubyException(RubyRuntime.TypeErrorClass, "superclass must be a Class (" + parent.getRubyClass().getName() + " given)");
        }

        return defineClass(name, null == parent ? null : (RubyClass)parent);
    }

    public RubyModule defineNewModule(String name) {
        RubyModule m = new RubyModule(name, this);
        m.setName(name);
        m.setRubyClass(RubyRuntime.ModuleClass);
        constants_.put(name, m);//NOTE, do not use ObjectFactory.createClass, it will cause initialization issue
        return m;
    }

    public RubyModule defineModule(String name) {
        RubyValue v = constants_.get(name);
        if (null == v) {
            return defineNewModule(name);
        }

        if (!(v instanceof RubyModule) || (v instanceof RubyClass)) {
            throw new RubyException(RubyRuntime.TypeErrorClass, name + " is not a module");
        }

        return (RubyModule)v;
    }

    public RubyValue getConstant(String name) {
        RubyValue v = super.getOwnConstant(name);
        if (null != v) {
            return v;
        }

        if (null != this.superclass_) {
            v = this.superclass_.getConstant(name);
            if (null != v) {
                return v;
            }
        }

        if (null != owner_) {
            v = this.owner_.getConstant(name);
            if (null != v) {
                return v;
            }
        }

        return null;
    }

    public void to_s(RubyString s) {
        if (null != owner_) {
            owner_.to_s(s);
            if (s.length() > 0) {
                s.appendString("::");
            }
        }

        String name = this.getName();
        if (name != null) {
            s.appendString(getName());
        }
    }

    /*
     * Include Module
     */
    public void includeModule(RubyModule module) {
        if (module == null) {
            return;
        }
        RubyModule c = this;
        boolean changed = false;
        while (module != null) {
            boolean superclassSeen = false;
            if (c.methods_ == module.methods_) {
                throw new RubyException(RubyRuntime.ArgumentErrorClass, "cyclic include detected");
            }

            boolean skip = false;

            for (RubyClass p = this.superclass_; p != null; p = p.superclass_) {
                if (p instanceof RubyIncludeClass) {
                    if (p.methods_ == module.methods_) {
                        if (!superclassSeen) {
                            c = p;
                            skip = true;
                            break;
                        }
                    }
                } else if (p.isRealClass()) {
                    superclassSeen = true;
                    break;
                }
            }

            if (!skip) {
                c.superclass_ = new RubyIncludeClass(module, c.superclass_);
                c = c.superclass_;
                changed = true;
            }

            module = module.superclass_;
        }

        if (changed) {
            RubyClass.resetCache();
        }
    }

    public final void collectIncludedModuleNames(RubyArray a) {
        for (RubyModule m = this; m != null; m = m.superclass_) {
            if (m.isRealModule()) {
                a.add(m);
            } else if (m instanceof RubyIncludeClass) {
                a.add(((RubyIncludeClass)m).getIncludedModule());
            }
        }
    }

    public void module_function(String method_name) {
        RubyID mid = RubyID.intern(method_name);
        RubyMethod m = findOwnMethod(mid);
        if (null == m || UndefMethod.isUndef(m)) {
            throw new RubyException(RubyRuntime.NoMethodErrorClass, "undefined method '" +  method_name + "' for " + getName());
        }
        getSingletonClass().defineMethod(method_name, m);
    }
    
    // Class object instance variable
    public RubyValue getInstanceVariable(RubyID id) {
		if (null == instance_varibles_) {
            return RubyConstant.QNIL;
        }

        RubyValue v = instance_varibles_.get(id);
        
        return (null != v) ? v : RubyConstant.QNIL;
	}

	public RubyValue setInstanceVariable(RubyValue value, RubyID id) {
		if (null == instance_varibles_) {
            instance_varibles_ = new HashMap<RubyID, RubyValue>();
        }

        instance_varibles_.put(id, value);
        return value;
	}
    
    // Class Variable    
    public RubyValue getClassVariable(String name) {
		RubyModule klass = this;
		RubyID id = RubyID.intern(name);
		
		while (klass != null) {
			if (klass.instance_varibles_ != null) {
				RubyValue v = klass.instance_varibles_.get(id);
				if (v != null) {
					return v;
				}
			}
			
			klass = klass.superclass_;
		}
		
		throw new RubyException(RubyRuntime.NameErrorClass,
				"uninitialized class variable " + name + " in " + (null == name_ ? "Object" : name_)); 
	}
    
    public RubyValue setClassVariable(RubyValue value, String name) {
    	RubyModule klass = this;
		RubyID id = RubyID.intern(name);
		
		while (klass != null) {
			if (klass.instance_varibles_ != null) {
				klass.instance_varibles_.put(id, value);
				return value;
			}
			
			klass = klass.superclass_;
		}
		
		this.setInstanceVariable(value, id);
		return value;
    }
    
    @RubyLevelMethod(name="attr_reader", privateMethod=true)
    public RubyValue attrReader(RubyArray args) {
        for (RubyValue v : args) {
            RubyID id = v.toID();
            this.defineMethod(id, new AttrReader(id));
        }

        return RubyConstant.QNIL;
    }
    
    @RubyLevelMethod(name="attr_writer", privateMethod=true)
    public RubyValue attrWriter(RubyArray args) {
        for (RubyValue v : args) {
            RubyID id = v.toID();
            this.defineMethod(id + "=", new AttrWriter(id));
        }

        return RubyConstant.QNIL;
    }
    
    @RubyLevelMethod(name="attr_accessor", privateMethod=true)
    public RubyValue attrAccessor(RubyArray args) {
        for (RubyValue v : args) {
            RubyID id = v.toID();
            this.defineMethod(id, new AttrReader(id));
            this.defineMethod(id + "=", new AttrWriter(id));
        }

        return RubyConstant.QNIL;
    }
    
    @RubyLevelMethod(name="attr", privateMethod=true)
    public RubyValue attr(RubyValue arg) {
        RubyID id = arg.toID();
        this.defineMethod(id, new AttrReader(id));
        return RubyConstant.QNIL;
    }
    
    @RubyLevelMethod(name="attr", privateMethod=true)
    public RubyValue attr(RubyValue arg0, RubyValue arg1) {
        RubyID id = arg0.toID();
        this.defineMethod(id, new AttrReader(id));

        if (arg1 != RubyConstant.QFALSE && arg1 != RubyConstant.QNIL) {
            this.defineMethod(id + "=", new AttrWriter(id));
        }

        return RubyConstant.QNIL;
    }
    
    private static void setAccess(int access, RubyModule c, RubyArray args) {
        if (null == args) {
            c.setAccessMode(access);
            return;
        }

        for (RubyValue arg : args) {
            String method_name;
            if (arg instanceof RubyString) {
                method_name = arg.toString();
            } else if (arg instanceof RubySymbol) {
                method_name = arg.toString();
            } else {
                throw new RubyException(RubyRuntime.TypeErrorClass, arg.toString() + " is not a symbol");
            }

            RubyID mid = RubyID.intern(method_name);

            if (c.setAccess(mid, access) == null) {
                throw new RubyException(RubyRuntime.NameErrorClass, "undefined method '" + method_name + "' for " + c.getName());
            }
        }
    }
    
    @RubyLevelMethod(name="public", privateMethod=true)
    public RubyValue modPublic(RubyArray args) {
        setAccess(RubyMethod.PUBLIC, this, args);
        return this;
    }
    
    @RubyLevelMethod(name="protected", privateMethod=true)
    public RubyValue modProtected(RubyArray args) {
        setAccess(RubyMethod.PROTECTED, this, args);
        return this;
    }
    
    @RubyLevelMethod(name="private", privateMethod=true)
    public RubyValue modPrivate(RubyArray args) {
        setAccess(RubyMethod.PRIVATE, this, args);
        return this;
    }
    
    @RubyLevelMethod(name="private_class_method")
    public RubyValue private_class_method(RubyArray args) {
    	setAccess(RubyMethod.PRIVATE, this.getSingletonClass(), args);
        return this;
    }

    @RubyLevelMethod(name="public_class_method")
    public RubyValue public_class_method(RubyArray args) {
    	setAccess(RubyMethod.PUBLIC, this.getSingletonClass(), args);
        return this;
    }
    
    @RubyLevelMethod(name="to_s", alias="name")
    public RubyValue modName() {
        RubyString s = ObjectFactory.createString();
        this.to_s(s);
        return s;
    }
    
    @RubyLevelMethod(name="inspect")
    public RubyValue rubyInspect() {
        return RubyAPI.callNoArgMethod(this, null, RubyID.toSID);
    }
    
    @RubyLevelMethod(name="include")
    public RubyValue include() {
        return this;
    }
	
    @RubyLevelMethod(name="include")
	public RubyValue include(RubyValue arg) {
        this.includeModule((RubyModule)arg);
        return this;
    }
	
    @RubyLevelMethod(name="include")
    public RubyValue include(RubyArray args) {
    	for (RubyValue m : args) {
    		this.includeModule((RubyModule) m);
    	}

        return this;
    }
    
    @RubyLevelMethod(name="extend_object")
    public RubyValue extendObject(RubyValue arg) {
        arg.getSingletonClass().includeModule(this);
        return arg;
    }
    
    @RubyLevelMethod(name="<=>")
    public RubyValue opSpaceship(RubyValue arg) {
        if (this == arg) {
            return ObjectFactory.FIXNUM0;
        }

        if (!(arg instanceof RubyModule)) {
            return RubyConstant.QNIL;
        }

        RubyModule other_module = (RubyModule) arg;

        // FIXME: could be Module
        if (this instanceof RubyClass && other_module instanceof RubyClass) {
            RubyClass c1 = (RubyClass) this;
            RubyClass c2 = (RubyClass) other_module;
            if (c1.isKindOf(c2)) {
                return ObjectFactory.FIXNUM_NEGATIVE_ONE;
            } else if (c2.isKindOf(c1)) {
                return ObjectFactory.FIXNUM1;
            }
        }

        return RubyConstant.QNIL;
    }
    
    
    @RubyLevelMethod(name="<")
    public RubyValue opLt(RubyValue arg) {
        if (!(arg instanceof RubyModule)) {
            throw new RubyException(RubyRuntime.TypeErrorClass, "compared with non class/module");
        }

        return compareModule(this, arg);
    }
    
    @RubyLevelMethod(name=">")
    public RubyValue opGt(RubyValue arg) {
        if (!(arg instanceof RubyModule)) {
            throw new RubyException(RubyRuntime.TypeErrorClass, "compared with non class/module");
        }

        return compareModule(arg, this);
    }
    
    @RubyLevelMethod(name=">=")
    public RubyValue opGe(RubyValue arg) {
        if (!(arg instanceof RubyModule)) {
            throw new RubyException(RubyRuntime.TypeErrorClass, "compared with non class/module");
        }

        if (arg == this) {
           return RubyConstant.QTRUE;
        }

        return compareModule(arg, this);
    }
    
    private static RubyValue compareModule(RubyValue module, RubyValue other_module) {
        if (module == other_module) {
           return RubyConstant.QFALSE;
        }

        if (module instanceof RubyClass && other_module instanceof RubyClass) {
            RubyClass c1 = (RubyClass) module;
            RubyClass c2 = (RubyClass) other_module;
            if (c1.isKindOf(c2)) {
                return RubyConstant.QTRUE;
            } else if (c2.isKindOf(c1)) {
                return RubyConstant.QFALSE;
            }
        }

        return RubyConstant.QNIL;
    }
    
    @RubyLevelMethod(name="===")
    public RubyValue caseEqual(RubyValue arg) {
        if (this instanceof RubyClass) {
        	// FIXME: Maybe Module
            return ObjectFactory.createBoolean(RubyAPI.isKindOf(this, arg));
        } else {
            //TODO does not work as expected
            RubyModule module = (RubyModule)this;
            RubyArray a = new RubyArray();
            module.collectIncludedModuleNames(a);
            return a.include(arg.getRubyClass());
        }
    }
    
    @RubyLevelMethod(name="ancestors")
    public RubyValue ancestors() {
        RubyArray r = new RubyArray();
        this.collectIncludedModuleNames(r);
        return r;
    }

    @RubyLevelMethod(name="public_instance_methods")
    public RubyValue public_instance_methods(RubyArray args) {
    	return get_instance_methods(this, args, RubyMethod.PUBLIC);
    }

    @RubyLevelMethod(name="protected_instance_methods")
    public RubyValue protected_instance_methods(RubyArray args) {
    	return get_instance_methods(this, args, RubyMethod.PROTECTED);
    }

    @RubyLevelMethod(name="private_instance_methods")
    public RubyValue private_instance_methods(RubyArray args) {
    	return get_instance_methods(this, args, RubyMethod.PRIVATE);
    }
    
    private RubyValue get_instance_methods(RubyValue receiver, RubyArray args, int mode) {
        boolean include_super = false;
        if (args != null && args.get(0).isTrue()) {
            include_super = true;
        }

        RubyArray a = new RubyArray();
        if (include_super) {
            ((RubyClass)receiver).collectClassMethodNames(a, mode);
        } else {
            ((RubyModule)receiver).collectOwnMethodNames(a, mode);
        }
        return a;
    }
    
    @RubyLevelMethod(name="module_function")
    public RubyValue module_function() {
        return this;
    }
    
    @RubyLevelMethod(name="module_function")
    public RubyValue module_function(RubyArray args) {
    	for (RubyValue v : args) {
    		RubySymbol s = (RubySymbol) v;
    		this.module_function(s.toString());
    	}

    	return this;
    }
    
    @RubyLevelMethod(name="module_eval", alias="class_eval")
    public RubyValue module_eval(RubyArray args, RubyBlock block) {
        //TODO duplicated code: instance_eval
        if (null == args && null == block) {
            throw new RubyException(RubyRuntime.ArgumentErrorClass, "block not supplied");
        }

        if (null != args) {
            RubyString program_text = (RubyString) args.get(0);
            RubyBinding binding = new RubyBinding();
            binding.setScope(this);
            binding.setSelf(this);
            return RubyKernelModule.eval(program_text, binding, null);
        } else {
            block.setScope(this);
            block.setSelf(this);
            return block.invoke(this);
        }
    }
    
    @RubyLevelMethod(name="const_get")
    public RubyValue constGet(RubyValue arg) {
        RubySymbol s = RubyTypesUtil.convertToSymbol(arg);
        return RubyAPI.getConstant(this, s.toString());
    }
    
    @RubyLevelMethod(name="const_set")
    public RubyValue constSet(RubyValue arg1, RubyValue arg2) {
        RubySymbol s = RubyTypesUtil.convertToSymbol(arg1);
        return RubyAPI.setConstant(arg2, this, s.toString());
    }
    
    @RubyLevelMethod(name="define_method")
    public RubyValue define_method(RubyArray args, RubyBlock block) {

        final RubyBlock b;
        if (null != args && args.size() == 1 && null != block) {
            b = block;
        } else {
            b = ((RubyProc)args.get(1)).getBlock();
        }

        String name = RubyTypesUtil.convertToJavaString(args.get(0));
        RubyMethod method = new RubyVarArgMethod() {
            protected RubyValue run(RubyValue _receiver, RubyArray _args, RubyBlock _block) {
                b.setArgsOfCurrentMethod(_args);
                return b.invoke(_receiver, _args);
            }
        };

        b.setCurrentMethod(method);
        return this.defineMethod(name, method);
    }
    
    @RubyLevelMethod(name="remove_method")
    public RubyValue remove_method(RubyArray args) {
        for (RubyValue arg : args) {
            String method_name = RubyTypesUtil.convertToJavaString(arg);
            this.undefMethod(method_name);
        }

        return this;
    }
    
    @RubyLevelMethod(name="new")
    public static RubyValue newModule(RubyValue receiver, RubyBlock block) {
        return RubyAPI.defineModule("");
    }
}
