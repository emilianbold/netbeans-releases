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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Specifies the Java source level (for example 1.4) to use for freeform sources.
 * @author Jesse Glick
 */
final class SourceLevelQueryImpl implements SourceLevelQueryImplementation, AntProjectListener {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    
    /**
     * Map from package roots to source levels.
     */
    private final Map/*<FileObject,String>*/ sourceLevels = new WeakHashMap();
    
    public SourceLevelQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        this.helper.addAntProjectListener(this);
    }
    
    public String getSourceLevel(FileObject file) {
        // Check for cached value.
        synchronized (this) {
            Iterator it = sourceLevels.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                FileObject root = (FileObject)entry.getKey();
                if (root == file || FileUtil.isParentOf(root, file)) {
                    // Already have it.
                    return (String)entry.getValue();
                }
            }
        }
        // Need to compute it.
        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        if (java == null) {
            return null;
        }
        List/*<Element>*/ compilationUnits = Util.findSubElements(java);
        Iterator it = compilationUnits.iterator();
        while (it.hasNext()) {
            Element compilationUnitEl = (Element)it.next();
            assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
            List/*<FileObject>*/ packageRoots = Classpaths.findPackageRoots(helper, evaluator, compilationUnitEl);
            Iterator it2 = packageRoots.iterator();
            while (it2.hasNext()) {
                FileObject root = (FileObject)it2.next();
                if (root == file || FileUtil.isParentOf(root, file)) {
                    synchronized (this) {
                        // Got it. Retrieve source level and cache it (for each root).
                        String lvl = getLevel(compilationUnitEl);
                        it2 = packageRoots.iterator();
                        while (it2.hasNext()) {
                            FileObject root2 = (FileObject)it2.next();
                            sourceLevels.put(root2, lvl);
                        }
                        return lvl;
                    }
                }
            }
        }
        // Didn't find anything.
        return null;
    }
    
    public void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent ev) {
    }

    public void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent ev) {
        synchronized (this) {
            this.sourceLevels.clear();
        }
    }
    
    /**
     * Get the source level indicated in a compilation unit (or null if none is indicated).
     */
    private String getLevel(Element compilationUnitEl) {
        Element sourceLevelEl = Util.findElement(compilationUnitEl, "source-level", JavaProjectNature.NS_JAVA_2);
        if (sourceLevelEl != null) {
            return Util.findText(sourceLevelEl);
        } else {
            return null;
        }
    }   
    
}
