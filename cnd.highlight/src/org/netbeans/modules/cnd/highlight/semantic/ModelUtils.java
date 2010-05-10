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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 *
 * @author Sergey Grinev
 */
public class ModelUtils {

    public static final int HIGHLIGHT_DELAY = getInt("cnd.reparce.delay", 500); // NOI18N
    public static final int RESCHEDULE_HIGHLIGHT_DELAY = getInt("cnd.reschedule.task.delay", 500); // NOI18N

    public static final int OCCURRENCES_DELAY = getInt("cnd.reparce.delay", 300); // NOI18N
    public static final int RESCHEDULE_OCCURRENCES_DELAY = getInt("cnd.reschedule.task.delay", 300); // NOI18N

    public static final int SEMANTIC_DELAY = getInt("cnd.reparce.delay", 500); // NOI18N
    public static final int RESCHEDULE_SEMANTIC_DELAY = getInt("cnd.reschedule.task.delay", 500); // NOI18N

    private ModelUtils() {
    }

    private static int getInt(String name, int result){
        String text = System.getProperty(name);
        if( text != null ) {
            try {
                result = Integer.parseInt(text);
            } catch(NumberFormatException e){
                // default value
            }
        }
        return result;
    }


    /*package*/ static List<CsmReference> collect(final CsmFile csmFile, final ReferenceCollector collector) {
        CsmFileReferences.getDefault().accept(csmFile, new CsmFileReferences.Visitor() {
            @Override
                public void visit(CsmReferenceContext context) {
                    collector.visit(context.getReference(), csmFile);
                }
        });
        return collector.getReferences();
    }

    /*package*/ static List<CsmOffsetable> getInactiveCodeBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file);
    }

    /*package*/ static List<CsmReference> getMacroBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getMacroUsages(file);
    }

    private static abstract class AbstractReferenceCollector implements ReferenceCollector {
        protected final List<CsmReference> list;
        public AbstractReferenceCollector() {
            list = new ArrayList<CsmReference>();
        }
        @Override
        public List<CsmReference> getReferences() {
            return list;
        }
    }

    /*package*/ static class FieldReferenceCollector extends AbstractReferenceCollector {
        public String getEntityName() {
            return "class-fields"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            CsmObject obj = ref.getReferencedObject();
            if (CsmKindUtilities.isField(obj)) {
                list.add(ref);
            }
        }
    }

    /*package*/ static class TypedefReferenceCollector extends AbstractReferenceCollector {
        public String getEntityName() {
            return "typedefs"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            CsmObject obj = ref.getReferencedObject();
            if (CsmKindUtilities.isTypedef(obj)) {
                list.add(ref);
            }
        }
    }
    /*package*/ static class FunctionReferenceCollector extends AbstractReferenceCollector {
        public String getEntityName() {
            return "functions-names"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            if (isWanted(ref, file)) {
                list.add(ref);
            }
        }
        private boolean isWanted(CsmReference ref, CsmFile file) {
            CsmObject csmObject = ref.getReferencedObject();
            return CsmKindUtilities.isFunction(csmObject);
        }
    }

    /*package*/ static class UnusedVariableCollector implements ReferenceCollector {
        private final Map<CsmUID, ReferenceCounter> counters;
        private Set<CsmUID> parameters;
        public UnusedVariableCollector() {
            counters = new LinkedHashMap<CsmUID, ReferenceCounter>();
        }
        public String getEntityName() {
            return "unused-variables"; // NOI18N
        }
        @Override
        public void visit(CsmReference ref, CsmFile file) {
            CsmObject obj = ref.getReferencedObject();
            if (isWanted(obj, file)) {
                CsmUID uid = UIDs.get(obj);
                ReferenceCounter counter = counters.get(uid);
                if (counter == null) {
                    counter = new ReferenceCounter(ref);
                    counters.put(uid, counter);
                } else {
                    counter.increment();
                }
            }
        }
        @Override
        public List<CsmReference> getReferences() {
            List<CsmReference> result = new ArrayList<CsmReference>();
            for (ReferenceCounter counter : counters.values()) {
                if (counter.getCount() == 1) {
                    result.add(counter.getFirstReference());
                }
            }
            return result;
        }
        private boolean isWanted(CsmObject obj, CsmFile file) {
            if (!CsmKindUtilities.isLocalVariable(obj)) {
                // we want only local variables ...
                return false;
            }
            CsmVariable var = (CsmVariable)obj;
            if (!var.getContainingFile().equals(file)) {
                // ... only from current file
                return false;
            }
            if (CsmKindUtilities.isParameter(obj)) {
                Set<CsmUID> set = getFunctionDefinitionParameters(file);
                return set.contains(UIDs.get(var));
            } else {
                return true;
            }
        }
        private Set<CsmUID> getFunctionDefinitionParameters(CsmFile file) {
            if (parameters == null) {
                parameters = new HashSet<CsmUID>();
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.FUNCTION_DEFINITION, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
                Iterator<CsmOffsetableDeclaration> i = CsmSelect.getDeclarations(file, filter);
                while (i.hasNext()) {
                    CsmFunctionDefinition fundef = (CsmFunctionDefinition)i.next();
                    for (Object obj : fundef.getParameters()) {
                        parameters.add(UIDs.get(obj));
                    }
                }
            }
            return parameters;
        }
    }

    private static class ReferenceCounter {

        private CsmReference reference;
        private int count;

        public ReferenceCounter(CsmReference reference) {
            this.reference = reference;
            this.count = 1;
        }

        public CsmReference getFirstReference() {
            return reference;
        }

        public int getCount() {
            return count;
        }

        public void increment() {
            ++count;
            reference = null;
        }

    }

}
