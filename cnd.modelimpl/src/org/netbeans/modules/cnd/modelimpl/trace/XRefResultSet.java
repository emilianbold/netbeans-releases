/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vladimir Voskresenky
 */
public final class XRefResultSet {

    public static Collection<ContextScope> sortedContextScopes(XRefResultSet bag, boolean byEntries) {
        List<ContextScope> out = new ArrayList(ContextScope.values().length);
        for (ContextScope scope : ContextScope.values()) {
            boolean added = false;
            int scopeNum;
            if (byEntries) {
                scopeNum = bag.getEntries(scope).size();
            } else {
                scopeNum = bag.getNumberOfContexts(scope, false);
            }
            for (int i = 0; i < out.size(); i++) {
                int curScopeNum;
                ContextScope curScope = out.get(i);
                if (byEntries) {
                    curScopeNum = bag.getEntries(curScope).size();
                } else {
                    curScopeNum = bag.getNumberOfContexts(curScope, false);
                }                
                if (curScopeNum <= scopeNum) {
                    out.add(i, scope);
                    added = true;
                    break;
                }
            }
            if (!added) {
                out.add(scope);
            }
        }

        return out;
    }

    private final Map<ContextScope, Collection<ContextEntry>> scopeEntries;
    private final Map<ContextScope, Integer> scopes;
    private final Map<CharSequence, Object> unresolved;

    public XRefResultSet() {
        scopeEntries = new HashMap<ContextScope, Collection<ContextEntry>>(ContextScope.values().length);
        scopes = new HashMap<ContextScope, Integer>(ContextScope.values().length);
        unresolved = new HashMap<CharSequence, Object>(100);
        for (ContextScope scopeContext : ContextScope.values()) {
            scopeEntries.put(scopeContext, new ArrayList<ContextEntry>(1024));
            scopes.put(scopeContext, new Integer(0));
        }
    }
    
    public final void addEntry(ContextScope contextScope, ContextEntry entry) {
        scopeEntries.get(contextScope).add(entry);
    }
    
    public final Collection<ContextEntry> getEntries(ContextScope contextScope) {
        return scopeEntries.get(contextScope);
    }
    
    public final void incrementScopeCounter(ContextScope contextScope) {
        int val = scopes.get(contextScope);
        scopes.put(contextScope, ++val);
    }
    
    public final int getNumberOfAllContexts() {
        int out = 0;
        for (int val : scopes.values()) {
            out += val;
        }
        return out;
    }

    public final int getNumberOfContexts(ContextScope contextScope, boolean relative) {
        int num = scopes.get(contextScope);
        if (relative && (num != 0)) {
            assert num > 0;
            num = (num * 100) / getNumberOfAllContexts();
        }
        return num;
    }

    public <T>T getUnresolvedEntry(CharSequence name) {
        return (T)unresolved.get(name);
    }
    
    public <T> void addUnresolvedEntry(CharSequence name, T value) {
        unresolved.put(name, value);
    }
    
    public <T> Collection<T> getUnresolvedEntries(Comparator<? super T> comparator) {
        List out = new ArrayList(unresolved.values());
        Collections.sort(out, comparator);
        return out;
    }
            
    public enum ContextScope {
        GLOBAL_FUNCTION,
        NAMESPACE_FUNCTION,
        FILE_LOCAL_FUNCTION,
        METHOD,
        CONSTRUCTOR,
        INLINED_METHOD,
        INLINED_CONSTRUCTOR,
        UNRESOLVED,
    };
    
    public enum DeclarationKind {
        CLASSIFIER,
        ENUMERATOR,
        VARIABLE,
        PARAMETER,
        FUNCTION,
        NAMESPACE,
        CLASS_FORWARD,
        MACRO,
        UNRESOLVED,
    }
    
    public enum DeclarationScope {
        FUNCTION_THIS,
        CLASSIFIER_THIS,
        CLASSIFIER_PARENT,
        PROJECT_CLASSIFIER,
        LIBRARY_CLASSIFIER,
        NAMESPACE_THIS,
        NAMESPACE_PARENT,
        PROJECT_NAMESPACE,
        LIBRARY_NAMESPACE,
        FILE_THIS,
        PROJECT_FILE,
        LIBRARY_FILE,
        PROJECT_GLOBAL,
        LIBRARY_GLOBAL,
        UNRESOLVED,
    }
    
    public enum IncludeLevel {
        THIS_FILE,
        PROJECT_DIRECT,
        LIBRARY_DIRECT,
        PROJECT_DEEP,
        LIBRARY_DEEP,
        UNRESOLVED,
    }
    
    public enum UsageStatistics {
        FIRST_USAGE,
        SECOND_USAGE,
        NEXT_USAGE,
        UNKNOWN,
    }
    
    public static final class ContextEntry {
        public final DeclarationKind declaration;
        public final DeclarationScope declarationScope;
        public final IncludeLevel declarationIncludeLevel;
        public final UsageStatistics usageStatistics;
        
        public final static ContextEntry UNRESOLVED = new ContextEntry(DeclarationKind.UNRESOLVED, DeclarationScope.UNRESOLVED, 
                IncludeLevel.UNRESOLVED, UsageStatistics.UNKNOWN);
        public final static ContextEntry UNRESOLVED_AFTER_UNRESOLVED = new ContextEntry(DeclarationKind.UNRESOLVED, DeclarationScope.UNRESOLVED,
                IncludeLevel.UNRESOLVED, UsageStatistics.UNKNOWN);
        public final static ContextEntry UNRESOLVED_TEMPLATE_BASED = new ContextEntry(DeclarationKind.UNRESOLVED, DeclarationScope.UNRESOLVED, 
                IncludeLevel.UNRESOLVED, UsageStatistics.UNKNOWN);    
        public final static ContextEntry UNRESOLVED_MACRO_BASED = new ContextEntry(DeclarationKind.UNRESOLVED, DeclarationScope.UNRESOLVED, 
                IncludeLevel.UNRESOLVED, UsageStatistics.UNKNOWN);         
        public final static ContextEntry RESOLVED = new ContextEntry(DeclarationKind.UNRESOLVED, DeclarationScope.UNRESOLVED,
                IncludeLevel.UNRESOLVED, UsageStatistics.UNKNOWN);
        
        public ContextEntry(DeclarationKind declaration, DeclarationScope declarationScope, 
                IncludeLevel declarationIncludeLevel, UsageStatistics usageStatistics) {
            this.declaration = declaration;
            this.declarationScope = declarationScope;
            this.declarationIncludeLevel = declarationIncludeLevel;
            this.usageStatistics = usageStatistics;
        }
    }
    
}
