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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Handles Javadoc information.
 * @author Jesse Glick
 */
final class JavadocQuery implements JavadocForBinaryQueryImplementation {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;
    
    public JavadocQuery(AntProjectHelper helper, PropertyEvaluator eval, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.eval = eval;
        this.aux = aux;
    }

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        if (data != null) {
            Iterator/*<Element>*/ cus = Util.findSubElements(data).iterator();
            while (cus.hasNext()) {
                Element cu = (Element) cus.next();
                assert cu.getLocalName().equals("compilation-unit") : cu;
                boolean rightCU = false;
                Iterator/*<Element>*/ builtTos = Util.findSubElements(cu).iterator();
                while (builtTos.hasNext()) {
                    Element builtTo = (Element) builtTos.next();
                    if (builtTo.getLocalName().equals("built-to")) { // NOI18N
                        String rawtext = Util.findText(builtTo);
                        assert rawtext != null;
                        String evaltext = eval.evaluate(rawtext);
                        if (evaltext != null) {
                            if (evalTextToURL(evaltext).equals(binaryRoot)) {
                                rightCU = true;
                                break;
                            }
                        }
                    }
                }
                if (rightCU) {
                    List/*<URL>*/ resultURLs = new ArrayList();
                    Iterator/*<Element>*/ javadocTos = Util.findSubElements(cu).iterator();
                    while (javadocTos.hasNext()) {
                        Element javadocTo = (Element) javadocTos.next();
                        if (javadocTo.getLocalName().equals("javadoc-built-to")) { // NOI18N
                            String rawtext = Util.findText(javadocTo);
                            assert rawtext != null;
                            String evaltext = eval.evaluate(rawtext);
                            if (evaltext != null) {
                                resultURLs.add(evalTextToURL(evaltext));
                            }
                        }
                    }
                    return new FixedResult(resultURLs);
                }
            }
        }
        return null;
    }
    
    private URL evalTextToURL(String evaltext) {
        File location = helper.resolveFile(evaltext);
        URL u;
        try {
            u = location.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        if (FileUtil.isArchiveFile(u)) {
            return FileUtil.getArchiveRoot(u);
        } else {
            String us = u.toExternalForm();
            if (us.endsWith("/")) {
                return u;
            } else {
                try {
                    return new URL(us + '/');
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
    
    private static final class FixedResult implements JavadocForBinaryQuery.Result {
        
        private final List/*<URL>*/ urls;
        
        public FixedResult(List/*<URL>*/ urls) {
            this.urls = urls;
        }

        public URL[] getRoots() {
            return (URL[]) urls.toArray(new URL[urls.size()]);
        }
        
        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}

    }
    
}
