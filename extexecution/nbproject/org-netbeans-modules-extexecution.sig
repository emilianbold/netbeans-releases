#Signature file v4.0
#Version 1.14.1

CLSS public abstract interface java.io.Closeable
meth public abstract void close() throws java.io.IOException

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

CLSS public abstract interface java.lang.Runnable
meth public abstract void run()

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public final org.netbeans.api.extexecution.ExecutionDescriptor
cons public ExecutionDescriptor()
innr public abstract interface static InputProcessorFactory
innr public abstract interface static LineConvertorFactory
innr public abstract interface static RerunCondition
meth public org.netbeans.api.extexecution.ExecutionDescriptor charset(java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor controllable(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errConvertorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errLineBased(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor frontWindow(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor inputOutput(org.openide.windows.InputOutput)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor inputVisible(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor optionsPath(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outConvertorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outLineBased(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor postExecution(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor preExecution(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor rerunCondition(org.netbeans.api.extexecution.ExecutionDescriptor$RerunCondition)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor showProgress(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor showSuspended(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds charset,controllable,errConvertorFactory,errLineBased,errProcessorFactory,front,input,inputOutput,optionsPath,outConvertorFactory,outLineBased,outProcessorFactory,postExecution,preExecution,progress,rerunCondition,suspend
hcls DescriptorData

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory
meth public abstract org.netbeans.api.extexecution.input.InputProcessor newInputProcessor(org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory
meth public abstract org.netbeans.api.extexecution.print.LineConvertor newLineConvertor()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$RerunCondition
meth public abstract boolean isRerunPossible()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public final org.netbeans.api.extexecution.ExecutionService
meth public java.util.concurrent.Future<java.lang.Integer> run()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.ExecutionService newService(java.util.concurrent.Callable<java.lang.Process>,org.netbeans.api.extexecution.ExecutionDescriptor,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EXECUTOR_SERVICE,EXECUTOR_SHUTDOWN_SLICE,LOGGER,RUNNING_PROCESSES,descriptor,originalDisplayName,processCreator
hcls ProgressAction,ProgressCancellable,WrappedException

CLSS public final org.netbeans.api.extexecution.ExternalProcessBuilder
cons public ExternalProcessBuilder(java.lang.String)
intf java.util.concurrent.Callable<java.lang.Process>
meth public java.lang.Process call() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder addArgument(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder addEnvironmentVariable(java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder prependPath(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder redirectErrorStream(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder workingDirectory(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER,PROXY_AUTHENTICATION_PASSWORD,PROXY_AUTHENTICATION_USERNAME,USE_PROXY_AUTHENTICATION,arguments,envVariables,executable,paths,redirectErrorStream,workingDirectory
hcls BuilderData

CLSS public abstract interface org.netbeans.api.extexecution.input.InputProcessor
intf java.io.Closeable
meth public abstract void close() throws java.io.IOException
meth public abstract void processInput(char[]) throws java.io.IOException
meth public abstract void reset() throws java.io.IOException

CLSS public final org.netbeans.api.extexecution.input.InputProcessors
meth public !varargs static org.netbeans.api.extexecution.input.InputProcessor proxy(org.netbeans.api.extexecution.input.InputProcessor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor ansiStripping(org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor bridge(org.netbeans.api.extexecution.input.LineProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor copying(java.io.Writer)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor printing(org.openide.windows.OutputWriter,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor printing(org.openide.windows.OutputWriter,org.netbeans.api.extexecution.print.LineConvertor,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER
hcls AnsiStrippingInputProcessor,Bridge,CopyingInputProcessor,PrintingInputProcessor,ProxyInputProcessor

CLSS public abstract interface org.netbeans.api.extexecution.input.InputReader
intf java.io.Closeable
meth public abstract int readInput(org.netbeans.api.extexecution.input.InputProcessor) throws java.io.IOException
meth public abstract void close() throws java.io.IOException

CLSS public final org.netbeans.api.extexecution.input.InputReaderTask
intf java.lang.Runnable
intf org.openide.util.Cancellable
meth public boolean cancel()
meth public static org.netbeans.api.extexecution.input.InputReaderTask newDrainingTask(org.netbeans.api.extexecution.input.InputReader,org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReaderTask newTask(org.netbeans.api.extexecution.input.InputReader,org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void run()
supr java.lang.Object
hfds DELAY,LOGGER,cancelled,draining,inputProcessor,inputReader,running

CLSS public final org.netbeans.api.extexecution.input.InputReaders
innr public final static FileInput
meth public static org.netbeans.api.extexecution.input.InputReader forFile(java.io.File,java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReader forFileInputProvider(org.netbeans.api.extexecution.input.InputReaders$FileInput$Provider)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReader forReader(java.io.Reader)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReader forStream(java.io.InputStream,java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static org.netbeans.api.extexecution.input.InputReaders$FileInput
cons public FileInput(java.io.File,java.nio.charset.Charset)
innr public abstract interface static Provider
meth public java.io.File getFile()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.nio.charset.Charset getCharset()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds charset,file

CLSS public abstract interface static org.netbeans.api.extexecution.input.InputReaders$FileInput$Provider
meth public abstract org.netbeans.api.extexecution.input.InputReaders$FileInput getFileInput()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.api.extexecution.input.LineProcessor
intf java.io.Closeable
meth public abstract void close()
meth public abstract void processLine(java.lang.String)
meth public abstract void reset()

CLSS public final org.netbeans.api.extexecution.input.LineProcessors
meth public !varargs static org.netbeans.api.extexecution.input.LineProcessor proxy(org.netbeans.api.extexecution.input.LineProcessor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.LineProcessor patternWaiting(java.util.regex.Pattern,java.util.concurrent.CountDownLatch)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.LineProcessor printing(org.openide.windows.OutputWriter,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.LineProcessor printing(org.openide.windows.OutputWriter,org.netbeans.api.extexecution.print.LineConvertor,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER
hcls PrintingLineProcessor,ProxyLineProcessor,WaitingLineProcessor

CLSS public final org.netbeans.api.extexecution.print.ConvertedLine
meth public java.lang.String getText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.windows.OutputListener getListener()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.extexecution.print.ConvertedLine forText(java.lang.String,org.openide.windows.OutputListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds listener,text

CLSS public abstract interface org.netbeans.api.extexecution.print.LineConvertor
meth public abstract java.util.List<org.netbeans.api.extexecution.print.ConvertedLine> convert(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final org.netbeans.api.extexecution.print.LineConvertors
innr public abstract interface static FileLocator
meth public !varargs static org.netbeans.api.extexecution.print.LineConvertor proxy(org.netbeans.api.extexecution.print.LineConvertor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.print.LineConvertor filePattern(org.netbeans.api.extexecution.print.LineConvertors$FileLocator,java.util.regex.Pattern,java.util.regex.Pattern,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.print.LineConvertor httpUrl()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER
hcls FilePatternConvertor,HttpUrlConvertor,ProxyLineConvertor,UrlOutputListener

CLSS public abstract interface static org.netbeans.api.extexecution.print.LineConvertors$FileLocator
meth public abstract org.openide.filesystems.FileObject find(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

