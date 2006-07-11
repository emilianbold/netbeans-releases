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
    
    static boolean checkUnresolvedReferences(Collection prjDefs) {
        return getInvalidUserLibraries(prjDefs).size() > 0;
    }

    static boolean checkNotFoundUserLibraries(Collection prjDefs) {
        return notFoundUserLibraries(prjDefs).size() > 0;
    }
    
    static Collection notFoundUserLibraries(Collection allPrjDefs) {
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
