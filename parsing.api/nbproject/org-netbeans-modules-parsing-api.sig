#Signature file v4.1
#Version 1.55.1

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public java.lang.Exception
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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

CLSS public java.lang.Throwable
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public final org.netbeans.modules.parsing.api.Embedding
meth public final boolean containsOriginalOffset(int)
meth public final java.lang.String getMimeType()
meth public final org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public java.lang.String toString()
meth public static org.netbeans.modules.parsing.api.Embedding create(java.util.List<org.netbeans.modules.parsing.api.Embedding>)
supr java.lang.Object
hfds TMS_VCLV,mimePath,snapshot

CLSS public final org.netbeans.modules.parsing.api.ParserManager
meth public static java.util.concurrent.Future<java.lang.Void> parseWhenScanFinished(java.lang.String,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.concurrent.Future<java.lang.Void> parseWhenScanFinished(java.util.Collection<org.netbeans.modules.parsing.api.Source>,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void parse(java.lang.String,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void parse(java.util.Collection<org.netbeans.modules.parsing.api.Source>,org.netbeans.modules.parsing.api.UserTask) throws org.netbeans.modules.parsing.spi.ParseException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds cachedParsers
hcls LazySnapshots,MimeTaskAction,MultiUserTaskAction,UserTaskAction

CLSS public final org.netbeans.modules.parsing.api.ResultIterator
meth public java.lang.Iterable<org.netbeans.modules.parsing.api.Embedding> getEmbeddings()
meth public org.netbeans.modules.parsing.api.ResultIterator getResultIterator(org.netbeans.modules.parsing.api.Embedding)
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.netbeans.modules.parsing.spi.Parser$Result getParserResult() throws org.netbeans.modules.parsing.spi.ParseException
meth public org.netbeans.modules.parsing.spi.Parser$Result getParserResult(int) throws org.netbeans.modules.parsing.spi.ParseException
supr java.lang.Object
hfds children,embeddingToResultIterator,parser,result,sourceCache,task
hcls MyAccessor

CLSS public final org.netbeans.modules.parsing.api.Snapshot
meth public int getEmbeddedOffset(int)
meth public int getOriginalOffset(int)
meth public java.lang.CharSequence getText()
meth public java.lang.String getMimeType()
meth public java.lang.String toString()
meth public org.netbeans.api.editor.mimelookup.MimePath getMimePath()
meth public org.netbeans.api.lexer.TokenHierarchy<?> getTokenHierarchy()
meth public org.netbeans.modules.parsing.api.Embedding create(int,int,java.lang.String)
meth public org.netbeans.modules.parsing.api.Embedding create(java.lang.CharSequence,java.lang.String)
meth public org.netbeans.modules.parsing.api.Source getSource()
supr java.lang.Object
hfds currentToOriginal,lineStartOffsets,mimePath,originalToCurrent,source,text,tokenHierarchy

CLSS public final org.netbeans.modules.parsing.api.Source
meth public java.lang.String getMimeType()
meth public java.lang.String toString()
meth public javax.swing.text.Document getDocument(boolean)
meth public org.netbeans.modules.parsing.api.Snapshot createSnapshot()
meth public org.openide.filesystems.FileObject getFileObject()
meth public static org.netbeans.modules.parsing.api.Source create(javax.swing.text.Document)
meth public static org.netbeans.modules.parsing.api.Source create(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds LOG,cache,cachedParser,document,eventId,fileObject,flags,instances,mimeType,preferFile,schedulerEvents,sourceModificationEvent,support,suppressListening,taskCount,unspecifiedSourceModificationEvent
hcls ASourceModificationEvent,MySourceAccessor

CLSS public abstract org.netbeans.modules.parsing.api.Task
cons public init()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.api.UserTask
cons public init()
meth public abstract void run(org.netbeans.modules.parsing.api.ResultIterator) throws java.lang.Exception
supr org.netbeans.modules.parsing.api.Task

CLSS public final org.netbeans.modules.parsing.api.indexing.IndexingManager
meth public !varargs void refreshAllIndices(boolean,boolean,java.io.File[])
meth public !varargs void refreshAllIndices(boolean,boolean,org.openide.filesystems.FileObject[])
meth public !varargs void refreshAllIndices(org.openide.filesystems.FileObject[])
meth public <%0 extends java.lang.Object> {%%0} runProtected(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public boolean isIndexing()
meth public static org.netbeans.modules.parsing.api.indexing.IndexingManager getDefault()
meth public void refreshAllIndices(java.lang.String)
meth public void refreshIndex(java.net.URL,java.util.Collection<? extends java.net.URL>)
meth public void refreshIndex(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean)
meth public void refreshIndex(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public void refreshIndexAndWait(java.net.URL,java.util.Collection<? extends java.net.URL>)
meth public void refreshIndexAndWait(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean)
meth public void refreshIndexAndWait(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean,boolean)
supr java.lang.Object
hfds inRefreshIndexAndWait,instance
hcls MyAccessor

CLSS public org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent
cons protected init(java.lang.Object,int,int)
meth public int getCaretOffset()
meth public int getMarkOffset()
meth public java.lang.String toString()
supr org.netbeans.modules.parsing.spi.SchedulerEvent
hfds caretOffset,markOffset

CLSS public abstract org.netbeans.modules.parsing.spi.EmbeddingProvider
cons public init()
meth public abstract int getPriority()
meth public abstract java.util.List<org.netbeans.modules.parsing.api.Embedding> getEmbeddings(org.netbeans.modules.parsing.api.Snapshot)
meth public final java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons protected init(org.netbeans.modules.parsing.spi.TaskIndexingMode)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final org.netbeans.modules.parsing.spi.TaskIndexingMode getIndexingMode()
supr org.netbeans.modules.parsing.spi.ParserResultTask<{org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask%0}>
hfds scanMode

CLSS public org.netbeans.modules.parsing.spi.ParseException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public final static !enum org.netbeans.modules.parsing.spi.Parser$CancelReason
 outer org.netbeans.modules.parsing.spi.Parser
fld public final static org.netbeans.modules.parsing.spi.Parser$CancelReason PARSER_RESULT_TASK
fld public final static org.netbeans.modules.parsing.spi.Parser$CancelReason SOURCE_MODIFICATION_EVENT
fld public final static org.netbeans.modules.parsing.spi.Parser$CancelReason USER_TASK
meth public static org.netbeans.modules.parsing.spi.Parser$CancelReason valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.Parser$CancelReason[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.Parser$CancelReason>

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

CLSS public abstract org.netbeans.modules.parsing.spi.ParserBasedEmbeddingProvider<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract int getPriority()
meth public abstract java.util.List<org.netbeans.modules.parsing.api.Embedding> getEmbeddings({org.netbeans.modules.parsing.spi.ParserBasedEmbeddingProvider%0})
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.ParserFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.Parser createParser(java.util.Collection<org.netbeans.modules.parsing.api.Snapshot>)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.ParserResultTask<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract int getPriority()
meth public abstract void run({org.netbeans.modules.parsing.spi.ParserResultTask%0},org.netbeans.modules.parsing.spi.SchedulerEvent)
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.Scheduler
cons public init()
fld public final static int DEFAULT_REPARSE_DELAY = 500
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> CURSOR_SENSITIVE_TASK_SCHEDULER
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> EDITOR_SENSITIVE_TASK_SCHEDULER
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> SELECTED_NODES_SENSITIVE_TASK_SCHEDULER
meth protected abstract org.netbeans.modules.parsing.spi.SchedulerEvent createSchedulerEvent(org.netbeans.modules.parsing.spi.SourceModificationEvent)
meth protected final void schedule(org.netbeans.modules.parsing.api.Source,org.netbeans.modules.parsing.spi.SchedulerEvent)
meth protected final void schedule(org.netbeans.modules.parsing.spi.SchedulerEvent)
supr java.lang.Object
hfds reparseDelay,requestProcessor,source,task
hcls Accessor

CLSS public org.netbeans.modules.parsing.spi.SchedulerEvent
cons protected init(java.lang.Object)
meth public java.lang.String toString()
supr java.util.EventObject

CLSS public abstract org.netbeans.modules.parsing.spi.SchedulerTask
meth public abstract int getPriority()
meth public abstract java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
meth public abstract void cancel()
supr org.netbeans.modules.parsing.api.Task

CLSS public org.netbeans.modules.parsing.spi.SourceModificationEvent
cons protected init(java.lang.Object)
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.Object,boolean)
meth public boolean sourceChanged()
meth public java.lang.String toString()
meth public org.netbeans.modules.parsing.api.Source getModifiedSource()
supr java.util.EventObject
hfds sourceChanged

CLSS public abstract org.netbeans.modules.parsing.spi.TaskFactory
cons public init()
meth public abstract java.util.Collection<? extends org.netbeans.modules.parsing.spi.SchedulerTask> create(org.netbeans.modules.parsing.api.Snapshot)
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.parsing.spi.TaskIndexingMode
fld public final static org.netbeans.modules.parsing.spi.TaskIndexingMode ALLOWED_DURING_SCAN
fld public final static org.netbeans.modules.parsing.spi.TaskIndexingMode DISALLOWED_DURING_SCAN
meth public static org.netbeans.modules.parsing.spi.TaskIndexingMode valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.TaskIndexingMode[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.TaskIndexingMode>

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.BinaryIndexer
cons public init()
meth protected abstract void index(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory
cons public init()
meth public abstract int getIndexVersion()
meth public abstract java.lang.String getIndexerName()
meth public abstract org.netbeans.modules.parsing.spi.indexing.BinaryIndexer createIndexer()
meth public abstract void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer
cons public init()
innr public abstract interface static !annotation Registration
meth protected abstract void index(java.util.Map<java.lang.String,? extends java.lang.Iterable<? extends org.openide.filesystems.FileObject>>,org.netbeans.modules.parsing.spi.indexing.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth protected boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth protected void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth protected void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface static !annotation org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer$Registration
 outer org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String namePattern()
meth public abstract !hasdefault java.lang.String[] mimeType()
meth public abstract !hasdefault java.lang.String[] requiredResource()
meth public abstract int indexVersion()
meth public abstract java.lang.String indexerName()

CLSS public final org.netbeans.modules.parsing.spi.indexing.Context
meth public boolean checkForEditorModifications()
meth public boolean isAllFilesIndexing()
meth public boolean isCancelled()
meth public boolean isSourceForBinaryRootIndexing()
meth public boolean isSupplementaryFilesIndexing()
meth public java.net.URL getRootURI()
meth public org.netbeans.modules.parsing.spi.indexing.SuspendStatus getSuspendStatus()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getIndexFolder()
meth public org.openide.filesystems.FileObject getRoot()
meth public void addSupplementaryFiles(java.net.URL,java.util.Collection<? extends java.net.URL>)
supr java.lang.Object
hfds allFilesJob,cancelRequest,checkForEditorModifications,factory,followUpJob,indexBaseFolder,indexFolder,indexerName,indexerVersion,indexingSupport,logContext,props,root,rootURL,sourceForBinaryRoot,suspendedStatus

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.CustomIndexer
cons public init()
meth protected abstract void index(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory
cons public init()
meth public abstract boolean supportsEmbeddedIndexers()
meth public abstract org.netbeans.modules.parsing.spi.indexing.CustomIndexer createIndexer()
supr org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
cons public init()
meth protected abstract void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
supr org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory

CLSS public org.netbeans.modules.parsing.spi.indexing.ErrorsCache
innr public abstract interface static Convertor
innr public final static !enum ErrorKind
meth public static <%0 extends java.lang.Object> void setErrors(java.net.URL,org.netbeans.modules.parsing.spi.indexing.Indexable,java.lang.Iterable<? extends {%%0}>,org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor<{%%0}>)
meth public static boolean isInError(org.openide.filesystems.FileObject,boolean)
meth public static java.util.Collection<? extends java.net.URL> getAllFilesInError(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor<%0 extends java.lang.Object>
 outer org.netbeans.modules.parsing.spi.indexing.ErrorsCache
meth public abstract int getLineNumber({org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor%0})
meth public abstract java.lang.String getMessage({org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor%0})
meth public abstract org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind getKind({org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor%0})

CLSS public final static !enum org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind
 outer org.netbeans.modules.parsing.spi.indexing.ErrorsCache
fld public final static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind ERROR
fld public final static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind ERROR_NO_BADGE
fld public final static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind WARNING
meth public static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind>

CLSS public final org.netbeans.modules.parsing.spi.indexing.Indexable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getMimeType()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getRelativePath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.net.URL getURL()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds delegate
hcls MyAccessor

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.PathRecognizer
cons public init()
meth public abstract java.util.Set<java.lang.String> getBinaryLibraryPathIds()
meth public abstract java.util.Set<java.lang.String> getLibraryPathIds()
meth public abstract java.util.Set<java.lang.String> getMimeTypes()
meth public abstract java.util.Set<java.lang.String> getSourcePathIds()
supr java.lang.Object

CLSS public abstract interface !annotation org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] binaryLibraryPathIds()
meth public abstract !hasdefault java.lang.String[] libraryPathIds()
meth public abstract !hasdefault java.lang.String[] mimeTypes()
meth public abstract !hasdefault java.lang.String[] sourcePathIds()

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory
cons public init()
meth public abstract int getIndexVersion()
meth public abstract java.lang.String getIndexerName()
meth public abstract void filesDeleted(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public abstract void filesDirty(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public final org.netbeans.modules.parsing.spi.indexing.SuspendStatus
meth public boolean isSuspended()
meth public void parkWhileSuspended() throws java.lang.InterruptedException
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.IndexDocument
meth public void addPair(java.lang.String,java.lang.String,boolean,boolean)
supr java.lang.Object
hfds spi

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.IndexResult
meth public java.lang.String getRelativePath()
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String[] getValues(java.lang.String)
meth public java.net.URL getRoot()
meth public java.net.URL getUrl()
meth public org.netbeans.modules.parsing.spi.indexing.Indexable getIndexable()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds LOG,cachedFile,cachedUrl,root,spi

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport
meth public boolean isValid()
meth public org.netbeans.modules.parsing.spi.indexing.support.IndexDocument createDocument(org.netbeans.modules.parsing.spi.indexing.Indexable)
meth public org.netbeans.modules.parsing.spi.indexing.support.IndexDocument createDocument(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport getInstance(org.netbeans.modules.parsing.spi.indexing.Context) throws java.io.IOException
meth public void addDocument(org.netbeans.modules.parsing.spi.indexing.support.IndexDocument)
meth public void markDirtyDocuments(org.netbeans.modules.parsing.spi.indexing.Indexable)
meth public void removeDocuments(org.netbeans.modules.parsing.spi.indexing.Indexable)
supr java.lang.Object
hfds LOG,context,spiFactory,spiIndex

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.QuerySupport
innr public final static !enum Kind
meth public !varargs java.util.Collection<? extends org.netbeans.modules.parsing.spi.indexing.support.IndexResult> query(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,java.lang.String[]) throws java.io.IOException
meth public !varargs static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport forRoots(java.lang.String,int,java.net.URL[]) throws java.io.IOException
meth public !varargs static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport forRoots(java.lang.String,int,org.openide.filesystems.FileObject[]) throws java.io.IOException
meth public static java.util.Collection<org.openide.filesystems.FileObject> findRoots(org.netbeans.api.project.Project,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>)
meth public static java.util.Collection<org.openide.filesystems.FileObject> findRoots(org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds LOG,indexerQuery,roots
hcls IndexerQuery

CLSS public final static !enum org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind
 outer org.netbeans.modules.parsing.spi.indexing.support.QuerySupport
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CAMEL_CASE
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CASE_INSENSITIVE_CAMEL_CASE
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CASE_INSENSITIVE_PREFIX
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CASE_INSENSITIVE_REGEXP
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind EXACT
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind PREFIX
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind REGEXP
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind>

