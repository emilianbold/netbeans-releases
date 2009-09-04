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

package org.netbeans.modules.subversion.notifications;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnKenaiSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Notifies about external changes on the given files.
 * @author Ondra Vrabec
 */
public class NotificationsManager {

    private static NotificationsManager instance;
    private static final Logger LOG = Logger.getLogger(NotificationsManager.class.getName());
    private static final Set<File> alreadySeen = Collections.synchronizedSet(new WeakSet<File>());
    private static final String NOTIFICATION_ICON_PATH = "org/netbeans/modules/subversion/resources/icons/info.png"; //NOI18N
    private static final String NOTIFICATION_DIFF_LINK = "{0}<br><a href=\"\">{1}</a>"; //NOI18N
    private static final String NOTIFICATION_REVISION_LINK = "<br><a href=\"{0}\">{1}</a>"; //NOI18N

    private final HashSet<File> files;
    private final RequestProcessor rp;
    private final RequestProcessor.Task notificationTask;
    private final FileStatusCache cache;
    private final SvnKenaiSupport supp;
    private Boolean enabled;

    private NotificationsManager () {
        files = new HashSet<File>();
        rp = new RequestProcessor("SubversionNotifications", 1, true);  //NOI18N
        notificationTask = rp.create(new NotificationTask());
        cache = Subversion.getInstance().getStatusCache();
        supp = SvnKenaiSupport.getInstance();
    }

    /**
     * Returns an instance of the class
     * @return
     */
    public static synchronized NotificationsManager getInstance() {
        if (instance == null) {
            instance = new NotificationsManager();
        }
        return instance;
    }

    /**
     * Plans a task detecting if a notification for the file is needed and in that case displaying the notification.
     * The notification is displayed if the file is still up-to-date (during the time of the call) and there's an external change
     * in the repository.
     * @param file file to scan
     */
    public void scheduleFor(File file) {
        if (!isEnabled() || !isUpToDate(file)) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("File " + file.getAbsolutePath() + " is " + (isUpToDate(file) ? "" : "not ") + " up to date, notifications enabled: " + isEnabled()); //NOI18N
            }
            return;
        }
        boolean refresh = false;
        // register the file for the scan
        synchronized (files) {
            int size = files.size();
            files.add(file);
            refresh = files.size() != size;
        }
        if (refresh) {
            notificationTask.schedule(100);
        }
    }

    /**
     * Notifications are enabled only for logged kenai users and unless disabled by a switch
     * @return
     */
    private boolean isEnabled() {
        if (enabled == null) {
            // let's leave a possibility to disable the notifications
            enabled = new Boolean(!"false".equals(System.getProperty("subversion.notificationsEnabled", "true"))); //NOI18N
        }
        return enabled.booleanValue() && supp.isLogged();
    }

    private boolean isUpToDate(File file) {
        boolean upToDate = false;
        FileInformation info = cache.getCachedStatus(file);
        if (info == null || (info.getStatus() & FileInformation.STATUS_VERSIONED_UPTODATE) != 0 && !info.isDirectory()) {
            upToDate = true;
        }
        return upToDate;
    }

    private class NotificationTask implements Runnable {

        public void run() {
            HashSet<File> filesToScan;
            synchronized (files) {
                filesToScan = new HashSet<File>(files);
                files.clear();
            }
            removeSeenFiles(filesToScan);
            if (filesToScan.size() != 0) {
                scanFiles(filesToScan);
            }
        }

        private void removeSeenFiles (Collection<File> filesToScan) {
            for (Iterator<File> it = filesToScan.iterator(); it.hasNext();) {
                File file = it.next();
                if (alreadySeen.contains(file)) {
                    it.remove();
                }
            }
        }

        private void scanFiles (Collection<File> filesToScan) {
            HashMap<SVNUrl, HashSet<File>> filesPerRepository = sortByRepository(filesToScan);
            for (Map.Entry<SVNUrl, HashSet<File>> entry : filesPerRepository.entrySet()) {
                SVNUrl repositoryUrl = entry.getKey();
                try {
                    SvnClient client = Subversion.getInstance().getClient(repositoryUrl);
                    if (client != null) {
                        HashSet<File> files = entry.getValue();
                        for (File file : files) {
                            // get repository status
                            ISVNStatus[] statuses = client.getStatus(file, false, false, true);
                            if (statuses.length == 1) { // is an interesting status
                                ISVNStatus status = statuses[0];
                                if (status.getRepositoryTextStatus().equals(SVNStatusKind.MODIFIED)
                                    || status.getRepositoryTextStatus().equals(SVNStatusKind.DELETED)
                                    || status.getRepositoryTextStatus().equals(SVNStatusKind.ADDED)) {
                                    // this will refresh versioning view as well
                                    cache.refresh(file, status);
                                    // notify in a bubble
                                    notifyFileChange(file, status, repositoryUrl);
                                }
                            }
                        }
                    }
                } catch (SVNClientException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            }
        }

        private HashMap<SVNUrl, HashSet<File>> sortByRepository (Collection<File> files) {
            HashMap<SVNUrl, HashSet<File>> filesByRepository = new HashMap<SVNUrl, HashSet<File>>();
            for (File file : files) {
                SVNUrl repositoryUrl = getRepositoryRoot(file);
                if (repositoryUrl != null) {
                    HashSet<File> filesPerRepository = filesByRepository.get(repositoryUrl);
                    if (filesPerRepository == null) {
                        filesPerRepository = new HashSet<File>();
                        filesByRepository.put(repositoryUrl, filesPerRepository);
                    }
                    filesPerRepository.add(file);
                }
            }
            return filesByRepository;
        }

        private void notifyFileChange(File file, ISVNStatus status, SVNUrl repositoryUrl) {
            NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_Title"), //NOI18N
                    ImageUtilities.loadImageIcon(NOTIFICATION_ICON_PATH, false),
                    getSimplePane(file), getDetailsPane(file, status, repositoryUrl), NotificationDisplayer.Priority.NORMAL);
            alreadySeen.add(file);
        }

        private JTextPane getSimplePane (File file) {
            JTextPane bubble = new JTextPane();
            String text = NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_Description", file.getName()); //NOI18N
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

        private JTextPane getDetailsPane (final File file, final ISVNStatus status, SVNUrl repositoryUrl) {
            JTextPane bubble = getSimplePane(file);
            bubble.setContentType("text/html");                        //NOI18N
            String text = java.text.MessageFormat.format(NOTIFICATION_DIFF_LINK,
                    NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_Description", file.getName()),  //NOI18N
                    NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_DescriptionDiff")); //NOI18N
            String url = repositoryUrl == null ? null : getRevisionLink(status, repositoryUrl);
            if (url != null) {
                text += java.text.MessageFormat.format(NOTIFICATION_REVISION_LINK, url,
                        NbBundle.getMessage(NotificationsManager.class, "MSG_NotificationBubble_DescriptionRevision"));
            }
            bubble.setText(text);

            bubble.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        URL url = e.getURL();
                        if (url != null) {
                            // open a browser
                            URLDisplayer.getDefault().showURL(e.getURL());
                        } else {
                            // show diff
                            DiffAction.diff(file, status);
                        }
                    }
                }
            });
            return bubble;
        }

        private String getRevisionLink(ISVNStatus status, SVNUrl repositoryRoot) {
            String revisionLink = null;
            revisionLink = supp.getRevisionUrl(repositoryRoot.toString(), status.getRevision().toString());
            return revisionLink;
        }

        /**
         * Returns repository root url for the given file or null if the repository is unknown or does not belong to allowed urls.
         * Currently allowed repositories are kenai repositories.
         * @param file
         * @return
         */
        private SVNUrl getRepositoryRoot (File file) {
            SVNUrl repositoryUrl = null;
            SVNUrl url = getRepositoryUrl(file);
            if (url != null && supp.isKenai(url.toString())) {
                repositoryUrl = url;
            }
            return repositoryUrl;
        }

        private SVNUrl getRepositoryUrl (File file) {
            SVNUrl url = null;
            try {
                url = SvnUtils.getRepositoryRootUrl(file);
            } catch (SVNClientException ex) {
                LOG.log(Level.WARNING, "getRepositoryUrl: No repository root url found for managed file : [" + file + "]", ex); //NOI18N
                try {
                    url = SvnUtils.getRepositoryUrl(file); // try to falback
                } catch (SVNClientException ex1) {
                    LOG.log(Level.WARNING, "getRepositoryUrl: No repository url found for managed file : [" + file + "]", ex1); //NOI18N
                }
            }
            return url;
        }
    }
}
