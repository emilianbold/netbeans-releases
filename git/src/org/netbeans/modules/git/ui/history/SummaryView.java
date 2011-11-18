/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.git.ui.history;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.DateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.TextUI;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.diff.ExportCommitAction;
import org.netbeans.modules.git.ui.revert.RevertCommitAction;
import org.netbeans.modules.git.ui.tag.CreateTagAction;
import org.netbeans.modules.git.ui.tag.ManageTagsAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.AuthorLinker;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.IssueLinker;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.StyledDocumentHyperlink;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

class SummaryView implements MouseListener, ComponentListener, MouseMotionListener {

    private static final String SUMMARY_DIFF_PROPERTY = "Summary-Diff-";

    private final SearchHistoryPanel master;
    
    private JList       resultsList;
    private JScrollPane scrollPane;

    private final List  dispResults;
    private String      message;
    private AttributeSet searchHiliteAttrs;
    private List<RepositoryRevision> results;

    private Map<String, KenaiUser> kenaiUsersMap = null;
    private VCSHyperlinkSupport linkerSupport = new VCSHyperlinkSupport();
    private static final Logger LOG = Logger.getLogger(SummaryView.class.getName());
    
    public SummaryView(SearchHistoryPanel master, List<RepositoryRevision> results) {
        this.master = master;
        this.results = results;
        this.dispResults = expandResults(results);
        FontColorSettings fcs = (FontColorSettings) MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class); // NOI18N
        searchHiliteAttrs = fcs.getFontColors("highlight-search"); // NOI18N
        message = master.getCriteria().getCommitMessage();
        resultsList = new JList(new SummaryListModel());
        resultsList.setFixedCellHeight(-1);
        resultsList.addMouseListener(this);
        resultsList.addMouseMotionListener(this);
        resultsList.setCellRenderer(new SummaryCellRenderer());
        resultsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SummaryView.class, "ACSN_SummaryView_List")); // NOI18N
        resultsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SummaryView.class, "ACSD_SummaryView_List")); // NOI18N
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        master.addComponentListener(this);
        resultsList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
        resultsList.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(resultsList));
            }
        });

        // TODO kenai support?
    }

    @Override
    public void componentResized(ComponentEvent e) {
        int [] selection = resultsList.getSelectedIndices();
        resultsList.setModel(new SummaryListModel());
        resultsList.setSelectedIndices(selection);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // not interested
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // not interested
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // not interested
    }
    
    @SuppressWarnings("unchecked")
    private List expandResults(List<RepositoryRevision> results) {
        ArrayList newResults = new ArrayList(results.size());
        for (RepositoryRevision repositoryRevision : results) {
            newResults.add(repositoryRevision);
            if (master.isShowInfo()) {
                List<RepositoryRevision.Event> events = repositoryRevision.getEvents();
                for (RepositoryRevision.Event event : events) {
                    newResults.add(event);
                }
            }
        }
        return newResults;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty(SUMMARY_DIFF_PROPERTY + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            diffPrevious(idx);
        }
        linkerSupport.mouseClicked(p, idx);
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // not interested
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;

        resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resultsList.setToolTipText("");

        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty(SUMMARY_DIFF_PROPERTY + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        linkerSupport.mouseMoved(p, resultsList, idx);
    }

    private void onPopup(MouseEvent e) {
        onPopup(e.getPoint());
    }
    
    private void onPopup(Point p) {
        int [] sel = resultsList.getSelectedIndices();
        if (sel.length <= 1) {
            int idx = resultsList.locationToIndex(p);
            if (idx == -1) return;
            resultsList.setSelectedIndex(idx);
            sel = new int [] { idx };
        }
        final int [] selection = sel;

        JPopupMenu menu = new JPopupMenu();
        
        final RepositoryRevision container;
        final RepositoryRevision.Event[] drev;

        Object revCon = dispResults.get(selection[0]);
        
        boolean revisionSelected;
        boolean missingFile = false;        
        boolean oneRevisionMultiselected = true;
        
        if (revCon instanceof RepositoryRevision) {
            revisionSelected = true;
            container = (RepositoryRevision) dispResults.get(selection[0]);
            drev = new RepositoryRevision.Event[0];
            oneRevisionMultiselected = true;
        } else {
            revisionSelected = false;
            drev = new RepositoryRevision.Event[selection.length];

            for(int i = 0; i < selection.length; i++) {
                drev[i] = (RepositoryRevision.Event) dispResults.get(selection[i]);
                
                if(!missingFile && drev[i].getFile() == null) {
                    missingFile = true;
                }
                if(oneRevisionMultiselected && i > 0 && drev[0].getLogInfoHeader().getLog().getRevision().equals(drev[i].getLogInfoHeader().getLog().getRevision())) {
                    oneRevisionMultiselected = false;
                }                
            }                
            container = drev[0].getLogInfoHeader();
        }
        boolean hasParents = container.getLog().getParents().length > 0;

        final boolean singleSelection = selection.length == 1;
        final boolean viewEnabled = singleSelection && !revisionSelected && drev[0].getFile() != null && !drev[0].getFile().isDirectory();
        
        if (hasParents) {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious")) { // NOI18N
                {
                    setEnabled(singleSelection);
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    diffPrevious(selection[0]);
                }
            }));
        }

        if (revisionSelected) {
            if (singleSelection) {
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction_PopupName.revision", container.getLog().getRevision().substring(0, 7))) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        CreateTagAction action = SystemAction.get(CreateTagAction.class);
                        action.createTag(master.getRepository(), container.getLog().getRevision());
                    }
                }));
                if (container.getLog().getParents().length < 2) {
                    menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_PopupName")) { //NOI18N
                        @Override
                        public void actionPerformed (ActionEvent e) {
                            ExportCommitAction action = SystemAction.get(ExportCommitAction.class);
                            action.exportCommit(master.getRepository(), container.getLog().getRevision());
                        }
                    }));
                    menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(RevertCommitAction.class, "LBL_RevertCommitAction_PopupName")) { //NOI18N
                        @Override
                        public void actionPerformed (ActionEvent e) {
                            RevertCommitAction action = SystemAction.get(RevertCommitAction.class);
                            action.revert(master.getRepository(), master.getRoots(), container.getLog().getRevision());
                        }
                    }));
                }
            }
        } else {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")) { // NOI18N
                {
                    setEnabled(viewEnabled);
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    new GitProgressSupport() {
                        @Override
                        protected void perform () {
                            for (RepositoryRevision.Event evt : drev) {
                                try {
                                    File originalFile = evt.getFile();
                                    String revision = evt.getLogInfoHeader().getLog().getRevision();
                                    GitUtils.openInRevision(originalFile, -1, revision, false, this);
                                } catch (IOException ex) {
                                    LOG.log(Level.FINE, null, ex);
                                }
                            }
                        }
                    }.start(Git.getInstance().getRequestProcessor(), master.getRepository(), NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
                }
            }));
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                {
                    setEnabled(viewEnabled);
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    new GitProgressSupport() {
                        @Override
                        protected void perform () {
                            for (RepositoryRevision.Event evt : drev) {
                                try {
                                    File originalFile = evt.getFile();
                                    String revision = evt.getLogInfoHeader().getLog().getRevision();
                                    GitUtils.openInRevision(originalFile, -1, revision, true, this);
                                } catch (IOException ex) {
                                    LOG.log(Level.FINE, null, ex);
                                }
                            }
                        }
                    }.start(Git.getInstance().getRequestProcessor(), master.getRepository(), NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
                }
            }));
        }
        menu.show(resultsList, p.x, p.y);
    }

    private void diffPrevious(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof RepositoryRevision.Event) {
            RepositoryRevision.Event drev = (RepositoryRevision.Event) o;
            master.showDiff(drev);
        } else {
            RepositoryRevision container = (RepositoryRevision) o;
            master.showDiff(container);
        }
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    private class SummaryListModel extends AbstractListModel {

        @Override
        public int getSize() {
            return dispResults.size();
        }

        @Override
        public Object getElementAt(int index) {
            return dispResults.get(index);
        }
    }
    
    private class SummaryCellRenderer implements ListCellRenderer {

        private static final String FIELDS_SEPARATOR = "        "; // NOI18N

        private RevisionRenderer rr = new RevisionRenderer();
        private ChangepathRenderer cpr = new ChangepathRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof RepositoryRevision) {
                return rr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            } else {
                return cpr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }

        private class RevisionRenderer extends JPanel implements ListCellRenderer {
            private static final double DARKEN_FACTOR = 0.95;

            private Style selectedStyle;
            private Style normalStyle;
            private Style filenameStyle;
            private Style indentStyle;
            private Style noindentStyle;
            private Style hiliteStyle;
            private Style issueHyperlinkStyle;
            private final Style authorStyle;

            private Color selectionBackground;
            private Color selectionForeground;

            private JTextPane textPane = new JTextPane();
            private JPanel    actionsPane = new JPanel();

            private DateFormat defaultFormat;

            private int             index;
            private HyperlinkLabel  diffLink;

            public RevisionRenderer() {
                selectionBackground = new JList().getSelectionBackground();
                selectionForeground = new JList().getSelectionForeground();

                selectedStyle = textPane.addStyle("selected", null); // NOI18N
                StyleConstants.setForeground(selectedStyle, selectionForeground);
                StyleConstants.setBackground(selectedStyle, selectionBackground);
                normalStyle = textPane.addStyle("normal", null); // NOI18N
                StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); // NOI18N
                filenameStyle = textPane.addStyle("filename", normalStyle); // NOI18N
                StyleConstants.setBold(filenameStyle, true);
                indentStyle = textPane.addStyle("indent", null); // NOI18N
                StyleConstants.setLeftIndent(indentStyle, 50);
                noindentStyle = textPane.addStyle("noindent", null); // NOI18N
                StyleConstants.setLeftIndent(noindentStyle, 0);
                defaultFormat = DateFormat.getDateTimeInstance();

                issueHyperlinkStyle = textPane.addStyle("issuehyperlink", normalStyle); //NOI18N
                StyleConstants.setForeground(issueHyperlinkStyle, Color.BLUE);
                StyleConstants.setUnderline(issueHyperlinkStyle, true);

                authorStyle = textPane.addStyle("author", normalStyle); //NOI18N
                StyleConstants.setForeground(authorStyle, Color.BLUE);

                hiliteStyle = textPane.addStyle("hilite", normalStyle); // NOI18N
                Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
                if (c != null) StyleConstants.setBackground(hiliteStyle, c);
                c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
                if (c != null) StyleConstants.setForeground(hiliteStyle, c);

                setLayout(new BorderLayout());
                add(textPane);
                add(actionsPane, BorderLayout.PAGE_END);
                actionsPane.setLayout(new FlowLayout(FlowLayout.TRAILING, 2, 5));

                diffLink = new HyperlinkLabel();
                diffLink.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
                actionsPane.add(diffLink);

                textPane.setBorder(null);
            }

            public Color darker(Color c) {
                return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0),
                     Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
                     Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
            }

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                renderContainer(list, (RepositoryRevision) value, index, isSelected);
                return this;
            }

            private void renderContainer(JList list, RepositoryRevision container, int index, boolean isSelected) {

                StyledDocument sd = textPane.getStyledDocument();

                Style style;
                Color backgroundColor;
                Color foregroundColor;

                if (isSelected) {
                    foregroundColor = selectionForeground;
                    backgroundColor = selectionBackground;
                    style = selectedStyle;
                } else {
                    foregroundColor = UIManager.getColor("List.foreground"); // NOI18N
                    backgroundColor = UIManager.getColor("List.background"); // NOI18N
                    backgroundColor = darker(backgroundColor);
                    style = normalStyle;
                }
                textPane.setBackground(backgroundColor);
                actionsPane.setBackground(backgroundColor);

                this.index = index;

                // XXX cache
                Lookup.Result<VCSHyperlinkProvider> hpResult = Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
                Collection<VCSHyperlinkProvider> hpInstances = (Collection<VCSHyperlinkProvider>) hpResult.allInstances();

                try {
                    // clear document
                    sd.remove(0, sd.getLength());
                    sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);

                    addAliases(linkerSupport, sd, index, container);

                    // add revision
                    String rev = container.getLog().getRevision();
                    sd.insertString(sd.getLength(), rev.length() > 7 ? rev.substring(0, 7) : rev, null);
                    sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);

                    // add author
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR, style);
                    GitUser user = container.getLog().getAuthor();
                    if (user == null) {
                        user = container.getLog().getCommitter();
                    }
                    String author = user.toString();
                    StyledDocumentHyperlink l = linkerSupport.getLinker(AuthorLinker.class, index);
                    if(l == null) {
                        if(kenaiUsersMap != null && author != null && !"".equals(author)) {
                            KenaiUser kenaiUser = kenaiUsersMap.get(author);
                            if(kenaiUser != null) {
                                l = new AuthorLinker(kenaiUser, authorStyle, sd, author);
                                linkerSupport.add(l, index);
                            }
                        }
                    }
                    if(l != null) {
                        l.insertString(sd, isSelected ? style : null);
                    } else {
                        sd.insertString(sd.getLength(), author, style);
                    }

                    // add date
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + defaultFormat.format(new Date(container.getLog().getCommitTime())), null);

                    // add commit msg
                    String commitMessage = container.getLog().getFullMessage();
                    if (commitMessage.endsWith("\n")) commitMessage = commitMessage.substring(0, commitMessage.length() - 1); // NOI18N
                    sd.insertString(sd.getLength(), "\n", null);

                    // compute issue hyperlinks
                    l = linkerSupport.getLinker(IssueLinker.class, index);
                    if(l == null) {
                        for (VCSHyperlinkProvider hp : hpInstances) {
                            l = IssueLinker.create(hp, issueHyperlinkStyle, master.getRoots()[0], sd, commitMessage);
                            if(l != null) {
                                linkerSupport.add(l, index);
                                break; // get the first one
                            }
                        }
                    }
                    if(l != null) {
                        l.insertString(sd, style);
                    } else {
                        sd.insertString(sd.getLength(), commitMessage, style);
                    }

                    int msglen = commitMessage.length();
                    int doclen = sd.getLength();
                    if (message != null && !isSelected) {
                        int idx = commitMessage.indexOf(message);
                        if (idx != -1) {
                            sd.setCharacterAttributes(doclen - msglen + idx, message.length(), hiliteStyle, true);
                        }
                    }

                    resizePane(commitMessage, list.getFontMetrics(list.getFont()));
                    if(isSelected) {
                        sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                    }
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }

                actionsPane.setVisible(true);
                diffLink.set(NbBundle.getMessage(SummaryView.class, "CTL_Action_Diff"), foregroundColor, backgroundColor);// NOI18N
            }

            @SuppressWarnings("empty-statement")
            private void resizePane(String text, FontMetrics fm) {
                if(text == null) {
                    text = "";
                }
                int width = master.getWidth();
                if (width > 0) {
                    Rectangle2D rect = fm.getStringBounds(text, textPane.getGraphics());
                    int nlc, i;
                    for (nlc = -1, i = 0; i != -1 ; i = text.indexOf('\n', i + 1), nlc++);
                    nlc++;
                    int lines = (int) (rect.getWidth() / (width - 80) + 1);
                    int ph = fm.getHeight() * (lines + nlc) + 0;
                    textPane.setPreferredSize(new Dimension(width - 50, ph));
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (index == -1) return;
                Rectangle apb = actionsPane.getBounds();
                Rectangle bounds = diffLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty(SUMMARY_DIFF_PROPERTY + index, bounds); // NOI18N
                linkerSupport.computeBounds(textPane, index);
            }

            private void addAliases (VCSHyperlinkSupport linkerSupport, StyledDocument sd, int index, RepositoryRevision container) throws BadLocationException {
                StyledDocumentHyperlink l = linkerSupport.getLinker(RefAliasLinker.class, index);
                if(l == null) {
                    l = new RefAliasLinker(sd, container);
                    linkerSupport.add(l, index);
                }
                l.insertString(sd, null);
                l = linkerSupport.getLinker(BranchAliasLinker.class, index);
                if(l == null) {
                    l = new BranchAliasLinker(sd, container);
                    linkerSupport.add(l, index);
                }
                l.insertString(sd, null);
                l = linkerSupport.getLinker(TagAliasLinker.class, index);
                if(l == null) {
                    l = new TagAliasLinker(master.getRepository(), sd, container);
                    linkerSupport.add(l, index);
                }
                l.insertString(sd, null);
            }
        }

        private class ChangepathRenderer extends DefaultListCellRenderer {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                RepositoryRevision.Event revisionEvent = (RepositoryRevision.Event) value;
                StringBuilder sb = new StringBuilder();
                sb.append(FIELDS_SEPARATOR);
                sb.append(String.valueOf(revisionEvent.getAction()));
                sb.append(FIELDS_SEPARATOR);
                sb.append(revisionEvent.getPath());
                Component renderer = super.getListCellRendererComponent(list, sb.toString(), index, isSelected, isSelected);
                if(renderer instanceof JLabel) {
                    ((JLabel) renderer).setToolTipText(sb.toString());
                }
                return renderer;
            }
        }
    }

    private static class HyperlinkLabel extends JLabel {

        public HyperlinkLabel() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void set(String text, Color foreground, Color background) {
            StringBuilder sb = new StringBuilder(100);
            if (foreground.equals(UIManager.getColor("List.foreground"))) { // NOI18N
                sb.append("<html><a href=\"\">"); // NOI18N
                sb.append(text);
                sb.append("</a>"); // NOI18N
            } else {
                sb.append("<html><a href=\"\" style=\"color:"); // NOI18N
                sb.append("rgb("); // NOI18N
                sb.append(foreground.getRed());
                sb.append(","); // NOI18N
                sb.append(foreground.getGreen());
                sb.append(","); // NOI18N
                sb.append(foreground.getBlue());
                sb.append(")"); // NOI18N
                sb.append("\">"); // NOI18N
                sb.append(text);
                sb.append("</a>"); // NOI18N
            }
            setText(sb.toString());
            setBackground(background);
        }
    }

    private static abstract class AliasLinker extends StyledDocumentHyperlink {

        private Rectangle bounds[];
        private final String[] labels;
        private final int docLen;
        private final boolean supportsClick;

        private AliasLinker (StyledDocument sd, List<String> labels, boolean supportsClick) {
            this.bounds = new Rectangle[0];
            this.labels = labels.toArray(new String[labels.size()]);
            this.supportsClick = supportsClick;
            this.docLen = sd.getLength();
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle[labels.length];
            int i = 0;
            int labelStartPos = docLen;
            for (String label : labels) {
                try {
                    Rectangle startr = tui.modelToView(textPane, labelStartPos, Position.Bias.Forward).getBounds();
                    Rectangle endr = tui.modelToView(textPane, labelStartPos = labelStartPos + label.length(), Position.Bias.Backward).getBounds();
                    this.bounds[i] = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                } catch (BadLocationException ex) { }
                ++i;
                ++labelStartPos; // plus one for a space between labels
            }
        }

        @Override
        public boolean mouseMoved (Point p, JComponent component) {
            if (bounds != null) {
                for (int i = 0; i < bounds.length; ++i) {
                    if (bounds[i].contains(p)) {
                        if (supportsClick) {
                            component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        }
                        String toolTip = getToolTip(labels[i]);
                        if (toolTip != null && !toolTip.isEmpty()) {
                            component.setToolTipText(toolTip);
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked (Point p) {
            if (supportsClick && bounds != null) {
                for (int i = 0; i < bounds.length; ++i) {
                    if (bounds[i].contains(p)) {
                        return mouseClicked(labels[i]);
                    }
                }
            }
            return false;
        }

        @Override
        public void insertString(StyledDocument sd, Style style) throws BadLocationException {
            for (String label : labels) {
                sd.insertString(sd.getLength(), label + " ", style); //NOI18N
            }
        }

        protected boolean mouseClicked (String label) {
            return false;
        }

        protected String getToolTip (String label) {
            return null;
        }
    }
    
    private static final class RefAliasLinker extends AliasLinker {
        
        private RefAliasLinker (StyledDocument sd, RepositoryRevision container) {
            super(sd, computeLabels(container), false);
        }

        private static List<String> computeLabels(RepositoryRevision container) {
            List<String> refs = new LinkedList<String>();
            for (GitBranch b : container.getBranches()) {
                if (b.isActive()) {
                    refs.add(GitUtils.HEAD);
                }
            }
            return refs;
        }
    }
    
    private static final class BranchAliasLinker extends AliasLinker {
        
        private BranchAliasLinker (StyledDocument sd, RepositoryRevision container) {
            super(sd, computeLabels(container), false);
        }

        private static List<String> computeLabels(RepositoryRevision container) {
            List<String> labels = new LinkedList<String>();
            for (boolean remote : new boolean[] { false, true }) {
                List<String> toSort = new ArrayList<String>(container.getBranches().length);
                for (GitBranch b : container.getBranches()) {
                    if (remote == b.isRemote() && b.getName() != GitBranch.NO_BRANCH) {
                        toSort.add(b.getName());
                    }
                }
                Collections.sort(toSort);
                labels.addAll(toSort);
            }
            return labels;
        }

        @Override
        protected String getToolTip (String label) {
            return NbBundle.getMessage(SummaryView.class, "CTL_SummaryView.branchAlias.TT", label); //NOI18N
        }
    }

    private static final class TagAliasLinker extends AliasLinker {
        private final File repository;
        
        private TagAliasLinker (File repository, StyledDocument sd, RepositoryRevision container) {
            super(sd, computeLabels(container), true);
            this.repository = repository;
        }

        private static List<String> computeLabels(RepositoryRevision container) {
            List<String> labels = new ArrayList<String>(container.getTags().length);
            for (GitTag b : container.getTags()) {
                labels.add(b.getTagName());
            }
            Collections.sort(labels);
            return labels;
        }

        @Override
        protected String getToolTip (String label) {
            return NbBundle.getMessage(SummaryView.class, "CTL_SummaryView.tagAlias.TT", label); //NOI18N
        }

        @Override
        protected boolean mouseClicked (String label) {
            ManageTagsAction action = SystemAction.get(ManageTagsAction.class);
            action.showTagManager(repository, label);
            return true;
        }
    }
}
