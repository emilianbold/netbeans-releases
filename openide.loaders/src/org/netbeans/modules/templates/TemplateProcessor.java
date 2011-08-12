/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.templates;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TemplateProcessor extends LayerGeneratingProcessor {

    @Override public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(TemplateRegistration.class.getCanonicalName(), TemplateRegistrations.class.getCanonicalName()));
    }

    @Override protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(TemplateRegistration.class)) {
            process(e, e.getAnnotation(TemplateRegistration.class));
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(TemplateRegistrations.class)) {
            for (TemplateRegistration t : e.getAnnotation(TemplateRegistrations.class).value()) {
                process(e, t);
            }
        }
        return true;
    }

    private void process(Element e, TemplateRegistration t) throws LayerGenerationException {
        LayerBuilder builder = layer(e);
        String basename;
        if (!t.id().isEmpty()) {
            if (t.content().length > 0) {
                throw new LayerGenerationException("Cannot specify both id and content", e, processingEnv, t);
            }
            basename = t.id();
        } else if (t.content().length > 0) {
            basename = basename(t.content()[0]);
        } else {
            if (e.getKind() == ElementKind.CLASS) {
                basename = ((TypeElement) e).getQualifiedName().toString().replace('.', '-');
            } else if (e.getKind() == ElementKind.METHOD) {
                basename = ((TypeElement) e.getEnclosingElement()).getQualifiedName().toString().replace('.', '-') + '-' + e.getSimpleName();
            } else {
                throw new LayerGenerationException("cannot use @Template on a package without specifying content", e, processingEnv, t);
            }
        }
        String folder = "Templates/" + t.folder() + '/';
        LayerBuilder.File f = builder.file(folder + basename);
        f.boolvalue("template", true);
        f.position(t.position());
        if (!t.displayName().isEmpty()) {
            f.bundlevalue("displayName", t.displayName());
        }
        if (!t.iconBase().isEmpty()) {
            builder.validateResource(t.iconBase(), e, t, "iconBase", true);
            f.stringvalue("iconBase", t.iconBase());
        } else if (t.content().length == 0) {
            throw new LayerGenerationException("Must specify iconBase if content is not specified", e, processingEnv, t);
        }
        if (!t.description().isEmpty()) {
            f.urlvalue("instantiatingWizardURL", contentURI(e, t.description(), builder, t, "description"));
        }
        if (e.getKind() != ElementKind.PACKAGE) {
            f.instanceAttribute("instantiatingIterator", InstantiatingIterator.class);
        }
        if (t.content().length > 0) {
            f.url(contentURI(e, t.content()[0], builder, t, "content").toString());
            for (int i = 1; i < t.content().length; i++) {
                builder.file(folder + basename(t.content()[i])).url(contentURI(e, t.content()[i], builder, t, "content").toString()).position(0).write();
            }
        }
        if (!t.scriptEngine().isEmpty()) {
            f.stringvalue(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, t.scriptEngine());
        }
        if (t.category().length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String c : t.category()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(c);
            }
            f.stringvalue("templateCategory", sb.toString());
        }
        f.write();
    }

    private static String basename(String relativeResource) {
        return relativeResource.replaceFirst(".+/", "").replaceFirst("[.]template$", "");
    }

    private URI contentURI(Element e, String relativePath, LayerBuilder builder, TemplateRegistration t, String annotationMethod) throws LayerGenerationException {
        String path = LayerBuilder.absolutizeResource(e, relativePath);
        builder.validateResource(path, e, t, annotationMethod, false);
        try {
            return new URI("nbresloc", "/" + path, null).normalize();
        } catch (URISyntaxException x) {
            throw new LayerGenerationException("could not translate " + path, e, processingEnv, t);
        }
    }

}
