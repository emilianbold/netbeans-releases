/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.netbeans.modules.debugger.AbstractDebugger;
import org.netbeans.modules.debugger.Register;
import org.netbeans.modules.debugger.jpda.JPDADebugger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Ant task to reload classes in VM for running debugging session. 
 *
 * @author David Konecny
 */
public class JPDAReload extends Task {

    private List filesets = new ArrayList();
 
    /**
     * FileSet with .class files to reload. The base dir of the fileset is expected
     * to be classpath root for these classes.
     */
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
    
    public void execute() throws BuildException {
        if (filesets.size() == 0) {
            throw new BuildException("A nested fileset with class to refresh in VM must be specified.");
        }
        
        // check debugger state
        AbstractDebugger ad = Register.getCurrentDebugger();
        if (ad == null) {
            throw new BuildException("No debugging sessions was found.");
        }
        if (!ad.getDebuggerState().isFixEnabled()) {
            throw new BuildException("The debugger does not support Fix action.");
        }
        if (ad.getState() == AbstractDebugger.DEBUGGER_NOT_RUNNING) {
            throw new BuildException("The debugger is not running");
        }
        
        log("Classes to be reload:", Project.MSG_VERBOSE);
        
        FileUtils fu = FileUtils.newFileUtils();
        
        List classNames = new ArrayList();
        List files = new ArrayList();
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String fileNames[] = ds.getIncludedFiles();
            File baseDir = FileUtil.normalizeFile(fs.getDir(getProject()));
            for (int i=0; i<fileNames.length; i++) {
                File f = fu.resolveFile(baseDir, fileNames[i]);
                if (f != null) {
                    FileObject fos[] = FileUtil.fromFile(f);
                    if (fos.length > 0) {
                        files.add(fos[0]);
                        // remove ".class" from and use dots for for separator
                        classNames.add(fileNames[0].substring(0, fileNames[0].length()-6).replace(File.separatorChar,'.'));
                        log(" "+f, Project.MSG_VERBOSE);
                    }
                }
            }
        }
        if (files.size() == 0) {
            log(" No class to reload", Project.MSG_VERBOSE);
            return;
        }
        
        assert ad instanceof JPDADebugger;
        ((JPDADebugger)ad).reloadBytecode(files, classNames);
    }

}
