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
    private boolean emptyAllowed;
    private boolean emptyIsNull;
    String origValue;

    protected TextItemEditorModel(XmlMultiViewDataObject dataObject, boolean emptyAllowed) {
        this(dataObject, emptyAllowed, false);

    }
    protected TextItemEditorModel(XmlMultiViewDataObject dataObject, boolean emptyAllowed, boolean emptyIsNull) {
        this.dataObject = dataObject;
        this.emptyAllowed = emptyAllowed;
        this.emptyIsNull = emptyIsNull;
        origValue = getValue();
    }

    protected boolean validate(String value) {
        return emptyAllowed ? true : value != null && value.length() > 0;
    }

    protected abstract void setValue(String value);

    protected abstract String getValue();

    public final boolean setItemValue(String value) {
        if (emptyAllowed && emptyIsNull && value != null) {
            while (value.length() > 0 && value.charAt(0) == ' ') {
                value = value.substring(1);
            }
            if (value.length() == 0) {
                value = null;
            }
        }
        if (validate(value)) {
            String currentValue = getValue();
            if (!(value == currentValue || value != null && value.equals(currentValue))) {
                setValue(value);
                dataObject.modelUpdatedFromUI();
            }
            return true;
        } else {
            return false;
        }
    }

    public final String getItemValue() {
        String value = getValue();
        return value == null ? "" : value;
    }

    public void documentUpdated() {
        setItemValue(getEditorText());
    }
}
