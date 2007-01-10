/*
 * MonitoringPanel.java
 *
 * Created on October 25, 2006, 2:38 PM
 */

package org.netbeans.modules.jmx.j2seproject.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jmx.runtime.J2SEProjectType;
import org.openide.filesystems.FileUtil;
import org.openide.util.MutexException;

/**
 *
 * @author  jfdenise
 */
public class MonitoringPanel extends javax.swing.JPanel implements ActionListener {
    private Project project;
    private MultiPurposeListener listener;
    
    private static class MgtFileFilter extends FileFilter {
        
        public boolean accept(File f) {
            if(f != null) {
                if(f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if(extension != null && extension.equals( "properties")) {// NOI18N
                    return true;
                };
            }
            return false;
        }
        
        public String getDescription() {
            return  "Management Configuration File (.properties)";// NOI18N
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
    }
    
    private static class PluginsFileFilter extends FileFilter {
        
        public boolean accept(File f) {
            if(f != null) {
              if(f.isDirectory()) {
                    return true;
                }
                String extension = MgtFileFilter.getExtension(f);
                if(extension != null && extension.equals( "jar")) {// NOI18N
                    return true;
                };
            }
            return false;
        }
        
        public String getDescription() {
            return  "JConsole Plugins path (jar or dir)";// NOI18N
        }
    }
    
    
    private class MultiPurposeListener implements FocusListener,
            ActionListener, KeyListener {
        private JFileChooser chooser;
        private JFileChooser pluginsChooser;
        MultiPurposeListener(String path) {
            chooser = new JFileChooser(path);
            chooser.setFileFilter(new MgtFileFilter());
            pluginsChooser = new JFileChooser(path);
            pluginsChooser.setMultiSelectionEnabled(true); 
            pluginsChooser.setFileFilter(new PluginsFileFilter());
            pluginsChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        
        public void keyTyped(KeyEvent e) {
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
             if(e.getActionCommand().equals( "rmi")) {// NOI18N
                 if(enableRMI.isSelected()) {
                     choicePort.setEnabled(true);
                     choiceFile.setEnabled(true); 
                    if(choicePort.isSelected()) {
                        rmiPort.setEnabled(true);
                        confFile.setEnabled(false);
                        browse.setEnabled(false);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                rmiPort.requestFocus();
                                rmiPort.selectAll();
                            }
                        });
                    } else {
                        rmiPort.setEnabled(false);
                        confFile.setEnabled(true);
                        browse.setEnabled(true);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                confFile.requestFocus();
                                confFile.selectAll();
                            }
                        });
                    }
                 } else {
                        choiceFile.setEnabled(false);
                        choicePort.setEnabled(false);
                        rmiPort.setEnabled(false);
                        confFile.setEnabled(false);
                        browse.setEnabled(false);
               }
            }else
            if(e.getActionCommand().equals( "attach")) {// NOI18N
                boolean selected = attachJConsole.isSelected();
                period.setEnabled(selected);
                periodLabel.setEnabled(selected);
                classpath.setEnabled(selected);
                pluginsClasspath.setEnabled(selected);
                pluginsPath.setEnabled(selected);
                pluginsBrowse.setEnabled(selected);
                pluginsPathLabel.setEnabled(selected);
                pluginsLabel.setEnabled(selected);
            }else
                if(e.getActionCommand().equals( "port")) {// NOI18N
                    rmiPort.setEnabled(true);
                    confFile.setEnabled(false);
                    browse.setEnabled(false);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            rmiPort.requestFocus();
                            rmiPort.selectAll();
                        }
                    });
                }else if (e.getActionCommand().equals( "file")) {// NOI18N
                    rmiPort.setEnabled(false);
                    confFile.setEnabled(true);
                    browse.setEnabled(true);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            confFile.requestFocus();
                            confFile.selectAll();
                        }
                    });
                } else if (e.getActionCommand().equals( "browse")) {// NOI18N
                    int returnVal = chooser.showOpenDialog(null);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        confFile.setText(chooser.getSelectedFile().getAbsolutePath());
                    }
                } else if (e.getActionCommand().equals( "pluginsBrowse")) {// NOI18N
                    int returnVal = pluginsChooser.showOpenDialog(null);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        String current = pluginsPath.getText();
                        if(current != null && !current.equals(""))
                            current = current + File.pathSeparator;
                        
                        File[] selection = pluginsChooser.getSelectedFiles();
                        int size = selection.length;
                        StringBuffer buff = new StringBuffer();
                        for(int i = 0; i < size; i++) {
                            buff.append(selection[i].getAbsolutePath());
                            if(i < size - 1)
                                buff.append(File.pathSeparator);
                        }
                        pluginsPath.setText(current + buff.toString());
                    }
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
    
    /** Creates new form MonitoringPanel */
    public MonitoringPanel(Project project, boolean plugins) {
        initComponents();
        
        // Plugins are supported for JDK 6 only
        pluginsBrowse.setVisible(plugins);
        pluginsClasspath.setVisible(plugins);
        pluginsLabel.setVisible(plugins);
        pluginsPath.setVisible(plugins);
        pluginsPathLabel.setVisible(plugins);
        
        
        
        String projectRootDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        this.project = project;
        
        Properties projectProperties = J2SEProjectType.getProjectProperties(project);
        //Data loading
        // Because we are a standalone module, we are enabling by default the auto attachement
        boolean localAttach =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.ATTACH_JCONSOLE_KEY,"true")); // NOI18N
        boolean rmiConnect =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.ENABLE_RMI_KEY,"false")); // NOI18N
        boolean usePort =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.RMI_USE_PORT_KEY,"true")); // NOI18N
        String rmiPortVal = projectProperties.getProperty(ManagementCompositePanelProvider.RMI_PORT_KEY,"1099"); // NOI18N
        String configFile = projectProperties.getProperty(ManagementCompositePanelProvider.CONFIG_FILE_KEY); // NOI18N
        
        String periodVal = projectProperties.getProperty(ManagementCompositePanelProvider.POLLING_PERIOD_KEY, "4");
        
        boolean resolveClassPath = 
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.RESOLVE_CLASSPATH_KEY, 
                "true")); // NOI18N
        
        String pluginsPathVal = projectProperties.getProperty(ManagementCompositePanelProvider.PLUGINS_PATH_KEY);
        boolean pluginsClassPathVal = 
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.PLUGINS_CLASSPATH_KEY, 
                "true")); // NOI18N
        
        if(pluginsPathVal != null)
            pluginsPath.setText(pluginsPathVal);
        pluginsClasspath.setSelected(pluginsClassPathVal);
        
        classpath.setSelected(resolveClassPath);
        
        attachJConsole.setSelected(localAttach);
        period.setEnabled(localAttach);
        periodLabel.setEnabled(localAttach);
        classpath.setEnabled(localAttach);
        pluginsClasspath.setEnabled(localAttach);
        pluginsPath.setEnabled(localAttach);
        pluginsBrowse.setEnabled(localAttach);
        pluginsPathLabel.setEnabled(localAttach);
        pluginsLabel.setEnabled(localAttach);
        
        enableRMI.setSelected(rmiConnect);
        rmiPort.setEnabled(rmiConnect);
        confFile.setEnabled(rmiConnect);
        browse.setEnabled(rmiConnect);
        choiceFile.setEnabled(rmiConnect);
        choicePort.setEnabled(rmiConnect);
        
        choicePort.setSelected(usePort);
        choiceFile.setSelected(!usePort);
        
        if(rmiConnect) {
            rmiPort.setEnabled(usePort);
            rmiPort.setEnabled(!usePort);
            browse.setEnabled(!usePort);
        }
        
        period.setText(periodVal);
        rmiPort.setText(rmiPortVal);
        confFile.setText(configFile);
        
        
        // Event handling
        
        attachJConsole.setActionCommand("attach");
        enableRMI.setActionCommand("rmi");
        
        choicePort.setActionCommand( "port");
        choiceFile.setActionCommand( "file");
        browse.setActionCommand( "browse");
        pluginsBrowse.setActionCommand("pluginsBrowse");

        listener = new MultiPurposeListener(projectRootDir);
        
        pluginsBrowse.addActionListener(listener);
        attachJConsole.addActionListener(listener);
        enableRMI.addActionListener(listener);
        choicePort.addActionListener(listener);
        choiceFile.addActionListener(listener);
        browse.addActionListener(listener);
        confFile.addFocusListener(listener);
        rmiPort.addKeyListener(listener);
        period.addKeyListener(listener);
        
       
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
        // Strore all properties when OK is clicked.
        Map<String, String> jmxProperties = new HashMap<String, String>();
        
        jmxProperties.put(ManagementCompositePanelProvider.ATTACH_JCONSOLE_KEY, String.valueOf(attachJConsole.isSelected()));
        jmxProperties.put(ManagementCompositePanelProvider.ENABLE_RMI_KEY, String.valueOf(enableRMI.isSelected()));
        jmxProperties.put(ManagementCompositePanelProvider.RMI_USE_PORT_KEY, String.valueOf(choicePort.isSelected()));
        jmxProperties.put(ManagementCompositePanelProvider.RMI_PORT_KEY, rmiPort.getText());
        jmxProperties.put(ManagementCompositePanelProvider.CONFIG_FILE_KEY, confFile.getText());
        jmxProperties.put(ManagementCompositePanelProvider.POLLING_PERIOD_KEY, period.getText());
        jmxProperties.put(ManagementCompositePanelProvider.RESOLVE_CLASSPATH_KEY, String.valueOf(classpath.isSelected()));
        jmxProperties.put(ManagementCompositePanelProvider.PLUGINS_PATH_KEY, pluginsPath.getText());
        jmxProperties.put(ManagementCompositePanelProvider.PLUGINS_CLASSPATH_KEY, String.valueOf(pluginsClasspath.isSelected()));
        J2SEProjectType.addProjectProperties(jmxProperties, project);
        }catch(MutexException mx) {
            System.out.println(mx.toString());
            throw new RuntimeException("Error when Storing Management and Monitoring Properties " + mx);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        attachJConsole = new javax.swing.JCheckBox();
        period = new javax.swing.JTextField();
        periodLabel = new javax.swing.JLabel();
        classpath = new javax.swing.JCheckBox();
        pluginsLabel = new javax.swing.JLabel();
        pluginsClasspath = new javax.swing.JCheckBox();
        pluginsPathLabel = new javax.swing.JLabel();
        pluginsPath = new javax.swing.JTextField();
        pluginsBrowse = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        enableRMI = new javax.swing.JCheckBox();
        choicePort = new javax.swing.JRadioButton();
        rmiPort = new javax.swing.JTextField();
        choiceFile = new javax.swing.JRadioButton();
        confFile = new javax.swing.JTextField();
        browse = new javax.swing.JButton();

        attachJConsole.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.attachJConsole.text")); // NOI18N
        attachJConsole.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        attachJConsole.setMargin(new java.awt.Insets(0, 0, 0, 0));

        period.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.period.text")); // NOI18N

        periodLabel.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.periodLabel.text")); // NOI18N

        classpath.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.classpath.text")); // NOI18N
        classpath.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classpath.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pluginsLabel.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsLabel.text")); // NOI18N

        pluginsClasspath.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsClasspath.text")); // NOI18N
        pluginsClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pluginsClasspath.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pluginsPathLabel.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsPathLabel.text")); // NOI18N

        pluginsPath.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsPath.text")); // NOI18N

        pluginsBrowse.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsBrowse.text")); // NOI18N

        enableRMI.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.enableRMI.text")); // NOI18N
        enableRMI.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableRMI.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(choicePort);
        choicePort.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.choicePort.text")); // NOI18N
        choicePort.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        choicePort.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rmiPort.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.rmiPort.text")); // NOI18N

        buttonGroup1.add(choiceFile);
        choiceFile.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.choiceFile.text")); // NOI18N
        choiceFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        choiceFile.setMargin(new java.awt.Insets(0, 0, 0, 0));

        confFile.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.confFile.text")); // NOI18N

        browse.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.browse.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(choicePort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                        .add(31, 31, 31))
                    .add(enableRMI)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(choiceFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(confFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 226, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rmiPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browse))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(enableRMI)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(choicePort)
                    .add(rmiPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(choiceFile)
                    .add(browse)
                    .add(confFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(29, 29, 29)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(pluginsPathLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(pluginsPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(pluginsBrowse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(pluginsLabel)
                            .add(pluginsClasspath)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(attachJConsole)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(classpath)
                                    .add(layout.createSequentialGroup()
                                        .add(periodLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(period, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(attachJConsole)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(periodLabel)
                    .add(period, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(classpath)
                .add(22, 22, 22)
                .add(pluginsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pluginsClasspath)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pluginsPathLabel)
                    .add(pluginsPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pluginsBrowse))
                .add(54, 54, 54)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox attachJConsole;
    private javax.swing.JButton browse;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton choiceFile;
    private javax.swing.JRadioButton choicePort;
    private javax.swing.JCheckBox classpath;
    private javax.swing.JTextField confFile;
    private javax.swing.JCheckBox enableRMI;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField period;
    private javax.swing.JLabel periodLabel;
    private javax.swing.JButton pluginsBrowse;
    private javax.swing.JCheckBox pluginsClasspath;
    private javax.swing.JLabel pluginsLabel;
    private javax.swing.JTextField pluginsPath;
    private javax.swing.JLabel pluginsPathLabel;
    private javax.swing.JTextField rmiPort;
    // End of variables declaration//GEN-END:variables
    
}