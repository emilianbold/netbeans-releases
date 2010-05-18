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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion;

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.api.elements.IndexedField;
import org.netbeans.modules.groovy.editor.api.elements.IndexedMethod;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;

/**
 *
 * @author Petr Hejl
 */
public final class GroovyElementHandler {

    private static final Logger LOGGER = Logger.getLogger(GroovyElementHandler.class.getName());

    private final ParserResult info;

    private GroovyElementHandler(ParserResult info) {
        this.info = info;
    }

    public static GroovyElementHandler forCompilationInfo(ParserResult info) {
        return new GroovyElementHandler(info);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    public Map<MethodSignature, ? extends CompletionItem> getMethods(GroovyIndex index, String className,
            String prefix, int anchor, boolean emphasise, Set<AccessLevel> levels, boolean nameOnly) {

        if (index == null) {
            return Collections.emptyMap();
        }

        String methodName = "";
        if (prefix != null) {
            methodName = prefix;
        }

        Set<IndexedMethod> methods;

        if (methodName.equals("")) {
            methods = index.getMethods(".*", className, QuerySupport.Kind.REGEXP);
        } else {
            methods = index.getMethods(methodName, className, QuerySupport.Kind.PREFIX);
        }

        if (methods.size() == 0) {
            LOGGER.log(Level.FINEST, "Nothing found in GroovyIndex");
            return Collections.emptyMap();
        }

        LOGGER.log(Level.FINEST, "Found this number of methods : {0} ", methods.size());

        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        for (IndexedMethod indexedMethod : methods) {
            LOGGER.log(Level.FINEST, "method from index : {0} ", indexedMethod.getName());

            if (!accept(levels, indexedMethod)) {
                continue;
            }

            // FIXME move sig to method item
            List<String> params = indexedMethod.getParameters();
            StringBuffer sb = new StringBuffer();

            for (String string : params) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(NbUtilities.stripPackage(string));
            }

            // FIXME return type
            result.put(getSignature(indexedMethod), CompletionItem.forJavaMethod(className, indexedMethod.getName(), sb.toString(), indexedMethod.getReturnType(),
                    org.netbeans.modules.groovy.editor.java.Utilities.gsfModifiersToModel(indexedMethod.getModifiers(), Modifier.PUBLIC), anchor, emphasise, nameOnly));
        }

        return result;
    }

    public Map<FieldSignature, ? extends CompletionItem> getFields(GroovyIndex index, String className,
            String prefix, int anchor, boolean emphasise) {

        if (index == null) {
            return Collections.emptyMap();
        }

        String methodName = "";
        if (prefix != null) {
            methodName = prefix;
        }

        Set<IndexedField> fields;

        if (methodName.equals("")) {
            fields = index.getFields(".*", className, QuerySupport.Kind.REGEXP);
        } else {
            fields = index.getFields(methodName, className, QuerySupport.Kind.PREFIX);
        }

        if (fields.size() == 0) {
            LOGGER.log(Level.FINEST, "Nothing found in GroovyIndex");
            return Collections.emptyMap();
        }

        LOGGER.log(Level.FINEST, "Found this number of fields : {0} ", fields.size());

        Map<FieldSignature, CompletionItem.JavaFieldItem> result = new HashMap<FieldSignature, CompletionItem.JavaFieldItem>();
        for (IndexedField indexedField : fields) {
            LOGGER.log(Level.FINEST, "field from index : {0} ", indexedField.getName());

            //System.out.println("INDEX: " + indexedField.getName() + " " + indexedField.getType() + " " + org.netbeans.modules.groovy.editor.java.Utilities.gsfModifiersToModel(indexedField.getModifiers(), Modifier.PRIVATE));
            result.put(getSignature(indexedField), new CompletionItem.JavaFieldItem(
                    className, indexedField.getName(), null, org.netbeans.modules.groovy.editor.java.Utilities.gsfModifiersToModel(indexedField.getModifiers(), Modifier.PRIVATE), anchor, emphasise));
        }

        return result;
    }

    private MethodSignature getSignature(IndexedMethod method) {
        String[] parameters = method.getParameters().toArray(new String[method.getParameters().size()]);
        return new MethodSignature(method.getName(), parameters);
    }

    private FieldSignature getSignature(IndexedField field) {
        return new FieldSignature(field.getName());
    }

    private boolean accept(Set<AccessLevel> levels, IndexedElement element) {
        for (AccessLevel level : levels) {
            if (level.accept(element.getModifiers())) {
                return true;
            }
        }

        return false;
    }
}
