/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.classpath.j2ee;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.classpath.ClassPathItem;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

// will be moved to j2ee.common
/**
 *
 * @author Tomas Mysik
 * @since 1.21
 */
abstract class BaseClassPathSupport<T extends BaseClassPathSupport.Item> {

    static final String ATTR_FILES = "files"; // NOI18N
    static final String ATTR_DIRS = "dirs"; // NOI18N

    final AntProjectHelper antProjectHelper;
    final ClassPathSupport delegate;
    final String projectConfigurationNameSpace;

    BaseClassPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper, String[] wellKnownPaths, String libraryPrefix, String librarySuffix,
            String antArtifactPrefix, String projectConfigurationNameSpace) {
        // XXX call Parameters here as well?
        assert projectConfigurationNameSpace != null;

        this.antProjectHelper = antProjectHelper;
        this.projectConfigurationNameSpace = projectConfigurationNameSpace;
        delegate = ClassPathSupport.create(evaluator, referenceHelper, antProjectHelper, wellKnownPaths, libraryPrefix,
                librarySuffix, antArtifactPrefix);
    }

    /**
     * Creates list of <CODE>Items</CODE> from given property.
     */
    public Iterator<T> itemsIterator(String propertyValue, String libraryElementName) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList(propertyValue, libraryElementName).iterator();
    }

    public abstract List<T> itemsList(String propertyValue, String libraryElementName);

    public abstract String[] encodeToStrings(final List<T> items, String libraryElementName);

    // XXX move into utilities module
    public static void getFilesForItem(Item item, List<File> files, List<File> dirs) {
        if (item.isBroken()) {
            return;
        }
        switch (item.getType()) {
            case LIBRARY:
                List<URL> roots = item.getLibrary().getContent("classpath"); // NOI18N
                for (URL rootUrl : roots) {
                    File f = FileUtil.archiveOrDirForURL(rootUrl);
                    if (f != null) {
                        if (f.isFile()) {
                            files.add(f);
                        } else {
                            dirs.add(f);
                        }
                    }
                }
                break;
            case JAR:
                File root = item.getFile();
                if (root != null) {
                    if (root.isFile()) {
                        files.add(root);
                    } else {
                        dirs.add(root);
                    }
                }
                break;
            case ARTIFACT:
                String artifactFolder = item.getArtifact().getScriptLocation().getParent();
                URI[] locations = item.getArtifact().getArtifactLocations();
                for (URI location : locations) {
                    String fullLocation = artifactFolder + File.separator + location;
                    if (fullLocation.endsWith(File.separator)) {
                        dirs.add(new File(fullLocation));
                    } else {
                        files.add(new File(fullLocation));
                    }
                }
                break;
            default:
                // noop
                break;
        }
    }

    public String getLibraryReference(T item) {
        return delegate.getLibraryReference(item.delegate);
    }

    /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param ep the editable properties in which the result should be stored
     * @param aph AntProjectHelper used to resolve files
     * @param libCpProperty the library classpath property
     */
    public static boolean relativizeLibraryClassPath(final EditableProperties ep, final AntProjectHelper aph,
            final String libCpProperty) {
        return ClassPathSupport.relativizeLibraryClassPath(ep, aph, libCpProperty);
    }

    /**
     * Extracts <b>the first</b> nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    static String findText(Node parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }

    abstract static class Item implements ClassPathItem {

        final ClassPathSupport.Item delegate;
        private String property;
        private String raw;

        Item(ClassPathSupport.Item delegate, String property, String raw) {
            this.delegate = delegate;
            this.property = property;
            this.raw = raw;
        }

        void setReference(String property) {
            this.property = property;
        }

        public ClassPathItem.Type getType() {
            return delegate.getType();
        }

        public Library getLibrary() {
            return delegate.getLibrary();
        }

        public File getFile() {
            return delegate.getFile();
        }

        public AntArtifact getArtifact() {
            return delegate.getArtifact();
        }

        public URI getArtifactURI() {
            return delegate.getArtifactURI();
        }

        public String getReference() {
            return delegate.getReference();
        }

        public boolean isBroken() {
            return delegate.isBroken();
        }

        public String getRaw() {
            return raw;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object itemObject) {
            if (itemObject instanceof Item) {
                Item item = (Item) itemObject;
                return delegate.equals(item);
            }
            return false;
        }
    }
}
