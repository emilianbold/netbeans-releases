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

package org.netbeans.modules.ruby.railsprojects.server.spi;

import java.io.File;
import java.util.concurrent.Future;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;


/** 
 * Ruby Server API
 * 
 * An implementation of this interface provides the ability to start, stop, 
 * and administer a server instance offering Ruby runtime capabilities to the 
 * NetBeans IDE.
 * 
 * @author Peter Williams
 */
public interface RubyInstance {

    /**
     * Enum for the current state of the server (stopped, running, etc.)
     */
    public static enum ServerState { 
        STOPPED,    // Server is not running or otherwise unavailable.
        STARTING,   // Server is in the process of starting up.
        RUNNING,    // Server is running and available.
        STOPPING    // Server is in the process of stopping.
    };
    
    /**
     * Enum for the current state of a server operation (e.g start, stop, deploy)
     */
    public static enum OperationState {
        RUNNING,    // Operation is running.
        COMPLETED,  // Operation completed successfully.
        FAILED      // Operation failed.
    }
    
    /**
     * Immutable object combining an operation state with a useful message about
     * how the operation ended up in that state, e.g. failure message, running
     * server greeting, etc.
     */
    public static class OperationResult {
        
        public final OperationState operationState;
        public final ServerState serverState;
        public final String message;
        
        /**
         * Constructor for an OperationResult object describing the current
         * status of an operation.
         * 
         * @param operationState Status of operation for this result.
         * @param serverState Server state at the time of this result.
         * @param message Message describing result, e.g. failure message, etc.
         */
        public OperationResult(final OperationState operationState, 
                final ServerState serverState, final String message) {
            this.operationState = operationState;
            this.serverState = serverState;
            this.message = message;
        }
    }
    
    /**
     * Get the URI that uniquely identifies this Ruby Server Instance
     * 
     * @return URI for this ruby server instance.
     */
    public String getServerUri();
    
    
    /**
     * Get a user displayable name for this server instance.
     * 
     * @return The name of this server instance, in user displayable form.
     */
    public String getDisplayName();
    
    
    /**
     * Returns current state of server (starting, running, stopping, stopped, etc.)
     * 
     * The implementation of this method must be threadsafe.
     * 
     * TODO Should ServerState be defined in common server SPI?  Is it really API?
     * 
     * @return Current state of server (starting, running, stopped, etc.)
     */
    public ServerState getServerState();
    
    
    /**
     * Start the server using the given <code>platform</code>, or if a 
     * <code>null</code> was passed, using the "default" platform of the server.
     * 
     * @param platform the platform to use or <code>null</code>.
     * 
     * @return true if startup succeeded and server is running, false otherwise.
     */
    public Future<OperationState> startServer(RubyPlatform platform);

    
    /**
     * Stop the server
     * 
     * @return true if server successfully stopped (or was already stopped), false
     * if server is still running or otherwise unresponsive.
     */
    public Future<OperationState> stopServer();

    
    /**
     * Deploy the ruby application in the specified folder to the server.
     * 
     * TODO Should rails be assumed here?  How will we handle alternative web
     * frameworks?  They should not require additional api methods (though they
     * may require additional parameters).
     * 
     * TODO Need better error reporting for this result, asynchronous or not.
     * Perhaps use Future<OperationResult> which combines the operation state
     * with a message.
     * 
     * @param applicationName name to assign to deployed application (defaults
     *   to directory or jar name if null or empty).
     * @param applicationDir directory containing Ruby (Rails?) application.
     * 
     * @return true if the application was successfully deployed, false otherwise.
     */
    public Future<OperationState> deploy(String applicationName, File applicationDir);
    
    
    /**
     * Stops the application at the specified location, if it is currently running
     * (e.g. servicing requests).
     * 
     * @param applicationName name assigned to previously deployed application.
     *   Use directory or jar name if no application name was originally specified.
     * 
     * @return true if the application was stopped (or was not running), false otherwise.
     */
    public Future<OperationState> stop(String applicationName);
    

    /**
     * Executes the application against this server.  Starts or restarts server
     * if necessary, deploys application to server and enables it.
     * 
     * @param platform the platform to use or <code>null</code>.
     * @param applicationName name to assign to deployed application (defaults
     *   to directory or jar name if null or empty).
     * @param applicationDir directory containing Ruby (Rails?) application.
     * 
     * @return true if server was successfully started (if necessary) and if 
     *   application was successfully deployed and executed.
     */
    public Future<OperationState> runApplication(RubyPlatform platform, 
            String applicationName, File applicationDir);
    
    /**
     * Checks whether the given <code>platform</code> is supported by 
     * this instance. In other words, checks whether this instance can
     * be used for running applications with the given <code>platform</code>.
     * 
     * @param platform the platform to check; must not be null.
     * @return true if the given <code>platform</code> is supported by this
     * instances, false otherwise.
     */
    public boolean isPlatformSupported(RubyPlatform platform);
    
    /**
     * Add a change listener to receive changes in server state from this server 
     * instance.  Listener implementation can call getServerState() to determine
     * the current state.
     * 
     * @param listener Listener to receive change events whenever the running
     * state of this server instance changes.
     */
    public void addChangeListener(ChangeListener listener);
    
    /**
     * Remove a change listener previously added to this server instance.
     * 
     * @param listener Listener that was being notified of state change events
     * from this server instance.
     */
    public void removeChangeListener(ChangeListener listener);
    
    /**
     * Returns the context path for the specified application on this server.
     * 
     * @return the context path for the specified application.
     */
    public String getContextRoot(String applicationName);
    
    /**
     * The http port used for accessing rails apps for this server.
     * 
     * @return http port for rails on this server.
     */
    public int getRailsPort();

}
