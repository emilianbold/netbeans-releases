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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;

/**
 * Domain of client ids of all components in scope that generate events of any
 * kind, i.e. all action and input components. An input component is one that
 * implements {@link javax.faces.component.EditableValueHolder}, and an action
 * component is one that implements {@link javax.faces.component.ActionSource}.
 */
public class EventClientIdsDomain extends ClientIdsDomain {

    protected boolean isDomainComponent(UIComponent component) {
        Class c = component.getClass();
        if (EditableValueHolder.class.isAssignableFrom(c) || ActionSource.class.isAssignableFrom(c))
            return true;
        return false;
    }

}
