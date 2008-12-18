# From http://www.netbeans.org/issues/show_bug.cgi?id=155586
lck = self.lock_somehow()
try:
   do_some_atomic_stuff()
finally:
   del lck
