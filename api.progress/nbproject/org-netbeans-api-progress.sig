#API master signature file
#Version 1.10.1
CLSS public final org.netbeans.api.progress.ProgressHandle
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
meth public void org.netbeans.api.progress.ProgressHandle.finish()
meth public void org.netbeans.api.progress.ProgressHandle.progress(int)
meth public void org.netbeans.api.progress.ProgressHandle.progress(java.lang.String)
meth public void org.netbeans.api.progress.ProgressHandle.progress(java.lang.String,int)
meth public void org.netbeans.api.progress.ProgressHandle.setDisplayName(java.lang.String)
meth public void org.netbeans.api.progress.ProgressHandle.setInitialDelay(int)
meth public void org.netbeans.api.progress.ProgressHandle.start()
meth public void org.netbeans.api.progress.ProgressHandle.start(int)
meth public void org.netbeans.api.progress.ProgressHandle.start(int,long)
meth public void org.netbeans.api.progress.ProgressHandle.suspend(java.lang.String)
meth public void org.netbeans.api.progress.ProgressHandle.switchToDeterminate(int)
meth public void org.netbeans.api.progress.ProgressHandle.switchToDeterminate(int,long)
meth public void org.netbeans.api.progress.ProgressHandle.switchToIndeterminate()
supr java.lang.Object
CLSS public final org.netbeans.api.progress.ProgressHandleFactory
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
meth public static javax.swing.JComponent org.netbeans.api.progress.ProgressHandleFactory.createProgressComponent(org.netbeans.api.progress.ProgressHandle)
meth public static javax.swing.JLabel org.netbeans.api.progress.ProgressHandleFactory.createDetailLabelComponent(org.netbeans.api.progress.ProgressHandle)
meth public static javax.swing.JLabel org.netbeans.api.progress.ProgressHandleFactory.createMainLabelComponent(org.netbeans.api.progress.ProgressHandle)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createHandle(java.lang.String)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createHandle(java.lang.String,javax.swing.Action)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createHandle(java.lang.String,org.openide.util.Cancellable)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createHandle(java.lang.String,org.openide.util.Cancellable,javax.swing.Action)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createSystemHandle(java.lang.String)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createSystemHandle(java.lang.String,org.openide.util.Cancellable)
meth public static org.netbeans.api.progress.ProgressHandle org.netbeans.api.progress.ProgressHandleFactory.createSystemHandle(java.lang.String,org.openide.util.Cancellable,javax.swing.Action)
supr java.lang.Object
CLSS public final org.netbeans.api.progress.aggregate.AggregateProgressFactory
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
meth public static javax.swing.JComponent org.netbeans.api.progress.aggregate.AggregateProgressFactory.createProgressComponent(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public static javax.swing.JLabel org.netbeans.api.progress.aggregate.AggregateProgressFactory.createDetailLabelComponent(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public static javax.swing.JLabel org.netbeans.api.progress.aggregate.AggregateProgressFactory.createMainLabelComponent(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public static org.netbeans.api.progress.aggregate.AggregateProgressHandle org.netbeans.api.progress.aggregate.AggregateProgressFactory.createHandle(java.lang.String,[Lorg.netbeans.api.progress.aggregate.ProgressContributor;,org.openide.util.Cancellable,javax.swing.Action)
meth public static org.netbeans.api.progress.aggregate.AggregateProgressHandle org.netbeans.api.progress.aggregate.AggregateProgressFactory.createSystemHandle(java.lang.String,[Lorg.netbeans.api.progress.aggregate.ProgressContributor;,org.openide.util.Cancellable,javax.swing.Action)
meth public static org.netbeans.api.progress.aggregate.ProgressContributor org.netbeans.api.progress.aggregate.AggregateProgressFactory.createProgressContributor(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.api.progress.aggregate.AggregateProgressHandle
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
meth public synchronized void org.netbeans.api.progress.aggregate.AggregateProgressHandle.addContributor(org.netbeans.api.progress.aggregate.ProgressContributor)
meth public synchronized void org.netbeans.api.progress.aggregate.AggregateProgressHandle.finish()
meth public synchronized void org.netbeans.api.progress.aggregate.AggregateProgressHandle.start(long)
meth public void org.netbeans.api.progress.aggregate.AggregateProgressHandle.setDisplayName(java.lang.String)
meth public void org.netbeans.api.progress.aggregate.AggregateProgressHandle.setInitialDelay(int)
meth public void org.netbeans.api.progress.aggregate.AggregateProgressHandle.setMonitor(org.netbeans.api.progress.aggregate.ProgressMonitor)
meth public void org.netbeans.api.progress.aggregate.AggregateProgressHandle.start()
meth public void org.netbeans.api.progress.aggregate.AggregateProgressHandle.suspend(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.api.progress.aggregate.ProgressContributor
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.progress.aggregate.ProgressContributor.getTrackingId()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.api.progress.aggregate.ProgressContributor.finish()
meth public void org.netbeans.api.progress.aggregate.ProgressContributor.progress(int)
meth public void org.netbeans.api.progress.aggregate.ProgressContributor.progress(java.lang.String)
meth public void org.netbeans.api.progress.aggregate.ProgressContributor.progress(java.lang.String,int)
meth public void org.netbeans.api.progress.aggregate.ProgressContributor.start(int)
supr java.lang.Object
CLSS public abstract interface org.netbeans.api.progress.aggregate.ProgressMonitor
meth public abstract void org.netbeans.api.progress.aggregate.ProgressMonitor.finished(org.netbeans.api.progress.aggregate.ProgressContributor)
meth public abstract void org.netbeans.api.progress.aggregate.ProgressMonitor.progressed(org.netbeans.api.progress.aggregate.ProgressContributor)
meth public abstract void org.netbeans.api.progress.aggregate.ProgressMonitor.started(org.netbeans.api.progress.aggregate.ProgressContributor)
supr null
