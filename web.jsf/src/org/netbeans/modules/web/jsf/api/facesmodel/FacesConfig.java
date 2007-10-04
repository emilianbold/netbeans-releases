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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "faces-config" element is the root of the configuration
 * information hierarchy, and contains nested elements for all
 * of the other configuration settings.
 * @author Petr Pisl
 */

public interface FacesConfig extends JSFConfigComponent {
    
    /**
     * Property for &lt;managed-bean&gt; element
     */
    public static final String MANAGED_BEAN = JSFConfigQNames.MANAGED_BEAN.getLocalName();
    /**
     * Property of &lt;navigation-rule&gt; element
     */
    public static final String NAVIGATION_RULE = JSFConfigQNames.NAVIGATION_RULE.getLocalName();
    /**
     * Property of &lt;converter&gt; element
     */
    public static final String CONVERTER = JSFConfigQNames.CONVERTER.getLocalName();
    
    /**
     * Property of &lt;application&gt; element
     */
    public static final String APPLICATION = JSFConfigQNames.APPLICATION.getLocalName();
    
    List<Converter> getConverters();
    void addConverter(Converter converter);
    void addConverter(int index, Converter converter);
    void removeConverter(Converter converter);
    
    List <ManagedBean> getManagedBeans();
    void addManagedBean(ManagedBean bean);
    void addManagedBean(int index, ManagedBean bean);
    void removeManagedBean(ManagedBean bean);
    
    List<NavigationRule> getNavigationRules();
    void addNavigationRule(NavigationRule rule);
    void addNavigationRule(int index, NavigationRule rule);
    void removeNavigationRule(NavigationRule rule);
    
    List<Application> getApplications();
    void addApplication(Application application);
    void addApplication(int index, Application application);
    void removeApplication(Application application);
}
