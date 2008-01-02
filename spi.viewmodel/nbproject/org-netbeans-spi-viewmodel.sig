#API master signature file
#Version 1.12.1
CLSS public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
cons public NodeChanged(java.lang.Object,java.lang.Object)
cons public NodeChanged(java.lang.Object,java.lang.Object,int)
fld  constant public static final int org.netbeans.spi.viewmodel.ModelEvent$NodeChanged.CHILDREN_MASK
fld  constant public static final int org.netbeans.spi.viewmodel.ModelEvent$NodeChanged.DISPLAY_NAME_MASK
fld  constant public static final int org.netbeans.spi.viewmodel.ModelEvent$NodeChanged.ICON_MASK
fld  constant public static final int org.netbeans.spi.viewmodel.ModelEvent$NodeChanged.SHORT_DESCRIPTION_MASK
fld  protected transient java.lang.Object java.util.EventObject.source
innr public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.viewmodel.ModelEvent$NodeChanged.getChange()
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.Object org.netbeans.spi.viewmodel.ModelEvent$NodeChanged.getNode()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.viewmodel.ModelEvent
CLSS public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
cons public TableValueChanged(java.lang.Object,java.lang.Object,java.lang.String)
fld  protected transient java.lang.Object java.util.EventObject.source
innr public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.Object org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged.getNode()
meth public java.lang.String java.util.EventObject.toString()
meth public java.lang.String org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged.getColumnID()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.viewmodel.ModelEvent
CLSS public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
cons public TreeChanged(java.lang.Object)
fld  protected transient java.lang.Object java.util.EventObject.source
innr public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
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
supr org.netbeans.spi.viewmodel.ModelEvent
CLSS public static abstract interface org.netbeans.spi.viewmodel.Models$ActionPerformer
meth public abstract boolean org.netbeans.spi.viewmodel.Models$ActionPerformer.isEnabled(java.lang.Object)
meth public abstract void org.netbeans.spi.viewmodel.Models$ActionPerformer.perform([Ljava.lang.Object;)
supr null
CLSS public static final org.netbeans.spi.viewmodel.Models$CompoundModel
fld  constant public static final java.lang.String org.netbeans.spi.viewmodel.TreeModel.ROOT
intf org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.Model
intf org.netbeans.spi.viewmodel.NodeActionsProvider
intf org.netbeans.spi.viewmodel.NodeModel
intf org.netbeans.spi.viewmodel.TableModel
intf org.netbeans.spi.viewmodel.TreeExpansionModel
intf org.netbeans.spi.viewmodel.TreeModel
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.Object; org.netbeans.spi.viewmodel.Models$CompoundModel.getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public [Ljavax.swing.Action; org.netbeans.spi.viewmodel.Models$CompoundModel.getActions(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public [Lorg.netbeans.spi.viewmodel.ColumnModel; org.netbeans.spi.viewmodel.Models$CompoundModel.getColumns()
meth public [Lorg.openide.util.datatransfer.PasteType; org.netbeans.spi.viewmodel.Models$CompoundModel.getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.viewmodel.Models$CompoundModel.canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean org.netbeans.spi.viewmodel.Models$CompoundModel.canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean org.netbeans.spi.viewmodel.Models$CompoundModel.canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean org.netbeans.spi.viewmodel.Models$CompoundModel.isExpanded(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean org.netbeans.spi.viewmodel.Models$CompoundModel.isLeaf(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean org.netbeans.spi.viewmodel.Models$CompoundModel.isReadOnly(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.viewmodel.Models$CompoundModel.getChildrenCount(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable org.netbeans.spi.viewmodel.Models$CompoundModel.clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable org.netbeans.spi.viewmodel.Models$CompoundModel.clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object org.netbeans.spi.viewmodel.Models$CompoundModel.getRoot()
meth public java.lang.Object org.netbeans.spi.viewmodel.Models$CompoundModel.getValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.viewmodel.Models$CompoundModel.getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.viewmodel.Models$CompoundModel.getHelpId()
meth public java.lang.String org.netbeans.spi.viewmodel.Models$CompoundModel.getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.viewmodel.Models$CompoundModel.getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.viewmodel.Models$CompoundModel.getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String org.netbeans.spi.viewmodel.Models$CompoundModel.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.nodeCollapsed(java.lang.Object)
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.nodeExpanded(java.lang.Object)
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.performDefaultAction(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void org.netbeans.spi.viewmodel.Models$CompoundModel.setValueAt(java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
CLSS public static final org.netbeans.spi.viewmodel.Models$TreeFeatures
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.viewmodel.Models$TreeFeatures.isExpanded(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.viewmodel.Models$TreeFeatures.collapseNode(java.lang.Object)
meth public void org.netbeans.spi.viewmodel.Models$TreeFeatures.expandNode(java.lang.Object)
supr java.lang.Object
CLSS public abstract org.netbeans.spi.viewmodel.ColumnModel
cons public ColumnModel()
intf org.netbeans.spi.viewmodel.Model
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Class org.netbeans.spi.viewmodel.ColumnModel.getType()
meth public abstract java.lang.String org.netbeans.spi.viewmodel.ColumnModel.getDisplayName()
meth public abstract java.lang.String org.netbeans.spi.viewmodel.ColumnModel.getID()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.viewmodel.ColumnModel.isSortable()
meth public boolean org.netbeans.spi.viewmodel.ColumnModel.isSorted()
meth public boolean org.netbeans.spi.viewmodel.ColumnModel.isSortedDescending()
meth public boolean org.netbeans.spi.viewmodel.ColumnModel.isVisible()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.viewmodel.ColumnModel.getColumnWidth()
meth public int org.netbeans.spi.viewmodel.ColumnModel.getCurrentOrderNumber()
meth public java.beans.PropertyEditor org.netbeans.spi.viewmodel.ColumnModel.getPropertyEditor()
meth public java.lang.Character org.netbeans.spi.viewmodel.ColumnModel.getDisplayedMnemonic()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.viewmodel.ColumnModel.getNextColumnID()
meth public java.lang.String org.netbeans.spi.viewmodel.ColumnModel.getPreviuosColumnID()
meth public java.lang.String org.netbeans.spi.viewmodel.ColumnModel.getShortDescription()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.viewmodel.ColumnModel.setColumnWidth(int)
meth public void org.netbeans.spi.viewmodel.ColumnModel.setCurrentOrderNumber(int)
meth public void org.netbeans.spi.viewmodel.ColumnModel.setSorted(boolean)
meth public void org.netbeans.spi.viewmodel.ColumnModel.setSortedDescending(boolean)
meth public void org.netbeans.spi.viewmodel.ColumnModel.setVisible(boolean)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.Model
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract [Lorg.openide.util.datatransfer.PasteType; org.netbeans.spi.viewmodel.ExtendedNodeModel.getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.ExtendedNodeModel.canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.ExtendedNodeModel.canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.ExtendedNodeModel.canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable org.netbeans.spi.viewmodel.ExtendedNodeModel.clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable org.netbeans.spi.viewmodel.ExtendedNodeModel.clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.ExtendedNodeModel.getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModel.getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModel.getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModel.getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.ExtendedNodeModel.setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.NodeModel.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.NodeModel.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModelFilter
intf org.netbeans.spi.viewmodel.Model
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract [Lorg.openide.util.datatransfer.PasteType; org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.getPasteTypes(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.canCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.canCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.canRename(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.clipboardCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.clipboardCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.getIconBaseWithExtension(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModelFilter.getDisplayName(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModelFilter.getIconBase(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModelFilter.getShortDescription(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.ExtendedNodeModelFilter.setName(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.NodeModelFilter.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.NodeModelFilter.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.Model
supr null
CLSS public org.netbeans.spi.viewmodel.ModelEvent
fld  protected transient java.lang.Object java.util.EventObject.source
innr public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
innr public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
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
supr java.util.EventObject
CLSS public abstract interface org.netbeans.spi.viewmodel.ModelListener
intf java.util.EventListener
meth public abstract void org.netbeans.spi.viewmodel.ModelListener.modelChanged(org.netbeans.spi.viewmodel.ModelEvent)
supr null
CLSS public final org.netbeans.spi.viewmodel.Models
cons public Models()
fld  public static int org.netbeans.spi.viewmodel.Models.MULTISELECTION_TYPE_ALL
fld  public static int org.netbeans.spi.viewmodel.Models.MULTISELECTION_TYPE_ANY
fld  public static int org.netbeans.spi.viewmodel.Models.MULTISELECTION_TYPE_EXACTLY_ONE
fld  public static org.netbeans.spi.viewmodel.Models$CompoundModel org.netbeans.spi.viewmodel.Models.EMPTY_MODEL
innr public static abstract interface org.netbeans.spi.viewmodel.Models$ActionPerformer
innr public static final org.netbeans.spi.viewmodel.Models$CompoundModel
innr public static final org.netbeans.spi.viewmodel.Models$TreeFeatures
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
meth public static javax.swing.Action org.netbeans.spi.viewmodel.Models.createAction(java.lang.String,org.netbeans.spi.viewmodel.Models$ActionPerformer,int)
meth public static javax.swing.JComponent org.netbeans.spi.viewmodel.Models.createView(org.netbeans.spi.viewmodel.Models$CompoundModel)
meth public static org.netbeans.spi.viewmodel.Models$CompoundModel org.netbeans.spi.viewmodel.Models.createCompoundModel(java.util.List)
meth public static org.netbeans.spi.viewmodel.Models$CompoundModel org.netbeans.spi.viewmodel.Models.createCompoundModel(java.util.List,java.lang.String)
meth public static org.netbeans.spi.viewmodel.Models$TreeFeatures org.netbeans.spi.viewmodel.Models.treeFeatures(javax.swing.JComponent) throws java.lang.UnsupportedOperationException
meth public static void org.netbeans.spi.viewmodel.Models.setModelsToView(javax.swing.JComponent,org.netbeans.spi.viewmodel.Models$CompoundModel)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProvider
intf org.netbeans.spi.viewmodel.Model
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.viewmodel.NodeActionsProvider.getActions(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.NodeActionsProvider.performDefaultAction(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProviderFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.viewmodel.NodeActionsProviderFilter.getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.NodeActionsProviderFilter.performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModel.getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModel.getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModel.getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.NodeModel.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.NodeModel.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModelFilter.getDisplayName(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModelFilter.getIconBase(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String org.netbeans.spi.viewmodel.NodeModelFilter.getShortDescription(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.NodeModelFilter.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.NodeModelFilter.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.TableModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean org.netbeans.spi.viewmodel.TableModel.isReadOnly(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object org.netbeans.spi.viewmodel.TableModel.getValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.TableModel.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.TableModel.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.TableModel.setValueAt(java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.TableModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean org.netbeans.spi.viewmodel.TableModelFilter.isReadOnly(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object org.netbeans.spi.viewmodel.TableModelFilter.getValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.TableModelFilter.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.TableModelFilter.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.TableModelFilter.setValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.TreeExpansionModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean org.netbeans.spi.viewmodel.TreeExpansionModel.isExpanded(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void org.netbeans.spi.viewmodel.TreeExpansionModel.nodeCollapsed(java.lang.Object)
meth public abstract void org.netbeans.spi.viewmodel.TreeExpansionModel.nodeExpanded(java.lang.Object)
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModel
fld  constant public static final java.lang.String org.netbeans.spi.viewmodel.TreeModel.ROOT
intf org.netbeans.spi.viewmodel.Model
meth public abstract [Ljava.lang.Object; org.netbeans.spi.viewmodel.TreeModel.getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.TreeModel.isLeaf(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int org.netbeans.spi.viewmodel.TreeModel.getChildrenCount(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object org.netbeans.spi.viewmodel.TreeModel.getRoot()
meth public abstract void org.netbeans.spi.viewmodel.TreeModel.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.TreeModel.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
supr null
CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract [Ljava.lang.Object; org.netbeans.spi.viewmodel.TreeModelFilter.getChildren(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean org.netbeans.spi.viewmodel.TreeModelFilter.isLeaf(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int org.netbeans.spi.viewmodel.TreeModelFilter.getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object org.netbeans.spi.viewmodel.TreeModelFilter.getRoot(org.netbeans.spi.viewmodel.TreeModel)
meth public abstract void org.netbeans.spi.viewmodel.TreeModelFilter.addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void org.netbeans.spi.viewmodel.TreeModelFilter.removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
supr null
CLSS public org.netbeans.spi.viewmodel.UnknownTypeException
cons public UnknownTypeException(java.lang.Object)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.Exception
