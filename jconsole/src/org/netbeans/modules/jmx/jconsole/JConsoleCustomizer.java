/*
 * JConsoleCustomizer.java
 *
 * Created on October 26, 2006, 2:20 PM
 */

package org.netbeans.modules.jmx.jconsole;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.jmx.j2seproject.customizer.PathController;

/**
 *
 * @author  jfdenise
 */
public class JConsoleCustomizer extends javax.swing.JPanel {
    private boolean changed;
    private boolean initialized;
    private PathController pluginsController;
    private PathController classpathController;
    private JFileChooser classPathchooser;
    private JFileChooser pluginsChooser;
    private static class CustomizerFileFilter extends FileFilter {
        private String type;
        CustomizerFileFilter(String type) {
            this.type = type;
        }
        public boolean accept(File f) {
            if(f != null) {
                if(f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if(extension != null && extension.equals( "jar")) {// NOI18N
                    return true;
                };
            }
            return false;
        }
        
        public static String getExtension(File f) {
            if(f != null) {
                String filename = f.getName();
                int i = filename.lastIndexOf('.');
                if(i>0 && i<filename.length()-1) {
                    return filename.substring(i+1).toLowerCase();
                };
            }
            return null;
        }
        
        public String getDescription() {
            return  "JConsole "+ type + " path (jar or dir)";// NOI18N
        }
    }
    
    private class ChangedListener implements ListDataListener, KeyListener {
        public void intervalAdded(ListDataEvent arg0) {
        }
        
        public void intervalRemoved(ListDataEvent arg0) {
        }
        
        public void contentsChanged(ListDataEvent arg0) {
            changed();
        }
    
        public void keyTyped(KeyEvent arg0) {
            changed();
        }

        public void keyPressed(KeyEvent arg0) {
            
        }

        public void keyReleased(KeyEvent arg0) {
            
        }
}
    
    private class KeyLstnr implements KeyListener {
        
        KeyLstnr() {
        }
        
        public void keyTyped(KeyEvent e) {
            changed();
            char c = e.getKeyChar();
            if (!(Character.isDigit(c) ||
                    c == KeyEvent.VK_BACK_SPACE ||
                    c == KeyEvent.VK_DELETE)) {
                e.consume();
            }
        }
        
        public void keyPressed(KeyEvent e) {
        }
        
        public void keyReleased(KeyEvent e) {
        }
        
        public void focusGained(FocusEvent e) {
            Object source = e.getSource();
            Component opposite = e.getOppositeComponent();
            
            if (!e.isTemporary() &&
                    source instanceof JTextField &&
                    opposite instanceof JComponent ) {
                
                ((JTextField)source).selectAll();
            }
        }
        
        public void focusLost(FocusEvent e) {
            
        }
    }
    
    /** Creates new form JConsoleCustomizer */
    public JConsoleCustomizer() {
        initComponents();
        
        classPathchooser = new JFileChooser();
        classPathchooser.setMultiSelectionEnabled(true);
        classPathchooser.setFileFilter(new CustomizerFileFilter("Class"));// NOI18N
        classPathchooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        pluginsChooser = new JFileChooser();
        pluginsChooser.setMultiSelectionEnabled(true);
        pluginsChooser.setFileFilter(new CustomizerFileFilter("Plugins"));// NOI18N
        pluginsChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        ChangedListener changedListener = new ChangedListener();
        
        pluginsController = new PathController(jList1, pathLabel, jButtonAddJarC,
                pluginsChooser,
                jButtonRemoveC,
                jButtonMoveUpC,jButtonMoveDownC, changedListener);
        
        classpathController = new PathController(jList2, pathLabel1, jButtonAddJarC1,
                classPathchooser,
                jButtonRemoveC1,
                jButtonMoveUpC1,jButtonMoveDownC1, changedListener);
        
        pluginsController.setVisible(JConsoleSettings.isNetBeansJVMGreaterThanJDK15());
        
        KeyLstnr listener = new KeyLstnr();
        period.addKeyListener(listener);
        
        otherArgs.addKeyListener(changedListener);
        vmOptions.addKeyListener(changedListener);
        defaultUrl.addKeyListener(changedListener);
    }
    
    synchronized void changed() {
        changed = true;
    }
    
    synchronized void update() {
        String path = JConsoleSettings.getDefault().getClassPath();
        String url = JConsoleSettings.getDefault().getDefaultUrl();
        String plugins = JConsoleSettings.getDefault().getPluginsPath();
        Integer polling = JConsoleSettings.getDefault().getPolling();
        Boolean tileVal = JConsoleSettings.getDefault().getTile();
        String vmArgs = JConsoleSettings.getDefault().getVMOptions();
        String otherArgsVal = JConsoleSettings.getDefault().getOtherArgs();
        
        classpathController.updateModel(path);
        defaultUrl.setText(url);
        pluginsController.updateModel(plugins);
        period.setText(polling.toString());
        tile.setSelected(tileVal);
        vmOptions.setText(vmArgs);
        otherArgs.setText(otherArgsVal);
        changed = false;
        initialized = true;
    }
    
    synchronized void applyChanges() {
        if(!initialized) return;
        JConsoleSettings.getDefault().setClassPath(classpathController.toString());
        JConsoleSettings.getDefault().setDefaultUrl(defaultUrl.getText());
        JConsoleSettings.getDefault().setPluginsPath(pluginsController.toString());
        JConsoleSettings.getDefault().setPolling(Integer.valueOf(period.getText()));
        JConsoleSettings.getDefault().setTile(tile.isSelected());
        JConsoleSettings.getDefault().setVMOptions(vmOptions.getText());
        JConsoleSettings.getDefault().setOtherArgs(otherArgs.getText());
        changed = false;
    }
    
    void cancel() {
        
    }
    
    boolean dataValid() {
        return true;
    }
    
    synchronized boolean isChanged() {
        return true;
        // Too buggy...
        //return changed;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        defaultUrl = new javax.swing.JTextField();
        tile = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        period = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        vmOptions = new javax.swing.JTextField();
        otherArgs = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButtonAddJarC = new javax.swing.JButton();
        jButtonMoveUpC = new javax.swing.JButton();
        jButtonMoveDownC = new javax.swing.JButton();
        jButtonRemoveC = new javax.swing.JButton();
        pathLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jButtonAddJarC1 = new javax.swing.JButton();
        jButtonMoveUpC1 = new javax.swing.JButton();
        jButtonMoveDownC1 = new javax.swing.JButton();
        jButtonRemoveC1 = new javax.swing.JButton();
        pathLabel1 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.border.title"))); // NOI18N

        jLabel1.setDisplayedMnemonic('P');
        jLabel1.setLabelFor(period);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_POLLING_PERIOD_DESCRIPTION")); // NOI18N

        defaultUrl.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.defaultUrl.text")); // NOI18N

        tile.setMnemonic('T');
        tile.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.tile.text")); // NOI18N
        tile.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_TILE_WINDOWS_DESCRIPTION")); // NOI18N
        tile.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel2.setDisplayedMnemonic('D');
        jLabel2.setLabelFor(defaultUrl);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_DEFAULT_URL_DESCRIPTION")); // NOI18N

        period.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.period.text")); // NOI18N

        jLabel5.setDisplayedMnemonic('V');
        jLabel5.setLabelFor(vmOptions);
        jLabel5.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_VM_OPTIONS_DESCRIPTION")); // NOI18N

        vmOptions.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.vmOptions.text")); // NOI18N

        otherArgs.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.otherArgs.text")); // NOI18N
        otherArgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherArgsActionPerformed(evt);
            }
        });

        jLabel6.setDisplayedMnemonic('A');
        jLabel6.setLabelFor(otherArgs);
        jLabel6.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_OTHER_ARGS_DESCRIPTION")); // NOI18N

        jScrollPane1.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jList1.AccessibleContext.accessibleName")); // NOI18N
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jList1.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonAddJarC.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveUpC.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveDownC.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonRemoveC.text")); // NOI18N
        jButtonRemoveC.setActionCommand(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonRemoveC.actionCommand")); // NOI18N

        pathLabel.setDisplayedMnemonic('l');
        pathLabel.setLabelFor(jList1);
        pathLabel.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pathLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonRemoveC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonAddJarC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonMoveUpC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonMoveDownC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(pathLabel)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(pathLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonAddJarC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonMoveUpC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonMoveDownC))
                    .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jButtonAddJarC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonAddJarC.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveUpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveUpC.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveDownC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveDownC.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveDownC.getAccessibleContext().setAccessibleParent(jButtonAddJarC);
        jButtonRemoveC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonRemoveC.AccessibleContext.accessibleDescription")); // NOI18N
        pathLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pathLabel.AccessibleContext.accessibleDescription")); // NOI18N

        jScrollPane2.setViewportView(jList2);
        jList2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jList2.AccessibleContext.accessibleName")); // NOI18N
        jList2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jList2.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC1, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonAddJarC1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC1, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveUpC1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC1, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveDownC1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC1, org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonRemoveC1.text")); // NOI18N

        pathLabel1.setDisplayedMnemonic('C');
        pathLabel1.setLabelFor(jList2);
        pathLabel1.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pathLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonRemoveC1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonAddJarC1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonMoveUpC1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonMoveDownC1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(pathLabel1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(pathLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonAddJarC1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveC1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonMoveUpC1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonMoveDownC1))
                    .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jButtonAddJarC1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonAddJarC1.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveUpC1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveUpC1.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveDownC1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonMoveDownC1.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonRemoveC1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jButtonRemoveC1.AccessibleContext.accessibleDescription")); // NOI18N
        pathLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pathLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tile, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(otherArgs)
                            .addComponent(vmOptions)
                            .addComponent(period)
                            .addComponent(defaultUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(period, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)))
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(vmOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(otherArgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        defaultUrl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.defaultUrl.AccessibleContext.accessibleName")); // NOI18N
        defaultUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.defaultUrl.AccessibleContext.accessibleDescription")); // NOI18N
        period.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.period.AccessibleContext.accessibleName")); // NOI18N
        period.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.period.AccessibleContext.accessibleDescription")); // NOI18N
        vmOptions.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.vmOptions.AccessibleContext.accessibleName")); // NOI18N
        vmOptions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.vmOptions.AccessibleContext.accessibleDescription")); // NOI18N
        otherArgs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.otherArgs.AccessibleContext.accessibleName")); // NOI18N
        otherArgs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.otherArgs.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void otherArgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherArgsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_otherArgsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField defaultUrl;
    private javax.swing.JButton jButtonAddJarC;
    private javax.swing.JButton jButtonAddJarC1;
    private javax.swing.JButton jButtonMoveDownC;
    private javax.swing.JButton jButtonMoveDownC1;
    private javax.swing.JButton jButtonMoveUpC;
    private javax.swing.JButton jButtonMoveUpC1;
    private javax.swing.JButton jButtonRemoveC;
    private javax.swing.JButton jButtonRemoveC1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField otherArgs;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel pathLabel1;
    private javax.swing.JTextField period;
    private javax.swing.JCheckBox tile;
    private javax.swing.JTextField vmOptions;
    // End of variables declaration//GEN-END:variables
    
}

