/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.openide.filesystems.declmime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MIMEResolverProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(MIMEResolver.Registration.class.getCanonicalName());
        return set;
    }
    
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(MIMEResolver.Registration.class)) {
            MIMEResolver.Registration r = e.getAnnotation(MIMEResolver.Registration.class);
            String absRes = LayerBuilder.absolutizeResource(e, r.value());
            final LayerBuilder b = layer(e);
            FileObject fo = b.validateResource(absRes, e, r, null, false);
            File f = b.instanceFile("Services/MIMEResolver", null, MIMEResolver.class);
            try {
                final InputStream is = fo.openInputStream();
                generateInstanceResolver(f, is);
                is.close();
            } catch (IOException ex) {
                final LayerGenerationException le = new LayerGenerationException("Cannot process " + absRes, e);
                le.initCause(ex);
                throw le;
            }
        }
        return true;
    }

    private void generateInstanceResolver(File f, InputStream is) throws IOException {
        org.openide.filesystems.FileObject tmp = FileUtil.createMemoryFileSystem().getRoot().createData("resolver.xml");
        OutputStream os = tmp.getOutputStream();
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            os.write(ch);
        }
        os.close();
        
    }
    
}
