/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

/**
 * @author pfiala
 */
public abstract class TextItemEditorModel extends ItemEditorHelper.ItemEditorModel {
    XmlMultiViewDataObject dataObject;
    String origValue;

    protected TextItemEditorModel(XmlMultiViewDataObject dataObject) {
        this.dataObject = dataObject;
        origValue = getValue();
    }

    protected boolean validate(String value) {
        return true;
    }

    protected abstract void setValue(String value);

    protected abstract String getValue();

    public final boolean setItemValue(String value) {
        if (validate(value)) {
            setValue(value);
            origValue = value;
            dataObject.modelUpdatedFromUI();
            return true;
        } else {
            return false;
        }
    }

    public final String getItemValue() {
        return getValue();
    }

    public void documentUpdated() {
        String value = getEditorText();
        if (validate(value)) {
            setValue(value);
            dataObject.modelUpdatedFromUI();
        } else {
            setValue(origValue);
        }
    }
}
