#Signature file v4.0
#Version 1.31.1

CLSS public abstract java.awt.Component
cons protected Component()
fld public final static float BOTTOM_ALIGNMENT = 1.0
fld public final static float CENTER_ALIGNMENT = 0.5
fld public final static float LEFT_ALIGNMENT = 0.0
fld public final static float RIGHT_ALIGNMENT = 1.0
fld public final static float TOP_ALIGNMENT = 0.0
innr protected BltBufferStrategy
innr protected FlipBufferStrategy
innr protected abstract AccessibleAWTComponent
innr public final static !enum BaselineResizeBehavior
intf java.awt.MenuContainer
intf java.awt.image.ImageObserver
intf java.io.Serializable
meth protected boolean requestFocus(boolean)
meth protected boolean requestFocusInWindow(boolean)
meth protected final void disableEvents(long)
meth protected final void enableEvents(long)
meth protected java.awt.AWTEvent coalesceEvents(java.awt.AWTEvent,java.awt.AWTEvent)
meth protected java.lang.String paramString()
meth protected void firePropertyChange(java.lang.String,boolean,boolean)
meth protected void firePropertyChange(java.lang.String,int,int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void processComponentEvent(java.awt.event.ComponentEvent)
meth protected void processEvent(java.awt.AWTEvent)
meth protected void processFocusEvent(java.awt.event.FocusEvent)
meth protected void processHierarchyBoundsEvent(java.awt.event.HierarchyEvent)
meth protected void processHierarchyEvent(java.awt.event.HierarchyEvent)
meth protected void processInputMethodEvent(java.awt.event.InputMethodEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean action(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean areFocusTraversalKeysSet(int)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean getFocusTraversalKeysEnabled()
meth public boolean getIgnoreRepaint()
meth public boolean gotFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean handleEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public boolean hasFocus()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean inside(int,int)
 anno 0 java.lang.Deprecated()
meth public boolean isBackgroundSet()
meth public boolean isCursorSet()
meth public boolean isDisplayable()
meth public boolean isDoubleBuffered()
meth public boolean isEnabled()
meth public boolean isFocusCycleRoot(java.awt.Container)
meth public boolean isFocusOwner()
meth public boolean isFocusTraversable()
 anno 0 java.lang.Deprecated()
meth public boolean isFocusable()
meth public boolean isFontSet()
meth public boolean isForegroundSet()
meth public boolean isLightweight()
meth public boolean isMaximumSizeSet()
meth public boolean isMinimumSizeSet()
meth public boolean isOpaque()
meth public boolean isPreferredSizeSet()
meth public boolean isShowing()
meth public boolean isValid()
meth public boolean isVisible()
meth public boolean keyDown(java.awt.Event,int)
 anno 0 java.lang.Deprecated()
meth public boolean keyUp(java.awt.Event,int)
 anno 0 java.lang.Deprecated()
meth public boolean lostFocus(java.awt.Event,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean mouseDown(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseDrag(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseEnter(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseExit(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseMove(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean mouseUp(java.awt.Event,int,int)
 anno 0 java.lang.Deprecated()
meth public boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public boolean prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public boolean requestFocusInWindow()
meth public final java.lang.Object getTreeLock()
meth public final void dispatchEvent(java.awt.AWTEvent)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int getBaseline(int,int)
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component locate(int,int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior()
meth public java.awt.ComponentOrientation getComponentOrientation()
meth public java.awt.Container getFocusCycleRootAncestor()
meth public java.awt.Container getParent()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.Dimension minimumSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension size()
 anno 0 java.lang.Deprecated()
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.GraphicsConfiguration getGraphicsConfiguration()
meth public java.awt.Image createImage(int,int)
meth public java.awt.Image createImage(java.awt.image.ImageProducer)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Point getMousePosition()
meth public java.awt.Point location()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle bounds()
 anno 0 java.lang.Deprecated()
meth public java.awt.Rectangle getBounds()
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Toolkit getToolkit()
meth public java.awt.dnd.DropTarget getDropTarget()
meth public java.awt.event.ComponentListener[] getComponentListeners()
meth public java.awt.event.FocusListener[] getFocusListeners()
meth public java.awt.event.HierarchyBoundsListener[] getHierarchyBoundsListeners()
meth public java.awt.event.HierarchyListener[] getHierarchyListeners()
meth public java.awt.event.InputMethodListener[] getInputMethodListeners()
meth public java.awt.event.KeyListener[] getKeyListeners()
meth public java.awt.event.MouseListener[] getMouseListeners()
meth public java.awt.event.MouseMotionListener[] getMouseMotionListeners()
meth public java.awt.event.MouseWheelListener[] getMouseWheelListeners()
meth public java.awt.im.InputContext getInputContext()
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.image.ColorModel getColorModel()
meth public java.awt.image.VolatileImage createVolatileImage(int,int)
meth public java.awt.image.VolatileImage createVolatileImage(int,int,java.awt.ImageCapabilities) throws java.awt.AWTException
meth public java.awt.peer.ComponentPeer getPeer()
 anno 0 java.lang.Deprecated()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Locale getLocale()
meth public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int)
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public void add(java.awt.PopupMenu)
meth public void addComponentListener(java.awt.event.ComponentListener)
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void addHierarchyListener(java.awt.event.HierarchyListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void addKeyListener(java.awt.event.KeyListener)
meth public void addMouseListener(java.awt.event.MouseListener)
meth public void addMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void addMouseWheelListener(java.awt.event.MouseWheelListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void deliverEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public void disable()
 anno 0 java.lang.Deprecated()
meth public void doLayout()
meth public void enable()
 anno 0 java.lang.Deprecated()
meth public void enable(boolean)
 anno 0 java.lang.Deprecated()
meth public void enableInputMethods(boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void hide()
 anno 0 java.lang.Deprecated()
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated()
meth public void list()
meth public void list(java.io.PrintStream)
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter)
meth public void list(java.io.PrintWriter,int)
meth public void move(int,int)
 anno 0 java.lang.Deprecated()
meth public void nextFocus()
 anno 0 java.lang.Deprecated()
meth public void paint(java.awt.Graphics)
meth public void paintAll(java.awt.Graphics)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void remove(java.awt.MenuComponent)
meth public void removeComponentListener(java.awt.event.ComponentListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener)
meth public void removeHierarchyListener(java.awt.event.HierarchyListener)
meth public void removeInputMethodListener(java.awt.event.InputMethodListener)
meth public void removeKeyListener(java.awt.event.KeyListener)
meth public void removeMouseListener(java.awt.event.MouseListener)
meth public void removeMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void removeMouseWheelListener(java.awt.event.MouseWheelListener)
meth public void removeNotify()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint()
meth public void repaint(int,int,int,int)
meth public void repaint(long)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated()
meth public void resize(int,int)
 anno 0 java.lang.Deprecated()
meth public void resize(java.awt.Dimension)
 anno 0 java.lang.Deprecated()
meth public void setBackground(java.awt.Color)
meth public void setBounds(int,int,int,int)
meth public void setBounds(java.awt.Rectangle)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setCursor(java.awt.Cursor)
meth public void setDropTarget(java.awt.dnd.DropTarget)
meth public void setEnabled(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFocusTraversalKeysEnabled(boolean)
meth public void setFocusable(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setIgnoreRepaint(boolean)
meth public void setLocale(java.util.Locale)
meth public void setLocation(int,int)
meth public void setLocation(java.awt.Point)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setName(java.lang.String)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setVisible(boolean)
meth public void show()
 anno 0 java.lang.Deprecated()
meth public void show(boolean)
 anno 0 java.lang.Deprecated()
meth public void transferFocus()
meth public void transferFocusBackward()
meth public void transferFocusUpCycle()
meth public void update(java.awt.Graphics)
meth public void validate()
supr java.lang.Object
hfds FOCUS_TRAVERSABLE_DEFAULT,FOCUS_TRAVERSABLE_SET,FOCUS_TRAVERSABLE_UNKNOWN,LOCK,accessibleContext,actionListenerK,adjustmentListenerK,appContext,background,boundsOp,bufferStrategy,changeSupport,coalesceEventsParams,coalesceMap,coalescingEnabled,componentListener,componentListenerK,componentOrientation,componentSerializedDataVersion,containerListenerK,cursor,dbg,dropTarget,enabled,eventCache,eventMask,focusListener,focusListenerK,focusLog,focusTraversalKeyPropertyNames,focusTraversalKeys,focusTraversalKeysEnabled,focusable,font,foreground,graphicsConfig,height,hierarchyBoundsListener,hierarchyBoundsListenerK,hierarchyListener,hierarchyListenerK,ignoreRepaint,incRate,inputMethodListener,inputMethodListenerK,isFocusTraversableOverridden,isInc,isPacked,itemListenerK,keyListener,keyListenerK,locale,log,maxSize,maxSizeSet,minSize,minSizeSet,mouseListener,mouseListenerK,mouseMotionListener,mouseMotionListenerK,mouseWheelListener,mouseWheelListenerK,name,nameExplicitlySet,nativeInLightFixer,newEventsOnly,ownedWindowK,parent,peer,peerFont,popups,prefSize,prefSizeSet,privateKey,requestFocusController,serialVersionUID,textListenerK,valid,visible,width,windowClosingException,windowFocusListenerK,windowListenerK,windowStateListenerK,x,y
hcls AWTTreeLock,BltSubRegionBufferStrategy,DummyRequestFocusController,FlipSubRegionBufferStrategy,NativeInLightFixer,SingleBufferStrategy

CLSS public java.awt.Container
cons public Container()
innr protected AccessibleAWTContainer
meth protected java.lang.String paramString()
meth protected void addImpl(java.awt.Component,java.lang.Object,int)
meth protected void processContainerEvent(java.awt.event.ContainerEvent)
meth protected void processEvent(java.awt.AWTEvent)
meth protected void validateTree()
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean areFocusTraversalKeysSet(int)
meth public boolean isAncestorOf(java.awt.Component)
meth public boolean isFocusCycleRoot()
meth public boolean isFocusCycleRoot(java.awt.Container)
meth public boolean isFocusTraversalPolicySet()
meth public final boolean isFocusTraversalPolicyProvider()
meth public final void setFocusTraversalPolicyProvider(boolean)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int countComponents()
 anno 0 java.lang.Deprecated()
meth public int getComponentCount()
meth public int getComponentZOrder(java.awt.Component)
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Component findComponentAt(int,int)
meth public java.awt.Component findComponentAt(java.awt.Point)
meth public java.awt.Component getComponent(int)
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component locate(int,int)
 anno 0 java.lang.Deprecated()
meth public java.awt.Component[] getComponents()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension minimumSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.Dimension preferredSize()
 anno 0 java.lang.Deprecated()
meth public java.awt.FocusTraversalPolicy getFocusTraversalPolicy()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets insets()
 anno 0 java.lang.Deprecated()
meth public java.awt.LayoutManager getLayout()
meth public java.awt.Point getMousePosition(boolean)
meth public java.awt.event.ContainerListener[] getContainerListeners()
meth public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int)
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void addContainerListener(java.awt.event.ContainerListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void applyComponentOrientation(java.awt.ComponentOrientation)
meth public void deliverEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public void doLayout()
meth public void invalidate()
meth public void layout()
 anno 0 java.lang.Deprecated()
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter,int)
meth public void paint(java.awt.Graphics)
meth public void paintComponents(java.awt.Graphics)
meth public void print(java.awt.Graphics)
meth public void printComponents(java.awt.Graphics)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void removeContainerListener(java.awt.event.ContainerListener)
meth public void removeNotify()
meth public void setComponentZOrder(java.awt.Component,int)
meth public void setFocusCycleRoot(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFocusTraversalPolicy(java.awt.FocusTraversalPolicy)
meth public void setFont(java.awt.Font)
meth public void setLayout(java.awt.LayoutManager)
meth public void transferFocusBackward()
meth public void transferFocusDownCycle()
meth public void update(java.awt.Graphics)
meth public void validate()
supr java.awt.Component
hfds INCLUDE_SELF,SEARCH_HEAVYWEIGHTS,component,containerListener,containerSerializedDataVersion,dbg,descendantsCount,dispatcher,focusCycleRoot,focusTraversalPolicy,focusTraversalPolicyProvider,layoutMgr,listeningBoundsChildren,listeningChildren,modalAppContext,modalComp,ncomponents,printing,printingThreads,serialPersistentFields,serialVersionUID
hcls DropTargetEventTargetFilter,EventTargetFilter,MouseEventTargetFilter,WakingRunnable

CLSS public abstract interface java.awt.MenuContainer
meth public abstract boolean postEvent(java.awt.Event)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Font getFont()
meth public abstract void remove(java.awt.MenuComponent)

CLSS public abstract interface java.awt.image.ImageObserver
fld public final static int ABORT = 128
fld public final static int ALLBITS = 32
fld public final static int ERROR = 64
fld public final static int FRAMEBITS = 16
fld public final static int HEIGHT = 2
fld public final static int PROPERTIES = 4
fld public final static int SOMEBITS = 8
fld public final static int WIDTH = 1
meth public abstract boolean imageUpdate(java.awt.Image,int,int,int,int,int)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected AbstractMap()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds keySet,values

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract javax.swing.JComponent
cons public JComponent()
fld protected javax.accessibility.AccessibleContext accessibleContext
fld protected javax.swing.event.EventListenerList listenerList
fld protected javax.swing.plaf.ComponentUI ui
fld public final static int UNDEFINED_CONDITION = -1
fld public final static int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 1
fld public final static int WHEN_FOCUSED = 0
fld public final static int WHEN_IN_FOCUSED_WINDOW = 2
fld public final static java.lang.String TOOL_TIP_TEXT_KEY = "ToolTipText"
innr public abstract AccessibleJComponent
intf java.io.Serializable
meth protected boolean processKeyBinding(javax.swing.KeyStroke,java.awt.event.KeyEvent,int,boolean)
meth protected boolean requestFocusInWindow(boolean)
meth protected java.awt.Graphics getComponentGraphics(java.awt.Graphics)
meth protected java.lang.String paramString()
meth protected void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected void paintBorder(java.awt.Graphics)
meth protected void paintChildren(java.awt.Graphics)
meth protected void paintComponent(java.awt.Graphics)
meth protected void printBorder(java.awt.Graphics)
meth protected void printChildren(java.awt.Graphics)
meth protected void printComponent(java.awt.Graphics)
meth protected void processComponentKeyEvent(java.awt.event.KeyEvent)
meth protected void processKeyEvent(java.awt.event.KeyEvent)
meth protected void processMouseEvent(java.awt.event.MouseEvent)
meth protected void processMouseMotionEvent(java.awt.event.MouseEvent)
meth protected void setUI(javax.swing.plaf.ComponentUI)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean contains(int,int)
meth public boolean getAutoscrolls()
meth public boolean getInheritsPopupMenu()
meth public boolean getVerifyInputWhenFocusTarget()
meth public boolean isDoubleBuffered()
meth public boolean isManagingFocus()
 anno 0 java.lang.Deprecated()
meth public boolean isOpaque()
meth public boolean isOptimizedDrawingEnabled()
meth public boolean isPaintingTile()
meth public boolean isRequestFocusEnabled()
meth public boolean isValidateRoot()
meth public boolean requestDefaultFocus()
 anno 0 java.lang.Deprecated()
meth public boolean requestFocus(boolean)
meth public boolean requestFocusInWindow()
meth public final boolean isPaintingForPrint()
meth public final java.lang.Object getClientProperty(java.lang.Object)
meth public final javax.swing.ActionMap getActionMap()
meth public final javax.swing.InputMap getInputMap()
meth public final javax.swing.InputMap getInputMap(int)
meth public final void putClientProperty(java.lang.Object,java.lang.Object)
meth public final void setActionMap(javax.swing.ActionMap)
meth public final void setInputMap(int,javax.swing.InputMap)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int getBaseline(int,int)
meth public int getConditionForKeyStroke(javax.swing.KeyStroke)
meth public int getDebugGraphicsOptions()
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Component getNextFocusableComponent()
 anno 0 java.lang.Deprecated()
meth public java.awt.Component$BaselineResizeBehavior getBaselineResizeBehavior()
meth public java.awt.Container getTopLevelAncestor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.Insets getInsets()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getPopupLocation(java.awt.event.MouseEvent)
meth public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent)
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Rectangle getVisibleRect()
meth public java.awt.event.ActionListener getActionForKeyStroke(javax.swing.KeyStroke)
meth public java.beans.VetoableChangeListener[] getVetoableChangeListeners()
meth public java.lang.String getToolTipText()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.InputVerifier getInputVerifier()
meth public javax.swing.JPopupMenu getComponentPopupMenu()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.JToolTip createToolTip()
meth public javax.swing.KeyStroke[] getRegisteredKeyStrokes()
meth public javax.swing.TransferHandler getTransferHandler()
meth public javax.swing.border.Border getBorder()
meth public javax.swing.event.AncestorListener[] getAncestorListeners()
meth public static boolean isLightweightComponent(java.awt.Component)
meth public static java.util.Locale getDefaultLocale()
meth public static void setDefaultLocale(java.util.Locale)
meth public void addAncestorListener(javax.swing.event.AncestorListener)
meth public void addNotify()
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void computeVisibleRect(java.awt.Rectangle)
meth public void disable()
 anno 0 java.lang.Deprecated()
meth public void enable()
 anno 0 java.lang.Deprecated()
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void grabFocus()
meth public void paint(java.awt.Graphics)
meth public void paintImmediately(int,int,int,int)
meth public void paintImmediately(java.awt.Rectangle)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void registerKeyboardAction(java.awt.event.ActionListener,java.lang.String,javax.swing.KeyStroke,int)
meth public void registerKeyboardAction(java.awt.event.ActionListener,javax.swing.KeyStroke,int)
meth public void removeAncestorListener(javax.swing.event.AncestorListener)
meth public void removeNotify()
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void repaint(java.awt.Rectangle)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void resetKeyboardActions()
meth public void reshape(int,int,int,int)
 anno 0 java.lang.Deprecated()
meth public void revalidate()
meth public void scrollRectToVisible(java.awt.Rectangle)
meth public void setAlignmentX(float)
meth public void setAlignmentY(float)
meth public void setAutoscrolls(boolean)
meth public void setBackground(java.awt.Color)
meth public void setBorder(javax.swing.border.Border)
meth public void setComponentPopupMenu(javax.swing.JPopupMenu)
meth public void setDebugGraphicsOptions(int)
meth public void setDoubleBuffered(boolean)
meth public void setEnabled(boolean)
meth public void setFocusTraversalKeys(int,java.util.Set<? extends java.awt.AWTKeyStroke>)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setInheritsPopupMenu(boolean)
meth public void setInputVerifier(javax.swing.InputVerifier)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setNextFocusableComponent(java.awt.Component)
 anno 0 java.lang.Deprecated()
meth public void setOpaque(boolean)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setRequestFocusEnabled(boolean)
meth public void setToolTipText(java.lang.String)
meth public void setTransferHandler(javax.swing.TransferHandler)
meth public void setVerifyInputWhenFocusTarget(boolean)
meth public void setVisible(boolean)
meth public void unregisterKeyboardAction(javax.swing.KeyStroke)
meth public void update(java.awt.Graphics)
meth public void updateUI()
supr java.awt.Container
hfds ACTIONMAP_CREATED,ANCESTOR_INPUTMAP_CREATED,ANCESTOR_NOTIFIER_KEY,ANCESTOR_USING_BUFFER,AUTOSCROLLS_SET,COMPLETELY_OBSCURED,CREATED_DOUBLE_BUFFER,DEBUG_GRAPHICS_LOADED,FOCUS_INPUTMAP_CREATED,FOCUS_TRAVERSAL_KEYS_BACKWARD_SET,FOCUS_TRAVERSAL_KEYS_FORWARD_SET,INHERITS_POPUP_MENU,INPUT_VERIFIER_KEY,INPUT_VERIFIER_SOURCE_KEY,IS_DOUBLE_BUFFERED,IS_OPAQUE,IS_PAINTING_TILE,IS_PRINTING,IS_PRINTING_ALL,IS_REPAINTING,KEYBOARD_BINDINGS_KEY,KEY_EVENTS_ENABLED,NEXT_FOCUS,NOT_OBSCURED,OPAQUE_SET,PARTIALLY_OBSCURED,REQUEST_FOCUS_DISABLED,RESERVED_1,RESERVED_2,RESERVED_3,RESERVED_4,RESERVED_5,RESERVED_6,TRANSFER_HANDLER_KEY,WHEN_IN_FOCUSED_WINDOW_BINDINGS,WIF_INPUTMAP_CREATED,WRITE_OBJ_COUNTER_FIRST,WRITE_OBJ_COUNTER_LAST,aaTextInfo,actionMap,alignmentX,alignmentY,ancestorInputMap,autoscrolls,border,clientProperties,componentObtainingGraphicsFrom,componentObtainingGraphicsFromLock,defaultLocale,flags,focusController,focusInputMap,inputVerifier,isAlignmentXSet,isAlignmentYSet,managingFocusBackwardTraversalKeys,managingFocusForwardTraversalKeys,paintingChild,popupMenu,readObjectCallbacks,tempRectangles,uiClassID,verifyInputWhenFocusTarget,vetoableChangeSupport,windowInputMap
hcls ActionStandin,IntVector,KeyboardState,ReadObjectCallback

CLSS public javax.swing.JFileChooser
cons public JFileChooser()
cons public JFileChooser(java.io.File)
cons public JFileChooser(java.io.File,javax.swing.filechooser.FileSystemView)
cons public JFileChooser(java.lang.String)
cons public JFileChooser(java.lang.String,javax.swing.filechooser.FileSystemView)
cons public JFileChooser(javax.swing.filechooser.FileSystemView)
fld protected javax.accessibility.AccessibleContext accessibleContext
fld public final static int APPROVE_OPTION = 0
fld public final static int CANCEL_OPTION = 1
fld public final static int CUSTOM_DIALOG = 2
fld public final static int DIRECTORIES_ONLY = 1
fld public final static int ERROR_OPTION = -1
fld public final static int FILES_AND_DIRECTORIES = 2
fld public final static int FILES_ONLY = 0
fld public final static int OPEN_DIALOG = 0
fld public final static int SAVE_DIALOG = 1
fld public final static java.lang.String ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY = "acceptAllFileFilterUsedChanged"
fld public final static java.lang.String ACCESSORY_CHANGED_PROPERTY = "AccessoryChangedProperty"
fld public final static java.lang.String APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY = "ApproveButtonMnemonicChangedProperty"
fld public final static java.lang.String APPROVE_BUTTON_TEXT_CHANGED_PROPERTY = "ApproveButtonTextChangedProperty"
fld public final static java.lang.String APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY = "ApproveButtonToolTipTextChangedProperty"
fld public final static java.lang.String APPROVE_SELECTION = "ApproveSelection"
fld public final static java.lang.String CANCEL_SELECTION = "CancelSelection"
fld public final static java.lang.String CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY = "ChoosableFileFilterChangedProperty"
fld public final static java.lang.String CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY = "ControlButtonsAreShownChangedProperty"
fld public final static java.lang.String DIALOG_TITLE_CHANGED_PROPERTY = "DialogTitleChangedProperty"
fld public final static java.lang.String DIALOG_TYPE_CHANGED_PROPERTY = "DialogTypeChangedProperty"
fld public final static java.lang.String DIRECTORY_CHANGED_PROPERTY = "directoryChanged"
fld public final static java.lang.String FILE_FILTER_CHANGED_PROPERTY = "fileFilterChanged"
fld public final static java.lang.String FILE_HIDING_CHANGED_PROPERTY = "FileHidingChanged"
fld public final static java.lang.String FILE_SELECTION_MODE_CHANGED_PROPERTY = "fileSelectionChanged"
fld public final static java.lang.String FILE_SYSTEM_VIEW_CHANGED_PROPERTY = "FileSystemViewChanged"
fld public final static java.lang.String FILE_VIEW_CHANGED_PROPERTY = "fileViewChanged"
fld public final static java.lang.String MULTI_SELECTION_ENABLED_CHANGED_PROPERTY = "MultiSelectionEnabledChangedProperty"
fld public final static java.lang.String SELECTED_FILES_CHANGED_PROPERTY = "SelectedFilesChangedProperty"
fld public final static java.lang.String SELECTED_FILE_CHANGED_PROPERTY = "SelectedFileChangedProperty"
innr protected AccessibleJFileChooser
intf javax.accessibility.Accessible
meth protected java.lang.String paramString()
meth protected javax.swing.JDialog createDialog(java.awt.Component)
meth protected void fireActionPerformed(java.lang.String)
meth protected void setup(javax.swing.filechooser.FileSystemView)
meth public boolean accept(java.io.File)
meth public boolean getControlButtonsAreShown()
meth public boolean getDragEnabled()
meth public boolean isAcceptAllFileFilterUsed()
meth public boolean isDirectorySelectionEnabled()
meth public boolean isFileHidingEnabled()
meth public boolean isFileSelectionEnabled()
meth public boolean isMultiSelectionEnabled()
meth public boolean isTraversable(java.io.File)
meth public boolean removeChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public int getApproveButtonMnemonic()
meth public int getDialogType()
meth public int getFileSelectionMode()
meth public int showDialog(java.awt.Component,java.lang.String)
meth public int showOpenDialog(java.awt.Component)
meth public int showSaveDialog(java.awt.Component)
meth public java.awt.event.ActionListener[] getActionListeners()
meth public java.io.File getCurrentDirectory()
meth public java.io.File getSelectedFile()
meth public java.io.File[] getSelectedFiles()
meth public java.lang.String getApproveButtonText()
meth public java.lang.String getApproveButtonToolTipText()
meth public java.lang.String getDescription(java.io.File)
meth public java.lang.String getDialogTitle()
meth public java.lang.String getName(java.io.File)
meth public java.lang.String getTypeDescription(java.io.File)
meth public java.lang.String getUIClassID()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.Icon getIcon(java.io.File)
meth public javax.swing.JComponent getAccessory()
meth public javax.swing.filechooser.FileFilter getAcceptAllFileFilter()
meth public javax.swing.filechooser.FileFilter getFileFilter()
meth public javax.swing.filechooser.FileFilter[] getChoosableFileFilters()
meth public javax.swing.filechooser.FileSystemView getFileSystemView()
meth public javax.swing.filechooser.FileView getFileView()
meth public javax.swing.plaf.FileChooserUI getUI()
meth public void addActionListener(java.awt.event.ActionListener)
meth public void addChoosableFileFilter(javax.swing.filechooser.FileFilter)
meth public void approveSelection()
meth public void cancelSelection()
meth public void changeToParentDirectory()
meth public void ensureFileIsVisible(java.io.File)
meth public void removeActionListener(java.awt.event.ActionListener)
meth public void rescanCurrentDirectory()
meth public void resetChoosableFileFilters()
meth public void setAcceptAllFileFilterUsed(boolean)
meth public void setAccessory(javax.swing.JComponent)
meth public void setApproveButtonMnemonic(char)
meth public void setApproveButtonMnemonic(int)
meth public void setApproveButtonText(java.lang.String)
meth public void setApproveButtonToolTipText(java.lang.String)
meth public void setControlButtonsAreShown(boolean)
meth public void setCurrentDirectory(java.io.File)
meth public void setDialogTitle(java.lang.String)
meth public void setDialogType(int)
meth public void setDragEnabled(boolean)
meth public void setFileFilter(javax.swing.filechooser.FileFilter)
meth public void setFileHidingEnabled(boolean)
meth public void setFileSelectionMode(int)
meth public void setFileSystemView(javax.swing.filechooser.FileSystemView)
meth public void setFileView(javax.swing.filechooser.FileView)
meth public void setMultiSelectionEnabled(boolean)
meth public void setSelectedFile(java.io.File)
meth public void setSelectedFiles(java.io.File[])
meth public void updateUI()
supr javax.swing.JComponent
hfds SHOW_HIDDEN_PROP,accessory,actionListener,approveButtonMnemonic,approveButtonText,approveButtonToolTipText,controlsShown,currentDirectory,dialog,dialogTitle,dialogType,dragEnabled,fileFilter,fileSelectionMode,fileSystemView,fileView,filters,multiSelectionEnabled,returnValue,selectedFile,selectedFiles,showFilesListener,uiClassID,uiFileView,useAcceptAllFileFilter,useFileHiding
hcls WeakPCL

CLSS public abstract org.netbeans.api.project.ant.AntArtifact
cons protected AntArtifact()
meth public abstract java.io.File getScriptLocation()
meth public abstract java.lang.String getCleanTargetName()
meth public abstract java.lang.String getTargetName()
meth public abstract java.lang.String getType()
meth public final org.openide.filesystems.FileObject getArtifactFile()
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileObject getScriptFile()
meth public final org.openide.filesystems.FileObject[] getArtifactFiles()
meth public java.lang.String getID()
meth public java.net.URI getArtifactLocation()
 anno 0 java.lang.Deprecated()
meth public java.net.URI[] getArtifactLocations()
meth public java.util.Properties getProperties()
meth public org.netbeans.api.project.Project getProject()
supr java.lang.Object
hfds PROPS,warnedClasses

CLSS public org.netbeans.api.project.ant.AntArtifactQuery
meth public static org.netbeans.api.project.ant.AntArtifact findArtifactByID(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.ant.AntArtifact findArtifactFromFile(java.io.File)
meth public static org.netbeans.api.project.ant.AntArtifact[] findArtifactsByType(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.project.ant.AntBuildExtender
fld public final static java.lang.String ANT_CUSTOMTASKS_LIBS_PROPNAME = "ant.customtasks.libs"
innr public final Extension
meth public java.util.List<java.lang.String> getExtensibleTargets()
meth public org.netbeans.api.project.ant.AntBuildExtender$Extension addExtension(java.lang.String,org.openide.filesystems.FileObject)
meth public org.netbeans.api.project.ant.AntBuildExtender$Extension getExtension(java.lang.String)
meth public void addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
meth public void removeExtension(java.lang.String)
meth public void removeLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
supr java.lang.Object
hfds db,extensions,implementation,refHelper

CLSS public final org.netbeans.api.project.ant.AntBuildExtender$Extension
meth public void addDependency(java.lang.String,java.lang.String)
meth public void removeDependency(java.lang.String,java.lang.String)
supr java.lang.Object
hfds dependencies,file,id,path

CLSS public final org.netbeans.api.project.ant.FileChooser
cons public FileChooser(java.io.File,java.io.File)
cons public FileChooser(org.netbeans.spi.project.support.ant.AntProjectHelper,boolean)
meth public java.lang.String[] getSelectedPathVariables()
meth public java.lang.String[] getSelectedPaths() throws java.io.IOException
meth public void approveSelection()
meth public void enableVariableBasedSelection(boolean)
supr javax.swing.JFileChooser
hfds accessory

CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactProvider
meth public abstract org.netbeans.api.project.ant.AntArtifact[] getBuildArtifacts()

CLSS public abstract interface org.netbeans.spi.project.ant.AntArtifactQueryImplementation
meth public abstract org.netbeans.api.project.ant.AntArtifact findArtifact(java.io.File)

CLSS public final org.netbeans.spi.project.ant.AntBuildExtenderFactory
meth public static org.netbeans.api.project.ant.AntBuildExtender createAntExtender(org.netbeans.spi.project.ant.AntBuildExtenderImplementation)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.project.ant.AntBuildExtender createAntExtender(org.netbeans.spi.project.ant.AntBuildExtenderImplementation,org.netbeans.spi.project.support.ant.ReferenceHelper)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ant.AntBuildExtenderImplementation
meth public abstract java.util.List<java.lang.String> getExtensibleTargets()
meth public abstract org.netbeans.api.project.Project getOwningProject()

CLSS public abstract interface !annotation org.netbeans.spi.project.support.ant.AntBasedProjectRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String privateName()
meth public abstract !hasdefault java.lang.String sharedName()
meth public abstract java.lang.String iconResource()
meth public abstract java.lang.String privateNamespace()
meth public abstract java.lang.String sharedNamespace()
meth public abstract java.lang.String type()

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntBasedProjectType
meth public abstract java.lang.String getPrimaryConfigurationDataElementName(boolean)
meth public abstract java.lang.String getPrimaryConfigurationDataElementNamespace(boolean)
meth public abstract java.lang.String getType()
meth public abstract org.netbeans.api.project.Project createProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException

CLSS public final org.netbeans.spi.project.support.ant.AntProjectEvent
meth public boolean isExpected()
meth public java.lang.String getPath()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getHelper()
supr java.util.EventObject
hfds expected,path

CLSS public final org.netbeans.spi.project.support.ant.AntProjectHelper
fld public final static java.lang.String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"
fld public final static java.lang.String PRIVATE_XML_PATH = "nbproject/private/private.xml"
fld public final static java.lang.String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"
fld public final static java.lang.String PROJECT_XML_PATH = "nbproject/project.xml"
meth public boolean isSharableProject()
meth public java.io.File resolveFile(java.lang.String)
meth public java.lang.String getLibrariesLocation()
meth public java.lang.String resolvePath(java.lang.String)
meth public java.lang.String toString()
meth public org.netbeans.api.project.ant.AntArtifact createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String)
meth public org.netbeans.api.project.ant.AntArtifact createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.AuxiliaryConfiguration createAuxiliaryConfiguration()
meth public org.netbeans.spi.project.AuxiliaryProperties createAuxiliaryProperties()
meth public org.netbeans.spi.project.CacheDirectoryProvider createCacheDirectoryProvider()
meth public org.netbeans.spi.project.support.ant.EditableProperties getProperties(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator getStandardPropertyEvaluator()
meth public org.netbeans.spi.project.support.ant.PropertyProvider getProjectLibrariesPropertyProvider()
meth public org.netbeans.spi.project.support.ant.PropertyProvider getPropertyProvider(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyProvider getStockPropertyPreprovider()
meth public org.netbeans.spi.queries.FileBuiltQueryImplementation createGlobFileBuiltQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public org.netbeans.spi.queries.SharabilityQueryImplementation createSharabilityQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public org.openide.filesystems.FileObject getProjectDirectory()
meth public org.openide.filesystems.FileObject resolveFileObject(java.lang.String)
meth public org.w3c.dom.Element getPrimaryConfigurationData(boolean)
meth public void addAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void notifyDeleted()
meth public void putPrimaryConfigurationData(org.w3c.dom.Element,boolean)
meth public void putProperties(java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties)
meth public void removeAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void setLibrariesLocation(java.lang.String)
supr java.lang.Object
hfds NONEXISTENT,PRIVATE_NS,PROJECT_NS,QUIETLY_SWALLOW_XML_LOAD_ERRORS,RP,db,dir,fileListener,listeners,modifiedMetadataPaths,pendingHook,pendingHookCount,privateXml,privateXmlValid,projectXml,projectXmlValid,properties,state,type,writingXML
hcls ActionImpl,FileListener,RunnableImpl

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntProjectListener
intf java.util.EventListener
meth public abstract void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public abstract void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)

CLSS public final org.netbeans.spi.project.support.ant.EditableProperties
cons public EditableProperties()
cons public EditableProperties(boolean)
cons public EditableProperties(java.util.Map<java.lang.String,java.lang.String>)
intf java.lang.Cloneable
meth public java.lang.Object clone()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String[])
meth public java.lang.String[] getComment(java.lang.String)
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public org.netbeans.spi.project.support.ant.EditableProperties cloneProperties()
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void setComment(java.lang.String,java.lang.String[],boolean)
meth public void store(java.io.OutputStream) throws java.io.IOException
supr java.util.AbstractMap<java.lang.String,java.lang.String>
hfds INDENT,READING_KEY_VALUE,WAITING_FOR_KEY_VALUE,alphabetize,commentChars,itemIndex,items,keyValueSeparators,strictKeyValueSeparators,whiteSpaceChars
hcls Item,IteratorImpl,MapEntryImpl,SetImpl

CLSS public abstract org.netbeans.spi.project.support.ant.FilterPropertyProvider
cons protected FilterPropertyProvider(org.netbeans.spi.project.support.ant.PropertyProvider)
intf org.netbeans.spi.project.support.ant.PropertyProvider
meth protected final void setDelegate(org.netbeans.spi.project.support.ant.PropertyProvider)
meth public final java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds cs,delegate,strongListener,weakListener

CLSS public final org.netbeans.spi.project.support.ant.GeneratedFilesHelper
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper)
cons public GeneratedFilesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.project.ant.AntBuildExtender)
cons public GeneratedFilesHelper(org.openide.filesystems.FileObject)
fld public final static int FLAG_MISSING = 2
fld public final static int FLAG_MODIFIED = 4
fld public final static int FLAG_OLD_PROJECT_XML = 8
fld public final static int FLAG_OLD_STYLESHEET = 16
fld public final static int FLAG_UNKNOWN = 32
fld public final static java.lang.String BUILD_IMPL_XML_PATH = "nbproject/build-impl.xml"
fld public final static java.lang.String BUILD_XML_PATH = "build.xml"
meth public boolean refreshBuildScript(java.lang.String,java.net.URL,boolean) throws java.io.IOException
meth public int getBuildScriptState(java.lang.String,java.net.URL)
meth public void generateBuildScriptFromStylesheet(java.lang.String,java.net.URL) throws java.io.IOException
supr java.lang.Object
hfds GENFILES_PROPERTIES_PATH,KEY_SUFFIX_DATA_CRC,KEY_SUFFIX_SCRIPT_CRC,KEY_SUFFIX_STYLESHEET_CRC_PLUS_VERSION,STYLESHEET_VERSIONS,crcCache,crcCacheTimestampsXorSizes,dir,extender,h
hcls EolFilterOutputStream

CLSS public final org.netbeans.spi.project.support.ant.PathMatcher
cons public PathMatcher(java.lang.String,java.lang.String,java.io.File)
meth public boolean matches(java.lang.String,boolean)
meth public java.lang.String toString()
meth public java.util.Set<java.io.File> findIncludedRoots()
supr java.lang.Object
hfds base,excludePattern,excludes,includePattern,includes,knownIncludes

CLSS public org.netbeans.spi.project.support.ant.ProjectGenerator
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
cons protected ProjectXmlSavedHook()
meth protected abstract void projectXmlSaved() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyEvaluator
meth public abstract java.lang.String evaluate(java.lang.String)
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.spi.project.support.ant.PropertyUtils
meth public !varargs static org.netbeans.spi.project.support.ant.PropertyEvaluator sequentialPropertyEvaluator(org.netbeans.spi.project.support.ant.PropertyProvider,org.netbeans.spi.project.support.ant.PropertyProvider[])
meth public static boolean isUsablePropertyName(java.lang.String)
meth public static java.io.File resolveFile(java.io.File,java.lang.String)
meth public static java.lang.String getUsablePropertyName(java.lang.String)
meth public static java.lang.String relativizeFile(java.io.File,java.io.File)
meth public static java.lang.String[] tokenizePath(java.lang.String)
meth public static org.netbeans.spi.project.support.ant.EditableProperties getGlobalProperties()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider fixedPropertyProvider(java.util.Map<java.lang.String,java.lang.String>)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider globalPropertyProvider()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider propertiesFilePropertyProvider(java.io.File)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider userPropertiesProvider(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.io.File)
meth public static void putGlobalProperties(org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object
hfds RELATIVE_SLASH_SEPARATED_PATH,VALID_PROPERTY_NAME,globalPropertyProviders
hcls FilePropertyProvider,FixedPropertyProvider,SequentialPropertyEvaluator,UserPropertiesProvider

CLSS public final org.netbeans.spi.project.support.ant.ReferenceHelper
cons public ReferenceHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.AuxiliaryConfiguration,org.netbeans.spi.project.support.ant.PropertyEvaluator)
innr public final static RawReference
meth public boolean addRawReference(org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference)
meth public boolean addReference(org.netbeans.api.project.ant.AntArtifact)
 anno 0 java.lang.Deprecated()
meth public boolean destroyReference(java.lang.String)
meth public boolean isReferenced(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public boolean removeRawReference(java.lang.String,java.lang.String)
meth public boolean removeReference(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public boolean removeReference(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object[] findArtifactAndLocation(java.lang.String)
meth public java.lang.String addReference(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public java.lang.String createExtraForeignFileReferenceAsIs(java.lang.String,java.lang.String)
meth public java.lang.String createForeignFileReference(java.io.File,java.lang.String)
meth public java.lang.String createForeignFileReference(org.netbeans.api.project.ant.AntArtifact)
 anno 0 java.lang.Deprecated()
meth public java.lang.String createForeignFileReferenceAsIs(java.lang.String,java.lang.String)
meth public java.lang.String createLibraryReference(org.netbeans.api.project.libraries.Library,java.lang.String)
meth public org.netbeans.api.project.ant.AntArtifact getForeignFileReferenceAsArtifact(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.project.libraries.Library copyLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
meth public org.netbeans.api.project.libraries.Library findLibrary(java.lang.String)
meth public org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler getLibraryChooserImportHandler()
meth public org.netbeans.api.project.libraries.LibraryManager getProjectLibraryManager()
meth public org.netbeans.spi.project.SubprojectProvider createSubprojectProvider()
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference getRawReference(java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference[] getRawReferences()
meth public static org.netbeans.api.project.libraries.LibraryManager getProjectLibraryManager(org.netbeans.api.project.Project)
meth public void addExtraBaseDirectory(java.lang.String)
meth public void destroyForeignFileReference(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void fixReferences(java.io.File)
meth public void removeExtraBaseDirectory(java.lang.String)
supr java.lang.Object
hfds FOREIGN_FILE_REFERENCE,FOREIGN_FILE_REFERENCE_OLD,FOREIGN_PLAIN_FILE_REFERENCE,LIBRARY_REFERENCE,REFS_NAME,REFS_NS,REFS_NS2,REF_NAME,aux,eval,extraBaseDirectories,h

CLSS public final static org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
cons public RawReference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Properties)
cons public RawReference(java.lang.String,java.lang.String,java.net.URI,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getArtifactType()
meth public java.lang.String getCleanTargetName()
meth public java.lang.String getForeignProjectName()
meth public java.lang.String getID()
meth public java.lang.String getScriptLocationValue()
meth public java.lang.String getTargetName()
meth public java.lang.String toString()
meth public java.net.URI getScriptLocation()
 anno 0 java.lang.Deprecated()
meth public java.util.Properties getProperties()
meth public org.netbeans.api.project.ant.AntArtifact toAntArtifact(org.netbeans.spi.project.support.ant.ReferenceHelper)
supr java.lang.Object
hfds SUB_ELEMENT_NAMES,artifactID,artifactType,cleanTargetName,foreignProjectName,newScriptLocation,props,scriptLocation,targetName

CLSS public final org.netbeans.spi.project.support.ant.SourcesHelper
cons public SourcesHelper(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
cons public SourcesHelper(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.project.Sources createSources()
meth public void addNonSourceRoot(java.lang.String)
meth public void addOwnedFile(java.lang.String)
meth public void addPrincipalSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void addPrincipalSourceRoot(java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
meth public void registerExternalRoots(int)
meth public void registerExternalRoots(int,boolean)
supr java.lang.Object
hfds aph,evaluator,lastRegisteredRoots,minimalSubfolders,nonSourceRoots,ownedFiles,principalSourceRoots,project,propChangeL,registeredRootAlgorithm,typedSourceRoots
hcls PropChangeL,Root,SourceRoot,SourcesImpl,TypedSourceRoot

CLSS public org.netbeans.spi.project.support.ant.ui.StoreGroup
cons public StoreGroup()
meth public final javax.swing.JToggleButton$ToggleButtonModel createInverseToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.JToggleButton$ToggleButtonModel createToggleButtonModel(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public final javax.swing.text.Document createStringDocument(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
meth public void store(org.netbeans.spi.project.support.ant.EditableProperties)
supr java.lang.Object
hfds BOOLEAN_KIND_ED,BOOLEAN_KIND_TF,BOOLEAN_KIND_YN,documentListener,models,modifiedDocuments

CLSS public org.netbeans.spi.project.support.ant.ui.VariablesSupport
meth public static void showVariablesCustomizer()
supr java.lang.Object

