/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author  Petr Hrebejk
 */
public final class JavaTargetChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private static final String FOLDER_TO_DELETE = "folderToDelete";    //NOI18N

    private final SpecificationVersion JDK_14 = new SpecificationVersion ("1.4");   //NOI18N
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private JavaTargetChooserPanelGUI gui;
    private WizardDescriptor.Panel<WizardDescriptor> bottomPanel;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    private int type;
    private boolean isValidPackageRequired;

    public JavaTargetChooserPanel( Project project, SourceGroup folders[], WizardDescriptor.Panel<WizardDescriptor> bottomPanel, int type, boolean isValidPackageRequired ) {
        this.project = project;
        this.folders = folders;
        this.bottomPanel = bottomPanel;
        this.type = type;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
        this.isValidPackageRequired = isValidPackageRequired;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new JavaTargetChooserPanelGUI( project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), type );
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        if ( bottomPanel != null ) {
            HelpCtx bottomHelp = bottomPanel.getHelp();
            if ( bottomHelp != null ) {
                return bottomHelp;
            }
        }
        return new HelpCtx(JavaTargetChooserPanel.class);
    }

    public boolean isValid() {              
        if (gui == null) {
           setErrorMessage( null );
           return false;
        }        
        if ( type == NewJavaFileWizardIterator.TYPE_PACKAGE) {
            if (gui.getTargetName() == null) {
                setInfoMessage("INFO_JavaTargetChooser_ProvidePackageName");
                return false;
            }
            if ( !isValidPackageName( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getTargetName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        }
        else if (type == NewJavaFileWizardIterator.TYPE_PKG_INFO) {
            //Change in firing order caused that isValid is called before readSettings completed => no targetName available
            if (gui.getTargetName() == null) {
                setInfoMessage("INFO_JavaTargetChooser_ProvideClassName");
                return false;
            }
            assert "package-info".equals( gui.getTargetName() );        //NOI18N
            if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        }
        else {
            if (gui.getTargetName() == null) {
                setInfoMessage("INFO_JavaTargetChooser_ProvideClassName");
                return false;
            } 
            else if ( !isValidTypeIdentifier( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidClass" );
                return false;
            }
            else if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        }
        
        // check if the file name can be created
        FileObject template = Templates.getTemplate( wizard );

        boolean returnValue=true;
        FileObject rootFolder = gui.getRootFolder();
        SpecificationVersion specVersion = null;
        if (type != NewJavaFileWizardIterator.TYPE_PACKAGE) {
            String sl = SourceLevelQuery.getSourceLevel(rootFolder);
            specVersion = sl != null? new SpecificationVersion(sl): null;
        }
        String errorMessage = canUseFileName (rootFolder, gui.getPackageFileName(), gui.getTargetName(), template.getExt ());        
        if (gui != null) {
            wizard.getNotificationLineSupport ().setErrorMessage (errorMessage);
        }
        if (errorMessage!=null) returnValue=false;                
        
        if (type != NewJavaFileWizardIterator.TYPE_PACKAGE && returnValue && gui.getPackageName().length() == 0 && specVersion != null && JDK_14.compareTo(specVersion)<=0) { 
            if(isValidPackageRequired){
                setInfoMessage( "ERR_JavaTargetChooser_CantUseDefaultPackage" );
                return false;
            }
            //Only warning, display it only if everything else is OK.
            setErrorMessage( "ERR_JavaTargetChooser_DefaultPackage" );            
        }
        String templateSrcLev = (String) template.getAttribute("javac.source"); // NOI18N
        //Only warning, display it only if everything else id OK.
        if (specVersion != null && templateSrcLev != null && specVersion.compareTo(new SpecificationVersion(templateSrcLev)) < 0) {
            setErrorMessage("ERR_JavaTargetChooser_WrongPlatform"); // NOI18N
        }
        
        // this enables to display error messages from the bottom panel
        // Nevertheless, the previous error messages have bigger priorities 
        if (returnValue && bottomPanel != null) {
           if (!bottomPanel.isValid())
               return false;
        }
        
        return returnValue;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings(WizardDescriptor wizard) {
        this.wizard = wizard;
        if ( gui != null ) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder( wizard );            
            // Init values
            gui.initValues( Templates.getTemplate( wizard ), preselectedFolder );
        }
        
        if ( bottomPanel != null ) {
            bottomPanel.readSettings(wizard);
        }        
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        if (gui != null) {
            Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
            }
        }
    }

    public void storeSettings(WizardDescriptor wizard) { 
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value) ||
                WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( wizard );
            }
            Templates.setTargetFolder(wizard, getTargetFolderFromGUI(wizard));
            Templates.setTargetName(wizard, gui.getTargetName());
        }
        wizard.putProperty("NewFileWizard_Title", null); // NOI18N
        
        if (WizardDescriptor.FINISH_OPTION.equals(value)) {
            wizard.putProperty(FOLDER_TO_DELETE, null);
        }
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    // Private methods ---------------------------------------------------------
    
    private void setErrorMessage( String key ) {
        if ( key == null ) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(JavaTargetChooserPanelGUI.class, key));
        }
    }
    
    private void setInfoMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setInformationMessage (NbBundle.getMessage(JavaTargetChooserPanelGUI.class, key));
        }
    }
    
    private FileObject getTargetFolderFromGUI (WizardDescriptor wd) {
        assert gui != null;
        FileObject rootFolder = gui.getRootFolder();
        FileObject folder = null;
        if ( type != NewJavaFileWizardIterator.TYPE_PACKAGE ) {
            String packageFileName = gui.getPackageFileName();
            folder = rootFolder.getFileObject( packageFileName );
            if ( folder == null ) {
                try {
                    folder = rootFolder;
                    StringTokenizer tk = new StringTokenizer (packageFileName,"/"); //NOI18N
                    String name = null;
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        FileObject fo = folder.getFileObject (name,"");   //NOI18N
                        if (fo == null) {
                            break;
                        }
                        folder = fo;
                    }
                    folder = folder.createFolder(name);
                    FileObject toDelete = (FileObject) wd.getProperty(FOLDER_TO_DELETE);
                    if (toDelete == null) {
                        wd.putProperty(FOLDER_TO_DELETE,folder);
                    }
                    else if (!toDelete.equals(folder)) {
                        toDelete.delete();
                        wd.putProperty(FOLDER_TO_DELETE,folder);
                    }
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        folder = folder.createFolder(name);
                    }

                }
                catch( IOException e ) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        else {
            folder = rootFolder;
        }
        return folder;
    }
    
    // Nice copy of useful methods (Taken from JavaModule)
    
    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    private static boolean isValidPackage (FileObject root, final String path) {
        //May be null when nothing selected in the GUI.
        if (root == null) {
            return false;
        }
        if (path == null) {
            return false;
        }
        final StringTokenizer tk = new StringTokenizer(path,".");   //NOI18N
        while (tk.hasMoreTokens()) {
            root = root.getFileObject(tk.nextToken());
            if (root == null) {
                return true;
            }
            else if (root.isData()) {
                return false;
            }
        }
        return true;
    }
    
    static boolean isValidTypeIdentifier(String ident) {
        if (ident == null || "".equals(ident) || !Utilities.isJavaIdentifier( ident ) ) {
            return false;
        }
        else {
            return true;
        }
    }
    
    // helper methods copied from project/ui/ProjectUtilities
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    final public static String canUseFileName (FileObject targetFolder, String folderName, String newObjectName, String extension) {
        String newObjectNameToDisplay = newObjectName;
        if (newObjectName != null) {
            newObjectName = newObjectName.replace ('.', '/'); // NOI18N
        }
        if (extension != null && extension.length () > 0) {
            StringBuffer sb = new StringBuffer ();
            sb.append (newObjectName);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        
        if (extension != null && extension.length () > 0) {
            StringBuffer sb = new StringBuffer ();
            sb.append (newObjectNameToDisplay);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectNameToDisplay = sb.toString ();
        }
        
        String relFileName = folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target package should be writable
        File targetPackage = folderName != null ? new File (FileUtil.toFile (targetFolder), folderName) : FileUtil.toFile (targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists () && ! targetPackage.canWrite ()) {
                return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (! targetFolder.canWrite ()) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }
        
        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        boolean result = false;
        File fileForTargetFolder = FileUtil.toFile(targetFolder);
        if (fileForTargetFolder.exists()) {
            result = new File (fileForTargetFolder, relFileName).exists();
        } else {
            result = targetFolder.getFileObject (relFileName) != null;
        }
        
        return result;
    }
}
