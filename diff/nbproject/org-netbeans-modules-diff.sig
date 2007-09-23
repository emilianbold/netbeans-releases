#API master signature file
#Version 1.5.0_11
CLSS public static final org.netbeans.api.diff.Difference$Part
cons public Part(int,int,int,int)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.diff.Difference$Part.getEndPosition()
meth public int org.netbeans.api.diff.Difference$Part.getLine()
meth public int org.netbeans.api.diff.Difference$Part.getStartPosition()
meth public int org.netbeans.api.diff.Difference$Part.getType()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.api.diff.Diff
cons public Diff()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.awt.Component org.netbeans.api.diff.Diff.createDiff(java.lang.String,java.lang.String,java.io.Reader,java.lang.String,java.lang.String,java.io.Reader,java.lang.String) throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.diff.DiffView org.netbeans.api.diff.Diff.createDiff(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
meth public static java.util.Collection org.netbeans.api.diff.Diff.getAll()
meth public static org.netbeans.api.diff.Diff org.netbeans.api.diff.Diff.getDefault()
supr java.lang.Object
CLSS public abstract interface org.netbeans.api.diff.DiffView
fld  constant public static final java.lang.String org.netbeans.api.diff.DiffView.PROP_DIFF_COUNT
meth public abstract boolean org.netbeans.api.diff.DiffView.canSetCurrentDifference()
meth public abstract int org.netbeans.api.diff.DiffView.getCurrentDifference() throws java.lang.UnsupportedOperationException
meth public abstract int org.netbeans.api.diff.DiffView.getDifferenceCount()
meth public abstract java.awt.Component org.netbeans.api.diff.DiffView.getComponent()
meth public abstract javax.swing.JToolBar org.netbeans.api.diff.DiffView.getToolBar()
meth public abstract void org.netbeans.api.diff.DiffView.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.diff.DiffView.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.diff.DiffView.setCurrentDifference(int) throws java.lang.UnsupportedOperationException
supr null
CLSS public org.netbeans.api.diff.Difference
cons public Difference(int,int,int,int,int)
cons public Difference(int,int,int,int,int,java.lang.String,java.lang.String)
cons public Difference(int,int,int,int,int,java.lang.String,java.lang.String,[Lorg.netbeans.api.diff.Difference$Part;,[Lorg.netbeans.api.diff.Difference$Part;)
fld  constant public static final int org.netbeans.api.diff.Difference.ADD
fld  constant public static final int org.netbeans.api.diff.Difference.CHANGE
fld  constant public static final int org.netbeans.api.diff.Difference.DELETE
innr public static final org.netbeans.api.diff.Difference$Part
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.netbeans.api.diff.Difference$Part; org.netbeans.api.diff.Difference.getFirstLineDiffs()
meth public [Lorg.netbeans.api.diff.Difference$Part; org.netbeans.api.diff.Difference.getSecondLineDiffs()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.diff.Difference.getFirstEnd()
meth public int org.netbeans.api.diff.Difference.getFirstStart()
meth public int org.netbeans.api.diff.Difference.getSecondEnd()
meth public int org.netbeans.api.diff.Difference.getSecondStart()
meth public int org.netbeans.api.diff.Difference.getType()
meth public java.lang.String org.netbeans.api.diff.Difference.getFirstText()
meth public java.lang.String org.netbeans.api.diff.Difference.getSecondText()
meth public java.lang.String org.netbeans.api.diff.Difference.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.api.diff.StreamSource
cons public StreamSource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.io.Reader org.netbeans.api.diff.StreamSource.createReader() throws java.io.IOException
meth public abstract java.io.Writer org.netbeans.api.diff.StreamSource.createWriter([Lorg.netbeans.api.diff.Difference;) throws java.io.IOException
meth public abstract java.lang.String org.netbeans.api.diff.StreamSource.getMIMEType()
meth public abstract java.lang.String org.netbeans.api.diff.StreamSource.getName()
meth public abstract java.lang.String org.netbeans.api.diff.StreamSource.getTitle()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.diff.StreamSource org.netbeans.api.diff.StreamSource.createSource(java.lang.String,java.lang.String,java.lang.String,java.io.File)
meth public static org.netbeans.api.diff.StreamSource org.netbeans.api.diff.StreamSource.createSource(java.lang.String,java.lang.String,java.lang.String,java.io.Reader)
meth public void org.netbeans.api.diff.StreamSource.close()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.diff.DiffProvider
cons public DiffProvider()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Lorg.netbeans.api.diff.Difference; org.netbeans.spi.diff.DiffProvider.computeDiff(java.io.Reader,java.io.Reader) throws java.io.IOException
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
CLSS public abstract org.netbeans.spi.diff.DiffVisualizer
cons public DiffVisualizer()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.awt.Component org.netbeans.spi.diff.DiffVisualizer.createView([Lorg.netbeans.api.diff.Difference;,java.lang.String,java.lang.String,java.io.Reader,java.lang.String,java.lang.String,java.io.Reader,java.lang.String) throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.diff.DiffView org.netbeans.spi.diff.DiffVisualizer.createDiff([Lorg.netbeans.api.diff.Difference;,org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
supr java.lang.Object
CLSS public abstract org.netbeans.spi.diff.MergeVisualizer
cons public MergeVisualizer()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.awt.Component org.netbeans.spi.diff.MergeVisualizer.createView([Lorg.netbeans.api.diff.Difference;,org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
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
