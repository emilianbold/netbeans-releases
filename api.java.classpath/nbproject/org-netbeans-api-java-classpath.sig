#Signature file v4.0
#Version 1.19.1

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected Enum(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object
hfds name,ordinal

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public final org.netbeans.api.java.classpath.ClassPath
fld public final static java.lang.String BOOT = "classpath/boot"
fld public final static java.lang.String COMPILE = "classpath/compile"
fld public final static java.lang.String DEBUG = "classpath/debug"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String EXECUTE = "classpath/execute"
fld public final static java.lang.String PROP_ENTRIES = "entries"
fld public final static java.lang.String PROP_INCLUDES = "includes"
fld public final static java.lang.String PROP_ROOTS = "roots"
fld public final static java.lang.String SOURCE = "classpath/source"
innr public final Entry
innr public final static !enum PathConversionMode
meth public final boolean contains(org.openide.filesystems.FileObject)
meth public final boolean isResourceVisible(org.openide.filesystems.FileObject)
meth public final java.lang.ClassLoader getClassLoader(boolean)
meth public final java.lang.String getResourceName(org.openide.filesystems.FileObject)
meth public final java.lang.String getResourceName(org.openide.filesystems.FileObject,char,boolean)
meth public final java.util.List<org.openide.filesystems.FileObject> findAllResources(java.lang.String)
meth public final org.openide.filesystems.FileObject findOwnerRoot(org.openide.filesystems.FileObject)
meth public final org.openide.filesystems.FileObject findResource(java.lang.String)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String toString()
meth public java.lang.String toString(org.netbeans.api.java.classpath.ClassPath$PathConversionMode)
meth public java.util.List<org.netbeans.api.java.classpath.ClassPath$Entry> entries()
meth public org.openide.filesystems.FileObject[] getRoots()
meth public static org.netbeans.api.java.classpath.ClassPath getClassPath(org.openide.filesystems.FileObject,java.lang.String)
supr java.lang.Object
hfds EMPTY_REF,LOG,entriesCache,impl,implementations,invalidEntries,invalidRoots,pListener,propSupport,refClassLoader,root2Filter,rootsCache,rootsListener,weakPListener
hcls RootsListener,SPIListener

CLSS public final org.netbeans.api.java.classpath.ClassPath$Entry
meth public boolean equals(java.lang.Object)
meth public boolean includes(java.lang.String)
meth public boolean includes(java.net.URL)
meth public boolean includes(org.openide.filesystems.FileObject)
meth public boolean isValid()
meth public int hashCode()
meth public java.io.IOException getError()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public org.netbeans.api.java.classpath.ClassPath getDefiningClassPath()
meth public org.openide.filesystems.FileObject getRoot()
supr java.lang.Object
hfds filter,lastError,root,url

CLSS public final static !enum org.netbeans.api.java.classpath.ClassPath$PathConversionMode
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode FAIL
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode PRINT
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode SKIP
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode WARN
meth public static org.netbeans.api.java.classpath.ClassPath$PathConversionMode valueOf(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath$PathConversionMode[] values()
supr java.lang.Enum<org.netbeans.api.java.classpath.ClassPath$PathConversionMode>

CLSS public final org.netbeans.api.java.classpath.GlobalPathRegistry
meth public java.util.Set<org.netbeans.api.java.classpath.ClassPath> getPaths(java.lang.String)
meth public java.util.Set<org.openide.filesystems.FileObject> getSourceRoots()
meth public org.openide.filesystems.FileObject findResource(java.lang.String)
meth public static org.netbeans.api.java.classpath.GlobalPathRegistry getDefault()
meth public void addGlobalPathRegistryListener(org.netbeans.api.java.classpath.GlobalPathRegistryListener)
meth public void register(java.lang.String,org.netbeans.api.java.classpath.ClassPath[])
meth public void removeGlobalPathRegistryListener(org.netbeans.api.java.classpath.GlobalPathRegistryListener)
meth public void unregister(java.lang.String,org.netbeans.api.java.classpath.ClassPath[])
supr java.lang.Object
hfds DEFAULT,LOG,classpathListener,listeners,paths,resetCount,resultListener,results,sourceRoots
hcls SFBQListener

CLSS public final org.netbeans.api.java.classpath.GlobalPathRegistryEvent
meth public java.lang.String getId()
meth public java.util.Set<org.netbeans.api.java.classpath.ClassPath> getChangedPaths()
meth public org.netbeans.api.java.classpath.GlobalPathRegistry getRegistry()
supr java.util.EventObject
hfds changed,id

CLSS public abstract interface org.netbeans.api.java.classpath.GlobalPathRegistryListener
intf java.util.EventListener
meth public abstract void pathsAdded(org.netbeans.api.java.classpath.GlobalPathRegistryEvent)
meth public abstract void pathsRemoved(org.netbeans.api.java.classpath.GlobalPathRegistryEvent)

CLSS public final org.netbeans.api.java.queries.BinaryForSourceQuery
innr public abstract interface static Result
meth public static org.netbeans.api.java.queries.BinaryForSourceQuery$Result findBinaryRoots(java.net.URL)
supr java.lang.Object
hcls DefaultResult

CLSS public abstract interface static org.netbeans.api.java.queries.BinaryForSourceQuery$Result
meth public abstract java.net.URL[] getRoots()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.api.java.queries.SourceForBinaryQuery
innr public abstract interface static Result
innr public static Result2
meth public static org.netbeans.api.java.queries.SourceForBinaryQuery$Result findSourceRoots(java.net.URL)
meth public static org.netbeans.api.java.queries.SourceForBinaryQuery$Result2 findSourceRoots2(java.net.URL)
supr java.lang.Object
hfds EMPTY_RESULT,EMPTY_RESULT2,LOG,implementations
hcls EmptyResult

CLSS public abstract interface static org.netbeans.api.java.queries.SourceForBinaryQuery$Result
meth public abstract org.openide.filesystems.FileObject[] getRoots()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public static org.netbeans.api.java.queries.SourceForBinaryQuery$Result2
intf org.netbeans.api.java.queries.SourceForBinaryQuery$Result
meth public boolean preferSources()
meth public org.openide.filesystems.FileObject[] getRoots()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds changeSupport,delegate,spiListener

CLSS public final org.netbeans.spi.java.classpath.ClassPathFactory
meth public static org.netbeans.api.java.classpath.ClassPath createClassPath(org.netbeans.spi.java.classpath.ClassPathImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.java.classpath.ClassPathImplementation
fld public final static java.lang.String PROP_RESOURCES = "resources"
meth public abstract java.util.List<? extends org.netbeans.spi.java.classpath.PathResourceImplementation> getResources()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.java.classpath.ClassPathProvider
meth public abstract org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)

CLSS public abstract interface org.netbeans.spi.java.classpath.FilteringPathResourceImplementation
fld public final static java.lang.String PROP_INCLUDES = "includes"
intf org.netbeans.spi.java.classpath.PathResourceImplementation
meth public abstract boolean includes(java.net.URL,java.lang.String)

CLSS public abstract interface org.netbeans.spi.java.classpath.PathResourceImplementation
fld public final static java.lang.String PROP_ROOTS = "roots"
meth public abstract java.net.URL[] getRoots()
meth public abstract org.netbeans.spi.java.classpath.ClassPathImplementation getContent()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public org.netbeans.spi.java.classpath.support.ClassPathSupport
meth public !varargs static org.netbeans.api.java.classpath.ClassPath createClassPath(java.net.URL[])
meth public !varargs static org.netbeans.api.java.classpath.ClassPath createClassPath(org.openide.filesystems.FileObject[])
meth public !varargs static org.netbeans.api.java.classpath.ClassPath createProxyClassPath(org.netbeans.api.java.classpath.ClassPath[])
meth public !varargs static org.netbeans.spi.java.classpath.ClassPathImplementation createProxyClassPathImplementation(org.netbeans.spi.java.classpath.ClassPathImplementation[])
meth public static org.netbeans.api.java.classpath.ClassPath createClassPath(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath createClassPath(java.util.List<? extends org.netbeans.spi.java.classpath.PathResourceImplementation>)
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createClassPathImplementation(java.util.List<? extends org.netbeans.spi.java.classpath.PathResourceImplementation>)
meth public static org.netbeans.spi.java.classpath.PathResourceImplementation createResource(java.net.URL)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.java.classpath.support.CompositePathResourceBase
cons public CompositePathResourceBase()
intf org.netbeans.spi.java.classpath.PathResourceImplementation
meth protected abstract org.netbeans.spi.java.classpath.ClassPathImplementation createContent()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final java.net.URL[] getRoots()
meth public final org.netbeans.spi.java.classpath.ClassPathImplementation getContent()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds model,pListeners,roots

CLSS public abstract org.netbeans.spi.java.classpath.support.PathResourceBase
cons public PathResourceBase()
intf org.netbeans.spi.java.classpath.PathResourceImplementation
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds pListeners

CLSS public abstract interface org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation
meth public abstract org.netbeans.api.java.queries.BinaryForSourceQuery$Result findBinaryRoots(java.net.URL)

CLSS public abstract interface org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation
meth public abstract org.netbeans.api.java.queries.SourceForBinaryQuery$Result findSourceRoots(java.net.URL)

CLSS public abstract interface org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2
innr public abstract interface static Result
intf org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation
meth public abstract org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2$Result findSourceRoots2(java.net.URL)

CLSS public abstract interface static org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2$Result
intf org.netbeans.api.java.queries.SourceForBinaryQuery$Result
meth public abstract boolean preferSources()

CLSS public abstract org.netbeans.spi.java.queries.support.SourceForBinaryQueryImpl2Base
cons public SourceForBinaryQueryImpl2Base()
intf org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2
meth protected final org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2$Result asResult(org.netbeans.api.java.queries.SourceForBinaryQuery$Result)
supr java.lang.Object

