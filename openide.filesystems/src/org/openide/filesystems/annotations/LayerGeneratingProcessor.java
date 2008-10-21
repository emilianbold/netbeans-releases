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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openide.filesystems.XMLFileSystem;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Convenience base class for an annotation processor which creates XML layer entries.
 * @see XMLFileSystem
 */
public abstract class LayerGeneratingProcessor extends AbstractProcessor {

    private static final String GENERATED_LAYER = "META-INF/generated-layer.xml";
    private static final String PUBLIC_DTD_ID = "-//NetBeans//DTD Filesystem 1.2//EN";
    private static final String NETWORK_DTD_URL = "http://www.netbeans.org/dtds/filesystem-1_2.dtd";
    private static final String LOCAL_DTD_RESOURCE = "/org/openide/filesystems/filesystem1_2.dtd";

    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
        public void warning(SAXParseException exception) throws SAXException {throw exception;}
        public void error(SAXParseException exception) throws SAXException {throw exception;}
        public void fatalError(SAXParseException exception) throws SAXException {throw exception;}
    };

    private static final EntityResolver ENTITY_RESOLVER = new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (PUBLIC_DTD_ID.equals(publicId)) {
                return new InputSource(LayerGeneratingProcessor.class.getResource(LOCAL_DTD_RESOURCE).toString());
            } else {
                return null;
            }
        }
    };

    private static final Map<ProcessingEnvironment,Document> generatedLayerByProcessor = new WeakHashMap<ProcessingEnvironment,Document>();
    private static final Map<ProcessingEnvironment,List<Element>> originatingElementsByProcessor = new WeakHashMap<ProcessingEnvironment,List<Element>>();

    /** For access by subclasses. */
    protected LayerGeneratingProcessor() {}

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean ret = doProcess(annotations, roundEnv);
        if (roundEnv.processingOver()) {
            Document doc = generatedLayerByProcessor.remove(processingEnv);
            if (doc != null) {
                Element[] originatingElementsA = new Element[0];
                List<Element> originatingElementsL = originatingElementsByProcessor.remove(processingEnv);
                if (originatingElementsL != null) {
                    originatingElementsA = originatingElementsL.toArray(originatingElementsA);
                }
                try {
                    // Write to memory and reparse to make sure it is valid according to DTD before writing to disk.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    XMLUtil.write(doc, baos, "UTF-8");
                    byte[] data = baos.toByteArray();
                    XMLUtil.parse(new InputSource(new ByteArrayInputStream(data)), true, true, ERROR_HANDLER, ENTITY_RESOLVER);
                    FileObject layer = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", GENERATED_LAYER, originatingElementsA);
                    OutputStream os = layer.openOutputStream();
                    try {
                        os.write(data);
                    } finally {
                        os.close();
                    }
                    {
                        SortedSet<String> files = new TreeSet<String>();
                        NodeList nl = doc.getElementsByTagName("file");
                        for (int i = 0; i < nl.getLength(); i++) {
                            org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(i);
                            String name = e.getAttribute("name");
                            while ((e = (org.w3c.dom.Element) e.getParentNode()).getTagName().equals("folder")) {
                                name = e.getAttribute("name") + "/" + name;
                            }
                            files.add(name);
                        }
                        processingEnv.getMessager().printMessage(Kind.NOTE, "generated layer entries: " + files);
                    }
                } catch (IOException x) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write generated-layer.xml: " + x.toString());
                } catch (SAXException x) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Refused to write invalid generated-layer.xml: " + x.toString());
                }
            }
        }
        return ret;
    }

    /**
     * The regular body of {@link #process}.
     * In the last round, one of the layer-generating processors will write out generated-layer.xml.
     * <p>Do not attempt to read or write the layer file directly; just use {@link #layer}.
     * You may however wish to create other resource files yourself: see {@link LayerBuilder.File#url} for syntax.
     * @param annotations as in {@link #process}
     * @param roundEnv as in {@link #process}
     * @return as in {@link #process}
     */
    protected abstract boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

    /**
     * Access the generated XML layer document.
     * May already have content from a previous compilation run which should be overwritten.
     * May also have content from other layer-generated processors which should be appended to.
     * Simply make changes to the document and they will be written to disk at the end of the job.
     * <p>Use {@link LayerBuilder} to easily add file entries without working with the DOM directly.
     * @param originatingElements as in {@link Filer#createResource}, optional
     * @return the DOM document corresponding to the XML layer being created
     */
    protected final Document layer(Element... originatingElements) {
        List<Element> originatingElementsL = originatingElementsByProcessor.get(processingEnv);
        if (originatingElementsL == null) {
            originatingElementsL = new ArrayList<Element>();
            originatingElementsByProcessor.put(processingEnv, originatingElementsL);
        }
        originatingElementsL.addAll(Arrays.asList(originatingElements));
        Document doc = generatedLayerByProcessor.get(processingEnv);
        if (doc == null) {
            try {
                FileObject layer = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", GENERATED_LAYER);
                InputStream is = layer.openInputStream();
                try {
                    doc = XMLUtil.parse(new InputSource(is), true, true, ERROR_HANDLER, ENTITY_RESOLVER);
                } finally {
                    is.close();
                }
            } catch (FileNotFoundException fnfe) {
                // Fine, not yet created.
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to read generated-layer.xml: " + x.toString());
            } catch (SAXException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to parse generated-layer.xml: " + x.toString());
            }
            if (doc == null) {
                doc = XMLUtil.createDocument("filesystem", null, PUBLIC_DTD_ID, NETWORK_DTD_URL);
            }
            generatedLayerByProcessor.put(processingEnv, doc);
        }
        return doc;
    }

    /**
     * Generate an instance file whose {@code InstanceCookie} would load a given class or method.
     * Useful for processors which define layer fragments which instantiate Java objects from the annotated code.
     * <p>While you can pick a specific instance file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param builder a builder to add a file to
     * @param annotationTarget an annotated {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
     * @param path path to folder of instance file, e.g. {@code "Menu/File"}
     * @param name instance file basename, e.g. {@code "my-menu-Item"}, or null to pick a name according to the element
     * @param type a type to which the instance ought to be assignable, or null to skip this check
     * @return an instance file (call {@link LayerBuilder.File#write} to finalize)
     * @throws IllegalArgumentException if the annotationTarget is not of a suitable sort
     *                                  (detail message can be reported as a {@link Kind#ERROR})
     */
    protected final LayerBuilder.File instanceFile(LayerBuilder builder, Element annotationTarget,
            String path, String name, Class type) throws IllegalArgumentException {
        String clazz, method;
        TypeMirror typeMirror = type != null ? processingEnv.getElementUtils().getTypeElement(type.getName()).asType() : null;
        switch (annotationTarget.getKind()) {
            case CLASS: {
                clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) annotationTarget).toString();
                method = null;
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
                break;
            }
            case METHOD: {
                clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) annotationTarget.getEnclosingElement()).toString();
                method = annotationTarget.getSimpleName().toString();
                if (!annotationTarget.getModifiers().contains(Modifier.STATIC)) {
                    throw new IllegalArgumentException(clazz + "." + method + " must be static");
                }
                if (!((ExecutableElement) annotationTarget).getParameters().isEmpty()) {
                    throw new IllegalArgumentException(clazz + "." + method + " must not take arguments");
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement) annotationTarget).getReturnType(), typeMirror)) {
                    throw new IllegalArgumentException(clazz + "." + method + " is not assignable to " + typeMirror);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + annotationTarget);
        }
        String basename;
        if (name == null) {
            basename = clazz.replace('.', '-');
            if (method != null) {
                basename += "-" + method;
            }
        } else {
            basename = name;
        }
        LayerBuilder.File f = builder.file(path + "/" + basename + ".instance");
        if (method != null) {
            f.methodvalue("instanceCreate", clazz, method);
        } else if (name != null) {
            f.stringvalue("instanceClass", clazz);
        } // else name alone suffices
        return f;
    }

}
