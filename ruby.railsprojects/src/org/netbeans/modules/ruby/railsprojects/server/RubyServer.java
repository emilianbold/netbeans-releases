/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby.railsprojects.server;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil.RailsVersion;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;

/**
 * An interface specific to Mongrel and WEBrick to facilitate 
 * their handling.
 * 
 * XXX: a work in progress 
 * 
 * @author Erno Mononen
 */
public interface RubyServer extends RubyInstance {

    /**
     * Gets the name for the node displayed in the services tab.
     * 
     * @return the name for the node.
     */
    String getNodeName();
    
    /**
     * Returns the location of this gem.  Helpful if jruby's load path needs
     * augmentation when starting the server.
     *
     * @return fully qualified path of gem folder or null if unknown / not required.
     */
    public String getLocation();
    
    /**
     * Gets the startup params for forcing an instance of this server
     * to started.
     * 
     * @return the startup params.
     */
    List<String> getStartupParams(RailsVersion version);

    /**
     * Prefix option for invoking server script if required.
     *
     * @return option for invoking server script, or null if not required.
     */
    String getScriptPrefix();

    /**
     * Gets the path to the startup script of this server.
     * @param project the project this server belongs to.
     * @return the path to the startup script.
     */
    String getServerPath(RailsVersion version);

    /**
     * Checks whether the given <code>outputLine</code> represented a startup
     * message of this server.
     * 
     * @param outputLine the line to check.
     * @return true if the given <code>outputLine</code> was a startup message
     * of this server, false otherwise.
     */
    boolean isStartupMsg(String outputLine);

    /**
     * @return the applications running on this server.
     */
    List<RailsApplication> getApplications();

    /**
     * Adds a new running application to this server. Must notify 
     * all registered listeners.
     * 
     * @param application the application to add.
     * @return true if adding was successful, false otherwise.
     */
    boolean addApplication(RailsApplication application);

    /**
     * Removes the application running on the given <code>port</code>. Must
     * notify all registered listeners.
     * 
     * @param port the port on which the given application runs.
     * @return true if removing the identified application was successful, false
     * otherwise.
     */
    boolean removeApplication(int port);

    /**
     * Registers a change listener to be notified when a new application has been 
     * started or a running application stopped on this server.
     * @param listener
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes the given <code>listener</code>. 
     * 
     * @param listener
     * 
     * @see addChangeListener(listener)
     */
    void removeChangeListener(ChangeListener listener);
}
