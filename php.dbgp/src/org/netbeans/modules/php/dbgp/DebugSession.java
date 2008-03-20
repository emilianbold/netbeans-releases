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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.dbgp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.api.SessionId;
import org.netbeans.modules.php.dbgp.models.AbstractIDEBridge;
import org.netbeans.modules.php.dbgp.packets.DbgpCommand;
import org.netbeans.modules.php.dbgp.packets.DbgpMessage;
import org.netbeans.modules.php.dbgp.packets.DbgpResponse;
import org.netbeans.modules.php.dbgp.packets.InitMessage;


/**
 * @author ads
 *
 */
public class DebugSession implements Runnable {

    private static final int SLEEP_TIME = 100;

    DebugSession( Socket socket ){
        mySocket = socket;
        isStopped = new AtomicBoolean( false );
        myCommands = new LinkedList<DbgpCommand>();
        mySessionId = new AtomicReference<SessionId>();
        myBridge = new IDESessionBridge();
        myFileName = new AtomicReference<String>();
        myEngine = new AtomicReference<DebuggerEngine>();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        setSessionThread( Thread.currentThread() );
        boolean moreCommands = false;
        synchronized ( myCommands ){
            moreCommands = myCommands.size() > 0;
        }
        while( !isStopped.get() || moreCommands ){
            try { 
                sendCommands();
                receiveData();
                sleepTillNewCommand();
            }
            catch ( IOException e ){
                log( e );
            }
        }
        setSessionThread( null );
        try {
            getSocket().close();
        }
        catch (IOException e) {
            log( e);
        }
    }
    
    public void sendCommandLater( DbgpCommand command ){
        synchronized ( this ) {
            /*
             *  Do not collect command before session is not initialized.
             *  So any command before Init message will not be sent.
             *  ( F.e. commands for getting watch values will be just ignored
             *  if they was requested before Init message ).
             */
            if ( getSessionId() == null ){
                return;
            }
            if ( getSessionThread() == null ){
                return;
            }
            addCommand(command);
            getSessionThread().interrupt();
        }
    }
    
    public DbgpResponse sendSynchronCommand( DbgpCommand command ) {
        if ( getSessionThread() != Thread.currentThread() ) {
            throw new IllegalStateException("Method incorrect usage. " +
                    "It should be called in handler thread only");  // NOI18N
        }
        try {
            command.send( getSocket().getOutputStream() );
            if ( command.wantAcknowledgment() ) {
                DbgpMessage message = receiveData( command );
                if ( message instanceof DbgpResponse ) {
                    return (DbgpResponse) message;
                }
            }
        }
        catch (IOException e) {
            log(e);
        }
        return null;
    }
    
    public String getTransactionId() {
        return myTransactionId.getAndIncrement() +"";
    }
    
    public void stop() {
        isStopped.set( true );
        getBridge().setSuspended( false );
        getBridge().hideAnnotations();
        StartActionProviderImpl.getInstance().removeSession( this );
        getBridge().getBreakpointModel().setCurrentStack( null , this);
        getBridge().getCallStackModel().clearModel();
        getBridge().getThreadsModel().update();
        getBridge().getVariablesModel().clearModel();
        getBridge().getWatchesModel().clearModel();
        Session session = (Session) getBridge().getEngine().lookupFirst(null, Session.class);
        StartActionProviderImpl.getInstance().stop(session);
    }

    public void setId( InitMessage message ) {
        setSessionFile( message.getFileUri() );
        String sessionId = message.getSessionId();
        DebuggerEngine[] engines = 
            DebuggerManager.getDebuggerManager().getDebuggerEngines();
        for (DebuggerEngine engine : engines) {
            SessionId id = (SessionId)engine.lookupFirst( null , SessionId.class );
            if ( id.getId().equals( sessionId ) ) {
                mySessionId.set( id );
                id.setFileUri( message.getFileUri() );
                myEngine.set(engine);
            }
        }
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            SessionId id = (SessionId)session.lookupFirst( null , SessionId.class );
            if ( id.getId().equals( sessionId ) ) {
                StartActionProviderImpl.getInstance().attachDebugSession( session ,
                        this );
            }
        }
    }

    public SessionId getSessionId() {
        return mySessionId.get();
    }
    
    public IDESessionBridge getBridge() {
        return myBridge;
    }
    
    public String getFileName() {
        return myFileName.get();
    }
    
    private void setSessionFile( String fileName ) {
        myFileName.set( fileName );
    }
    
    private void sleepTillNewCommand() {
        try {
            // Wake up every 100 milliseconds and see if the debuggee has something to say.
            // The IDE side can interrupt the sleep to send new packets to the
            // debugger.
            Thread.sleep(SLEEP_TIME); 
        }
        catch (InterruptedException ie) {
            // OK, run the look again.
        }
    }
    
    private synchronized void setSessionThread( Thread thread ){
        mySessionThread = thread;
    }

    private void sendCommands() throws IOException {
        List<DbgpCommand> list;
        synchronized ( myCommands ) {
            list = new ArrayList<DbgpCommand>( myCommands );
            myCommands.clear();
        }
        for (DbgpCommand command : list) {
            command.send( getSocket().getOutputStream() );
            if ( command.wantAcknowledgment() ) {
                receiveData( command );
            }
        }
    }
    
    
    private void addCommand( DbgpCommand command ){
        synchronized ( myCommands ) {
            myCommands.add( command );
        }
    }
    
    private Thread getSessionThread(){
        return mySessionThread;
    }

    private void receiveData() throws IOException{
        receiveData( null );
    }
    
    private DbgpMessage receiveData( DbgpCommand command ) throws IOException{
        if ( command!= null || getSocket().getInputStream().available() > 0) {
            DbgpMessage message = DbgpMessage.create( 
                    getSocket().getInputStream() );
            handleMessage(command, message);
            return message;
        }
        return null;
    }

    private void handleMessage( DbgpCommand command, DbgpMessage message ) 
        throws IOException  
    {
        if ( message == null ) {
            return;
        }
        
        if ( command == null ) {
            // this is case when we don't need achnowl-t
            message.process(this, null );
            return;
        }
        
        boolean awaited = false;
        if ( message instanceof DbgpResponse ) {    
            DbgpResponse response = (DbgpResponse) message;
            String id = response.getTransactionId();
            if ( id.equals( command.getTransactionId())) {
                awaited = true;
                message.process(this, command);
            }
        }
        if ( !awaited ) {
            message.process(this, null );
            receiveData( command );
        }
    }
    
    private Socket getSocket(){
        return mySocket;
    }
    
    private void log( IOException e ) {
        Logger.getLogger( DebugSession.class.getName() ).log( 
                Level.SEVERE, null, e );
    }
    
    /*
     * This class is associated  with DebugSession but is intended for
     * cooperation with IDE UI. 
     */
    public class IDESessionBridge extends AbstractIDEBridge {

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.dbgp.models.AbstractIDEBridge#getEngine()
         */
        @Override
        protected DebuggerEngine getEngine()
        {
            return myEngine.get();
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.php.dbgp.models.AbstractIDEBridge#getDebugSession()
         */
        protected DebugSession getDebugSession(){
            return DebugSession.this;
        }

    }
    
    private Socket mySocket;
    
    private AtomicBoolean isStopped;
    
    private Thread mySessionThread;
    
    private List<DbgpCommand> myCommands;
    
    private AtomicReference<SessionId> mySessionId;
    
    private AtomicReference<DebuggerEngine> myEngine;
    
    private static final AtomicInteger myTransactionId = new AtomicInteger( 0 );
    
    private final IDESessionBridge myBridge;
    
    private AtomicReference<String> myFileName;
    
}
