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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.beans.BeanDescriptor;
import org.netbeans.editor.LocaleSupport;

/**
* Beaninfo for JavaIndentEngine.
*
* @author Miloslav Metelka
*/

public class SimpleIndentEngineBeanInfo extends FormatterIndentEngineBeanInfo {

    private BeanDescriptor beanDescriptor;

    public SimpleIndentEngineBeanInfo() {
    }

    public BeanDescriptor getBeanDescriptor () {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(getBeanClass());
            beanDescriptor.setDisplayName(getString("LAB_SimpleIndentEngine"));
            beanDescriptor.setShortDescription(getString("HINT_SimpleIndentEngine"));
            beanDescriptor.setValue("global", Boolean.TRUE); // NOI18N
        }
        return beanDescriptor;
    }

    protected Class getBeanClass() {
        return SimpleIndentEngine.class;
    }

    protected String getString(String key) {
        String retVal = LocaleSupport.getString(key);
	if( retVal == null ) {
	    retVal = super.getString(key);
	}
        return retVal;
    }

}

