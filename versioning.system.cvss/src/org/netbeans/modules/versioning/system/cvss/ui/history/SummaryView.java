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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import java.io.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.ErrorManager;
import org.openide.cookies.ViewCookie;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.text.DateFormat;
import java.io.File;

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
        resultsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SummaryView.class, "ACSN_SummaryView_List"));
        resultsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SummaryView.class, "ACSD_SummaryView_List"));
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
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acp-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            associatedChangesInProject(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acop-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            associatedChangesInOpenProjects(idx);
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
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acp-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acop-" + idx); // NOI18N
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
            previousRevision = Utils.previousRevision(drev.getRevision().getNumber().trim());
        }
        if (container != null) {
            String eldest = container.getEldestRevision();
            if (eldest == null) {
                eldest = ((SearchHistoryPanel.DispRevision) container.getRevisions().get(container.getRevisions().size() - 1)).getRevision().getNumber();
            }
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_Diff", eldest, container.getNewestRevision())) {
                public void actionPerformed(ActionEvent e) {
                    diffPrevious(selection[0]);
                }
            }));
        } else {
            if (previousRevision != null) {
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", previousRevision)) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(selection[0]);
                    }
                }));
            }
        }
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackChange")) {
            {
                setEnabled(someRevisions(selection));
            }
            public void actionPerformed(ActionEvent e) {
                rollbackChange(selection);
            }
        }));
        if (drev != null) {
            if (!"dead".equals(drev.getRevision().getState())) { // NOI18N
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackTo", drev.getRevision().getNumber())) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        rollback(selection[0]);
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View", drev.getRevision().getNumber())) {
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

            Project prj = Utils.getProject(drev.getRevision().getLogInfoHeader().getFile());
            if (prj != null) {
                String prjName = ProjectUtils.getInformation(prj).getDisplayName();
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_Action_AssociateChangesInProject", prjName)) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        associatedChangesInProject(selection[0]);
                    }
                }));
            }
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_Action_AssociateChangesInOpenProjects")) {
                {
                    setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                }
                public void actionPerformed(ActionEvent e) {
                    associatedChangesInOpenProjects(selection[0]);
                }
            }));
        }

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

    private static void rollbackChange(LogInformation.Revision change, ExecutorGroup group) {
        UpdateCommand cmd = new UpdateCommand();
        cmd.setFiles(new File [] { change.getLogInfoHeader().getFile() });
        cmd.setMergeRevision1(change.getNumber());
        cmd.setMergeRevision2(Utils.previousRevision(change.getNumber()));
        group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null));
    }

    static void rollbackChanges(LogInformation.Revision [] changes) {
        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(SummaryView.class, "MSG_SummaryView_RollingBackChange"));
        for (int i = 0; i < changes.length; i++) {
            rollbackChange(changes[i], group);
        }
        group.execute();
    }
    
    private void rollback(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            String revision = drev.getRevision().getNumber().trim();
            File file = drev.getRevision().getLogInfoHeader().getFile();
            GetCleanAction.rollback(file, revision);
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

    private void associatedChangesInOpenProjects(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            Project [] projects  = OpenProjects.getDefault().getOpenProjects();
            int n = projects.length;
            SearchHistoryAction.openSearch(
                    (n == 1) ? ProjectUtils.getInformation(projects[0]).getDisplayName() : 
                    NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_OpenProjects_Title", Integer.toString(n)),
                    drev.getRevision().getMessage().trim(), drev.getRevision().getAuthor(), drev.getRevision().getDate());
        }
    }

    private void associatedChangesInProject(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            Project project = Utils.getProject(file);                
            Context context = Utils.getProjectsContext(new Project[] { Utils.getProject(file) });
            SearchHistoryAction.openSearch(
                    context, 
                    ProjectUtils.getInformation(project).getDisplayName(),
                    drev.getRevision().getMessage().trim(), drev.getRevision().getAuthor(), drev.getRevision().getDate());
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
        private HyperlinkLabel  acpLink;
        private HyperlinkLabel  acopLink;

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
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + NbBundle.getMessage(SummaryView.class, "MSG_SummaryView_DeadState"), null);
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
            String prev = Utils.previousRevision(dispRevision.getRevision().getNumber());
            if (prev != null) {
                JLabel l1 = new JLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_DiffTo"));
                l1.setForeground(foregroundColor);
                actionsPane.add(l1);
                diffLink = new HyperlinkLabel(prev, foregroundColor, backgroundColor);
                diffLink.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
                actionsPane.add(diffLink);
            } else {
                diffLink = null;
            }

            Project [] projects  = OpenProjects.getDefault().getOpenProjects();
            if (projects.length > 0) {
                acopLink = new HyperlinkLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindCommitInOpenProjects"), foregroundColor, backgroundColor);
            } else {
                acopLink = null;
            }
            Project prj = Utils.getProject(dispRevision.getRevision().getLogInfoHeader().getFile());
            if (prj != null) {
                String prjName = ProjectUtils.getInformation(prj).getDisplayName();
                acpLink = new HyperlinkLabel("\"" + prjName + "\"", foregroundColor, backgroundColor); // NOI18N
            } else {
                acpLink = null;
            }

            if (acpLink != null || acopLink != null) {
                JLabel l1 = new JLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindCommitIn"));
                l1.setForeground(foregroundColor);
                actionsPane.add(l1);
                if (acpLink != null) {
                    actionsPane.add(acpLink);
                }
                if (acopLink != null) {
                    if (acpLink != null) {
                        JLabel l2 = new JLabel(","); // NOI18N
                        l2.setForeground(foregroundColor);
                        actionsPane.add(l2);
                    }
                    actionsPane.add(acopLink);
                }
            }
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
            if (acpLink != null) {
                Rectangle bounds = acpLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Acp-" + index, bounds); // NOI18N
            }
            if (acopLink != null) {
                Rectangle bounds = acopLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Acop-" + index, bounds); // NOI18N
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
