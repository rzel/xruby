/** 
 * Copyright 2005-2007 Xue Yong Zhi
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.compiler.codedom;

public class YieldExpressionTest extends TestingAstTestCase {
	public void test_nil() {
		String program_text = "yield nil";
		
		String expected_result = 
			"yield\n" +
			"[:1:1:false\n" +
			"nil\n" +
			"]!\n" +
			"end yield\n" +
			"EOF";
		assertAstOutput(program_text, expected_result);
	}
	
	public void test_asterisk_nil() {
		String program_text = "yield *nil";
		
		String expected_result = 
			"yield\n" +
			"[:0:0:true\n" +
			"nil\n" +
			"[]*\n" +
			"]!\n" +
			"end yield\n" +
			"EOF";
		assertAstOutput(program_text, expected_result);
	}
	
}