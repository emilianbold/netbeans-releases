#API master signature file
#Version 2.3.1
CLSS public final org.netbeans.spi.editor.errorstripe.UpToDateStatus
fld  public static final org.netbeans.spi.editor.errorstripe.UpToDateStatus org.netbeans.spi.editor.errorstripe.UpToDateStatus.UP_TO_DATE_DIRTY
fld  public static final org.netbeans.spi.editor.errorstripe.UpToDateStatus org.netbeans.spi.editor.errorstripe.UpToDateStatus.UP_TO_DATE_OK
fld  public static final org.netbeans.spi.editor.errorstripe.UpToDateStatus org.netbeans.spi.editor.errorstripe.UpToDateStatus.UP_TO_DATE_PROCESSING
intf java.lang.Comparable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.spi.editor.errorstripe.UpToDateStatus.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.editor.errorstripe.UpToDateStatus.compareTo(java.lang.Object)
meth public int org.netbeans.spi.editor.errorstripe.UpToDateStatus.hashCode()
meth public java.lang.String org.netbeans.spi.editor.errorstripe.UpToDateStatus.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public abstract org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider
cons public UpToDateStatusProvider()
fld  constant public static final java.lang.String org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider.PROP_UP_TO_DATE
meth protected final void org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.netbeans.spi.editor.errorstripe.UpToDateStatus org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider.getUpToDate()
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
CLSS public abstract interface org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory
meth public abstract org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory.createUpToDateStatusProvider(javax.swing.text.Document)
supr null
