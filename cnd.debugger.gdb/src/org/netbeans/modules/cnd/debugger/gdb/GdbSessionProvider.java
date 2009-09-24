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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb;

import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.openide.util.NbBundle;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;

public class GdbSessionProvider extends SessionProvider {

    private String sessionName = NbBundle.getMessage(GdbSessionProvider.class, "CTL_GDB_SESSION"); // NOI18N
    //private String locationName = NbBundle.getMessage(GdbSessionProvider.class, "CTL_GDB_SESSION"); // NOI18N
    private final String [] supportedLanguages = new String [] { "C++", "C", "Fortran" }; // NOI18N
    private final ProjectActionEvent projectActionEvent;

    public GdbSessionProvider(ContextProvider contextProvider) {
        projectActionEvent = contextProvider.lookupFirst(null, ProjectActionEvent.class);
    };

    public String getSessionName () {
        String sn = null;
        if (projectActionEvent != null) {
            sn = projectActionEvent.getExecutable();
        }
        if (sn == null) {
            return sessionName;
        }
        if (sn.length() > 8) {
            // Name is too long - get base name
            if (sn.lastIndexOf('/') >= 0) {
                sn = sn.substring(sn.lastIndexOf('/') + 1);
            }
        }
        if (sn.length() > 0) {
            // Set session name
            sessionName = sn; 
        }
        return sessionName;
    }
    
    public String getLocationName() {
        if (projectActionEvent != null) {
            return ServerList.get((projectActionEvent.getConfiguration()).
                    getDevelopmentHost().getExecutionEnvironment()).getServerName();
        }
        return CompilerSetManager.LOCALHOST;
    }
    
    public String getTypeID() {
        return GdbDebugger.SESSION_ID;
    }
    
    public Object[] getServices() {
        return supportedLanguages; 
    }

}
