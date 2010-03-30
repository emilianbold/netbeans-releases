/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.support.OneCheckboxCustomizerProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsHint {

    private static final String ERASURE_PREFS_KEY = "eguals-hint-erasure"; // NOI18N
    private static final boolean ERASURE_PREFS_DEFAULT = true; // NOI18N
    
    @Hint(category="bugs", suppressWarnings="ArrayEquals")
    @TriggerPatterns({
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="java.lang.Object[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="boolean[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="byte[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="short[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="char[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="int[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="long[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="float[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @Constraint(variable="$obj", type="java.lang.Object"),
                            @Constraint(variable="$arr", type="double[]")
                        })
    })
    public static ErrorDescription arrayEquals(HintContext ctx) {
        //XXX: this check should not be needed:
        TreePath arr = ctx.getVariables().get("$arr");
        TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(arr);

        if (tm == null || tm.getKind() != TypeKind.ARRAY) {
            return null;
        }
        //XXX end

        String fixArraysDisplayName = NbBundle.getMessage(EqualsHint.class, "FIX_ReplaceWithArraysEquals");
        Fix arrays = JavaFix.rewriteFix(ctx, fixArraysDisplayName, ctx.getPath(), "java.util.Arrays.equals($obj, $arr)");
        String fixInstanceDisplayName = NbBundle.getMessage(EqualsHint.class, "FIX_ReplaceWithInstanceEquals");
        Fix instance = JavaFix.rewriteFix(ctx, fixInstanceDisplayName, ctx.getPath(), "$obj == $arr");
        String displayName = NbBundle.getMessage(EqualsHint.class, "ERR_ARRAY_EQUALS");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, arrays, instance);
    }
    
    @Hint(id="org.netbeans.modules.java.hints.EqualsHint", category="bugs", customizerProvider=CustomizerProviderImpl.class, suppressWarnings="IncompatibleEquals")
    @TriggerPattern(value="$this.equals($par)",
                    constraints={
                        @Constraint(variable="$this", type="java.lang.Object"),
                        @Constraint(variable="$par", type="java.lang.Object")
                    })
    public static ErrorDescription incompatibleEquals(HintContext ctx) {
        TreePath ths = ctx.getVariables().get("$this");
        TreePath par = ctx.getVariables().get("$par");
        TypeMirror thsType;

        if (ths != null) {
            thsType = ctx.getInfo().getTrees().getTypeMirror(ths);
        } else {
            TreePath cls = ctx.getPath();

            while (cls != null && cls.getLeaf().getKind() != Kind.CLASS) {
                cls = cls.getParentPath();
            }

            if (cls == null) {
                return null;
            }

            thsType = ctx.getInfo().getTrees().getTypeMirror(cls);

            if (thsType == null || thsType.getKind() != TypeKind.DECLARED) {
                return null;
            }
        }
        
        TypeMirror parType = ctx.getInfo().getTrees().getTypeMirror(par);
        if (ctx.getPreferences().getBoolean(ERASURE_PREFS_KEY, ERASURE_PREFS_DEFAULT)) {
            Types types = ctx.getInfo().getTypes();
            thsType = types.erasure(thsType);
            parType = types.erasure(parType);
        }
        boolean castable = ctx.getInfo().getTypeUtilities().isCastable(thsType, parType) || ctx.getInfo().getTypeUtilities().isCastable(parType, thsType);

        if (castable) {
            return null;
        }

        String displayName = NbBundle.getMessage(EqualsHint.class, "ERR_INCOMPATIBLE_EQUALS"); // NOI18N
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    public static final class CustomizerProviderImpl extends OneCheckboxCustomizerProvider {
        public CustomizerProviderImpl() {
            super(NbBundle.getMessage(EqualsHint.class, "LBL_Customizer_IncompatibleEquals"),
                  NbBundle.getMessage(EqualsHint.class, "TP_Customizer_IncompatibleEquals"),
                  ERASURE_PREFS_KEY,
                  ERASURE_PREFS_DEFAULT);
        }
    }

}
