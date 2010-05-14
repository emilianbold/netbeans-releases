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
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmType;

/**
 *
 * @author nk160297
 */
public class AlarmTypeEditor extends PropertyEditorSupport {

    /** Creates a new instance of ModelReferenceEditor */
    public AlarmTypeEditor() {
    }

    public String getAsText() {
        AlarmType val = (AlarmType)getValue();
        if (val == null) {
            return AlarmType.NOT_ASSIGNED.getDisplayName();
        } else {
            return val.getDisplayName();
        }
    }
    
    public Component getCustomEditor() {
        return null;
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        AlarmType newValue = AlarmType.forString(text);
        setValue(newValue);
    }
    
    public String[] getTags() {
        AlarmType[] alarmTypeArr = AlarmType.values();
        ArrayList<String> list = new ArrayList<String> (alarmTypeArr.length);
        for (AlarmType alarmType : alarmTypeArr) {
            if (alarmType == AlarmType.INVALID || 
                    alarmType == AlarmType.NOT_ASSIGNED) {
                continue;
            }
            String alarmTypeName = alarmType.getDisplayName();
            list.add(alarmTypeName);
        }
        return list.toArray(new String[list.size()]);
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof AlarmType;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof AlarmType;
        }
        super.setValue(newValue);
    }
}
