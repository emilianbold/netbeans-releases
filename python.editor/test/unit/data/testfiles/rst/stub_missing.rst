NetBeans extra documentation:
Documentation for APIs missing from the RST documentation shipping with Python.
This is generated from introspecting python code using extract_rst.py.
Python version stats:
2.6 (trunk:66714:66715M, Oct  1 2008, 18:36:04) 
[GCC 4.0.1 (Apple Computer, Inc. build 5370)]


.. class:: int

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.


.. attribute:: __class__

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.


.. method:: __cmp__(y)

   x.__cmp__(y) <==> cmp(x,y)


.. method:: __coerce__(y)

   x.__coerce__(y) <==> coerce(x, y)


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. method:: __divmod__(y)

   x.__divmod__(y) <==> divmod(x, y)


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __float__()

   x.__float__() <==> float(x)


.. method:: __format__()

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __hex__()

   x.__hex__() <==> hex(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __int__()

   x.__int__() <==> int(x)


.. method:: __long__()

   x.__long__() <==> long(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __nonzero__()

   x.__nonzero__() <==> x != 0


.. method:: __oct__()

   x.__oct__() <==> oct(x)


.. method:: __radd__(y)

   x.__radd__(y) <==> y+x


.. method:: __rand__(y)

   x.__rand__(y) <==> y&x


.. method:: __rdiv__(y)

   x.__rdiv__(y) <==> y/x


.. method:: __rdivmod__(y)

   x.__rdivmod__(y) <==> divmod(y, x)


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rfloordiv__(y)

   x.__rfloordiv__(y) <==> y//x


.. method:: __rlshift__(y)

   x.__rlshift__(y) <==> y<<x


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(y)

   x.__rmul__(y) <==> y*x


.. method:: __ror__(y)

   x.__ror__(y) <==> y|x


.. method:: __rpow__(x)

   y.__rpow__(x[, z]) <==> pow(x, y[, z])


.. method:: __rrshift__(y)

   x.__rrshift__(y) <==> y>>x


.. method:: __rsub__(y)

   x.__rsub__(y) <==> y-x


.. method:: __rtruediv__(y)

   x.__rtruediv__(y) <==> y/x


.. method:: __rxor__(y)

   x.__rxor__(y) <==> y^x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   __sizeof__() -> size of object in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: __trunc__()

   Truncating an Integral returns itself.

   .. versionadded:: 2.6

.. method:: conjugate()

   Returns self, the complex conjugate of any int.

   .. versionadded:: 2.6

.. attribute:: denominator

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. attribute:: imag

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. attribute:: numerator

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. attribute:: real

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. class:: float

   float(x) -> floating point number
   
   Convert a string or number to a floating point number, if possible.


.. attribute:: __class__

   float(x) -> floating point number
   
   Convert a string or number to a floating point number, if possible.


.. method:: __coerce__(y)

   x.__coerce__(y) <==> coerce(x, y)


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. method:: __divmod__(y)

   x.__divmod__(y) <==> divmod(x, y)


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __float__()

   x.__float__() <==> float(x)


.. method:: __format__(format_spec)

   float.__format__(format_spec) -> string
   
   Formats the float according to format_spec.

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getformat__(typestr)

   float.__getformat__(typestr) -> string
   
   You probably don't want to use this function.  It exists mainly to be
   used in Python's test suite.
   
   typestr must be 'double' or 'float'.  This function returns whichever of
   'unknown', 'IEEE, big-endian' or 'IEEE, little-endian' best describes the
   format of floating point numbers used by the C type named by typestr.


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __int__()

   x.__int__() <==> int(x)


.. method:: __long__()

   x.__long__() <==> long(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __nonzero__()

   x.__nonzero__() <==> x != 0


.. method:: __radd__(y)

   x.__radd__(y) <==> y+x


.. method:: __rdiv__(y)

   x.__rdiv__(y) <==> y/x


.. method:: __rdivmod__(y)

   x.__rdivmod__(y) <==> divmod(y, x)


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rfloordiv__(y)

   x.__rfloordiv__(y) <==> y//x


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(y)

   x.__rmul__(y) <==> y*x


.. method:: __rpow__(x)

   y.__rpow__(x[, z]) <==> pow(x, y[, z])


.. method:: __rsub__(y)

   x.__rsub__(y) <==> y-x


.. method:: __rtruediv__(y)

   x.__rtruediv__(y) <==> y/x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __setformat__(typestr, fmt)

   float.__setformat__(typestr, fmt) -> None
   
   You probably don't want to use this function.  It exists mainly to be
   used in Python's test suite.
   
   typestr must be 'double' or 'float'.  fmt must be one of 'unknown',
   'IEEE, big-endian' or 'IEEE, little-endian', and in addition can only be
   one of the latter two if it appears to match the underlying C reality.
   
   Overrides the automatic determination of C-level floating point type.
   This affects how floats are converted to and from binary strings.


.. method:: __sizeof__()

   __sizeof__() -> size of object in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: __trunc__()

   Returns the Integral closest to x between 0 and x.

   .. versionadded:: 2.6

.. method:: conjugate()

   Returns self, the complex conjugate of any float.

   .. versionadded:: 2.6

.. attribute:: imag

   float(x) -> floating point number
   
   Convert a string or number to a floating point number, if possible.

   .. versionadded:: 2.6

.. method:: is_integer()

   Returns True if the float is an integer.

   .. versionadded:: 2.6

.. attribute:: real

   float(x) -> floating point number
   
   Convert a string or number to a floating point number, if possible.

   .. versionadded:: 2.6

.. class:: long

   long(x[, base]) -> integer
   
   Convert a string or number to a long integer, if possible.  A floating
   point argument will be truncated towards zero (this does not include a
   string representation of a floating point number!)  When converting a
   string, use the optional base.  It is an error to supply a base when
   converting a non-string.


.. attribute:: __class__

   long(x[, base]) -> integer
   
   Convert a string or number to a long integer, if possible.  A floating
   point argument will be truncated towards zero (this does not include a
   string representation of a floating point number!)  When converting a
   string, use the optional base.  It is an error to supply a base when
   converting a non-string.


.. method:: __cmp__(y)

   x.__cmp__(y) <==> cmp(x,y)


.. method:: __coerce__(y)

   x.__coerce__(y) <==> coerce(x, y)


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. method:: __divmod__(y)

   x.__divmod__(y) <==> divmod(x, y)


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __float__()

   x.__float__() <==> float(x)


.. method:: __format__()

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __hex__()

   x.__hex__() <==> hex(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __int__()

   x.__int__() <==> int(x)


.. method:: __long__()

   x.__long__() <==> long(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __nonzero__()

   x.__nonzero__() <==> x != 0


.. method:: __oct__()

   x.__oct__() <==> oct(x)


.. method:: __radd__(y)

   x.__radd__(y) <==> y+x


.. method:: __rand__(y)

   x.__rand__(y) <==> y&x


.. method:: __rdiv__(y)

   x.__rdiv__(y) <==> y/x


.. method:: __rdivmod__(y)

   x.__rdivmod__(y) <==> divmod(y, x)


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rfloordiv__(y)

   x.__rfloordiv__(y) <==> y//x


.. method:: __rlshift__(y)

   x.__rlshift__(y) <==> y<<x


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(y)

   x.__rmul__(y) <==> y*x


.. method:: __ror__(y)

   x.__ror__(y) <==> y|x


.. method:: __rpow__(x)

   y.__rpow__(x[, z]) <==> pow(x, y[, z])


.. method:: __rrshift__(y)

   x.__rrshift__(y) <==> y>>x


.. method:: __rsub__(y)

   x.__rsub__(y) <==> y-x


.. method:: __rtruediv__(y)

   x.__rtruediv__(y) <==> y/x


.. method:: __rxor__(y)

   x.__rxor__(y) <==> y^x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   Returns size in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: __trunc__()

   Truncating an Integral returns itself.

   .. versionadded:: 2.6

.. method:: conjugate()

   Returns self, the complex conjugate of any long.

   .. versionadded:: 2.6

.. attribute:: denominator

   long(x[, base]) -> integer
   
   Convert a string or number to a long integer, if possible.  A floating
   point argument will be truncated towards zero (this does not include a
   string representation of a floating point number!)  When converting a
   string, use the optional base.  It is an error to supply a base when
   converting a non-string.

   .. versionadded:: 2.6

.. attribute:: imag

   long(x[, base]) -> integer
   
   Convert a string or number to a long integer, if possible.  A floating
   point argument will be truncated towards zero (this does not include a
   string representation of a floating point number!)  When converting a
   string, use the optional base.  It is an error to supply a base when
   converting a non-string.

   .. versionadded:: 2.6

.. attribute:: numerator

   long(x[, base]) -> integer
   
   Convert a string or number to a long integer, if possible.  A floating
   point argument will be truncated towards zero (this does not include a
   string representation of a floating point number!)  When converting a
   string, use the optional base.  It is an error to supply a base when
   converting a non-string.

   .. versionadded:: 2.6

.. attribute:: real

   long(x[, base]) -> integer
   
   Convert a string or number to a long integer, if possible.  A floating
   point argument will be truncated towards zero (this does not include a
   string representation of a floating point number!)  When converting a
   string, use the optional base.  It is an error to supply a base when
   converting a non-string.

   .. versionadded:: 2.6

.. class:: bool

   bool(x) -> bool
   
   Returns True when the argument x is true, False otherwise.
   The builtins True and False are the only two instances of the class bool.
   The class bool is a subclass of the class int, and cannot be subclassed.


.. method:: __abs__()

   x.__abs__() <==> abs(x)


.. method:: __add__(y)

   x.__add__(y) <==> x+y


.. method:: __and__(y)

   x.__and__(y) <==> x&y


.. attribute:: __class__

   bool(x) -> bool
   
   Returns True when the argument x is true, False otherwise.
   The builtins True and False are the only two instances of the class bool.
   The class bool is a subclass of the class int, and cannot be subclassed.


.. method:: __cmp__(y)

   x.__cmp__(y) <==> cmp(x,y)


.. method:: __coerce__(y)

   x.__coerce__(y) <==> coerce(x, y)


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. method:: __div__(y)

   x.__div__(y) <==> x/y


.. method:: __divmod__(y)

   x.__divmod__(y) <==> divmod(x, y)


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __float__()

   x.__float__() <==> float(x)


.. method:: __floordiv__(y)

   x.__floordiv__(y) <==> x//y


.. method:: __format__()

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __hex__()

   x.__hex__() <==> hex(x)


.. method:: __index__()

   x[y:z] <==> x[y.__index__():z.__index__()]


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __int__()

   x.__int__() <==> int(x)


.. method:: __invert__()

   x.__invert__() <==> ~x


.. method:: __long__()

   x.__long__() <==> long(x)


.. method:: __lshift__(y)

   x.__lshift__(y) <==> x<<y


.. method:: __mod__(y)

   x.__mod__(y) <==> x%y


.. method:: __mul__(y)

   x.__mul__(y) <==> x*y


.. method:: __neg__()

   x.__neg__() <==> -x


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __nonzero__()

   x.__nonzero__() <==> x != 0


.. method:: __oct__()

   x.__oct__() <==> oct(x)


.. method:: __or__(y)

   x.__or__(y) <==> x|y


.. method:: __pos__()

   x.__pos__() <==> +x


.. method:: __pow__(y)

   x.__pow__(y[, z]) <==> pow(x, y[, z])


.. method:: __radd__(y)

   x.__radd__(y) <==> y+x


.. method:: __rand__(y)

   x.__rand__(y) <==> y&x


.. method:: __rdiv__(y)

   x.__rdiv__(y) <==> y/x


.. method:: __rdivmod__(y)

   x.__rdivmod__(y) <==> divmod(y, x)


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rfloordiv__(y)

   x.__rfloordiv__(y) <==> y//x


.. method:: __rlshift__(y)

   x.__rlshift__(y) <==> y<<x


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(y)

   x.__rmul__(y) <==> y*x


.. method:: __ror__(y)

   x.__ror__(y) <==> y|x


.. method:: __rpow__(x)

   y.__rpow__(x[, z]) <==> pow(x, y[, z])


.. method:: __rrshift__(y)

   x.__rrshift__(y) <==> y>>x


.. method:: __rshift__(y)

   x.__rshift__(y) <==> x>>y


.. method:: __rsub__(y)

   x.__rsub__(y) <==> y-x


.. method:: __rtruediv__(y)

   x.__rtruediv__(y) <==> y/x


.. method:: __rxor__(y)

   x.__rxor__(y) <==> y^x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   __sizeof__() -> size of object in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __sub__(y)

   x.__sub__(y) <==> x-y


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: __truediv__(y)

   x.__truediv__(y) <==> x/y


.. method:: __trunc__()

   Truncating an Integral returns itself.

   .. versionadded:: 2.6

.. method:: __xor__(y)

   x.__xor__(y) <==> x^y


.. method:: conjugate()

   Returns self, the complex conjugate of any int.

   .. versionadded:: 2.6

.. attribute:: denominator

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. attribute:: imag

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. attribute:: numerator

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. attribute:: real

   int(x[, base]) -> integer
   
   Convert a string or number to an integer, if possible.  A floating point
   argument will be truncated towards zero (this does not include a string
   representation of a floating point number!)  When converting a string, use
   the optional base.  It is an error to supply a base when converting a
   non-string.  If base is zero, the proper base is guessed based on the
   string content.  If the argument is outside the integer range a
   long object will be returned instead.

   .. versionadded:: 2.6

.. class:: complex

   complex(real[, imag]) -> complex number
   
   Create a complex number from a real part and an optional imaginary part.
   This is equivalent to (real + imag*1j) where imag defaults to 0.


.. attribute:: __class__

   complex(real[, imag]) -> complex number
   
   Create a complex number from a real part and an optional imaginary part.
   This is equivalent to (real + imag*1j) where imag defaults to 0.


.. method:: __coerce__(y)

   x.__coerce__(y) <==> coerce(x, y)


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. method:: __divmod__(y)

   x.__divmod__(y) <==> divmod(x, y)


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __float__()

   x.__float__() <==> float(x)


.. method:: __format__()

   default object formatter

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __int__()

   x.__int__() <==> int(x)


.. method:: __long__()

   x.__long__() <==> long(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __nonzero__()

   x.__nonzero__() <==> x != 0


.. method:: __radd__(y)

   x.__radd__(y) <==> y+x


.. method:: __rdiv__(y)

   x.__rdiv__(y) <==> y/x


.. method:: __rdivmod__(y)

   x.__rdivmod__(y) <==> divmod(y, x)


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rfloordiv__(y)

   x.__rfloordiv__(y) <==> y//x


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(y)

   x.__rmul__(y) <==> y*x


.. method:: __rpow__(x)

   y.__rpow__(x[, z]) <==> pow(x, y[, z])


.. method:: __rsub__(y)

   x.__rsub__(y) <==> y-x


.. method:: __rtruediv__(y)

   x.__rtruediv__(y) <==> y/x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   __sizeof__() -> size of object in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: conjugate()

   complex.conjugate() -> complex
   
   Returns the complex conjugate of its argument. (3-4j).conjugate() == 3+4j.


.. attribute:: imag

   float(x) -> floating point number
   
   Convert a string or number to a floating point number, if possible.


.. attribute:: real

   float(x) -> floating point number
   
   Convert a string or number to a floating point number, if possible.


.. class:: list

   list() -> new list
   list(sequence) -> new list initialized from sequence's items


.. attribute:: __class__

   list() -> new list
   list(sequence) -> new list initialized from sequence's items


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __format__()

   default object formatter

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. attribute:: __hash__


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __iter__()

   x.__iter__() <==> iter(x)


.. method:: __len__()

   x.__len__() <==> len(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __reversed__()

   L.__reversed__() -- return a reverse iterator over the list


.. method:: __rmul__(n)

   x.__rmul__(n) <==> n*x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   L.__sizeof__() -- size of L in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: append()

   L.append(object) -- append object to end


.. method:: count(value)

   L.count(value) -> integer -- return number of occurrences of value


.. method:: extend()

   L.extend(iterable) -- extend list by appending elements from the iterable


.. method:: insert()

   L.insert(index, object) -- insert object before index


.. method:: pop()

   L.pop([index]) -> item -- remove and return item at index (default last)


.. method:: remove()

   L.remove(value) -- remove first occurrence of value


.. method:: reverse()

   L.reverse() -- reverse *IN PLACE*


.. method:: sort(cmp=None, key=None, reverse=False) __ stable sort *IN PLACE*;
cmp(x, y)

   L.sort(cmp=None, key=None, reverse=False) -- stable sort *IN PLACE*;
   cmp(x, y) -> -1, 0, 1


.. class:: dict

   dict() -> new empty dictionary.
   dict(mapping) -> new dictionary initialized from a mapping object's
       (key, value) pairs.
   dict(seq) -> new dictionary initialized as if via:
       d = {}
       for k, v in seq:
           d[k] = v
   dict(**kwargs) -> new dictionary initialized with the name=value pairs
       in the keyword argument list.  For example:  dict(one=1, two=2)


.. attribute:: __class__

   dict() -> new empty dictionary.
   dict(mapping) -> new dictionary initialized from a mapping object's
       (key, value) pairs.
   dict(seq) -> new dictionary initialized as if via:
       d = {}
       for k, v in seq:
           d[k] = v
   dict(**kwargs) -> new dictionary initialized with the name=value pairs
       in the keyword argument list.  For example:  dict(one=1, two=2)


.. method:: __cmp__(y)

   x.__cmp__(y) <==> cmp(x,y)


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __format__()

   default object formatter

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. attribute:: __hash__


.. method:: __iter__()

   x.__iter__() <==> iter(x)


.. method:: __len__()

   x.__len__() <==> len(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   D.__sizeof__() -> size of D in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. class:: tuple

   tuple() -> an empty tuple
   tuple(sequence) -> tuple initialized from sequence's items
   
   If the argument is a tuple, the return value is the same object.


.. attribute:: __class__

   tuple() -> an empty tuple
   tuple(sequence) -> tuple initialized from sequence's items
   
   If the argument is a tuple, the return value is the same object.


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __format__()

   default object formatter

   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __iter__()

   x.__iter__() <==> iter(x)


.. method:: __len__()

   x.__len__() <==> len(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rmul__(n)

   x.__rmul__(n) <==> n*x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   T.__sizeof__() -- size of T in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: count(value)

   T.count(value) -> integer -- return number of occurrences of value

   .. versionadded:: 2.6

.. class:: str

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. attribute:: __class__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __format__(format_spec)

   S.__format__(format_spec) -> unicode
   


   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __len__()

   x.__len__() <==> len(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(n)

   x.__rmul__(n) <==> n*x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   S.__sizeof__() -> size of S in memory, in bytes

   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: _formatter_field_name_split()

   .. versionadded:: 2.6

.. method:: _formatter_parser()

   .. versionadded:: 2.6

.. class:: unicode

   unicode(string [, encoding[, errors]]) -> object
   
   Create a new Unicode object from the given encoded string.
   encoding defaults to the current default string encoding.
   errors can be 'strict', 'replace' or 'ignore' and defaults to 'strict'.


.. attribute:: __class__

   unicode(string [, encoding[, errors]]) -> object
   
   Create a new Unicode object from the given encoded string.
   encoding defaults to the current default string encoding.
   errors can be 'strict', 'replace' or 'ignore' and defaults to 'strict'.


.. method:: __delattr__(name)

   x.__delattr__('name') <==> del x.name


.. attribute:: __doc__

   str(object) -> string
   
   Return a nice string representation of the object.
   If the argument is a string, the return value is the same object.


.. method:: __format__(format_spec)

   S.__format__(format_spec) -> unicode
   


   .. versionadded:: 2.6

.. method:: __getattribute__(name)

   x.__getattribute__('name') <==> x.name


.. method:: __getnewargs__()


.. method:: __hash__()

   x.__hash__() <==> hash(x)


.. method:: __init__()

   x.__init__(...) initializes x; see x.__class__.__doc__ for signature


.. method:: __len__()

   x.__len__() <==> len(x)


.. method:: __new__(S, ___)

   T.__new__(S, ...) -> a new object with type S, a subtype of T


.. method:: __reduce__()

   helper for pickle


.. method:: __reduce_ex__()

   helper for pickle


.. method:: __repr__()

   x.__repr__() <==> repr(x)


.. method:: __rmod__(y)

   x.__rmod__(y) <==> y%x


.. method:: __rmul__(n)

   x.__rmul__(n) <==> n*x


.. method:: __setattr__(name, value)

   x.__setattr__('name', value) <==> x.name = value


.. method:: __sizeof__()

   S.__sizeof__() -> size of S in memory, in bytes
   


   .. versionadded:: 2.6

.. method:: __str__()

   x.__str__() <==> str(x)


.. method:: __subclasshook__()

   Abstract classes can override this to customize issubclass().
   
   This is invoked early on by abc.ABCMeta.__subclasscheck__().
   It should return True, False or NotImplemented.  If it returns
   NotImplemented, the normal algorithm is used.  Otherwise, it
   overrides the normal algorithm (and the outcome is cached).


   .. versionadded:: 2.6

.. method:: _formatter_field_name_split()

   .. versionadded:: 2.6

.. method:: _formatter_parser()

   .. versionadded:: 2.6

.. method:: capitalize()

   S.capitalize() -> unicode
   
   Return a capitalized version of S, i.e. make the first character
   have upper case.


.. method:: center(width)

   S.center(width[, fillchar]) -> unicode
   
   Return S centered in a Unicode string of length width. Padding is
   done using the specified fill character (default is a space)


.. method:: count(sub)

   S.count(sub[, start[, end]]) -> int
   
   Return the number of non-overlapping occurrences of substring sub in
   Unicode string S[start:end].  Optional arguments start and end are
   interpreted as in slice notation.


.. method:: decode()

   S.decode([encoding[,errors]]) -> string or unicode
   
   Decodes S using the codec registered for encoding. encoding defaults
   to the default encoding. errors may be given to set a different error
   handling scheme. Default is 'strict' meaning that encoding errors raise
   a UnicodeDecodeError. Other possible values are 'ignore' and 'replace'
   as well as any other name registerd with codecs.register_error that is
   able to handle UnicodeDecodeErrors.


.. method:: encode()

   S.encode([encoding[,errors]]) -> string or unicode
   
   Encodes S using the codec registered for encoding. encoding defaults
   to the default encoding. errors may be given to set a different error
   handling scheme. Default is 'strict' meaning that encoding errors raise
   a UnicodeEncodeError. Other possible values are 'ignore', 'replace' and
   'xmlcharrefreplace' as well as any other name registered with
   codecs.register_error that can handle UnicodeEncodeErrors.


.. method:: endswith(suffix)

   S.endswith(suffix[, start[, end]]) -> bool
   
   Return True if S ends with the specified suffix, False otherwise.
   With optional start, test S beginning at that position.
   With optional end, stop comparing S at that position.
   suffix can also be a tuple of strings to try.


.. method:: expandtabs()

   S.expandtabs([tabsize]) -> unicode
   
   Return a copy of S where all tab characters are expanded using spaces.
   If tabsize is not given, a tab size of 8 characters is assumed.


.. method:: find(sub )

   S.find(sub [,start [,end]]) -> int
   
   Return the lowest index in S where substring sub is found,
   such that sub is contained within s[start:end].  Optional
   arguments start and end are interpreted as in slice notation.
   
   Return -1 on failure.


.. method:: format(*args, **kwargs)

   S.format(*args, **kwargs) -> unicode
   


   .. versionadded:: 2.6

.. method:: isalnum()

   S.isalnum() -> bool
   
   Return True if all characters in S are alphanumeric
   and there is at least one character in S, False otherwise.


.. method:: isalpha()

   S.isalpha() -> bool
   
   Return True if all characters in S are alphabetic
   and there is at least one character in S, False otherwise.


.. method:: isdigit()

   S.isdigit() -> bool
   
   Return True if all characters in S are digits
   and there is at least one character in S, False otherwise.


.. method:: islower()

   S.islower() -> bool
   
   Return True if all cased characters in S are lowercase and there is
   at least one cased character in S, False otherwise.


.. method:: isspace()

   S.isspace() -> bool
   
   Return True if all characters in S are whitespace
   and there is at least one character in S, False otherwise.


.. method:: istitle()

   S.istitle() -> bool
   
   Return True if S is a titlecased string and there is at least one
   character in S, i.e. upper- and titlecase characters may only
   follow uncased characters and lowercase characters only cased ones.
   Return False otherwise.


.. method:: isupper()

   S.isupper() -> bool
   
   Return True if all cased characters in S are uppercase and there is
   at least one cased character in S, False otherwise.


.. method:: join(sequence)

   S.join(sequence) -> unicode
   
   Return a string which is the concatenation of the strings in the
   sequence.  The separator between elements is S.


.. method:: ljust(width)

   S.ljust(width[, fillchar]) -> int
   
   Return S left justified in a Unicode string of length width. Padding is
   done using the specified fill character (default is a space).


.. method:: lower()

   S.lower() -> unicode
   
   Return a copy of the string S converted to lowercase.


.. method:: lstrip()

   S.lstrip([chars]) -> unicode
   
   Return a copy of the string S with leading whitespace removed.
   If chars is given and not None, remove characters in chars instead.
   If chars is a str, it will be converted to unicode before stripping


.. method:: partition(sep)

   S.partition(sep) -> (head, sep, tail)
   
   Searches for the separator sep in S, and returns the part before it,
   the separator itself, and the part after it.  If the separator is not
   found, returns S and two empty strings.


.. method:: replace(old, new)

   S.replace (old, new[, count]) -> unicode
   
   Return a copy of S with all occurrences of substring
   old replaced by new.  If the optional argument count is
   given, only the first count occurrences are replaced.


.. method:: rfind(sub )

   S.rfind(sub [,start [,end]]) -> int
   
   Return the highest index in S where substring sub is found,
   such that sub is contained within s[start:end].  Optional
   arguments start and end are interpreted as in slice notation.
   
   Return -1 on failure.


.. method:: rindex(sub )

   S.rindex(sub [,start [,end]]) -> int
   
   Like S.rfind() but raise ValueError when the substring is not found.


.. method:: rjust(width)

   S.rjust(width[, fillchar]) -> unicode
   
   Return S right justified in a Unicode string of length width. Padding is
   done using the specified fill character (default is a space).


.. method:: rpartition(sep)

   S.rpartition(sep) -> (tail, sep, head)
   
   Searches for the separator sep in S, starting at the end of S, and returns
   the part before it, the separator itself, and the part after it.  If the
   separator is not found, returns two empty strings and S.


.. method:: rsplit()

   S.rsplit([sep [,maxsplit]]) -> list of strings
   
   Return a list of the words in S, using sep as the
   delimiter string, starting at the end of the string and
   working to the front.  If maxsplit is given, at most maxsplit
   splits are done. If sep is not specified, any whitespace string
   is a separator.


.. method:: rstrip()

   S.rstrip([chars]) -> unicode
   
   Return a copy of the string S with trailing whitespace removed.
   If chars is given and not None, remove characters in chars instead.
   If chars is a str, it will be converted to unicode before stripping


.. method:: split()

   S.split([sep [,maxsplit]]) -> list of strings
   
   Return a list of the words in S, using sep as the
   delimiter string.  If maxsplit is given, at most maxsplit
   splits are done. If sep is not specified or is None, any
   whitespace string is a separator and empty strings are
   removed from the result.


.. method:: splitlines()

   S.splitlines([keepends]]) -> list of strings
   
   Return a list of the lines in S, breaking at line boundaries.
   Line breaks are not included in the resulting list unless keepends
   is given and true.


.. method:: startswith(prefix)

   S.startswith(prefix[, start[, end]]) -> bool
   
   Return True if S starts with the specified prefix, False otherwise.
   With optional start, test S beginning at that position.
   With optional end, stop comparing S at that position.
   prefix can also be a tuple of strings to try.


.. method:: strip()

   S.strip([chars]) -> unicode
   
   Return a copy of the string S with leading and trailing
   whitespace removed.
   If chars is given and not None, remove characters in chars instead.
   If chars is a str, it will be converted to unicode before stripping


.. method:: swapcase()

   S.swapcase() -> unicode
   
   Return a copy of S with uppercase characters converted to lowercase
   and vice versa.


.. method:: title()

   S.title() -> unicode
   
   Return a titlecased version of S, i.e. words start with title case
   characters, all remaining cased characters have lower case.


.. method:: translate(table)

   S.translate(table) -> unicode
   
   Return a copy of the string S, where all characters have been mapped
   through the given translation table, which must be a mapping of
   Unicode ordinals to Unicode ordinals, Unicode strings or None.
   Unmapped characters are left untouched. Characters mapped to None
   are deleted.


.. method:: upper()

   S.upper() -> unicode
   
   Return a copy of S converted to uppercase.


.. method:: zfill(width)

   S.zfill(width) -> unicode
   
   Pad a numeric string S with zeros on the left, to fill a field
   of the specified width. The string S is never truncated.


