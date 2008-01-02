#API master signature file
#Version 7.10.1
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
CLSS public static abstract interface org.openide.ErrorManager$Annotation
meth public abstract int org.openide.ErrorManager$Annotation.getSeverity()
meth public abstract java.lang.String org.openide.ErrorManager$Annotation.getLocalizedMessage()
meth public abstract java.lang.String org.openide.ErrorManager$Annotation.getMessage()
meth public abstract java.lang.Throwable org.openide.ErrorManager$Annotation.getStackTrace()
meth public abstract java.util.Date org.openide.ErrorManager$Annotation.getDate()
supr null
CLSS public static final org.openide.ServiceType$Handle
cons public Handle(org.openide.ServiceType)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.ServiceType$Handle.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.ServiceType org.openide.ServiceType$Handle.getServiceType()
supr java.lang.Object
CLSS public static abstract org.openide.ServiceType$Registry
cons public Registry()
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.util.Enumeration org.openide.ServiceType$Registry.services()
meth public abstract java.util.List org.openide.ServiceType$Registry.getServiceTypes()
meth public abstract void org.openide.ServiceType$Registry.setServiceTypes(java.util.List)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Enumeration org.openide.ServiceType$Registry.services(java.lang.Class)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.ServiceType org.openide.ServiceType$Registry.find(java.lang.Class)
meth public org.openide.ServiceType org.openide.ServiceType$Registry.find(java.lang.String)
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Enumerations$Processor
meth public abstract java.lang.Object org.openide.util.Enumerations$Processor.process(java.lang.Object,java.util.Collection)
supr null
CLSS public static abstract interface org.openide.util.HelpCtx$Provider
meth public abstract org.openide.util.HelpCtx org.openide.util.HelpCtx$Provider.getHelpCtx()
supr null
CLSS public static abstract org.openide.util.Lookup$Item
cons public Item()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Class org.openide.util.Lookup$Item.getType()
meth public abstract java.lang.Object org.openide.util.Lookup$Item.getInstance()
meth public abstract java.lang.String org.openide.util.Lookup$Item.getDisplayName()
meth public abstract java.lang.String org.openide.util.Lookup$Item.getId()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.Lookup$Item.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Lookup$Provider
meth public abstract org.openide.util.Lookup org.openide.util.Lookup$Provider.getLookup()
supr null
CLSS public static abstract org.openide.util.Lookup$Result
cons public Result()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.util.Collection org.openide.util.Lookup$Result.allInstances()
meth public abstract void org.openide.util.Lookup$Result.addLookupListener(org.openide.util.LookupListener)
meth public abstract void org.openide.util.Lookup$Result.removeLookupListener(org.openide.util.LookupListener)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Collection org.openide.util.Lookup$Result.allItems()
meth public java.util.Set org.openide.util.Lookup$Result.allClasses()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static final org.openide.util.Lookup$Template
cons public Template()
cons public Template(java.lang.Class)
cons public Template(java.lang.Class,java.lang.String,java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.util.Lookup$Template.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.util.Lookup$Template.hashCode()
meth public java.lang.Class org.openide.util.Lookup$Template.getType()
meth public java.lang.Object org.openide.util.Lookup$Template.getInstance()
meth public java.lang.String org.openide.util.Lookup$Template.getId()
meth public java.lang.String org.openide.util.Lookup$Template.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Mutex$Action
intf org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$Action.run()
supr null
CLSS public static abstract interface org.openide.util.Mutex$ExceptionAction
meth public abstract java.lang.Object org.openide.util.Mutex$ExceptionAction.run() throws java.lang.Exception
supr null
CLSS public static final org.openide.util.Mutex$Privileged
cons public Privileged()
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
meth public void org.openide.util.Mutex$Privileged.enterReadAccess()
meth public void org.openide.util.Mutex$Privileged.enterWriteAccess()
meth public void org.openide.util.Mutex$Privileged.exitReadAccess()
meth public void org.openide.util.Mutex$Privileged.exitWriteAccess()
supr java.lang.Object
CLSS public static abstract interface org.openide.util.NbBundle$ClassLoaderFinder
meth public abstract java.lang.ClassLoader org.openide.util.NbBundle$ClassLoaderFinder.find()
supr null
CLSS public final org.openide.util.RequestProcessor$Task
fld  public static final org.openide.util.Task org.openide.util.Task.EMPTY
intf java.lang.Runnable
intf org.openide.util.Cancellable
meth protected final void org.openide.util.Task.notifyFinished()
meth protected final void org.openide.util.Task.notifyRunning()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.RequestProcessor$Task.cancel()
meth public boolean org.openide.util.RequestProcessor$Task.waitFinished(long) throws java.lang.InterruptedException
meth public final boolean org.openide.util.Task.isFinished()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.util.RequestProcessor$Task.getDelay()
meth public int org.openide.util.RequestProcessor$Task.getPriority()
meth public java.lang.String org.openide.util.RequestProcessor$Task.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized void org.openide.util.Task.addTaskListener(org.openide.util.TaskListener)
meth public synchronized void org.openide.util.Task.removeTaskListener(org.openide.util.TaskListener)
meth public void org.openide.util.RequestProcessor$Task.run()
meth public void org.openide.util.RequestProcessor$Task.schedule(int)
meth public void org.openide.util.RequestProcessor$Task.setPriority(int)
meth public void org.openide.util.RequestProcessor$Task.waitFinished()
supr org.openide.util.Task
CLSS public static org.openide.util.Utilities$UnorderableException
cons public UnorderableException(java.lang.String,java.util.Collection,java.util.Map)
cons public UnorderableException(java.util.Collection,java.util.Map)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public java.util.Collection org.openide.util.Utilities$UnorderableException.getUnorderable()
meth public java.util.Map org.openide.util.Utilities$UnorderableException.getDeps()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.RuntimeException
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
CLSS public static abstract interface org.openide.util.datatransfer.ExClipboard$Convertor
meth public abstract java.awt.datatransfer.Transferable org.openide.util.datatransfer.ExClipboard$Convertor.convert(java.awt.datatransfer.Transferable)
supr null
CLSS public static org.openide.util.datatransfer.ExTransferable$Multi
cons public Multi([Ljava.awt.datatransfer.Transferable;)
intf java.awt.datatransfer.Transferable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.awt.datatransfer.DataFlavor; org.openide.util.datatransfer.ExTransferable$Multi.getTransferDataFlavors()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.datatransfer.ExTransferable$Multi.isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.util.datatransfer.ExTransferable$Multi.getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static abstract org.openide.util.datatransfer.ExTransferable$Single
cons public Single(java.awt.datatransfer.DataFlavor)
intf java.awt.datatransfer.Transferable
meth protected abstract java.lang.Object org.openide.util.datatransfer.ExTransferable$Single.getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.awt.datatransfer.DataFlavor; org.openide.util.datatransfer.ExTransferable$Single.getTransferDataFlavors()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.datatransfer.ExTransferable$Single.isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.util.datatransfer.ExTransferable$Single.getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static org.openide.util.lookup.AbstractLookup$Content
cons public Content()
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.lookup.AbstractLookup$Content.addPair(org.openide.util.lookup.AbstractLookup$Pair)
meth public final void org.openide.util.lookup.AbstractLookup$Content.removePair(org.openide.util.lookup.AbstractLookup$Pair)
meth public final void org.openide.util.lookup.AbstractLookup$Content.setPairs(java.util.Collection)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public static abstract org.openide.util.lookup.AbstractLookup$Pair
cons protected Pair()
intf java.io.Serializable
meth protected abstract boolean org.openide.util.lookup.AbstractLookup$Pair.creatorOf(java.lang.Object)
meth protected abstract boolean org.openide.util.lookup.AbstractLookup$Pair.instanceOf(java.lang.Class)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Class org.openide.util.Lookup$Item.getType()
meth public abstract java.lang.Object org.openide.util.Lookup$Item.getInstance()
meth public abstract java.lang.String org.openide.util.Lookup$Item.getDisplayName()
meth public abstract java.lang.String org.openide.util.Lookup$Item.getId()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.Lookup$Item.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.openide.util.Lookup$Item
CLSS public static abstract interface org.openide.util.lookup.InstanceContent$Convertor
meth public abstract java.lang.Class org.openide.util.lookup.InstanceContent$Convertor.type(java.lang.Object)
meth public abstract java.lang.Object org.openide.util.lookup.InstanceContent$Convertor.convert(java.lang.Object)
meth public abstract java.lang.String org.openide.util.lookup.InstanceContent$Convertor.displayName(java.lang.Object)
meth public abstract java.lang.String org.openide.util.lookup.InstanceContent$Convertor.id(java.lang.Object)
supr null
CLSS public abstract org.openide.ErrorManager
cons public ErrorManager()
fld  constant public static final int org.openide.ErrorManager.ERROR
fld  constant public static final int org.openide.ErrorManager.EXCEPTION
fld  constant public static final int org.openide.ErrorManager.INFORMATIONAL
fld  constant public static final int org.openide.ErrorManager.UNKNOWN
fld  constant public static final int org.openide.ErrorManager.USER
fld  constant public static final int org.openide.ErrorManager.WARNING
innr public static abstract interface org.openide.ErrorManager$Annotation
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Lorg.openide.ErrorManager$Annotation; org.openide.ErrorManager.findAnnotations(java.lang.Throwable)
meth public abstract java.lang.Throwable org.openide.ErrorManager.annotate(java.lang.Throwable,int,java.lang.String,java.lang.String,java.lang.Throwable,java.util.Date)
meth public abstract java.lang.Throwable org.openide.ErrorManager.attachAnnotations(java.lang.Throwable,[Lorg.openide.ErrorManager$Annotation;)
meth public abstract org.openide.ErrorManager org.openide.ErrorManager.getInstance(java.lang.String)
meth public abstract void org.openide.ErrorManager.log(int,java.lang.String)
meth public abstract void org.openide.ErrorManager.notify(int,java.lang.Throwable)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.ErrorManager.isLoggable(int)
meth public boolean org.openide.ErrorManager.isNotifiable(int)
meth public final java.lang.Throwable org.openide.ErrorManager.annotate(java.lang.Throwable,java.lang.String)
meth public final java.lang.Throwable org.openide.ErrorManager.annotate(java.lang.Throwable,java.lang.Throwable)
meth public final java.lang.Throwable org.openide.ErrorManager.copyAnnotation(java.lang.Throwable,java.lang.Throwable)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.ErrorManager.log(java.lang.String)
meth public final void org.openide.ErrorManager.notify(java.lang.Throwable)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.ErrorManager org.openide.ErrorManager.getDefault()
supr java.lang.Object
CLSS public abstract org.openide.LifecycleManager
cons protected LifecycleManager()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.openide.LifecycleManager.exit()
meth public abstract void org.openide.LifecycleManager.saveAll()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.LifecycleManager org.openide.LifecycleManager.getDefault()
supr java.lang.Object
CLSS public abstract org.openide.ServiceType
cons public ServiceType()
fld  constant public static final java.lang.String org.openide.ServiceType.PROP_NAME
innr public static abstract org.openide.ServiceType$Registry
innr public static final org.openide.ServiceType$Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void org.openide.ServiceType.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object org.openide.ServiceType.clone() throws java.lang.CloneNotSupportedException
meth protected java.lang.String org.openide.ServiceType.displayName()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.openide.util.HelpCtx org.openide.ServiceType.getHelpCtx()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final org.openide.ServiceType org.openide.ServiceType.createClone()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.ServiceType.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.ServiceType.getName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized final void org.openide.ServiceType.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.ServiceType.setName(java.lang.String)
supr java.lang.Object
CLSS public abstract interface org.openide.util.AsyncGUIJob
meth public abstract void org.openide.util.AsyncGUIJob.construct()
meth public abstract void org.openide.util.AsyncGUIJob.finished()
supr null
CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean org.openide.util.Cancellable.cancel()
supr null
CLSS public final org.openide.util.ChangeSupport
cons public ChangeSupport(java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.ChangeSupport.hasListeners()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.util.ChangeSupport.addChangeListener(javax.swing.event.ChangeListener)
meth public void org.openide.util.ChangeSupport.fireChange()
meth public void org.openide.util.ChangeSupport.removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
CLSS public abstract interface org.openide.util.ContextAwareAction
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
meth public abstract boolean javax.swing.Action.isEnabled()
meth public abstract java.lang.Object javax.swing.Action.getValue(java.lang.String)
meth public abstract javax.swing.Action org.openide.util.ContextAwareAction.createContextAwareInstance(org.openide.util.Lookup)
meth public abstract void java.awt.event.ActionListener.actionPerformed(java.awt.event.ActionEvent)
meth public abstract void javax.swing.Action.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void javax.swing.Action.putValue(java.lang.String,java.lang.Object)
meth public abstract void javax.swing.Action.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void javax.swing.Action.setEnabled(boolean)
supr null
CLSS public abstract interface org.openide.util.ContextGlobalProvider
meth public abstract org.openide.util.Lookup org.openide.util.ContextGlobalProvider.createGlobalContext()
supr null
CLSS public final org.openide.util.Enumerations
innr public static abstract interface org.openide.util.Enumerations$Processor
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
meth public static final java.util.Enumeration org.openide.util.Enumerations.empty()
meth public static java.util.Enumeration org.openide.util.Enumerations.concat(java.util.Enumeration)
meth public static java.util.Enumeration org.openide.util.Enumerations.concat(java.util.Enumeration,java.util.Enumeration)
meth public static java.util.Enumeration org.openide.util.Enumerations.convert(java.util.Enumeration,org.openide.util.Enumerations$Processor)
meth public static java.util.Enumeration org.openide.util.Enumerations.filter(java.util.Enumeration,org.openide.util.Enumerations$Processor)
meth public static java.util.Enumeration org.openide.util.Enumerations.queue(java.util.Enumeration,org.openide.util.Enumerations$Processor)
meth public static java.util.Enumeration org.openide.util.Enumerations.removeDuplicates(java.util.Enumeration)
meth public static java.util.Enumeration org.openide.util.Enumerations.removeNulls(java.util.Enumeration)
meth public static java.util.Enumeration org.openide.util.Enumerations.singleton(java.lang.Object)
meth public static transient java.util.Enumeration org.openide.util.Enumerations.array([Ljava.lang.Object;)
supr java.lang.Object
CLSS public final org.openide.util.Exceptions
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
meth public static java.lang.String org.openide.util.Exceptions.findLocalizedMessage(java.lang.Throwable)
meth public static java.lang.Throwable org.openide.util.Exceptions.attachLocalizedMessage(java.lang.Throwable,java.lang.String)
meth public static java.lang.Throwable org.openide.util.Exceptions.attachMessage(java.lang.Throwable,java.lang.String)
meth public static void org.openide.util.Exceptions.printStackTrace(java.lang.Throwable)
supr java.lang.Object
CLSS public final org.openide.util.HelpCtx
cons public HelpCtx(java.lang.Class)
cons public HelpCtx(java.lang.String)
cons public HelpCtx(java.net.URL)
fld  public static final org.openide.util.HelpCtx org.openide.util.HelpCtx.DEFAULT_HELP
innr public static abstract interface org.openide.util.HelpCtx$Provider
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.util.HelpCtx.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.util.HelpCtx.hashCode()
meth public java.lang.String org.openide.util.HelpCtx.getHelpID()
meth public java.lang.String org.openide.util.HelpCtx.toString()
meth public java.net.URL org.openide.util.HelpCtx.getHelp()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static org.openide.util.HelpCtx org.openide.util.HelpCtx.findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx org.openide.util.HelpCtx.findHelp(java.lang.Object)
meth public static void org.openide.util.HelpCtx.setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
CLSS public abstract org.openide.util.Lookup
cons public Lookup()
fld  public static final org.openide.util.Lookup org.openide.util.Lookup.EMPTY
innr public static abstract interface org.openide.util.Lookup$Provider
innr public static abstract org.openide.util.Lookup$Item
innr public static abstract org.openide.util.Lookup$Result
innr public static final org.openide.util.Lookup$Template
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Object org.openide.util.Lookup.lookup(java.lang.Class)
meth public abstract org.openide.util.Lookup$Result org.openide.util.Lookup.lookup(org.openide.util.Lookup$Template)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Collection org.openide.util.Lookup.lookupAll(java.lang.Class)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.Lookup$Item org.openide.util.Lookup.lookupItem(org.openide.util.Lookup$Template)
meth public org.openide.util.Lookup$Result org.openide.util.Lookup.lookupResult(java.lang.Class)
meth public static synchronized org.openide.util.Lookup org.openide.util.Lookup.getDefault()
supr java.lang.Object
CLSS public final org.openide.util.LookupEvent
cons public LookupEvent(org.openide.util.Lookup$Result)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.util.EventObject
CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void org.openide.util.LookupListener.resultChanged(org.openide.util.LookupEvent)
supr null
CLSS public org.openide.util.MapFormat
cons public MapFormat(java.util.Map)
intf java.io.Serializable
intf java.lang.Cloneable
meth protected java.lang.Object org.openide.util.MapFormat.processKey(java.lang.String)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.MapFormat.isExactMatch()
meth public boolean org.openide.util.MapFormat.willThrowExceptionIfKeyWasNotFound()
meth public final java.lang.String java.text.Format.format(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.text.Format.clone()
meth public java.lang.Object java.text.Format.parseObject(java.lang.String) throws java.text.ParseException
meth public java.lang.Object org.openide.util.MapFormat.parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.util.MapFormat.getLeftBrace()
meth public java.lang.String org.openide.util.MapFormat.getRightBrace()
meth public java.lang.String org.openide.util.MapFormat.parse(java.lang.String)
meth public java.lang.String org.openide.util.MapFormat.processPattern(java.lang.String) throws java.lang.IllegalArgumentException
meth public java.lang.StringBuffer org.openide.util.MapFormat.format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.text.AttributedCharacterIterator java.text.Format.formatToCharacterIterator(java.lang.Object)
meth public java.util.Map org.openide.util.MapFormat.getMap()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static java.lang.String org.openide.util.MapFormat.format(java.lang.String,java.util.Map)
meth public void org.openide.util.MapFormat.setExactMatch(boolean)
meth public void org.openide.util.MapFormat.setLeftBrace(java.lang.String)
meth public void org.openide.util.MapFormat.setMap(java.util.Map)
meth public void org.openide.util.MapFormat.setRightBrace(java.lang.String)
meth public void org.openide.util.MapFormat.setThrowExceptionIfKeyWasNotFound(boolean)
supr java.text.Format
CLSS public final org.openide.util.Mutex
cons public Mutex()
cons public Mutex(java.lang.Object)
cons public Mutex(org.openide.util.Mutex$Privileged)
fld  public static final org.openide.util.Mutex org.openide.util.Mutex.EVENT
innr public static abstract interface org.openide.util.Mutex$Action
innr public static abstract interface org.openide.util.Mutex$ExceptionAction
innr public static final org.openide.util.Mutex$Privileged
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.Mutex.isReadAccess()
meth public boolean org.openide.util.Mutex.isWriteAccess()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.util.Mutex.readAccess(org.openide.util.Mutex$Action)
meth public java.lang.Object org.openide.util.Mutex.readAccess(org.openide.util.Mutex$ExceptionAction) throws org.openide.util.MutexException
meth public java.lang.Object org.openide.util.Mutex.writeAccess(org.openide.util.Mutex$Action)
meth public java.lang.Object org.openide.util.Mutex.writeAccess(org.openide.util.Mutex$ExceptionAction) throws org.openide.util.MutexException
meth public java.lang.String org.openide.util.Mutex.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.util.Mutex.postReadRequest(java.lang.Runnable)
meth public void org.openide.util.Mutex.postWriteRequest(java.lang.Runnable)
meth public void org.openide.util.Mutex.readAccess(java.lang.Runnable)
meth public void org.openide.util.Mutex.writeAccess(java.lang.Runnable)
supr java.lang.Object
CLSS public org.openide.util.MutexException
cons public MutexException(java.lang.Exception)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Exception org.openide.util.MutexException.getException()
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable org.openide.util.MutexException.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.Exception
CLSS public org.openide.util.NbBundle
cons public NbBundle()
innr public static abstract interface org.openide.util.NbBundle$ClassLoaderFinder
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
meth public static final java.util.ResourceBundle org.openide.util.NbBundle.getBundle(java.lang.String) throws java.util.MissingResourceException
meth public static final java.util.ResourceBundle org.openide.util.NbBundle.getBundle(java.lang.String,java.util.Locale) throws java.util.MissingResourceException
meth public static final java.util.ResourceBundle org.openide.util.NbBundle.getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader) throws java.util.MissingResourceException
meth public static java.lang.Object org.openide.util.NbBundle.getLocalizedValue(java.util.Map,java.lang.String)
meth public static java.lang.Object org.openide.util.NbBundle.getLocalizedValue(java.util.Map,java.lang.String,java.util.Locale)
meth public static java.lang.String org.openide.util.NbBundle.getBranding()
meth public static java.lang.String org.openide.util.NbBundle.getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name)
meth public static java.lang.String org.openide.util.NbBundle.getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name,java.util.Locale)
meth public static java.lang.String org.openide.util.NbBundle.getMessage(java.lang.Class,java.lang.String) throws java.util.MissingResourceException
meth public static java.lang.String org.openide.util.NbBundle.getMessage(java.lang.Class,java.lang.String,[Ljava.lang.Object;) throws java.util.MissingResourceException
meth public static java.lang.String org.openide.util.NbBundle.getMessage(java.lang.Class,java.lang.String,java.lang.Object) throws java.util.MissingResourceException
meth public static java.lang.String org.openide.util.NbBundle.getMessage(java.lang.Class,java.lang.String,java.lang.Object,java.lang.Object) throws java.util.MissingResourceException
meth public static java.lang.String org.openide.util.NbBundle.getMessage(java.lang.Class,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object) throws java.util.MissingResourceException
meth public static java.util.Iterator org.openide.util.NbBundle.getLocalizingSuffixes()
meth public static java.util.ResourceBundle org.openide.util.NbBundle.getBundle(java.lang.Class) throws java.util.MissingResourceException
meth public static synchronized java.net.URL org.openide.util.NbBundle.getLocalizedFile(java.lang.String,java.lang.String) throws java.util.MissingResourceException
meth public static synchronized java.net.URL org.openide.util.NbBundle.getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale) throws java.util.MissingResourceException
meth public static synchronized java.net.URL org.openide.util.NbBundle.getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale,java.lang.ClassLoader) throws java.util.MissingResourceException
meth public static void org.openide.util.NbBundle.setBranding(java.lang.String) throws java.lang.IllegalArgumentException
meth public static void org.openide.util.NbBundle.setClassLoaderFinder(org.openide.util.NbBundle$ClassLoaderFinder)
supr java.lang.Object
CLSS public org.openide.util.NbCollections
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
meth public static java.lang.Iterable org.openide.util.NbCollections.iterable(java.util.Enumeration)
meth public static java.lang.Iterable org.openide.util.NbCollections.iterable(java.util.Iterator)
meth public static java.util.Enumeration org.openide.util.NbCollections.checkedEnumerationByFilter(java.util.Enumeration,java.lang.Class,boolean)
meth public static java.util.Iterator org.openide.util.NbCollections.checkedIteratorByFilter(java.util.Iterator,java.lang.Class,boolean)
meth public static java.util.List org.openide.util.NbCollections.checkedListByCopy(java.util.List,java.lang.Class,boolean) throws java.lang.ClassCastException
meth public static java.util.Map org.openide.util.NbCollections.checkedMapByCopy(java.util.Map,java.lang.Class,java.lang.Class,boolean) throws java.lang.ClassCastException
meth public static java.util.Map org.openide.util.NbCollections.checkedMapByFilter(java.util.Map,java.lang.Class,java.lang.Class,boolean)
meth public static java.util.Set org.openide.util.NbCollections.checkedSetByCopy(java.util.Set,java.lang.Class,boolean) throws java.lang.ClassCastException
meth public static java.util.Set org.openide.util.NbCollections.checkedSetByFilter(java.util.Set,java.lang.Class,boolean)
supr java.lang.Object
CLSS public final org.openide.util.NbPreferences
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
meth public static java.util.prefs.Preferences org.openide.util.NbPreferences.forModule(java.lang.Class)
meth public static java.util.prefs.Preferences org.openide.util.NbPreferences.root()
supr java.lang.Object
CLSS public org.openide.util.NotImplementedException
cons public NotImplementedException()
cons public NotImplementedException(java.lang.String)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.RuntimeException
CLSS public org.openide.util.Parameters
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
meth public static void org.openide.util.Parameters.javaIdentifier(java.lang.CharSequence,java.lang.CharSequence)
meth public static void org.openide.util.Parameters.javaIdentifierOrNull(java.lang.CharSequence,java.lang.CharSequence)
meth public static void org.openide.util.Parameters.notEmpty(java.lang.CharSequence,java.lang.CharSequence)
meth public static void org.openide.util.Parameters.notNull(java.lang.CharSequence,java.lang.Object)
meth public static void org.openide.util.Parameters.notWhitespace(java.lang.CharSequence,java.lang.CharSequence)
supr java.lang.Object
CLSS public org.openide.util.Queue
cons public Queue()
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
meth public synchronized java.lang.Object org.openide.util.Queue.get()
meth public synchronized void org.openide.util.Queue.put(java.lang.Object)
supr java.lang.Object
CLSS public final org.openide.util.RequestProcessor
cons public RequestProcessor()
cons public RequestProcessor(java.lang.String)
cons public RequestProcessor(java.lang.String,int)
cons public RequestProcessor(java.lang.String,int,boolean)
innr public final org.openide.util.RequestProcessor$Task
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.RequestProcessor.isRequestProcessorThread()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.create(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.create(java.lang.Runnable,boolean)
meth public org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.post(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.post(java.lang.Runnable,int)
meth public org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.post(java.lang.Runnable,int,int)
meth public static org.openide.util.RequestProcessor org.openide.util.RequestProcessor.getDefault()
meth public static org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.createRequest(java.lang.Runnable)
meth public static org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.postRequest(java.lang.Runnable)
meth public static org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.postRequest(java.lang.Runnable,int)
meth public static org.openide.util.RequestProcessor$Task org.openide.util.RequestProcessor.postRequest(java.lang.Runnable,int,int)
meth public void org.openide.util.RequestProcessor.stop()
supr java.lang.Object
CLSS public abstract org.openide.util.SharedClassObject
cons protected SharedClassObject()
intf java.io.Externalizable
intf java.io.Serializable
meth protected boolean org.openide.util.SharedClassObject.clearSharedData()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getLock()
meth protected final java.lang.Object org.openide.util.SharedClassObject.getProperty(java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object org.openide.util.SharedClassObject.putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void org.openide.util.SharedClassObject.finalize() throws java.lang.Throwable
meth protected java.lang.Object org.openide.util.SharedClassObject.writeReplace()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void org.openide.util.SharedClassObject.addNotify()
meth protected void org.openide.util.SharedClassObject.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void org.openide.util.SharedClassObject.initialize()
meth protected void org.openide.util.SharedClassObject.removeNotify()
meth protected void org.openide.util.SharedClassObject.reset()
meth public final boolean org.openide.util.SharedClassObject.equals(java.lang.Object)
meth public final int org.openide.util.SharedClassObject.hashCode()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.SharedClassObject.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.util.SharedClassObject.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
CLSS public org.openide.util.Task
cons protected Task()
cons public Task(java.lang.Runnable)
fld  public static final org.openide.util.Task org.openide.util.Task.EMPTY
intf java.lang.Runnable
meth protected final void org.openide.util.Task.notifyFinished()
meth protected final void org.openide.util.Task.notifyRunning()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.Task.waitFinished(long) throws java.lang.InterruptedException
meth public final boolean org.openide.util.Task.isFinished()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.Task.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized void org.openide.util.Task.addTaskListener(org.openide.util.TaskListener)
meth public synchronized void org.openide.util.Task.removeTaskListener(org.openide.util.TaskListener)
meth public void org.openide.util.Task.run()
meth public void org.openide.util.Task.waitFinished()
supr java.lang.Object
CLSS public abstract interface org.openide.util.TaskListener
intf java.util.EventListener
meth public abstract void org.openide.util.TaskListener.taskFinished(org.openide.util.Task)
supr null
CLSS public final org.openide.util.TopologicalSortException
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final [Ljava.util.Set; org.openide.util.TopologicalSortException.topologicalSets()
meth public final [Ljava.util.Set; org.openide.util.TopologicalSortException.unsortableSets()
meth public final java.util.List org.openide.util.TopologicalSortException.partialSort()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.TopologicalSortException.printStackTrace(java.io.PrintStream)
meth public final void org.openide.util.TopologicalSortException.printStackTrace(java.io.PrintWriter)
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String org.openide.util.TopologicalSortException.getMessage()
meth public java.lang.String org.openide.util.TopologicalSortException.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.lang.Exception
CLSS public abstract org.openide.util.Union2
intf java.io.Serializable
intf java.lang.Cloneable
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.openide.util.Union2.hasFirst()
meth public abstract boolean org.openide.util.Union2.hasSecond()
meth public abstract java.lang.Object org.openide.util.Union2.first() throws java.lang.IllegalArgumentException
meth public abstract java.lang.Object org.openide.util.Union2.second() throws java.lang.IllegalArgumentException
meth public abstract org.openide.util.Union2 org.openide.util.Union2.clone()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.util.Union2 org.openide.util.Union2.createFirst(java.lang.Object)
meth public static org.openide.util.Union2 org.openide.util.Union2.createSecond(java.lang.Object)
meth public volatile java.lang.Object org.openide.util.Union2.clone() throws java.lang.CloneNotSupportedException
supr java.lang.Object
CLSS public org.openide.util.UserCancelException
cons public UserCancelException()
cons public UserCancelException(java.lang.String)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.io.IOException
CLSS public abstract org.openide.util.UserQuestionException
cons public UserQuestionException()
cons public UserQuestionException(java.lang.String)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public abstract void org.openide.util.UserQuestionException.confirmed() throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.io.IOException
CLSS public final org.openide.util.Utilities
fld  constant public static final int org.openide.util.Utilities.OS_AIX
fld  constant public static final int org.openide.util.Utilities.OS_DEC
fld  constant public static final int org.openide.util.Utilities.OS_FREEBSD
fld  constant public static final int org.openide.util.Utilities.OS_HP
fld  constant public static final int org.openide.util.Utilities.OS_IRIX
fld  constant public static final int org.openide.util.Utilities.OS_LINUX
fld  constant public static final int org.openide.util.Utilities.OS_MAC
fld  constant public static final int org.openide.util.Utilities.OS_OS2
fld  constant public static final int org.openide.util.Utilities.OS_OTHER
fld  constant public static final int org.openide.util.Utilities.OS_SOLARIS
fld  constant public static final int org.openide.util.Utilities.OS_SUNOS
fld  constant public static final int org.openide.util.Utilities.OS_TRU64
fld  constant public static final int org.openide.util.Utilities.OS_UNIX_MASK
fld  constant public static final int org.openide.util.Utilities.OS_VMS
fld  constant public static final int org.openide.util.Utilities.OS_WIN2000
fld  constant public static final int org.openide.util.Utilities.OS_WIN95
fld  constant public static final int org.openide.util.Utilities.OS_WIN98
fld  constant public static final int org.openide.util.Utilities.OS_WINDOWS_MASK
fld  constant public static final int org.openide.util.Utilities.OS_WINNT
fld  constant public static final int org.openide.util.Utilities.OS_WIN_OTHER
fld  constant public static final int org.openide.util.Utilities.TYPICAL_WINDOWS_TASKBAR_HEIGHT
innr public static org.openide.util.Utilities$UnorderableException
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
meth public static [Ljava.lang.Object; org.openide.util.Utilities.toObjectArray(java.lang.Object)
meth public static [Ljava.lang.String; org.openide.util.Utilities.parseParameters(java.lang.String)
meth public static [Ljava.lang.String; org.openide.util.Utilities.wrapStringToArray(java.lang.String,int,boolean,boolean)
meth public static [Ljava.lang.String; org.openide.util.Utilities.wrapStringToArray(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static [Ljavax.swing.KeyStroke; org.openide.util.Utilities.stringToKeys(java.lang.String)
meth public static boolean org.openide.util.Utilities.compareObjects(java.lang.Object,java.lang.Object)
meth public static boolean org.openide.util.Utilities.compareObjectsImpl(java.lang.Object,java.lang.Object,int)
meth public static final boolean org.openide.util.Utilities.isJavaIdentifier(java.lang.String)
meth public static final boolean org.openide.util.Utilities.isLargeFrameIcons()
meth public static final boolean org.openide.util.Utilities.isMac()
meth public static final boolean org.openide.util.Utilities.isUnix()
meth public static final boolean org.openide.util.Utilities.isWindows()
meth public static final int org.openide.util.Utilities.getOperatingSystem()
meth public static final int org.openide.util.Utilities.showJFileChooser(javax.swing.JFileChooser,java.awt.Component,java.lang.String)
meth public static final java.awt.Cursor org.openide.util.Utilities.createProgressCursor(java.awt.Component)
meth public static final java.awt.Dimension org.openide.util.Utilities.getScreenSize()
meth public static final java.awt.Image org.openide.util.Utilities.icon2Image(javax.swing.Icon)
meth public static final java.awt.Image org.openide.util.Utilities.loadImage(java.lang.String)
meth public static final java.awt.Image org.openide.util.Utilities.loadImage(java.lang.String,boolean)
meth public static final java.awt.Image org.openide.util.Utilities.mergeImages(java.awt.Image,java.awt.Image,int,int)
meth public static final java.lang.String org.openide.util.Utilities.pureClassName(java.lang.String)
meth public static final void org.openide.util.Utilities.attachInitJob(java.awt.Component,org.openide.util.AsyncGUIJob)
meth public static int org.openide.util.Utilities.arrayHashCode([Ljava.lang.Object;)
meth public static java.awt.Component org.openide.util.Utilities.getFocusTraversableComponent(java.awt.Component)
meth public static java.awt.Cursor org.openide.util.Utilities.createCustomCursor(java.awt.Component,java.awt.Image,java.lang.String)
meth public static java.awt.Rectangle org.openide.util.Utilities.findCenterBounds(java.awt.Dimension)
meth public static java.awt.Rectangle org.openide.util.Utilities.getUsableScreenBounds()
meth public static java.awt.Rectangle org.openide.util.Utilities.getUsableScreenBounds(java.awt.GraphicsConfiguration)
meth public static java.beans.BeanInfo org.openide.util.Utilities.getBeanInfo(java.lang.Class) throws java.beans.IntrospectionException
meth public static java.beans.BeanInfo org.openide.util.Utilities.getBeanInfo(java.lang.Class,java.lang.Class) throws java.beans.IntrospectionException
meth public static java.io.File org.openide.util.Utilities.toFile(java.net.URL)
meth public static java.lang.Class org.openide.util.Utilities.getObjectType(java.lang.Class)
meth public static java.lang.Class org.openide.util.Utilities.getPrimitiveType(java.lang.Class)
meth public static java.lang.Object org.openide.util.Utilities.toPrimitiveArray([Ljava.lang.Object;)
meth public static java.lang.String org.openide.util.Utilities.escapeParameters([Ljava.lang.String;)
meth public static java.lang.String org.openide.util.Utilities.getClassName(java.lang.Class)
meth public static java.lang.String org.openide.util.Utilities.getShortClassName(java.lang.Class)
meth public static java.lang.String org.openide.util.Utilities.keyToString(javax.swing.KeyStroke)
meth public static java.lang.String org.openide.util.Utilities.replaceString(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String org.openide.util.Utilities.translate(java.lang.String)
meth public static java.lang.String org.openide.util.Utilities.wrapString(java.lang.String,int,boolean,boolean)
meth public static java.lang.String org.openide.util.Utilities.wrapString(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.net.URL org.openide.util.Utilities.toURL(java.io.File) throws java.net.MalformedURLException
meth public static java.util.List org.openide.util.Utilities.partialSort(java.util.List,java.util.Comparator,boolean) throws org.openide.util.Utilities$UnorderableException
meth public static java.util.List org.openide.util.Utilities.topologicalSort(java.util.Collection,java.util.Map) throws org.openide.util.TopologicalSortException
meth public static javax.swing.JPopupMenu org.openide.util.Utilities.actionsToPopup([Ljavax.swing.Action;,java.awt.Component)
meth public static javax.swing.JPopupMenu org.openide.util.Utilities.actionsToPopup([Ljavax.swing.Action;,org.openide.util.Lookup)
meth public static javax.swing.KeyStroke org.openide.util.Utilities.stringToKey(java.lang.String)
meth public static org.openide.util.Lookup org.openide.util.Utilities.actionsGlobalContext()
meth public static synchronized java.lang.ref.ReferenceQueue org.openide.util.Utilities.activeReferenceQueue()
supr java.lang.Object
CLSS public final org.openide.util.WeakListeners
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
meth public static java.beans.PropertyChangeListener org.openide.util.WeakListeners.propertyChange(java.beans.PropertyChangeListener,java.lang.Object)
meth public static java.beans.VetoableChangeListener org.openide.util.WeakListeners.vetoableChange(java.beans.VetoableChangeListener,java.lang.Object)
meth public static java.util.EventListener org.openide.util.WeakListeners.create(java.lang.Class,java.lang.Class,java.util.EventListener,java.lang.Object)
meth public static java.util.EventListener org.openide.util.WeakListeners.create(java.lang.Class,java.util.EventListener,java.lang.Object)
meth public static javax.swing.event.ChangeListener org.openide.util.WeakListeners.change(javax.swing.event.ChangeListener,java.lang.Object)
meth public static javax.swing.event.DocumentListener org.openide.util.WeakListeners.document(javax.swing.event.DocumentListener,java.lang.Object)
supr java.lang.Object
CLSS public org.openide.util.WeakSet
cons public WeakSet()
cons public WeakSet(int)
cons public WeakSet(int,float)
cons public WeakSet(java.util.Collection)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.lang.Iterable
intf java.util.Collection
intf java.util.Set
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.Object; org.openide.util.WeakSet.toArray()
meth public [Ljava.lang.Object; org.openide.util.WeakSet.toArray([Ljava.lang.Object;)
meth public boolean java.util.AbstractCollection.addAll(java.util.Collection)
meth public boolean java.util.AbstractCollection.containsAll(java.util.Collection)
meth public boolean java.util.AbstractCollection.retainAll(java.util.Collection)
meth public boolean java.util.AbstractSet.equals(java.lang.Object)
meth public boolean java.util.AbstractSet.removeAll(java.util.Collection)
meth public boolean org.openide.util.WeakSet.add(java.lang.Object)
meth public boolean org.openide.util.WeakSet.contains(java.lang.Object)
meth public boolean org.openide.util.WeakSet.isEmpty()
meth public boolean org.openide.util.WeakSet.remove(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int java.util.AbstractSet.hashCode()
meth public int org.openide.util.WeakSet.size()
meth public java.lang.Object org.openide.util.WeakSet.clone()
meth public java.lang.String org.openide.util.WeakSet.toString()
meth public java.util.Iterator org.openide.util.WeakSet.iterator()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public void org.openide.util.WeakSet.clear()
supr java.util.AbstractSet
CLSS public abstract interface org.openide.util.actions.ActionPerformer
meth public abstract void org.openide.util.actions.ActionPerformer.performAction(org.openide.util.actions.SystemAction)
supr null
CLSS public abstract org.openide.util.actions.BooleanStateAction
cons public BooleanStateAction()
fld  constant public static final java.lang.String org.openide.util.actions.BooleanStateAction.PROP_BOOLEAN_STATE
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
meth protected void org.openide.util.actions.BooleanStateAction.initialize()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public boolean org.openide.util.actions.BooleanStateAction.getBooleanState()
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
meth public java.awt.Component org.openide.util.actions.BooleanStateAction.getToolbarPresenter()
meth public java.lang.String java.lang.Object.toString()
meth public javax.swing.JMenuItem org.openide.util.actions.BooleanStateAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.BooleanStateAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.BooleanStateAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.BooleanStateAction.setBooleanState(boolean)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.SystemAction
CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public CallableSystemAction()
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
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public abstract void org.openide.util.actions.CallableSystemAction.performAction()
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
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.CallableSystemAction.actionPerformed(java.awt.event.ActionEvent)
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.actions.SystemAction
CLSS public abstract org.openide.util.actions.CallbackSystemAction
cons public CallbackSystemAction()
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
meth protected void org.openide.util.actions.CallbackSystemAction.initialize()
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
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
meth public javax.swing.Action org.openide.util.actions.CallbackSystemAction.createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getMenuPresenter()
meth public javax.swing.JMenuItem org.openide.util.actions.CallableSystemAction.getPopupPresenter()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
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
supr org.openide.util.actions.CallableSystemAction
CLSS public abstract interface org.openide.util.actions.Presenter
innr public static abstract interface org.openide.util.actions.Presenter$Menu
innr public static abstract interface org.openide.util.actions.Presenter$Popup
innr public static abstract interface org.openide.util.actions.Presenter$Toolbar
supr null
CLSS public abstract org.openide.util.actions.SystemAction
cons public SystemAction()
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
intf java.awt.event.ActionListener
intf java.io.Externalizable
intf java.io.Serializable
intf java.util.EventListener
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
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
meth public abstract java.lang.String org.openide.util.actions.SystemAction.getName()
meth public abstract org.openide.util.HelpCtx org.openide.util.actions.SystemAction.getHelpCtx()
meth public abstract void org.openide.util.actions.SystemAction.actionPerformed(java.awt.event.ActionEvent)
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
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static [Lorg.openide.util.actions.SystemAction; org.openide.util.actions.SystemAction.linkActions([Lorg.openide.util.actions.SystemAction;,[Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JPopupMenu org.openide.util.actions.SystemAction.createPopupMenu([Lorg.openide.util.actions.SystemAction;)
meth public static javax.swing.JToolBar org.openide.util.actions.SystemAction.createToolbarPresenter([Lorg.openide.util.actions.SystemAction;)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class)
meth public static org.openide.util.SharedClassObject org.openide.util.SharedClassObject.findObject(java.lang.Class,boolean)
meth public static org.openide.util.actions.SystemAction org.openide.util.actions.SystemAction.get(java.lang.Class)
meth public void org.openide.util.SharedClassObject.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void org.openide.util.SharedClassObject.writeExternal(java.io.ObjectOutput) throws java.io.IOException
meth public void org.openide.util.actions.SystemAction.setEnabled(boolean)
supr org.openide.util.SharedClassObject
CLSS public final org.openide.util.datatransfer.ClipboardEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.datatransfer.ClipboardEvent.isConsumed()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.datatransfer.ExClipboard org.openide.util.datatransfer.ClipboardEvent.getClipboard()
meth public void org.openide.util.datatransfer.ClipboardEvent.consume()
supr java.util.EventObject
CLSS public abstract interface org.openide.util.datatransfer.ClipboardListener
intf java.util.EventListener
meth public abstract void org.openide.util.datatransfer.ClipboardListener.clipboardChanged(org.openide.util.datatransfer.ClipboardEvent)
supr null
CLSS public abstract org.openide.util.datatransfer.ExClipboard
cons public ExClipboard(java.lang.String)
fld  protected java.awt.datatransfer.ClipboardOwner java.awt.datatransfer.Clipboard.owner
fld  protected java.awt.datatransfer.Transferable java.awt.datatransfer.Clipboard.contents
innr public static abstract interface org.openide.util.datatransfer.ExClipboard$Convertor
meth protected abstract [Lorg.openide.util.datatransfer.ExClipboard$Convertor; org.openide.util.datatransfer.ExClipboard.getConvertors()
meth protected final void org.openide.util.datatransfer.ExClipboard.fireClipboardChange()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.awt.datatransfer.DataFlavor; java.awt.datatransfer.Clipboard.getAvailableDataFlavors()
meth public boolean java.awt.datatransfer.Clipboard.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.datatransfer.ExClipboard.addClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public final void org.openide.util.datatransfer.ExClipboard.removeClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public java.awt.datatransfer.Transferable org.openide.util.datatransfer.ExClipboard.convert(java.awt.datatransfer.Transferable)
meth public java.lang.Object java.awt.datatransfer.Clipboard.getData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String java.awt.datatransfer.Clipboard.getName()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static void org.openide.util.datatransfer.ExClipboard.transferableAccepted(java.awt.datatransfer.Transferable,int)
meth public static void org.openide.util.datatransfer.ExClipboard.transferableOwnershipLost(java.awt.datatransfer.Transferable)
meth public static void org.openide.util.datatransfer.ExClipboard.transferableRejected(java.awt.datatransfer.Transferable)
meth public synchronized [Ljava.awt.datatransfer.FlavorListener; java.awt.datatransfer.Clipboard.getFlavorListeners()
meth public synchronized java.awt.datatransfer.Transferable java.awt.datatransfer.Clipboard.getContents(java.lang.Object)
meth public synchronized void java.awt.datatransfer.Clipboard.addFlavorListener(java.awt.datatransfer.FlavorListener)
meth public synchronized void java.awt.datatransfer.Clipboard.removeFlavorListener(java.awt.datatransfer.FlavorListener)
meth public synchronized void org.openide.util.datatransfer.ExClipboard.setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.awt.datatransfer.Clipboard
CLSS public org.openide.util.datatransfer.ExTransferable
fld  public static final java.awt.datatransfer.DataFlavor org.openide.util.datatransfer.ExTransferable.multiFlavor
fld  public static final java.awt.datatransfer.Transferable org.openide.util.datatransfer.ExTransferable.EMPTY
innr public static abstract org.openide.util.datatransfer.ExTransferable$Single
innr public static org.openide.util.datatransfer.ExTransferable$Multi
intf java.awt.datatransfer.Transferable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.awt.datatransfer.DataFlavor; org.openide.util.datatransfer.ExTransferable.getTransferDataFlavors()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.util.datatransfer.ExTransferable.isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.util.datatransfer.ExTransferable.getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.util.datatransfer.ExTransferable org.openide.util.datatransfer.ExTransferable.create(java.awt.datatransfer.Transferable)
meth public synchronized final void org.openide.util.datatransfer.ExTransferable.addTransferListener(org.openide.util.datatransfer.TransferListener)
meth public synchronized final void org.openide.util.datatransfer.ExTransferable.removeTransferListener(org.openide.util.datatransfer.TransferListener)
meth public void org.openide.util.datatransfer.ExTransferable.put(org.openide.util.datatransfer.ExTransferable$Single)
meth public void org.openide.util.datatransfer.ExTransferable.remove(java.awt.datatransfer.DataFlavor)
supr java.lang.Object
CLSS public abstract interface org.openide.util.datatransfer.MultiTransferObject
meth public abstract [Ljava.awt.datatransfer.DataFlavor; org.openide.util.datatransfer.MultiTransferObject.getTransferDataFlavors(int)
meth public abstract boolean org.openide.util.datatransfer.MultiTransferObject.areDataFlavorsSupported([Ljava.awt.datatransfer.DataFlavor;)
meth public abstract boolean org.openide.util.datatransfer.MultiTransferObject.isDataFlavorSupported(int,java.awt.datatransfer.DataFlavor)
meth public abstract int org.openide.util.datatransfer.MultiTransferObject.getCount()
meth public abstract java.awt.datatransfer.Transferable org.openide.util.datatransfer.MultiTransferObject.getTransferableAt(int)
meth public abstract java.lang.Object org.openide.util.datatransfer.MultiTransferObject.getTransferData(int,java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr null
CLSS public abstract org.openide.util.datatransfer.NewType
cons public NewType()
intf org.openide.util.HelpCtx$Provider
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.openide.util.datatransfer.NewType.create() throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.util.datatransfer.NewType.getName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.HelpCtx org.openide.util.datatransfer.NewType.getHelpCtx()
supr java.lang.Object
CLSS public abstract org.openide.util.datatransfer.PasteType
cons public PasteType()
intf org.openide.util.HelpCtx$Provider
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.awt.datatransfer.Transferable org.openide.util.datatransfer.PasteType.paste() throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.util.datatransfer.PasteType.getName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.HelpCtx org.openide.util.datatransfer.PasteType.getHelpCtx()
supr java.lang.Object
CLSS public abstract interface org.openide.util.datatransfer.TransferListener
intf java.util.EventListener
meth public abstract void org.openide.util.datatransfer.TransferListener.accepted(int)
meth public abstract void org.openide.util.datatransfer.TransferListener.ownershipLost()
meth public abstract void org.openide.util.datatransfer.TransferListener.rejected()
supr null
CLSS public org.openide.util.io.FoldingIOException
cons public FoldingIOException(java.lang.Throwable)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String org.openide.util.io.FoldingIOException.getLocalizedMessage()
meth public java.lang.String org.openide.util.io.FoldingIOException.toString()
meth public java.lang.Throwable java.lang.Throwable.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
meth public void org.openide.util.io.FoldingIOException.printStackTrace()
meth public void org.openide.util.io.FoldingIOException.printStackTrace(java.io.PrintStream)
meth public void org.openide.util.io.FoldingIOException.printStackTrace(java.io.PrintWriter)
supr java.io.IOException
CLSS public final org.openide.util.io.NbMarshalledObject
cons public NbMarshalledObject(java.lang.Object) throws java.io.IOException
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.openide.util.io.NbMarshalledObject.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.openide.util.io.NbMarshalledObject.hashCode()
meth public java.lang.Object org.openide.util.io.NbMarshalledObject.get() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public org.openide.util.io.NbObjectInputStream
cons public NbObjectInputStream(java.io.InputStream) throws java.io.IOException
fld  public static final byte java.io.ObjectStreamConstants.SC_BLOCK_DATA
fld  public static final byte java.io.ObjectStreamConstants.SC_ENUM
fld  public static final byte java.io.ObjectStreamConstants.SC_EXTERNALIZABLE
fld  public static final byte java.io.ObjectStreamConstants.SC_SERIALIZABLE
fld  public static final byte java.io.ObjectStreamConstants.SC_WRITE_METHOD
fld  public static final byte java.io.ObjectStreamConstants.TC_ARRAY
fld  public static final byte java.io.ObjectStreamConstants.TC_BASE
fld  public static final byte java.io.ObjectStreamConstants.TC_BLOCKDATA
fld  public static final byte java.io.ObjectStreamConstants.TC_BLOCKDATALONG
fld  public static final byte java.io.ObjectStreamConstants.TC_CLASS
fld  public static final byte java.io.ObjectStreamConstants.TC_CLASSDESC
fld  public static final byte java.io.ObjectStreamConstants.TC_ENDBLOCKDATA
fld  public static final byte java.io.ObjectStreamConstants.TC_ENUM
fld  public static final byte java.io.ObjectStreamConstants.TC_EXCEPTION
fld  public static final byte java.io.ObjectStreamConstants.TC_LONGSTRING
fld  public static final byte java.io.ObjectStreamConstants.TC_MAX
fld  public static final byte java.io.ObjectStreamConstants.TC_NULL
fld  public static final byte java.io.ObjectStreamConstants.TC_OBJECT
fld  public static final byte java.io.ObjectStreamConstants.TC_PROXYCLASSDESC
fld  public static final byte java.io.ObjectStreamConstants.TC_REFERENCE
fld  public static final byte java.io.ObjectStreamConstants.TC_RESET
fld  public static final byte java.io.ObjectStreamConstants.TC_STRING
fld  public static final int java.io.ObjectStreamConstants.PROTOCOL_VERSION_1
fld  public static final int java.io.ObjectStreamConstants.PROTOCOL_VERSION_2
fld  public static final int java.io.ObjectStreamConstants.baseWireHandle
fld  public static final java.io.SerializablePermission java.io.ObjectStreamConstants.SUBCLASS_IMPLEMENTATION_PERMISSION
fld  public static final java.io.SerializablePermission java.io.ObjectStreamConstants.SUBSTITUTION_PERMISSION
fld  public static final short java.io.ObjectStreamConstants.STREAM_MAGIC
fld  public static final short java.io.ObjectStreamConstants.STREAM_VERSION
intf java.io.Closeable
intf java.io.DataInput
intf java.io.ObjectInput
intf java.io.ObjectStreamConstants
meth protected boolean java.io.ObjectInputStream.enableResolveObject(boolean) throws java.lang.SecurityException
meth protected java.io.ObjectStreamClass org.openide.util.io.NbObjectInputStream.readClassDescriptor() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class java.io.ObjectInputStream.resolveProxyClass([Ljava.lang.String;) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class org.openide.util.io.NbObjectInputStream.resolveClass(java.io.ObjectStreamClass) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object java.io.ObjectInputStream.readObjectOverride() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object java.io.ObjectInputStream.resolveObject(java.lang.Object) throws java.io.IOException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.io.ObjectInputStream.readStreamHeader() throws java.io.IOException,java.io.StreamCorruptedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.io.InputStream.markSupported()
meth public boolean java.io.ObjectInputStream.readBoolean() throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public byte java.io.ObjectInputStream.readByte() throws java.io.IOException
meth public char java.io.ObjectInputStream.readChar() throws java.io.IOException
meth public double java.io.ObjectInputStream.readDouble() throws java.io.IOException
meth public final java.lang.Object java.io.ObjectInputStream.readObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public float java.io.ObjectInputStream.readFloat() throws java.io.IOException
meth public int java.io.InputStream.read([B) throws java.io.IOException
meth public int java.io.ObjectInputStream.available() throws java.io.IOException
meth public int java.io.ObjectInputStream.read() throws java.io.IOException
meth public int java.io.ObjectInputStream.read([B,int,int) throws java.io.IOException
meth public int java.io.ObjectInputStream.readInt() throws java.io.IOException
meth public int java.io.ObjectInputStream.readUnsignedByte() throws java.io.IOException
meth public int java.io.ObjectInputStream.readUnsignedShort() throws java.io.IOException
meth public int java.io.ObjectInputStream.skipBytes(int) throws java.io.IOException
meth public java.io.ObjectInputStream$GetField java.io.ObjectInputStream.readFields() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object java.io.ObjectInputStream.readUnshared() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String java.io.ObjectInputStream.readLine() throws java.io.IOException
meth public java.lang.String java.io.ObjectInputStream.readUTF() throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public long java.io.InputStream.skip(long) throws java.io.IOException
meth public long java.io.ObjectInputStream.readLong() throws java.io.IOException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public short java.io.ObjectInputStream.readShort() throws java.io.IOException
meth public static java.lang.Object org.openide.util.io.NbObjectInputStream.readSafely(java.io.ObjectInput) throws java.io.IOException
meth public static void org.openide.util.io.NbObjectInputStream.skipSafely(java.io.ObjectInput) throws java.io.IOException
meth public synchronized void java.io.InputStream.mark(int)
meth public synchronized void java.io.InputStream.reset() throws java.io.IOException
meth public void java.io.ObjectInputStream.close() throws java.io.IOException
meth public void java.io.ObjectInputStream.defaultReadObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public void java.io.ObjectInputStream.readFully([B) throws java.io.IOException
meth public void java.io.ObjectInputStream.readFully([B,int,int) throws java.io.IOException
meth public void java.io.ObjectInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.InvalidObjectException,java.io.NotActiveException
supr java.io.ObjectInputStream
CLSS public org.openide.util.io.NbObjectOutputStream
cons public NbObjectOutputStream(java.io.OutputStream) throws java.io.IOException
fld  public static final byte java.io.ObjectStreamConstants.SC_BLOCK_DATA
fld  public static final byte java.io.ObjectStreamConstants.SC_ENUM
fld  public static final byte java.io.ObjectStreamConstants.SC_EXTERNALIZABLE
fld  public static final byte java.io.ObjectStreamConstants.SC_SERIALIZABLE
fld  public static final byte java.io.ObjectStreamConstants.SC_WRITE_METHOD
fld  public static final byte java.io.ObjectStreamConstants.TC_ARRAY
fld  public static final byte java.io.ObjectStreamConstants.TC_BASE
fld  public static final byte java.io.ObjectStreamConstants.TC_BLOCKDATA
fld  public static final byte java.io.ObjectStreamConstants.TC_BLOCKDATALONG
fld  public static final byte java.io.ObjectStreamConstants.TC_CLASS
fld  public static final byte java.io.ObjectStreamConstants.TC_CLASSDESC
fld  public static final byte java.io.ObjectStreamConstants.TC_ENDBLOCKDATA
fld  public static final byte java.io.ObjectStreamConstants.TC_ENUM
fld  public static final byte java.io.ObjectStreamConstants.TC_EXCEPTION
fld  public static final byte java.io.ObjectStreamConstants.TC_LONGSTRING
fld  public static final byte java.io.ObjectStreamConstants.TC_MAX
fld  public static final byte java.io.ObjectStreamConstants.TC_NULL
fld  public static final byte java.io.ObjectStreamConstants.TC_OBJECT
fld  public static final byte java.io.ObjectStreamConstants.TC_PROXYCLASSDESC
fld  public static final byte java.io.ObjectStreamConstants.TC_REFERENCE
fld  public static final byte java.io.ObjectStreamConstants.TC_RESET
fld  public static final byte java.io.ObjectStreamConstants.TC_STRING
fld  public static final int java.io.ObjectStreamConstants.PROTOCOL_VERSION_1
fld  public static final int java.io.ObjectStreamConstants.PROTOCOL_VERSION_2
fld  public static final int java.io.ObjectStreamConstants.baseWireHandle
fld  public static final java.io.SerializablePermission java.io.ObjectStreamConstants.SUBCLASS_IMPLEMENTATION_PERMISSION
fld  public static final java.io.SerializablePermission java.io.ObjectStreamConstants.SUBSTITUTION_PERMISSION
fld  public static final short java.io.ObjectStreamConstants.STREAM_MAGIC
fld  public static final short java.io.ObjectStreamConstants.STREAM_VERSION
intf java.io.Closeable
intf java.io.DataOutput
intf java.io.Flushable
intf java.io.ObjectOutput
intf java.io.ObjectStreamConstants
meth protected boolean java.io.ObjectOutputStream.enableReplaceObject(boolean) throws java.lang.SecurityException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.io.ObjectOutputStream.annotateProxyClass(java.lang.Class) throws java.io.IOException
meth protected void java.io.ObjectOutputStream.drain() throws java.io.IOException
meth protected void java.io.ObjectOutputStream.writeClassDescriptor(java.io.ObjectStreamClass) throws java.io.IOException
meth protected void java.io.ObjectOutputStream.writeObjectOverride(java.lang.Object) throws java.io.IOException
meth protected void java.io.ObjectOutputStream.writeStreamHeader() throws java.io.IOException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.util.io.NbObjectOutputStream.annotateClass(java.lang.Class) throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.io.ObjectOutputStream.writeObject(java.lang.Object) throws java.io.IOException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.ObjectOutputStream$PutField java.io.ObjectOutputStream.putFields() throws java.io.IOException
meth public java.lang.Object org.openide.util.io.NbObjectOutputStream.replaceObject(java.lang.Object) throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static void org.openide.util.io.NbObjectOutputStream.writeSafely(java.io.ObjectOutput,java.lang.Object) throws java.io.IOException
meth public void java.io.ObjectOutputStream.close() throws java.io.IOException
meth public void java.io.ObjectOutputStream.defaultWriteObject() throws java.io.IOException
meth public void java.io.ObjectOutputStream.flush() throws java.io.IOException
meth public void java.io.ObjectOutputStream.reset() throws java.io.IOException
meth public void java.io.ObjectOutputStream.useProtocolVersion(int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.write([B) throws java.io.IOException
meth public void java.io.ObjectOutputStream.write([B,int,int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.write(int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeBoolean(boolean) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeByte(int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeBytes(java.lang.String) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeChar(int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeChars(java.lang.String) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeDouble(double) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeFields() throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeFloat(float) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeInt(int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeLong(long) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeShort(int) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeUTF(java.lang.String) throws java.io.IOException
meth public void java.io.ObjectOutputStream.writeUnshared(java.lang.Object) throws java.io.IOException
supr java.io.ObjectOutputStream
CLSS public org.openide.util.io.NullInputStream
cons public NullInputStream()
fld  public boolean org.openide.util.io.NullInputStream.throwException
intf java.io.Closeable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.io.InputStream.markSupported()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int java.io.InputStream.available() throws java.io.IOException
meth public int java.io.InputStream.read([B) throws java.io.IOException
meth public int java.io.InputStream.read([B,int,int) throws java.io.IOException
meth public int org.openide.util.io.NullInputStream.read() throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public long java.io.InputStream.skip(long) throws java.io.IOException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized void java.io.InputStream.mark(int)
meth public synchronized void java.io.InputStream.reset() throws java.io.IOException
meth public void java.io.InputStream.close() throws java.io.IOException
supr java.io.InputStream
CLSS public org.openide.util.io.NullOutputStream
cons public NullOutputStream()
fld  public boolean org.openide.util.io.NullOutputStream.throwException
intf java.io.Closeable
intf java.io.Flushable
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
meth public void java.io.OutputStream.close() throws java.io.IOException
meth public void java.io.OutputStream.flush() throws java.io.IOException
meth public void java.io.OutputStream.write([B) throws java.io.IOException
meth public void java.io.OutputStream.write([B,int,int) throws java.io.IOException
meth public void org.openide.util.io.NullOutputStream.write(int) throws java.io.IOException
supr java.io.OutputStream
CLSS public org.openide.util.io.OperationException
cons public OperationException(java.lang.Exception)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Exception org.openide.util.io.OperationException.getException()
meth public java.lang.String java.lang.Throwable.getLocalizedMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.String org.openide.util.io.OperationException.getMessage()
meth public java.lang.Throwable org.openide.util.io.OperationException.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.io.IOException
CLSS public org.openide.util.io.ReaderInputStream
cons public ReaderInputStream(java.io.Reader) throws java.io.IOException
cons public ReaderInputStream(java.io.Reader,java.lang.String) throws java.io.IOException
intf java.io.Closeable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.io.InputStream.markSupported()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int java.io.InputStream.read([B) throws java.io.IOException
meth public int org.openide.util.io.ReaderInputStream.available() throws java.io.IOException
meth public int org.openide.util.io.ReaderInputStream.read() throws java.io.IOException
meth public int org.openide.util.io.ReaderInputStream.read([B,int,int) throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public long java.io.InputStream.skip(long) throws java.io.IOException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized void java.io.InputStream.mark(int)
meth public synchronized void java.io.InputStream.reset() throws java.io.IOException
meth public void org.openide.util.io.ReaderInputStream.close() throws java.io.IOException
supr java.io.InputStream
CLSS public org.openide.util.io.SafeException
cons public SafeException(java.lang.Exception)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Exception org.openide.util.io.SafeException.getException()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String org.openide.util.io.FoldingIOException.getLocalizedMessage()
meth public java.lang.String org.openide.util.io.FoldingIOException.toString()
meth public java.lang.Throwable org.openide.util.io.SafeException.getCause()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
meth public void org.openide.util.io.FoldingIOException.printStackTrace()
meth public void org.openide.util.io.FoldingIOException.printStackTrace(java.io.PrintStream)
meth public void org.openide.util.io.FoldingIOException.printStackTrace(java.io.PrintWriter)
supr org.openide.util.io.FoldingIOException
CLSS public org.openide.util.lookup.AbstractLookup
cons protected AbstractLookup()
cons public AbstractLookup(org.openide.util.lookup.AbstractLookup$Content)
fld  public static final org.openide.util.Lookup org.openide.util.Lookup.EMPTY
innr public static abstract interface org.openide.util.Lookup$Provider
innr public static abstract org.openide.util.Lookup$Item
innr public static abstract org.openide.util.Lookup$Result
innr public static abstract org.openide.util.lookup.AbstractLookup$Pair
innr public static final org.openide.util.Lookup$Template
innr public static org.openide.util.lookup.AbstractLookup$Content
intf java.io.Serializable
meth protected final void org.openide.util.lookup.AbstractLookup.addPair(org.openide.util.lookup.AbstractLookup$Pair)
meth protected final void org.openide.util.lookup.AbstractLookup.removePair(org.openide.util.lookup.AbstractLookup$Pair)
meth protected final void org.openide.util.lookup.AbstractLookup.setPairs(java.util.Collection)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.util.lookup.AbstractLookup.beforeLookup(org.openide.util.Lookup$Template)
meth protected void org.openide.util.lookup.AbstractLookup.initialize()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.lang.Object org.openide.util.lookup.AbstractLookup.lookup(java.lang.Class)
meth public final org.openide.util.Lookup$Item org.openide.util.lookup.AbstractLookup.lookupItem(org.openide.util.Lookup$Template)
meth public final org.openide.util.Lookup$Result org.openide.util.lookup.AbstractLookup.lookup(org.openide.util.Lookup$Template)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.lookup.AbstractLookup.toString()
meth public java.util.Collection org.openide.util.Lookup.lookupAll(java.lang.Class)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.Lookup$Result org.openide.util.Lookup.lookupResult(java.lang.Class)
meth public static synchronized org.openide.util.Lookup org.openide.util.Lookup.getDefault()
supr org.openide.util.Lookup
CLSS public final org.openide.util.lookup.InstanceContent
cons public InstanceContent()
innr public static abstract interface org.openide.util.lookup.InstanceContent$Convertor
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.util.lookup.AbstractLookup$Content.addPair(org.openide.util.lookup.AbstractLookup$Pair)
meth public final void org.openide.util.lookup.AbstractLookup$Content.removePair(org.openide.util.lookup.AbstractLookup$Pair)
meth public final void org.openide.util.lookup.AbstractLookup$Content.setPairs(java.util.Collection)
meth public final void org.openide.util.lookup.InstanceContent.add(java.lang.Object)
meth public final void org.openide.util.lookup.InstanceContent.add(java.lang.Object,org.openide.util.lookup.InstanceContent$Convertor)
meth public final void org.openide.util.lookup.InstanceContent.remove(java.lang.Object)
meth public final void org.openide.util.lookup.InstanceContent.remove(java.lang.Object,org.openide.util.lookup.InstanceContent$Convertor)
meth public final void org.openide.util.lookup.InstanceContent.set(java.util.Collection,org.openide.util.lookup.InstanceContent$Convertor)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.openide.util.lookup.AbstractLookup$Content
CLSS public org.openide.util.lookup.Lookups
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
meth public static org.openide.util.Lookup org.openide.util.lookup.Lookups.fixed([Ljava.lang.Object;,org.openide.util.lookup.InstanceContent$Convertor)
meth public static org.openide.util.Lookup org.openide.util.lookup.Lookups.forPath(java.lang.String)
meth public static org.openide.util.Lookup org.openide.util.lookup.Lookups.metaInfServices(java.lang.ClassLoader)
meth public static org.openide.util.Lookup org.openide.util.lookup.Lookups.metaInfServices(java.lang.ClassLoader,java.lang.String)
meth public static org.openide.util.Lookup org.openide.util.lookup.Lookups.proxy(org.openide.util.Lookup$Provider)
meth public static org.openide.util.Lookup org.openide.util.lookup.Lookups.singleton(java.lang.Object)
meth public static org.openide.util.Lookup$Item org.openide.util.lookup.Lookups.lookupItem(java.lang.Object,java.lang.String)
meth public static transient org.openide.util.Lookup org.openide.util.lookup.Lookups.exclude(org.openide.util.Lookup,[Ljava.lang.Class;)
meth public static transient org.openide.util.Lookup org.openide.util.lookup.Lookups.fixed([Ljava.lang.Object;)
supr java.lang.Object
CLSS public org.openide.util.lookup.ProxyLookup
cons protected ProxyLookup()
cons public transient ProxyLookup([Lorg.openide.util.Lookup;)
fld  public static final org.openide.util.Lookup org.openide.util.Lookup.EMPTY
innr public static abstract interface org.openide.util.Lookup$Provider
innr public static abstract org.openide.util.Lookup$Item
innr public static abstract org.openide.util.Lookup$Result
innr public static final org.openide.util.Lookup$Template
meth protected final [Lorg.openide.util.Lookup; org.openide.util.lookup.ProxyLookup.getLookups()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected transient final void org.openide.util.lookup.ProxyLookup.setLookups([Lorg.openide.util.Lookup;)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.util.lookup.ProxyLookup.beforeLookup(org.openide.util.Lookup$Template)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.lang.Object org.openide.util.lookup.ProxyLookup.lookup(java.lang.Class)
meth public final org.openide.util.Lookup$Item org.openide.util.lookup.ProxyLookup.lookupItem(org.openide.util.Lookup$Template)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.openide.util.lookup.ProxyLookup.toString()
meth public java.util.Collection org.openide.util.Lookup.lookupAll(java.lang.Class)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.Lookup$Result org.openide.util.Lookup.lookupResult(java.lang.Class)
meth public static synchronized org.openide.util.Lookup org.openide.util.Lookup.getDefault()
meth public synchronized final org.openide.util.Lookup$Result org.openide.util.lookup.ProxyLookup.lookup(org.openide.util.Lookup$Template)
supr org.openide.util.Lookup
CLSS public abstract org.openide.xml.EntityCatalog
cons public EntityCatalog()
fld  constant public static final java.lang.String org.openide.xml.EntityCatalog.PUBLIC_ID
intf org.xml.sax.EntityResolver
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.xml.sax.InputSource org.xml.sax.EntityResolver.resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.xml.EntityCatalog org.openide.xml.EntityCatalog.getDefault()
supr java.lang.Object
CLSS public final org.openide.xml.XMLUtil
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
meth public static [B org.openide.xml.XMLUtil.fromHex([C,int,int) throws java.io.IOException
meth public static java.lang.String org.openide.xml.XMLUtil.toAttributeValue(java.lang.String) throws java.io.CharConversionException
meth public static java.lang.String org.openide.xml.XMLUtil.toElementContent(java.lang.String) throws java.io.CharConversionException
meth public static java.lang.String org.openide.xml.XMLUtil.toHex([B,int,int)
meth public static org.w3c.dom.Document org.openide.xml.XMLUtil.createDocument(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.w3c.dom.DOMException
meth public static org.w3c.dom.Document org.openide.xml.XMLUtil.parse(org.xml.sax.InputSource,boolean,boolean,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader org.openide.xml.XMLUtil.createXMLReader() throws org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader org.openide.xml.XMLUtil.createXMLReader(boolean) throws org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader org.openide.xml.XMLUtil.createXMLReader(boolean,boolean) throws org.xml.sax.SAXException
meth public static void org.openide.xml.XMLUtil.write(org.w3c.dom.Document,java.io.OutputStream,java.lang.String) throws java.io.IOException
supr java.lang.Object
