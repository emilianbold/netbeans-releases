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

package org.netbeans.modules.apisupport.project.ui.wizard.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Wizard for creating new project templates.
 *
 * @author Milos Kleint
 */
public class NewProjectIterator extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
    private NewProjectIterator.DataModel data = null;
    
    public static NewProjectIterator createIterator() {
        return new NewProjectIterator();
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
        data = new NewProjectIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new SelectProjectPanel(wiz, data),
                    new NameAndLocationPanel(wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private Project template;
        private String name;
        private String displayName;
        private String category;
        
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
        
        public Project getTemplate() {
            return template;
        }
        
        public void setTemplate(Project template) {
            this.template = template;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public CreatedModifiedFiles getFiles() {
            return files;
        }
        
        public void setFiles(CreatedModifiedFiles files) {
            this.files = files;
        }
        
    }
    
    public static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles fileChanges = new CreatedModifiedFiles(model.getProject());
        NbModuleProject project = model.getProject();
        final String category = model.getCategory();
        final String displayName = model.getDisplayName();
        final String name = model.getName();
        final String packageName = model.getPackageName();
        
        HashMap replaceTokens = new HashMap();
        replaceTokens.put("@@CATEGORY@@", category);//NOI18N
        replaceTokens.put("@@DISPLAYNAME@@", displayName);//NOI18N
        replaceTokens.put("@@TEMPLATENAME@@", name);//NOI18N
        replaceTokens.put("@@PACKAGENAME@@", packageName);//NOI18N
        
        
        // 1. create project description file
        final String descName = getRelativePath(project, packageName,
                name, "Description.html"); //NOI18N
        // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        URL template = NewProjectIterator.class.getResource("templateDescription.html");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(descName, template, replaceTokens));
        
        // 2. update project dependencies
        ProjectXMLManager manager = new ProjectXMLManager(project.getHelper());
        try {
            SortedSet set = manager.getDirectDependencies(project.getPlatform());
            if (set != null) {
                Iterator it = set.iterator();
                boolean filesystems = false;
                boolean loaders = false;
                boolean dialogs = false;
                boolean util = false;
                boolean projectui = false;
                boolean projectapi = false;
                boolean awt = false;
                while (it.hasNext()) {
                    ModuleDependency dep = (ModuleDependency)it.next();
                    if ("org.openide.filesystems".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        filesystems = true;
                    }
                    if ("org.openide.loaders".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        loaders = true;
                    }
                    if ("org.openide.dialogs".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        dialogs = true;
                    }
                    if ("org.openide.util".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        util = true;
                    }
                    if ("org.netbeans.modules.projectuiapi".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        projectui = true;
                    }
                    if ("org.netbeans.modules.projectapi".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        projectapi = true;
                    }
                    if ("org.openide.awt".equals(dep.getModuleEntry().getCodeNameBase())) { //NOI18N
                        // awt is here because of org.openide.awt.Mnemonics
                        awt = true;
                    }
                }
                if (!filesystems) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.filesystems", -1, null, true)); //NOI18N
                }
                if (!loaders) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.loaders", -1, null, true)); //NOI18N
                }
                if (!dialogs) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.dialogs", -1, null, true)); //NOI18N
                }
                if (!util) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.util", -1, null, true)); //NOI18N
                }
                if (!projectui) {
                    fileChanges.add(fileChanges.addModuleDependency("org.netbeans.modules.projectuiapi", -1, null, true)); //NOI18N
                }
                if (!projectapi) {
                    fileChanges.add(fileChanges.addModuleDependency("org.netbeans.modules.projectapi", -1, null, true)); //NOI18N
                }
                if (!awt) {
                    fileChanges.add(fileChanges.addModuleDependency("org.openide.awt", -1, null, true)); //NOI18N
                }
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        
        // 3. create sample template
        FileObject xml = LayerUtils.layerForProject(project).getLayerFile();
        FileObject parent = xml != null ? xml.getParent() : null;
        // XXX this is not fully accurate since if two ops would both create the same file,
        // really the second one would automatically generate a uniquified name... but close enough!
        Set externalFiles = Collections.singleton(LayerUtils.findGeneratedName(parent, name + "Project.zip")); // NOI18N
        fileChanges.add(fileChanges.layerModifications(new CreateProjectZipOperation(project, model.getTemplate(),
                name, packageName, category),
                externalFiles
                ));
        fileChanges.add(fileChanges.bundleKeyDefaultBundle(category + "/" + name +  "Project.zip", displayName)); // NOI18N
        
        // x. generate java classes
        final String iteratorName = getRelativePath(project, packageName,
                name, "WizardIterator.java"); //NOI18N
        // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        template = NewProjectIterator.class.getResource("templateWizardIterator.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(iteratorName, template, replaceTokens));
        final String panelName = getRelativePath(project, packageName,
                name, "WizardPanel.java"); //NOI18N
        // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        template = NewProjectIterator.class.getResource("templateWizardPanel.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(panelName, template, replaceTokens));
        
        final String panelVisName = getRelativePath(project, packageName,
                name, "PanelVisual.java"); //NOI18N
        // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
        template = NewProjectIterator.class.getResource("templatePanelVisual.javx");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(panelVisName, template, replaceTokens));
        
        
        model.setCreatedModifiedFiles(fileChanges);
    }
    
    private static String getRelativePath(NbModuleProject project, String fullyQualifiedPackageName,
            String prefix, String postfix) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(project.getSourceDirectoryPath()).append("/").append(fullyQualifiedPackageName.replace('.','/')) //NOI18N
        .append("/").append(prefix).append(postfix);//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    private static void createProjectZip(OutputStream target, Project source) throws IOException {
        Sources srcs = (Sources)source.getLookup().lookup(Sources.class);
        // assuming we got 1-sized array, should be enforced by UI.
        SourceGroup[] grps = srcs.getSourceGroups(Sources.TYPE_GENERIC);
        SourceGroup group = grps[0];
        Collection files = new ArrayList();
        collectFiles(group.getRootFolder(), files,
                SharabilityQuery.getSharability(FileUtil.toFile(group.getRootFolder())));
        createZipFile(target, group.getRootFolder(), files);
    }
    
    private static void collectFiles(FileObject parent, Collection accepted, int parentSharab) {
        FileObject[] fos = parent.getChildren();
        for (int i = 0; i < fos.length; i++) {
            int sharab;
            if (parentSharab == SharabilityQuery.UNKNOWN || parentSharab == SharabilityQuery.MIXED) {
                sharab = SharabilityQuery.getSharability(FileUtil.toFile(fos[i]));
            } else {
                sharab = parentSharab;
            }
            if (fos[i].isData() && !fos[i].isVirtual() && sharab == SharabilityQuery.SHARABLE) {
                accepted.add(fos[i]);
            } else if (fos[i].isFolder() && sharab != SharabilityQuery.NOT_SHARABLE) {
                collectFiles(fos[i], accepted, sharab);
            }
        }
    }
    
    private static void createZipFile(OutputStream target, FileObject root, Collection /* FileObject*/ files) throws IOException {
        //TODO create the zip..
        ZipOutputStream str = null;
        try {
            str = new ZipOutputStream(target);
            Iterator it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();
                ZipEntry entry = new ZipEntry(FileUtil.getRelativePath(root, fo));
                str.putNextEntry(entry);
                InputStream in = null;
                try {
                    in = fo.getInputStream();
                    FileUtil.copy(in, str);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                str.closeEntry();
            }
        } finally {
            if (str != null) {
                str.close();
            }
        }
    }
    
    static class CreateProjectZipOperation implements CreatedModifiedFiles.LayerOperation {
        
        private String name;
        private String packageName;
        private Project templateProject;
        private String category;
        
        public CreateProjectZipOperation(NbModuleProject project, Project template,
                                         String name, String packageName, String category) {
            this.packageName = packageName;
            this.name = name;
            this.category = category;
            templateProject = template;
        }
        
        public void run(FileSystem layer) throws IOException {
            FileObject folder = layer.getRoot().getFileObject(category);// NOI18N
            if (folder == null) {
                folder = FileUtil.createFolder(layer.getRoot(), category); // NOI18N
            }
            FileObject file = folder.createData(name + "Project", "zip"); // NOI18N
            FileLock lock = file.lock();
            try {
                createProjectZip(file.getOutputStream(lock), templateProject);
            } catch (IOException exc) {
                exc.printStackTrace();
            } finally {
                lock.releaseLock();
            }
            file.setAttribute("template", Boolean.TRUE); // NOI18N
            file.setAttribute("SystemFileSystem.localizingBundle", packageName + ".Bundle"); // NOI18N
            URL descURL = new URL("nbresloc:/" + packageName.replace('.', '/') + "/" + name + "Description.html"); // NOI18N
            file.setAttribute("instantiatingWizardURL", descURL); // NOI18N
            file.setAttribute("instantiatingIterator", "methodvalue:" + packageName + "." + name + "WizardIterator.createIterator"); // NOI18N
        }
    }
    
}
