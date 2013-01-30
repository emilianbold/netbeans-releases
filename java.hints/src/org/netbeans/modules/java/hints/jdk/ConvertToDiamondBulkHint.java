/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010-2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jdk.ConvertToDiamondBulkHint.CustomizerProviderImpl;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_Javac_canUseDiamond", description = "#DESC_Javac_canUseDiamond", id=ConvertToDiamondBulkHint.ID, category="rules15",enabled=true, customizerProvider=CustomizerProviderImpl.class)
public class ConvertToDiamondBulkHint {

    public static final String ID = "Javac_canUseDiamond";
    public static final Set<String> CODES = new HashSet<String>(Arrays.asList("compiler.warn.diamond.redundant.args", "compiler.warn.diamond.redundant.args.1"));

    //XXX: hack:
    public static boolean isHintEnabled() {
        for (HintMetadata hm : RulesManager.getInstance().readHints(null, null, null).keySet()) {
            if (ID.equals(hm.id)) {
                return HintsSettings.isEnabled(hm);
            }
        }

        return true;
    }

    private static final Map<String, Collection<String>> key2Pattern = new LinkedHashMap<String, Collection<String>>();

    static {
        key2Pattern.put("initializer", Arrays.asList("$mods$ $type $name = $init;"));
        key2Pattern.put("assignment", Arrays.asList("$var = $init"));
        key2Pattern.put("return", Arrays.asList("return $init;"));
        key2Pattern.put("argument", Arrays.asList("$site.<$T$>$name($p$, $init, $s$)", "$name($p$, $init, $s$)", "new $type<$T$>($p$, $init, $s$)", "new $type($p$, $init, $s$)"));
        key2Pattern.put("other", Arrays.asList(new String[] {null}));
    }
    
    @TriggerPatterns({
        @TriggerPattern("new $clazz<$tparams$>($params$)")
    })
    public static ErrorDescription compute(HintContext ctx) {
        if (ctx.getMultiVariables().get("$tparams$").isEmpty()) return null;
        
        TreePath clazz = ctx.getVariables().get("$clazz");
        long start = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(clazz.getCompilationUnit(), clazz.getLeaf());

        ctx.getVariables().put("$init", ctx.getPath());
        
        OUTER: for (Diagnostic<?> d : ctx.getInfo().getDiagnostics()) {
            if (start != d.getStartPosition()) continue;
            if (!CODES.contains(d.getCode())) continue;

            FOUND: for (Entry<String, Collection<String>> e : key2Pattern.entrySet()) {
                for (String p : e.getValue()) {
                    if (p == null || MatcherUtilities.matches(ctx, ctx.getPath().getParentPath(), p)) {
                        boolean enabled = isEnabled(ctx, e.getKey());

                        if (!enabled) {
                            continue OUTER;
                        } else {
                            break FOUND;
                        }
                    }
                }
            }

            return ErrorDescriptionFactory.forTree(ctx, clazz.getParentPath(), d.getMessage(null), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
        }

        return null;
    }

    static final String KEY = "enabledVariants";
    static final String ALL = "initializer,assignment,return,argument,other";
    
    static String getConfiguration(Preferences p) {
        return p.get(KEY, ALL);
    }

    static void putConfiguration(Preferences p, String configuration) {
        p.put(KEY, configuration);
    }

    private static boolean isEnabled(HintContext ctx, String key) {
        return isEnabled(ctx.getPreferences(), key);
    }

    static boolean isEnabled(Preferences p, String key) {
        return ("," + getConfiguration(p) + ",").contains("," + key + ",");
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        public String getText() {
            return NbBundle.getMessage(ConvertToDiamondBulkHint.class, "FIX_ConvertToDiamond");
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            if (tp.getLeaf().getKind() != Tree.Kind.NEW_CLASS) {
                //XXX: warning
                return ;
            }

            NewClassTree nct = (NewClassTree) tp.getLeaf();

            if (nct.getIdentifier().getKind() != Tree.Kind.PARAMETERIZED_TYPE) {
                //XXX: warning
                return ;
            }

            TreeMaker make = copy.getTreeMaker();
            ParameterizedTypeTree ptt = (ParameterizedTypeTree) nct.getIdentifier();
            ParameterizedTypeTree nue = make.ParameterizedType(ptt.getType(), Collections.<Tree>emptyList());

            copy.rewrite(ptt, nue);
        }

    }

    public static final class CustomizerProviderImpl implements CustomizerProvider {

        @Override public JComponent getCustomizer(Preferences prefs) {
            return new ConvertToDiamondBulkHintPanel(prefs);
        }

    }
}
