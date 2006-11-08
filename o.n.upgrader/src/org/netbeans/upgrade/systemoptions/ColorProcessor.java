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

package org.netbeans.upgrade.systemoptions;
import java.lang.Object;
import java.util.Iterator;


/**
 * @author Radek Matous
 */
class ColorProcessor extends PropertyProcessor {
    ColorProcessor() {
        super("java.awt.Color");//NOI18N
    }
    
    
    void processPropertyImpl(String propertyName, Object value) {
        StringBuffer sb = new StringBuffer();
        if ("connectionBorderColor".equals(propertyName)||
                "dragBorderColor".equals(propertyName)||
                "formDesignerBackgroundColor".equals(propertyName)||
                "formDesignerBorderColor".equals(propertyName)||
                "guidingLineColor".equals(propertyName)||
                "selectionBorderColor".equals(propertyName)) {//NOI18N
            for (Iterator it = ((SerParser.ObjectWrapper)value).data.iterator(); it.hasNext();) {
                Object o = it.next();
                if (o instanceof SerParser.NameValue && "value".equals(((SerParser.NameValue)o).name.name)) {//NOI18N
                    addProperty(propertyName, ((SerParser.NameValue)o).value.toString());
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
}
