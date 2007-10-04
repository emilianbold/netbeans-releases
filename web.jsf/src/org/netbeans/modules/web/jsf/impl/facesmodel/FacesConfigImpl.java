/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.jsf.impl.facesmodel;


import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class FacesConfigImpl extends JSFConfigComponentImpl implements FacesConfig{
    
    /** Creates a new instance of FacesConfigImpl */
    public FacesConfigImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public FacesConfigImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.FACES_CONFIG));
    }
    
    public List<ManagedBean> getManagedBeans() {
        return getChildren(ManagedBean.class);
    }
    
    public void addManagedBean(ManagedBean bean) {
        appendChild(MANAGED_BEAN, bean);
    }
    
    public void addManagedBean(int index, ManagedBean bean) {
        insertAtIndex(MANAGED_BEAN, bean, index, ManagedBean.class);
    }
    
    public void removeManagedBean(ManagedBean bean) {
        removeChild(MANAGED_BEAN, bean);
    }
    
    public List<NavigationRule> getNavigationRules() {
        return getChildren(NavigationRule.class);
    }
    
    public void addNavigationRule(NavigationRule rule) {
        appendChild(NAVIGATION_RULE, rule);
    }
    
    public void addNavigationRule(int index, NavigationRule rule) {
        insertAtIndex(NAVIGATION_RULE, rule, index, NavigationRule.class);
    }
    public void removeNavigationRule(NavigationRule rule) {
        removeChild(NAVIGATION_RULE, rule);
    }
    
    public List<Converter> getConverters() {
        return getChildren(Converter.class);
    }
    
    public void addConverter(Converter converter) {
        appendChild(CONVERTER, converter);
    }
    
    public void addConverter(int index, Converter converter) {
        insertAtIndex(CONVERTER, converter, index, NavigationRule.class);
    }
    
    public void removeConverter(Converter converter) {
        removeChild(CONVERTER, converter);
    }
    
    public List<Application> getApplications() {
        return getChildren(Application.class);
    }

    public void addApplication(Application application) {
        appendChild(APPLICATION, application);
    }

    public void addApplication(int index, Application application) {
        insertAtIndex(APPLICATION, application, index, Application.class);
    }

    public void removeApplication(Application application) {
        removeChild(APPLICATION, application);
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
