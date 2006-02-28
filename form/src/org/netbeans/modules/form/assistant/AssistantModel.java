/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.assistant;

import java.beans.*;
import java.util.*;

/**
 * Assistant model.
 *
 * @author Jan Stola
 */
public class AssistantModel {
    private PropertyChangeSupport support;
    private String context;
    private String additionalContext;
    private Object[] parameters;

    public AssistantModel() {
        support = new PropertyChangeSupport(this);
    }

    public String getContext() {
        return context;
    }

    public String getAdditionalContext() {
        return additionalContext;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setContext(String context) {
        setContext(context, (String)null);
    }

    public void setContext(String context, String additionalContext) {
        this.context = context;
        this.additionalContext = additionalContext;
        this.parameters = null;
        fireContextChange();
    }

    public void setContext(String context, Object[] parameters) {
        this.context = context;
        this.additionalContext = null;
        this.parameters = parameters;
        fireContextChange();
    }

    private void fireContextChange() {
        support.firePropertyChange("context", null, null); // NOI18N
    }

    public String[] getMessages() {
        return AssistantMessages.getDefault().getMessages(context);
    }

    public String[] getAdditionalMessages() {
        return AssistantMessages.getDefault().getMessages(additionalContext);
    }

    // Property change support
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

}
