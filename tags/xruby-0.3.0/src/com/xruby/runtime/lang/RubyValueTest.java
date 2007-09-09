/**
 * Copyright 2005-2007 Xue Yong Zhi
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.lang;

import com.xruby.runtime.value.ObjectFactory;
import junit.framework.TestCase;

public class RubyValueTest extends TestCase {
    public void test_equals() {
        assertFalse(ObjectFactory.FIXNUM1.equals(ObjectFactory.NIL_VALUE));
        assertFalse(ObjectFactory.NIL_VALUE.equals(ObjectFactory.FIXNUM1));
    }
    
    public void testTrue() {
    	assertTrue(ObjectFactory.TRUE_VALUE.isTrue());
    	assertFalse(ObjectFactory.FALSE_VALUE.isTrue());
    	assertFalse(ObjectFactory.NIL_VALUE.isTrue());
    	assertTrue(ObjectFactory.FIXNUM0.isTrue());
    	assertTrue(ObjectFactory.FIXNUM1.isTrue());
    	assertTrue(ObjectFactory.createString("").isTrue());
    	assertTrue((ObjectFactory.createString("XXX").isTrue()));
    }
    
    public void testRespondTo() {
    	RubyValue v = RubyConstant.QNIL;
    	assertTrue(v.respondTo(RubyID.RESPOND_TO_P));
    	assertFalse(v.respondTo(RubyID.intern("balabala")));
    }
    
    public void testConvertoInt() {
    	RubyValue v = ObjectFactory.createFixnum(0);
    	assertEquals(0, v.toInt());
    	    	
    	v = RubyConstant.QTRUE;
    	try {
    		v.toInt();
    		fail("true can't be convert to true");
    	} catch (RubyException e) {    		
    	}
    }
}