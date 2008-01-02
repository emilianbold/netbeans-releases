#API master signature file
#Version 1.13.1
CLSS public abstract org.netbeans.spi.settings.Convertor
cons public Convertor()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected static org.openide.util.Lookup org.netbeans.spi.settings.Convertor.findContext(java.io.Reader)
meth protected static org.openide.util.Lookup org.netbeans.spi.settings.Convertor.findContext(java.io.Writer)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.Object org.netbeans.spi.settings.Convertor.read(java.io.Reader) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void org.netbeans.spi.settings.Convertor.registerSaver(java.lang.Object,org.netbeans.spi.settings.Saver)
meth public abstract void org.netbeans.spi.settings.Convertor.unregisterSaver(java.lang.Object,org.netbeans.spi.settings.Saver)
meth public abstract void org.netbeans.spi.settings.Convertor.write(java.io.Writer,java.lang.Object) throws java.io.IOException
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
CLSS public abstract org.netbeans.spi.settings.DOMConvertor
cons protected DOMConvertor(java.lang.String,java.lang.String,java.lang.String)
meth protected abstract java.lang.Object org.netbeans.spi.settings.DOMConvertor.readElement(org.w3c.dom.Element) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected abstract void org.netbeans.spi.settings.DOMConvertor.writeElement(org.w3c.dom.Document,org.w3c.dom.Element,java.lang.Object) throws java.io.IOException,org.w3c.dom.DOMException
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected static final java.lang.Object org.netbeans.spi.settings.DOMConvertor.delegateRead(org.w3c.dom.Element) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected static final org.w3c.dom.Element org.netbeans.spi.settings.DOMConvertor.delegateWrite(org.w3c.dom.Document,java.lang.Object) throws java.io.IOException,org.w3c.dom.DOMException
meth protected static org.openide.util.Lookup org.netbeans.spi.settings.Convertor.findContext(java.io.Reader)
meth protected static org.openide.util.Lookup org.netbeans.spi.settings.Convertor.findContext(java.io.Writer)
meth protected static org.openide.util.Lookup org.netbeans.spi.settings.DOMConvertor.findContext(org.w3c.dom.Document)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.netbeans.spi.settings.Convertor.registerSaver(java.lang.Object,org.netbeans.spi.settings.Saver)
meth public abstract void org.netbeans.spi.settings.Convertor.unregisterSaver(java.lang.Object,org.netbeans.spi.settings.Saver)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final java.lang.Object org.netbeans.spi.settings.DOMConvertor.read(java.io.Reader) throws java.io.IOException,java.lang.ClassNotFoundException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.netbeans.spi.settings.DOMConvertor.write(java.io.Writer,java.lang.Object) throws java.io.IOException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.settings.Convertor
CLSS public abstract interface org.netbeans.spi.settings.Saver
meth public abstract void org.netbeans.spi.settings.Saver.markDirty()
meth public abstract void org.netbeans.spi.settings.Saver.requestSave() throws java.io.IOException
supr null
