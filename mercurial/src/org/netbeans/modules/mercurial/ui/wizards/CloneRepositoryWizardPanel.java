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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.BorderLayout;
import java.security.KeyManagementException;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JPanel;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import static org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme.FILE;
import static org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme.HTTP;
import static org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme.HTTPS;

public class CloneRepositoryWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel, ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CloneRepositoryPanel component;
    private Repository repository;
    private int repositoryModeMask;
    private boolean valid;
    private String errorMessage;
    private WizardStepProgressSupport support;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CloneRepositoryPanel();
            if (repository == null) {
                repositoryModeMask = repositoryModeMask | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
                String title = org.openide.util.NbBundle.getMessage(CloneRepositoryWizardPanel.class, "CTL_Repository_Location");       // NOI18N
                repository = new Repository(repositoryModeMask, title, false);
                repository.addChangeListener(this);
                CloneRepositoryPanel panel = (CloneRepositoryPanel)component;
                panel.repositoryPanel.setLayout(new BorderLayout());
                panel.repositoryPanel.add(repository.getPanel());
                valid();
            }
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(CloneRepositoryWizardPanel.class);
    }
    
    //public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
    //    return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    //}
    
    public void stateChanged(ChangeEvent evt) {
        if(repository.isValid()) {
            valid(repository.getMessage());
        } else {
            invalid(repository.getMessage());
        }
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    protected final void valid() {
        setValid(true, null);
    }

    protected final void valid(String extErrorMessage) {
        setValid(true, extErrorMessage);
    }

    protected final void invalid(String message) {
        setValid(false, message);
    }

    public final boolean isValid() {
        return valid;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    private void displayErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            throw new IllegalArgumentException("<null> message");
        }

        if (!errorMessage.equals(this.errorMessage)) {
            this.errorMessage = errorMessage;
            fireChangeEvent();
        }
    }

    private void setValid(boolean valid, String errorMessage) {
        boolean fire = this.valid != valid;
        fire |= errorMessage != null && (errorMessage.equals(this.errorMessage) == false);
        this.valid = valid;
        this.errorMessage = errorMessage;
        if (fire) {
            fireChangeEvent();
        }
    }

    protected void validateBeforeNext() throws WizardValidationException {
        try {
            HgURL url;
            try {
                url = repository.getUrl();
            } catch (URISyntaxException ex) {
                throw new WizardValidationException((JComponent) component,
                                                    ex.getMessage(),
                                                    ex.getLocalizedMessage());
            }

            support = new RepositoryStepProgressSupport(component.progressPanel);

            support.setRepositoryRoot(url);
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(url);
            RequestProcessor.Task task = support.start(rp, url, NbBundle.getMessage(CloneRepositoryWizardPanel.class, "BK2012"));
            task.waitFinished();
        } finally {
            support = null;
        }

    }

    // comes on next or finish
    public final void validate () throws WizardValidationException {
        validateBeforeNext();
        if (isValid() == false || errorMessage != null) {
            throw new WizardValidationException (
                (javax.swing.JComponent) component,
                errorMessage,
                errorMessage
            );
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {}
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            try {
                ((WizardDescriptor) settings).putProperty("repository", repository.getUrl()); // NOI18N
            } catch (URISyntaxException ex) {
                /*
                 * The panel's data may not be validated yet (bug #163078)
                 * so we cannot assume that the entered URL is valid - so
                 * we must catch the URISyntaxException.
                 */
                Logger.getLogger(getClass().getName()).throwing(
                                                        getClass().getName(),
                                                        "storeSettings",//NOI18N
                                                        ex);
            }
        }
    }

    public void prepareValidation() {
    }

    private void storeHistory() {
        RepositoryConnection rc = getRepositoryConnection();
        if(rc != null) {
            HgModuleConfig.getDefault().insertRecentUrl(rc);
        }
    }

    private RepositoryConnection getRepositoryConnection() {
        try {
            return repository.getRepositoryConnection();
        } catch (Exception ex) {
            displayErrorMessage(ex.getLocalizedMessage());
            return null;
        }
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {

        public RepositoryStepProgressSupport(JPanel panel) {
            super(panel);
        }

        public void perform() {
            final RepositoryConnection rc = getRepositoryConnection();
            if (rc == null) {
                return;
            }
            String invalidMsg = null;
            HttpURLConnection con = null;
            try {
                HgURL hgUrl = getRepositoryRoot();

                HgURL.Scheme uriSch = hgUrl.getScheme();
                if (uriSch == FILE) {
                    File f = HgURL.getFile(hgUrl);
                    if(!f.exists() || !f.canRead()){
                        invalidMsg = NbBundle.getMessage(CloneRepositoryWizardPanel.class,
                               "MSG_Progress_Clone_CannotAccess_Err");
                        return;
                    }
                } else if ((uriSch == HTTP) || (uriSch == HTTPS)) {
                    URL url = hgUrl.toURL();
                    con = (HttpURLConnection) url.openConnection();
                    // Note: valid repository returns con.getContentLength() = -1
                    // so no way to reliably test if this url exists, without using hg
                    if (con != null) {
                        String userInfo = url.getUserInfo();
                        boolean bNoUserAndOrPasswordInURL = userInfo == null;
                        // If username or username:password is in the URL the con.getResponseCode() returns -1 and this check would fail
                        if (uriSch == HTTPS) {
                            setupHttpsConnection(con);
                        }
                        if (bNoUserAndOrPasswordInURL && con.getResponseCode() != HttpURLConnection.HTTP_OK){
                            invalidMsg = NbBundle.getMessage(CloneRepositoryWizardPanel.class,
                                    "MSG_Progress_Clone_CannotAccess_Err");
                            con.disconnect();
                            return;
                        }else if (userInfo != null){
                            Mercurial.LOG.log(Level.FINE, 
                                "RepositoryStepProgressSupport.perform(): UserInfo - {0}", new Object[]{userInfo}); // NOI18N
                        }
                    }
                 }
            } catch (java.lang.IllegalArgumentException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
                 invalidMsg = NbBundle.getMessage(CloneRepositoryWizardPanel.class,
                                  "MSG_Progress_Clone_InvalidURL_Err");
                 return;
            } catch (IOException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
                 invalidMsg = NbBundle.getMessage(CloneRepositoryWizardPanel.class,
                                  "MSG_Progress_Clone_CannotAccess_Err");
                return;
            } catch (RuntimeException re) {
                Throwable t = re.getCause();
                if(t != null) {
                    invalidMsg = t.getLocalizedMessage();
                } else {
                    invalidMsg = re.getLocalizedMessage();
                }
                Mercurial.LOG.log(Level.INFO, invalidMsg, re);
                return;
            } finally {
                if(con != null) {
                    con.disconnect();
                }
                if(isCanceled()) {
                  displayErrorMessage(org.openide.util.NbBundle.getMessage(CloneRepositoryWizardPanel.class, "CTL_Repository_Canceled")); // NOI18N
                } else if(invalidMsg == null) {
                  storeHistory();
                } else {
                  displayErrorMessage(invalidMsg);
                }
            }
        }

        public void setEditable(boolean editable) {
            //XXX PENDING: repository.setEditable(editable);
        }

        private void setupHttpsConnection(HttpURLConnection con) {
            X509TrustManager tm = new X509TrustManager() {
                 public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                     // do nothing
                 }
                 public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                     // do nothing
                 }
                 public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                 }
            };
            HostnameVerifier hnv = new HostnameVerifier() {
                 public boolean verify(String hostname, SSLSession session) {
                    return true;
                 }
            };
            try {
                SSLContext context = SSLContext.getInstance("SSLv3");
                TrustManager[] trustManagerArray = { tm };
                context.init(null, trustManagerArray, null);
                HttpsURLConnection c = (HttpsURLConnection) con;
                c.setSSLSocketFactory(context.getSocketFactory());
                c.setHostnameVerifier(hnv);
            } catch (KeyManagementException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
            } catch (NoSuchAlgorithmException ex) {
                 Mercurial.LOG.log(Level.INFO, ex.getMessage(), ex);
            }
        }
    };

}

