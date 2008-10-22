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

package org.netbeans.modules.groovy.editor.completion;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.editor.api.elements.IndexedMethod;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;

/**
 *
 * @author Petr Hejl
 */
public final class GroovyElementHandler {

    private static final Logger LOGGER = Logger.getLogger(GroovyElementHandler.class.getName());

    private final CompilationInfo info;

    private GroovyElementHandler(CompilationInfo info) {
        this.info = info;
    }

    public static GroovyElementHandler forCompilationInfo(CompilationInfo info) {
        return new GroovyElementHandler(info);
    }

    public Map<MethodSignature, ? extends CompletionItem> getMethods(String className,
            String prefix, int anchor) {

        GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));

        if (index == null) {
            return Collections.emptyMap();
        }

        String methodName = "";
        if (prefix != null) {
            methodName = prefix;
        }

        Set<IndexedMethod> methods;

        if (methodName.equals("")) {
            methods = index.getMethods(".*", className,
                    NameKind.REGEXP, EnumSet.allOf(SearchScope.class));
        } else {
            methods = index.getMethods(methodName, className,
                    NameKind.PREFIX, EnumSet.allOf(SearchScope.class));
        }

        if (methods.size() == 0) {
            LOGGER.log(Level.FINEST, "Nothing found in GroovyIndex");
            return Collections.emptyMap();
        }

        LOGGER.log(Level.FINEST, "Found this number of methods : {0} ", methods.size());

        Map<MethodSignature, CompletionItem.JavaMethodItem> result = new HashMap<MethodSignature, CompletionItem.JavaMethodItem>();
        for (IndexedMethod indexedMethod : methods) {
            LOGGER.log(Level.FINEST, "method from index : {0} ", indexedMethod.getName());

            // FIXME move sig to method item
            List<String> params = indexedMethod.getParameters();
            StringBuffer sb = new StringBuffer();

            for (String string : params) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(NbUtilities.stripPackage(string));
            }

            // FIXME what is this intended to do ? + modifiers
            result.put(getSignature(indexedMethod), new CompletionItem.JavaMethodItem(indexedMethod.getName(), sb.toString(), null,
                    org.netbeans.modules.groovy.editor.java.Utilities.gsfModifiersToModel(indexedMethod.getModifiers(), Modifier.PUBLIC), anchor));
        }

        return result;
    }

    private MethodSignature getSignature(IndexedMethod method) {
        String[] parameters = method.getParameters().toArray(new String[method.getParameters().size()]);
        return new MethodSignature(method.getName(), parameters);
    }
}
