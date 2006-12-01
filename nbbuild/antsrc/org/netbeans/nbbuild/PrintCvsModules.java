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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/** Pseudo-task to unpack a set of modules.
 * Resolves build-time dependencies of modules in selected moduleconfig
 * and print list of cvs modules which you need to checkout from cvs
 *
 * @author Rudolf Balada
 * based on dependency resolving code originally by Jesse Glick in NbMerge.java
 */
public class PrintCvsModules extends Task {
    
    private List<String> modules; // list of modules defined by build.xml
    private String selectorId;
    private File dir;
    private String cvsModulesProperty;
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new ArrayList<String>();
        while (tok.hasMoreTokens ())
            modules.add(tok.nextToken ());
    }
    
    /**
     * Specify a property which will be given the list of CVS modules, separated by spaces.
     */
    public void setCvsModulesProperty(String p) {
        cvsModulesProperty = p;
    }
    
    /** Name of property to set the file set to.
     */
    public void setId (String s) {
        selectorId = s;
    }
    
    /** Directory with sources (meaningful only with {@link #setId}) */
    public void setDir (File f) {
        dir = f;
    }

    public void execute () throws BuildException {
        Set<String> cvslist = new TreeSet<String>();
        cvslist.add("nbbuild");
        for (String module: modules) {
            int slash = module.indexOf('/');
            if (slash > 0) {
                module = module.substring(0, slash);
            }
            cvslist.add(module);
        }

        log("selectedmodules="+modules);
        log("cvsmodules="+cvslist);    
        if (cvsModulesProperty != null) {
            StringBuffer cvslistSpaces = new StringBuffer();
            Iterator cvsIt = cvslist.iterator();
            while (cvsIt.hasNext()) {
                if (cvslistSpaces.length() > 0) {
                    cvslistSpaces.append(' ');
                }
                cvslistSpaces.append((String) cvsIt.next());
            }
            getProject().setNewProperty(cvsModulesProperty, cvslistSpaces.toString());
        }
        
        if (selectorId != null) {
            FileSet set = new CvsFileSet();
            set.setDir(dir);
            Iterator it = cvslist.iterator();
            while (it.hasNext()) {
                String modname = (String) it.next();
                set.createInclude().setName(modname + "/**/*");
                set.createExclude().setName(modname + "/www/**/*");
                set.createExclude().setName(modname + "/test/**/*");
            }
            set.createExclude().setName("*/*/test/**/*");
            getProject().addReference(selectorId, set);
        }
    }        

}
