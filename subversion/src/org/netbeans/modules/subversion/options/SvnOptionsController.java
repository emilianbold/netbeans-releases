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

package org.netbeans.modules.subversion.options;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class SvnOptionsController extends OptionsPanelController implements ActionListener, AWTEventListener, ListSelectionListener {

    private final SvnOptionsPanel panel; 
    private final Repository repository;    
    private Set<String> urlsToRemove;

    private ActionListener removeActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            onRemoveClick();
        }
    };
    
    public SvnOptionsController() {
        panel = new SvnOptionsPanel();
        panel.connectionsSettingsPanel.setLayout(new BorderLayout());
                
        repository = new Repository(SvnModuleConfig.getDefault().getRecentUrls(), true, false, removeActionListener, 
                                    org.openide.util.NbBundle.getMessage(SvnOptionsController.class, "CTL_Repository_Location")); // NOI18N        
        JPanel repositoryJpanel = repository.getPanel();
        repositoryJpanel.setBackground(new Color(255,255,255));
        panel.connectionsSettingsPanel.add(repositoryJpanel);
        panel.browseButton.addActionListener(this);
        panel.labelsButton.addActionListener(this);
    }
    
    public void update () {
        
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault().getExecutableBinaryPath());        
        panel.annotationTextField.setText(SvnModuleConfig.getDefault().getAnnotationFormat());
                
        repository.refreshUrlHistory(SvnModuleConfig.getDefault().getRecentUrls());
        
    }
    
    public void applyChanges () {        
                
        // executable
        SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.executablePathTextField.getText());     
        // XXX only if value changed?
        // Subversion.setupSvnClientFactory(); this won't work anyway because the svnclientadapter doesn't allow more setups per client!
        
        // labels
        SvnModuleConfig.getDefault().setAnnotationFormat(panel.annotationTextField.getText());             
        Subversion.getInstance().getAnnotator().refreshFormat();
        Subversion.getInstance().refreshAllAnnotations();
        
        panel.annotationTextField.setText(SvnModuleConfig.getDefault().getAnnotationFormat());
        if(getUrlsToRemove().size() > 0) {
            SvnModuleConfig.getDefault().removeFromRecentUrls(urlsToRemove.toArray(new String[urlsToRemove.size()]));
            getUrlsToRemove().clear();
        }                        
                
        // connection ???
        
    }
    
    public void cancel () {
        getUrlsToRemove().clear();
    }
    
    public boolean isValid () {
        return true;        
    }
    
    public boolean isChanged () {
        return false; // NOI18N // XXX
    }
    
    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx("netbeans.optionsDialog.advanced.subversion");
    }

    public javax.swing.JComponent getComponent (org.openide.util.Lookup masterLookup) {
        return panel;
    }

    public void addPropertyChangeListener (java.beans.PropertyChangeListener l) {
        
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.browseButton) {
            onBrowseClick();
        } else if (evt.getSource() == panel.labelsButton) {
            onLabelsClick();
        }
    }
    
    private File getExecutableFile() { 
        String execPath = panel.executablePathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }
    
    private void onBrowseClick() {
        File oldFile = getExecutableFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(SvnOptionsController.class, "ACSD_BrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(SvnOptionsController.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
                for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(SvnOptionsController.class, "SVNExec");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }
    
    private void onRemoveClick() {
        try {
            String urlToRemove = repository.getSelection();
            if (urlToRemove != null) {
                getUrlsToRemove().add(urlToRemove);                
                repository.removefromModel(urlToRemove);                                                                
            }
        } catch (InterruptedException ex) {
            // ignore
        };                    
    }    

    private JWindow labelsWindow;
    private LabelsPanel labelsPanel; 
    
    private void onLabelsClick() {
        labelsPanel = new LabelsPanel();
        labelsWindow = new JWindow();
        DefaultListModel model = new DefaultListModel();
        
        for (int i = 0; i < Annotator.LABELS.length; i++) {            
            model.addElement(Annotator.LABELS[i]);   
        }       
        labelsPanel.labelsList.setModel(model);        
                
        labelsWindow.add(labelsPanel);
        labelsWindow.pack();
        Point loc = panel.labelsButton.getLocationOnScreen();        
        labelsWindow.setLocation(new Point((int)loc.getX(), (int) (loc.getY() + panel.labelsButton.getHeight())));
        labelsPanel.labelsList.addListSelectionListener(this);
        labelsWindow.setVisible(true);                        
        
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            onClick(evt);
        }              
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        Window w = SwingUtilities.windowForComponent(component);
        if (w != labelsWindow) shutdown();
    }

    private void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if(labelsWindow!=null) {
            labelsWindow.dispose();
            labelsWindow = null;
        }        
    }
    
    private Set<String> getUrlsToRemove() {
        if(urlsToRemove==null) {
            urlsToRemove = new HashSet<String>();
        }
        return urlsToRemove;
    }     
    
    public void valueChanged(ListSelectionEvent evt) {
        int idx = evt.getFirstIndex();
        String selection = (String) labelsPanel.labelsList.getModel().getElementAt(idx);
        
        shutdown(); 
        
        selection = "{" + selection + "}";
        
        String annotation = panel.annotationTextField.getText();
        int pos = panel.annotationTextField.getCaretPosition();
        if(pos < 0) pos = annotation.length();
        
        StringBuffer sb = new StringBuffer(annotation.length() + selection.length());
        sb.append(annotation.substring(0, pos));
        sb.append(selection);
        if(pos < annotation.length()) {
            sb.append(annotation.substring(pos, annotation.length()));
        }
        panel.annotationTextField.setText(sb.toString());
        panel.annotationTextField.requestFocus();
        panel.annotationTextField.setCaretPosition(pos + selection.length());
        
    }
    
}
