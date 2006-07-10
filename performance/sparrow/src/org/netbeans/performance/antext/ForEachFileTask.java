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
package org.netbeans.performance.antext;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;
import java.util.*;
import java.io.File;

/** Call a target for each file in filesets
 * @author Jesse Glick, Tim Boudreau
 * @see CallTask
 */
public class ForEachFileTask extends Task {

    private String subTarget;
    public void setTarget(String t) {
        subTarget = t;
    }

    private List filesets = new LinkedList(); // List<FileSet>
    public void addFileset(FileSet fs) {
        filesets.add(fs); 
    }
    
    public static class Param {
        private String name, value; 
        public String getName() {return name;}
        public void setName(String s) {name = s;}
        public String getValue() {return value;}
        public void setValue(String s) {value = s;}
        public void setLocation(File f) {value = f.getAbsolutePath();}
    }
    private List properties = new LinkedList(); // List<Param>
    public void addParam(Param p) {
        properties.add(p);
    }
    
    public void execute() throws BuildException {
        if (subTarget == null) throw new BuildException("No subtarget set.");
        if (filesets.isEmpty()) throw new BuildException("No files to process - fileset is empty");
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                Ant callee = (Ant)project.createTask("ant");
                callee.setOwningTarget(target);
                callee.setTaskName(getTaskName());
                callee.setLocation(location);
                callee.init();
                Property p=callee.createProperty();
                p.setName ("configfile");
                File f = new File(basedir, files[i]);
                p.setLocation(f);
                
                p=callee.createProperty();
                p.setName ("configfile_unqualified");
                p.setValue(files[i]);
                
                System.out.println("Testing config file " + files[i]);
                Iterator props = properties.iterator();
                while (props.hasNext()) {
                    Param p1 = (Param)props.next();
                    Property p2 = callee.createProperty();
                    p2.setName(p1.getName());
                    p2.setValue(p1.getValue());
                }
                callee.setDir(project.getBaseDir());
                callee.setAntfile(project.getProperty("ant.file"));
                callee.setTarget(subTarget);
                //callee.setInheritAll(true);
                callee.execute();
            }
        }
    }
    
}
