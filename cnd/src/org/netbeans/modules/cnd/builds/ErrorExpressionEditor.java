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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.builds;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.cnd.settings.MakeSettings;

/**
 *  Provide a reasonable method for users to change the default property editor
 *  for ErrorExpressions.
 */
public class ErrorExpressionEditor extends PropertyEditorSupport {

    /** shared list of error expressions in the system */
    private static Collection<ErrorExpression> sharedList;

    static {
        sharedList = new HashSet<ErrorExpression>();
        sharedList.add(MakeSettings.SUN_COMPILERS);
        sharedList.add(MakeSettings.GNU_COMPILERS);
    }


    /** list to use for error expressions */
    private Collection<ErrorExpression> list;


    /** value to edit */
    private ErrorExpression value;


    /**
     *  Constructs property editor with shared array of registered expressions.
     */
    public ErrorExpressionEditor() {
        this(sharedList);
    }


    /**
     *  Constructs property editor given list of ErrorExpression. This list will be
     * presented to the user when the editor is used. Also the list is modified when
     * user adds a new ErrorExpression.
     *
     * @param list modifiable collection of <CODE>ErrorExpression</CODE>s
     */
    public ErrorExpressionEditor(Collection<ErrorExpression> list) {
        this.list = list;
    }

    @Override
    public Object getValue() {
        return value;
    }


    public void setValue(ErrorExpression value) {
        synchronized (this) {
            this.value = value;
            list.add(value);
        }
        firePropertyChange();
    }

    @Override
    public String getAsText() {
        return "";//value.getName(); // FIXUP - TRUNK - THP // NOI18N
    }

    @Override
    public void setAsText(String string) {
        //ErrorExpression[] exprs = getExpressions();

        //for (int i = 0; i < exprs.length; i++) {
            /* // FIXUP - TRUNK - THP
            if (string.equals(exprs[i].getName())) {
            setValue(exprs[i]);
            break;
            }
             */ // FIXUP - TRUNK - THP
        //}
    }

    @Override
    public String getJavaInitializationString() {
        return "new ErrorExpression (" + // NOI18N
                //value.getName() + ", " + // NOI18N // FIXUP - TRUNK - THP
                //value.getErrorExpression() + ", " + // NOI18N // FIXUP - TRUNK - THP
                //value.getFilePos() + ", " + // NOI18N // FIXUP - TRUNK - THP
                //value.getLinePos() + ", " + // NOI18N // FIXUP - TRUNK - THP
                //value.getColumnPos() + ", " + // NOI18N // FIXUP - TRUNK - THP
                //value.getDescriptionPos() + // FIXUP - TRUNK - THP
                ")"; // NOI18N
    }

    @Override
    public String[] getTags() {
        ErrorExpression[] exprs = getExpressions();
        String[] tags = new String[exprs.length];

        /* // FIXUP - TRUNK - THP
        for (int i = 0; i < exprs.length; i++) {
        tags[i] = exprs[i].getName();
        }
         */ // FIXUP - TRUNK - THP

        return tags;
    }

    @Override
    public boolean isPaintable() {
        return false;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
    }

    @Override
    public boolean supportsCustomEditor() {
	       return true;
    }

    @Override
    public Component getCustomEditor() {
	       return new ErrorExpressionPanel(this);
    }


    synchronized ErrorExpression[] getExpressions() {
	       return list.toArray(new ErrorExpression[list.size()]);
    }


    Collection<ErrorExpression> getExpressionsVector() {
	       return list;
    }
}
