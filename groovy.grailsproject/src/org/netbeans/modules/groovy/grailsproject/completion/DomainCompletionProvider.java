/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider.class)
public class DomainCompletionProvider extends DynamicCompletionProvider {

    private static final Map<MethodSignature, String> BASIC_METHODS = new HashMap<MethodSignature, String>();

    // FIXME move it to some resource file, check the grails version
    // 1.0.4
    static {
        String[] noParams = new String[] {};

        BASIC_METHODS.put(new MethodSignature("save", new String[] {"java.lang.Boolean"}), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("save", new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("save", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("merge", new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("merge", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("delete", noParams), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("delete", new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("refresh", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("discard", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("attach", noParams), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("isAttached", noParams), "java.lang.Boolean"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("getErrors", noParams), "org.springframework.validation.Errors"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("setErrors", new String[] {"org.springframework.validation.Errors"}), "org.springframework.validation.Errors"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("clearErrors", noParams), "org.springframework.validation.Errors"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("hasErrors", noParams), "java.lang.Boolean"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("validate", noParams), "java.lang.Boolean"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("delete", noParams), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("delete", new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("lock", noParams), "java.lang.Object"); // NOI18N
    }

    @Override
    public Map<FieldSignature, String> getFields(FileObject source, String sourceClassName, String className) {
        return Collections.emptyMap();
    }

    @Override
    public Map<MethodSignature, String> getMethods(FileObject source, String sourceClassName, String className) {
        Project project = FileOwnerQuery.getOwner(source);
        if (className.equals(sourceClassName) && project != null
                && project.getLookup().lookup(ControllerCompletionProvider.class) != null) {

            if (isDomain(source, project)) {
                return Collections.unmodifiableMap(BASIC_METHODS);
            }
        }
        return Collections.emptyMap();
    }


    private boolean isDomain(FileObject source, Project project) {
        return source.getParent().getName().equals("domain") // NOI18N
                    && source.getParent().getParent().getName().equals("grails-app") // NOI18N
                    && source.getParent().getParent().getParent().equals(project.getProjectDirectory());
    }
}
