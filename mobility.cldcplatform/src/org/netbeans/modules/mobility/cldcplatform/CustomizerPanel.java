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

/*
 * CustomizerPanel.java
 *
 * Created on April 5, 2004, 5:50 PM
 */
package org.netbeans.modules.mobility.cldcplatform;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
final public class CustomizerPanel extends javax.swing.JPanel implements ChangeListener, DocumentListener, FocusListener {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(560, 350);
    
    public static final String PREFS_TOOL = "prefs"; // NOI18N
    public static final String UTILS_TOOL = "utils"; // NOI18N
    public static final String MEM_PROP = "kvem.memory.monitor.enable"; // NOI18N
    public static final String JAM_PROP = "jammode"; // NOI18N
    public static final String NET_PROP = "kvem.netmon.enable"; // NOI18N
    public static final String NET_HTTP_PROP = "kvem.netmon.http.enable"; // NOI18N
    public static final String NET_HTTPS_PROP = "kvem.netmon.https.enable"; // NOI18N
    public static final String PROF_PROP = "kvem.profiler.enable"; // NOI18N
    public static final String FALSE = "false"; // NOI18N
    
    String fileChooserValue;
    J2MEPlatform platform;
    DefaultListModel mSources;
    DefaultListModel mJavaDocs;
    boolean mMemoryMonitor;
    boolean mNetworkMonitor;
    boolean mProfiler;
    String preverifyInfo, executionInfo, debuggerInfo;
    ListDataListener sourcesListener = new ListDataListener() {
        public void intervalAdded(@SuppressWarnings("unused")
		final ListDataEvent e) {
            saveSources();
        }
        public void intervalRemoved(@SuppressWarnings("unused")
		final ListDataEvent e) {
            saveSources();
        }
        public void contentsChanged(@SuppressWarnings("unused")
		final ListDataEvent e) {
            saveSources();
        }
    };
    ListDataListener javadocsListener = new ListDataListener() {
        public void intervalAdded(@SuppressWarnings("unused")
		final ListDataEvent e) {
            saveJavadocs();
        }
        public void intervalRemoved(@SuppressWarnings("unused")
		final ListDataEvent e) {
            saveJavadocs();
        }
        public void contentsChanged(@SuppressWarnings("unused")
		final ListDataEvent e) {
            saveJavadocs();
        }
    };
    
    /** Creates new form CustomizerPanel */
    public CustomizerPanel(J2MEPlatform platform) {
        initComponents();
        infoPanel.setEditorKitForContentType("text/html", new HTMLEditorKit()); // NOI18N
        infoPanel.setContentType("text/html;charset=UTF-8"); // NOI18N
        if (platform.getType().equalsIgnoreCase( "custom")) jTabbedPane1.remove(jPanel5); //NOI18N
        else jTabbedPane1.remove(jPanel6);
        mSources = new DefaultListModel();
        lSourcePaths.setModel(mSources);
        mJavaDocs = new DefaultListModel();
        lJavaDocPaths.setModel(mJavaDocs);
        loadData(platform);
        addListeners();
    }
    
    void addListeners() {
        mSources.addListDataListener(sourcesListener);
        mJavaDocs.addListDataListener(javadocsListener);
        cMemoryMonitor.addChangeListener(this);
        cNetworkMonitor.addChangeListener(this);
        cProfiler.addChangeListener(this);
        jTextFieldPreverify.getDocument().addDocumentListener(this);
        jTextFieldExec.getDocument().addDocumentListener(this);
        jTextFieldDebug.getDocument().addDocumentListener(this);
        
        jTextFieldPreverify.addFocusListener(this);
        jTextFieldExec.addFocusListener(this);
        jTextFieldDebug.addFocusListener(this);
        
        preverifyInfo = loadInfo("nbresloc:/org/netbeans/modules/mobility/cldcplatform/customwizard/preverifyinfo.html"); // NOI18N
        executionInfo = loadInfo("nbresloc:/org/netbeans/modules/mobility/cldcplatform/customwizard/executioninfo.html"); // NOI18N
        debuggerInfo = loadInfo("nbresloc:/org/netbeans/modules/mobility/cldcplatform/customwizard/debuggerinfo.html"); // NOI18N
    }
    
    private String loadInfo(final String location) {
        final StringBuffer sb = new StringBuffer();
        InputStreamReader reader = null; // NOI18N
        try {
            reader = new InputStreamReader(new URL(location).openStream(), "UTF-8");//NOI18N
            final char[] chars = new char[4096];
            for (;;) {
                final int len = reader.read(chars, 0, 4096);
                if (len < 0)
                    break;
                sb.append(chars, 0, len);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // TODO
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        } finally {
            if (reader != null) try { reader.close(); } catch (IOException e) {}
        }
        return null;
    }
    
    public void clearData() {
        platform = null;
        mSources.clear();
        mJavaDocs.clear();
        bPreferences.setEnabled(false);
        bUtilities.setEnabled(false);
        cMemoryMonitor.setEnabled(false);
        cMemoryMonitor.setSelected(mMemoryMonitor = false);
        cNetworkMonitor.setEnabled(false);
        cNetworkMonitor.setSelected(mNetworkMonitor = false);
        cProfiler.setEnabled(false);
        cProfiler.setSelected(mProfiler = false);
    }
    
    public void loadData(final J2MEPlatform platform) {
        clearData();
        this.platform = platform;
        if (platform == null)
            return;
        
        if (fileChooserValue == null)
            fileChooserValue = platform.getHomePath();
        final DefaultListModel mDevices = new DefaultListModel();
        final J2MEPlatform.Device[] devices = platform.getDevices();
        if (devices != null) {
            if (devices.length > 0) jTextFieldDevice.setText(devices[0].getName());
            for (int a = 0; a < devices.length; a ++)
                mDevices.addElement(devices[a].isValid() ? devices[a].getName() : ("<html><font color=\"#A40000\"><strike>" + devices[a].getName() + "</strike></font>")); //NOI18N
        }
        lDevices.setModel(mDevices);
        
        jTextFieldPreverify.setText(platform.getPreverifyCmd());
        jTextFieldExec.setText(platform.getRunCmd());
        jTextFieldDebug.setText(platform.getDebugCmd());
        
        final FileObject[] al = platform.getSourceFolders().getRoots();
        mSources.clear();
        if (al != null) 
        	for (FileObject ala : al ) 
        		mSources.addElement(new ListItem<FileObject>(ala));
        
        final List<URL> l = platform.getJavadocFolders();
        mJavaDocs.clear();
        if (l != null) 
        	for (URL la : l )
        		mJavaDocs.addElement(new ListItem<URL>(la));
        
        bPreferences.setEnabled(platform.findTool(PREFS_TOOL) != null);
        bUtilities.setEnabled(platform.findTool(UTILS_TOOL) != null);
        
        final File f = new File(platform.getHomePath(),  "wtklib" + File.separator +  "emulator.properties"); //NOI18N
        if (f.exists()  &&  f.canWrite()) {
            final Properties props = new Properties();
            FileInputStream fis = null;
            try {
                props.load(fis = new FileInputStream(f));
            } catch (IOException e) {
            } finally {
                if (fis != null) {
                    try { fis.close(); } catch (IOException e) {}
                    fis = null;
                }
            }
            cMemoryMonitor.setEnabled(true);
            cNetworkMonitor.setEnabled(true);
            cProfiler.setEnabled(true);
            mMemoryMonitor = Boolean.valueOf(props.getProperty(MEM_PROP, FALSE)).booleanValue();
            cMemoryMonitor.setSelected(mMemoryMonitor);
            mNetworkMonitor = Boolean.valueOf(props.getProperty(NET_PROP, FALSE)).booleanValue();
            cNetworkMonitor.setSelected(mNetworkMonitor);
            mProfiler = Boolean.valueOf(props.getProperty(PROF_PROP, FALSE)).booleanValue();
            cProfiler.setSelected(mProfiler);
        }
        
        refreshButtons();
    }
    
    public void saveSources() {
        if (platform == null)
            return;
        List<FileObject> l;
        Object[] os;
        
        os = mSources.toArray();
        l = new ArrayList<FileObject>();
        if (os != null) 
        	for (Object osa : os) 
        		l.add(((ListItem<FileObject>)osa).getObject());
        platform.setSourceFolders(l);
    }
    
    public void saveJavadocs() {
        if (platform == null)
            return;
        List<URL> l;
        Object[] os;
        os = mJavaDocs.toArray();
        l = new ArrayList<URL>();
        if (os != null) 
            for (Object osa : os)
                l.add(((ListItem<URL>)osa).getObject());
        platform.setJavadocFolders(l);
    }
    
    public void saveWtkExtensions() {
        if (platform == null)
            return;
        
        final File f = new File(platform.getHomePath(),  "wtklib" + File.separator +  "emulator.properties"); //NOI18N
        if (f.exists()  &&  f.canWrite()  &&  cMemoryMonitor.isEnabled()  &&
                (cMemoryMonitor.isSelected() != mMemoryMonitor  ||  cNetworkMonitor.isSelected() != mNetworkMonitor  ||  cProfiler.isSelected() != mProfiler)) {
            final Properties props = new Properties();
            FileInputStream fis = null;
            try {
                props.load(fis = new FileInputStream(f));
            } catch (IOException e) {
            } finally {
                if (fis != null) {
                    try { fis.close(); } catch (IOException e) {}
                    fis = null;
                }
            }
            if (cMemoryMonitor.isSelected() != mMemoryMonitor) {
                final String res = String.valueOf(cMemoryMonitor.isSelected());
                props.setProperty(JAM_PROP,  ""); //NOI18N
                props.setProperty(MEM_PROP, res);
            }
            if (cNetworkMonitor.isSelected() != mNetworkMonitor) {
                final String res = String.valueOf(cNetworkMonitor.isSelected());
                props.setProperty(NET_PROP, res);
                props.setProperty(NET_HTTP_PROP, res);
                props.setProperty(NET_HTTPS_PROP, res);
            }
            if (cProfiler.isSelected() != mProfiler) {
                final String res = String.valueOf(cProfiler.isSelected());
                props.setProperty(PROF_PROP, res);
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                props.store(fos, null);
            } catch (IOException e) {
            } finally {
                if (fos != null) {
                    try { fos.close(); } catch (IOException e) {}
                    fos = null;
                }
            }
        }
    }
    
    public void refreshButtons() {
        bSourceRemove.setEnabled(lSourcePaths.getSelectedValue() != null);
        bSourceMoveUp.setEnabled(lSourcePaths.getSelectedIndex() > 0);
        bSourceMoveDown.setEnabled(lSourcePaths.getSelectedIndex() >= 0  &&  lSourcePaths.getSelectedIndex() < mSources.getSize() - 1);
        bJavaDocRemove.setEnabled(lJavaDocPaths.getSelectedValue() != null);
        bJavaDocMoveUp.setEnabled(lJavaDocPaths.getSelectedIndex() > 0);
        bJavaDocMoveDown.setEnabled(lJavaDocPaths.getSelectedIndex() >= 0  &&  lJavaDocPaths.getSelectedIndex() < mJavaDocs.getSize() - 1);
    }
    
    private String browseArchive(final String oldValue, final String title) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return (f.exists() && f.canRead() && (f.isDirectory() || (f.getName().endsWith(".zip") || f.getName().endsWith(".jar")))); // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(CustomizerPanel.class,"TXT_Customizer_ZipFilter"); // NOI18N
            }
        });
        if (oldValue != null)
            chooser.setSelectedFile(new File(oldValue));
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    
    private String browseFolder(final String oldValue, final String title) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.exists() && f.canRead() && f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(CustomizerPanel.class,"TXT_Customizer_FolderFilter"); // NOI18N
            }
        });
        if (oldValue != null)
            chooser.setSelectedFile(new File(oldValue));
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    
    private void openTool(final FileObject tool) {
        if (tool == null)
            return;
        final File f = FileUtil.toFile(tool);
        if (f == null)
            return;
        try {
            Runtime.getRuntime().exec(f.getAbsolutePath());
        } catch (IOException e) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(CustomizerPanel.class,  "MSG_CustomizerPanel_CannotExecTool", f.getAbsolutePath()))); //NOI18N
        }
    }
    
    static class ListItem<T> {
        
        T o;
        String str;
        
        ListItem(T o) {
            this.o = o;
            if (o instanceof FileObject)
                this.str = J2MEPlatform.getFilePath((FileObject) o);
            else if (o instanceof URL)
                this.str = J2MEPlatform.getFilePath(URLMapper.findFileObject((URL) o));
            if (this.str == null)
                this.str = o != null ? o.toString() : ""; // NOI18N
        }
        
        T getObject() {
            return o;
        }
        
        public String toString() {
            return str;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lDevices = new javax.swing.JList();
        jButtonRefresh = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldDevice = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldPreverify = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldExec = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldDebug = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        infoPanel = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lSourcePaths = new javax.swing.JList();
        bSourceAddArchive = new javax.swing.JButton();
        bSourceAddFolder = new javax.swing.JButton();
        bSourceRemove = new javax.swing.JButton();
        bSourceMoveUp = new javax.swing.JButton();
        bSourceMoveDown = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lJavaDocPaths = new javax.swing.JList();
        bJavaDocAddArchive = new javax.swing.JButton();
        bJavaDocAddFolder = new javax.swing.JButton();
        bJavaDocRemove = new javax.swing.JButton();
        bJavaDocMoveUp = new javax.swing.JButton();
        bJavaDocMoveDown = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        bPreferences = new javax.swing.JButton();
        bUtilities = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cMemoryMonitor = new javax.swing.JCheckBox();
        cNetworkMonitor = new javax.swing.JCheckBox();
        cProfiler = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();

        setEnabled(false);
        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setRequestFocusEnabled(false);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(lDevices);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Devices")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 6);
        jPanel5.add(jLabel3, gridBagConstraints);

        lDevices.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(lDevices);
        lDevices.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "ACD_CustomizerPanel_Devices")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel5.add(jScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRefresh, NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Refresh")); // NOI18N
        jButtonRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_Refresh")); // NOI18N
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel5.add(jButtonRefresh, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_Devices"), jPanel5); // NOI18N

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jLabel6.setLabelFor(jTextFieldDevice);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/cldcplatform/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, bundle.getString("LBL_Customizer_Device")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel6.add(jLabel6, gridBagConstraints);

        jTextFieldDevice.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        jPanel6.add(jTextFieldDevice, gridBagConstraints);

        jLabel7.setLabelFor(jTextFieldPreverify);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Preverify")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 0);
        jPanel6.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 5);
        jPanel6.add(jTextFieldPreverify, gridBagConstraints);

        jLabel8.setLabelFor(jTextFieldExec);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Exec")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 0);
        jPanel6.add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 5);
        jPanel6.add(jTextFieldExec, gridBagConstraints);

        jLabel9.setLabelFor(jTextFieldDebug);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Debug")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 0);
        jPanel6.add(jLabel9, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 5);
        jPanel6.add(jTextFieldDebug, gridBagConstraints);

        infoPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        infoPanel.setEditable(false);
        jScrollPane4.setViewportView(infoPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 5, 5);
        jPanel6.add(jScrollPane4, gridBagConstraints);

        jTabbedPane1.addTab(NbBundle.getMessage(CustomizerPanel.class, "LBL_CMD_Tab"), jPanel6); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(lSourcePaths);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Sources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 6);
        jPanel1.add(jLabel2, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 300));

        lSourcePaths.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lSourcePaths.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lSourcePathsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lSourcePaths);
        lSourcePaths.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "ACD_CustomizerPanel_Sources")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSourceAddArchive, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_AddArchive")); // NOI18N
        bSourceAddArchive.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_SrcAddZip")); // NOI18N
        bSourceAddArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSourceAddArchiveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel1.add(bSourceAddArchive, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSourceAddFolder, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_AddFolder")); // NOI18N
        bSourceAddFolder.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_SrcAddFolder")); // NOI18N
        bSourceAddFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSourceAddFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel1.add(bSourceAddFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSourceRemove, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Remove")); // NOI18N
        bSourceRemove.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_SrcRemove")); // NOI18N
        bSourceRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSourceRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel1.add(bSourceRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSourceMoveUp, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_MoveUp")); // NOI18N
        bSourceMoveUp.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_SrcMoveUp")); // NOI18N
        bSourceMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSourceMoveUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel1.add(bSourceMoveUp, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSourceMoveDown, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_MoveDown")); // NOI18N
        bSourceMoveDown.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_SrcMoveDown")); // NOI18N
        bSourceMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSourceMoveDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel1.add(bSourceMoveDown, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_Sources"), jPanel1); // NOI18N

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(lJavaDocPaths);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_JavaDocs")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 6);
        jPanel2.add(jLabel1, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(500, 300));

        lJavaDocPaths.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lJavaDocPaths.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lJavaDocPathsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lJavaDocPaths);
        lJavaDocPaths.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "ACD_CustomizerPanel_Javadoc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel2.add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bJavaDocAddArchive, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_AddArchive")); // NOI18N
        bJavaDocAddArchive.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_Javadoc_AddJar")); // NOI18N
        bJavaDocAddArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bJavaDocAddArchiveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel2.add(bJavaDocAddArchive, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bJavaDocAddFolder, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_AddFolder")); // NOI18N
        bJavaDocAddFolder.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_JavadocAddFolder")); // NOI18N
        bJavaDocAddFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bJavaDocAddFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel2.add(bJavaDocAddFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bJavaDocRemove, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Remove")); // NOI18N
        bJavaDocRemove.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_JavadocRemove")); // NOI18N
        bJavaDocRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bJavaDocRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel2.add(bJavaDocRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bJavaDocMoveUp, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_MoveUp")); // NOI18N
        bJavaDocMoveUp.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_JavadocMoveUp")); // NOI18N
        bJavaDocMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bJavaDocMoveUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel2.add(bJavaDocMoveUp, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bJavaDocMoveDown, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_MoveDown")); // NOI18N
        bJavaDocMoveDown.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_JavadocMoveDown")); // NOI18N
        bJavaDocMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bJavaDocMoveDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel2.add(bJavaDocMoveDown, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_JavaDocs"), jPanel2); // NOI18N

        jPanel3.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_OpenTools")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_OpenTools")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel3.add(jLabel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bPreferences, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Preferences")); // NOI18N
        bPreferences.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_ToolsPreferences")); // NOI18N
        bPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bPreferencesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel3.add(bPreferences, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bUtilities, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Utilities")); // NOI18N
        bUtilities.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_ToolsUtilities")); // NOI18N
        bUtilities.setPreferredSize(bPreferences.getPreferredSize());
        bUtilities.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUtilitiesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel3.add(bUtilities, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_WTKExtensions")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel3.add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cMemoryMonitor, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_MemoryMonitor")); // NOI18N
        cMemoryMonitor.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_ToolsMemoryMon")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        jPanel3.add(cMemoryMonitor, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cNetworkMonitor, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_NetworkMonitor")); // NOI18N
        cNetworkMonitor.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_ToolsNetworkMon")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 6);
        jPanel3.add(cNetworkMonitor, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cProfiler, org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "LBL_Customizer_Profiler")); // NOI18N
        cProfiler.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TTT_CustomizerPanel_ToolsProfiler")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 6);
        jPanel3.add(cProfiler, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jPanel4, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_Tools"), jPanel3); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "ACN_CustomizerPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPanel.class, "ACD_CustomizerPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
  
    
    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
        final J2MEPlatform pfs[] = new J2MEPlatform[] {platform};
        final DetectPanel dtp = new DetectPanel();
        final DialogDescriptor desc = new DialogDescriptor(dtp, NbBundle.getMessage(CustomizerPanel.class,  "TITLE_Customizer_PlatformRefresh"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
        dtp.detectPlatform(pfs, platform.getHomePath(), null, desc);
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            platform.setDevices(pfs[0].getDevices());
            final ArrayList<URL> docs = new ArrayList<URL>(platform.getJavadocFolders());
            for ( final URL o : pfs[0].getJavadocFolders() ) {
                if (!docs.contains(o)) docs.add(o);
            }
            platform.setJavadocFolders(docs);
//            PlatformConvertor.createConfigurationTemplates(platform);
            loadData(platform);
        }
    }//GEN-LAST:event_jButtonRefreshActionPerformed
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    private void bJavaDocMoveDownActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bJavaDocMoveDownActionPerformed
        if (platform == null)
            return;
        final int row = lJavaDocPaths.getSelectedIndex();
        if (row < 0  &&  row >= mJavaDocs.getSize() - 1)
            return;
        final Object value1 = mJavaDocs.get(row);
        final Object value2 = mJavaDocs.get(row + 1);
        mJavaDocs.set(row, value2);
        mJavaDocs.set(row + 1, value1);
        lJavaDocPaths.setSelectedIndex(row + 1);
        refreshButtons();
    }//GEN-LAST:event_bJavaDocMoveDownActionPerformed
    
    private void bJavaDocMoveUpActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bJavaDocMoveUpActionPerformed
        if (platform == null)
            return;
        final int row = lJavaDocPaths.getSelectedIndex();
        if (row <= 0)
            return;
        final Object value1 = mJavaDocs.get(row - 1);
        final Object value2 = mJavaDocs.get(row);
        mJavaDocs.set(row - 1, value2);
        mJavaDocs.set(row, value1);
        lJavaDocPaths.setSelectedIndex(row - 1);
        refreshButtons();
    }//GEN-LAST:event_bJavaDocMoveUpActionPerformed
    
    private void bSourceMoveDownActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSourceMoveDownActionPerformed
        if (platform == null)
            return;
        final int row = lSourcePaths.getSelectedIndex();
        if (row < 0  &&  row >= mSources.getSize() - 1)
            return;
        final Object value1 = mSources.get(row);
        final Object value2 = mSources.get(row + 1);
        mSources.set(row, value2);
        mSources.set(row + 1, value1);
        lSourcePaths.setSelectedIndex(row + 1);
        refreshButtons();
    }//GEN-LAST:event_bSourceMoveDownActionPerformed
    
    private void bSourceMoveUpActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSourceMoveUpActionPerformed
        if (platform == null)
            return;
        final int row = lSourcePaths.getSelectedIndex();
        if (row <= 0)
            return;
        final Object value1 = mSources.get(row - 1);
        final Object value2 = mSources.get(row);
        mSources.set(row - 1, value2);
        mSources.set(row, value1);
        lSourcePaths.setSelectedIndex(row - 1);
        refreshButtons();
    }//GEN-LAST:event_bSourceMoveUpActionPerformed
    
    private void bJavaDocAddFolderActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bJavaDocAddFolderActionPerformed
        if (platform == null)
            return;
        final String value = browseFolder(fileChooserValue, NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_SelectJavaDocFolder")); // NOI18N
        if (value == null)
            return;
        fileChooserValue = value;
        final URL o = J2MEPlatform.localfilepath2url(value);
        if (o != null) {
            mJavaDocs.addElement(new ListItem<URL>(o));
            refreshButtons();
        }
    }//GEN-LAST:event_bJavaDocAddFolderActionPerformed
    
    private void lJavaDocPathsValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lJavaDocPathsValueChanged
        refreshButtons();
    }//GEN-LAST:event_lJavaDocPathsValueChanged
    
    private void lSourcePathsValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lSourcePathsValueChanged
        refreshButtons();
    }//GEN-LAST:event_lSourcePathsValueChanged
    
    private void bSourceAddFolderActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSourceAddFolderActionPerformed
        if (platform == null)
            return;
        final String value = browseFolder(fileChooserValue, NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_SelectSourceFolder")); // NOI18N
        if (value == null)
            return;
        fileChooserValue = value;
        final FileObject o = platform.resolveRelativePathToFileObject(value);
        if (o != null) {
            mSources.addElement(new ListItem<FileObject>(o));
            refreshButtons();
        }
    }//GEN-LAST:event_bSourceAddFolderActionPerformed
    
    private void bUtilitiesActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUtilitiesActionPerformed
        openTool(platform.findTool(UTILS_TOOL));
    }//GEN-LAST:event_bUtilitiesActionPerformed
    
    private void bPreferencesActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPreferencesActionPerformed
        openTool(platform.findTool(PREFS_TOOL));
    }//GEN-LAST:event_bPreferencesActionPerformed
    
    private void bJavaDocRemoveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bJavaDocRemoveActionPerformed
        final Object selected = lJavaDocPaths.getSelectedValue();
        if (selected == null)
            return;
        mJavaDocs.removeElement(selected);
        refreshButtons();
    }//GEN-LAST:event_bJavaDocRemoveActionPerformed
    
    private void bJavaDocAddArchiveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bJavaDocAddArchiveActionPerformed
        if (platform == null)
            return;
        final String value = browseArchive(fileChooserValue, NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_SelectJavaDocArchive")); // NOI18N
        if (value == null)
            return;
        fileChooserValue = value;
        final URL o = J2MEPlatform.localfilepath2url(value);
        if (o != null) {
            mJavaDocs.addElement(new ListItem<URL>(o));
            refreshButtons();
        }
    }//GEN-LAST:event_bJavaDocAddArchiveActionPerformed
    
    private void bSourceRemoveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSourceRemoveActionPerformed
        final Object selected = lSourcePaths.getSelectedValue();
        if (selected == null)
            return;
        mSources.removeElement(selected);
        refreshButtons();
    }//GEN-LAST:event_bSourceRemoveActionPerformed
    
    private void bSourceAddArchiveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSourceAddArchiveActionPerformed
        if (platform == null)
            return;
        final String value = browseArchive(fileChooserValue, NbBundle.getMessage(CustomizerPanel.class, "TITLE_Customizer_SelectSourceArchive")); // NOI18N
        if (value == null)
            return;
        fileChooserValue = value;
        final FileObject o = platform.resolveRelativePathToFileObject(value);
        if (o != null) {
            mSources.addElement(new ListItem<FileObject>(o));
            refreshButtons();
        }
    }//GEN-LAST:event_bSourceAddArchiveActionPerformed
    
    public void stateChanged(@SuppressWarnings("unused")
	final javax.swing.event.ChangeEvent e) {
        saveWtkExtensions();
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void changedUpdate(final DocumentEvent e) {
        final Document d = e.getDocument();
        if (jTextFieldPreverify.getDocument() == d) {
            platform.setPreverifyCmd(jTextFieldPreverify.getText());
        } else if (jTextFieldExec.getDocument() == d) {
            platform.setRunCmd(jTextFieldExec.getText());
        } else {
            platform.setDebugCmd(jTextFieldDebug.getText());
        }
    }
    
    public void focusGained(final FocusEvent e) {
        final Component component = e.getComponent();
        if (component == jTextFieldPreverify)
            infoPanel.setText(preverifyInfo);
        else if (component == jTextFieldExec)
            infoPanel.setText(executionInfo);
        else if (component == jTextFieldDebug)
            infoPanel.setText(debuggerInfo);
        infoPanel.setCaretPosition(0);
    }
    
    public void focusLost(final FocusEvent e) {
        final Component oppositeComponent = e.getOppositeComponent();
        if (oppositeComponent != null
                &&  oppositeComponent != jTextFieldPreverify
                &&  oppositeComponent != jTextFieldExec
                &&  oppositeComponent != jTextFieldDebug
                &&  oppositeComponent != infoPanel)
            infoPanel.setText(""); // NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bJavaDocAddArchive;
    private javax.swing.JButton bJavaDocAddFolder;
    private javax.swing.JButton bJavaDocMoveDown;
    private javax.swing.JButton bJavaDocMoveUp;
    private javax.swing.JButton bJavaDocRemove;
    private javax.swing.JButton bPreferences;
    private javax.swing.JButton bSourceAddArchive;
    private javax.swing.JButton bSourceAddFolder;
    private javax.swing.JButton bSourceMoveDown;
    private javax.swing.JButton bSourceMoveUp;
    private javax.swing.JButton bSourceRemove;
    private javax.swing.JButton bUtilities;
    private javax.swing.JCheckBox cMemoryMonitor;
    private javax.swing.JCheckBox cNetworkMonitor;
    private javax.swing.JCheckBox cProfiler;
    private javax.swing.JTextPane infoPanel;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldDebug;
    private javax.swing.JTextField jTextFieldDevice;
    private javax.swing.JTextField jTextFieldExec;
    private javax.swing.JTextField jTextFieldPreverify;
    private javax.swing.JList lDevices;
    private javax.swing.JList lJavaDocPaths;
    private javax.swing.JList lSourcePaths;
    // End of variables declaration//GEN-END:variables
    
}
