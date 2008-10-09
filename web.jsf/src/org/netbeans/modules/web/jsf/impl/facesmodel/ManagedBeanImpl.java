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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class ManagedBeanImpl extends DescriptionGroupImpl implements ManagedBean {
    
    // caching properties
    private String beanName;
    private String beanClass;
    private ManagedBean.Scope beanScope;
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>();
    static {
        SORTED_ELEMENTS.addAll(DescriptionGroupImpl.DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add(JSFConfigQNames.MANAGED_BEAN_NAME.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.MANAGED_BEAN_CLASS.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.MANAGED_BEAN_SCOPE.getLocalName());
    }
    
    /** Creates a new instance of ManagedBeanImpl */
    public ManagedBeanImpl(JSFConfigModelImpl model,Element element) {
        super(model, element);
        beanName = null;
        beanClass = null;
        beanScope = null;
        
        this.addPropertyChangeListener(new PropertyChangeListener () {
            
            public void propertyChange(PropertyChangeEvent event) {
                // The managed bean was changed -> reset all cache fields
                // When user modifies the source file by hand, then the property name
                // is "textContent", so it's easier to reset all fields, then 
                // parse the new value.
                beanName = null;
                beanClass = null;
                beanScope = null;
            }
            
        });
        
    }
    
    public ManagedBeanImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.MANAGED_BEAN));
    }
    
    public String getManagedBeanName() {
        if (beanName == null) {
            beanName = getChildElementText(JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getNamespaceURI()));
            if (beanName != null) {
                beanName = beanName.trim();
            }
        }
        return beanName;
    }
    
    public void setManagedBeanName(String name) {
        setChildElementText(MANAGED_BEAN_NAME, name, JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getNamespaceURI()));
    }
    
    public String getManagedBeanClass() {
        if (beanClass ==  null) {
            beanClass = getChildElementText(JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getNamespaceURI()));
            if (beanClass != null) {
                beanClass = beanClass.trim();
            }
        }
        return beanClass;
    }
    
    public void setManagedBeanClass(String beanClass) {
        setChildElementText(MANAGED_BEAN_CLASS, beanClass, JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getNamespaceURI()));
    }
    
    public ManagedBean.Scope getManagedBeanScope() {
        if (beanScope == null) {
            String scopeText = getChildElementText(JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getNamespaceURI()));
            scopeText = scopeText.trim().toUpperCase(Locale.ENGLISH);
            try{
                beanScope = ManagedBean.Scope.valueOf(scopeText);
            }
            catch (IllegalArgumentException exception){
                // do nothing. The value is wrong and the method should return null.
            }
        }
        return beanScope;
    }
    
    public void setManagedBeanScope(ManagedBean.Scope scope) {
        setChildElementText(MANAGED_BEAN_SCOPE, scope.toString(), JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getPeer().getNamespaceURI()));
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
