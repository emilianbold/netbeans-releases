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

package org.netbeans.modules.ruby.rubyproject.templates;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
//import org.netbeans.api.gsfpath.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
//import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Petr Hrebejk
 */
public final class RubyTargetChooserPanel implements WizardDescriptor.Panel, ChangeListener {    

    private static final String FOLDER_TO_DELETE = "folderToDelete";    //NOI18N

    //private final SpecificationVersion JDK_14 = new SpecificationVersion ("1.4");   //NOI18N
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private RubyTargetChooserPanelGUI gui;
    private WizardDescriptor.Panel bottomPanel;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    private int type;
    private boolean isValidPackageRequired;
    
    public RubyTargetChooserPanel( Project project, SourceGroup folders[], WizardDescriptor.Panel bottomPanel, int type ) {
        this(project, folders, bottomPanel, type, false);
    }
    
    public RubyTargetChooserPanel( Project project, SourceGroup folders[], WizardDescriptor.Panel bottomPanel, int type, boolean isValidPackageRequired ) {
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
            gui = new RubyTargetChooserPanelGUI( project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), type );
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
        
        //XXX
        return null;
        
    }

    public boolean isValid() {              
        if (gui == null || gui.getTargetName() == null) {
           setErrorMessage( null );
           return false;
        }        
        setLocalizedErrorMessage(null);
        
        if (type == NewRubyFileWizardIterator.TYPE_CLASS || type == NewRubyFileWizardIterator.TYPE_MODULE ||
            type == NewRubyFileWizardIterator.TYPE_TEST) {
            if (type == NewRubyFileWizardIterator.TYPE_CLASS || type == NewRubyFileWizardIterator.TYPE_TEST) {
                if (gui.getClassName() == null || !RubyUtils.isValidRubyClassName(gui.getClassName())) {
                    setErrorMessage("ERR_RubyTargetChooser_InvalidClass"); // NOI18N
                    return false;
                }
                String msg = RubyUtils.getIdentifierWarning(gui.getClassName(), 0);
                if (msg != null) {
                    setLocalizedErrorMessage(msg); // warning only, don't return false
                }
                String superclass = gui.getExtends();
                if (superclass != null && superclass.length() > 0) {
                    String[] mods = superclass.split("::"); // NOI18N
                    for (String mod : mods) {
                        if (!RubyUtils.isValidRubyClassName(mod)) {
                            setErrorMessage("ERR_RubyTargetChooser_InvalidSuperclass"); // NOI18N
                            return false;
                        }
                        msg = RubyUtils.getIdentifierWarning(mod, 0);
                        if (msg != null) {
                            setLocalizedErrorMessage(msg); // warning only, don't return false
                        }
                    }
                }
            }
            if (type == NewRubyFileWizardIterator.TYPE_MODULE) {
                if (gui.getClassName() == null || !RubyUtils.isValidRubyClassName(gui.getClassName())) {
                    setErrorMessage("ERR_RubyTargetChooser_InvalidModule"); // NOI18N
                    return false;
                }
                String msg = RubyUtils.getIdentifierWarning(gui.getClassName(), 0);
                if (msg != null) {
                    setLocalizedErrorMessage(msg); // warning only, don't return false
                }
            }
            String in = gui.getModuleName();
            if (in != null && in.length() > 0) {
                String[] mods = in.split("::"); // NOI18N
                for (String mod : mods) {
                    if (!RubyUtils.isValidRubyClassName(mod)) {
                        setErrorMessage("ERR_RubyTargetChooser_InvalidInModule"); // NOI18N
                        return false;
                    }
                    String msg = RubyUtils.getIdentifierWarning(mod, 0);
                    if (msg != null) {
                        setLocalizedErrorMessage(msg); // warning only, don't return false
                    }
                }
            }
        }
        
        if ( type == NewRubyFileWizardIterator.TYPE_PACKAGE) {
            if ( !isValidPackageName( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_RubyTargetChooser_InvalidPackage" ); // NOI18N
                return false;
            }
        }
        else if (type == NewRubyFileWizardIterator.TYPE_PKG_INFO) {
            assert "package-info".equals( gui.getTargetName() );        //NOI18N
            if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_RubyTargetChooser_InvalidPackage" ); // NOI18N
                return false;
            }
        }
        else {
//            if ( !isValidTypeIdentifier( gui.getTargetName() ) ) {
//                setErrorMessage( "ERR_RubyTargetChooser_InvalidClass" );
//                return false;
//            }
//            else if ( !isValidPackageName( gui.getPackageName() ) ) {
//                setErrorMessage( "ERR_RubyTargetChooser_InvalidPackage" );
//                return false;
//            }            
            if (!isValidFileName(gui.getTargetName())) {
                setErrorMessage( "ERR_RubyTargetChooser_InvalidFilename" ); // NOI18N
                return false;
            }
        }
        
        // check if the file name can be created
        FileObject template = Templates.getTemplate( wizard );

        boolean returnValue=true;
        FileObject rootFolder = gui.getRootFolder();
        //SpecificationVersion specVersion = null;
        //if (type != NewRubyFileWizardIterator.TYPE_PACKAGE) {
        //    String sl = SourceLevelQuery.getSourceLevel(rootFolder);
        //    specVersion = sl != null? new SpecificationVersion(sl): null;
        //}
        String errorMessage = canUseFileName (rootFolder, gui.getPackageFileName(), gui.getTargetName(), template.getExt ());        
        if (gui != null && errorMessage != null) {
            setLocalizedErrorMessage (errorMessage);
        }
        if (errorMessage!=null) {
            returnValue = false;
        }                
        
//        if (type != NewRubyFileWizardIterator.TYPE_PACKAGE && returnValue && gui.getPackageName().length() == 0 && specVersion != null && JDK_14.compareTo(specVersion)<=0) { 
//            if(isValidPackageRequired){
//                setErrorMessage( "ERR_RubyTargetChooser_CantUseDefaultPackage" );
//                return false;
//            }
//            //Only warning, display it only if everything else is OK.
//            setErrorMessage( "ERR_RubyTargetChooser_DefaultPackage" );            
//        }
//        String templateSrcLev = (String) template.getAttribute("javac.source"); // NOI18N
        //Only warning, display it only if everything else id OK.
//        if (specVersion != null && templateSrcLev != null && specVersion.compareTo(new SpecificationVersion(templateSrcLev)) < 0) {
//            setErrorMessage("ERR_RubyTargetChooser_WrongPlatform"); // NOI18N
//        }
        
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

    public void readSettings( Object settings ) {
        
        wizard = (WizardDescriptor)settings;
        
        if ( gui != null ) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder( wizard );            
            // Init values
            gui.initValues( Templates.getTemplate( wizard ), preselectedFolder );
        }
        
        if ( bottomPanel != null ) {
            bottomPanel.readSettings( settings );
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

    public void storeSettings(Object settings) { 
        Object value = ((WizardDescriptor)settings).getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value) ||
                WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( settings );
            }
            Templates.setTargetFolder( (WizardDescriptor)settings, getTargetFolderFromGUI ((WizardDescriptor)settings));
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
            
            if (type == NewRubyFileWizardIterator.TYPE_CLASS || 
                    type == NewRubyFileWizardIterator.TYPE_TEST) {
                wizard.putProperty("class", gui.getClassName()); // NOI18N
                wizard.putProperty("module", gui.getModuleName()); // NOI18N
                wizard.putProperty("extend", gui.getExtends()); // NOI18N
            } else if (type == NewRubyFileWizardIterator.TYPE_MODULE) {
                // NOTE - even when adding a -module-, we will use the "class" textfield
                // to represent the name of the module, and the "module" text field to represent
                // modules surrounding the current module
                wizard.putProperty("module", gui.getClassName()); // NOI18N
                wizard.putProperty("outermodules", gui.getModuleName()); // NOI18N
            }
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
        
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
            setLocalizedErrorMessage ( "" ); // NOI18N
        }
        else {
            setLocalizedErrorMessage ( NbBundle.getMessage( RubyTargetChooserPanelGUI.class, key) ); // NOI18N
        }
    }
    
    private void setLocalizedErrorMessage (String message) {
        wizard.putProperty ("WizardPanel_errorMessage", message); // NOI18N
    }
    
    private FileObject getTargetFolderFromGUI (WizardDescriptor wd) {
        assert gui != null;
        FileObject rootFolder = gui.getRootFolder();
        FileObject folder = null;
        if ( type != NewRubyFileWizardIterator.TYPE_PACKAGE ) {
            String packageFileName = gui.getPackageFileName();
            folder = rootFolder.getFileObject( packageFileName );
            if ( folder == null ) {
                try {
                    folder = rootFolder;
                    StringTokenizer tk = new StringTokenizer (packageFileName,"/"); //NOI18N
                    String name = null;
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        FileObject fo = folder.getFileObject (name,"");   //NOI8N
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
                    ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
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
        StringTokenizer tukac = new StringTokenizer(str, "."); // NOI18N
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
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

    static boolean isValidFileName(String ident) {
        if (ident == null || ident.length() == 0) {
            return false;
        }
        
        // TODO - do I want to filter filenames down to anything?
        
        return true;
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
            newObjectName = newObjectName.replace ('.', '/'); // NOI8N
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
            return NbBundle.getMessage (RubyTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target filesystem should be writable
        if (!targetFolder.canWrite ()) {
            return NbBundle.getMessage (RubyTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        
        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage (RubyTargetChooserPanel.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
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
