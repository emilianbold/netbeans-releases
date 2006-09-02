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
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Reports location of unit tests.
 * Rather than associating each test root to each source root, the project may
 * have any number of source and test roots, and each source root is associated
 * with all test roots, and each test root is associated with all source roots.
 * This is not as precise as it could be but in practice it is unlikely to matter.
 * Also all package roots within one compilation unit are treated interchangeably.
 * @see "#47835"
 * @author Jesse Glick
 */
final class TestQuery implements MultipleRootsUnitTestForSourceQueryImplementation {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;
    
    public TestQuery(AntProjectHelper helper, PropertyEvaluator eval, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.eval = eval;
        this.aux = aux;
    }

    public URL[] findUnitTests(FileObject source) {
        URL[][] data = findSourcesAndTests();
        URL sourceURL;
        try {
            sourceURL = source.getURL();
        } catch (FileStateInvalidException e) {
            return null;
        }
        if (Arrays.asList(data[0]).contains(sourceURL)) {
            return data[1];
        } else {
            return null;
        }
    }

    public URL[] findSources(FileObject unitTest) {
        URL[][] data = findSourcesAndTests();
        URL testURL;
        try {
            testURL = unitTest.getURL();
        } catch (FileStateInvalidException e) {
            return null;
        }
        if (Arrays.asList(data[1]).contains(testURL)) {
            return data[0];
        } else {
            return null;
        }
    }
    
    /**
     * Look for all source roots and test source roots in the project.
     * @return two-element array: first source roots, then test source roots
     */
    private URL[][] findSourcesAndTests() {
        List<URL> sources = new ArrayList<URL>();
        List<URL> tests = new ArrayList<URL>();
        Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        if (data != null) {
            for (Element cu : Util.findSubElements(data)) {
                assert cu.getLocalName().equals("compilation-unit") : cu;
                boolean isTests = Util.findElement(cu, "unit-tests", JavaProjectNature.NS_JAVA_2) != null; // NOI18N
                for (Element pr : Util.findSubElements(cu)) {
                    if (pr.getLocalName().equals("package-root")) { // NOI18N
                        String rawtext = Util.findText(pr);
                        assert rawtext != null;
                        String evaltext = eval.evaluate(rawtext);
                        if (evaltext != null) {
                            (isTests ? tests : sources).add(evalTextToURL(evaltext));
                        }
                    }
                }
            }
        }
        return new URL[][] {
            sources.toArray(new URL[sources.size()]),
            tests.toArray(new URL[tests.size()]),
        };
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
    
}
