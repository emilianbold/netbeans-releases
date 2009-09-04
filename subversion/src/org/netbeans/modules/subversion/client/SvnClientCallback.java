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
package org.netbeans.modules.subversion.client;

import java.awt.Dialog;
import java.net.PasswordAuthentication;
import javax.swing.JButton;
import org.netbeans.modules.subversion.kenai.SvnKenaiSupport;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka 
 */
public class SvnClientCallback implements ISVNPromptUserPassword {
    
    private final SVNUrl url;
    private final int handledExceptions;
    
    private String username = null;
    private String password = null;        

    private final boolean prompt;
    
    /** Creates a new instance of SvnClientCallback */
    public SvnClientCallback(SVNUrl url, int handledExceptions) {
        this.url = url;
        this.handledExceptions = handledExceptions;
        this.prompt = (SvnClientExceptionHandler.EX_AUTHENTICATION & handledExceptions) == SvnClientExceptionHandler.EX_AUTHENTICATION;
    }

    public boolean askYesNo(String string, String string0, boolean b) {
        // TODO implement me
        return false;
    }

    public String getUsername() {
        if((SvnClientExceptionHandler.EX_AUTHENTICATION & handledExceptions) != SvnClientExceptionHandler.EX_AUTHENTICATION) {
            return null;
        }
        if(username == null) {
            getAuthData();
        }
        String ret = username;
        username = null;
        return ret;        
    }

    public String getPassword() {
        if((SvnClientExceptionHandler.EX_AUTHENTICATION & handledExceptions) != SvnClientExceptionHandler.EX_AUTHENTICATION) {
            return null;
        }        
        if(password == null) {
            getAuthData();
        }
        String ret = password;
        password = null;
        return ret;             
    }

    public int askTrustSSLServer(String certMessage, boolean b) {
        
        if((SvnClientExceptionHandler.EX_NO_CERTIFICATE & handledExceptions) != SvnClientExceptionHandler.EX_NO_CERTIFICATE) {
            return -1; // XXX test me
        }
        
        AcceptCertificatePanel acceptCertificatePanel = new AcceptCertificatePanel();
        acceptCertificatePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Error_CertFailed"));
        acceptCertificatePanel.certificatePane.setText(certMessage);
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(acceptCertificatePanel, org.openide.util.NbBundle.getMessage(SvnClientCallback.class, "CTL_Error_CertFailed")); // NOI18N        
        
        JButton permanentlyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptPermanently")); // NOI18N
        JButton temporarilyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptTemp")); // NOI18N
        JButton rejectButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_Reject")); // NOI18N
        
        dialogDescriptor.setOptions(new Object[] { permanentlyButton, temporarilyButton, rejectButton }); 
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.subversion.serverCertificateVerification"));

        showDialog(dialogDescriptor);

        if(dialogDescriptor.getValue() == permanentlyButton) {
            return ISVNPromptUserPassword.AcceptPermanently;
        } else if(dialogDescriptor.getValue() == temporarilyButton) {
            return ISVNPromptUserPassword.AcceptTemporary;
        } else {
            return ISVNPromptUserPassword.Reject;
        }
    }

    public boolean prompt(String string, String string0, boolean b) {        
        return prompt;
    }

    public String askQuestion(String string, String string0, boolean b, boolean b0) {
        // TODO implement me
        return null;
    }

    public boolean userAllowedSave() {
        return true;
    }

    public boolean promptSSH(String string, String string0, int i, boolean b) {
        // TODO implement me
        return false;
    }

    public String getSSHPrivateKeyPath() {
        // TODO implement me
        return null;
    }

    public String getSSHPrivateKeyPassphrase() {
        // TODO implement me
        return null;
    }

    public int getSSHPort() {
        // TODO implement me
        return -1;
    }

    public boolean promptSSL(String string, boolean b) {
        // TODO implement me
        return false;
    }

    public String getSSLClientCertPassword() {
        // TODO implement me
        return null;
    }

    public String getSSLClientCertPath() {
        // TODO implement me
        return null;
    }

    private void getKenaiAuthData(SvnKenaiSupport support) {
        final String urlString = url.toString();
        
        PasswordAuthentication pa = support.getPasswordAuthentication(true);
        if(pa == null) {
            throw new RuntimeException(new InterruptedException(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_ActionCanceledByUser"))); //NOI18N
        }
        String user = pa.getUserName();
        char[] psswd = pa.getPassword();

        username = user != null ? user : "";
        password = psswd != null ? new String(psswd) : "";
    }

    private void showDialog(DialogDescriptor dialogDescriptor) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(false);     

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }    
    
    private void getAuthData() {        
        SvnKenaiSupport support = SvnKenaiSupport.getInstance();
        if(support != null && support.isKenai(url.toString())) {
            getKenaiAuthData(support);
        } else {
            String title = org.openide.util.NbBundle.getMessage(SvnClientCallback.class, "MSG_Error_ConnectionParameters");                 // NOI18N

            Repository repository = new Repository(title);
            repository.selectUrl(url, true);

            JButton retryButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientCallback.class, "CTL_Action_Retry"));           // NOI18N
            Object option = repository.show(org.openide.util.NbBundle.getMessage(SvnClientCallback.class, "MSG_Error_AuthFailed"),          // NOI18N
                                            new HelpCtx(this.getClass()),
                                            new Object[] {retryButton, org.openide.util.NbBundle.getMessage(SvnClientCallback.class, "CTL_Action_Cancel")},     // NOI18N
                                            retryButton);


            boolean ret = (option == retryButton);
            if(ret) {
                RepositoryConnection rc = repository.getSelectedRC();
                username = rc.getUsername();
                password = rc.getPassword();
                // XXX we don't need this and it also should be assured that the adapter isn't precofigured with auth data as long it's not the commandline ...
                //adapter.setUsername(username);
                //adapter.setPassword(password);
                if(rc.getSavePassword()) {
                    SvnModuleConfig.getDefault().insertRecentUrl(rc);
                }
            }
        }
    }

    public boolean promptUser(String arg0, String arg1, boolean arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
