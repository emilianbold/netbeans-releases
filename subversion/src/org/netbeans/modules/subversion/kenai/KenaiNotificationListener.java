/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.kenai;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.notifications.NotificationsManager;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VCSKenaiSupport;
import org.netbeans.modules.versioning.util.VCSKenaiSupport.VCSKenaiModification;
import org.netbeans.modules.versioning.util.VCSKenaiSupport.VCSKenaiNotification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiNotificationListener implements PropertyChangeListener {

    private RequestProcessor rp = new RequestProcessor("Kenai vcs notifications");
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(VCSKenaiSupport.PROP_KENAI_VCS_NOTIFICATION)) {
            handleVCSNotification((VCSKenaiNotification) evt.getNewValue());
        }
    }

    private void handleVCSNotification(final VCSKenaiNotification notification) {
        if(notification.getService() != VCSKenaiSupport.Service.VCS_SVN) {
            return;
        }
        rp.post(new Runnable() {
            public void run() {
                URI uri = notification.getUri();
                SVNUrl notificationUrl;
                try {
                    notificationUrl = new SVNUrl(uri.toString());
                } catch (MalformedURLException ex) {
                    Subversion.LOG.log(Level.WARNING, null, ex);
                    return;
                }
                Project projects[] = OpenProjects.getDefault().getOpenProjects();
                for (Project project : projects) {
                    File root = FileUtil.toFile(project.getProjectDirectory());
                    if(!SvnUtils.isManaged(root)) {
                        continue;
                    }
                    SVNUrl repositoryRoot;
                    try {
                        repositoryRoot = SvnUtils.getRepositoryRootUrl(root);
                    } catch (SVNClientException ex) {
                        Subversion.LOG.log(Level.WARNING, null, ex);
                        continue;
                    }
                    if(repositoryRoot.equals(notificationUrl)) {
                        File[] files = Subversion.getInstance().getStatusCache().listFiles(root);
                        List<VCSKenaiModification> modifications = notification.getModifications();

                        for (File file : files) {
                            for (VCSKenaiModification modification : modifications) {
                                String resource = modification.getResource();
                                if(file.equals(new File(root, resource))) {
                                    notifyChange(file, notification, modification);
                                }
                            }
                        }
                    }
                }
            }

        });

    }
    
    private void notifyChange(File file, VCSKenaiNotification notification, VCSKenaiModification modification) {
        try {
            notifyFileChange(file, new SVNUrl(notification.getUri().toString()), Long.parseLong(modification.getId()));
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.WARNING, null, ex);
        }
    }

    private static final String NOTIFICATION_ICON_PATH = "org/netbeans/modules/subversion/resources/icons/info.png"; //NOI18N    
    private static final String NOTIFICATION_REVISION_LINK = "<a href=\"{0}\">{1}</a>"; //NOI18N    
    
    private void notifyFileChange(File file, SVNUrl url, long revision) {
        NotificationDisplayer.getDefault().notify(
                NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_Title"), //NOI18N
                ImageUtilities.loadImageIcon(NOTIFICATION_ICON_PATH, false),
                getSimplePane(file), getDetailsPane(file, url, revision), NotificationDisplayer.Priority.NORMAL);
    }

    private JTextPane getSimplePane (File file) {
        JTextPane bubble = new JTextPane();
        String text = NbBundle.getMessage(KenaiNotificationListener.class, "MSG_NotificationBubble_Description", file.getName()); //NOI18N
        bubble.setText(text);
        bubble.setOpaque(false);
        bubble.setEditable(false);

        if (UIManager.getLookAndFeel().getID().equals("Nimbus")) {  //NOI18N
            //#134837
            //http://forums.java.net/jive/thread.jspa?messageID=283882
            bubble.setBackground(new Color(0, 0, 0, 0));
        }
        return bubble;
    }

    private JTextPane getDetailsPane (final File file, final SVNUrl svnUrl, final long revision) {
        JTextPane bubble = getSimplePane(file);
        bubble.setContentType("text/html");                        //NOI18N
        String url = svnUrl.toString();
        String text = java.text.MessageFormat.format(NOTIFICATION_REVISION_LINK, url,
                    NbBundle.getMessage(KenaiNotificationListener.class, "MSG_NotificationBubble_DescriptionRevision"));
        bubble.setText(text);

        bubble.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    SearchHistoryAction.openSearch(svnUrl, file, revision);
                }
            }
        });
        return bubble;
    }

}
