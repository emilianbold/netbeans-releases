/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
