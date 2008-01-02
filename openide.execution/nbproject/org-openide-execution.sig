#API master signature file
#Version 1.10.1
CLSS public static final org.openide.ServiceType$Handle
cons public Handle(org.openide.ServiceType)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.ServiceType$Handle.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.ServiceType org.openide.ServiceType$Handle.getServiceType()
supr java.lang.Object
CLSS public static abstract org.openide.ServiceType$Registry
cons public Registry()
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.util.Enumeration org.openide.ServiceType$Registry.services()
meth public abstract java.util.List org.openide.ServiceType$Registry.getServiceTypes()
meth public abstract void org.openide.ServiceType$Registry.setServiceTypes(java.util.List)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Enumeration org.openide.ServiceType$Registry.services(java.lang.Class)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.ServiceType org.openide.ServiceType$Registry.find(java.lang.Class)
meth public org.openide.ServiceType org.openide.ServiceType$Registry.find(java.lang.String)
supr java.lang.Object
CLSS public static org.openide.execution.ScriptType$Context
cons public Context()
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
supr java.lang.Object
CLSS public static abstract org.openide.filesystems.FileSystem$Environment
cons public Environment()
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
meth public void org.openide.filesystems.FileSystem$Environment.addClassPath(java.lang.String)
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Enumerations$Processor
meth public abstract java.lang.Object org.openide.util.Enumerations$Processor.process(java.lang.Object,java.util.Collection)
supr null
CLSS public static abstract interface org.openide.util.HelpCtx$Provider
meth public abstract org.openide.util.HelpCtx org.openide.util.HelpCtx$Provider.getHelpCtx()
supr null
CLSS public abstract org.openide.execution.ExecutionEngine
cons public ExecutionEngine()
meth protected abstract java.security.PermissionCollection org.openide.execution.ExecutionEngine.createPermissions(java.security.CodeSource,org.openide.windows.InputOutput)
meth protected abstract org.openide.execution.NbClassPath org.openide.execution.ExecutionEngine.createLibraryPath()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.openide.execution.ExecutorTask org.openide.execution.ExecutionEngine.execute(java.lang.String,java.lang.Runnable,org.openide.windows.InputOutput)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.execution.ExecutionEngine org.openide.execution.ExecutionEngine.getDefault()
supr java.lang.Object
CLSS public abstract org.openide.execution.ExecutorTask
cons protected ExecutorTask(java.lang.Runnable)
fld  public static final org.openide.util.Task org.openide.util.Task.EMPTY
intf java.lang.Runnable
meth protected final void org.openide.util.Task.notifyFinished()
meth protected final void org.openide.util.Task.notifyRunning()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract int org.openide.execution.ExecutorTask.result()
meth public abstract org.openide.windows.InputOutput org.openide.execution.ExecutorTask.getInputOutput()
meth public abstract void org.openide.execution.ExecutorTask.stop()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.Task.waitFinished(long) throws java.lang.InterruptedException
meth public final boolean org.openide.util.Task.isFinished()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.Task.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized void org.openide.util.Task.addTaskListener(org.openide.util.TaskListener)
meth public synchronized void org.openide.util.Task.removeTaskListener(org.openide.util.TaskListener)
meth public void org.openide.util.Task.run()
meth public void org.openide.util.Task.waitFinished()
supr org.openide.util.Task
CLSS public org.openide.execution.NbClassLoader
cons public NbClassLoader()
cons public NbClassLoader([Lorg.openide.filesystems.FileObject;,java.lang.ClassLoader,org.openide.windows.InputOutput) throws org.openide.filesystems.FileStateInvalidException
cons public NbClassLoader([Lorg.openide.filesystems.FileSystem;)
cons public NbClassLoader([Lorg.openide.filesystems.FileSystem;,java.lang.ClassLoader)
cons public NbClassLoader(org.openide.windows.InputOutput)
fld  protected org.openide.windows.InputOutput org.openide.execution.NbClassLoader.inout
meth protected [Ljava.lang.Package; java.lang.ClassLoader.getPackages()
meth protected final java.lang.Class java.lang.ClassLoader.defineClass([B,int,int) throws java.lang.ClassFormatError
meth protected final java.lang.Class java.lang.ClassLoader.defineClass(java.lang.String,[B,int,int) throws java.lang.ClassFormatError
meth protected final java.lang.Class java.lang.ClassLoader.defineClass(java.lang.String,[B,int,int,java.security.ProtectionDomain) throws java.lang.ClassFormatError
meth protected final java.lang.Class java.lang.ClassLoader.defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain) throws java.lang.ClassFormatError
meth protected final java.lang.Class java.lang.ClassLoader.findLoadedClass(java.lang.String)
meth protected final java.lang.Class java.lang.ClassLoader.findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final java.lang.Class java.security.SecureClassLoader.defineClass(java.lang.String,[B,int,int,java.security.CodeSource)
meth protected final java.lang.Class java.security.SecureClassLoader.defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected final void java.lang.ClassLoader.resolveClass(java.lang.Class)
meth protected final void java.lang.ClassLoader.setSigners(java.lang.Class,[Ljava.lang.Object;)
meth protected java.lang.Class org.openide.execution.NbClassLoader.findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Package java.lang.ClassLoader.definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL) throws java.lang.IllegalArgumentException
meth protected java.lang.Package java.lang.ClassLoader.getPackage(java.lang.String)
meth protected java.lang.Package java.net.URLClassLoader.definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL) throws java.lang.IllegalArgumentException
meth protected java.lang.String java.lang.ClassLoader.findLibrary(java.lang.String)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected synchronized final java.security.PermissionCollection org.openide.execution.NbClassLoader.getPermissions(java.security.CodeSource)
meth protected synchronized java.lang.Class java.lang.ClassLoader.loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void java.net.URLClassLoader.addURL(java.net.URL)
meth public [Ljava.net.URL; java.net.URLClassLoader.getURLs()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.lang.ClassLoader java.lang.ClassLoader.getParent()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.InputStream java.lang.ClassLoader.getResourceAsStream(java.lang.String)
meth public java.lang.Class java.lang.ClassLoader.loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.String java.lang.Object.toString()
meth public java.net.URL java.net.URLClassLoader.findResource(java.lang.String)
meth public java.net.URL org.openide.execution.NbClassLoader.getResource(java.lang.String)
meth public java.util.Enumeration java.lang.ClassLoader.getResources(java.lang.String) throws java.io.IOException
meth public java.util.Enumeration java.net.URLClassLoader.findResources(java.lang.String) throws java.io.IOException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static java.io.InputStream java.lang.ClassLoader.getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader java.lang.ClassLoader.getSystemClassLoader()
meth public static java.net.URL java.lang.ClassLoader.getSystemResource(java.lang.String)
meth public static java.net.URLClassLoader java.net.URLClassLoader.newInstance([Ljava.net.URL;)
meth public static java.net.URLClassLoader java.net.URLClassLoader.newInstance([Ljava.net.URL;,java.lang.ClassLoader)
meth public static java.util.Enumeration java.lang.ClassLoader.getSystemResources(java.lang.String) throws java.io.IOException
meth public synchronized void java.lang.ClassLoader.clearAssertionStatus()
meth public synchronized void java.lang.ClassLoader.setClassAssertionStatus(java.lang.String,boolean)
meth public synchronized void java.lang.ClassLoader.setDefaultAssertionStatus(boolean)
meth public synchronized void java.lang.ClassLoader.setPackageAssertionStatus(java.lang.String,boolean)
meth public void org.openide.execution.NbClassLoader.setDefaultPermissions(java.security.PermissionCollection)
supr java.net.URLClassLoader
CLSS public final org.openide.execution.NbClassPath
cons public NbClassPath([Ljava.io.File;)
cons public NbClassPath([Ljava.lang.String;)
cons public NbClassPath(java.lang.String)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.Exception; org.openide.execution.NbClassPath.getExceptions()
meth public boolean org.openide.execution.NbClassPath.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.execution.NbClassPath.getClassPath()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static java.io.File org.openide.execution.NbClassPath.toFile(org.openide.filesystems.FileObject)
meth public static org.openide.execution.NbClassPath org.openide.execution.NbClassPath.createBootClassPath()
meth public static org.openide.execution.NbClassPath org.openide.execution.NbClassPath.createClassPath()
meth public static org.openide.execution.NbClassPath org.openide.execution.NbClassPath.createLibraryPath()
meth public static org.openide.execution.NbClassPath org.openide.execution.NbClassPath.createRepositoryPath()
meth public static org.openide.execution.NbClassPath org.openide.execution.NbClassPath.createRepositoryPath(org.openide.filesystems.FileSystemCapability)
supr java.lang.Object
CLSS public final org.openide.execution.NbProcessDescriptor
cons public NbProcessDescriptor(java.lang.String,java.lang.String)
cons public NbProcessDescriptor(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.execution.NbProcessDescriptor.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.execution.NbProcessDescriptor.hashCode()
meth public java.lang.Process org.openide.execution.NbProcessDescriptor.exec() throws java.io.IOException
meth public java.lang.Process org.openide.execution.NbProcessDescriptor.exec(java.text.Format) throws java.io.IOException
meth public java.lang.Process org.openide.execution.NbProcessDescriptor.exec(java.text.Format,[Ljava.lang.String;) throws java.io.IOException
meth public java.lang.Process org.openide.execution.NbProcessDescriptor.exec(java.text.Format,[Ljava.lang.String;,boolean,java.io.File) throws java.io.IOException
meth public java.lang.Process org.openide.execution.NbProcessDescriptor.exec(java.text.Format,[Ljava.lang.String;,java.io.File) throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.execution.NbProcessDescriptor.getArguments()
meth public java.lang.String org.openide.execution.NbProcessDescriptor.getInfo()
meth public java.lang.String org.openide.execution.NbProcessDescriptor.getProcessName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public abstract org.openide.execution.ScriptType
cons public ScriptType()
fld  constant public static final java.lang.String org.openide.ServiceType.PROP_NAME
innr public static abstract org.openide.ServiceType$Registry
innr public static final org.openide.ServiceType$Handle
innr public static org.openide.execution.ScriptType$Context
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void org.openide.ServiceType.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object org.openide.ServiceType.clone() throws java.lang.CloneNotSupportedException
meth protected java.lang.String org.openide.ServiceType.displayName()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.openide.execution.ScriptType.acceptFileObject(org.openide.filesystems.FileObject)
meth public abstract java.lang.Object org.openide.execution.ScriptType.eval(java.io.Reader,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public abstract java.lang.Object org.openide.execution.ScriptType.eval(java.lang.String,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public abstract org.openide.util.HelpCtx org.openide.ServiceType.getHelpCtx()
meth public abstract void org.openide.execution.ScriptType.addVariable(java.lang.String,java.lang.Object)
meth public abstract void org.openide.execution.ScriptType.exec(java.io.Reader,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public abstract void org.openide.execution.ScriptType.exec(java.lang.String,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.lang.Object org.openide.execution.ScriptType.eval(java.io.Reader) throws java.lang.reflect.InvocationTargetException
meth public final java.lang.Object org.openide.execution.ScriptType.eval(java.lang.String) throws java.lang.reflect.InvocationTargetException
meth public final org.openide.ServiceType org.openide.ServiceType.createClone()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.ServiceType.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.execution.ScriptType.exec(java.io.Reader) throws java.lang.reflect.InvocationTargetException
meth public final void org.openide.execution.ScriptType.exec(java.lang.String) throws java.lang.reflect.InvocationTargetException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.ServiceType.getName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static java.util.Enumeration org.openide.execution.ScriptType.scriptTypes()
meth public static org.openide.execution.ScriptType org.openide.execution.ScriptType.find(java.lang.Class)
meth public static org.openide.execution.ScriptType org.openide.execution.ScriptType.find(java.lang.String)
meth public static org.openide.execution.ScriptType org.openide.execution.ScriptType.getDefault()
meth public synchronized final void org.openide.ServiceType.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.ServiceType.setName(java.lang.String)
supr org.openide.ServiceType
