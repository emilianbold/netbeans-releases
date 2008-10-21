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

package org.openide.filesystems.annotations;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Convenience class for generating fragments of an XML layer.
 * @see LayerGeneratingProcessor#layer
 */
public final class LayerBuilder {

    private final Document doc;

    /**
     * Creates a new builder.
     * @param document a DOM representation of an XML layer which will be modified
     */
    public LayerBuilder(Document document) {
        this.doc = document;
    }

    /**
     * Adds a file to the layer.
     * You need to {@link File#write} it in order to finalize the effect.
     * @param path the full path to the desired file in resource format, e.g. {@code "Menu/File/exit.instance"}
     * @return a file builder
     */
    public File file(String path) {
        return new File(path);
    }

    /**
     * Builder for creating a single file entry.
     */
    public final class File {

        private final String path;
        private final Map<String,String[]> attrs = new LinkedHashMap<String,String[]>();
        private String contents;
        private String url;

        File(String path) {
            this.path = path;
        }

        /**
         * Gets the path this file is to be created under.
         * @return the configured path, as in {@link #file}
         */
        public String getPath() {
            return path;
        }

        /**
         * Configures the file to have inline text contents.
         * @param contents text to use as the body of the file
         * @return this builder
         */
        public File contents(String contents) {
            if (this.contents != null || url != null || contents == null) {
                throw new IllegalArgumentException();
            }
            this.contents = contents;
            return this;
        }

        /**
         * Configures the file to have external contents.
         * @param url a URL to the body of the file, e.g. {@code "nbresloc:/org/my/module/resources/definition.xml"}
         *            or more commonly an absolute resource path such as {@code "/org/my/module/resources/definition.xml"}
         * @return this builder
         */
        public File url(String url) {
            if (contents != null || this.url != null || url == null) {
                throw new IllegalArgumentException();
            }
            this.url = url;
            return this;
        }

        /**
         * Adds a string-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File stringvalue(String attr, String value) {
            attrs.put(attr, new String[] {"stringvalue", value});
            return this;
        }

        /**
         * Adds a byte-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File bytevalue(String attr, byte value) {
            attrs.put(attr, new String[] {"bytevalue", Byte.toString(value)});
            return this;
        }

        /**
         * Adds a short-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File shortvalue(String attr, short value) {
            attrs.put(attr, new String[] {"shortvalue", Short.toString(value)});
            return this;
        }

        /**
         * Adds an int-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File intvalue(String attr, int value) {
            attrs.put(attr, new String[] {"intvalue", Integer.toString(value)});
            return this;
        }

        /**
         * Adds a long-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File longvalue(String attr, long value) {
            attrs.put(attr, new String[] {"longvalue", Long.toString(value)});
            return this;
        }

        /**
         * Adds a float-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File floatvalue(String attr, float value) {
            attrs.put(attr, new String[] {"floatvalue", Float.toString(value)});
            return this;
        }

        /**
         * Adds a double-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File doublevalue(String attr, double value) {
            attrs.put(attr, new String[] {"doublevalue", Double.toString(value)});
            return this;
        }

        /**
         * Adds a boolean-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File boolvalue(String attr, boolean value) {
            attrs.put(attr, new String[] {"boolvalue", Boolean.toString(value)});
            return this;
        }

        /**
         * Adds a character-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File charvalue(String attr, char value) {
            attrs.put(attr, new String[] {"charvalue", Character.toString(value)});
            return this;
        }

        /**
         * Adds a URL-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File urlvalue(String attr, URL value) {
            attrs.put(attr, new String[] {"urlvalue", value.toString()});
            return this;
        }

        /**
         * Adds an attribute loaded from a Java method.
         * @param attr the attribute name
         * @param clazz the fully-qualified name of the factory class
         * @param method the name of a static method
         * @return this builder
         */
        public File methodvalue(String attr, String clazz, String method) {
            attrs.put(attr, new String[] {"methodvalue", clazz + "." + method});
            return this;
        }

        /**
         * Adds an attribute loaded from a Java constructor.
         * @param attr the attribute name
         * @param clazz the fully-qualified name of a class with a no-argument constructor
         * @return this builder
         */
        public File newvalue(String attr, String clazz) {
            attrs.put(attr, new String[] {"newvalue", clazz});
            return this;
        }

        /**
         * Adds an attribute loaded from a resource bundle.
         * @param attr the attribute name
         * @param bundle the full name of the bundle, e.g. {@code "org.my.module.Bundle"}
         * @param key the key to look up inside the bundle
         * @return this builder
         */
        public File bundlevalue(String attr, String bundle, String key) {
            attrs.put(attr, new String[] {"bundlevalue", bundle + "#" + key});
            return this;
        }

        // XXX do we want/need serialvalue? passed as String, or byte[], or Object?

        /**
         * Sets a position attribute.
         * This is a convenience method so you can define in your annotation:
         * <code>int position() default Integer.MAX_VALUE;</code>
         * and later call:
         * <code>fileBuilder.position(annotation.position())</code>
         * @param position a numeric position for this file, or {@link Integer#MAX_VALUE} to not define any position
         * @return this builder
         */
        public File position(int position) {
            if (position != Integer.MAX_VALUE) {
                intvalue("position", position);
            }
            return this;
        }

        /**
         * Writes the file to the layer.
         * Any intervening parent folders are created automatically.
         * If the file already exists, the old copy is replaced.
         * @return the originating layer builder, in case you want to add another file
         */
        public LayerBuilder write() {
            Element e = doc.getDocumentElement();
            String[] pieces = path.split("/");
            for (String piece : Arrays.asList(pieces).subList(0, pieces.length - 1)) {
                Element kid = find(e, piece);
                if (kid != null) {
                    if (!kid.getNodeName().equals("folder")) {
                        throw new IllegalArgumentException(path);
                    }
                    e = kid;
                } else {
                    e = (Element) e.appendChild(doc.createElement("folder"));
                    e.setAttribute("name", piece);
                }
            }
            String piece = pieces[pieces.length - 1];
            Element file = find(e,piece);
            if (file != null) {
                e.removeChild(file);
            }
            file = (Element) e.appendChild(doc.createElement("file"));
            file.setAttribute("name", piece);
            for (Map.Entry<String,String[]> entry : attrs.entrySet()) {
                Element attr = (Element) file.appendChild(doc.createElement("attr"));
                attr.setAttribute("name", entry.getKey());
                attr.setAttribute(entry.getValue()[0], entry.getValue()[1]);
            }
            if (url != null) {
                file.setAttribute("url", url);
            } else if (contents != null) {
                file.appendChild(doc.createCDATASection(contents));
            }
            return LayerBuilder.this;
        }

        private Element find(Element parent, String name) {
            NodeList nl = parent.getElementsByTagName("*");
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("name").equals(name)) {
                    return e;
                }
            }
            return null;
        }

    }

}
