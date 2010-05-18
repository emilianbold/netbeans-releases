/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.projectimport.jbuilder.ui;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.WarningContainer;
import org.netbeans.modules.projectimport.j2seimport.ui.WarningMessage;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
final class Utils {
    
    /** Creates a new instance of Utils */
    private  Utils() {}
    
    static boolean checkNotFoundUserLibraries(Collection prjDefs) {
        boolean notfound = notFoundUserLibraries(prjDefs).size() > 0;
        if (!notfound && prjDefs != null) {
            for (Iterator it = prjDefs.iterator(); it.hasNext();) {
                AbstractProject prj = (AbstractProject) it.next();
                if (prj.getJdkId() != null && prj.getJDKDirectory() == null) return true;
            }
        }
        return notfound;
    }
    
    private static Collection notFoundUserLibraries(Collection allPrjDefs) {
        Collection notFoundUserLibraries = new HashSet();
        if (allPrjDefs != null) {
            Iterator prjsIt = allPrjDefs.iterator();
            while (prjsIt.hasNext()) {
                AbstractProject ap = (AbstractProject)prjsIt.next();
                Iterator it = ap.getUserLibraries().iterator();
                while (it.hasNext()) {
                    AbstractProject.UserLibrary uLib = (AbstractProject.UserLibrary)it.next();                    
                    if (uLib.fileNotFound()) {
                        notFoundUserLibraries.add(uLib);
                    }
                }
            }
        }
        
        return notFoundUserLibraries;
    }
    
    static Collection getInvalidUserLibraries(Collection allPrjDefs) {
        Collection invalidUserLibraries = new HashSet();
        if (allPrjDefs != null) {
            Iterator prjsIt = allPrjDefs.iterator();
            while (prjsIt.hasNext()) {
                AbstractProject ap = (AbstractProject)prjsIt.next();
                if (ap.getJDKDirectory() == null && ap.getJdkId() != null) {
                    invalidUserLibraries.add(new AbstractProject.UserLibrary(ap.getJdkId(), false));
                }                
                Iterator it = ap.getUserLibraries().iterator();
                while (it.hasNext()) {
                    AbstractProject.UserLibrary uLib = (AbstractProject.UserLibrary)it.next();
                    if (!uLib.isValid()) {
                        invalidUserLibraries.add(uLib);
                    }
                }
            }
        }
        
        return invalidUserLibraries;
    }
    
    static StringBuffer getDependencyErrors(Collection prjDefs, File destinationDir) {
        return getProjectsErrors(collectAllDependencies(prjDefs), destinationDir);
    }
    
    static StringBuffer getProjectsErrors(Collection prjDefs, File destinationDir) {
        StringBuffer errBuf = null;
        if (prjDefs != null) {
            for (Iterator it = prjDefs.iterator(); it.hasNext();) {
                AbstractProject ap = (AbstractProject)it.next();
                File importLocation = new File(destinationDir, ap.getName());
                
                if (ap.getErrors().size() > 0 || importLocation.exists()) {
                    errBuf = (errBuf == null) ? new StringBuffer() : errBuf;
                    for (Iterator errIt = ap.getErrors().iterator(); errIt.hasNext();) {
                        errBuf.append((String)errIt.next()).append("  ");//NOI18N
                    }
                    if (importLocation.exists()) {
                        String errMsg = NbBundle.getMessage(JBWizardPanel.class,
                                "MSG_ProjectAlreadyExists",importLocation.getAbsolutePath()); // NOI18N
                        errBuf.append(errMsg).append("  ");//NOI18N
                    }
                }
            }
        }        
        return errBuf;
    }

    static String getHtmlWarnings(Collection prjDefs) {
        Collection all = new LinkedList();
        if (prjDefs != null) {
            for (Iterator it = prjDefs.iterator(); it.hasNext();) {
                AbstractProject ap = (AbstractProject)it.next();
                all.addAll(ap.getWarnings().getAllWarnings());
            }
        }        
                        
        return (all.isEmpty()) ? null : 
            WarningMessage.createHtmlString("",all.iterator(), true, 4);//NOI18N
    }
    
    private  static Collection collectAllDependencies(Collection prjDefs) {
        Collection result = new HashSet();
        if (prjDefs != null && prjDefs.size() > 0) {
            for (Iterator it = prjDefs.iterator(); it.hasNext();) {
                AbstractProject ap = (AbstractProject)it.next();
                for (Iterator depsIt = ap.getDependencies().iterator(); depsIt.hasNext();) {
                    AbstractProject dep = (AbstractProject)depsIt.next();
                    result.add(dep);
                }
                
            }
            if (result.size() > 0) {
                result.addAll(collectAllDependencies(result));
            }
        }
        
        return result;
    }        
}
