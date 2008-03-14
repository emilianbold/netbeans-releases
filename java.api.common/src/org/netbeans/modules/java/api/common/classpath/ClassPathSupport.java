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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.api.common.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hrebejk, Tomas Mysik
 */
public final class ClassPathSupport {

    private final PropertyEvaluator evaluator;
    private final ReferenceHelper referenceHelper;
    private final AntProjectHelper antProjectHelper;
    private final Set<String> wellKnownPaths;
    private final String libraryPrefix;
    private final String librarySuffix;
    private final String antArtifactPrefix;

    public static ClassPathSupport create(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper, String[] wellKnownPaths, String libraryPrefix, String librarySuffix,
            String antArtifactPrefix) {
        Parameters.notNull("evaluator", evaluator);
        Parameters.notNull("referenceHelper", referenceHelper);
        Parameters.notNull("antProjectHelper", antProjectHelper);
        Parameters.notNull("libraryPrefix", libraryPrefix);
        Parameters.notNull("librarySuffix", librarySuffix);
        Parameters.notNull("antArtifactPrefix", antArtifactPrefix);

        return new ClassPathSupport(evaluator, referenceHelper, antProjectHelper, wellKnownPaths, libraryPrefix,
                librarySuffix, antArtifactPrefix);
    }

    ClassPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper, AntProjectHelper antProjectHelper,
            String[] wellKnownPaths, String libraryPrefix, String librarySuffix, String antArtifactPrefix) {
        assert evaluator != null;
        assert referenceHelper != null;
        assert antProjectHelper != null;
        assert libraryPrefix != null;
        assert librarySuffix != null;
        assert antArtifactPrefix != null;

        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        if (wellKnownPaths == null) {
            this.wellKnownPaths = null;
        } else {
            this.wellKnownPaths = new HashSet<String>(Arrays.asList(wellKnownPaths));
        }
        this.libraryPrefix = libraryPrefix;
        this.librarySuffix = librarySuffix;
        this.antArtifactPrefix = antArtifactPrefix;
    }

    /**
     * Creates list of <CODE>Items</CODE> from given property.
     */
    public Iterator<Item> itemsIterator(String propertyValue) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList(propertyValue).iterator();
    }

    public List<Item> itemsList(String propertyValue) {
        String pe[] = PropertyUtils.tokenizePath(propertyValue == null ? "" : propertyValue); // NOI18N
        List<Item> items = new ArrayList<Item>(pe.length);
        for (String p : pe) {
            Item item;

            // first try to find out whether the item is well known classpath
            if (isWellKnownPath(p)) {
                // some well know classpath
                item = Item.create(p);
            } else if (isLibrary(p)) {
                // library from library manager
                String libraryName = p.substring(libraryPrefix.length(), p.lastIndexOf('.')); //NOI18N
                Library library = LibraryManager.getDefault().getLibrary(libraryName);
                if (library == null) {
                    item = Item.createBroken(ClassPathItem.Type.LIBRARY, p);
                } else {
                    item = Item.create(library, p);
                }
            } else if (isAntArtifact(p)) {
                // ant artifact from another project
                Object[] ret = referenceHelper.findArtifactAndLocation(p);
                if (ret[0] == null || ret[1] == null) {
                    item = Item.createBroken(ClassPathItem.Type.ARTIFACT, p);
                } else {
                    // fix of issue #55316
                    AntArtifact artifact = (AntArtifact) ret[0];
                    URI uri = (URI) ret[1];
                    File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(p));
                    File artifactFile = new File(artifact.getScriptLocation().toURI().resolve(uri).normalize());
                    if (usedFile.equals(artifactFile)) {
                        item = Item.create(artifact, uri, p);
                    } else {
                        item = Item.createBroken(ClassPathItem.Type.ARTIFACT, p);
                    }
                }
            } else {
                // standalone jar or property
                String eval = evaluator.evaluate(p);
                File f = null;
                if (eval != null) {
                    f = antProjectHelper.resolveFile(eval);
                }
                if (f == null || !f.exists()) {
                    item = Item.createBroken(f, p);
                } else {
                    item = Item.create(f, p);
                }
            }
            items.add(item);
        }

        return items;

    }

    /**
     * Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     */
    public String[] encodeToStrings(final List<Item> items) {
        List<String> result = new ArrayList<String>();
        for (Item item : items) {
            String reference = getReference(item);
            if (reference != null) {
                result.add(reference);
            }
        }

        // XXX create util method
        String[] strings = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            if (i < result.size() - 1) {
                strings[i] = result.get(i) + ":"; // NOI18N
            } else  {
                strings[i] = result.get(i);
            }
        }
        return strings;
    }

    public String getReference(final Item item) {
        String reference = null;
        switch (item.getType()) {
            case JAR:
                reference = item.getReference();
                if (item.isBroken()) {
                    break;
                }
                if (reference == null) {
                    // new file
                    File file = item.getFile();
                    // pass null as expected artifact type to always get file reference
                    reference = referenceHelper.createForeignFileReference(file, null);
                }
                break;
            case LIBRARY:
                reference = item.getReference();
                if (item.isBroken()) {
                    break;
                }
                Library library = item.getLibrary();
                if (reference == null) {
                    if (library == null) {
                        break;
                    }
                    reference = getLibraryReference(item);
                }
                break;
            case ARTIFACT:
                reference = item.getReference();
                if (item.isBroken()) {
                    break;
                }
                AntArtifact artifact = item.getArtifact();
                if (reference == null) {
                    if (artifact == null) {
                        break;
                    }
                    reference = referenceHelper.addReference(item.getArtifact(), item.getArtifactURI());
                }
                break;
            case CLASSPATH:
                reference = item.getReference();
                break;
        }
        return reference;
    }

    public String getLibraryReference(Item item) {
        if (item.getType() != ClassPathItem.Type.LIBRARY) {
            throw new IllegalArgumentException("Item must be of type LIBRARY");
        }
        return libraryPrefix + item.getLibrary().getName() + librarySuffix;
    }

    // Private methods ---------------------------------------------------------

    private boolean isWellKnownPath(String property) {
        return wellKnownPaths == null ? false : wellKnownPaths.contains(property);
    }

    private boolean isAntArtifact(String property) {
        return antArtifactPrefix == null ? false : property.startsWith(antArtifactPrefix);
    }

    private boolean isLibrary(String property) {
        if (libraryPrefix != null && property.startsWith(libraryPrefix)) {
            return librarySuffix == null ? true : property.endsWith(librarySuffix);
        }
        return false;
    }

    // Innerclasses ------------------------------------------------------------

    /** Item of the classpath.
     */
    public static final class Item implements ClassPathItem {

        private final ClassPathItem.Type type;
        private final Object object;
        private final String property;
        private final boolean broken;
        private URI artifactURI;

        private Item(ClassPathItem.Type type, Object object, String property, boolean broken) {
            this.type = type;
            this.object = object;
            this.property = property;
            this.broken = broken;
        }

        private Item(ClassPathItem.Type type, Object object, String property) {
            this(type, object, property, false);
        }

        private Item(ClassPathItem.Type type, Object object, URI artifactURI, String property) {
            this(type, object, property, false);
            this.artifactURI = artifactURI;
        }

        // Factory methods -----------------------------------------------------


        public static Item create(Library library, String property) {
            if (library == null) {
                throw new IllegalArgumentException("library must not be null");
            }
            return new Item(ClassPathItem.Type.LIBRARY, library, property);
        }

        public static Item create(AntArtifact artifact, URI artifactURI, String property) {
            if (artifactURI == null) {
                throw new IllegalArgumentException("artifactURI must not be null");
            }
            if (artifact == null) {
                throw new IllegalArgumentException("artifact must not be null");
            }
            return new Item(ClassPathItem.Type.ARTIFACT, artifact, artifactURI, property);
        }

        public static Item create(File file, String property) {
            if (file == null) {
                throw new IllegalArgumentException("file must not be null");
            }
            return new Item(ClassPathItem.Type.JAR, file, property);
        }

        public static Item create(String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null");
            }
            return new Item(ClassPathItem.Type.CLASSPATH, null, property);
        }

        public static Item createBroken(ClassPathItem.Type type, String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null in broken items");
            }
            return new Item(type, null, property, true);
        }

        public static Item createBroken(final File file, String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null in broken items");
            }
            return new Item(ClassPathItem.Type.JAR, file, property, true);
        }

        // Instance methods ----------------------------------------------------

        public ClassPathItem.Type getType() {
            return type;
        }

        public Library getLibrary() {
            if (getType() != ClassPathItem.Type.LIBRARY) {
                throw new IllegalArgumentException("Item is not of required type - LIBRARY");
            }
            assert object instanceof Library : "Invalid object type: " + object.getClass().getName()
                    + " instance: " + object.toString() + " expected type: Library";
            return (Library) object;
        }

        public File getFile() {
            if (getType() != ClassPathItem.Type.JAR) {
                throw new IllegalArgumentException("Item is not of required type - JAR");
            }
            return (File) object;
        }

        public AntArtifact getArtifact() {
            if (getType() != ClassPathItem.Type.ARTIFACT) {
                throw new IllegalArgumentException("Item is not of required type - ARTIFACT");
            }
            return (AntArtifact) object;
        }

        public URI getArtifactURI() {
            if (getType() != ClassPathItem.Type.ARTIFACT) {
                throw new IllegalArgumentException("Item is not of required type - ARTIFACT");
            }
            return artifactURI;
        }

        public String getReference() {
            return property;
        }

        public boolean isBroken() {
            return this.broken;
        }

        @Override
        public int hashCode() {
            int hash = getType().ordinal();

            if (this.broken) {
                return 42;
            }

            switch (getType()) {
                case ARTIFACT:
                    hash += getArtifact().getType().hashCode();
                    hash += getArtifact().getScriptLocation().hashCode();
                    hash += getArtifactURI().hashCode();
                    break;
                case CLASSPATH:
                    hash += property.hashCode();
                    break;
                default:
                    hash += object.hashCode();
            }
            return hash;
        }

        @Override
        public boolean equals(Object itemObject) {
            if (!(itemObject instanceof Item)) {
                return false;
            }

            Item item = (Item) itemObject;
            if (getType() != item.getType()) {
                return false;
            }
            if (isBroken() != item.isBroken()) {
                return false;
            }
            if (isBroken()) {
                return getReference().equals(item.getReference());
            }

            switch (getType()) {
                case ARTIFACT:
                    if (getArtifact().getType() != item.getArtifact().getType()) {
                        return false;
                    }
                    if (!getArtifact().getScriptLocation().equals(item.getArtifact().getScriptLocation())) {
                        return false;
                    }
                    if (!getArtifactURI().equals(item.getArtifactURI())) {
                        return false;
                    }
                    return true;
                case CLASSPATH:
                    return property.equals(item.property);
            }
            return object.equals(item.object);
        }
    }

    /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param ep the editable properties in which the result should be stored
     * @param aph AntProjectHelper used to resolve files
     * @param libCpProperty the library classpath property
     */
    public static boolean relativizeLibraryClassPath(final EditableProperties ep, final AntProjectHelper aph,
            final String libCpProperty) {
        String value = PropertyUtils.getGlobalProperties().getProperty(libCpProperty);
        // bugfix #42852, check if the classpath property is set, otherwise return null
        if (value == null) {
            return false;
        }
        String[] paths = PropertyUtils.tokenizePath(value);
        StringBuilder sb = new StringBuilder();
        File projectDir = FileUtil.toFile(aph.getProjectDirectory());
        for (int i = 0; i < paths.length; i++) {
            File f = aph.resolveFile(paths[i]);
            if (CollocationQuery.areCollocated(f, projectDir)) {
                sb.append(PropertyUtils.relativizeFile(projectDir, f));
            } else {
                return false;
            }
            if (i + 1 < paths.length) {
                sb.append(File.pathSeparatorChar);
            }
        }
        if (sb.length() == 0) {
            return false;
        }
        ep.setProperty(libCpProperty, sb.toString());
        ep.setComment(libCpProperty, new String[]{
            // XXX this should be I18N! Not least because the English is wrong...
            "# Property "+libCpProperty+" is set here just to make sharing of project simpler.",
            "# The library definition has always preference over this property."}, false);
        return true;
    }
}
