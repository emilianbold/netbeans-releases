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

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.elements.IndexedField;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public final class DynamicElementHandler {

    private static final Logger LOGGER = Logger.getLogger(DynamicElementHandler.class.getName());

    private final ParserResult info;

    private DynamicElementHandler(ParserResult info) {
        this.info = info;
    }

    public static DynamicElementHandler forCompilationInfo(ParserResult info) {
        return new DynamicElementHandler(info);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    // FIXME SPI to plug here for Grails dynamic methods
    public Map<MethodSignature, ? extends CompletionItem> getMethods(String sourceClassName,
            String className, String prefix, int anchor, boolean nameOnly, boolean leaf, FileObject classSource) {

        if (info.getSnapshot().getSource().getFileObject() == null) {
            return Collections.emptyMap();
        }

        CompletionContext context = new CompletionContext(anchor, classSource,
                sourceClassName, className, prefix, false, getProperties(className), leaf, nameOnly);

        Map<MethodSignature, CompletionItem> resultDynamic =
                new HashMap<MethodSignature, CompletionItem>();

        for (DynamicCompletionProvider provider : Lookup.getDefault().lookupAll(DynamicCompletionProvider.class)) {
            for (Map.Entry<MethodSignature, CompletionItem> entry : provider.getMethods(context).entrySet()) {
                if (entry.getKey().getName().startsWith(prefix)) {
                    resultDynamic.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return resultDynamic;
    }

    public Map<FieldSignature, ? extends CompletionItem> getFields(String sourceClassName,
            String className, String prefix, int anchor, boolean leaf, FileObject classSource) {

        if (info.getSnapshot().getSource().getFileObject() == null) {
            return Collections.emptyMap();
        }
        
        CompletionContext context = new CompletionContext(anchor, classSource,
                sourceClassName, className, prefix, false, getProperties(className), leaf, false);

        Map<FieldSignature, CompletionItem> resultDynamic =
                new HashMap<FieldSignature, CompletionItem>();

        for (DynamicCompletionProvider provider : Lookup.getDefault().lookupAll(DynamicCompletionProvider.class)) {
            for (Map.Entry<FieldSignature, CompletionItem> entry : provider.getFields(context).entrySet()) {
                if (entry.getKey().getName().startsWith(prefix)) {
                    resultDynamic.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return resultDynamic;
    }

    private List<String> getProperties(String className) {

        FileObject f = info.getSnapshot().getSource().getFileObject();
        if (f == null) {
            return Collections.<String>emptyList();
        }

        GroovyIndex index = GroovyIndex.get(QuerySupport.findRoots(f,
                Collections.singleton(ClassPath.SOURCE), Collections.<String>emptySet(), Collections.<String>emptySet()));

        Set<IndexedField> fields = index.getFields(".*", className, QuerySupport.Kind.REGEXP);

        if (fields.size() == 0) {
            LOGGER.log(Level.FINEST, "Nothing found in GroovyIndex");
            return Collections.emptyList();
        }

        LOGGER.log(Level.FINEST, "Found this number of fields : {0} ", fields.size());

        List<String> result = new ArrayList<String>();
        for (IndexedField indexedField : fields) {
            LOGGER.log(Level.FINEST, "field from index : {0} ", indexedField.getName());

            // FIXME perhaps we want gather even static props - in such
            // case we would need more than just string
            if (!indexedField.isStatic() && indexedField.isProperty()) {
                result.add(indexedField.getName());
            }
        }

        return result;
    }
}
