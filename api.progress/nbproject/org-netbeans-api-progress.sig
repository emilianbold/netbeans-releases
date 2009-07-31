#Signature file v4.0
#Version 1.13.1

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

CLSS public final org.netbeans.api.progress.ProgressHandle
meth public void finish()
meth public void progress(int)
meth public void progress(java.lang.String)
meth public void progress(java.lang.String,int)
meth public void setDisplayName(java.lang.String)
meth public void setInitialDelay(int)
meth public void start()
meth public void start(int)
meth public void start(int,long)
meth public void suspend(java.lang.String)
meth public void switchToDeterminate(int)
meth public void switchToDeterminate(int,long)
meth public void switchToIndeterminate()
supr java.lang.Object
hfds LOG,internal

CLSS public final org.netbeans.api.progress.ProgressHandleFactory
meth public static javax.swing.JComponent createProgressComponent(org.netbeans.api.progress.ProgressHandle)
meth public static javax.swing.JLabel createDetailLabelComponent(org.netbeans.api.progress.ProgressHandle)
meth public static javax.swing.JLabel createMainLabelComponent(org.netbeans.api.progress.ProgressHandle)
meth public static org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String)
meth public static org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String,javax.swing.Action)
meth public static org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String,org.openide.util.Cancellable)
meth public static org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String,org.openide.util.Cancellable,javax.swing.Action)
meth public static org.netbeans.api.progress.ProgressHandle createSystemHandle(java.lang.String)
meth public static org.netbeans.api.progress.ProgressHandle createSystemHandle(java.lang.String,org.openide.util.Cancellable)
meth public static org.netbeans.api.progress.ProgressHandle createSystemHandle(java.lang.String,org.openide.util.Cancellable,javax.swing.Action)
supr java.lang.Object

CLSS public final org.netbeans.api.progress.aggregate.AggregateProgressFactory
meth public static javax.swing.JComponent createProgressComponent(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public static javax.swing.JLabel createDetailLabelComponent(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public static javax.swing.JLabel createMainLabelComponent(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public static org.netbeans.api.progress.aggregate.AggregateProgressHandle createHandle(java.lang.String,org.netbeans.api.progress.aggregate.ProgressContributor[],org.openide.util.Cancellable,javax.swing.Action)
meth public static org.netbeans.api.progress.aggregate.AggregateProgressHandle createSystemHandle(java.lang.String,org.netbeans.api.progress.aggregate.ProgressContributor[],org.openide.util.Cancellable,javax.swing.Action)
meth public static org.netbeans.api.progress.aggregate.ProgressContributor createProgressContributor(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.progress.aggregate.AggregateProgressHandle
meth public void addContributor(org.netbeans.api.progress.aggregate.ProgressContributor)
meth public void finish()
meth public void setDisplayName(java.lang.String)
meth public void setInitialDelay(int)
meth public void setMonitor(org.netbeans.api.progress.aggregate.ProgressMonitor)
meth public void start()
meth public void start(long)
meth public void suspend(java.lang.String)
supr java.lang.Object
hfds LOG,WORKUNITS,contributors,current,displayName,finished,handle,monitor

CLSS public final org.netbeans.api.progress.aggregate.ProgressContributor
meth public java.lang.String getTrackingId()
meth public void finish()
meth public void progress(int)
meth public void progress(java.lang.String)
meth public void progress(java.lang.String,int)
meth public void start(int)
supr java.lang.Object
hfds current,id,lastParentedUnit,parent,parentUnits,workunits

CLSS public abstract interface org.netbeans.api.progress.aggregate.ProgressMonitor
meth public abstract void finished(org.netbeans.api.progress.aggregate.ProgressContributor)
meth public abstract void progressed(org.netbeans.api.progress.aggregate.ProgressContributor)
meth public abstract void started(org.netbeans.api.progress.aggregate.ProgressContributor)

