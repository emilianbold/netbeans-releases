#API master signature file
#Version 6.6.1
CLSS public static abstract org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable
cons public ActionRunnable(java.awt.event.ActionEvent,org.openide.util.actions.SystemAction,boolean)
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
intf java.awt.event.ActionListener
intf java.util.EventListener
intf javax.swing.Action
meth protected abstract void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.run()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final boolean org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.isEnabled()
meth public final boolean org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.needsToBeSynchronous()
meth public final java.lang.Object org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.getValue(java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.actionPerformed(java.awt.event.ActionEvent)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.doRun()
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.putValue(java.lang.String,java.lang.Object)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.netbeans.modules.openide.util.ActionsBridge$ActionRunnable.setEnabled(boolean)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static abstract interface org.openide.actions.ToolsAction$Model
meth public abstract [Lorg.openide.util.actions.SystemAction; org.openide.actions.ToolsAction$Model.getActions()
meth public abstract void org.openide.actions.ToolsAction$Model.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.openide.actions.ToolsAction$Model.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public static abstract interface org.openide.awt.Actions$SubMenuModel
meth public abstract int org.openide.awt.Actions$SubMenuModel.getCount()
meth public abstract java.lang.String org.openide.awt.Actions$SubMenuModel.getLabel(int)
meth public abstract org.openide.util.HelpCtx org.openide.awt.Actions$SubMenuModel.getHelpCtx(int)
meth public abstract void org.openide.awt.Actions$SubMenuModel.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.openide.awt.Actions$SubMenuModel.performActionAt(int)
meth public abstract void org.openide.awt.Actions$SubMenuModel.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public static abstract interface org.openide.util.HelpCtx$Provider
meth public abstract org.openide.util.HelpCtx org.openide.util.HelpCtx$Provider.getHelpCtx()
supr null
CLSS public static abstract interface org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
supr null
CLSS public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem org.openide.util.actions.Presenter$Menu.getMenuPresenter()
supr null
CLSS public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem org.openide.util.actions.Presenter$Popup.getPopupPresenter()
supr null
CLSS public static abstract interface org.openide.util.actions.Presenter$Toolbar
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component org.openide.util.actions.Presenter$Toolbar.getToolbarPresenter()
supr null
CLSS public abstract org.openide.actions.ActionManager
cons public ActionManager()
fld  constant public static final java.lang.String org.openide.actions.ActionManager.PROP_CONTEXT_ACTIONS
meth protected final void org.openide.actions.ActionManager.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Lorg.openide.util.actions.SystemAction; org.openide.actions.ActionManager.getContextActions()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.actions.ActionManager.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.actions.ActionManager.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.actions.ActionManager org.openide.actions.ActionManager.getDefault()
meth public void org.openide.actions.ActionManager.invokeAction(javax.swing.Action,java.awt.event.ActionEvent)
supr java.lang.Object
CLSS public org.openide.actions.CloneViewAction
cons public CloneViewAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.CloneViewAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.CloneViewAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.actions.CloneViewAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.CloneViewAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.CloneViewAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.CloseViewAction
cons public CloseViewAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.CloseViewAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.actions.CloseViewAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.CloseViewAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.CloseViewAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.CopyAction
cons public CopyAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.CopyAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.CopyAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.CopyAction.initialize()
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.actions.CopyAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.CopyAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.CopyAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.CustomizeAction
cons public CustomizeAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.CustomizeAction.asynchronous()
meth protected boolean org.openide.actions.CustomizeAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.CustomizeAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.CustomizeAction.getName()
meth public javax.swing.Action org.openide.util.actions.NodeAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.CustomizeAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public org.openide.actions.CutAction
cons public CutAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.CutAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.CutAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.CutAction.initialize()
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.actions.CutAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.CutAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.CutAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.DeleteAction
cons public DeleteAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.DeleteAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.DeleteAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.DeleteAction.initialize()
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.actions.DeleteAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.DeleteAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.DeleteAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.EditAction
cons public EditAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected [Ljava.lang.Class; org.openide.actions.EditAction.cookieClasses()
meth protected boolean org.openide.actions.EditAction.asynchronous()
meth protected boolean org.openide.actions.EditAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected int org.openide.actions.EditAction.mode()
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.EditAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.EditAction.getName()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.EditAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CookieAction
CLSS public org.openide.actions.FindAction
cons public FindAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.FindAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.FindAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.FindAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.FindAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.GarbageCollectAction
cons public GarbageCollectAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.GarbageCollectAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.actions.GarbageCollectAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.GarbageCollectAction.getName()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.GarbageCollectAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.actions.GarbageCollectAction.performAction()
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
CLSS public org.openide.actions.GotoAction
cons public GotoAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.GotoAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.GotoAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.GotoAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public final org.openide.actions.MoveDownAction
cons public MoveDownAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.MoveDownAction.asynchronous()
meth protected boolean org.openide.actions.MoveDownAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.MoveDownAction.initialize()
meth protected void org.openide.actions.MoveDownAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.MoveDownAction.getName()
meth public javax.swing.Action org.openide.util.actions.NodeAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.MoveDownAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public final org.openide.actions.MoveUpAction
cons public MoveUpAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.MoveUpAction.asynchronous()
meth protected boolean org.openide.actions.MoveUpAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.MoveUpAction.initialize()
meth protected void org.openide.actions.MoveUpAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.MoveUpAction.getName()
meth public javax.swing.Action org.openide.util.actions.NodeAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.MoveUpAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public final org.openide.actions.NewAction
cons public NewAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.NewAction.asynchronous()
meth protected boolean org.openide.actions.NewAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.NewAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.NewAction.getName()
meth public javax.swing.Action org.openide.actions.NewAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.actions.NewAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.actions.NewAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.NewAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public org.openide.actions.NextTabAction
cons public NextTabAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.NextTabAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.NextTabAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.NextTabAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.NextTabAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.OpenAction
cons public OpenAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected [Ljava.lang.Class; org.openide.actions.OpenAction.cookieClasses()
meth protected boolean org.openide.actions.OpenAction.asynchronous()
meth protected boolean org.openide.actions.OpenAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected int org.openide.actions.OpenAction.mode()
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.OpenAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.OpenAction.getName()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.OpenAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CookieAction
CLSS public final org.openide.actions.OpenLocalExplorerAction
cons public OpenLocalExplorerAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.OpenLocalExplorerAction.asynchronous()
meth protected boolean org.openide.actions.OpenLocalExplorerAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.OpenLocalExplorerAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.OpenLocalExplorerAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.OpenLocalExplorerAction.getName()
meth public javax.swing.Action org.openide.util.actions.NodeAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.OpenLocalExplorerAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public final org.openide.actions.PageSetupAction
cons public PageSetupAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.PageSetupAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.PageSetupAction.getName()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.PageSetupAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public synchronized void org.openide.actions.PageSetupAction.performAction()
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
CLSS public final org.openide.actions.PasteAction
cons public PasteAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.PasteAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.PasteAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.PasteAction.initialize()
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public [Lorg.openide.util.datatransfer.PasteType; org.openide.actions.PasteAction.getPasteTypes()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.actions.PasteAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.PasteAction.getName()
meth public javax.swing.Action org.openide.actions.PasteAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.actions.PasteAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.actions.PasteAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.PasteAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.actions.PasteAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.actions.PasteAction.setPasteTypes([Lorg.openide.util.datatransfer.PasteType;)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public final org.openide.actions.PopupAction
cons public PopupAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.PopupAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.PopupAction.initialize()
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.PopupAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.PopupAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.PreviousTabAction
cons public PreviousTabAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.PreviousTabAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.PreviousTabAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.PreviousTabAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.PreviousTabAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.PrintAction
cons public PrintAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected [Ljava.lang.Class; org.openide.actions.PrintAction.cookieClasses()
meth protected boolean org.openide.actions.PrintAction.asynchronous()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected int org.openide.actions.PrintAction.mode()
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.PrintAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.PrintAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.PrintAction.getName()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.PrintAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CookieAction
CLSS public org.openide.actions.PropertiesAction
cons public PropertiesAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.PropertiesAction.asynchronous()
meth protected boolean org.openide.actions.PropertiesAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.PropertiesAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.PropertiesAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.PropertiesAction.getName()
meth public javax.swing.Action org.openide.actions.PropertiesAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.actions.PropertiesAction.getPopupPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.PropertiesAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public org.openide.actions.RedoAction
cons public RedoAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.RedoAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.RedoAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public boolean org.openide.actions.RedoAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.RedoAction.getName()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.RedoAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.actions.RedoAction.performAction()
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
CLSS public org.openide.actions.RenameAction
cons public RenameAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.RenameAction.asynchronous()
meth protected boolean org.openide.actions.RenameAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.actions.RenameAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.RenameAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.RenameAction.getName()
meth public javax.swing.Action org.openide.util.actions.NodeAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.RenameAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.NodeAction
CLSS public org.openide.actions.ReorderAction
cons public ReorderAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected [Ljava.lang.Class; org.openide.actions.ReorderAction.cookieClasses()
meth protected boolean org.openide.actions.ReorderAction.asynchronous()
meth protected boolean org.openide.actions.ReorderAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected int org.openide.actions.ReorderAction.mode()
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.ReorderAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.ReorderAction.getName()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.ReorderAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CookieAction
CLSS public org.openide.actions.ReplaceAction
cons public ReplaceAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.ReplaceAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.ReplaceAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.ReplaceAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.SaveAction
cons public SaveAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected [Ljava.lang.Class; org.openide.actions.SaveAction.cookieClasses()
meth protected boolean org.openide.actions.SaveAction.asynchronous()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.NodeAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected int org.openide.actions.SaveAction.mode()
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.SaveAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.SaveAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.SaveAction.getName()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.SaveAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CookieAction
CLSS public org.openide.actions.ToolsAction
cons public ToolsAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.actions.ToolsAction$Model
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.ToolsAction.getName()
meth public javax.swing.Action org.openide.actions.ToolsAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.actions.ToolsAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.actions.ToolsAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.ToolsAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public static void org.openide.actions.ToolsAction.setModel(org.openide.actions.ToolsAction$Model)
meth public void org.openide.actions.ToolsAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.SystemAction
CLSS public org.openide.actions.UndoAction
cons public UndoAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.UndoAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.UndoAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public boolean org.openide.actions.UndoAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.UndoAction.getName()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.UndoAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.actions.UndoAction.performAction()
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
CLSS public org.openide.actions.UndockAction
cons public UndockAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.actions.UndockAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.actions.UndockAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public boolean org.openide.util.actions.CallbackSystemAction.getSurviveFocusChange()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.Object org.openide.util.actions.CallbackSystemAction.getActionMapKey()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.UndockAction.getName()
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.UndockAction.getHelpCtx()
meth public org.openide.util.actions.ActionPerformer org.openide.util.actions.CallbackSystemAction.getActionPerformer()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallbackSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.CallbackSystemAction.performAction()
meth public void org.openide.util.actions.CallbackSystemAction.setActionPerformer(org.openide.util.actions.ActionPerformer)
meth public void org.openide.util.actions.CallbackSystemAction.setSurviveFocusChange(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallbackSystemAction
CLSS public org.openide.actions.ViewAction
cons public ViewAction()
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ALL
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ANY
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_EXACTLY_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_ONE
fld  constant public static final int org.openide.util.actions.CookieAction.MODE_SOME
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.ContextAwareAction
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected [Ljava.lang.Class; org.openide.actions.ViewAction.cookieClasses()
meth protected boolean org.openide.actions.ViewAction.asynchronous()
meth protected boolean org.openide.actions.ViewAction.surviveFocusChange()
meth protected boolean org.openide.util.actions.CookieAction.enable([Lorg.openide.nodes.Node;)
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected int org.openide.actions.ViewAction.mode()
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.actions.ViewAction.performAction([Lorg.openide.nodes.Node;)
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.NodeAction.addNotify()
meth protected void org.openide.util.actions.NodeAction.initialize()
meth protected void org.openide.util.actions.NodeAction.removeNotify()
meth public boolean org.openide.util.actions.NodeAction.isEnabled()
meth public final [Lorg.openide.nodes.Node; org.openide.util.actions.NodeAction.getActivatedNodes()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.ViewAction.getName()
meth public javax.swing.Action org.openide.util.actions.CookieAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.ViewAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.NodeAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.NodeAction.performAction()
meth public void org.openide.util.actions.NodeAction.setEnabled(boolean)
supr org.openide.util.actions.CookieAction
CLSS public org.openide.actions.WorkspaceSwitchAction
cons public WorkspaceSwitchAction()
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ENABLED
fld  constant public static final java.lang.String org.openide.util.actions.SystemAction.PROP_ICON
fld  public static final java.lang.String javax.swing.Action.ACCELERATOR_KEY
fld  public static final java.lang.String javax.swing.Action.ACTION_COMMAND_KEY
fld  public static final java.lang.String javax.swing.Action.DEFAULT
fld  public static final java.lang.String javax.swing.Action.LONG_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.MNEMONIC_KEY
fld  public static final java.lang.String javax.swing.Action.NAME
fld  public static final java.lang.String javax.swing.Action.SHORT_DESCRIPTION
fld  public static final java.lang.String javax.swing.Action.SMALL_ICON
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean org.openide.util.actions.CallableSystemAction.asynchronous()
meth protected boolean org.openide.util.actions.SystemAction.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected java.lang.String org.openide.util.actions.SystemAction.iconResource()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth protected void org.openide.util.actions.SystemAction.initialize()
meth public boolean org.openide.util.actions.SystemAction.isEnabled()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final java.lang.Object org.openide.util.actions.SystemAction.getValue(java.lang.String)
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon()
meth public final javax.swing.Icon org.openide.util.actions.SystemAction.getIcon(boolean)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.actions.SystemAction.putValue(java.lang.String,java.lang.Object)
meth public final void org.openide.util.actions.SystemAction.setIcon(javax.swing.Icon)
meth public java.awt.Component org.openide.util.actions.CallableSystemAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.actions.WorkspaceSwitchAction.getName()
meth public javax.swing.JMenuItem org.openide.actions.WorkspaceSwitchAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.openide.util.HelpCtx org.openide.actions.WorkspaceSwitchAction.getHelpCtx()
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.actions.WorkspaceSwitchAction.performAction()
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
