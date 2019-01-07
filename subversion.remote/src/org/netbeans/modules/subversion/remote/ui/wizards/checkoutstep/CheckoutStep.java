/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.subversion.remote.ui.wizards.checkoutstep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.config.SvnConfigFiles;
import org.netbeans.modules.subversion.remote.ui.browser.Browser;
import org.netbeans.modules.subversion.remote.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.remote.ui.search.SvnSearch;
import org.netbeans.modules.subversion.remote.ui.wizards.AbstractStep;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * 
 */
public class CheckoutStep extends AbstractStep implements ActionListener, DocumentListener, FocusListener, ItemListener {

    public static final String CHECKOUT_DIRECTORY = "checkoutStep.checkoutDirectory"; //NOI18N
    
    private CheckoutPanel workdirPanel;
    private RepositoryPaths repositoryPaths;
    private boolean invalidTarget;
    private final FileSystem fileSystem;

    public CheckoutStep(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    @Override
    public HelpCtx getHelp() {    
        return new HelpCtx(CheckoutStep.class);
    }    

    @Override
    protected JComponent createComponent() {
        if (workdirPanel == null) {
            workdirPanel = new CheckoutPanel(fileSystem);
            workdirPanel.browseWorkdirButton.addActionListener(this);
            workdirPanel.scanForProjectsCheckBox.addItemListener(this);
            workdirPanel.atWorkingDirLevelCheckBox.addItemListener(this);
                    
            workdirPanel.workdirTextField.setText(defaultWorkingDirectory().getPath().trim());            
            workdirPanel.workdirTextField.getDocument().addDocumentListener(this);                
            workdirPanel.workdirTextField.addFocusListener(this);
            workdirPanel.repositoryPathTextField.getDocument().addDocumentListener(this);        
            workdirPanel.repositoryPathTextField.addFocusListener(this);
            workdirPanel.revisionTextField.getDocument().addDocumentListener(this);
            workdirPanel.revisionTextField.addFocusListener(this);                        
        }          
        validateUserInput(true);                                
        return workdirPanel;              
    }

    public void setup(RepositoryFile repositoryFile) {
        if(repositoryPaths == null) {                    
            repositoryPaths = 
                new RepositoryPaths(
                        fileSystem, repositoryFile, 
                        workdirPanel.repositoryPathTextField, 
                        workdirPanel.browseRepositoryButton, 
                        workdirPanel.revisionTextField, 
                        workdirPanel.searchRevisionButton,
                        workdirPanel.browseRevisionButton
                );        
            String browserPurposeMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "LBL_BrowserMessage");
            int browserMode = Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;
            repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_CHECKOUT, SvnSearch.SEACRH_HELP_ID_CHECKOUT);
        } else {
            repositoryPaths.setRepositoryFile(repositoryFile);
        }                
        workdirPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        refreshWorkingCopy(new RepositoryFile[] {repositoryFile});
        if(!repositoryFile.getRevision().equals(SVNRevision.HEAD)) {
            workdirPanel.revisionTextField.setText(repositoryFile.getRevision().toString());
        } else {
            workdirPanel.revisionTextField.setText(SVNRevision.HEAD.toString());
        }
        workdirPanel.revisionTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify (JComponent input) {
                if (workdirPanel.revisionTextField.getText().trim().isEmpty()) {
                    workdirPanel.revisionTextField.setText(SVNRevision.HEAD.toString());
                }
                return true;
            }
        });
        workdirPanel.scanForProjectsCheckBox.setSelected(SvnModuleConfig.getDefault(fileSystem).getShowCheckoutCompleted());
    }    
     
    @Override
    protected void validateBeforeNext() {
        if (validateUserInput(true)) {
            String text = getWorkdirText();
            VCSFileProxy file = VCSFileProxySupport.getResource(fileSystem, text);
            if (!file.exists()) {
                boolean done = VCSFileProxySupport.mkdirs(file);
                if (done == false) {
                    invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2013") + file.getPath(), false));// NOI18N
                    invalidTarget = true;
                }
            }
        }
    }

    private String getWorkdirText () {
        return workdirPanel.workdirTextField.getText().trim();
    }

    private boolean validateUserInput(boolean full) {                
        invalidTarget = false;
        if(repositoryPaths != null) {
            try {           
                repositoryPaths.getRepositoryFiles();
                if (repositoryPaths.getRevision() instanceof SVNRevision.Number && ((SVNRevision.Number) repositoryPaths.getRevision()).getNumber() < 0) {
                    invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2018"), false)); //NOI18N
                    return false;
                }
            } catch (NumberFormatException ex) {
                invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2018"), false));// NOI18N
                return false;
            } catch (MalformedURLException ex) {
                invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2015"), true));// NOI18N
                return false;
            }
        }
        
        String text = getWorkdirText();
        if (text.isEmpty()) {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2014"), true));// NOI18N
            return false;
        }                
        
        AbstractStep.WizardMessage errorMessage = null;
        if (full) {
            VCSFileProxy file = VCSFileProxySupport.getResource(fileSystem, text);
            if (!file.exists()) {
                // it's automaticaly create later on, check for permisions here
                VCSFileProxy parent = file.getParentFile();
                while (parent != null) {
                    if (parent.exists()) {
                        if (!parent.canWrite()) {
                            errorMessage = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2016") + parent.getPath(), false);// NOI18N
                        }
                        break;
                    }
                    parent = parent.getParentFile();
                }
            } else {
                if (file.isFile()) {
                    errorMessage = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2017"), false);// NOI18N
                }
            }
        }

        if (errorMessage == null) {
            valid();
        } else {
            invalid(errorMessage);
        }

        return errorMessage == null;
    }
    
    private void onBrowseWorkdir() {
        VCSFileProxy defaultDir = defaultWorkingDirectory();
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(defaultDir);
        fileChooser.setDialogTitle(NbBundle.getMessage(CheckoutStep.class, "BK0010"));// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(CheckoutStep.class, "BK0008");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(workdirPanel, NbBundle.getMessage(CheckoutStep.class, "BK0009"));// NOI18N
        VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        if (f != null) {
            workdirPanel.workdirTextField.setText(f.getPath().trim());
        }                
    }    
    
    /**
     * Returns file to be initaly used.
     * <ul>
     * <li>first is takes text in workTextField
     * <li>then recent project folder
     * <li>then recent checkout folder
     * <li>finally <tt>user.home</tt>
     * <ul>
     */
    private VCSFileProxy defaultWorkingDirectory() {
        VCSFileProxy defaultDir = null;
        String current = getWorkdirText();
        if (!current.trim().isEmpty()) {
            VCSFileProxy currentFile = VCSFileProxySupport.getResource(fileSystem, current);
            while (currentFile != null && !currentFile.exists()) {
                currentFile = currentFile.getParentFile();
            }
            if (currentFile != null) {
                if (currentFile.isFile()) {
                    defaultDir = currentFile.getParentFile();
                } else {
                    defaultDir = currentFile;
                }
            }
        }

        if (defaultDir == null) {
            String coDir = SvnModuleConfig.getDefault(fileSystem).getPreferences().get(CHECKOUT_DIRECTORY, null);
            if(coDir != null) {
                VCSFileProxy currentFile = VCSFileProxySupport.getResource(fileSystem, coDir);
            }            
        }

        //TODO: last selected project?
        //if (defaultDir == null) {
        //    File projectFolder = ProjectChooser.getProjectsFolder();
        //    if (projectFolder.exists() && projectFolder.isDirectory()) {
        //        defaultDir = projectFolder;
        //    }
        //}

        if (defaultDir == null) {
            defaultDir = SvnConfigFiles.getUserConfigPath(fileSystem);
        }

        return defaultDir;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {        
        validateUserInput(false);
        repositoryFoldersChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateUserInput(false);
        repositoryFoldersChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {        
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (!invalidTarget) {
            // click on Finish triggers in a series of focus events which results in deletion of the invalid target message
            // so do not validate when Finish is clicked and the message is shown
            validateUserInput(true);
        }
        repositoryFoldersChanged();
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
        assert e.getSource() == workdirPanel.browseWorkdirButton;
        onBrowseWorkdir();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source == workdirPanel.scanForProjectsCheckBox) {
            SvnModuleConfig.getDefault(fileSystem).setShowCheckoutCompleted(workdirPanel.scanForProjectsCheckBox.isSelected());
        } else if (source == workdirPanel.atWorkingDirLevelCheckBox) {
            RepositoryFile[] repositoryFiles = null;
            if (getRepositoryPath().length() != 0) {
                try {
                    repositoryFiles = repositoryPaths.getRepositoryFiles();
                } catch (NumberFormatException ex) {
                    // ignore
                } catch (MalformedURLException ex) {
                    // ignore
                }
            }
            refreshWorkingCopy(repositoryFiles);
        }
    }
    
    public VCSFileProxy getWorkdir() {
        return VCSFileProxySupport.getResource(fileSystem, getWorkdirText());
    }        

    public RepositoryFile[] getRepositoryFiles() {
        try {            
            return repositoryPaths.getRepositoryFiles("."); //NOI18N
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        }
        return null;
    }

    public boolean isAtWorkingDirLevel() {
        return workdirPanel.atWorkingDirLevelCheckBox.isSelected();
    }

    public boolean isExport() {
        return workdirPanel.exportCheckBox.isSelected();
    }

    public boolean isOldFormatPreferred () {
        return workdirPanel.preferOldFormatCheckBox.isSelected();
    }

    private void repositoryFoldersChanged() {
        if (getRepositoryPath().equals("")) {
            resetWorkingDirLevelCheckBox();
            refreshWorkingCopy(null);
            return;
        }        
        
        RepositoryFile[] repositoryFiles = null;
        try {
            repositoryFiles = repositoryPaths.getRepositoryFiles();
        } catch (NumberFormatException ex) {
            // ignore
        } catch (MalformedURLException ex) {
            // ignore
        }

        if ((repositoryFiles == null) || (repositoryFiles.length == 0) ||
           repositoryFiles.length >  1) 
        { 
            resetWorkingDirLevelCheckBox();
            refreshWorkingCopy(repositoryFiles);
            return;
        }        
        
        String repositoryFolder = repositoryFiles[0].getFileUrl().getLastPathSegment().trim();                           
        if(repositoryFolder.equals("")  ||      // the skip option doesn't make sense if there is no one, 
           repositoryFolder.equals(".")) //NOI18N // or more as one folder to be checked out  
        {
            resetWorkingDirLevelCheckBox();
            refreshWorkingCopy(repositoryFiles);
            return;
        } else {                        
            workdirPanel.atWorkingDirLevelCheckBox.setText (
                    NbBundle.getMessage(CheckoutStep.class, 
                                        "CTL_Checkout_CheckoutContentFolder",  //NOI18N
                                         new Object[] {repositoryFolder})
            );
            workdirPanel.atWorkingDirLevelCheckBox.setEnabled(true);                
            refreshWorkingCopy(repositoryFiles);
        }
    }

    private void resetWorkingDirLevelCheckBox() {
        workdirPanel.atWorkingDirLevelCheckBox.setText(NbBundle.getMessage(CheckoutStep.class, "CTL_Checkout_CheckoutContentEmpty"));
        workdirPanel.atWorkingDirLevelCheckBox.setEnabled(false);
    }

    private String getRepositoryPath() {
        return workdirPanel.repositoryPathTextField.getText().trim();
    }

    private void refreshWorkingCopy(RepositoryFile[] repositoryFiles) {
        String localFolderPath = trimTrailingSlashes(getWorkdirText());
        int filesCount = (repositoryFiles != null) ? repositoryFiles.length : 0;

        String workingCopyPath;
        if (filesCount == 0) {
            workingCopyPath = localFolderPath;
        } else {
            String repositoryFilePath = trimSlashes(repositoryFiles[0].getPath());
            if (repositoryFilePath.equals(".")) {                       //NOI18N
                repositoryFilePath = "";                                //NOI18N
            }
            if ((filesCount == 1)
                && (workdirPanel.atWorkingDirLevelCheckBox.isSelected()
                    || (repositoryFilePath.length() == 0))) {
                workingCopyPath = localFolderPath;
            } else {
                String repositoryFolderName = repositoryFiles[0].getName();
                StringBuilder buf = new StringBuilder(localFolderPath.length()
                                                      + repositoryFolderName.length()
                                                      + 10);
                buf.append(localFolderPath).append('/').append(repositoryFolderName);
                if (filesCount > 1) {
                    buf.append(", ...");                                //NOI18N
                }
                workingCopyPath = buf.toString();
            }
        }
        workdirPanel.workingCopy.setText(workingCopyPath);
    }

    private static String trimTrailingSlashes(String path) {
        return trimSlashes(path, true);
    }

    private static String trimSlashes(String path) {
        return trimSlashes(path, false);
    }

    private static String trimSlashes(String path, boolean trailingOnly) {
        final int length = path.length();
        if (length == 0) {
            return path;
        }

        int startIndex = 0;
        if (!trailingOnly) {
            while ((startIndex < length) && (path.charAt(startIndex) == '/')) {
                startIndex++;
            }
            if (startIndex == length) {
                return "";                                              //NOI18N
            }
        }

        int endIndex = length;
        while ((endIndex != 0) && (path.charAt(endIndex - 1) == '/')) {
            endIndex--;
        }
        if (endIndex == 0) {
            return "";                                                  //NOI18N
        }

        return (startIndex == 0) && (endIndex == length)
               ? path
               : path.substring(startIndex, endIndex);
    }

}

