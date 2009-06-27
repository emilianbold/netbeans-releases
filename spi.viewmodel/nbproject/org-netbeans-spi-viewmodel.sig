#Signature file v4.0
#Version 1.17.1

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Exception
cons public Exception()
cons public Exception(java.lang.String)
cons public Exception(java.lang.String,java.lang.Throwable)
cons public Exception(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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

CLSS public java.lang.Throwable
cons public Throwable()
cons public Throwable(java.lang.String)
cons public Throwable(java.lang.String,java.lang.Throwable)
cons public Throwable(java.lang.Throwable)
intf java.io.Serializable
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object
hfds backtrace,cause,detailMessage,serialVersionUID,stackTrace

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface org.netbeans.spi.viewmodel.CheckNodeModel
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract boolean isCheckEnabled(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean isCheckable(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Boolean isSelected(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setSelected(java.lang.Object,java.lang.Boolean) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.CheckNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract boolean isCheckEnabled(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean isCheckable(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Boolean isSelected(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setSelected(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object,java.lang.Boolean) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract org.netbeans.spi.viewmodel.ColumnModel
cons public ColumnModel()
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.Class getType()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getID()
meth public boolean isSortable()
meth public boolean isSorted()
meth public boolean isSortedDescending()
meth public boolean isVisible()
meth public int getColumnWidth()
meth public int getCurrentOrderNumber()
meth public java.beans.PropertyEditor getPropertyEditor()
meth public java.lang.Character getDisplayedMnemonic()
meth public java.lang.String getNextColumnID()
meth public java.lang.String getPreviuosColumnID()
meth public java.lang.String getShortDescription()
meth public void setColumnWidth(int)
meth public void setCurrentOrderNumber(int)
meth public void setSorted(boolean)
meth public void setSortedDescending(boolean)
meth public void setVisible(boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract boolean canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract boolean canCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRename(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBaseWithExtension(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setName(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.Model

CLSS public org.netbeans.spi.viewmodel.ModelEvent
innr public static NodeChanged
innr public static TableValueChanged
innr public static TreeChanged
supr java.util.EventObject

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
cons public NodeChanged(java.lang.Object,java.lang.Object)
cons public NodeChanged(java.lang.Object,java.lang.Object,int)
fld public final static int CHILDREN_MASK = 8
fld public final static int DISPLAY_NAME_MASK = 1
fld public final static int EXPANSION_MASK = 16
fld public final static int ICON_MASK = 2
fld public final static int SHORT_DESCRIPTION_MASK = 4
meth public int getChange()
meth public java.lang.Object getNode()
supr org.netbeans.spi.viewmodel.ModelEvent
hfds change,node

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
cons public TableValueChanged(java.lang.Object,java.lang.Object,java.lang.String)
meth public java.lang.Object getNode()
meth public java.lang.String getColumnID()
supr org.netbeans.spi.viewmodel.ModelEvent
hfds columnID,node

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
cons public TreeChanged(java.lang.Object)
supr org.netbeans.spi.viewmodel.ModelEvent

CLSS public abstract interface org.netbeans.spi.viewmodel.ModelListener
intf java.util.EventListener
meth public abstract void modelChanged(org.netbeans.spi.viewmodel.ModelEvent)

CLSS public final org.netbeans.spi.viewmodel.Models
cons public Models()
fld public static int MULTISELECTION_TYPE_ALL
fld public static int MULTISELECTION_TYPE_ANY
fld public static int MULTISELECTION_TYPE_EXACTLY_ONE
fld public static org.netbeans.spi.viewmodel.Models$CompoundModel EMPTY_MODEL
innr public abstract interface static ActionPerformer
innr public abstract static TreeFeatures
innr public final static CompoundModel
meth public static javax.swing.Action createAction(java.lang.String,org.netbeans.spi.viewmodel.Models$ActionPerformer,int)
meth public static javax.swing.JComponent createView(org.netbeans.spi.viewmodel.Models$CompoundModel)
meth public static org.netbeans.spi.viewmodel.Models$CompoundModel createCompoundModel(java.util.List)
meth public static org.netbeans.spi.viewmodel.Models$CompoundModel createCompoundModel(java.util.List,java.lang.String)
meth public static org.netbeans.spi.viewmodel.Models$TreeFeatures treeFeatures(javax.swing.JComponent)
meth public static org.openide.nodes.Node createNodes(org.netbeans.spi.viewmodel.Models$CompoundModel,org.openide.explorer.view.TreeView)
meth public static void setModelsToView(javax.swing.JComponent,org.netbeans.spi.viewmodel.Models$CompoundModel)
supr java.lang.Object
hfds defaultExpansionModels,verbose
hcls ActionSupport,CompoundNodeActionsProvider,CompoundNodeModel,CompoundTableModel,CompoundTreeExpansionModel,CompoundTreeModel,DefaultTreeExpansionModel,DefaultTreeFeatures,DelegatingNodeActionsProvider,DelegatingNodeModel,DelegatingTableModel,DelegatingTreeExpansionModel,DelegatingTreeModel,EmptyNodeActionsProvider,EmptyNodeModel,EmptyTableModel,EmptyTreeModel

CLSS public abstract interface static org.netbeans.spi.viewmodel.Models$ActionPerformer
meth public abstract boolean isEnabled(java.lang.Object)
meth public abstract void perform(java.lang.Object[])

CLSS public final static org.netbeans.spi.viewmodel.Models$CompoundModel
intf org.netbeans.spi.viewmodel.CheckNodeModel
intf org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.NodeActionsProvider
intf org.netbeans.spi.viewmodel.TableModel
intf org.netbeans.spi.viewmodel.TreeExpansionModel
intf org.netbeans.spi.viewmodel.TreeModel
meth public boolean canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isCheckEnabled(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isCheckable(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isExpanded(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isLeaf(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isReadOnly(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public int getChildrenCount(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Boolean isSelected(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object getRoot()
meth public java.lang.Object getValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object[] getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getHelpId()
meth public java.lang.String getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String toString()
meth public javax.swing.Action[] getActions(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public org.netbeans.spi.viewmodel.ColumnModel[] getColumns()
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void nodeCollapsed(java.lang.Object)
meth public void nodeExpanded(java.lang.Object)
meth public void performDefaultAction(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setSelected(java.lang.Object,java.lang.Boolean) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setValueAt(java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hfds cnodeModel,columnModels,nodeActionsProvider,nodeModel,propertiesHelpID,rp,tableModel,treeExpansionModel,treeModel

CLSS public abstract static org.netbeans.spi.viewmodel.Models$TreeFeatures
cons public TreeFeatures()
meth public abstract boolean isExpanded(java.lang.Object)
meth public abstract void collapseNode(java.lang.Object)
meth public abstract void expandNode(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProvider
intf org.netbeans.spi.viewmodel.Model
meth public abstract javax.swing.Action[] getActions(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProviderFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TableModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isReadOnly(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void setValueAt(java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TableModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void setValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeExpansionModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isExpanded(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void nodeCollapsed(java.lang.Object)
meth public abstract void nodeExpanded(java.lang.Object)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeExpansionModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isExpanded(org.netbeans.spi.viewmodel.TreeExpansionModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void nodeCollapsed(java.lang.Object)
meth public abstract void nodeExpanded(java.lang.Object)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModel
fld public final static java.lang.String ROOT = "Root"
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isLeaf(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getRoot()
meth public abstract java.lang.Object[] getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getRoot(org.netbeans.spi.viewmodel.TreeModel)
meth public abstract java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public org.netbeans.spi.viewmodel.UnknownTypeException
cons public UnknownTypeException(java.lang.Object)
supr java.lang.Exception

