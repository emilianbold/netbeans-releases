#API master signature file
#Version 1.8.1
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
CLSS public final org.netbeans.api.editor.mimelookup.MimeLookup
fld  public static final org.openide.util.Lookup org.openide.util.Lookup.EMPTY
innr public static abstract interface org.openide.util.Lookup$Provider
innr public static abstract org.openide.util.Lookup$Item
innr public static abstract org.openide.util.Lookup$Result
innr public static final org.openide.util.Lookup$Template
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.api.editor.mimelookup.MimeLookup.lookup(java.lang.Class)
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Collection org.openide.util.Lookup.lookupAll(java.lang.Class)
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.mimelookup.MimeLookup org.netbeans.api.editor.mimelookup.MimeLookup.childLookup(java.lang.String)
meth public org.openide.util.Lookup$Item org.openide.util.Lookup.lookupItem(org.openide.util.Lookup$Template)
meth public org.openide.util.Lookup$Result org.netbeans.api.editor.mimelookup.MimeLookup.lookup(org.openide.util.Lookup$Template)
meth public org.openide.util.Lookup$Result org.openide.util.Lookup.lookupResult(java.lang.Class)
meth public static org.netbeans.api.editor.mimelookup.MimeLookup org.netbeans.api.editor.mimelookup.MimeLookup.getMimeLookup(java.lang.String)
meth public static org.openide.util.Lookup org.netbeans.api.editor.mimelookup.MimeLookup.getLookup(java.lang.String)
meth public static org.openide.util.Lookup org.netbeans.api.editor.mimelookup.MimeLookup.getLookup(org.netbeans.api.editor.mimelookup.MimePath)
meth public static synchronized org.openide.util.Lookup org.openide.util.Lookup.getDefault()
supr org.openide.util.Lookup
CLSS public final org.netbeans.api.editor.mimelookup.MimePath
fld  public static final org.netbeans.api.editor.mimelookup.MimePath org.netbeans.api.editor.mimelookup.MimePath.EMPTY
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.editor.mimelookup.MimePath.size()
meth public java.lang.String org.netbeans.api.editor.mimelookup.MimePath.getMimeType(int)
meth public java.lang.String org.netbeans.api.editor.mimelookup.MimePath.getPath()
meth public java.lang.String org.netbeans.api.editor.mimelookup.MimePath.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.editor.mimelookup.MimePath org.netbeans.api.editor.mimelookup.MimePath.getPrefix(int)
meth public static boolean org.netbeans.api.editor.mimelookup.MimePath.validate(java.lang.CharSequence)
meth public static boolean org.netbeans.api.editor.mimelookup.MimePath.validate(java.lang.CharSequence,java.lang.CharSequence)
meth public static org.netbeans.api.editor.mimelookup.MimePath org.netbeans.api.editor.mimelookup.MimePath.get(java.lang.String)
meth public static org.netbeans.api.editor.mimelookup.MimePath org.netbeans.api.editor.mimelookup.MimePath.get(org.netbeans.api.editor.mimelookup.MimePath,java.lang.String)
meth public static org.netbeans.api.editor.mimelookup.MimePath org.netbeans.api.editor.mimelookup.MimePath.parse(java.lang.String)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.editor.mimelookup.Class2LayerFolder
meth public abstract java.lang.Class org.netbeans.spi.editor.mimelookup.Class2LayerFolder.getClazz()
meth public abstract java.lang.String org.netbeans.spi.editor.mimelookup.Class2LayerFolder.getLayerFolderName()
meth public abstract org.netbeans.spi.editor.mimelookup.InstanceProvider org.netbeans.spi.editor.mimelookup.Class2LayerFolder.getInstanceProvider()
supr null
CLSS public abstract interface org.netbeans.spi.editor.mimelookup.InstanceProvider
meth public abstract java.lang.Object org.netbeans.spi.editor.mimelookup.InstanceProvider.createInstance(java.util.List)
supr null
CLSS public abstract interface org.netbeans.spi.editor.mimelookup.MimeDataProvider
meth public abstract org.openide.util.Lookup org.netbeans.spi.editor.mimelookup.MimeDataProvider.getLookup(org.netbeans.api.editor.mimelookup.MimePath)
supr null
CLSS public abstract interface org.netbeans.spi.editor.mimelookup.MimeLookupInitializer
meth public abstract org.openide.util.Lookup org.netbeans.spi.editor.mimelookup.MimeLookupInitializer.lookup()
meth public abstract org.openide.util.Lookup$Result org.netbeans.spi.editor.mimelookup.MimeLookupInitializer.child(java.lang.String)
supr null
