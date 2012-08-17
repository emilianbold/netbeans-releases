#Signature file v4.1
#Version 1.60.1

CLSS public abstract interface java.io.Serializable

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

CLSS public final org.netbeans.api.editor.DialogBinding
cons public init()
meth public static void bindComponentToDocument(javax.swing.text.Document,int,int,int,javax.swing.text.JTextComponent)
meth public static void bindComponentToDocument(javax.swing.text.Document,int,int,javax.swing.text.JTextComponent)
meth public static void bindComponentToFile(org.openide.filesystems.FileObject,int,int,int,javax.swing.text.JTextComponent)
meth public static void bindComponentToFile(org.openide.filesystems.FileObject,int,int,javax.swing.text.JTextComponent)
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.api.editor.EditorActionNames
fld public final static java.lang.String gotoDeclaration = "goto-declaration"
fld public final static java.lang.String moveCodeElementDown = "move-code-element-down"
fld public final static java.lang.String moveCodeElementUp = "move-code-element-up"
fld public final static java.lang.String removeSurroundingCode = "remove-surrounding-code"
fld public final static java.lang.String toggleLineNumbers = "toggle-line-numbers"
fld public final static java.lang.String toggleNonPrintableCharacters = "toggle-non-printable-characters"
fld public final static java.lang.String toggleRectangularSelection = "toggle-rectangular-selection"
fld public final static java.lang.String toggleToolbar = "toggle-toolbar"
fld public final static java.lang.String transposeLetters = "transpose-letters"
fld public final static java.lang.String zoomTextIn = "zoom-text-in"
fld public final static java.lang.String zoomTextOut = "zoom-text-out"
supr java.lang.Object

CLSS public abstract interface !annotation org.netbeans.api.editor.EditorActionRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int menuPosition()
meth public abstract !hasdefault int popupPosition()
meth public abstract !hasdefault int toolBarPosition()
meth public abstract !hasdefault int weight()
meth public abstract !hasdefault java.lang.String iconResource()
meth public abstract !hasdefault java.lang.String menuPath()
meth public abstract !hasdefault java.lang.String menuText()
meth public abstract !hasdefault java.lang.String mimeType()
meth public abstract !hasdefault java.lang.String popupPath()
meth public abstract !hasdefault java.lang.String popupText()
meth public abstract !hasdefault java.lang.String preferencesKey()
meth public abstract !hasdefault java.lang.String shortDescription()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation org.netbeans.api.editor.EditorActionRegistrations
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.api.editor.EditorActionRegistration[] value()

CLSS public final org.netbeans.api.editor.EditorRegistry
fld public final static java.lang.String COMPONENT_REMOVED_PROPERTY = "componentRemoved"
fld public final static java.lang.String FOCUSED_DOCUMENT_PROPERTY = "focusedDocument"
fld public final static java.lang.String FOCUS_GAINED_PROPERTY = "focusGained"
fld public final static java.lang.String FOCUS_LOST_PROPERTY = "focusLost"
fld public final static java.lang.String LAST_FOCUSED_REMOVED_PROPERTY = "lastFocusedRemoved"
meth public static java.util.List<? extends javax.swing.text.JTextComponent> componentList()
meth public static javax.swing.text.JTextComponent focusedComponent()
meth public static javax.swing.text.JTextComponent lastFocusedComponent()
meth public static void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,USED_BY_CLONEABLE_EDITOR_PROPERTY,ignoredAncestorClass,items,pcs
hcls AncestorL,CloneableEditorUsageL,FocusL,Item,PackageAccessor,PropertyDocL

CLSS public final org.netbeans.api.editor.EditorUtilities
meth public static javax.swing.Action getAction(javax.swing.text.EditorKit,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.editor.document.EditorDocumentUtils
meth public static void runExclusive(javax.swing.text.Document,java.lang.Runnable)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.editor.codegen.CodeGenerator
innr public abstract interface static Factory
meth public abstract java.lang.String getDisplayName()
meth public abstract void invoke()

CLSS public abstract interface static org.netbeans.spi.editor.codegen.CodeGenerator$Factory
 outer org.netbeans.spi.editor.codegen.CodeGenerator
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CodeGenerators")
meth public abstract java.util.List<? extends org.netbeans.spi.editor.codegen.CodeGenerator> create(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CodeGeneratorContextProviders")
innr public abstract interface static Task
meth public abstract void runTaskWithinContext(org.openide.util.Lookup,org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider$Task)

CLSS public abstract interface static org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider$Task
 outer org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider
meth public abstract void run(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.editor.document.UndoableEditWrapper
meth public abstract javax.swing.undo.UndoableEdit wrap(javax.swing.undo.UndoableEdit,javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightAttributeValue<%0 extends java.lang.Object>
meth public abstract {org.netbeans.spi.editor.highlighting.HighlightAttributeValue%0} getValue(javax.swing.text.JTextComponent,javax.swing.text.Document,java.lang.Object,int,int)

CLSS public final org.netbeans.spi.editor.highlighting.HighlightsChangeEvent
cons public init(org.netbeans.spi.editor.highlighting.HighlightsContainer,int,int)
meth public int getEndOffset()
meth public int getStartOffset()
supr java.util.EventObject
hfds endOffset,startOffset

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsChangeListener
intf java.util.EventListener
meth public abstract void highlightChanged(org.netbeans.spi.editor.highlighting.HighlightsChangeEvent)

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsContainer
fld public final static java.lang.String ATTR_EXTENDS_EMPTY_LINE = "org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE"
fld public final static java.lang.String ATTR_EXTENDS_EOL = "org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL"
meth public abstract org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public abstract void addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
meth public abstract void removeHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)

CLSS public final org.netbeans.spi.editor.highlighting.HighlightsLayer
meth public java.lang.String toString()
meth public static org.netbeans.spi.editor.highlighting.HighlightsLayer create(java.lang.String,org.netbeans.spi.editor.highlighting.ZOrder,boolean,org.netbeans.spi.editor.highlighting.HighlightsContainer)
supr java.lang.Object
hfds accessor,container,fixedSize,layerTypeId,zOrder
hcls PackageAccessor

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsLayerFactory
innr public final static Context
meth public abstract org.netbeans.spi.editor.highlighting.HighlightsLayer[] createLayers(org.netbeans.spi.editor.highlighting.HighlightsLayerFactory$Context)

CLSS public final static org.netbeans.spi.editor.highlighting.HighlightsLayerFactory$Context
 outer org.netbeans.spi.editor.highlighting.HighlightsLayerFactory
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds component,document

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsSequence
fld public final static org.netbeans.spi.editor.highlighting.HighlightsSequence EMPTY
meth public abstract boolean moveNext()
meth public abstract int getEndOffset()
meth public abstract int getStartOffset()
meth public abstract javax.swing.text.AttributeSet getAttributes()

CLSS public final org.netbeans.spi.editor.highlighting.ZOrder
fld public final static org.netbeans.spi.editor.highlighting.ZOrder BOTTOM_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder CARET_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder DEFAULT_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder SHOW_OFF_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder SYNTAX_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder TOP_RACK
meth public java.lang.String toString()
meth public org.netbeans.spi.editor.highlighting.ZOrder forPosition(int)
supr java.lang.Object
hfds COMPARATOR,LOG,position,rack

CLSS public abstract org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer
cons protected init()
intf org.netbeans.spi.editor.highlighting.HighlightsContainer
meth protected final void fireHighlightsChange(int,int)
meth public abstract org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public final void addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
meth public final void removeHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
supr java.lang.Object
hfds listeners

CLSS public final org.netbeans.spi.editor.highlighting.support.OffsetsBag
cons public init(javax.swing.text.Document)
cons public init(javax.swing.text.Document,boolean)
meth public org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public void addAllHighlights(org.netbeans.spi.editor.highlighting.HighlightsSequence)
meth public void addHighlight(int,int,javax.swing.text.AttributeSet)
meth public void clear()
meth public void discard()
meth public void removeHighlights(int,int,boolean)
meth public void setHighlights(org.netbeans.spi.editor.highlighting.HighlightsSequence)
meth public void setHighlights(org.netbeans.spi.editor.highlighting.support.OffsetsBag)
supr org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer
hfds LOG,discardCaller,discardThreadId,docListener,document,lastAddIndex,lastMoveNextIndex,marks,mergeHighlights,version
hcls DocL,Mark,Seq

CLSS public final org.netbeans.spi.editor.highlighting.support.PositionsBag
cons public init(javax.swing.text.Document)
cons public init(javax.swing.text.Document,boolean)
meth public org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public void addAllHighlights(org.netbeans.spi.editor.highlighting.support.PositionsBag)
meth public void addHighlight(javax.swing.text.Position,javax.swing.text.Position,javax.swing.text.AttributeSet)
meth public void clear()
meth public void removeHighlights(int,int)
meth public void removeHighlights(javax.swing.text.Position,javax.swing.text.Position,boolean)
meth public void setHighlights(org.netbeans.spi.editor.highlighting.support.PositionsBag)
supr org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer
hfds LOG,attributes,document,marks,mergeHighlights,version
hcls Seq

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor
innr public abstract interface static Factory
innr public final static Context
meth public abstract boolean beforeRemove(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterRemove(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context)
meth public abstract void remove(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context) throws javax.swing.text.BadLocationException

CLSS public final static org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context
 outer org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor
meth public boolean isBackwardDelete()
meth public int getOffset()
meth public java.lang.String getText()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds backwardDelete,component,document,offset,removedText

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor createDeletedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
innr public abstract interface static Factory
innr public final static MutableContext
innr public static Context
meth public abstract boolean beforeInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context)
meth public abstract void insert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$MutableContext) throws javax.swing.text.BadLocationException

CLSS public static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public int getBreakInsertOffset()
meth public int getCaretOffset()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds breakInsertOffset,caretOffset,component,document

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor createTypedBreakInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public final static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$MutableContext
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public !varargs void setText(java.lang.String,int,int,int[])
supr org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context
hfds breakInsertPosition,caretPosition,insertionText,reindentBlocks

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
innr public abstract interface static Factory
innr public final static MutableContext
innr public static Context
meth public abstract boolean beforeInsert(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterInsert(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context)
meth public abstract void insert(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$MutableContext) throws javax.swing.text.BadLocationException

CLSS public static org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context
 outer org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
meth public int getOffset()
meth public java.lang.String getText()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds component,document,offset,originallyTypedText

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public final static org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$MutableContext
 outer org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
meth public java.lang.String getText()
meth public void setText(java.lang.String,int)
supr org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context
hfds caretPosition,insertionText
hcls Accessor

