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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/** Assistent in changing of build scripts.
 *
 * @author  Jaroslav Tulach
 */
public class FixDependencies extends Task {
    /** Replace*/
    private List<Replace> replaces = new ArrayList<Replace>();
    /** files to fix */
    private FileSet set;
    /** verify target */
    private String target;
    /** clean target */
    private String clean;
    /** relative path from module file to build script to use for verification */
    private String ant;
    /** trip only changed */
    private boolean onlyChanged;
    /** fail on error */
    private boolean fail;
    
    
    /** tasks to be executed */
    
    /** Initialize. */
    public FixDependencies() {
    }
    
    
    public Replace createReplace () {
        Replace r = new Replace ();
        replaces.add (r);
        return r;
    }
    
    public FileSet createFileset() throws BuildException {
        if (this.set != null) throw new BuildException ("Only one file set is allowed");
        this.set = new FileSet();
        return this.set;
    }
    
    public void setBuildTarget (String s) {
        target = s;
    }
    
    public void setCleanTarget (String s) {
        clean = s;
    }
    
    public void setAntFile (String r) {
        ant = r;
    }
    
    public void setStripOnlyChanged (boolean b) {
        onlyChanged = b;
    }
    
    public void setFailOnError (boolean b) {
        fail = b;
    }

    public void execute () throws org.apache.tools.ant.BuildException {
        FileScanner scan = this.set.getDirectoryScanner(getProject());
        File dir = scan.getBasedir();
        for (String kid : scan.getIncludedFiles()) {
            File xml = new File(dir, kid);
            if (!xml.exists()) throw new BuildException("File does not exist: " + xml, getLocation());

            log ("Fixing " + xml, Project.MSG_INFO);

            File script = null;
            Ant task = null;
            Ant cleanTask = null;
            if (ant != null && target != null) {
                task = (org.apache.tools.ant.taskdefs.Ant)getProject ().createTask ("ant");
                script = FileUtils.getFileUtils().resolveFile(xml, ant);
                if (!script.exists ()) {
                    String msg = "Skipping. Cannot find file " + ant + " from + " + xml;
                    if (fail) {
                        throw new BuildException (msg);
                    }
                    log(msg, Project.MSG_ERR);
                    continue;
                }
                task.setAntfile (script.getPath ());
                task.setDir (script.getParentFile ());
                task.setTarget (target);
                if (clean != null) {
                    cleanTask = (Ant) getProject().createTask("ant");
                    cleanTask.setAntfile (script.getPath ());
                    cleanTask.setDir (script.getParentFile ());
                    cleanTask.setTarget (clean);
                }

                try {
                    // before we do anything else, let's verify that we build
                    if (cleanTask != null) {
                        log ("Cleaning " + clean + " in " + script, org.apache.tools.ant.Project.MSG_INFO);
                        cleanTask.execute ();
                    }
                    log ("Sanity check executes " + target + " in " + script, org.apache.tools.ant.Project.MSG_INFO);
                    task.execute ();
                } catch (BuildException ex) {
                    if (fail) {
                        throw ex;
                    }

                    log("Skipping. Could not execute " + target + " in " + script, org.apache.tools.ant.Project.MSG_ERR);
                    continue;
                }
            }


            
            try {
                boolean change = fix (xml);
                if (onlyChanged && !change) {
                    continue;
                }
                simplify (xml, script, task, cleanTask);
            } catch (IOException ex) {
                throw new BuildException (ex, getLocation ());
            }
        }
    }
    
    /** Modifies the xml file to replace dependencies wiht new ones.
     * @return true if there was a change in the file
     */
    private boolean fix (File file) throws IOException, BuildException {
        int s = (int)file.length ();
        byte[] data = new byte[s];
        InputStream is = new FileInputStream(file);
        if (s != is.read (data)) {
            is.close ();
            throw new BuildException ("Cannot read " + file);
        }
        is.close ();
        
        String stream = new String (data);
        String old = stream;
        data = null;

        for (Replace r : replaces) {
            int idx = stream.indexOf ("<code-name-base>" + r.codeNameBase + "</code-name-base>");
            if (idx == -1) continue;
            
            int from = stream.lastIndexOf ("<dependency>", idx);
            if (from == -1) throw new BuildException ("No <dependency> tag before index " + idx);
            int after = stream.indexOf ("</dependency>", idx);
            if (after == -1) throw new BuildException ("No </dependency> tag after index " + idx);
            after = findNonSpace (stream, after + "</dependency>".length ());
            
            String remove = stream.substring (from, after);
            if (r.addCompileTime && remove.indexOf ("compile-dependency") == -1) {
                int fromAfter = "<dependency>".length();
                int nonSpace = findNonSpace (remove, fromAfter);
                String spaces = remove.substring (fromAfter, nonSpace);
                remove = remove.substring (0, fromAfter) + spaces + "<compile-dependency/>" + remove.substring (fromAfter);
            }
            
            StringBuffer sb = new StringBuffer ();
            sb.append (stream.substring (0, from));
            
            for (Module m : r.modules) {
                if (stream.indexOf ("<code-name-base>" + m.codeNameBase + "</code-name-base>") != -1) {
                    continue;
                }

                int beg = remove.indexOf (r.codeNameBase);
                int aft = beg + r.codeNameBase.length ();
                sb.append (remove.substring (0, beg));
                sb.append (m.codeNameBase);
                String a = remove.substring (aft);
                if (m.specVersion != null) {
                    a = a.replaceAll (
                        "<specification-version>[0-9\\.]*</specification-version>", 
                        "<specification-version>" + m.specVersion + "</specification-version>"
                    );
                }
                if (m.releaseVersion == null) {
                    a = a.replaceAll (
                        "<release-version>[0-9]*</release-version>[\n\r ]*", 
                        ""
                    );
                }
                
                sb.append (a);
            }
            
            sb.append (stream.substring (after));
            
            stream = sb.toString ();
        }
        
        if (!old.equals (stream)) {
            FileWriter fw = new FileWriter (file);
            fw.write (stream);
            fw.close ();
            return true;
        } else {
            return false;
        }
    } // end of fix
    
    private void simplify (
        File file, File script, org.apache.tools.ant.taskdefs.Ant task, org.apache.tools.ant.taskdefs.Ant cleanTask
    ) throws IOException, BuildException {
        if (ant == null || target == null) {
            return;
        }
        
        int s = (int)file.length ();
        byte[] data = new byte[s];
        InputStream is = new FileInputStream(file);
        if (s != is.read (data)) {
            is.close ();
            throw new BuildException ("Cannot read " + file);
        }
        is.close ();
        
        String stream = new String (data);
        String old = stream;

        int first = -1;
        int last = -1;
        int begin = -1;
        StringBuffer success = new StringBuffer ();
        StringBuffer sb = new StringBuffer ();
        for (;;) {
            if (cleanTask != null) {
                log ("Cleaning " + clean + " in " + script, Project.MSG_INFO);
                cleanTask.execute ();
            }
            
            int from = stream.indexOf ("<dependency>", begin);
            if (from == -1) {
                break;
            }
            
            if (first == -1) {
                first = from;
            }
            
            int after = stream.indexOf ("</dependency>", from);
            if (after == -1) throw new BuildException ("No </dependency> tag after index " + from);
            after = findNonSpace (stream, after + "</dependency>".length ());
            
            last = after;
            begin = last;

            // write the file without the 
            FileWriter fw = new FileWriter (file);
            fw.write (stream.substring (0, from) + stream.substring (after));
            fw.close ();
            
            String dep = stream.substring (from, after);
            if (dep.indexOf ("compile-dependency") == -1) {
                // skip non-compile dependencies
                sb.append (stream.substring (from, after));
                continue;
            }
            
            
            int cnbBeg = dep.indexOf ("<code-name-base>");
            int cnbEnd = dep.indexOf ("</code-name-base>");
            if (cnbBeg != -1 && cnbEnd != -1) {
                dep = dep.substring (cnbBeg + "<code-name-base>".length (), cnbEnd);
            }
            

            String result;
            try {
                log ("Executing target " + target + " in " + script, Project.MSG_INFO);
                task.execute ();
                result = "Ok";
                success.append (dep);
                success.append ("\n");
            } catch (BuildException ex) {
                result = "Failure";
                // ok, this is needed dependency
                sb.append (stream.substring (from, after));
            }
            log ("Removing dependency " + dep + ": " + result, Project.MSG_INFO);
            
        }

        if (first != -1) {
            // write the file without the 
            FileWriter fw = new FileWriter (file);
            fw.write (stream.substring (0, first) + sb.toString () + stream.substring (last));
            fw.close ();
        }
        
        log ("Final verification runs " + target + " in " + script, Project.MSG_INFO);
        // now verify, if there is a failure then something is wrong now
        task.execute ();
        
        if (success.length () == 0) {
            log ("No dependencies removed from " + script);
        } else {
            log ("Removed dependencies from " + script + ":\n" + success);
        }
    } // end of simplify

    private static int findNonSpace (String where, int from) {
        while (from < where.length () && Character.isWhitespace (where.charAt (from))) {
            from++;
        }
        return from;
    }

    public static final class Replace extends Object {
        String codeNameBase;
        List<Module> modules = new ArrayList<Module>();
        boolean addCompileTime;
        
        public void setCodeNameBase (String s) {
            codeNameBase = s;
        }
        
        public void setAddCompileTime (boolean b) {
            addCompileTime = b;
        }
        
        public Module createModule () {
            Module m = new Module ();
            modules.add (m);
            return m;
        }
        
    }
    
    public static final class Module extends Object {
        String codeNameBase;
        String specVersion;
        String releaseVersion;
        
        public void setCodeNameBase (String s) {
            codeNameBase = s;
        }
        
        
        public void setSpec (String s) {
            specVersion = s;
        }
        
        public void setRelease (String r) {
            releaseVersion = r;
        }
    }
}
