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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Report the location of source folders (compilation units)
 * corresponding to declared build products.
 * @author Jesse Glick
 */
final class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation, AntProjectListener {
    
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private AuxiliaryConfiguration aux;
    
    /**
     * Map from known binary roots to lists of source roots.
     */
    private Map/*<URL,FileObject[]*/ roots = null;
    
    public SourceForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        helper.addAntProjectListener(this);
    }
    
    private void refresh () {
        roots = null;
    }
    
    public synchronized SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (roots == null) {
            // Need to compute it. Easiest to compute them all at once.
            roots = new HashMap();
            Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
            if (java == null) {
                return null;
            }
            List/*<Element>*/ compilationUnits = Util.findSubElements(java);
            Iterator it = compilationUnits.iterator();
            while (it.hasNext()) {
                Element compilationUnitEl = (Element)it.next();
                assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                List/*<URL>*/ binaries = findBinaries(compilationUnitEl);
                if (!binaries.isEmpty()) {
                    List/*<FileObject>*/ packageRoots = Classpaths.findPackageRoots(helper, evaluator, compilationUnitEl);
                    FileObject[] sources = (FileObject[])packageRoots.toArray(new FileObject[packageRoots.size()]);
                    Iterator it2 = binaries.iterator();
                    while (it2.hasNext()) {                        
                        URL u = (URL)it2.next();
                        FileObject[] orig = (FileObject[]) roots.get (u);
                        //The case when sources are in the separate compilation units but
                        //the output is built into a single archive is not very common.
                        //It is better to recreate arrays rather then to add source roots 
                        //into lists which will slow down creation of Result instances.
                        if (orig != null) {
                            FileObject[] merged = new FileObject[orig.length+sources.length];
                            System.arraycopy(orig, 0, merged, 0, orig.length);
                            System.arraycopy(sources, 0,  merged, orig.length, sources.length);
                            sources = merged;
                        }
                        roots.put(u, sources);
                    }
                }
            }
        }
        assert roots != null;
        FileObject[] sources = (FileObject[])roots.get(binaryRoot);
        return sources == null ? null : new Result (sources);       //TODO: Optimize it, resolution of sources should be done in the result        
    }
    
    /**
     * Find a list of URLs of binaries which will be produced from a compilation unit.
     * Result may be empty.
     */
    private List/*<URL>*/ findBinaries(Element compilationUnitEl) {
        List/*<Element>*/ builtToEls = Util.findSubElements(compilationUnitEl);
        List/*<URL>*/ binaries = new ArrayList(builtToEls.size());
        Iterator it = builtToEls.iterator();
        while (it.hasNext()) {
            Element builtToEl = (Element)it.next();
            if (!builtToEl.getLocalName().equals("built-to")) { // NOI18N
                continue;
            }
            String text = Util.findText(builtToEl);
            String textEval = evaluator.evaluate(text);
            if (textEval == null) {
                continue;
            }
            File buildProduct = helper.resolveFile(textEval);
            URL buildProductURL;
            try {
                buildProductURL = buildProduct.toURI().toURL();
            } catch (MalformedURLException e) {
                assert false : e;
                continue;
            }
            if (FileUtil.isArchiveFile(buildProductURL)) {
                buildProductURL = FileUtil.getArchiveRoot(buildProductURL);
            } else {
                // If it is not jar then it has to be folder. Make sure folder
                // URL ends with slash character. If buildProduct file above
                // does not exist then created URL will not end with slash!
                if (!buildProduct.exists() && !buildProductURL.toExternalForm().endsWith("/")) {
                    try {
                        buildProductURL = new URL(buildProductURL.toExternalForm()+"/");
                    } catch (MalformedURLException e) {
                        assert false : e;
                    }
                }
            }
            binaries.add(buildProductURL);
        }
        return binaries;
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        refresh();
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    private static class Result implements SourceForBinaryQuery.Result {
        
        private FileObject[] ret;
        
        public Result (FileObject[] ret) {
            this.ret = ret;
        }
        
        public FileObject[] getRoots () {
            return ret;
        }
        
        public void addChangeListener (ChangeListener l) {
            // XXX
        }
        
        public void removeChangeListener (ChangeListener l) {
            // XXX
        }
        
    }
    
}
