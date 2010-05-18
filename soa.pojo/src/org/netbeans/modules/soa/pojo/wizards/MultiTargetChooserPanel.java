/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class MultiTargetChooserPanel implements org.openide.WizardDescriptor.Panel, ChangeListener
{
    public static final String TARGET_NAME = "MultiTargetChooserPanel.TARGET_NAME"; //NOI18N
    public static final String MULTI_TARGET_GUI_INSTANCE = "MULTI_TARGET_GUI_INSTANCE"; //NOI18N
    public static final String TARGET_FOLDER = "MultiTargetChooserPanel.TARGET_FOLDER"; //NOI18N
    private static final String J2SE_PROJECT = "org.netbeans.modules.java.j2seproject.J2SEProject"; //NOI18N

    static final int TYPE_FILE = 0;
    static final int TYPE_PACKAGE = 1;
    static final int TYPE_PKG_INFO = 2;
    static final int TYPE_PKG_NT_EDITABLE = 3;
    private static final String FOLDER_TO_DELETE = "folderToDelete";
    
    private final SpecificationVersion JDK_14;
    private final List listeners;
    private MultiTargetChooserPanelGUI gui;
    private org.openide.WizardDescriptor.Panel bottomPanel;
    private WizardDescriptor wizard;
    private Project project;
    private SourceGroup folders[];
    private int type;
    private boolean isValidPackageRequired;
    //static final boolean $assertionsDisabled = !org/netbeans/modules/soa/pojo/wizards/MultiTargetChooserPanel.desiredAssertionStatus();

    public MultiTargetChooserPanel(Project project, SourceGroup folders[], org.openide.WizardDescriptor.Panel bottomPanel)
    {
        this(project, folders, bottomPanel, false);
    }

    public MultiTargetChooserPanel(Project project, SourceGroup folders[], org.openide.WizardDescriptor.Panel bottomPanel, boolean isValidPackageRequired)
    {
        JDK_14 = new SpecificationVersion("1.4");
        listeners = new ArrayList();
        if ( project != null) {
            this.project = project;
            this.folders = folders;
            if(0 == folders.length)
                this.folders = ProjectUtils.getSources(project).getSourceGroups("java");
        }
        this.bottomPanel = bottomPanel;
        type = 0;
        if(bottomPanel != null)
            bottomPanel.addChangeListener(this);
        this.isValidPackageRequired = isValidPackageRequired;
    }

    public java.awt.Component getComponent()
    {
        if(gui == null)
        {
            gui = new MultiTargetChooserPanelGUI(project, folders, bottomPanel != null ? bottomPanel.getComponent() : null, type);
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp()
    {
        if(bottomPanel != null)
        {
            HelpCtx bottomHelp = bottomPanel.getHelp();
            if(bottomHelp != null)
                return bottomHelp;
        }
        return null;
    }

    public boolean isValid()
    {
        if (!isValidProject(this.project)){
              setErrorMessage( "ERR_Invalid_ProjectType" );
              return false;
        }

        if ( !isValidPackageName( gui.getPackageName() ) ) {
              setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
              return false;
        }        
        if ( !isValidTypeIdentifier( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidClass" );
                return false;
        }
        if (  gui.getPackageName().length() == 0) {
             setErrorMessage( "ERR_JavaTargetChooser_DefaultPackage" );     
        } else {
            setErrorMessage(null);
        }
        
        return  bottomPanel.isValid();
    }

    public void addChangeListener(ChangeListener l)
    {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l)
    {
        listeners.remove(l);
    }

    private void fireChange()
    {
        ChangeEvent e = new ChangeEvent(this);
        for(Iterator it = listeners.iterator(); it.hasNext(); ((ChangeListener)it.next()).stateChanged(e));
    }

    public void readSettings(Object settings)
    {
        wizard = (WizardDescriptor)settings;
        if(gui != null)
        {
            FileObject preselectedFolder = Templates.getTargetFolder(wizard);
            FileObject templateFolder = Templates.getTemplate(wizard);
            templateFolder = (templateFolder != null)?templateFolder:preselectedFolder;
                    
            gui.initValues(templateFolder, preselectedFolder);
            
        }
        if(bottomPanel != null) {
            if ( Templates.getTargetFolder(wizard) == null) {
            Templates.setTargetFolder((WizardDescriptor)settings, getTargetFolderFromGUI((WizardDescriptor)settings));
            Templates.setTargetName((WizardDescriptor)settings, gui.getTargetName());
            }
            wizard.putProperty(MULTI_TARGET_GUI_INSTANCE, gui);

            bottomPanel.readSettings(settings);
        }
        if(gui != null)
        {
            Object substitute = gui.getClientProperty("NewFileWizard_Title");
            if(substitute != null)
                wizard.putProperty("NewFileWizard_Title", substitute);
        }
    }

    public void storeSettings(Object settings)
    {
        Object value = ((WizardDescriptor)settings).getValue();
        if(WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value) || WizardDescriptor.CLOSED_OPTION.equals(value))
            return;
        if(isValid())
        {
            if ( Templates.getTargetFolder(wizard) == null) {
                Templates.setTargetFolder((WizardDescriptor)settings, getTargetFolderFromGUI((WizardDescriptor)settings));
                Templates.setTargetName((WizardDescriptor)settings, gui.getTargetName());
            }
            ((WizardDescriptor)settings).putProperty(GeneratorUtil.POJO_DEST_FOLDER, getTargetFolderFromGUI((WizardDescriptor)settings) );
           ((WizardDescriptor)settings).putProperty(GeneratorUtil.POJO_DEST_NAME,  gui.getTargetName() );
            if(bottomPanel != null)
                bottomPanel.storeSettings(settings);
           
        }
        ((WizardDescriptor)settings).putProperty(GeneratorUtil.POJO_DEST_FOLDER, getTargetFolderFromGUI((WizardDescriptor)settings) );
       ((WizardDescriptor)settings).putProperty(GeneratorUtil.POJO_DEST_NAME,  gui.getTargetName() );
        ((WizardDescriptor)settings).putProperty("NewFileWizard_Title", null);
        if(WizardDescriptor.FINISH_OPTION.equals(value))
            wizard.putProperty("folderToDelete", null);
    }

    public void stateChanged(ChangeEvent e)
    {
        fireChange();
    }

    private void setErrorMessage(String key)
    {
        if(key == null)
            setLocalizedErrorMessage("");
        else
            setLocalizedErrorMessage(NbBundle.getMessage(MultiTargetChooserPanelGUI.class, key));
    }

    private void setLocalizedErrorMessage(String message)
    {
        wizard.putProperty("WizardPanel_errorMessage", message);//NOI18N
    }

    private FileObject getTargetFolderFromGUI(WizardDescriptor wd)
    {
        assert gui != null;

        if (gui == null) {
            return null;
        }
        FileObject rootFolder = gui.getRootFolder();

        if (rootFolder == null) {
            return null;
        }
        FileObject folder = null;

        if(type != 1)
        {
            String packageFileName = gui.getPackageFileName();
            folder = rootFolder.getFileObject(packageFileName);

            if(folder == null)
                try
                {
                    folder = rootFolder;
                    StringTokenizer tk = new StringTokenizer(packageFileName, "/");
                    String name = null;
                    do
                    {
                        if(!tk.hasMoreTokens())
                            break;
                        name = tk.nextToken();
                        FileObject fo = folder.getFileObject(name, "");
                        if(fo == null)
                            break;
                        folder = fo;
                    } while(true);
                    folder = folder.createFolder(name);
                    FileObject toDelete = (FileObject)wd.getProperty("folderToDelete");
                    if(toDelete == null)
                        wd.putProperty("folderToDelete", folder);
                    else
                    if(!toDelete.equals(folder))
                    {
                        toDelete.delete();
                        wd.putProperty("folderToDelete", folder);
                    }
                    while(tk.hasMoreTokens()) 
                    {
                        name = tk.nextToken();
                        folder = folder.createFolder(name);
                    }
                }
                catch(IOException e)
                {
                    ErrorManager.getDefault().notify(1, e);
                }
        } else
        {
            folder = rootFolder;
        }
        return folder;
    }

    static boolean isValidPackageName(String str)
    {
        if(str.length() > 0 && str.charAt(0) == '.')
            return false;
        for(StringTokenizer tukac = new StringTokenizer(str, "."); tukac.hasMoreTokens();)
        {
            String token = tukac.nextToken();
            if("".equals(token))
                return false;
            if(!Utilities.isJavaIdentifier(token))
                return false;
        }

        return true;
    }

    static boolean isValidTypeIdentifier(String ident)
    {
        return ident != null && !"".equals(ident) && Utilities.isJavaIdentifier(ident);
    }

    public static final String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension)
    {
        String newObjectNameToDisplay = newObjectName;
        if(newObjectName != null)
            newObjectName = newObjectName.replace('.', '/');
        if(extension != null && extension.length() > 0)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(newObjectName);
            sb.append('.');
            sb.append(extension);
            newObjectName = sb.toString();
        }
        if(extension != null && extension.length() > 0)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(newObjectNameToDisplay);
            sb.append('.');
            sb.append(extension);
            newObjectNameToDisplay = sb.toString();
        }
        String relFileName = (new StringBuilder()).append(folderName).append("/").append(newObjectName).toString();
        if(targetFolder == null)
            return NbBundle.getMessage(MultiTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist");
        if(!targetFolder.canWrite())
            return NbBundle.getMessage(MultiTargetChooserPanel.class, "MSG_fs_is_readonly");
        if(existFileName(targetFolder, relFileName))
            return NbBundle.getMessage(MultiTargetChooserPanel.class, "MSG_file_already_exist", newObjectNameToDisplay);
        else
            return null;
    }

    static boolean isValidProject(Project prj){
        if (prj != null){
            String prjType = prj.getClass().getName();
            if (J2SE_PROJECT.equals(prjType)){
                return true;
            }
        }
        return false;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName)
    {
        boolean result = false;
        File fileForTargetFolder = FileUtil.toFile(targetFolder);
        if(fileForTargetFolder.exists())
            result = (new File(fileForTargetFolder, relFileName)).exists();
        else
            result = targetFolder.getFileObject(relFileName) != null;
        return result;
    }

}