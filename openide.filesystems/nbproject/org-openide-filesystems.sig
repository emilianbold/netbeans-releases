#API master signature file
#Version 7.3.1
CLSS public static abstract interface org.openide.filesystems.AbstractFileSystem$Attr
fld  constant public static final long org.openide.filesystems.AbstractFileSystem$Attr.serialVersionUID
intf java.io.Serializable
meth public abstract java.lang.Object org.openide.filesystems.AbstractFileSystem$Attr.readAttribute(java.lang.String,java.lang.String)
meth public abstract java.util.Enumeration org.openide.filesystems.AbstractFileSystem$Attr.attributes(java.lang.String)
meth public abstract void org.openide.filesystems.AbstractFileSystem$Attr.deleteAttributes(java.lang.String)
meth public abstract void org.openide.filesystems.AbstractFileSystem$Attr.renameAttributes(java.lang.String,java.lang.String)
meth public abstract void org.openide.filesystems.AbstractFileSystem$Attr.writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr null
CLSS public static abstract interface org.openide.filesystems.AbstractFileSystem$Change
fld  constant public static final long org.openide.filesystems.AbstractFileSystem$Change.serialVersionUID
intf java.io.Serializable
meth public abstract void org.openide.filesystems.AbstractFileSystem$Change.createData(java.lang.String) throws java.io.IOException
meth public abstract void org.openide.filesystems.AbstractFileSystem$Change.createFolder(java.lang.String) throws java.io.IOException
meth public abstract void org.openide.filesystems.AbstractFileSystem$Change.delete(java.lang.String) throws java.io.IOException
meth public abstract void org.openide.filesystems.AbstractFileSystem$Change.rename(java.lang.String,java.lang.String) throws java.io.IOException
supr null
CLSS public static abstract interface org.openide.filesystems.AbstractFileSystem$Info
fld  constant public static final long org.openide.filesystems.AbstractFileSystem$Info.serialVersionUID
intf java.io.Serializable
meth public abstract boolean org.openide.filesystems.AbstractFileSystem$Info.folder(java.lang.String)
meth public abstract boolean org.openide.filesystems.AbstractFileSystem$Info.readOnly(java.lang.String)
meth public abstract java.io.InputStream org.openide.filesystems.AbstractFileSystem$Info.inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public abstract java.io.OutputStream org.openide.filesystems.AbstractFileSystem$Info.outputStream(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String org.openide.filesystems.AbstractFileSystem$Info.mimeType(java.lang.String)
meth public abstract java.util.Date org.openide.filesystems.AbstractFileSystem$Info.lastModified(java.lang.String)
meth public abstract long org.openide.filesystems.AbstractFileSystem$Info.size(java.lang.String)
meth public abstract void org.openide.filesystems.AbstractFileSystem$Info.lock(java.lang.String) throws java.io.IOException
meth public abstract void org.openide.filesystems.AbstractFileSystem$Info.markUnimportant(java.lang.String)
meth public abstract void org.openide.filesystems.AbstractFileSystem$Info.unlock(java.lang.String)
supr null
CLSS public static abstract interface org.openide.filesystems.AbstractFileSystem$List
fld  constant public static final long org.openide.filesystems.AbstractFileSystem$List.serialVersionUID
intf java.io.Serializable
meth public abstract [Ljava.lang.String; org.openide.filesystems.AbstractFileSystem$List.children(java.lang.String)
supr null
CLSS public static abstract interface org.openide.filesystems.AbstractFileSystem$Transfer
fld  constant public static final long org.openide.filesystems.AbstractFileSystem$Transfer.serialVersionUID
intf java.io.Serializable
meth public abstract boolean org.openide.filesystems.AbstractFileSystem$Transfer.copy(java.lang.String,org.openide.filesystems.AbstractFileSystem$Transfer,java.lang.String) throws java.io.IOException
meth public abstract boolean org.openide.filesystems.AbstractFileSystem$Transfer.move(java.lang.String,org.openide.filesystems.AbstractFileSystem$Transfer,java.lang.String) throws java.io.IOException
supr null
CLSS public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
meth public abstract void org.openide.filesystems.FileSystem$AtomicAction.run() throws java.io.IOException
supr null
CLSS public static abstract org.openide.filesystems.FileSystem$Environment
cons public Environment()
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
meth public void org.openide.filesystems.FileSystem$Environment.addClassPath(java.lang.String)
supr java.lang.Object
CLSS public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
intf org.openide.filesystems.FileSystem$Status
meth public abstract java.awt.Image org.openide.filesystems.FileSystem$Status.annotateIcon(java.awt.Image,int,java.util.Set)
meth public abstract java.lang.String org.openide.filesystems.FileSystem$HtmlStatus.annotateNameHtml(java.lang.String,java.util.Set)
meth public abstract java.lang.String org.openide.filesystems.FileSystem$Status.annotateName(java.lang.String,java.util.Set)
supr null
CLSS public static abstract interface org.openide.filesystems.FileSystem$Status
meth public abstract java.awt.Image org.openide.filesystems.FileSystem$Status.annotateIcon(java.awt.Image,int,java.util.Set)
meth public abstract java.lang.String org.openide.filesystems.FileSystem$Status.annotateName(java.lang.String,java.util.Set)
supr null
CLSS public static org.openide.filesystems.FileSystemCapability$Bean
cons public Bean()
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.ALL
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.COMPILE
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.DEBUG
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.DOC
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.EXECUTE
innr public static org.openide.filesystems.FileSystemCapability$Bean
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileSystemCapability$Bean.capableOf(org.openide.filesystems.FileSystemCapability)
meth public boolean org.openide.filesystems.FileSystemCapability$Bean.getCompile()
meth public boolean org.openide.filesystems.FileSystemCapability$Bean.getDebug()
meth public boolean org.openide.filesystems.FileSystemCapability$Bean.getDoc()
meth public boolean org.openide.filesystems.FileSystemCapability$Bean.getExecute()
meth public final java.util.Enumeration org.openide.filesystems.FileSystemCapability.findAll(java.lang.String,java.lang.String,java.lang.String)
meth public final org.openide.filesystems.FileObject org.openide.filesystems.FileSystemCapability.find(java.lang.String,java.lang.String,java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Enumeration org.openide.filesystems.FileSystemCapability.fileSystems()
meth public java.util.Enumeration org.openide.filesystems.FileSystemCapability.findAllResources(java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileSystemCapability.findResource(java.lang.String)
meth public synchronized void org.openide.filesystems.FileSystemCapability$Bean.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.filesystems.FileSystemCapability$Bean.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.filesystems.FileSystemCapability$Bean.setCompile(boolean)
meth public void org.openide.filesystems.FileSystemCapability$Bean.setDebug(boolean)
meth public void org.openide.filesystems.FileSystemCapability$Bean.setDoc(boolean)
meth public void org.openide.filesystems.FileSystemCapability$Bean.setExecute(boolean)
supr org.openide.filesystems.FileSystemCapability
CLSS public static org.openide.filesystems.JarFileSystem$Impl
cons public Impl(org.openide.filesystems.JarFileSystem)
intf java.io.Serializable
intf org.openide.filesystems.AbstractFileSystem$Attr
intf org.openide.filesystems.AbstractFileSystem$Change
intf org.openide.filesystems.AbstractFileSystem$Info
intf org.openide.filesystems.AbstractFileSystem$List
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.openide.filesystems.JarFileSystem$Impl.children(java.lang.String)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.JarFileSystem$Impl.folder(java.lang.String)
meth public boolean org.openide.filesystems.JarFileSystem$Impl.readOnly(java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.InputStream org.openide.filesystems.JarFileSystem$Impl.inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public java.io.OutputStream org.openide.filesystems.JarFileSystem$Impl.outputStream(java.lang.String) throws java.io.IOException
meth public java.lang.Object org.openide.filesystems.JarFileSystem$Impl.readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.filesystems.JarFileSystem$Impl.mimeType(java.lang.String)
meth public java.util.Date org.openide.filesystems.JarFileSystem$Impl.lastModified(java.lang.String)
meth public java.util.Enumeration org.openide.filesystems.JarFileSystem$Impl.attributes(java.lang.String)
meth public long org.openide.filesystems.JarFileSystem$Impl.size(java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.filesystems.JarFileSystem$Impl.createData(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.JarFileSystem$Impl.createFolder(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.JarFileSystem$Impl.delete(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.JarFileSystem$Impl.deleteAttributes(java.lang.String)
meth public void org.openide.filesystems.JarFileSystem$Impl.lock(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.JarFileSystem$Impl.markUnimportant(java.lang.String)
meth public void org.openide.filesystems.JarFileSystem$Impl.rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.JarFileSystem$Impl.renameAttributes(java.lang.String,java.lang.String)
meth public void org.openide.filesystems.JarFileSystem$Impl.unlock(java.lang.String)
meth public void org.openide.filesystems.JarFileSystem$Impl.writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr java.lang.Object
CLSS public static org.openide.filesystems.LocalFileSystem$Impl
cons public Impl(org.openide.filesystems.LocalFileSystem)
intf java.io.Serializable
intf org.openide.filesystems.AbstractFileSystem$Change
intf org.openide.filesystems.AbstractFileSystem$Info
intf org.openide.filesystems.AbstractFileSystem$List
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.openide.filesystems.LocalFileSystem$Impl.children(java.lang.String)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.LocalFileSystem$Impl.folder(java.lang.String)
meth public boolean org.openide.filesystems.LocalFileSystem$Impl.readOnly(java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.InputStream org.openide.filesystems.LocalFileSystem$Impl.inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public java.io.OutputStream org.openide.filesystems.LocalFileSystem$Impl.outputStream(java.lang.String) throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.filesystems.LocalFileSystem$Impl.mimeType(java.lang.String)
meth public java.util.Date org.openide.filesystems.LocalFileSystem$Impl.lastModified(java.lang.String)
meth public long org.openide.filesystems.LocalFileSystem$Impl.size(java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.filesystems.LocalFileSystem$Impl.createData(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.LocalFileSystem$Impl.createFolder(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.LocalFileSystem$Impl.delete(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.LocalFileSystem$Impl.lock(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.LocalFileSystem$Impl.markUnimportant(java.lang.String)
meth public void org.openide.filesystems.LocalFileSystem$Impl.rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.LocalFileSystem$Impl.unlock(java.lang.String)
supr java.lang.Object
CLSS public static org.openide.filesystems.XMLFileSystem$Impl
cons public Impl(org.openide.filesystems.XMLFileSystem)
intf java.io.Serializable
intf org.openide.filesystems.AbstractFileSystem$Attr
intf org.openide.filesystems.AbstractFileSystem$Change
intf org.openide.filesystems.AbstractFileSystem$Info
intf org.openide.filesystems.AbstractFileSystem$List
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.openide.filesystems.XMLFileSystem$Impl.children(java.lang.String)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.XMLFileSystem$Impl.folder(java.lang.String)
meth public boolean org.openide.filesystems.XMLFileSystem$Impl.readOnly(java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.InputStream org.openide.filesystems.XMLFileSystem$Impl.inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public java.io.OutputStream org.openide.filesystems.XMLFileSystem$Impl.outputStream(java.lang.String) throws java.io.IOException
meth public java.lang.Object org.openide.filesystems.XMLFileSystem$Impl.readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.openide.filesystems.XMLFileSystem$Impl.mimeType(java.lang.String)
meth public java.util.Date org.openide.filesystems.XMLFileSystem$Impl.lastModified(java.lang.String)
meth public java.util.Enumeration org.openide.filesystems.XMLFileSystem$Impl.attributes(java.lang.String)
meth public long org.openide.filesystems.XMLFileSystem$Impl.size(java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.filesystems.XMLFileSystem$Impl.createData(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.XMLFileSystem$Impl.createFolder(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.XMLFileSystem$Impl.delete(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.XMLFileSystem$Impl.deleteAttributes(java.lang.String)
meth public void org.openide.filesystems.XMLFileSystem$Impl.lock(java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.XMLFileSystem$Impl.markUnimportant(java.lang.String)
meth public void org.openide.filesystems.XMLFileSystem$Impl.rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.XMLFileSystem$Impl.renameAttributes(java.lang.String,java.lang.String)
meth public void org.openide.filesystems.XMLFileSystem$Impl.unlock(java.lang.String)
meth public void org.openide.filesystems.XMLFileSystem$Impl.writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr java.lang.Object
CLSS public static abstract interface org.openide.util.Enumerations$Processor
meth public abstract java.lang.Object org.openide.util.Enumerations$Processor.process(java.lang.Object,java.util.Collection)
supr null
CLSS public abstract org.openide.filesystems.AbstractFileSystem
cons public AbstractFileSystem()
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_HIDDEN
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_READ_ONLY
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_ROOT
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_SYSTEM_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_VALID
fld  protected org.openide.filesystems.AbstractFileSystem$Attr org.openide.filesystems.AbstractFileSystem.attr
fld  protected org.openide.filesystems.AbstractFileSystem$Change org.openide.filesystems.AbstractFileSystem.change
fld  protected org.openide.filesystems.AbstractFileSystem$Info org.openide.filesystems.AbstractFileSystem.info
fld  protected org.openide.filesystems.AbstractFileSystem$List org.openide.filesystems.AbstractFileSystem.list
fld  protected org.openide.filesystems.AbstractFileSystem$Transfer org.openide.filesystems.AbstractFileSystem.transfer
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Attr
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Change
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Info
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$List
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Transfer
innr public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
innr public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
innr public static abstract interface org.openide.filesystems.FileSystem$Status
innr public static abstract org.openide.filesystems.FileSystem$Environment
intf java.io.Serializable
meth protected boolean org.openide.filesystems.AbstractFileSystem.canRead(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.canWrite(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.checkVirtual(java.lang.String)
meth protected boolean org.openide.filesystems.FileSystem.isPersistent()
meth protected final int org.openide.filesystems.AbstractFileSystem.getRefreshTime()
meth protected final java.lang.ref.Reference org.openide.filesystems.AbstractFileSystem.findReference(java.lang.String)
meth protected final java.util.Enumeration org.openide.filesystems.AbstractFileSystem.existingFileObjects(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.refreshRoot()
meth protected final void org.openide.filesystems.AbstractFileSystem.refreshResource(java.lang.String,boolean)
meth protected final void org.openide.filesystems.FileSystem.fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void org.openide.filesystems.FileSystem.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.filesystems.FileSystem.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.FileSystem.setCapability(org.openide.filesystems.FileSystemCapability)
meth protected final void org.openide.filesystems.FileSystem.setSystemName(java.lang.String) throws java.beans.PropertyVetoException
meth protected java.lang.ref.Reference org.openide.filesystems.AbstractFileSystem.createReference(org.openide.filesystems.FileObject)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected synchronized final void org.openide.filesystems.AbstractFileSystem.setRefreshTime(int)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.filesystems.AbstractFileSystem.markImportant(java.lang.String,boolean)
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.AbstractFileSystem.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.FileSystem.getActions(java.util.Set)
meth public abstract boolean org.openide.filesystems.FileSystem.isReadOnly()
meth public abstract java.lang.String org.openide.filesystems.AbstractFileSystem.getDisplayName()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final boolean org.openide.filesystems.FileSystem.isDefault()
meth public final boolean org.openide.filesystems.FileSystem.isHidden()
meth public final boolean org.openide.filesystems.FileSystem.isValid()
meth public final java.lang.String org.openide.filesystems.FileSystem.getSystemName()
meth public final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystem.getCapability()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileSystem.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public final void org.openide.filesystems.FileSystem.setHidden(boolean)
meth public java.lang.String org.openide.filesystems.FileSystem.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.find(java.lang.String,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.findResource(java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.getRoot()
meth public org.openide.filesystems.FileSystem$Status org.openide.filesystems.FileSystem.getStatus()
meth public void org.openide.filesystems.AbstractFileSystem.refresh(boolean)
meth public void org.openide.filesystems.FileSystem.addNotify()
meth public void org.openide.filesystems.FileSystem.prepareEnvironment(org.openide.filesystems.FileSystem$Environment) throws org.openide.filesystems.EnvironmentNotSupportedException
meth public void org.openide.filesystems.FileSystem.removeNotify()
supr org.openide.filesystems.FileSystem
CLSS public org.openide.filesystems.DefaultAttributes
cons protected DefaultAttributes(org.openide.filesystems.AbstractFileSystem$Info,org.openide.filesystems.AbstractFileSystem$Change,org.openide.filesystems.AbstractFileSystem$List,java.lang.String)
cons public DefaultAttributes(org.openide.filesystems.AbstractFileSystem$Info,org.openide.filesystems.AbstractFileSystem$Change,org.openide.filesystems.AbstractFileSystem$List)
fld  constant public static final java.lang.String org.openide.filesystems.DefaultAttributes.ATTR_EXT
fld  constant public static final java.lang.String org.openide.filesystems.DefaultAttributes.ATTR_NAME
fld  constant public static final java.lang.String org.openide.filesystems.DefaultAttributes.ATTR_NAME_EXT
intf java.io.Serializable
intf org.openide.filesystems.AbstractFileSystem$Attr
intf org.openide.filesystems.AbstractFileSystem$List
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.String; org.openide.filesystems.DefaultAttributes.children(java.lang.String)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.openide.filesystems.DefaultAttributes.readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized java.util.Enumeration org.openide.filesystems.DefaultAttributes.attributes(java.lang.String)
meth public synchronized void org.openide.filesystems.DefaultAttributes.deleteAttributes(java.lang.String)
meth public synchronized void org.openide.filesystems.DefaultAttributes.renameAttributes(java.lang.String,java.lang.String)
meth public void org.openide.filesystems.DefaultAttributes.writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr java.lang.Object
CLSS public org.openide.filesystems.EnvironmentNotSupportedException
cons public EnvironmentNotSupportedException(org.openide.filesystems.FileSystem)
cons public EnvironmentNotSupportedException(org.openide.filesystems.FileSystem,java.lang.String)
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
meth public org.openide.filesystems.FileSystem org.openide.filesystems.EnvironmentNotSupportedException.getFileSystem()
meth public synchronized java.lang.Throwable java.lang.Throwable.initCause(java.lang.Throwable)
meth public synchronized native java.lang.Throwable java.lang.Throwable.fillInStackTrace()
meth public void java.lang.Throwable.printStackTrace()
meth public void java.lang.Throwable.printStackTrace(java.io.PrintStream)
meth public void java.lang.Throwable.printStackTrace(java.io.PrintWriter)
meth public void java.lang.Throwable.setStackTrace([Ljava.lang.StackTraceElement;)
supr java.io.IOException
CLSS public org.openide.filesystems.FileAlreadyLockedException
cons public FileAlreadyLockedException()
cons public FileAlreadyLockedException(java.lang.String)
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
CLSS public org.openide.filesystems.FileAttributeEvent
cons public FileAttributeEvent(org.openide.filesystems.FileObject,java.lang.String,java.lang.Object,java.lang.Object)
cons public FileAttributeEvent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.Object,java.lang.Object)
cons public FileAttributeEvent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.Object,java.lang.Object,boolean)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileEvent.firedFrom(org.openide.filesystems.FileSystem$AtomicAction)
meth public final boolean org.openide.filesystems.FileEvent.isExpected()
meth public final long org.openide.filesystems.FileEvent.getTime()
meth public final org.openide.filesystems.FileObject org.openide.filesystems.FileEvent.getFile()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.Object org.openide.filesystems.FileAttributeEvent.getNewValue()
meth public java.lang.Object org.openide.filesystems.FileAttributeEvent.getOldValue()
meth public java.lang.String org.openide.filesystems.FileAttributeEvent.getName()
meth public java.lang.String org.openide.filesystems.FileEvent.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.openide.filesystems.FileEvent
CLSS public org.openide.filesystems.FileChangeAdapter
cons public FileChangeAdapter()
intf java.util.EventListener
intf org.openide.filesystems.FileChangeListener
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
meth public void org.openide.filesystems.FileChangeAdapter.fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void org.openide.filesystems.FileChangeAdapter.fileChanged(org.openide.filesystems.FileEvent)
meth public void org.openide.filesystems.FileChangeAdapter.fileDataCreated(org.openide.filesystems.FileEvent)
meth public void org.openide.filesystems.FileChangeAdapter.fileDeleted(org.openide.filesystems.FileEvent)
meth public void org.openide.filesystems.FileChangeAdapter.fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void org.openide.filesystems.FileChangeAdapter.fileRenamed(org.openide.filesystems.FileRenameEvent)
supr java.lang.Object
CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void org.openide.filesystems.FileChangeListener.fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void org.openide.filesystems.FileChangeListener.fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void org.openide.filesystems.FileChangeListener.fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void org.openide.filesystems.FileChangeListener.fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void org.openide.filesystems.FileChangeListener.fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void org.openide.filesystems.FileChangeListener.fileRenamed(org.openide.filesystems.FileRenameEvent)
supr null
CLSS public org.openide.filesystems.FileEvent
cons public FileEvent(org.openide.filesystems.FileObject)
cons public FileEvent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
cons public FileEvent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,boolean)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileEvent.firedFrom(org.openide.filesystems.FileSystem$AtomicAction)
meth public final boolean org.openide.filesystems.FileEvent.isExpected()
meth public final long org.openide.filesystems.FileEvent.getTime()
meth public final org.openide.filesystems.FileObject org.openide.filesystems.FileEvent.getFile()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String org.openide.filesystems.FileEvent.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.util.EventObject
CLSS public org.openide.filesystems.FileLock
cons public FileLock()
fld  public static final org.openide.filesystems.FileLock org.openide.filesystems.FileLock.NONE
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileLock.isValid()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.openide.filesystems.FileLock.finalize()
meth public void org.openide.filesystems.FileLock.releaseLock()
supr java.lang.Object
CLSS public abstract org.openide.filesystems.FileObject
cons public FileObject()
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.filesystems.FileObject.fireFileAttributeChangedEvent(java.util.Enumeration,org.openide.filesystems.FileAttributeEvent)
meth protected void org.openide.filesystems.FileObject.fireFileChangedEvent(java.util.Enumeration,org.openide.filesystems.FileEvent)
meth protected void org.openide.filesystems.FileObject.fireFileDataCreatedEvent(java.util.Enumeration,org.openide.filesystems.FileEvent)
meth protected void org.openide.filesystems.FileObject.fireFileDeletedEvent(java.util.Enumeration,org.openide.filesystems.FileEvent)
meth protected void org.openide.filesystems.FileObject.fireFileFolderCreatedEvent(java.util.Enumeration,org.openide.filesystems.FileEvent)
meth protected void org.openide.filesystems.FileObject.fireFileRenamedEvent(java.util.Enumeration,org.openide.filesystems.FileRenameEvent)
meth public abstract [Lorg.openide.filesystems.FileObject; org.openide.filesystems.FileObject.getChildren()
meth public abstract boolean org.openide.filesystems.FileObject.isData()
meth public abstract boolean org.openide.filesystems.FileObject.isFolder()
meth public abstract boolean org.openide.filesystems.FileObject.isReadOnly()
meth public abstract boolean org.openide.filesystems.FileObject.isRoot()
meth public abstract boolean org.openide.filesystems.FileObject.isValid()
meth public abstract java.io.InputStream org.openide.filesystems.FileObject.getInputStream() throws java.io.FileNotFoundException
meth public abstract java.io.OutputStream org.openide.filesystems.FileObject.getOutputStream(org.openide.filesystems.FileLock) throws java.io.IOException
meth public abstract java.lang.Object org.openide.filesystems.FileObject.getAttribute(java.lang.String)
meth public abstract java.lang.String org.openide.filesystems.FileObject.getExt()
meth public abstract java.lang.String org.openide.filesystems.FileObject.getName()
meth public abstract java.util.Date org.openide.filesystems.FileObject.lastModified()
meth public abstract java.util.Enumeration org.openide.filesystems.FileObject.getAttributes()
meth public abstract long org.openide.filesystems.FileObject.getSize()
meth public abstract org.openide.filesystems.FileLock org.openide.filesystems.FileObject.lock() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject org.openide.filesystems.FileObject.createData(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject org.openide.filesystems.FileObject.createFolder(java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject org.openide.filesystems.FileObject.getFileObject(java.lang.String,java.lang.String)
meth public abstract org.openide.filesystems.FileObject org.openide.filesystems.FileObject.getParent()
meth public abstract org.openide.filesystems.FileSystem org.openide.filesystems.FileObject.getFileSystem() throws org.openide.filesystems.FileStateInvalidException
meth public abstract void org.openide.filesystems.FileObject.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public abstract void org.openide.filesystems.FileObject.delete(org.openide.filesystems.FileLock) throws java.io.IOException
meth public abstract void org.openide.filesystems.FileObject.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public abstract void org.openide.filesystems.FileObject.rename(org.openide.filesystems.FileLock,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void org.openide.filesystems.FileObject.setAttribute(java.lang.String,java.lang.Object) throws java.io.IOException
meth public abstract void org.openide.filesystems.FileObject.setImportant(boolean)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileObject.canRead()
meth public boolean org.openide.filesystems.FileObject.canWrite()
meth public boolean org.openide.filesystems.FileObject.existsExt(java.lang.String)
meth public boolean org.openide.filesystems.FileObject.isLocked()
meth public boolean org.openide.filesystems.FileObject.isVirtual()
meth public final boolean org.openide.filesystems.FileObject.hasExt(java.lang.String)
meth public final java.io.OutputStream org.openide.filesystems.FileObject.getOutputStream() throws java.io.IOException,org.openide.filesystems.FileAlreadyLockedException
meth public final java.net.URL org.openide.filesystems.FileObject.getURL() throws org.openide.filesystems.FileStateInvalidException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileObject.delete() throws java.io.IOException
meth public java.lang.String org.openide.filesystems.FileObject.getMIMEType()
meth public java.lang.String org.openide.filesystems.FileObject.getNameExt()
meth public java.lang.String org.openide.filesystems.FileObject.getPackageName(char)
meth public java.lang.String org.openide.filesystems.FileObject.getPackageNameExt(char,char)
meth public java.lang.String org.openide.filesystems.FileObject.getPath()
meth public java.lang.String org.openide.filesystems.FileObject.toString()
meth public java.util.Enumeration org.openide.filesystems.FileObject.getChildren(boolean)
meth public java.util.Enumeration org.openide.filesystems.FileObject.getData(boolean)
meth public java.util.Enumeration org.openide.filesystems.FileObject.getFolders(boolean)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileObject.copy(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileObject.createData(java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileObject.getFileObject(java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileObject.move(org.openide.filesystems.FileLock,org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public void org.openide.filesystems.FileObject.refresh()
meth public void org.openide.filesystems.FileObject.refresh(boolean)
supr java.lang.Object
CLSS public org.openide.filesystems.FileRenameEvent
cons public FileRenameEvent(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
cons public FileRenameEvent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
cons public FileRenameEvent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,boolean)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileEvent.firedFrom(org.openide.filesystems.FileSystem$AtomicAction)
meth public final boolean org.openide.filesystems.FileEvent.isExpected()
meth public final long org.openide.filesystems.FileEvent.getTime()
meth public final org.openide.filesystems.FileObject org.openide.filesystems.FileEvent.getFile()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String org.openide.filesystems.FileEvent.toString()
meth public java.lang.String org.openide.filesystems.FileRenameEvent.getExt()
meth public java.lang.String org.openide.filesystems.FileRenameEvent.getName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.openide.filesystems.FileEvent
CLSS public org.openide.filesystems.FileStateInvalidException
cons public FileStateInvalidException()
cons public FileStateInvalidException(java.lang.String)
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
meth public java.lang.String org.openide.filesystems.FileStateInvalidException.getFileSystemName()
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
CLSS public final org.openide.filesystems.FileStatusEvent
cons public FileStatusEvent(org.openide.filesystems.FileSystem,boolean,boolean)
cons public FileStatusEvent(org.openide.filesystems.FileSystem,java.util.Set,boolean,boolean)
cons public FileStatusEvent(org.openide.filesystems.FileSystem,org.openide.filesystems.FileObject,boolean,boolean)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileStatusEvent.hasChanged(org.openide.filesystems.FileObject)
meth public boolean org.openide.filesystems.FileStatusEvent.isIconChange()
meth public boolean org.openide.filesystems.FileStatusEvent.isNameChange()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileSystem org.openide.filesystems.FileStatusEvent.getFileSystem()
supr java.util.EventObject
CLSS public abstract interface org.openide.filesystems.FileStatusListener
intf java.util.EventListener
meth public abstract void org.openide.filesystems.FileStatusListener.annotationChanged(org.openide.filesystems.FileStatusEvent)
supr null
CLSS public abstract org.openide.filesystems.FileSystem
cons public FileSystem()
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_HIDDEN
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_READ_ONLY
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_ROOT
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_SYSTEM_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_VALID
innr public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
innr public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
innr public static abstract interface org.openide.filesystems.FileSystem$Status
innr public static abstract org.openide.filesystems.FileSystem$Environment
intf java.io.Serializable
meth protected boolean org.openide.filesystems.FileSystem.isPersistent()
meth protected final void org.openide.filesystems.FileSystem.fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void org.openide.filesystems.FileSystem.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.filesystems.FileSystem.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.FileSystem.setCapability(org.openide.filesystems.FileSystemCapability)
meth protected final void org.openide.filesystems.FileSystem.setSystemName(java.lang.String) throws java.beans.PropertyVetoException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.FileSystem.getActions(java.util.Set)
meth public abstract [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.FileSystem.getActions()
meth public abstract boolean org.openide.filesystems.FileSystem.isReadOnly()
meth public abstract java.lang.String org.openide.filesystems.FileSystem.getDisplayName()
meth public abstract org.openide.filesystems.FileObject org.openide.filesystems.FileSystem.findResource(java.lang.String)
meth public abstract org.openide.filesystems.FileObject org.openide.filesystems.FileSystem.getRoot()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final boolean org.openide.filesystems.FileSystem.isDefault()
meth public final boolean org.openide.filesystems.FileSystem.isHidden()
meth public final boolean org.openide.filesystems.FileSystem.isValid()
meth public final java.lang.String org.openide.filesystems.FileSystem.getSystemName()
meth public final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystem.getCapability()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileSystem.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public final void org.openide.filesystems.FileSystem.setHidden(boolean)
meth public java.lang.String org.openide.filesystems.FileSystem.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileSystem.find(java.lang.String,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileSystem$Status org.openide.filesystems.FileSystem.getStatus()
meth public void org.openide.filesystems.FileSystem.addNotify()
meth public void org.openide.filesystems.FileSystem.prepareEnvironment(org.openide.filesystems.FileSystem$Environment) throws org.openide.filesystems.EnvironmentNotSupportedException
meth public void org.openide.filesystems.FileSystem.refresh(boolean)
meth public void org.openide.filesystems.FileSystem.removeNotify()
supr java.lang.Object
CLSS public org.openide.filesystems.FileSystemCapability
cons public FileSystemCapability()
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.ALL
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.COMPILE
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.DEBUG
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.DOC
fld  public static final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystemCapability.EXECUTE
innr public static org.openide.filesystems.FileSystemCapability$Bean
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.FileSystemCapability.capableOf(org.openide.filesystems.FileSystemCapability)
meth public final java.util.Enumeration org.openide.filesystems.FileSystemCapability.findAll(java.lang.String,java.lang.String,java.lang.String)
meth public final org.openide.filesystems.FileObject org.openide.filesystems.FileSystemCapability.find(java.lang.String,java.lang.String,java.lang.String)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Enumeration org.openide.filesystems.FileSystemCapability.fileSystems()
meth public java.util.Enumeration org.openide.filesystems.FileSystemCapability.findAllResources(java.lang.String)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.FileSystemCapability.findResource(java.lang.String)
meth public synchronized void org.openide.filesystems.FileSystemCapability.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void org.openide.filesystems.FileSystemCapability.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
CLSS public final org.openide.filesystems.FileUtil
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
meth public static [Lorg.openide.filesystems.FileObject; org.openide.filesystems.FileUtil.fromFile(java.io.File)
meth public static boolean org.openide.filesystems.FileUtil.affectsOrder(org.openide.filesystems.FileAttributeEvent)
meth public static boolean org.openide.filesystems.FileUtil.isArchiveFile(java.net.URL)
meth public static boolean org.openide.filesystems.FileUtil.isArchiveFile(org.openide.filesystems.FileObject)
meth public static boolean org.openide.filesystems.FileUtil.isParentOf(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static java.io.File org.openide.filesystems.FileUtil.normalizeFile(java.io.File)
meth public static java.io.File org.openide.filesystems.FileUtil.toFile(org.openide.filesystems.FileObject)
meth public static java.lang.String org.openide.filesystems.FileUtil.findFreeFileName(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static java.lang.String org.openide.filesystems.FileUtil.findFreeFolderName(org.openide.filesystems.FileObject,java.lang.String)
meth public static java.lang.String org.openide.filesystems.FileUtil.getExtension(java.lang.String)
meth public static java.lang.String org.openide.filesystems.FileUtil.getFileDisplayName(org.openide.filesystems.FileObject)
meth public static java.lang.String org.openide.filesystems.FileUtil.getMIMEType(java.lang.String)
meth public static java.lang.String org.openide.filesystems.FileUtil.getMIMEType(org.openide.filesystems.FileObject)
meth public static java.lang.String org.openide.filesystems.FileUtil.getRelativePath(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static java.net.URL org.openide.filesystems.FileUtil.getArchiveFile(java.net.URL)
meth public static java.net.URL org.openide.filesystems.FileUtil.getArchiveRoot(java.net.URL)
meth public static java.net.URLStreamHandler org.openide.filesystems.FileUtil.nbfsURLStreamHandler()
meth public static java.util.List org.openide.filesystems.FileUtil.getOrder(java.util.Collection,boolean) throws java.lang.IllegalArgumentException
meth public static org.openide.filesystems.FileChangeListener org.openide.filesystems.FileUtil.weakFileChangeListener(org.openide.filesystems.FileChangeListener,java.lang.Object)
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.copyFile(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.copyFile(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.createData(java.io.File) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.createData(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.createFolder(java.io.File) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.createFolder(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.findBrother(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.getArchiveFile(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.getArchiveRoot(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.moveFile(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject org.openide.filesystems.FileUtil.toFileObject(java.io.File)
meth public static org.openide.filesystems.FileStatusListener org.openide.filesystems.FileUtil.weakFileStatusListener(org.openide.filesystems.FileStatusListener,java.lang.Object)
meth public static org.openide.filesystems.FileSystem org.openide.filesystems.FileUtil.createMemoryFileSystem()
meth public static void org.openide.filesystems.FileUtil.copy(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public static void org.openide.filesystems.FileUtil.copyAttributes(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void org.openide.filesystems.FileUtil.extractJar(org.openide.filesystems.FileObject,java.io.InputStream) throws java.io.IOException
meth public static void org.openide.filesystems.FileUtil.preventFileChooserSymlinkTraversal(javax.swing.JFileChooser,java.io.File)
meth public static void org.openide.filesystems.FileUtil.setMIMEType(java.lang.String,java.lang.String)
meth public static void org.openide.filesystems.FileUtil.setOrder(java.util.List) throws java.io.IOException,java.lang.IllegalArgumentException
supr java.lang.Object
CLSS public org.openide.filesystems.JarFileSystem
cons public JarFileSystem()
cons public JarFileSystem(org.openide.filesystems.FileSystemCapability)
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_HIDDEN
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_READ_ONLY
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_ROOT
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_SYSTEM_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_VALID
fld  protected org.openide.filesystems.AbstractFileSystem$Attr org.openide.filesystems.AbstractFileSystem.attr
fld  protected org.openide.filesystems.AbstractFileSystem$Change org.openide.filesystems.AbstractFileSystem.change
fld  protected org.openide.filesystems.AbstractFileSystem$Info org.openide.filesystems.AbstractFileSystem.info
fld  protected org.openide.filesystems.AbstractFileSystem$List org.openide.filesystems.AbstractFileSystem.list
fld  protected org.openide.filesystems.AbstractFileSystem$Transfer org.openide.filesystems.AbstractFileSystem.transfer
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Attr
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Change
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Info
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$List
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Transfer
innr public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
innr public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
innr public static abstract interface org.openide.filesystems.FileSystem$Status
innr public static abstract org.openide.filesystems.FileSystem$Environment
innr public static org.openide.filesystems.JarFileSystem$Impl
intf java.io.Serializable
meth protected [Ljava.lang.String; org.openide.filesystems.JarFileSystem.children(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.canRead(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.canWrite(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.checkVirtual(java.lang.String)
meth protected boolean org.openide.filesystems.FileSystem.isPersistent()
meth protected boolean org.openide.filesystems.JarFileSystem.folder(java.lang.String)
meth protected boolean org.openide.filesystems.JarFileSystem.readOnly(java.lang.String)
meth protected final int org.openide.filesystems.AbstractFileSystem.getRefreshTime()
meth protected final java.lang.ref.Reference org.openide.filesystems.AbstractFileSystem.findReference(java.lang.String)
meth protected final java.util.Enumeration org.openide.filesystems.AbstractFileSystem.existingFileObjects(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.refreshRoot()
meth protected final void org.openide.filesystems.AbstractFileSystem.refreshResource(java.lang.String,boolean)
meth protected final void org.openide.filesystems.FileSystem.fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void org.openide.filesystems.FileSystem.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.filesystems.FileSystem.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.FileSystem.setCapability(org.openide.filesystems.FileSystemCapability)
meth protected final void org.openide.filesystems.FileSystem.setSystemName(java.lang.String) throws java.beans.PropertyVetoException
meth protected java.io.InputStream org.openide.filesystems.JarFileSystem.inputStream(java.lang.String) throws java.io.FileNotFoundException
meth protected java.io.OutputStream org.openide.filesystems.JarFileSystem.outputStream(java.lang.String) throws java.io.IOException
meth protected java.lang.Object org.openide.filesystems.JarFileSystem.readAttribute(java.lang.String,java.lang.String)
meth protected java.lang.String org.openide.filesystems.JarFileSystem.mimeType(java.lang.String)
meth protected java.lang.ref.Reference org.openide.filesystems.JarFileSystem.createReference(org.openide.filesystems.FileObject)
meth protected java.util.Date org.openide.filesystems.JarFileSystem.lastModified(java.lang.String)
meth protected java.util.Enumeration org.openide.filesystems.JarFileSystem.attributes(java.lang.String)
meth protected long org.openide.filesystems.JarFileSystem.size(java.lang.String)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected synchronized final void org.openide.filesystems.AbstractFileSystem.setRefreshTime(int)
meth protected void org.openide.filesystems.AbstractFileSystem.markImportant(java.lang.String,boolean)
meth protected void org.openide.filesystems.JarFileSystem.createData(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.JarFileSystem.createFolder(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.JarFileSystem.delete(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.JarFileSystem.deleteAttributes(java.lang.String)
meth protected void org.openide.filesystems.JarFileSystem.finalize() throws java.lang.Throwable
meth protected void org.openide.filesystems.JarFileSystem.lock(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.JarFileSystem.markUnimportant(java.lang.String)
meth protected void org.openide.filesystems.JarFileSystem.rename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.JarFileSystem.renameAttributes(java.lang.String,java.lang.String)
meth protected void org.openide.filesystems.JarFileSystem.unlock(java.lang.String)
meth protected void org.openide.filesystems.JarFileSystem.writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.AbstractFileSystem.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.FileSystem.getActions(java.util.Set)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.JarFileSystem.isReadOnly()
meth public final boolean org.openide.filesystems.FileSystem.isDefault()
meth public final boolean org.openide.filesystems.FileSystem.isHidden()
meth public final boolean org.openide.filesystems.FileSystem.isValid()
meth public final java.lang.String org.openide.filesystems.FileSystem.getSystemName()
meth public final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystem.getCapability()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileSystem.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public final void org.openide.filesystems.FileSystem.setHidden(boolean)
meth public java.io.File org.openide.filesystems.JarFileSystem.getJarFile()
meth public java.lang.String org.openide.filesystems.FileSystem.toString()
meth public java.lang.String org.openide.filesystems.JarFileSystem.getDisplayName()
meth public java.util.jar.Manifest org.openide.filesystems.JarFileSystem.getManifest()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.find(java.lang.String,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.findResource(java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.getRoot()
meth public org.openide.filesystems.FileSystem$Status org.openide.filesystems.FileSystem.getStatus()
meth public void org.openide.filesystems.AbstractFileSystem.refresh(boolean)
meth public void org.openide.filesystems.FileSystem.addNotify()
meth public void org.openide.filesystems.JarFileSystem.prepareEnvironment(org.openide.filesystems.FileSystem$Environment)
meth public void org.openide.filesystems.JarFileSystem.removeNotify()
meth public void org.openide.filesystems.JarFileSystem.setJarFile(java.io.File) throws java.beans.PropertyVetoException,java.io.IOException
supr org.openide.filesystems.AbstractFileSystem
CLSS public org.openide.filesystems.LocalFileSystem
cons public LocalFileSystem()
cons public LocalFileSystem(org.openide.filesystems.FileSystemCapability)
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_HIDDEN
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_READ_ONLY
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_ROOT
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_SYSTEM_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_VALID
fld  protected org.openide.filesystems.AbstractFileSystem$Attr org.openide.filesystems.AbstractFileSystem.attr
fld  protected org.openide.filesystems.AbstractFileSystem$Change org.openide.filesystems.AbstractFileSystem.change
fld  protected org.openide.filesystems.AbstractFileSystem$Info org.openide.filesystems.AbstractFileSystem.info
fld  protected org.openide.filesystems.AbstractFileSystem$List org.openide.filesystems.AbstractFileSystem.list
fld  protected org.openide.filesystems.AbstractFileSystem$Transfer org.openide.filesystems.AbstractFileSystem.transfer
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Attr
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Change
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Info
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$List
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Transfer
innr public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
innr public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
innr public static abstract interface org.openide.filesystems.FileSystem$Status
innr public static abstract org.openide.filesystems.FileSystem$Environment
innr public static org.openide.filesystems.LocalFileSystem$Impl
intf java.io.Serializable
meth protected [Ljava.lang.String; org.openide.filesystems.LocalFileSystem.children(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.canRead(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.canWrite(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.checkVirtual(java.lang.String)
meth protected boolean org.openide.filesystems.FileSystem.isPersistent()
meth protected boolean org.openide.filesystems.LocalFileSystem.folder(java.lang.String)
meth protected boolean org.openide.filesystems.LocalFileSystem.readOnly(java.lang.String)
meth protected final int org.openide.filesystems.AbstractFileSystem.getRefreshTime()
meth protected final java.lang.ref.Reference org.openide.filesystems.AbstractFileSystem.findReference(java.lang.String)
meth protected final java.util.Enumeration org.openide.filesystems.AbstractFileSystem.existingFileObjects(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.refreshRoot()
meth protected final void org.openide.filesystems.AbstractFileSystem.refreshResource(java.lang.String,boolean)
meth protected final void org.openide.filesystems.FileSystem.fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void org.openide.filesystems.FileSystem.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.filesystems.FileSystem.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.FileSystem.setCapability(org.openide.filesystems.FileSystemCapability)
meth protected final void org.openide.filesystems.FileSystem.setSystemName(java.lang.String) throws java.beans.PropertyVetoException
meth protected java.io.InputStream org.openide.filesystems.LocalFileSystem.inputStream(java.lang.String) throws java.io.FileNotFoundException
meth protected java.io.OutputStream org.openide.filesystems.LocalFileSystem.outputStream(java.lang.String) throws java.io.IOException
meth protected java.lang.String org.openide.filesystems.LocalFileSystem.computeSystemName(java.io.File)
meth protected java.lang.String org.openide.filesystems.LocalFileSystem.mimeType(java.lang.String)
meth protected java.lang.ref.Reference org.openide.filesystems.AbstractFileSystem.createReference(org.openide.filesystems.FileObject)
meth protected java.util.Date org.openide.filesystems.LocalFileSystem.lastModified(java.lang.String)
meth protected long org.openide.filesystems.LocalFileSystem.size(java.lang.String)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected synchronized final void org.openide.filesystems.AbstractFileSystem.setRefreshTime(int)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.filesystems.AbstractFileSystem.markImportant(java.lang.String,boolean)
meth protected void org.openide.filesystems.LocalFileSystem.createData(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.LocalFileSystem.createFolder(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.LocalFileSystem.delete(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.LocalFileSystem.lock(java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.LocalFileSystem.markUnimportant(java.lang.String)
meth protected void org.openide.filesystems.LocalFileSystem.rename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void org.openide.filesystems.LocalFileSystem.unlock(java.lang.String)
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.AbstractFileSystem.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.FileSystem.getActions(java.util.Set)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.LocalFileSystem.isReadOnly()
meth public final boolean org.openide.filesystems.FileSystem.isDefault()
meth public final boolean org.openide.filesystems.FileSystem.isHidden()
meth public final boolean org.openide.filesystems.FileSystem.isValid()
meth public final java.lang.String org.openide.filesystems.FileSystem.getSystemName()
meth public final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystem.getCapability()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileSystem.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public final void org.openide.filesystems.FileSystem.setHidden(boolean)
meth public java.io.File org.openide.filesystems.LocalFileSystem.getRootDirectory()
meth public java.lang.String org.openide.filesystems.FileSystem.toString()
meth public java.lang.String org.openide.filesystems.LocalFileSystem.getDisplayName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.find(java.lang.String,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.findResource(java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.getRoot()
meth public org.openide.filesystems.FileSystem$Status org.openide.filesystems.FileSystem.getStatus()
meth public synchronized void org.openide.filesystems.LocalFileSystem.setRootDirectory(java.io.File) throws java.beans.PropertyVetoException,java.io.IOException
meth public void org.openide.filesystems.AbstractFileSystem.refresh(boolean)
meth public void org.openide.filesystems.FileSystem.addNotify()
meth public void org.openide.filesystems.FileSystem.removeNotify()
meth public void org.openide.filesystems.LocalFileSystem.prepareEnvironment(org.openide.filesystems.FileSystem$Environment)
meth public void org.openide.filesystems.LocalFileSystem.setReadOnly(boolean)
supr org.openide.filesystems.AbstractFileSystem
CLSS public abstract org.openide.filesystems.MIMEResolver
cons public MIMEResolver()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.openide.filesystems.MIMEResolver.findMIMEType(org.openide.filesystems.FileObject)
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
CLSS public org.openide.filesystems.MultiFileSystem
cons protected MultiFileSystem()
cons public MultiFileSystem([Lorg.openide.filesystems.FileSystem;)
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_HIDDEN
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_READ_ONLY
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_ROOT
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_SYSTEM_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_VALID
innr public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
innr public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
innr public static abstract interface org.openide.filesystems.FileSystem$Status
innr public static abstract org.openide.filesystems.FileSystem$Environment
intf java.io.Serializable
meth protected boolean org.openide.filesystems.FileSystem.isPersistent()
meth protected final [Lorg.openide.filesystems.FileSystem; org.openide.filesystems.MultiFileSystem.getDelegates()
meth protected final org.openide.filesystems.FileSystem org.openide.filesystems.MultiFileSystem.findSystem(org.openide.filesystems.FileObject) throws java.lang.IllegalArgumentException
meth protected final void org.openide.filesystems.FileSystem.fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void org.openide.filesystems.FileSystem.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.filesystems.FileSystem.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.FileSystem.setCapability(org.openide.filesystems.FileSystemCapability)
meth protected final void org.openide.filesystems.FileSystem.setSystemName(java.lang.String) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.MultiFileSystem.hideResource(java.lang.String,boolean) throws java.io.IOException
meth protected final void org.openide.filesystems.MultiFileSystem.setPropagateMasks(boolean)
meth protected java.util.Set org.openide.filesystems.MultiFileSystem.createLocksOn(java.lang.String) throws java.io.IOException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected org.openide.filesystems.FileObject org.openide.filesystems.MultiFileSystem.findResourceOn(org.openide.filesystems.FileSystem,java.lang.String)
meth protected org.openide.filesystems.FileSystem org.openide.filesystems.MultiFileSystem.createWritableOn(java.lang.String) throws java.io.IOException
meth protected org.openide.filesystems.FileSystem org.openide.filesystems.MultiFileSystem.createWritableOnForRename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected static java.util.Enumeration org.openide.filesystems.MultiFileSystem.hiddenFiles(org.openide.filesystems.FileObject,boolean)
meth protected transient final void org.openide.filesystems.MultiFileSystem.setDelegates([Lorg.openide.filesystems.FileSystem;)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.filesystems.MultiFileSystem.markUnimportant(org.openide.filesystems.FileObject)
meth protected void org.openide.filesystems.MultiFileSystem.notifyMigration(org.openide.filesystems.FileObject)
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.MultiFileSystem.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.MultiFileSystem.getActions(java.util.Set)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.MultiFileSystem.isReadOnly()
meth public final boolean org.openide.filesystems.FileSystem.isDefault()
meth public final boolean org.openide.filesystems.FileSystem.isHidden()
meth public final boolean org.openide.filesystems.FileSystem.isValid()
meth public final boolean org.openide.filesystems.MultiFileSystem.getPropagateMasks()
meth public final java.lang.String org.openide.filesystems.FileSystem.getSystemName()
meth public final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystem.getCapability()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileSystem.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public final void org.openide.filesystems.FileSystem.setHidden(boolean)
meth public java.lang.String org.openide.filesystems.FileSystem.toString()
meth public java.lang.String org.openide.filesystems.MultiFileSystem.getDisplayName()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.MultiFileSystem.find(java.lang.String,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.MultiFileSystem.findResource(java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.MultiFileSystem.getRoot()
meth public org.openide.filesystems.FileSystem$Status org.openide.filesystems.FileSystem.getStatus()
meth public void org.openide.filesystems.MultiFileSystem.addNotify()
meth public void org.openide.filesystems.MultiFileSystem.prepareEnvironment(org.openide.filesystems.FileSystem$Environment) throws org.openide.filesystems.EnvironmentNotSupportedException
meth public void org.openide.filesystems.MultiFileSystem.refresh(boolean)
meth public void org.openide.filesystems.MultiFileSystem.removeNotify()
supr org.openide.filesystems.FileSystem
CLSS public org.openide.filesystems.Repository
cons public Repository(org.openide.filesystems.FileSystem)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final [Lorg.openide.filesystems.FileSystem; org.openide.filesystems.Repository.toArray()
meth public final java.util.Enumeration org.openide.filesystems.Repository.fileSystems()
meth public final java.util.Enumeration org.openide.filesystems.Repository.findAll(java.lang.String,java.lang.String,java.lang.String)
meth public final java.util.Enumeration org.openide.filesystems.Repository.findAllResources(java.lang.String)
meth public final java.util.Enumeration org.openide.filesystems.Repository.getFileSystems()
meth public final org.openide.filesystems.FileObject org.openide.filesystems.Repository.find(java.lang.String,java.lang.String,java.lang.String)
meth public final org.openide.filesystems.FileObject org.openide.filesystems.Repository.findResource(java.lang.String)
meth public final org.openide.filesystems.FileSystem org.openide.filesystems.Repository.findFileSystem(java.lang.String)
meth public final org.openide.filesystems.FileSystem org.openide.filesystems.Repository.getDefaultFileSystem()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.Repository.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.Repository.addFileSystem(org.openide.filesystems.FileSystem)
meth public final void org.openide.filesystems.Repository.addRepositoryListener(org.openide.filesystems.RepositoryListener)
meth public final void org.openide.filesystems.Repository.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.Repository.removeFileSystem(org.openide.filesystems.FileSystem)
meth public final void org.openide.filesystems.Repository.removeRepositoryListener(org.openide.filesystems.RepositoryListener)
meth public final void org.openide.filesystems.Repository.reorder([I)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static org.openide.filesystems.Repository org.openide.filesystems.Repository.getDefault()
meth public synchronized final void org.openide.filesystems.Repository.readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public synchronized final void org.openide.filesystems.Repository.writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
CLSS public org.openide.filesystems.RepositoryAdapter
cons public RepositoryAdapter()
intf java.util.EventListener
intf org.openide.filesystems.RepositoryListener
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
meth public void org.openide.filesystems.RepositoryAdapter.fileSystemAdded(org.openide.filesystems.RepositoryEvent)
meth public void org.openide.filesystems.RepositoryAdapter.fileSystemPoolReordered(org.openide.filesystems.RepositoryReorderedEvent)
meth public void org.openide.filesystems.RepositoryAdapter.fileSystemRemoved(org.openide.filesystems.RepositoryEvent)
supr java.lang.Object
CLSS public org.openide.filesystems.RepositoryEvent
cons public RepositoryEvent(org.openide.filesystems.Repository,org.openide.filesystems.FileSystem,boolean)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.RepositoryEvent.isAdded()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileSystem org.openide.filesystems.RepositoryEvent.getFileSystem()
meth public org.openide.filesystems.Repository org.openide.filesystems.RepositoryEvent.getRepository()
supr java.util.EventObject
CLSS public abstract interface org.openide.filesystems.RepositoryListener
intf java.util.EventListener
meth public abstract void org.openide.filesystems.RepositoryListener.fileSystemAdded(org.openide.filesystems.RepositoryEvent)
meth public abstract void org.openide.filesystems.RepositoryListener.fileSystemPoolReordered(org.openide.filesystems.RepositoryReorderedEvent)
meth public abstract void org.openide.filesystems.RepositoryListener.fileSystemRemoved(org.openide.filesystems.RepositoryEvent)
supr null
CLSS public org.openide.filesystems.RepositoryReorderedEvent
cons public RepositoryReorderedEvent(org.openide.filesystems.Repository,[I)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [I org.openide.filesystems.RepositoryReorderedEvent.getPermutation()
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
meth public org.openide.filesystems.Repository org.openide.filesystems.RepositoryReorderedEvent.getRepository()
supr java.util.EventObject
CLSS public abstract org.openide.filesystems.URLMapper
cons public URLMapper()
fld  constant public static final int org.openide.filesystems.URLMapper.EXTERNAL
fld  constant public static final int org.openide.filesystems.URLMapper.INTERNAL
fld  constant public static final int org.openide.filesystems.URLMapper.NETWORK
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Lorg.openide.filesystems.FileObject; org.openide.filesystems.URLMapper.getFileObjects(java.net.URL)
meth public abstract java.net.URL org.openide.filesystems.URLMapper.getURL(org.openide.filesystems.FileObject,int)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static [Lorg.openide.filesystems.FileObject; org.openide.filesystems.URLMapper.findFileObjects(java.net.URL)
meth public static java.net.URL org.openide.filesystems.URLMapper.findURL(org.openide.filesystems.FileObject,int)
meth public static org.openide.filesystems.FileObject org.openide.filesystems.URLMapper.findFileObject(java.net.URL)
supr java.lang.Object
CLSS public final org.openide.filesystems.XMLFileSystem
cons public XMLFileSystem()
cons public XMLFileSystem(java.lang.String) throws org.xml.sax.SAXException
cons public XMLFileSystem(java.net.URL) throws org.xml.sax.SAXException
cons public XMLFileSystem(org.openide.filesystems.FileSystemCapability)
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_DISPLAY_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_HIDDEN
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_READ_ONLY
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_ROOT
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_SYSTEM_NAME
fld  constant public static final java.lang.String org.openide.filesystems.FileSystem.PROP_VALID
fld  protected org.openide.filesystems.AbstractFileSystem$Attr org.openide.filesystems.AbstractFileSystem.attr
fld  protected org.openide.filesystems.AbstractFileSystem$Change org.openide.filesystems.AbstractFileSystem.change
fld  protected org.openide.filesystems.AbstractFileSystem$Info org.openide.filesystems.AbstractFileSystem.info
fld  protected org.openide.filesystems.AbstractFileSystem$List org.openide.filesystems.AbstractFileSystem.list
fld  protected org.openide.filesystems.AbstractFileSystem$Transfer org.openide.filesystems.AbstractFileSystem.transfer
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Attr
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Change
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Info
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$List
innr public static abstract interface org.openide.filesystems.AbstractFileSystem$Transfer
innr public static abstract interface org.openide.filesystems.FileSystem$AtomicAction
innr public static abstract interface org.openide.filesystems.FileSystem$HtmlStatus
innr public static abstract interface org.openide.filesystems.FileSystem$Status
innr public static abstract org.openide.filesystems.FileSystem$Environment
innr public static org.openide.filesystems.XMLFileSystem$Impl
intf java.io.Serializable
meth protected boolean org.openide.filesystems.AbstractFileSystem.canRead(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.canWrite(java.lang.String)
meth protected boolean org.openide.filesystems.AbstractFileSystem.checkVirtual(java.lang.String)
meth protected boolean org.openide.filesystems.FileSystem.isPersistent()
meth protected final int org.openide.filesystems.AbstractFileSystem.getRefreshTime()
meth protected final java.lang.ref.Reference org.openide.filesystems.AbstractFileSystem.findReference(java.lang.String)
meth protected final java.util.Enumeration org.openide.filesystems.AbstractFileSystem.existingFileObjects(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.refreshRoot()
meth protected final void org.openide.filesystems.AbstractFileSystem.refreshResource(java.lang.String,boolean)
meth protected final void org.openide.filesystems.FileSystem.fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void org.openide.filesystems.FileSystem.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void org.openide.filesystems.FileSystem.fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void org.openide.filesystems.FileSystem.setCapability(org.openide.filesystems.FileSystemCapability)
meth protected final void org.openide.filesystems.FileSystem.setSystemName(java.lang.String) throws java.beans.PropertyVetoException
meth protected java.lang.ref.Reference org.openide.filesystems.XMLFileSystem.createReference(org.openide.filesystems.FileObject)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected synchronized final void org.openide.filesystems.AbstractFileSystem.setRefreshTime(int)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.filesystems.AbstractFileSystem.markImportant(java.lang.String,boolean)
meth public [Ljava.net.URL; org.openide.filesystems.XMLFileSystem.getXmlUrls()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.AbstractFileSystem.getActions()
meth public [Lorg.openide.util.actions.SystemAction; org.openide.filesystems.FileSystem.getActions(java.util.Set)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.openide.filesystems.XMLFileSystem.isReadOnly()
meth public final boolean org.openide.filesystems.FileSystem.isDefault()
meth public final boolean org.openide.filesystems.FileSystem.isHidden()
meth public final boolean org.openide.filesystems.FileSystem.isValid()
meth public final java.lang.String org.openide.filesystems.FileSystem.getSystemName()
meth public final org.openide.filesystems.FileSystemCapability org.openide.filesystems.FileSystem.getCapability()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.filesystems.FileSystem.addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void org.openide.filesystems.FileSystem.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.filesystems.FileSystem.removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void org.openide.filesystems.FileSystem.runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public final void org.openide.filesystems.FileSystem.setHidden(boolean)
meth public java.lang.String org.openide.filesystems.FileSystem.toString()
meth public java.lang.String org.openide.filesystems.XMLFileSystem.getDisplayName()
meth public java.net.URL org.openide.filesystems.XMLFileSystem.getXmlUrl()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.find(java.lang.String,java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.findResource(java.lang.String)
meth public org.openide.filesystems.FileObject org.openide.filesystems.AbstractFileSystem.getRoot()
meth public org.openide.filesystems.FileSystem$Status org.openide.filesystems.FileSystem.getStatus()
meth public synchronized void org.openide.filesystems.XMLFileSystem.setXmlUrl(java.net.URL) throws java.beans.PropertyVetoException,java.io.IOException
meth public void org.openide.filesystems.AbstractFileSystem.refresh(boolean)
meth public void org.openide.filesystems.FileSystem.prepareEnvironment(org.openide.filesystems.FileSystem$Environment) throws org.openide.filesystems.EnvironmentNotSupportedException
meth public void org.openide.filesystems.XMLFileSystem.addNotify()
meth public void org.openide.filesystems.XMLFileSystem.removeNotify()
meth public void org.openide.filesystems.XMLFileSystem.setXmlUrl(java.net.URL,boolean) throws java.beans.PropertyVetoException,java.io.IOException
meth public void org.openide.filesystems.XMLFileSystem.setXmlUrls([Ljava.net.URL;) throws java.beans.PropertyVetoException,java.io.IOException
supr org.openide.filesystems.AbstractFileSystem
