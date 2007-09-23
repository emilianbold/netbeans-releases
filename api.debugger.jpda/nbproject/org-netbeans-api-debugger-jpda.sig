#API master signature file
#Version 1.5.0_11
CLSS public abstract org.netbeans.api.debugger.jpda.AbstractDICookie
cons public AbstractDICookie()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract com.sun.jdi.VirtualMachine org.netbeans.api.debugger.jpda.AbstractDICookie.getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,com.sun.jdi.connect.VMStartException,java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public final org.netbeans.api.debugger.jpda.AttachingDICookie
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.AttachingDICookie.ID
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public com.sun.jdi.VirtualMachine org.netbeans.api.debugger.jpda.AttachingDICookie.getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,java.io.IOException
meth public com.sun.jdi.connect.AttachingConnector org.netbeans.api.debugger.jpda.AttachingDICookie.getAttachingConnector()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.AttachingDICookie.getPortNumber()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.debugger.jpda.AttachingDICookie.getHostName()
meth public java.lang.String org.netbeans.api.debugger.jpda.AttachingDICookie.getSharedMemoryName()
meth public java.util.Map org.netbeans.api.debugger.jpda.AttachingDICookie.getArgs()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.AttachingDICookie org.netbeans.api.debugger.jpda.AttachingDICookie.create(com.sun.jdi.connect.AttachingConnector,java.util.Map)
meth public static org.netbeans.api.debugger.jpda.AttachingDICookie org.netbeans.api.debugger.jpda.AttachingDICookie.create(java.lang.String)
meth public static org.netbeans.api.debugger.jpda.AttachingDICookie org.netbeans.api.debugger.jpda.AttachingDICookie.create(java.lang.String,int)
supr org.netbeans.api.debugger.jpda.AbstractDICookie
CLSS public abstract interface org.netbeans.api.debugger.jpda.CallStackFrame
meth public abstract [Lorg.netbeans.api.debugger.jpda.LocalVariable; org.netbeans.api.debugger.jpda.CallStackFrame.getLocalVariables() throws com.sun.jdi.AbsentInformationException
meth public abstract boolean org.netbeans.api.debugger.jpda.CallStackFrame.isObsolete()
meth public abstract int org.netbeans.api.debugger.jpda.CallStackFrame.getLineNumber(java.lang.String)
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.CallStackFrame.getClassName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.CallStackFrame.getDefaultStratum()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.CallStackFrame.getMethodName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.CallStackFrame.getSourceName(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.CallStackFrame.getSourcePath(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.util.List org.netbeans.api.debugger.jpda.CallStackFrame.getAvailableStrata()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThread org.netbeans.api.debugger.jpda.CallStackFrame.getThread()
meth public abstract org.netbeans.api.debugger.jpda.This org.netbeans.api.debugger.jpda.CallStackFrame.getThisVariable()
meth public abstract void org.netbeans.api.debugger.jpda.CallStackFrame.makeCurrent()
meth public abstract void org.netbeans.api.debugger.jpda.CallStackFrame.popFrame()
supr null
CLSS public final org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
fld  constant public static final int org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED
fld  constant public static final int org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.PROP_BREAKPOINT_TYPE
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.PROP_CLASS_EXCLUSION_FILTERS
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.PROP_CLASS_FILTERS
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public [Ljava.lang.String; org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.getClassExclusionFilters()
meth public [Ljava.lang.String; org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.getClassFilters()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.getBreakpointType()
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.toString()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.create(int)
meth public static org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.create(java.lang.String,boolean,int)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.setBreakpointType(int)
meth public void org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.setClassExclusionFilters([Ljava.lang.String;)
meth public void org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint.setClassFilters([Ljava.lang.String;)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
CLSS public org.netbeans.api.debugger.jpda.DebuggerStartException
cons public DebuggerStartException(java.lang.String)
cons public DebuggerStartException(java.lang.Throwable)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public java.lang.Throwable org.netbeans.api.debugger.jpda.DebuggerStartException.getTargetException()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.Exception
CLSS public final org.netbeans.api.debugger.jpda.ExceptionBreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED
fld  constant public static final int org.netbeans.api.debugger.jpda.ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED_UNCATCHED
fld  constant public static final int org.netbeans.api.debugger.jpda.ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ExceptionBreakpoint.PROP_CATCH_TYPE
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ExceptionBreakpoint.PROP_CONDITION
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ExceptionBreakpoint.PROP_EXCEPTION_CLASS_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.ExceptionBreakpoint.getCatchType()
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.ExceptionBreakpoint.getCondition()
meth public java.lang.String org.netbeans.api.debugger.jpda.ExceptionBreakpoint.getExceptionClassName()
meth public java.lang.String org.netbeans.api.debugger.jpda.ExceptionBreakpoint.toString()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.ExceptionBreakpoint org.netbeans.api.debugger.jpda.ExceptionBreakpoint.create(java.lang.String,int)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.ExceptionBreakpoint.setCatchType(int)
meth public void org.netbeans.api.debugger.jpda.ExceptionBreakpoint.setCondition(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.ExceptionBreakpoint.setExceptionClassName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
CLSS public abstract interface org.netbeans.api.debugger.jpda.Field
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract boolean org.netbeans.api.debugger.jpda.Field.isStatic()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Field.getClassName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Field.getDeclaredType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Field.getName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getValue()
meth public abstract void org.netbeans.api.debugger.jpda.Field.setValue(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
supr null
CLSS public final org.netbeans.api.debugger.jpda.FieldBreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.FieldBreakpoint.TYPE_ACCESS
fld  constant public static final int org.netbeans.api.debugger.jpda.FieldBreakpoint.TYPE_MODIFICATION
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.PROP_BREAKPOINT_TYPE
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.PROP_CLASS_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.PROP_CONDITION
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.PROP_FIELD_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.FieldBreakpoint.getBreakpointType()
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.getClassName()
meth public java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.getCondition()
meth public java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.getFieldName()
meth public java.lang.String org.netbeans.api.debugger.jpda.FieldBreakpoint.toString()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.FieldBreakpoint org.netbeans.api.debugger.jpda.FieldBreakpoint.create(java.lang.String,java.lang.String,int)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.FieldBreakpoint.setBreakpointType(int)
meth public void org.netbeans.api.debugger.jpda.FieldBreakpoint.setClassName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.FieldBreakpoint.setCondition(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.FieldBreakpoint.setFieldName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
CLSS public org.netbeans.api.debugger.jpda.InvalidExpressionException
cons public InvalidExpressionException(java.lang.String)
cons public InvalidExpressionException(java.lang.Throwable)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public java.lang.Throwable org.netbeans.api.debugger.jpda.InvalidExpressionException.getTargetException()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.Exception
CLSS public org.netbeans.api.debugger.jpda.JPDABreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
supr org.netbeans.api.debugger.Breakpoint
CLSS public abstract org.netbeans.api.debugger.jpda.JPDADebugger
cons public JPDADebugger()
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDADebugger.STATE_DISCONNECTED
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDADebugger.STATE_RUNNING
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDADebugger.STATE_STARTING
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDADebugger.STATE_STOPPED
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDADebugger.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDADebugger.SUSPEND_EVENT_THREAD
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDADebugger.ENGINE_ID
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDADebugger.PROP_CURRENT_THREAD
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDADebugger.PROP_STATE
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDADebugger.PROP_SUSPEND
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDADebugger.SESSION_ID
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.jpda.JPDADebugger.fireBreakpointEvent(org.netbeans.api.debugger.jpda.JPDABreakpoint,org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)
meth public abstract boolean org.netbeans.api.debugger.jpda.JPDADebugger.canFixClasses()
meth public abstract boolean org.netbeans.api.debugger.jpda.JPDADebugger.canPopFrames()
meth public abstract int org.netbeans.api.debugger.jpda.JPDADebugger.getState()
meth public abstract int org.netbeans.api.debugger.jpda.JPDADebugger.getSuspend()
meth public abstract org.netbeans.api.debugger.jpda.CallStackFrame org.netbeans.api.debugger.jpda.JPDADebugger.getCurrentCallStackFrame()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThread org.netbeans.api.debugger.jpda.JPDADebugger.getCurrentThread()
meth public abstract org.netbeans.api.debugger.jpda.SmartSteppingFilter org.netbeans.api.debugger.jpda.JPDADebugger.getSmartSteppingFilter()
meth public abstract org.netbeans.api.debugger.jpda.Variable org.netbeans.api.debugger.jpda.JPDADebugger.evaluate(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.fixClasses(java.util.Map)
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.setSuspend(int)
meth public abstract void org.netbeans.api.debugger.jpda.JPDADebugger.waitRunning() throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDADebugger.canBeModified()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.debugger.jpda.JPDAStep org.netbeans.api.debugger.jpda.JPDADebugger.createJPDAStep(int,int)
meth public static org.netbeans.api.debugger.jpda.JPDADebugger org.netbeans.api.debugger.jpda.JPDADebugger.attach(java.lang.String,[Ljava.lang.Object;) throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public static org.netbeans.api.debugger.jpda.JPDADebugger org.netbeans.api.debugger.jpda.JPDADebugger.attach(java.lang.String,int,[Ljava.lang.Object;) throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public static org.netbeans.api.debugger.jpda.JPDADebugger org.netbeans.api.debugger.jpda.JPDADebugger.listen(com.sun.jdi.connect.ListeningConnector,java.util.Map,[Ljava.lang.Object;) throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public static void org.netbeans.api.debugger.jpda.JPDADebugger.launch(java.lang.String,[Ljava.lang.String;,java.lang.String,boolean)
meth public static void org.netbeans.api.debugger.jpda.JPDADebugger.startListening(com.sun.jdi.connect.ListeningConnector,java.util.Map,[Ljava.lang.Object;) throws org.netbeans.api.debugger.jpda.DebuggerStartException
supr java.lang.Object
CLSS public abstract org.netbeans.api.debugger.jpda.JPDAStep
cons public JPDAStep(org.netbeans.api.debugger.jpda.JPDADebugger,int,int)
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAStep.STEP_INTO
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAStep.STEP_LINE
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAStep.STEP_MIN
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAStep.STEP_OUT
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAStep.STEP_OVER
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDAStep.PROP_STATE_EXEC
fld  protected org.netbeans.api.debugger.jpda.JPDADebugger org.netbeans.api.debugger.jpda.JPDAStep.debugger
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.jpda.JPDAStep.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void org.netbeans.api.debugger.jpda.JPDAStep.addStep(org.netbeans.api.debugger.jpda.JPDAThread)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDAStep.getHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.JPDAStep.getDepth()
meth public int org.netbeans.api.debugger.jpda.JPDAStep.getSize()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.api.debugger.jpda.JPDAStep.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.jpda.JPDAStep.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.jpda.JPDAStep.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.jpda.JPDAStep.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.jpda.JPDAStep.setDepth(int)
meth public void org.netbeans.api.debugger.jpda.JPDAStep.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDAStep.setSize(int)
supr java.lang.Object
CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAThread
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_MONITOR
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_NOT_STARTED
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_RUNNING
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_SLEEPING
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_UNKNOWN
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_WAIT
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDAThread.STATE_ZOMBIE
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.PROP_CALLSTACK
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.PROP_VARIABLES
meth public abstract [Lorg.netbeans.api.debugger.jpda.CallStackFrame; org.netbeans.api.debugger.jpda.JPDAThread.getCallStack() throws com.sun.jdi.AbsentInformationException
meth public abstract [Lorg.netbeans.api.debugger.jpda.CallStackFrame; org.netbeans.api.debugger.jpda.JPDAThread.getCallStack(int,int) throws com.sun.jdi.AbsentInformationException
meth public abstract [Lorg.netbeans.api.debugger.jpda.ObjectVariable; org.netbeans.api.debugger.jpda.JPDAThread.getOwnedMonitors()
meth public abstract boolean org.netbeans.api.debugger.jpda.JPDAThread.isSuspended()
meth public abstract int org.netbeans.api.debugger.jpda.JPDAThread.getLineNumber(java.lang.String)
meth public abstract int org.netbeans.api.debugger.jpda.JPDAThread.getStackDepth()
meth public abstract int org.netbeans.api.debugger.jpda.JPDAThread.getState()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.getClassName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.getMethodName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.getName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.getSourceName(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAThread.getSourcePath(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract org.netbeans.api.debugger.jpda.JPDAThreadGroup org.netbeans.api.debugger.jpda.JPDAThread.getParentThreadGroup()
meth public abstract org.netbeans.api.debugger.jpda.ObjectVariable org.netbeans.api.debugger.jpda.JPDAThread.getContendedMonitor()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAThread.interrupt()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAThread.makeCurrent()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAThread.resume()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAThread.suspend()
supr null
CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAThreadGroup
meth public abstract [Lorg.netbeans.api.debugger.jpda.JPDAThread; org.netbeans.api.debugger.jpda.JPDAThreadGroup.getThreads()
meth public abstract [Lorg.netbeans.api.debugger.jpda.JPDAThreadGroup; org.netbeans.api.debugger.jpda.JPDAThreadGroup.getThreadGroups()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAThreadGroup.getName()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThreadGroup org.netbeans.api.debugger.jpda.JPDAThreadGroup.getParentThreadGroup()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAThreadGroup.resume()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAThreadGroup.suspend()
supr null
CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAWatch
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAWatch.getExceptionDescription()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAWatch.getExpression()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAWatch.getToStringValue() throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAWatch.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.JPDAWatch.getValue()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAWatch.remove()
meth public abstract void org.netbeans.api.debugger.jpda.JPDAWatch.setExpression(java.lang.String)
meth public abstract void org.netbeans.api.debugger.jpda.JPDAWatch.setValue(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
supr null
CLSS public final org.netbeans.api.debugger.jpda.LaunchingDICookie
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.LaunchingDICookie.ID
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.LaunchingDICookie.getSuspend()
meth public com.sun.jdi.VirtualMachine org.netbeans.api.debugger.jpda.LaunchingDICookie.getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,com.sun.jdi.connect.VMStartException,java.io.IOException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.debugger.jpda.LaunchingDICookie.getClassName()
meth public java.lang.String org.netbeans.api.debugger.jpda.LaunchingDICookie.getCommandLine()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static java.lang.String org.netbeans.api.debugger.jpda.LaunchingDICookie.getTransportName()
meth public static org.netbeans.api.debugger.jpda.LaunchingDICookie org.netbeans.api.debugger.jpda.LaunchingDICookie.create(java.lang.String,[Ljava.lang.String;,java.lang.String,boolean)
meth public static org.netbeans.api.debugger.jpda.LaunchingDICookie org.netbeans.api.debugger.jpda.LaunchingDICookie.create(java.lang.String,java.lang.String,java.lang.String,boolean)
supr org.netbeans.api.debugger.jpda.AbstractDICookie
CLSS public org.netbeans.api.debugger.jpda.LineBreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
fld  public static final java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.PROP_CONDITION
fld  public static final java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.PROP_LINE_NUMBER
fld  public static final java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.PROP_SOURCE_NAME
fld  public static final java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.PROP_SOURCE_PATH
fld  public static final java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.PROP_STRATUM
fld  public static final java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.PROP_URL
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public int org.netbeans.api.debugger.jpda.LineBreakpoint.getLineNumber()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.getCondition()
meth public java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.getSourceName()
meth public java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.getSourcePath()
meth public java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.getStratum()
meth public java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.getURL()
meth public java.lang.String org.netbeans.api.debugger.jpda.LineBreakpoint.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.LineBreakpoint org.netbeans.api.debugger.jpda.LineBreakpoint.create(java.lang.String,int)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
meth public void org.netbeans.api.debugger.jpda.LineBreakpoint.setCondition(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.LineBreakpoint.setLineNumber(int)
meth public void org.netbeans.api.debugger.jpda.LineBreakpoint.setSourceName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.LineBreakpoint.setSourcePath(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.LineBreakpoint.setStratum(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.LineBreakpoint.setURL(java.lang.String)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
CLSS public final org.netbeans.api.debugger.jpda.ListeningDICookie
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ListeningDICookie.ID
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public com.sun.jdi.VirtualMachine org.netbeans.api.debugger.jpda.ListeningDICookie.getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,java.io.IOException
meth public com.sun.jdi.connect.ListeningConnector org.netbeans.api.debugger.jpda.ListeningDICookie.getListeningConnector()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.ListeningDICookie.getPortNumber()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.debugger.jpda.ListeningDICookie.getSharedMemoryName()
meth public java.util.Map org.netbeans.api.debugger.jpda.ListeningDICookie.getArgs()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.ListeningDICookie org.netbeans.api.debugger.jpda.ListeningDICookie.create(com.sun.jdi.connect.ListeningConnector,java.util.Map)
meth public static org.netbeans.api.debugger.jpda.ListeningDICookie org.netbeans.api.debugger.jpda.ListeningDICookie.create(int)
meth public static org.netbeans.api.debugger.jpda.ListeningDICookie org.netbeans.api.debugger.jpda.ListeningDICookie.create(java.lang.String)
supr org.netbeans.api.debugger.jpda.AbstractDICookie
CLSS public abstract interface org.netbeans.api.debugger.jpda.LocalVariable
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.LocalVariable.getClassName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.LocalVariable.getDeclaredType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.LocalVariable.getName()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getValue()
meth public abstract void org.netbeans.api.debugger.jpda.LocalVariable.setValue(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
supr null
CLSS public final org.netbeans.api.debugger.jpda.MethodBreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final int org.netbeans.api.debugger.jpda.MethodBreakpoint.TYPE_METHOD_ENTRY
fld  constant public static final int org.netbeans.api.debugger.jpda.MethodBreakpoint.TYPE_METHOD_EXIT
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.PROP_BREAKPOINT_TYPE
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.PROP_CLASS_EXCLUSION_FILTERS
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.PROP_CLASS_FILTERS
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.PROP_CONDITION
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.PROP_METHOD_NAME
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public [Ljava.lang.String; org.netbeans.api.debugger.jpda.MethodBreakpoint.getClassExclusionFilters()
meth public [Ljava.lang.String; org.netbeans.api.debugger.jpda.MethodBreakpoint.getClassFilters()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public int org.netbeans.api.debugger.jpda.MethodBreakpoint.getBreakpointType()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.getCondition()
meth public java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.getMethodName()
meth public java.lang.String org.netbeans.api.debugger.jpda.MethodBreakpoint.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.MethodBreakpoint org.netbeans.api.debugger.jpda.MethodBreakpoint.create()
meth public static org.netbeans.api.debugger.jpda.MethodBreakpoint org.netbeans.api.debugger.jpda.MethodBreakpoint.create(java.lang.String,java.lang.String)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
meth public void org.netbeans.api.debugger.jpda.MethodBreakpoint.setBreakpointType(int)
meth public void org.netbeans.api.debugger.jpda.MethodBreakpoint.setClassExclusionFilters([Ljava.lang.String;)
meth public void org.netbeans.api.debugger.jpda.MethodBreakpoint.setClassFilters([Ljava.lang.String;)
meth public void org.netbeans.api.debugger.jpda.MethodBreakpoint.setCondition(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.MethodBreakpoint.setMethodName(java.lang.String)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
CLSS public abstract interface org.netbeans.api.debugger.jpda.ObjectVariable
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getAllStaticFields(int,int)
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getFields(int,int)
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getInheritedFields(int,int)
meth public abstract int org.netbeans.api.debugger.jpda.ObjectVariable.getFieldsCount()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.ObjectVariable.getToStringValue() throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getValue()
meth public abstract org.netbeans.api.debugger.jpda.Field org.netbeans.api.debugger.jpda.ObjectVariable.getField(java.lang.String)
meth public abstract org.netbeans.api.debugger.jpda.Super org.netbeans.api.debugger.jpda.ObjectVariable.getSuper()
meth public abstract org.netbeans.api.debugger.jpda.Variable org.netbeans.api.debugger.jpda.ObjectVariable.invokeMethod(java.lang.String,java.lang.String,[Lorg.netbeans.api.debugger.jpda.Variable;) throws java.lang.NoSuchMethodException,org.netbeans.api.debugger.jpda.InvalidExpressionException
supr null
CLSS public abstract interface org.netbeans.api.debugger.jpda.SmartSteppingFilter
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.SmartSteppingFilter.PROP_EXCLUSION_PATTERNS
meth public abstract [Ljava.lang.String; org.netbeans.api.debugger.jpda.SmartSteppingFilter.getExclusionPatterns()
meth public abstract void org.netbeans.api.debugger.jpda.SmartSteppingFilter.addExclusionPatterns(java.util.Set)
meth public abstract void org.netbeans.api.debugger.jpda.SmartSteppingFilter.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.debugger.jpda.SmartSteppingFilter.removeExclusionPatterns(java.util.Set)
meth public abstract void org.netbeans.api.debugger.jpda.SmartSteppingFilter.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
CLSS public abstract interface org.netbeans.api.debugger.jpda.Super
intf org.netbeans.api.debugger.jpda.ObjectVariable
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getAllStaticFields(int,int)
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getFields(int,int)
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getInheritedFields(int,int)
meth public abstract int org.netbeans.api.debugger.jpda.ObjectVariable.getFieldsCount()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.ObjectVariable.getToStringValue() throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getValue()
meth public abstract org.netbeans.api.debugger.jpda.Field org.netbeans.api.debugger.jpda.ObjectVariable.getField(java.lang.String)
meth public abstract org.netbeans.api.debugger.jpda.Super org.netbeans.api.debugger.jpda.ObjectVariable.getSuper()
meth public abstract org.netbeans.api.debugger.jpda.Variable org.netbeans.api.debugger.jpda.ObjectVariable.invokeMethod(java.lang.String,java.lang.String,[Lorg.netbeans.api.debugger.jpda.Variable;) throws java.lang.NoSuchMethodException,org.netbeans.api.debugger.jpda.InvalidExpressionException
supr null
CLSS public abstract interface org.netbeans.api.debugger.jpda.This
intf org.netbeans.api.debugger.jpda.ObjectVariable
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getAllStaticFields(int,int)
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getFields(int,int)
meth public abstract [Lorg.netbeans.api.debugger.jpda.Field; org.netbeans.api.debugger.jpda.ObjectVariable.getInheritedFields(int,int)
meth public abstract int org.netbeans.api.debugger.jpda.ObjectVariable.getFieldsCount()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.ObjectVariable.getToStringValue() throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getValue()
meth public abstract org.netbeans.api.debugger.jpda.Field org.netbeans.api.debugger.jpda.ObjectVariable.getField(java.lang.String)
meth public abstract org.netbeans.api.debugger.jpda.Super org.netbeans.api.debugger.jpda.ObjectVariable.getSuper()
meth public abstract org.netbeans.api.debugger.jpda.Variable org.netbeans.api.debugger.jpda.ObjectVariable.invokeMethod(java.lang.String,java.lang.String,[Lorg.netbeans.api.debugger.jpda.Variable;) throws java.lang.NoSuchMethodException,org.netbeans.api.debugger.jpda.InvalidExpressionException
supr null
CLSS public final org.netbeans.api.debugger.jpda.ThreadBreakpoint
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_ALL
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_EVENT_THREAD
fld  constant public static final int org.netbeans.api.debugger.jpda.JPDABreakpoint.SUSPEND_NONE
fld  constant public static final int org.netbeans.api.debugger.jpda.ThreadBreakpoint.TYPE_THREAD_DEATH
fld  constant public static final int org.netbeans.api.debugger.jpda.ThreadBreakpoint.TYPE_THREAD_STARTED
fld  constant public static final int org.netbeans.api.debugger.jpda.ThreadBreakpoint.TYPE_THREAD_STARTED_OR_DEATH
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_DISPOSED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_ENABLED
fld  constant public static final java.lang.String org.netbeans.api.debugger.Breakpoint.PROP_GROUP_NAME
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_HIDDEN
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_PRINT_TEXT
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.PROP_SUSPEND
fld  constant public static final java.lang.String org.netbeans.api.debugger.jpda.ThreadBreakpoint.PROP_BREAKPOINT_TYPE
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.api.debugger.Breakpoint.dispose()
meth protected void org.netbeans.api.debugger.Breakpoint.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isEnabled()
meth public boolean org.netbeans.api.debugger.jpda.JPDABreakpoint.isHidden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.JPDABreakpoint.getSuspend()
meth public int org.netbeans.api.debugger.jpda.ThreadBreakpoint.getBreakpointType()
meth public java.lang.String org.netbeans.api.debugger.Breakpoint.getGroupName()
meth public java.lang.String org.netbeans.api.debugger.jpda.JPDABreakpoint.getPrintText()
meth public java.lang.String org.netbeans.api.debugger.jpda.ThreadBreakpoint.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.debugger.jpda.ThreadBreakpoint org.netbeans.api.debugger.jpda.ThreadBreakpoint.create()
meth public synchronized void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public synchronized void org.netbeans.api.debugger.jpda.JPDABreakpoint.removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void org.netbeans.api.debugger.Breakpoint.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void org.netbeans.api.debugger.Breakpoint.setGroupName(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.disable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.enable()
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setHidden(boolean)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setPrintText(java.lang.String)
meth public void org.netbeans.api.debugger.jpda.JPDABreakpoint.setSuspend(int)
meth public void org.netbeans.api.debugger.jpda.ThreadBreakpoint.setBreakpointType(int)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
CLSS public abstract interface org.netbeans.api.debugger.jpda.Variable
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getType()
meth public abstract java.lang.String org.netbeans.api.debugger.jpda.Variable.getValue()
supr null
CLSS public final org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent
cons public JPDABreakpointEvent(org.netbeans.api.debugger.jpda.JPDABreakpoint,org.netbeans.api.debugger.jpda.JPDADebugger,int,org.netbeans.api.debugger.jpda.JPDAThread,com.sun.jdi.ReferenceType,org.netbeans.api.debugger.jpda.Variable)
cons public JPDABreakpointEvent(org.netbeans.api.debugger.jpda.JPDABreakpoint,org.netbeans.api.debugger.jpda.JPDADebugger,java.lang.Throwable,org.netbeans.api.debugger.jpda.JPDAThread,com.sun.jdi.ReferenceType,org.netbeans.api.debugger.jpda.Variable)
fld  constant public static final int org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.CONDITION_FAILED
fld  constant public static final int org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.CONDITION_FALSE
fld  constant public static final int org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.CONDITION_NONE
fld  constant public static final int org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.CONDITION_TRUE
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getResume()
meth public com.sun.jdi.ReferenceType org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getReferenceType()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getConditionResult()
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public java.lang.Throwable org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getConditionException()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.debugger.jpda.JPDADebugger org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getDebugger()
meth public org.netbeans.api.debugger.jpda.JPDAThread org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getThread()
meth public org.netbeans.api.debugger.jpda.Variable org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.getVariable()
meth public void org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent.resume()
supr java.util.EventObject
CLSS public abstract interface org.netbeans.api.debugger.jpda.event.JPDABreakpointListener
intf java.util.EventListener
meth public abstract void org.netbeans.api.debugger.jpda.event.JPDABreakpointListener.breakpointReached(org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)
supr null
CLSS public abstract org.netbeans.spi.debugger.jpda.EditorContext
cons public EditorContext()
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.BREAKPOINT_ANNOTATION_TYPE
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.CURRENT_LINE_ANNOTATION_TYPE
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE
fld  public static final java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.PROP_LINE_NUMBER
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.EditorContext.getImports(java.lang.String)
meth public abstract boolean org.netbeans.spi.debugger.jpda.EditorContext.showSource(java.lang.String,int,java.lang.Object)
meth public abstract int org.netbeans.spi.debugger.jpda.EditorContext.getCurrentLineNumber()
meth public abstract int org.netbeans.spi.debugger.jpda.EditorContext.getFieldLineNumber(java.lang.String,java.lang.String,java.lang.String)
meth public abstract int org.netbeans.spi.debugger.jpda.EditorContext.getLineNumber(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object org.netbeans.spi.debugger.jpda.EditorContext.annotate(java.lang.String,int,java.lang.String,java.lang.Object)
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getClassName(java.lang.String,int)
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getCurrentClassName()
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getCurrentFieldName()
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getCurrentMethodName()
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getCurrentURL()
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getSelectedIdentifier()
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.EditorContext.getSelectedMethodName()
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.createTimeStamp(java.lang.Object)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.disposeTimeStamp(java.lang.Object)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.removeAnnotation(java.lang.Object)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.jpda.EditorContext.updateTimeStamp(java.lang.Object,java.lang.String)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.debugger.jpda.SmartSteppingCallback
cons public SmartSteppingCallback()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.spi.debugger.jpda.SmartSteppingCallback.stopHere(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public abstract void org.netbeans.spi.debugger.jpda.SmartSteppingCallback.initFilter(org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.debugger.jpda.SourcePathProvider
cons public SourcePathProvider()
fld  constant public static final java.lang.String org.netbeans.spi.debugger.jpda.SourcePathProvider.PROP_SOURCE_ROOTS
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.SourcePathProvider.getOriginalSourceRoots()
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.SourcePathProvider.getSourceRoots()
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.SourcePathProvider.getRelativePath(java.lang.String,char,boolean)
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.SourcePathProvider.getURL(java.lang.String,boolean)
meth public abstract void org.netbeans.spi.debugger.jpda.SourcePathProvider.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.jpda.SourcePathProvider.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.jpda.SourcePathProvider.setSourceRoots([Ljava.lang.String;)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.debugger.jpda.VariablesFilter
cons public VariablesFilter()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Ljava.lang.Object; org.netbeans.spi.debugger.jpda.VariablesFilter.getChildren(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.VariablesFilter.getSupportedAncestors()
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.VariablesFilter.getSupportedTypes()
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.debugger.jpda.VariablesFilter.getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.debugger.jpda.VariablesFilter.isLeaf(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.debugger.jpda.VariablesFilter.isReadOnly(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int org.netbeans.spi.debugger.jpda.VariablesFilter.getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object org.netbeans.spi.debugger.jpda.VariablesFilter.getValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.VariablesFilter.getDisplayName(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.VariablesFilter.getIconBase(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.debugger.jpda.VariablesFilter.getShortDescription(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.debugger.jpda.VariablesFilter.performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.debugger.jpda.VariablesFilter.setValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.debugger.jpda.VariablesFilterAdapter
cons public VariablesFilterAdapter()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.Object; org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getChildren(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public [Ljavax.swing.Action; org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getSupportedAncestors()
meth public abstract [Ljava.lang.String; org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getSupportedTypes()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.isLeaf(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.isReadOnly(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getDisplayName(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getIconBase(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.getShortDescription(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void org.netbeans.spi.debugger.jpda.VariablesFilterAdapter.setValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr org.netbeans.spi.debugger.jpda.VariablesFilter
