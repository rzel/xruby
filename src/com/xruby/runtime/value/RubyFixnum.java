/**
 * Copyright 2005-2007 Xue Yong Zhi, Ye Zheng, Jie Li
 * Distributed under the GNU General Public License 2.0
 */

package com.xruby.runtime.value;

import java.math.BigInteger;

import com.xruby.runtime.lang.*;
import com.xruby.runtime.lang.annotation.MethodType;
import com.xruby.runtime.lang.annotation.RubyLevelClass;
import com.xruby.runtime.lang.annotation.RubyLevelMethod;

@RubyLevelClass(name="Fixnum", superclass="Integer")
public class RubyFixnum extends RubyInteger {
    private final int value_;

    RubyFixnum(int i) {
        value_ = i;
    }
    
    public void setRubyClass(RubyClass klass) {
		throw new RubyException(RubyRuntime.TypeErrorClass, this.getRubyClass().getName() + " can't be set class");
	}
	
	public RubyClass getSingletonClass() {
    	throw new RubyException(RubyRuntime.TypeErrorClass, this.getRubyClass().getName() + " can't define singleton");
    }

	public RubyValue clone() {
		throw new RubyException(RubyRuntime.TypeErrorClass, "can't clone " + this.getRubyClass().getName());
	}
    
	public int toInt() {
		return value_;
	}
	
	public double toFloat() {
		return this.value_;
	}

	public RubyFixnum convertToInteger() {
		return this;
    }

	public int hashCode() {
        return value_;
    }

    public RubyClass getRubyClass() {
        return RubyRuntime.FixnumClass;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof RubyFixnum) {
            return value_ == ((RubyFixnum)o).value_;
        } else {
            return super.equals(o);
        }
    }

    public String toString() {
        return Integer.toString(value_);
    }

    public String toString(int radix) {
        return Integer.toString(value_, radix);
    }
    
    @RubyLevelMethod(name="-@")
    public RubyValue uminus() {
    	return ObjectFactory.createFixnum(-this.value_);
    }
    
    @RubyLevelMethod(name="+", type=MethodType.ONE_ARG)
    public RubyValue opPlus(RubyValue v) {
    	if (v instanceof RubyFixnum) {
    		return RubyBignum.bignorm((long)this.value_ + (long)v.toInt());
    	}
    	
    	if (v instanceof RubyFloat) {
            return ObjectFactory.createFloat(value_ + ((RubyFloat)v).doubleValue());
    	}
    	
    	if (v instanceof RubyBignum) {
    		BigInteger bigValue1 = BigInteger.valueOf(value_);
    		BigInteger bigValue2 = ((RubyBignum)v).getInternal();
    		return RubyBignum.bignorm(bigValue1.add(bigValue2));
    	}
    	
    	return coerceBin(RubyID.plusID, v);
    }
    
    @RubyLevelMethod(name="-", type=MethodType.ONE_ARG)
    public RubyValue opMinus(RubyValue v) {
    	if (v instanceof RubyFixnum) {
    		return RubyBignum.bignorm((long)this.value_ - (long)v.toInt());
    	}
    	
    	if (v instanceof RubyFloat) {
    		return ObjectFactory.createFloat(this.value_ - ((RubyFloat)v).doubleValue());
    	}
    	
    	if (v instanceof RubyBignum) {
    		BigInteger bigValue1 = BigInteger.valueOf(this.value_);
            BigInteger bigValue2 = ((RubyBignum)v).getInternal();
            return RubyBignum.bignorm(bigValue1.subtract(bigValue2));
    	}
    	
    	return coerceBin(RubyID.subID, v);
    }
    
    @RubyLevelMethod(name="*", type=MethodType.ONE_ARG)
    public RubyValue opMul(RubyValue v) {
    	if (v instanceof RubyFixnum) {
    		return RubyBignum.bignorm((long)this.value_ * (long)v.toInt());
    	}
    	
    	if (v instanceof RubyFloat) {
    		return ObjectFactory.createFloat(this.value_ * ((RubyFloat)v).doubleValue());
    	}
    	
    	if (v instanceof RubyBignum) {
    		BigInteger bigValue1 = BigInteger.valueOf(this.value_);
            BigInteger bigValue2 = ((RubyBignum)v).getInternal();
            return RubyBignum.bignorm(bigValue1.multiply(bigValue2));
    	}
    	
    	return coerceBin(RubyID.mulID, v);
    }
    
    @RubyLevelMethod(name="/", type=MethodType.ONE_ARG)
    public RubyValue opDiv(RubyValue v) {
    	if (v instanceof RubyFixnum) {
    		int intValue1 = this.value_;
            int intValue2 = v.toInt();
    		int div = intValue1 / intValue2;
            int mod = intValue1 - div * intValue2;
            if (mod != 0 && div < 0) {
                --div;
            }
            return RubyBignum.bignorm(div);
    	} else if (v instanceof RubyFloat) {
    		return ObjectFactory.createFloat(this.value_ / v.toFloat());
    	} else if (v instanceof RubyBignum) {
    		BigInteger bigValue1 = BigInteger.valueOf(this.value_);
            BigInteger bigValue2 = ((RubyBignum)v).getInternal();
            return RubyBignum.bignorm(bigValue1.divide(bigValue2));
    	} else {
    		return coerceBin(RubyID.divID, v);
    	}
    }
    
    @RubyLevelMethod(name="%", type=MethodType.ONE_ARG)
    public RubyValue mod(RubyValue v) {
    	if (v instanceof RubyFixnum) {
    		return RubyBignum.bignorm(this.value_ % v.toInt());
    	}
    	
    	return coerceBin(RubyID.modID, v);
    }
    
    @RubyLevelMethod(name="**", type=MethodType.ONE_ARG)
    public RubyValue pow(RubyValue v) {
    	if (v instanceof RubyFixnum) {
    		int p = v.toInt();
    		if (p == 0) {
    			return ObjectFactory.FIXNUM1;
    		} else if (p == 1) {
    			return this;
    		}
    		
    		if (p > 0) {
    			BigInteger b = BigInteger.valueOf(this.value_);
    			return RubyBignum.bignorm(b.pow(p));
    		}
    		
    		return ObjectFactory.createFloat(Math.pow(this.value_, p));
    	} else if (v instanceof RubyFloat) {
    		return ObjectFactory.createFloat(Math.pow(this.value_, v.toFloat()));
    	}
    	
    	return coerceBin(RubyID.powID, v);
    }
    
    
    @RubyLevelMethod(name="~")
    public RubyValue opRev() {
    	return RubyBignum.bignorm(~this.value_);
    }
    
    @RubyLevelMethod(name="to_f")
    public RubyFloat convertToFloat() {
    	return ObjectFactory.createFloat(this.value_);
    }
    
    @RubyLevelMethod(name="<<", type=MethodType.ONE_ARG)
    public RubyValue lshift(RubyValue arg) {
    	return lshift(arg.toInt());
    }
    
    @RubyLevelMethod(name=">>", type=MethodType.ONE_ARG)
    public RubyValue rshift(RubyValue arg) {
    	return rshift(arg.toInt());
    }
    
    private static int BIT_SIZE = 32;
    
    private RubyValue lshift(int i) {
    	if (i == 0) {
    		return this;
    	} else if (i < 0) {
    		return rshift(-i);
    	}
    	// FIXME: TO BE IMPROVED
    	BigInteger bigValue1 = BigInteger.valueOf(this.value_);
        return RubyBignum.bignorm(bigValue1.shiftLeft(i));
    }

	private RubyValue rshift(int i) {
		if (i == 0) {
    		return this;
    	} else if (i < 0) {
    		return lshift(-i);
    	}
		
		if (i >= BIT_SIZE - 1) {
    		if (this.value_ < 0) {
    			return ObjectFactory.createFixnum(-1);
    		}
    		
    		return ObjectFactory.FIXNUM0;
    	}
    	
    	return ObjectFactory.createFixnum(this.value_ >> i);
	}
	
	@RubyLevelMethod(name="==", type=MethodType.ONE_ARG)
	public RubyValue opEqual(RubyValue arg) {
		if (arg == this) {
			return RubyConstant.QTRUE;
		}
		
		if (arg instanceof RubyFixnum) {
			return ObjectFactory.createBoolean(this.value_ == ((RubyFixnum)arg).value_);
		}
		
		return RubyAPI.callOneArgMethod(arg, this, null, RubyID.equalID);
	}
	
	@RubyLevelMethod(name="<=", type=MethodType.ONE_ARG)
	public RubyValue opLe(RubyValue v) {
		if (v instanceof RubyFixnum) {
			return ObjectFactory.createBoolean(this.value_ <= v.toInt());
		} else if (v instanceof RubyFloat) {
            return ObjectFactory.createBoolean(this.value_ <= v.toFloat());
		}
		
		return coerceRelop(RubyID.leID, v);
	}
	
	@RubyLevelMethod(name="<", type=MethodType.ONE_ARG)
	public RubyValue opLt(RubyValue v) {
		if (v instanceof RubyFixnum) {
			return ObjectFactory.createBoolean(this.value_ < v.toInt());
		} else if (v instanceof RubyFloat) {
            return ObjectFactory.createBoolean(this.value_ < v.toFloat());
		}
		
		return coerceRelop(RubyID.ltID, v);
	}
	
	@RubyLevelMethod(name=">=", type=MethodType.ONE_ARG)
	public RubyValue opGe(RubyValue v) {
		if (v instanceof RubyFixnum) {
			return ObjectFactory.createBoolean(this.value_ >= v.toInt());
		} else if (v instanceof RubyFloat) {
            return ObjectFactory.createBoolean(this.value_ >= v.toFloat());
		}
		
		return coerceRelop(RubyID.geID, v);
	}
	
	@RubyLevelMethod(name=">", type=MethodType.ONE_ARG)
	public RubyValue opGt(RubyValue v) {
		if (v instanceof RubyFixnum) {
			return ObjectFactory.createBoolean(this.value_ > v.toInt());
		} else if (v instanceof RubyFloat) {
            return ObjectFactory.createBoolean(this.value_ > v.toFloat());
		}
		
		return coerceRelop(RubyID.gtID, v);
	}
	
	@RubyLevelMethod(name="<=>", type=MethodType.ONE_ARG)
	public RubyValue opCmp(RubyValue v) {
		if (this == v) {
			return ObjectFactory.FIXNUM0;
		}
		
		if (v instanceof RubyFixnum) {
			int a = v.toInt();
			if (this.value_ > a) {
				return ObjectFactory.FIXNUM1;
			} else if (this.value_ == a) {
				return ObjectFactory.FIXNUM0;
			} else {
				return ObjectFactory.FIXNUM_NEGATIVE_ONE;
			}
		}
		
		return coerceCmp(RubyID.unequalID, v);
	}
	
	@RubyLevelMethod(name="|", type=MethodType.ONE_ARG)
	public RubyValue opOr(RubyValue v) {
		if (v instanceof RubyBignum) {
			return ((RubyBignum)v).op_bor(this);
		}
		
		return RubyBignum.bignorm(this.value_ | v.toInt());
	}
	
	@RubyLevelMethod(name="&", type=MethodType.ONE_ARG)
	public RubyValue opAnd(RubyValue v) {
		if (v instanceof RubyBignum) {
			return ((RubyBignum)v).op_band(this);
		}
		
		return RubyBignum.bignorm(this.value_ & v.toInt());
	}
	
	@RubyLevelMethod(name="^", type=MethodType.ONE_ARG)
	public RubyValue opXor(RubyValue v) {
		if (v instanceof RubyBignum) {
			return ((RubyBignum)v).op_bxor(this);
		}
		
		return RubyBignum.bignorm(this.value_ ^ v.toInt());
	}
	
	@RubyLevelMethod(name="to_s", type=MethodType.NO_OR_ONE_ARG)
	public RubyString to_s() {
		return ObjectFactory.createString(this.toString());
	}
	
	public RubyString to_s(RubyValue v) {
		 int radix = v.toInt();
         if (radix < 2 || radix > 36) {
             throw new RubyException(RubyRuntime.ArgumentErrorClass, "illegal radix " + radix);
         }
         
         return ObjectFactory.createString(this.toString(radix));
	}
	
	@RubyLevelMethod(name="quo", type=MethodType.ONE_ARG)
	public RubyFloat quo(RubyValue v) {
        if (v instanceof RubyFixnum) {
        	return ObjectFactory.createFloat(this.value_ / v.toInt());
        }
        
        // FIXME: should be coerced.
        throw new RubyException(RubyRuntime.TypeErrorClass, v.getRubyClass().getName() + " can't be coersed into Fixnum");
	}
	
	@RubyLevelMethod(name="[]", type=MethodType.ONE_ARG)
	public RubyFixnum aref(RubyValue idx) {
		if (idx instanceof RubyBignum) {
			idx = RubyBignum.bignorm(idx);
			if (!(idx instanceof RubyFixnum)) {
				if (this.value_ > 0 || ((RubyBignum)idx).getInternal().compareTo(BigInteger.ZERO) > 0) {
					return ObjectFactory.FIXNUM0;
				} else {
					return ObjectFactory.FIXNUM1;
				}
			}
		}
		
		int i = idx.toInt();
		if (i < 0 || i > BIT_SIZE) {
			return ObjectFactory.FIXNUM0;
		}
		
		if ((this.value_ & (1l << i)) > 0) {
			return ObjectFactory.FIXNUM1;
		}
		
		return ObjectFactory.FIXNUM0;
	}

	protected RubyValue doStep(RubyValue toArg, RubyValue stepArg, RubyBlock block) {
		if ((toArg instanceof RubyFixnum) && (stepArg instanceof RubyFixnum)) {
			int i = this.value_;
			int end = toArg.toInt();
			int diff = stepArg.toInt();
			if (diff > 0) {
				while (i <= end) {
					RubyValue v = block.invoke(this, ObjectFactory.createFixnum(i));
                    if (block.breakedOrReturned()) {
                        return v;
                    }
                    i += diff;
				}
			} else {
				while (i >= end) {
					RubyValue v = block.invoke(this, ObjectFactory.createFixnum(i));
                    if (block.breakedOrReturned()) {
                        return v;
                    }
                    i += diff;
				}
			}
			
			return this;
		}
		
		return super.doStep(toArg, stepArg, block);
	}

	public RubyValue times(RubyBlock block) {
        for (int i = 0; i < this.value_; ++i) {
            RubyValue v = block.invoke(this, ObjectFactory.createFixnum(i));
            if (block.breakedOrReturned()) {
                return v;
            }
        }
        
        return this;
	}
}
