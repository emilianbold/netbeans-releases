/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

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
        FontColorSettings fcs = (FontColorSettings) MimeLookup.getMimeLookup("text/x-java").lookup(FontColorSettings.class);
        searchHiliteAttrs = fcs.getFontColors("highlight-search");
        message = master.getCriteria().getCommitMessage();
        resultsList = new JList(new SummaryListModel());
        resultsList.setFixedCellHeight(-1);
        resultsList.addMouseListener(this);
        resultsList.addMouseMotionListener(this);
        resultsList.setCellRenderer(new SummaryCellRenderer());
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        master.addComponentListener(this);
    }

    public void componentResized(ComponentEvent e) {
        resultsList.setModel(new SummaryListModel());
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
            newResults.add(o);
            if (o instanceof SearchHistoryPanel.ResultsContainer) {
                SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
                for (Iterator j = container.getRevisions().iterator(); j.hasNext();) {
                    LogInformation.Revision revision = (LogInformation.Revision) j.next();
                    newResults.add(new SearchHistoryPanel.DispRevision(revision, true));
                }
            }
        }
        return newResults;
    }

    public void mouseClicked(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Diff-" + idx);
        if (diffBounds != null && diffBounds.contains(p)) {
            diffPrevious(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acp-" + idx);
        if (diffBounds != null && diffBounds.contains(p)) {
            associatedChangesInProject(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acop-" + idx);
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
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Diff-" + idx);
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acp-" + idx);
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acop-" + idx);
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
                eldest = ((LogInformation.Revision) container.getRevisions().get(container.getRevisions().size() - 1)).getNumber();
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
        if (drev != null) {
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackTo", drev.getRevision().getNumber())) {
                {
                    setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                }
                public void actionPerformed(ActionEvent e) {
                    rollback(selection[0]);
                }
            }));

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

    private void rollback(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            String revision = drev.getRevision().getNumber().trim();
            File file = drev.getRevision().getLogInfoHeader().getFile();
            int res = JOptionPane.showConfirmDialog(
                    null, 
                    NbBundle.getMessage(SummaryView.class, "CTL_Rollback_Prompt", file.getName(), revision),
                    NbBundle.getMessage(SummaryView.class, "CTL_Rollback_Title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (res != JOptionPane.YES_OPTION) return;
            GetCleanAction.rollback(file, revision);
        }
    }

    private void diffPrevious(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            master.showDiff(drev.getRevision());
        } else {
            SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
            master.showDiff(container);
        }
    }

    private void associatedChangesInOpenProjects(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            SearchHistoryAction.openSearch(NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), drev.getRevision().getNumber()), 
                                           drev.getRevision().getMessage().trim(), drev.getRevision().getAuthor(), drev.getRevision().getDate());
        }
    }

    private void associatedChangesInProject(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            Context context = Utils.getProjectsContext(new Project[] { Utils.getProject(file) });
            SearchHistoryAction.openSearch(context, NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), drev.getRevision().getNumber()), 
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

        private static final String FIELDS_SEPARATOR = "        ";
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
            selectedStyle = textPane.addStyle("selected", null);
            StyleConstants.setForeground(selectedStyle, UIManager.getColor("List.selectionForeground"));
            normalStyle = textPane.addStyle("normal", null);
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground"));
            filenameStyle = textPane.addStyle("filename", normalStyle);
            StyleConstants.setBold(filenameStyle, true);
            indentStyle = textPane.addStyle("indent", null);
            StyleConstants.setLeftIndent(indentStyle, 50);
            noindentStyle = textPane.addStyle("noindent", null);
            StyleConstants.setLeftIndent(noindentStyle, 0);
            defaultFormat = DateFormat.getDateTimeInstance();

            hiliteStyle = textPane.addStyle("hilite", normalStyle);
            StyleConstants.setBackground(hiliteStyle, (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background));
            StyleConstants.setForeground(hiliteStyle, (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground));
            
            setLayout(new BorderLayout());
            add(textPane);
            add(actionsPane, BorderLayout.PAGE_END);
            actionsPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            actionsPane.setLayout(new FlowLayout(FlowLayout.TRAILING, 10, 0));
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
                textPane.setBackground(UIManager.getColor("List.selectionBackground"));
                actionsPane.setBackground(UIManager.getColor("List.selectionBackground"));
                style = selectedStyle;
            } else {
                Color c = UIManager.getColor("List.background");
                textPane.setBackground((index & 1) == 0 ? c : darker(c));
                actionsPane.setBackground((index & 1) == 0 ? c : darker(c));
                style = normalStyle;
            }
            
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                sd.insertString(0, container.getName(), null);
                sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                sd.insertString(sd.getLength(), FIELDS_SEPARATOR + container.getPath() + "\n", null);
                
                
                LogInformation.Revision newestRev = (LogInformation.Revision) container.getRevisions().get(0);
                LogInformation.Revision oldestRev = (LogInformation.Revision) container.getRevisions().get(container.getRevisions().size() - 1);
                
                sd.insertString(sd.getLength(), oldestRev.getNumber() + " - " + newestRev.getNumber() + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), defaultFormat.format(newestRev.getDate()) + "\n", null);
                sd.setCharacterAttributes(0, sd.getLength(), style, false);
                sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            actionsPane.removeAll();
            actionsPane.validate();
        }

        private void renderRevision(JList list, SearchHistoryPanel.DispRevision dispRevision, final int index, boolean isSelected) {
            Style style;
            StyledDocument sd = textPane.getStyledDocument();

            this.index = index;
            
            Color backgroundColor;
            Color foregroundColor;
            
            if (isSelected) {
                foregroundColor = UIManager.getColor("List.selectionForeground");
                backgroundColor = UIManager.getColor("List.selectionBackground"); 
                style = selectedStyle;
            } else {
                foregroundColor = UIManager.getColor("List.foreground");
                backgroundColor = UIManager.getColor("List.background");
                backgroundColor = (index & 1) == 0 ? backgroundColor : darker(backgroundColor); 
                style = normalStyle;
            }
            textPane.setBackground(backgroundColor);
            actionsPane.setBackground(backgroundColor);
            
            LogInformation.Revision revision = dispRevision.getRevision();
            String commitMessage = revision.getMessage();
            if (commitMessage.endsWith("\n")) commitMessage = commitMessage.substring(0, commitMessage.length() - 1);
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                if (!dispRevision.isIndented()) {
                    sd.insertString(0, dispRevision.getRevision().getLogInfoHeader().getFile().getName(), null);
                    sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + dispRevision.getName().substring(0, dispRevision.getName().lastIndexOf('/')) + "\n", null);
                }
                sd.insertString(sd.getLength(), revision.getNumber() + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), defaultFormat.format(revision.getDate()) + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), revision.getAuthor() + "\n", null);
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
                if (dispRevision.isIndented()) {
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
                    if (!dispRevision.isIndented()) nlc++;
                    int lines = (int) (rect.getWidth() / (width - 80) + 1);
                    int ph = fm.getHeight() * (lines + nlc + 1) + 10;
                    textPane.setPreferredSize(new Dimension(width - 50, ph));
                }
            }
            
            actionsPane.removeAll();
            String prev = Utils.previousRevision(dispRevision.getRevision().getNumber());
            diffLink = (prev != null) ? new HyperlinkLabel("Diff to " + prev, foregroundColor, backgroundColor)  : null;
            if (diffLink != null) {
                actionsPane.add(diffLink);
            }
            
            Project prj = Utils.getProject(dispRevision.getRevision().getLogInfoHeader().getFile());
            if (prj != null) {
                String prjName = ProjectUtils.getInformation(prj).getDisplayName();
                acpLink = new HyperlinkLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_AssociateChangesInProject", prjName), foregroundColor, backgroundColor);
                actionsPane.add(acpLink);
            } else {
                acpLink = null;
            }
            acopLink = new HyperlinkLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_AssociateChangesInOpenProjects"), foregroundColor, backgroundColor);
            actionsPane.add(acopLink);
            actionsPane.revalidate();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle apb = actionsPane.getBounds();
            if (diffLink != null) {
                Rectangle bounds = diffLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Diff-" + index, bounds);
            }
            if (acpLink != null) {
                Rectangle bounds = acpLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Acp-" + index, bounds);
            }
            if (acopLink != null) {
                Rectangle bounds = acopLink.getBounds();
                bounds.setBounds(bounds.x, bounds.y + apb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Acop-" + index, bounds);
            }
        }
    }
    
    private static class HyperlinkLabel extends JLabel {
        
        public HyperlinkLabel(String text, Color foreground, Color background) {
            if (foreground.equals(UIManager.getColor("List.foreground"))) {
                setText("<html><a href=\"\">" + text + "</a></html>");
            } else {
                String clr = "rgb(" + foreground.getRed() + "," + foreground.getGreen() + "," + foreground.getBlue() + ")";
                setText("<html><a href=\"\" style=\"color:" + clr + "\">" + text + "</a></html>");
            }
            setBackground(background);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
