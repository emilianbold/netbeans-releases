#API master signature file
#Version 1.6.1
CLSS public final org.netbeans.api.editor.fold.Fold
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.editor.fold.Fold.isCollapsed()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.editor.fold.Fold.getEndOffset()
meth public int org.netbeans.api.editor.fold.Fold.getFoldCount()
meth public int org.netbeans.api.editor.fold.Fold.getFoldIndex(org.netbeans.api.editor.fold.Fold)
meth public int org.netbeans.api.editor.fold.Fold.getStartOffset()
meth public java.lang.String org.netbeans.api.editor.fold.Fold.getDescription()
meth public java.lang.String org.netbeans.api.editor.fold.Fold.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.Fold.getFold(int)
meth public org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.Fold.getParent()
meth public org.netbeans.api.editor.fold.FoldHierarchy org.netbeans.api.editor.fold.Fold.getHierarchy()
meth public org.netbeans.api.editor.fold.FoldType org.netbeans.api.editor.fold.Fold.getType()
supr java.lang.Object
CLSS public final org.netbeans.api.editor.fold.FoldHierarchy
fld  public static final org.netbeans.api.editor.fold.FoldType org.netbeans.api.editor.fold.FoldHierarchy.ROOT_FOLD_TYPE
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.api.editor.fold.FoldHierarchy.toString()
meth public javax.swing.text.JTextComponent org.netbeans.api.editor.fold.FoldHierarchy.getComponent()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldHierarchy.getRootFold()
meth public static synchronized org.netbeans.api.editor.fold.FoldHierarchy org.netbeans.api.editor.fold.FoldHierarchy.get(javax.swing.text.JTextComponent)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.addFoldHierarchyListener(org.netbeans.api.editor.fold.FoldHierarchyListener)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.collapse(java.util.Collection)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.collapse(org.netbeans.api.editor.fold.Fold)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.expand(java.util.Collection)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.expand(org.netbeans.api.editor.fold.Fold)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.lock()
meth public void org.netbeans.api.editor.fold.FoldHierarchy.removeFoldHierarchyListener(org.netbeans.api.editor.fold.FoldHierarchyListener)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.render(java.lang.Runnable)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.toggle(org.netbeans.api.editor.fold.Fold)
meth public void org.netbeans.api.editor.fold.FoldHierarchy.unlock()
supr java.lang.Object
CLSS public final org.netbeans.api.editor.fold.FoldHierarchyEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.editor.fold.FoldHierarchyEvent.getAddedFoldCount()
meth public int org.netbeans.api.editor.fold.FoldHierarchyEvent.getAffectedEndOffset()
meth public int org.netbeans.api.editor.fold.FoldHierarchyEvent.getAffectedStartOffset()
meth public int org.netbeans.api.editor.fold.FoldHierarchyEvent.getFoldStateChangeCount()
meth public int org.netbeans.api.editor.fold.FoldHierarchyEvent.getRemovedFoldCount()
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String org.netbeans.api.editor.fold.FoldHierarchyEvent.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldHierarchyEvent.getAddedFold(int)
meth public org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldHierarchyEvent.getRemovedFold(int)
meth public org.netbeans.api.editor.fold.FoldStateChange org.netbeans.api.editor.fold.FoldHierarchyEvent.getFoldStateChange(int)
supr java.util.EventObject
CLSS public abstract interface org.netbeans.api.editor.fold.FoldHierarchyListener
intf java.util.EventListener
meth public abstract void org.netbeans.api.editor.fold.FoldHierarchyListener.foldHierarchyChanged(org.netbeans.api.editor.fold.FoldHierarchyEvent)
supr null
CLSS public final org.netbeans.api.editor.fold.FoldStateChange
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.editor.fold.FoldStateChange.isCollapsedChanged()
meth public boolean org.netbeans.api.editor.fold.FoldStateChange.isDescriptionChanged()
meth public boolean org.netbeans.api.editor.fold.FoldStateChange.isEndOffsetChanged()
meth public boolean org.netbeans.api.editor.fold.FoldStateChange.isStartOffsetChanged()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.editor.fold.FoldStateChange.getOriginalEndOffset()
meth public int org.netbeans.api.editor.fold.FoldStateChange.getOriginalStartOffset()
meth public java.lang.String org.netbeans.api.editor.fold.FoldStateChange.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldStateChange.getFold()
supr java.lang.Object
CLSS public final org.netbeans.api.editor.fold.FoldType
cons public FoldType(java.lang.String)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.editor.fold.FoldType.accepts(org.netbeans.api.editor.fold.FoldType)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.api.editor.fold.FoldType.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public final org.netbeans.api.editor.fold.FoldUtilities
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
meth public static [Lorg.netbeans.api.editor.fold.Fold; org.netbeans.api.editor.fold.FoldUtilities.childrenToArray(org.netbeans.api.editor.fold.Fold)
meth public static [Lorg.netbeans.api.editor.fold.Fold; org.netbeans.api.editor.fold.FoldUtilities.childrenToArray(org.netbeans.api.editor.fold.Fold,int,int)
meth public static boolean org.netbeans.api.editor.fold.FoldUtilities.containsOffset(org.netbeans.api.editor.fold.Fold,int)
meth public static boolean org.netbeans.api.editor.fold.FoldUtilities.isEmpty(org.netbeans.api.editor.fold.Fold)
meth public static boolean org.netbeans.api.editor.fold.FoldUtilities.isRootFold(org.netbeans.api.editor.fold.Fold)
meth public static int org.netbeans.api.editor.fold.FoldUtilities.findFoldEndIndex(org.netbeans.api.editor.fold.Fold,int)
meth public static int org.netbeans.api.editor.fold.FoldUtilities.findFoldStartIndex(org.netbeans.api.editor.fold.Fold,int)
meth public static java.util.Iterator org.netbeans.api.editor.fold.FoldUtilities.collapsedFoldIterator(org.netbeans.api.editor.fold.FoldHierarchy,int,int)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.childrenAsList(org.netbeans.api.editor.fold.Fold)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.childrenAsList(org.netbeans.api.editor.fold.Fold,int,int)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.find(org.netbeans.api.editor.fold.Fold,java.util.Collection)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.find(org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldType)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.findRecursive(org.netbeans.api.editor.fold.Fold)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.findRecursive(org.netbeans.api.editor.fold.Fold,java.util.Collection)
meth public static java.util.List org.netbeans.api.editor.fold.FoldUtilities.findRecursive(org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldType)
meth public static org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldUtilities.findCollapsedFold(org.netbeans.api.editor.fold.FoldHierarchy,int,int)
meth public static org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldUtilities.findNearestFold(org.netbeans.api.editor.fold.FoldHierarchy,int)
meth public static org.netbeans.api.editor.fold.Fold org.netbeans.api.editor.fold.FoldUtilities.findOffsetFold(org.netbeans.api.editor.fold.FoldHierarchy,int)
meth public static void org.netbeans.api.editor.fold.FoldUtilities.collapse(org.netbeans.api.editor.fold.FoldHierarchy,java.util.Collection)
meth public static void org.netbeans.api.editor.fold.FoldUtilities.collapse(org.netbeans.api.editor.fold.FoldHierarchy,org.netbeans.api.editor.fold.FoldType)
meth public static void org.netbeans.api.editor.fold.FoldUtilities.collapseAll(org.netbeans.api.editor.fold.FoldHierarchy)
meth public static void org.netbeans.api.editor.fold.FoldUtilities.expand(org.netbeans.api.editor.fold.FoldHierarchy,java.util.Collection)
meth public static void org.netbeans.api.editor.fold.FoldUtilities.expand(org.netbeans.api.editor.fold.FoldHierarchy,org.netbeans.api.editor.fold.FoldType)
meth public static void org.netbeans.api.editor.fold.FoldUtilities.expandAll(org.netbeans.api.editor.fold.FoldHierarchy)
supr java.lang.Object
CLSS public final org.netbeans.spi.editor.fold.FoldHierarchyTransaction
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
meth public void org.netbeans.spi.editor.fold.FoldHierarchyTransaction.commit()
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManager
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.changedUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.expandNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.init(org.netbeans.spi.editor.fold.FoldOperation)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.initFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.insertUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.release()
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.removeDamagedNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.removeEmptyNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void org.netbeans.spi.editor.fold.FoldManager.removeUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
supr null
CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManagerFactory
meth public abstract org.netbeans.spi.editor.fold.FoldManager org.netbeans.spi.editor.fold.FoldManagerFactory.createFoldManager()
supr null
CLSS public final org.netbeans.spi.editor.fold.FoldOperation
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.editor.fold.FoldOperation.isAddedOrBlocked(org.netbeans.api.editor.fold.Fold)
meth public boolean org.netbeans.spi.editor.fold.FoldOperation.isBlocked(org.netbeans.api.editor.fold.Fold)
meth public boolean org.netbeans.spi.editor.fold.FoldOperation.isEndDamaged(org.netbeans.api.editor.fold.Fold)
meth public boolean org.netbeans.spi.editor.fold.FoldOperation.isReleased()
meth public boolean org.netbeans.spi.editor.fold.FoldOperation.isStartDamaged(org.netbeans.api.editor.fold.Fold)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.spi.editor.fold.FoldOperation.getExtraInfo(org.netbeans.api.editor.fold.Fold)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.fold.Fold org.netbeans.spi.editor.fold.FoldOperation.addToHierarchy(org.netbeans.api.editor.fold.FoldType,java.lang.String,boolean,int,int,int,int,java.lang.Object,org.netbeans.spi.editor.fold.FoldHierarchyTransaction) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.fold.FoldHierarchy org.netbeans.spi.editor.fold.FoldOperation.getHierarchy()
meth public org.netbeans.spi.editor.fold.FoldHierarchyTransaction org.netbeans.spi.editor.fold.FoldOperation.openTransaction()
meth public static boolean org.netbeans.spi.editor.fold.FoldOperation.isBoundsValid(int,int,int,int)
meth public void org.netbeans.spi.editor.fold.FoldOperation.removeFromHierarchy(org.netbeans.api.editor.fold.Fold,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
supr java.lang.Object
