require 'common'

test_check("numeric")

test_ok(1.0.abs == 1.0)
test_ok((-1.0).abs == 1.0)
test_ok(0.abs == 0)
test_ok(0.0.abs == 0.0)
test_ok(1.coerce(0) == [1, 0])
test_ok(7.divmod(3) == [2, 1])
test_ok(7.integer? == true)
test_ok(7.0.integer? == false)
test_ok((1 << 33).integer? == true)
test_ok(1.eql?(1) == true)
test_ok(2.eql?(1) == false)
test_ok(1.0.eql?(1.0) == true)
test_ok(1.0.eql?(2.0) == false)
test_ok(1.nonzero? == 1)
test_ok((-1).nonzero? == -1)
test_ok(0.nonzero? == nil)
test_ok(1.zero? == false)
test_ok((-1).zero? == false)
test_ok(0.zero? == true)
test_ok(7.remainder(3) == 1)
test_ok(7.remainder(-3) == 1)
test_ok(-7.remainder(3) == -1)
test_ok(-7.remainder(-3) == -1)
