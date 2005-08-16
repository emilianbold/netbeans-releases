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
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffExecutor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.io.File;

/**
 * Shows Search History results in a JList.
 * 
 * @author Maros Sandor
 */
class SummaryView implements MouseListener {

    private final SearchHistoryPanel master;
    
    private JList       resultsList;
    private JScrollPane scrollPane;

    private final List  dispResults;
    private String      message;

    public SummaryView(SearchHistoryPanel master, List results) {
        this.master = master;
        this.dispResults = expandResults(results);
        message = master.getCriteria().getCommitMessage();
        resultsList = new JList(new SummaryListModel());
        resultsList.addMouseListener(this);
        resultsList.setCellRenderer(new SummaryCellRenderer());
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
        // not interested
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

    private void onPopup(MouseEvent e) {
        final int [] selection = resultsList.getSelectedIndices();
        
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindAssociateChanges")) {
            {
                setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
            }
            public void actionPerformed(ActionEvent e) {
                findAssociateChanges(selection[0]);
            }
        }));

        String previousRevision = null;
        try {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) dispResults.get(selection[0]);
            previousRevision = Utils.previousRevision(drev.getRevision().getNumber().trim());
        } catch (Exception ex) {
            previousRevision = NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_Previous");
        }
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", previousRevision)) {
            {
                setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
            }
            public void actionPerformed(ActionEvent e) {
                diffPrevious(selection[0]);
            }
        }));
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void diffPrevious(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            DiffExecutor de = new DiffExecutor(file.getName());
            String revision = drev.getRevision().getNumber().trim();
            de.showDiff(file, Utils.previousRevision(revision), revision);
        }
    }

    private void findAssociateChanges(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            SearchHistoryAction.openSearch(NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), drev.getRevision().getNumber()), 
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
    
    private class SummaryCellRenderer extends JTextPane implements ListCellRenderer {

        private static final String FIELDS_SEPARATOR = "        ";

        private Style selectedStyle;
        private Style normalStyle;
        private Style filenameStyle;
        private Style indentStyle;
        private Style noindentStyle;
        private Style hiliteStyle;
        
        private DateFormat defaultFormat;

        public SummaryCellRenderer() {
            selectedStyle = this.addStyle("selected", null);
            StyleConstants.setForeground(selectedStyle, UIManager.getColor("List.selectionForeground"));
            normalStyle = this.addStyle("normal", null);
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground"));
            filenameStyle = this.addStyle("filename", normalStyle);
            StyleConstants.setBold(filenameStyle, true);
            indentStyle = this.addStyle("indent", null);
            StyleConstants.setLeftIndent(indentStyle, 50);
            noindentStyle = this.addStyle("noindent", null);
            StyleConstants.setLeftIndent(noindentStyle, 0);
            defaultFormat = DateFormat.getDateTimeInstance();

            hiliteStyle = this.addStyle("hilite", normalStyle);
            // TODO: take this color from Editor.Settings
            StyleConstants.setBackground(hiliteStyle, new Color(246, 248, 139));
        }

        private static final double FACTOR = 0.95;
        
        public Color darker(Color c) {
            return new Color(Math.max((int)(c.getRed()  *FACTOR), 0), 
                 Math.max((int)(c.getGreen()*FACTOR), 0),
                 Math.max((int)(c.getBlue() *FACTOR), 0));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof SearchHistoryPanel.ResultsContainer) {
                renderContainer((SearchHistoryPanel.ResultsContainer) value, index, isSelected);
            } else {
                renderRevision((SearchHistoryPanel.DispRevision) value, index, isSelected);
            }
            return this;
        }

        private void renderContainer(SearchHistoryPanel.ResultsContainer container, int index, boolean isSelected) {

            StyledDocument sd = getStyledDocument();

            Style style;
            if (isSelected) {
                this.setBackground(UIManager.getColor("List.selectionBackground"));
                style = selectedStyle;
            } else {
                Color c = UIManager.getColor("List.background");
                this.setBackground((index & 1) == 0 ? c : darker(c));
                style = normalStyle;
            }
            
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                sd.insertString(0, container.getName() + "\n", null);
                sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                
                LogInformation.Revision newestRev = (LogInformation.Revision) container.getRevisions().get(0);
                LogInformation.Revision oldestRev = (LogInformation.Revision) container.getRevisions().get(container.getRevisions().size() - 1);
                
                sd.insertString(sd.getLength(), oldestRev.getNumber() + " - " + newestRev.getNumber() + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), defaultFormat.format(newestRev.getDate()) + "\n", null);
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                sd.setParagraphAttributes(0, Integer.MAX_VALUE, noindentStyle, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        private void renderRevision(SearchHistoryPanel.DispRevision dispRevision, int index, boolean isSelected) {
            Style style;
            StyledDocument sd = getStyledDocument();

            if (isSelected) {
                this.setBackground(UIManager.getColor("List.selectionBackground"));
                style = selectedStyle;
            } else {
                Color c = UIManager.getColor("List.background");
                this.setBackground((index & 1) == 0 ? c : darker(c));
                style = normalStyle;
            }
            
            LogInformation.Revision revision = dispRevision.getRevision();
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                if (!dispRevision.isIndented()) {
                    sd.insertString(0, dispRevision.getName() + "\n", null);
                    sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                }
                sd.insertString(sd.getLength(), revision.getNumber() + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), defaultFormat.format(revision.getDate()) + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), revision.getAuthor() + "\n", null);
                sd.insertString(sd.getLength(), revision.getMessage(), null);
                if (message != null && !isSelected) {
                    int idx = revision.getMessage().indexOf(message);
                    if (idx != -1) {
                        int len = revision.getMessage().length();
                        int doclen = sd.getLength();
                        sd.setCharacterAttributes(doclen - len + idx, message.length(), hiliteStyle, false);
                    }
                }
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                if (dispRevision.isIndented()) {
                    sd.setParagraphAttributes(0, Integer.MAX_VALUE, indentStyle, false);
                } else {
                    sd.setParagraphAttributes(0, Integer.MAX_VALUE, noindentStyle, false);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

}
