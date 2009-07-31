#Signature file v4.0
#Version 1.16.1

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

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected Enum(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object
hfds name,ordinal

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

CLSS public abstract org.openide.windows.IOColorLines
cons public IOColorLines()
meth protected abstract void println(java.lang.CharSequence,org.openide.windows.OutputListener,boolean,java.awt.Color) throws java.io.IOException
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static void println(org.openide.windows.InputOutput,java.lang.CharSequence,java.awt.Color) throws java.io.IOException
meth public static void println(org.openide.windows.InputOutput,java.lang.CharSequence,org.openide.windows.OutputListener,boolean,java.awt.Color) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.openide.windows.IOColors
cons public IOColors()
innr public final static !enum OutputType
meth protected abstract java.awt.Color getColor(org.openide.windows.IOColors$OutputType)
meth protected abstract void setColor(org.openide.windows.IOColors$OutputType,java.awt.Color)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static java.awt.Color getColor(org.openide.windows.InputOutput,org.openide.windows.IOColors$OutputType)
meth public static void setColor(org.openide.windows.InputOutput,org.openide.windows.IOColors$OutputType,java.awt.Color)
supr java.lang.Object

CLSS public final static !enum org.openide.windows.IOColors$OutputType
fld public final static org.openide.windows.IOColors$OutputType ERROR
fld public final static org.openide.windows.IOColors$OutputType HYPERLINK
fld public final static org.openide.windows.IOColors$OutputType HYPERLINK_IMPORTANT
fld public final static org.openide.windows.IOColors$OutputType OUTPUT
meth public static org.openide.windows.IOColors$OutputType valueOf(java.lang.String)
meth public static org.openide.windows.IOColors$OutputType[] values()
supr java.lang.Enum<org.openide.windows.IOColors$OutputType>

CLSS public final org.openide.windows.IOContainer
innr public abstract interface static CallBacks
innr public abstract interface static Provider
meth public boolean isActivated()
meth public boolean isCloseable(javax.swing.JComponent)
meth public javax.swing.JComponent getSelected()
meth public static org.openide.windows.IOContainer create(org.openide.windows.IOContainer$Provider)
meth public static org.openide.windows.IOContainer getDefault()
meth public void add(javax.swing.JComponent,org.openide.windows.IOContainer$CallBacks)
meth public void open()
meth public void remove(javax.swing.JComponent)
meth public void requestActive()
meth public void requestVisible()
meth public void select(javax.swing.JComponent)
meth public void setIcon(javax.swing.JComponent,javax.swing.Icon)
meth public void setTitle(javax.swing.JComponent,java.lang.String)
meth public void setToolTipText(javax.swing.JComponent,java.lang.String)
meth public void setToolbarActions(javax.swing.JComponent,javax.swing.Action[])
supr java.lang.Object
hfds LOGGER,defaultIOContainer,provider
hcls Trivial

CLSS public abstract interface static org.openide.windows.IOContainer$CallBacks
meth public abstract void activated()
meth public abstract void closed()
meth public abstract void deactivated()
meth public abstract void selected()

CLSS public abstract interface static org.openide.windows.IOContainer$Provider
meth public abstract boolean isActivated()
meth public abstract boolean isCloseable(javax.swing.JComponent)
meth public abstract javax.swing.JComponent getSelected()
meth public abstract void add(javax.swing.JComponent,org.openide.windows.IOContainer$CallBacks)
meth public abstract void open()
meth public abstract void remove(javax.swing.JComponent)
meth public abstract void requestActive()
meth public abstract void requestVisible()
meth public abstract void select(javax.swing.JComponent)
meth public abstract void setIcon(javax.swing.JComponent,javax.swing.Icon)
meth public abstract void setTitle(javax.swing.JComponent,java.lang.String)
meth public abstract void setToolTipText(javax.swing.JComponent,java.lang.String)
meth public abstract void setToolbarActions(javax.swing.JComponent,javax.swing.Action[])

CLSS public abstract org.openide.windows.IOPosition
cons public IOPosition()
innr public abstract interface static Position
meth protected abstract org.openide.windows.IOPosition$Position currentPosition()
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static org.openide.windows.IOPosition$Position currentPosition(org.openide.windows.InputOutput)
supr java.lang.Object

CLSS public abstract interface static org.openide.windows.IOPosition$Position
meth public abstract void scrollTo()

CLSS public abstract org.openide.windows.IOProvider
cons protected IOProvider()
meth public abstract org.openide.windows.InputOutput getIO(java.lang.String,boolean)
meth public abstract org.openide.windows.OutputWriter getStdOut()
meth public java.lang.String getName()
meth public org.openide.windows.InputOutput getIO(java.lang.String,javax.swing.Action[])
meth public org.openide.windows.InputOutput getIO(java.lang.String,javax.swing.Action[],org.openide.windows.IOContainer)
meth public static org.openide.windows.IOProvider get(java.lang.String)
meth public static org.openide.windows.IOProvider getDefault()
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.windows.IOTab
cons public IOTab()
meth protected abstract java.lang.String getToolTipText()
meth protected abstract javax.swing.Icon getIcon()
meth protected abstract void setIcon(javax.swing.Icon)
meth protected abstract void setToolTipText(java.lang.String)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static java.lang.String getToolTipText(org.openide.windows.InputOutput)
meth public static javax.swing.Icon getIcon(org.openide.windows.InputOutput)
meth public static void setIcon(org.openide.windows.InputOutput,javax.swing.Icon)
meth public static void setToolTipText(org.openide.windows.InputOutput,java.lang.String)
supr java.lang.Object

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

