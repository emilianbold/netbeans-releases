#API master signature file
#Version 1.11.1
CLSS public abstract org.openide.windows.IOProvider
cons protected IOProvider()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.openide.windows.InputOutput org.openide.windows.IOProvider.getIO(java.lang.String,boolean)
meth public abstract org.openide.windows.OutputWriter org.openide.windows.IOProvider.getStdOut()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.windows.InputOutput org.openide.windows.IOProvider.getIO(java.lang.String,[Ljavax.swing.Action;)
meth public static org.openide.windows.IOProvider org.openide.windows.IOProvider.getDefault()
supr java.lang.Object
CLSS public abstract interface org.openide.windows.InputOutput
fld  public static final java.io.Reader org.openide.windows.InputOutput.nullReader
fld  public static final org.openide.windows.InputOutput org.openide.windows.InputOutput.NULL
fld  public static final org.openide.windows.OutputWriter org.openide.windows.InputOutput.nullWriter
meth public abstract boolean org.openide.windows.InputOutput.isClosed()
meth public abstract boolean org.openide.windows.InputOutput.isErrSeparated()
meth public abstract boolean org.openide.windows.InputOutput.isFocusTaken()
meth public abstract java.io.Reader org.openide.windows.InputOutput.flushReader()
meth public abstract java.io.Reader org.openide.windows.InputOutput.getIn()
meth public abstract org.openide.windows.OutputWriter org.openide.windows.InputOutput.getErr()
meth public abstract org.openide.windows.OutputWriter org.openide.windows.InputOutput.getOut()
meth public abstract void org.openide.windows.InputOutput.closeInputOutput()
meth public abstract void org.openide.windows.InputOutput.select()
meth public abstract void org.openide.windows.InputOutput.setErrSeparated(boolean)
meth public abstract void org.openide.windows.InputOutput.setErrVisible(boolean)
meth public abstract void org.openide.windows.InputOutput.setFocusTaken(boolean)
meth public abstract void org.openide.windows.InputOutput.setInputVisible(boolean)
meth public abstract void org.openide.windows.InputOutput.setOutputVisible(boolean)
supr null
CLSS public abstract org.openide.windows.OutputEvent
cons public OutputEvent(org.openide.windows.InputOutput)
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.openide.windows.OutputEvent.getLine()
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
meth public org.openide.windows.InputOutput org.openide.windows.OutputEvent.getInputOutput()
supr java.util.EventObject
CLSS public abstract interface org.openide.windows.OutputListener
intf java.util.EventListener
meth public abstract void org.openide.windows.OutputListener.outputLineAction(org.openide.windows.OutputEvent)
meth public abstract void org.openide.windows.OutputListener.outputLineCleared(org.openide.windows.OutputEvent)
meth public abstract void org.openide.windows.OutputListener.outputLineSelected(org.openide.windows.OutputEvent)
supr null
CLSS public abstract org.openide.windows.OutputWriter
cons protected OutputWriter(java.io.Writer)
fld  protected java.io.Writer java.io.PrintWriter.out
fld  protected java.lang.Object java.io.Writer.lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.io.PrintWriter.setError()
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract void org.openide.windows.OutputWriter.println(java.lang.String,org.openide.windows.OutputListener) throws java.io.IOException
meth public abstract void org.openide.windows.OutputWriter.reset() throws java.io.IOException
meth public boolean java.io.PrintWriter.checkError()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.PrintWriter java.io.PrintWriter.append(char)
meth public java.io.PrintWriter java.io.PrintWriter.append(java.lang.CharSequence)
meth public java.io.PrintWriter java.io.PrintWriter.append(java.lang.CharSequence,int,int)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public transient java.io.PrintWriter java.io.PrintWriter.format(java.lang.String,[Ljava.lang.Object;)
meth public transient java.io.PrintWriter java.io.PrintWriter.format(java.util.Locale,java.lang.String,[Ljava.lang.Object;)
meth public transient java.io.PrintWriter java.io.PrintWriter.printf(java.lang.String,[Ljava.lang.Object;)
meth public transient java.io.PrintWriter java.io.PrintWriter.printf(java.util.Locale,java.lang.String,[Ljava.lang.Object;)
meth public void java.io.PrintWriter.close()
meth public void java.io.PrintWriter.flush()
meth public void java.io.PrintWriter.print([C)
meth public void java.io.PrintWriter.print(boolean)
meth public void java.io.PrintWriter.print(char)
meth public void java.io.PrintWriter.print(double)
meth public void java.io.PrintWriter.print(float)
meth public void java.io.PrintWriter.print(int)
meth public void java.io.PrintWriter.print(java.lang.Object)
meth public void java.io.PrintWriter.print(java.lang.String)
meth public void java.io.PrintWriter.print(long)
meth public void java.io.PrintWriter.println()
meth public void java.io.PrintWriter.println([C)
meth public void java.io.PrintWriter.println(boolean)
meth public void java.io.PrintWriter.println(char)
meth public void java.io.PrintWriter.println(double)
meth public void java.io.PrintWriter.println(float)
meth public void java.io.PrintWriter.println(int)
meth public void java.io.PrintWriter.println(java.lang.Object)
meth public void java.io.PrintWriter.println(java.lang.String)
meth public void java.io.PrintWriter.println(long)
meth public void java.io.PrintWriter.write([C)
meth public void java.io.PrintWriter.write([C,int,int)
meth public void java.io.PrintWriter.write(int)
meth public void java.io.PrintWriter.write(java.lang.String)
meth public void java.io.PrintWriter.write(java.lang.String,int,int)
meth public void org.openide.windows.OutputWriter.println(java.lang.String,org.openide.windows.OutputListener,boolean) throws java.io.IOException
meth public volatile java.io.Writer java.io.PrintWriter.append(char) throws java.io.IOException
meth public volatile java.io.Writer java.io.PrintWriter.append(java.lang.CharSequence) throws java.io.IOException
meth public volatile java.io.Writer java.io.PrintWriter.append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public volatile java.lang.Appendable java.io.PrintWriter.append(char) throws java.io.IOException
meth public volatile java.lang.Appendable java.io.PrintWriter.append(java.lang.CharSequence) throws java.io.IOException
meth public volatile java.lang.Appendable java.io.PrintWriter.append(java.lang.CharSequence,int,int) throws java.io.IOException
supr java.io.PrintWriter
