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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class CollectionRemove extends AbstractHint {

    private static final String  SUPPRESS_WARNING_KEY = "element-type-mismatch";
            static final String  WARN_FOR_CASTABLE_KEY = "warn-for-castable";
            static final boolean WARN_FOR_CASTABLE_DEFAULT = true;
    
    private final AtomicBoolean cancel = new AtomicBoolean();
    
    public CollectionRemove() {
        super(true, true, HintSeverity.WARNING, SUPPRESS_WARNING_KEY, "collection-remove"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(CollectionRemove.class, "DESC_CollectionRemove");
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath tp) {
        cancel.set(false);
        MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
        ExpressionTree select = mit.getMethodSelect();
        TreePath method = new TreePath(tp, select);
        Element el = info.getTrees().getElement(method);

        if (el == null || el.getKind() != ElementKind.METHOD)
            return null;

        String simpleName = el.getSimpleName().toString();
        ExecutableElement invoked = (ExecutableElement) el;
        TypeElement owner = (TypeElement) invoked.getEnclosingElement();
        Collection<MappingDefinition> mds = CHECKED_METHODS_MAP.get(simpleName);

        if (mds == null) {
            return null;
        }

        List<ErrorDescription> result = new LinkedList<ErrorDescription>();
        
        for (MappingDefinition md : mds) {
            if (cancel.get())
                return null;
            
            ExecutableElement toCheck = resolveMethod(info, md.checkMethod);

            if (toCheck == null || (invoked != toCheck && !info.getElements().overrides(invoked, toCheck, owner))) {
                continue;
            }

            ExecutableElement toCheckAgainst = resolveMethod(info, md.checkAgainstMethod);

            if (toCheckAgainst == null) {
                continue; //XXX: log?
            }

            DeclaredType site;

            OUTER: switch (select.getKind()) {
                case MEMBER_SELECT:
                    TypeMirror tm = info.getTrees().getTypeMirror(new TreePath(method, ((MemberSelectTree) select).getExpression()));
                    if (tm != null && tm.getKind() == TypeKind.TYPEVAR)
                        tm = ((TypeVariable) tm).getUpperBound();
                    if (tm != null && tm.getKind() == TypeKind.DECLARED)
                        site = (DeclaredType) tm;
                    else
                        site = null;
                    break;
                case IDENTIFIER:
                    Scope s = info.getTrees().getScope(tp);

                    while (s != null) {
                        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(s.getEnclosingClass()))) {
                            if (ee == toCheckAgainst || info.getElements().overrides(ee, toCheckAgainst, owner)) {
                                site = (DeclaredType) s.getEnclosingClass().asType();
                                break OUTER;
                            }
                        }
                        s = s.getEnclosingScope();
                    }

                    site = null;
                    break;
                default:
                    throw new IllegalStateException();
            }

            if (site == null) {
                continue; //XXX: log
            }

            ExecutableType againstType = (ExecutableType) info.getTypes().asMemberOf(site, toCheckAgainst);

            for (Entry<Integer,Integer> e : md.parametersMapping.entrySet()) {
                TypeMirror actualParam = info.getTrees().getTypeMirror(new TreePath(tp, mit.getArguments().get(e.getKey())));
                TypeMirror designedType = org.netbeans.modules.java.hints.errors.Utilities.resolveCapturedType(info, againstType.getParameterTypes().get(e.getValue()));

                if (designedType.getKind() == TypeKind.WILDCARD) {
                    designedType = ((WildcardType) designedType).getExtendsBound();

                    if (designedType == null) {
                        designedType = info.getElements().getTypeElement("java.lang.Object").asType();
                    }
                }

                if (!info.getTypes().isAssignable(actualParam,designedType)) {
                    String warningKey;

                    if (compatibleTypes(info, actualParam,designedType)) {
                        warningKey = "HINT_SuspiciousCall"; //NOI18N

                        if (!getPreferences(null).getBoolean(WARN_FOR_CASTABLE_KEY, WARN_FOR_CASTABLE_DEFAULT)) {
                            continue;
                        }
                    } else {
                        warningKey = "HINT_SuspiciousCallIncompatibleTypes"; //NOI18N
                    }
                    
                    String semiFQN = md.checkMethod.substring(0, md.checkMethod.indexOf('('));

                    String warning = NbBundle.getMessage(CollectionRemove.class,
                                                         warningKey,
                                                         semiFQN,
                                                         Utilities.getTypeName(actualParam, false),
                                                         Utilities.getTypeName(designedType, false));

                    int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), mit);
                    int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), mit);
                    List<Fix> fixes = FixFactory.createSuppressWarnings(info, tp, SUPPRESS_WARNING_KEY);

                    result.add(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), warning, fixes, info.getFileObject(), start, end));
                }
            }
        }

        return result;
    }

    private static boolean compatibleTypes(CompilationInfo info, TypeMirror type1, TypeMirror type2) {
        type1 = info.getTypes().erasure(type1);
        type2 = info.getTypes().erasure(type2);

        return info.getTypeUtilities().isCastable(type1, type2);
    }

    private static final Pattern SPLIT = Pattern.compile("(.+)\\.([^.]+)\\((.*)\\)");
    
    private static ExecutableElement resolveMethod(CompilationInfo info, String name) {
        Matcher m = SPLIT.matcher(name);

        if (!m.matches()) {
            throw new IllegalArgumentException();
        }

        String className = m.group(1);
        String methodName = m.group(2);
        String paramsSpec = m.group(3);

        TypeElement te = info.getElements().getTypeElement(className);

        if (te == null) {
            return null;
        }

        String[] paramList = paramsSpec.split(",");
        List<TypeMirror> params = new LinkedList<TypeMirror>();

        TypeElement topLevel = info.getTopLevelElements().get(0);

        for (String t : paramList) {
            params.add(info.getTreeUtilities().parseType(t, topLevel));
        }

        for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
            if (!methodName.equals(ee.getSimpleName().toString()) || ee.getParameters().size() != params.size()) {
                continue;
            }
            
            Iterator<TypeMirror> designed = params.iterator();
            boolean found = true;

            for (VariableElement param : ee.getParameters()) {
                if (!info.getTypes().isSameType(info.getTypes().erasure(param.asType()), designed.next())) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return ee;
            }
        }
        
        return null;
    }

    public String getId() {
        return CollectionRemove.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CollectionRemove.class, "DN_CollectionRemove");
    }

    public void cancel() {
        cancel.set(true);
    }

    @Override
    public JComponent getCustomizer(final Preferences node) {
        return new CollectionRemoveCustomizer(node);
    }

    private static final Iterable<MappingDefinition> MAPPINGS = Arrays.asList(
            new MappingDefinition("java.util.Collection.remove(java.lang.Object)", "java.util.Collection.add(java.lang.Object)", 0, 0), //NOI18N
            new MappingDefinition("java.util.Collection.contains(java.lang.Object)", "java.util.Collection.add(java.lang.Object)", 0, 0), //NOI18N
            new MappingDefinition("java.util.Map.containsKey(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 0), //NOI18N
            new MappingDefinition("java.util.Map.get(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 0), //NOI18N
            new MappingDefinition("java.util.Map.remove(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 0), //NOI18N
            new MappingDefinition("java.util.Map.containsValue(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 1) //NOI18N
    );

    private static final Map<String, Collection<MappingDefinition>> CHECKED_METHODS_MAP;

    static {
        CHECKED_METHODS_MAP = new HashMap<String, Collection<MappingDefinition>>();
        
        for (MappingDefinition d : MAPPINGS) {
            Matcher m = SPLIT.matcher(d.checkMethod);

            if (!m.matches()) {
                throw new IllegalArgumentException();
            }

            String methodName = m.group(2);
            Collection<MappingDefinition> c = CHECKED_METHODS_MAP.get(methodName);

            if (c == null) {
                CHECKED_METHODS_MAP.put(methodName, c = new LinkedList<MappingDefinition>());
            }

            c.add(d);
        }
    }

    private static final class MappingDefinition {

        private final String checkMethod;
        private final String checkAgainstMethod;
        private final Map<Integer, Integer> parametersMapping;

        public MappingDefinition(String checkMethod, String checkAgainstMethod, int... parametersMapping) {
            this.checkMethod = checkMethod;
            this.checkAgainstMethod = checkAgainstMethod;

            assert parametersMapping.length % 2 == 0;
            
            this.parametersMapping = new HashMap<Integer, Integer>();

            for (int cntr = 0; cntr < parametersMapping.length; cntr += 2) {
                this.parametersMapping.put(parametersMapping[cntr], parametersMapping[cntr + 1]);
            }
        }

    }

}
