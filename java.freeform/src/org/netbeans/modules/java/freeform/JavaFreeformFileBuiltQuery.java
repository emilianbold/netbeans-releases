/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Provides a FileBuiltQueryImplementation for the Java Freeform projects.
 * Currently, for each compilation unit, it looks to built-to element, finds the first
 * directory and supposes it is the target of compilation for this compilation unit.
 *
 * @author Jan Lahoda
 */
final class JavaFreeformFileBuiltQuery implements FileBuiltQueryImplementation, AntProjectListener {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(JavaFreeformFileBuiltQuery.class.getName());
    
    private Project project;
    private AntProjectHelper projectHelper;
    private PropertyEvaluator projectEvaluator;
    private AuxiliaryConfiguration aux;
    
    public JavaFreeformFileBuiltQuery(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        this.project = project;
        this.projectHelper = projectHelper;
        this.projectEvaluator = projectEvaluator;
        this.aux = aux;
        
        this.delegateTo = null;
        
        projectHelper.addAntProjectListener(this);
    }
    
    private FileBuiltQueryImplementation delegateTo;
    
    private FileBuiltQueryImplementation createDelegateTo() {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "JavaFreeformFileBuiltQuery.createDelegateTo start"); // NOI18N
        }
        
        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        List from = new ArrayList();
        List to   = new ArrayList();
        
        if (java != null) {
            List/*<Element>*/ compilationUnits = Util.findSubElements(java);
            Iterator it = compilationUnits.iterator();
            while (it.hasNext()) {
                Element compilationUnitEl = (Element)it.next();
                assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                List rootNames = Classpaths.findPackageRootNames(compilationUnitEl);
                List builtToName = findBuiltToNames(compilationUnitEl);
                
                List rootPatterns = new ArrayList();
                String builtToPattern = null;
                
                for (int cntr = 0; cntr < rootNames.size(); cntr++) {
                    rootPatterns.add(projectEvaluator.evaluate((String) rootNames.get(cntr)) + "/*.java"); // NOI18N
                }
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "Looking for a suitable built-to:"); // NOI18N
                }
                
                for (int cntr = 0; cntr < builtToName.size(); cntr++) {
                    String builtTo = projectEvaluator.evaluate((String) builtToName.get(cntr));
                    boolean isFolder = JavaProjectGenerator.isFolder(projectEvaluator, FileUtil.toFile(project.getProjectDirectory()), builtTo);
                    
                    if (isFolder && builtToPattern == null) {
                        builtToPattern = builtTo + "/*.class"; // NOI18N
                        break;
                    }
                }
                
                if (builtToPattern != null) {
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log(ErrorManager.INFORMATIONAL, "Found built to pattern=" + builtToPattern + ", rootPatterns=" + rootPatterns); // NOI18N
                    }
                    for (int rootIndex = 0; rootIndex < rootPatterns.size(); rootIndex++) {
                        from.add(rootPatterns.get(rootIndex));
                        to.add(builtToPattern);
                    }
                } else {
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log(ErrorManager.INFORMATIONAL, "No built to pattern found, rootPatterns=" + rootPatterns); // NOI18N
                    }
                }
            }
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "JavaFreeformFileBuiltQuery from=" + from + " to=" + to); // NOI18N
        }
        
        String[] fromStrings = (String[] ) from.toArray(new String[from.size()]);
        String[] toStrings = (String[] ) to.toArray(new String[to.size()]);
        
        FileBuiltQueryImplementation fbqi = projectHelper.createGlobFileBuiltQuery(projectEvaluator, fromStrings, toStrings);
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "JavaFreeformFileBuiltQuery.createDelegateTo end"); // NOI18N
        }
        
        return fbqi;
    }
    
    public void propertiesChanged(AntProjectEvent evt) {
        //ignore
    }
    
    public void configurationXmlChanged(AntProjectEvent evt) {
        synchronized (this) {
            delegateTo = null;
        }
    }
    
    public synchronized Status getStatus(FileObject fo) {
        if (delegateTo == null)
            delegateTo = createDelegateTo();
        
        return delegateTo.getStatus(fo);
    }
    
    static List/*<String>*/ findBuiltToNames(Element compilationUnitEl) {
        List/*<String>*/ names = new ArrayList();
        Iterator it = Util.findSubElements(compilationUnitEl).iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            if (!e.getLocalName().equals("built-to")) { // NOI18N
                continue;
            }
            String location = Util.findText(e);
            names.add(location);
        }
        return names;
    }
    
}
