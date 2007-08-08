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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;

/** Use this class for a column if the entry is stored as an attribute in
 *  the parent bean class.
 *
 * @author Peter Williams
 */
public class AttributeEntry extends TableEntry {

    public AttributeEntry(String pn, String c, int w) {
        super(pn, c, w);
    }

    public AttributeEntry(String pn, String c, int w, boolean required) {
        super(pn, c, w, required);
    }

    public AttributeEntry(String ppn, String pn, String c, int w, boolean required) {
        super(ppn, pn, c, w, required);
    }

    public AttributeEntry(String ppn, String pn, String c, int w, boolean required, boolean isName) {
        super(ppn, pn, c, w, required, isName);
    }

    public AttributeEntry(String ppn, String pn, ResourceBundle resBundle,
            String resourceBase, int w, boolean required, boolean isName) {
        super(ppn, pn, resBundle, resourceBase, w, required, isName);
    }

            public Object getEntry(CommonDDBean parent) {
        return parent.getAttributeValue(propertyName);
    }

    public void setEntry(CommonDDBean parent, Object value) {
        String attrValue = null;
        if(value != null) {
            attrValue = value.toString();
        }
        parent.setAttributeValue(propertyName, attrValue);
    }

    public Object getEntry(CommonDDBean parent, int row) {
        return parent.getAttributeValue(parentPropertyName, row, propertyName);
    }

    public void setEntry(CommonDDBean parent, int row, Object value) {
        String attrValue = null;
        if(value != null) {
            attrValue = value.toString();
        }

        parent.setAttributeValue(parentPropertyName, row, propertyName, attrValue);
        // !PW FIXME I think Cliff Draper fixed the bug this was put in for... we'll see.
        // The issue was that attributes that were children of non-property objects and
        // thus were attached to a boolean array, needed to have the boolean set to true
        // in order to be recognized, otherwise, it was as if they did not exist.
        // attributes of real properties (those that have non-attribute children as well)
        // work fine regardless.
//        if(Common.isBoolean(parent.beanProp(parentPropertyName).getType())) {
//            parent.setValue(parentPropertyName, row, Boolean.TRUE);
//        }
    }
}
