#Signature file v4.0
#Version 1.10.1

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public final org.netbeans.api.editor.fold.Fold
meth public boolean isCollapsed()
meth public int getEndOffset()
meth public int getFoldCount()
meth public int getFoldIndex(org.netbeans.api.editor.fold.Fold)
meth public int getStartOffset()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.Fold getFold(int)
meth public org.netbeans.api.editor.fold.Fold getParent()
meth public org.netbeans.api.editor.fold.FoldHierarchy getHierarchy()
meth public org.netbeans.api.editor.fold.FoldType getType()
supr java.lang.Object
hfds DEFAULT_DESCRIPTION,EMPTY_FOLD_ARRAY,children,collapsed,description,endGuardedLength,endPos,extraInfo,guardedEndPos,guardedStartPos,operation,parent,rawIndex,startGuardedLength,startPos,type

CLSS public final org.netbeans.api.editor.fold.FoldHierarchy
fld public final static org.netbeans.api.editor.fold.FoldType ROOT_FOLD_TYPE
meth public java.lang.String toString()
meth public javax.swing.text.JTextComponent getComponent()
meth public org.netbeans.api.editor.fold.Fold getRootFold()
meth public static org.netbeans.api.editor.fold.FoldHierarchy get(javax.swing.text.JTextComponent)
meth public void addFoldHierarchyListener(org.netbeans.api.editor.fold.FoldHierarchyListener)
meth public void collapse(java.util.Collection)
meth public void collapse(org.netbeans.api.editor.fold.Fold)
meth public void expand(java.util.Collection)
meth public void expand(org.netbeans.api.editor.fold.Fold)
meth public void lock()
meth public void removeFoldHierarchyListener(org.netbeans.api.editor.fold.FoldHierarchyListener)
meth public void render(java.lang.Runnable)
meth public void toggle(org.netbeans.api.editor.fold.Fold)
meth public void unlock()
supr java.lang.Object
hfds apiPackageAccessorRegistered,execution
hcls ApiPackageAccessorImpl

CLSS public final org.netbeans.api.editor.fold.FoldHierarchyEvent
meth public int getAddedFoldCount()
meth public int getAffectedEndOffset()
meth public int getAffectedStartOffset()
meth public int getFoldStateChangeCount()
meth public int getRemovedFoldCount()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.Fold getAddedFold(int)
meth public org.netbeans.api.editor.fold.Fold getRemovedFold(int)
meth public org.netbeans.api.editor.fold.FoldStateChange getFoldStateChange(int)
supr java.util.EventObject
hfds addedFolds,affectedEndOffset,affectedStartOffset,foldStateChanges,removedFolds

CLSS public abstract interface org.netbeans.api.editor.fold.FoldHierarchyListener
intf java.util.EventListener
meth public abstract void foldHierarchyChanged(org.netbeans.api.editor.fold.FoldHierarchyEvent)

CLSS public final org.netbeans.api.editor.fold.FoldStateChange
meth public boolean isCollapsedChanged()
meth public boolean isDescriptionChanged()
meth public boolean isEndOffsetChanged()
meth public boolean isStartOffsetChanged()
meth public int getOriginalEndOffset()
meth public int getOriginalStartOffset()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.fold.Fold getFold()
supr java.lang.Object
hfds COLLAPSED_CHANGED_BIT,DESCRIPTION_CHANGED_BIT,END_OFFSET_CHANGED_BIT,START_OFFSET_CHANGED_BIT,fold,originalEndOffset,originalStartOffset,stateChangeBits

CLSS public final org.netbeans.api.editor.fold.FoldType
cons public FoldType(java.lang.String)
meth public boolean accepts(org.netbeans.api.editor.fold.FoldType)
meth public java.lang.String toString()
supr java.lang.Object
hfds description

CLSS public final org.netbeans.api.editor.fold.FoldUtilities
meth public static boolean containsOffset(org.netbeans.api.editor.fold.Fold,int)
meth public static boolean isEmpty(org.netbeans.api.editor.fold.Fold)
meth public static boolean isRootFold(org.netbeans.api.editor.fold.Fold)
meth public static int findFoldEndIndex(org.netbeans.api.editor.fold.Fold,int)
meth public static int findFoldStartIndex(org.netbeans.api.editor.fold.Fold,int)
meth public static java.util.Iterator collapsedFoldIterator(org.netbeans.api.editor.fold.FoldHierarchy,int,int)
meth public static java.util.List childrenAsList(org.netbeans.api.editor.fold.Fold)
meth public static java.util.List childrenAsList(org.netbeans.api.editor.fold.Fold,int,int)
meth public static java.util.List find(org.netbeans.api.editor.fold.Fold,java.util.Collection)
meth public static java.util.List find(org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldType)
meth public static java.util.List findRecursive(org.netbeans.api.editor.fold.Fold)
meth public static java.util.List findRecursive(org.netbeans.api.editor.fold.Fold,java.util.Collection)
meth public static java.util.List findRecursive(org.netbeans.api.editor.fold.Fold,org.netbeans.api.editor.fold.FoldType)
meth public static org.netbeans.api.editor.fold.Fold findCollapsedFold(org.netbeans.api.editor.fold.FoldHierarchy,int,int)
meth public static org.netbeans.api.editor.fold.Fold findNearestFold(org.netbeans.api.editor.fold.FoldHierarchy,int)
meth public static org.netbeans.api.editor.fold.Fold findOffsetFold(org.netbeans.api.editor.fold.FoldHierarchy,int)
meth public static org.netbeans.api.editor.fold.Fold[] childrenToArray(org.netbeans.api.editor.fold.Fold)
meth public static org.netbeans.api.editor.fold.Fold[] childrenToArray(org.netbeans.api.editor.fold.Fold,int,int)
meth public static void collapse(org.netbeans.api.editor.fold.FoldHierarchy,java.util.Collection)
meth public static void collapse(org.netbeans.api.editor.fold.FoldHierarchy,org.netbeans.api.editor.fold.FoldType)
meth public static void collapseAll(org.netbeans.api.editor.fold.FoldHierarchy)
meth public static void expand(org.netbeans.api.editor.fold.FoldHierarchy,java.util.Collection)
meth public static void expand(org.netbeans.api.editor.fold.FoldHierarchy,org.netbeans.api.editor.fold.FoldType)
meth public static void expandAll(org.netbeans.api.editor.fold.FoldHierarchy)
supr java.lang.Object

CLSS public final org.netbeans.spi.editor.fold.FoldHierarchyTransaction
meth public void commit()
supr java.lang.Object
hfds impl

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManager
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void expandNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void init(org.netbeans.spi.editor.fold.FoldOperation)
meth public abstract void initFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void release()
meth public abstract void removeDamagedNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void removeEmptyNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManagerFactory
meth public abstract org.netbeans.spi.editor.fold.FoldManager createFoldManager()

CLSS public final org.netbeans.spi.editor.fold.FoldOperation
meth public boolean isAddedOrBlocked(org.netbeans.api.editor.fold.Fold)
meth public boolean isBlocked(org.netbeans.api.editor.fold.Fold)
meth public boolean isEndDamaged(org.netbeans.api.editor.fold.Fold)
meth public boolean isReleased()
meth public boolean isStartDamaged(org.netbeans.api.editor.fold.Fold)
meth public boolean owns(org.netbeans.api.editor.fold.Fold)
meth public java.lang.Object getExtraInfo(org.netbeans.api.editor.fold.Fold)
meth public org.netbeans.api.editor.fold.Fold addToHierarchy(org.netbeans.api.editor.fold.FoldType,java.lang.String,boolean,int,int,int,int,java.lang.Object,org.netbeans.spi.editor.fold.FoldHierarchyTransaction) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.fold.FoldHierarchy getHierarchy()
meth public org.netbeans.spi.editor.fold.FoldHierarchyTransaction openTransaction()
meth public static boolean isBoundsValid(int,int,int,int)
meth public void removeFromHierarchy(org.netbeans.api.editor.fold.Fold,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
supr java.lang.Object
hfds impl,spiPackageAccessorRegistered
hcls SpiPackageAccessorImpl

