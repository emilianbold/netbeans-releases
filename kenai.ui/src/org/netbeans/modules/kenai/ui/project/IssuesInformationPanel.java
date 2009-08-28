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
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.ProjectHandleImpl;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author petrdvorak
 */
public class IssuesInformationPanel extends javax.swing.JPanel implements RefreshableContentPanel {

    /** Creates new form IssuesInformationPanel */
    public IssuesInformationPanel() {
        initComponents();
        issuesInfoPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    issuesInfoPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    issuesInfoPane.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
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

    private String buildIssueInformation(KenaiProject instProj) throws KenaiException {
        String _appStr = String.format("<html><div class=\"section\"><h2>%s</h2>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_PROJECT_ISSUES")); //NOI18N
        KenaiFeature[] issueTrackers = instProj.getFeatures(Type.ISSUES);
        if (issueTrackers.length > 0) {
            KenaiFeature itrac = issueTrackers[0];
            String type = "kenai-small.png"; //NOI18N
            if (itrac.getService().equals(KenaiService.Names.BUGZILLA)) {
                type = "bugzilla-logo.png"; //NOI18N
            } else if (itrac.getService().equals(KenaiService.Names.JIRA)) {
                type = "jira-logo.png"; //NOI18N
            }
            _appStr += String.format("<table><tr><td width=\"50\"><img src=\"%s\"></td><td><h3>%s</h3></td></tr></table><br>", //NOI18N
                    IssuesInformationPanel.class.getResource("/org/netbeans/modules/kenai/ui/resources/" + type), //NOI18N
                    itrac.getDisplayName());
            _appStr += String.format("%s: %s<a href=\"%s\">%s</a>", //NOI18N
                    NbBundle.getMessage(IssuesInformationPanel.class, "MSG_ISSUE_TRACKER_ONLINE"), //NOI18N
                    kenaiProjectTopComponent.linkImageHTML,
                    itrac.getWebLocation(),
                    itrac.getWebLocation());
            _appStr += String.format("<br><br><h4>%s</h4>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_DID_YOU_FIND_ISSUE")); //NOI18N
            _appStr += "<table cellpadding=\"0\" cellspacing=\"0\"><tr><td>"; //NOI18N
            _appStr += String.format("<input id=\"find\" type=\"reset\" value=\"%s\"><br>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_FIND_ISSUE")); // NOI18N
            _appStr += "</td></tr><tr><td>"; //NOI18N
            _appStr += String.format("<input id=\"enter\" type=\"reset\" value=\"%s\"><br>", NbBundle.getMessage(IssuesInformationPanel.class, "MSG_NEW_REPORT")); //NOI18N
            _appStr += "</td></tr></table>";//NOI18N
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

                        public void actionPerformed(ActionEvent e) {
                            QueryAccessor.getDefault().getCreateIssueAction(new ProjectHandleImpl(instProj)).actionPerformed(e);
                        }
                    });
                    registerHTMLButton(doc, "find", new ActionListener() { //NOI18N

                        public void actionPerformed(ActionEvent e) {
                            QueryAccessor.getDefault().getFindIssueAction(new ProjectHandleImpl(instProj)).actionPerformed(e);
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
                issuesInfoPane.setText(String.format("<html><table><tr><td width=\"30\"><img src=\"%s\"></td><td>%s</td></tr></table></html>", //NOI18N
                        SourcesInformationPanel.class.getResource("/org/netbeans/modules/kenai/ui/resources/wait.gif"), //NOI18N
                        NbBundle.getMessage(SourcesInformationPanel.class, "MSG_WAIT"))); //NOI18N
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane issuesInfoPane;
    // End of variables declaration//GEN-END:variables

}
