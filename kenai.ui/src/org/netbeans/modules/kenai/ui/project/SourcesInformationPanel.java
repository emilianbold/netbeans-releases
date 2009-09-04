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
 * SourcesInformationPanel.java
 *
 * Created on Aug 26, 2009, 12:55:51 PM
 */

package org.netbeans.modules.kenai.ui.project;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultButtonModel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.GetSourcesFromKenaiAction;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl.ProjectAndFeature;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Dvorak (Petr.Dvorak@sun.com)
 */
public class SourcesInformationPanel extends javax.swing.JPanel implements RefreshableContentPanel {
    public static final int MAX_ENTRIES = 20;
    private final String WAIT_STRING = String.format("<html><table cellpadding=\"0\" border=\"0\" cellspacing=\"0\"><tr>" + //NOI18N
            "<td width=\"30\"><img src=\"%s\"></td><td>%s</td></tr></table></html>", //NOI18N
                        SourcesInformationPanel.class.getResource("/org/netbeans/modules/kenai/ui/resources/wait.gif"), //NOI18N
                        NbBundle.getMessage(SourcesInformationPanel.class, "MSG_WAIT")); //NOI18N

    /** Creates new form SourcesInformationPanel */
    public SourcesInformationPanel(final JScrollBar vbar) {
        initComponents();
        srcFeedPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    srcFeedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (e.getDescription().startsWith("http://") || e.getDescription().startsWith("https://")) { //NOI18N
                        srcFeedPane.setToolTipText(e.getDescription());
                    }
//                    else if (e.getDescription().startsWith("#")) { //NOI18N
//                        srcFeedPane.setToolTipText("Scroll down to this repository...");
//                    }
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    srcFeedPane.setCursor(Cursor.getDefaultCursor());
                    srcFeedPane.setToolTipText(""); //NOI18N
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//                    if (e.getDescription().startsWith("#repo")) { //NOI18N
//                        SwingUtilities.invokeLater(new Runnable() {
//
//                            public void run() {
//                                vbar.setValue(0);
//                                srcFeedPane.scrollToReference(e.getDescription().substring(1));
//                                Rectangle scrollVal = srcFeedPane.getVisibleRect();
//                                scrollVal.translate(0, 280); //Fix - scrollbar is not by the JEditorPane, but it is global - the header height must be added
//                                srcFeedPane.scrollRectToVisible(scrollVal);
//                            }
//                        });
//                        return;
//                    }
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


    private String addRepoHeaderWithButton(final KenaiFeature repo, String htmlID, int order) {
        String _appString = ""; //NOI18N
        if (repo.getService().equals(KenaiService.Names.SUBVERSION) || repo.getService().equals(KenaiService.Names.MERCURIAL)) {
            String repotype = "MSG_MERCURIAL";
            if (repo.getService().equals(KenaiService.Names.SUBVERSION)) {
                repotype = "MSG_SUBVERSION";
            }
            _appString += String.format("<a name=\"repo%d\"></a><table cellpadding=\"0\" border=\"0\" cellspacing=\"0\"><tr><td><h3>%s (%s)</h3></td><td width=\"200\" align=\"right\"><input type=\"reset\" id=\"%s\" value=\"%s\"></td></tr></table>", //NOI18N
                    order,
                    repo.getDisplayName(),
                    NbBundle.getMessage(SourcesInformationPanel.class, repotype),
                    htmlID,
                    NbBundle.getMessage(SourcesInformationPanel.class, "MSG_GET_THIS_REPO")); //NOI18N
        } else {
            String repotype = "MSG_UNKNOWN_SCM";
            if (repo.getService().equals(KenaiService.Names.GIT)) {
                repotype = "MSG_GIT";
            }
            _appString += String.format("<a name=\"repo%d\"></a><h3>%s (%s)</h3>", order, repo.getDisplayName(), NbBundle.getMessage(SourcesInformationPanel.class, repotype)); //NOI18N
        }
        return _appString;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        srcFeedPane = new javax.swing.JTextPane();

        setLayout(new java.awt.BorderLayout());

        srcFeedPane.setContentType(org.openide.util.NbBundle.getMessage(SourcesInformationPanel.class, "SourcesInformationPanel.srcFeedPane.contentType")); // NOI18N
        srcFeedPane.setEditable(false);
        srcFeedPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                srcFeedPaneFocusGained(evt);
            }
        });
        add(srcFeedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void srcFeedPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_srcFeedPaneFocusGained
        srcFeedPane.getCaret().setVisible(false); //MacOSX hack
    }//GEN-LAST:event_srcFeedPaneFocusGained

    private List<String> registeredButtonID = new LinkedList<String>();
    private HashMap<String, KenaiFeature> repoMap = new HashMap<String, KenaiFeature>();

    public String loadRepoFeeds(final KenaiProject proj) throws DOMException {
        registeredButtonID.clear();
        repoMap.clear();
        String _appString = "<div class=\"section\">"; //NOI18N
        try {
            KenaiFeature[] repos = proj.getFeatures(Type.SOURCE);
            if (repos.length == 0) {
                return String.format("<div class=\"section\"><i>%s</i></div>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_NO_REPOS")); //NOI18N
            }
            if (repos.length > 1) {
                _appString += String.format("<h3>%s</h3>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_REPO_LIST")); //NOI18N
                for (int k = 0; k < repos.length; k++) {
                    if (Thread.interrupted()) {
                        return WAIT_STRING;
                    }
                    final KenaiFeature repo = repos[k];
                    String repotype = "MSG_UNKNOWN_SCM";
                    if (repo.getService().equals(KenaiService.Names.SUBVERSION)) {
                        repotype = "MSG_SUBVERSION";
                    } else if (repo.getService().equals(KenaiService.Names.GIT)) {
                        repotype = "MSG_GIT";
                    } else if (repo.getService().equals(KenaiService.Names.MERCURIAL)) {
                        repotype = "MSG_MERCURIAL";
                    }
                    _appString += String.format("<div class=\"item\">-&nbsp;%s <i>(%s)</i></div>", //NOI18N
                            repo.getDisplayName(), NbBundle.getMessage(SourcesInformationPanel.class, repotype));
                }
            }
            for (int k = 0; k < repos.length; k++) {
                if (Thread.interrupted()) {
                    return WAIT_STRING;
                }
                final KenaiFeature repo = repos[k];

                DocumentBuilder dbf = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                String base = Kenai.getDefault().getUrl().toString().replaceFirst("https://", "http://"); //NOI18N
                String urlStr = base + repo.getWebLocation().getPath().replaceAll("/show$", "/history.atom"); //NOI18N
                int entriesCount = 0;
                NodeList entries = null;
                String htmlID = repo.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + k + "__" + proj.getName().replace('-', '_'); //NOI18N
                repoMap.put(htmlID, repo);
                registeredButtonID.add(htmlID);
                try {
                    new URL(urlStr).openStream(); // just to fail quickly if URL is invalid...
                    if (Thread.interrupted()) {
                        return WAIT_STRING;
                    }
                    Document doc = dbf.parse(urlStr);
                    entries = doc.getElementsByTagName("entry"); //NOI18N
                    entriesCount = entries.getLength();
                } catch (FileNotFoundException e) {
                    _appString += "<br>" + addRepoHeaderWithButton(repo, htmlID, k); //NOI18N
                    _appString += String.format("<i>%s</i><br>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_REPO_NOT_ON_KENAI")); //NOI18N
                    _appString += String.format("<p>&nbsp;&nbsp;&nbsp;&nbsp;%s&nbsp;<a href=\"%s\">%s</a></p>", kenaiProjectTopComponent.linkImageHTML, repo.getWebLocation(), repo.getWebLocation()); //NOI18N
                    _appString += "<br><div style=\"height: 0px; font-size: 0px; border-width: 1px; border-style: solid; border-color: silver\"></div><br>"; //NOI18N
                    continue;
                } catch (IOException e) {
                    _appString += "<br>" + addRepoHeaderWithButton(repo, htmlID, k); //NOI18N
                    _appString += String.format("<i>%s</i><br>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_CANNOT_OPEN_FEED")); //NOI18N
                    _appString += String.format("<p>&nbsp;&nbsp;&nbsp;&nbsp;%s&nbsp;<a href=\"%s\">%s</a></p>", kenaiProjectTopComponent.linkImageHTML, repo.getWebLocation(), repo.getWebLocation()); //NOI18N
                    _appString += "<br><div style=\"height: 0px; font-size: 0px; border-width: 1px; border-style: solid; border-color: silver\"></div><br>"; //NOI18N
                    continue;
                }
                if (Thread.interrupted()) {
                    return WAIT_STRING;
                }
                _appString += addRepoHeaderWithButton(repo, htmlID, k);
                if (Thread.interrupted()) {
                    return WAIT_STRING;
                }
                _appString += "<table cellspacing=\"0\" border=\"0\" cellpadding=\"0\">"; //NOI18N start table for each repository
                _appString += String.format("<tr><td colspan=\"4\"><h4>%s</h4></td></tr>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_RECENT_CHANGES")); //NOI18N
                if (entriesCount > 0 && entries != null) {
                    _appString += "<tr>"; //NOI18N
                    for (int i = 0; i < entriesCount && i < MAX_ENTRIES; i++) {
                        if (Thread.interrupted()) {
                            return WAIT_STRING;
                        }
                        Node entry = entries.item(i);
                        NodeList entryProps = entry.getChildNodes();
                        String title = null;
                        String updated = ""; //NOI18N
                        String content = String.format("<i>%s</i>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_NO_COMMIT_MSG")); //NOI18N
                        String href = null;
                        for (int j = 0; j < entryProps.getLength(); j++) {
                            if (Thread.interrupted()) {
                                return WAIT_STRING;
                            }
                            Node elem = entryProps.item(j);
                            if (elem.getNodeName().equals("title")) { //NOI18N - get title of the topic
                                Node firstChild = elem.getFirstChild();
                                if (firstChild != null) {
                                    title = firstChild.getNodeValue();
                                }
                            } else if (elem.getNodeName().equals("updated")) { //NOI18N - get update date of the topic
                                updated = elem.getFirstChild().getNodeValue();
                            } else if (elem.getNodeName().equals("link")) { //NOI18N - found link of the topic, get href...
                                href = elem.getAttributes().getNamedItem("href").getNodeValue(); //NOI18N
                                if (!href.startsWith(base)) {
                                    href = base + href;
                                }
                            } else if (elem.getNodeName().equals("content")) { //NOI18N get title of the topic
                                //NOI18N get title of the topic
                                Node firstChild = elem.getFirstChild();
                                if (firstChild != null) {
                                    content = firstChild.getNodeValue();
                                }
                            }
                        }
                        if (title != null && href != null) {
                            // Not correct - the Atom feed contains a timestamp (RFC 3339) with T/Z characters,
                            // i.e., that should be interpretted better than by replacing...
                            _appString += String.format("<td style=\"padding-top: 4px;\" valign=\"top\">%s</td>" + //NOI18N
                                    "<td valign=\"top\" style=\"padding-left: 3px;\"><a title=\"test\" href=\"%s\">%s</a></td>" + //NOI18N
                                    "<td style=\"padding-left: 4px\" valign=\"top\"><i>%s:</i></td>" + //NOI18N
                                    "<td style=\"padding-left: 4px\" valign=\"top\">%s</td></tr>", //NOI18N
                                    kenaiProjectTopComponent.linkImageHTML, href, title, updated.replaceAll("[a-zA-Z]", "&nbsp;"), content); //NOI18N
                        }
                    }
                } else {
                    _appString += String.format("<tr><td colspan=\"4\"><i>%s</i></td></tr>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_NO_CHANGES")); //NOI18N
                }
                _appString += "</table><br><div style=\"height: 0px; font-size: 0px; border-width: 1px; border-style: solid; border-color: silver\"></div><br>"; //NOI18N
            }
            _appString += "</div>"; //NOI18N
            return _appString;
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane srcFeedPane;
    // End of variables declaration//GEN-END:variables

    public void resetContent(final KenaiProject instProj) {
        // Style the document in order to look nice
        Font font = UIManager.getFont("Label.font"); // NOI18N
        String bodyRule = "body { background-color: white; font-family: " + font.getFamily() + "; " + // NOI18N
                "font-size: " + font.getSize() + "pt; padding: 10px;}"; // NOI18N
        final StyleSheet styleSheet = ((HTMLDocument) srcFeedPane.getDocument()).getStyleSheet();
        styleSheet.addRule(bodyRule);
        styleSheet.addRule("div.section {margin-bottom: 10px;}"); //NOI18N
        styleSheet.addRule("div.item {margin-bottom: 5px;}"); //NOI18N
        styleSheet.addRule("i {color: gray}"); //NOI18N
        styleSheet.addRule("h2 {color: rgb(0,22,103)}; font-size: 18pt"); //NOI18N
        styleSheet.addRule("h3 {font-size: 15pt"); //NOI18N
        styleSheet.addRule("h4 {font-size: 12pt"); //NOI18N
        styleSheet.addRule("h3 a {border: 0; font-weight: normal; text-decoration: none; font-size: smaller}"); //NOI18N
        styleSheet.addRule("h3 a img {color: white; border: 0}"); //NOI18N

        final String str = loadRepoFeeds(instProj);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (str != null) {
                    srcFeedPane.setText(String.format("<html><h2>%s</h2>%s</html>", NbBundle.getMessage(SourcesInformationPanel.class, "MSG_PROJECT_SOURCES"), str)); //NOI18N
                    srcFeedPane.validate();
                    srcFeedPane.setCaretPosition(0);
                    for (final String id : registeredButtonID) {
                        registerHTMLButton((HTMLDocument)srcFeedPane.getDocument(), id, new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                new GetSourcesFromKenaiAction(new ProjectAndFeature(instProj.getName(), repoMap.get(id), null), null).actionPerformed(e);
                            }
                        });
                    }
                }
            }

        });
    }

    public void clearContent() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                srcFeedPane.setText(WAIT_STRING); //NOI18N
            }
        });
    }

}
