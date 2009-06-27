#Signature file v4.0
#Version 7.22.1

CLSS public java.awt.datatransfer.Clipboard
cons public Clipboard(java.lang.String)
fld protected java.awt.datatransfer.ClipboardOwner owner
fld protected java.awt.datatransfer.Transferable contents
meth public boolean isDataFlavorAvailable(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getAvailableDataFlavors()
meth public java.awt.datatransfer.FlavorListener[] getFlavorListeners()
meth public java.awt.datatransfer.Transferable getContents(java.lang.Object)
meth public java.lang.Object getData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String getName()
meth public void addFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void removeFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.lang.Object
hfds currentDataFlavors,flavorListeners,name

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Closeable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.DataInput
meth public abstract boolean readBoolean() throws java.io.IOException
meth public abstract byte readByte() throws java.io.IOException
meth public abstract char readChar() throws java.io.IOException
meth public abstract double readDouble() throws java.io.IOException
meth public abstract float readFloat() throws java.io.IOException
meth public abstract int readInt() throws java.io.IOException
meth public abstract int readUnsignedByte() throws java.io.IOException
meth public abstract int readUnsignedShort() throws java.io.IOException
meth public abstract int skipBytes(int) throws java.io.IOException
meth public abstract java.lang.String readLine() throws java.io.IOException
meth public abstract java.lang.String readUTF() throws java.io.IOException
meth public abstract long readLong() throws java.io.IOException
meth public abstract short readShort() throws java.io.IOException
meth public abstract void readFully(byte[]) throws java.io.IOException
meth public abstract void readFully(byte[],int,int) throws java.io.IOException

CLSS public abstract interface java.io.DataOutput
meth public abstract void write(byte[]) throws java.io.IOException
meth public abstract void write(byte[],int,int) throws java.io.IOException
meth public abstract void write(int) throws java.io.IOException
meth public abstract void writeBoolean(boolean) throws java.io.IOException
meth public abstract void writeByte(int) throws java.io.IOException
meth public abstract void writeBytes(java.lang.String) throws java.io.IOException
meth public abstract void writeChar(int) throws java.io.IOException
meth public abstract void writeChars(java.lang.String) throws java.io.IOException
meth public abstract void writeDouble(double) throws java.io.IOException
meth public abstract void writeFloat(float) throws java.io.IOException
meth public abstract void writeInt(int) throws java.io.IOException
meth public abstract void writeLong(long) throws java.io.IOException
meth public abstract void writeShort(int) throws java.io.IOException
meth public abstract void writeUTF(java.lang.String) throws java.io.IOException

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.IOException
cons public IOException()
cons public IOException(java.lang.String)
cons public IOException(java.lang.String,java.lang.Throwable)
cons public IOException(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract java.io.InputStream
cons public InputStream()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object
hfds SKIP_BUFFER_SIZE,skipBuffer

CLSS public abstract interface java.io.ObjectInput
intf java.io.DataInput
meth public abstract int available() throws java.io.IOException
meth public abstract int read() throws java.io.IOException
meth public abstract int read(byte[]) throws java.io.IOException
meth public abstract int read(byte[],int,int) throws java.io.IOException
meth public abstract java.lang.Object readObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract long skip(long) throws java.io.IOException
meth public abstract void close() throws java.io.IOException

CLSS public java.io.ObjectInputStream
cons protected ObjectInputStream() throws java.io.IOException
cons public ObjectInputStream(java.io.InputStream) throws java.io.IOException
innr public abstract static GetField
intf java.io.ObjectInput
intf java.io.ObjectStreamConstants
meth protected boolean enableResolveObject(boolean)
meth protected java.io.ObjectStreamClass readClassDescriptor() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class<?> resolveProxyClass(java.lang.String[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object readObjectOverride() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object resolveObject(java.lang.Object) throws java.io.IOException
meth protected void readStreamHeader() throws java.io.IOException
meth public boolean readBoolean() throws java.io.IOException
meth public byte readByte() throws java.io.IOException
meth public char readChar() throws java.io.IOException
meth public double readDouble() throws java.io.IOException
meth public final java.lang.Object readObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public float readFloat() throws java.io.IOException
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public int readInt() throws java.io.IOException
meth public int readUnsignedByte() throws java.io.IOException
meth public int readUnsignedShort() throws java.io.IOException
meth public int skipBytes(int) throws java.io.IOException
meth public java.io.ObjectInputStream$GetField readFields() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object readUnshared() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String readLine() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.lang.String readUTF() throws java.io.IOException
meth public long readLong() throws java.io.IOException
meth public short readShort() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void defaultReadObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public void readFully(byte[]) throws java.io.IOException
meth public void readFully(byte[],int,int) throws java.io.IOException
meth public void registerValidation(java.io.ObjectInputValidation,int) throws java.io.InvalidObjectException,java.io.NotActiveException
supr java.io.InputStream
hfds NULL_HANDLE,bin,closed,curContext,defaultDataEnd,depth,enableOverride,enableResolve,handles,passHandle,primClasses,primVals,unsharedMarker,vlist
hcls BlockDataInputStream,Caches,CallbackContext,GetFieldImpl,HandleTable,PeekInputStream,ValidationList

CLSS public abstract interface java.io.ObjectOutput
intf java.io.DataOutput
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(byte[]) throws java.io.IOException
meth public abstract void write(byte[],int,int) throws java.io.IOException
meth public abstract void write(int) throws java.io.IOException
meth public abstract void writeObject(java.lang.Object) throws java.io.IOException

CLSS public java.io.ObjectOutputStream
cons protected ObjectOutputStream() throws java.io.IOException
cons public ObjectOutputStream(java.io.OutputStream) throws java.io.IOException
innr public abstract static PutField
intf java.io.ObjectOutput
intf java.io.ObjectStreamConstants
meth protected boolean enableReplaceObject(boolean)
meth protected java.lang.Object replaceObject(java.lang.Object) throws java.io.IOException
meth protected void annotateClass(java.lang.Class<?>) throws java.io.IOException
meth protected void annotateProxyClass(java.lang.Class<?>) throws java.io.IOException
meth protected void drain() throws java.io.IOException
meth protected void writeClassDescriptor(java.io.ObjectStreamClass) throws java.io.IOException
meth protected void writeObjectOverride(java.lang.Object) throws java.io.IOException
meth protected void writeStreamHeader() throws java.io.IOException
meth public final void writeObject(java.lang.Object) throws java.io.IOException
meth public java.io.ObjectOutputStream$PutField putFields() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void defaultWriteObject() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void reset() throws java.io.IOException
meth public void useProtocolVersion(int) throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void writeBoolean(boolean) throws java.io.IOException
meth public void writeByte(int) throws java.io.IOException
meth public void writeBytes(java.lang.String) throws java.io.IOException
meth public void writeChar(int) throws java.io.IOException
meth public void writeChars(java.lang.String) throws java.io.IOException
meth public void writeDouble(double) throws java.io.IOException
meth public void writeFields() throws java.io.IOException
meth public void writeFloat(float) throws java.io.IOException
meth public void writeInt(int) throws java.io.IOException
meth public void writeLong(long) throws java.io.IOException
meth public void writeShort(int) throws java.io.IOException
meth public void writeUTF(java.lang.String) throws java.io.IOException
meth public void writeUnshared(java.lang.Object) throws java.io.IOException
supr java.io.OutputStream
hfds bout,curDesc,curObj,curPut,debugInfoStack,depth,enableOverride,enableReplace,extendedDebugInfo,handles,primVals,protocol,subs
hcls BlockDataOutputStream,Caches,DebugTraceInfoStack,HandleTable,PutFieldImpl,ReplaceTable

CLSS public abstract interface java.io.ObjectStreamConstants
fld public final static byte SC_BLOCK_DATA = 8
fld public final static byte SC_ENUM = 16
fld public final static byte SC_EXTERNALIZABLE = 4
fld public final static byte SC_SERIALIZABLE = 2
fld public final static byte SC_WRITE_METHOD = 1
fld public final static byte TC_ARRAY = 117
fld public final static byte TC_BASE = 112
fld public final static byte TC_BLOCKDATA = 119
fld public final static byte TC_BLOCKDATALONG = 122
fld public final static byte TC_CLASS = 118
fld public final static byte TC_CLASSDESC = 114
fld public final static byte TC_ENDBLOCKDATA = 120
fld public final static byte TC_ENUM = 126
fld public final static byte TC_EXCEPTION = 123
fld public final static byte TC_LONGSTRING = 124
fld public final static byte TC_MAX = 126
fld public final static byte TC_NULL = 112
fld public final static byte TC_OBJECT = 115
fld public final static byte TC_PROXYCLASSDESC = 125
fld public final static byte TC_REFERENCE = 113
fld public final static byte TC_RESET = 121
fld public final static byte TC_STRING = 116
fld public final static int PROTOCOL_VERSION_1 = 1
fld public final static int PROTOCOL_VERSION_2 = 2
fld public final static int baseWireHandle = 8257536
fld public final static java.io.SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION
fld public final static java.io.SerializablePermission SUBSTITUTION_PERMISSION
fld public final static short STREAM_MAGIC = -21267
fld public final static short STREAM_VERSION = 5

CLSS public abstract java.io.OutputStream
cons public OutputStream()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
intf java.lang.annotation.Annotation

CLSS public java.lang.Exception
cons public Exception()
cons public Exception(java.lang.String)
cons public Exception(java.lang.String,java.lang.Throwable)
cons public Exception(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()

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

CLSS public abstract interface java.lang.Runnable
meth public abstract void run()

CLSS public java.lang.RuntimeException
cons public RuntimeException()
cons public RuntimeException(java.lang.String)
cons public RuntimeException(java.lang.String,java.lang.Throwable)
cons public RuntimeException(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public java.lang.Throwable
cons public Throwable()
cons public Throwable(java.lang.String)
cons public Throwable(java.lang.String,java.lang.Throwable)
cons public Throwable(java.lang.Throwable)
intf java.io.Serializable
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object
hfds backtrace,cause,detailMessage,serialVersionUID,stackTrace

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract java.text.Format
cons protected Format()
innr public static Field
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public abstract java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.String format(java.lang.Object)
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String) throws java.text.ParseException
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
supr java.lang.Object
hfds serialVersionUID
hcls FieldDelegate

CLSS public abstract java.util.AbstractCollection<%0 extends java.lang.Object>
cons protected AbstractCollection()
intf java.util.Collection<{java.util.AbstractCollection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.AbstractCollection%0}> iterator()
meth public boolean add({java.util.AbstractCollection%0})
meth public boolean addAll(java.util.Collection<? extends {java.util.AbstractCollection%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public void clear()
supr java.lang.Object

CLSS public abstract java.util.AbstractSet<%0 extends java.lang.Object>
cons protected AbstractSet()
intf java.util.Set<{java.util.AbstractSet%0}>
meth public boolean equals(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public int hashCode()
supr java.util.AbstractCollection<{java.util.AbstractSet%0}>

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Set%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Set%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Set%0}> iterator()
meth public abstract void clear()

CLSS public abstract interface java.util.concurrent.Executor
meth public abstract void execute(java.lang.Runnable)

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public abstract org.openide.ErrorManager
cons public ErrorManager()
fld public final static int ERROR = 65536
fld public final static int EXCEPTION = 4096
fld public final static int INFORMATIONAL = 1
fld public final static int UNKNOWN = 0
fld public final static int USER = 256
fld public final static int WARNING = 16
innr public abstract interface static Annotation
meth public abstract java.lang.Throwable annotate(java.lang.Throwable,int,java.lang.String,java.lang.String,java.lang.Throwable,java.util.Date)
meth public abstract java.lang.Throwable attachAnnotations(java.lang.Throwable,org.openide.ErrorManager$Annotation[])
meth public abstract org.openide.ErrorManager getInstance(java.lang.String)
meth public abstract org.openide.ErrorManager$Annotation[] findAnnotations(java.lang.Throwable)
meth public abstract void log(int,java.lang.String)
meth public abstract void notify(int,java.lang.Throwable)
meth public boolean isLoggable(int)
meth public boolean isNotifiable(int)
meth public final java.lang.Throwable annotate(java.lang.Throwable,java.lang.String)
meth public final java.lang.Throwable annotate(java.lang.Throwable,java.lang.Throwable)
meth public final java.lang.Throwable copyAnnotation(java.lang.Throwable,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void log(java.lang.String)
meth public final void notify(java.lang.Throwable)
meth public static org.openide.ErrorManager getDefault()
supr java.lang.Object
hfds current
hcls AnnException,DelegatingErrorManager,OwnLevel

CLSS public abstract interface static org.openide.ErrorManager$Annotation
meth public abstract int getSeverity()
meth public abstract java.lang.String getLocalizedMessage()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.Throwable getStackTrace()
meth public abstract java.util.Date getDate()

CLSS public abstract org.openide.LifecycleManager
cons protected LifecycleManager()
meth public abstract void exit()
meth public abstract void saveAll()
meth public static org.openide.LifecycleManager getDefault()
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public ServiceType()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public final static org.openide.ServiceType$Handle
 anno 0 java.lang.Deprecated()
cons public Handle(org.openide.ServiceType)
intf java.io.Serializable
meth public java.lang.String toString()
meth public org.openide.ServiceType getServiceType()
supr java.lang.Object
hfds className,name,serialVersionUID,serviceType

CLSS public abstract static org.openide.ServiceType$Registry
 anno 0 java.lang.Deprecated()
cons public Registry()
intf java.io.Serializable
meth public <%0 extends org.openide.ServiceType> java.util.Enumeration<{%%0}> services(java.lang.Class<{%%0}>)
meth public abstract java.util.Enumeration<org.openide.ServiceType> services()
meth public abstract java.util.List getServiceTypes()
meth public abstract void setServiceTypes(java.util.List)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.String)
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface org.openide.util.AsyncGUIJob
meth public abstract void construct()
meth public abstract void finished()

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

CLSS public final org.openide.util.ChangeSupport
cons public ChangeSupport(java.lang.Object)
meth public boolean hasListeners()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void fireChange()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds listeners,source

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

CLSS public abstract interface org.openide.util.ContextGlobalProvider
meth public abstract org.openide.util.Lookup createGlobalContext()

CLSS public final org.openide.util.Enumerations
innr public abstract interface static Processor
meth public !varargs static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> array({%%0}[])
meth public final static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> empty()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> convert(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> filter(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> queue(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> concat(java.util.Enumeration<? extends java.util.Enumeration<? extends {%%0}>>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> concat(java.util.Enumeration<? extends {%%0}>,java.util.Enumeration<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> removeDuplicates(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> removeNulls(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> singleton({%%0})
supr java.lang.Object
hcls AltEn,FilEn,QEn,RNulls,SeqEn

CLSS public abstract interface static org.openide.util.Enumerations$Processor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.openide.util.Enumerations$Processor%1} process({org.openide.util.Enumerations$Processor%0},java.util.Collection<{org.openide.util.Enumerations$Processor%0}>)

CLSS public final org.openide.util.Exceptions
meth public static <%0 extends java.lang.Throwable> {%%0} attachLocalizedMessage({%%0},java.lang.String)
meth public static <%0 extends java.lang.Throwable> {%%0} attachMessage({%%0},java.lang.String)
meth public static java.lang.String findLocalizedMessage(java.lang.Throwable)
meth public static void printStackTrace(java.lang.Throwable)
supr java.lang.Object
hfds LOC_MSG_PLACEHOLDER,LOG
hcls AnnException,OwnLevel

CLSS public final org.openide.util.HelpCtx
cons public HelpCtx(java.lang.Class)
cons public HelpCtx(java.lang.String)
cons public HelpCtx(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Provider
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public final org.openide.util.ImageUtilities
meth public final static java.awt.Image addToolTipToImage(java.awt.Image,java.lang.String)
meth public final static java.awt.Image assignToolTipToImage(java.awt.Image,java.lang.String)
meth public final static java.awt.Image icon2Image(javax.swing.Icon)
meth public final static java.awt.Image loadImage(java.lang.String)
meth public final static java.awt.Image loadImage(java.lang.String,boolean)
meth public final static java.awt.Image mergeImages(java.awt.Image,java.awt.Image,int,int)
meth public final static java.lang.String getImageToolTip(java.awt.Image)
meth public final static javax.swing.Icon image2Icon(java.awt.Image)
meth public final static javax.swing.ImageIcon loadImageIcon(java.lang.String,boolean)
supr java.lang.Object
hfds ERR,NO_ICON,PNG_READER,TOOLTIP_SEPAR,cache,component,compositeCache,currentLoader,extraInitialSlashes,imageToolTipCache,loaderQuery,localizedCache,mediaTrackerID,noLoaderWarned,tracker
hcls ActiveRef,CompositeImageKey,ToolTipImage,ToolTipImageKey

CLSS public abstract org.openide.util.Lookup
cons public Lookup()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds defaultLookup
hcls DefLookup,Empty

CLSS public abstract static org.openide.util.Lookup$Item<%0 extends java.lang.Object>
cons public Item()
meth public abstract java.lang.Class<? extends {org.openide.util.Lookup$Item%0}> getType()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract {org.openide.util.Lookup$Item%0} getInstance()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface static org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract static org.openide.util.Lookup$Result<%0 extends java.lang.Object>
cons public Result()
meth public abstract java.util.Collection<? extends {org.openide.util.Lookup$Result%0}> allInstances()
meth public abstract void addLookupListener(org.openide.util.LookupListener)
meth public abstract void removeLookupListener(org.openide.util.LookupListener)
meth public java.util.Collection<? extends org.openide.util.Lookup$Item<{org.openide.util.Lookup$Result%0}>> allItems()
meth public java.util.Set<java.lang.Class<? extends {org.openide.util.Lookup$Result%0}>> allClasses()
supr java.lang.Object

CLSS public final static org.openide.util.Lookup$Template<%0 extends java.lang.Object>
cons public Template()
 anno 0 java.lang.Deprecated()
cons public Template(java.lang.Class<{org.openide.util.Lookup$Template%0}>)
cons public Template(java.lang.Class<{org.openide.util.Lookup$Template%0}>,java.lang.String,{org.openide.util.Lookup$Template%0})
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Class<{org.openide.util.Lookup$Template%0}> getType()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public {org.openide.util.Lookup$Template%0} getInstance()
supr java.lang.Object
hfds hashCode,id,instance,type

CLSS public final org.openide.util.LookupEvent
cons public LookupEvent(org.openide.util.Lookup$Result)
supr java.util.EventObject

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

CLSS public org.openide.util.MapFormat
cons public MapFormat(java.util.Map)
meth protected java.lang.Object processKey(java.lang.String)
meth public boolean isExactMatch()
meth public boolean willThrowExceptionIfKeyWasNotFound()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.String getLeftBrace()
meth public java.lang.String getRightBrace()
meth public java.lang.String parse(java.lang.String)
meth public java.lang.String processPattern(java.lang.String)
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.util.Map getMap()
meth public static java.lang.String format(java.lang.String,java.util.Map)
meth public void setExactMatch(boolean)
meth public void setLeftBrace(java.lang.String)
meth public void setMap(java.util.Map)
meth public void setRightBrace(java.lang.String)
meth public void setThrowExceptionIfKeyWasNotFound(boolean)
supr java.text.Format
hfds BUFSIZE,argmap,arguments,exactmatch,ldel,locale,maxOffset,offsets,rdel,serialVersionUID,throwex

CLSS public final org.openide.util.Mutex
cons public Mutex()
cons public Mutex(java.lang.Object)
cons public Mutex(org.openide.util.Mutex$Privileged)
cons public Mutex(org.openide.util.Mutex$Privileged,java.util.concurrent.Executor)
fld public final static org.openide.util.Mutex EVENT
innr public abstract interface static Action
innr public abstract interface static ExceptionAction
innr public final static Privileged
meth public <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$Action<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$Action<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public boolean isReadAccess()
meth public boolean isWriteAccess()
meth public java.lang.String toString()
meth public void postReadRequest(java.lang.Runnable)
meth public void postWriteRequest(java.lang.Runnable)
meth public void readAccess(java.lang.Runnable)
meth public void writeAccess(java.lang.Runnable)
supr java.lang.Object
hfds CHAIN,LOCK,LOG,MODE_COUNT,NONE,S,X,beStrict,cmatrix,cnt,counter,grantedMode,origMode,readersNo,registeredThreads,waiters,wrapper
hcls InternalLock,QueueCell,ThreadInfo

CLSS public abstract interface static org.openide.util.Mutex$Action<%0 extends java.lang.Object>
intf org.openide.util.Mutex$ExceptionAction<{org.openide.util.Mutex$Action%0}>
meth public abstract {org.openide.util.Mutex$Action%0} run()

CLSS public abstract interface static org.openide.util.Mutex$ExceptionAction<%0 extends java.lang.Object>
meth public abstract {org.openide.util.Mutex$ExceptionAction%0} run() throws java.lang.Exception

CLSS public final static org.openide.util.Mutex$Privileged
cons public Privileged()
meth public void enterReadAccess()
meth public void enterWriteAccess()
meth public void exitReadAccess()
meth public void exitWriteAccess()
supr java.lang.Object
hfds parent

CLSS public org.openide.util.MutexException
cons public MutexException(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.Throwable getCause()
supr java.lang.Exception
hfds ex,serialVersionUID

CLSS public org.openide.util.NbBundle
cons public NbBundle()
 anno 0 java.lang.Deprecated()
innr public abstract interface static ClassLoaderFinder
meth public final static java.util.ResourceBundle getBundle(java.lang.String)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader)
meth public static <%0 extends java.lang.Object> {%%0} getLocalizedValue(java.util.Map<java.lang.String,{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} getLocalizedValue(java.util.Map<java.lang.String,{%%0}>,java.lang.String,java.util.Locale)
meth public static java.lang.String getBranding()
meth public static java.lang.String getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name)
meth public static java.lang.String getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name,java.util.Locale)
meth public static java.lang.String getMessage(java.lang.Class,java.lang.String)
meth public static java.lang.String getMessage(java.lang.Class,java.lang.String,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class,java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class,java.lang.String,java.lang.Object[])
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale,java.lang.ClassLoader)
 anno 0 java.lang.Deprecated()
meth public static java.util.Iterator<java.lang.String> getLocalizingSuffixes()
meth public static java.util.ResourceBundle getBundle(java.lang.Class)
meth public static void setBranding(java.lang.String)
meth public static void setClassLoaderFinder(org.openide.util.NbBundle$ClassLoaderFinder)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds LOG,USE_DEBUG_LOADER,brandingToken,bundleCache,localizedFileCache
hcls AttributesMap,DebugLoader,LocaleIterator,MergedBundle,PBundle

CLSS public abstract interface static org.openide.util.NbBundle$ClassLoaderFinder
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.ClassLoader find()
 anno 0 java.lang.Deprecated()

CLSS public org.openide.util.NbCollections
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedMapByCopy(java.util.Map,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedMapByFilter(java.util.Map,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,boolean)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> iterable(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> iterable(java.util.Iterator<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> checkedEnumerationByFilter(java.util.Enumeration,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> checkedIteratorByFilter(java.util.Iterator,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> checkedListByCopy(java.util.List,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> checkedSetByCopy(java.util.Set,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> checkedSetByFilter(java.util.Set,java.lang.Class<{%%0}>,boolean)
supr java.lang.Object
hfds LOG
hcls CheckedIterator,CheckedMap,CheckedSet

CLSS public final org.openide.util.NbPreferences
meth public static java.util.prefs.Preferences forModule(java.lang.Class)
meth public static java.util.prefs.Preferences root()
supr java.lang.Object
hfds PREFS_IMPL

CLSS public org.openide.util.NotImplementedException
cons public NotImplementedException()
cons public NotImplementedException(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public org.openide.util.Parameters
meth public static void javaIdentifier(java.lang.CharSequence,java.lang.CharSequence)
meth public static void javaIdentifierOrNull(java.lang.CharSequence,java.lang.CharSequence)
meth public static void notEmpty(java.lang.CharSequence,java.lang.CharSequence)
meth public static void notNull(java.lang.CharSequence,java.lang.Object)
meth public static void notWhitespace(java.lang.CharSequence,java.lang.CharSequence)
supr java.lang.Object

CLSS public org.openide.util.Queue<%0 extends java.lang.Object>
 anno 0 java.lang.Deprecated()
cons public Queue()
meth public void put({org.openide.util.Queue%0})
meth public {org.openide.util.Queue%0} get()
supr java.lang.Object
hfds queue

CLSS public final org.openide.util.RequestProcessor
cons public RequestProcessor()
cons public RequestProcessor(java.lang.String)
cons public RequestProcessor(java.lang.String,int)
cons public RequestProcessor(java.lang.String,int,boolean)
innr public final Task
intf java.util.concurrent.Executor
meth public boolean isRequestProcessorThread()
meth public org.openide.util.RequestProcessor$Task create(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task create(java.lang.Runnable,boolean)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable,int)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable,int,int)
meth public static org.openide.util.RequestProcessor getDefault()
meth public static org.openide.util.RequestProcessor$Task createRequest(java.lang.Runnable)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable,int)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable,int,int)
 anno 0 java.lang.Deprecated()
meth public void execute(java.lang.Runnable)
meth public void stop()
supr java.lang.Object
hfds DEFAULT,SLOW,UNLIMITED,counter,interruptThread,logger,name,processorLock,processors,queue,running,starterThread,stopped,throughput
hcls EnqueueTask,Item,Processor

CLSS public final org.openide.util.RequestProcessor$Task
intf org.openide.util.Cancellable
meth public boolean cancel()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public int getDelay()
meth public int getPriority()
meth public java.lang.String toString()
meth public void run()
meth public void schedule(int)
meth public void setPriority(int)
meth public void waitFinished()
supr org.openide.util.Task
hfds item,lastThread,priority,time

CLSS public abstract org.openide.util.SharedClassObject
cons protected SharedClassObject()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public org.openide.util.Task
cons protected Task()
cons public Task(java.lang.Runnable)
fld public final static org.openide.util.Task EMPTY
intf java.lang.Runnable
meth protected final void notifyFinished()
meth protected final void notifyRunning()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public final boolean isFinished()
meth public java.lang.String toString()
meth public void addTaskListener(org.openide.util.TaskListener)
meth public void removeTaskListener(org.openide.util.TaskListener)
meth public void run()
meth public void waitFinished()
supr java.lang.Object
hfds RP,finished,list,overrides,run

CLSS public abstract interface org.openide.util.TaskListener
intf java.util.EventListener
meth public abstract void taskFinished(org.openide.util.Task)

CLSS public final org.openide.util.TopologicalSortException
meth public final java.util.List partialSort()
meth public final java.util.Set[] topologicalSets()
meth public final java.util.Set[] unsortableSets()
meth public final void printStackTrace(java.io.PrintStream)
meth public final void printStackTrace(java.io.PrintWriter)
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr java.lang.Exception
hfds counter,dualGraph,edges,result,vertexes
hcls Vertex

CLSS public abstract org.openide.util.Union2<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract boolean hasFirst()
meth public abstract boolean hasSecond()
meth public abstract org.openide.util.Union2<{org.openide.util.Union2%0},{org.openide.util.Union2%1}> clone()
meth public abstract {org.openide.util.Union2%0} first()
meth public abstract {org.openide.util.Union2%1} second()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Union2<{%%0},{%%1}> createFirst({%%0})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Union2<{%%0},{%%1}> createSecond({%%1})
supr java.lang.Object
hfds serialVersionUID
hcls Union2First,Union2Second

CLSS public org.openide.util.UserCancelException
cons public UserCancelException()
cons public UserCancelException(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract org.openide.util.UserQuestionException
cons public UserQuestionException()
cons public UserQuestionException(java.lang.String)
meth public abstract void confirmed() throws java.io.IOException
supr java.io.IOException
hfds serialVersionUID

CLSS public final org.openide.util.Utilities
fld public final static int OS_AIX = 64
fld public final static int OS_DEC = 1024
 anno 0 java.lang.Deprecated()
fld public final static int OS_FREEBSD = 131072
fld public final static int OS_HP = 32
fld public final static int OS_IRIX = 128
fld public final static int OS_LINUX = 16
fld public final static int OS_MAC = 4096
fld public final static int OS_OPENBSD = 1048576
fld public final static int OS_OS2 = 2048
fld public final static int OS_OTHER = 65536
fld public final static int OS_SOLARIS = 8
fld public final static int OS_SUNOS = 256
fld public final static int OS_TRU64 = 512
fld public final static int OS_UNIX_MASK = 1709048
 anno 0 java.lang.Deprecated()
fld public final static int OS_UNIX_OTHER = 524288
fld public final static int OS_VMS = 16384
fld public final static int OS_WIN2000 = 8192
fld public final static int OS_WIN95 = 2
fld public final static int OS_WIN98 = 4
fld public final static int OS_WINDOWS_MASK = 303111
 anno 0 java.lang.Deprecated()
fld public final static int OS_WINNT = 1
fld public final static int OS_WINVISTA = 262144
fld public final static int OS_WIN_OTHER = 32768
fld public final static int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27
innr public static UnorderableException
meth public final static boolean isJavaIdentifier(java.lang.String)
meth public final static boolean isLargeFrameIcons()
 anno 0 java.lang.Deprecated()
meth public final static boolean isMac()
meth public final static boolean isUnix()
meth public final static boolean isWindows()
meth public final static int getOperatingSystem()
meth public final static int showJFileChooser(javax.swing.JFileChooser,java.awt.Component,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final static java.awt.Cursor createProgressCursor(java.awt.Component)
meth public final static java.awt.Dimension getScreenSize()
 anno 0 java.lang.Deprecated()
meth public final static java.awt.Image icon2Image(javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public final static java.awt.Image loadImage(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final static java.awt.Image loadImage(java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
meth public final static java.awt.Image mergeImages(java.awt.Image,java.awt.Image,int,int)
 anno 0 java.lang.Deprecated()
meth public final static java.lang.String pureClassName(java.lang.String)
meth public final static void attachInitJob(java.awt.Component,org.openide.util.AsyncGUIJob)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> topologicalSort(java.util.Collection<{%%0}>,java.util.Map<? super {%%0},? extends java.util.Collection<? extends {%%0}>>) throws org.openide.util.TopologicalSortException
meth public static boolean compareObjects(java.lang.Object,java.lang.Object)
meth public static boolean compareObjectsImpl(java.lang.Object,java.lang.Object,int)
meth public static int arrayHashCode(java.lang.Object[])
meth public static java.awt.Component getFocusTraversableComponent(java.awt.Component)
meth public static java.awt.Cursor createCustomCursor(java.awt.Component,java.awt.Image,java.lang.String)
meth public static java.awt.Rectangle findCenterBounds(java.awt.Dimension)
meth public static java.awt.Rectangle getUsableScreenBounds()
meth public static java.awt.Rectangle getUsableScreenBounds(java.awt.GraphicsConfiguration)
meth public static java.beans.BeanInfo getBeanInfo(java.lang.Class) throws java.beans.IntrospectionException
meth public static java.beans.BeanInfo getBeanInfo(java.lang.Class,java.lang.Class) throws java.beans.IntrospectionException
meth public static java.io.File toFile(java.net.URL)
 anno 0 java.lang.Deprecated()
meth public static java.lang.Class getObjectType(java.lang.Class)
meth public static java.lang.Class getPrimitiveType(java.lang.Class)
meth public static java.lang.Object toPrimitiveArray(java.lang.Object[])
meth public static java.lang.Object[] toObjectArray(java.lang.Object)
meth public static java.lang.String escapeParameters(java.lang.String[])
meth public static java.lang.String getClassName(java.lang.Class)
meth public static java.lang.String getShortClassName(java.lang.Class)
meth public static java.lang.String keyToString(javax.swing.KeyStroke)
meth public static java.lang.String keyToString(javax.swing.KeyStroke,boolean)
meth public static java.lang.String replaceString(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String translate(java.lang.String)
meth public static java.lang.String wrapString(java.lang.String,int,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String wrapString(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.String[] parseParameters(java.lang.String)
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.ref.ReferenceQueue<java.lang.Object> activeReferenceQueue()
meth public static java.net.URL toURL(java.io.File) throws java.net.MalformedURLException
 anno 0 java.lang.Deprecated()
meth public static java.util.List partialSort(java.util.List,java.util.Comparator,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<? extends javax.swing.Action> actionsForPath(java.lang.String)
meth public static javax.swing.JPopupMenu actionsToPopup(javax.swing.Action[],java.awt.Component)
meth public static javax.swing.JPopupMenu actionsToPopup(javax.swing.Action[],org.openide.util.Lookup)
meth public static javax.swing.KeyStroke stringToKey(java.lang.String)
meth public static javax.swing.KeyStroke[] stringToKeys(java.lang.String)
meth public static org.openide.util.Lookup actionsGlobalContext()
supr java.lang.Object
hfds ALT_WILDCARD_MASK,CTRL_WILDCARD_MASK,LOG,TRANS_LOCK,TYPICAL_MACOSX_MENU_HEIGHT,clearIntrospector,doClear,global,keywords,namesAndValues,operatingSystem,transExp,transLoader
hcls NamesAndValues,RE

CLSS public static org.openide.util.Utilities$UnorderableException
 anno 0 java.lang.Deprecated()
cons public UnorderableException(java.lang.String,java.util.Collection,java.util.Map)
cons public UnorderableException(java.util.Collection,java.util.Map)
meth public java.util.Collection getUnorderable()
meth public java.util.Map getDeps()
supr java.lang.RuntimeException
hfds deps,serialVersionUID,unorderable

CLSS public final org.openide.util.WeakListeners
meth public static <%0 extends java.util.EventListener> {%%0} create(java.lang.Class<{%%0}>,java.lang.Class<? super {%%0}>,{%%0},java.lang.Object)
meth public static <%0 extends java.util.EventListener> {%%0} create(java.lang.Class<{%%0}>,{%%0},java.lang.Object)
meth public static java.beans.PropertyChangeListener propertyChange(java.beans.PropertyChangeListener,java.lang.Object)
meth public static java.beans.VetoableChangeListener vetoableChange(java.beans.VetoableChangeListener,java.lang.Object)
meth public static javax.swing.event.ChangeListener change(javax.swing.event.ChangeListener,java.lang.Object)
meth public static javax.swing.event.DocumentListener document(javax.swing.event.DocumentListener,java.lang.Object)
supr java.lang.Object

CLSS public org.openide.util.WeakSet<%0 extends java.lang.Object>
cons public WeakSet()
cons public WeakSet(int)
cons public WeakSet(int,float)
cons public WeakSet(java.util.Collection<? extends {org.openide.util.WeakSet%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({org.openide.util.WeakSet%0})
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public java.util.Iterator<{org.openide.util.WeakSet%0}> iterator()
meth public void clear()
supr java.util.AbstractSet<{org.openide.util.WeakSet%0}>
hfds entries,iterChain,loadFactor,modcount,nullCount,refq,serialVersionUID,size
hcls Entry,WeakSetIterator

CLSS public abstract interface org.openide.util.actions.ActionPerformer
 anno 0 java.lang.Deprecated()
meth public abstract void performAction(org.openide.util.actions.SystemAction)

CLSS public abstract org.openide.util.actions.BooleanStateAction
cons public BooleanStateAction()
fld public final static java.lang.String PROP_BOOLEAN_STATE = "booleanState"
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected void initialize()
meth public boolean getBooleanState()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setBooleanState(boolean)
supr org.openide.util.actions.SystemAction
hfds serialVersionUID

CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public CallableSystemAction()
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean asynchronous()
meth public abstract void performAction()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds DEFAULT_ASYNCH,serialVersionUID,warnedAsynchronousActions

CLSS public abstract org.openide.util.actions.CallbackSystemAction
cons public CallbackSystemAction()
intf org.openide.util.ContextAwareAction
meth protected void initialize()
meth public boolean getSurviveFocusChange()
meth public java.lang.Object getActionMapKey()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.actions.ActionPerformer getActionPerformer()
 anno 0 java.lang.Deprecated()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setActionPerformer(org.openide.util.actions.ActionPerformer)
 anno 0 java.lang.Deprecated()
meth public void setSurviveFocusChange(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds LISTENER,PROP_ACTION_PERFORMER,err,notSurviving,serialVersionUID,surviving
hcls ActionDelegateListener,DelegateAction,GlobalManager,WeakAction

CLSS public abstract interface org.openide.util.actions.Presenter
innr public abstract interface static Menu
innr public abstract interface static Popup
innr public abstract interface static Toolbar

CLSS public abstract interface static org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getMenuPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getPopupPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component getToolbarPresenter()

CLSS public abstract org.openide.util.actions.SystemAction
cons public SystemAction()
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_ICON = "icon"
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public abstract java.lang.String getName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void actionPerformed(java.awt.event.ActionEvent)
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final javax.swing.Icon getIcon()
meth public final javax.swing.Icon getIcon(boolean)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public final void setIcon(javax.swing.Icon)
meth public static <%0 extends org.openide.util.actions.SystemAction> {%%0} get(java.lang.Class<{%%0}>)
meth public static javax.swing.JPopupMenu createPopupMenu(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public static javax.swing.JToolBar createToolbarPresenter(org.openide.util.actions.SystemAction[])
meth public static org.openide.util.actions.SystemAction[] linkActions(org.openide.util.actions.SystemAction[],org.openide.util.actions.SystemAction[])
meth public void setEnabled(boolean)
supr org.openide.util.SharedClassObject
hfds BLANK_ICON,LOG,PROP_ICON_TEXTUAL,relativeIconResourceClasses,serialVersionUID
hcls ComponentIcon

CLSS public final org.openide.util.datatransfer.ClipboardEvent
meth public boolean isConsumed()
meth public org.openide.util.datatransfer.ExClipboard getClipboard()
meth public void consume()
supr java.util.EventObject
hfds consumed,serialVersionUID

CLSS public abstract interface org.openide.util.datatransfer.ClipboardListener
intf java.util.EventListener
meth public abstract void clipboardChanged(org.openide.util.datatransfer.ClipboardEvent)

CLSS public abstract org.openide.util.datatransfer.ExClipboard
cons public ExClipboard(java.lang.String)
innr public abstract interface static Convertor
meth protected abstract org.openide.util.datatransfer.ExClipboard$Convertor[] getConvertors()
meth protected final void fireClipboardChange()
meth public final void addClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public final void removeClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public java.awt.datatransfer.Transferable convert(java.awt.datatransfer.Transferable)
meth public static void transferableAccepted(java.awt.datatransfer.Transferable,int)
meth public static void transferableOwnershipLost(java.awt.datatransfer.Transferable)
meth public static void transferableRejected(java.awt.datatransfer.Transferable)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.awt.datatransfer.Clipboard
hfds listeners

CLSS public abstract interface static org.openide.util.datatransfer.ExClipboard$Convertor
meth public abstract java.awt.datatransfer.Transferable convert(java.awt.datatransfer.Transferable)

CLSS public org.openide.util.datatransfer.ExTransferable
fld public final static java.awt.datatransfer.DataFlavor multiFlavor
fld public final static java.awt.datatransfer.Transferable EMPTY
innr public abstract static Single
innr public static Multi
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void addTransferListener(org.openide.util.datatransfer.TransferListener)
meth public final void removeTransferListener(org.openide.util.datatransfer.TransferListener)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public static org.openide.util.datatransfer.ExTransferable create(java.awt.datatransfer.Transferable)
meth public void put(org.openide.util.datatransfer.ExTransferable$Single)
meth public void remove(java.awt.datatransfer.DataFlavor)
supr java.lang.Object
hfds listeners,map
hcls Empty

CLSS public static org.openide.util.datatransfer.ExTransferable$Multi
cons public Multi(java.awt.datatransfer.Transferable[])
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds flavorList,transferObject
hcls TransferObjectImpl

CLSS public abstract static org.openide.util.datatransfer.ExTransferable$Single
cons public Single(java.awt.datatransfer.DataFlavor)
intf java.awt.datatransfer.Transferable
meth protected abstract java.lang.Object getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds flavor

CLSS public abstract interface org.openide.util.datatransfer.MultiTransferObject
meth public abstract boolean areDataFlavorsSupported(java.awt.datatransfer.DataFlavor[])
meth public abstract boolean isDataFlavorSupported(int,java.awt.datatransfer.DataFlavor)
meth public abstract int getCount()
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors(int)
meth public abstract java.awt.datatransfer.Transferable getTransferableAt(int)
meth public abstract java.lang.Object getTransferData(int,java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public abstract org.openide.util.datatransfer.NewType
cons public NewType()
intf org.openide.util.HelpCtx$Provider
meth public abstract void create() throws java.io.IOException
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object

CLSS public abstract org.openide.util.datatransfer.PasteType
cons public PasteType()
intf org.openide.util.HelpCtx$Provider
meth public abstract java.awt.datatransfer.Transferable paste() throws java.io.IOException
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object

CLSS public abstract interface org.openide.util.datatransfer.TransferListener
intf java.util.EventListener
meth public abstract void accepted(int)
meth public abstract void ownershipLost()
meth public abstract void rejected()

CLSS public org.openide.util.io.FoldingIOException
cons public FoldingIOException(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String toString()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.io.IOException
hfds serialVersionUID,t

CLSS public final org.openide.util.io.NbMarshalledObject
cons public NbMarshalledObject(java.lang.Object) throws java.io.IOException
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object get() throws java.io.IOException,java.lang.ClassNotFoundException
supr java.lang.Object
hfds HEX,hash,objBytes,serialVersionUID

CLSS public org.openide.util.io.NbObjectInputStream
cons public NbObjectInputStream(java.io.InputStream) throws java.io.IOException
meth protected java.io.ObjectStreamClass readClassDescriptor() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class resolveClass(java.io.ObjectStreamClass) throws java.io.IOException,java.lang.ClassNotFoundException
meth public static java.lang.Object readSafely(java.io.ObjectInput) throws java.io.IOException
meth public static void skipSafely(java.io.ObjectInput) throws java.io.IOException
supr java.io.ObjectInputStream

CLSS public org.openide.util.io.NbObjectOutputStream
cons public NbObjectOutputStream(java.io.OutputStream) throws java.io.IOException
meth protected void annotateClass(java.lang.Class) throws java.io.IOException
meth public java.lang.Object replaceObject(java.lang.Object) throws java.io.IOException
meth public static void writeSafely(java.io.ObjectOutput,java.lang.Object) throws java.io.IOException
supr java.io.ObjectOutputStream
hfds SVUID,alreadyReported,examinedClasses,serializing

CLSS public org.openide.util.io.NullInputStream
cons public NullInputStream()
fld public boolean throwException
meth public int read() throws java.io.IOException
supr java.io.InputStream

CLSS public org.openide.util.io.NullOutputStream
cons public NullOutputStream()
fld public boolean throwException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public org.openide.util.io.OperationException
cons public OperationException(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
supr java.io.IOException
hfds ex,serialVersionUID

CLSS public org.openide.util.io.ReaderInputStream
cons public ReaderInputStream(java.io.Reader) throws java.io.IOException
cons public ReaderInputStream(java.io.Reader,java.lang.String) throws java.io.IOException
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.io.InputStream
hfds osw,pis,pos,reader

CLSS public org.openide.util.io.SafeException
cons public SafeException(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.Throwable getCause()
supr org.openide.util.io.FoldingIOException
hfds ex,serialVersionUID

CLSS public org.openide.util.lookup.AbstractLookup
cons protected AbstractLookup()
cons public AbstractLookup(org.openide.util.lookup.AbstractLookup$Content)
innr public abstract static Pair
innr public static Content
intf java.io.Serializable
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>,java.util.concurrent.Executor)
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth protected void initialize()
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds count,serialVersionUID,tree,treeLock
hcls CycleError,ISE,Info,NotifyListeners,R,ReferenceIterator,ReferenceToResult,Storage

CLSS public static org.openide.util.lookup.AbstractLookup$Content
cons public Content()
cons public Content(java.util.concurrent.Executor)
intf java.io.Serializable
meth public final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth public final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth public final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>)
supr java.lang.Object
hfds al,notifyIn,serialVersionUID

CLSS public abstract static org.openide.util.lookup.AbstractLookup$Pair<%0 extends java.lang.Object>
cons protected Pair()
intf java.io.Serializable
meth protected abstract boolean creatorOf(java.lang.Object)
meth protected abstract boolean instanceOf(java.lang.Class<?>)
supr org.openide.util.Lookup$Item<{org.openide.util.lookup.AbstractLookup$Pair%0}>
hfds index,serialVersionUID

CLSS public final org.openide.util.lookup.InstanceContent
cons public InstanceContent()
cons public InstanceContent(java.util.concurrent.Executor)
innr public abstract interface static Convertor
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void add({%%0},org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void remove({%%0},org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void set(java.util.Collection<{%%0}>,org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public final void add(java.lang.Object)
meth public final void remove(java.lang.Object)
supr org.openide.util.lookup.AbstractLookup$Content
hcls ConvertingItem,SimpleItem

CLSS public abstract interface static org.openide.util.lookup.InstanceContent$Convertor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract java.lang.Class<? extends {org.openide.util.lookup.InstanceContent$Convertor%1}> type({org.openide.util.lookup.InstanceContent$Convertor%0})
meth public abstract java.lang.String displayName({org.openide.util.lookup.InstanceContent$Convertor%0})
meth public abstract java.lang.String id({org.openide.util.lookup.InstanceContent$Convertor%0})
meth public abstract {org.openide.util.lookup.InstanceContent$Convertor%1} convert({org.openide.util.lookup.InstanceContent$Convertor%0})

CLSS public org.openide.util.lookup.Lookups
meth public !varargs static org.openide.util.Lookup exclude(org.openide.util.Lookup,java.lang.Class[])
meth public !varargs static org.openide.util.Lookup fixed(java.lang.Object[])
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Lookup fixed({%%0}[],org.openide.util.lookup.InstanceContent$Convertor<? super {%%0},{%%1}>)
meth public static <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem({%%0},java.lang.String)
meth public static org.openide.util.Lookup forPath(java.lang.String)
meth public static org.openide.util.Lookup metaInfServices(java.lang.ClassLoader)
meth public static org.openide.util.Lookup metaInfServices(java.lang.ClassLoader,java.lang.String)
meth public static org.openide.util.Lookup proxy(org.openide.util.Lookup$Provider)
meth public static org.openide.util.Lookup singleton(java.lang.Object)
supr java.lang.Object
hcls LookupItem

CLSS public org.openide.util.lookup.ProxyLookup
cons protected ProxyLookup()
cons public !varargs ProxyLookup(org.openide.util.Lookup[])
meth protected !varargs final void setLookups(java.util.concurrent.Executor,org.openide.util.Lookup[])
meth protected !varargs final void setLookups(org.openide.util.Lookup[])
meth protected final org.openide.util.Lookup[] getLookups()
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds data
hcls EmptyInternalData,ImmutableInternalData,R,RealInternalData,SingleInternalData,WeakRef,WeakResult

CLSS public abstract interface !annotation org.openide.util.lookup.ServiceProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String path()
meth public abstract !hasdefault java.lang.String[] supersedes()
meth public abstract java.lang.Class<?> service()

CLSS public abstract interface !annotation org.openide.util.lookup.ServiceProviders
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract org.openide.util.lookup.ServiceProvider[] value()

CLSS public abstract org.openide.xml.EntityCatalog
cons public EntityCatalog()
fld public final static java.lang.String PUBLIC_ID = "-//NetBeans//Entity Mapping Registration 1.0//EN"
intf org.xml.sax.EntityResolver
meth public static org.openide.xml.EntityCatalog getDefault()
supr java.lang.Object
hfds instance
hcls Forwarder

CLSS public final org.openide.xml.XMLUtil
meth public static byte[] fromHex(char[],int,int) throws java.io.IOException
meth public static java.lang.String toAttributeValue(java.lang.String) throws java.io.CharConversionException
meth public static java.lang.String toElementContent(java.lang.String) throws java.io.CharConversionException
meth public static java.lang.String toHex(byte[],int,int)
meth public static org.w3c.dom.Document createDocument(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Document parse(org.xml.sax.InputSource,boolean,boolean,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader createXMLReader() throws org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader createXMLReader(boolean) throws org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader createXMLReader(boolean,boolean) throws org.xml.sax.SAXException
meth public static void validate(org.w3c.dom.Element,javax.xml.validation.Schema) throws org.xml.sax.SAXException
meth public static void write(org.w3c.dom.Document,java.io.OutputStream,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds DEC2HEX,IDENTITY_XSLT_WITH_INDENT,doms,saxes

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

