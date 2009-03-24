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
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.zip.CRC32;
import javax.swing.JComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
//TODO: indexed class IndexedClass must return interfaces
public class ImplementAbstractMethods extends ModelRule {
    Map<Integer,CachedFixInfo> cahcedFixedInfo = new HashMap<Integer, CachedFixInfo>();
    long diggest;

    public String getId() {
        return "Implement.Abstract.Methods";//NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ImplementAbstractMethods.class, "ImplementAbstractMethodsDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ImplementAbstractMethods.class, "ImplementAbstractMethodsDispName");//NOI18N
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }


    public boolean showInTasklist() {
        return false;
    }

    @Override
    void check(FileScope modelScope, RuleContext context, List<Hint> hints) {
        Collection<? extends TypeScope> allClasses = modelScope.getDeclaredTypes();
        long computedDigest = computeDigest(allClasses);
        if (computedDigest != diggest || !cahcedFixedInfo.isEmpty()) {            
            fillCachedFixedInfo(allClasses, computedDigest != diggest);
        }
        diggest = computedDigest;
        for (CachedFixInfo fixInfo : cahcedFixedInfo.values()) {
            hints.add(new Hint(ImplementAbstractMethods.this, getDisplayName(),
                    context.parserResult.getSnapshot().getSource().getFileObject(), fixInfo.classNameRange,
                    Collections.<HintFix>singletonList(new Fix(context,
                    fixInfo)), 500));
        }
    }

    private long computeDigest(Collection<? extends TypeScope> allTypes) {
        CRC32 crc32 = new CRC32();
        for (TypeScope typeScope : allTypes) {
            crc32.update(String.valueOf(typeScope.hashCode()).getBytes());
            crc32.update(String.valueOf(typeScope.getPhpModifiers().hashCode()).getBytes());
            Collection<? extends MethodScope> allMethods = typeScope.getDeclaredMethods();
            for (MethodScope methodScope : allMethods) {
                crc32.update(String.valueOf(methodScope.hashCode()).getBytes());
            }
        }
        return crc32.getValue();
    }

    private void fillCachedFixedInfo(Collection<? extends TypeScope> allTypes, boolean changed) {
        if (!changed && !cahcedFixedInfo.isEmpty()) {
            for (TypeScope classScope : allTypes) {
                CachedFixInfo fixInfo = cahcedFixedInfo.get(classScope.hashCode());
                if (fixInfo != null) {
                    fixInfo.setClassScope(classScope);
                }
            }
        } else {
            cahcedFixedInfo.clear();
            for (TypeScope typeScope : allTypes) {
                LinkedHashSet<MethodScope> abstrMethods = new LinkedHashSet<MethodScope>();
                ClassScope cls = (typeScope instanceof ClassScope) ? ModelUtils.getFirst(((ClassScope)typeScope).getSuperClasses()) : null;
                Collection<? extends InterfaceScope> interfaces = typeScope.getSuperInterfaces();
                if ((cls != null  || interfaces.size() > 0) && !typeScope.getPhpModifiers().isAbstract() && typeScope instanceof ClassScope) {
                    Set<String> methNames = new HashSet<String>();
                    Collection<? extends MethodScope> allInheritedMethods = typeScope.getMethods();
                    Collection<? extends MethodScope> allMethods = typeScope.getDeclaredMethods();
                    Set<String> methodNames = new HashSet<String>();
                    for (MethodScope methodScope : allMethods) {
                        methodNames.add(methodScope.getName());
                    }
                    for (MethodScope methodScope : allInheritedMethods) {
                        Scope inScope = methodScope.getInScope();
                        if (inScope instanceof InterfaceScope || methodScope.getPhpModifiers().isAbstract()) {                            
                            if (!methodNames.contains(methodScope.getName())) {
                                abstrMethods.add(methodScope);
                            }
                        } else {
                            methNames.add(methodScope.getName());
                        }
                    }
                    for (Iterator<? extends MethodScope> it = abstrMethods.iterator(); it.hasNext();) {
                        MethodScope methodScope = it.next();
                        if (methNames.contains(methodScope.getName())) {
                            it.remove();
                        }
                    }
                }
                if (!abstrMethods.isEmpty()) {
                    LinkedHashSet<String> methodSkeletons = new LinkedHashSet<String>();
                    for (MethodScope methodScope : abstrMethods) {
                        String skeleton = methodScope.getClassSkeleton();
                        skeleton = skeleton.replace("abstract ", ""); //NOI18N
                        methodSkeletons.add(skeleton);
                    }
                    CachedFixInfo fixInfo = new CachedFixInfo(typeScope, methodSkeletons);
                    cahcedFixedInfo.put(typeScope.hashCode(), fixInfo);
                }
            }
        }
    }

    private class Fix implements HintFix {

        private RuleContext context;
        private final CachedFixInfo fixInfo;

        Fix(RuleContext context, CachedFixInfo fixInfo) {
            this.context = context;
            this.fixInfo = fixInfo;
        }

        public String getDescription() {
            return ImplementAbstractMethods.this.getDescription();
        }

        public void implement() throws Exception {
            getEditList().apply();
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);
            for (String methodScope : fixInfo.methodSkeletons) {
                edits.replace(fixInfo.offset, 0, methodScope, true, 0);
            }
            return edits;
        }
    }

    private static class CachedFixInfo {
        private LinkedHashSet<String> methodSkeletons;
        private int offset;
        private OffsetRange classNameRange;

        CachedFixInfo(TypeScope typeScope, LinkedHashSet<String> methodSkeletons) {
            this.methodSkeletons = methodSkeletons;
            setClassScope(typeScope);
        }

        void setClassScope(TypeScope typeScope)  {
            this.classNameRange = typeScope.getNameRange();
            this.offset = typeScope.getBlockRange().getEnd() - 1;
        }
    }
}
