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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.versioning.history.AbstractSummaryView.ActionNode;
import org.netbeans.modules.versioning.history.AbstractSummaryView.EventNode;
import org.netbeans.modules.versioning.history.AbstractSummaryView.LogEntry;
import org.netbeans.modules.versioning.history.AbstractSummaryView.LogEntryNode;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.Hyperlink;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class SummaryCellRenderer implements TreeCellRenderer {
    private static final double DARKEN_FACTOR = 0.95;

    private final AbstractSummaryView master;
    private final Map<String, KenaiUser> kenaiUsersMap;
    private final VCSHyperlinkSupport linkerSupport;

    private Color selectionBackgroundColor = new JList().getSelectionBackground();
    private String selectionBackground = getColorString(selectionBackgroundColor);
    private String selectionForeground = getColorString(new JList().getSelectionForeground());

    private final String hiliteForegroundColor;
    private final String hiliteBackgroundColor;
    private final Color normalBackgroundColor = darker(UIManager.getColor("List.background"));
    private final String normalForeground = getColorString(UIManager.getColor("List.foreground"));
    private final String normalBackground = getColorString(normalBackgroundColor);
    private final String authorColor = getColorString(UIManager.getColor("TextPane.inactiveForeground"));
    private final String dateColor = authorColor;
    private final String pathColor = authorColor;
    private final String linkColor = getColorString(Color.BLUE);

    private RevisionRenderer cr = new RevisionRenderer();
    private EventRenderer rr = new EventRenderer();
    private ActionRenderer ar = new ActionRenderer();
    private DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();

    private int handleWidth = -1;
    private int oneLineHeight;
    private AttributeSet searchHiliteAttrs;

    private final String hiliteMessage;

    private double authorMaxWidth;
    private double dateMaxWidth;
    private double revisionMaxWidth;

    public SummaryCellRenderer(AbstractSummaryView master, VCSHyperlinkSupport linkerSupport, List<LogEntry> results, Map<String, KenaiUser> kenaiUsersMap) {
        this.master = master;
        this.hiliteMessage = master.getMessage();
        this.kenaiUsersMap = kenaiUsersMap;
        this.linkerSupport = linkerSupport;

        FontColorSettings fcs = (FontColorSettings) MimeLookup.getMimeLookup("text/x-java").lookup(FontColorSettings.class); // NOI18N
        searchHiliteAttrs = fcs.getFontColors("highlight-search"); // NOI18N

        Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
        hiliteBackgroundColor = c != null ? getColorString(c) : null;
        c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
        hiliteForegroundColor = c != null ? getColorString(c) : null;

        computeFieldSizes(results);
    }

    private void computeFieldSizes(List<LogEntry> results) {
        ArrayList newResults = new ArrayList(results.size());
        JTree t = new JTree();
        FontMetrics fm =t.getFontMetrics(t.getFont());
        for (LogEntry entry : results) {
            newResults.add(entry);
            double w = SwingUtilities.computeStringWidth(fm, entry.getAuthor());
            if(w > authorMaxWidth) authorMaxWidth = w;
            w = SwingUtilities.computeStringWidth(fm, entry.getDate());
            if(w > dateMaxWidth) dateMaxWidth = w;
            w = SwingUtilities.computeStringWidth(fm, entry.getRevision());
            if(w > revisionMaxWidth) revisionMaxWidth = w;
        }
    }


    private Color darker(Color c) {
        return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0),
             Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
             Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
    }

    private static String getColorString(Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); // NOI18N
    }

    private static String getHex(int i) {
        String hex = Integer.toHexString(i & 0x000000FF);
        if (hex.length() == 1) {
            hex = "0" + hex; // NOI18N
        }
        return hex;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if(handleWidth < 0) {
            TreeUI tvui = tree.getUI();
            if(tvui instanceof BasicTreeUI) {
                Icon icon = ((BasicTreeUI) tvui).getCollapsedIcon();
                handleWidth = icon != null ? icon.getIconWidth() : 0;
            }
        }

        if (value instanceof LogEntryNode) {
            return cr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        } else if(value instanceof EventNode) {
            return rr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        } else if(value instanceof ActionNode) {
            return ar.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        } else {
            Component r = dtcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if(r instanceof JLabel) ((JLabel)r).setIcon(null);
            return r;
        }
    }

    private class RevisionRenderer implements TreeCellRenderer {
        private String revision;

        private JTextPane textPane = new Pane();

        public RevisionRenderer() {
            textPane.setBorder(new MatteBorder(3, 0, 0, 0, Color.WHITE));
            textPane.setContentType("text/html");
            oneLineHeight = textPane.getPreferredSize().height + 3;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            LogEntry entry = (LogEntry) ((LogEntryNode) value).getUserObject();

            Color backgroundColor;
            String background;
            String foreground;

            if (selected) {
                foreground = selectionForeground;
                background = selectionBackground;
                backgroundColor = selectionBackgroundColor;
            } else {
                foreground = normalForeground;
                background = normalBackground;
                backgroundColor = normalBackgroundColor;
            }
            textPane.setBackground(backgroundColor);

            // XXX cache
            Lookup.Result<VCSHyperlinkProvider> hpResult = Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
            Collection<VCSHyperlinkProvider> hpInstances = (Collection<VCSHyperlinkProvider>) hpResult.allInstances();

            revision = entry.getRevision();
            String author = entry.getAuthor() != null ? entry.getAuthor() : "";
            String date = entry.getDate();
            String messageValue = escapeHTML(entry.getMessage());

            // message
            // XXX trim leading and tailing empty lines
            int nlc, i;
            for (i = 0, nlc = -1; i != -1 ; i = messageValue.indexOf('\n', i + 1), nlc++);
            messageValue =
                    nlc == 0 || entry.messageExpanded ? messageValue.replace("\n", "<br>")
                                                      : messageValue.substring(0, messageValue.indexOf("\n"));
            // compute issue hyperlinks
//                IssueLinker il = linkerSupport.getLinker(IssueLinker.class, index);
            int[] issuespans = null;
            VCSHyperlinkProvider hyperlinkProvider = null;
            String issuesMsgValue = null;
            for (VCSHyperlinkProvider hp : hpInstances) {
//                        il = IssueLinker.create(hp, issueHyperlinkStyle, master.getRoots()[0], sd, commitMessage);
                issuespans = hp.getSpans(entry.getMessage()); // compute spans from untouched message text
                if (issuespans == null) {
                    continue;
                }
                if(issuespans.length % 2 != 0) {
                    // XXX more info and log only _ONCE_
                    AbstractSummaryView.LOG.log(Level.WARNING, "Hyperlink provider {0} returns wrong spans", hp.getClass().getName());
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                int pos = 0;
                if(issuespans.length > 0) {
                    issuesMsgValue = messageValue;
                    hyperlinkProvider = hp;
                    boolean linked = true;
                    for (i = 0; i < issuespans.length;) {
                        int issueidx = i / 2;
                        int start = issuespans[i++];
                        int end = issuespans[i++];
                        if(pos >= messageValue.length() || start >= messageValue.length()) {
                            linked = false;
                            break;
                        }
                        sb.append(messageValue.substring(pos, start));
                        sb.append("<font color=\"");
                        sb.append(selected ? selectionForeground : linkColor);
                        sb.append("\" id=\"issue");
                        sb.append(issueidx);
                        sb.append("\">");
                        if(start >= messageValue.length() || end >= messageValue.length()) {
                            linked = false;
                            break;
                        }
                        sb.append(messageValue.substring(start, end));
                        sb.append("</font>");
                        pos = end;
                    }
                    if(linked) {
                        sb.append(messageValue.substring(pos));
                        messageValue = sb.toString();
                    }
                }

            }
            // hilite message
            if (hiliteMessage != null && !selected) {
                int idx = entry.getMessage().indexOf(hiliteMessage);
                if (idx != -1) {
                    messageValue =
                            messageValue.substring(0, idx) +
                            "<font bgcolor=\"" + hiliteBackgroundColor + "\" color=\"" + hiliteForegroundColor+ "\">" +
                            messageValue.substring(idx, idx + hiliteMessage.length()) +
                            "</font>" +
                            messageValue.substring(idx + hiliteMessage.length());
                }
            }
            if(nlc > 0 && !entry.messageExpanded) {
                messageValue = messageValue + " <font color=\"" + (selected ? selectionForeground : linkColor) + "\" id=\"expandmsg\">...</font>";
            }

            // author
            KenaiUser kenaiUser = null;
            if(kenaiUsersMap != null && author != null && !author.equals("")) {
                kenaiUser = kenaiUsersMap.get(author);
            }

            String txt = "<html><body>" +
                        "<table width=\"100%\" height=\"100%\" border=\"0\">" +
                            "<tr>" +
                                "<td bgcolor=\"" + background + "\" valign=\"top\" width=\"" + (revisionMaxWidth) + "\" border=\"0\"><font color=\"" + foreground + "\">" + revision + "</font></td>" +
                                "<td bgcolor=\"" + background + "\" valign=\"top\" width=\"" + (authorMaxWidth) + "\" border=\"0\" ><center><font color=\"" + (selected ? selectionForeground : (kenaiUser != null ? "#0000FF" : authorColor)) + "\" id=\"author\">(" + author + ")</font></center></td>" +
                                "<td bgcolor=\"" + background + "\" valign=\"top\" border=\"0\"><font color=\"" + foreground + "\">" + messageValue + "</font></td>" +
                                "<td bgcolor=\"" + background + "\" valign=\"top\" width=\"" + (dateMaxWidth) + "\" border=\"0\"><font color=\"" + (selected ? selectionForeground : dateColor) + "\">" + date + "</font></td>" +
                            "</tr>" +
                        "</table>" +
                    "</body></html>";
            textPane.setText(txt);

            HTMLDocument document = (HTMLDocument) textPane.getDocument();
            Element  e = document.getElement("author");
            if(e != null) {
                if(kenaiUser != null) {
                    Hyperlink l = linkerSupport.getLinker(AuthorHyperlink.class, revision);
                    if(l == null) {
                        l = new AuthorHyperlink(kenaiUser, author, e.getStartOffset(), e.getEndOffset());
                        linkerSupport.add(l, revision);
                    }
                    // XXX handle icon
                }
            }

            e = document.getElement("expandmsg");
            if(e != null) {
                Hyperlink l = linkerSupport.getLinker(ExpandMsgHyperlink.class, revision);
                if(l == null) {
                    l = new ExpandMsgHyperlink(entry, e.getStartOffset(), revision);
                    linkerSupport.add(l, revision);
                }
            }

            if(issuespans != null && issuespans.length % 2 == 0) {
                Hyperlink l = linkerSupport.getLinker(IssueHyperlink.class, revision);
                if(l == null) {
                    i = 0;
                    int[] offsets = new int[issuespans.length];
                    while((e = document.getElement("issue" + (i / 2))) != null) {
                        offsets[i++] = e.getStartOffset();
                        offsets[i++] = e.getEndOffset();
                    }
                    l = new IssueHyperlink(hyperlinkProvider, master.getRoot(), issuesMsgValue, issuespans, offsets);
                    linkerSupport.add(l, revision);
                }
            }
            int width = getItemWidth(tree);
            if (width > 0) {
                int ph;
                if (nlc > 0) {
                    int lines = entry.messageExpanded ? nlc + 1 : 1;
                    ph = oneLineHeight * lines;
                } else {
                    ph = oneLineHeight;
                }
                textPane.setPreferredSize(new Dimension(width, ph));
            }
            return textPane;
        }

        private class Pane extends JTextPane {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                linkerSupport.computeBounds(textPane, revision);
            }
        }
    }

    private class EventRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            LogEntry.Event revisionEvent = (LogEntry.Event) ((EventNode) value).getUserObject();
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");
            String color = master.getActionColors().get(revisionEvent.getAction());
            if(color != null) {
                sb.append("<font color=\"");
                sb.append(selected ? selectionForeground : color);
                sb.append("\">");
                sb.append(String.valueOf(revisionEvent.getAction()));
                sb.append("</font>");
            } else {
                sb.append(String.valueOf(revisionEvent.getAction()));
            }
            sb.append("\t");
            String path = revisionEvent.getPath();
            int idx = path.lastIndexOf("/");
            if(idx < 0) {
                sb.append(path);
            } else {
                sb.append("<font color=\"");
                sb.append(selected ? selectionForeground : pathColor);
                sb.append("\">");
                sb.append(path.substring(0, idx));
                sb.append("</font>");
                sb.append(path.substring(idx, path.length()));
            }
            sb.append("</body></html>");
            Component renderer = super.getTreeCellRendererComponent(tree, sb.toString(), selected, expanded, leaf, row, hasFocus);
            if(renderer instanceof JLabel) {
                JLabel l = (JLabel) renderer;
                l.setToolTipText(sb.toString());
                l.setIcon(null);
            }
            return renderer;
        }
    }

    private class ActionRenderer implements TreeCellRenderer {

        private String revision;
        private JTextPane textPane = new Pane();
        private class Pane extends JTextPane {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                linkerSupport.computeBounds(textPane, revision);
            }
        }

        public ActionRenderer() {
            textPane.setBorder(new MatteBorder(3, 0, 0, 0, Color.WHITE));
            textPane.setContentType("text/html");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            LogEntry entry = (LogEntry) ((LogEntryNode)((ActionNode) value).getParent()).getUserObject();

            textPane.setBackground(tree.getBackground());
            revision = entry.getRevision();
            Action[] actions = entry.getActions();

            StringBuilder sb = new StringBuilder();
            sb.append("<html><body><table><tr>");
            for (Action action : actions) {
                sb.append("<td id=\"");
                sb.append(action.getValue(Action.NAME));
                sb.append("\" color=\"");
                sb.append(linkColor);
                sb.append("\">");
                sb.append(action.getValue(Action.NAME));
                sb.append("</td>");
                sb.append("<td>&nbsp;&nbsp;</td>");
            }
            sb.append("</tr></table></body></html>");
            textPane.setText(sb.toString());

            Hyperlink l = linkerSupport.getLinker(ActionHyperlink.class, revision);
            if(l == null) {
                int[] offsets = new int[actions.length * 2];
                int idx = 0;
                HTMLDocument document = (HTMLDocument) textPane.getDocument();
                for (Action action : actions) {
                    Element e = document.getElement((String) action.getValue(Action.NAME));
                    if(e != null) {
                        offsets[idx++] = e.getStartOffset();
                        offsets[idx++] = e.getEndOffset();
                    }
                }
                l = new ActionHyperlink(actions, offsets);
                linkerSupport.add(l, revision);
            }
            textPane.setPreferredSize(new Dimension(textPane.getPreferredSize().width, oneLineHeight));
            return textPane;
        }
    }

    private String escapeHTML(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break; // NOI18N
                case '>': sb.append("&gt;"); break; // NOI18N
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    public int getItemWidth(JTree tree) {
        int width = tree.getWidth();
        if (width <= 0) {
            width = master.getComponent().getWidth();
        }
        width = width - handleWidth;
        return width;
    }

   private class ActionHyperlink extends Hyperlink {
        private Rectangle[] bounds;
        private final int[] offsets;
        private final Action[] actions;


        public ActionHyperlink(Action[] actions, int offsets[]) {
            assert offsets.length / 2 == actions.length;
            this.offsets = offsets;
            this.actions = actions;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle[actions.length];
            int aidx = 0;
            for (int i = 0; i < bounds.length; i++) {
                try {
                    Rectangle mtv = tui.modelToView(textPane, offsets[aidx++], Position.Bias.Forward);
                    if(mtv == null) return;
                    Rectangle startr = mtv.getBounds();
                    mtv = tui.modelToView(textPane, offsets[aidx++], Position.Bias.Backward);
                    if(mtv == null) return;
                    Rectangle endr = mtv.getBounds();
                    this.bounds[i] = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                } catch (BadLocationException ex) { }
            }
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            for (int i = 0; i < bounds.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    component.setToolTipText((String) actions[i].getValue(Action.NAME)); // XXX
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            for (int i = 0; i < bounds.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    Utils.setWaitCursor(true);
                    try {
                        actions[i].actionPerformed(new ActionEvent(actions[i], 0, ""));
                    } finally {
                        Utils.setWaitCursor(false);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private class ExpandMsgHyperlink extends Hyperlink {
        private Rectangle bounds;
        private final int startoffset;
        private final LogEntry entry;
        private final String revision;

        public ExpandMsgHyperlink(LogEntry entry, int startoffset, String revision) {
            this.startoffset = startoffset;
            this.revision = revision;
            this.entry = entry;
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText("Expand Commit Message"); // XXX
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                entry.messageExpanded = true;
                linkerSupport.remove(this, revision);
                Utils.setWaitCursor(true);
                try {
                    master.fireNodeChanged(revision);
                } finally {
                    Utils.setWaitCursor(false);
                }
                return true;
            }
            return false;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            bounds = new Rectangle();
            try {
                Rectangle mtv = tui.modelToView(textPane, startoffset, Position.Bias.Forward);
                if(mtv == null) return;
                Rectangle startr = mtv.getBounds();
                mtv = tui.modelToView(textPane, startoffset + 3, Position.Bias.Backward);
                if(mtv == null) return;
                Rectangle endr = mtv.getBounds();

                bounds = new Rectangle(tpBounds.x + startr.x - handleWidth, startr.y, endr.x - startr.x, startr.height);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public class AuthorHyperlink extends Hyperlink {

        private Rectangle bounds;
        private final int startoffset;
        private final int endoffset;
        private final KenaiUser kenaiUser;
        private final String author;
        private int handleWidth;

        public AuthorHyperlink(KenaiUser kenaiUser, String author, int startoffset, int endoffset) {
            this.kenaiUser = kenaiUser;
            this.author = author;

            this.startoffset = startoffset;
            this.endoffset = endoffset;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle();
            try {
                Rectangle startr = tui.modelToView(textPane, startoffset, Position.Bias.Forward).getBounds();
                Rectangle endr = tui.modelToView(textPane, endoffset, Position.Bias.Backward).getBounds();
                if(kenaiUser.getIcon() != null) {
                    endr.x += kenaiUser.getIcon().getIconWidth();
                }
                this.bounds = new Rectangle(tpBounds.x + startr.x - handleWidth, startr.y, endr.x - startr.x, startr.height);
            } catch (BadLocationException ex) {
                AbstractSummaryView.LOG.log(Level.OFF, null, ex);
            }
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                kenaiUser.startChat();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(VCSHyperlinkSupport.class, "LBL_StartChat", author));
                return true;
            }
            return false;
        }
    }

    public static class IssueHyperlink extends Hyperlink {

        private Rectangle bounds[];
        private final int offsetstart[];
        private final int offsetend[];
        private final int spanstart[];
        private final int spanend[];
        private final String text;
        private final VCSHyperlinkProvider hp;
        private final File root;
        private final int length;

        private IssueHyperlink(VCSHyperlinkProvider hp, File root, String text, int[] spans, int[] offsets) {
            assert spans.length == offsets.length;

            this.length = spans.length / 2;
            this.offsetstart = new int[length];
            this.offsetend = new int[length];
            this.spanstart = new int[length];
            this.spanend = new int[length];
            this.hp = hp;
            this.root = root;
            this.text = text;

            for (int i = 0; i < spans.length;) {
                int linkeridx = i / 2;
                this.spanstart[linkeridx] = spans[i];
                this.offsetstart[linkeridx] = offsets[i++];
                this.spanend[linkeridx] = spans[i];
                this.offsetend[linkeridx] = offsets[i++];
                if(spanend[linkeridx] < spanstart[linkeridx]) {
                    AbstractSummaryView.LOG.log(Level.WARNING, "Hyperlink provider {0} returns wrong spans [{1},{2}]", new Object[]{hp.getClass().getName(), spanstart, spanend});
                    continue;
                }
            }
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle[length];
            for (int i = 0; i < length; i++) {
                try {
                    Rectangle mtv = tui.modelToView(textPane, offsetstart[i], Position.Bias.Forward);
                    if(mtv == null) return;
                    Rectangle startr = mtv.getBounds();
                    mtv = tui.modelToView(textPane, offsetend[i], Position.Bias.Backward);
                    if(mtv == null) return;
                    Rectangle endr = mtv.getBounds();
                    this.bounds[i] = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                } catch (BadLocationException ex) { }
            }
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            for (int i = 0; i < spanstart.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            for (int i = 0; i < spanstart.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    hp.onClick(root, text, spanstart[i], spanend[i]);
                    return true;
                }
            }
            return false;
        }
    }

}