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
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.api.SessionId;
import org.netbeans.modules.php.dbgp.api.StartActionProvider;
import org.netbeans.modules.php.dbgp.packets.StatusCommand;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
public class StartActionProviderImpl  implements StartActionProvider
{

    private static final String LOCALHOST   = "localhost";          // NOI18N

    private static final int DEFAULT_PORT   = 9000; 
    
    private static final int PORT_RANGE     = 100;
    
    private static final int TIMEOUT        = 60000;
    
    private static final String PORT_OCCUPIED = "MSG_PortOccupied"; // NOI18N
    
    private StartActionProviderImpl ( ){
        mySessions = new HashSet<DebugSession>();
        myCurrentSessions = new WeakHashMap<Session, DebugSession>();
    }
    
    public static StartActionProviderImpl getInstance(){
        return INSTANCE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.StartActionProvider#start()
     */
    public synchronized void start( ) {
        if ( myThread == null ){
            /*
             *  TODO : port may be red from options, found free port via 
             *  #findFreePort(), suggest to user via option about free port.
             */
            int port = DEFAULT_PORT;
            myThread = new ServerThread( port );
            RequestProcessor.getDefault().post( myThread );
        }
        else {
            /*
             *  Case stopping thread ( situation when debug session was 
             *  started right after previous stopping ).
             */ 
            if ( myThread.isStopped() ){
                /*
                 *  Not accurate stop accepting from other thread.
                 *  But otherwise one need to wait TIMEOUT seconds 
                 *  for stopping listening thread.
                 */
                myThread.closeSocket();
                myThread = null;
                start();
            }
        }
    }
    
    public synchronized DebugSession getSessionById( String id ) {
        for( DebugSession session : mySessions ) {
            SessionId sessId = session.getSessionId();
            if ( sessId == null ) {
                continue;
            }
            String curId = sessId.getId();
            if ( id.equals( curId )){
                return session;
            }
        }
        return null;
    }
    
    public synchronized DebugSession getCurrentSession( SessionId id ){
        if ( id == null ) {
            return null;
        }
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            SessionId sessId = (SessionId)
                    session.lookupFirst( null , SessionId.class);
            if ( id.equals(sessId) ) {
                return myCurrentSessions.get( session );
            }
        }
        return null;
    }
    
    public synchronized Collection<DebugSession> getSessions( SessionId id ){
        List<DebugSession> result = new LinkedList<DebugSession>();
        for( DebugSession session : mySessions ) {
            if ( id.equals( session.getSessionId() )){
                result.add( session );
            }
        }
        return result;
    }
    
    public synchronized void stop( Session session ) {
        SessionId id = (SessionId)session.lookupFirst( null , SessionId.class );
        List<DebugSession> list = new ArrayList<DebugSession>( mySessions);
        for( DebugSession debSess : list) {
            if ( debSess.getSessionId() == id ) {
                debSess.stop();
                mySessions.remove(debSess);
            }
        }
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        boolean last = true;
        for (Session sess : sessions) {
            if ( sess.equals(session )) {
                continue;
            }
            if ( sess.lookupFirst( null , SessionId.class )!= null ) {
                last = false;
            }
        }
        if ( last ) {
            myThread.stop();
        }

        stopEngines( session );
    }
    
    public synchronized void setCurrentSession( Session session , 
            DebugSession debugSession )
    {
        myCurrentSessions.put( session, debugSession );
    }

    synchronized void attachDebugSession( Session session, 
            DebugSession debugSession )
    {
        myCurrentSessions.put(session, debugSession);
        debugSession.getBridge().hideAnnotations();
        debugSession.getBridge().setSuspended(false);
        debugSession.getBridge().getThreadsModel().update();
    }
    
    synchronized void removeSession( DebugSession session ){
        Session sess = (Session)
            session.getBridge().getEngine().lookupFirst(null, Session.class );
        SessionId id = session.getSessionId();
        mySessions.remove( session );
        if ( id!= null ){
            Collection<DebugSession> collection = getSessions(id);
            if ( collection.size() >0 ){
                DebugSession debugSession = collection.iterator().next();
                setCurrentSession(sess, debugSession);
                StatusCommand command = new StatusCommand( 
                        debugSession.getTransactionId() );
                debugSession.sendCommandLater(command);
            }
        }
    }
    
    private synchronized void setupCurrentSession( DebugSession session ){
        mySessions.add( session );
    }
    
    private void stopEngines( Session session ) {
        String[] languages = session.getSupportedLanguages();
        for (String language : languages) {
            DebuggerEngine engine = session.getEngineForLanguage(language);
            ((DbgpEngineProvider)engine.lookupFirst(null, 
                    DebuggerEngineProvider.class)).getDestructor().killEngine();
        }
                
    }
    
    private int findFreePort() {
        for (int port = DEFAULT_PORT ; port < DEFAULT_PORT + PORT_RANGE; port++) {
            Socket testClient = null;
            
            try {
                /*
                 *  Try to connect at localhost with spcified port and check if 
                 *  it is possible. If it is possible then port is listened 
                 *  by some server
                 */ 
                testClient = new Socket(LOCALHOST, port);
            }
            catch (ConnectException ce) {
                // connection failed , so port is not listened by anyone, return it. 
                return port;
            }
            catch (IOException e) {
                // just ignore
            }
            finally {
                closeTestSocket(testClient);
            }
        }

        return -1;
    }

    private void closeTestSocket( Socket testClient ) {
        if (testClient != null) {
            // something listened on that socket. It's not useful for us.
            try {
                testClient.close();
            }
            catch (IOException ioe) {
                // We don't care here.
            }
        }
    }
    
    private ServerThread myThread;
    
    private Set<DebugSession> mySessions;
    
    private Map<Session,DebugSession> myCurrentSessions;
    
    private static final StartActionProviderImpl INSTANCE = 
        new StartActionProviderImpl();
    
    private class ServerThread implements Runnable {
        
        ServerThread( int port ){
            myPort = port;
            isStopped  = new AtomicBoolean( false );
        }

        public void run() {
            if ( !createServer() ) {
                return;
            }
            while( !isStopped()){
                Socket sessionSocket = null;
                
                try {
                    sessionSocket = myServer.accept();
                }
                catch ( SocketException e ){
                    /*
                     *  This can be result of inaccurate closing socket from
                     *  other thread. Just log with inforamtion severity. 
                     */  
                    logInforamtion(e);
                }
                catch( SocketTimeoutException e ){
                    // skip this exception, it's normal
                }
                catch( IOException e ){
                    log( e );
                }
                if (!isStopped.get() && sessionSocket != null) {
                    DebugSession session = 
                        new DebugSession( sessionSocket );
                    RequestProcessor.getDefault().post( session );
                    setupCurrentSession( session );
                }
            }
            
            closeSocket();
        }

        private void log( Exception exception ){
            Logger.getLogger( StartActionProviderImpl.class.getName() ).log( 
                    Level.FINE, null, exception );
        }
        
        private void logInforamtion( SocketException e ){
            Logger.getLogger( StartActionProviderImpl.class.getName() ).log( 
                    Level.FINE, null, e );
        }
        
        private boolean createServer() {
            synchronized (StartActionProviderImpl.this) {
                try {
                    myServer = new ServerSocket(myPort);
                    myServer.setSoTimeout(TIMEOUT);
                } catch (IOException e) {
                    String mesg = NbBundle.getMessage(
                            StartActionProviderImpl.class, PORT_OCCUPIED);
                    mesg = MessageFormat.format(mesg, myPort);
                    NotifyDescriptor descriptor =
                            new NotifyDescriptor.Message(mesg,
                            NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(descriptor);
                    log(e);
                    return false;
                }
                return true;
            }
        }
        
        private void closeSocket() {
            synchronized(StartActionProviderImpl.this) {
                if ( myServer == null ){
                    return;
                }
                try {
                    if (!myServer.isClosed()) {
                        myServer.close();
                    }
                }
                catch (IOException e) {
                    log(e);
                }
            }
        }
        
        private void stop(){
            isStopped.set( true );
        }
        
        private boolean isStopped(){
            return isStopped.get();
        }
        
        private int myPort;
        
        private ServerSocket myServer;
        
        private AtomicBoolean isStopped;
        
    }

}
