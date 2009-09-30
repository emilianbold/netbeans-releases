/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.ui.models;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.VariablesFilterAdapter;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * Filter for instances of enumetaion types. Instead of #20 it displays the name of the constant.
 *
 * @author Maros Sandor
 */
public class EnumConstantsFilter extends VariablesFilterAdapter {

    public String[] getSupportedTypes () {
        return new String[] {
        };
    }
    
    public String[] getSupportedAncestors () {
        return new String[] {
            "java.lang.Enum"
        };
    }
    
    /**
     * Returns true.
     * 
     * @param   original the original tree model
     * @param   variable an enumeraion constant
     * @return  true
     */
    public boolean isLeaf (TreeModel original, Variable variable) throws UnknownTypeException {
        return true;
    }
    
    public Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException {
        
        ObjectVariable ov = (ObjectVariable) variable;
        if ( columnID == Constants.LOCALS_VALUE_COLUMN_ID || 
             columnID == Constants.WATCH_VALUE_COLUMN_ID
        ) {
            try {
                return ov.getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        } else if (columnID == Constants.LOCALS_TYPE_COLUMN_ID || columnID == Constants.WATCH_TYPE_COLUMN_ID) {
            String typeName = ov.getType();
            int idx = typeName.lastIndexOf("$");
            if (idx != -1) {
                return typeName.substring(idx + 1);
            }
            idx = typeName.lastIndexOf(".");
            if (idx != -1) {
                return typeName.substring(idx + 1);
            }
        }
        return original.getValueAt (variable, columnID);
    }
}
