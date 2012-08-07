#Signature file v4.1
#Version 1.26.1

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
intf java.lang.annotation.Annotation

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object
hfds name,ordinal

CLSS public java.lang.Object
cons public init()
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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract org.netbeans.modules.refactoring.api.AbstractRefactoring
cons protected init(org.openide.util.Lookup)
fld public final static int INIT = 0
fld public final static int PARAMETERS_CHECK = 2
fld public final static int PREPARE = 3
fld public final static int PRE_CHECK = 1
meth public final org.netbeans.modules.refactoring.api.Context getContext()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final org.netbeans.modules.refactoring.api.Problem checkParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.refactoring.api.Problem fastCheckParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.refactoring.api.Problem preCheck()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.netbeans.modules.refactoring.api.Problem prepare(org.netbeans.modules.refactoring.api.RefactoringSession)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final org.openide.util.Lookup getRefactoringSource()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void cancelRequest()
meth public final void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds PLUGIN_STEPS,cancel,currentState,gbHandlers,plugins,pluginsWithProgress,progressListener,progressSupport,refactoringSource,scope
hcls ProgressL

CLSS public final org.netbeans.modules.refactoring.api.Context
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public void add(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void remove(java.lang.Class<?>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.openide.util.Lookup
hfds delegate,instanceContent

CLSS public final org.netbeans.modules.refactoring.api.CopyRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds target

CLSS public final org.netbeans.modules.refactoring.api.MoveRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds target

CLSS public final org.netbeans.modules.refactoring.api.MultipleCopyRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds target

CLSS public final org.netbeans.modules.refactoring.api.Problem
cons public init(boolean,java.lang.String)
 anno 2 org.netbeans.api.annotations.common.NonNull()
cons public init(boolean,java.lang.String,org.netbeans.modules.refactoring.api.ProblemDetails)
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public boolean isFatal()
meth public java.lang.String getMessage()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.refactoring.api.Problem getNext()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.refactoring.api.ProblemDetails getDetails()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setNext(org.netbeans.modules.refactoring.api.Problem)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds details,fatal,message,next

CLSS public final org.netbeans.modules.refactoring.api.ProblemDetails
meth public java.lang.String getDetailsHint()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void showDetails(javax.swing.Action,org.openide.util.Cancellable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds pdi

CLSS public final org.netbeans.modules.refactoring.api.ProgressEvent
cons public init(java.lang.Object,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.Object,int,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final static int START = 1
fld public final static int STEP = 2
fld public final static int STOP = 4
meth public int getCount()
meth public int getEventId()
meth public int getOperationType()
supr java.util.EventObject
hfds count,eventId,operationType

CLSS public abstract interface org.netbeans.modules.refactoring.api.ProgressListener
intf java.util.EventListener
meth public abstract void start(org.netbeans.modules.refactoring.api.ProgressEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void step(org.netbeans.modules.refactoring.api.ProgressEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void stop(org.netbeans.modules.refactoring.api.ProgressEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.refactoring.api.RefactoringElement
fld public final static int GUARDED = 2
fld public final static int NORMAL = 0
fld public final static int READ_ONLY = 3
fld public final static int WARNING = 1
meth public boolean equals(java.lang.Object)
meth public boolean isEnabled()
meth public int getStatus()
meth public int hashCode()
meth public java.lang.String getDisplayText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getParentFile()
meth public org.openide.text.PositionBounds getPosition()
meth public org.openide.util.Lookup getLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void openInEditor()
meth public void setEnabled(boolean)
meth public void showPreview()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.refactoring.api.RefactoringSession
meth public java.util.Collection<org.netbeans.modules.refactoring.api.RefactoringElement> getRefactoringElements()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.refactoring.api.Problem doRefactoring(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.refactoring.api.Problem undoRefactoring(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.refactoring.api.RefactoringSession create(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds bag,description,finished,internalList,prepareStarted,progressSupport,realcommit,refactoringElements,undoManager
hcls ElementsCollection

CLSS public final org.netbeans.modules.refactoring.api.RenameRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isSearchInComments()
meth public java.lang.String getNewName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setNewName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setSearchInComments(boolean)
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds newName,searchInComments

CLSS public final org.netbeans.modules.refactoring.api.SafeDeleteRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isCheckInComments()
meth public void setCheckInComments(boolean)
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds checkInComments

CLSS public final org.netbeans.modules.refactoring.api.Scope
meth public java.util.Set<org.netbeans.api.fileinfo.NonRecursiveFolder> getFolders()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.openide.filesystems.FileObject> getFiles()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.openide.filesystems.FileObject> getSourceRoots()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.refactoring.api.Scope create(java.util.Collection<org.openide.filesystems.FileObject>,java.util.Collection<org.netbeans.api.fileinfo.NonRecursiveFolder>,java.util.Collection<org.openide.filesystems.FileObject>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds files,folders,sourceRoots

CLSS public final org.netbeans.modules.refactoring.api.SingleCopyRefactoring
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getNewName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.util.Lookup getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void setNewName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setTarget(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds newName,target

CLSS public final org.netbeans.modules.refactoring.api.WhereUsedQuery
cons public init(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final static java.lang.String FIND_REFERENCES = "FIND_REFERENCES"
fld public final static java.lang.String SEARCH_IN_COMMENTS = "SEARCH_IN_COMMENTS"
meth public final boolean getBooleanValue(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void putValue(java.lang.Object,java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void setRefactoringSource(org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.refactoring.api.AbstractRefactoring
hfds hash

CLSS public final org.netbeans.modules.refactoring.api.ui.ExplorerContext
cons public init()
meth public boolean isDelete()
meth public java.awt.datatransfer.Transferable getTransferable()
meth public java.lang.String getNewName()
meth public org.openide.nodes.Node getTargetNode()
meth public void setDelete(boolean)
meth public void setNewName(java.lang.String)
meth public void setTargetNode(org.openide.nodes.Node)
meth public void setTransferable(java.awt.datatransfer.Transferable)
supr java.lang.Object
hfds isDelete,newName,targetNode,transferable

CLSS public final org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory
fld public final static java.awt.event.ActionEvent DEFAULT_EVENT
meth public static javax.swing.Action editorSubmenuAction()
meth public static org.openide.util.ContextAwareAction copyAction()
meth public static org.openide.util.ContextAwareAction moveAction()
meth public static org.openide.util.ContextAwareAction popupSubmenuAction()
meth public static org.openide.util.ContextAwareAction renameAction()
meth public static org.openide.util.ContextAwareAction safeDeleteAction()
meth public static org.openide.util.ContextAwareAction whereUsedAction()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.refactoring.spi.BackupFacility
 anno 0 java.lang.Deprecated()
innr public abstract interface static Handle
meth public abstract !varargs org.netbeans.modules.refactoring.spi.BackupFacility$Handle backup(org.openide.filesystems.FileObject[]) throws java.io.IOException
meth public abstract void clear()
meth public final org.netbeans.modules.refactoring.spi.BackupFacility$Handle backup(java.util.Collection<? extends org.openide.filesystems.FileObject>) throws java.io.IOException
meth public static org.netbeans.modules.refactoring.spi.BackupFacility getDefault()
supr java.lang.Object
hfds defaultInstance
hcls DefaultHandle,DefaultImpl

CLSS public abstract interface static org.netbeans.modules.refactoring.spi.BackupFacility$Handle
 outer org.netbeans.modules.refactoring.spi.BackupFacility
meth public abstract void restore() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.refactoring.spi.GuardedBlockHandler
meth public abstract org.netbeans.modules.refactoring.api.Problem handleChange(org.netbeans.modules.refactoring.spi.RefactoringElementImplementation,java.util.Collection<org.netbeans.modules.refactoring.spi.RefactoringElementImplementation>,java.util.Collection<org.netbeans.modules.refactoring.spi.Transaction>)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory
meth public abstract org.netbeans.modules.refactoring.spi.GuardedBlockHandler createInstance(org.netbeans.modules.refactoring.api.AbstractRefactoring)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ModificationResult
meth public abstract java.lang.String getResultingSource(org.openide.filesystems.FileObject) throws java.io.IOException
meth public abstract java.util.Collection<? extends java.io.File> getNewFiles()
meth public abstract java.util.Collection<? extends org.openide.filesystems.FileObject> getModifiedFileObjects()
meth public abstract void commit() throws java.io.IOException

CLSS public org.netbeans.modules.refactoring.spi.ProblemDetailsFactory
meth public static org.netbeans.modules.refactoring.api.ProblemDetails createProblemDetails(org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation
meth public abstract java.lang.String getDetailsHint()
meth public abstract void showDetails(javax.swing.Action,org.openide.util.Cancellable)

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ProgressProvider
meth public abstract void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
meth public abstract void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)

CLSS public org.netbeans.modules.refactoring.spi.ProgressProviderAdapter
cons protected init()
intf org.netbeans.modules.refactoring.spi.ProgressProvider
meth protected final void fireProgressListenerStart(int,int)
meth protected final void fireProgressListenerStep()
meth protected final void fireProgressListenerStep(int)
meth protected final void fireProgressListenerStop()
meth public void addProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
meth public void removeProgressListener(org.netbeans.modules.refactoring.api.ProgressListener)
supr java.lang.Object
hfds progressSupport

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ReadOnlyFilesHandler
meth public abstract org.netbeans.modules.refactoring.api.Problem createProblem(org.netbeans.modules.refactoring.api.RefactoringSession,java.util.Collection)

CLSS public final org.netbeans.modules.refactoring.spi.RefactoringCommit
cons public init(java.util.Collection<? extends org.netbeans.modules.refactoring.spi.ModificationResult>)
intf org.netbeans.modules.refactoring.spi.Transaction
meth public void commit()
meth public void rollback()
supr java.lang.Object
hfds LOG,commited,ids,newFilesStored,results

CLSS public abstract interface org.netbeans.modules.refactoring.spi.RefactoringElementImplementation
fld public final static int GUARDED = 2
fld public final static int NORMAL = 0
fld public final static int READ_ONLY = 3
fld public final static int WARNING = 1
meth public abstract boolean isEnabled()
meth public abstract int getStatus()
meth public abstract java.lang.String getDisplayText()
meth public abstract java.lang.String getText()
meth public abstract org.openide.filesystems.FileObject getParentFile()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.text.PositionBounds getPosition()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void openInEditor()
meth public abstract void performChange()
meth public abstract void setEnabled(boolean)
meth public abstract void setStatus(int)
meth public abstract void showPreview()
meth public abstract void undoChange()

CLSS public final org.netbeans.modules.refactoring.spi.RefactoringElementsBag
meth public org.netbeans.modules.refactoring.api.Problem add(org.netbeans.modules.refactoring.api.AbstractRefactoring,org.netbeans.modules.refactoring.spi.RefactoringElementImplementation)
meth public org.netbeans.modules.refactoring.api.Problem addAll(org.netbeans.modules.refactoring.api.AbstractRefactoring,java.util.Collection<org.netbeans.modules.refactoring.spi.RefactoringElementImplementation>)
meth public org.netbeans.modules.refactoring.api.Problem addFileChange(org.netbeans.modules.refactoring.api.AbstractRefactoring,org.netbeans.modules.refactoring.spi.RefactoringElementImplementation)
meth public org.netbeans.modules.refactoring.api.RefactoringSession getSession()
meth public void registerTransaction(org.netbeans.modules.refactoring.spi.Transaction)
supr java.lang.Object
hfds commits,delegate,fileChanges,hasGuarded,hasReadOnly,readOnlyFiles,session

CLSS public abstract interface org.netbeans.modules.refactoring.spi.RefactoringPlugin
meth public abstract org.netbeans.modules.refactoring.api.Problem checkParameters()
meth public abstract org.netbeans.modules.refactoring.api.Problem fastCheckParameters()
meth public abstract org.netbeans.modules.refactoring.api.Problem preCheck()
meth public abstract org.netbeans.modules.refactoring.api.Problem prepare(org.netbeans.modules.refactoring.spi.RefactoringElementsBag)
meth public abstract void cancelRequest()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
meth public abstract org.netbeans.modules.refactoring.spi.RefactoringPlugin createInstance(org.netbeans.modules.refactoring.api.AbstractRefactoring)

CLSS public abstract org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation
cons public init()
intf org.netbeans.modules.refactoring.spi.RefactoringElementImplementation
meth protected java.lang.String getNewFileContent()
meth public boolean isEnabled()
meth public int getStatus()
meth public void openInEditor()
meth public void setEnabled(boolean)
meth public void setStatus(int)
meth public void showPreview()
meth public void undoChange()
supr java.lang.Object
hfds enabled,status

CLSS public abstract interface org.netbeans.modules.refactoring.spi.Transaction
meth public abstract void commit()
meth public abstract void rollback()

CLSS public abstract org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider
cons public init()
meth public boolean canCopy(org.openide.util.Lookup)
meth public boolean canDelete(org.openide.util.Lookup)
meth public boolean canFindUsages(org.openide.util.Lookup)
meth public boolean canMove(org.openide.util.Lookup)
meth public boolean canRename(org.openide.util.Lookup)
meth public void doCopy(org.openide.util.Lookup)
meth public void doDelete(org.openide.util.Lookup)
meth public void doFindUsages(org.openide.util.Lookup)
meth public void doMove(org.openide.util.Lookup)
meth public void doRename(org.openide.util.Lookup)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel
meth public abstract java.awt.Component getComponent()
meth public abstract void initialize()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI
meth public abstract java.awt.Component getCustomComponent(java.util.Collection<org.netbeans.modules.refactoring.api.RefactoringElement>)
meth public abstract java.lang.String getCustomToolTip()
meth public abstract javax.swing.Icon getCustomIcon()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.RefactoringUI
meth public abstract boolean hasParameters()
meth public abstract boolean isQuery()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring()
meth public abstract org.netbeans.modules.refactoring.api.Problem checkParameters()
meth public abstract org.netbeans.modules.refactoring.api.Problem setParameters()
meth public abstract org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel getPanel(javax.swing.event.ChangeListener)
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass
meth public abstract boolean isRefactoringBypassRequired()
meth public abstract void doRefactoringBypass() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.TreeElement
meth public abstract java.lang.Object getUserObject()
meth public abstract java.lang.String getText(boolean)
meth public abstract javax.swing.Icon getIcon()
meth public abstract org.netbeans.modules.refactoring.spi.ui.TreeElement getParent(boolean)

CLSS public final org.netbeans.modules.refactoring.spi.ui.TreeElementFactory
meth public static org.netbeans.modules.refactoring.spi.ui.TreeElement getTreeElement(java.lang.Object)
supr java.lang.Object
hfds implementations

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation
meth public abstract org.netbeans.modules.refactoring.spi.ui.TreeElement getTreeElement(java.lang.Object)
meth public abstract void cleanUp()

CLSS public final org.netbeans.modules.refactoring.spi.ui.UI
innr public final static !enum Constants
meth public static boolean setComponentForRefactoringPreview(java.awt.Component)
meth public static void openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI)
meth public static void openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI,org.netbeans.modules.refactoring.api.RefactoringSession,javax.swing.Action)
meth public static void openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI,org.openide.windows.TopComponent)
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.refactoring.spi.ui.UI$Constants
 outer org.netbeans.modules.refactoring.spi.ui.UI
fld public final static org.netbeans.modules.refactoring.spi.ui.UI$Constants REQUEST_PREVIEW
meth public static org.netbeans.modules.refactoring.spi.ui.UI$Constants valueOf(java.lang.String)
meth public static org.netbeans.modules.refactoring.spi.ui.UI$Constants[] values()
supr java.lang.Enum<org.netbeans.modules.refactoring.spi.ui.UI$Constants>

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds defaultLookup
hcls DefLookup,Empty

