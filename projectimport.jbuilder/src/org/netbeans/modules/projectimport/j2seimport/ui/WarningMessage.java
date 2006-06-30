/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * @author Radek Matous
 */
public class WarningMessage {
    public static void showMessages(final ImportProcess iProcess) {
        WarningContainer warnings = iProcess.getWarnings();
        
        if (warnings != null) {
            Iterator it = iProcess.getWarnings().getAllWarnings();
            String message = createHtmlString(it);
            if (message != null) {
                NotifyDescriptor d = new DialogDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }
    
    private static String createHtmlString(Iterator it) {
        StringBuffer sb = new StringBuffer();
        boolean existsAnyNotification = false;
        String msg = NbBundle.getMessage(WarningMessage.class, "MSG_ProblemsOccured");//NOI18N        
        sb.append("<html><b>").append(msg).append("</b><ul>");//NOI18N
        while (it.hasNext()) {
            WarningContainer.Warning warning = (Warning)it.next();
            if (warning.isUserNotification()) {
                existsAnyNotification = true;
                sb.append("<li>").append(warning.getMessage()).append("</li>");//NOI18N
            }
        }
        
        sb.append("</ul>");
        sb.append("</html>");//NOI18N
        return (existsAnyNotification) ? sb.toString() : null;
    }
    
    /** Creates a new instance of WarningMessage */
    private  WarningMessage() {
    }
    
}
