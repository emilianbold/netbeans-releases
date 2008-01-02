#API master signature file
#Version 7.3.1
CLSS public final org.openide.modules.Dependency
fld  constant public static final int org.openide.modules.Dependency.COMPARE_ANY
fld  constant public static final int org.openide.modules.Dependency.COMPARE_IMPL
fld  constant public static final int org.openide.modules.Dependency.COMPARE_SPEC
fld  constant public static final int org.openide.modules.Dependency.TYPE_IDE
fld  constant public static final int org.openide.modules.Dependency.TYPE_JAVA
fld  constant public static final int org.openide.modules.Dependency.TYPE_MODULE
fld  constant public static final int org.openide.modules.Dependency.TYPE_NEEDS
fld  constant public static final int org.openide.modules.Dependency.TYPE_PACKAGE
fld  constant public static final int org.openide.modules.Dependency.TYPE_RECOMMENDS
fld  constant public static final int org.openide.modules.Dependency.TYPE_REQUIRES
fld  constant public static final java.lang.String org.openide.modules.Dependency.JAVA_NAME
fld  constant public static final java.lang.String org.openide.modules.Dependency.VM_NAME
fld  public static final java.lang.String org.openide.modules.Dependency.IDE_IMPL
fld  public static final java.lang.String org.openide.modules.Dependency.IDE_NAME
fld  public static final java.lang.String org.openide.modules.Dependency.JAVA_IMPL
fld  public static final java.lang.String org.openide.modules.Dependency.VM_IMPL
fld  public static final org.openide.modules.SpecificationVersion org.openide.modules.Dependency.IDE_SPEC
fld  public static final org.openide.modules.SpecificationVersion org.openide.modules.Dependency.JAVA_SPEC
fld  public static final org.openide.modules.SpecificationVersion org.openide.modules.Dependency.VM_SPEC
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.modules.Dependency.equals(java.lang.Object)
meth public final int org.openide.modules.Dependency.getComparison()
meth public final int org.openide.modules.Dependency.getType()
meth public final java.lang.String org.openide.modules.Dependency.getName()
meth public final java.lang.String org.openide.modules.Dependency.getVersion()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.modules.Dependency.hashCode()
meth public java.lang.String org.openide.modules.Dependency.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static java.util.Set org.openide.modules.Dependency.create(int,java.lang.String) throws java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public abstract org.openide.modules.InstalledFileLocator
cons protected InstalledFileLocator()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.io.File org.openide.modules.InstalledFileLocator.locate(java.lang.String,java.lang.String,boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.modules.InstalledFileLocator org.openide.modules.InstalledFileLocator.getDefault()
supr java.lang.Object
CLSS public abstract org.openide.modules.ModuleInfo
cons protected ModuleInfo()
fld  constant public static final java.lang.String org.openide.modules.ModuleInfo.PROP_ENABLED
meth protected final void org.openide.modules.ModuleInfo.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.openide.modules.ModuleInfo.getProvides()
meth public abstract boolean org.openide.modules.ModuleInfo.isEnabled()
meth public abstract boolean org.openide.modules.ModuleInfo.owns(java.lang.Class)
meth public abstract int org.openide.modules.ModuleInfo.getCodeNameRelease()
meth public abstract java.lang.Object org.openide.modules.ModuleInfo.getAttribute(java.lang.String)
meth public abstract java.lang.Object org.openide.modules.ModuleInfo.getLocalizedAttribute(java.lang.String)
meth public abstract java.lang.String org.openide.modules.ModuleInfo.getCodeName()
meth public abstract java.lang.String org.openide.modules.ModuleInfo.getCodeNameBase()
meth public abstract java.util.Set org.openide.modules.ModuleInfo.getDependencies()
meth public abstract org.openide.modules.SpecificationVersion org.openide.modules.ModuleInfo.getSpecificationVersion()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.modules.ModuleInfo.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.modules.ModuleInfo.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.ClassLoader org.openide.modules.ModuleInfo.getClassLoader() throws java.lang.IllegalArgumentException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.modules.ModuleInfo.getBuildVersion()
meth public java.lang.String org.openide.modules.ModuleInfo.getDisplayName()
meth public java.lang.String org.openide.modules.ModuleInfo.getImplementationVersion()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public org.openide.modules.ModuleInstall
cons public ModuleInstall()
intf java.io.Externalizable
intf java.io.Serializable
meth protected boolean org.openide.modules.ModuleInstall.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.initialize()
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public boolean org.openide.modules.ModuleInstall.closing()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public void org.openide.modules.ModuleInstall.close()
meth public void org.openide.modules.ModuleInstall.installed()
meth public void org.openide.modules.ModuleInstall.restored()
meth public void org.openide.modules.ModuleInstall.uninstalled()
meth public void org.openide.modules.ModuleInstall.updated(int,java.lang.String)
meth public void org.openide.modules.ModuleInstall.validate() throws java.lang.IllegalStateException
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr org.openide.util.SharedClassObject
CLSS public final org.openide.modules.SpecificationVersion
cons public SpecificationVersion(java.lang.String) throws java.lang.NumberFormatException
intf java.lang.Comparable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.modules.SpecificationVersion.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.modules.SpecificationVersion.compareTo(java.lang.Object)
meth public int org.openide.modules.SpecificationVersion.hashCode()
meth public java.lang.String org.openide.modules.SpecificationVersion.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
