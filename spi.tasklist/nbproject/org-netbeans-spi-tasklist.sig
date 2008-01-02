#API master signature file
#Version 1.0.1
CLSS public static final org.netbeans.spi.tasklist.FileTaskScanner$Callback
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public transient void org.netbeans.spi.tasklist.FileTaskScanner$Callback.refresh([Lorg.openide.filesystems.FileObject;)
meth public void org.netbeans.spi.tasklist.FileTaskScanner$Callback.refreshAll()
supr java.lang.Object
CLSS public static final org.netbeans.spi.tasklist.PushTaskScanner$Callback
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.tasklist.PushTaskScanner$Callback.clearAllTasks()
meth public void org.netbeans.spi.tasklist.PushTaskScanner$Callback.finished()
meth public void org.netbeans.spi.tasklist.PushTaskScanner$Callback.setTasks(org.openide.filesystems.FileObject,java.util.List)
meth public void org.netbeans.spi.tasklist.PushTaskScanner$Callback.started()
supr java.lang.Object
CLSS public static final org.netbeans.spi.tasklist.TaskScanningScope$Callback
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.tasklist.TaskScanningScope$Callback.refresh()
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
supr null
CLSS public abstract org.netbeans.spi.tasklist.FileTaskScanner
cons public FileTaskScanner(java.lang.String,java.lang.String,java.lang.String)
innr public static final org.netbeans.spi.tasklist.FileTaskScanner$Callback
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.util.List org.netbeans.spi.tasklist.FileTaskScanner.scan(org.openide.filesystems.FileObject)
meth public abstract void org.netbeans.spi.tasklist.FileTaskScanner.attach(org.netbeans.spi.tasklist.FileTaskScanner$Callback)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.tasklist.FileTaskScanner.notifyFinish()
meth public void org.netbeans.spi.tasklist.FileTaskScanner.notifyPrepare()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.tasklist.PushTaskScanner
cons public PushTaskScanner(java.lang.String,java.lang.String,java.lang.String)
innr public static final org.netbeans.spi.tasklist.PushTaskScanner$Callback
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.netbeans.spi.tasklist.PushTaskScanner.setScope(org.netbeans.spi.tasklist.TaskScanningScope,org.netbeans.spi.tasklist.PushTaskScanner$Callback)
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
CLSS public final org.netbeans.spi.tasklist.Task
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.spi.tasklist.Task.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.tasklist.Task.hashCode()
meth public java.lang.String org.netbeans.spi.tasklist.Task.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static org.netbeans.spi.tasklist.Task org.netbeans.spi.tasklist.Task.create(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,int)
meth public static org.netbeans.spi.tasklist.Task org.netbeans.spi.tasklist.Task.create(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.awt.event.ActionListener)
supr java.lang.Object
CLSS public abstract org.netbeans.spi.tasklist.TaskScanningScope
cons public TaskScanningScope(java.lang.String,java.lang.String,java.awt.Image)
cons public TaskScanningScope(java.lang.String,java.lang.String,java.awt.Image,boolean)
innr public static final org.netbeans.spi.tasklist.TaskScanningScope$Callback
intf java.lang.Iterable
intf org.openide.util.Lookup$Provider
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.spi.tasklist.TaskScanningScope.isInScope(org.openide.filesystems.FileObject)
meth public abstract java.util.Iterator java.lang.Iterable.iterator()
meth public abstract org.openide.util.Lookup org.netbeans.spi.tasklist.TaskScanningScope.getLookup()
meth public abstract void org.netbeans.spi.tasklist.TaskScanningScope.attach(org.netbeans.spi.tasklist.TaskScanningScope$Callback)
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
