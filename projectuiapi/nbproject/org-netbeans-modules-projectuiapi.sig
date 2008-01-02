#API master signature file
#Version 1.24.1
CLSS public static final org.netbeans.spi.project.ui.support.ProjectCustomizer$Category
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.netbeans.spi.project.ui.support.ProjectCustomizer$Category; org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.getSubcategories()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.isValid()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.awt.Image org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.getIcon()
meth public java.awt.event.ActionListener org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.getOkButtonListener()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.getDisplayName()
meth public java.lang.String org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.getErrorMessage()
meth public java.lang.String org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.getName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static transient org.netbeans.spi.project.ui.support.ProjectCustomizer$Category org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.create(java.lang.String,java.lang.String,java.awt.Image,[Lorg.netbeans.spi.project.ui.support.ProjectCustomizer$Category;)
meth public void org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.setErrorMessage(java.lang.String)
meth public void org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.setOkButtonListener(java.awt.event.ActionListener)
meth public void org.netbeans.spi.project.ui.support.ProjectCustomizer$Category.setValid(boolean)
supr java.lang.Object
CLSS public static abstract interface org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider
meth public abstract javax.swing.JComponent org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider.create(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category)
supr null
CLSS public static abstract interface org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
meth public abstract javax.swing.JComponent org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider.createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$Category org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider.createCategory(org.openide.util.Lookup)
supr null
CLSS public static org.openide.nodes.Children$Array
cons protected Array(java.util.Collection)
cons public Array()
fld  protected java.util.Collection org.openide.nodes.Children$Array.nodes
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
intf java.lang.Cloneable
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Array.refresh()
meth protected java.util.Collection org.openide.nodes.Children$Array.initCollection()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Array.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Array.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.nodes.Children$Array.clone()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
supr org.openide.nodes.Children
CLSS public static abstract org.openide.nodes.Children$Keys
cons public Keys()
fld  protected java.util.Collection org.openide.nodes.Children$Array.nodes
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
intf java.lang.Cloneable
meth protected abstract [Lorg.openide.nodes.Node; org.openide.nodes.Children$Keys.createNodes(java.lang.Object)
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Array.refresh()
meth protected final void org.openide.nodes.Children$Keys.refreshKey(java.lang.Object)
meth protected final void org.openide.nodes.Children$Keys.setBefore(boolean)
meth protected final void org.openide.nodes.Children$Keys.setKeys([Ljava.lang.Object;)
meth protected final void org.openide.nodes.Children$Keys.setKeys(java.util.Collection)
meth protected java.util.Collection org.openide.nodes.Children$Array.initCollection()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children$Keys.destroyNodes([Lorg.openide.nodes.Node;)
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Keys.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Keys.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.nodes.Children$Keys.clone()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
supr org.openide.nodes.Children$Array
CLSS public static org.openide.nodes.Children$Map
cons protected Map(java.util.Map)
cons public Map()
fld  protected java.util.Map org.openide.nodes.Children$Map.nodes
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Map.put(java.lang.Object,org.openide.nodes.Node)
meth protected final void org.openide.nodes.Children$Map.putAll(java.util.Map)
meth protected final void org.openide.nodes.Children$Map.refresh()
meth protected final void org.openide.nodes.Children$Map.refreshKey(java.lang.Object)
meth protected final void org.openide.nodes.Children$Map.removeAll(java.util.Collection)
meth protected java.lang.Object org.openide.nodes.Children.clone() throws java.lang.CloneNotSupportedException
meth protected java.util.Map org.openide.nodes.Children$Map.initMap()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children$Map.remove(java.lang.Object)
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Map.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Map.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
supr org.openide.nodes.Children
CLSS public static org.openide.nodes.Children$SortedArray
cons protected SortedArray(java.util.Collection)
cons public SortedArray()
fld  protected java.util.Collection org.openide.nodes.Children$Array.nodes
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
intf java.lang.Cloneable
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Array.refresh()
meth protected java.util.Collection org.openide.nodes.Children$Array.initCollection()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Array.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Array.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.nodes.Children$Array.clone()
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Comparator org.openide.nodes.Children$SortedArray.getComparator()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
meth public void org.openide.nodes.Children$SortedArray.setComparator(java.util.Comparator)
supr org.openide.nodes.Children$Array
CLSS public static org.openide.nodes.Children$SortedMap
cons protected SortedMap(java.util.Map)
cons public SortedMap()
fld  protected java.util.Map org.openide.nodes.Children$Map.nodes
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Map.put(java.lang.Object,org.openide.nodes.Node)
meth protected final void org.openide.nodes.Children$Map.putAll(java.util.Map)
meth protected final void org.openide.nodes.Children$Map.refresh()
meth protected final void org.openide.nodes.Children$Map.refreshKey(java.lang.Object)
meth protected final void org.openide.nodes.Children$Map.removeAll(java.util.Collection)
meth protected java.lang.Object org.openide.nodes.Children.clone() throws java.lang.CloneNotSupportedException
meth protected java.util.Map org.openide.nodes.Children$Map.initMap()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children$Map.remove(java.lang.Object)
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Map.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Map.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Comparator org.openide.nodes.Children$SortedMap.getComparator()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
meth public void org.openide.nodes.Children$SortedMap.setComparator(java.util.Comparator)
supr org.openide.nodes.Children$Map
CLSS public static abstract interface org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
supr null
CLSS public final org.netbeans.api.project.ui.OpenProjects
fld  constant public static final java.lang.String org.netbeans.api.project.ui.OpenProjects.PROPERTY_MAIN_PROJECT
fld  constant public static final java.lang.String org.netbeans.api.project.ui.OpenProjects.PROPERTY_OPEN_PROJECTS
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.netbeans.api.project.Project; org.netbeans.api.project.ui.OpenProjects.getOpenProjects()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.Project org.netbeans.api.project.ui.OpenProjects.getMainProject()
meth public static org.netbeans.api.project.ui.OpenProjects org.netbeans.api.project.ui.OpenProjects.getDefault()
meth public void org.netbeans.api.project.ui.OpenProjects.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.api.project.ui.OpenProjects.close([Lorg.netbeans.api.project.Project;)
meth public void org.netbeans.api.project.ui.OpenProjects.open([Lorg.netbeans.api.project.Project;,boolean)
meth public void org.netbeans.api.project.ui.OpenProjects.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.api.project.ui.OpenProjects.setMainProject(org.netbeans.api.project.Project) throws java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void org.netbeans.spi.project.ui.CustomizerProvider.showCustomizer()
supr null
CLSS public abstract interface org.netbeans.spi.project.ui.LogicalViewProvider
meth public abstract org.openide.nodes.Node org.netbeans.spi.project.ui.LogicalViewProvider.createLogicalView()
meth public abstract org.openide.nodes.Node org.netbeans.spi.project.ui.LogicalViewProvider.findPath(org.openide.nodes.Node,java.lang.Object)
supr null
CLSS public abstract interface org.netbeans.spi.project.ui.PrivilegedTemplates
meth public abstract [Ljava.lang.String; org.netbeans.spi.project.ui.PrivilegedTemplates.getPrivilegedTemplates()
supr null
CLSS public abstract org.netbeans.spi.project.ui.ProjectOpenedHook
cons protected ProjectOpenedHook()
meth protected abstract void org.netbeans.spi.project.ui.ProjectOpenedHook.projectClosed()
meth protected abstract void org.netbeans.spi.project.ui.ProjectOpenedHook.projectOpened()
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
CLSS public abstract interface org.netbeans.spi.project.ui.RecommendedTemplates
meth public abstract [Ljava.lang.String; org.netbeans.spi.project.ui.RecommendedTemplates.getRecommendedTypes()
supr null
CLSS public org.netbeans.spi.project.ui.support.CommonProjectActions
fld  constant public static final java.lang.String org.netbeans.spi.project.ui.support.CommonProjectActions.EXISTING_SOURCES_FOLDER
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
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.closeProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.copyProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.customizeProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.deleteProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.moveProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.newFileAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.newProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.openSubprojectsAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.renameProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.setAsMainProjectAction()
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.CommonProjectActions.setProjectConfigurationAction()
supr java.lang.Object
CLSS public final org.netbeans.spi.project.ui.support.DefaultProjectOperations
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
meth public static void org.netbeans.spi.project.ui.support.DefaultProjectOperations.performDefaultCopyOperation(org.netbeans.api.project.Project) throws java.lang.IllegalArgumentException
meth public static void org.netbeans.spi.project.ui.support.DefaultProjectOperations.performDefaultDeleteOperation(org.netbeans.api.project.Project) throws java.lang.IllegalArgumentException
meth public static void org.netbeans.spi.project.ui.support.DefaultProjectOperations.performDefaultMoveOperation(org.netbeans.api.project.Project) throws java.lang.IllegalArgumentException
meth public static void org.netbeans.spi.project.ui.support.DefaultProjectOperations.performDefaultRenameOperation(org.netbeans.api.project.Project,java.lang.String) throws java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public org.netbeans.spi.project.ui.support.FileSensitiveActions
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
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.FileSensitiveActions.fileCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
supr java.lang.Object
CLSS public org.netbeans.spi.project.ui.support.MainProjectSensitiveActions
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
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.MainProjectSensitiveActions.mainProjectCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.MainProjectSensitiveActions.mainProjectSensitiveAction(org.netbeans.spi.project.ui.support.ProjectActionPerformer,java.lang.String,javax.swing.Icon)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeFactory
meth public abstract org.netbeans.spi.project.ui.support.NodeList org.netbeans.spi.project.ui.support.NodeFactory.createNodes(org.netbeans.api.project.Project)
supr null
CLSS public org.netbeans.spi.project.ui.support.NodeFactorySupport
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
meth public static org.openide.nodes.Children org.netbeans.spi.project.ui.support.NodeFactorySupport.createCompositeChildren(org.netbeans.api.project.Project,java.lang.String)
meth public static transient org.netbeans.spi.project.ui.support.NodeList org.netbeans.spi.project.ui.support.NodeFactorySupport.fixedNodeList([Lorg.openide.nodes.Node;)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeList
meth public abstract java.util.List org.netbeans.spi.project.ui.support.NodeList.keys()
meth public abstract org.openide.nodes.Node org.netbeans.spi.project.ui.support.NodeList.node(java.lang.Object)
meth public abstract void org.netbeans.spi.project.ui.support.NodeList.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.netbeans.spi.project.ui.support.NodeList.addNotify()
meth public abstract void org.netbeans.spi.project.ui.support.NodeList.removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.netbeans.spi.project.ui.support.NodeList.removeNotify()
supr null
CLSS public abstract interface org.netbeans.spi.project.ui.support.ProjectActionPerformer
meth public abstract boolean org.netbeans.spi.project.ui.support.ProjectActionPerformer.enable(org.netbeans.api.project.Project)
meth public abstract void org.netbeans.spi.project.ui.support.ProjectActionPerformer.perform(org.netbeans.api.project.Project)
supr null
CLSS public org.netbeans.spi.project.ui.support.ProjectChooser
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
meth public static java.io.File org.netbeans.spi.project.ui.support.ProjectChooser.getProjectsFolder()
meth public static javax.swing.JFileChooser org.netbeans.spi.project.ui.support.ProjectChooser.projectChooser()
meth public static void org.netbeans.spi.project.ui.support.ProjectChooser.setProjectsFolder(java.io.File)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public static abstract interface org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider
innr public static abstract interface org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
innr public static final org.netbeans.spi.project.ui.support.ProjectCustomizer$Category
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
meth public static java.awt.Dialog org.netbeans.spi.project.ui.support.ProjectCustomizer.createCustomizerDialog([Lorg.netbeans.spi.project.ui.support.ProjectCustomizer$Category;,org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
meth public static java.awt.Dialog org.netbeans.spi.project.ui.support.ProjectCustomizer.createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
supr java.lang.Object
CLSS public org.netbeans.spi.project.ui.support.ProjectSensitiveActions
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
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.ProjectSensitiveActions.projectCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
meth public static javax.swing.Action org.netbeans.spi.project.ui.support.ProjectSensitiveActions.projectSensitiveAction(org.netbeans.spi.project.ui.support.ProjectActionPerformer,java.lang.String,javax.swing.Icon)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.ui.support.UILookupMergerSupport
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
meth public static org.netbeans.spi.project.LookupMerger org.netbeans.spi.project.ui.support.UILookupMergerSupport.createPrivilegedTemplatesMerger()
meth public static org.netbeans.spi.project.LookupMerger org.netbeans.spi.project.ui.support.UILookupMergerSupport.createProjectOpenHookMerger(org.netbeans.spi.project.ui.ProjectOpenedHook)
meth public static org.netbeans.spi.project.LookupMerger org.netbeans.spi.project.ui.support.UILookupMergerSupport.createRecommendedTemplatesMerger()
supr java.lang.Object
CLSS public org.netbeans.spi.project.ui.templates.support.Templates
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
meth public static java.lang.String org.netbeans.spi.project.ui.templates.support.Templates.getTargetName(org.openide.WizardDescriptor)
meth public static org.netbeans.api.project.Project org.netbeans.spi.project.ui.templates.support.Templates.getProject(org.openide.WizardDescriptor)
meth public static org.openide.WizardDescriptor$Panel org.netbeans.spi.project.ui.templates.support.Templates.createSimpleTargetChooser(org.netbeans.api.project.Project,[Lorg.netbeans.api.project.SourceGroup;)
meth public static org.openide.WizardDescriptor$Panel org.netbeans.spi.project.ui.templates.support.Templates.createSimpleTargetChooser(org.netbeans.api.project.Project,[Lorg.netbeans.api.project.SourceGroup;,org.openide.WizardDescriptor$Panel)
meth public static org.openide.filesystems.FileObject org.netbeans.spi.project.ui.templates.support.Templates.getExistingSourcesFolder(org.openide.WizardDescriptor)
meth public static org.openide.filesystems.FileObject org.netbeans.spi.project.ui.templates.support.Templates.getTargetFolder(org.openide.WizardDescriptor)
meth public static org.openide.filesystems.FileObject org.netbeans.spi.project.ui.templates.support.Templates.getTemplate(org.openide.WizardDescriptor)
meth public static void org.netbeans.spi.project.ui.templates.support.Templates.setTargetFolder(org.openide.WizardDescriptor,org.openide.filesystems.FileObject)
meth public static void org.netbeans.spi.project.ui.templates.support.Templates.setTargetName(org.openide.WizardDescriptor,java.lang.String)
supr java.lang.Object
