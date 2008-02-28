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

package org.netbeans.modules.ruby.railsprojects.database;

import org.netbeans.modules.ruby.railsprojects.RailsProject;

/**
 * Handles the database configuration for a Rails project.
 *
 * @author Erno Mononen
 */
public interface RailsDatabaseConfiguration {

    /**
     * @return the name of the database represented by this configuration.
     */
    String getDatabase();
    
    /**
     * Checks whether this configuration requires additional config. If the 
     * method returns true, the client should invoke the {@link editConfig(FileObject)} 
     * method.
     * 
     * @return true if additional configuration is required, false otherwise.
     */
    boolean needExtraConfig();
    
    /**
     * Edits the database config file (database.yml) of the given <code>project</code>
     * as required by this configuration, and in case of JDBC connections, 
     * possibly adds a reference to the required 
     * driver jar files to the properties of the <code>project</code>.
     * 
     * @param projectDir the project whose <code>database.yml</code> is 
     * to be edited.
     */
    void editConfig(RailsProject project);
    
}
