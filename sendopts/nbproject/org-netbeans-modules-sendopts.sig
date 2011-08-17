#Signature file v4.1
#Version 2.13

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Exception
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.Object
cons public init()
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

CLSS public java.lang.Throwable
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object
hfds backtrace,cause,detailMessage,serialVersionUID,stackTrace

CLSS public final org.netbeans.api.sendopts.CommandException
cons public init(int)
cons public init(int,java.lang.String)
meth public int getExitCode()
meth public java.lang.String getLocalizedMessage()
supr java.lang.Exception
hfds exitCode,locMsg

CLSS public final org.netbeans.api.sendopts.CommandLine
meth public !varargs void process(java.lang.String[]) throws org.netbeans.api.sendopts.CommandException
meth public static org.netbeans.api.sendopts.CommandLine getDefault()
meth public void process(java.lang.String[],java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File) throws org.netbeans.api.sendopts.CommandException
meth public void usage(java.io.PrintWriter)
supr java.lang.Object
hfds ERROR_BASE

CLSS public final org.netbeans.spi.sendopts.Env
meth public java.io.File getCurrentDirectory()
meth public java.io.InputStream getInputStream()
meth public java.io.PrintStream getErrorStream()
meth public java.io.PrintStream getOutputStream()
supr java.lang.Object
hfds currentDir,err,is,os

CLSS public final org.netbeans.spi.sendopts.Option
fld public final static char NO_SHORT_NAME = '\uffff'
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.netbeans.spi.sendopts.Option additionalArguments(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option always()
meth public static org.netbeans.spi.sendopts.Option defaultArguments()
meth public static org.netbeans.spi.sendopts.Option displayName(org.netbeans.spi.sendopts.Option,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option optionalArgument(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option requiredArgument(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option shortDescription(org.netbeans.spi.sendopts.Option,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option withoutArgument(char,java.lang.String)
supr java.lang.Object
hfds EMPTY,bundles,impl,keys,longName,shortName

CLSS public final org.netbeans.spi.sendopts.OptionGroups
meth public !varargs static org.netbeans.spi.sendopts.Option allOf(org.netbeans.spi.sendopts.Option[])
meth public !varargs static org.netbeans.spi.sendopts.Option anyOf(org.netbeans.spi.sendopts.Option[])
meth public !varargs static org.netbeans.spi.sendopts.Option oneOf(org.netbeans.spi.sendopts.Option[])
meth public !varargs static org.netbeans.spi.sendopts.Option someOf(org.netbeans.spi.sendopts.Option[])
supr java.lang.Object

CLSS public abstract org.netbeans.spi.sendopts.OptionProcessor
cons protected init()
meth protected abstract java.util.Set<org.netbeans.spi.sendopts.Option> getOptions()
meth protected abstract void process(org.netbeans.spi.sendopts.Env,java.util.Map<org.netbeans.spi.sendopts.Option,java.lang.String[]>) throws org.netbeans.api.sendopts.CommandException
supr java.lang.Object

