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

package org.netbeans.modules.web.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class WebProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private WebProject project;
    
    public WebProjectOperations(WebProject project) {
        this.project = project;
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List/*<FileObject>*/ result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List/*<FileObject>*/ getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List/*<FileObject>*/ files = new ArrayList();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        List/*<FileObject>*/ files = new ArrayList();
        
        FileObject docRoot = project.getAPIWebModule().getDocumentBase();
        if (docRoot != null)
            files.add(docRoot);
        
        FileObject confDir = project.getWebModule().getConfDir();
        if (confDir != null)
            files.add(confDir);
        
        SourceRoots src = project.getSourceRoots();
        FileObject[] srcRoots = src.getRoots();

        for (int cntr = 0; cntr < srcRoots.length; cntr++) {
            files.add(srcRoots[cntr]);
        }

        PropertyEvaluator evaluator = project.evaluator();
        String prop = evaluator.getProperty(WebProjectProperties.SOURCE_ROOT);
        if (prop != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject srcDir = project.getAntProjectHelper().resolveFileObject(prop);
            if (projectDirectory != srcDir && !files.contains(srcDir))
                files.add(srcDir);
        }
        
        SourceRoots test = project.getTestSourceRoots();
        FileObject[] testRoots = test.getRoots();
        
        for (int cntr = 0; cntr < testRoots.length; cntr++) {
            files.add(testRoots[cntr]);
        }

        return files;
    }
    
    public void notifyDeleting() throws IOException {
        WebActionProvider ap = (WebActionProvider) project.getLookup().lookup(WebActionProvider.class);
        
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        //nothing.
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (project == original) { //TODO: this is illegal
            //do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        
        project.setName(nueName);
    }
    
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) {
        if (project == original) { //TODO: this is illegal
            //do nothing for the original project.
            return ;
        }
        
        project.setName(nueName);
        fixExternalSources(originalPath, project.getSourceRoots());
        fixExternalSources(originalPath, project.getTestSourceRoots());
    }
    
    private void fixExternalSources(final File originalPath, final SourceRoots sr) {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
                    String[] srcProps = sr.getRootProperties();
                    String[] names = sr.getRootNames();
                    List/*<URL>*/ roots = new ArrayList();
                    List/*<String>*/ displayNames = new ArrayList();
                    for (int i = 0; i < srcProps.length; i++) {
                        String prop = project.evaluator().getProperty(srcProps[i]);
                        if (prop != null) {
                            FileObject nueFile = null;
                            File originalFile = PropertyUtils.resolveFile(originalPath, prop);
                            
                            if (isParent(originalPath, originalFile)) {
                                nueFile = FileUtil.toFileObject(PropertyUtils.resolveFile(projectDirectory, prop));
                            } else {
                                nueFile = FileUtil.toFileObject(originalFile);
                            }
                            
                            if (nueFile == null) {
                                continue;
                            }
                            if (FileUtil.isArchiveFile(nueFile)) {
                                nueFile = FileUtil.getArchiveRoot(nueFile);
                            }
                            roots.add(nueFile.getURL());
                            displayNames.add(sr.getRootDisplayName(names[i], srcProps[i]));
                        }
                    }
                    
                    sr.putRoots((URL[] ) roots.toArray(new URL[0]), (String[] ) displayNames.toArray(new String[0])); //XXX
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private static boolean isParent(File folder, File fo) {
        if (folder.equals(fo))
            return false;
        
        while (fo != null) {
            if (fo.equals(folder))
                return true;
            
            fo = fo.getParentFile();
        }
        
        return false;
    }

}
