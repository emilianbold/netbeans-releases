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
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.Component;

/**
 * Shows Search History results in a JList.
 * 
 * @author Maros Sandor
 */
class SummaryView {
    
    private final List  results;
    
    private JList       resultsList;
    private JScrollPane scrollPane;
    
    public SummaryView(List results) {
        this.results = results;
        resultsList = new JList(new SummaryListModel());
        resultsList.setCellRenderer(new SummaryCellRenderer());
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    
    public JComponent getComponent() {
        return scrollPane;
    }

    private class SummaryListModel extends AbstractListModel {

        public int getSize() {
            return results.size();
        }

        public Object getElementAt(int index) {
            return results.get(index);
        }
    }
    
    private class SummaryCellRenderer extends JTextPane implements ListCellRenderer {

        private LogInformation.Revision revision;
        private boolean selected;
        private boolean hasFocus;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            revision = (LogInformation.Revision) value;
            selected = isSelected;
            hasFocus = cellHasFocus;
            
            StyledDocument sd = getStyledDocument();
            try {
                sd.remove(0, sd.getLength());
                SimpleAttributeSet as = new SimpleAttributeSet(); 
                sd.insertString(0, revision.getMessage() + "\n\n", as);
                sd.insertString(0, revision.getLogInfoHeader().getRepositoryFilename() + "\n", as);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }

            return this;
        }
    }
}
