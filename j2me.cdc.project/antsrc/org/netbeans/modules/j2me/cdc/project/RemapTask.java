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

package org.netbeans.modules.j2me.cdc.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author suchys
 */
public class RemapTask extends Task {
    
    private File baseDir;
    private File source;
    private String targetDir;
    private String targetName;
    private String remappedProp;
    
    public void execute() throws BuildException {
        if (baseDir == null) throw new BuildException("basedir == null");
        if (source == null) throw new BuildException("source == null");
        if (targetDir == null) throw new BuildException("targetdir == null");
        if (targetName == null) throw new BuildException("targetname == null");

        File root = baseDir;
        List<File> roots = Arrays.asList(File.listRoots());
        File parent = source;
        StringBuffer sb = new StringBuffer();
        while( !root.equals(parent) && !roots.contains(parent)){
            sb.insert(0,  parent.getName());
            parent = parent.getParentFile();
            if (!root.equals(parent) && !roots.contains(parent))
                sb.insert(0,  '/');
        }
        if (roots.contains(parent)){
            if (remappedProp != null)
                this.getProject().setNewProperty(remappedProp, "true");
            parent = root;
        }
        File dest = new File(parent, sb.toString());
        
        this.getProject().setNewProperty(targetDir, dest.getAbsolutePath());
        this.getProject().setNewProperty(targetName, sb.toString());   
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getRemappedProp() {
        return remappedProp;
    }

    public void setRemappedProp(String remappedProp) {
        this.remappedProp = remappedProp;
    }
}
