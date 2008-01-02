#API master signature file
#Version 1.17.1
CLSS public static abstract interface java.util.Map$Entry
meth public abstract boolean java.util.Map$Entry.equals(java.lang.Object)
meth public abstract int java.util.Map$Entry.hashCode()
meth public abstract java.lang.Object java.util.Map$Entry.getKey()
meth public abstract java.lang.Object java.util.Map$Entry.getValue()
meth public abstract java.lang.Object java.util.Map$Entry.setValue(java.lang.Object)
supr null
CLSS public final org.netbeans.api.project.ant.AntBuildExtender$Extension
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
meth public void org.netbeans.api.project.ant.AntBuildExtender$Extension.addDependency(java.lang.String,java.lang.String)
meth public void org.netbeans.api.project.ant.AntBuildExtender$Extension.removeDependency(java.lang.String,java.lang.String)
supr java.lang.Object
CLSS public static abstract interface org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton$AntProjectHelperCallback
meth public abstract org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton$AntProjectHelperCallback.createHelper(org.openide.filesystems.FileObject,org.w3c.dom.Document,org.netbeans.spi.project.ProjectState,org.netbeans.spi.project.support.ant.AntBasedProjectType)
meth public abstract void org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton$AntProjectHelperCallback.save(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException
supr null
CLSS public static abstract interface org.netbeans.modules.project.ant.UserQuestionHandler$Callback
meth public abstract void org.netbeans.modules.project.ant.UserQuestionHandler$Callback.accepted()
meth public abstract void org.netbeans.modules.project.ant.UserQuestionHandler$Callback.denied()
meth public abstract void org.netbeans.modules.project.ant.UserQuestionHandler$Callback.error(java.io.IOException)
supr null
CLSS public static final org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
cons public RawReference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Properties) throws java.lang.IllegalArgumentException
cons public RawReference(java.lang.String,java.lang.String,java.net.URI,java.lang.String,java.lang.String,java.lang.String) throws java.lang.IllegalArgumentException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getArtifactType()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getCleanTargetName()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getForeignProjectName()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getID()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getScriptLocationValue()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getTargetName()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.toString()
meth public java.net.URI org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getScriptLocation()
meth public java.util.Properties org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getProperties()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.toAntArtifact(org.netbeans.spi.project.support.ant.ReferenceHelper)
supr java.lang.Object
CLSS public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
meth public abstract void org.openide.filesystems.FileSystem$AtomicAction.run() throws java.io.IOException
supr null
CLSS public static abstract interface org.openide.util.Mutex$Action
intf org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$Action.run()
supr null
CLSS public static abstract interface org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$ExceptionAction.run() throws java.lang.Exception
supr null
CLSS public abstract org.netbeans.api.project.ant.AntArtifact
cons protected AntArtifact()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.net.URI; org.netbeans.api.project.ant.AntArtifact.getArtifactLocations()
meth public abstract java.io.File org.netbeans.api.project.ant.AntArtifact.getScriptLocation()
meth public abstract java.lang.String org.netbeans.api.project.ant.AntArtifact.getCleanTargetName()
meth public abstract java.lang.String org.netbeans.api.project.ant.AntArtifact.getTargetName()
meth public abstract java.lang.String org.netbeans.api.project.ant.AntArtifact.getType()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final [Lorg.openide.filesystems.FileObject; org.netbeans.api.project.ant.AntArtifact.getArtifactFiles()
meth public final org.openide.filesystems.FileObject org.netbeans.api.project.ant.AntArtifact.getArtifactFile()
meth public final org.openide.filesystems.FileObject org.netbeans.api.project.ant.AntArtifact.getScriptFile()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.project.ant.AntArtifact.getID()
meth public java.net.URI org.netbeans.api.project.ant.AntArtifact.getArtifactLocation()
meth public java.util.Properties org.netbeans.api.project.ant.AntArtifact.getProperties()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.Project org.netbeans.api.project.ant.AntArtifact.getProject()
supr java.lang.Object
CLSS public org.netbeans.api.project.ant.AntArtifactQuery
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
meth public static [Lorg.netbeans.api.project.ant.AntArtifact; org.netbeans.api.project.ant.AntArtifactQuery.findArtifactsByType(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.ant.AntArtifact org.netbeans.api.project.ant.AntArtifactQuery.findArtifactByID(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.ant.AntArtifact org.netbeans.api.project.ant.AntArtifactQuery.findArtifactFromFile(java.io.File)
supr java.lang.Object
CLSS public final org.netbeans.api.project.ant.AntBuildExtender
innr public final org.netbeans.api.project.ant.AntBuildExtender$Extension
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.List org.netbeans.api.project.ant.AntBuildExtender.getExtensibleTargets()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized org.netbeans.api.project.ant.AntBuildExtender$Extension org.netbeans.api.project.ant.AntBuildExtender.addExtension(java.lang.String,org.openide.filesystems.FileObject)
meth public synchronized org.netbeans.api.project.ant.AntBuildExtender$Extension org.netbeans.api.project.ant.AntBuildExtender.getExtension(java.lang.String)
meth public synchronized void org.netbeans.api.project.ant.AntBuildExtender.removeExtension(java.lang.String)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactProvider
meth public abstract [Lorg.netbeans.api.project.ant.AntArtifact; org.netbeans.spi.project.ant.AntArtifactProvider.getBuildArtifacts()
supr null
CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactQueryImplementation
meth public abstract org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.ant.AntArtifactQueryImplementation.findArtifact(java.io.File)
supr null
CLSS public final org.netbeans.spi.project.ant.AntBuildExtenderFactory
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
meth public static org.netbeans.api.project.ant.AntBuildExtender org.netbeans.spi.project.ant.AntBuildExtenderFactory.createAntExtender(org.netbeans.spi.project.ant.AntBuildExtenderImplementation)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.ant.AntBuildExtenderImplementation
meth public abstract java.util.List org.netbeans.spi.project.ant.AntBuildExtenderImplementation.getExtensibleTargets()
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.ant.AntBuildExtenderImplementation.getOwningProject()
supr null
CLSS public abstract interface org.netbeans.spi.project.support.ant.AntBasedProjectType
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.AntBasedProjectType.getPrimaryConfigurationDataElementName(boolean)
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.AntBasedProjectType.getPrimaryConfigurationDataElementNamespace(boolean)
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.AntBasedProjectType.getType()
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.support.ant.AntBasedProjectType.createProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException
supr null
CLSS public final org.netbeans.spi.project.support.ant.AntProjectEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.AntProjectEvent.isExpected()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectEvent.getPath()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.spi.project.support.ant.AntProjectEvent.getHelper()
supr java.util.EventObject
CLSS public final org.netbeans.spi.project.support.ant.AntProjectHelper
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_PROPERTIES_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_XML_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_PROPERTIES_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_XML_PATH
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.File org.netbeans.spi.project.support.ant.AntProjectHelper.resolveFile(java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.resolvePath(java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.support.ant.AntProjectHelper.createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.AuxiliaryConfiguration org.netbeans.spi.project.support.ant.AntProjectHelper.createAuxiliaryConfiguration()
meth public org.netbeans.spi.project.CacheDirectoryProvider org.netbeans.spi.project.support.ant.AntProjectHelper.createCacheDirectoryProvider()
meth public org.netbeans.spi.project.support.ant.EditableProperties org.netbeans.spi.project.support.ant.AntProjectHelper.getProperties(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator org.netbeans.spi.project.support.ant.AntProjectHelper.getStandardPropertyEvaluator()
meth public org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.AntProjectHelper.getPropertyProvider(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.AntProjectHelper.getStockPropertyPreprovider()
meth public org.netbeans.spi.queries.FileBuiltQueryImplementation org.netbeans.spi.project.support.ant.AntProjectHelper.createGlobFileBuiltQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,[Ljava.lang.String;,[Ljava.lang.String;) throws java.lang.IllegalArgumentException
meth public org.netbeans.spi.queries.SharabilityQueryImplementation org.netbeans.spi.project.support.ant.AntProjectHelper.createSharabilityQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,[Ljava.lang.String;,[Ljava.lang.String;)
meth public org.openide.filesystems.FileObject org.netbeans.spi.project.support.ant.AntProjectHelper.getProjectDirectory()
meth public org.openide.filesystems.FileObject org.netbeans.spi.project.support.ant.AntProjectHelper.resolveFileObject(java.lang.String)
meth public org.w3c.dom.Element org.netbeans.spi.project.support.ant.AntProjectHelper.getPrimaryConfigurationData(boolean)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.addAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.notifyDeleted()
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.putPrimaryConfigurationData(org.w3c.dom.Element,boolean) throws java.lang.IllegalArgumentException
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.putProperties(java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.removeAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.support.ant.AntProjectListener
intf java.util.EventListener
meth public abstract void org.netbeans.spi.project.support.ant.AntProjectListener.configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public abstract void org.netbeans.spi.project.support.ant.AntProjectListener.propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
supr null
CLSS public final org.netbeans.spi.project.support.ant.EditableProperties
cons public EditableProperties()
cons public EditableProperties(boolean)
cons public EditableProperties(java.util.Map)
intf java.lang.Cloneable
intf java.util.Map
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.netbeans.spi.project.support.ant.EditableProperties.getComment(java.lang.String)
meth public boolean java.util.AbstractMap.containsKey(java.lang.Object)
meth public boolean java.util.AbstractMap.containsValue(java.lang.Object)
meth public boolean java.util.AbstractMap.equals(java.lang.Object)
meth public boolean java.util.AbstractMap.isEmpty()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int java.util.AbstractMap.hashCode()
meth public int java.util.AbstractMap.size()
meth public java.lang.Object java.util.AbstractMap.get(java.lang.Object)
meth public java.lang.Object java.util.AbstractMap.remove(java.lang.Object)
meth public java.lang.Object org.netbeans.spi.project.support.ant.EditableProperties.clone()
meth public java.lang.String java.util.AbstractMap.toString()
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.getProperty(java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.put(java.lang.String,java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.setProperty(java.lang.String,[Ljava.lang.String;)
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.setProperty(java.lang.String,java.lang.String)
meth public java.util.Collection java.util.AbstractMap.values()
meth public java.util.Set java.util.AbstractMap.keySet()
meth public java.util.Set org.netbeans.spi.project.support.ant.EditableProperties.entrySet()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.netbeans.spi.project.support.ant.EditableProperties org.netbeans.spi.project.support.ant.EditableProperties.cloneProperties()
meth public void java.util.AbstractMap.clear()
meth public void java.util.AbstractMap.putAll(java.util.Map)
meth public void org.netbeans.spi.project.support.ant.EditableProperties.load(java.io.InputStream) throws java.io.IOException
meth public void org.netbeans.spi.project.support.ant.EditableProperties.setComment(java.lang.String,[Ljava.lang.String;,boolean)
meth public void org.netbeans.spi.project.support.ant.EditableProperties.store(java.io.OutputStream) throws java.io.IOException
meth public volatile java.lang.Object org.netbeans.spi.project.support.ant.EditableProperties.put(java.lang.Object,java.lang.Object)
supr java.util.AbstractMap
CLSS public abstract org.netbeans.spi.project.support.ant.FilterPropertyProvider
cons protected FilterPropertyProvider(org.netbeans.spi.project.support.ant.PropertyProvider)
intf org.netbeans.spi.project.support.ant.PropertyProvider
meth protected final void org.netbeans.spi.project.support.ant.FilterPropertyProvider.setDelegate(org.netbeans.spi.project.support.ant.PropertyProvider)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.util.Map org.netbeans.spi.project.support.ant.FilterPropertyProvider.getProperties()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized final void org.netbeans.spi.project.support.ant.FilterPropertyProvider.addChangeListener(javax.swing.event.ChangeListener)
meth public synchronized final void org.netbeans.spi.project.support.ant.FilterPropertyProvider.removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.GeneratedFilesHelper
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper)
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.project.ant.AntBuildExtender)
cons public GeneratedFilesHelper(org.openide.filesystems.FileObject)
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_MISSING
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_MODIFIED
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_PROJECT_XML
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_STYLESHEET
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_UNKNOWN
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.GeneratedFilesHelper.BUILD_IMPL_XML_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.GeneratedFilesHelper.BUILD_XML_PATH
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.GeneratedFilesHelper.refreshBuildScript(java.lang.String,java.net.URL,boolean) throws java.io.IOException,java.lang.IllegalStateException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.getBuildScriptState(java.lang.String,java.net.URL) throws java.lang.IllegalStateException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.project.support.ant.GeneratedFilesHelper.generateBuildScriptFromStylesheet(java.lang.String,java.net.URL) throws java.io.IOException,java.lang.IllegalStateException
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.PathMatcher
cons public PathMatcher(java.lang.String,java.lang.String,java.io.File)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.PathMatcher.matches(java.lang.String,boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.spi.project.support.ant.PathMatcher.toString()
meth public java.util.Set org.netbeans.spi.project.support.ant.PathMatcher.findIncludedRoots() throws java.lang.IllegalArgumentException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public org.netbeans.spi.project.support.ant.ProjectGenerator
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
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.spi.project.support.ant.ProjectGenerator.createProject(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException,java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public abstract org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
cons protected ProjectXmlSavedHook()
meth protected abstract void org.netbeans.spi.project.support.ant.ProjectXmlSavedHook.projectXmlSaved() throws java.io.IOException
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
CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyEvaluator
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.PropertyEvaluator.evaluate(java.lang.String)
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.PropertyEvaluator.getProperty(java.lang.String)
meth public abstract java.util.Map org.netbeans.spi.project.support.ant.PropertyEvaluator.getProperties()
meth public abstract void org.netbeans.spi.project.support.ant.PropertyEvaluator.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.support.ant.PropertyEvaluator.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyProvider
meth public abstract java.util.Map org.netbeans.spi.project.support.ant.PropertyProvider.getProperties()
meth public abstract void org.netbeans.spi.project.support.ant.PropertyProvider.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.netbeans.spi.project.support.ant.PropertyProvider.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public org.netbeans.spi.project.support.ant.PropertyUtils
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
meth public static [Ljava.lang.String; org.netbeans.spi.project.support.ant.PropertyUtils.tokenizePath(java.lang.String)
meth public static boolean org.netbeans.spi.project.support.ant.PropertyUtils.isUsablePropertyName(java.lang.String)
meth public static java.io.File org.netbeans.spi.project.support.ant.PropertyUtils.resolveFile(java.io.File,java.lang.String) throws java.lang.IllegalArgumentException
meth public static java.lang.String org.netbeans.spi.project.support.ant.PropertyUtils.getUsablePropertyName(java.lang.String)
meth public static java.lang.String org.netbeans.spi.project.support.ant.PropertyUtils.relativizeFile(java.io.File,java.io.File)
meth public static org.netbeans.spi.project.support.ant.EditableProperties org.netbeans.spi.project.support.ant.PropertyUtils.getGlobalProperties()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.fixedPropertyProvider(java.util.Map)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.propertiesFilePropertyProvider(java.io.File)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.userPropertiesProvider(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.io.File)
meth public static synchronized org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.globalPropertyProvider()
meth public static transient org.netbeans.spi.project.support.ant.PropertyEvaluator org.netbeans.spi.project.support.ant.PropertyUtils.sequentialPropertyEvaluator(org.netbeans.spi.project.support.ant.PropertyProvider,[Lorg.netbeans.spi.project.support.ant.PropertyProvider;)
meth public static void org.netbeans.spi.project.support.ant.PropertyUtils.putGlobalProperties(org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.ReferenceHelper
cons public ReferenceHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.AuxiliaryConfiguration,org.netbeans.spi.project.support.ant.PropertyEvaluator)
innr public static final org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.Object; org.netbeans.spi.project.support.ant.ReferenceHelper.findArtifactAndLocation(java.lang.String)
meth public [Lorg.netbeans.spi.project.support.ant.ReferenceHelper$RawReference; org.netbeans.spi.project.support.ant.ReferenceHelper.getRawReferences()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.addRawReference(org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.addReference(org.netbeans.api.project.ant.AntArtifact) throws java.lang.IllegalArgumentException
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.destroyReference(java.lang.String)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.isReferenced(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.lang.IllegalArgumentException
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.removeRawReference(java.lang.String,java.lang.String)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.removeReference(java.lang.String)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.removeReference(java.lang.String,java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.addReference(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.lang.IllegalArgumentException
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createForeignFileReference(java.io.File,java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createForeignFileReference(org.netbeans.api.project.ant.AntArtifact) throws java.lang.IllegalArgumentException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.support.ant.ReferenceHelper.getForeignFileReferenceAsArtifact(java.lang.String)
meth public org.netbeans.spi.project.SubprojectProvider org.netbeans.spi.project.support.ant.ReferenceHelper.createSubprojectProvider()
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference org.netbeans.spi.project.support.ant.ReferenceHelper.getRawReference(java.lang.String,java.lang.String)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.addExtraBaseDirectory(java.lang.String)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.destroyForeignFileReference(java.lang.String)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.fixReferences(java.io.File)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.removeExtraBaseDirectory(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.SourcesHelper
cons public SourcesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
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
meth public org.netbeans.api.project.Sources org.netbeans.spi.project.support.ant.SourcesHelper.createSources()
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addNonSourceRoot(java.lang.String) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addOwnedFile(java.lang.String) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addPrincipalSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addPrincipalSourceRoot(java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.registerExternalRoots(int) throws java.lang.IllegalArgumentException,java.lang.IllegalStateException
supr java.lang.Object
CLSS public org.netbeans.spi.project.support.ant.ui.StoreGroup
cons public StoreGroup()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final javax.swing.JToggleButton$ToggleButtonModel org.netbeans.spi.project.support.ant.ui.StoreGroup.createInverseToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.JToggleButton$ToggleButtonModel org.netbeans.spi.project.support.ant.ui.StoreGroup.createToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.text.Document org.netbeans.spi.project.support.ant.ui.StoreGroup.createStringDocument(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.project.support.ant.ui.StoreGroup.store(org.netbeans.spi.project.support.ant.EditableProperties)
supr java.lang.Object
