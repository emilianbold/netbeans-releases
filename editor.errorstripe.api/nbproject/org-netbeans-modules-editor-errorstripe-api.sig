#Signature file v4.0
#Version 2.6.1

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public final org.netbeans.spi.editor.errorstripe.UpToDateStatus
fld public final static org.netbeans.spi.editor.errorstripe.UpToDateStatus UP_TO_DATE_DIRTY
fld public final static org.netbeans.spi.editor.errorstripe.UpToDateStatus UP_TO_DATE_OK
fld public final static org.netbeans.spi.editor.errorstripe.UpToDateStatus UP_TO_DATE_PROCESSING
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds UP_TO_DATE_DIRTY_VALUE,UP_TO_DATE_OK_VALUE,UP_TO_DATE_PROCESSING_VALUE,status,statusNames

CLSS public abstract org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider
cons public UpToDateStatusProvider()
fld public final static java.lang.String PROP_UP_TO_DATE = "upToDate"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract org.netbeans.spi.editor.errorstripe.UpToDateStatus getUpToDate()
supr java.lang.Object
hfds pcs

CLSS public abstract interface org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory
meth public abstract org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider createUpToDateStatusProvider(javax.swing.text.Document)

