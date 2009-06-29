#Signature file v4.0
#Version 1.24.1

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

CLSS public org.netbeans.api.project.FileOwnerQuery
fld public final static int EXTERNAL_ALGORITHM_TRANSIENT = 0
meth public static org.netbeans.api.project.Project getOwner(java.net.URI)
meth public static org.netbeans.api.project.Project getOwner(org.openide.filesystems.FileObject)
meth public static void markExternalOwner(java.net.URI,org.netbeans.api.project.Project,int)
meth public static void markExternalOwner(org.openide.filesystems.FileObject,org.netbeans.api.project.Project,int)
supr java.lang.Object
hfds LOG,cache,implementations

CLSS public abstract interface org.netbeans.api.project.Project
intf org.openide.util.Lookup$Provider
meth public abstract org.openide.filesystems.FileObject getProjectDirectory()
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract interface org.netbeans.api.project.ProjectInformation
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_NAME = "name"
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract javax.swing.Icon getIcon()
meth public abstract org.netbeans.api.project.Project getProject()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.api.project.ProjectManager
innr public final static Result
meth public boolean isModified(org.netbeans.api.project.Project)
meth public boolean isProject(org.openide.filesystems.FileObject)
meth public boolean isValid(org.netbeans.api.project.Project)
meth public java.util.Set<org.netbeans.api.project.Project> getModifiedProjects()
meth public org.netbeans.api.project.Project findProject(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.api.project.ProjectManager$Result isProject2(org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.ProjectManager getDefault()
meth public static org.openide.util.Mutex mutex()
meth public void clearNonProjectCache()
meth public void saveAllProjects() throws java.io.IOException
meth public void saveProject(org.netbeans.api.project.Project) throws java.io.IOException
supr java.lang.Object
hfds DEFAULT,LOG,MUTEX,TIMERS,dir2Proj,factories,loadingThread,modifiedProjects,proj2Factory,projectDeletionListener,removedProjects
hcls LoadStatus,ProjectDeletionListener,ProjectStateImpl

CLSS public final static org.netbeans.api.project.ProjectManager$Result
cons public Result(javax.swing.Icon)
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds icon

CLSS public org.netbeans.api.project.ProjectUtils
meth public static boolean hasSubprojectCycles(org.netbeans.api.project.Project,org.netbeans.api.project.Project)
meth public static java.util.prefs.Preferences getPreferences(org.netbeans.api.project.Project,java.lang.Class,boolean)
meth public static org.netbeans.api.project.ProjectInformation getInformation(org.netbeans.api.project.Project)
meth public static org.netbeans.api.project.Sources getSources(org.netbeans.api.project.Project)
meth public static org.netbeans.spi.project.AuxiliaryConfiguration getAuxiliaryConfiguration(org.netbeans.api.project.Project)
supr java.lang.Object
hcls BasicInformation

CLSS public abstract interface org.netbeans.api.project.SourceGroup
fld public final static java.lang.String PROP_CONTAINERSHIP = "containership"
meth public abstract boolean contains(org.openide.filesystems.FileObject)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract javax.swing.Icon getIcon(boolean)
meth public abstract org.openide.filesystems.FileObject getRootFolder()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.api.project.SourceGroupModifier
innr public final static Future
meth public final static org.netbeans.api.project.SourceGroup createSourceGroup(org.netbeans.api.project.Project,java.lang.String,java.lang.String)
meth public final static org.netbeans.api.project.SourceGroupModifier$Future createSourceGroupFuture(org.netbeans.api.project.Project,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public final static org.netbeans.api.project.SourceGroupModifier$Future
meth public final org.netbeans.api.project.SourceGroup createSourceGroup()
meth public java.lang.String getHint()
meth public java.lang.String getType()
supr java.lang.Object
hfds hint,impl,type

CLSS public abstract interface org.netbeans.api.project.Sources
fld public final static java.lang.String TYPE_GENERIC = "generic"
meth public abstract org.netbeans.api.project.SourceGroup[] getSourceGroups(java.lang.String)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.spi.project.ActionProvider
fld public final static java.lang.String COMMAND_BUILD = "build"
fld public final static java.lang.String COMMAND_CLEAN = "clean"
fld public final static java.lang.String COMMAND_COMPILE_SINGLE = "compile.single"
fld public final static java.lang.String COMMAND_COPY = "copy"
fld public final static java.lang.String COMMAND_DEBUG = "debug"
fld public final static java.lang.String COMMAND_DEBUG_SINGLE = "debug.single"
fld public final static java.lang.String COMMAND_DEBUG_STEP_INTO = "debug.stepinto"
fld public final static java.lang.String COMMAND_DEBUG_TEST_SINGLE = "debug.test.single"
fld public final static java.lang.String COMMAND_DELETE = "delete"
fld public final static java.lang.String COMMAND_MOVE = "move"
fld public final static java.lang.String COMMAND_REBUILD = "rebuild"
fld public final static java.lang.String COMMAND_RENAME = "rename"
fld public final static java.lang.String COMMAND_RUN = "run"
fld public final static java.lang.String COMMAND_RUN_SINGLE = "run.single"
fld public final static java.lang.String COMMAND_TEST = "test"
fld public final static java.lang.String COMMAND_TEST_SINGLE = "test.single"
meth public abstract boolean isActionEnabled(java.lang.String,org.openide.util.Lookup)
meth public abstract java.lang.String[] getSupportedActions()
meth public abstract void invokeAction(java.lang.String,org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.project.AuxiliaryConfiguration
meth public abstract boolean removeConfigurationFragment(java.lang.String,java.lang.String,boolean)
meth public abstract org.w3c.dom.Element getConfigurationFragment(java.lang.String,java.lang.String,boolean)
meth public abstract void putConfigurationFragment(org.w3c.dom.Element,boolean)

CLSS public abstract interface org.netbeans.spi.project.AuxiliaryProperties
meth public abstract java.lang.Iterable<java.lang.String> listKeys(boolean)
meth public abstract java.lang.String get(java.lang.String,boolean)
meth public abstract void put(java.lang.String,java.lang.String,boolean)

CLSS public abstract interface org.netbeans.spi.project.CacheDirectoryProvider
meth public abstract org.openide.filesystems.FileObject getCacheDirectory() throws java.io.IOException

CLSS public abstract interface org.netbeans.spi.project.CopyOperationImplementation
intf org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract void notifyCopied(org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public abstract void notifyCopying() throws java.io.IOException

CLSS public abstract interface org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract java.util.List<org.openide.filesystems.FileObject> getDataFiles()
meth public abstract java.util.List<org.openide.filesystems.FileObject> getMetadataFiles()

CLSS public abstract interface org.netbeans.spi.project.DeleteOperationImplementation
intf org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract void notifyDeleted() throws java.io.IOException
meth public abstract void notifyDeleting() throws java.io.IOException

CLSS public abstract interface org.netbeans.spi.project.FileOwnerQueryImplementation
meth public abstract org.netbeans.api.project.Project getOwner(java.net.URI)
meth public abstract org.netbeans.api.project.Project getOwner(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.spi.project.LookupMerger<%0 extends java.lang.Object>
innr public abstract interface static !annotation Registration
meth public abstract java.lang.Class<{org.netbeans.spi.project.LookupMerger%0}> getMergeableClass()
meth public abstract {org.netbeans.spi.project.LookupMerger%0} merge(org.openide.util.Lookup)

CLSS public abstract interface static !annotation org.netbeans.spi.project.LookupMerger$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] projectType()
meth public abstract !hasdefault org.netbeans.spi.project.LookupProvider$Registration$ProjectType[] projectTypes()

CLSS public abstract interface org.netbeans.spi.project.LookupProvider
innr public abstract interface static !annotation Registration
meth public abstract org.openide.util.Lookup createAdditionalLookup(org.openide.util.Lookup)

CLSS public abstract interface static !annotation org.netbeans.spi.project.LookupProvider$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
innr public abstract interface static !annotation ProjectType
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] projectType()
meth public abstract !hasdefault org.netbeans.spi.project.LookupProvider$Registration$ProjectType[] projectTypes()

CLSS public abstract interface static !annotation org.netbeans.spi.project.LookupProvider$Registration$ProjectType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String id()

CLSS public abstract interface org.netbeans.spi.project.MoveOperationImplementation
intf org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract void notifyMoved(org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public abstract void notifyMoving() throws java.io.IOException

CLSS public abstract interface org.netbeans.spi.project.ProjectConfiguration
meth public abstract java.lang.String getDisplayName()

CLSS public abstract interface org.netbeans.spi.project.ProjectConfigurationProvider<%0 extends org.netbeans.spi.project.ProjectConfiguration>
fld public final static java.lang.String PROP_CONFIGURATIONS = "configurations"
fld public final static java.lang.String PROP_CONFIGURATION_ACTIVE = "activeConfiguration"
meth public abstract boolean configurationsAffectAction(java.lang.String)
meth public abstract boolean hasCustomizer()
meth public abstract java.util.Collection<{org.netbeans.spi.project.ProjectConfigurationProvider%0}> getConfigurations()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void customize()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setActiveConfiguration({org.netbeans.spi.project.ProjectConfigurationProvider%0}) throws java.io.IOException
meth public abstract {org.netbeans.spi.project.ProjectConfigurationProvider%0} getActiveConfiguration()

CLSS public abstract interface org.netbeans.spi.project.ProjectFactory
meth public abstract boolean isProject(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.api.project.Project loadProject(org.openide.filesystems.FileObject,org.netbeans.spi.project.ProjectState) throws java.io.IOException
meth public abstract void saveProject(org.netbeans.api.project.Project) throws java.io.IOException

CLSS public abstract interface org.netbeans.spi.project.ProjectFactory2
intf org.netbeans.spi.project.ProjectFactory
meth public abstract org.netbeans.api.project.ProjectManager$Result isProject2(org.openide.filesystems.FileObject)

CLSS public abstract interface !annotation org.netbeans.spi.project.ProjectServiceProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] projectType()
meth public abstract !hasdefault org.netbeans.spi.project.LookupProvider$Registration$ProjectType[] projectTypes()
meth public abstract java.lang.Class<?>[] service()

CLSS public abstract interface org.netbeans.spi.project.ProjectState
meth public abstract void markModified()
meth public abstract void notifyDeleted()

CLSS public final org.netbeans.spi.project.SingleMethod
cons public SingleMethod(org.openide.filesystems.FileObject,java.lang.String)
fld public final static java.lang.String COMMAND_DEBUG_SINGLE_METHOD = "debug.single.method"
fld public final static java.lang.String COMMAND_RUN_SINGLE_METHOD = "run.single.method"
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getMethodName()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds file,methodName

CLSS public abstract interface org.netbeans.spi.project.SourceGroupModifierImplementation
meth public abstract boolean canCreateSourceGroup(java.lang.String,java.lang.String)
meth public abstract org.netbeans.api.project.SourceGroup createSourceGroup(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.spi.project.SubprojectProvider
meth public abstract java.util.Set<? extends org.netbeans.api.project.Project> getSubprojects()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.spi.project.support.GenericSources
meth public static org.netbeans.api.project.SourceGroup group(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public static org.netbeans.api.project.Sources genericOnly(org.netbeans.api.project.Project)
supr java.lang.Object
hcls GenericOnlySources,Group

CLSS public final org.netbeans.spi.project.support.LookupProviderSupport
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.api.project.Sources> createSourcesMerger()
meth public static org.openide.util.Lookup createCompositeLookup(org.openide.util.Lookup,java.lang.String)
supr java.lang.Object
hcls DelegatingLookupImpl,SourcesImpl,SourcesMerger

CLSS public final org.netbeans.spi.project.support.ProjectOperations
meth public static boolean isCopyOperationSupported(org.netbeans.api.project.Project)
meth public static boolean isDeleteOperationSupported(org.netbeans.api.project.Project)
meth public static boolean isMoveOperationSupported(org.netbeans.api.project.Project)
meth public static java.util.List<org.openide.filesystems.FileObject> getDataFiles(org.netbeans.api.project.Project)
meth public static java.util.List<org.openide.filesystems.FileObject> getMetadataFiles(org.netbeans.api.project.Project)
meth public static void notifyCopied(org.netbeans.api.project.Project,org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public static void notifyCopying(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void notifyDeleted(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void notifyDeleting(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void notifyMoved(org.netbeans.api.project.Project,org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public static void notifyMoving(org.netbeans.api.project.Project) throws java.io.IOException
supr java.lang.Object

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

CLSS public abstract interface static org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup getLookup()

