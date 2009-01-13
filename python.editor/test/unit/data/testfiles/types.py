x = y = SomeOtherClass()
z = y
w = 5
yz = "foo"
someObject = SomeClass().foo();

# Type variables
# @type defined1: int
#defined1.x()

#FIRST_CARET_POS

# Redefine
# @type defined1: str
#defined1.y()

x = Other()
y = 5
z = "str"

#SECOND_CARET_POS
