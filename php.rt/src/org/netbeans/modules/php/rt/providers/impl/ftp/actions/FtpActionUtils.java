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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl.ftp.actions;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.absent.actions.AbsentActionsUtils;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
class FtpActionUtils {

    private static final String NO_FTP_TITLE = "LBL_NotConfiguredFtp_Title"; // NOI18N
    
    private static final String NO_FTP_MESSAGE = "LBL_NotConfiguredFtp_Message"; // NOI18N

    
    protected static boolean checkHostFtpPart(Host host, Project project, String command) {
        boolean res = false;
        if (host instanceof FtpHostImpl){
            FtpHostImpl impl = (FtpHostImpl)host;
            if (FtpHostImpl.Helper.isFtpReady(impl)) {
                res = true;
            } else {
                alertFtpNotConfigured(impl, project, command);
            }

        }
        return res;
    }

    private static void alertFtpNotConfigured(HostImpl host, Project project, String command){
                String projectName = ProjectUtils.getInformation(project).
                        getDisplayName();

                String title = NbBundle.getMessage(FtpActionUtils.class, NO_FTP_TITLE);
                String msg = NbBundle.getMessage(FtpActionUtils.class, 
                        NO_FTP_MESSAGE, host.getDisplayName(), command, projectName);

                AbsentActionsUtils.alertNotConfiguredHost(project, title, msg);
    }
    
}
