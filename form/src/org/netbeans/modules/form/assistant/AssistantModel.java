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
