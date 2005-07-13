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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/** Pseudo-task to unpack a set of modules.
 * Resolves build-time dependencies of modules in selected moduleconfig
 * and print list of cvs modules which you need to checkout from cvs
 *
 * @author Rudolf Balada
 * based on dependency resolving code originally by Jesse Glick in NbMerge.java
 */
public class PrintCvsModules extends Task {
    
    private Vector modules = new Vector (); // list of modules defined by build.xml
    private Vector buildmodules = new Vector (); // list of modules which will be built
    private String targetprefix = "all-";    
    private String dummyName;
    private Hashtable targets;
    private String selectorId;
    private File dir;
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    /** String which will have a module name appended to it.
     * This will form a target in the same project which should
     * create the <samp>netbeans/</samp> subdirectory.
     */
    public void setTargetprefix (String s) {
        targetprefix = s;
    }
    
    /** Name of property to set the file set to.
     */
    public void setId (String s) {
        selectorId = s;
    }
    
    /** Directory with sources */
    public void setDir (File f) {
        dir = f;
    }

    /** Mode - either sources or external unscrambled binaries
     */
    public static final class Mode extends org.apache.tools.ant.types.EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {
                "sources",
                "binaries"
            };
        }
    }
    
    public void setMode (Mode m) {
        mode = m.getValue ();
    }
    
    private String mode = "sources";
    
    /**
     * 
     * @throws org.apache.tools.ant.BuildException 
     */
    public void execute () throws BuildException {

        buildmodules.addAll(modules);
        Vector compiletime = new Vector();
        Vector cvslist = new Vector();
        cvslist.add("nbbuild");
        for (int i = 0; i < buildmodules.size (); i++) {
            String module = (String) buildmodules.elementAt (i);
            log("module.indexOf(\"/\") is " + module.indexOf("/"), Project.MSG_DEBUG);
            if (module.indexOf("/") > 0) {
                module = module.substring(0,module.indexOf("/"));
                log("cvs module name is " + module, Project.MSG_VERBOSE);
            }
            if ( ! cvslist.contains(module) ) {
                cvslist.add(module);
            } else {
                log("not adding cvs module name " + module, Project.MSG_VERBOSE);
            }
        }

        log("compiletime="+compiletime);
        log("selectedmodules="+modules);
        log("cvsmodules="+cvslist);    
        
        if (selectorId != null) {
            if ("sources".equals (mode)) {
                org.netbeans.nbbuild.CvsFileSet set = new org.netbeans.nbbuild.CvsFileSet ();
                set.setDir (dir);
                Iterator it = cvslist.iterator ();
                while (it.hasNext ()) {
                    String modname = (String)it.next ();
                    set.createInclude ().setName (modname + "/**/*");
                    set.createExclude ().setName (modname + "/www/**/*");
                    set.createExclude ().setName (modname + "/test/**/*");
                }
                set.createExclude ().setName ("*/*/test/**/*");
                getProject ().addReference (selectorId, set);
            } else {
                // binaries
                org.apache.tools.ant.types.FileSet set = new org.apache.tools.ant.types.FileSet ();
                set.setDir (dir);
                Iterator it = cvslist.iterator ();
                while (it.hasNext ()) {
                    String modname = (String)it.next ();
                    set.createInclude ().setName (modname + "/external/*");
                }
                set.createExclude ().setName ("**/unscrambling.log");
                // walk through selected files and explicitly exclude unscrambled files
                org.apache.tools.ant.DirectoryScanner ds = set.getDirectoryScanner(this.getProject());
                ds.scan();
                String[] includedFiles = ds.getIncludedFiles();
                for (int i=0; i < includedFiles.length; i++) {
                    if (includedFiles[i].endsWith(".scrambled")) {
                        String ne = includedFiles[i].substring(0,includedFiles[i].length()-10);
                        log("Setting exclude for unscrambled file "+ne, Project.MSG_VERBOSE);
                        set.createExclude().setName(ne);
                    }
                }
                getProject ().addReference (selectorId, set);
            }
        }
    }        

}
