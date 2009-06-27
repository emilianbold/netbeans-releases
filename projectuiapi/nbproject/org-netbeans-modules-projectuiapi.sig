#Signature file v4.0
#Version 1.35.1

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

CLSS public final org.netbeans.api.project.ui.OpenProjects
fld public final static java.lang.String PROPERTY_MAIN_PROJECT = "MainProject"
fld public final static java.lang.String PROPERTY_OPEN_PROJECTS = "openProjects"
meth public boolean isProjectOpen(org.netbeans.api.project.Project)
meth public java.util.concurrent.Future<org.netbeans.api.project.Project[]> openProjects()
meth public org.netbeans.api.project.Project getMainProject()
meth public org.netbeans.api.project.Project[] getOpenProjects()
meth public static org.netbeans.api.project.ui.OpenProjects getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void close(org.netbeans.api.project.Project[])
meth public void open(org.netbeans.api.project.Project[],boolean)
meth public void open(org.netbeans.api.project.Project[],boolean,boolean)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setMainProject(org.netbeans.api.project.Project)
supr java.lang.Object
hfds INSTANCE,trampoline

CLSS public abstract interface org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void showCustomizer()

CLSS public abstract interface org.netbeans.spi.project.ui.LogicalViewProvider
meth public abstract org.openide.nodes.Node createLogicalView()
meth public abstract org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)

CLSS public abstract interface org.netbeans.spi.project.ui.PrivilegedTemplates
meth public abstract java.lang.String[] getPrivilegedTemplates()

CLSS public abstract org.netbeans.spi.project.ui.ProjectOpenedHook
cons protected ProjectOpenedHook()
meth protected abstract void projectClosed()
meth protected abstract void projectOpened()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.RecommendedTemplates
meth public abstract java.lang.String[] getRecommendedTypes()

CLSS public final org.netbeans.spi.project.ui.support.BuildExecutionSupport
innr public abstract interface static Item
meth public static void registerFinishedItem(org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item)
meth public static void registerRunningItem(org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item
meth public abstract boolean isRunning()
meth public abstract java.lang.String getDisplayName()
meth public abstract void repeatExecution()
meth public abstract void stopRunning()

CLSS public org.netbeans.spi.project.ui.support.CommonProjectActions
fld public final static java.lang.String EXISTING_SOURCES_FOLDER = "existingSourcesFolder"
meth public static javax.swing.Action closeProjectAction()
meth public static javax.swing.Action copyProjectAction()
meth public static javax.swing.Action customizeProjectAction()
meth public static javax.swing.Action deleteProjectAction()
meth public static javax.swing.Action moveProjectAction()
meth public static javax.swing.Action newFileAction()
meth public static javax.swing.Action newProjectAction()
meth public static javax.swing.Action openSubprojectsAction()
meth public static javax.swing.Action renameProjectAction()
meth public static javax.swing.Action setAsMainProjectAction()
meth public static javax.swing.Action setProjectConfigurationAction()
supr java.lang.Object

CLSS public final org.netbeans.spi.project.ui.support.DefaultProjectOperations
meth public static void performDefaultCopyOperation(org.netbeans.api.project.Project)
meth public static void performDefaultDeleteOperation(org.netbeans.api.project.Project)
meth public static void performDefaultMoveOperation(org.netbeans.api.project.Project)
meth public static void performDefaultRenameOperation(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.spi.project.ui.support.FileSensitiveActions
meth public static javax.swing.Action fileCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
supr java.lang.Object

CLSS public org.netbeans.spi.project.ui.support.MainProjectSensitiveActions
meth public static javax.swing.Action mainProjectCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
meth public static javax.swing.Action mainProjectSensitiveAction(org.netbeans.spi.project.ui.support.ProjectActionPerformer,java.lang.String,javax.swing.Icon)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeFactory
innr public abstract interface static !annotation Registration
meth public abstract org.netbeans.spi.project.ui.support.NodeList<?> createNodes(org.netbeans.api.project.Project)

CLSS public abstract interface static !annotation org.netbeans.spi.project.ui.support.NodeFactory$Registration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String[] projectType()

CLSS public org.netbeans.spi.project.ui.support.NodeFactorySupport
meth public !varargs static org.netbeans.spi.project.ui.support.NodeList fixedNodeList(org.openide.nodes.Node[])
meth public static org.openide.nodes.Children createCompositeChildren(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object
hcls DelegateChildren,FixedNodeList,NodeListKeyWrapper

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeList<%0 extends java.lang.Object>
meth public abstract java.util.List<{org.netbeans.spi.project.ui.support.NodeList%0}> keys()
meth public abstract org.openide.nodes.Node node({org.netbeans.spi.project.ui.support.NodeList%0})
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addNotify()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeNotify()

CLSS public abstract interface org.netbeans.spi.project.ui.support.ProjectActionPerformer
meth public abstract boolean enable(org.netbeans.api.project.Project)
meth public abstract void perform(org.netbeans.api.project.Project)

CLSS public org.netbeans.spi.project.ui.support.ProjectChooser
meth public static java.io.File getProjectsFolder()
meth public static javax.swing.JFileChooser projectChooser()
meth public static void setProjectsFolder(java.io.File)
supr java.lang.Object

CLSS public final org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static CategoryComponentProvider
innr public abstract interface static CompositeCategoryProvider
innr public final static Category
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
supr java.lang.Object
hcls DelegateCategoryProvider

CLSS public final static org.netbeans.spi.project.ui.support.ProjectCustomizer$Category
meth public !varargs static org.netbeans.spi.project.ui.support.ProjectCustomizer$Category create(java.lang.String,java.lang.String,java.awt.Image,org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[])
meth public boolean isValid()
meth public java.awt.Image getIcon()
meth public java.awt.event.ActionListener getOkButtonListener()
meth public java.awt.event.ActionListener getStoreListener()
meth public java.lang.String getDisplayName()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getName()
meth public org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[] getSubcategories()
meth public void setErrorMessage(java.lang.String)
meth public void setOkButtonListener(java.awt.event.ActionListener)
meth public void setStoreListener(java.awt.event.ActionListener)
meth public void setValid(boolean)
supr java.lang.Object
hfds displayName,errorMessage,icon,name,okListener,storeListener,subcategories,valid

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider
meth public abstract javax.swing.JComponent create(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category)

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
meth public abstract javax.swing.JComponent createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$Category createCategory(org.openide.util.Lookup)

CLSS public org.netbeans.spi.project.ui.support.ProjectSensitiveActions
meth public static javax.swing.Action projectCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
meth public static javax.swing.Action projectSensitiveAction(org.netbeans.spi.project.ui.support.ProjectActionPerformer,java.lang.String,javax.swing.Icon)
supr java.lang.Object

CLSS public final org.netbeans.spi.project.ui.support.UILookupMergerSupport
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.PrivilegedTemplates> createPrivilegedTemplatesMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.ProjectOpenedHook> createProjectOpenHookMerger(org.netbeans.spi.project.ui.ProjectOpenedHook)
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.RecommendedTemplates> createRecommendedTemplatesMerger()
supr java.lang.Object
hcls OpenHookImpl,OpenMerger,PrivilegedMerger,PrivilegedTemplatesImpl,RecommendedMerger,RecommendedTemplatesImpl

CLSS public org.netbeans.spi.project.ui.templates.support.Templates
meth public static java.lang.String getTargetName(org.openide.WizardDescriptor)
meth public static org.netbeans.api.project.Project getProject(org.openide.WizardDescriptor)
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createSimpleTargetChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[])
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createSimpleTargetChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[],org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>)
meth public static org.openide.filesystems.FileObject getExistingSourcesFolder(org.openide.WizardDescriptor)
meth public static org.openide.filesystems.FileObject getTargetFolder(org.openide.WizardDescriptor)
meth public static org.openide.filesystems.FileObject getTemplate(org.openide.WizardDescriptor)
meth public static void setTargetFolder(org.openide.WizardDescriptor,org.openide.filesystems.FileObject)
meth public static void setTargetName(org.openide.WizardDescriptor,java.lang.String)
supr java.lang.Object

