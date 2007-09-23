#API master signature file
#Version 1.5.0_11
CLSS public abstract org.netbeans.spi.palette.DragAndDropHandler
cons public DragAndDropHandler()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.netbeans.spi.palette.DragAndDropHandler.customize(org.openide.util.datatransfer.ExTransferable,org.openide.util.Lookup)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.palette.DragAndDropHandler.canDrop(org.openide.util.Lookup,[Ljava.awt.datatransfer.DataFlavor;,int)
meth public boolean org.netbeans.spi.palette.DragAndDropHandler.canReorderCategories(org.openide.util.Lookup)
meth public boolean org.netbeans.spi.palette.DragAndDropHandler.doDrop(org.openide.util.Lookup,java.awt.datatransfer.Transferable,int,int)
meth public boolean org.netbeans.spi.palette.DragAndDropHandler.moveCategory(org.openide.util.Lookup,int)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.palette.PaletteActions
cons public PaletteActions()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.palette.PaletteActions.getCustomCategoryActions(org.openide.util.Lookup)
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.palette.PaletteActions.getCustomItemActions(org.openide.util.Lookup)
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.palette.PaletteActions.getCustomPaletteActions()
meth public abstract [Ljavax.swing.Action; org.netbeans.spi.palette.PaletteActions.getImportActions()
meth public abstract javax.swing.Action org.netbeans.spi.palette.PaletteActions.getPreferredAction(org.openide.util.Lookup)
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
CLSS public final org.netbeans.spi.palette.PaletteController
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_HELP_ID
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_ICON_SIZE
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_IS_EXPANDED
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_IS_READONLY
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_IS_VISIBLE
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_ITEM_WIDTH
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.ATTR_SHOW_ITEM_NAMES
fld  constant public static final java.lang.String org.netbeans.spi.palette.PaletteController.PROP_SELECTED_ITEM
fld  public static final java.awt.datatransfer.DataFlavor org.netbeans.spi.palette.PaletteController.ITEM_DATA_FLAVOR
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
meth public org.openide.util.Lookup org.netbeans.spi.palette.PaletteController.getRoot()
meth public org.openide.util.Lookup org.netbeans.spi.palette.PaletteController.getSelectedCategory()
meth public org.openide.util.Lookup org.netbeans.spi.palette.PaletteController.getSelectedItem()
meth public void org.netbeans.spi.palette.PaletteController.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.spi.palette.PaletteController.clearSelection()
meth public void org.netbeans.spi.palette.PaletteController.refresh()
meth public void org.netbeans.spi.palette.PaletteController.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.netbeans.spi.palette.PaletteController.setSelectedItem(org.openide.util.Lookup,org.openide.util.Lookup)
meth public void org.netbeans.spi.palette.PaletteController.showCustomizer()
supr java.lang.Object
CLSS public final org.netbeans.spi.palette.PaletteFactory
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
meth public static org.netbeans.spi.palette.PaletteController org.netbeans.spi.palette.PaletteFactory.createPalette(java.lang.String,org.netbeans.spi.palette.PaletteActions) throws java.io.IOException
meth public static org.netbeans.spi.palette.PaletteController org.netbeans.spi.palette.PaletteFactory.createPalette(java.lang.String,org.netbeans.spi.palette.PaletteActions,org.netbeans.spi.palette.PaletteFilter,org.netbeans.spi.palette.DragAndDropHandler) throws java.io.IOException
meth public static org.netbeans.spi.palette.PaletteController org.netbeans.spi.palette.PaletteFactory.createPalette(org.openide.nodes.Node,org.netbeans.spi.palette.PaletteActions)
meth public static org.netbeans.spi.palette.PaletteController org.netbeans.spi.palette.PaletteFactory.createPalette(org.openide.nodes.Node,org.netbeans.spi.palette.PaletteActions,org.netbeans.spi.palette.PaletteFilter,org.netbeans.spi.palette.DragAndDropHandler)
supr java.lang.Object
CLSS public abstract org.netbeans.spi.palette.PaletteFilter
cons public PaletteFilter()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.spi.palette.PaletteFilter.isValidCategory(org.openide.util.Lookup)
meth public abstract boolean org.netbeans.spi.palette.PaletteFilter.isValidItem(org.openide.util.Lookup)
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
