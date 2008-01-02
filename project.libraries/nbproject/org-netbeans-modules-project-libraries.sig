#API master signature file
#Version 1.15.1
CLSS public static abstract interface org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
supr null
CLSS public final org.netbeans.api.project.libraries.LibrariesCustomizer
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
meth public static boolean org.netbeans.api.project.libraries.LibrariesCustomizer.showCustomizer(org.netbeans.api.project.libraries.Library)
supr java.lang.Object
CLSS public final org.netbeans.api.project.libraries.Library
fld  constant public static final java.lang.String org.netbeans.api.project.libraries.Library.PROP_CONTENT
fld  constant public static final java.lang.String org.netbeans.api.project.libraries.Library.PROP_DESCRIPTION
fld  constant public static final java.lang.String org.netbeans.api.project.libraries.Library.PROP_NAME
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.api.project.libraries.Library.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.project.libraries.Library.hashCode()
meth public java.lang.String org.netbeans.api.project.libraries.Library.getDescription()
meth public java.lang.String org.netbeans.api.project.libraries.Library.getDisplayName()
meth public java.lang.String org.netbeans.api.project.libraries.Library.getName()
meth public java.lang.String org.netbeans.api.project.libraries.Library.getType()
meth public java.lang.String org.netbeans.api.project.libraries.Library.toString()
meth public java.util.List org.netbeans.api.project.libraries.Library.getContent(java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public synchronized void org.netbeans.api.project.libraries.Library.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void org.netbeans.api.project.libraries.Library.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
CLSS public final org.netbeans.api.project.libraries.LibraryManager
fld  constant public static final java.lang.String org.netbeans.api.project.libraries.LibraryManager.PROP_LIBRARIES
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
meth public org.netbeans.api.project.libraries.Library org.netbeans.api.project.libraries.LibraryManager.getLibrary(java.lang.String)
meth public static synchronized org.netbeans.api.project.libraries.LibraryManager org.netbeans.api.project.libraries.LibraryManager.getDefault()
meth public synchronized [Lorg.netbeans.api.project.libraries.Library; org.netbeans.api.project.libraries.LibraryManager.getLibraries()
meth public synchronized void org.netbeans.api.project.libraries.LibraryManager.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.api.project.libraries.LibraryManager.addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException,java.lang.IllegalArgumentException
meth public void org.netbeans.api.project.libraries.LibraryManager.removeLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException,java.lang.IllegalArgumentException
meth public void org.netbeans.api.project.libraries.LibraryManager.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
CLSS public org.netbeans.spi.project.libraries.LibraryFactory
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
meth public static org.netbeans.api.project.libraries.Library org.netbeans.spi.project.libraries.LibraryFactory.createLibrary(org.netbeans.spi.project.libraries.LibraryImplementation)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryImplementation
fld  constant public static final java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.PROP_CONTENT
fld  constant public static final java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.PROP_DESCRIPTION
fld  constant public static final java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.PROP_NAME
meth public abstract java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.getDescription()
meth public abstract java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.getLocalizingBundle()
meth public abstract java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.getName()
meth public abstract java.lang.String org.netbeans.spi.project.libraries.LibraryImplementation.getType()
meth public abstract java.util.List org.netbeans.spi.project.libraries.LibraryImplementation.getContent(java.lang.String) throws java.lang.IllegalArgumentException
meth public abstract void org.netbeans.spi.project.libraries.LibraryImplementation.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.libraries.LibraryImplementation.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.libraries.LibraryImplementation.setContent(java.lang.String,java.util.List) throws java.lang.IllegalArgumentException
meth public abstract void org.netbeans.spi.project.libraries.LibraryImplementation.setDescription(java.lang.String)
meth public abstract void org.netbeans.spi.project.libraries.LibraryImplementation.setLocalizingBundle(java.lang.String)
meth public abstract void org.netbeans.spi.project.libraries.LibraryImplementation.setName(java.lang.String)
supr null
CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryProvider
fld  constant public static final java.lang.String org.netbeans.spi.project.libraries.LibraryProvider.PROP_LIBRARIES
meth public abstract [Lorg.netbeans.spi.project.libraries.LibraryImplementation; org.netbeans.spi.project.libraries.LibraryProvider.getLibraries()
meth public abstract void org.netbeans.spi.project.libraries.LibraryProvider.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.libraries.LibraryProvider.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryTypeProvider
intf org.openide.util.Lookup$Provider
meth public abstract [Ljava.lang.String; org.netbeans.spi.project.libraries.LibraryTypeProvider.getSupportedVolumeTypes()
meth public abstract java.beans.Customizer org.netbeans.spi.project.libraries.LibraryTypeProvider.getCustomizer(java.lang.String)
meth public abstract java.lang.String org.netbeans.spi.project.libraries.LibraryTypeProvider.getDisplayName()
meth public abstract java.lang.String org.netbeans.spi.project.libraries.LibraryTypeProvider.getLibraryType()
meth public abstract org.netbeans.spi.project.libraries.LibraryImplementation org.netbeans.spi.project.libraries.LibraryTypeProvider.createLibrary()
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
meth public abstract void org.netbeans.spi.project.libraries.LibraryTypeProvider.libraryCreated(org.netbeans.spi.project.libraries.LibraryImplementation)
meth public abstract void org.netbeans.spi.project.libraries.LibraryTypeProvider.libraryDeleted(org.netbeans.spi.project.libraries.LibraryImplementation)
supr null
CLSS public final org.netbeans.spi.project.libraries.support.LibrariesSupport
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
meth public static [Lorg.netbeans.spi.project.libraries.LibraryTypeProvider; org.netbeans.spi.project.libraries.support.LibrariesSupport.getLibraryTypeProviders()
meth public static org.netbeans.spi.project.libraries.LibraryImplementation org.netbeans.spi.project.libraries.support.LibrariesSupport.createLibraryImplementation(java.lang.String,[Ljava.lang.String;)
meth public static org.netbeans.spi.project.libraries.LibraryTypeProvider org.netbeans.spi.project.libraries.support.LibrariesSupport.getLibraryTypeProvider(java.lang.String)
supr java.lang.Object
