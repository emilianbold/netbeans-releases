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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Convenience class for generating fragments of an XML layer.
 * @see LayerGeneratingProcessor#layer
 * @since XXX #150447
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
     * Generates an instance file whose {@code InstanceCookie} would load a given class or method.
     * Useful for {@link LayerGeneratingProcessor}s which define layer fragments which instantiate Java objects from the annotated code.
     * <p>While you can pick a specific instance file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param annotationTarget an annotated {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
     * @param path path to folder of instance file, e.g. {@code "Menu/File"}
     * @param name instance file basename, e.g. {@code "my-menu-Item"}, or null to pick a name according to the element
     * @param type a type to which the instance ought to be assignable, or null to skip this check
     * @param processingEnv a processor environment used for {@link ProcessingEnvironment#getElementUtils} and {@link ProcessingEnvironment#getTypeUtils}
     * @return an instance file (call {@link File#write} to finalize)
     * @throws IllegalArgumentException if the annotationTarget is not of a suitable sort
     *                                  (detail message can be reported as a {@link Kind#ERROR})
     */
    public File instanceFile(javax.lang.model.element.Element annotationTarget, String path, String name, Class type,
            ProcessingEnvironment processingEnv) throws IllegalArgumentException {
        String[] clazzOrMethod = instantiableClassOrMethod(annotationTarget, type, processingEnv);
        String clazz = clazzOrMethod[0];
        String method = clazzOrMethod[1];
        String basename;
        if (name == null) {
            basename = clazz.replace('.', '-');
            if (method != null) {
                basename += "-" + method;
            }
        } else {
            basename = name;
        }
        LayerBuilder.File f = file(path + "/" + basename + ".instance");
        if (method != null) {
            f.methodvalue("instanceCreate", clazz, method);
        } else if (name != null) {
            f.stringvalue("instanceClass", clazz);
        } // else name alone suffices
        return f;
    }

    private static String[] instantiableClassOrMethod(javax.lang.model.element.Element annotationTarget, Class type,
            ProcessingEnvironment processingEnv) throws IllegalArgumentException {
        TypeMirror typeMirror = type != null ? processingEnv.getElementUtils().getTypeElement(type.getName().replace('$', '.')).asType() : null;
        switch (annotationTarget.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) annotationTarget).toString();
                if (annotationTarget.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new IllegalArgumentException(clazz + " must not be abstract");
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(annotationTarget.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new IllegalArgumentException(clazz + " must have a no-argument constructor");
                    }
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(annotationTarget.asType(), typeMirror)) {
                    throw new IllegalArgumentException(clazz + " is not assignable to " + typeMirror);
                }
                return new String[] {clazz, null};
            }
            case METHOD: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) annotationTarget.getEnclosingElement()).toString();
                String method = annotationTarget.getSimpleName().toString();
                if (!annotationTarget.getModifiers().contains(Modifier.STATIC)) {
                    throw new IllegalArgumentException(clazz + "." + method + " must be static");
                }
                if (!((ExecutableElement) annotationTarget).getParameters().isEmpty()) {
                    throw new IllegalArgumentException(clazz + "." + method + " must not take arguments");
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement) annotationTarget).getReturnType(), typeMirror)) {
                    throw new IllegalArgumentException(clazz + "." + method + " is not assignable to " + typeMirror);
                }
                return new String[] {clazz, method};
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + annotationTarget);
        }
    }

    /**
     * Convenience method to create a shadow file (like a symbolic link).
     * <p>While you can pick a specific shadow file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param target the complete path to the original file (use {@link File#getPath} if you just made it)
     * @param folder the folder path in which to create the shadow, e.g. {@code "Menu/File"}
     * @param name the basename of the shadow file sans extension, e.g. {@code "my-Action"}, or null to pick a default
     * @return a shadow file (call {@link File#write} to finalize)
     */
    public File shadowFile(String target, String folder, String name) {
        if (name == null) {
            name = target.replaceFirst("^.+/", "").replaceFirst("\\.[^./]+$", "");
        }
        return file(folder + "/" + name + ".shadow").stringvalue("originalFile", target);
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
         * @param value the attribute value, e.g. {@code "/my/module/resource.html"}
         *              or {@code "nbresloc:/my/module/resource.html"}; relative values permitted
         *              but not likely useful as base URL would be e.g. {@code "jar:...!/META-INF/"}
         * @return this builder
         * @throws IllegalArgumentException in case an opaque URI is passed as {@code value}
         */
        public File urlvalue(String attr, URI value) throws IllegalArgumentException {
            if (value.isOpaque()) {
                throw new IllegalArgumentException("Cannot use an opaque URI: " + value);
            }
            attrs.put(attr, new String[] {"urlvalue", value.toString()});
            return this;
        }

        /**
         * Adds a URL-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value, e.g. {@code "/my/module/resource.html"}
         *              or {@code "nbresloc:/my/module/resource.html"}; relative values permitted
         *              but not likely useful as base URL would be e.g. {@code "jar:...!/META-INF/"}
         * @return this builder
         * @throws IllegalArgumentException in case {@code value} cannot be passed as a URI or is opaque
         */
        public File urlvalue(String attr, String value) throws IllegalArgumentException {
            return urlvalue(attr, URI.create(value));
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
         * Adds an attribute to load a given class or method.
         * Useful for {@link LayerGeneratingProcessor}s which define layer fragments which instantiate Java objects from the annotated code.
         * @param attr the attribute name
         * @param annotationTarget an annotated {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
         * @param type a type to which the instance ought to be assignable, or null to skip this check
         * @param processingEnv a processor environment used for {@link ProcessingEnvironment#getElementUtils} and {@link ProcessingEnvironment#getTypeUtils}
         * @return this builder
         * @throws IllegalArgumentException if the annotationTarget is not of a suitable sort
         *                                  (detail message can be reported as a {@link Kind#ERROR})
         */
        public File instanceAttribute(String attr, javax.lang.model.element.Element annotationTarget, Class type,
            ProcessingEnvironment processingEnv) throws IllegalArgumentException {
            String[] clazzOrMethod = instantiableClassOrMethod(annotationTarget, type, processingEnv);
            if (clazzOrMethod[1] == null) {
                newvalue(attr, clazzOrMethod[0]);
            } else {
                methodvalue(attr, clazzOrMethod[0], clazzOrMethod[1]);
            }
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

        /**
         * Adds an attribute for a possibly localized string.
         * @param attr the attribute name
         * @param label either a general string to store as is, or a resource bundle reference
         *              such as {@code "my.module.Bundle#some_key"},
         *              or just {@code "#some_key"} to load from a {@code "Bundle"} in the same package
         * @param referenceElement if not null, a source element to determine the package
         * @param filer if not null, a way to look up the source bundle to verify that it exists and has the specified key
         * @return this builder
         * @throws IllegalArgumentException if a bundle key is requested but it cannot be found in sources
         *                                  (detail message can be reported as a {@link Kind#ERROR})
         */
        public File bundlevalue(String attr, String label, javax.lang.model.element.Element referenceElement, Filer filer) throws IllegalArgumentException {
            String javaIdentifier = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)";
            Matcher m = Pattern.compile("((?:" + javaIdentifier + "\\.)+[^\\s.#]+)?#(\\S+)").matcher(label);
            if (m.matches()) {
                String bundle = m.group(1);
                String key = m.group(2);
                if (bundle == null) {
                    while (referenceElement != null && referenceElement.getKind() != ElementKind.PACKAGE) {
                        referenceElement = referenceElement.getEnclosingElement();
                    }
                    if (referenceElement == null) {
                        throw new IllegalArgumentException("No reference element to determine package in '" + label + "'");
                    }
                    bundle = ((PackageElement) referenceElement).getQualifiedName() + ".Bundle";
                }
                if (filer != null) {
                    try {
                        InputStream is = filer.getResource(StandardLocation.SOURCE_PATH, "", bundle.replace('.', '/') + ".properties").openInputStream();
                        try {
                            Properties p = new Properties();
                            p.load(is);
                            if (p.getProperty(key) == null) {
                                throw new IllegalArgumentException("No key '" + key + "' found in " + bundle);
                            }
                        } finally {
                            is.close();
                        }
                    } catch (IOException x) {
                        throw new IllegalArgumentException("Could not open " + bundle + ": " + x);
                    }
                }
                bundlevalue(attr, bundle, key);
            } else {
                stringvalue(attr, label);
            }
            return this;
        }

        /**
         * Adds an attribute which deserializes a Java value.
         * @param attr the attribute name
         * @param data the serial data as created by {@link ObjectOutputStream}
         * @return this builder
         */
        public File serialvalue(String attr, byte[] data) {
            StringBuilder buf = new StringBuilder(data.length * 2);
            for (byte b : data) {
                if (b >= 0 && b < 16) {
                    buf.append('0');
                }
                buf.append(Integer.toHexString(b < 0 ? b + 256 : b));
            }
            attrs.put(attr, new String[] {"serialvalue", buf.toString().toUpperCase(Locale.ENGLISH)});
            return this;
        }

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
