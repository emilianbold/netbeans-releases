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

package org.apache.tools.ant.module.run;

import java.awt.Component;
import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Displays an alert asking user to pick a running build to stop.
 * @author Jesse Glick
 */
final class StopBuildingAlert extends JPanel {
    
    /**
     * Select one or more processes to kill among several choices.
     * @param processesWithDisplayNames a list of possible threads to kill, mapped to display names
     * @return the selection(s) (or empty if cancelled)
     */
    public static Thread[] selectProcessToKill(final Map<Thread,String> processesWithDisplayNames) {
        StopBuildingAlert alert = new StopBuildingAlert(processesWithDisplayNames);
        final JList list = alert.buildsList;
        // Add all threads, sorted by display name.
        DefaultListModel model = new DefaultListModel();
        Comparator<Thread> comp = new Comparator<Thread>() {
            private final Collator coll = Collator.getInstance();
            public int compare(Thread t1, Thread t2) {
                String n1 = processesWithDisplayNames.get(t1);
                String n2 = processesWithDisplayNames.get(t2);
                int r = coll.compare(n1, n2);
                if (r != 0) {
                    return r;
                } else {
                    // Arbitrary. XXX Note that there is no way to predict which is
                    // which if you have more than one build running. Ideally it
                    // would be subsorted by creation time, probably.
                    return System.identityHashCode(t1) - System.identityHashCode(t2);
                }
            }
        };
        SortedSet<Thread> threads = new TreeSet<Thread>(comp);
        threads.addAll(processesWithDisplayNames.keySet());
        for (Thread t : threads) {
            model.addElement(t);
        }
        list.setModel(model);
        list.setSelectedIndex(0);
        // Make a dialog with buttons "Stop Building" and "Cancel".
        DialogDescriptor dd = new DialogDescriptor(alert, NbBundle.getMessage(StopBuildingAlert.class, "TITLE_SBA"));
        dd.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
        final JButton stopButton = new JButton(NbBundle.getMessage(StopBuildingAlert.class, "LBL_SBA_stop"));
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                stopButton.setEnabled(list.getSelectedValue() != null);
            }
        });
        dd.setOptions(new Object[] {stopButton, DialogDescriptor.CANCEL_OPTION});
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (dd.getValue() == stopButton) {
            Object[] _selectedThreads = list.getSelectedValues();
            Thread[] selectedThreads = new Thread[_selectedThreads.length];
            for (int i = 0; i < _selectedThreads.length; i++) {
                selectedThreads[i] = (Thread) _selectedThreads[i];
            }
            return selectedThreads;
        } else {
            return new Thread[0];
        }
    }
    
    private final Map<Thread,String> processesWithDisplayNames;
    
    private StopBuildingAlert(Map<Thread,String> processesWithDisplayNames) {
        this.processesWithDisplayNames = processesWithDisplayNames;
        initComponents();
        buildsList.setCellRenderer(new ProcessCellRenderer());
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        introLabel = new javax.swing.JLabel();
        buildsLabel = new javax.swing.JLabel();
        buildsScrollPane = new javax.swing.JScrollPane();
        buildsList = new javax.swing.JList();

        org.openide.awt.Mnemonics.setLocalizedText(introLabel, org.openide.util.NbBundle.getMessage(StopBuildingAlert.class, "LBL_SBA_intro"));

        buildsLabel.setLabelFor(buildsList);
        org.openide.awt.Mnemonics.setLocalizedText(buildsLabel, org.openide.util.NbBundle.getMessage(StopBuildingAlert.class, "LBL_SBA_select"));

        buildsScrollPane.setViewportView(buildsList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(buildsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(buildsLabel)
                            .add(introLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(28, 28, 28))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(introLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buildsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buildsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addContainerGap())
        );
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel buildsLabel;
    public javax.swing.JList buildsList;
    public javax.swing.JScrollPane buildsScrollPane;
    public javax.swing.JLabel introLabel;
    // End of variables declaration//GEN-END:variables

    private final class ProcessCellRenderer extends DefaultListCellRenderer/*<Thread>*/ {
        
        public ProcessCellRenderer() {}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Thread t = (Thread) value;
            String displayName = processesWithDisplayNames.get(t);
            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
        
    }
    
}
