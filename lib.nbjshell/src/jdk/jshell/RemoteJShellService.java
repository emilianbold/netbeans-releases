/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdk.jshell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Carries out non-agent tasks for the JShell. Its actual implementation
 * may vary depending on the launch mode of the JShell
 * 
 * @author sdedic
 */
public interface RemoteJShellService {
    /**
     * Redefines classes within the machine. May communicate with the agent. To obtain
     * a handle for a class, {@link #getClasshandle} must be called.
     * 
     * @param redefines handle-to-classfile contents map.
     */
    public void         redefineClasses(Map<Object, byte[]> redefines) throws IOException, IllegalStateException;
    
    /**
     * Returns handle that corresponds to a named class. Returns null, 
     * if the name is ambiguous.
     * @param className class name
     * @return the handle.
     */
    public Object       getClassHandle(String className);
    
    /**
     * Provides Executor to execute user code. User code should be run in a specific
     * Executor to provide synchronization with other possible parallel actions.
     * @return Executor which should be used to run the user code.
     */
    public Executor     getCodeExecutor();
    
    /**
     * Ensures the connection has been established. Waits up to millis milliseconds
     * before it times out and returns. May throw IOException on I/O erro other than
     * timeout. 
     * @param millis timeout for connection
     * @throws IOException 
     */
    public void         waitConnected(long millis) throws IOException;
    
    /**
     * Stream with agent responses. Throws IOException when the stream 
     * could not be opened (i.e. connection was not established, or is closed).
     * @return agent response stream
     */
    public InputStream  getResponseStream() throws IOException;
    
    /**
     * Stream where to write agent commands. Throws IOException if the
     * stream has been closed.
     * @return command stream to agent
     */
    public OutputStream getCommandStream() throws IOException;
    
    /**
     * Attempts to stop the user code in the target machine.
     * Some implementations may decide to discard the request.
     * @throws IllegalStateException 
     * @return true, if the implementation even attempted to stop
     * the machine.
     */
    public boolean sendStopUserCode() throws IllegalStateException, IOException;
    
    /**
     * Decorates launcher arguments. Results undefined, if the environment
     * connects to an existing VM. This method is called when the JShell
     * attempts to launch the machine on its own. The implementation is expected
     * to add any classpath necessary.
     * 
     * @param baseArgs
     * @return 
     */
    public String decorateLaunchArgs(String baseArgs);
    
    /**
     * Requests shutdown of the target process. The implementation may ignore
     * the request, but JShell should terminate at the local side anyway.
     * @return true, if the request was accepted
     */
    public boolean requestShutdown();
    
    /**
     * Closes the supplied I/O streams. If the streams are not yet opened or created
     * the method does not even attempt to initiate the target VM. Further requests
     * to get streams will result in an IOException.
     */
    public void closeStreams();
    
    public String getTargetSpec();
}
