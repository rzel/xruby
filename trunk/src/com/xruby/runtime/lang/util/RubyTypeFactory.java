/**
 * Copyright 2007 Ye Zheng
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.lang.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.CheckClassAdapter;

import com.xruby.compiler.XRubyClassLoader;
import com.xruby.compiler.codegen.CgConfig;
import com.xruby.compiler.codegen.CgUtil;
import com.xruby.compiler.codegen.ClassDumper;
import com.xruby.compiler.codegen.Types;
import com.xruby.runtime.lang.RubyBlock;
import com.xruby.runtime.lang.RubyClass;
import com.xruby.runtime.lang.RubyMethod;
import com.xruby.runtime.lang.RubyModule;
import com.xruby.runtime.lang.RubyValue;
import com.xruby.runtime.lang.annotation.RubyAllocMethod;
import com.xruby.runtime.lang.annotation.RubyLevelConstant;
import com.xruby.runtime.lang.annotation.RubyLevelMethod;
import com.xruby.runtime.value.RubyArray;

public abstract class RubyTypeFactory {
	public static RubyClass getClass(Class klass) {	
		Class loadClass = new RubyClassFactory(klass).loadClass();
		if (loadClass == null) { 
			return null; 
		}
		
		return loadRubyClassBuilder(loadClass).createRubyClass();
	}

	private static RubyClassBuilder loadRubyClassBuilder(Class loadClass) {
		try {
			return (RubyClassBuilder)loadClass.newInstance();
		} catch (InstantiationException ie) {
			throw new RuntimeException("fail to create Ruby class", ie);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException("fail to create Ruby class", iae);
		}
	}
	
	public static RubyModule getModule(Class klass) {
		Class loadClass = new RubyModuleFactory(klass).loadClass();
		if (loadClass == null) { 
			return null; 
		}
		
		return loadRubyModuleBuilder(loadClass).createRubyModule();
	}
	
	private static RubyModuleBuilder loadRubyModuleBuilder(Class loadClass) {
		try {
			return (RubyModuleBuilder)loadClass.newInstance();
		} catch (InstantiationException ie) {
			throw new RuntimeException("fail to create Ruby class", ie);
		} catch (IllegalAccessException iae) {
			throw new RuntimeException("fail to create Ruby class", iae);
		}
	}
	
	Class loadClass() {
		String name = getBuilderName(klass);
		Class klass = tryClass(name);
		if (klass == null) {
			return createClass(name);
		}
		
		return klass;
	}

	private Class createClass(String name) {
		Annotation annotation = klass.getAnnotation(this.getTypeAnnotationClass());
		if (annotation == null) {
			return null;
		}
		
		startBuilder(name);
		createBuilderMethod(annotation);
		endClassBuilder();
		
		return loadBuidlerClass(name);
	}
	
	private Class loadBuidlerClass(String name) {
		byte[] content = cw.toByteArray();
        dupmer.dump(name, content);
		return cl.load(name, content);
	}
	
	private Class tryClass(String name) {
		try {
			return cl.load(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static final Type methodFactoryType = Type.getType(MethodFactory.class);
	private static final Type methodTypeType = Type.getType(MethodType.class);
	private static final XRubyClassLoader cl = new XRubyClassLoader();
	private static ClassDumper dupmer = new ClassDumper();
	
	Class klass;
	ClassWriter cw;
	ClassVisitor cv;
	
	RubyTypeFactory(Class klass) {
		this.klass = klass;
		this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		this.cv = new CheckClassAdapter(cw);
	}
	
	protected abstract Class getTypeAnnotationClass();
	protected abstract String getBuilderName(Class klass);
	protected abstract Class getInterface();
	protected abstract Method getBuilderMethod();
	protected abstract int createRubyType(GeneratorAdapter mg, Annotation annotation);
	protected abstract String createMethodFactoryName();
	protected abstract boolean isModule();
	
	private void startBuilder(String name) {
		this.cv.visit(CgConfig.TARGET_VERSION, Opcodes.ACC_PUBLIC, name, 
				null, CgUtil.getInternalName(Object.class), new String[] { CgUtil.getInternalName(getInterface())});
		
		CgUtil.createImplicitConstructor(this.cv, Type.getType(Object.class));
	}
	
	private GeneratorAdapter startBuilderMethod() {
		GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC,
                getBuilderMethod(), null, null, this.cv);
		mg.visitCode();
		return mg;
	}
	
	private void endClassBuilder() {
		cv.visitEnd();
	}
	
	private void createBuilderMethod(Annotation annotation) {	
		GeneratorAdapter mg = startBuilderMethod();
		int rubyTypeIdx = createRubyType(mg, annotation);
		generateMethod(mg, rubyTypeIdx);
		generateConstant(mg, rubyTypeIdx);
		
		mg.loadLocal(rubyTypeIdx);		
        mg.returnValue();
        mg.endMethod();
	}
	
	private void generateConstant(GeneratorAdapter mg, int rubyTypeIdx) {
		for (Field field : klass.getFields()) {
			int modifier = field.getModifiers();
			if (!(Modifier.isStatic(modifier) || Modifier.isPublic(modifier))) {
				continue;
			}
			
			Annotation rawFieldAnnotation = field.getAnnotation(RubyLevelConstant.class);
			if (rawFieldAnnotation == null) {
				continue;
			}
			
			defineRubyConstant(mg, field, (RubyLevelConstant)rawFieldAnnotation, rubyTypeIdx);
		}
	}

	private void defineRubyConstant(GeneratorAdapter mg, Field field, 
			RubyLevelConstant constant, int rubyTypeIdx) {
		mg.loadLocal(rubyTypeIdx);
		mg.push(constant.name());
		mg.getStatic(Type.getType(this.klass), field.getName(), Type.getType(field.getType()));
		mg.invokeVirtual(Types.RUBY_MODULE_TYPE, Method.getMethod(
				CgUtil.getMethodName("setConstant", RubyValue.class, String.class, RubyValue.class)));
	}

	private void generateMethod(GeneratorAdapter mg, int rubyTypeIdx) {
		int factoryIdx = createLocalMethodFactory(mg);
		
		Map<String, CgMethodItem> methodMap = new HashMap<String, CgMethodItem> ();
		CgMethodItem allocItem = null;
		
		for (java.lang.reflect.Method method : klass.getMethods()) {
			Annotation rawMethodAnnotation = method.getAnnotation(RubyLevelMethod.class);
			if (rawMethodAnnotation != null) {
				CgMethodItem newItem = createNormalItem((RubyLevelMethod)rawMethodAnnotation, method);
				CgMethodItem item = methodMap.get(newItem.name);
				if (item != null) {
					item.type = MethodType.valueOf((item.type.value() | newItem.type.value()));
				} else {
					methodMap.put(newItem.name, newItem);
				}
				
				continue;
			}
			
			Annotation allocMethodAnnotation = method.getAnnotation(RubyAllocMethod.class);
			if (allocMethodAnnotation != null) {
				CgMethodItem item = createAllocItem((RubyAllocMethod)allocMethodAnnotation, method);
				if (allocItem != null) {
					allocItem.type = MethodType.valueOf(allocItem.type.value() | (item.type.value()));
				} else {
					allocItem = createAllocItem((RubyAllocMethod)allocMethodAnnotation, method);
				}
				continue;
			}
		}
		
		for (CgMethodItem item : methodMap.values()) {	
			defineRubyMethod(mg, rubyTypeIdx, factoryIdx, item);
		}
		
		if (allocItem != null) {
			defineAllocMethod(mg, rubyTypeIdx, factoryIdx, allocItem);
		}
	}
	
	private CgMethodItem createNormalItem(RubyLevelMethod annotation, java.lang.reflect.Method method) {
		CgMethodItem item = new CgMethodItem();
		item.name = annotation.name();
		item.javaName = method.getName();
		item.alias = annotation.alias();
		makeItemPros(method, item);
		
		return item;
	}
	
	private CgMethodItem createAllocItem(RubyAllocMethod annotation, java.lang.reflect.Method method) {
		CgMethodItem item = new CgMethodItem();
		item.name = null;
		item.javaName = method.getName();
		item.alias = null;
		makeItemPros(method, item);
		
		return item;
	}

	private void makeItemPros(java.lang.reflect.Method method, CgMethodItem item) {
		item.singleton = false;
		Class[] paramTypes = method.getParameterTypes();
		int start = 0;
		int end = paramTypes.length - 1;
		if (Modifier.isStatic(method.getModifiers())) {
			if (paramTypes[0] != RubyValue.class) {
				throw new IllegalArgumentException("unknown Ruby method specification:" + method);
			}
			
			if (!isModule()) {
				item.singleton = true;
			}
			
			start++;
		}
		
		item.block = false;
		if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1] == RubyBlock.class) {
			item.block = true;
			end--;
		}
		
		item.type = getMethodType(paramTypes, start, end);
		if (item.type == MethodType.UNKNOWN) {
			throw new IllegalArgumentException("unknown Ruby method specification:" + method);
		}
	}

	private MethodType getMethodType(Class[] paramTypes, int start, int end) {
		int argSize = end - start;
		if (argSize < 0) {
			return MethodType.NO_ARG;
		} else if (argSize == 0) {
			if (paramTypes[start] == RubyValue.class) {
				return MethodType.ONE_ARG;
			} else if (paramTypes[start] == RubyArray.class) {
				return MethodType.VAR_ARG;
			}
		} else if (argSize == 1) {
			if (paramTypes[start] == RubyValue.class && paramTypes[end] == RubyValue.class) {
				return MethodType.TWO_ARG;
			}
		}

		return MethodType.UNKNOWN;
	}
	
	private void defineAllocMethod(GeneratorAdapter mg, int rubyTypeIdx, int factoryIdx, CgMethodItem item) {
		mg.loadLocal(rubyTypeIdx);
		mg.loadLocal(factoryIdx);
		
		getMethod(mg, item.javaName, item.type, true, item.block);
		mg.invokeVirtual(Types.RUBY_CLASS_TYPE, 
				Method.getMethod(CgUtil.getMethodName("defineAllocMethod", Void.TYPE, RubyMethod.class)));
	}
	
	private int createLocalMethodFactory(GeneratorAdapter mg) {
		createMehtodFactory(mg, klass);
		int factoryIdx = mg.newLocal(methodFactoryType);
		mg.storeLocal(factoryIdx);
		return factoryIdx;
	}
	
	private void defineRubyMethod(GeneratorAdapter mg, int rubyTypeIdx, 
			int factoryIdx, CgMethodItem item) {
		mg.loadLocal(rubyTypeIdx);
		if (item.singleton) {
			mg.invokeVirtual(Types.RUBY_MODULE_TYPE, 
					Method.getMethod(CgUtil.getMethodName("getSingletonClass", RubyClass.class)));
		}
		String annotationName = item.name;
		mg.push(annotationName);

		mg.loadLocal(factoryIdx);
		getMethod(mg, item.javaName, item.type, item.singleton, item.block);
		
		mg.invokeVirtual(Types.RUBY_MODULE_TYPE, 
				Method.getMethod(CgUtil.getMethodName("defineMethod", RubyValue.class, String.class, RubyMethod.class)));
		
		defineAlias(mg, rubyTypeIdx, annotationName, item.alias);
	}

	private void defineAlias(GeneratorAdapter mg, int rubyTypeIdx, String oldName, String[] aliases) {
		for (String alias : aliases) {
			mg.loadLocal(rubyTypeIdx);
			mg.push(alias);
			mg.push(oldName);
			mg.invokeVirtual(Types.RUBY_MODULE_TYPE, 
					Method.getMethod(CgUtil.getMethodName("aliasMethod", Void.TYPE, String.class, String.class)));
		}
	}
	
	private void getMethod(GeneratorAdapter mg, String methodName, MethodType type, boolean singleton, boolean block) {
		mg.push(methodName);
		mg.getStatic(methodTypeType, type.toString(), methodTypeType);
		mg.push(singleton);
		mg.push(block);
		mg.invokeVirtual(methodFactoryType, Method.getMethod(
				CgUtil.getMethodName("getMethod", RubyMethod.class, String.class, MethodType.class, Boolean.TYPE, Boolean.TYPE)));
	}

	private void createMehtodFactory(GeneratorAdapter mg, Class klass) {
		mg.push(Type.getType(klass));		
		mg.invokeStatic(methodFactoryType, 
				Method.getMethod(
						CgUtil.getMethodName(createMethodFactoryName(), MethodFactory.class, Class.class)));
	}
}
