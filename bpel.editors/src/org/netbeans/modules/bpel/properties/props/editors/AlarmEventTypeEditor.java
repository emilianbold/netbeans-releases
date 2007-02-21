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
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmEventType;

/**
 *
 * @author nk160297
 */
public class AlarmEventTypeEditor extends PropertyEditorSupport {

    /** Creates a new instance of ModelReferenceEditor */
    public AlarmEventTypeEditor() {
    }

    public String getAsText() {
        AlarmEventType val = (AlarmEventType)getValue();
        if (val == null) {
            return AlarmEventType.NOT_ASSIGNED.getDisplayName();
        } else {
            return val.getDisplayName();
        }
    }
    
    public Component getCustomEditor() {
        return null;
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        AlarmEventType newValue = AlarmEventType.forString(text);
        setValue(newValue);
    }
    
    public String[] getTags() {
        AlarmEventType[] alarmEventTypeArr = AlarmEventType.values();
        ArrayList<String> list = new ArrayList<String> (alarmEventTypeArr.length);
        for (AlarmEventType alarmEventType : alarmEventTypeArr) {
            if (alarmEventType == AlarmEventType.INVALID || 
                    alarmEventType == AlarmEventType.NOT_ASSIGNED) {
                continue;
            }
            String alarmEventTypeName = alarmEventType.getDisplayName();
            list.add(alarmEventTypeName);
        }
        return list.toArray(new String[list.size()]);
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof AlarmEventType;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof AlarmEventType;
        }
        super.setValue(newValue);
    }
}
