#API master signature file
#Version 7.2.1
CLSS public static abstract org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable
cons public ActionRunnable(java.awt.event.ActionEvent,org.openide.util.actions.SystemAction,boolean)
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
intf java.awt.event.ActionListener
intf java.util.EventListener
intf javax.swing.Action
meth protected abstract void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.run()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final boolean org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.isEnabled()
meth public final boolean org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.needsToBeSynchronous()
meth public final java.lang.Object org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.getValue(java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.actionPerformed(java.awt.event.ActionEvent)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.doRun()
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.putValue(java.lang.String,java.lang.Object)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.setEnabled(boolean)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static org.openide.cookies.ConnectionCookie$Event
cons public Event(org.openide.nodes.Node,org.openide.cookies.ConnectionCookie$Type)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.cookies.ConnectionCookie$Type org.openide.cookies.ConnectionCookie$Event.getType()
meth public org.openide.nodes.Node org.openide.cookies.ConnectionCookie$Event.getNode()
supr java.util.EventObject
CLSS public static abstract interface org.openide.cookies.ConnectionCookie$Listener
intf java.util.EventListener
intf org.openide.nodes.Node$Cookie
meth public abstract void org.openide.cookies.ConnectionCookie$Listener.notify(org.openide.cookies.ConnectionCookie$Event) throws java.lang.ClassCastException,java.lang.IllegalArgumentException
supr null
CLSS public static abstract interface org.openide.cookies.ConnectionCookie$Type
intf java.io.Serializable
meth public abstract boolean org.openide.cookies.ConnectionCookie$Type.isPersistent()
meth public abstract boolean org.openide.cookies.ConnectionCookie$Type.overlaps(org.openide.cookies.ConnectionCookie$Type)
meth public abstract java.lang.Class org.openide.cookies.ConnectionCookie$Type.getEventClass()
supr null
CLSS public static abstract interface org.openide.cookies.InstanceCookie$Of
innr public static abstract interface org.openide.cookies.InstanceCookie$Of
intf org.openide.cookies.InstanceCookie
intf org.openide.nodes.Node$Cookie
meth public abstract boolean org.openide.cookies.InstanceCookie$Of.instanceOf(java.lang.Class)
meth public abstract java.lang.Class org.openide.cookies.InstanceCookie.instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.Object org.openide.cookies.InstanceCookie.instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.String org.openide.cookies.InstanceCookie.instanceName()
supr null
CLSS public static abstract interface org.openide.nodes.BeanChildren$Factory
meth public abstract org.openide.nodes.Node org.openide.nodes.BeanChildren$Factory.createNode(java.lang.Object) throws java.beans.IntrospectionException
supr null
CLSS public static final org.openide.nodes.BeanNode$Descriptor
fld  public final [Lorg.openide.nodes.Node$Property; org.openide.nodes.BeanNode$Descriptor.expert
fld  public final [Lorg.openide.nodes.Node$Property; org.openide.nodes.BeanNode$Descriptor.hidden
fld  public final [Lorg.openide.nodes.Node$Property; org.openide.nodes.BeanNode$Descriptor.property
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
CLSS public static abstract interface org.openide.nodes.CookieSet$Before
meth public abstract void org.openide.nodes.CookieSet$Before.beforeLookup(java.lang.Class)
supr null
CLSS public static abstract interface org.openide.nodes.CookieSet$Factory
meth public abstract org.openide.nodes.Node$Cookie org.openide.nodes.CookieSet$Factory.createCookie(java.lang.Class)
supr null
CLSS public static org.openide.nodes.FilterNode$Children
cons public Children(org.openide.nodes.Node)
fld  protected java.util.Collection org.openide.nodes.Children$Array.nodes
fld  protected org.openide.nodes.Node org.openide.nodes.FilterNode$Children.original
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
intf java.lang.Cloneable
meth protected [Lorg.openide.nodes.Node; org.openide.nodes.FilterNode$Children.createNodes(org.openide.nodes.Node)
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Array.refresh()
meth protected final void org.openide.nodes.Children$Keys.refreshKey(java.lang.Object)
meth protected final void org.openide.nodes.Children$Keys.setBefore(boolean)
meth protected final void org.openide.nodes.Children$Keys.setKeys([Ljava.lang.Object;)
meth protected final void org.openide.nodes.Children$Keys.setKeys(java.util.Collection)
meth protected final void org.openide.nodes.FilterNode$Children.changeOriginal(org.openide.nodes.Node)
meth protected java.util.Collection org.openide.nodes.Children$Array.initCollection()
meth protected org.openide.nodes.Node org.openide.nodes.FilterNode$Children.copyNode(org.openide.nodes.Node)
meth protected void org.openide.nodes.Children$Keys.destroyNodes([Lorg.openide.nodes.Node;)
meth protected void org.openide.nodes.FilterNode$Children.addNotify()
meth protected void org.openide.nodes.FilterNode$Children.filterChildrenAdded(org.openide.nodes.NodeMemberEvent)
meth protected void org.openide.nodes.FilterNode$Children.filterChildrenRemoved(org.openide.nodes.NodeMemberEvent)
meth protected void org.openide.nodes.FilterNode$Children.filterChildrenReordered(org.openide.nodes.NodeReorderEvent)
meth protected void org.openide.nodes.FilterNode$Children.finalize()
meth protected void org.openide.nodes.FilterNode$Children.removeNotify()
meth protected volatile [Lorg.openide.nodes.Node; org.openide.nodes.FilterNode$Children.createNodes(java.lang.Object)
meth public [Lorg.openide.nodes.Node; org.openide.nodes.FilterNode$Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.FilterNode$Children.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.FilterNode$Children.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.nodes.FilterNode$Children.clone()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.FilterNode$Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
supr org.openide.nodes.Children$Keys
CLSS protected static org.openide.nodes.FilterNode$NodeAdapter
cons public NodeAdapter(org.openide.nodes.FilterNode)
intf java.beans.PropertyChangeListener
intf java.util.EventListener
intf org.openide.nodes.NodeListener
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.FilterNode$NodeAdapter.propertyChange(org.openide.nodes.FilterNode,java.beans.PropertyChangeEvent)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.FilterNode$NodeAdapter.nodeDestroyed(org.openide.nodes.NodeEvent)
meth public final void org.openide.nodes.FilterNode$NodeAdapter.propertyChange(java.beans.PropertyChangeEvent)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.nodes.FilterNode$NodeAdapter.childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public void org.openide.nodes.FilterNode$NodeAdapter.childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public void org.openide.nodes.FilterNode$NodeAdapter.childrenReordered(org.openide.nodes.NodeReorderEvent)
supr java.lang.Object
CLSS protected static org.openide.nodes.FilterNode$PropertyChangeAdapter
cons public PropertyChangeAdapter(org.openide.nodes.FilterNode)
intf java.beans.PropertyChangeListener
intf java.util.EventListener
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.FilterNode$PropertyChangeAdapter.propertyChange(org.openide.nodes.FilterNode,java.beans.PropertyChangeEvent)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.FilterNode$PropertyChangeAdapter.propertyChange(java.beans.PropertyChangeEvent)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static org.openide.nodes.Index$ArrayChildren
cons public ArrayChildren()
fld  protected java.util.Collection org.openide.nodes.Children$Array.nodes
fld  protected org.openide.nodes.Index org.openide.nodes.Index$ArrayChildren.support
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static abstract org.openide.nodes.Index$KeysChildren
innr public static abstract org.openide.nodes.Index$Support
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
innr public static org.openide.nodes.Index$ArrayChildren
intf java.lang.Cloneable
intf org.openide.nodes.Index
intf org.openide.nodes.Node$Cookie
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Array.refresh()
meth protected java.util.List org.openide.nodes.Index$ArrayChildren.initCollection()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth protected volatile java.util.Collection org.openide.nodes.Index$ArrayChildren.initCollection()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Array.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Array.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Index$ArrayChildren.indexOf(org.openide.nodes.Node)
meth public java.lang.Object org.openide.nodes.Children$Array.clone()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
meth public void org.openide.nodes.Index$ArrayChildren.addChangeListener(javax.swing.event.ChangeListener)
meth public void org.openide.nodes.Index$ArrayChildren.exchange(int,int)
meth public void org.openide.nodes.Index$ArrayChildren.move(int,int)
meth public void org.openide.nodes.Index$ArrayChildren.moveDown(int)
meth public void org.openide.nodes.Index$ArrayChildren.moveUp(int)
meth public void org.openide.nodes.Index$ArrayChildren.removeChangeListener(javax.swing.event.ChangeListener)
meth public void org.openide.nodes.Index$ArrayChildren.reorder()
meth public void org.openide.nodes.Index$ArrayChildren.reorder([I)
supr org.openide.nodes.Children$Array
CLSS public static abstract org.openide.nodes.Index$KeysChildren
cons public KeysChildren(java.util.List)
fld  protected final java.util.List org.openide.nodes.Index$KeysChildren.list
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
meth protected java.lang.Object org.openide.nodes.Index$KeysChildren.lock()
meth protected java.util.Collection org.openide.nodes.Children$Array.initCollection()
meth protected org.openide.nodes.Index org.openide.nodes.Index$KeysChildren.createIndex()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children$Keys.destroyNodes([Lorg.openide.nodes.Node;)
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth protected void org.openide.nodes.Index$KeysChildren.reorder([I)
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.Children$Keys.add([Lorg.openide.nodes.Node;)
meth public boolean org.openide.nodes.Children$Keys.remove([Lorg.openide.nodes.Node;)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes()
meth public final int org.openide.nodes.Children.getNodesCount()
meth public final java.util.Enumeration org.openide.nodes.Children.nodes()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.Index$KeysChildren.update()
meth public java.lang.Object org.openide.nodes.Children$Keys.clone()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Index org.openide.nodes.Index$KeysChildren.getIndex()
meth public org.openide.nodes.Node org.openide.nodes.Children.findChild(java.lang.String)
meth public static org.openide.nodes.Children org.openide.nodes.Children.create(org.openide.nodes.ChildFactory,boolean)
supr org.openide.nodes.Children$Keys
CLSS public static abstract org.openide.nodes.Index$Support
cons public Support()
innr public static abstract org.openide.nodes.Index$KeysChildren
innr public static abstract org.openide.nodes.Index$Support
innr public static org.openide.nodes.Index$ArrayChildren
intf org.openide.nodes.Index
intf org.openide.nodes.Node$Cookie
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Index$Support.fireChangeEvent(javax.swing.event.ChangeEvent)
meth public abstract [Lorg.openide.nodes.Node; org.openide.nodes.Index$Support.getNodes()
meth public abstract int org.openide.nodes.Index$Support.getNodesCount()
meth public abstract void org.openide.nodes.Index$Support.reorder([I)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Index$Support.indexOf(org.openide.nodes.Node)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static void org.openide.nodes.Index$Support.showIndexedCustomizer(org.openide.nodes.Index)
meth public void org.openide.nodes.Index$Support.addChangeListener(javax.swing.event.ChangeListener)
meth public void org.openide.nodes.Index$Support.exchange(int,int)
meth public void org.openide.nodes.Index$Support.move(int,int)
meth public void org.openide.nodes.Index$Support.moveDown(int)
meth public void org.openide.nodes.Index$Support.moveUp(int)
meth public void org.openide.nodes.Index$Support.removeChangeListener(javax.swing.event.ChangeListener)
meth public void org.openide.nodes.Index$Support.reorder()
supr java.lang.Object
CLSS public static abstract interface org.openide.nodes.Node$Cookie
supr null
CLSS public static abstract interface org.openide.nodes.Node$Handle
fld  constant public static final long org.openide.nodes.Node$Handle.serialVersionUID
intf java.io.Serializable
meth public abstract org.openide.nodes.Node org.openide.nodes.Node$Handle.getNode() throws java.io.IOException
supr null
CLSS public static abstract org.openide.nodes.Node$IndexedProperty
cons public IndexedProperty(java.lang.Class,java.lang.Class)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.openide.nodes.Node$IndexedProperty.canIndexedRead()
meth public abstract boolean org.openide.nodes.Node$IndexedProperty.canIndexedWrite()
meth public abstract boolean org.openide.nodes.Node$Property.canRead()
meth public abstract boolean org.openide.nodes.Node$Property.canWrite()
meth public abstract java.lang.Object org.openide.nodes.Node$IndexedProperty.getIndexedValue(int) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public abstract java.lang.Object org.openide.nodes.Node$Property.getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract void org.openide.nodes.Node$IndexedProperty.setIndexedValue(int,java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public abstract void org.openide.nodes.Node$Property.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$IndexedProperty.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$IndexedProperty.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$IndexedProperty.getIndexedPropertyEditor()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$IndexedProperty.getElementType()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.Node$Property
CLSS public static abstract org.openide.nodes.Node$Property
cons public Property(java.lang.Class)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.openide.nodes.Node$Property.canRead()
meth public abstract boolean org.openide.nodes.Node$Property.canWrite()
meth public abstract java.lang.Object org.openide.nodes.Node$Property.getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract void org.openide.nodes.Node$Property.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr java.beans.FeatureDescriptor
CLSS public static abstract org.openide.nodes.Node$PropertySet
cons public PropertySet()
cons public PropertySet(java.lang.String,java.lang.String,java.lang.String)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Lorg.openide.nodes.Node$Property; org.openide.nodes.Node$PropertySet.getProperties()
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$PropertySet.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$PropertySet.hashCode()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$PropertySet.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
supr java.beans.FeatureDescriptor
CLSS public static abstract interface org.openide.nodes.NodeTransfer$Paste
meth public abstract [Lorg.openide.util.datatransfer.PasteType; org.openide.nodes.NodeTransfer$Paste.types(org.openide.nodes.Node)
supr null
CLSS public static final org.openide.nodes.PropertySupport$Name
cons public Name(org.openide.nodes.Node)
cons public Name(org.openide.nodes.Node,java.lang.String,java.lang.String)
innr public static abstract org.openide.nodes.PropertySupport$ReadOnly
innr public static abstract org.openide.nodes.PropertySupport$ReadWrite
innr public static abstract org.openide.nodes.PropertySupport$WriteOnly
innr public static final org.openide.nodes.PropertySupport$Name
innr public static org.openide.nodes.PropertySupport$Reflection
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public boolean org.openide.nodes.PropertySupport.canRead()
meth public boolean org.openide.nodes.PropertySupport.canWrite()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.lang.String org.openide.nodes.PropertySupport$Name.getValue() throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public void org.openide.nodes.PropertySupport$Name.setValue(java.lang.String) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public volatile java.lang.Object org.openide.nodes.PropertySupport$Name.getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public volatile void org.openide.nodes.PropertySupport$Name.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport
CLSS public static abstract org.openide.nodes.PropertySupport$ReadOnly
cons public ReadOnly(java.lang.String,java.lang.Class,java.lang.String,java.lang.String)
innr public static abstract org.openide.nodes.PropertySupport$ReadOnly
innr public static abstract org.openide.nodes.PropertySupport$ReadWrite
innr public static abstract org.openide.nodes.PropertySupport$WriteOnly
innr public static final org.openide.nodes.PropertySupport$Name
innr public static org.openide.nodes.PropertySupport$Reflection
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Object org.openide.nodes.Node$Property.getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public boolean org.openide.nodes.PropertySupport.canRead()
meth public boolean org.openide.nodes.PropertySupport.canWrite()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public void org.openide.nodes.PropertySupport$ReadOnly.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport
CLSS public static abstract org.openide.nodes.PropertySupport$ReadWrite
cons public ReadWrite(java.lang.String,java.lang.Class,java.lang.String,java.lang.String)
innr public static abstract org.openide.nodes.PropertySupport$ReadOnly
innr public static abstract org.openide.nodes.PropertySupport$ReadWrite
innr public static abstract org.openide.nodes.PropertySupport$WriteOnly
innr public static final org.openide.nodes.PropertySupport$Name
innr public static org.openide.nodes.PropertySupport$Reflection
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Object org.openide.nodes.Node$Property.getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract void org.openide.nodes.Node$Property.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public boolean org.openide.nodes.PropertySupport.canRead()
meth public boolean org.openide.nodes.PropertySupport.canWrite()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport
CLSS public static org.openide.nodes.PropertySupport$Reflection
cons public Reflection(java.lang.Object,java.lang.Class,java.lang.String) throws java.lang.NoSuchMethodException
cons public Reflection(java.lang.Object,java.lang.Class,java.lang.String,java.lang.String) throws java.lang.NoSuchMethodException
cons public Reflection(java.lang.Object,java.lang.Class,java.lang.reflect.Method,java.lang.reflect.Method)
fld  protected java.lang.Object org.openide.nodes.PropertySupport$Reflection.instance
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public boolean org.openide.nodes.PropertySupport$Reflection.canRead()
meth public boolean org.openide.nodes.PropertySupport$Reflection.canWrite()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.PropertySupport$Reflection.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.Object org.openide.nodes.PropertySupport$Reflection.getValue() throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public void org.openide.nodes.PropertySupport$Reflection.setPropertyEditorClass(java.lang.Class)
meth public void org.openide.nodes.PropertySupport$Reflection.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.Node$Property
CLSS public static abstract org.openide.nodes.PropertySupport$WriteOnly
cons public WriteOnly(java.lang.String,java.lang.Class,java.lang.String,java.lang.String)
innr public static abstract org.openide.nodes.PropertySupport$ReadOnly
innr public static abstract org.openide.nodes.PropertySupport$ReadWrite
innr public static abstract org.openide.nodes.PropertySupport$WriteOnly
innr public static final org.openide.nodes.PropertySupport$Name
innr public static org.openide.nodes.PropertySupport$Reflection
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.openide.nodes.Node$Property.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public boolean org.openide.nodes.PropertySupport.canRead()
meth public boolean org.openide.nodes.PropertySupport.canWrite()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.Object org.openide.nodes.PropertySupport$WriteOnly.getValue() throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.PropertySupport
CLSS public static final org.openide.nodes.Sheet$Set
cons public Set()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.openide.nodes.Node$Property; org.openide.nodes.Sheet$Set.getProperties()
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$PropertySet.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$PropertySet.hashCode()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$PropertySet.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.nodes.Node$Property org.openide.nodes.Sheet$Set.get(java.lang.String)
meth public synchronized org.openide.nodes.Node$Property org.openide.nodes.Sheet$Set.put(org.openide.nodes.Node$Property)
meth public synchronized org.openide.nodes.Node$Property org.openide.nodes.Sheet$Set.remove(java.lang.String)
meth public synchronized org.openide.nodes.Sheet$Set org.openide.nodes.Sheet$Set.cloneSet()
meth public synchronized void org.openide.nodes.Sheet$Set.put([Lorg.openide.nodes.Node$Property;)
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Sheet$Set.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.nodes.Sheet$Set.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr org.openide.nodes.Node$PropertySet
CLSS public static abstract interface org.openide.util.HelpCtx$Provider
meth public abstract org.openide.util.HelpCtx org.openide.util.HelpCtx$Provider.getHelpCtx()
supr null
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
CLSS public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem org.openide.util.actions.Presenter$Menu.getMenuPresenter()
supr null
CLSS public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem org.openide.util.actions.Presenter$Popup.getPopupPresenter()
supr null
CLSS public static abstract interface org.openide.util.actions.Presenter$Toolbar
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component org.openide.util.actions.Presenter$Toolbar.getToolbarPresenter()
supr null
CLSS public static abstract org.openide.util.datatransfer.ExTransferable$Single
cons public Single(java.awt.datatransfer.DataFlavor)
intf java.awt.datatransfer.Transferable
meth protected abstract java.lang.Object org.openide.util.datatransfer.ExTransferable$Single.getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.awt.datatransfer.DataFlavor; org.openide.util.datatransfer.ExTransferable$Single.getTransferDataFlavors()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.datatransfer.ExTransferable$Single.isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.util.datatransfer.ExTransferable$Single.getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static abstract org.openide.util.lookup.AbstractLookup$Pair
cons protected Pair()
intf java.io.Serializable
meth protected abstract boolean org.openide.util.lookup.AbstractLookup$Pair.creatorOf(java.lang.Object)
meth protected abstract boolean org.openide.util.lookup.AbstractLookup$Pair.instanceOf(java.lang.Class)
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
supr org.openide.util.Lookup$Item
CLSS public static abstract interface org.openide.util.lookup.InstanceContent$Convertor
meth public abstract java.lang.Class org.openide.util.lookup.InstanceContent$Convertor.type(java.lang.Object)
meth public abstract java.lang.Object org.openide.util.lookup.InstanceContent$Convertor.convert(java.lang.Object)
meth public abstract java.lang.String org.openide.util.lookup.InstanceContent$Convertor.displayName(java.lang.Object)
meth public abstract java.lang.String org.openide.util.lookup.InstanceContent$Convertor.id(java.lang.Object)
supr null
CLSS public abstract interface org.openide.cookies.CloseCookie
intf org.openide.nodes.Node$Cookie
meth public abstract boolean org.openide.cookies.CloseCookie.close()
supr null
CLSS public abstract interface org.openide.cookies.ConnectionCookie
innr public static abstract interface org.openide.cookies.ConnectionCookie$Listener
innr public static abstract interface org.openide.cookies.ConnectionCookie$Type
innr public static org.openide.cookies.ConnectionCookie$Event
intf org.openide.nodes.Node$Cookie
meth public abstract java.util.Set org.openide.cookies.ConnectionCookie.getTypes()
meth public abstract void org.openide.cookies.ConnectionCookie.register(org.openide.cookies.ConnectionCookie$Type,org.openide.nodes.Node) throws java.io.IOException
meth public abstract void org.openide.cookies.ConnectionCookie.unregister(org.openide.cookies.ConnectionCookie$Type,org.openide.nodes.Node) throws java.io.IOException
supr null
CLSS public abstract interface org.openide.cookies.EditCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void org.openide.cookies.EditCookie.edit()
supr null
CLSS public abstract interface org.openide.cookies.FilterCookie
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.Class org.openide.cookies.FilterCookie.getFilterClass()
meth public abstract java.lang.Object org.openide.cookies.FilterCookie.getFilter()
meth public abstract void org.openide.cookies.FilterCookie.setFilter(java.lang.Object)
supr null
CLSS public abstract interface org.openide.cookies.InstanceCookie
innr public static abstract interface org.openide.cookies.InstanceCookie$Of
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.Class org.openide.cookies.InstanceCookie.instanceClass() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.Object org.openide.cookies.InstanceCookie.instanceCreate() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract java.lang.String org.openide.cookies.InstanceCookie.instanceName()
supr null
CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void org.openide.cookies.OpenCookie.open()
supr null
CLSS public abstract interface org.openide.cookies.PrintCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void org.openide.cookies.PrintCookie.print()
supr null
CLSS public abstract interface org.openide.cookies.SaveCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void org.openide.cookies.SaveCookie.save() throws java.io.IOException
supr null
CLSS public abstract interface org.openide.cookies.ViewCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void org.openide.cookies.ViewCookie.view()
supr null
CLSS public org.openide.nodes.AbstractNode
cons public AbstractNode(org.openide.nodes.Children)
cons public AbstractNode(org.openide.nodes.Children,org.openide.util.Lookup)
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_COOKIE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_LEAF
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_OPENED_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PARENT_NODE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PROPERTY_SETS
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_SHORT_DESCRIPTION
fld  protected [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.systemActions
fld  protected java.text.MessageFormat org.openide.nodes.AbstractNode.displayFormat
fld  public static final org.openide.nodes.Node org.openide.nodes.Node.EMPTY
innr public static abstract interface org.openide.nodes.Node$Cookie
innr public static abstract interface org.openide.nodes.Node$Handle
innr public static abstract org.openide.nodes.Node$IndexedProperty
innr public static abstract org.openide.nodes.Node$Property
innr public static abstract org.openide.nodes.Node$PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.createActions()
meth protected final boolean org.openide.nodes.Node.hasPropertyChangeListener()
meth protected final org.openide.nodes.CookieSet org.openide.nodes.AbstractNode.getCookieSet()
meth protected final void org.openide.nodes.Node.fireCookieChange()
meth protected final void org.openide.nodes.Node.fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireIconChange()
meth protected final void org.openide.nodes.Node.fireNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireNodeDestroyed()
meth protected final void org.openide.nodes.Node.fireOpenedIconChange()
meth protected final void org.openide.nodes.Node.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.nodes.Node.firePropertySetsChange([Lorg.openide.nodes.Node$PropertySet;,[Lorg.openide.nodes.Node$PropertySet;)
meth protected final void org.openide.nodes.Node.fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.setChildren(org.openide.nodes.Children)
meth protected java.lang.Object org.openide.nodes.Node.clone() throws java.lang.CloneNotSupportedException
meth protected org.openide.nodes.Sheet org.openide.nodes.AbstractNode.createSheet()
meth protected synchronized final org.openide.nodes.Sheet org.openide.nodes.AbstractNode.getSheet()
meth protected synchronized final void org.openide.nodes.AbstractNode.setCookieSet(org.openide.nodes.CookieSet)
meth protected synchronized final void org.openide.nodes.AbstractNode.setSheet(org.openide.nodes.Sheet)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.AbstractNode.createPasteTypes(java.awt.datatransfer.Transferable,java.util.List)
meth public [Ljavax.swing.Action; org.openide.nodes.Node.getActions(boolean)
meth public [Lorg.openide.nodes.Node$PropertySet; org.openide.nodes.AbstractNode.getPropertySets()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.Node.getContextActions()
meth public [Lorg.openide.util.datatransfer.NewType; org.openide.nodes.AbstractNode.getNewTypes()
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.AbstractNode.canCopy()
meth public boolean org.openide.nodes.AbstractNode.canCut()
meth public boolean org.openide.nodes.AbstractNode.canDestroy()
meth public boolean org.openide.nodes.AbstractNode.canRename()
meth public boolean org.openide.nodes.AbstractNode.hasCustomizer()
meth public boolean org.openide.nodes.Node.equals(java.lang.Object)
meth public final [Lorg.openide.util.datatransfer.PasteType; org.openide.nodes.AbstractNode.getPasteTypes(java.awt.datatransfer.Transferable)
meth public final boolean org.openide.nodes.Node.isLeaf()
meth public final javax.swing.JPopupMenu org.openide.nodes.Node.getContextMenu()
meth public final org.openide.nodes.Children org.openide.nodes.Node.getChildren()
meth public final org.openide.nodes.Node org.openide.nodes.Node.getParentNode()
meth public final org.openide.util.Lookup org.openide.nodes.Node.getLookup()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.AbstractNode.setIconBaseWithExtension(java.lang.String)
meth public final void org.openide.nodes.Node.addNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.nodes.Node.removeNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.awt.Component org.openide.nodes.AbstractNode.getCustomizer()
meth public java.awt.Image org.openide.nodes.AbstractNode.getIcon(int)
meth public java.awt.Image org.openide.nodes.AbstractNode.getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.drag() throws java.io.IOException
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String org.openide.nodes.Node.getHtmlDisplayName()
meth public java.lang.String org.openide.nodes.Node.toString()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public javax.swing.Action org.openide.nodes.AbstractNode.getPreferredAction()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.AbstractNode.cloneNode()
meth public org.openide.nodes.Node$Cookie org.openide.nodes.AbstractNode.getCookie(java.lang.Class)
meth public org.openide.nodes.Node$Handle org.openide.nodes.AbstractNode.getHandle()
meth public org.openide.util.HelpCtx org.openide.nodes.AbstractNode.getHelpCtx()
meth public org.openide.util.actions.SystemAction org.openide.nodes.AbstractNode.getDefaultAction()
meth public org.openide.util.datatransfer.PasteType org.openide.nodes.AbstractNode.getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.AbstractNode.setDefaultAction(org.openide.util.actions.SystemAction)
meth public void org.openide.nodes.AbstractNode.setIconBase(java.lang.String)
meth public void org.openide.nodes.AbstractNode.setName(java.lang.String)
meth public void org.openide.nodes.Node.destroy() throws java.io.IOException
meth public void org.openide.nodes.Node.setDisplayName(java.lang.String)
meth public void org.openide.nodes.Node.setShortDescription(java.lang.String)
supr org.openide.nodes.Node
CLSS public org.openide.nodes.BeanChildren
cons public BeanChildren(java.beans.beancontext.BeanContext)
cons public BeanChildren(java.beans.beancontext.BeanContext,org.openide.nodes.BeanChildren$Factory)
fld  protected java.util.Collection org.openide.nodes.Children$Array.nodes
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract interface org.openide.nodes.BeanChildren$Factory
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
intf java.lang.Cloneable
meth protected [Lorg.openide.nodes.Node; org.openide.nodes.BeanChildren.createNodes(java.lang.Object)
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected final void org.openide.nodes.Children$Array.refresh()
meth protected final void org.openide.nodes.Children$Keys.refreshKey(java.lang.Object)
meth protected final void org.openide.nodes.Children$Keys.setBefore(boolean)
meth protected final void org.openide.nodes.Children$Keys.setKeys([Ljava.lang.Object;)
meth protected final void org.openide.nodes.Children$Keys.setKeys(java.util.Collection)
meth protected java.util.Collection org.openide.nodes.Children$Array.initCollection()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.BeanChildren.addNotify()
meth protected void org.openide.nodes.BeanChildren.removeNotify()
meth protected void org.openide.nodes.Children$Keys.destroyNodes([Lorg.openide.nodes.Node;)
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
supr org.openide.nodes.Children$Keys
CLSS public org.openide.nodes.BeanNode
cons protected BeanNode(java.lang.Object,org.openide.nodes.Children) throws java.beans.IntrospectionException
cons protected BeanNode(java.lang.Object,org.openide.nodes.Children,org.openide.util.Lookup) throws java.beans.IntrospectionException
cons public BeanNode(java.lang.Object) throws java.beans.IntrospectionException
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_COOKIE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_LEAF
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_OPENED_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PARENT_NODE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PROPERTY_SETS
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_SHORT_DESCRIPTION
fld  protected [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.systemActions
fld  protected java.text.MessageFormat org.openide.nodes.AbstractNode.displayFormat
fld  public static final org.openide.nodes.Node org.openide.nodes.Node.EMPTY
innr public static abstract interface org.openide.nodes.Node$Cookie
innr public static abstract interface org.openide.nodes.Node$Handle
innr public static abstract org.openide.nodes.Node$IndexedProperty
innr public static abstract org.openide.nodes.Node$Property
innr public static abstract org.openide.nodes.Node$PropertySet
innr public static final org.openide.nodes.BeanNode$Descriptor
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.createActions()
meth protected final boolean org.openide.nodes.Node.hasPropertyChangeListener()
meth protected final org.openide.nodes.CookieSet org.openide.nodes.AbstractNode.getCookieSet()
meth protected final void org.openide.nodes.Node.fireCookieChange()
meth protected final void org.openide.nodes.Node.fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireIconChange()
meth protected final void org.openide.nodes.Node.fireNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireNodeDestroyed()
meth protected final void org.openide.nodes.Node.fireOpenedIconChange()
meth protected final void org.openide.nodes.Node.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.nodes.Node.firePropertySetsChange([Lorg.openide.nodes.Node$PropertySet;,[Lorg.openide.nodes.Node$PropertySet;)
meth protected final void org.openide.nodes.Node.fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.setChildren(org.openide.nodes.Children)
meth protected java.lang.Object org.openide.nodes.BeanNode.getBean()
meth protected java.lang.Object org.openide.nodes.Node.clone() throws java.lang.CloneNotSupportedException
meth protected org.openide.nodes.Sheet org.openide.nodes.AbstractNode.createSheet()
meth protected synchronized final org.openide.nodes.Sheet org.openide.nodes.AbstractNode.getSheet()
meth protected synchronized final void org.openide.nodes.AbstractNode.setCookieSet(org.openide.nodes.CookieSet)
meth protected synchronized final void org.openide.nodes.AbstractNode.setSheet(org.openide.nodes.Sheet)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.AbstractNode.createPasteTypes(java.awt.datatransfer.Transferable,java.util.List)
meth protected void org.openide.nodes.BeanNode.createProperties(java.lang.Object,java.beans.BeanInfo)
meth protected void org.openide.nodes.BeanNode.setSynchronizeName(boolean)
meth public [Ljavax.swing.Action; org.openide.nodes.BeanNode.getActions(boolean)
meth public [Lorg.openide.nodes.Node$PropertySet; org.openide.nodes.AbstractNode.getPropertySets()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.Node.getContextActions()
meth public [Lorg.openide.util.datatransfer.NewType; org.openide.nodes.AbstractNode.getNewTypes()
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.BeanNode.canCopy()
meth public boolean org.openide.nodes.BeanNode.canCut()
meth public boolean org.openide.nodes.BeanNode.canDestroy()
meth public boolean org.openide.nodes.BeanNode.canRename()
meth public boolean org.openide.nodes.BeanNode.hasCustomizer()
meth public boolean org.openide.nodes.Node.equals(java.lang.Object)
meth public final [Lorg.openide.util.datatransfer.PasteType; org.openide.nodes.AbstractNode.getPasteTypes(java.awt.datatransfer.Transferable)
meth public final boolean org.openide.nodes.Node.isLeaf()
meth public final javax.swing.JPopupMenu org.openide.nodes.Node.getContextMenu()
meth public final org.openide.nodes.Children org.openide.nodes.Node.getChildren()
meth public final org.openide.nodes.Node org.openide.nodes.Node.getParentNode()
meth public final org.openide.util.Lookup org.openide.nodes.Node.getLookup()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.AbstractNode.setIconBaseWithExtension(java.lang.String)
meth public final void org.openide.nodes.Node.addNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.nodes.Node.removeNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.awt.Component org.openide.nodes.BeanNode.getCustomizer()
meth public java.awt.Image org.openide.nodes.BeanNode.getIcon(int)
meth public java.awt.Image org.openide.nodes.BeanNode.getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.drag() throws java.io.IOException
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String org.openide.nodes.Node.getHtmlDisplayName()
meth public java.lang.String org.openide.nodes.Node.toString()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public javax.swing.Action org.openide.nodes.BeanNode.getPreferredAction()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.AbstractNode.cloneNode()
meth public org.openide.nodes.Node$Cookie org.openide.nodes.AbstractNode.getCookie(java.lang.Class)
meth public org.openide.nodes.Node$Handle org.openide.nodes.AbstractNode.getHandle()
meth public org.openide.util.HelpCtx org.openide.nodes.BeanNode.getHelpCtx()
meth public org.openide.util.actions.SystemAction org.openide.nodes.AbstractNode.getDefaultAction()
meth public org.openide.util.datatransfer.PasteType org.openide.nodes.AbstractNode.getDropType(java.awt.datatransfer.Transferable,int,int)
meth public static org.openide.nodes.BeanNode$Descriptor org.openide.nodes.BeanNode.computeProperties(java.lang.Object,java.beans.BeanInfo)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.AbstractNode.setDefaultAction(org.openide.util.actions.SystemAction)
meth public void org.openide.nodes.AbstractNode.setIconBase(java.lang.String)
meth public void org.openide.nodes.BeanNode.destroy() throws java.io.IOException
meth public void org.openide.nodes.BeanNode.setName(java.lang.String)
meth public void org.openide.nodes.Node.setDisplayName(java.lang.String)
meth public void org.openide.nodes.Node.setShortDescription(java.lang.String)
supr org.openide.nodes.AbstractNode
CLSS public abstract org.openide.nodes.ChildFactory
cons public ChildFactory()
meth protected [Lorg.openide.nodes.Node; org.openide.nodes.ChildFactory.createNodesForKey(java.lang.Object)
meth protected abstract boolean org.openide.nodes.ChildFactory.createKeys(java.util.List)
meth protected final void org.openide.nodes.ChildFactory.refresh(boolean)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected org.openide.nodes.Node org.openide.nodes.ChildFactory.createNodeForKey(java.lang.Object)
meth protected org.openide.nodes.Node org.openide.nodes.ChildFactory.createWaitNode()
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
CLSS public abstract org.openide.nodes.Children
cons public Children()
fld  public static final org.openide.nodes.Children org.openide.nodes.Children.LEAF
fld  public static final org.openide.util.Mutex org.openide.nodes.Children.MUTEX
innr public static abstract org.openide.nodes.Children$Keys
innr public static org.openide.nodes.Children$Array
innr public static org.openide.nodes.Children$Map
innr public static org.openide.nodes.Children$SortedArray
innr public static org.openide.nodes.Children$SortedMap
meth protected final boolean org.openide.nodes.Children.isInitialized()
meth protected final org.openide.nodes.Node org.openide.nodes.Children.getNode()
meth protected java.lang.Object org.openide.nodes.Children.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.Children.addNotify()
meth protected void org.openide.nodes.Children.removeNotify()
meth public [Lorg.openide.nodes.Node; org.openide.nodes.Children.getNodes(boolean)
meth public abstract boolean org.openide.nodes.Children.add([Lorg.openide.nodes.Node;)
meth public abstract boolean org.openide.nodes.Children.remove([Lorg.openide.nodes.Node;)
meth public boolean java.lang.Object.equals(java.lang.Object)
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
supr java.lang.Object
CLSS public final org.openide.nodes.CookieSet
cons public CookieSet()
innr public static abstract interface org.openide.nodes.CookieSet$Before
innr public static abstract interface org.openide.nodes.CookieSet$Factory
intf org.openide.util.Lookup$Provider
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
meth public org.openide.nodes.Node$Cookie org.openide.nodes.CookieSet.getCookie(java.lang.Class)
meth public org.openide.util.Lookup org.openide.nodes.CookieSet.getLookup()
meth public static org.openide.nodes.CookieSet org.openide.nodes.CookieSet.createGeneric(org.openide.nodes.CookieSet$Before)
meth public transient void org.openide.nodes.CookieSet.assign(java.lang.Class,[Ljava.lang.Object;)
meth public void org.openide.nodes.CookieSet.add([Ljava.lang.Class;,org.openide.nodes.CookieSet$Factory)
meth public void org.openide.nodes.CookieSet.add(java.lang.Class,org.openide.nodes.CookieSet$Factory)
meth public void org.openide.nodes.CookieSet.add(org.openide.nodes.Node$Cookie)
meth public void org.openide.nodes.CookieSet.addChangeListener(javax.swing.event.ChangeListener)
meth public void org.openide.nodes.CookieSet.remove([Ljava.lang.Class;,org.openide.nodes.CookieSet$Factory)
meth public void org.openide.nodes.CookieSet.remove(java.lang.Class,org.openide.nodes.CookieSet$Factory)
meth public void org.openide.nodes.CookieSet.remove(org.openide.nodes.Node$Cookie)
meth public void org.openide.nodes.CookieSet.removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
CLSS public final org.openide.nodes.DefaultHandle
intf java.io.Serializable
intf org.openide.nodes.Node$Handle
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.nodes.DefaultHandle.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.DefaultHandle.getNode() throws java.io.IOException
meth public static org.openide.nodes.DefaultHandle org.openide.nodes.DefaultHandle.createHandle(org.openide.nodes.Node)
supr java.lang.Object
CLSS public org.openide.nodes.FilterNode
cons public FilterNode(org.openide.nodes.Node)
cons public FilterNode(org.openide.nodes.Node,org.openide.nodes.Children)
cons public FilterNode(org.openide.nodes.Node,org.openide.nodes.Children,org.openide.util.Lookup)
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_DESTROY
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_GET_ACTIONS
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_GET_CONTEXT_ACTIONS
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_GET_DISPLAY_NAME
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_GET_NAME
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_GET_SHORT_DESCRIPTION
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_GET_VALUE
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_SET_DISPLAY_NAME
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_SET_NAME
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_SET_SHORT_DESCRIPTION
fld  constant protected static final int org.openide.nodes.FilterNode.DELEGATE_SET_VALUE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_COOKIE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_LEAF
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_OPENED_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PARENT_NODE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PROPERTY_SETS
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_SHORT_DESCRIPTION
fld  public static final org.openide.nodes.Node org.openide.nodes.Node.EMPTY
innr protected static org.openide.nodes.FilterNode$NodeAdapter
innr protected static org.openide.nodes.FilterNode$PropertyChangeAdapter
innr public static abstract interface org.openide.nodes.Node$Cookie
innr public static abstract interface org.openide.nodes.Node$Handle
innr public static abstract org.openide.nodes.Node$IndexedProperty
innr public static abstract org.openide.nodes.Node$Property
innr public static abstract org.openide.nodes.Node$PropertySet
innr public static org.openide.nodes.FilterNode$Children
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean org.openide.nodes.Node.hasPropertyChangeListener()
meth protected final void org.openide.nodes.FilterNode.changeOriginal(org.openide.nodes.Node,boolean)
meth protected final void org.openide.nodes.FilterNode.disableDelegation(int)
meth protected final void org.openide.nodes.FilterNode.enableDelegation(int)
meth protected final void org.openide.nodes.Node.fireCookieChange()
meth protected final void org.openide.nodes.Node.fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireIconChange()
meth protected final void org.openide.nodes.Node.fireNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireNodeDestroyed()
meth protected final void org.openide.nodes.Node.fireOpenedIconChange()
meth protected final void org.openide.nodes.Node.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.nodes.Node.firePropertySetsChange([Lorg.openide.nodes.Node$PropertySet;,[Lorg.openide.nodes.Node$PropertySet;)
meth protected final void org.openide.nodes.Node.fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.setChildren(org.openide.nodes.Children)
meth protected java.beans.PropertyChangeListener org.openide.nodes.FilterNode.createPropertyChangeListener()
meth protected java.lang.Object org.openide.nodes.Node.clone() throws java.lang.CloneNotSupportedException
meth protected org.openide.nodes.Node org.openide.nodes.FilterNode.getOriginal()
meth protected org.openide.nodes.NodeListener org.openide.nodes.FilterNode.createNodeListener()
meth protected void org.openide.nodes.FilterNode.finalize()
meth public [Ljavax.swing.Action; org.openide.nodes.FilterNode.getActions(boolean)
meth public [Lorg.openide.nodes.Node$PropertySet; org.openide.nodes.FilterNode.getPropertySets()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.FilterNode.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.FilterNode.getContextActions()
meth public [Lorg.openide.util.datatransfer.NewType; org.openide.nodes.FilterNode.getNewTypes()
meth public [Lorg.openide.util.datatransfer.PasteType; org.openide.nodes.FilterNode.getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.FilterNode.canCopy()
meth public boolean org.openide.nodes.FilterNode.canCut()
meth public boolean org.openide.nodes.FilterNode.canDestroy()
meth public boolean org.openide.nodes.FilterNode.canRename()
meth public boolean org.openide.nodes.FilterNode.equals(java.lang.Object)
meth public boolean org.openide.nodes.FilterNode.hasCustomizer()
meth public final boolean org.openide.nodes.Node.isLeaf()
meth public final javax.swing.JPopupMenu org.openide.nodes.Node.getContextMenu()
meth public final org.openide.nodes.Children org.openide.nodes.Node.getChildren()
meth public final org.openide.nodes.Node org.openide.nodes.Node.getParentNode()
meth public final org.openide.util.Lookup org.openide.nodes.Node.getLookup()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.Node.addNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.nodes.Node.removeNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int org.openide.nodes.FilterNode.hashCode()
meth public java.awt.Component org.openide.nodes.FilterNode.getCustomizer()
meth public java.awt.Image org.openide.nodes.FilterNode.getIcon(int)
meth public java.awt.Image org.openide.nodes.FilterNode.getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable org.openide.nodes.FilterNode.clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.FilterNode.clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.FilterNode.drag() throws java.io.IOException
meth public java.lang.Object org.openide.nodes.FilterNode.getValue(java.lang.String)
meth public java.lang.String org.openide.nodes.FilterNode.getDisplayName()
meth public java.lang.String org.openide.nodes.FilterNode.getHtmlDisplayName()
meth public java.lang.String org.openide.nodes.FilterNode.getName()
meth public java.lang.String org.openide.nodes.FilterNode.getShortDescription()
meth public java.lang.String org.openide.nodes.Node.toString()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public javax.swing.Action org.openide.nodes.FilterNode.getPreferredAction()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.nodes.Node org.openide.nodes.FilterNode.cloneNode()
meth public org.openide.nodes.Node$Cookie org.openide.nodes.FilterNode.getCookie(java.lang.Class)
meth public org.openide.nodes.Node$Handle org.openide.nodes.FilterNode.getHandle()
meth public org.openide.util.HelpCtx org.openide.nodes.FilterNode.getHelpCtx()
meth public org.openide.util.actions.SystemAction org.openide.nodes.FilterNode.getDefaultAction()
meth public org.openide.util.datatransfer.PasteType org.openide.nodes.FilterNode.getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void org.openide.nodes.FilterNode.destroy() throws java.io.IOException
meth public void org.openide.nodes.FilterNode.setDisplayName(java.lang.String)
meth public void org.openide.nodes.FilterNode.setName(java.lang.String)
meth public void org.openide.nodes.FilterNode.setShortDescription(java.lang.String)
meth public void org.openide.nodes.FilterNode.setValue(java.lang.String,java.lang.Object)
supr org.openide.nodes.Node
CLSS public abstract interface org.openide.nodes.Index
innr public static abstract org.openide.nodes.Index$KeysChildren
innr public static abstract org.openide.nodes.Index$Support
innr public static org.openide.nodes.Index$ArrayChildren
intf org.openide.nodes.Node$Cookie
meth public abstract [Lorg.openide.nodes.Node; org.openide.nodes.Index.getNodes()
meth public abstract int org.openide.nodes.Index.getNodesCount()
meth public abstract int org.openide.nodes.Index.indexOf(org.openide.nodes.Node)
meth public abstract void org.openide.nodes.Index.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.openide.nodes.Index.exchange(int,int)
meth public abstract void org.openide.nodes.Index.move(int,int)
meth public abstract void org.openide.nodes.Index.moveDown(int)
meth public abstract void org.openide.nodes.Index.moveUp(int)
meth public abstract void org.openide.nodes.Index.removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.openide.nodes.Index.reorder()
meth public abstract void org.openide.nodes.Index.reorder([I)
supr null
CLSS public final org.openide.nodes.IndexedCustomizer
cons public IndexedCustomizer()
fld  protected boolean javax.swing.JDialog.rootPaneCheckingEnabled
fld  protected javax.accessibility.AccessibleContext javax.swing.JDialog.accessibleContext
fld  protected javax.swing.JRootPane javax.swing.JDialog.rootPane
fld  public static final float java.awt.Component.BOTTOM_ALIGNMENT
fld  public static final float java.awt.Component.CENTER_ALIGNMENT
fld  public static final float java.awt.Component.LEFT_ALIGNMENT
fld  public static final float java.awt.Component.RIGHT_ALIGNMENT
fld  public static final float java.awt.Component.TOP_ALIGNMENT
fld  public static final int java.awt.image.ImageObserver.ABORT
fld  public static final int java.awt.image.ImageObserver.ALLBITS
fld  public static final int java.awt.image.ImageObserver.ERROR
fld  public static final int java.awt.image.ImageObserver.FRAMEBITS
fld  public static final int java.awt.image.ImageObserver.HEIGHT
fld  public static final int java.awt.image.ImageObserver.PROPERTIES
fld  public static final int java.awt.image.ImageObserver.SOMEBITS
fld  public static final int java.awt.image.ImageObserver.WIDTH
fld  public static final int javax.swing.WindowConstants.DISPOSE_ON_CLOSE
fld  public static final int javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
fld  public static final int javax.swing.WindowConstants.EXIT_ON_CLOSE
fld  public static final int javax.swing.WindowConstants.HIDE_ON_CLOSE
intf java.awt.MenuContainer
intf java.awt.image.ImageObserver
intf java.beans.Customizer
intf java.io.Serializable
intf javax.accessibility.Accessible
intf javax.swing.RootPaneContainer
intf javax.swing.WindowConstants
meth protected boolean java.awt.Component.requestFocus(boolean)
meth protected boolean java.awt.Component.requestFocusInWindow(boolean)
meth protected boolean javax.swing.JDialog.isRootPaneCheckingEnabled()
meth protected final void java.awt.Component.disableEvents(long)
meth protected final void java.awt.Component.enableEvents(long)
meth protected java.awt.AWTEvent java.awt.Component.coalesceEvents(java.awt.AWTEvent,java.awt.AWTEvent)
meth protected java.lang.String javax.swing.JDialog.paramString()
meth protected javax.swing.JRootPane javax.swing.JDialog.createRootPane()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.awt.Component.firePropertyChange(java.lang.String,boolean,boolean)
meth protected void java.awt.Component.firePropertyChange(java.lang.String,int,int)
meth protected void java.awt.Component.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void java.awt.Component.processComponentEvent(java.awt.event.ComponentEvent)
meth protected void java.awt.Component.processFocusEvent(java.awt.event.FocusEvent)
meth protected void java.awt.Component.processHierarchyBoundsEvent(java.awt.event.HierarchyEvent)
meth protected void java.awt.Component.processHierarchyEvent(java.awt.event.HierarchyEvent)
meth protected void java.awt.Component.processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void java.awt.Component.processKeyEvent(java.awt.event.KeyEvent)
meth protected void java.awt.Component.processMouseEvent(java.awt.event.MouseEvent)
meth protected void java.awt.Component.processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void java.awt.Component.processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth protected void java.awt.Container.processContainerEvent(java.awt.event.ContainerEvent)
meth protected void java.awt.Container.validateTree()
meth protected void java.awt.Window.finalize() throws java.lang.Throwable
meth protected void java.awt.Window.processEvent(java.awt.AWTEvent)
meth protected void java.awt.Window.processWindowFocusEvent(java.awt.event.WindowEvent)
meth protected void java.awt.Window.processWindowStateEvent(java.awt.event.WindowEvent)
meth protected void javax.swing.JDialog.addImpl(java.awt.Component,java.lang.Object,int)
meth protected void javax.swing.JDialog.dialogInit()
meth protected void javax.swing.JDialog.processWindowEvent(java.awt.event.WindowEvent)
meth protected void javax.swing.JDialog.setRootPane(javax.swing.JRootPane)
meth protected void javax.swing.JDialog.setRootPaneCheckingEnabled(boolean)
meth public [Ljava.awt.Component; java.awt.Container.getComponents()
meth public [Ljava.awt.Window; java.awt.Window.getOwnedWindows()
meth public [Ljava.util.EventListener; java.awt.Window.getListeners(java.lang.Class)
meth public boolean java.awt.Component.action(java.awt.Event,java.lang.Object)
meth public boolean java.awt.Component.contains(int,int)
meth public boolean java.awt.Component.contains(java.awt.Point)
meth public boolean java.awt.Component.getFocusTraversalKeysEnabled()
meth public boolean java.awt.Component.getIgnoreRepaint()
meth public boolean java.awt.Component.gotFocus(java.awt.Event,java.lang.Object)
meth public boolean java.awt.Component.handleEvent(java.awt.Event)
meth public boolean java.awt.Component.hasFocus()
meth public boolean java.awt.Component.imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean java.awt.Component.inside(int,int)
meth public boolean java.awt.Component.isBackgroundSet()
meth public boolean java.awt.Component.isCursorSet()
meth public boolean java.awt.Component.isDisplayable()
meth public boolean java.awt.Component.isDoubleBuffered()
meth public boolean java.awt.Component.isEnabled()
meth public boolean java.awt.Component.isFocusOwner()
meth public boolean java.awt.Component.isFocusTraversable()
meth public boolean java.awt.Component.isFocusable()
meth public boolean java.awt.Component.isFontSet()
meth public boolean java.awt.Component.isForegroundSet()
meth public boolean java.awt.Component.isLightweight()
meth public boolean java.awt.Component.isMaximumSizeSet()
meth public boolean java.awt.Component.isMinimumSizeSet()
meth public boolean java.awt.Component.isOpaque()
meth public boolean java.awt.Component.isPreferredSizeSet()
meth public boolean java.awt.Component.isValid()
meth public boolean java.awt.Component.isVisible()
meth public boolean java.awt.Component.keyDown(java.awt.Event,int)
meth public boolean java.awt.Component.keyUp(java.awt.Event,int)
meth public boolean java.awt.Component.lostFocus(java.awt.Event,java.lang.Object)
meth public boolean java.awt.Component.mouseDown(java.awt.Event,int,int)
meth public boolean java.awt.Component.mouseDrag(java.awt.Event,int,int)
meth public boolean java.awt.Component.mouseEnter(java.awt.Event,int,int)
meth public boolean java.awt.Component.mouseExit(java.awt.Event,int,int)
meth public boolean java.awt.Component.mouseMove(java.awt.Event,int,int)
meth public boolean java.awt.Component.mouseUp(java.awt.Event,int,int)
meth public boolean java.awt.Component.prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean java.awt.Component.prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public boolean java.awt.Component.requestFocusInWindow()
meth public boolean java.awt.Container.areFocusTraversalKeysSet(int)
meth public boolean java.awt.Container.isAncestorOf(java.awt.Component)
meth public boolean java.awt.Container.isFocusCycleRoot(java.awt.Container)
meth public boolean java.awt.Container.isFocusTraversalPolicySet()
meth public boolean java.awt.Dialog.isModal()
meth public boolean java.awt.Dialog.isResizable()
meth public boolean java.awt.Dialog.isUndecorated()
meth public boolean java.awt.Window.getFocusableWindowState()
meth public boolean java.awt.Window.isActive()
meth public boolean java.awt.Window.isFocused()
meth public boolean java.awt.Window.isLocationByPlatform()
meth public boolean java.awt.Window.isShowing()
meth public boolean java.awt.Window.postEvent(java.awt.Event)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.nodes.IndexedCustomizer.isImmediateReorder()
meth public final boolean java.awt.Container.isFocusTraversalPolicyProvider()
meth public final boolean java.awt.Window.isAlwaysOnTop()
meth public final boolean java.awt.Window.isFocusCycleRoot()
meth public final boolean java.awt.Window.isFocusableWindow()
meth public final int java.awt.Container.getComponentZOrder(java.awt.Component)
meth public final java.awt.Container java.awt.Window.getFocusCycleRootAncestor()
meth public final java.lang.Object java.awt.Component.getTreeLock()
meth public final java.lang.String java.awt.Window.getWarningString()
meth public final void java.awt.Component.dispatchEvent(java.awt.AWTEvent)
meth public final void java.awt.Container.setComponentZOrder(java.awt.Component,int)
meth public final void java.awt.Container.setFocusTraversalPolicyProvider(boolean)
meth public final void java.awt.Window.setAlwaysOnTop(boolean) throws java.lang.SecurityException
meth public final void java.awt.Window.setFocusCycleRoot(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public float java.awt.Container.getAlignmentX()
meth public float java.awt.Container.getAlignmentY()
meth public int java.awt.Component.checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int java.awt.Component.checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int java.awt.Component.getHeight()
meth public int java.awt.Component.getWidth()
meth public int java.awt.Component.getX()
meth public int java.awt.Component.getY()
meth public int java.awt.Container.countComponents()
meth public int java.awt.Container.getComponentCount()
meth public int javax.swing.JDialog.getDefaultCloseOperation()
meth public java.awt.Color java.awt.Component.getBackground()
meth public java.awt.Color java.awt.Component.getForeground()
meth public java.awt.Component java.awt.Container.add(java.awt.Component)
meth public java.awt.Component java.awt.Container.add(java.awt.Component,int)
meth public java.awt.Component java.awt.Container.add(java.lang.String,java.awt.Component)
meth public java.awt.Component java.awt.Container.findComponentAt(int,int)
meth public java.awt.Component java.awt.Container.findComponentAt(java.awt.Point)
meth public java.awt.Component java.awt.Container.getComponent(int)
meth public java.awt.Component java.awt.Container.getComponentAt(int,int)
meth public java.awt.Component java.awt.Container.getComponentAt(java.awt.Point)
meth public java.awt.Component java.awt.Container.locate(int,int)
meth public java.awt.Component java.awt.Window.getFocusOwner()
meth public java.awt.Component java.awt.Window.getMostRecentFocusOwner()
meth public java.awt.Component javax.swing.JDialog.getGlassPane()
meth public java.awt.ComponentOrientation java.awt.Component.getComponentOrientation()
meth public java.awt.Container java.awt.Component.getParent()
meth public java.awt.Container javax.swing.JDialog.getContentPane()
meth public java.awt.Cursor java.awt.Component.getCursor()
meth public java.awt.Dimension java.awt.Component.getSize()
meth public java.awt.Dimension java.awt.Component.getSize(java.awt.Dimension)
meth public java.awt.Dimension java.awt.Component.size()
meth public java.awt.Dimension java.awt.Container.getMaximumSize()
meth public java.awt.Dimension java.awt.Container.getMinimumSize()
meth public java.awt.Dimension java.awt.Container.minimumSize()
meth public java.awt.Dimension java.awt.Container.preferredSize()
meth public java.awt.Dimension org.openide.nodes.IndexedCustomizer.getPreferredSize()
meth public java.awt.FocusTraversalPolicy java.awt.Container.getFocusTraversalPolicy()
meth public java.awt.Font java.awt.Component.getFont()
meth public java.awt.FontMetrics java.awt.Component.getFontMetrics(java.awt.Font)
meth public java.awt.Graphics java.awt.Component.getGraphics()
meth public java.awt.GraphicsConfiguration java.awt.Window.getGraphicsConfiguration()
meth public java.awt.Image java.awt.Component.createImage(int,int)
meth public java.awt.Image java.awt.Component.createImage(java.awt.image.ImageProducer)
meth public java.awt.Insets java.awt.Container.getInsets()
meth public java.awt.Insets java.awt.Container.insets()
meth public java.awt.LayoutManager java.awt.Container.getLayout()
meth public java.awt.Point java.awt.Component.getLocation()
meth public java.awt.Point java.awt.Component.getLocation(java.awt.Point)
meth public java.awt.Point java.awt.Component.getLocationOnScreen()
meth public java.awt.Point java.awt.Component.getMousePosition() throws java.awt.HeadlessException
meth public java.awt.Point java.awt.Component.location()
meth public java.awt.Point java.awt.Container.getMousePosition(boolean) throws java.awt.HeadlessException
meth public java.awt.Rectangle java.awt.Component.bounds()
meth public java.awt.Rectangle java.awt.Component.getBounds()
meth public java.awt.Rectangle java.awt.Component.getBounds(java.awt.Rectangle)
meth public java.awt.Toolkit java.awt.Window.getToolkit()
meth public java.awt.Window java.awt.Window.getOwner()
meth public java.awt.im.InputContext java.awt.Window.getInputContext()
meth public java.awt.im.InputMethodRequests java.awt.Component.getInputMethodRequests()
meth public java.awt.image.BufferStrategy java.awt.Window.getBufferStrategy()
meth public java.awt.image.ColorModel java.awt.Component.getColorModel()
meth public java.awt.image.VolatileImage java.awt.Component.createVolatileImage(int,int)
meth public java.awt.image.VolatileImage java.awt.Component.createVolatileImage(int,int,java.awt.ImageCapabilities) throws java.awt.AWTException
meth public java.awt.peer.ComponentPeer java.awt.Component.getPeer()
meth public java.lang.String java.awt.Component.getName()
meth public java.lang.String java.awt.Component.toString()
meth public java.lang.String java.awt.Dialog.getTitle()
meth public java.util.Locale java.awt.Window.getLocale()
meth public java.util.Set java.awt.Window.getFocusTraversalKeys(int)
meth public javax.accessibility.AccessibleContext javax.swing.JDialog.getAccessibleContext()
meth public javax.swing.JLayeredPane javax.swing.JDialog.getLayeredPane()
meth public javax.swing.JMenuBar javax.swing.JDialog.getJMenuBar()
meth public javax.swing.JRootPane javax.swing.JDialog.getRootPane()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static boolean javax.swing.JDialog.isDefaultLookAndFeelDecorated()
meth public static void javax.swing.JDialog.setDefaultLookAndFeelDecorated(boolean)
meth public synchronized [Ljava.awt.event.ComponentListener; java.awt.Component.getComponentListeners()
meth public synchronized [Ljava.awt.event.ContainerListener; java.awt.Container.getContainerListeners()
meth public synchronized [Ljava.awt.event.FocusListener; java.awt.Component.getFocusListeners()
meth public synchronized [Ljava.awt.event.HierarchyBoundsListener; java.awt.Component.getHierarchyBoundsListeners()
meth public synchronized [Ljava.awt.event.HierarchyListener; java.awt.Component.getHierarchyListeners()
meth public synchronized [Ljava.awt.event.InputMethodListener; java.awt.Component.getInputMethodListeners()
meth public synchronized [Ljava.awt.event.KeyListener; java.awt.Component.getKeyListeners()
meth public synchronized [Ljava.awt.event.MouseListener; java.awt.Component.getMouseListeners()
meth public synchronized [Ljava.awt.event.MouseMotionListener; java.awt.Component.getMouseMotionListeners()
meth public synchronized [Ljava.awt.event.MouseWheelListener; java.awt.Component.getMouseWheelListeners()
meth public synchronized [Ljava.awt.event.WindowFocusListener; java.awt.Window.getWindowFocusListeners()
meth public synchronized [Ljava.awt.event.WindowListener; java.awt.Window.getWindowListeners()
meth public synchronized [Ljava.awt.event.WindowStateListener; java.awt.Window.getWindowStateListeners()
meth public synchronized [Ljava.beans.PropertyChangeListener; java.awt.Component.getPropertyChangeListeners()
meth public synchronized [Ljava.beans.PropertyChangeListener; java.awt.Component.getPropertyChangeListeners(java.lang.String)
meth public synchronized java.awt.dnd.DropTarget java.awt.Component.getDropTarget()
meth public synchronized void java.awt.Component.add(java.awt.PopupMenu)
meth public synchronized void java.awt.Component.addComponentListener(java.awt.event.ComponentListener)
meth public synchronized void java.awt.Component.addFocusListener(java.awt.event.FocusListener)
meth public synchronized void java.awt.Component.addInputMethodListener(java.awt.event.InputMethodListener)
meth public synchronized void java.awt.Component.addKeyListener(java.awt.event.KeyListener)
meth public synchronized void java.awt.Component.addMouseListener(java.awt.event.MouseListener)
meth public synchronized void java.awt.Component.addMouseMotionListener(java.awt.event.MouseMotionListener)
meth public synchronized void java.awt.Component.addMouseWheelListener(java.awt.event.MouseWheelListener)
meth public synchronized void java.awt.Component.remove(java.awt.MenuComponent)
meth public synchronized void java.awt.Component.removeComponentListener(java.awt.event.ComponentListener)
meth public synchronized void java.awt.Component.removeFocusListener(java.awt.event.FocusListener)
meth public synchronized void java.awt.Component.removeInputMethodListener(java.awt.event.InputMethodListener)
meth public synchronized void java.awt.Component.removeKeyListener(java.awt.event.KeyListener)
meth public synchronized void java.awt.Component.removeMouseListener(java.awt.event.MouseListener)
meth public synchronized void java.awt.Component.removeMouseMotionListener(java.awt.event.MouseMotionListener)
meth public synchronized void java.awt.Component.removeMouseWheelListener(java.awt.event.MouseWheelListener)
meth public synchronized void java.awt.Component.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public synchronized void java.awt.Component.setDropTarget(java.awt.dnd.DropTarget)
meth public synchronized void java.awt.Container.addContainerListener(java.awt.event.ContainerListener)
meth public synchronized void java.awt.Container.removeContainerListener(java.awt.event.ContainerListener)
meth public synchronized void java.awt.Window.addWindowFocusListener(java.awt.event.WindowFocusListener)
meth public synchronized void java.awt.Window.addWindowListener(java.awt.event.WindowListener)
meth public synchronized void java.awt.Window.addWindowStateListener(java.awt.event.WindowStateListener)
meth public synchronized void java.awt.Window.removeWindowFocusListener(java.awt.event.WindowFocusListener)
meth public synchronized void java.awt.Window.removeWindowListener(java.awt.event.WindowListener)
meth public synchronized void java.awt.Window.removeWindowStateListener(java.awt.event.WindowStateListener)
meth public void java.awt.Component.addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void java.awt.Component.addHierarchyListener(java.awt.event.HierarchyListener)
meth public void java.awt.Component.disable()
meth public void java.awt.Component.enable()
meth public void java.awt.Component.enable(boolean)
meth public void java.awt.Component.enableInputMethods(boolean)
meth public void java.awt.Component.firePropertyChange(java.lang.String,byte,byte)
meth public void java.awt.Component.firePropertyChange(java.lang.String,char,char)
meth public void java.awt.Component.firePropertyChange(java.lang.String,double,double)
meth public void java.awt.Component.firePropertyChange(java.lang.String,float,float)
meth public void java.awt.Component.firePropertyChange(java.lang.String,long,long)
meth public void java.awt.Component.firePropertyChange(java.lang.String,short,short)
meth public void java.awt.Component.list()
meth public void java.awt.Component.list(java.io.PrintStream)
meth public void java.awt.Component.list(java.io.PrintWriter)
meth public void java.awt.Component.move(int,int)
meth public void java.awt.Component.nextFocus()
meth public void java.awt.Component.paintAll(java.awt.Graphics)
meth public void java.awt.Component.printAll(java.awt.Graphics)
meth public void java.awt.Component.removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void java.awt.Component.removeHierarchyListener(java.awt.event.HierarchyListener)
meth public void java.awt.Component.repaint()
meth public void java.awt.Component.repaint(int,int,int,int)
meth public void java.awt.Component.repaint(long)
meth public void java.awt.Component.repaint(long,int,int,int,int)
meth public void java.awt.Component.requestFocus()
meth public void java.awt.Component.reshape(int,int,int,int)
meth public void java.awt.Component.resize(int,int)
meth public void java.awt.Component.resize(java.awt.Dimension)
meth public void java.awt.Component.setBackground(java.awt.Color)
meth public void java.awt.Component.setBounds(java.awt.Rectangle)
meth public void java.awt.Component.setComponentOrientation(java.awt.ComponentOrientation)
meth public void java.awt.Component.setEnabled(boolean)
meth public void java.awt.Component.setFocusTraversalKeysEnabled(boolean)
meth public void java.awt.Component.setFocusable(boolean)
meth public void java.awt.Component.setForeground(java.awt.Color)
meth public void java.awt.Component.setIgnoreRepaint(boolean)
meth public void java.awt.Component.setLocale(java.util.Locale)
meth public void java.awt.Component.setLocation(int,int)
meth public void java.awt.Component.setLocation(java.awt.Point)
meth public void java.awt.Component.setMaximumSize(java.awt.Dimension)
meth public void java.awt.Component.setMinimumSize(java.awt.Dimension)
meth public void java.awt.Component.setName(java.lang.String)
meth public void java.awt.Component.setPreferredSize(java.awt.Dimension)
meth public void java.awt.Component.setSize(int,int)
meth public void java.awt.Component.setSize(java.awt.Dimension)
meth public void java.awt.Component.setVisible(boolean)
meth public void java.awt.Component.show(boolean)
meth public void java.awt.Component.transferFocus()
meth public void java.awt.Component.transferFocusUpCycle()
meth public void java.awt.Container.add(java.awt.Component,java.lang.Object)
meth public void java.awt.Container.add(java.awt.Component,java.lang.Object,int)
meth public void java.awt.Container.applyComponentOrientation(java.awt.ComponentOrientation)
meth public void java.awt.Container.deliverEvent(java.awt.Event)
meth public void java.awt.Container.doLayout()
meth public void java.awt.Container.invalidate()
meth public void java.awt.Container.layout()
meth public void java.awt.Container.list(java.io.PrintStream,int)
meth public void java.awt.Container.list(java.io.PrintWriter,int)
meth public void java.awt.Container.paint(java.awt.Graphics)
meth public void java.awt.Container.paintComponents(java.awt.Graphics)
meth public void java.awt.Container.print(java.awt.Graphics)
meth public void java.awt.Container.printComponents(java.awt.Graphics)
meth public void java.awt.Container.remove(int)
meth public void java.awt.Container.removeAll()
meth public void java.awt.Container.removeNotify()
meth public void java.awt.Container.setFocusTraversalKeys(int,java.util.Set)
meth public void java.awt.Container.setFocusTraversalPolicy(java.awt.FocusTraversalPolicy)
meth public void java.awt.Container.setFont(java.awt.Font)
meth public void java.awt.Container.transferFocusBackward()
meth public void java.awt.Container.transferFocusDownCycle()
meth public void java.awt.Container.validate()
meth public void java.awt.Dialog.addNotify()
meth public void java.awt.Dialog.hide()
meth public void java.awt.Dialog.setModal(boolean)
meth public void java.awt.Dialog.setResizable(boolean)
meth public void java.awt.Dialog.setTitle(java.lang.String)
meth public void java.awt.Dialog.setUndecorated(boolean)
meth public void java.awt.Dialog.show()
meth public void java.awt.Window.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void java.awt.Window.applyResourceBundle(java.lang.String)
meth public void java.awt.Window.applyResourceBundle(java.util.ResourceBundle)
meth public void java.awt.Window.createBufferStrategy(int)
meth public void java.awt.Window.createBufferStrategy(int,java.awt.BufferCapabilities) throws java.awt.AWTException
meth public void java.awt.Window.dispose()
meth public void java.awt.Window.pack()
meth public void java.awt.Window.setBounds(int,int,int,int)
meth public void java.awt.Window.setCursor(java.awt.Cursor)
meth public void java.awt.Window.setFocusableWindowState(boolean)
meth public void java.awt.Window.setLocationByPlatform(boolean)
meth public void java.awt.Window.setLocationRelativeTo(java.awt.Component)
meth public void java.awt.Window.toBack()
meth public void java.awt.Window.toFront()
meth public void javax.swing.JDialog.remove(java.awt.Component)
meth public void javax.swing.JDialog.setContentPane(java.awt.Container)
meth public void javax.swing.JDialog.setDefaultCloseOperation(int)
meth public void javax.swing.JDialog.setGlassPane(java.awt.Component)
meth public void javax.swing.JDialog.setJMenuBar(javax.swing.JMenuBar)
meth public void javax.swing.JDialog.setLayeredPane(javax.swing.JLayeredPane)
meth public void javax.swing.JDialog.setLayout(java.awt.LayoutManager)
meth public void javax.swing.JDialog.update(java.awt.Graphics)
meth public void org.openide.nodes.IndexedCustomizer.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.nodes.IndexedCustomizer.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.nodes.IndexedCustomizer.setImmediateReorder(boolean)
meth public void org.openide.nodes.IndexedCustomizer.setObject(java.lang.Object)
supr javax.swing.JDialog
CLSS public org.openide.nodes.IndexedNode
cons protected IndexedNode(org.openide.nodes.Children,org.openide.nodes.Index)
cons public IndexedNode()
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_COOKIE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_LEAF
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_OPENED_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PARENT_NODE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PROPERTY_SETS
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_SHORT_DESCRIPTION
fld  protected [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.systemActions
fld  protected java.text.MessageFormat org.openide.nodes.AbstractNode.displayFormat
fld  public static final org.openide.nodes.Node org.openide.nodes.Node.EMPTY
innr public static abstract interface org.openide.nodes.Node$Cookie
innr public static abstract interface org.openide.nodes.Node$Handle
innr public static abstract org.openide.nodes.Node$IndexedProperty
innr public static abstract org.openide.nodes.Node$Property
innr public static abstract org.openide.nodes.Node$PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.createActions()
meth protected final boolean org.openide.nodes.Node.hasPropertyChangeListener()
meth protected final org.openide.nodes.CookieSet org.openide.nodes.AbstractNode.getCookieSet()
meth protected final void org.openide.nodes.Node.fireCookieChange()
meth protected final void org.openide.nodes.Node.fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireIconChange()
meth protected final void org.openide.nodes.Node.fireNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireNodeDestroyed()
meth protected final void org.openide.nodes.Node.fireOpenedIconChange()
meth protected final void org.openide.nodes.Node.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.nodes.Node.firePropertySetsChange([Lorg.openide.nodes.Node$PropertySet;,[Lorg.openide.nodes.Node$PropertySet;)
meth protected final void org.openide.nodes.Node.fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.setChildren(org.openide.nodes.Children)
meth protected java.lang.Object org.openide.nodes.Node.clone() throws java.lang.CloneNotSupportedException
meth protected org.openide.nodes.Sheet org.openide.nodes.AbstractNode.createSheet()
meth protected synchronized final org.openide.nodes.Sheet org.openide.nodes.AbstractNode.getSheet()
meth protected synchronized final void org.openide.nodes.AbstractNode.setCookieSet(org.openide.nodes.CookieSet)
meth protected synchronized final void org.openide.nodes.AbstractNode.setSheet(org.openide.nodes.Sheet)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.nodes.AbstractNode.createPasteTypes(java.awt.datatransfer.Transferable,java.util.List)
meth public [Ljavax.swing.Action; org.openide.nodes.Node.getActions(boolean)
meth public [Lorg.openide.nodes.Node$PropertySet; org.openide.nodes.AbstractNode.getPropertySets()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.AbstractNode.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.Node.getContextActions()
meth public [Lorg.openide.util.datatransfer.NewType; org.openide.nodes.AbstractNode.getNewTypes()
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.AbstractNode.canCopy()
meth public boolean org.openide.nodes.AbstractNode.canCut()
meth public boolean org.openide.nodes.AbstractNode.canDestroy()
meth public boolean org.openide.nodes.AbstractNode.canRename()
meth public boolean org.openide.nodes.IndexedNode.hasCustomizer()
meth public boolean org.openide.nodes.Node.equals(java.lang.Object)
meth public final [Lorg.openide.util.datatransfer.PasteType; org.openide.nodes.AbstractNode.getPasteTypes(java.awt.datatransfer.Transferable)
meth public final boolean org.openide.nodes.Node.isLeaf()
meth public final javax.swing.JPopupMenu org.openide.nodes.Node.getContextMenu()
meth public final org.openide.nodes.Children org.openide.nodes.Node.getChildren()
meth public final org.openide.nodes.Node org.openide.nodes.Node.getParentNode()
meth public final org.openide.util.Lookup org.openide.nodes.Node.getLookup()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.AbstractNode.setIconBaseWithExtension(java.lang.String)
meth public final void org.openide.nodes.Node.addNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.nodes.Node.removeNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.awt.Component org.openide.nodes.IndexedNode.getCustomizer()
meth public java.awt.Image org.openide.nodes.AbstractNode.getIcon(int)
meth public java.awt.Image org.openide.nodes.AbstractNode.getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable org.openide.nodes.AbstractNode.drag() throws java.io.IOException
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String org.openide.nodes.Node.getHtmlDisplayName()
meth public java.lang.String org.openide.nodes.Node.toString()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public javax.swing.Action org.openide.nodes.AbstractNode.getPreferredAction()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.AbstractNode.cloneNode()
meth public org.openide.nodes.Node$Cookie org.openide.nodes.IndexedNode.getCookie(java.lang.Class)
meth public org.openide.nodes.Node$Handle org.openide.nodes.AbstractNode.getHandle()
meth public org.openide.util.HelpCtx org.openide.nodes.AbstractNode.getHelpCtx()
meth public org.openide.util.actions.SystemAction org.openide.nodes.AbstractNode.getDefaultAction()
meth public org.openide.util.datatransfer.PasteType org.openide.nodes.AbstractNode.getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.AbstractNode.setDefaultAction(org.openide.util.actions.SystemAction)
meth public void org.openide.nodes.AbstractNode.setIconBase(java.lang.String)
meth public void org.openide.nodes.AbstractNode.setName(java.lang.String)
meth public void org.openide.nodes.Node.destroy() throws java.io.IOException
meth public void org.openide.nodes.Node.setDisplayName(java.lang.String)
meth public void org.openide.nodes.Node.setShortDescription(java.lang.String)
supr org.openide.nodes.AbstractNode
CLSS public org.openide.nodes.IndexedPropertySupport
cons public IndexedPropertySupport(java.lang.Object,java.lang.Class,java.lang.Class,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method)
fld  protected java.lang.Object org.openide.nodes.IndexedPropertySupport.instance
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.IndexedPropertySupport.canIndexedRead()
meth public boolean org.openide.nodes.IndexedPropertySupport.canIndexedWrite()
meth public boolean org.openide.nodes.IndexedPropertySupport.canRead()
meth public boolean org.openide.nodes.IndexedPropertySupport.canWrite()
meth public boolean org.openide.nodes.Node$IndexedProperty.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.IndexedPropertySupport.setDisplayName(java.lang.String)
meth public final void org.openide.nodes.IndexedPropertySupport.setName(java.lang.String)
meth public final void org.openide.nodes.IndexedPropertySupport.setShortDescription(java.lang.String)
meth public int org.openide.nodes.Node$IndexedProperty.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$IndexedProperty.getIndexedPropertyEditor()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$IndexedProperty.getElementType()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.Object org.openide.nodes.IndexedPropertySupport.getIndexedValue(int) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public java.lang.Object org.openide.nodes.IndexedPropertySupport.getValue() throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.IndexedPropertySupport.setIndexedValue(int,java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public void org.openide.nodes.IndexedPropertySupport.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.Node$IndexedProperty
CLSS public abstract org.openide.nodes.Node
cons protected Node(org.openide.nodes.Children) throws java.lang.IllegalStateException
cons protected Node(org.openide.nodes.Children,org.openide.util.Lookup) throws java.lang.IllegalStateException
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_COOKIE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_LEAF
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_NAME
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_OPENED_ICON
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PARENT_NODE
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_PROPERTY_SETS
fld  constant public static final java.lang.String org.openide.nodes.Node.PROP_SHORT_DESCRIPTION
fld  public static final org.openide.nodes.Node org.openide.nodes.Node.EMPTY
innr public static abstract interface org.openide.nodes.Node$Cookie
innr public static abstract interface org.openide.nodes.Node$Handle
innr public static abstract org.openide.nodes.Node$IndexedProperty
innr public static abstract org.openide.nodes.Node$Property
innr public static abstract org.openide.nodes.Node$PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean org.openide.nodes.Node.hasPropertyChangeListener()
meth protected final void org.openide.nodes.Node.fireCookieChange()
meth protected final void org.openide.nodes.Node.fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireIconChange()
meth protected final void org.openide.nodes.Node.fireNameChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.fireNodeDestroyed()
meth protected final void org.openide.nodes.Node.fireOpenedIconChange()
meth protected final void org.openide.nodes.Node.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.nodes.Node.firePropertySetsChange([Lorg.openide.nodes.Node$PropertySet;,[Lorg.openide.nodes.Node$PropertySet;)
meth protected final void org.openide.nodes.Node.fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void org.openide.nodes.Node.setChildren(org.openide.nodes.Children)
meth protected java.lang.Object org.openide.nodes.Node.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljavax.swing.Action; org.openide.nodes.Node.getActions(boolean)
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.Node.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.nodes.Node.getContextActions()
meth public abstract [Lorg.openide.nodes.Node$PropertySet; org.openide.nodes.Node.getPropertySets()
meth public abstract [Lorg.openide.util.datatransfer.NewType; org.openide.nodes.Node.getNewTypes()
meth public abstract [Lorg.openide.util.datatransfer.PasteType; org.openide.nodes.Node.getPasteTypes(java.awt.datatransfer.Transferable)
meth public abstract boolean org.openide.nodes.Node.canCopy()
meth public abstract boolean org.openide.nodes.Node.canCut()
meth public abstract boolean org.openide.nodes.Node.canDestroy()
meth public abstract boolean org.openide.nodes.Node.canRename()
meth public abstract boolean org.openide.nodes.Node.hasCustomizer()
meth public abstract java.awt.Component org.openide.nodes.Node.getCustomizer()
meth public abstract java.awt.Image org.openide.nodes.Node.getIcon(int)
meth public abstract java.awt.Image org.openide.nodes.Node.getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable org.openide.nodes.Node.clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable org.openide.nodes.Node.clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable org.openide.nodes.Node.drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node org.openide.nodes.Node.cloneNode()
meth public abstract org.openide.nodes.Node$Handle org.openide.nodes.Node.getHandle()
meth public abstract org.openide.util.HelpCtx org.openide.nodes.Node.getHelpCtx()
meth public abstract org.openide.util.datatransfer.PasteType org.openide.nodes.Node.getDropType(java.awt.datatransfer.Transferable,int,int)
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node.equals(java.lang.Object)
meth public final boolean org.openide.nodes.Node.isLeaf()
meth public final javax.swing.JPopupMenu org.openide.nodes.Node.getContextMenu()
meth public final org.openide.nodes.Children org.openide.nodes.Node.getChildren()
meth public final org.openide.nodes.Node org.openide.nodes.Node.getParentNode()
meth public final org.openide.util.Lookup org.openide.nodes.Node.getLookup()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.nodes.Node.addNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.nodes.Node.removeNodeListener(org.openide.nodes.NodeListener)
meth public final void org.openide.nodes.Node.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String org.openide.nodes.Node.getHtmlDisplayName()
meth public java.lang.String org.openide.nodes.Node.toString()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public javax.swing.Action org.openide.nodes.Node.getPreferredAction()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node$Cookie org.openide.nodes.Node.getCookie(java.lang.Class)
meth public org.openide.util.actions.SystemAction org.openide.nodes.Node.getDefaultAction()
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node.destroy() throws java.io.IOException
meth public void org.openide.nodes.Node.setDisplayName(java.lang.String)
meth public void org.openide.nodes.Node.setName(java.lang.String)
meth public void org.openide.nodes.Node.setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
CLSS public abstract interface org.openide.nodes.NodeAcceptor
meth public abstract boolean org.openide.nodes.NodeAcceptor.acceptNodes([Lorg.openide.nodes.Node;)
supr null
CLSS public org.openide.nodes.NodeAdapter
cons public NodeAdapter()
intf java.beans.PropertyChangeListener
intf java.util.EventListener
intf org.openide.nodes.NodeListener
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
meth public void org.openide.nodes.NodeAdapter.childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public void org.openide.nodes.NodeAdapter.childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public void org.openide.nodes.NodeAdapter.childrenReordered(org.openide.nodes.NodeReorderEvent)
meth public void org.openide.nodes.NodeAdapter.nodeDestroyed(org.openide.nodes.NodeEvent)
meth public void org.openide.nodes.NodeAdapter.propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
CLSS public org.openide.nodes.NodeEvent
cons public NodeEvent(org.openide.nodes.Node)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final org.openide.nodes.Node org.openide.nodes.NodeEvent.getNode()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.util.EventObject
CLSS public abstract interface org.openide.nodes.NodeListener
intf java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void java.beans.PropertyChangeListener.propertyChange(java.beans.PropertyChangeEvent)
meth public abstract void org.openide.nodes.NodeListener.childrenAdded(org.openide.nodes.NodeMemberEvent)
meth public abstract void org.openide.nodes.NodeListener.childrenRemoved(org.openide.nodes.NodeMemberEvent)
meth public abstract void org.openide.nodes.NodeListener.childrenReordered(org.openide.nodes.NodeReorderEvent)
meth public abstract void org.openide.nodes.NodeListener.nodeDestroyed(org.openide.nodes.NodeEvent)
supr null
CLSS public org.openide.nodes.NodeMemberEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final [Lorg.openide.nodes.Node; org.openide.nodes.NodeMemberEvent.getDelta()
meth public final boolean org.openide.nodes.NodeMemberEvent.isAddEvent()
meth public final org.openide.nodes.Node org.openide.nodes.NodeEvent.getNode()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String org.openide.nodes.NodeMemberEvent.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized [I org.openide.nodes.NodeMemberEvent.getDeltaIndices()
supr org.openide.nodes.NodeEvent
CLSS public final org.openide.nodes.NodeNotFoundException
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.NodeNotFoundException.getClosestNodeDepth()
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.String org.openide.nodes.NodeNotFoundException.getMissingChildName()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.nodes.Node org.openide.nodes.NodeNotFoundException.getClosestNode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.io.IOException
CLSS public final org.openide.nodes.NodeOp
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
meth public static [I org.openide.nodes.NodeOp.computePermutation([Lorg.openide.nodes.Node;,[Lorg.openide.nodes.Node;) throws java.lang.IllegalArgumentException
meth public static [Ljava.lang.String; org.openide.nodes.NodeOp.createPath(org.openide.nodes.Node,org.openide.nodes.Node)
meth public static [Ljavax.swing.Action; org.openide.nodes.NodeOp.findActions([Lorg.openide.nodes.Node;)
meth public static [Lorg.openide.nodes.Node$Handle; org.openide.nodes.NodeOp.toHandles([Lorg.openide.nodes.Node;)
meth public static [Lorg.openide.nodes.Node; org.openide.nodes.NodeOp.fromHandles([Lorg.openide.nodes.Node$Handle;) throws java.io.IOException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.nodes.NodeOp.getDefaultActions()
meth public static boolean org.openide.nodes.NodeOp.isSon(org.openide.nodes.Node,org.openide.nodes.Node)
meth public static javax.swing.JPopupMenu org.openide.nodes.NodeOp.findContextMenu([Lorg.openide.nodes.Node;)
meth public static org.openide.nodes.Node org.openide.nodes.NodeOp.findChild(org.openide.nodes.Node,java.lang.String)
meth public static org.openide.nodes.Node org.openide.nodes.NodeOp.findPath(org.openide.nodes.Node,[Ljava.lang.String;) throws org.openide.nodes.NodeNotFoundException
meth public static org.openide.nodes.Node org.openide.nodes.NodeOp.findPath(org.openide.nodes.Node,java.util.Enumeration) throws org.openide.nodes.NodeNotFoundException
meth public static org.openide.nodes.Node org.openide.nodes.NodeOp.findRoot(org.openide.nodes.Node)
meth public static org.openide.nodes.NodeListener org.openide.nodes.NodeOp.weakNodeListener(org.openide.nodes.NodeListener,java.lang.Object)
meth public static void org.openide.nodes.NodeOp.setDefaultActions([Lorg.openide.util.actions.SystemAction;)
supr java.lang.Object
CLSS public abstract org.openide.nodes.NodeOperation
cons protected NodeOperation()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.openide.nodes.Node; org.openide.nodes.NodeOperation.select(java.lang.String,java.lang.String,org.openide.nodes.Node,org.openide.nodes.NodeAcceptor) throws org.openide.util.UserCancelException
meth public abstract [Lorg.openide.nodes.Node; org.openide.nodes.NodeOperation.select(java.lang.String,java.lang.String,org.openide.nodes.Node,org.openide.nodes.NodeAcceptor,java.awt.Component) throws org.openide.util.UserCancelException
meth public abstract boolean org.openide.nodes.NodeOperation.customize(org.openide.nodes.Node)
meth public abstract void org.openide.nodes.NodeOperation.explore(org.openide.nodes.Node)
meth public abstract void org.openide.nodes.NodeOperation.showProperties([Lorg.openide.nodes.Node;)
meth public abstract void org.openide.nodes.NodeOperation.showProperties(org.openide.nodes.Node)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final org.openide.nodes.Node org.openide.nodes.NodeOperation.select(java.lang.String,java.lang.String,org.openide.nodes.Node) throws org.openide.util.UserCancelException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.nodes.NodeOperation org.openide.nodes.NodeOperation.getDefault()
supr java.lang.Object
CLSS public final org.openide.nodes.NodeReorderEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [I org.openide.nodes.NodeReorderEvent.getPermutation()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final org.openide.nodes.Node org.openide.nodes.NodeEvent.getNode()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.NodeReorderEvent.getPermutationSize()
meth public int org.openide.nodes.NodeReorderEvent.newIndexOf(int)
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String org.openide.nodes.NodeReorderEvent.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.openide.nodes.NodeEvent
CLSS public abstract org.openide.nodes.NodeTransfer
fld  constant public static final int org.openide.nodes.NodeTransfer.CLIPBOARD_COPY
fld  constant public static final int org.openide.nodes.NodeTransfer.CLIPBOARD_CUT
fld  constant public static final int org.openide.nodes.NodeTransfer.COPY
fld  constant public static final int org.openide.nodes.NodeTransfer.DND_COPY
fld  constant public static final int org.openide.nodes.NodeTransfer.DND_COPY_OR_MOVE
fld  constant public static final int org.openide.nodes.NodeTransfer.DND_LINK
fld  constant public static final int org.openide.nodes.NodeTransfer.DND_MOVE
fld  constant public static final int org.openide.nodes.NodeTransfer.DND_NONE
fld  constant public static final int org.openide.nodes.NodeTransfer.DND_REFERENCE
fld  constant public static final int org.openide.nodes.NodeTransfer.MOVE
innr public static abstract interface org.openide.nodes.NodeTransfer$Paste
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
meth public static [Lorg.openide.nodes.Node; org.openide.nodes.NodeTransfer.nodes(java.awt.datatransfer.Transferable,int)
meth public static org.openide.nodes.Node org.openide.nodes.NodeTransfer.node(java.awt.datatransfer.Transferable,int)
meth public static org.openide.nodes.Node$Cookie org.openide.nodes.NodeTransfer.cookie(java.awt.datatransfer.Transferable,int,java.lang.Class)
meth public static org.openide.nodes.NodeTransfer$Paste org.openide.nodes.NodeTransfer.findPaste(java.awt.datatransfer.Transferable)
meth public static org.openide.util.datatransfer.ExTransferable$Single org.openide.nodes.NodeTransfer.createPaste(org.openide.nodes.NodeTransfer$Paste)
meth public static org.openide.util.datatransfer.ExTransferable$Single org.openide.nodes.NodeTransfer.transferable(org.openide.nodes.Node,int)
supr java.lang.Object
CLSS public abstract org.openide.nodes.PropertySupport
cons public PropertySupport(java.lang.String,java.lang.Class,java.lang.String,java.lang.String,boolean,boolean)
innr public static abstract org.openide.nodes.PropertySupport$ReadOnly
innr public static abstract org.openide.nodes.PropertySupport$ReadWrite
innr public static abstract org.openide.nodes.PropertySupport$WriteOnly
innr public static final org.openide.nodes.PropertySupport$Name
innr public static org.openide.nodes.PropertySupport$Reflection
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Object org.openide.nodes.Node$Property.getValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract void org.openide.nodes.Node$Property.setValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.IllegalArgumentException,java.lang.reflect.InvocationTargetException
meth public boolean java.beans.FeatureDescriptor.isExpert()
meth public boolean java.beans.FeatureDescriptor.isHidden()
meth public boolean java.beans.FeatureDescriptor.isPreferred()
meth public boolean org.openide.nodes.Node$Property.equals(java.lang.Object)
meth public boolean org.openide.nodes.Node$Property.isDefaultValue()
meth public boolean org.openide.nodes.Node$Property.supportsDefaultValue()
meth public boolean org.openide.nodes.PropertySupport.canRead()
meth public boolean org.openide.nodes.PropertySupport.canWrite()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.nodes.Node$Property.hashCode()
meth public java.beans.PropertyEditor org.openide.nodes.Node$Property.getPropertyEditor()
meth public java.lang.Class org.openide.nodes.Node$Property.getValueType()
meth public java.lang.Object java.beans.FeatureDescriptor.getValue(java.lang.String)
meth public java.lang.String java.beans.FeatureDescriptor.getDisplayName()
meth public java.lang.String java.beans.FeatureDescriptor.getName()
meth public java.lang.String java.beans.FeatureDescriptor.getShortDescription()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.nodes.Node$Property.getHtmlDisplayName()
meth public java.util.Enumeration java.beans.FeatureDescriptor.attributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void java.beans.FeatureDescriptor.setDisplayName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setExpert(boolean)
meth public void java.beans.FeatureDescriptor.setHidden(boolean)
meth public void java.beans.FeatureDescriptor.setName(java.lang.String)
meth public void java.beans.FeatureDescriptor.setPreferred(boolean)
meth public void java.beans.FeatureDescriptor.setShortDescription(java.lang.String)
meth public void java.beans.FeatureDescriptor.setValue(java.lang.String,java.lang.Object)
meth public void org.openide.nodes.Node$Property.restoreDefaultValue() throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr org.openide.nodes.Node$Property
CLSS public final org.openide.nodes.Sheet
cons public Sheet()
fld  constant public static final java.lang.String org.openide.nodes.Sheet.EXPERT
fld  constant public static final java.lang.String org.openide.nodes.Sheet.PROPERTIES
innr public static final org.openide.nodes.Sheet$Set
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final [Lorg.openide.nodes.Node$PropertySet; org.openide.nodes.Sheet.toArray()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.nodes.Sheet org.openide.nodes.Sheet.createDefault()
meth public static org.openide.nodes.Sheet$Set org.openide.nodes.Sheet.createExpertSet()
meth public static org.openide.nodes.Sheet$Set org.openide.nodes.Sheet.createPropertiesSet()
meth public synchronized org.openide.nodes.Sheet org.openide.nodes.Sheet.cloneSheet()
meth public synchronized org.openide.nodes.Sheet$Set org.openide.nodes.Sheet.get(java.lang.String)
meth public synchronized org.openide.nodes.Sheet$Set org.openide.nodes.Sheet.put(org.openide.nodes.Sheet$Set)
meth public synchronized org.openide.nodes.Sheet$Set org.openide.nodes.Sheet.remove(java.lang.String)
meth public void org.openide.nodes.Sheet.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.nodes.Sheet.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
CLSS public abstract interface org.openide.util.actions.ActionPerformer
meth public abstract void org.openide.util.actions.ActionPerformer.performAction(org.openide.util.actions.SystemAction)
supr null
CLSS public abstract org.openide.util.actions.BooleanStateAction
cons public BooleanStateAction()
fld  constant public static final java.lang.String org.openide.util.actions.BooleanStateAction.PROP_BOOLEAN_STATE
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.BooleanStateAction.initialize()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public boolean org.openide.util.actions.BooleanStateAction.getBooleanState()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.BooleanStateAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public javax.swing.JMenuItem org.openide.util.actions.BooleanStateAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.BooleanStateAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.BooleanStateAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.BooleanStateAction.setBooleanState(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.SystemAction
CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public CallableSystemAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.util.actions.CallableSystemAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public abstract void org.openide.util.actions.CallableSystemAction.performAction()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.SystemAction
CLSS public abstract org.openide.util.actions.CallbackSystemAction
cons public CallbackSystemAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.util.actions.CallableSystemAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
CLSS public abstract org.openide.util.actions.CookieAction
cons public CookieAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected abstract [Ljava.lang.Class; org.openide.util.actions.CookieAction.cookieClasses()
meth protected abstract int org.openide.util.actions.CookieAction.mode()
meth protected abstract void org.openide.util.actions.NodeAction.performAction([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.CallableSystemAction.asynchronous()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public abstract org.openide.util.actions.NodeAction
cons public NodeAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected abstract boolean org.openide.util.actions.NodeAction.enable([Lorg.openide.nodes.Node;)
meth protected abstract void org.openide.util.actions.NodeAction.performAction([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.CallableSystemAction.asynchronous()
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public javax.swing.Action org.openide.util.actions.NodeAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
CLSS public abstract interface org.openide.util.actions.Presenter
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
supr null
CLSS public abstract org.openide.util.actions.SystemAction
cons public SystemAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public abstract void org.openide.util.actions.SystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.SharedClassObject
