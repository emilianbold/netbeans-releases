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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Tiny {

    @Hint(category="bugs", suppressWarnings="ReplaceAllDot")
    @TriggerPattern(value="$str.replaceAll(\".\", $to)",
                    constraints=@Constraint(variable="$str", type="java.lang.String"))
    public static ErrorDescription stringReplaceAllDot(HintContext ctx) {
        Tree constant = ((MethodInvocationTree) ctx.getPath().getLeaf()).getArguments().get(0);
        TreePath constantTP = new TreePath(ctx.getPath(), constant);

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_string-replace-all-dot");
        Fix fix = JavaFix.rewriteFix(ctx, fixDisplayName, constantTP, "\"\\\\.\"");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_string-replace-all-dot");

        return ErrorDescriptionFactory.forTree(ctx, constant, displayName, fix);
    }

    @Hint(category="bugs", suppressWarnings="ResultOfObjectAllocationIgnored")
    //TODO: anonymous innerclasses?
    @TriggerPatterns({
        @TriggerPattern(value="new $type($params$);"),
        @TriggerPattern(value="$enh.new $type($params$);")
    })
    public static ErrorDescription newObject(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_newObject");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="bugs", suppressWarnings="SuspiciousSystemArraycopy")
    @TriggerPattern(value="java.lang.System.arraycopy($src, $srcPos, $dest, $destPos, $length)")
    public static List<ErrorDescription> systemArrayCopy(HintContext ctx) {
        List<ErrorDescription> result = new LinkedList<ErrorDescription>();

        for (String objName : Arrays.asList("$src", "$dest")) {
            TreePath obj = ctx.getVariables().get(objName);
            TypeMirror type = ctx.getInfo().getTrees().getTypeMirror(obj);

            if (type != null && type.getKind() != TypeKind.ERROR && type.getKind() != TypeKind.ARRAY) {
                String treeDisplayName = Utilities.shortDisplayName(ctx.getInfo(), (ExpressionTree) obj.getLeaf());
                String displayName = NbBundle.getMessage(Tiny.class, "ERR_system_arraycopy_notarray", treeDisplayName);
                
                result.add(ErrorDescriptionFactory.forTree(ctx, obj, displayName));
            }
        }

        for (String countName : Arrays.asList("$srcPos", "$destPos", "$length")) {
            TreePath count = ctx.getVariables().get(countName);
            Number value = ArithmeticUtilities.compute(ctx.getInfo(), count, true);

            if (value != null && value.intValue() < 0) {
                String treeDisplayName = Utilities.shortDisplayName(ctx.getInfo(), (ExpressionTree) count.getLeaf());
                String displayName = NbBundle.getMessage(Tiny.class, "ERR_system_arraycopy_negative", treeDisplayName);

                result.add(ErrorDescriptionFactory.forTree(ctx, count, displayName));
            }
        }

        return result;
    }


    @Hint(category="bugs", suppressWarnings="ObjectEqualsNull")
    @TriggerPattern(value="$obj.equals(null)")
    public static ErrorDescription equalsNull(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_equalsNull");
        Fix fix = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$obj == null");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_equalsNull");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, fix);
    }

    @Hint(category="bugs")
    @TriggerPattern(value="$set.$method($columnIndex, $other$)",
                    constraints={
                        @Constraint(variable="$set", type="java.sql.ResultSet"),
                        @Constraint(variable="$columnIndex", type="int")
                    })
    public static ErrorDescription resultSet(HintContext ctx) {
        TypeElement resultSet = ctx.getInfo().getElements().getTypeElement("java.sql.ResultSet");
        String methodName = ctx.getVariableNames().get("$method");

        if (!METHOD_NAME.contains(methodName)) {
            return null;
        }

        TreePath columnIndex = ctx.getVariables().get("$columnIndex");
        Number value = ArithmeticUtilities.compute(ctx.getInfo(), columnIndex, true);

        if (value == null) {
            return null;
        }

        int intValue = value.intValue();

        if (intValue > 0) {
            return null;
        }

        Element methodEl = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (methodEl == null || methodEl.getKind() != ElementKind.METHOD) {
            return null;
        }

        ExecutableElement methodElement = (ExecutableElement) methodEl;
        boolean found = false;

        for (ExecutableElement e : ElementFilter.methodsIn(resultSet.getEnclosedElements())) {
            if (e.equals(methodEl)) {
                found = true;
                break;
            }
            if (ctx.getInfo().getElements().overrides(methodElement, e, (TypeElement) methodElement.getEnclosingElement())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return null;
        }

        String key = intValue == 0 ? "ERR_ResultSetZero" : "ERR_ResultSetNegative";
        String displayName = NbBundle.getMessage(Tiny.class, key);

        return ErrorDescriptionFactory.forName(ctx, columnIndex, displayName);
    }
    
    private static final Set<String> METHOD_NAME = new HashSet<String>(Arrays.asList(
            "getString", "getBoolean", "getByte", "getShort", "getInt", "getLong",
            "getFloat", "getDouble", "getBigDecimal", "getBytes", "getDate",
            "getTime", "getTimestamp", "getAsciiStream", "getUnicodeStream",
            "getBinaryStream", "getObject", "getCharacterStream", "getBigDecimal",
            "updateNull", "updateBoolean", "updateByte", "updateShort", "updateInt",
            "updateLong", "updateFloat", "updateDouble", "updateBigDecimal", "updateString",
            "updateBytes", "updateDate", "updateTime", "updateTimestamp", "updateAsciiStream",
            "updateBinaryStream", "updateCharacterStream", "updateObject", "updateObject",
            "getObject", "getRef", "getBlob", "getClob", "getArray", "getDate", "getTime",
            "getTimestamp", "getURL", "updateRef", "updateBlob", "updateClob", "updateArray",
            "getRowId", "updateRowId", "updateNString", "updateNClob", "getNClob", "getSQLXML",
            "updateSQLXML", "getNString", "getNCharacterStream", "updateNCharacterStream",
            "updateAsciiStream", "updateBinaryStream", "updateCharacterStream", "updateBlob",
            "updateClob", "updateNClob", "updateNCharacterStream", "updateAsciiStream",
            "updateBinaryStream", "updateCharacterStream", "updateBlob", "updateClob",
            "updateNClob"
    ));
    
}
