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
 * ForumsAndMailingListsPanel.java
 *
 * Created on Aug 19, 2009, 6:07:58 PM
 */

package org.netbeans.modules.kenai.ui.project;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.DefaultButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
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
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.collab.chat.ChatTopComponent;
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
public class ForumsAndMailingListsPanel extends javax.swing.JPanel implements RefreshableContentPanel {
    public static final String CHAT_BUTTON = "CHAT_BUTTON"; //NOI18N

    private final String WAIT_STRING = String.format("<html><table cellpadding=\"0\" border=\"0\" cellspacing=\"0\"><tr><td width=\"30\"><img src=\"%s\"></td><td>%s</td></tr></table></html>", //NOI18N
                        SourcesInformationPanel.class.getResource("/org/netbeans/modules/kenai/ui/resources/wait.gif"), //NOI18N
                        NbBundle.getMessage(SourcesInformationPanel.class, "MSG_WAIT"));

    public ForumsAndMailingListsPanel() {
        initComponents();
        commChannelsDisplayer.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    commChannelsDisplayer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    commChannelsDisplayer.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URLDisplayer.getDefault().showURL(e.getURL());
                    return;
                }
            }
        });
    }

    public String getChatRoomHTML(final KenaiProject instProj) {
        String innerStr = ""; //NOI18N
        try {
            if (Kenai.getDefault().getPasswordAuthentication() != null && Kenai.getDefault().getMyProjects().contains(instProj)) {
                KenaiFeature[] chats = instProj.getFeatures(Type.CHAT);
                innerStr += String.format("<div class=\"section\"><h2>%s</h2>", NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_CHATROOM")); //NOI18N
                if (chats.length > 0) {
                    innerStr += String.format("<input type=\"reset\" id=\"" + CHAT_BUTTON + "\" value=\"%s\"><br>", NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_ENTER_CHATROOM")); //NOI18N
                } else {
                    innerStr += String.format("<i>%s</i><br>", NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_NO_CHAT")); //NOI18N
                }
            }
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        innerStr += "</div>"; //NOI18N
        return innerStr;
    }

    public String getForumsHTML(final KenaiProject instProj) {
        KenaiFeature[] forums = null;
        String innerStr = ""; //NOI18N
        try {
            forums = instProj.getFeatures(Type.FORUM);
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (forums.length > 0) {
            innerStr = String.format("<div class=\"section\"><h2>%s</h2>", NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_FORUMS")); //NOI18N
            for (int i = 0; i < forums.length; i++) {
                KenaiFeature forum = forums[i];
                innerStr += String.format("<div class=\"item\">%s&nbsp;<a href=\"%s\">%s</a> - <i>%s</i></div>",
                        kenaiProjectTopComponent.linkImageHTML,
                        forum.getWebLocation(),
                        forum.getDisplayName(),
                        forum.getWebLocation()); //NOI18N
            }
            innerStr += "</div>"; //NOI18N
        }
        return innerStr;
    }

    public String getMailingListsHTML(final KenaiProject instProj) {
        KenaiFeature[] mails = null;
        String innerStr = ""; //NOI18N
        try {
            mails = instProj.getFeatures(Type.LISTS);
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (mails.length > 0) {
            innerStr += String.format("<div class=\"section\"><h2>%s</h2>", NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_MAILING_LISTS")); //NOI18N
            for (int i = 0; i < mails.length; i++) {
                KenaiFeature mail = mails[i];
                innerStr += String.format("<div class=\"item\">%s&nbsp;<a href=\"%s\">%s</a> - <i>%s</i></div>",
                        kenaiProjectTopComponent.linkImageHTML,
                        mail.getWebLocation(),
                        mail.getDisplayName(),
                        mail.getWebLocation()); //NOI18N
            }
            innerStr += "</div>"; //NOI18N
        }
        return innerStr;
    }

    public void loadActiveTopics(final KenaiProject proj) throws DOMException {
        
        try {
            DocumentBuilder dbf = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String base = Kenai.getDefault().getUrl().toString().replaceFirst("https://", "http://"); //NOI18N
            String urlStr = base + "/projects/" + proj.getName() + "/forums?format=atom"; //NOI18N
            int entriesCount = 0;
            NodeList entries = null;
            if (Thread.interrupted()) {
                clearContent();
                return;
            }
            try {
                Document doc = dbf.parse(urlStr);
                if (Thread.interrupted()) {
                    clearContent();
                    return;
                }
                entries = doc.getElementsByTagName("entry"); //NOI18N
                entriesCount = entries.getLength();
            } catch (FileNotFoundException e) {
                // url does not exist?
            } catch (IOException e) {
                // url does not exist?
            }
            String _appString = "<div class=\"section\">"; //NOI18N
            if (entriesCount > 0 && entries != null) {
                for (int i = 0; i < entriesCount; i++) {
                    if (Thread.interrupted()) {
                        clearContent();
                        return;
                    }
                    Node entry = entries.item(i);
                    NodeList entryProps = entry.getChildNodes();
                    String title = null;
                    String content = null;
                    String href = null;
                    for (int j = 0; j < entryProps.getLength(); j++) {
                        if (Thread.interrupted()) {
                            clearContent();
                            return;
                        }
                        Node elem = entryProps.item(j);
                        if (elem.getNodeName().equals("title")) { //NOI18N - get title of the topic
                            title = elem.getFirstChild().getNodeValue();
                        } else if (elem.getNodeName().equals("link")) { //NOI18N - found link of the topic, get href...
                            href = elem.getAttributes().getNamedItem("href").getNodeValue(); //NOI18N
                            if (!href.startsWith(base)) {
                                href = base + href;
                            }
                        } else if (elem.getNodeName().equals("content")) { //NOI18N get title of the topic
                            content = elem.getFirstChild().getNodeValue();
                        }
                    }
                    if (title != null && href != null) {
                        _appString += String.format("%s&nbsp;<a href=\"%s\">%s</a><br>", kenaiProjectTopComponent.linkImageHTML, href, title); //NOI18N
                    }
                    if (content != null) {
                        _appString += String.format("<i>%s</i><br><br>", content); //NOI18N
                    }
                }
                _appString += "</div>"; //NOI18N
                if (Thread.interrupted()) {
                    clearContent();
                    return;
                }
                final String appString = _appString;
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            Element insertionPoint = ((HTMLDocument) commChannelsDisplayer.getDocument()).getElement("DYN_CONTENT"); //NOI18N
                            if (insertionPoint != null) {
                                ((HTMLDocument) commChannelsDisplayer.getDocument()).insertAfterStart(insertionPoint, appString);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } else {
                final String appString = _appString + "<i>" + NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_NO_ACTIVE_TOPICS") + "</i></div>"; //NOI18N
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            Element insertionPoint = ((HTMLDocument) commChannelsDisplayer.getDocument()).getElement("DYN_CONTENT"); //NOI18N
                            if (insertionPoint != null) {
                                ((HTMLDocument) commChannelsDisplayer.getDocument()).insertAfterStart(insertionPoint, appString);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    public void resetContent(final KenaiProject instProj) {
        // Style the document in order to look nice
        Font font = UIManager.getFont("Label.font"); // NOI18N
        String bodyRule = "body { background-color: white; font-family: " + font.getFamily() + "; " + // NOI18N
                "font-size: " + font.getSize() + "pt; padding: 10px;}"; // NOI18N
        final StyleSheet styleSheet = ((HTMLDocument) commChannelsDisplayer.getDocument()).getStyleSheet();
        styleSheet.addRule(bodyRule);
        styleSheet.addRule("div.section {margin-bottom: 10px;}"); //NOI18N
        styleSheet.addRule("div.item {margin-bottom: 5px;}"); //NOI18N
        styleSheet.addRule("i {color: gray}"); //NOI18N
        styleSheet.addRule("h2 {color: rgb(0,22,103)}"); //NOI18N

        String innerStr = "<html>"; //NOI18N

        innerStr += getMailingListsHTML(instProj);
        if (Thread.interrupted()) {
            clearContent();
            return;
        }
        innerStr += getChatRoomHTML(instProj);
        if (Thread.interrupted()) {
            clearContent();
            return;
        }
        innerStr += getForumsHTML(instProj);
        if (Thread.interrupted()) {
            clearContent();
            return;
        }
        innerStr += String.format("<h2>%s</h2><div id=\"DYN_CONTENT\"></div>", NbBundle.getMessage(ForumsAndMailingListsPanel.class, "MSG_ACTIVE_FORUMS")); //NOI18N
        if (Thread.interrupted()) {
            clearContent();
            return;
        }
        innerStr += "</html>"; //NOI18N
        if (Thread.interrupted()) {
            clearContent();
            return;
        }
        final String _istr = innerStr;

        // Render the content and register an action to a HTML button in JEditorPane
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                commChannelsDisplayer.setText(_istr);
                commChannelsDisplayer.validate();
                commChannelsDisplayer.setCaretPosition(0);
                HTMLDocument htm = (HTMLDocument) commChannelsDisplayer.getDocument();
                Element e = htm.getElement(CHAT_BUTTON);
                if (e != null) {
                    AttributeSet attr = e.getAttributes();
                    Enumeration enu = attr.getAttributeNames();
                    while (enu.hasMoreElements()) {
                        Object name = enu.nextElement();
                        Object value = attr.getAttribute(name);
                        if ("model".equals(name.toString())) { //NOI18N
                            final DefaultButtonModel model = (DefaultButtonModel) value;
                            model.setActionCommand(CHAT_BUTTON);
                            model.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {
                                    ChatTopComponent ct = ChatTopComponent.findInstance();
                                    ct.open();
                                    ct.requestActive();
                                    ct.setActiveGroup(instProj.getName());
                                }
                            });
                        }
                    }
                }
            }
        });
        loadActiveTopics(instProj);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        commChannelsDisplayer = new javax.swing.JEditorPane();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.BorderLayout());

        commChannelsDisplayer.setContentType(org.openide.util.NbBundle.getMessage(ForumsAndMailingListsPanel.class, "ForumsAndMailingListsPanel.commChannelsDisplayer.contentType")); // NOI18N
        commChannelsDisplayer.setEditable(false);
        commChannelsDisplayer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                commChannelsDisplayerFocusGained(evt);
            }
        });
        add(commChannelsDisplayer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void commChannelsDisplayerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commChannelsDisplayerFocusGained
        commChannelsDisplayer.getCaret().setVisible(false); //MacOSX hack
    }//GEN-LAST:event_commChannelsDisplayerFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane commChannelsDisplayer;
    // End of variables declaration//GEN-END:variables

    public void clearContent() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                commChannelsDisplayer.setText(WAIT_STRING); //NOI18N
            }
        });
    }

}
