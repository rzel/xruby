package com.xruby.runtime.builtin;

import com.xruby.runtime.lang.*;
import com.xruby.runtime.value.*;

public class CoreBuilder implements ExtensionBuilder {	

	public void initialize() {
		RubyRuntime.objectClass = RubyAPI.defineBootClass("Object", null);
		RubyRuntime.moduleClass = RubyAPI.defineBootClass("Module", RubyRuntime.objectClass);
		RubyRuntime.classClass = RubyAPI.defineBootClass("Class", RubyRuntime.moduleClass);
		
		RubyClass metaClass = RubyAPI.createMetaClass(RubyRuntime.objectClass, RubyRuntime.classClass);
		metaClass = RubyAPI.createMetaClass(RubyRuntime.moduleClass, metaClass);
		metaClass = RubyAPI.createMetaClass(RubyRuntime.classClass, metaClass);
		
		RubyRuntime.kernelModule = RubyAPI.defineModule("Kernel");
		RubyRuntime.objectClass.includeModule(RubyRuntime.kernelModule);
		RubyRuntime.objectClass.defineAllocMethod(ClassMethod.alloc);
		
		RubyRuntime.objectClass.definePrivateMethod("initialize", RubyMethod.DUMMY_METHOD, 0);
		RubyRuntime.classClass.definePrivateMethod("inherited", RubyMethod.DUMMY_METHOD, 1);
		RubyRuntime.moduleClass.definePrivateMethod("included", RubyMethod.DUMMY_METHOD, 1);
		RubyRuntime.moduleClass.definePrivateMethod("extended", RubyMethod.DUMMY_METHOD, 1);
		RubyRuntime.moduleClass.definePrivateMethod("method_added", RubyMethod.DUMMY_METHOD, 1);
		RubyRuntime.moduleClass.definePrivateMethod("method_removed", RubyMethod.DUMMY_METHOD, 1);
		RubyRuntime.moduleClass.definePrivateMethod("method_undefined", RubyMethod.DUMMY_METHOD, 1);
		
		RubyRuntime.kernelModule.defineMethod("nil?", RubyMethod.FALSE_METHOD, 0);
		
		
		RubyRuntime.kernelModule.defineMethod("==", KernelMethod.objectEqual, 1);
		RubyRuntime.kernelModule.defineMethod("equal?", KernelMethod.objectEqual, 1);
		RubyRuntime.kernelModule.defineMethod("===", KernelMethod.equal, 1);
		//kernelModule.defineMethod("=~", new ObjectPatternMatch(), 1);
		
		RubyRuntime.kernelModule.defineMethod("eql?", KernelMethod.objectEqual, 1);
		//RubyRuntime.rbKernelModule.defineMethod("id", new ObjIdObsoleteMethod(), 0);
		RubyRuntime.kernelModule.defineMethod("type", KernelMethod.objectType, 0);
	    RubyRuntime.kernelModule.defineMethod("class", KernelMethod.objectClass, 0);
		
		//rb_define_method(rb_mKernel, "clone", rb_obj_clone, 0);
	    //rb_define_method(rb_mKernel, "dup", rb_obj_dup, 0);
	    //rb_define_method(rb_mKernel, "initialize_copy", rb_obj_init_copy, 1);
		
		/*
		rb_define_method(rb_mKernel, "taint", rb_obj_taint, 0);
	    rb_define_method(rb_mKernel, "tainted?", rb_obj_tainted, 0);
	    rb_define_method(rb_mKernel, "untaint", rb_obj_untaint, 0);
	    rb_define_method(rb_mKernel, "freeze", rb_obj_freeze, 0);
	    rb_define_method(rb_mKernel, "frozen?", rb_obj_frozen_p, 0);
	    
	    */

	    RubyRuntime.kernelModule.defineMethod("to_a", KernelMethod.anyToA, 0);
	    RubyRuntime.kernelModule.defineMethod("to_s", KernelMethod.anyToS, 0);
	    RubyRuntime.kernelModule.defineMethod("inspect", KernelMethod.objectInspect, 0);
	    RubyRuntime.kernelModule.defineMethod("methods", KernelMethod.methods, -1);
	    /*
	    rb_define_method(rb_mKernel, "singleton_methods", 
			     rb_obj_singleton_methods, -1); // in class.c 
			     */
	    RubyRuntime.kernelModule.defineMethod("protected_methods", KernelMethod.protectedMethods, -1);
	    RubyRuntime.kernelModule.defineMethod("private_methods", KernelMethod.privateMethods, -1);
	    RubyRuntime.kernelModule.defineMethod("private_methods", KernelMethod.publicMethods, -1);
	    /*
	    rb_define_method(rb_mKernel, "instance_variables", 
			     rb_obj_instance_variables, 0); // in variable.c
	    rb_define_method(rb_mKernel, "instance_variable_get", rb_obj_ivar_get, 1);
	    rb_define_method(rb_mKernel, "instance_variable_set", rb_obj_ivar_set, 2);
	    rb_define_private_method(rb_mKernel, "remove_instance_variable",
				     rb_obj_remove_instance_variable, 1); // in variable.c
		*/

	    RubyRuntime.kernelModule.defineMethod("instance_of?", KernelMethod.isInstanceOf, 1);
	    RubyRuntime.kernelModule.defineMethod("kind_of?", KernelMethod.isKindOf, 1);
	    RubyRuntime.kernelModule.defineMethod("is_a?", KernelMethod.isKindOf, 1);
	    
	    /*
	    rb_define_private_method(rb_mKernel, "singleton_method_added", rb_obj_dummy, 1);
	    rb_define_private_method(rb_mKernel, "singleton_method_removed", rb_obj_dummy, 1);
	    rb_define_private_method(rb_mKernel, "singleton_method_undefined", rb_obj_dummy, 1);

	    rb_define_global_function("sprintf", rb_f_sprintf, -1); // in sprintf.c
	    rb_define_global_function("format", rb_f_sprintf, -1);  // in sprintf.c
	    rb_define_global_function("Integer", rb_f_integer, 1);
	    rb_define_global_function("Float", rb_f_float, 1);

	    rb_define_global_function("String", rb_f_string, 1);
	    rb_define_global_function("Array", rb_f_array, 1);

	    rb_define_method(rb_cModule, "freeze", rb_mod_freeze, 0);
	    rb_define_method(rb_cModule, "===", rb_mod_eqq, 1);
	    */
	    RubyRuntime.moduleClass.defineMethod("==", KernelMethod.objectEqual, 1);
	    /*
	    rb_define_method(rb_cModule, "<=>",  rb_mod_cmp, 1);
	    rb_define_method(rb_cModule, "<",  rb_mod_lt, 1);
	    rb_define_method(rb_cModule, "<=", rb_class_inherited_p, 1);
	    rb_define_method(rb_cModule, ">",  rb_mod_gt, 1);
	    rb_define_method(rb_cModule, ">=", rb_mod_ge, 1);
	    rb_define_method(rb_cModule, "initialize_copy", rb_mod_init_copy, 1); // in class.c
	    rb_define_method(rb_cModule, "to_s", rb_mod_to_s, 0);
	    rb_define_method(rb_cModule, "included_modules", 
			     rb_mod_included_modules, 0); // in class.c
	    rb_define_method(rb_cModule, "include?", rb_mod_include_p, 1); // in class.c
	    rb_define_method(rb_cModule, "name", rb_mod_name, 0);  // in variable.c
	    rb_define_method(rb_cModule, "ancestors", rb_mod_ancestors, 0); // in class.c

	    rb_define_private_method(rb_cModule, "attr", rb_mod_attr, -1);
	    rb_define_private_method(rb_cModule, "attr_reader", rb_mod_attr_reader, -1);
	    rb_define_private_method(rb_cModule, "attr_writer", rb_mod_attr_writer, -1);
	    rb_define_private_method(rb_cModule, "attr_accessor", rb_mod_attr_accessor, -1);

	    rb_define_alloc_func(rb_cModule, rb_module_s_alloc);
	    rb_define_method(rb_cModule, "initialize", rb_mod_initialize, 0);
	    rb_define_method(rb_cModule, "instance_methods", 
			     rb_class_instance_methods, -1);           // in class.c
	    rb_define_method(rb_cModule, "public_instance_methods", 
			     rb_class_public_instance_methods, -1);    // in class.c
	    rb_define_method(rb_cModule, "protected_instance_methods", 
			     rb_class_protected_instance_methods, -1); // in class.c
	    rb_define_method(rb_cModule, "private_instance_methods", 
			     rb_class_private_instance_methods, -1);   // in class.c

	    rb_define_method(rb_cModule, "constants", rb_mod_constants, 0); // in variable.c
	    rb_define_method(rb_cModule, "const_get", rb_mod_const_get, 1);
	    rb_define_method(rb_cModule, "const_set", rb_mod_const_set, 2);
	    rb_define_method(rb_cModule, "const_defined?", rb_mod_const_defined, 1);
	    rb_define_private_method(rb_cModule, "remove_const", 
				     rb_mod_remove_const, 1); // in variable.c
	    rb_define_method(rb_cModule, "const_missing", 
			     rb_mod_const_missing, 1); // in variable.c
	    rb_define_method(rb_cModule, "class_variables", 
			     rb_mod_class_variables, 0); // in variable.c
	    rb_define_private_method(rb_cModule, "remove_class_variable", 
				     rb_mod_remove_cvar, 1); // in variable.c
	    rb_define_private_method(rb_cModule, "class_variable_get", rb_mod_cvar_get, 1);
	    rb_define_private_method(rb_cModule, "class_variable_set", rb_mod_cvar_set, 2);

	    rb_define_method(rb_cClass, "allocate", rb_obj_alloc, 0);
	    */
		
		RubyRuntime.classClass.defineMethod("new", ClassMethod.newInstance, -1);
	    
	    /*
	    rb_define_method(rb_cClass, "initialize", rb_class_initialize, -1);
	    rb_define_method(rb_cClass, "initialize_copy", rb_class_init_copy, 1); // in class.c
	    rb_define_method(rb_cClass, "superclass", rb_class_superclass, 0);
	    rb_define_alloc_func(rb_cClass, rb_class_s_alloc);
	    rb_undef_method(rb_cClass, "extend_object");
	    rb_undef_method(rb_cClass, "append_features");
	    */
	}
}

class ClassMethod {
	private static RubyID init = StringMap.intern("initialize");
	
	public static RubyMethod alloc = new RubyNoArgMethod() {
		protected RubyValue run(RubyValue receiver) {
			return new RubyObject((RubyClass)receiver);
			// FIXME: type cast exception
		}
	};
	
	public static RubyMethod newInstance = new NoBlockRubyMethod() {
		public RubyValue run(RubyValue receiver, RubyArray args) throws RubyException {
			RubyValue obj = receiver.callMethod(RubyID.ID_ALLOCATOR, args);			
			obj.callMethod(init, args);
			
			return obj;
		}
	};
}
