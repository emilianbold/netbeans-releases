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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 *
 * @author Peter Williams
 */
public abstract class DDTextFieldEditorModel extends TextItemEditorModel {

    private final String nameProperty;
    private final String attrProperty;

    public DDTextFieldEditorModel(final XmlMultiViewDataSynchronizer synchronizer, final String np) {
        super(synchronizer, true, true);
        this.nameProperty = np;
        this.attrProperty = null;
    }
    
    public DDTextFieldEditorModel(final XmlMultiViewDataSynchronizer synchronizer, final String np, String ap) {
        super(synchronizer, true, true);
        this.nameProperty = np;
        this.attrProperty = ap;
    }

    protected abstract CommonDDBean getBean();

    protected String getValue() {
        if(attrProperty == null) {
            return (String) getBean().getValue(nameProperty);
        } else if(nameProperty == null) {
            return (String) getBean().getAttributeValue(attrProperty);
        } else {
            return (String) getBean().getAttributeValue(nameProperty, attrProperty);
        }
    }

    protected void setValue(String value) {
        if(attrProperty == null) {
            getBean().setValue(nameProperty, value);
        } else if(nameProperty == null) {
            getBean().setAttributeValue(attrProperty, value);
        } else {
            getBean().setAttributeValue(nameProperty, attrProperty, value);
        }
    }

}
