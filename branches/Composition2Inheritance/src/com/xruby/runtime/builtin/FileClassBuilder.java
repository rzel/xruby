package com.xruby.runtime.builtin;

import java.io.File;
import java.math.BigInteger;
import java.util.Date;

import com.xruby.runtime.lang.*;
import com.xruby.runtime.value.*;



class File_basename extends RubyMethod {
	public File_basename() {
		super(2, false, 1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String fileName = RubyTypesUtil.convertToString(args.get(0)).toString();
		String basename = new File(fileName).getName();
		
		if (args.size() == 1){
			return ObjectFactory.createString(basename);
		}
		
		String suffix = RubyTypesUtil.convertToString(args.get(1)).toString();
		if (basename.endsWith(suffix)){
			return ObjectFactory.createString(basename.substring(0, basename.length() - suffix.length()));
		}
		return ObjectFactory.createString(basename);
	}
}

class File_delete extends RubyMethod {
	public File_delete() {
		super(-1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		int deleted = 0;
		if (args!= null){
			for (int i=0; i<args.size(); ++i){
				String fileName = RubyTypesUtil.convertToString(args.get(i)).toString();
				File file = new File(fileName);
				if (file.isDirectory()){
					throw new RubyException(RubyRuntime.RuntimeErrorClass, "Is a directory - " + fileName);
				}else if(file.isFile()){
					file.delete();
					++ deleted;
				}else{
					throw new RubyException(RubyRuntime.RuntimeErrorClass, "No such file or directory - " + fileName);
				}
			}
		}
		return ObjectFactory.createFixnum(deleted);
	}
}

class File_separator extends RubyMethod {
	public File_separator() {
		super(0);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		return ObjectFactory.createString(File.separator);
	}
}

class File_file_question extends RubyMethod {
	public File_file_question() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String fileName = RubyTypesUtil.convertToString(args.get(0)).toString();
		File file = new File(fileName);
		if (file.isFile()){
			return ObjectFactory.trueValue;
		}
		return ObjectFactory.falseValue;
	}
}

class File_expand_path extends RubyMethod {
	public File_expand_path() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String fileName = RubyTypesUtil.convertToString(args.get(0)).toString();
		File file = new File(fileName);
		return ObjectFactory.createString(file.getAbsolutePath());
	}
}

class File_dirname extends RubyMethod {
	public File_dirname() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String fileName = RubyTypesUtil.convertToString(args.get(0)).toString();
		File file = new File(fileName);
		String parent = file.getParent();
		if (parent == null){
			return ObjectFactory.createString(".");
		}
		return ObjectFactory.createString(parent);
	}
}

class File_mtime extends RubyMethod {
	public File_mtime() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String fileName = RubyTypesUtil.convertToString(args.get(0)).toString();
		File file = new File(fileName);
		if (!file.isFile() && !file.isDirectory()){
			throw new RubyException(RubyRuntime.RuntimeErrorClass, "No such file or directory - " + fileName);
		}
		return ObjectFactory.createTime(new Date(file.lastModified()));
	}
}

class File_size extends RubyMethod {
	public File_size() {
		super(1);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String fileName = RubyTypesUtil.convertToString(args.get(0)).toString();
		File file = new File(fileName);
		if (!file.isFile() && !file.isDirectory()){
			throw new RubyException(RubyRuntime.RuntimeErrorClass, "No such file or directory - " + fileName);
		}
		return RubyBignum.bignorm(BigInteger.valueOf(file.length()));
	}
}

class File_rename extends RubyMethod {
	public File_rename() {
		super(2);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String file1 = RubyTypesUtil.convertToString(args.get(0)).toString();
		String file2 = RubyTypesUtil.convertToString(args.get(1)).toString();
		File file = new File(file1);
		if (!file.isFile() && !file.isDirectory()){
			throw new RubyException(RubyRuntime.RuntimeErrorClass, "No such file or directory - " + file1);
		}
		return ObjectFactory.createBoolean(file.renameTo(new File(file2)));
	}
}

class File_open extends RubyMethod {
	public File_open() {
		super(3, false, 2);
	}

	protected RubyValue run(RubyValue receiver, RubyArray args, RubyBlock block) {
		String filename = RubyTypesUtil.convertToString(args.get(0)).toString();
		String mode = "r";
		RubyValue arg1 = args.get(1);
		if (arg1 != ObjectFactory.nilValue){
			mode = RubyTypesUtil.convertToString(arg1).toString();
		}
		return ObjectFactory.createFile(filename, mode);
	}
}

public class FileClassBuilder {

	public static void initialize() {
		RubyClass c = RubyRuntime.FileClass;
		//c.defineMethod("id2name", new Symbol_id2name());
		
		c.defineSingletonMethod("file?", new File_file_question());
		c.defineSingletonMethod("expand_path", new File_expand_path());
		c.defineSingletonMethod("dirname", new File_dirname());
		c.defineSingletonMethod("delete", new File_delete());
		c.defineSingletonMethod("basename", new File_basename());
		c.defineSingletonMethod("separator", new File_separator());
		c.defineSingletonMethod("mtime", new File_mtime());
		c.defineSingletonMethod("size", new File_size());
		c.defineSingletonMethod("open", new File_open());
		c.defineSingletonMethod("rename", new File_rename());
		
	}

}
