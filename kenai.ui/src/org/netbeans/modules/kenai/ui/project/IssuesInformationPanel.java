/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * IssuesInformationPanel.java
 *
 * Created on Aug 28, 2009, 1:29:13 PM
 */

package org.netbeans.modules.kenai.ui.project;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.DefaultButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.KenaiPopupActionsProvider;
import org.netbeans.modules.kenai.ui.ProjectHandleImpl;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author petrdvorak
 */
public class IssuesInformationPanel extends javax.swing.JPanel implements RefreshableContentPanel {

    private final String WAIT_STRING = String.format("<html><table cellpadding=\"0\" border=\"0\" cellspacing=\"0\"><tr><td width=\"30\"><img src=\"%s\"></td><td>%s</td></tr></table></html>", //NOI18N
                        SourcesInformationPanel.class.getResource("/org/netbeans/modules/kenai/ui/resources/wait.gif"), //NOI18N
                        NbBundle.getMessage(SourcesInformationPanel.class, "MSG_WAIT_ISSUES"));

    private KenaiProject instPr = null;
    private final KenaiIssueAccessor issueAccessor;

    /** Creates new form IssuesInformationPanel */
    public IssuesInformationPanel(KenaiProject proj) {
        initComponents();
        instPr = proj;
        issueAccessor = KenaiIssueAccessor.getDefault();
        issuesInfoPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    issuesInfoPane.setToolTipText(e.getDescription());
                    issuesInfoPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    issuesInfoPane.setToolTipText(""); //NOI18N
                    issuesInfoPane.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (issueAccessor != null && e.getDescription().startsWith("issue:")) { //NOI18N
                        final String issueNumber = e.getDescription().substring(6);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                issueAccessor.open(instPr, issueNumber);
                            }
                        });
                        return;
                    }
                    URLDisplayer.getDefault().showURL(e.getURL());
                    return;
                }
            }
        });
    }

    private void registerHTMLButton(HTMLDocument htm, String elementID, ActionListener action) {
        Element e = htm.getElement(elementID);
        if (e != null) {
            AttributeSet attr = e.getAttributes();
            Enumeration enu = attr.getAttributeNames();
            while (enu.hasMoreElements()) {
                Object name = enu.nextElement();
                Object value = attr.getAttribute(name);
                if ("model".equals(name.toString())) { //NOI18N
                    final DefaultButtonModel model = (DefaultButtonModel) value;
                    model.setActionCommand(elementID);
                    model.addActionListener(action);
                }
            }
        }
    }

    private String getRecentIssuesTable(KenaiProject instProj) {
        IssueHandle[] recentIssues = issueAccessor == null ? null : issueAccessor.getRecentIssues(instProj);
        if (recentIssues == null || recentIssues.length == 0) {
            return ""; //NOI18N
        }
        String issueTable = String.format("<br><h4>%s</h4><table>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_RECENTLY_OPENED")); //NOI18N
        for (int i = 0; i < recentIssues.length; i++) {
            IssueHandle issue = recentIssues[i];
            issueTable += String.format("<tr><td><a href=\"issue:%s\">%s</a></td><td>%s</td></tr>", issue.getID(), issue.getID(), issue.getDisplayName()); //NOI18N
        }
        issueTable += "</table>"; //NOI18N
        return issueTable;
    }

    private String buildIssueInformation(KenaiProject instProj) throws KenaiException {
        String _appStr = String.format("<html><div class=\"section\"><h2>%s</h2>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_PROJECT_ISSUES")); //NOI18N
        KenaiFeature[] issueTrackers = instProj.getFeatures(Type.ISSUES);
        if (issueTrackers.length > 0) {
            if (Thread.interrupted()) {
                return WAIT_STRING;
            }
            KenaiFeature itrac = issueTrackers[0];
            String type = "external.png"; //NOI18N
            if (itrac.getService().equals(KenaiService.Names.BUGZILLA)) {
                type = "bugzilla-logo.png"; //NOI18N
            } else if (itrac.getService().equals(KenaiService.Names.JIRA)) {
                type = "jira-logo.png"; //NOI18N
            }
            if (Thread.interrupted()) {
                return WAIT_STRING;
            }
            _appStr += String.format("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\"><tr><td><img src=\"%s\"></td><td width=\"10px\"></td><td><h3>%s</h3></td></tr></table><br>", //NOI18N
                    IssuesInformationPanel.class.getResource("/org/netbeans/modules/kenai/ui/resources/" + type), //NOI18N
                    itrac.getDisplayName() + (type.equals("external.png")?(" <i>(" + NbBundle.getMessage(IssuesInformationPanel.class, "MSG_UNKNOWN_IT") + ")</i>"):"")); //NOI18N
            if (Thread.interrupted()) {
                return WAIT_STRING;
            }
            if (itrac.getService().equals(KenaiService.Names.BUGZILLA) || itrac.getService().equals(KenaiService.Names.JIRA)) {
                _appStr += String.format("<h4>%s</h4>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_DID_YOU_FIND_ISSUE")); //NOI18N
                _appStr += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>"; //NOI18N
                _appStr += String.format("<input id=\"find\" type=\"reset\" value=\"%s\"><br>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_FIND_ISSUE")); // NOI18N
                _appStr += "</td></tr><tr><td>"; //NOI18N
                _appStr += String.format("<input id=\"enter\" type=\"reset\" value=\"%s\"><br>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_NEW_REPORT")); //NOI18N
                _appStr += "</td></tr></table><br>";//NOI18N
            }
            _appStr += String.format("%s:<br><p>&nbsp;&nbsp;&nbsp;&nbsp;%s&nbsp;<a href=\"%s\">%s</a></p>", //NOI18N
                    NbBundle.getMessage(IssuesInformationPanel.class, "MSG_ISSUE_TRACKER_ONLINE"), //NOI18N
                    kenaiProjectTopComponent.linkImageHTML,
                    itrac.getWebLocation(),
                    itrac.getWebLocation());
            _appStr += getRecentIssuesTable(instProj);
        } else {
            //There are no issue trackers
            _appStr += String.format("<i>%s</i>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_NO_ISSUE_TRACKERS")); //NOI18N
        }
        _appStr += "</div></html>"; //NOI18N
        return _appStr;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        issuesInfoPane = new javax.swing.JEditorPane();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(0, 800));
        setLayout(new java.awt.BorderLayout());

        issuesInfoPane.setContentType(org.openide.util.NbBundle.getMessage(IssuesInformationPanel.class, "IssuesInformationPanel.issuesInfoPane.contentType")); // NOI18N
        issuesInfoPane.setEditable(false);
        issuesInfoPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                issuesInfoPaneFocusGained(evt);
            }
        });
        add(issuesInfoPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void issuesInfoPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_issuesInfoPaneFocusGained
        issuesInfoPane.getCaret().setVisible(false);
    }//GEN-LAST:event_issuesInfoPaneFocusGained

    public void resetContent(final KenaiProject instProj) {
        // Style the document in order to look nice
        Font font = UIManager.getFont("Label.font"); // NOI18N
        String bodyRule = "body { background-color: white; font-family: " + font.getFamily() + "; " + // NOI18N
                "font-size: " + font.getSize() + "pt; padding: 10px;}"; // NOI18N
        final StyleSheet styleSheet = ((HTMLDocument) issuesInfoPane.getDocument()).getStyleSheet();
        styleSheet.addRule(bodyRule);
        styleSheet.addRule("div.section {margin-bottom: 10px;}"); //NOI18N
        styleSheet.addRule("i {color: gray}"); //NOI18N
        styleSheet.addRule("h2 {color: rgb(0,22,103)}; font-size: 18pt"); //NOI18N
        styleSheet.addRule("h3 {font-size: 15pt"); //NOI18N
        styleSheet.addRule("h4 {font-size: 12pt"); //NOI18N
        try {
            final String _appStr = buildIssueInformation(instProj);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    HTMLDocument doc = (HTMLDocument) issuesInfoPane.getDocument();
                    issuesInfoPane.setText(_appStr);
                    issuesInfoPane.validate();
                    issuesInfoPane.setCaretPosition(0);
                    registerHTMLButton(doc, "enter", new ActionListener() { //NOI18N

                        public void actionPerformed(final ActionEvent e) {
                            final ProjectHandleImpl pHandle = new ProjectHandleImpl(instProj);
                            final DashboardSupport<KenaiProject> dashboard = KenaiServer.getDashboard(pHandle);
                            Utilities.addProject(pHandle, false, false);
                            Utilities.getRequestProcessor().post(new Runnable() {

                                public void run() {
                                    ProgressHandle h = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupActionsProvider.class, "CONTACTING_ISSUE_TRACKER"));
                                    h.start();
                                    dashboard.getDashboardProvider().getQueryAccessor(KenaiProject.class).getCreateIssueAction(pHandle).actionPerformed(e);
                                    h.finish();
                                }
                            });
                        }
                    });
                    registerHTMLButton(doc, "find", new ActionListener() { //NOI18N

                        public void actionPerformed(final ActionEvent e) {
                            try {
                                if (instProj.getFeatures(Type.ISSUES).length > 0) {
                                    final ProjectHandleImpl pHandle = new ProjectHandleImpl(instProj);
                                    final DashboardSupport<KenaiProject> dashboard = KenaiServer.getDashboard(pHandle);                                    
                                    Utilities.addProject(pHandle, false, false);
                                    Utilities.getRequestProcessor().post(new Runnable() {

                                        public void run() {
                                            ProgressHandle h = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupActionsProvider.class, "CONTACTING_ISSUE_TRACKER"));
                                            h.start();
                                            dashboard.getDashboardProvider().getQueryAccessor(KenaiProject.class).getFindIssueAction(pHandle).actionPerformed(e);
                                            h.finish();
                                        }
                                    });
                                }
                            } catch (KenaiException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            });
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void clearContent() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                issuesInfoPane.setText(WAIT_STRING); //NOI18N
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane issuesInfoPane;
    // End of variables declaration//GEN-END:variables

}
