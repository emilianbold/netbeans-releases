/*
 * JConsoleCustomizer.java
 *
 * Created on October 26, 2006, 2:20 PM
 */

package org.netbeans.modules.jmx.jconsole;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author  jfdenise
 */
public class JConsoleCustomizer extends javax.swing.JPanel {
    private boolean changed;
    private boolean initialized;
    
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
    
    private class ChangedListener implements KeyListener {
        public void keyTyped(java.awt.event.KeyEvent e) {
            changed();
        }

        public void keyPressed(java.awt.event.KeyEvent e) {
        }

        public void keyReleased(java.awt.event.KeyEvent e) {
        }
        
    }
    
    private class MultiPurposeListener implements FocusListener,
            ActionListener, KeyListener {
        private JFileChooser classPathchooser;
        private JFileChooser pluginsChooser;
        MultiPurposeListener() {
            classPathchooser = new JFileChooser();
            classPathchooser.setMultiSelectionEnabled(true);
            classPathchooser.setFileFilter(new CustomizerFileFilter("Class"));
            classPathchooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            pluginsChooser = new JFileChooser();
            pluginsChooser.setMultiSelectionEnabled(true);
            pluginsChooser.setFileFilter(new CustomizerFileFilter("Plugins"));
            pluginsChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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
        
        public void actionPerformed(ActionEvent e) {
            changed();
            if (e.getActionCommand().equals("pluginsBrowse")) {// NOI18N
                updateField(pluginsPath, pluginsChooser);
                
            }else
                if (e.getActionCommand().equals("classBrowse")) {// NOI18N
                    updateField(classPath, classPathchooser);
                }
        }
        
        private void updateField(JTextField field, JFileChooser chooser) {
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                String current = field.getText();
                if(current != null && !current.equals(""))
                    current = current + File.pathSeparator;
                
                File[] selection = chooser.getSelectedFiles();
                int size = selection.length;
                StringBuffer buff = new StringBuffer();
                for(int i = 0; i < size; i++) {
                    buff.append(selection[i].getAbsolutePath());
                    if(i < size - 1)
                        buff.append(File.pathSeparator);
                }
                field.setText(current + buff.toString());
            }
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
        if(!JConsoleSettings.isNetBeansJVMGreaterThanJDK15()) {
            //Hidding plugins
            pluginsBrowse.setVisible(false);
            pluginsPath.setVisible(false);
            pluginsLabel.setVisible(false);
        }
        
        classpathBrowse.setActionCommand("classBrowse");
        pluginsBrowse.setActionCommand("pluginsBrowse");
        MultiPurposeListener listener = new MultiPurposeListener();
        classpathBrowse.addActionListener(listener);
        pluginsBrowse.addActionListener(listener);
        
        period.addKeyListener(listener);
        tile.addActionListener(listener);
        
        ChangedListener changedListener = new ChangedListener();
        classPath.addKeyListener(changedListener);
        pluginsPath.addKeyListener(changedListener);
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
        
        classPath.setText(path);
        defaultUrl.setText(url);
        pluginsPath.setText(plugins);
        period.setText(polling.toString());
        tile.setSelected(tileVal);
        vmOptions.setText(vmArgs);
        otherArgs.setText(otherArgsVal);
        changed = false;
        initialized = true;
    }
    
    synchronized void applyChanges() {
        if(!initialized) return;
        JConsoleSettings.getDefault().setClassPath(classPath.getText());
        JConsoleSettings.getDefault().setDefaultUrl(defaultUrl.getText());
        JConsoleSettings.getDefault().setPluginsPath(pluginsPath.getText());
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        defaultUrl = new javax.swing.JTextField();
        tile = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        period = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        classPath = new javax.swing.JTextField();
        classpathBrowse = new javax.swing.JButton();
        pluginsLabel = new javax.swing.JLabel();
        pluginsPath = new javax.swing.JTextField();
        pluginsBrowse = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        vmOptions = new javax.swing.JTextField();
        otherArgs = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_POLLING_PERIOD_DESCRIPTION")); // NOI18N

        defaultUrl.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.defaultUrl.text")); // NOI18N

        tile.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.tile.text")); // NOI18N
        tile.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_TILE_WINDOWS_DESCRIPTION")); // NOI18N
        tile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tile.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel2.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_DEFAULT_URL_DESCRIPTION")); // NOI18N

        period.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.period.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_CLASSPATH_DESCRIPTION")); // NOI18N

        classPath.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.classPath.text")); // NOI18N

        classpathBrowse.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.classpathBrowse.text")); // NOI18N

        pluginsLabel.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pluginsLabel.text")); // NOI18N
        pluginsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_PLUGINS_PATH_DESCRIPTION")); // NOI18N

        pluginsPath.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pluginsPath.text")); // NOI18N

        pluginsBrowse.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.pluginsBrowse.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_VM_OPTIONS_DESCRIPTION")); // NOI18N

        vmOptions.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.vmOptions.text")); // NOI18N

        otherArgs.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.otherArgs.text")); // NOI18N
        otherArgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherArgsActionPerformed(evt);
            }
        });

        jLabel6.setText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "JConsoleCustomizer.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(JConsoleCustomizer.class, "PROPERTY_OTHER_ARGS_DESCRIPTION")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tile)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel5)
                            .add(jLabel6)
                            .add(jLabel3)
                            .add(pluginsLabel)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(pluginsPath)
                            .add(classPath)
                            .add(otherArgs)
                            .add(vmOptions)
                            .add(period)
                            .add(defaultUrl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(pluginsBrowse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(classpathBrowse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tile)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(defaultUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(period, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel1)))
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(vmOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(otherArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(classpathBrowse)
                    .add(classPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pluginsLabel)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(pluginsBrowse)
                        .add(pluginsPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void otherArgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherArgsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_otherArgsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classPath;
    private javax.swing.JButton classpathBrowse;
    private javax.swing.JTextField defaultUrl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField otherArgs;
    private javax.swing.JTextField period;
    private javax.swing.JButton pluginsBrowse;
    private javax.swing.JLabel pluginsLabel;
    private javax.swing.JTextField pluginsPath;
    private javax.swing.JCheckBox tile;
    private javax.swing.JTextField vmOptions;
    // End of variables declaration//GEN-END:variables
    
}

