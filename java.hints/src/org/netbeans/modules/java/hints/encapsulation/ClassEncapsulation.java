/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.encapsulation;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class ClassEncapsulation {

    @Hint(category="encapsulation",suppressWarnings={"PublicInnerClass"}, enabled=false)   //NOI18N
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription publicCls(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, Modifier.PUBLIC,
            NbBundle.getMessage(ClassEncapsulation.class, "TXT_PublicInnerClass"), "PublicInnerClass");  //NOI18N
    }

    @Hint(category="encapsulation",suppressWarnings={"ProtectedInnerClass"}, enabled=false)    //NOI18N
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription protectedCls(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, Modifier.PROTECTED,
            NbBundle.getMessage(ClassEncapsulation.class, "TXT_ProtectedInnerClass"), "ProtectedInnerClass"); //NOI18N
    }

    @Hint(category="encapsulation", suppressWarnings={"PackageVisibleInnerClass"}, enabled=false)
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription packageCls(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, null,
            NbBundle.getMessage(ClassEncapsulation.class, "TXT_PackageInnerClass"), "PackageVisibleInnerClass");    //NOI18N
    }

    private static ErrorDescription create(final HintContext ctx, final Modifier visibility,
        final String description, final String suppressWarnings) {
        assert ctx != null;
        assert description != null;
        assert suppressWarnings != null;
        final TreePath tp = ctx.getPath();
        final Tree owner = tp.getParentPath().getLeaf();
        if (owner.getKind() != Kind.CLASS) {
            return null;
        }
        if (!hasRequiredVisibility(((ClassTree)tp.getLeaf()).getModifiers().getFlags(),visibility)) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, tp, description,
            FixFactory.createSuppressWarningsFix(ctx.getInfo(), tp, suppressWarnings));
    }

    private static boolean hasRequiredVisibility(final Set<Modifier> modifiers, final Modifier reqModifier) {
        return reqModifier != null ?
            modifiers.contains(reqModifier):
            modifiers.isEmpty() ? true:
                !EnumSet.copyOf(modifiers).removeAll(EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC));
    }
}
