#API master signature file
#Version 2.1.1
CLSS public static abstract org.netbeans.modules.sendopts.OptionImpl$Trampoline
cons public Trampoline()
fld  public static org.netbeans.modules.sendopts.OptionImpl$Trampoline org.netbeans.modules.sendopts.OptionImpl$Trampoline.DEFAULT
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract [Lorg.netbeans.spi.sendopts.Option; org.netbeans.modules.sendopts.OptionImpl$Trampoline.getOptions(org.netbeans.spi.sendopts.OptionProcessor)
meth public abstract int org.netbeans.modules.sendopts.OptionImpl$Trampoline.getShortName(org.netbeans.spi.sendopts.Option)
meth public abstract java.lang.String org.netbeans.modules.sendopts.OptionImpl$Trampoline.getDisplayName(org.netbeans.spi.sendopts.Option,java.util.Locale)
meth public abstract java.lang.String org.netbeans.modules.sendopts.OptionImpl$Trampoline.getLongName(org.netbeans.spi.sendopts.Option)
meth public abstract org.netbeans.modules.sendopts.OptionImpl org.netbeans.modules.sendopts.OptionImpl$Trampoline.impl(org.netbeans.spi.sendopts.Option)
meth public abstract org.netbeans.spi.sendopts.Env org.netbeans.modules.sendopts.OptionImpl$Trampoline.create(java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File)
meth public abstract void org.netbeans.modules.sendopts.OptionImpl$Trampoline.process(org.netbeans.spi.sendopts.OptionProcessor,org.netbeans.spi.sendopts.Env,java.util.Map) throws org.netbeans.api.sendopts.CommandException
meth public abstract void org.netbeans.modules.sendopts.OptionImpl$Trampoline.usage(java.io.PrintWriter,org.netbeans.spi.sendopts.Option,int)
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
CLSS public final org.netbeans.api.sendopts.CommandException
cons public CommandException(int)
cons public CommandException(int,java.lang.String)
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public [Ljava.lang.StackTraceElement; java.lang.Throwable.getStackTrace()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.sendopts.CommandException.getExitCode()
meth public java.lang.String java.lang.Throwable.getMessage()
meth public java.lang.String java.lang.Throwable.toString()
meth public java.lang.String org.netbeans.api.sendopts.CommandException.getLocalizedMessage()
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
supr java.lang.Exception
CLSS public final org.netbeans.api.sendopts.CommandLine
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
meth public static org.netbeans.api.sendopts.CommandLine org.netbeans.api.sendopts.CommandLine.getDefault()
meth public void org.netbeans.api.sendopts.CommandLine.process([Ljava.lang.String;) throws org.netbeans.api.sendopts.CommandException
meth public void org.netbeans.api.sendopts.CommandLine.process([Ljava.lang.String;,java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File) throws org.netbeans.api.sendopts.CommandException
meth public void org.netbeans.api.sendopts.CommandLine.usage(java.io.PrintWriter)
supr java.lang.Object
CLSS public final org.netbeans.spi.sendopts.Env
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.io.File org.netbeans.spi.sendopts.Env.getCurrentDirectory()
meth public java.io.InputStream org.netbeans.spi.sendopts.Env.getInputStream()
meth public java.io.PrintStream org.netbeans.spi.sendopts.Env.getErrorStream()
meth public java.io.PrintStream org.netbeans.spi.sendopts.Env.getOutputStream()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public final org.netbeans.spi.sendopts.Option
fld  constant public static final char org.netbeans.spi.sendopts.Option.NO_SHORT_NAME
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.spi.sendopts.Option.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.sendopts.Option.hashCode()
meth public java.lang.String org.netbeans.spi.sendopts.Option.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.additionalArguments(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.always()
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.defaultArguments()
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.displayName(org.netbeans.spi.sendopts.Option,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.optionalArgument(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.requiredArgument(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.shortDescription(org.netbeans.spi.sendopts.Option,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.Option.withoutArgument(char,java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.spi.sendopts.OptionGroups
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
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.OptionGroups.allOf([Lorg.netbeans.spi.sendopts.Option;)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.OptionGroups.anyOf([Lorg.netbeans.spi.sendopts.Option;)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.OptionGroups.oneOf([Lorg.netbeans.spi.sendopts.Option;)
meth public static org.netbeans.spi.sendopts.Option org.netbeans.spi.sendopts.OptionGroups.someOf([Lorg.netbeans.spi.sendopts.Option;)
supr java.lang.Object
CLSS public abstract org.netbeans.spi.sendopts.OptionProcessor
cons protected OptionProcessor()
meth protected abstract java.util.Set org.netbeans.spi.sendopts.OptionProcessor.getOptions()
meth protected abstract void org.netbeans.spi.sendopts.OptionProcessor.process(org.netbeans.spi.sendopts.Env,java.util.Map) throws org.netbeans.api.sendopts.CommandException
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
supr java.lang.Object
