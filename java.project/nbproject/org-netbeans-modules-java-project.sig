#Signature file v4.0
#Version 

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
intf java.lang.annotation.Annotation

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

CLSS public org.netbeans.api.java.project.JavaProjectConstants
fld public final static java.lang.String ARTIFACT_TYPE_FOLDER = "folder"
fld public final static java.lang.String ARTIFACT_TYPE_JAR = "jar"
fld public final static java.lang.String COMMAND_DEBUG_FIX = "debug.fix"
fld public final static java.lang.String COMMAND_JAVADOC = "javadoc"
fld public final static java.lang.String SOURCES_TYPE_JAVA = "java"
fld public final static java.lang.String SOURCES_TYPE_RESOURCES = "resources"
supr java.lang.Object

CLSS public org.netbeans.api.java.project.classpath.ProjectClassPathModifier
meth public static boolean addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean addLibraries(org.netbeans.api.project.libraries.Library[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean addRoots(java.net.URL[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeLibraries(org.netbeans.api.project.libraries.Library[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeRoots(java.net.URL[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
supr java.lang.Object
hcls Extensible

CLSS public abstract interface org.netbeans.spi.java.project.classpath.ProjectClassPathExtender
 anno 0 java.lang.Deprecated()
meth public abstract boolean addAntArtifact(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public abstract boolean addArchiveFile(org.openide.filesystems.FileObject) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public abstract boolean addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
 anno 0 java.lang.Deprecated()

CLSS public abstract org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation
cons protected ProjectClassPathModifierImplementation()
meth protected abstract boolean addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean addLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean addRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract java.lang.String[] getExtensibleClassPathTypes(org.netbeans.api.project.SourceGroup)
meth protected abstract org.netbeans.api.project.SourceGroup[] getExtensibleSourceGroups()
supr java.lang.Object
hcls Accessor

CLSS public org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createPropertyBasedClassPathImplementation(java.io.File,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[])
supr java.lang.Object

CLSS public org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport
meth public static boolean isBroken(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String[],java.lang.String[])
meth public static void showAlert()
meth public static void showCustomizer(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String[],java.lang.String[])
supr java.lang.Object
hfds BROKEN_ALERT_TIMEOUT,brokenAlertLastTime,brokenAlertShown
hcls MainWindowListener

CLSS public org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer
cons public IncludeExcludeVisualizer()
meth public java.lang.String getExcludePattern()
meth public java.lang.String getIncludePattern()
meth public javax.swing.JComponent getVisualizerPanel()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setExcludePattern(java.lang.String)
meth public void setIncludePattern(java.lang.String)
meth public void setRoots(java.io.File[])
supr java.lang.Object
hfds DELAY,GRANULARITY,RP,busy,excluded,excludes,included,includes,interrupted,listeners,panel,roots,scanCounter,task
hcls RecalculateTask

CLSS public abstract interface org.netbeans.spi.java.project.support.ui.PackageRenameHandler
meth public abstract void handleRename(org.openide.nodes.Node,java.lang.String)

CLSS public org.netbeans.spi.java.project.support.ui.PackageView
meth public static javax.swing.ComboBoxModel createListView(org.netbeans.api.project.SourceGroup)
meth public static javax.swing.ListCellRenderer listRenderer()
meth public static org.openide.nodes.Node createPackageView(org.netbeans.api.project.SourceGroup)
meth public static org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)
supr java.lang.Object
hcls PackageItem,PackageListCellRenderer,RootNode

CLSS public org.netbeans.spi.java.project.support.ui.templates.JavaTemplates
meth public static org.openide.WizardDescriptor$InstantiatingIterator createJavaTemplateIterator()
meth public static org.openide.WizardDescriptor$Panel createPackageChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[])
meth public static org.openide.WizardDescriptor$Panel createPackageChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[],org.openide.WizardDescriptor$Panel)
meth public static org.openide.WizardDescriptor$Panel createPackageChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[],org.openide.WizardDescriptor$Panel,boolean)
supr java.lang.Object

