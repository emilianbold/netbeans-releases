#Signature file v4.0
#Version 1.14.1

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

CLSS public abstract interface org.netbeans.api.fileinfo.NonRecursiveFolder
meth public abstract org.openide.filesystems.FileObject getFolder()

CLSS public final org.netbeans.api.queries.CollocationQuery
meth public static boolean areCollocated(java.io.File,java.io.File)
meth public static java.io.File findRoot(java.io.File)
supr java.lang.Object
hfds implementations

CLSS public final org.netbeans.api.queries.FileBuiltQuery
innr public abstract interface static Status
meth public static org.netbeans.api.queries.FileBuiltQuery$Status getStatus(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds implementations

CLSS public abstract interface static org.netbeans.api.queries.FileBuiltQuery$Status
meth public abstract boolean isBuilt()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.api.queries.FileEncodingQuery
meth public static java.nio.charset.Charset getDefaultEncoding()
meth public static java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
meth public static void setDefaultEncoding(java.nio.charset.Charset)
supr java.lang.Object
hfds BUFSIZ,DECODER_SELECTED,DEFAULT_ENCODING,ENCODER_SELECTED,LOG,UTF_8
hcls ProxyCharset

CLSS public final org.netbeans.api.queries.SharabilityQuery
fld public final static int MIXED = 3
fld public final static int NOT_SHARABLE = 2
fld public final static int SHARABLE = 1
fld public final static int UNKNOWN = 0
meth public static int getSharability(java.io.File)
supr java.lang.Object
hfds implementations

CLSS public final org.netbeans.api.queries.VisibilityQuery
meth public boolean isVisible(java.io.File)
meth public boolean isVisible(org.openide.filesystems.FileObject)
meth public final static org.netbeans.api.queries.VisibilityQuery getDefault()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds INSTANCE,cachedVqiInstances,changeSupport,resultListener,vqiListener,vqiResult
hcls ResultListener,VqiChangedListener

CLSS public abstract interface org.netbeans.spi.queries.CollocationQueryImplementation
meth public abstract boolean areCollocated(java.io.File,java.io.File)
meth public abstract java.io.File findRoot(java.io.File)

CLSS public abstract interface org.netbeans.spi.queries.FileBuiltQueryImplementation
meth public abstract org.netbeans.api.queries.FileBuiltQuery$Status getStatus(org.openide.filesystems.FileObject)

CLSS public abstract org.netbeans.spi.queries.FileEncodingQueryImplementation
cons public FileEncodingQueryImplementation()
meth protected static void throwUnknownEncoding()
meth public abstract java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.queries.SharabilityQueryImplementation
meth public abstract int getSharability(java.io.File)

CLSS public abstract interface org.netbeans.spi.queries.VisibilityQueryImplementation
meth public abstract boolean isVisible(org.openide.filesystems.FileObject)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.spi.queries.VisibilityQueryImplementation2
intf org.netbeans.spi.queries.VisibilityQueryImplementation
meth public abstract boolean isVisible(java.io.File)

