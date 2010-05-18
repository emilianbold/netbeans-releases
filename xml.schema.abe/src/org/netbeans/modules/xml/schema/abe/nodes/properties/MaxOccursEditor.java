/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * MaxOccursEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 */
public class MaxOccursEditor  extends PropertyEditorSupport
        implements ExPropertyEditor
{
    /** Creates a new instance of MaxOccursEditor */
    public MaxOccursEditor() {
    }

@Override
    public String[] getTags() {
            return new String[] {NbBundle.getMessage(MaxOccursEditor.class,"LBL_Unbounded")};
    }

@Override
    public void setAsText(String text) throws IllegalArgumentException {
        if ( NbBundle.getMessage(MaxOccursEditor.class,"LBL_DefaultValueOne").equals(text) &&
                getValue() == null )
            return;
        // Allow positive integers, "unbounded" or *    
        if (text.matches("[0-9]*")) {   //NOI18N
                if (Integer.valueOf(text).intValue() < 0) {
                // if not an integer, NumberFormatException is thrown
                        throwError(text);
                }
                else {
                    setValue(text);
                }
        }
        else {
            // asterisk (*) means unbounded
            if (text.equals("unbounded") || text.equals("*")){   //NOI18N
                 setValue("unbounded");
            }
            else {
                throwError(text);
            }
        }
    }

    private void throwError(String text){
            String msg = NbBundle.getMessage(MaxOccursEditor.class, "LBL_Illegal_MaxOccurs_Value", text); //NOI18N
            IllegalArgumentException iae = new IllegalArgumentException(msg);
            ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                            msg, msg, null, new java.util.Date());
            throw iae;

    }

@Override
    public String getAsText() {
            Object val = getValue();
            return val==null?NbBundle.getMessage(MaxOccursEditor.class,"LBL_DefaultValueOne"):val.toString();
    }

    
    
    /**
     *
     *  implement ExPropertyEditor
     *
     */
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this an editable combo tagged editor  
        desc.setValue("canEditAsText", Boolean.TRUE); // NOI18N
    }
}
