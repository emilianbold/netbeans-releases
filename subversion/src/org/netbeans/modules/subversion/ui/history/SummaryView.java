/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.cookies.ViewCookie;
import org.openide.ErrorManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.update.RevertModifications;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
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
import java.text.DateFormat;

/**
 * @author Maros Sandor
 */
/**
 * Shows Search History results in a JList.
 * 
 * @author Maros Sandor
 */
class SummaryView implements MouseListener, ComponentListener, MouseMotionListener {

    private final SearchHistoryPanel master;
    
    private JList       resultsList;
    private JScrollPane scrollPane;

    private final List  dispResults;
    private String      message;
    private AttributeSet searchHiliteAttrs;

    public SummaryView(SearchHistoryPanel master, List results) {
        this.master = master;
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
    
    private List expandResults(List results) {
        ArrayList newResults = new ArrayList(results.size());
        for (Iterator i = results.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof SearchHistoryPanel.ResultsContainer) {
                newResults.add(o);
                SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
                for (Iterator j = container.getRevisions().iterator(); j.hasNext();) {
                    SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) j.next();
                    newResults.add(revision);
                }
                for (Iterator j = container.getRevisions().iterator(); j.hasNext();) {
                    SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) j.next();
                    addResults(newResults, revision, 1);
                }
            } else {
                newResults.add(o);
                addResults(newResults, (SearchHistoryPanel.DispRevision) o, 0);
            }
        }
        return newResults;
    }

    private void addResults(ArrayList newResults, SearchHistoryPanel.DispRevision dispRevision, int indentation) {
        dispRevision.setIndentation(indentation);
        List children = dispRevision.getChildren();
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext();) {
                SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) i.next();
                newResults.add(revision);
            }
            for (Iterator i = children.iterator(); i.hasNext();) {
                SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) i.next();
                addResults(newResults, revision, indentation + 1);
            }
        }
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
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Fc-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            associatedChanges(idx);
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
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Fc-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onPopup(MouseEvent e) {
        int [] sel = resultsList.getSelectedIndices();
        if (sel.length == 0) {
            int idx = resultsList.locationToIndex(e.getPoint());
            if (idx == -1) return;
            resultsList.setSelectedIndex(idx);
            sel = new int [] { idx };
        }
        final int [] selection = sel;

        JPopupMenu menu = new JPopupMenu();
        
        String previousRevision = null;
        SearchHistoryPanel.ResultsContainer container = null; 
        SearchHistoryPanel.DispRevision drev = null;
        Object revCon = dispResults.get(selection[0]);
        if (revCon instanceof SearchHistoryPanel.ResultsContainer) {
            container = (SearchHistoryPanel.ResultsContainer) dispResults.get(selection[0]); 
        } else {
            drev = (SearchHistoryPanel.DispRevision) dispResults.get(selection[0]);
            previousRevision = SvnUtils.previousRevision(drev.getRevision().getNumber().trim());
        }
        if (container != null) {
            String eldest = container.getEldestRevision();
            if (eldest == null) {
                eldest = ((SearchHistoryPanel.DispRevision) container.getRevisions().get(container.getRevisions().size() - 1)).getRevision().getNumber();
            }
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_Diff", eldest, container.getNewestRevision())) { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    diffPrevious(selection[0]);
                }
            }));
        } else {
            if (previousRevision != null) {
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", previousRevision)) { // NOI18N
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(selection[0]);
                    }
                }));
            }
        }
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackChange")) { // NOI18N
            {
                setEnabled(someRevisions(selection));
            }
            public void actionPerformed(ActionEvent e) {
                rollbackChange(selection);
            }
        }));
        if (drev != null) {
            if (!"dead".equals(drev.getRevision().getState())) { // NOI18N
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View", drev.getRevision().getNumber())) { // NOI18N
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
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
        }
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindCommit")) { // NOI18N
            {
                setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
            }
            public void actionPerformed(ActionEvent e) {
                associatedChanges(selection[0]);
            }
        }));

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private boolean someRevisions(int[] selection) {
        for (int i = 0; i < selection.length; i++) {
            Object revCon = dispResults.get(selection[i]);
            if (revCon instanceof SearchHistoryPanel. DispRevision) {
                return true;
            }
        }
        return false;
    }

    private void rollbackChange(int [] selection) {
        List changes = new ArrayList();
        for (int i = 0; i < selection.length; i++) {
            int idx = selection[i];
            Object o = dispResults.get(idx);
            if (o instanceof SearchHistoryPanel.DispRevision) {
                SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
                changes.add(drev.getRevision());
            }
        }
        rollbackChanges((LogInformation.Revision[]) changes.toArray(new LogInformation.Revision[changes.size()]));
    }

    private static void rollbackChange(LogInformation.Revision change) {
        File file = change.getLogInfoHeader().getFile();
        String revision = change.getNumber();
        
        final Context ctx = new Context(file);
        SVNUrl url = SvnUtils.getRepositoryRootUrl(file);
        RepositoryFile repositoryFile = new RepositoryFile(url, url, SVNRevision.HEAD);
        final RevertModifications revertModifications = new RevertModifications(repositoryFile, revision);

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {                    
                RevertModificationsAction.performRevert(ctx, revertModifications, this);
            }
        };
        support.start(rp, url, NbBundle.getMessage(SummaryView.class, "MSG_Revert_Progress")); // NOI18N
    }

    static void rollbackChanges(LogInformation.Revision [] changes) {
        for (int i = 0; i < changes.length; i++) {
            rollbackChange(changes[i]);
        }
    }
    
    private void view(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            String revision = drev.getRevision().getNumber().trim();
            ViewCookie view = (ViewCookie) new RevisionNode(drev).getCookie(ViewCookie.class);
            if (view != null) {
                view.view();
            }
        }
    }    
    private void diffPrevious(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            master.showDiff(drev);
        } else {
            SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
            master.showDiff(container);
        }
    }

    private void associatedChanges(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            long revision = Long.parseLong(drev.getRevision().getNumber());
            File file = drev.getRevision().getLogInfoHeader().getFile();
            // look for the top folder that is checked out from the same repository
            SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(drev.getRevision().getLogInfoHeader().getFile());
            File rootFile = file;
            for (;;) {
                File rootParent = rootFile.getParentFile();
                if (rootParent == null || !repoUrl.equals(SvnUtils.getRepositoryRootUrl(rootParent))) break;
                rootFile = rootParent;
            }
            SearchHistoryAction.openSearch(repoUrl, rootFile, revision);
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
        private HyperlinkLabel  fcLink;

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
            StyleConstants.setBackground(hiliteStyle, (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background));
            StyleConstants.setForeground(hiliteStyle, (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground));
            
            setLayout(new BorderLayout());
            add(textPane);
            add(actionsPane, BorderLayout.PAGE_END);
            actionsPane.setLayout(new FlowLayout(FlowLayout.TRAILING, 2, 5));
            textPane.setBorder(null);
        }
        
        public Color darker(Color c) {
            return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0), 
                 Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
                 Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SearchHistoryPanel.ResultsContainer) {
                renderContainer(list, (SearchHistoryPanel.ResultsContainer) value, index, isSelected);
            } else {
                renderRevision(list, (SearchHistoryPanel.DispRevision) value, index, isSelected);
            }
            return this;
        }

        private void renderContainer(JList list, SearchHistoryPanel.ResultsContainer container, int index, boolean isSelected) {

            StyledDocument sd = textPane.getStyledDocument();

            Style style;
            if (isSelected) {
                textPane.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                actionsPane.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                style = selectedStyle;
            } else {
                Color c = UIManager.getColor("List.background"); // NOI18N
                textPane.setBackground((index & 1) == 0 ? c : darker(c));
                actionsPane.setBackground((index & 1) == 0 ? c : darker(c));
                style = normalStyle;
            }
            
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                sd.insertString(0, container.getName(), null);
                sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                sd.insertString(sd.getLength(), FIELDS_SEPARATOR + container.getPath(), null);
                sd.setCharacterAttributes(0, sd.getLength(), style, false);
                sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            actionsPane.removeAll();
            actionsPane.revalidate();
        }

        private void renderRevision(JList list, SearchHistoryPanel.DispRevision dispRevision, final int index, boolean isSelected) {
            Style style;
            StyledDocument sd = textPane.getStyledDocument();

            this.index = index;
            
            Color backgroundColor;
            Color foregroundColor;
            
            if (isSelected) {
                foregroundColor = UIManager.getColor("List.selectionForeground"); // NOI18N
                backgroundColor = UIManager.getColor("List.selectionBackground"); // NOI18N
                style = selectedStyle;
            } else {
                foregroundColor = UIManager.getColor("List.foreground"); // NOI18N
                backgroundColor = UIManager.getColor("List.background"); // NOI18N
                backgroundColor = (index & 1) == 0 ? backgroundColor : darker(backgroundColor); 
                style = normalStyle;
            }
            textPane.setBackground(backgroundColor);
            actionsPane.setBackground(backgroundColor);
            
            LogInformation.Revision revision = dispRevision.getRevision();
            String commitMessage = revision.getMessage();
            if (commitMessage.endsWith("\n")) commitMessage = commitMessage.substring(0, commitMessage.length() - 1); // NOI18N
            int indentation = dispRevision.getIndentation();
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                if (indentation == 0) {
                    sd.insertString(0, dispRevision.getRevision().getLogInfoHeader().getFile().getName(), null);
                    sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + dispRevision.getName().substring(0, dispRevision.getName().lastIndexOf('/')) + "\n", null); // NOI18N
                }
                sd.insertString(sd.getLength(), revision.getNumber() + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), defaultFormat.format(revision.getDate()) + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), revision.getAuthor(), null);
                if ("dead".equalsIgnoreCase(dispRevision.getRevision().getState())) { // NOI18N
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + NbBundle.getMessage(SummaryView.class, "MSG_SummaryView_DeadState"), null); // NOI18N
                }
                sd.insertString(sd.getLength(), "\n", null); // NOI18N
                sd.insertString(sd.getLength(), commitMessage, null);
                if (message != null && !isSelected) {
                    int idx = revision.getMessage().indexOf(message);
                    if (idx != -1) {
                        int len = commitMessage.length();
                        int doclen = sd.getLength();
                        sd.setCharacterAttributes(doclen - len + idx, message.length(), hiliteStyle, false);
                    }
                }
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                if (indentation > 0) {
                    sd.setParagraphAttributes(0, sd.getLength(), indentStyle, false);
                } else {
                    sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            if (commitMessage != null) {
                int width = master.getWidth();
                if (width > 0) {
                    FontMetrics fm = list.getFontMetrics(list.getFont());
                    Rectangle2D rect = fm.getStringBounds(commitMessage, textPane.getGraphics());
                    int nlc, i;
                    for (nlc = -1, i = 0; i != -1 ; i = commitMessage.indexOf('\n', i + 1), nlc++);
                    if (indentation == 0) nlc++;
                    int lines = (int) (rect.getWidth() / (width - 80) + 1);
                    int ph = fm.getHeight() * (lines + nlc + 1) + 0;
                    textPane.setPreferredSize(new Dimension(width - 50, ph));
                }
            }
            
            actionsPane.removeAll();
            String prev = SvnUtils.previousRevision(dispRevision.getRevision().getNumber());
            if (prev != null) {
                JLabel l1 = new JLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_DiffTo")); // NOI18N
                l1.setForeground(foregroundColor);
                actionsPane.add(l1);
                diffLink = new HyperlinkLabel(prev, foregroundColor, backgroundColor);
                actionsPane.add(diffLink);
                JLabel comma = new JLabel(", "); // NOI18N
                comma.setForeground(foregroundColor);
                actionsPane.add(comma);
            } else {
                diffLink = null;
            }

            fcLink = new HyperlinkLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindCommit"), foregroundColor, backgroundColor); // NOI18N
            actionsPane.add(fcLink);

            actionsPane.revalidate();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle apb = actionsPane.getBounds();
            if (diffLink != null) {
                Rectangle bounds = diffLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Diff-" + index, bounds); // NOI18N
            }
            if (fcLink != null) {
                Rectangle bounds = fcLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Fc-" + index, bounds); // NOI18N
            }
        }
    }
    
    private static class HyperlinkLabel extends JLabel {
        
        public HyperlinkLabel(String text, Color foreground, Color background) {
            if (foreground.equals(UIManager.getColor("List.foreground"))) { // NOI18N
                setText("<html><a href=\"\">" + text + "</a></html>"); // NOI18N
            } else {
                String clr = "rgb(" + foreground.getRed() + "," + foreground.getGreen() + "," + foreground.getBlue() + ")"; // NOI18N
                setText("<html><a href=\"\" style=\"color:" + clr + "\">" + text + "</a></html>"); // NOI18N
            }
            setBackground(background);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
