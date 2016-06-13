/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjshell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import static java.util.stream.Collectors.toList;
import jdk.internal.jshell.debug.InternalDebugControl;
import static jdk.internal.jshell.debug.InternalDebugControl.DBG_GEN;
import static jdk.internal.jshell.remote.RemoteCodes.CMD_CLASSPATH;
import static jdk.internal.jshell.remote.RemoteCodes.CMD_INVOKE;
import static jdk.internal.jshell.remote.RemoteCodes.CMD_LOAD;
import static jdk.internal.jshell.remote.RemoteCodes.CMD_VARVALUE;
import static jdk.internal.jshell.remote.RemoteCodes.RESULT_CORRALLED;
import static jdk.internal.jshell.remote.RemoteCodes.RESULT_EXCEPTION;
import static jdk.internal.jshell.remote.RemoteCodes.RESULT_FAIL;
import static jdk.internal.jshell.remote.RemoteCodes.RESULT_KILLED;
import static jdk.internal.jshell.remote.RemoteCodes.RESULT_SUCCESS;
import jdk.jshell.JShellException;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import jdk.jshell.EvalException;
import jdk.jshell.UnresolvedReferenceException;
import static java.util.stream.Collectors.toMap;
import static jdk.internal.jshell.remote.RemoteCodes.CMD_EXIT;

/**
 * Support class to assist implementing remoting {@link ExecutionControl} implementations.
 * The class implements serialization of {@link ExecutionControl} commands to the wire between
 * JShell and its agent. The wire is represented as {@link RemoteEnv} passed during to the constructor.
 * <p/>
 * The class tracks bytecode uploaded to the agent using {@link #load} and {@link #redefine} in order
 * to answer the {@link ExecutionControl#getClassStatus} call. Classes are identified by a custom type
 * T. The actual implementation details of obtaining class id and 
 * <p/>
 * This class intentionally implements methods with the same signature as {@link ExecutionControl}; the
 * {@link ExecutionControl} implementor can delegate to this support class for default handling.
 * 
 * @author sdedic
 */
public final class RemoteExecutionSupport<T> {
    
    /**
     * The connection to the JShell agent, possibly in a different VM. Commands
     * to the agent are serialized to {@link #getRemoteOut}, and ACks or responses 
     * are read from {@link #getRemoteIn}.
     */
    public interface RemoteEnv {
        /**
         * Returs stream to read command responses from the agent.
         * @return response stream
         */
        public ObjectInput          getRemoteIn();
        
        /**
         * Returns a stream where to write commands for the agent. The 
         * stream must be flushed at the end of command write and before
         * ExecutionControl implementation waits for agent's response.
         * @return command output stream.
         */
        public ObjectOutput         getRemoteOut();
        
        /**
         * Checks whether the connection is closed.
         * @return true, if connection to agent is closed or never opened.
         */
        public boolean              isClosed();
        
        /**
         * Initiates shutdown of the agent connection.
         */
        public void                 shutdown();
    }
    
    /**
     * Class handling callback. Translates class name to an ID (collection of IDs),
     * and allows to redefine a class based on the ID. IDs are not interpreted
     * in any way. 
     * @param <T> ID type
     */
    public interface ClassControl<T> {
        /**
         * Translates class name to a collection of IDs. The implementation should return
         * IDs for all classes matching the passed classname, which were loaded by the agent. If a class
         * has not been loaded, it's ID should not be returned. The method must return an
         * empty collection rather than {@code null}. In case of a failure, the method must return
         * an empty collection as well.
         * 
         * @param className class name
         * @return collection of IDs for matching definitions of class
         */
        public Collection<T>  nameToRef(String className);
        
        /**
         * Redefines classes. The passed map is keyed by class ID, which must have
         * been obtained previously by a call to {@link #nameToRef}. The implementation
         * should redefine the classes identified by map keys using class file bytes
         * stored in map values. The method returns true, if redefinition succeeded,
         * false otherwise.
         * 
         * @param bytes class contents to use as new definitions
         * @return if redefinition succeeds
         * @throws IOException if communication fails
         */
        public boolean        redefineClasses(Map<T, byte[]> bytes) throws IOException;
    }
    
    private final ClassTracker<T> tracker;

    private final ObjectOutput    remoteOut;
    private final ObjectInput     remoteIn;
    private final ExecutionEnv    execEnv;
    private final RemoteEnv          remoteEnv;
    private final ClassControl       classControl;

    /**
     * Constructs the support object
     * @param remoteEnv wire interface to the remote agent
     * @param execEnv JShell execution environment
     * @param classControl class manipulation interface
     * @param lock synchronization lock for state data
     */
    public RemoteExecutionSupport(
            RemoteEnv remoteEnv,
            ExecutionEnv execEnv,
            ClassControl<T> classControl,
            Object lock) {
        this.remoteOut = remoteEnv.getRemoteOut();
        this.remoteIn = remoteEnv.getRemoteIn();
        this.classControl = classControl;
        this.remoteEnv = remoteEnv;
        this.execEnv = execEnv;
        this.STOP_LOCK = lock;
        this.tracker =  new ClassTracker<T>(classControl::nameToRef);
    }
    
    public final ObjectOutput getRemoteOut() {
        return remoteOut;
    }
    
    public final ObjectInput getRemoteIn() {
        return remoteIn;
    }
    
    public void close() throws IOException {
        remoteOut.writeInt(CMD_EXIT);
        remoteOut.flush();
        this.remoteEnv.shutdown();
    }
    
    /**
     * Loads the list of classes specified. Sends a load command to the remote
     * agent with pairs of classname/bytes.
     *
     * @param classes the names of the wrapper classes to loaded
     * @return true if all classes loaded successfully
     */
    public boolean load(Collection<String> classes) {
        try {
            // Create corresponding ClassInfo instances to track the classes.
            // Each ClassInfo has the current class bytes associated with it.
            List<ClassTracker.ClassInfo> infos = withBytes(classes);
            // Send a load command to the remote agent.
            remoteOut.writeInt(CMD_LOAD);
            remoteOut.writeInt(classes.size());
            for (ClassTracker.ClassInfo ci : infos) {
                remoteOut.writeUTF(ci.getClassName());
                remoteOut.writeObject(ci.getBytes());
            }
            remoteOut.flush();
            // Retrieve and report results from the remote agent.
            boolean result = readAndReportResult();
            // For each class that now has a JDI ReferenceType, mark the bytes
            // as loaded.
            infos.stream()
                    .filter(ci -> ci.getReferenceTypeOrNull() != null)
                    .forEach(ci -> ci.markLoaded());
            return result;
        } catch (IOException ex) {
            debug(DBG_GEN, "IOException on remote load operation: %s\n", ex);
            return false;
        }
    }
    
    /**
     * Invoke the doit method on the specified class.
     *
     * @param classname name of the wrapper class whose doit should be invoked
     * @return return the result value of the doit
     * @throws JShellException if a user exception was thrown (EvalException) or
     * an unresolved reference was encountered (UnresolvedReferenceException)
     */
    public String invoke(String classname, String methodname) throws JShellException {
        try {
            synchronized (STOP_LOCK) {
                userCodeRunning = true;
            }
            // Send the invoke command to the remote agent.
            remoteOut.writeInt(CMD_INVOKE);
            remoteOut.writeUTF(classname);
            remoteOut.writeUTF(methodname);
            remoteOut.flush();
            // Retrieve and report results from the remote agent.
            if (readAndReportExecutionResult()) {
                String result = remoteIn.readUTF();
                return result;
            }
        } catch (IOException | RuntimeException ex) {
            if (!remoteEnv.isClosed()) {
                debug(DBG_GEN, "Exception on remote invoke: %s\n", ex);
                return "Execution failure: " + ex.getMessage();
            }
        } finally {
            synchronized (STOP_LOCK) {
                userCodeRunning = false;
            }
        }
        return "";
    }

    public final boolean isUserCodeRunning() {
        return userCodeRunning;
    }

    /**
     * Retrieves the value of a JShell variable.
     *
     * @param classname name of the wrapper class holding the variable
     * @param varname name of the variable
     * @return the value as a String
     */
    public String varValue(String classname, String varname) {
        try {
            // Send the variable-value command to the remote agent.
            remoteOut.writeInt(CMD_VARVALUE);
            remoteOut.writeUTF(classname);
            remoteOut.writeUTF(varname);
            remoteOut.flush();
            // Retrieve and report results from the remote agent.
            if (readAndReportResult()) {
                String result = remoteIn.readUTF();
                return result;
            }
        } catch (EOFException ex) {
            remoteEnv.shutdown();
        } catch (IOException ex) {
            debug(DBG_GEN, "Exception on remote var value: %s\n", ex);
            return "Execution failure: " + ex.getMessage();
        }
        return "";
    }

    /**
     * Adds a path to the remote classpath.
     *
     * @param cp the additional path element
     * @return true if succesful
     */
    public boolean addToClasspath(String cp) {
        try {
            // Send the classpath addition command to the remote agent.
            remoteOut.writeInt(CMD_CLASSPATH);
            remoteOut.writeUTF(cp);
            remoteOut.flush();
            // Retrieve and report results from the remote agent.
            return readAndReportResult();
        } catch (IOException ex) {
            throw new InternalError("Classpath addition failed: " + cp, ex);
        }
    }
    
    /**
     * Redefine the specified classes. Where 'redefine' is, as in JDI and JVMTI,
     * an in-place replacement of the classes (preserving class identity) --
     * that is, existing references to the class do not need to be recompiled.
     * This implementation uses JDI redefineClasses. It will be unsuccessful if
     * the signature of the class has changed (see the JDI spec). The
     * JShell-core is designed to adapt to unsuccessful redefine.
     *
     * @param classes the names of the classes to redefine
     * @return true if all the classes were redefined
     */
    public boolean redefine(Collection<String> classes) {
        try {
            // Create corresponding ClassInfo instances to track the classes.
            // Each ClassInfo has the current class bytes associated with it.
            List<ClassTracker.ClassInfo> infos = withBytes(classes);
            // Convert to the JDI ReferenceType to class bytes map form needed
            // by JDI.
            Map<? super T, byte[]> rmp = infos.stream()
                    .collect(toMap(
                            ci -> ci.getReferenceTypeOrNull(),
                            ci -> ci.getBytes()));
            // Attempt redefine.  Throws exceptions on failure.
            classControl.redefineClasses((Map<T, byte[]>)rmp);
            // Successful: mark the bytes as loaded.
            infos.stream()
                    .forEach(ci -> ci.markLoaded());
            return true;
        } catch (UnsupportedOperationException ex) {
            // A form of class transformation not supported by JDI
            return false;
        } catch (Exception ex) {
            debug(DBG_GEN, "Exception on JDI redefine: %s\n", ex);
            return false;
        }
    }

    /**
     * Converts a collection of class names into ClassInfo instances associated
     * with the most recently compiled class bytes.
     *
     * @param classes names of the classes
     * @return a list of corresponding ClassInfo instances
     */
    private List<ClassTracker.ClassInfo> withBytes(Collection<String> classes) {
        return classes.stream()
                .map(cn -> tracker.classInfo(cn, execEnv.getClassBytes(cn)))
                .collect(toList());
    }

    /**
     * Reports the status of the named class. UNKNOWN if not loaded. CURRENT if
     * the most recent successfully loaded/redefined bytes match the current
     * compiled bytes.
     *
     * @param classname the name of the class to test
     * @return the status
     */
    public ExecutionControl.ClassStatus getClassStatus(String classname) {
        ClassTracker.ClassInfo ci = tracker.get(classname);
        if (ci.getReferenceTypeOrNull() == null) {
            // If the class does not have a JDI ReferenceType it has not been loaded
            return ExecutionControl.ClassStatus.UNKNOWN;
        }
        // Compare successfully loaded with last compiled bytes.
        return (Arrays.equals(execEnv.getClassBytes(classname), ci.getLoadedBytes()))
                ? ExecutionControl.ClassStatus.CURRENT
                : ExecutionControl.ClassStatus.NOT_CURRENT;
    }

    /**
     * Reports results from a remote agent command that does not expect
     * exceptions.
     *
     * @return true if successful
     * @throws IOException if the connection has dropped
     */
    public boolean readAndReportResult() throws IOException {
        int ok = remoteIn.readInt();
        switch (ok) {
            case RESULT_SUCCESS:
                return true;
            case RESULT_FAIL: {
                String ex = remoteIn.readUTF();
                debug(DBG_GEN, "Exception on remote operation: %s\n", ex);
                return false;
            }
            default: {
                debug(DBG_GEN, "Bad remote result code: %s\n", ok);
                return false;
            }
        }
    }

    /**
     * Reports results from a remote agent command that expects runtime
     * exceptions.
     *
     * @return true if successful
     * @throws IOException if the connection has dropped
     * @throws EvalException if a user exception was encountered on invoke
     * @throws UnresolvedReferenceException if an unresolved reference was
     * encountered
     */
    public boolean readAndReportExecutionResult() throws IOException, JShellException {
        int ok = remoteIn.readInt();
        switch (ok) {
            case RESULT_SUCCESS:
                return true;
            case RESULT_FAIL: {
                // An internal error has occurred.
                String ex = remoteIn.readUTF();
                return false;
            }
            case RESULT_EXCEPTION: {
                // A user exception was encountered.
                String exceptionClassName = remoteIn.readUTF();
                String message = remoteIn.readUTF();
                StackTraceElement[] elems = readStackTrace();
                throw execEnv.createEvalException(message, exceptionClassName, elems);
            }
            case RESULT_CORRALLED: {
                // An unresolved reference was encountered.
                int id = remoteIn.readInt();
                StackTraceElement[] elems = readStackTrace();
                throw execEnv.createUnresolvedReferenceException(id, elems);
            }
            case RESULT_KILLED: {
                // Execution was aborted by the stop()
                debug(DBG_GEN, "Killed.");
                return false;
            }
            default: {
                debug(DBG_GEN, "Bad remote result code: %s\n", ok);
                return false;
            }
        }
    }

    private StackTraceElement[] readStackTrace() throws IOException {
        int elemCount = remoteIn.readInt();
        StackTraceElement[] elems = new StackTraceElement[elemCount];
        for (int i = 0; i < elemCount; ++i) {
            String className = remoteIn.readUTF();
            String methodName = remoteIn.readUTF();
            String fileName = remoteIn.readUTF();
            int line = remoteIn.readInt();
            elems[i] = new StackTraceElement(className, methodName, fileName, line);
        }
        return elems;
    }

    private final Object STOP_LOCK;
    private boolean userCodeRunning = false;

    void debug(int flags, String format, Object... args) {
        InternalDebugControl.debug(execEnv.state(), execEnv.userErr(), flags, format, args);
    }

    void debug(Exception ex, String where) {
        InternalDebugControl.debug(execEnv.state(), execEnv.userErr(), ex, where);
    }
    
    /**
     * Provides demultiplexer for the Agent's output. The method will demultiplex "out" and "err"
     * stream contents from the agent's output and write them into passed 'output' and 'error'
     * PrintStreams. These outputs will be read continuously and immediately printed to their
     * respective streams. If either of the PrintStreams is {@code null}, the streams content
     * will be discarded after reading.
     * <p/>
     * Agent's responses to commands can be read from the returned {@code InputStream}. Calling
     * {@link InputStream#close} on the returned stream will close and clean up the demultiplexer.
     * <p/>
     * As the agent may send data asynchronously, reading must be continuous. The reading task, which 
     * also does the demultiplexing will start using the supplied {@link Executor} instance.
     * @param in raw agent output
     * @param output user output, remoted {@link System#out}; may be {@code null}
     * @param error user error, remoted {@link System#err}; may be {@code null}
     * @param executeWith executor to run the demultiplexing task
     * @return InputStream containing just the agent response part
     */
    public static InputStream demultiplexResponseStream(
            InputStream in, PrintStream output, PrintStream error, Executor executeWith) {
        final DemultiplexInput demux[] = new DemultiplexInput[1];
        PipeInputStream pis = new PipeInputStream() {
            public void close() {
                demux[0].close();
            }
        };
        demux[0] = new DemultiplexInput(in, pis, output, error);
        executeWith.execute(demux[0]);
        return pis;
    }
}
