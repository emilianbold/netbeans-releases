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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class NavigationCaseImpl extends JSFConfigComponentImpl.ComponentInfoImpl implements NavigationCase{
    
    public static final String REDIRECT = JSFConfigQNames.REDIRECT.getLocalName();
    public static final String FROM_ACTION = JSFConfigQNames.FROM_ACTION.getLocalName();
    public static final String FROM_OUTCOME = JSFConfigQNames.FROM_OUTCOME.getLocalName();
    public static final String TO_VIEW_ID = JSFConfigQNames.TO_VIEW_ID.getLocalName();
    
    /** Creates a new instance of NavigationCaseImpl */
    public NavigationCaseImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public NavigationCaseImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.NAVIGATION_CASE));
    }
    
    public String getFromAction() {
        return getChildElementText(JSFConfigQNames.FROM_ACTION.getQName(getModel().getVersion()));
    }
    
    public void setFromAction(String fromAction) {
        setChildElementText(FROM_ACTION, fromAction, JSFConfigQNames.FROM_ACTION.getQName(getModel().getVersion()));
    }
    
    public String getFromOutcome() {
        return getChildElementText(JSFConfigQNames.FROM_OUTCOME.getQName(getModel().getVersion()));
    }
    
    public void setFromOutcome(String fromOutcome) {
        setChildElementText(FROM_OUTCOME, fromOutcome, JSFConfigQNames.FROM_OUTCOME.getQName(getModel().getVersion()));
    }
    
    public void setRedirected(boolean redirect) {
        if (redirect)
            setChildElementText(REDIRECT, "", JSFConfigQNames.REDIRECT.getQName(getModel().getVersion()));
        else
            setChildElementText(REDIRECT, null, JSFConfigQNames.REDIRECT.getQName(getModel().getVersion()));
    }
    
    public boolean isRedirected() {
        return (null != getChildElementText(JSFConfigQNames.REDIRECT.getQName(getModel().getVersion())));
    }
    
    public String getToViewId() {
        return getChildElementText(JSFConfigQNames.TO_VIEW_ID.getQName(getModel().getVersion()));
    }
    
    public void setToViewId(String toViewId) {
        setChildElementText(TO_VIEW_ID, toViewId, JSFConfigQNames.TO_VIEW_ID.getQName(getModel().getVersion()));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
