
foo1!
foo2(3)
foo3(4,5)
foo4(x, *y)
foo5a(x, y, z(w,x,v))
foo5b(x, y, z.t(w,x,v), u)
x.foo5c(x, y, z.t(w,x,v), u)

def foo6
end

def foo7(x)
end

def foo8(x, y)
end

def foo9(x, y, z=5, w=6)
end

def foo10(x,y,z=5,w=6,*foo)
end

def foo11(x,y,&foo)
end


