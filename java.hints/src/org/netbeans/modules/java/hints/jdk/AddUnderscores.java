/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.CustomizerProvider;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.jdk.AddUnderscores.CustomizerProviderImpl;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(id=AddUnderscores.ID, category="rules15", enabled=false, severity=HintSeverity.CURRENT_LINE_WARNING, customizerProvider=CustomizerProviderImpl.class)
public class AddUnderscores {
    public static final String ID = "org.netbeans.modules.java.hints.jdk.AddUnderscores";

    @TriggerTreeKind({Kind.INT_LITERAL, Kind.LONG_LITERAL})
    public static ErrorDescription hint(HintContext ctx) {
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) return null;
        
        TreePath tp = ctx.getPath();
        int start = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
        int end = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
        String literal = ctx.getInfo().getText().substring(start, end);
        if (!isReplaceLiteralsWithUnderscores(ctx.getPreferences()) && literal.contains("_")) return null;
        RadixInfo info = radixInfo(literal);
        if (info.radix == 8) return null;//octals ignored for now
        String normalized = info.constant.replaceAll(Pattern.quote("_"), "");
        int separateCount = getSizeForRadix(ctx.getPreferences(), info.radix);
        StringBuilder split = new StringBuilder();
        int count = separateCount + 1;

        for (int i = normalized.length(); i > 0; i--) {
            if (--count == 0) {
                split.append("_");
                count = separateCount;
            }
            split.append(normalized.charAt(i - 1));
        }

        split.reverse();

        String result = info.prefix + split.toString() + info.suffix;

        if (result.equals(literal)) return null;

        String displayName = NbBundle.getMessage(AddUnderscores.class, "ERR_" + ID);
        Fix f = JavaFix.toEditorFix(new FixImpl(ctx.getInfo(), tp, result));

        return ErrorDescriptionFactory.forTree(ctx, tp, displayName, f);
    }

    private static final String KEY_SIZE_BINARY = "size-binary";
    private static final String KEY_SIZE_DECIMAL = "size-decimal";
    private static final String KEY_SIZE_HEXADECIMAL = "size-hexadecimal";
    private static final String KEY_ALSO_WITH_UNDERSCORES = "also-with-underscores";
    
    static int getSizeForRadix(Preferences prefs, int radix) {
        String key;
        int def;

        switch (radix) {
            case 2: key = KEY_SIZE_BINARY; def = 4; break;
            case 10: key = KEY_SIZE_DECIMAL; def = 3; break;
            case 16: key = KEY_SIZE_HEXADECIMAL; def = 4; break;
            default: throw new IllegalStateException("radix=" + radix);
        }

        return prefs.getInt(key, def);
    }

    static boolean isReplaceLiteralsWithUnderscores(Preferences prefs) {
        return prefs.getBoolean(KEY_ALSO_WITH_UNDERSCORES, false);
    }

    static void setSizeForRadix(Preferences prefs, int radix, int size) {
        String key;

        switch (radix) {
            case 2: key = KEY_SIZE_BINARY; break;
            case 10: key = KEY_SIZE_DECIMAL; break;
            case 16: key = KEY_SIZE_HEXADECIMAL; break;
            default: throw new IllegalStateException("radix=" + radix);
        }

        prefs.putInt(key, size);
    }

    static void setReplaceLiteralsWithUnderscores(Preferences prefs, boolean value) {
        prefs.putBoolean(KEY_ALSO_WITH_UNDERSCORES, value);
    }


    public static RadixInfo radixInfo(String literal) {
        String suffix = "";

        if (literal.endsWith("l") || literal.endsWith("L")) {
            suffix = literal.substring(literal.length() - 1);
            literal = literal.substring(0, literal.length() - 1);
        }

        int currentRadix = 10;
        String prefix = "";

        if (literal.startsWith("0x") || literal.startsWith("0X")) {
            currentRadix = 16;
            prefix = literal.substring(0, 2);
            literal = literal.substring(2);
        } else if (literal.startsWith("0b") || literal.startsWith("0B")) {
            currentRadix = 2;
            prefix = literal.substring(0, 2);
            literal = literal.substring(2);
        } else if (literal.startsWith("0") && literal.length() > 1) {
            currentRadix = 8;
            prefix = literal.substring(0, 1);
            literal = literal.substring(1);
        }

        return new RadixInfo(prefix, literal, suffix, currentRadix);
    }

    public static final class RadixInfo {
        public final String prefix;
        public final String constant;
        public final String suffix;
        public final int radix;

        public RadixInfo(String prefix, String constant, String suffix, int radix) {
            this.prefix = prefix;
            this.constant = constant;
            this.suffix = suffix;
            this.radix = radix;
        }

    }
    
    private static final class FixImpl extends JavaFix {

        private final String target;

        public FixImpl(CompilationInfo info, TreePath tp, String target) {
            super(info, tp);
            this.target = target;
        }


        @Override
        protected String getText() {
            return NbBundle.getMessage(AddUnderscores.class, "FIX_" + ID, target);
        }

        @Override
        protected void performRewrite(WorkingCopy wc, TreePath tp, boolean canShowUI) {
            wc.rewrite(tp.getLeaf(), wc.getTreeMaker().Identifier(target));
        }

    }

    public static final class CustomizerProviderImpl implements CustomizerProvider {

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            return new AddUnderscoresPanel(prefs);
        }

    }
}
