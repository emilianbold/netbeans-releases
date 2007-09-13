#Signature file v4.0
#Version 

CLSS public java.lang.Object
cons public Object()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract org.netbeans.spi.debugger.ui.AttachType
cons public AttachType()
meth public abstract java.lang.String getTypeDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
supr java.lang.Object

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointType
cons public BreakpointType()
meth public abstract boolean isDefault()
meth public abstract java.lang.String getCategoryDisplayName()
meth public abstract java.lang.String getTypeDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.debugger.ui.Constants
fld public final static java.lang.String BREAKPOINT_ENABLED_COLUMN_ID = "BreakpointEnabled"
fld public final static java.lang.String CALL_STACK_FRAME_LOCATION_COLUMN_ID = "CallStackFrameLocation"
fld public final static java.lang.String LOCALS_TO_STRING_COLUMN_ID = "LocalsToString"
fld public final static java.lang.String LOCALS_TYPE_COLUMN_ID = "LocalsType"
fld public final static java.lang.String LOCALS_VALUE_COLUMN_ID = "LocalsValue"
fld public final static java.lang.String SESSION_HOST_NAME_COLUMN_ID = "SessionHostName"
fld public final static java.lang.String SESSION_LANGUAGE_COLUMN_ID = "SessionLanguage"
fld public final static java.lang.String SESSION_STATE_COLUMN_ID = "SessionState"
fld public final static java.lang.String THREAD_STATE_COLUMN_ID = "ThreadState"
fld public final static java.lang.String THREAD_SUSPENDED_COLUMN_ID = "ThreadSuspended"
fld public final static java.lang.String WATCH_TO_STRING_COLUMN_ID = "WatchToString"
fld public final static java.lang.String WATCH_TYPE_COLUMN_ID = "WatchType"
fld public final static java.lang.String WATCH_VALUE_COLUMN_ID = "WatchValue"

CLSS public abstract interface org.netbeans.spi.debugger.ui.Controller
fld public final static java.lang.String PROP_VALID = "valid"
meth public abstract boolean cancel()
meth public abstract boolean isValid()
meth public abstract boolean ok()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

