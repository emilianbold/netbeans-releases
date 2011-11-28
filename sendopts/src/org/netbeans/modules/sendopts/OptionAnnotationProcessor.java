/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.sendopts;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.spi.sendopts.annotations.Description;
import org.netbeans.spi.sendopts.annotations.Option;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@ServiceProvider(service=Processor.class)
public final class OptionAnnotationProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(Option.class.getName());
        return set;
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        PrimitiveType boolType = processingEnv.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN);
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        ArrayType stringArray = processingEnv.getTypeUtils().getArrayType(stringType);
        int cnt = 0;
        for (Element e : roundEnv.getElementsAnnotatedWith(Option.class)) {
            Option o = e.getAnnotation(Option.class);
            Description d = e.getAnnotation(Description.class);
            
            File f;
            f = layer(e).file("Services/OptionProcessors/" + processingEnv.getElementUtils().getBinaryName((TypeElement)e.getEnclosingElement()).toString().replace('.', '-') + ".instance");
            f.methodvalue("instanceCreate", DefaultProcessor.class.getName(), "create");
            f.stringvalue("field", e.getSimpleName().toString());
            f.stringvalue("class", processingEnv.getElementUtils().getBinaryName((TypeElement)e.getEnclosingElement()).toString());
            f.charvalue("shortName", o.shortName());
            f.stringvalue("longName", o.longName());
            if (boolType == e.asType()) {
                f.stringvalue("type", "withoutArgument");
            } else if (stringType == e.asType()) {
                f.stringvalue("type", "requiredArgument");
            } else {
                
                if (!stringArray.equals(e.asType())) {
                    throw new LayerGenerationException("Field type has to be either boolean, String or String[]!", e);
                }
                f.stringvalue("type", "additionalArguments");
            }
            if (d != null) {
                f.bundlevalue("displayName", d.displayName());
                f.bundlevalue("shortDescription", d.shortDescription());
            }
            f.write();
            
            cnt++;
        }
        return true;
    }

}
