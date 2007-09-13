#Signature file v4.0
#Version 

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected AbstractMap()
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds keySet,values
hcls SimpleEntry

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)

CLSS public abstract org.netbeans.api.project.ant.AntArtifact
cons protected AntArtifact()
meth public abstract java.io.File getScriptLocation()
meth public abstract java.lang.String getCleanTargetName()
meth public abstract java.lang.String getTargetName()
meth public abstract java.lang.String getType()
meth public final org.openide.filesystems.FileObject getArtifactFile()
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileObject getScriptFile()
meth public final org.openide.filesystems.FileObject[] getArtifactFiles()
meth public java.lang.String getID()
meth public java.net.URI getArtifactLocation()
 anno 0 java.lang.Deprecated()
meth public java.net.URI[] getArtifactLocations()
meth public java.util.Properties getProperties()
meth public org.netbeans.api.project.Project getProject()
supr java.lang.Object
hfds PROPS,warnedClasses

CLSS public org.netbeans.api.project.ant.AntArtifactQuery
meth public static org.netbeans.api.project.ant.AntArtifact findArtifactByID(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.ant.AntArtifact findArtifactFromFile(java.io.File)
meth public static org.netbeans.api.project.ant.AntArtifact[] findArtifactsByType(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.project.ant.AntBuildExtender
innr public final Extension
meth public java.util.List<java.lang.String> getExtensibleTargets()
meth public org.netbeans.api.project.ant.AntBuildExtender$Extension addExtension(java.lang.String,org.openide.filesystems.FileObject)
meth public org.netbeans.api.project.ant.AntBuildExtender$Extension getExtension(java.lang.String)
meth public void removeExtension(java.lang.String)
supr java.lang.Object
hfds db,extensions,implementation

CLSS public final org.netbeans.api.project.ant.AntBuildExtender$Extension
meth public void addDependency(java.lang.String,java.lang.String)
meth public void removeDependency(java.lang.String,java.lang.String)
supr java.lang.Object
hfds dependencies,file,id,path

CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactProvider
meth public abstract org.netbeans.api.project.ant.AntArtifact[] getBuildArtifacts()

CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactQueryImplementation
meth public abstract org.netbeans.api.project.ant.AntArtifact findArtifact(java.io.File)

CLSS public final org.netbeans.spi.project.ant.AntBuildExtenderFactory
meth public static org.netbeans.api.project.ant.AntBuildExtender createAntExtender(org.netbeans.spi.project.ant.AntBuildExtenderImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ant.AntBuildExtenderImplementation
meth public abstract java.util.List<java.lang.String> getExtensibleTargets()
meth public abstract org.netbeans.api.project.Project getOwningProject()

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntBasedProjectType
meth public abstract java.lang.String getPrimaryConfigurationDataElementName(boolean)
meth public abstract java.lang.String getPrimaryConfigurationDataElementNamespace(boolean)
meth public abstract java.lang.String getType()
meth public abstract org.netbeans.api.project.Project createProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException

CLSS public final org.netbeans.spi.project.support.ant.AntProjectEvent
meth public boolean isExpected()
meth public java.lang.String getPath()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getHelper()
supr java.util.EventObject
hfds expected,path

CLSS public final org.netbeans.spi.project.support.ant.AntProjectHelper
fld public final static java.lang.String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"
fld public final static java.lang.String PRIVATE_XML_PATH = "nbproject/private/private.xml"
fld public final static java.lang.String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"
fld public final static java.lang.String PROJECT_XML_PATH = "nbproject/project.xml"
meth public java.io.File resolveFile(java.lang.String)
meth public java.lang.String resolvePath(java.lang.String)
meth public java.lang.String toString()
meth public org.netbeans.api.project.ant.AntArtifact createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.AuxiliaryConfiguration createAuxiliaryConfiguration()
meth public org.netbeans.spi.project.CacheDirectoryProvider createCacheDirectoryProvider()
meth public org.netbeans.spi.project.support.ant.EditableProperties getProperties(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator getStandardPropertyEvaluator()
meth public org.netbeans.spi.project.support.ant.PropertyProvider getPropertyProvider(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyProvider getStockPropertyPreprovider()
meth public org.netbeans.spi.queries.FileBuiltQueryImplementation createGlobFileBuiltQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public org.netbeans.spi.queries.SharabilityQueryImplementation createSharabilityQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public org.openide.filesystems.FileObject getProjectDirectory()
meth public org.openide.filesystems.FileObject resolveFileObject(java.lang.String)
meth public org.w3c.dom.Element getPrimaryConfigurationData(boolean)
meth public void addAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void notifyDeleted()
meth public void putPrimaryConfigurationData(org.w3c.dom.Element,boolean)
meth public void putProperties(java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties)
meth public void removeAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
supr java.lang.Object
hfds PRIVATE_NS,PROJECT_NS,QUIETLY_SWALLOW_XML_LOAD_ERRORS,RP,db,dir,fileListener,listeners,modifiedMetadataPaths,pendingHook,pendingHookCount,privateXml,projectXml,properties,state,type,writingXML
hcls FileListener

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntProjectListener
intf java.util.EventListener
meth public abstract void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public abstract void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)

CLSS public final org.netbeans.spi.project.support.ant.EditableProperties
cons public EditableProperties()
cons public EditableProperties(boolean)
cons public EditableProperties(java.util.Map<java.lang.String,java.lang.String>)
intf java.lang.Cloneable
meth public java.lang.Object clone()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String[])
meth public java.lang.String[] getComment(java.lang.String)
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public org.netbeans.spi.project.support.ant.EditableProperties cloneProperties()
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void setComment(java.lang.String,java.lang.String[],boolean)
meth public void store(java.io.OutputStream) throws java.io.IOException
supr java.util.AbstractMap<java.lang.String,java.lang.String>
hfds INDENT,READING_KEY_VALUE,WAITING_FOR_KEY_VALUE,alphabetize,commentChars,itemIndex,items,keyValueSeparators,strictKeyValueSeparators,whiteSpaceChars
hcls Item,IteratorImpl,MapEntryImpl,SetImpl

CLSS public abstract org.netbeans.spi.project.support.ant.FilterPropertyProvider
cons protected FilterPropertyProvider(org.netbeans.spi.project.support.ant.PropertyProvider)
intf org.netbeans.spi.project.support.ant.PropertyProvider
meth protected final void setDelegate(org.netbeans.spi.project.support.ant.PropertyProvider)
meth public final java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds cs,delegate,strongListener,weakListener

CLSS public final org.netbeans.spi.project.support.ant.GeneratedFilesHelper
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper)
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.project.ant.AntBuildExtender)
cons public GeneratedFilesHelper(org.openide.filesystems.FileObject)
fld public final static int FLAG_MISSING = 2
fld public final static int FLAG_MODIFIED = 4
fld public final static int FLAG_OLD_PROJECT_XML = 8
fld public final static int FLAG_OLD_STYLESHEET = 16
fld public final static int FLAG_UNKNOWN = 32
fld public final static java.lang.String BUILD_IMPL_XML_PATH = "nbproject/build-impl.xml"
fld public final static java.lang.String BUILD_XML_PATH = "build.xml"
meth public boolean refreshBuildScript(java.lang.String,java.net.URL,boolean) throws java.io.IOException
meth public int getBuildScriptState(java.lang.String,java.net.URL)
meth public void generateBuildScriptFromStylesheet(java.lang.String,java.net.URL) throws java.io.IOException
supr java.lang.Object
hfds GENFILES_PROPERTIES_PATH,KEY_SUFFIX_DATA_CRC,KEY_SUFFIX_SCRIPT_CRC,KEY_SUFFIX_STYLESHEET_CRC,crcCache,crcCacheTimestampsXorSizes,dir,extender,h
hcls EolFilterOutputStream

CLSS public final org.netbeans.spi.project.support.ant.PathMatcher
cons public PathMatcher(java.lang.String,java.lang.String,java.io.File)
meth public boolean matches(java.lang.String,boolean)
meth public java.lang.String toString()
meth public java.util.Set<java.io.File> findIncludedRoots()
supr java.lang.Object
hfds base,excludePattern,excludes,includePattern,includes,knownIncludes

CLSS public org.netbeans.spi.project.support.ant.ProjectGenerator
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
cons protected ProjectXmlSavedHook()
meth protected abstract void projectXmlSaved() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyEvaluator
meth public abstract java.lang.String evaluate(java.lang.String)
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.spi.project.support.ant.PropertyUtils
meth public !varargs static org.netbeans.spi.project.support.ant.PropertyEvaluator sequentialPropertyEvaluator(org.netbeans.spi.project.support.ant.PropertyProvider,org.netbeans.spi.project.support.ant.PropertyProvider[])
meth public static boolean isUsablePropertyName(java.lang.String)
meth public static java.io.File resolveFile(java.io.File,java.lang.String)
meth public static java.lang.String getUsablePropertyName(java.lang.String)
meth public static java.lang.String relativizeFile(java.io.File,java.io.File)
meth public static java.lang.String[] tokenizePath(java.lang.String)
meth public static org.netbeans.spi.project.support.ant.EditableProperties getGlobalProperties()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider fixedPropertyProvider(java.util.Map<java.lang.String,java.lang.String>)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider globalPropertyProvider()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider propertiesFilePropertyProvider(java.io.File)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider userPropertiesProvider(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.io.File)
meth public static void putGlobalProperties(org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object
hfds RELATIVE_SLASH_SEPARATED_PATH,VALID_PROPERTY_NAME,globalPropertyProviders
hcls FilePropertyProvider,FixedPropertyProvider,SequentialPropertyEvaluator,UserPropertiesProvider

CLSS public final org.netbeans.spi.project.support.ant.ReferenceHelper
cons public ReferenceHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.AuxiliaryConfiguration,org.netbeans.spi.project.support.ant.PropertyEvaluator)
innr public final static RawReference
meth public boolean addRawReference(org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference)
meth public boolean addReference(org.netbeans.api.project.ant.AntArtifact)
 anno 0 java.lang.Deprecated()
meth public boolean destroyReference(java.lang.String)
meth public boolean isReferenced(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public boolean removeRawReference(java.lang.String,java.lang.String)
meth public boolean removeReference(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public boolean removeReference(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object[] findArtifactAndLocation(java.lang.String)
meth public java.lang.String addReference(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public java.lang.String createForeignFileReference(java.io.File,java.lang.String)
meth public java.lang.String createForeignFileReference(org.netbeans.api.project.ant.AntArtifact)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.project.ant.AntArtifact getForeignFileReferenceAsArtifact(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.spi.project.SubprojectProvider createSubprojectProvider()
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference getRawReference(java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference[] getRawReferences()
meth public void addExtraBaseDirectory(java.lang.String)
meth public void destroyForeignFileReference(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void fixReferences(java.io.File)
meth public void removeExtraBaseDirectory(java.lang.String)
supr java.lang.Object
hfds FOREIGN_FILE_REFERENCE,FOREIGN_FILE_REFERENCE_OLD,FOREIGN_PLAIN_FILE_REFERENCE,REFS_NAME,REFS_NS,REFS_NS2,REF_NAME,aux,eval,extraBaseDirectories,h

CLSS public final static org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
cons public RawReference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Properties)
cons public RawReference(java.lang.String,java.lang.String,java.net.URI,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getArtifactType()
meth public java.lang.String getCleanTargetName()
meth public java.lang.String getForeignProjectName()
meth public java.lang.String getID()
meth public java.lang.String getScriptLocationValue()
meth public java.lang.String getTargetName()
meth public java.lang.String toString()
meth public java.net.URI getScriptLocation()
 anno 0 java.lang.Deprecated()
meth public java.util.Properties getProperties()
meth public org.netbeans.api.project.ant.AntArtifact toAntArtifact(org.netbeans.spi.project.support.ant.ReferenceHelper)
supr java.lang.Object
hfds SUB_ELEMENT_NAMES,artifactID,artifactType,cleanTargetName,foreignProjectName,newScriptLocation,props,scriptLocation,targetName

CLSS public final org.netbeans.spi.project.support.ant.SourcesHelper
cons public SourcesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public org.netbeans.api.project.Sources createSources()
meth public void addNonSourceRoot(java.lang.String)
meth public void addPrincipalSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void addPrincipalSourceRoot(java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void registerExternalRoots(int)
supr java.lang.Object
hfds evaluator,lastRegisteredRoots,nonSourceRoots,principalSourceRoots,project,propChangeL,registeredRootAlgorithm,typedSourceRoots
hcls PropChangeL,Root,SourceRoot,SourcesImpl,TypedSourceRoot

CLSS public org.netbeans.spi.project.support.ant.ui.StoreGroup
cons public StoreGroup()
meth public final javax.swing.JToggleButton$ToggleButtonModel createInverseToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.JToggleButton$ToggleButtonModel createToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.text.Document createStringDocument(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public void store(org.netbeans.spi.project.support.ant.EditableProperties)
supr java.lang.Object
hfds BOOLEAN_KIND_ED,BOOLEAN_KIND_TF,BOOLEAN_KIND_YN,documentListener,models,modifiedDocuments

