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
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
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
    private final List  results;
    
    private JList       resultsList;
    private JScrollPane scrollPane;

    private final List  dispResults;
    
    public SummaryView(SearchHistoryPanel master, List results) {
        this.master = master;
        this.results = results;
        dispResults = new ArrayList(results.size());
        createDisplayList();
        resultsList = new JList(new SummaryListModel());
        resultsList.addMouseListener(this);
        resultsList.setCellRenderer(new SummaryCellRenderer());
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_FindAssociateChanges")) {
            {
                setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof DispRevision);
            }
            public void actionPerformed(ActionEvent e) {
                findAssociateChanges(selection[0]);
            }
        }));

        String previousRevision = null;
        try {
            DispRevision drev = (DispRevision) dispResults.get(selection[0]);
            previousRevision = Utils.previousRevision(drev.getRevision().getNumber().trim());
        } catch (Exception ex) {
            previousRevision = NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_Previous");
        }
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", previousRevision)) {
            {
                setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof DispRevision);
            }
            public void actionPerformed(ActionEvent e) {
                diffPrevious(selection[0]);
            }
        }));
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void diffPrevious(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof DispRevision) {
            DispRevision drev = (DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            DiffExecutor de = new DiffExecutor(NbBundle.getMessage(SummaryView.class, "CTL_DiffToPrevious_Title", file.getName()));
            String revision = drev.getRevision().getNumber().trim();
            de.showDiff(file, Utils.previousRevision(revision), revision);
        }
    }

    private void findAssociateChanges(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof DispRevision) {
            DispRevision drev = (DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            SearchHistoryAction.openSearch(master.getRoots(), NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), drev.getRevision().getNumber()), 
                                           drev.getRevision().getMessage().trim(), drev.getRevision().getAuthor(), drev.getRevision().getDate());
        }
    }

    private static class ResultsContainer {
        
        private final LogInformation.Revision newestRevision;
        private LogInformation.Revision eldestRevision;
        private String name;

        public ResultsContainer(LogInformation.Revision newestRevision) {
            this.newestRevision = newestRevision;
            File file = newestRevision.getLogInfoHeader().getFile();
            try {
                name = CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getParentFile().getAbsolutePath(), "") + "/" + file.getName();
            } catch (Exception e) {
                name = newestRevision.getLogInfoHeader().getRepositoryFilename();
                if (name.endsWith(",v")) name = name.substring(0, name.lastIndexOf(",v"));
            }
        }

        public String getName() {
            return name;
        }

        public void setEldestRevision(LogInformation.Revision eldestRevision) {
            this.eldestRevision = eldestRevision;
        }

        public LogInformation.Revision getNewestRevision() {
            return newestRevision;
        }

        public LogInformation.Revision getEldestRevision() {
            return eldestRevision;
        }
    }

    private static class DispRevision {
        
        private final LogInformation.Revision revision;
        private final boolean indented;
        private String name;

        public DispRevision(LogInformation.Revision revision, boolean indented) {
            this.revision = revision;
            File file = revision.getLogInfoHeader().getFile();
            try {
                name = CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getParentFile().getAbsolutePath(), "") + "/" + file.getName();
            } catch (Exception e) {
                name = revision.getLogInfoHeader().getRepositoryFilename();
                if (name.endsWith(",v")) name = name.substring(0, name.lastIndexOf(",v"));
            }
            this.indented = indented;
        }

        public String getName() {
            return name;
        }

        public LogInformation.Revision getRevision() {
            return revision;
        }

        public boolean isIndented() {
            return indented;
        }
    }

    private void createDisplayList() {
        Collections.sort(results, new ByRemotePathRevisionNumberComparator());
        ResultsContainer currentContainer = null;
        LogInformation.Revision lastRevision = null;
        int n = results.size();
        for (int i = 0; i < n; i++) {
            LogInformation.Revision revision = (LogInformation.Revision) results.get(i);
            if (!sameCategory(revision, lastRevision)) {
                if (i < n - 1) {
                    LogInformation.Revision nextRevision = (LogInformation.Revision) results.get(i + 1);
                    if (sameCategory(revision, nextRevision)) {
                        currentContainer = new ResultsContainer(revision);
                        dispResults.add(currentContainer);
                    } else {
                        currentContainer = null;
                    }
                }
            }
            if (currentContainer != null) {
                currentContainer.setEldestRevision(lastRevision);
            }
            lastRevision = revision;
            dispResults.add(new DispRevision(revision, currentContainer != null));
        }
    }

    private boolean sameCategory(LogInformation.Revision revision, LogInformation.Revision lastRevision) {
        if (lastRevision == null) return false;
        if (!revision.getLogInfoHeader().getRepositoryFilename().equals(lastRevision.getLogInfoHeader().getRepositoryFilename())) return false;
        String b1 = revision.getNumber().substring(0, revision.getNumber().lastIndexOf('.'));
        String b2 = lastRevision.getNumber().substring(0, lastRevision.getNumber().lastIndexOf('.'));
        return b1.equals(b2);
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
        }

        private static final double FACTOR = 0.95;
        
        public Color darker(Color c) {
            return new Color(Math.max((int)(c.getRed()  *FACTOR), 0), 
                 Math.max((int)(c.getGreen()*FACTOR), 0),
                 Math.max((int)(c.getBlue() *FACTOR), 0));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof ResultsContainer) {
                renderContainer((ResultsContainer) value, index, isSelected);
            } else {
                renderRevision((DispRevision) value, index, isSelected);
            }
            return this;
        }

        private void renderContainer(ResultsContainer container, int index, boolean isSelected) {

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
                sd.insertString(sd.getLength(), container.getEldestRevision().getNumber() + " - " + container.getNewestRevision().getNumber() + FIELDS_SEPARATOR, null);
                sd.insertString(sd.getLength(), defaultFormat.format(container.getNewestRevision().getDate()) + "\n", null);
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                sd.setParagraphAttributes(0, Integer.MAX_VALUE, noindentStyle, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        private void renderRevision(DispRevision dispRevision, int index, boolean isSelected) {
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
    
    private static class ByRemotePathRevisionNumberComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            LogInformation.Revision r1 = (LogInformation.Revision) o1;
            LogInformation.Revision r2 = (LogInformation.Revision) o2;
            int namec = r1.getLogInfoHeader().getRepositoryFilename().compareTo(r2.getLogInfoHeader().getRepositoryFilename());
            if (namec != 0) return namec;
            // 1.2  ?  1.4.4.2
            int revc = r2.getNumber().length() - r1.getNumber().length();
            if (revc != 0) return revc;
            // 1.4.4.3  ?  1.4.4.2
            long r1l = Long.parseLong(r1.getNumber().replaceAll("\\.", ""));
            long r2l = Long.parseLong(r2.getNumber().replaceAll("\\.", ""));
            return r1l < r2l ? 1 : r1l > r2l ? -1 : 0;
        }
    }
}
