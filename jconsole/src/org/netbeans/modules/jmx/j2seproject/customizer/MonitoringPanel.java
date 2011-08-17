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
import org.netbeans.modules.jmx.common.runtime.J2SEProjectType;
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
        MultiPurposeListener(String path) {
            chooser = new JFileChooser(path);
            chooser.setFileFilter(new MgtFileFilter());
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
                return;
            }
            
            if(e.getActionCommand().equals( "attach")) {// NOI18N
                boolean selected = attachJConsole.isSelected();
                period.setEnabled(selected);
                periodLabel.setEnabled(selected);
                classpath.setEnabled(selected);
                pluginsClasspath.setEnabled(selected);
                pathController.setEnabled(selected);
               
                pluginsLabel.setEnabled(selected);
                return;
            }
            
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
                return;
            }
            
            if (e.getActionCommand().equals( "file")) {// NOI18N
                rmiPort.setEnabled(false);
                confFile.setEnabled(true);
                browse.setEnabled(true);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        confFile.requestFocus();
                        confFile.selectAll();
                    }
                });
                return;
            }
            
            if (e.getActionCommand().equals( "browse")) {// NOI18N
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    confFile.setText(chooser.getSelectedFile().getAbsolutePath());
                }
                return;
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
    
    private PathController pathController;
    
    /** Creates new form MonitoringPanel */
    public MonitoringPanel(Project project, boolean plugins) {
        initComponents();
        
        // Plugins are supported for JDK 6 only
        pluginsClasspath.setVisible(plugins);
        pluginsLabel.setVisible(plugins);
        
        
        
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
        
        String periodVal = projectProperties.getProperty(ManagementCompositePanelProvider.POLLING_PERIOD_KEY, "4");// NOI18N
        
        boolean resolveClassPath =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.RESOLVE_CLASSPATH_KEY,
                "true")); // NOI18N
        
        String pluginsPathVal = projectProperties.getProperty(ManagementCompositePanelProvider.PLUGINS_PATH_KEY, "");
       
        boolean pluginsClassPathVal =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.PLUGINS_CLASSPATH_KEY,
                "true")); // NOI18N
        
        pluginsClasspath.setSelected(pluginsClassPathVal);
        
        classpath.setSelected(resolveClassPath);
        
        attachJConsole.setSelected(localAttach);
        period.setEnabled(localAttach);
        periodLabel.setEnabled(localAttach);
        classpath.setEnabled(localAttach);
        pluginsClasspath.setEnabled(localAttach);
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
            browse.setEnabled(!usePort);
        }
        
        period.setText(periodVal);
        rmiPort.setText(rmiPortVal);
        confFile.setText(configFile);
        
        
        // Event handling
        
        attachJConsole.setActionCommand("attach");// NOI18N
        enableRMI.setActionCommand("rmi");// NOI18N
        
        choicePort.setActionCommand( "port");// NOI18N
        choiceFile.setActionCommand( "file");// NOI18N
        browse.setActionCommand( "browse");// NOI18N
        
        listener = new MultiPurposeListener(projectRootDir);
        
        attachJConsole.addActionListener(listener);
        enableRMI.addActionListener(listener);
        choicePort.addActionListener(listener);
        choiceFile.addActionListener(listener);
        browse.addActionListener(listener);
        confFile.addFocusListener(listener);
        rmiPort.addKeyListener(listener);
        period.addKeyListener(listener);
        JFileChooser pluginsChooser = new JFileChooser(projectRootDir);
        pluginsChooser.setMultiSelectionEnabled(true);
        pluginsChooser.setFileFilter(new PluginsFileFilter());
        pluginsChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        pathController = new PathController(jList1, pathLabel, pluginsPathVal, jButtonAddJarC,
                pluginsChooser,
                jButtonRemoveC,
                jButtonMoveUpC,jButtonMoveDownC, null);
        
        pathController.setVisible(plugins);
        pathController.setEnabled(localAttach);
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
           jmxProperties.put(ManagementCompositePanelProvider.PLUGINS_CLASSPATH_KEY, String.valueOf(pluginsClasspath.isSelected()));
            
           
            jmxProperties.put(ManagementCompositePanelProvider.PLUGINS_PATH_KEY, pathController.toString());
            J2SEProjectType.addProjectProperties(jmxProperties, project);
        }catch(MutexException mx) {
            //System.out.println(mx.toString());
            throw new RuntimeException("Error when Storing Management and Monitoring Properties " + mx);// NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        browse = new javax.swing.JButton();
        attachJConsole = new javax.swing.JCheckBox();
        period = new javax.swing.JTextField();
        periodLabel = new javax.swing.JLabel();
        classpath = new javax.swing.JCheckBox();
        pluginsLabel = new javax.swing.JLabel();
        pluginsClasspath = new javax.swing.JCheckBox();
        enableRMI = new javax.swing.JCheckBox();
        choiceFile = new javax.swing.JRadioButton();
        choicePort = new javax.swing.JRadioButton();
        confFile = new javax.swing.JTextField();
        rmiPort = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButtonAddJarC = new javax.swing.JButton();
        jButtonMoveUpC = new javax.swing.JButton();
        jButtonMoveDownC = new javax.swing.JButton();
        jButtonRemoveC = new javax.swing.JButton();
        pathLabel = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(2000, 2000));
        setPreferredSize(new java.awt.Dimension(2000, 2000));

        browse.setMnemonic('B');
        browse.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.browse.text")); // NOI18N

        attachJConsole.setMnemonic('A');
        attachJConsole.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.attachJConsole.text")); // NOI18N
        attachJConsole.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        attachJConsole.setMargin(new java.awt.Insets(0, 0, 0, 0));

        period.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.period.text")); // NOI18N

        periodLabel.setDisplayedMnemonic('P');
        periodLabel.setLabelFor(period);
        periodLabel.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.periodLabel.text")); // NOI18N

        classpath.setMnemonic('s');
        classpath.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.classpath.text")); // NOI18N
        classpath.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classpath.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pluginsLabel.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsLabel.text")); // NOI18N

        pluginsClasspath.setMnemonic('c');
        pluginsClasspath.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsClasspath.text")); // NOI18N
        pluginsClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pluginsClasspath.setMargin(new java.awt.Insets(0, 0, 0, 0));

        enableRMI.setMnemonic('E');
        enableRMI.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.enableRmiRemoteAccessCheckBox.text")); // NOI18N
        enableRMI.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableRMI.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableRMI.setName("enableRmiRemoteAccessCheckBox"); // NOI18N

        buttonGroup1.add(choiceFile);
        choiceFile.setMnemonic('M');
        choiceFile.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.choiceFile.text")); // NOI18N
        choiceFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        choiceFile.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(choicePort);
        choicePort.setMnemonic('I');
        choicePort.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.choicePort.text")); // NOI18N
        choicePort.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        choicePort.setMargin(new java.awt.Insets(0, 0, 0, 0));

        confFile.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.confFile.text")); // NOI18N

        rmiPort.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.rMIPortJTextField.text")); // NOI18N
        rmiPort.setName("rMIPortJTextField"); // NOI18N

        jScrollPane1.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jList1.AccessibleContext.accessibleName")); // NOI18N
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jList1.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonAddJarC.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonMoveUpC.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonMoveDownC.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC, org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonRemoveC.text")); // NOI18N

        pathLabel.setDisplayedMnemonic('l');
        pathLabel.setLabelFor(jList1);
        pathLabel.setText(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pathLabel.text")); // NOI18N

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

        jButtonAddJarC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonAddJarC.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveUpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonMoveUpC.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonMoveDownC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonMoveDownC.AccessibleContext.accessibleDescription")); // NOI18N
        jButtonRemoveC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.jButtonRemoveC.AccessibleContext.accessibleDescription")); // NOI18N
        pathLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pathLabel.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attachJConsole)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(enableRMI, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(pluginsClasspath, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(classpath, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(choicePort)
                                                        .addGap(1, 1, 1))
                                                    .addComponent(choiceFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(rmiPort, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                                                    .addComponent(confFile, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)))
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(periodLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(period, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE))
                                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(browse, javax.swing.GroupLayout.PREFERRED_SIZE, 88, Short.MAX_VALUE)
                                        .addGap(770, 770, 770))
                                    .addComponent(pluginsLabel, javax.swing.GroupLayout.Alignment.LEADING))))
                        .addGap(695, 695, 695)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attachJConsole)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(periodLabel)
                    .addComponent(period, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(classpath)
                .addGap(21, 21, 21)
                .addComponent(pluginsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pluginsClasspath)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addGap(16, 16, 16)
                .addComponent(enableRMI)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choicePort)
                    .addComponent(rmiPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(choiceFile)
                    .addComponent(confFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browse))
                .addGap(1614, 1614, 1614))
        );

        attachJConsole.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.attachJConsole.AccessibleContext.accessibleDescription")); // NOI18N
        period.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.period.AccessibleContext.accessibleName")); // NOI18N
        period.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.period.AccessibleContext.accessibleDescription")); // NOI18N
        periodLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.periodLabel.AccessibleContext.accessibleDescription")); // NOI18N
        classpath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.classpath.AccessibleContext.accessibleDescription")); // NOI18N
        pluginsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        pluginsClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.pluginsClasspath.AccessibleContext.accessibleDescription")); // NOI18N
        enableRMI.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.enableRMI.AccessibleContext.accessibleDescription")); // NOI18N
        choiceFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.choiceFile.AccessibleContext.accessibleDescription")); // NOI18N
        choicePort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MonitoringPanel.class, "MonitoringPanel.choicePort.AccessibleContext.accessibleDescription")); // NOI18N
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
    private javax.swing.JButton jButtonAddJarC;
    private javax.swing.JButton jButtonMoveDownC;
    private javax.swing.JButton jButtonMoveUpC;
    private javax.swing.JButton jButtonRemoveC;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JTextField period;
    private javax.swing.JLabel periodLabel;
    private javax.swing.JCheckBox pluginsClasspath;
    private javax.swing.JLabel pluginsLabel;
    private javax.swing.JTextField rmiPort;
    // End of variables declaration//GEN-END:variables
    
}
