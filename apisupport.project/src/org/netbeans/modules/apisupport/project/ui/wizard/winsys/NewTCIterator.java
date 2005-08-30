/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.winsys;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard for creating new TopComponent.
 *
 * @author Milos Kleint
 */
public class NewTCIterator extends BasicWizardIterator {

    private static final long serialVersionUID = 1L;
    private NewTCIterator.DataModel data;
    
    public static NewTCIterator createIterator() {
        return new NewTCIterator();
    }
    
    public Set instantiate() throws IOException {
        assert data != null;
        CreatedModifiedFiles fileOperations = data.getCreatedModifiedFiles();
        if (fileOperations != null) {
            fileOperations.run();
        }
        String[] paths = fileOperations.getCreatedPaths();
        HashSet set = new HashSet();
        for (int i =0; i < paths.length; i++) {
            FileObject fo = data.getProject().getProjectDirectory().getFileObject(paths[i]);
            if (fo != null) {
                set.add(fo);
            }
        }
        return set;
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewTCIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new BasicSettingsPanel(wiz, data),
            new NameAndLocationPanel(wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private String name;
        private String icon;
        private String mode;
        private boolean opened = false;
        
        private CreatedModifiedFiles files;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return getFiles();
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.setFiles(files);
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public CreatedModifiedFiles getFiles() {
            return files;
        }
        
        public void setFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public boolean isOpened() {
            return opened;
        }

        public void setOpened(boolean opened) {
            this.opened = opened;
        }
        
    }
    
    public static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles fileChanges = new CreatedModifiedFiles(model.getProject());
        NbModuleProject project = model.getProject();
        final String name = model.getName();
        final String packageName = model.getPackageName();
        final String mode = model.getMode();
        
        HashMap replaceTokens = new HashMap();
        replaceTokens.put("@@TEMPLATENAME@@", name);//NOI18N
        replaceTokens.put("@@PACKAGENAME@@", packageName);//NOI18N
        replaceTokens.put("@@MODE@@", mode); //NOI18N
        replaceTokens.put("@@OPENED@@", model.isOpened() ? "true" : "false"); //NOI18N

        // 0. move icon file if necessary
        String icon = model.getIcon();
        File fil = null;
        if (icon != null) {
            fil = new File(icon);
            if (!fil.exists()) {
                fil = null;
            }
        }
        if (fil != null) {
            FileObject fo = FileUtil.toFileObject(fil);
            String relativeIconPath = null;
            if (!FileUtil.isParentOf(model.getProject().getSourceDirectory(), fo)) {
                String iconPath = getRelativePath(model.getProject(), packageName, 
                                                "", fo.getNameExt()); //NOI18N
                try {
                    fileChanges.add(fileChanges.createFile(iconPath, fo.getURL()));
                    relativeIconPath = packageName.replace('.', '/') + "/" + fo.getNameExt(); // NOI18N
                } catch (FileStateInvalidException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            } else {
                relativeIconPath = FileUtil.getRelativePath(model.getProject().getSourceDirectory(), fo);
            }
            replaceTokens.put("@@ICONPATH@@", relativeIconPath);//NOI18N
        } else {
            replaceTokens.put("@@ICONPATH@@", "SET/PATH/TO/ICON/HERE"); //NOI18N
        }
        
        
        // 2. update project dependencies
        ProjectXMLManager manager = new ProjectXMLManager(project.getHelper());
        replaceTokens.put("@@MODULENAME@@", project.getCodeNameBase()); // NOI18N
        //TODO how to figure the currect specification version for module?
        replaceTokens.put("@@SPECVERSION@@", project.getSpecVersion()); // NOI18N
        try {
            SortedSet set = manager.getDirectDependencies(project.getPlatform());
            if (set != null) {
                Iterator it = set.iterator();
                boolean windows = false;
                boolean util = false;
                while (it.hasNext()) {
                    ModuleDependency dep = (ModuleDependency)it.next();
                    if ("org.openide.windows".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        windows = true;
                    }
                    if ("org.openide.util".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        util = true;
                    }
                }
                if (!windows) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.windows", -1, null, true)); //NOI18N
                }
                if (!util) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.util", -1, null, true)); //NOI18N
                }
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        // x. generate java classes
        final String tcName = getRelativePath(project, packageName,
                name, "TopComponent.java"); //NOI18N
        // TODO use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        URL template = NewTCIterator.class.getResource("templateTopComponent.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(tcName, template, replaceTokens));
        final String actionName = getRelativePath(project, packageName,
                name, "Action.java"); //NOI18N
        // TODO use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        template = NewTCIterator.class.getResource("templateAction.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(actionName, template, replaceTokens));
        
        final String settingsName = name + "TopComponent.settings"; //NOI18N
        // TODO use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        template = NewTCIterator.class.getResource("templateSettings.xml");//NOI18N
        fileChanges.add(fileChanges.createLayerEntry("Windows2/Components/" + settingsName, template, replaceTokens, null, null)); // NOI18N
        
        final String wstcrefName = name + "TopComponent.wstcref"; //NOI18N
        // TODO use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        template = NewTCIterator.class.getResource("templateWstcref.xml");//NOI18N
        fileChanges.add(fileChanges.createLayerEntry("Windows2/Modes/" + mode + "/" + wstcrefName, // NOI18N
                             template, replaceTokens, null, null));
        
        fileChanges.add(fileChanges.layerModifications(new CreateActionEntryOperation(name + "Action", packageName), // NOI18N
                                                       Collections.EMPTY_SET));
        String bundlePath = getRelativePath(model.getProject(), packageName, "", "Bundle.properties"); //NOI18N
        fileChanges.add(fileChanges.bundleKey(bundlePath, "CTL_" + name + "Action",  // NOI18N
                                NbBundle.getMessage(NewTCIterator.class, "LBL_TemplateActionName", name))); //NOI18N
        
        fileChanges.add(fileChanges.bundleKey(bundlePath, "CTL_" + name + "TopComponent",  // NOI18N
                                NbBundle.getMessage(NewTCIterator.class, "LBL_TemplateTCName", name))); //NOI18N
        fileChanges.add(fileChanges.bundleKey(bundlePath, "HINT_" + name + "TopComponent",  // NOI18N
                                NbBundle.getMessage(NewTCIterator.class, "HINT_TemplateTCName", name))); //NOI18N
        
        model.setCreatedModifiedFiles(fileChanges);
    }
    
    private static String getRelativePath(NbModuleProject project, String fullyQualifiedPackageName,
            String prefix, String postfix) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(project.getSourceDirectoryPath()).append("/").append(fullyQualifiedPackageName.replace('.','/')) //NOI18N
                        .append("/").append(prefix).append(postfix);//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    static class CreateActionEntryOperation implements CreatedModifiedFiles.LayerOperation {
        private String name;
        private String packageName;
        
        public CreateActionEntryOperation(String actionname, String packageName) {
            this.packageName = packageName;
            this.name = actionname;
        }
        
        public void run(FileSystem layer) throws IOException {
            FileObject folder = layer.getRoot().getFileObject("Actions/Window");// NOI18N
            if (folder == null) {
                folder = FileUtil.createFolder(layer.getRoot(), "Actions/Window"); // NOI18N
            }
            String instance = packageName.replace('.','-') + "-" + name; // NOI18N
            FileObject file = folder.createData(instance, "instance"); // NOI18N
            folder = layer.getRoot().getFileObject("Menu/Window");// NOI18N
            if (folder == null) {
                folder = FileUtil.createFolder(layer.getRoot(), "Menu/Window"); // NOI18N
            }
            file = folder.createData(name, "shadow"); // NOI18N
            file.setAttribute("originalFile", "Actions/Window/" + instance + ".instance"); // NOI18N
        }
    }
    
}
