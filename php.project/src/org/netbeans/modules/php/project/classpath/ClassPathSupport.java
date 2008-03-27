/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.project.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 * @author Petr Hrebejk, Tomas Mysik
 */
public class ClassPathSupport {

    private static final Set<String> WELL_KNOWN_PATHS = new HashSet<String>(Arrays.asList(
            "${" + PhpProjectProperties.GLOBAL_INCLUDE_PATH + "}"));

    private final PropertyEvaluator evaluator;
    private final ReferenceHelper referenceHelper;
    private final AntProjectHelper antProjectHelper;

    public  ClassPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper) {
        assert evaluator != null;
        assert referenceHelper != null;
        assert antProjectHelper != null;

        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
    }

    public Iterator<Item> itemsIterator(String propertyValue) {
        // XXX more performance friendly impl. would return a lazzy iterator
        return itemsList(propertyValue).iterator();
    }

    public List<Item> itemsList(String propertyValue) {
        String[] pe = PropertyUtils.tokenizePath(propertyValue == null ? "" : propertyValue);
        List<Item> items = new ArrayList<Item>(pe.length);
        for (String p : pe) {
            Item item = null;
            if (WELL_KNOWN_PATHS.contains(p)) {
                // some well know classpath
                item = Item.create(p);
            } else {
                File f = null;
                String eval = evaluator.evaluate(p);
                if (eval != null) {
                    f = antProjectHelper.resolveFile(eval);
                }
                if (f == null || !f.exists()) {
                    item = Item.createBroken(eval, p);
                } else {
                    item = Item.create(eval, p);
                }
            }
            items.add(item);
        }
        return items;
    }

    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     */
    public String[] encodeToStrings(Iterator<Item> classpath) {
        List<String> result = new ArrayList<String>();
        while (classpath.hasNext()) {
            Item item = classpath.next();
            String reference = item.getReference();
            switch (item.getType()) {
                case FOLDER:
                    if (reference == null) {
                        // new file
                        String file = item.getFilePath();
                        // pass null as expected artifact type to always get file reference
                        reference = referenceHelper.createForeignFileReferenceAsIs(file, null);
                        item.property = reference;
                    }
                    break;
            }
            if (reference != null) {
                result.add(reference);
            }
        }

        String[] items = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            if (i < result.size() - 1) {
                items[i] = result.get(i) + ":"; // NOI18N
            } else  {
                items[i] = result.get(i);
            }
        }
        return items;
    }

    public static final class Item {
        public static enum Type {
            CLASSPATH,
            FOLDER
        }
        private final Type type;
        private final String filePath;
        private final boolean broken;
        private String property;

        private Item(Type type, String filePath, String property, boolean broken) {
            this.type = type;
            this.filePath = filePath;
            this.property = property;
            this.broken = broken;
        }

        // classpath
        public static Item create(String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null");
            }
            return new Item(Type.CLASSPATH, null, property, false);
        }

        // folder
        public static Item create(String filePath, String property) {
            if (filePath == null) {
                throw new IllegalArgumentException("filePath must not be null");
            }
            return new Item(Type.FOLDER, filePath, property, false);
        }

        // broken folder
        public static Item createBroken(String filePath, String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null in broken items");
            }
            return new Item(Type.FOLDER, filePath, property, true);
        }

        public Type getType() {
            return type;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getReference() {
            return property;
        }

        public boolean isBroken() {
            return broken;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("ClassPathSupport.Item[ type: ");
            sb.append(type.name());
            sb.append(", filePath: ");
            sb.append(filePath);
            sb.append(", property: ");
            sb.append(property);
            sb.append(", broken: ");
            sb.append(broken);
            sb.append(" ]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Item other = (Item) obj;
            if (broken != other.broken) {
                return false;
            }
            switch (getType()) {
                case CLASSPATH:
                    if (property != other.property && (property == null || !property.equals(other.property))) {
                        return false;
                    }
                    break;
                default:
                    if (filePath != other.filePath && (filePath == null || !filePath.equals(other.filePath))) {
                        return false;
                    }
                    break;
            }
            return true;
        }

        @Override
        public int hashCode() {
            if (broken) {
                return 42;
            }
            int hash = getType().ordinal();
            switch (getType()) {
                case CLASSPATH:
                    hash += property.hashCode();
                    break;
                default:
                    hash = 41 * hash + (filePath != null ? filePath.hashCode() : 0);
                    break;
            }
            return hash;
        }
    }

    /**
     * Converts the ant reference to the name of the referenced property
     * @param ant reference
     * @param the name of the referenced property
     */
    public static String getAntPropertyName(String property) {
        if (property != null
                && property.startsWith("${") // NOI18N
                && property.endsWith("}")) { // NOI18N
            return property.substring(2, property.length() - 1);
        }
        return property;
    }
}
