/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import org.netbeans.modules.bugtracking.kenai.spi.RecentIssue;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiAccessor;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.issue.PatchContextChooser;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka, Jan Stola
 * @author Marian Petras
 */
public class BugtrackingUtil {

    /**
     * Metrics logger
     */
    private static Logger METRICS_LOG = Logger.getLogger("org.netbeans.ui.metrics.bugtracking"); // NOI18N

    /**
     * The automatic refresh was set on or off.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>is on : Boolean
     * </ol>
     */
    public static final String USG_BUGTRACKING_AUTOMATIC_REFRESH = "USG_BUGTRACKING_AUTOMATIC_REFRESH"; // NOI18N

    /**
     * A query was refreshed.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>query name : String
     *  <li>issues count : Integer
     *  <li>is a kenai query : Boolean
     *  <li>is a automatic refresh : Boolean
     * </ol>
     */
    public static final String USG_BUGTRACKING_QUERY             = "USG_BUGTRACKING_QUERY"; // NOI18N

    private static final String USG_ISSUE_TRACKING = "USG_ISSUE_TRACKING"; // NOI18N

    private static Set<String> loggedParams; // to avoid logging same params more than once in a session

    public static boolean show(JPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(NbBundle.getMessage(BugtrackingUtil.class, "LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        final DialogDescriptor dd =
            new DialogDescriptor(
                    panel,
                    title,
                    true,
                    new Object[]{ok, cancel},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(panel.getClass()),
                    null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    /**
     * Returns all curently openend issues which aren't new.
     * 
     * @return issues
     */
    public static Issue[] getOpenIssues() {
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        List<Issue> issues = new ArrayList<Issue>();
        for (TopComponent tc : tcs) {
            if(tc instanceof IssueTopComponent) {
                Issue issue = ((IssueTopComponent)tc).getIssue();
                if(!issue.isNew()) {
                    issues.add(issue);
                }
            }
        }
        return issues.toArray(new Issue[issues.size()]);
    }

    /**
     * Determines if the gives issue opened in the editor area
     * @param issue
     * @return true in case the given issue is opened in the editor are, otherwise false
     */
    public static boolean isOpened(Issue issue) {
        IssueTopComponent tc = IssueTopComponent.find(issue, false);
        return tc != null ? tc.isOpened() : false;
    }

    /**
     * Determines if the gives issue opened in the editor area and
     * showing on the screen
     * @param issue
     * @return true in case the given issue is opened in the editor area
     *         and showing on the screen, otherwise false
     */
    public static boolean isShowing(Issue issue) {
        IssueTopComponent tc = IssueTopComponent.find(issue, false);
        return tc != null ? tc.isShowing() : false;
    }

    /**
     * Filters the given issue by the given criteria and returns
     * those which either case unsensitively contain the criteria
     * in their summary or those which id equals the criteria.
     *
     * @param issues
     * @param criteria
     * @return
     */
    public static Issue[] getByIdOrSummary(Issue[] issues, String criteria) {
        if(criteria == null) {
            return issues;
        }
        criteria = criteria.trim();
        if(criteria.equals("")) {                                               // NOI18N
            return issues;
        }
        criteria = criteria.toLowerCase();
        List<Issue> ret = new ArrayList<Issue>();
        for (Issue issue : issues) {
            if(issue.isNew()) continue;
            String id = issue.getID();
            if(id == null) continue;
            String summary = issue.getSummary();
            if(id.toLowerCase().startsWith(criteria) ||
               (summary != null && summary.toLowerCase().indexOf(criteria) > -1))
            {
                ret.add(issue);
            }  
        }
        return ret.toArray(new Issue[ret.size()]);
    }

    public static Repository createRepository() {
        RepositorySelector rs = new RepositorySelector();
        Repository repo = rs.create();
        return repo;
    }

    public static boolean editRepository(Repository repository, String errorMessage) {
        RepositorySelector rs = new RepositorySelector();
        return rs.edit(repository, errorMessage);
    }

    public static boolean editRepository(Repository repository) {
        return editRepository(repository, null);
    }

    public static Repository[] getKnownRepositories(boolean pingOpenProjects) {
        return BugtrackingManager.getInstance().getKnownRepositories(pingOpenProjects);
    }

    public static BugtrackingConnector[] getBugtrackingConnectors() {
        return BugtrackingManager.getInstance().getConnectors();
    }

    public static String scramble(String str) {
        return Scrambler.getInstance().scramble(str);
    }

    public static String descramble(String str) {
        return Scrambler.getInstance().descramble(str);
    }

    public static Issue selectIssue(String message, Repository repository, JPanel caller, HelpCtx helpCtx) {
        QuickSearchComboBar bar = new QuickSearchComboBar(caller);
        bar.setRepository(repository);
        bar.setAlignmentX(0f);
        bar.setMaximumSize(new Dimension(Short.MAX_VALUE, bar.getPreferredSize().height));
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(layout);
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, message);
        panel.add(label);
        label.setLabelFor(bar.getIssueComponent());
        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        int gap = layoutStyle.getPreferredGap(label, bar, LayoutStyle.RELATED, SwingConstants.SOUTH, panel);
        panel.add(Box.createVerticalStrut(gap));
        panel.add(bar);
        panel.add(Box.createVerticalStrut(gap));
        ResourceBundle bundle = NbBundle.getBundle(BugtrackingUtil.class);
        JLabel hintLabel = new JLabel(bundle.getString("MSG_SelectIssueHint")); // NOI18N
        hintLabel.setEnabled(false);
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(80));
        panel.setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(panel, SwingConstants.NORTH, null),
                layoutStyle.getContainerGap(panel, SwingConstants.WEST, null),
                0,
                layoutStyle.getContainerGap(panel, SwingConstants.EAST, null)));
        panel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_IssueSelector"));
        Issue issue = null;
        JButton ok = new JButton(bundle.getString("LBL_Select")); // NOI18N
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                bundle.getString("LBL_Issues"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(helpCtx);
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            issue = bar.getIssue();
        }
        return issue;
    }

    public static File selectPatchContext() {
        PatchContextChooser chooser = new PatchContextChooser();
        ResourceBundle bundle = NbBundle.getBundle(BugtrackingUtil.class);
        JButton ok = new JButton(bundle.getString("LBL_Apply")); // NOI18N
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                chooser,
                bundle.getString("LBL_ApplyPatch"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.bugtracking.patchContextChooser")); // NOI18N
        File context = null;
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            context = chooser.getSelectedFile();
        }
        return context;
    }
//
//    public static void applyPatch(File patch, File context) {
//        try {
//            ContextualPatch cp = ContextualPatch.create(patch, context);
//            cp.patch(false);
//        } catch (PatchException ex) {
//            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
//        }
//    }
//
//    public static boolean isPatch(FileObject fob) throws IOException {
//        boolean isPatch = false;
//        Reader reader = new BufferedReader(new InputStreamReader(fob.getInputStream()));
//        try {
//            isPatch = (Patch.parse(reader).length > 0);
//        } finally {
//            reader.close();
//        }
//        return isPatch;
//    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    public static File getLargerContext() {
        FileObject openFile = getOpenFileObj();
        if (openFile != null) {
            File largerContext = getLargerContext(openFile);
            if (largerContext != null) {
                return largerContext;
            }
        }

        return getContextFromProjects();
    }

    public static File getContextFromProjects() {
        final OpenProjects projects = OpenProjects.getDefault();

        Project mainProject = projects.getMainProject();
        if (mainProject != null) {
            return getLargerContext(mainProject);       //null or non-null
        }

        Project[] openProjects = projects.getOpenProjects();
        if ((openProjects != null) && (openProjects.length == 1)) {
            return getLargerContext(openProjects[0]);
        }

        return null;
    }

    public static File getLargerContext(File file) {
        return getLargerContext(file, null);
    }

    public static File getLargerContext(FileObject fileObj) {
        return getLargerContext(null, fileObj);
    }

    public static File getLargerContext(File file, FileObject fileObj) {
        if ((file == null) && (fileObj == null)) {
            throw new IllegalArgumentException(
                    "both File and FileObject are null");               //NOI18N
        }

        assert (file == null)
               || (fileObj == null)
               || FileUtil.toFileObject(file).equals(fileObj);

        if (fileObj == null) {
            fileObj = getFileObjForFileOrParent(file);
        } else if (file == null) {
            file = FileUtil.toFile(fileObj);
        }

        if (fileObj == null) {
            return null;
        }
        if (!fileObj.isValid()) {
            return null;
        }

        Project parentProject = FileOwnerQuery.getOwner(fileObj);
        if (parentProject != null) {
            FileObject parentProjectFolder = parentProject.getProjectDirectory();
            if (parentProjectFolder.equals(fileObj) && (file != null)) {
                return file;
            }
            File folder = FileUtil.toFile(parentProjectFolder);
            if (folder != null) {
                return folder;
            }
        }

        if (fileObj.isFolder()) {
            return file;                        //whether it is null or non-null
        } else {
            fileObj = fileObj.getParent();
            assert fileObj != null;      //every non-folder should have a parent
            return FileUtil.toFile(fileObj);    //whether it is null or non-null
        }
    }

    private static FileObject getFileObjForFileOrParent(File file) {
        FileObject fileObj = FileUtil.toFileObject(file);
        if (fileObj != null) {
            return fileObj;
        }

        File closestParentFile = file.getParentFile();
        while (closestParentFile != null) {
            fileObj = FileUtil.toFileObject(closestParentFile);
            if (fileObj != null) {
                return fileObj;
            }
            closestParentFile = closestParentFile.getParentFile();
        }

        return null;
    }

    public static File getLargerContext(Project project) {
        FileObject projectFolder = project.getProjectDirectory();
        assert projectFolder != null;

        return FileUtil.toFile(projectFolder);
    }

    private static FileObject getOpenFileObj() {
        TopComponent activatedTopComponent = TopComponent.getRegistry()
                                             .getActivated();
        if (activatedTopComponent == null) {
            return null;
        }

        DataObject dataObj = activatedTopComponent.getLookup()
                             .lookup(DataObject.class);
        if ((dataObj == null) || !dataObj.isValid()) {
            return null;
        }

        return dataObj.getPrimaryFile();
    }

    public static void keepFocusedComponentVisible(JScrollPane scrollPane) {
        keepFocusedComponentVisible(scrollPane.getViewport().getView());
    }

    public static void keepFocusedComponentVisible(Component component) {
        FocusListener listener= getScrollingFocusListener();
        component.removeFocusListener(listener); // Making sure that it is not added twice
        component.addFocusListener(listener);
        if (component instanceof Container) {
            for (Component subComponent : ((Container)component).getComponents()) {
                keepFocusedComponentVisible(subComponent);
            }
        }
    }

    private static FocusListener scrollingFocusListener;
    private static FocusListener getScrollingFocusListener() {
        if (scrollingFocusListener == null) {
            scrollingFocusListener = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!e.isTemporary()) {
                        Component comp = e.getComponent();
                        Container cont = comp.getParent();
                        if (cont instanceof JViewport) {
                            // comp is JViewport's view;
                            // we want the viewport itself to be shown in this case
                            comp = cont;
                            cont = cont.getParent();
                        }
                        if (cont instanceof JComponent) {
                            ((JComponent)cont).scrollRectToVisible(comp.getBounds());
                        }
                    }
                }
            };
        }
        return scrollingFocusListener;
    }

    public static void logQueryEvent(String connector, String name, int count, boolean isKenai, boolean isAutoRefresh) {
        name = obfuscateQueryName(name);
        logBugtrackingEvents(USG_BUGTRACKING_QUERY, new Object[] {connector, name, count, isKenai, isAutoRefresh} );
    }

    public static void logAutoRefreshEvent(String connector, String queryName, boolean isKenai, boolean on) {
        queryName = obfuscateQueryName(queryName);
        logBugtrackingEvents(USG_BUGTRACKING_AUTOMATIC_REFRESH, new Object[] {connector, queryName, isKenai, on} );
    }

    public static synchronized void logBugtrackingUsage(Repository repository, String operation) {
        if (repository == null) {
            return;
        }
        String btType = getBugtrackingType(repository);
        if (btType == null) {
            return;
        }
        // log Kenai usage
        if (KenaiUtil.isKenai(repository)) {
            KenaiUtil.logKenaiUsage("ISSUE_TRACKING", btType); // NOI18N
        }
        if (operation == null) {
            return;
        }
        // log general bugtracking usage
        String paramStr = getParamString(btType, operation);
        if (loggedParams == null || !loggedParams.contains(paramStr)) {
            // not logged in this session yet
            LogRecord rec = new LogRecord(Level.INFO, USG_ISSUE_TRACKING);
            rec.setParameters(new Object[] { btType, operation });
            rec.setLoggerName(METRICS_LOG.getName());
            METRICS_LOG.log(rec);

            if (loggedParams == null) {
                loggedParams = new HashSet<String>();
            }
            loggedParams.add(paramStr);
        }
    }

    private static String getParamString(Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return ""; // NOI18N
        }
        if (parameters.length == 1) {
            return parameters[0].toString();
        }
        StringBuilder buf = new StringBuilder();
        for (Object p : parameters) {
            buf.append(p.toString());
        }
        return buf.toString();
    }

    private static String getBugtrackingType(Repository repository) {
        // XXX hack: there's no clean way to determine the type of bugtracking
        // from Repository (need BugtrackingConnector.getDisplayName)
        String clsName = repository.getClass().getName();
        if (clsName.contains(".bugzilla.")) { // NOI18N
            return "Bugzilla"; // NOI18N
        }
        if (clsName.contains(".jira.")) { // NOI18N
            return "Jira"; // NOI18N
        }
        return null;
    }

    public static int getColumnWidthInPixels(int widthInLeters, JComponent comp) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < widthInLeters; i++, sb.append("w"));                // NOI18N
        return getColumnWidthInPixels(sb.toString(), comp);
    }

    public static int getColumnWidthInPixels(String str, JComponent comp) {
        FontMetrics fm = comp.getFontMetrics(comp.getFont());
        return fm.stringWidth(str);
    }

    public static int getLongestWordWidth(String header, List<String> values, JComponent comp) {
        return getLongestWordWidth(header, values, comp, false);
    }

    public static int getLongestWordWidth(String header, List<String> values, JComponent comp, boolean regardIcon) {
        String[] valuesArray = values.toArray(new String[values.size()]);
        return getLongestWordWidth(header, valuesArray, comp, regardIcon);
    }

    public static int getLongestWordWidth(String header, String[] values, JComponent comp) {
        return getLongestWordWidth(header, values, comp, false);
    }

    public static int getLongestWordWidth(String header, String[] values, JComponent comp, boolean regardIcon) {
        int size = header.length();
        for (String s : values) {
            if(size < s.length()) {
                size = s.length();
            }
        }
        return getColumnWidthInPixels(size, comp) + (regardIcon ? 16 : 0);
    }

    /**
     * Logs bugtracking events
     *
     * @param key - the events key
     * @param parameters - the parameters for the given event
     */
    private static void logBugtrackingEvents(String key, Object[] parameters) {
        LogRecord rec = new LogRecord(Level.INFO, key);
        rec.setParameters(parameters);
        rec.setLoggerName(METRICS_LOG.getName());
        METRICS_LOG.log(rec);
    }

    private static String getMD5(String name) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");                          // NOI18N
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            return null;
        }
        digest.update(name.getBytes());
        byte[] hash = digest.digest();
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret.append(hex);
        }
        return ret.toString();
    }

    private static String obfuscateQueryName(String name) {
        if (name == null) {
            name = "Find Issues"; // NOI18N
        } else {
            name = getMD5(name);
        }
        return name;
    }

    // A11Y - Issues 163597 and 163598
    public static void fixFocusTraversalKeys(JComponent component) {
        Set<AWTKeyStroke> set = component.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        set = new HashSet<AWTKeyStroke>(set);
        set.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
    }

    public static void issue163946Hack(final JScrollPane scrollPane) {
        MouseWheelListener listener = new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (scrollPane.getVerticalScrollBar().isShowing()) {
                    if (e.getSource() != scrollPane) {
                        e.setSource(scrollPane);
                        scrollPane.dispatchEvent(e);
                    }
                } else {
                    scrollPane.getParent().dispatchEvent(e);
                }
            }
        };
        scrollPane.addMouseWheelListener(listener);
        scrollPane.getViewport().getView().addMouseWheelListener(listener);
    }

    public static void openPluginManager() {
        try {
            ClassLoader cl = Lookup.getDefault ().lookup (ClassLoader.class);
            Class<CallableSystemAction> clz = (Class<CallableSystemAction>) cl.loadClass("org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction");
            final CallableSystemAction a = CallableSystemAction.findObject(clz, true);
            a.putValue("InitialTab", "available"); // NOI18N
            Runnable inAWT = new Runnable() {
                public void run() {
                    a.performAction ();
                }
            };
            if (EventQueue.isDispatchThread()) {
                inAWT.run();
            } else {
                EventQueue.invokeLater(inAWT);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static Pattern netbeansUrlPattern = Pattern.compile("(https|http)://(([a-z]|\\d)+\\.)*([a-z]|\\d)*netbeans([a-z]|\\d)*(([a-z]|\\d)*\\.)+org(.*)"); // NOI18N
    /**
     * Determines wheter the given {@link Repository} is the
     * repository hosting netbeans or not
     *
     * @param repo
     * @return true if the given repository is the netbenas bugzilla, otherwise false
     */
    public static boolean isNbRepository(Repository repo) {
        String url = repo.getUrl();
        return isNbRepository(url);
    }

    public static boolean isNbRepository(String url) {
        boolean ret = netbeansUrlPattern.matcher(url).matches();
        if(ret) {
            return true;
        }
        String nbUrl = System.getProperty("netbeans.bugzilla.url");  // NOI18N
        if(nbUrl == null || nbUrl.equals("")) {                      // NOI18N
            return false;
        }
        return url.startsWith(nbUrl);
    }

    /**
     *
     * @param password
     * @param prefix
     * @param user
     * @param url
     * @throws MissingResourceException
     */
    public static void savePassword(String password, String prefix, String user, String url) throws MissingResourceException {
        if (password != null && !password.trim().equals("")) {                  // NOI18N
            Keyring.save(getPasswordKey(prefix, user, url), password.toCharArray(), NbBundle.getMessage(BugtrackingUtil.class, "password_keyring_description", url)); // NOI18N
        } else {
            Keyring.delete(getPasswordKey(prefix, user, url));
        }
    }

    /**
     *
     * @param scrambledPassword
     * @param keyPrefix
     * @param url
     * @param user
     * @return
     */
    public static char[] readPassword(String scrambledPassword, String keyPrefix, String user, String url) {
        if (!scrambledPassword.equals("")) {                                    // NOI18N
            return BugtrackingUtil.descramble(scrambledPassword).toCharArray();
        } else {
            char[] password = Keyring.read(getPasswordKey(keyPrefix, user, url));
            return password != null ? password : new char[0];
        }
    }
    
    private static String getPasswordKey(String prefix, String user, String url) {
        return (prefix != null ? prefix + "-" : "") + user + "@" + url;         // NOI18N
    }


    /**
     * Determines if the jira plugin is instaled or not
     *
     * @return true if jira plugin is installed, otherwise false
     */
    public static boolean isJiraInstalled() {
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            // XXX hack
            if(c.getClass().getName().startsWith("org.netbeans.modules.jira")) {    // NOI18N
                return true;
            }
        }
        return false;
    }

    public static void openQuery(final Query query, final Repository repository, final boolean suggestedSelectionOnly) {
        QueryAction.openQuery(query, repository, suggestedSelectionOnly);
    }

    public static void openIssue(File file, String issueId) {
        IssueAction.openIssue(file, issueId);
    }

    public static Map<String, List<RecentIssue>> getAllRecentIssues() {
        return BugtrackingManager.getInstance().getAllRecentIssues();
    }

    public static Collection<Issue> getRecentIssues(Repository repo) {
        return BugtrackingManager.getInstance().getRecentIssues(repo);
    }

    public static void closeQuery(Query query) {
        QueryAction.closeQuery(query);
    }

    public static void createIssue(Repository repo) {
        IssueAction.createIssue(repo);
    }

    public static String getPasswordLog(String psswd) {
        if(psswd == null) {
            return ""; // NOI18N
        }
        if("true".equals(System.getProperty("org.netbeans.modules.bugtracking.logPasswords", "false"))) { // NOI18N
            return psswd; 
        }
        return "******"; // NOI18N
    }
    
}
