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


import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class NavigationRuleImpl extends JSFConfigComponentImpl.ComponentInfoImpl implements NavigationRule{
    
    public NavigationRuleImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public NavigationRuleImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.NAVIGATION_RULE));
    }
    
    public List<NavigationCase> getNavigationCases() {
        return getChildren(NavigationCase.class);
    }
    
    public void addNavigationCase(NavigationCase navigationCase) {
        appendChild(NAVIGATION_CASE, navigationCase);
    }
    
    public void addNavigationCase(int index, NavigationCase navigationCase) {
        insertAtIndex(NAVIGATION_CASE, navigationCase, index, NavigationCase.class);
    }
    
    public void removeNavigationCase(NavigationCase navigationCase) {
        removeChild(NAVIGATION_CASE, navigationCase);
    }
    
    public String getFromViewId() {
        return getChildElementText(JSFConfigQNames.FROM_VIEW_ID.getQName(getModel().getVersion()));
    }
    
    public void setFromViewId(String fromView) {
        setChildElementText(FROM_VIEW_ID, fromView, JSFConfigQNames.FROM_VIEW_ID.getQName(getModel().getVersion()));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
