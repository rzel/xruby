/**
 * Copyright 2005-2007 Xue Yong Zhi
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.compiler.codegen;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import com.xruby.runtime.lang.RubyBinding;

class ClassGeneratorForRubyProgram extends ClassGenerator {
    private RubyBinding binding_;
    private String fileName;

    public ClassGeneratorForRubyProgram(String name, String fileName, RubyBinding binding) {
        super(name);
        this.fileName = fileName;
        mg_for_run_method_ = visitRubyProgram(binding, fileName);
        binding_ = binding;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private MethodGenerator visitRubyProgram(RubyBinding binding, String fileName) {
        cv_.visit(Opcodes.V1_5,
                Opcodes.ACC_PUBLIC,
                name_,
                null,										// signature
                "com/xruby/runtime/lang/RubyProgram",		// superName
                null										// interface
                );

        // set source file's name, for debug
        if(fileName != null) {
            cv_.visitSource(fileName, null);
        }

        createImplicitConstructor(cv_);
        createStaticVoidMain(cv_);

        //Implement RubyProgram
        return new MethodGenerator(Opcodes.ACC_PROTECTED,
                Method.getMethod("com.xruby.runtime.lang.RubyValue run(com.xruby.runtime.lang.RubyValue, com.xruby.runtime.value.RubyArray, com.xruby.runtime.lang.RubyBlock, com.xruby.runtime.lang.RubyModule)"),
                cv_,
                binding,
                null,
                false);
    }

    private void createImplicitConstructor(ClassVisitor cw) {
        Method m = Method.getMethod("void <init> ()");
        MethodGenerator mg = new MethodGenerator(Opcodes.ACC_PUBLIC,
                m,
                cw,
                null,
                null,
                false);
        mg.loadThis();
        mg.invokeConstructor(Types.RUBY_PROGRAM_TYPE, m);
        mg.returnValue();
        mg.endMethod();
    }

    private void createStaticVoidMain(ClassVisitor cv) {
        MethodGenerator mg = new MethodGenerator(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                Method.getMethod("void main (String[])"),
                cv,
                null,
                null,
                false);

        mg.loadArg(0);
        mg.invokeStatic(Types.RUBY_RUNTIME_TYPE,
                Method.getMethod("void init(String[])"));

        Type program = Type.getType("L" + name_ + ";");
        mg.newInstance(program);
        mg.dup();
        mg.invokeConstructor(program,
                Method.getMethod("void <init> ()"));
        mg.invokeVirtual(program,
                Method.getMethod("com.xruby.runtime.lang.RubyValue invoke()"));
        mg.pop();

        mg.invokeStatic(Types.RUBY_RUNTIME_TYPE,
                Method.getMethod("void fini()"));

        mg.returnValue();
        mg.endMethod();
    }

    public void storeVariable(String name) {
        updateBinding(binding_, name);

        super.storeVariable(name);
    }

}

