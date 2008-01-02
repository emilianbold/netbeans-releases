#API master signature file
#Version 1.13.1
CLSS public static abstract org.openide.util.Lookup$Item
cons public Item()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Class org.openide.util.Lookup$Item.getType()
meth public abstract java.lang.Object org.openide.util.Lookup$Item.getInstance()
meth public abstract java.lang.String org.openide.util.Lookup$Item.getDisplayName()
meth public abstract java.lang.String org.openide.util.Lookup$Item.getId()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.Lookup$Item.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
supr null
CLSS public static abstract org.openide.util.Lookup$Result
cons public Result()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.util.Collection org.openide.util.Lookup$Result.allInstances()
meth public abstract void org.openide.util.Lookup$Result.addLookupListener(org.openide.util.LookupListener)
meth public abstract void org.openide.util.Lookup$Result.removeLookupListener(org.openide.util.LookupListener)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Collection org.openide.util.Lookup$Result.allItems()
meth public java.util.Set org.openide.util.Lookup$Result.allClasses()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static final org.openide.util.Lookup$Template
cons public Template()
cons public Template(java.lang.Class)
cons public Template(java.lang.Class,java.lang.String,java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.util.Lookup$Template.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.util.Lookup$Template.hashCode()
meth public java.lang.Class org.openide.util.Lookup$Template.getType()
meth public java.lang.Object org.openide.util.Lookup$Template.getInstance()
meth public java.lang.String org.openide.util.Lookup$Template.getId()
meth public java.lang.String org.openide.util.Lookup$Template.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Mutex$Action
intf org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$Action.run()
supr null
CLSS public static abstract interface org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$ExceptionAction.run() throws java.lang.Exception
supr null
CLSS public org.netbeans.api.project.FileOwnerQuery
fld  constant public static final int org.netbeans.api.project.FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT
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
meth public static org.netbeans.api.project.Project org.netbeans.api.project.FileOwnerQuery.getOwner(java.net.URI)
meth public static org.netbeans.api.project.Project org.netbeans.api.project.FileOwnerQuery.getOwner(org.openide.filesystems.FileObject)
meth public static void org.netbeans.api.project.FileOwnerQuery.markExternalOwner(java.net.URI,org.netbeans.api.project.Project,int) throws java.lang.IllegalArgumentException
meth public static void org.netbeans.api.project.FileOwnerQuery.markExternalOwner(org.openide.filesystems.FileObject,org.netbeans.api.project.Project,int) throws java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public abstract interface org.netbeans.api.project.Project
intf org.openide.util.Lookup$Provider
meth public abstract org.openide.filesystems.FileObject org.netbeans.api.project.Project.getProjectDirectory()
meth public abstract org.openide.util.Lookup org.netbeans.api.project.Project.getLookup()
supr null
CLSS public abstract interface org.netbeans.api.project.ProjectInformation
fld  constant public static final java.lang.String org.netbeans.api.project.ProjectInformation.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.netbeans.api.project.ProjectInformation.PROP_ICON
fld  constant public static final java.lang.String org.netbeans.api.project.ProjectInformation.PROP_NAME
meth public abstract java.lang.String org.netbeans.api.project.ProjectInformation.getDisplayName()
meth public abstract java.lang.String org.netbeans.api.project.ProjectInformation.getName()
meth public abstract javax.swing.Icon org.netbeans.api.project.ProjectInformation.getIcon()
meth public abstract org.netbeans.api.project.Project org.netbeans.api.project.ProjectInformation.getProject()
meth public abstract void org.netbeans.api.project.ProjectInformation.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.project.ProjectInformation.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
CLSS public final org.netbeans.api.project.ProjectManager
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.project.ProjectManager.isModified(org.netbeans.api.project.Project) throws java.lang.IllegalArgumentException
meth public boolean org.netbeans.api.project.ProjectManager.isProject(org.openide.filesystems.FileObject) throws java.lang.IllegalArgumentException
meth public boolean org.netbeans.api.project.ProjectManager.isValid(org.netbeans.api.project.Project)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Set org.netbeans.api.project.ProjectManager.getModifiedProjects()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.Project org.netbeans.api.project.ProjectManager.findProject(org.openide.filesystems.FileObject) throws java.io.IOException,java.lang.IllegalArgumentException
meth public static org.netbeans.api.project.ProjectManager org.netbeans.api.project.ProjectManager.getDefault()
meth public static org.openide.util.Mutex org.netbeans.api.project.ProjectManager.mutex()
meth public void org.netbeans.api.project.ProjectManager.clearNonProjectCache()
meth public void org.netbeans.api.project.ProjectManager.saveAllProjects() throws java.io.IOException
meth public void org.netbeans.api.project.ProjectManager.saveProject(org.netbeans.api.project.Project) throws java.io.IOException,java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public org.netbeans.api.project.ProjectUtils
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
meth public static boolean org.netbeans.api.project.ProjectUtils.hasSubprojectCycles(org.netbeans.api.project.Project,org.netbeans.api.project.Project)
meth public static org.netbeans.api.project.ProjectInformation org.netbeans.api.project.ProjectUtils.getInformation(org.netbeans.api.project.Project)
meth public static org.netbeans.api.project.Sources org.netbeans.api.project.ProjectUtils.getSources(org.netbeans.api.project.Project)
supr java.lang.Object
CLSS public abstract interface org.netbeans.api.project.SourceGroup
fld  constant public static final java.lang.String org.netbeans.api.project.SourceGroup.PROP_CONTAINERSHIP
meth public abstract boolean org.netbeans.api.project.SourceGroup.contains(org.openide.filesystems.FileObject) throws java.lang.IllegalArgumentException
meth public abstract java.lang.String org.netbeans.api.project.SourceGroup.getDisplayName()
meth public abstract java.lang.String org.netbeans.api.project.SourceGroup.getName()
meth public abstract javax.swing.Icon org.netbeans.api.project.SourceGroup.getIcon(boolean)
meth public abstract org.openide.filesystems.FileObject org.netbeans.api.project.SourceGroup.getRootFolder()
meth public abstract void org.netbeans.api.project.SourceGroup.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.api.project.SourceGroup.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
CLSS public abstract interface org.netbeans.api.project.Sources
fld  constant public static final java.lang.String org.netbeans.api.project.Sources.TYPE_GENERIC
meth public abstract [Lorg.netbeans.api.project.SourceGroup; org.netbeans.api.project.Sources.getSourceGroups(java.lang.String)
meth public abstract void org.netbeans.api.project.Sources.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.netbeans.api.project.Sources.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public abstract interface org.netbeans.spi.project.ActionProvider
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_BUILD
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_COMPILE_SINGLE
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_COPY
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG_SINGLE
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG_STEP_INTO
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG_TEST_SINGLE
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_DELETE
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_MOVE
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_RENAME
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_RUN
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_RUN_SINGLE
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_TEST
fld  constant public static final java.lang.String org.netbeans.spi.project.ActionProvider.COMMAND_TEST_SINGLE
meth public abstract [Ljava.lang.String; org.netbeans.spi.project.ActionProvider.getSupportedActions()
meth public abstract boolean org.netbeans.spi.project.ActionProvider.isActionEnabled(java.lang.String,org.openide.util.Lookup) throws java.lang.IllegalArgumentException
meth public abstract void org.netbeans.spi.project.ActionProvider.invokeAction(java.lang.String,org.openide.util.Lookup) throws java.lang.IllegalArgumentException
supr null
CLSS public abstract interface org.netbeans.spi.project.AuxiliaryConfiguration
meth public abstract boolean org.netbeans.spi.project.AuxiliaryConfiguration.removeConfigurationFragment(java.lang.String,java.lang.String,boolean) throws java.lang.IllegalArgumentException
meth public abstract org.w3c.dom.Element org.netbeans.spi.project.AuxiliaryConfiguration.getConfigurationFragment(java.lang.String,java.lang.String,boolean)
meth public abstract void org.netbeans.spi.project.AuxiliaryConfiguration.putConfigurationFragment(org.w3c.dom.Element,boolean) throws java.lang.IllegalArgumentException
supr null
CLSS public abstract interface org.netbeans.spi.project.CacheDirectoryProvider
meth public abstract org.openide.filesystems.FileObject org.netbeans.spi.project.CacheDirectoryProvider.getCacheDirectory() throws java.io.IOException
supr null
CLSS public abstract interface org.netbeans.spi.project.CopyOperationImplementation
intf org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getDataFiles()
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getMetadataFiles()
meth public abstract void org.netbeans.spi.project.CopyOperationImplementation.notifyCopied(org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public abstract void org.netbeans.spi.project.CopyOperationImplementation.notifyCopying() throws java.io.IOException
supr null
CLSS public abstract interface org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getDataFiles()
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getMetadataFiles()
supr null
CLSS public abstract interface org.netbeans.spi.project.DeleteOperationImplementation
intf org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getDataFiles()
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getMetadataFiles()
meth public abstract void org.netbeans.spi.project.DeleteOperationImplementation.notifyDeleted() throws java.io.IOException
meth public abstract void org.netbeans.spi.project.DeleteOperationImplementation.notifyDeleting() throws java.io.IOException
supr null
CLSS public abstract interface org.netbeans.spi.project.FileOwnerQueryImplementation
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.FileOwnerQueryImplementation.getOwner(java.net.URI)
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.FileOwnerQueryImplementation.getOwner(org.openide.filesystems.FileObject)
supr null
CLSS public abstract interface org.netbeans.spi.project.LookupMerger
meth public abstract java.lang.Class org.netbeans.spi.project.LookupMerger.getMergeableClass()
meth public abstract java.lang.Object org.netbeans.spi.project.LookupMerger.merge(org.openide.util.Lookup)
supr null
CLSS public abstract interface org.netbeans.spi.project.LookupProvider
meth public abstract org.openide.util.Lookup org.netbeans.spi.project.LookupProvider.createAdditionalLookup(org.openide.util.Lookup)
supr null
CLSS public abstract interface org.netbeans.spi.project.MoveOperationImplementation
intf org.netbeans.spi.project.DataFilesProviderImplementation
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getDataFiles()
meth public abstract java.util.List org.netbeans.spi.project.DataFilesProviderImplementation.getMetadataFiles()
meth public abstract void org.netbeans.spi.project.MoveOperationImplementation.notifyMoved(org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public abstract void org.netbeans.spi.project.MoveOperationImplementation.notifyMoving() throws java.io.IOException
supr null
CLSS public abstract interface org.netbeans.spi.project.ProjectConfiguration
meth public abstract java.lang.String org.netbeans.spi.project.ProjectConfiguration.getDisplayName()
supr null
CLSS public abstract interface org.netbeans.spi.project.ProjectConfigurationProvider
fld  constant public static final java.lang.String org.netbeans.spi.project.ProjectConfigurationProvider.PROP_CONFIGURATIONS
fld  constant public static final java.lang.String org.netbeans.spi.project.ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE
meth public abstract boolean org.netbeans.spi.project.ProjectConfigurationProvider.configurationsAffectAction(java.lang.String)
meth public abstract boolean org.netbeans.spi.project.ProjectConfigurationProvider.hasCustomizer()
meth public abstract java.util.Collection org.netbeans.spi.project.ProjectConfigurationProvider.getConfigurations()
meth public abstract org.netbeans.spi.project.ProjectConfiguration org.netbeans.spi.project.ProjectConfigurationProvider.getActiveConfiguration()
meth public abstract void org.netbeans.spi.project.ProjectConfigurationProvider.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.ProjectConfigurationProvider.customize()
meth public abstract void org.netbeans.spi.project.ProjectConfigurationProvider.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.ProjectConfigurationProvider.setActiveConfiguration(org.netbeans.spi.project.ProjectConfiguration) throws java.io.IOException,java.lang.IllegalArgumentException
supr null
CLSS public abstract interface org.netbeans.spi.project.ProjectFactory
meth public abstract boolean org.netbeans.spi.project.ProjectFactory.isProject(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.ProjectFactory.loadProject(org.openide.filesystems.FileObject,org.netbeans.spi.project.ProjectState) throws java.io.IOException
meth public abstract void org.netbeans.spi.project.ProjectFactory.saveProject(org.netbeans.api.project.Project) throws java.io.IOException,java.lang.ClassCastException
supr null
CLSS public abstract interface org.netbeans.spi.project.ProjectState
meth public abstract void org.netbeans.spi.project.ProjectState.markModified()
meth public abstract void org.netbeans.spi.project.ProjectState.notifyDeleted() throws java.lang.IllegalStateException
supr null
CLSS public abstract interface org.netbeans.spi.project.SubprojectProvider
meth public abstract java.util.Set org.netbeans.spi.project.SubprojectProvider.getSubprojects()
meth public abstract void org.netbeans.spi.project.SubprojectProvider.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.netbeans.spi.project.SubprojectProvider.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public org.netbeans.spi.project.support.GenericSources
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
meth public static org.netbeans.api.project.SourceGroup org.netbeans.spi.project.support.GenericSources.group(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public static org.netbeans.api.project.Sources org.netbeans.spi.project.support.GenericSources.genericOnly(org.netbeans.api.project.Project)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.LookupProviderSupport
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
meth public static org.netbeans.spi.project.LookupMerger org.netbeans.spi.project.support.LookupProviderSupport.createSourcesMerger()
meth public static org.openide.util.Lookup org.netbeans.spi.project.support.LookupProviderSupport.createCompositeLookup(org.openide.util.Lookup,java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ProjectOperations
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
meth public static boolean org.netbeans.spi.project.support.ProjectOperations.isCopyOperationSupported(org.netbeans.api.project.Project)
meth public static boolean org.netbeans.spi.project.support.ProjectOperations.isDeleteOperationSupported(org.netbeans.api.project.Project)
meth public static boolean org.netbeans.spi.project.support.ProjectOperations.isMoveOperationSupported(org.netbeans.api.project.Project)
meth public static java.util.List org.netbeans.spi.project.support.ProjectOperations.getDataFiles(org.netbeans.api.project.Project)
meth public static java.util.List org.netbeans.spi.project.support.ProjectOperations.getMetadataFiles(org.netbeans.api.project.Project)
meth public static void org.netbeans.spi.project.support.ProjectOperations.notifyCopied(org.netbeans.api.project.Project,org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public static void org.netbeans.spi.project.support.ProjectOperations.notifyCopying(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void org.netbeans.spi.project.support.ProjectOperations.notifyDeleted(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void org.netbeans.spi.project.support.ProjectOperations.notifyDeleting(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void org.netbeans.spi.project.support.ProjectOperations.notifyMoved(org.netbeans.api.project.Project,org.netbeans.api.project.Project,java.io.File,java.lang.String) throws java.io.IOException
meth public static void org.netbeans.spi.project.support.ProjectOperations.notifyMoving(org.netbeans.api.project.Project) throws java.io.IOException
supr java.lang.Object
