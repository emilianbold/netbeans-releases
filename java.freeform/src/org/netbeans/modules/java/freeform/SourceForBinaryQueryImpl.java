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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
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

    private static final String CACHE_FREEFORM_ARTIFICAL_BIN = "nbproject/.artificial-binaries";   //NOI18N
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private AuxiliaryConfiguration aux;
    //@GuardedBy("this")
    private final Map<URI,URL> artificalBinariesCache;

    /**
     * Map from known binary roots to lists of source roots.
     */
    //@GuardedBy("this")
    private Map<URL,FileObject[]> roots = null;

    public SourceForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        this.artificalBinariesCache = new HashMap<>();
        helper.addAntProjectListener(this);
    }

    private synchronized void refresh () {
        roots = null;
    }

    public SourceForBinaryQuery.Result findSourceRoots(final URL binaryRoot) {
        final Map<URL,FileObject[]> rts = getRoots();
        assert rts != null;
        final FileObject[] sources = rts.get(binaryRoot);
        return sources == null ? null : new Result (sources);
    }

    public Collection<URL> findBinaryRoots(final URL sourceRoot) {
        //Todo: Perf - cache inverted map, do not convert fo->url
        final Map<URL,FileObject[]> rts = getRoots();
        assert rts != null;
        final List<URL> res = new ArrayList<>();
        for (Map.Entry<URL,FileObject[]> e : rts.entrySet()) {
            for (FileObject root : e.getValue()) {
                if (root.toURL().equals(sourceRoot)) {
                    res.add(e.getKey());
                }
            }
        }
        return res;
    }

    private Map<URL,FileObject[]> getRoots() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Map<URL,FileObject[]>>() {
            @Override
            public Map<URL,FileObject[]> run() {
                synchronized (SourceForBinaryQueryImpl.this) {
                    if (roots == null) {
                        // Need to compute it. Easiest to compute them all at once.
                        Map<URL, FileObject[]> tmp = new HashMap<>();
                        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
                        if (java == null) {
                            return null;
                        }
                        for (Element compilationUnit : XMLUtil.findSubElements(java)) {
                            assert compilationUnit.getLocalName().equals("compilation-unit") : compilationUnit;
                            List<URL> binaries = findBinaries(compilationUnit);
                            List<FileObject> packageRoots = Classpaths.findPackageRoots(helper, evaluator, compilationUnit);
                            FileObject[] sources = packageRoots.toArray(new FileObject[packageRoots.size()]);
                            if (binaries.isEmpty()) {
                                binaries = createArtificialBinaries(sources);
                            }
                            for (URL u : binaries) {
                                FileObject[] orig = tmp.get(u);
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
                                tmp.put(u, sources);
                            }
                        }
                        roots = Collections.unmodifiableMap(tmp);
                    }
                    return roots;
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

    private List<URL> createArtificialBinaries(FileObject[] fos) {
        assert Thread.holdsLock(this);
        final List<URL> res = new ArrayList<>(fos.length);
        File artBinaries = null;
        MessageDigest md5 = null;
        try {
            for (FileObject fo : fos) {
                final URI srcURI = fo.toURI();
                URL bin = artificalBinariesCache.get(srcURI);
                if (bin == null) {
                    if (artBinaries == null) {
                        final File projectFolder = FileUtil.toFile(helper.getProjectDirectory());
                        artBinaries = new File(projectFolder, CACHE_FREEFORM_ARTIFICAL_BIN.replace('/', File.separatorChar));   //NOI18N
                        md5 = MessageDigest.getInstance("MD5"); //NOI18N
                    } else {
                        md5.reset();
                    }
                    final String digest = str(md5.digest(srcURI.toString().getBytes("UTF-8"))); //NOI18N
                    final File binFile = new File (artBinaries,digest);
                    bin = FileUtil.urlForArchiveOrDir(binFile);
                    artificalBinariesCache.put(srcURI, bin);
                }
                res.add(bin);
            }
            return res;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String str(byte[] data) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String sbyte = Integer.toHexString(data[i] & 0xff);
            if (sbyte.length() == 1) {
                sb.append('0'); //NOI18N
            }
            sb.append(sbyte);
        }
        return sb.toString();
    }
}
