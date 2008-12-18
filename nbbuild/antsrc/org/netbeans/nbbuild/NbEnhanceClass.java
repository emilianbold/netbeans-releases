/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
