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
import org.netbeans.api.ruby.platform.RubyPlatform;

/** 
 * ROUGH DRAFT
 * 
 * Ruby Server API
 * 
 * An implementation of this interface provides the ability to start, stop, and
 * administer a server instance offering Ruby runtime capabilities to the NetBeans
 * IDE.
 * 
 * TODO Some apis (marked below) should be asynchronous and should return a monitor object
 * of some type.  FutureTask<T> is an interesting candidate, but there does not
 * appear to be a way to return status messages as the action progresses.  Maybe
 * a derived class of FutureTask in common server SPI
 * 
 * TODO Some enumerated types are needed (ServerState, OperationState).  Common Server
 * SPI candidates?  Could simplify to static strings/ints in this interface (cheezy, IMO)
 * 
 *
 * @author Peter Williams
 */
public interface RubyInstance {

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
     * TODO What is this for and what does this api return?
     * 
     * !PW FIXME Suggested by Erno, please fill in.
     * 
     * @return FIXME describe return value.
     */
//    public String getServerType();
    
    
    /**
     * Returns current state of server (starting, running, stopping, stopped, etc.)
     * 
     * TODO Define and use enumerated type for server states.  See o.n.spi.glassfish.ServerState
     * in glassfish.common [V3] module for an example.
     * 
     * TODO Should ServerState be defined in common server SPI?  Is it really API?
     * 
     * @return current state of server (TODO change type to enumeration)
     */
    public String getServerState();
    
    
    /**
     * Start the server using the given <code>platform</code>, or if a 
     * <code>null</code> was passed, using the "default" platform of the server.
     * 
     * TODO - this method should execute asynchronously, returning a FutureTask<T>
     * instance or similar to monitor and retrieve progress and status of the
     * startup process.
     * 
     * @param platform the platform to use or <code>null</code>.
     * 
     * @return true if startup succeeded and server is running, false otherwise.
     */
    public boolean startServer(RubyPlatform platform);

    
    /**
     * Stop the server
     * 
     * TODO - this method should execute asynchronously, returning a FutureTask<T>
     * instance or similar to monitor and retrieve progress and status of the
     * shutdown process.
     * 
     * @return true if server successfully stopped (or was already stopped), false
     * if server is still running or otherwise unresponsive.
     */
    public boolean stopServer();

    
    /**
     * Deploy the ruby application in the specified folder to the server.
     * 
     * TODO Should rails be assumed here?  How will we handle alternative web
     * frameworks?  They should not require additional api methods (though they
     * may require additional parameters).
     * 
     * TODO Should this api be asynchronous?  Directory deployment, especially for
     * Ruby/Rails apps should be very fast, except possibly in the case where
     * the server is recently started and JRuby has not yet been initialized -- 
     * that case may take up to 10 seconds...
     * 
     * @param applicationName name to assign to deployed application (defaults
     *   to directory or jar name if null or empty).
     * @param applicationDir directory containing Ruby (Rails?) application.
     * 
     * @return true if the application was successfully deployed, false otherwise.
     * TODO Need better error reporting for this result, asynchronous or not.
     */
    public boolean deploy(String applicationName, File applicationDir);
    
    
    /**
     * Stops the application at the specified location, if it is currently running
     * (e.g. servicing requests).
     * 
     * @param applicationName name assigned to previously deployed application.
     *   Use directory or jar name if no application name was originally specified.
     * 
     * @return true if the application was stopped (or was not running), false otherwise.
     */
    public boolean stop(String applicationName);
    

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
}
