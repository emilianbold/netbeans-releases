#API master signature file
#Version 1.19
CLSS public static abstract interface java.util.Map$Entry
meth public abstract boolean java.util.Map$Entry.equals(java.lang.Object)
meth public abstract int java.util.Map$Entry.hashCode()
meth public abstract java.lang.Object java.util.Map$Entry.getKey()
meth public abstract java.lang.Object java.util.Map$Entry.getValue()
meth public abstract java.lang.Object java.util.Map$Entry.setValue(java.lang.Object)
supr null
CLSS public final org.netbeans.api.project.ant.AntBuildExtender$Extension
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
meth public void org.netbeans.api.project.ant.AntBuildExtender$Extension.addDependency(java.lang.String,java.lang.String)
meth public void org.netbeans.api.project.ant.AntBuildExtender$Extension.removeDependency(java.lang.String,java.lang.String)
supr java.lang.Object
CLSS public static abstract interface org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler
meth public abstract org.netbeans.api.project.libraries.Library org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler.importLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
supr null
CLSS public static abstract interface org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton$AntProjectHelperCallback
meth public abstract org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton$AntProjectHelperCallback.createHelper(org.openide.filesystems.FileObject,org.w3c.dom.Document,org.netbeans.spi.project.ProjectState,org.netbeans.spi.project.support.ant.AntBasedProjectType)
meth public abstract void org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton$AntProjectHelperCallback.save(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException
supr null
CLSS public static abstract interface org.netbeans.modules.project.ant.UserQuestionHandler$Callback
meth public abstract void org.netbeans.modules.project.ant.UserQuestionHandler$Callback.accepted()
meth public abstract void org.netbeans.modules.project.ant.UserQuestionHandler$Callback.denied()
meth public abstract void org.netbeans.modules.project.ant.UserQuestionHandler$Callback.error(java.io.IOException)
supr null
CLSS public static final org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
cons public RawReference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Properties) throws java.lang.IllegalArgumentException
cons public RawReference(java.lang.String,java.lang.String,java.net.URI,java.lang.String,java.lang.String,java.lang.String) throws java.lang.IllegalArgumentException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getArtifactType()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getCleanTargetName()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getForeignProjectName()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getID()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getScriptLocationValue()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getTargetName()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.toString()
meth public java.net.URI org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getScriptLocation()
meth public java.util.Properties org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.getProperties()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference.toAntArtifact(org.netbeans.spi.project.support.ant.ReferenceHelper)
supr java.lang.Object
CLSS public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
meth public abstract void org.openide.filesystems.FileSystem$AtomicAction.run() throws java.io.IOException
supr null
CLSS public static abstract interface org.openide.util.Mutex$Action
intf org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$Action.run()
supr null
CLSS public static abstract interface org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$ExceptionAction.run() throws java.lang.Exception
supr null
CLSS public abstract org.netbeans.api.project.ant.AntArtifact
cons protected AntArtifact()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.net.URI; org.netbeans.api.project.ant.AntArtifact.getArtifactLocations()
meth public abstract java.io.File org.netbeans.api.project.ant.AntArtifact.getScriptLocation()
meth public abstract java.lang.String org.netbeans.api.project.ant.AntArtifact.getCleanTargetName()
meth public abstract java.lang.String org.netbeans.api.project.ant.AntArtifact.getTargetName()
meth public abstract java.lang.String org.netbeans.api.project.ant.AntArtifact.getType()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final [Lorg.openide.filesystems.FileObject; org.netbeans.api.project.ant.AntArtifact.getArtifactFiles()
meth public final org.openide.filesystems.FileObject org.netbeans.api.project.ant.AntArtifact.getArtifactFile()
meth public final org.openide.filesystems.FileObject org.netbeans.api.project.ant.AntArtifact.getScriptFile()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.project.ant.AntArtifact.getID()
meth public java.net.URI org.netbeans.api.project.ant.AntArtifact.getArtifactLocation()
meth public java.util.Properties org.netbeans.api.project.ant.AntArtifact.getProperties()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.Project org.netbeans.api.project.ant.AntArtifact.getProject()
supr java.lang.Object
CLSS public org.netbeans.api.project.ant.AntArtifactQuery
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
meth public static [Lorg.netbeans.api.project.ant.AntArtifact; org.netbeans.api.project.ant.AntArtifactQuery.findArtifactsByType(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.ant.AntArtifact org.netbeans.api.project.ant.AntArtifactQuery.findArtifactByID(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.ant.AntArtifact org.netbeans.api.project.ant.AntArtifactQuery.findArtifactFromFile(java.io.File)
supr java.lang.Object
CLSS public final org.netbeans.api.project.ant.AntBuildExtender
innr public final org.netbeans.api.project.ant.AntBuildExtender$Extension
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.List org.netbeans.api.project.ant.AntBuildExtender.getExtensibleTargets()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized org.netbeans.api.project.ant.AntBuildExtender$Extension org.netbeans.api.project.ant.AntBuildExtender.addExtension(java.lang.String,org.openide.filesystems.FileObject)
meth public synchronized org.netbeans.api.project.ant.AntBuildExtender$Extension org.netbeans.api.project.ant.AntBuildExtender.getExtension(java.lang.String)
meth public synchronized void org.netbeans.api.project.ant.AntBuildExtender.removeExtension(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.api.project.ant.FileChooser
cons public FileChooser(java.io.File,java.io.File)
cons public FileChooser(org.netbeans.spi.project.support.ant.AntProjectHelper,boolean)
fld  protected javax.accessibility.AccessibleContext javax.swing.JFileChooser.accessibleContext
fld  protected javax.swing.event.EventListenerList javax.swing.JComponent.listenerList
fld  protected transient javax.swing.plaf.ComponentUI javax.swing.JComponent.ui
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
fld  public static final int javax.swing.JComponent.UNDEFINED_CONDITION
fld  public static final int javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
fld  public static final int javax.swing.JComponent.WHEN_FOCUSED
fld  public static final int javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
fld  public static final int javax.swing.JFileChooser.APPROVE_OPTION
fld  public static final int javax.swing.JFileChooser.CANCEL_OPTION
fld  public static final int javax.swing.JFileChooser.CUSTOM_DIALOG
fld  public static final int javax.swing.JFileChooser.DIRECTORIES_ONLY
fld  public static final int javax.swing.JFileChooser.ERROR_OPTION
fld  public static final int javax.swing.JFileChooser.FILES_AND_DIRECTORIES
fld  public static final int javax.swing.JFileChooser.FILES_ONLY
fld  public static final int javax.swing.JFileChooser.OPEN_DIALOG
fld  public static final int javax.swing.JFileChooser.SAVE_DIALOG
fld  public static final java.lang.String javax.swing.JComponent.TOOL_TIP_TEXT_KEY
fld  public static final java.lang.String javax.swing.JFileChooser.ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.ACCESSORY_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.APPROVE_SELECTION
fld  public static final java.lang.String javax.swing.JFileChooser.CANCEL_SELECTION
fld  public static final java.lang.String javax.swing.JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.DIALOG_TITLE_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.DIRECTORY_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.FILE_FILTER_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.FILE_HIDING_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.FILE_SYSTEM_VIEW_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.FILE_VIEW_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.SELECTED_FILES_CHANGED_PROPERTY
fld  public static final java.lang.String javax.swing.JFileChooser.SELECTED_FILE_CHANGED_PROPERTY
intf java.awt.MenuContainer
intf java.awt.image.ImageObserver
intf java.io.Serializable
intf javax.accessibility.Accessible
meth protected boolean javax.swing.JComponent.processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected boolean javax.swing.JComponent.requestFocusInWindow(boolean)
meth protected final void java.awt.Component.disableEvents(long)
meth protected final void java.awt.Component.enableEvents(long)
meth protected java.awt.AWTEvent java.awt.Component.coalesceEvents(java.awt.AWTEvent,java.awt.AWTEvent)
meth protected java.awt.Graphics javax.swing.JComponent.getComponentGraphics(java.awt.Graphics)
meth protected java.lang.String javax.swing.JFileChooser.paramString()
meth protected javax.swing.JDialog javax.swing.JFileChooser.createDialog(java.awt.Component) throws java.awt.HeadlessException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.awt.Component.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void java.awt.Component.processComponentEvent(java.awt.event.ComponentEvent)
meth protected void java.awt.Component.processFocusEvent(java.awt.event.FocusEvent)
meth protected void java.awt.Component.processHierarchyBoundsEvent(java.awt.event.HierarchyEvent)
meth protected void java.awt.Component.processHierarchyEvent(java.awt.event.HierarchyEvent)
meth protected void java.awt.Component.processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void java.awt.Component.processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth protected void java.awt.Container.addImpl(java.awt.Component,java.lang.Object,int)
meth protected void java.awt.Container.processContainerEvent(java.awt.event.ContainerEvent)
meth protected void java.awt.Container.processEvent(java.awt.AWTEvent)
meth protected void java.awt.Container.validateTree()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void javax.swing.JComponent.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected void javax.swing.JComponent.paintBorder(java.awt.Graphics)
meth protected void javax.swing.JComponent.paintChildren(java.awt.Graphics)
meth protected void javax.swing.JComponent.paintComponent(java.awt.Graphics)
meth protected void javax.swing.JComponent.printBorder(java.awt.Graphics)
meth protected void javax.swing.JComponent.printChildren(java.awt.Graphics)
meth protected void javax.swing.JComponent.printComponent(java.awt.Graphics)
meth protected void javax.swing.JComponent.processComponentKeyEvent(java.awt.event.KeyEvent)
meth protected void javax.swing.JComponent.processKeyEvent(java.awt.event.KeyEvent)
meth protected void javax.swing.JComponent.processMouseEvent(java.awt.event.MouseEvent)
meth protected void javax.swing.JComponent.processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void javax.swing.JComponent.setUI(javax.swing.plaf.ComponentUI)
meth protected void javax.swing.JFileChooser.fireActionPerformed(java.lang.String)
meth protected void javax.swing.JFileChooser.setup(javax.swing.filechooser.FileSystemView)
meth public [Ljava.awt.Component; java.awt.Container.getComponents()
meth public [Ljava.awt.event.ActionListener; javax.swing.JFileChooser.getActionListeners()
meth public [Ljava.io.File; javax.swing.JFileChooser.getSelectedFiles()
meth public [Ljava.lang.String; org.netbeans.api.project.ant.FileChooser.getSelectedPaths() throws java.io.IOException
meth public [Ljava.util.EventListener; javax.swing.JComponent.getListeners(java.lang.Class)
meth public [Ljavax.swing.KeyStroke; javax.swing.JComponent.getRegisteredKeyStrokes()
meth public [Ljavax.swing.event.AncestorListener; javax.swing.JComponent.getAncestorListeners()
meth public [Ljavax.swing.filechooser.FileFilter; javax.swing.JFileChooser.getChoosableFileFilters()
meth public boolean java.awt.Component.action(java.awt.Event,java.lang.Object)
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
meth public boolean java.awt.Component.isEnabled()
meth public boolean java.awt.Component.isFocusOwner()
meth public boolean java.awt.Component.isFocusTraversable()
meth public boolean java.awt.Component.isFocusable()
meth public boolean java.awt.Component.isFontSet()
meth public boolean java.awt.Component.isForegroundSet()
meth public boolean java.awt.Component.isLightweight()
meth public boolean java.awt.Component.isMaximumSizeSet()
meth public boolean java.awt.Component.isMinimumSizeSet()
meth public boolean java.awt.Component.isPreferredSizeSet()
meth public boolean java.awt.Component.isShowing()
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
meth public boolean java.awt.Component.postEvent(java.awt.Event)
meth public boolean java.awt.Component.prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean java.awt.Component.prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public boolean java.awt.Container.areFocusTraversalKeysSet(int)
meth public boolean java.awt.Container.isAncestorOf(java.awt.Component)
meth public boolean java.awt.Container.isFocusCycleRoot()
meth public boolean java.awt.Container.isFocusCycleRoot(java.awt.Container)
meth public boolean java.awt.Container.isFocusTraversalPolicySet()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean javax.swing.JComponent.contains(int,int)
meth public boolean javax.swing.JComponent.getAutoscrolls()
meth public boolean javax.swing.JComponent.getInheritsPopupMenu()
meth public boolean javax.swing.JComponent.getVerifyInputWhenFocusTarget()
meth public boolean javax.swing.JComponent.isDoubleBuffered()
meth public boolean javax.swing.JComponent.isManagingFocus()
meth public boolean javax.swing.JComponent.isOpaque()
meth public boolean javax.swing.JComponent.isOptimizedDrawingEnabled()
meth public boolean javax.swing.JComponent.isPaintingTile()
meth public boolean javax.swing.JComponent.isRequestFocusEnabled()
meth public boolean javax.swing.JComponent.isValidateRoot()
meth public boolean javax.swing.JComponent.requestDefaultFocus()
meth public boolean javax.swing.JComponent.requestFocus(boolean)
meth public boolean javax.swing.JComponent.requestFocusInWindow()
meth public boolean javax.swing.JFileChooser.accept(java.io.File)
meth public boolean javax.swing.JFileChooser.getControlButtonsAreShown()
meth public boolean javax.swing.JFileChooser.getDragEnabled()
meth public boolean javax.swing.JFileChooser.isAcceptAllFileFilterUsed()
meth public boolean javax.swing.JFileChooser.isDirectorySelectionEnabled()
meth public boolean javax.swing.JFileChooser.isFileHidingEnabled()
meth public boolean javax.swing.JFileChooser.isFileSelectionEnabled()
meth public boolean javax.swing.JFileChooser.isMultiSelectionEnabled()
meth public boolean javax.swing.JFileChooser.isTraversable(java.io.File)
meth public boolean javax.swing.JFileChooser.removeChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public final boolean java.awt.Container.isFocusTraversalPolicyProvider()
meth public final int java.awt.Container.getComponentZOrder(java.awt.Component)
meth public final java.lang.Object java.awt.Component.getTreeLock()
meth public final java.lang.Object javax.swing.JComponent.getClientProperty(java.lang.Object)
meth public final javax.swing.ActionMap javax.swing.JComponent.getActionMap()
meth public final javax.swing.InputMap javax.swing.JComponent.getInputMap()
meth public final javax.swing.InputMap javax.swing.JComponent.getInputMap(int)
meth public final void java.awt.Component.dispatchEvent(java.awt.AWTEvent)
meth public final void java.awt.Container.setComponentZOrder(java.awt.Component,int)
meth public final void java.awt.Container.setFocusTraversalPolicyProvider(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void javax.swing.JComponent.putClientProperty(java.lang.Object,java.lang.Object)
meth public final void javax.swing.JComponent.setActionMap(javax.swing.ActionMap)
meth public final void javax.swing.JComponent.setInputMap(int,javax.swing.InputMap)
meth public float javax.swing.JComponent.getAlignmentX()
meth public float javax.swing.JComponent.getAlignmentY()
meth public int java.awt.Component.checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int java.awt.Component.checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int java.awt.Container.countComponents()
meth public int java.awt.Container.getComponentCount()
meth public int javax.swing.JComponent.getConditionForKeyStroke(javax.swing.KeyStroke)
meth public int javax.swing.JComponent.getDebugGraphicsOptions()
meth public int javax.swing.JComponent.getHeight()
meth public int javax.swing.JComponent.getWidth()
meth public int javax.swing.JComponent.getX()
meth public int javax.swing.JComponent.getY()
meth public int javax.swing.JFileChooser.getApproveButtonMnemonic()
meth public int javax.swing.JFileChooser.getDialogType()
meth public int javax.swing.JFileChooser.getFileSelectionMode()
meth public int javax.swing.JFileChooser.showDialog(java.awt.Component,java.lang.String) throws java.awt.HeadlessException
meth public int javax.swing.JFileChooser.showOpenDialog(java.awt.Component) throws java.awt.HeadlessException
meth public int javax.swing.JFileChooser.showSaveDialog(java.awt.Component) throws java.awt.HeadlessException
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
meth public java.awt.Component javax.swing.JComponent.getNextFocusableComponent()
meth public java.awt.ComponentOrientation java.awt.Component.getComponentOrientation()
meth public java.awt.Container java.awt.Component.getFocusCycleRootAncestor()
meth public java.awt.Container java.awt.Component.getParent()
meth public java.awt.Container javax.swing.JComponent.getTopLevelAncestor()
meth public java.awt.Cursor java.awt.Component.getCursor()
meth public java.awt.Dimension java.awt.Component.getSize()
meth public java.awt.Dimension java.awt.Component.size()
meth public java.awt.Dimension java.awt.Container.minimumSize()
meth public java.awt.Dimension java.awt.Container.preferredSize()
meth public java.awt.Dimension javax.swing.JComponent.getMaximumSize()
meth public java.awt.Dimension javax.swing.JComponent.getMinimumSize()
meth public java.awt.Dimension javax.swing.JComponent.getPreferredSize()
meth public java.awt.Dimension javax.swing.JComponent.getSize(java.awt.Dimension)
meth public java.awt.FocusTraversalPolicy java.awt.Container.getFocusTraversalPolicy()
meth public java.awt.Font java.awt.Component.getFont()
meth public java.awt.FontMetrics javax.swing.JComponent.getFontMetrics(java.awt.Font)
meth public java.awt.Graphics javax.swing.JComponent.getGraphics()
meth public java.awt.GraphicsConfiguration java.awt.Component.getGraphicsConfiguration()
meth public java.awt.Image java.awt.Component.createImage(int,int)
meth public java.awt.Image java.awt.Component.createImage(java.awt.image.ImageProducer)
meth public java.awt.Insets java.awt.Container.insets()
meth public java.awt.Insets javax.swing.JComponent.getInsets()
meth public java.awt.Insets javax.swing.JComponent.getInsets(java.awt.Insets)
meth public java.awt.LayoutManager java.awt.Container.getLayout()
meth public java.awt.Point java.awt.Component.getLocation()
meth public java.awt.Point java.awt.Component.getLocationOnScreen()
meth public java.awt.Point java.awt.Component.getMousePosition() throws java.awt.HeadlessException
meth public java.awt.Point java.awt.Component.location()
meth public java.awt.Point java.awt.Container.getMousePosition(boolean) throws java.awt.HeadlessException
meth public java.awt.Point javax.swing.JComponent.getLocation(java.awt.Point)
meth public java.awt.Point javax.swing.JComponent.getPopupLocation(java.awt.event.MouseEvent)
meth public java.awt.Point javax.swing.JComponent.getToolTipLocation(java.awt.event.MouseEvent)
meth public java.awt.Rectangle java.awt.Component.bounds()
meth public java.awt.Rectangle java.awt.Component.getBounds()
meth public java.awt.Rectangle javax.swing.JComponent.getBounds(java.awt.Rectangle)
meth public java.awt.Rectangle javax.swing.JComponent.getVisibleRect()
meth public java.awt.Toolkit java.awt.Component.getToolkit()
meth public java.awt.event.ActionListener javax.swing.JComponent.getActionForKeyStroke(javax.swing.KeyStroke)
meth public java.awt.im.InputContext java.awt.Component.getInputContext()
meth public java.awt.im.InputMethodRequests java.awt.Component.getInputMethodRequests()
meth public java.awt.image.ColorModel java.awt.Component.getColorModel()
meth public java.awt.image.VolatileImage java.awt.Component.createVolatileImage(int,int)
meth public java.awt.image.VolatileImage java.awt.Component.createVolatileImage(int,int,java.awt.ImageCapabilities) throws java.awt.AWTException
meth public java.awt.peer.ComponentPeer java.awt.Component.getPeer()
meth public java.io.File javax.swing.JFileChooser.getCurrentDirectory()
meth public java.io.File javax.swing.JFileChooser.getSelectedFile()
meth public java.lang.String java.awt.Component.getName()
meth public java.lang.String java.awt.Component.toString()
meth public java.lang.String javax.swing.JComponent.getToolTipText()
meth public java.lang.String javax.swing.JComponent.getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String javax.swing.JFileChooser.getApproveButtonText()
meth public java.lang.String javax.swing.JFileChooser.getApproveButtonToolTipText()
meth public java.lang.String javax.swing.JFileChooser.getDescription(java.io.File)
meth public java.lang.String javax.swing.JFileChooser.getDialogTitle()
meth public java.lang.String javax.swing.JFileChooser.getName(java.io.File)
meth public java.lang.String javax.swing.JFileChooser.getTypeDescription(java.io.File)
meth public java.lang.String javax.swing.JFileChooser.getUIClassID()
meth public java.util.Locale java.awt.Component.getLocale()
meth public java.util.Set java.awt.Container.getFocusTraversalKeys(int)
meth public javax.accessibility.AccessibleContext javax.swing.JFileChooser.getAccessibleContext()
meth public javax.swing.Icon javax.swing.JFileChooser.getIcon(java.io.File)
meth public javax.swing.InputVerifier javax.swing.JComponent.getInputVerifier()
meth public javax.swing.JComponent javax.swing.JFileChooser.getAccessory()
meth public javax.swing.JPopupMenu javax.swing.JComponent.getComponentPopupMenu()
meth public javax.swing.JRootPane javax.swing.JComponent.getRootPane()
meth public javax.swing.JToolTip javax.swing.JComponent.createToolTip()
meth public javax.swing.TransferHandler javax.swing.JComponent.getTransferHandler()
meth public javax.swing.border.Border javax.swing.JComponent.getBorder()
meth public javax.swing.filechooser.FileFilter javax.swing.JFileChooser.getAcceptAllFileFilter()
meth public javax.swing.filechooser.FileFilter javax.swing.JFileChooser.getFileFilter()
meth public javax.swing.filechooser.FileSystemView javax.swing.JFileChooser.getFileSystemView()
meth public javax.swing.filechooser.FileView javax.swing.JFileChooser.getFileView()
meth public javax.swing.plaf.FileChooserUI javax.swing.JFileChooser.getUI()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static boolean javax.swing.JComponent.isLightweightComponent(java.awt.Component)
meth public static java.util.Locale javax.swing.JComponent.getDefaultLocale()
meth public static void javax.swing.JComponent.setDefaultLocale(java.util.Locale)
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
meth public synchronized [Ljava.beans.PropertyChangeListener; java.awt.Component.getPropertyChangeListeners()
meth public synchronized [Ljava.beans.PropertyChangeListener; java.awt.Component.getPropertyChangeListeners(java.lang.String)
meth public synchronized [Ljava.beans.VetoableChangeListener; javax.swing.JComponent.getVetoableChangeListeners()
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
meth public synchronized void java.awt.Component.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public synchronized void java.awt.Component.removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public synchronized void java.awt.Component.setDropTarget(java.awt.dnd.DropTarget)
meth public synchronized void java.awt.Container.addContainerListener(java.awt.event.ContainerListener)
meth public synchronized void java.awt.Container.removeContainerListener(java.awt.event.ContainerListener)
meth public synchronized void javax.swing.JComponent.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public synchronized void javax.swing.JComponent.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void java.awt.Component.addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void java.awt.Component.addHierarchyListener(java.awt.event.HierarchyListener)
meth public void java.awt.Component.enable(boolean)
meth public void java.awt.Component.enableInputMethods(boolean)
meth public void java.awt.Component.firePropertyChange(java.lang.String,byte,byte)
meth public void java.awt.Component.firePropertyChange(java.lang.String,double,double)
meth public void java.awt.Component.firePropertyChange(java.lang.String,float,float)
meth public void java.awt.Component.firePropertyChange(java.lang.String,long,long)
meth public void java.awt.Component.firePropertyChange(java.lang.String,short,short)
meth public void java.awt.Component.hide()
meth public void java.awt.Component.list()
meth public void java.awt.Component.list(java.io.PrintStream)
meth public void java.awt.Component.list(java.io.PrintWriter)
meth public void java.awt.Component.move(int,int)
meth public void java.awt.Component.nextFocus()
meth public void java.awt.Component.paintAll(java.awt.Graphics)
meth public void java.awt.Component.removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void java.awt.Component.removeHierarchyListener(java.awt.event.HierarchyListener)
meth public void java.awt.Component.repaint()
meth public void java.awt.Component.repaint(int,int,int,int)
meth public void java.awt.Component.repaint(long)
meth public void java.awt.Component.resize(int,int)
meth public void java.awt.Component.resize(java.awt.Dimension)
meth public void java.awt.Component.setBounds(int,int,int,int)
meth public void java.awt.Component.setBounds(java.awt.Rectangle)
meth public void java.awt.Component.setComponentOrientation(java.awt.ComponentOrientation)
meth public void java.awt.Component.setCursor(java.awt.Cursor)
meth public void java.awt.Component.setFocusTraversalKeysEnabled(boolean)
meth public void java.awt.Component.setFocusable(boolean)
meth public void java.awt.Component.setIgnoreRepaint(boolean)
meth public void java.awt.Component.setLocale(java.util.Locale)
meth public void java.awt.Component.setLocation(int,int)
meth public void java.awt.Component.setLocation(java.awt.Point)
meth public void java.awt.Component.setName(java.lang.String)
meth public void java.awt.Component.setSize(int,int)
meth public void java.awt.Component.setSize(java.awt.Dimension)
meth public void java.awt.Component.show()
meth public void java.awt.Component.show(boolean)
meth public void java.awt.Component.transferFocus()
meth public void java.awt.Component.transferFocusUpCycle()
meth public void java.awt.Container.add(java.awt.Component,java.lang.Object)
meth public void java.awt.Container.add(java.awt.Component,java.lang.Object,int)
meth public void java.awt.Container.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void java.awt.Container.addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void java.awt.Container.applyComponentOrientation(java.awt.ComponentOrientation)
meth public void java.awt.Container.deliverEvent(java.awt.Event)
meth public void java.awt.Container.doLayout()
meth public void java.awt.Container.invalidate()
meth public void java.awt.Container.layout()
meth public void java.awt.Container.list(java.io.PrintStream,int)
meth public void java.awt.Container.list(java.io.PrintWriter,int)
meth public void java.awt.Container.paintComponents(java.awt.Graphics)
meth public void java.awt.Container.printComponents(java.awt.Graphics)
meth public void java.awt.Container.remove(int)
meth public void java.awt.Container.remove(java.awt.Component)
meth public void java.awt.Container.removeAll()
meth public void java.awt.Container.setFocusCycleRoot(boolean)
meth public void java.awt.Container.setFocusTraversalPolicy(java.awt.FocusTraversalPolicy)
meth public void java.awt.Container.setLayout(java.awt.LayoutManager)
meth public void java.awt.Container.transferFocusBackward()
meth public void java.awt.Container.transferFocusDownCycle()
meth public void java.awt.Container.validate()
meth public void javax.swing.JComponent.addAncestorListener(javax.swing.event.AncestorListener)
meth public void javax.swing.JComponent.addNotify()
meth public void javax.swing.JComponent.computeVisibleRect(java.awt.Rectangle)
meth public void javax.swing.JComponent.disable()
meth public void javax.swing.JComponent.enable()
meth public void javax.swing.JComponent.firePropertyChange(java.lang.String,boolean,boolean)
meth public void javax.swing.JComponent.firePropertyChange(java.lang.String,char,char)
meth public void javax.swing.JComponent.firePropertyChange(java.lang.String,int,int)
meth public void javax.swing.JComponent.grabFocus()
meth public void javax.swing.JComponent.paint(java.awt.Graphics)
meth public void javax.swing.JComponent.paintImmediately(int,int,int,int)
meth public void javax.swing.JComponent.paintImmediately(java.awt.Rectangle)
meth public void javax.swing.JComponent.print(java.awt.Graphics)
meth public void javax.swing.JComponent.printAll(java.awt.Graphics)
meth public void javax.swing.JComponent.registerKeyboardAction(java.awt.event.ActionListener,java.lang.String,javax.swing.KeyStroke,int)
meth public void javax.swing.JComponent.registerKeyboardAction(java.awt.event.ActionListener,javax.swing.KeyStroke,int)
meth public void javax.swing.JComponent.removeAncestorListener(javax.swing.event.AncestorListener)
meth public void javax.swing.JComponent.removeNotify()
meth public void javax.swing.JComponent.repaint(java.awt.Rectangle)
meth public void javax.swing.JComponent.repaint(long,int,int,int,int)
meth public void javax.swing.JComponent.requestFocus()
meth public void javax.swing.JComponent.resetKeyboardActions()
meth public void javax.swing.JComponent.reshape(int,int,int,int)
meth public void javax.swing.JComponent.revalidate()
meth public void javax.swing.JComponent.scrollRectToVisible(java.awt.Rectangle)
meth public void javax.swing.JComponent.setAlignmentX(float)
meth public void javax.swing.JComponent.setAlignmentY(float)
meth public void javax.swing.JComponent.setAutoscrolls(boolean)
meth public void javax.swing.JComponent.setBackground(java.awt.Color)
meth public void javax.swing.JComponent.setBorder(javax.swing.border.Border)
meth public void javax.swing.JComponent.setComponentPopupMenu(javax.swing.JPopupMenu)
meth public void javax.swing.JComponent.setDebugGraphicsOptions(int)
meth public void javax.swing.JComponent.setDoubleBuffered(boolean)
meth public void javax.swing.JComponent.setEnabled(boolean)
meth public void javax.swing.JComponent.setFocusTraversalKeys(int,java.util.Set)
meth public void javax.swing.JComponent.setFont(java.awt.Font)
meth public void javax.swing.JComponent.setForeground(java.awt.Color)
meth public void javax.swing.JComponent.setInheritsPopupMenu(boolean)
meth public void javax.swing.JComponent.setInputVerifier(javax.swing.InputVerifier)
meth public void javax.swing.JComponent.setMaximumSize(java.awt.Dimension)
meth public void javax.swing.JComponent.setMinimumSize(java.awt.Dimension)
meth public void javax.swing.JComponent.setNextFocusableComponent(java.awt.Component)
meth public void javax.swing.JComponent.setOpaque(boolean)
meth public void javax.swing.JComponent.setPreferredSize(java.awt.Dimension)
meth public void javax.swing.JComponent.setRequestFocusEnabled(boolean)
meth public void javax.swing.JComponent.setToolTipText(java.lang.String)
meth public void javax.swing.JComponent.setTransferHandler(javax.swing.TransferHandler)
meth public void javax.swing.JComponent.setVerifyInputWhenFocusTarget(boolean)
meth public void javax.swing.JComponent.setVisible(boolean)
meth public void javax.swing.JComponent.unregisterKeyboardAction(javax.swing.KeyStroke)
meth public void javax.swing.JComponent.update(java.awt.Graphics)
meth public void javax.swing.JFileChooser.addActionListener(java.awt.event.ActionListener)
meth public void javax.swing.JFileChooser.addChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public void javax.swing.JFileChooser.cancelSelection()
meth public void javax.swing.JFileChooser.changeToParentDirectory()
meth public void javax.swing.JFileChooser.ensureFileIsVisible(java.io.File)
meth public void javax.swing.JFileChooser.removeActionListener(java.awt.event.ActionListener)
meth public void javax.swing.JFileChooser.rescanCurrentDirectory()
meth public void javax.swing.JFileChooser.resetChoosableFileFilters()
meth public void javax.swing.JFileChooser.setAcceptAllFileFilterUsed(boolean)
meth public void javax.swing.JFileChooser.setAccessory(javax.swing.JComponent)
meth public void javax.swing.JFileChooser.setApproveButtonMnemonic(char)
meth public void javax.swing.JFileChooser.setApproveButtonMnemonic(int)
meth public void javax.swing.JFileChooser.setApproveButtonText(java.lang.String)
meth public void javax.swing.JFileChooser.setApproveButtonToolTipText(java.lang.String)
meth public void javax.swing.JFileChooser.setControlButtonsAreShown(boolean)
meth public void javax.swing.JFileChooser.setCurrentDirectory(java.io.File)
meth public void javax.swing.JFileChooser.setDialogTitle(java.lang.String)
meth public void javax.swing.JFileChooser.setDialogType(int)
meth public void javax.swing.JFileChooser.setDragEnabled(boolean)
meth public void javax.swing.JFileChooser.setFileFilter(javax.swing.filechooser.FileFilter)
meth public void javax.swing.JFileChooser.setFileHidingEnabled(boolean)
meth public void javax.swing.JFileChooser.setFileSelectionMode(int)
meth public void javax.swing.JFileChooser.setFileSystemView(javax.swing.filechooser.FileSystemView)
meth public void javax.swing.JFileChooser.setFileView(javax.swing.filechooser.FileView)
meth public void javax.swing.JFileChooser.setMultiSelectionEnabled(boolean)
meth public void javax.swing.JFileChooser.setSelectedFile(java.io.File)
meth public void javax.swing.JFileChooser.setSelectedFiles([Ljava.io.File;)
meth public void javax.swing.JFileChooser.updateUI()
meth public void org.netbeans.api.project.ant.FileChooser.approveSelection()
supr javax.swing.JFileChooser
CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactProvider
meth public abstract [Lorg.netbeans.api.project.ant.AntArtifact; org.netbeans.spi.project.ant.AntArtifactProvider.getBuildArtifacts()
supr null
CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactQueryImplementation
meth public abstract org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.ant.AntArtifactQueryImplementation.findArtifact(java.io.File)
supr null
CLSS public final org.netbeans.spi.project.ant.AntBuildExtenderFactory
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
meth public static org.netbeans.api.project.ant.AntBuildExtender org.netbeans.spi.project.ant.AntBuildExtenderFactory.createAntExtender(org.netbeans.spi.project.ant.AntBuildExtenderImplementation)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.ant.AntBuildExtenderImplementation
meth public abstract java.util.List org.netbeans.spi.project.ant.AntBuildExtenderImplementation.getExtensibleTargets()
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.ant.AntBuildExtenderImplementation.getOwningProject()
supr null
CLSS public abstract interface org.netbeans.spi.project.support.ant.AntBasedProjectType
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.AntBasedProjectType.getPrimaryConfigurationDataElementName(boolean)
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.AntBasedProjectType.getPrimaryConfigurationDataElementNamespace(boolean)
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.AntBasedProjectType.getType()
meth public abstract org.netbeans.api.project.Project org.netbeans.spi.project.support.ant.AntBasedProjectType.createProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException
supr null
CLSS public final org.netbeans.spi.project.support.ant.AntProjectEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.AntProjectEvent.isExpected()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectEvent.getPath()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.spi.project.support.ant.AntProjectEvent.getHelper()
supr java.util.EventObject
CLSS public final org.netbeans.spi.project.support.ant.AntProjectHelper
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_PROPERTIES_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_XML_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_PROPERTIES_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_XML_PATH
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.AntProjectHelper.isSharableProject()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.File org.netbeans.spi.project.support.ant.AntProjectHelper.resolveFile(java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.getLibrariesLocation()
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.resolvePath(java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.AntProjectHelper.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.support.ant.AntProjectHelper.createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.AuxiliaryConfiguration org.netbeans.spi.project.support.ant.AntProjectHelper.createAuxiliaryConfiguration()
meth public org.netbeans.spi.project.CacheDirectoryProvider org.netbeans.spi.project.support.ant.AntProjectHelper.createCacheDirectoryProvider()
meth public org.netbeans.spi.project.support.ant.EditableProperties org.netbeans.spi.project.support.ant.AntProjectHelper.getProperties(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator org.netbeans.spi.project.support.ant.AntProjectHelper.getStandardPropertyEvaluator()
meth public org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.AntProjectHelper.getProjectLibrariesPropertyProvider()
meth public org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.AntProjectHelper.getPropertyProvider(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.AntProjectHelper.getStockPropertyPreprovider()
meth public org.netbeans.spi.queries.FileBuiltQueryImplementation org.netbeans.spi.project.support.ant.AntProjectHelper.createGlobFileBuiltQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,[Ljava.lang.String;,[Ljava.lang.String;) throws java.lang.IllegalArgumentException
meth public org.netbeans.spi.queries.SharabilityQueryImplementation org.netbeans.spi.project.support.ant.AntProjectHelper.createSharabilityQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,[Ljava.lang.String;,[Ljava.lang.String;)
meth public org.openide.filesystems.FileObject org.netbeans.spi.project.support.ant.AntProjectHelper.getProjectDirectory()
meth public org.openide.filesystems.FileObject org.netbeans.spi.project.support.ant.AntProjectHelper.resolveFileObject(java.lang.String)
meth public org.w3c.dom.Element org.netbeans.spi.project.support.ant.AntProjectHelper.getPrimaryConfigurationData(boolean)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.addAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.notifyDeleted()
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.putPrimaryConfigurationData(org.w3c.dom.Element,boolean) throws java.lang.IllegalArgumentException
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.putProperties(java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.removeAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void org.netbeans.spi.project.support.ant.AntProjectHelper.setLibrariesLocation(java.lang.String)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.project.support.ant.AntProjectListener
intf java.util.EventListener
meth public abstract void org.netbeans.spi.project.support.ant.AntProjectListener.configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public abstract void org.netbeans.spi.project.support.ant.AntProjectListener.propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
supr null
CLSS public final org.netbeans.spi.project.support.ant.EditableProperties
cons public EditableProperties()
cons public EditableProperties(boolean)
cons public EditableProperties(java.util.Map)
intf java.lang.Cloneable
intf java.util.Map
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.netbeans.spi.project.support.ant.EditableProperties.getComment(java.lang.String)
meth public boolean java.util.AbstractMap.containsKey(java.lang.Object)
meth public boolean java.util.AbstractMap.containsValue(java.lang.Object)
meth public boolean java.util.AbstractMap.equals(java.lang.Object)
meth public boolean java.util.AbstractMap.isEmpty()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int java.util.AbstractMap.hashCode()
meth public int java.util.AbstractMap.size()
meth public java.lang.Object java.util.AbstractMap.get(java.lang.Object)
meth public java.lang.Object java.util.AbstractMap.remove(java.lang.Object)
meth public java.lang.Object org.netbeans.spi.project.support.ant.EditableProperties.clone()
meth public java.lang.String java.util.AbstractMap.toString()
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.getProperty(java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.put(java.lang.String,java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.setProperty(java.lang.String,[Ljava.lang.String;)
meth public java.lang.String org.netbeans.spi.project.support.ant.EditableProperties.setProperty(java.lang.String,java.lang.String)
meth public java.util.Collection java.util.AbstractMap.values()
meth public java.util.Set java.util.AbstractMap.keySet()
meth public java.util.Set org.netbeans.spi.project.support.ant.EditableProperties.entrySet()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.netbeans.spi.project.support.ant.EditableProperties org.netbeans.spi.project.support.ant.EditableProperties.cloneProperties()
meth public void java.util.AbstractMap.clear()
meth public void java.util.AbstractMap.putAll(java.util.Map)
meth public void org.netbeans.spi.project.support.ant.EditableProperties.load(java.io.InputStream) throws java.io.IOException
meth public void org.netbeans.spi.project.support.ant.EditableProperties.setComment(java.lang.String,[Ljava.lang.String;,boolean)
meth public void org.netbeans.spi.project.support.ant.EditableProperties.store(java.io.OutputStream) throws java.io.IOException
meth public volatile java.lang.Object org.netbeans.spi.project.support.ant.EditableProperties.put(java.lang.Object,java.lang.Object)
supr java.util.AbstractMap
CLSS public abstract org.netbeans.spi.project.support.ant.FilterPropertyProvider
cons protected FilterPropertyProvider(org.netbeans.spi.project.support.ant.PropertyProvider)
intf org.netbeans.spi.project.support.ant.PropertyProvider
meth protected final void org.netbeans.spi.project.support.ant.FilterPropertyProvider.setDelegate(org.netbeans.spi.project.support.ant.PropertyProvider)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.util.Map org.netbeans.spi.project.support.ant.FilterPropertyProvider.getProperties()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized final void org.netbeans.spi.project.support.ant.FilterPropertyProvider.addChangeListener(javax.swing.event.ChangeListener)
meth public synchronized final void org.netbeans.spi.project.support.ant.FilterPropertyProvider.removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.GeneratedFilesHelper
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper)
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.project.ant.AntBuildExtender)
cons public GeneratedFilesHelper(org.openide.filesystems.FileObject)
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_MISSING
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_MODIFIED
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_PROJECT_XML
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_STYLESHEET
fld  constant public static final int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_UNKNOWN
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.GeneratedFilesHelper.BUILD_IMPL_XML_PATH
fld  constant public static final java.lang.String org.netbeans.spi.project.support.ant.GeneratedFilesHelper.BUILD_XML_PATH
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.GeneratedFilesHelper.refreshBuildScript(java.lang.String,java.net.URL,boolean) throws java.io.IOException,java.lang.IllegalStateException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.project.support.ant.GeneratedFilesHelper.getBuildScriptState(java.lang.String,java.net.URL) throws java.lang.IllegalStateException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.project.support.ant.GeneratedFilesHelper.generateBuildScriptFromStylesheet(java.lang.String,java.net.URL) throws java.io.IOException,java.lang.IllegalStateException
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.PathMatcher
cons public PathMatcher(java.lang.String,java.lang.String,java.io.File)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.PathMatcher.matches(java.lang.String,boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.spi.project.support.ant.PathMatcher.toString()
meth public java.util.Set org.netbeans.spi.project.support.ant.PathMatcher.findIncludedRoots() throws java.lang.IllegalArgumentException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public org.netbeans.spi.project.support.ant.ProjectGenerator
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
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.spi.project.support.ant.ProjectGenerator.createProject(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException,java.lang.IllegalArgumentException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper org.netbeans.spi.project.support.ant.ProjectGenerator.createProject(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException,java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public abstract org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
cons protected ProjectXmlSavedHook()
meth protected abstract void org.netbeans.spi.project.support.ant.ProjectXmlSavedHook.projectXmlSaved() throws java.io.IOException
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
CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyEvaluator
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.PropertyEvaluator.evaluate(java.lang.String)
meth public abstract java.lang.String org.netbeans.spi.project.support.ant.PropertyEvaluator.getProperty(java.lang.String)
meth public abstract java.util.Map org.netbeans.spi.project.support.ant.PropertyEvaluator.getProperties()
meth public abstract void org.netbeans.spi.project.support.ant.PropertyEvaluator.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.project.support.ant.PropertyEvaluator.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyProvider
meth public abstract java.util.Map org.netbeans.spi.project.support.ant.PropertyProvider.getProperties()
meth public abstract void org.netbeans.spi.project.support.ant.PropertyProvider.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.netbeans.spi.project.support.ant.PropertyProvider.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public org.netbeans.spi.project.support.ant.PropertyUtils
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
meth public static [Ljava.lang.String; org.netbeans.spi.project.support.ant.PropertyUtils.tokenizePath(java.lang.String)
meth public static boolean org.netbeans.spi.project.support.ant.PropertyUtils.isUsablePropertyName(java.lang.String)
meth public static java.io.File org.netbeans.spi.project.support.ant.PropertyUtils.resolveFile(java.io.File,java.lang.String) throws java.lang.IllegalArgumentException
meth public static java.lang.String org.netbeans.spi.project.support.ant.PropertyUtils.getUsablePropertyName(java.lang.String)
meth public static java.lang.String org.netbeans.spi.project.support.ant.PropertyUtils.relativizeFile(java.io.File,java.io.File)
meth public static org.netbeans.spi.project.support.ant.EditableProperties org.netbeans.spi.project.support.ant.PropertyUtils.getGlobalProperties()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.fixedPropertyProvider(java.util.Map)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.propertiesFilePropertyProvider(java.io.File)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.userPropertiesProvider(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.io.File)
meth public static synchronized org.netbeans.spi.project.support.ant.PropertyProvider org.netbeans.spi.project.support.ant.PropertyUtils.globalPropertyProvider()
meth public static transient org.netbeans.spi.project.support.ant.PropertyEvaluator org.netbeans.spi.project.support.ant.PropertyUtils.sequentialPropertyEvaluator(org.netbeans.spi.project.support.ant.PropertyProvider,[Lorg.netbeans.spi.project.support.ant.PropertyProvider;)
meth public static void org.netbeans.spi.project.support.ant.PropertyUtils.putGlobalProperties(org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.ReferenceHelper
cons public ReferenceHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.AuxiliaryConfiguration,org.netbeans.spi.project.support.ant.PropertyEvaluator)
innr public static final org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.Object; org.netbeans.spi.project.support.ant.ReferenceHelper.findArtifactAndLocation(java.lang.String)
meth public [Lorg.netbeans.spi.project.support.ant.ReferenceHelper$RawReference; org.netbeans.spi.project.support.ant.ReferenceHelper.getRawReferences()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.addRawReference(org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.addReference(org.netbeans.api.project.ant.AntArtifact) throws java.lang.IllegalArgumentException
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.destroyReference(java.lang.String)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.isReferenced(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.lang.IllegalArgumentException
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.removeRawReference(java.lang.String,java.lang.String)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.removeReference(java.lang.String)
meth public boolean org.netbeans.spi.project.support.ant.ReferenceHelper.removeReference(java.lang.String,java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.addReference(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.lang.IllegalArgumentException
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createExtraForeignFileReferenceAsIs(java.lang.String,java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createForeignFileReference(java.io.File,java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createForeignFileReference(org.netbeans.api.project.ant.AntArtifact) throws java.lang.IllegalArgumentException
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createForeignFileReferenceAsIs(java.lang.String,java.lang.String)
meth public java.lang.String org.netbeans.spi.project.support.ant.ReferenceHelper.createLibraryReference(org.netbeans.api.project.libraries.Library,java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.project.ant.AntArtifact org.netbeans.spi.project.support.ant.ReferenceHelper.getForeignFileReferenceAsArtifact(java.lang.String)
meth public org.netbeans.api.project.libraries.Library org.netbeans.spi.project.support.ant.ReferenceHelper.copyLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
meth public org.netbeans.api.project.libraries.Library org.netbeans.spi.project.support.ant.ReferenceHelper.findLibrary(java.lang.String)
meth public org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler org.netbeans.spi.project.support.ant.ReferenceHelper.getLibraryChooserImportHandler()
meth public org.netbeans.api.project.libraries.LibraryManager org.netbeans.spi.project.support.ant.ReferenceHelper.getProjectLibraryManager()
meth public org.netbeans.spi.project.SubprojectProvider org.netbeans.spi.project.support.ant.ReferenceHelper.createSubprojectProvider()
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference org.netbeans.spi.project.support.ant.ReferenceHelper.getRawReference(java.lang.String,java.lang.String)
meth public static org.netbeans.api.project.libraries.LibraryManager org.netbeans.spi.project.support.ant.ReferenceHelper.getProjectLibraryManager(org.netbeans.api.project.Project)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.addExtraBaseDirectory(java.lang.String)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.destroyForeignFileReference(java.lang.String)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.fixReferences(java.io.File)
meth public void org.netbeans.spi.project.support.ant.ReferenceHelper.removeExtraBaseDirectory(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.spi.project.support.ant.SourcesHelper
cons public SourcesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
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
meth public org.netbeans.api.project.Sources org.netbeans.spi.project.support.ant.SourcesHelper.createSources()
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addNonSourceRoot(java.lang.String) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addOwnedFile(java.lang.String) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addPrincipalSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addPrincipalSourceRoot(java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon) throws java.lang.IllegalStateException
meth public void org.netbeans.spi.project.support.ant.SourcesHelper.registerExternalRoots(int) throws java.lang.IllegalArgumentException,java.lang.IllegalStateException
supr java.lang.Object
CLSS public org.netbeans.spi.project.support.ant.ui.StoreGroup
cons public StoreGroup()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final javax.swing.JToggleButton$ToggleButtonModel org.netbeans.spi.project.support.ant.ui.StoreGroup.createInverseToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.JToggleButton$ToggleButtonModel org.netbeans.spi.project.support.ant.ui.StoreGroup.createToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.text.Document org.netbeans.spi.project.support.ant.ui.StoreGroup.createStringDocument(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.project.support.ant.ui.StoreGroup.store(org.netbeans.spi.project.support.ant.EditableProperties)
supr java.lang.Object
