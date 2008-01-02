#API master signature file
#Version 3.27.1
CLSS public static abstract org.apache.tools.ant.module.AntSettings$IntrospectedInfoSerializer
cons public IntrospectedInfoSerializer()
fld  public static org.apache.tools.ant.module.AntSettings$IntrospectedInfoSerializer org.apache.tools.ant.module.AntSettings$IntrospectedInfoSerializer.instance
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.apache.tools.ant.module.api.IntrospectedInfo org.apache.tools.ant.module.AntSettings$IntrospectedInfoSerializer.load(java.util.prefs.Preferences)
meth public abstract void org.apache.tools.ant.module.AntSettings$IntrospectedInfoSerializer.store(java.util.prefs.Preferences,org.apache.tools.ant.module.api.IntrospectedInfo)
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
CLSS public static abstract interface org.apache.tools.ant.module.api.AntProjectCookie$ParseStatus
innr public static abstract interface org.apache.tools.ant.module.api.AntProjectCookie$ParseStatus
intf org.apache.tools.ant.module.api.AntProjectCookie
intf org.openide.nodes.Node$Cookie
meth public abstract boolean org.apache.tools.ant.module.api.AntProjectCookie$ParseStatus.isParsed()
meth public abstract java.io.File org.apache.tools.ant.module.api.AntProjectCookie.getFile()
meth public abstract java.lang.Throwable org.apache.tools.ant.module.api.AntProjectCookie.getParseException()
meth public abstract org.openide.filesystems.FileObject org.apache.tools.ant.module.api.AntProjectCookie.getFileObject()
meth public abstract org.w3c.dom.Document org.apache.tools.ant.module.api.AntProjectCookie.getDocument()
meth public abstract org.w3c.dom.Element org.apache.tools.ant.module.api.AntProjectCookie.getProjectElement()
meth public abstract void org.apache.tools.ant.module.api.AntProjectCookie.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.apache.tools.ant.module.api.AntProjectCookie.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public static final org.apache.tools.ant.module.api.AntTargetExecutor$Env
cons public Env()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.apache.tools.ant.module.api.AntTargetExecutor$Env.getVerbosity()
meth public java.io.OutputStream org.apache.tools.ant.module.api.AntTargetExecutor$Env.getLogger()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.util.Properties org.apache.tools.ant.module.api.AntTargetExecutor$Env.getProperties()
meth public synchronized void org.apache.tools.ant.module.api.AntTargetExecutor$Env.setProperties(java.util.Properties)
meth public void org.apache.tools.ant.module.api.AntTargetExecutor$Env.setLogger(java.io.OutputStream)
meth public void org.apache.tools.ant.module.api.AntTargetExecutor$Env.setVerbosity(int)
supr java.lang.Object
CLSS public static final org.apache.tools.ant.module.api.support.TargetLister$Target
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.apache.tools.ant.module.api.support.TargetLister$Target.isDefault()
meth public boolean org.apache.tools.ant.module.api.support.TargetLister$Target.isDescribed()
meth public boolean org.apache.tools.ant.module.api.support.TargetLister$Target.isInternal()
meth public boolean org.apache.tools.ant.module.api.support.TargetLister$Target.isOverridden()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.apache.tools.ant.module.api.support.TargetLister$Target.getName()
meth public java.lang.String org.apache.tools.ant.module.api.support.TargetLister$Target.getQualifiedName()
meth public java.lang.String org.apache.tools.ant.module.api.support.TargetLister$Target.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.apache.tools.ant.module.api.AntProjectCookie org.apache.tools.ant.module.api.support.TargetLister$Target.getScript()
meth public org.w3c.dom.Element org.apache.tools.ant.module.api.support.TargetLister$Target.getElement()
supr java.lang.Object
CLSS public static abstract interface org.apache.tools.ant.module.run.LoggerTrampoline$Creator
meth public abstract org.apache.tools.ant.module.spi.AntEvent org.apache.tools.ant.module.run.LoggerTrampoline$Creator.makeAntEvent(org.apache.tools.ant.module.run.LoggerTrampoline$AntEventImpl)
meth public abstract org.apache.tools.ant.module.spi.AntSession org.apache.tools.ant.module.run.LoggerTrampoline$Creator.makeAntSession(org.apache.tools.ant.module.run.LoggerTrampoline$AntSessionImpl)
meth public abstract org.apache.tools.ant.module.spi.TaskStructure org.apache.tools.ant.module.run.LoggerTrampoline$Creator.makeTaskStructure(org.apache.tools.ant.module.run.LoggerTrampoline$TaskStructureImpl)
supr null
CLSS public static abstract interface org.openide.nodes.Node$Cookie
supr null
CLSS public abstract interface org.apache.tools.ant.module.api.AntProjectCookie
innr public static abstract interface org.apache.tools.ant.module.api.AntProjectCookie$ParseStatus
intf org.openide.nodes.Node$Cookie
meth public abstract java.io.File org.apache.tools.ant.module.api.AntProjectCookie.getFile()
meth public abstract java.lang.Throwable org.apache.tools.ant.module.api.AntProjectCookie.getParseException()
meth public abstract org.openide.filesystems.FileObject org.apache.tools.ant.module.api.AntProjectCookie.getFileObject()
meth public abstract org.w3c.dom.Document org.apache.tools.ant.module.api.AntProjectCookie.getDocument()
meth public abstract org.w3c.dom.Element org.apache.tools.ant.module.api.AntProjectCookie.getProjectElement()
meth public abstract void org.apache.tools.ant.module.api.AntProjectCookie.addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void org.apache.tools.ant.module.api.AntProjectCookie.removeChangeListener(javax.swing.event.ChangeListener)
supr null
CLSS public final org.apache.tools.ant.module.api.AntTargetExecutor
innr public static final org.apache.tools.ant.module.api.AntTargetExecutor$Env
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
meth public org.openide.execution.ExecutorTask org.apache.tools.ant.module.api.AntTargetExecutor.execute(org.apache.tools.ant.module.api.AntProjectCookie,[Ljava.lang.String;) throws java.io.IOException
meth public static org.apache.tools.ant.module.api.AntTargetExecutor org.apache.tools.ant.module.api.AntTargetExecutor.createTargetExecutor(org.apache.tools.ant.module.api.AntTargetExecutor$Env)
supr java.lang.Object
CLSS public abstract interface org.apache.tools.ant.module.api.ElementCookie
intf org.openide.nodes.Node$Cookie
meth public abstract org.w3c.dom.Element org.apache.tools.ant.module.api.ElementCookie.getElement()
supr null
CLSS public final org.apache.tools.ant.module.api.IntrospectedInfo
cons public IntrospectedInfo()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.apache.tools.ant.module.api.IntrospectedInfo.getTags(java.lang.String) throws java.lang.IllegalArgumentException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.apache.tools.ant.module.api.IntrospectedInfo.isKnown(java.lang.String)
meth public boolean org.apache.tools.ant.module.api.IntrospectedInfo.supportsText(java.lang.String) throws java.lang.IllegalArgumentException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.apache.tools.ant.module.api.IntrospectedInfo.toString()
meth public java.util.Map org.apache.tools.ant.module.api.IntrospectedInfo.getAttributes(java.lang.String) throws java.lang.IllegalArgumentException
meth public java.util.Map org.apache.tools.ant.module.api.IntrospectedInfo.getDefs(java.lang.String)
meth public java.util.Map org.apache.tools.ant.module.api.IntrospectedInfo.getElements(java.lang.String) throws java.lang.IllegalArgumentException
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static synchronized org.apache.tools.ant.module.api.IntrospectedInfo org.apache.tools.ant.module.api.IntrospectedInfo.getDefaults()
meth public static synchronized org.apache.tools.ant.module.api.IntrospectedInfo org.apache.tools.ant.module.api.IntrospectedInfo.getKnownInfo()
meth public synchronized void org.apache.tools.ant.module.api.IntrospectedInfo.register(java.lang.String,java.lang.Class,java.lang.String)
meth public synchronized void org.apache.tools.ant.module.api.IntrospectedInfo.unregister(java.lang.String,java.lang.String)
meth public void org.apache.tools.ant.module.api.IntrospectedInfo.addChangeListener(javax.swing.event.ChangeListener)
meth public void org.apache.tools.ant.module.api.IntrospectedInfo.removeChangeListener(javax.swing.event.ChangeListener)
meth public void org.apache.tools.ant.module.api.IntrospectedInfo.scanProject(java.util.Map)
supr java.lang.Object
CLSS public abstract interface org.apache.tools.ant.module.api.IntrospectionCookie
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.String org.apache.tools.ant.module.api.IntrospectionCookie.getClassName()
supr null
CLSS public final org.apache.tools.ant.module.api.support.ActionUtils
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
meth public static [Lorg.openide.filesystems.FileObject; org.apache.tools.ant.module.api.support.ActionUtils.findSelectedFiles(org.openide.util.Lookup,org.openide.filesystems.FileObject,java.lang.String,boolean)
meth public static [Lorg.openide.filesystems.FileObject; org.apache.tools.ant.module.api.support.ActionUtils.regexpMapFiles([Lorg.openide.filesystems.FileObject;,org.openide.filesystems.FileObject,java.util.regex.Pattern,org.openide.filesystems.FileObject,java.lang.String,boolean) throws java.lang.IllegalArgumentException
meth public static java.lang.String org.apache.tools.ant.module.api.support.ActionUtils.antIncludesList([Lorg.openide.filesystems.FileObject;,org.openide.filesystems.FileObject) throws java.lang.IllegalArgumentException
meth public static java.lang.String org.apache.tools.ant.module.api.support.ActionUtils.antIncludesList([Lorg.openide.filesystems.FileObject;,org.openide.filesystems.FileObject,boolean) throws java.lang.IllegalArgumentException
meth public static org.openide.execution.ExecutorTask org.apache.tools.ant.module.api.support.ActionUtils.runTarget(org.openide.filesystems.FileObject,[Ljava.lang.String;,java.util.Properties) throws java.io.IOException,java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public org.apache.tools.ant.module.api.support.TargetLister
innr public static final org.apache.tools.ant.module.api.support.TargetLister$Target
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
meth public static java.util.Set org.apache.tools.ant.module.api.support.TargetLister.getTargets(org.apache.tools.ant.module.api.AntProjectCookie) throws java.io.IOException
supr java.lang.Object
CLSS public final org.apache.tools.ant.module.spi.AntEvent
fld  constant public static final int org.apache.tools.ant.module.spi.AntEvent.LOG_DEBUG
fld  constant public static final int org.apache.tools.ant.module.spi.AntEvent.LOG_ERR
fld  constant public static final int org.apache.tools.ant.module.spi.AntEvent.LOG_INFO
fld  constant public static final int org.apache.tools.ant.module.spi.AntEvent.LOG_VERBOSE
fld  constant public static final int org.apache.tools.ant.module.spi.AntEvent.LOG_WARN
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.apache.tools.ant.module.spi.AntEvent.isConsumed()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.apache.tools.ant.module.spi.AntEvent.getLine()
meth public int org.apache.tools.ant.module.spi.AntEvent.getLogLevel()
meth public java.io.File org.apache.tools.ant.module.spi.AntEvent.getScriptLocation()
meth public java.lang.String org.apache.tools.ant.module.spi.AntEvent.evaluate(java.lang.String)
meth public java.lang.String org.apache.tools.ant.module.spi.AntEvent.getMessage()
meth public java.lang.String org.apache.tools.ant.module.spi.AntEvent.getProperty(java.lang.String)
meth public java.lang.String org.apache.tools.ant.module.spi.AntEvent.getTargetName()
meth public java.lang.String org.apache.tools.ant.module.spi.AntEvent.getTaskName()
meth public java.lang.String org.apache.tools.ant.module.spi.AntEvent.toString()
meth public java.lang.Throwable org.apache.tools.ant.module.spi.AntEvent.getException()
meth public java.util.Set org.apache.tools.ant.module.spi.AntEvent.getPropertyNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.apache.tools.ant.module.spi.AntSession org.apache.tools.ant.module.spi.AntEvent.getSession()
meth public org.apache.tools.ant.module.spi.TaskStructure org.apache.tools.ant.module.spi.AntEvent.getTaskStructure()
meth public void org.apache.tools.ant.module.spi.AntEvent.consume() throws java.lang.IllegalStateException
supr java.lang.Object
CLSS public abstract org.apache.tools.ant.module.spi.AntLogger
cons protected AntLogger()
fld  public static final [Ljava.lang.String; org.apache.tools.ant.module.spi.AntLogger.ALL_TARGETS
fld  public static final [Ljava.lang.String; org.apache.tools.ant.module.spi.AntLogger.ALL_TASKS
fld  public static final [Ljava.lang.String; org.apache.tools.ant.module.spi.AntLogger.NO_TARGETS
fld  public static final [Ljava.lang.String; org.apache.tools.ant.module.spi.AntLogger.NO_TASKS
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [I org.apache.tools.ant.module.spi.AntLogger.interestedInLogLevels(org.apache.tools.ant.module.spi.AntSession)
meth public [Ljava.lang.String; org.apache.tools.ant.module.spi.AntLogger.interestedInTargets(org.apache.tools.ant.module.spi.AntSession)
meth public [Ljava.lang.String; org.apache.tools.ant.module.spi.AntLogger.interestedInTasks(org.apache.tools.ant.module.spi.AntSession)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.apache.tools.ant.module.spi.AntLogger.interestedInAllScripts(org.apache.tools.ant.module.spi.AntSession)
meth public boolean org.apache.tools.ant.module.spi.AntLogger.interestedInScript(java.io.File,org.apache.tools.ant.module.spi.AntSession)
meth public boolean org.apache.tools.ant.module.spi.AntLogger.interestedInSession(org.apache.tools.ant.module.spi.AntSession)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.apache.tools.ant.module.spi.AntLogger.buildFinished(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.buildInitializationFailed(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.buildStarted(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.messageLogged(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.targetFinished(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.targetStarted(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.taskFinished(org.apache.tools.ant.module.spi.AntEvent)
meth public void org.apache.tools.ant.module.spi.AntLogger.taskStarted(org.apache.tools.ant.module.spi.AntEvent)
supr java.lang.Object
CLSS public abstract org.apache.tools.ant.module.spi.AntOutputStream
cons public AntOutputStream()
intf java.io.Closeable
intf java.io.Flushable
meth protected abstract void org.apache.tools.ant.module.spi.AntOutputStream.writeLine(java.lang.String) throws java.io.IOException
meth protected boolean org.apache.tools.ant.module.spi.AntOutputStream.writeLine(java.lang.String,java.net.URL,int,int,int,int,java.lang.String) throws java.io.IOException
meth protected java.lang.String org.apache.tools.ant.module.spi.AntOutputStream.formatMessage(java.lang.String,java.lang.String,int,int,int,int)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.apache.tools.ant.module.spi.AntOutputStream.handleClose() throws java.io.IOException
meth protected void org.apache.tools.ant.module.spi.AntOutputStream.writeLine(java.lang.String,org.openide.filesystems.FileObject,int,int,int,int,java.lang.String) throws java.io.IOException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.apache.tools.ant.module.spi.AntOutputStream.close() throws java.io.IOException
meth public final void org.apache.tools.ant.module.spi.AntOutputStream.flush() throws java.io.IOException
meth public final void org.apache.tools.ant.module.spi.AntOutputStream.write([B) throws java.io.IOException
meth public final void org.apache.tools.ant.module.spi.AntOutputStream.write([B,int,int) throws java.io.IOException
meth public final void org.apache.tools.ant.module.spi.AntOutputStream.write(int) throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.io.OutputStream
CLSS public final org.apache.tools.ant.module.spi.AntSession
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.apache.tools.ant.module.spi.AntSession.getOriginatingTargets()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.apache.tools.ant.module.spi.AntSession.isExceptionConsumed(java.lang.Throwable)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.apache.tools.ant.module.spi.AntSession.getVerbosity()
meth public java.io.File org.apache.tools.ant.module.spi.AntSession.getOriginatingScript()
meth public java.lang.Object org.apache.tools.ant.module.spi.AntSession.getCustomData(org.apache.tools.ant.module.spi.AntLogger)
meth public java.lang.String org.apache.tools.ant.module.spi.AntSession.getDisplayName()
meth public java.lang.String org.apache.tools.ant.module.spi.AntSession.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.windows.OutputListener org.apache.tools.ant.module.spi.AntSession.createStandardHyperlink(java.net.URL,java.lang.String,int,int,int,int)
meth public void org.apache.tools.ant.module.spi.AntSession.consumeException(java.lang.Throwable) throws java.lang.IllegalStateException
meth public void org.apache.tools.ant.module.spi.AntSession.deliverMessageLogged(org.apache.tools.ant.module.spi.AntEvent,java.lang.String,int)
meth public void org.apache.tools.ant.module.spi.AntSession.println(java.lang.String,boolean,org.openide.windows.OutputListener)
meth public void org.apache.tools.ant.module.spi.AntSession.putCustomData(org.apache.tools.ant.module.spi.AntLogger,java.lang.Object)
supr java.lang.Object
CLSS public abstract interface org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider
meth public abstract [Ljava.io.File; org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider.getClasspathItems()
supr null
CLSS public final org.apache.tools.ant.module.spi.TaskStructure
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.apache.tools.ant.module.spi.TaskStructure; org.apache.tools.ant.module.spi.TaskStructure.getChildren()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.apache.tools.ant.module.spi.TaskStructure.getAttribute(java.lang.String)
meth public java.lang.String org.apache.tools.ant.module.spi.TaskStructure.getName()
meth public java.lang.String org.apache.tools.ant.module.spi.TaskStructure.getText()
meth public java.lang.String org.apache.tools.ant.module.spi.TaskStructure.toString()
meth public java.util.Set org.apache.tools.ant.module.spi.TaskStructure.getAttributeNames()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
