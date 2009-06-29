#Signature file v4.0
#Version 1.15.1

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract org.netbeans.spi.palette.DragAndDropHandler
cons protected DragAndDropHandler(boolean)
cons public DragAndDropHandler()
meth public abstract void customize(org.openide.util.datatransfer.ExTransferable,org.openide.util.Lookup)
meth public boolean canDrop(org.openide.util.Lookup,java.awt.datatransfer.DataFlavor[],int)
meth public boolean canReorderCategories(org.openide.util.Lookup)
meth public boolean doDrop(org.openide.util.Lookup,java.awt.datatransfer.Transferable,int,int)
meth public boolean moveCategory(org.openide.util.Lookup,int)
supr java.lang.Object
hfds defaultHandler,isTextDnDEnabled
hcls DefaultDragAndDropHandler

CLSS public abstract org.netbeans.spi.palette.PaletteActions
cons public PaletteActions()
meth public abstract javax.swing.Action getPreferredAction(org.openide.util.Lookup)
meth public abstract javax.swing.Action[] getCustomCategoryActions(org.openide.util.Lookup)
meth public abstract javax.swing.Action[] getCustomItemActions(org.openide.util.Lookup)
meth public abstract javax.swing.Action[] getCustomPaletteActions()
meth public abstract javax.swing.Action[] getImportActions()
meth public javax.swing.Action getRefreshAction()
meth public javax.swing.Action getResetAction()
supr java.lang.Object

CLSS public final org.netbeans.spi.palette.PaletteController
fld public final static java.awt.datatransfer.DataFlavor ITEM_DATA_FLAVOR
fld public final static java.lang.String ATTR_HELP_ID = "helpId"
fld public final static java.lang.String ATTR_ICON_SIZE = "iconSize"
fld public final static java.lang.String ATTR_IS_EXPANDED = "isExpanded"
fld public final static java.lang.String ATTR_IS_READONLY = "isReadonly"
fld public final static java.lang.String ATTR_IS_VISIBLE = "isVisible"
fld public final static java.lang.String ATTR_ITEM_WIDTH = "itemWidth"
fld public final static java.lang.String ATTR_SHOW_ITEM_NAMES = "showItemNames"
fld public final static java.lang.String PROP_SELECTED_ITEM = "selectedItem"
meth public org.openide.util.Lookup getRoot()
meth public org.openide.util.Lookup getSelectedCategory()
meth public org.openide.util.Lookup getSelectedItem()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void clearSelection()
meth public void refresh()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setSelectedItem(org.openide.util.Lookup,org.openide.util.Lookup)
meth public void showCustomizer()
supr java.lang.Object
hfds model,settings,support

CLSS public final org.netbeans.spi.palette.PaletteFactory
meth public static org.netbeans.spi.palette.PaletteController createPalette(java.lang.String,org.netbeans.spi.palette.PaletteActions) throws java.io.IOException
meth public static org.netbeans.spi.palette.PaletteController createPalette(java.lang.String,org.netbeans.spi.palette.PaletteActions,org.netbeans.spi.palette.PaletteFilter,org.netbeans.spi.palette.DragAndDropHandler) throws java.io.IOException
meth public static org.netbeans.spi.palette.PaletteController createPalette(org.openide.nodes.Node,org.netbeans.spi.palette.PaletteActions)
meth public static org.netbeans.spi.palette.PaletteController createPalette(org.openide.nodes.Node,org.netbeans.spi.palette.PaletteActions,org.netbeans.spi.palette.PaletteFilter,org.netbeans.spi.palette.DragAndDropHandler)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.palette.PaletteFilter
cons public PaletteFilter()
meth public abstract boolean isValidCategory(org.openide.util.Lookup)
meth public abstract boolean isValidItem(org.openide.util.Lookup)
supr java.lang.Object

CLSS public org.netbeans.spi.palette.PaletteModule
cons public PaletteModule()
meth public void restored()
supr org.openide.modules.ModuleInstall

CLSS public org.openide.modules.ModuleInstall
cons public ModuleInstall()
meth protected boolean clearSharedData()
meth public boolean closing()
meth public void close()
meth public void installed()
 anno 0 java.lang.Deprecated()
meth public void restored()
meth public void uninstalled()
meth public void updated(int,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void validate()
supr org.openide.util.SharedClassObject
hfds serialVersionUID

CLSS public abstract org.openide.util.SharedClassObject
cons protected SharedClassObject()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

