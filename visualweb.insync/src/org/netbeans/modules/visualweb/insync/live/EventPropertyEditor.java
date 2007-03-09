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
package org.netbeans.modules.visualweb.insync.live;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.EventDescriptor;

/**
 *
 */
public class EventPropertyEditor extends PropertyEditorSupport {

    static final boolean USING_TAGS = false;

    BeansDesignEvent event;
    String[] methods;
    String current;

    public EventPropertyEditor(BeansDesignEvent event) {
        this.event = event;
        assert Trace.trace("insync.live", "EPE: " + event);
        //setValue(event.getHandlerName());
    }

    // load up-to-date list of all sibling beans
    void loadList() {
        assert Trace.trace("insync.live", "EPE.loadList");
        BeansDesignBean bean = (BeansDesignBean)event.getDesignBean();
        LiveUnit unit = bean.unit;
        EventDescriptor ed = event.getEventDescriptor();
        java.lang.reflect.Method m = ed.getListenerMethodDescriptor().getMethod();
        methods = unit.sourceUnit.getThisClass().getMethodNames(
                m.getParameterTypes(), m.getReturnType()).toArray(new String[0]);
    }

    void update() {
        if (USING_TAGS)
            loadList();
    }

    public String getAsText() {
        String text = current != null ? current + "()" : "";
        assert Trace.trace("insync.live", "EPE.getAsText: " + text);
        return text;
    }

    public String getJavaInitializationString() {
        return null;
    }

    public String[] getMethods() {
        loadList();  // always load
        return methods;
    }

    public String[] getTags() {
        update();
        return USING_TAGS ? methods : null;
    }

    public Object getValue() {
        Object value = current;
        assert Trace.trace("insync.live", "EPE.getValue: " + value);
        return value;
    }

    public void setAsText(String text)  {
        assert Trace.trace("insync.live", "EPE.setAsText: " + text);
        setValue(text);
    }

    public void setValue(Object value) {
        assert Trace.trace("insync.live", "EPE.setValue: " + value);
        String previous = current;
        if (value instanceof String) {
            String text = (String)value;
            if (text.endsWith("()"))
                text = text.substring(0, text.length()-2);
            if (text.trim().length() == 0)
                text = "";
            current = text;
        }
        else {
            current = value != null ? value.toString() : null;
        }
        if ((previous == null) != (current == null) || previous != null && !previous.equals(current))
            firePropertyChange();
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public Component getCustomEditor() {
        EventEditPanel vbp = new EventEditPanel(this, event);
        return vbp;
    }
}
