/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.VersionsCache;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.update.RevertModifications;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.ui.diff.DiffSetupSource;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * @author Maros Sandor
 */
/**
 * Shows Search History results in a JList.
 * 
 * @author Maros Sandor
 */
class SummaryView implements MouseListener, ComponentListener, MouseMotionListener, DiffSetupSource {

    private static final String SUMMARY_REVERT_PROPERTY = "Summary-Revert-";

    private final SearchHistoryPanel master;
    
    private JList       resultsList;
    private JScrollPane scrollPane;

    private final List  dispResults;
    private String      message;
    private AttributeSet searchHiliteAttrs;
    private List<RepositoryRevision> results;

    public SummaryView(SearchHistoryPanel master, List<RepositoryRevision> results) {
        this.master = master;
        this.results = results;
        this.dispResults = expandResults(results);
        FontColorSettings fcs = (FontColorSettings) MimeLookup.getMimeLookup("text/x-java").lookup(FontColorSettings.class); // NOI18N
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
            public void actionPerformed(ActionEvent e) {
                onPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(resultsList));
            }
        });        
    }

    public void componentResized(ComponentEvent e) {
        int [] selection = resultsList.getSelectedIndices();
        resultsList.setModel(new SummaryListModel());
        resultsList.setSelectedIndices(selection);
    }

    public void componentHidden(ComponentEvent e) {
        // not interested
    }

    public void componentMoved(ComponentEvent e) {
        // not interested
    }

    public void componentShown(ComponentEvent e) {
        // not interested
    }
    
    private List expandResults(List<RepositoryRevision> results) {
        ArrayList newResults = new ArrayList(results.size());
        for (RepositoryRevision repositoryRevision : results) {
            newResults.add(repositoryRevision);
            List<RepositoryRevision.Event> events = repositoryRevision.getEvents();
            for (RepositoryRevision.Event event : events) {
                newResults.add(event);
            }
        }
        return newResults;
    }

    public void mouseClicked(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Diff-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            diffPrevious(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty(SUMMARY_REVERT_PROPERTY + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            revertModifications(new int [] { idx });
        }
    }

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mouseExited(MouseEvent e) {
        // not interested
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Diff-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty(SUMMARY_REVERT_PROPERTY + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public Collection getSetups() {
        Node [] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes.length == 0) {
            return master.getSetups(results.toArray(new RepositoryRevision[results.size()]), new RepositoryRevision.Event[0]);
        }
    
        Set<RepositoryRevision.Event> events = new HashSet<RepositoryRevision.Event>();
        Set<RepositoryRevision> revisions = new HashSet<RepositoryRevision>();

        int [] sel = resultsList.getSelectedIndices();
        for (int i : sel) {
            Object revCon = dispResults.get(i);            
            if (revCon instanceof RepositoryRevision) {
                revisions.add((RepositoryRevision) revCon);
            } else {
                events.add((RepositoryRevision.Event) revCon);
            }
        }
        return master.getSetups(revisions.toArray(new RepositoryRevision[revisions.size()]), events.toArray(new RepositoryRevision.Event[events.size()]));
    }

    public String getSetupDisplayName() {
        return null;
    }

    private void onPopup(MouseEvent e) {
        onPopup(e.getPoint());
    }
    
    private void onPopup(Point p) {
        int [] sel = resultsList.getSelectedIndices();
        if (sel.length == 0) {
            int idx = resultsList.locationToIndex(p);
            if (idx == -1) return;
            resultsList.setSelectedIndex(idx);
            sel = new int [] { idx };
        }
        final int [] selection = sel;

        JPopupMenu menu = new JPopupMenu();
        
        String previousRevision = null;
        RepositoryRevision container = null;
        final RepositoryRevision.Event drev;

        Object revCon = dispResults.get(selection[0]);
        final boolean revisionSelected;

        if (revCon instanceof RepositoryRevision) {
            revisionSelected = true;
            container = (RepositoryRevision) dispResults.get(selection[0]);
            drev = null;
        } else {
            revisionSelected = false;
            drev = (RepositoryRevision.Event) dispResults.get(selection[0]);
            container = drev.getLogInfoHeader();
        }
        long revision = container.getLog().getRevision().getNumber();

        if (revision > 1) {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", previousRevision)) { // NOI18N
                {
                    setEnabled(selection.length == 1);
                }
                public void actionPerformed(ActionEvent e) {
                    diffPrevious(selection[0]);
                }
            }));
        }

        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackChange")) { // NOI18N
            {
                setEnabled(drev == null || !(drev.getFile().exists() && drev.getChangedPath().getAction() == 'D') );
            }
            public void actionPerformed(ActionEvent e) {
                revertModifications(selection);
            }
        }));

        if (!revisionSelected) {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackTo", revision)) { // NOI18N
                {                    
                    setEnabled(selection.length == 1 && !revisionSelected);
                }
                public void actionPerformed(ActionEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            rollback(drev);
                        }
                    });
                }
            }));
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")) { // NOI18N
                {
                    setEnabled(selection.length == 1 && !revisionSelected && drev.getFile() != null && drev.getFile().exists() &&  !drev.getFile().isDirectory());
                }
                public void actionPerformed(ActionEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            view(selection[0]);
                        }
                    });
                }
            }));
        }

        menu.show(resultsList, p.x, p.y);
    }

    /**
     * Overwrites local file with this revision.
     *
     * @param event
     */
    static void rollback(final RepositoryRevision.Event event) {
        // TODO: confirmation
        SVNUrl repository = event.getLogInfoHeader().getRepositoryRootUrl();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                rollback(event, this);
            }
        };
        support.start(rp, repository, NbBundle.getMessage(SummaryView.class, "MSG_Rollback_Progress")); // NOI18N
    }

    private static void rollback(RepositoryRevision.Event event, SvnProgressSupport progress) {
        File file = event.getFile();
        File parent = file.getParentFile();
        parent.mkdirs();
        try {
            File oldFile = VersionsCache.getInstance().getFileRevision(event.getFile(), Long.toString(event.getLogInfoHeader().getLog().getRevision().getNumber()));
            file.delete();
            FileUtil.copyFile(FileUtil.toFileObject(oldFile), FileUtil.toFileObject(parent), file.getName(), "");
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private void revertModifications(int[] selection) {
        Set<RepositoryRevision.Event> events = new HashSet<RepositoryRevision.Event>();
        Set<RepositoryRevision> revisions = new HashSet<RepositoryRevision>();
        for (int idx : selection) {
            Object o = dispResults.get(idx);
            if (o instanceof RepositoryRevision) {
                revisions.add((RepositoryRevision) o);
            } else {
                events.add((RepositoryRevision.Event) o);
            }
        }
        revert(master, revisions.toArray(new RepositoryRevision[revisions.size()]), (RepositoryRevision.Event[]) events.toArray(new RepositoryRevision.Event[events.size()]));
    }

    static void revert(final SearchHistoryPanel master, final RepositoryRevision [] revisions, final RepositoryRevision.Event [] events) {
        SVNUrl url;
        try {
            url = master.getSearchRepositoryRootUrl();        
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);                
            return;
        }                   
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                revertImpl(master, revisions, events, this);
            }
        };
        support.start(rp, url, NbBundle.getMessage(SummaryView.class, "MSG_Revert_Progress")); // NOI18N
    }

    private static void revertImpl(SearchHistoryPanel master, RepositoryRevision[] revisions, RepositoryRevision.Event[] events, SvnProgressSupport progress) {
        SVNUrl url;
        try {
            url = master.getSearchRepositoryRootUrl();        
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);                
            return;            
        }                           
        final RepositoryFile repositoryFile = new RepositoryFile(url, url, SVNRevision.HEAD);
        for (RepositoryRevision revision : revisions) {
            RevertModifications.RevisionInterval revisionInterval = new RevertModifications.RevisionInterval(revision.getLog().getRevision());
            final Context ctx = new Context(master.getRoots());
            RevertModificationsAction.performRevert(revisionInterval, false, ctx, progress);
        }
        for (RepositoryRevision.Event event : events) {
            if (event.getFile() == null) continue;
            RevertModifications.RevisionInterval revisionInterval = new RevertModifications.RevisionInterval(event.getLogInfoHeader().getLog().getRevision());            
            final Context ctx = new Context(event.getFile());
            RevertModificationsAction.performRevert(revisionInterval, false, ctx, progress);
        }
    }

    private void view(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof RepositoryRevision.Event) {
            RepositoryRevision.Event drev = (RepositoryRevision.Event) o;
            FileObject fo = FileUtil.toFileObject(drev.getFile());
            org.netbeans.modules.versioning.util.Utils.openFile(fo, drev.getLogInfoHeader().getLog().getRevision().toString());
        }
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

        public int getSize() {
            return dispResults.size();
        }

        public Object getElementAt(int index) {
            return dispResults.get(index);
        }
    }
    
    private class SummaryCellRenderer extends JPanel implements ListCellRenderer {

        private static final String FIELDS_SEPARATOR = "        "; // NOI18N
        private static final double DARKEN_FACTOR = 0.95;

        private Style selectedStyle;
        private Style normalStyle;
        private Style filenameStyle;
        private Style indentStyle;
        private Style noindentStyle;
        private Style hiliteStyle;
        
        private JTextPane textPane = new JTextPane();
        private JPanel    actionsPane = new JPanel();
        
        private DateFormat defaultFormat;
        
        private int             index;
        private HyperlinkLabel  diffLink;
        private HyperlinkLabel  revertLink;

        public SummaryCellRenderer() {
            selectedStyle = textPane.addStyle("selected", null); // NOI18N
            StyleConstants.setForeground(selectedStyle, UIManager.getColor("List.selectionForeground")); // NOI18N
            normalStyle = textPane.addStyle("normal", null); // NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); // NOI18N
            filenameStyle = textPane.addStyle("filename", normalStyle); // NOI18N
            StyleConstants.setBold(filenameStyle, true);
            indentStyle = textPane.addStyle("indent", null); // NOI18N
            StyleConstants.setLeftIndent(indentStyle, 50);
            noindentStyle = textPane.addStyle("noindent", null); // NOI18N
            StyleConstants.setLeftIndent(noindentStyle, 0);
            defaultFormat = DateFormat.getDateTimeInstance();

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

            revertLink = new HyperlinkLabel();
            actionsPane.add(revertLink);
            
            textPane.setBorder(null);
        }
        
        public Color darker(Color c) {
            return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0), 
                 Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
                 Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof RepositoryRevision) {
                renderContainer(list, (RepositoryRevision) value, index, isSelected);
            } else {
                renderRevision(list, (RepositoryRevision.Event) value, index, isSelected);
            }
            return this;
        }

        private void renderContainer(JList list, RepositoryRevision container, int index, boolean isSelected) {

            StyledDocument sd = textPane.getStyledDocument();

            Style style;
            Color backgroundColor;
            Color foregroundColor;
            
            if (isSelected) {
                foregroundColor = UIManager.getColor("List.selectionForeground"); // NOI18N
                backgroundColor = UIManager.getColor("List.selectionBackground"); // NOI18N
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
                        
            try {
                sd.remove(0, sd.getLength());
                sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);

                sd.insertString(0, Long.toString(container.getLog().getRevision().getNumber()), null);
                sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                sd.insertString(sd.getLength(), FIELDS_SEPARATOR + container.getLog().getAuthor(), null);                
                sd.insertString(sd.getLength(), FIELDS_SEPARATOR + defaultFormat.format(container.getLog().getDate()), null);
                
                String commitMessage = container.getLog().getMessage();
                if (commitMessage.endsWith("\n")) commitMessage = commitMessage.substring(0, commitMessage.length() - 1); // NOI18N
                sd.insertString(sd.getLength(), "\n", null);

                sd.insertString(sd.getLength(), commitMessage, null);
                
                if (message != null && !isSelected) {
                    int idx = commitMessage.indexOf(message);
                    if (idx != -1) {
                        int len = commitMessage.length();
                        int doclen = sd.getLength();
                        sd.setCharacterAttributes(doclen - len + idx, message.length(), hiliteStyle, false);
                    }
                }
                                
                if (commitMessage != null) {
                    int width = master.getWidth();
                    if (width > 0) {
                        FontMetrics fm = list.getFontMetrics(list.getFont());
                        Rectangle2D rect = fm.getStringBounds(commitMessage, textPane.getGraphics());
                        int nlc, i;
                        for (nlc = -1, i = 0; i != -1 ; i = commitMessage.indexOf('\n', i + 1), nlc++);
                        nlc++;
                        int lines = (int) (rect.getWidth() / (width - 80) + 1);
                        int ph = fm.getHeight() * (lines + nlc) + 0;
                        textPane.setPreferredSize(new Dimension(width - 50, ph));
                    }
                }
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            actionsPane.setVisible(true);
            diffLink.set(NbBundle.getMessage(SummaryView.class, "CTL_Action_Diff"), foregroundColor, backgroundColor);
            revertLink.set(NbBundle.getMessage(SummaryView.class, "CTL_Action_Revert"), foregroundColor, backgroundColor); // NOI18N
        }

        private void renderRevision(JList list, RepositoryRevision.Event dispRevision, final int index, boolean isSelected) {
            Style style;
            StyledDocument sd = textPane.getStyledDocument();

            Color backgroundColor;
            Color foregroundColor;
            
            if (isSelected) {
                foregroundColor = UIManager.getColor("List.selectionForeground"); // NOI18N
                backgroundColor = UIManager.getColor("List.selectionBackground"); // NOI18N
                style = selectedStyle;
            } else {
                foregroundColor = UIManager.getColor("List.foreground"); // NOI18N
                backgroundColor = UIManager.getColor("List.background"); // NOI18N
                style = normalStyle;
            }
            textPane.setBackground(backgroundColor);
            actionsPane.setVisible(false);
            
            this.index = -1;
            try {
                sd.remove(0, sd.getLength());
                sd.setParagraphAttributes(0, sd.getLength(), indentStyle, false);
                
                sd.insertString(sd.getLength(), String.valueOf(dispRevision.getChangedPath().getAction()), null);
                sd.insertString(sd.getLength(), FIELDS_SEPARATOR + dispRevision.getChangedPath().getPath(), null);
                
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (index == -1) return;
            Rectangle apb = actionsPane.getBounds();
            
            {
                Rectangle bounds = diffLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Diff-" + index, bounds); // NOI18N
            }

            Rectangle bounds = revertLink.getBounds();
            bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
            resultsList.putClientProperty(SUMMARY_REVERT_PROPERTY + index, bounds); // NOI18N
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
}
