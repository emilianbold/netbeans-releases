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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Jaroslav Tulach
 */
public class NbEnhanceClass extends Task {

    /* Path to library containing the patch method */
    private Path patchPath;
    public Path createClasspath() {
        if (patchPath == null) {
            patchPath = new Path(getProject());
        }
        return patchPath.createPath();
    }
    
    /* Name of class with patch method */
    private String patchClass = "org.netbeans.PatchByteCode";
    public void setPatchClass (String f) {
        patchClass = f;
    }
    
    /** Name of the method to call. Must have signature byte[] x(byte[], Map)
     */
    private String enhanceMethod = "enhanceClass";
    public void setEnhanceMethod (String f) {
        enhanceMethod = f;
    }
    
    /* Base dir to find classes relative to */
    private File basedir;
    public void setBasedir (File f) {
        basedir = f;
    }
    
    /* The class to change its super class and the value of the super class.
     */
    public static class Patch {
        String clazz;
        String nbSuperClass;
        String nbImplements;
        List<Member> members;
        
        
        /** Class in form of java/lang/Object */
        public void setClass (String s) {
            clazz = s;
        }
        /** Class in form of java/lang/Object */
        public void setSuper (String s) {
            nbSuperClass = s;
        }
        public void setImplements (String s) {
            nbImplements = s;
        }
        
        public Object createMember () {
            Member m = new Member();
            if (members == null) {
                members = new ArrayList<Member>();
            }
            members.add (m);
            return m;
        }
        
        public static final class Member extends Object {
            String name;
            String rename;
            
            public void setName (String s) {
                name = s;
            }
            
            public void setRename (String s) {
                rename = s;
            }
        }
    }
    private List<Patch> patches = new ArrayList<Patch>();
    public Patch createPatch () {
        Patch n = new Patch ();
        patches.add(n);
        return n;
    }
    
    
    public void execute() throws BuildException {
        if (basedir == null) {
            throw new BuildException ("Attribute basedir must be specified");
        }
        
        if (patches.isEmpty()) {
            // no work
            return;
        }
        
        //
        // Initialize the method
        //
        
        ClassLoader cl = new AntClassLoader(getProject(), patchPath, false);
        
        Method m;
        try {
            Class<?> c = cl.loadClass(patchClass);
            m = c.getMethod(enhanceMethod, byte[].class, Map.class);
            if (m.getReturnType() != byte[].class) {
                throw new BuildException ("Method does not return byte[]: " + m);
            }
        } catch (Exception ex) {
            throw new BuildException ("Cannot initialize class " + patchClass + " and method " + enhanceMethod, ex);
        }
            
        /*
        try {
            log ("Testing method " + m);
            byte[] res = (byte[])m.invoke (null, new Object[] { new byte[0], "someString" });
        } catch (Exception ex) {
            throw new BuildException ("Exception during test invocation of the method", ex);
        }
         */
            
        //
        // Ok we have the method and we can do the patching
        //

        for (Patch p : patches) {
            if (p.clazz == null) {
                throw new BuildException ("Attribute class must be specified");
            }
            
            File f = new File (basedir, p.clazz + ".class");
            if (!f.exists ()) {
                throw new BuildException ("File " + f + " for class " + p.clazz + " does not exists");
            }
            
            byte[] arr = new byte[(int)f.length()];
            try {
                FileInputStream is = new FileInputStream (f);
                if (arr.length != is.read (arr)) {
                    throw new BuildException ("Not all bytes read");
                }
                is.close ();
            } catch (IOException ex) {
                throw new BuildException ("Cannot read file " + f, ex);
            }
            
            List<String> members = null;
            List<String> rename = null;
            if (p.members != null) {
                members = new ArrayList<String>();
                for (Patch.Member mem : p.members) {
                    members.add (mem.name);
                    
                    if (mem.rename != null) {
                        if (rename == null) {
                            rename = new ArrayList<String>();
                        }
                        rename.add (mem.name);
                        rename.add (mem.rename);
                    }
                }
            }
                
             
            byte[] out;
            try {
                Map<String,Object> args = new HashMap<String,Object>();
                if (p.nbSuperClass != null) {
                    args.put ("netbeans.superclass", p.nbSuperClass);
                }
                if (p.nbImplements != null) {
                    args.put ("netbeans.interfaces", p.nbImplements);
                }
                if (members != null) {
                    args.put ("netbeans.public", members);
                }
                if (rename != null) {
                    args.put ("netbeans.rename", rename);
                }
                
                log("Patching " + p.clazz + " with arguments " + args, Project.MSG_VERBOSE);
                
                out = (byte[]) m.invoke(null, arr, args);
                if (out == null) {
                    // no patching needed
                    continue;
                }
            } catch (Exception ex) {
                throw new BuildException (ex);
            }

            if (p.nbSuperClass != null) {
                log ("Enhanced " + f + " to have alternate superclass " + p.nbSuperClass + " and be public");
            } else {
                log ("Enhanced " + f + " to be public");
            }
            
            try {
                FileOutputStream os = new FileOutputStream (f);
                os.write (out);
                os.close ();
            } catch (IOException ex) {
                throw new BuildException ("Cannot overwrite file " + f, ex);
            }
        }
    }
    
}
