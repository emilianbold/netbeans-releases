#Signature file v4.0
#Version 1.13.1

CLSS public abstract interface java.io.Closeable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.PrintWriter
cons public PrintWriter(java.io.File) throws java.io.FileNotFoundException
cons public PrintWriter(java.io.File,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
cons public PrintWriter(java.io.OutputStream)
cons public PrintWriter(java.io.OutputStream,boolean)
cons public PrintWriter(java.io.Writer)
cons public PrintWriter(java.io.Writer,boolean)
cons public PrintWriter(java.lang.String) throws java.io.FileNotFoundException
cons public PrintWriter(java.lang.String,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
fld protected java.io.Writer out
meth protected void clearError()
meth protected void setError()
meth public !varargs java.io.PrintWriter format(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter format(java.util.Locale,java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter printf(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter printf(java.util.Locale,java.lang.String,java.lang.Object[])
meth public boolean checkError()
meth public java.io.PrintWriter append(char)
meth public java.io.PrintWriter append(java.lang.CharSequence)
meth public java.io.PrintWriter append(java.lang.CharSequence,int,int)
meth public void close()
meth public void flush()
meth public void print(boolean)
meth public void print(char)
meth public void print(char[])
meth public void print(double)
meth public void print(float)
meth public void print(int)
meth public void print(java.lang.Object)
meth public void print(java.lang.String)
meth public void print(long)
meth public void println()
meth public void println(boolean)
meth public void println(char)
meth public void println(char[])
meth public void println(double)
meth public void println(float)
meth public void println(int)
meth public void println(java.lang.Object)
meth public void println(java.lang.String)
meth public void println(long)
meth public void write(char[])
meth public void write(char[],int,int)
meth public void write(int)
meth public void write(java.lang.String)
meth public void write(java.lang.String,int,int)
supr java.io.Writer
hfds autoFlush,formatter,lineSeparator,psOut,trouble

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.io.Writer
cons protected Writer()
cons protected Writer(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(char[],int,int) throws java.io.IOException
meth public java.io.Writer append(char) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public void write(char[]) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.lang.Object
hfds writeBuffer,writeBufferSize

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract org.openide.windows.IOProvider
cons protected IOProvider()
meth public abstract org.openide.windows.InputOutput getIO(java.lang.String,boolean)
meth public abstract org.openide.windows.OutputWriter getStdOut()
meth public org.openide.windows.InputOutput getIO(java.lang.String,javax.swing.Action[])
meth public static org.openide.windows.IOProvider getDefault()
supr java.lang.Object
hcls Trivial

CLSS public abstract interface org.openide.windows.InputOutput
fld public final static java.io.Reader nullReader
 anno 0 java.lang.Deprecated()
fld public final static org.openide.windows.InputOutput NULL
fld public final static org.openide.windows.OutputWriter nullWriter
 anno 0 java.lang.Deprecated()
meth public abstract boolean isClosed()
meth public abstract boolean isErrSeparated()
meth public abstract boolean isFocusTaken()
meth public abstract java.io.Reader flushReader()
 anno 0 java.lang.Deprecated()
meth public abstract java.io.Reader getIn()
meth public abstract org.openide.windows.OutputWriter getErr()
meth public abstract org.openide.windows.OutputWriter getOut()
meth public abstract void closeInputOutput()
meth public abstract void select()
meth public abstract void setErrSeparated(boolean)
meth public abstract void setErrVisible(boolean)
meth public abstract void setFocusTaken(boolean)
meth public abstract void setInputVisible(boolean)
meth public abstract void setOutputVisible(boolean)

CLSS public abstract org.openide.windows.OutputEvent
cons public OutputEvent(org.openide.windows.InputOutput)
meth public abstract java.lang.String getLine()
meth public org.openide.windows.InputOutput getInputOutput()
supr java.util.EventObject
hfds serialVersionUID

CLSS public abstract interface org.openide.windows.OutputListener
intf java.util.EventListener
meth public abstract void outputLineAction(org.openide.windows.OutputEvent)
meth public abstract void outputLineCleared(org.openide.windows.OutputEvent)
meth public abstract void outputLineSelected(org.openide.windows.OutputEvent)

CLSS public abstract org.openide.windows.OutputWriter
cons protected OutputWriter(java.io.Writer)
meth public abstract void println(java.lang.String,org.openide.windows.OutputListener) throws java.io.IOException
meth public abstract void reset() throws java.io.IOException
meth public void println(java.lang.String,org.openide.windows.OutputListener,boolean) throws java.io.IOException
supr java.io.PrintWriter

