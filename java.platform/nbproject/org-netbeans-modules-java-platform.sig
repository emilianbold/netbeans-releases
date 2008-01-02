#API master signature file
#Version 1.9.1
CLSS public abstract org.netbeans.api.java.platform.JavaPlatform
cons protected JavaPlatform()
fld  constant public static final java.lang.String org.netbeans.api.java.platform.JavaPlatform.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.netbeans.api.java.platform.JavaPlatform.PROP_JAVADOC_FOLDER
fld  constant public static final java.lang.String org.netbeans.api.java.platform.JavaPlatform.PROP_SOURCE_FOLDER
fld  constant public static final java.lang.String org.netbeans.api.java.platform.JavaPlatform.PROP_SYSTEM_PROPERTIES
meth protected final void org.netbeans.api.java.platform.JavaPlatform.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.netbeans.api.java.platform.JavaPlatform.setSystemProperties(java.util.Map)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.netbeans.api.java.platform.JavaPlatform.getDisplayName()
meth public abstract java.lang.String org.netbeans.api.java.platform.JavaPlatform.getVendor()
meth public abstract java.util.Collection org.netbeans.api.java.platform.JavaPlatform.getInstallFolders()
meth public abstract java.util.List org.netbeans.api.java.platform.JavaPlatform.getJavadocFolders()
meth public abstract java.util.Map org.netbeans.api.java.platform.JavaPlatform.getProperties()
meth public abstract org.netbeans.api.java.classpath.ClassPath org.netbeans.api.java.platform.JavaPlatform.getBootstrapLibraries()
meth public abstract org.netbeans.api.java.classpath.ClassPath org.netbeans.api.java.platform.JavaPlatform.getSourceFolders()
meth public abstract org.netbeans.api.java.classpath.ClassPath org.netbeans.api.java.platform.JavaPlatform.getStandardLibraries()
meth public abstract org.netbeans.api.java.platform.Specification org.netbeans.api.java.platform.JavaPlatform.getSpecification()
meth public abstract org.openide.filesystems.FileObject org.netbeans.api.java.platform.JavaPlatform.findTool(java.lang.String)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.util.Map org.netbeans.api.java.platform.JavaPlatform.getSystemProperties()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.netbeans.api.java.platform.JavaPlatform.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.netbeans.api.java.platform.JavaPlatform.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.netbeans.api.java.platform.JavaPlatform org.netbeans.api.java.platform.JavaPlatform.getDefault()
supr java.lang.Object
CLSS public final org.netbeans.api.java.platform.JavaPlatformManager
cons public JavaPlatformManager()
fld  constant public static final java.lang.String org.netbeans.api.java.platform.JavaPlatformManager.PROP_INSTALLED_PLATFORMS
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.netbeans.api.java.platform.JavaPlatform; org.netbeans.api.java.platform.JavaPlatformManager.getPlatforms(java.lang.String,org.netbeans.api.java.platform.Specification)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.java.platform.JavaPlatform org.netbeans.api.java.platform.JavaPlatformManager.getDefaultPlatform()
meth public static synchronized org.netbeans.api.java.platform.JavaPlatformManager org.netbeans.api.java.platform.JavaPlatformManager.getDefault()
meth public synchronized [Lorg.netbeans.api.java.platform.JavaPlatform; org.netbeans.api.java.platform.JavaPlatformManager.getInstalledPlatforms()
meth public void org.netbeans.api.java.platform.JavaPlatformManager.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.api.java.platform.JavaPlatformManager.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
CLSS public final org.netbeans.api.java.platform.PlatformsCustomizer
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
meth public static boolean org.netbeans.api.java.platform.PlatformsCustomizer.showCustomizer(org.netbeans.api.java.platform.JavaPlatform)
supr java.lang.Object
CLSS public org.netbeans.api.java.platform.Profile
cons public Profile(java.lang.String,org.openide.modules.SpecificationVersion)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.api.java.platform.Profile.equals(java.lang.Object)
meth public final java.lang.String org.netbeans.api.java.platform.Profile.getName()
meth public final org.openide.modules.SpecificationVersion org.netbeans.api.java.platform.Profile.getVersion()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.java.platform.Profile.hashCode()
meth public java.lang.String org.netbeans.api.java.platform.Profile.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public final org.netbeans.api.java.platform.Specification
cons public Specification(java.lang.String,org.openide.modules.SpecificationVersion)
cons public Specification(java.lang.String,org.openide.modules.SpecificationVersion,[Lorg.netbeans.api.java.platform.Profile;)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.api.java.platform.Specification.equals(java.lang.Object)
meth public final [Lorg.netbeans.api.java.platform.Profile; org.netbeans.api.java.platform.Specification.getProfiles()
meth public final java.lang.String org.netbeans.api.java.platform.Specification.getName()
meth public final org.openide.modules.SpecificationVersion org.netbeans.api.java.platform.Specification.getVersion()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.java.platform.Specification.hashCode()
meth public java.lang.String org.netbeans.api.java.platform.Specification.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public abstract org.netbeans.spi.java.platform.CustomPlatformInstall
cons public CustomPlatformInstall()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.netbeans.spi.java.platform.GeneralPlatformInstall.getDisplayName()
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator org.netbeans.spi.java.platform.CustomPlatformInstall.createIterator()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.java.platform.GeneralPlatformInstall
CLSS public abstract org.netbeans.spi.java.platform.GeneralPlatformInstall
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.netbeans.spi.java.platform.GeneralPlatformInstall.getDisplayName()
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
CLSS public abstract org.netbeans.spi.java.platform.PlatformInstall
cons public PlatformInstall()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.spi.java.platform.PlatformInstall.accept(org.openide.filesystems.FileObject)
meth public abstract java.lang.String org.netbeans.spi.java.platform.GeneralPlatformInstall.getDisplayName()
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator org.netbeans.spi.java.platform.PlatformInstall.createIterator(org.openide.filesystems.FileObject)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.java.platform.GeneralPlatformInstall
