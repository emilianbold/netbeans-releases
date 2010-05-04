/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
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
    private Map<URL,FileObject[]> roots = null;

    public SourceForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        helper.addAntProjectListener(this);
    }

    private void refresh () {
        roots = null;
    }

    public SourceForBinaryQuery.Result findSourceRoots(final URL binaryRoot) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceForBinaryQuery.Result>() {
            public SourceForBinaryQuery.Result run() {
                synchronized (SourceForBinaryQueryImpl.this) {
        if (roots == null) {
            // Need to compute it. Easiest to compute them all at once.
            roots = new HashMap<URL,FileObject[]>();
            Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_3, true);
            if (java == null) {
                return null;
            }
            for (Element compilationUnit : XMLUtil.findSubElements(java)) {
                assert compilationUnit.getLocalName().equals("compilation-unit") : compilationUnit;
                List<URL> binaries = findBinaries(compilationUnit);
                if (!binaries.isEmpty()) {
                    List<FileObject> packageRoots = Classpaths.findPackageRoots(helper, evaluator, compilationUnit);
                    FileObject[] sources = packageRoots.toArray(new FileObject[packageRoots.size()]);
                    for (URL u : binaries) {
                        FileObject[] orig = roots.get(u);
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
        FileObject[] sources = roots.get(binaryRoot);
        return sources == null ? null : new Result (sources);       //TODO: Optimize it, resolution of sources should be done in the result
                }
            }
        });
    }

    /**
     * Find a list of URLs of binaries which will be produced from a compilation unit.
     * Result may be empty.
     */
    private List<URL> findBinaries(Element compilationUnitEl) {
        List<URL> binaries = new ArrayList<URL>();
        for (Element builtToEl : XMLUtil.findSubElements(compilationUnitEl)) {
            if (!builtToEl.getLocalName().equals("built-to")) { // NOI18N
                continue;
            }
            String text = XMLUtil.findText(builtToEl);
            String textEval = evaluator.evaluate(text);
            if (textEval == null) {
                continue;
            }
            File buildProduct = helper.resolveFile(textEval);
            URL buildProductURL = FileUtil.urlForArchiveOrDir(buildProduct);
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
