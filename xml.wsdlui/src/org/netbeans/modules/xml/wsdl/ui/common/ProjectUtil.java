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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;

/**
 * @author radval
 *
 * Netbeans Project related utility operations
 */
public class ProjectUtil {


    public static  Set getClasspathProjects(Project p) {
        Set classpathProjects = new HashSet();
        try {
            SubprojectProvider sp = (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
            Set ls = sp.getSubprojects();
            if (ls.size() < 1) {
                return classpathProjects;
            }

            Project[] sps = (Project[])ls.toArray(new Project[ls.size()]);
            String[] spn = new String[sps.length];
            for (int i = 0; i < sps.length; i++) {
                spn[i] = sps[i].getProjectDirectory().getPath().toLowerCase();
            }

            String sroot = p.getProjectDirectory().getPath();
            AntProjectHelper ah = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
            String src = ah.getStandardPropertyEvaluator().getProperty("javac.classpath");//NOI18N
            StringTokenizer st = new StringTokenizer(src, ";");//NOI18N
            Vector v = new Vector();
            while (st.hasMoreTokens()) {
                String spath = st.nextToken();
                File sfile =  new File(sroot+"/"+spath);//NOI18N
                v.add(sfile.getCanonicalPath().replace('\\', '/').toLowerCase());
            }
            String[] vs = (String[]) v.toArray(new String[0]);

            v.removeAllElements();
            for (int i = 0; i < vs.length; i++) {
                for (int j =0; j <sps.length; j++) {
                    if (vs[i].startsWith(spn[j])) {
                        v.add(sps[j]);
                        break;
                    }
                }
            }
            
            classpathProjects.addAll(v);
            return classpathProjects;
        } catch(Exception exception) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
        }

        return classpathProjects;
    }

}
