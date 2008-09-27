/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.common.dbgp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.tools.common.dbgp.Breakpoint.*;
import org.netbeans.modules.web.client.tools.common.dbgp.Eval.*;
import org.netbeans.modules.web.client.tools.common.dbgp.Property.*;
import org.netbeans.modules.web.client.tools.common.dbgp.Source.*;
import org.netbeans.modules.web.client.tools.common.dbgp.Stack.*;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jdeva
 */
public class DebuggerProxy {
    private volatile Socket sessionSocket;
    private String sessionID;
    private BlockingQueue<Message> suspensionPointQueue = new ArrayBlockingQueue<Message>(128);
    private BlockingQueue<Message> httpQueue = new ArrayBlockingQueue<Message>(200); //this queue may be a lot larger.
    private BlockingQueue<ResponseMessage> responseQueue = new ArrayBlockingQueue<ResponseMessage>(8);
    public static final List<DebuggerProxy> proxies = new CopyOnWriteArrayList<DebuggerProxy>();
    private CommandFactory commandFactory;
    private static final AtomicInteger transactionId = new AtomicInteger(0);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private volatile Thread messageHandlerThread;
    private volatile Thread messageSentThread;
    private List<Integer> ignoreIDs = new LinkedList<Integer>();

    public DebuggerProxy(Socket socket, String id)  {
        this.sessionSocket = socket;
        this.sessionID = id;
    }

    public synchronized boolean isSuspensionQueueActive() {
        return messageHandlerThread != null || suspensionPointQueue.size() > 0;
    }
    
    public synchronized boolean isHttpQueueActive() {
        return messageHandlerThread != null || httpQueue.size() > 0;
    }

    public void setBooleanFeature(Feature.Name featureName, boolean featureValue) {
        sendCommand(getCommandFactory().featureSetCommand(featureName, String.valueOf(featureValue)));
    }

    public void openURI(URI uri) throws URISyntaxException {
        sendCommand(getCommandFactory().openURICommand(uri));
    }

    private void cleanup() {
        try {
            sessionSocket.close();
        } catch (IOException ioe) {
            Log.getLogger().log(Level.SEVERE, "Cannot close socket", ioe);
        }
        proxies.remove(this);
        messageHandlerThread = null;
        sessionSocket = null;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    int getTransactionId() {
        return transactionId.getAndIncrement();
    }

    public boolean startDebugging() {
        boolean successfullyStarted = false;
        if(messageHandlerThread == null) {
            try {
                commandFactory = new CommandFactory(this);
                messageHandlerThread = new MessageHandler(sessionSocket.getInputStream(), sessionID);    //NOI18N
                messageHandlerThread.start();
                successfullyStarted = true;
            } catch (IOException ioe) {
                Log.getLogger().log(Level.SEVERE, "Cannot get socket's input stream", ioe); //NOI18N
            }
        }
        return successfullyStarted;
    }

    public boolean stopDebugging() {
        boolean successfullyStopped = false;
        if(messageHandlerThread != null) {
            stop.set(true);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    sendCommand(commandFactory.stopCommand());
                }
            });
            
            sendStopMessage(statusTextUser);
            successfullyStopped = true;
        }
        return successfullyStopped;
    }

    public void run() {
        sendCommand(getCommandFactory().runCommand());
    }
    
    public void pause() {
        sendCommand(getCommandFactory().pauseCommand());
    }    

    public void stepInto() {
        sendCommand(getCommandFactory().stepIntoCommand());
    }

    public void stepOver() {
        sendCommand(getCommandFactory().stepOverCommand());
    }

    public void stepOut() {
        sendCommand(getCommandFactory().stepOutCommand());
    }

    public void runToCursor(BreakpointSetCommand breakpointSetCommand) {
        //Set temporary breakpoint, extension will do the resume for this request
        setBreakpoint(breakpointSetCommand);
    }

    public String setBreakpoint(BreakpointSetCommand breakpointSetCommand) {
        BreakpointSetResponse response = (BreakpointSetResponse) sendCommand(breakpointSetCommand);
        return response != null ? response.getId() : null;
    }

    public boolean removeBreakpoint(String id) {
        BreakpointRemoveResponse response = (BreakpointRemoveResponse)
                sendCommand(commandFactory.breakpointRemoveCommand(id));
        return response != null ? true : false;
    }
    
    public boolean updateBreakpoint(String id, Boolean state, int line, int hitValue, HIT_COUNT_FILTERING_STYLE hitCondition, String condition) {
        BreakpointUpdateCommand command = commandFactory.breakpointUpdateCommand(id);
        if(state != null) {
            command.setState(state.booleanValue());
        }
        if(line != -1) {
            command.setLineNumber(line);
        }
        if(hitValue != -1) {
            command.setHitValue(hitValue);
        }
        if(hitCondition != null) {
            command.setHitCondition(hitCondition.name());
        }
        if(condition != null) {
            command.setCondition(condition);
        }
        BreakpointUpdateResponse response = (BreakpointUpdateResponse) sendCommand(command);
        return response != null ? true : false;
    }

    public List<Breakpoint> getBreakpoints(List<String> breakpointIds) {
        List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
        for(String breakpointId: breakpointIds) {
            Breakpoint bp = getBreakpoint(breakpointId);
            if (bp != null) {
                breakpoints.add(bp);
            }
        }
        return breakpoints;
    }

    public Breakpoint getBreakpoint(String breakpointId) {
        BreakpointGetResponse response = (BreakpointGetResponse)
                sendCommand(getCommandFactory().breakpointGetCommand(breakpointId));
        return response != null ? response.getBreakpoint() : null;
    }

    public List<Breakpoint> getBreakpoints() {
        BreakpointListResponse response = (BreakpointListResponse)
                sendCommand(getCommandFactory().breakpointListCommand());
        return response != null ? response.getBreakpoints() : null;
    }

    public byte[] getSource(String uri, boolean stripBeginCharacter) {
        SourceResponse response = (SourceResponse) sendCommand(getCommandFactory().sourceCommand(uri));
        return (response != null && response.isSusccess()) ? response.getSourceCode(stripBeginCharacter) : null;
    }

    public Message getSuspensionPoint() {
        try {
            return suspensionPointQueue.take();
        } catch (InterruptedException ie) {
            Log.getLogger().log(Level.FINEST, "Interrrupted while waiting for suspension point", ie);
        }
        return null;
    }
    
    public Message getHttpMessage() {
        try {
            return httpQueue.take();
        } catch (InterruptedException ie) {
            Log.getLogger().log(Level.FINEST, "Interrrupted while waiting for http message", ie);
        }
        return null;
    }

    public Stack getCallStack(int depth){
        StackGetResponse response = (StackGetResponse) sendCommand(getCommandFactory().stackGetCommand(depth));
        return response != null ? response.getStackElements().get(0) : null;
    }

    public List<Stack> getCallStacks(){
        StackGetResponse response = (StackGetResponse) sendCommand(getCommandFactory().stackGetCommand(-1));
        return response != null ? response.getStackElements() : null;
    }
    
    public Property getProperty(String name, int stackDepth) {
        PropertyGetResponse response = (PropertyGetResponse)sendCommand(
                getCommandFactory().propertyGetCommand(name, stackDepth));
        return response != null ? response.getProperty() : null;
    }
    
    public boolean setProperty(String name, String value, int stackDepth) {
        PropertySetResponse response = (PropertySetResponse)sendCommand(
                getCommandFactory().propertySetCommand(name, value, stackDepth));
        return response != null ? response.isSet() : false;
    }    
    
    public Property eval(String data, int stackDepth) {
        EvalResponse response = (EvalResponse)sendCommand(
                getCommandFactory().evalCommand(data, stackDepth));
        return (response != null && response.isSuccess()) ? response.getProperty() : null;
    }        

    public synchronized ResponseMessage sendCommand(Command command) {
        if(sessionSocket == null) {
            Log.getLogger().log(Level.FINE, "Cannot send command after session is closed: " + command.getCommandName());
            
            return null;
        } else if (stop.get() && !(command instanceof Continue.StopCommand)) {
            Log.getLogger().log(Level.INFO, "Ignored command after session is closed: " + command.getCommandName());
            return null;
        }
        try {
            messageSentThread = Thread.currentThread();
            
            command.send(sessionSocket.getOutputStream());
            if (command.wantAcknowledgment()) {
                Message message = responseQueue.poll(20, TimeUnit.SECONDS);
                //Synchronize to ensure timed out response is ignored
                synchronized(ignoreIDs) {                
                    if (message instanceof ResponseMessage) {
                        ResponseMessage response = (ResponseMessage) message;
                        assert (response.getTransactionId() == command.getTransactionId());

                        if (message instanceof RuntimeErrorResponse) {
                            Log.getLogger().log(Level.WARNING, "Unexpected debugger extension error: " + 
                                    ((RuntimeErrorResponse)message).getMessage());
                            return null;
                        }

                        return response;
                    }
                    Log.getLogger().log(Level.FINE, command.getCommandName() + " request timed-out");  //NOI18N
                    //Track the id of the timed-out request to ignore the corresponding response
                    ignoreIDs.add(command.getTransactionId());
                }
            }
        } catch (SocketException se) {
            Log.getLogger().log(Level.WARNING, se.getMessage(), se);
            fireStoppedEvent();
        } catch (IOException ioe) {
            Log.getLogger().log(Level.SEVERE, ioe.getMessage(), ioe);
        } catch (InterruptedException ie) {
            Log.getLogger().log(Level.FINE, "Interrrupted while waiting for response", ie);     //NOI18N
        } finally {
            messageSentThread = null;
        }
        return null;
    }

    private void handleMessage(Message message) {
        if (message instanceof ResponseMessage) {
            ResponseMessage responseMessage = (ResponseMessage)message;
            int txID = responseMessage.getTransactionId();
            if( txID == -1) {
                suspensionPointQueue.add(message);
            }else {
                //Synchronize to ensure timed out response is ignored
                synchronized(ignoreIDs) {
                    //Ignore if the response is for a timed-out request
                    if(ignoreIDs.size() > 0) {
                        int index = ignoreIDs.indexOf(txID);
                        if(index != -1) {
                            ignoreIDs.remove(index);
                            return;
                        }
                    }
                    responseQueue.add((ResponseMessage) message);
                }
            }
        } else if (message instanceof InitMessage ||
                   message instanceof OnloadMessage ||
                   message instanceof SourcesMessage ||
                   message instanceof WindowsMessage ||
                   message instanceof StreamMessage) {
            suspensionPointQueue.add(message);
        } else if ( message instanceof HttpMessage ) {
//            Log.getLogger().info("Receiving HttpMessage Id:" + ((HttpMessage)message).getId());
            httpQueue.add(message);
        }
    }
    
    private static final String statusTextException = "<response command=\"status\" status=\"stopped\" reason=\"exception\"/>";
    private static final String statusTextUser = "<response command=\"status\" status=\"stopped\" reason=\"ok\"/>";
    
    private void fireStoppedEvent() {
        if(messageSentThread != null) {
            messageSentThread.interrupt();
        }
        
        if (stop.get()) {
            sendStopMessage(statusTextUser);
        }else {
            sendStopMessage(statusTextException);
        }
    } 
    
    private void sendStopMessage(String msg) {
        Message message = Message.createMessage(msg);
        boolean addedToSuspensionQueue = suspensionPointQueue.offer(message);
        boolean addedToHttpQueue = httpQueue.offer(message);

        if (!addedToSuspensionQueue) {
            Log.getLogger().log(Level.INFO, "Could not send terminate message to Suspension Point Handler");
        }

        if (!addedToHttpQueue) {
            Log.getLogger().log(Level.INFO, "Could not send terminate message to HTTP Message Handler");
        }
    }

    private class MessageHandler extends Thread {
        private final InputStream is;

        MessageHandler(final InputStream is, String id) {
            super("Dbgp message handler " + id);  //NOI18N
            this.setDaemon(true);
            this.is = is;
        }

        @Override
        public void run() {
            Log.getLogger().log(Level.FINEST, "Starting " + getName());                      //NOI18N
            try {
                while (!stop.get()) {
                    handleMessage(Message.create(is));
                }
            } catch(IOException ioe) {
                Log.getLogger().log(Level.FINE, getName() + " stopping because of exception", ioe);   //NOI18N
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.getLogger().log(Level.SEVERE, "Cannot close socket's input stream", e); //NOI18N
                }
            }
            
            DebuggerProxy.this.cleanup();
            fireStoppedEvent();
            
            Log.getLogger().log(Level.FINEST, "Ending " + getName());  //NOI18N
        }
    }
}
