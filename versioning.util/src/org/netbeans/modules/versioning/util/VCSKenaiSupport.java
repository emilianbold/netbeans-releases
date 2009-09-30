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

package org.netbeans.modules.versioning.util;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSKenaiSupport {

    /**
     * Some kenai vcs repository was changed
     */
    public final static String PROP_KENAI_VCS_NOTIFICATION = "kenai.vcs.notification"; // NOI18N

    protected static Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.util.VCSKenaiSupport");

    /**
     * A Kenai service
     */
    public enum Service {
        VCS_SVN,
        VCS_HG,
        UNKNOWN;
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or null if user not logged in.
     *
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication();

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials or forces a login if forceLogin is true
     *
     * @param forceLogin opens a login dialog is user not logged in
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(boolean forceLogin);

    /**
     * Returns true if the given url belongs to a Kenai project, otherwise false.
     * 
     * @param url
     * @return
     */
    public abstract boolean isKenai(String url);

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public abstract boolean showLogin();

    /**
     * Creates a firm association between the roots and a kenai issuetracking repository
     * which has the given vcs url
     *
     * @param roots roots to be associated with a kenai repository
     * @param url vcs url to match a kenai repository
     */
    public abstract void setFirmAssociations(File[] roots, String url);

    /**
     * Determines if the user is logged into kenai
     * @return true if user is logged into kenai otherwise false
     */
    public abstract boolean isLogged ();

    /**
     * Returns a {@link KenaiUser} with the given name
     * @param userName user name
     * @return a KenaiUser instance
     */
    public abstract KenaiUser forName(final String userName);

    /**
     * Determines wheter the user with the given name is online or not
     *
     * @param userName user name
     * @return true if user is online, otherwise false
     */
    public abstract boolean isUserOnline(String userName);

    /**
     * Registers a listener to listen on changes in a kenai VCS repository
     * @param l listener
     */
    public abstract void addVCSNoficationListener(PropertyChangeListener l);

    /**
     * Unregisters a listener to listen on changes in a kenai VCS repository
     * @param l listener
     */
    public abstract void removeVCSNoficationListener(PropertyChangeListener l);

    /**
     * Returns a path to a web page showing information about a revision in the repository.
     * @param sourcesUrl repository url
     * @param revision required revision
     * @return
     */
    public abstract String getRevisionUrl (String sourcesUrl, String revision);

    /**
     * Logs usage of a versioning system for a specific repository - if the
     * repository is from Kenai.
     * @param vcs name of the versioning system
     * @param repositoryUrl repository URL
     */
    public abstract void logVcsUsage(String vcs, String repositoryUrl);

    /**
     * Repesents a Kenai user
     */
    public abstract class KenaiUser {

        /**
         * Determines wheter the {@link KenaiUser} is online or not
         * @return true if user is online, othewise false
         */
        public abstract boolean isOnline();

        /**
         * Register a listener
         * @param listener
         */
        public abstract void addPropertyChangeListener(PropertyChangeListener listener);

        /**
         * Unregister a listener
         * @param listener
         */
        public abstract void removePropertyChangeListener(PropertyChangeListener listener);

        /**
         * Returns an icon representing the users online status
         * @return
         */
        public abstract Icon getIcon();

        /**
         * Returns an widget representing the users
         * @return
         */
        public abstract JLabel createUserWidget();

        /**
         * Returns user name
         * @return
         */
        public abstract String getUser();

        /**
         * Start a chat session with this user
         */
        public abstract void startChat();
    }

    /**
     * Represents a change in a kenai VCS repository
     */
    public abstract class VCSKenaiNotification {

        /**
         * The repository uri
         * @return
         */
        public abstract URI getUri();

        /**
         * Timestamp of change
         * @return
         */
        public abstract Date getStamp();

        /**
         * Determines the repository service - e.g svn, hg
         * @return
         */
        public abstract Service getService();

        /**
         * Notified modifications
         * @return
         */
        public abstract List<VCSKenaiModification> getModifications();

        /**
         * Author who made the change
         * @return
         */
        public abstract String getAuthor();

        /**
         * Returns the netbeans projects directoru
         */
        public abstract File getProjectDirectory();
    }

    /**
     * Represenst a modification in a Kenai VCS repository
     */
    public abstract static class VCSKenaiModification {

        /**
         * Type of modification
         */
        public static enum Type {
            NEW,
            CHANGE,
            DELETE
        }

        /**
         * Determines the type of this modification
         * @return
         */
        public abstract Type getType();

        /**
         * Determines the affeted resource
         * @return
         */
        public abstract String getResource();

        /**
         * Identifies this modification - e.g reviosion or changeset
         * @return
         */
        public abstract String getId();
    }

    /**
     * Hadles VCS notifications from kenai
     */
    public abstract static class KenaiNotificationListener implements PropertyChangeListener {

        protected static Logger LOG = VCSKenaiSupport.LOG;

        private static final String NOTIFICATION_ICON_PATH = "org/netbeans/modules/versioning/util/resources/info.png"; //NOI18N
        private RequestProcessor rp = new RequestProcessor("Kenai VCS notifications");                                  //NOI18N

        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(VCSKenaiSupport.PROP_KENAI_VCS_NOTIFICATION)) {
                final VCSKenaiNotification notification = (VCSKenaiNotification) evt.getNewValue();
                rp.post(new Runnable() {
                    public void run() {
                        handleVCSNotification(notification);
                    }
                });
            }
        }

        /**
         * Do whatever you have to do with a nofitication
         *
         * @param notification
         */
        protected abstract void handleVCSNotification(VCSKenaiNotification notification);

        /**
         * Called to setup the textpane used for the notification buble
         *
         * @param files
         * @param url
         * @param revision
         * @return
         */
        protected abstract void setupPane(JTextPane pane, File[] file, File projectDir, String url, String revision);

        /**
         * Removes a leading and trailing slash from the given string
         * @param str
         * @return
         */
        protected String trim(String str) {
            if(str.startsWith("/")) {
                str = str.substring(1, str.length());
            }
            if(str.endsWith("/")) {
                str = str.substring(0, str.length() - 1);
            }
            return str;
        }

        /**
         * 
         * @param files
         * @return
         */
        protected String getFileNames(final File[] files) {
            StringBuffer filesSb = new StringBuffer();
            for (int i = 0; i < files.length; i++) {
                if (i == 4) {
                    filesSb.append("...");                                      // NOI18N
                    break;
                }
                File file = files[i];
                filesSb.append("&nbsp;&nbsp;&nbsp;&nbsp;");                     // NOI18N
                filesSb.append(file.getName());
                filesSb.append("<br>");                                         // NOI18N
            }
            filesSb.append("<br>");                                             // NOI18N
            return filesSb.toString();
        }

        /**
         *
         * Opens a notification buble in the IDEs status bar containing the text returned in
         * {@link #getPaneText(java.io.File, java.lang.String, java.lang.String)}
         *
         * @param files
         * @param url
         * @param revision
         */
        protected void notifyFileChange(File[] files, File projectDir, String url, String revision) {
            JTextPane ballonDetails = getPane(files, projectDir, url, revision); // using the same pane causes the balloon popup
            JTextPane popupDetails = getPane(files, projectDir, url, revision);  // to trim the text to the first line
            NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(KenaiNotificationListener.class, "MSG_NotificationBubble_Title"), //NOI18N
                    ImageUtilities.loadImageIcon(NOTIFICATION_ICON_PATH, false),
                    ballonDetails, popupDetails, NotificationDisplayer.Priority.NORMAL);
        }

        private JTextPane getPane (final File[] files, final File projectDir, final String url, final String revision) {
            JTextPane bubble = new JTextPane();
            bubble.setOpaque(false);
            bubble.setEditable(false);

            if (UIManager.getLookAndFeel().getID().equals("Nimbus")) {                   //NOI18N
                //#134837
                //http://forums.java.net/jive/thread.jspa?messageID=283882
                bubble.setBackground(new Color(0, 0, 0, 0));
            }

            bubble.setContentType("text/html");                                          //NOI18N
            setupPane(bubble, files, projectDir, url, revision);
            return bubble;
        }

    }
    
}
