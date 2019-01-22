/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.projectimport.j2seimport.ui;
import java.util.Iterator;
import org.netbeans.modules.projectimport.j2seimport.WarningContainer;
import org.netbeans.modules.projectimport.j2seimport.ImportProcess;
import org.netbeans.modules.projectimport.j2seimport.WarningContainer.Warning;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 *
 * 
 */
public class WarningMessage {
    public static void showMessages(final ImportProcess iProcess) {
        WarningContainer warnings = iProcess.getWarnings();
        
        if (warnings != null) {
            Iterator it = iProcess.getWarnings().getIterator();
            String message = createHtmlString(NbBundle.getMessage(WarningMessage.class, "MSG_ProblemsOccured"), it, true, 10);//NOI18N
            if (message != null) {
                NotifyDescriptor d = new DialogDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }
    
    public static String createHtmlString(String msg, Iterator it, boolean userNotificationOnly, int itemsLimit) {
        StringBuffer sb = new StringBuffer();
        int items = 0;
        sb.append("<html><b>").append(msg).append("</b><ul>");//NOI18N
        while (it.hasNext()) {
            WarningContainer.Warning warning = (Warning)it.next();
            boolean add = (userNotificationOnly && !warning.isUserNotification()) ? false : true;
            if (items < itemsLimit) {
                if (add) {
                    items++;
                    sb.append("<li>").append(warning.getMessage()).append("</li>");//NOI18N
                }
            } else {
                break;
            }
        }
        
        sb.append("</ul>");
        sb.append("</html>");//NOI18N
        return (items > 0) ? sb.toString() : null;
    }
    
    /** Creates a new instance of WarningMessage */
    private  WarningMessage() {
    }
    
}
