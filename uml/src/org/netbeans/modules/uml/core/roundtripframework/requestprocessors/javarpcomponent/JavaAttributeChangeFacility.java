/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.roundtripframework.AttributeChangeFacility;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 */
public class JavaAttributeChangeFacility extends AttributeChangeFacility
        implements IJavaAttributeChangeFacility 
{
    private JavaChangeHandlerUtilities m_Utils   =
                            new JavaChangeHandlerUtilities();
    private JavaAttributeChangeHandler m_Handler = 
                            new JavaAttributeChangeHandler();
    private ILanguage                  m_Language = null;
    
    public JavaAttributeChangeFacility()
    {
        m_Handler.setChangeHandlerUtilities(m_Utils);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaAttributeChangeFacility#added(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void added(IAttribute attr)
    {
        if (attr == null) return;
        
        IClassifier cl = attr instanceof INavigableEnd?
                         ((INavigableEnd) attr).getReferencingClassifier()
                       : attr.getFeaturingClassifier();
        m_Handler.added(attr, true, cl);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaAttributeChangeFacility#deleted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void deleted(IAttribute attr, IClassifier classifier)
    {
        if (attr == null || classifier == null) return;
        
        m_Handler.deleted(attr, classifier);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaAttributeChangeFacility#nameChanged(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void nameChanged(IAttribute attr)
    {
        if (attr == null) return;
        
        IClassifier cl = attr.getFeaturingClassifier();
        m_Handler.nameChange(attr, cl);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaAttributeChangeFacility#typeChanged(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void typeChanged(IAttribute attr)
    {
        if (attr == null) return ;
        
        IClassifier c = attr.getFeaturingClassifier();
        m_Handler.typeChange(attr, c);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaAttributeChangeFacility#getLanguage()
     */
    public ILanguage getLanguage()
    {
        if (m_Language == null)
        {
            ICoreProduct product = ProductRetriever.retrieveProduct();
            m_Language = product.getLanguageManager().getLanguage("Java");
        }
        return m_Language;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.AttributeChangeFacility#preChangeNavigableToAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd)
     */
    protected void preChangeNavigableToAttribute(INavigableEnd end,
                IAttribute attr)
    {
        if (end == null) return ;

        m_Utils.changeAttributeOfAccessors(end, attr);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.AttributeChangeFacility#preChangeAttributeToNavigable(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd)
     */
    protected void preChangeAttributeToNavigable(IAttribute attr,
            INavigableEnd end)
    {
        if (attr == null || end == null) return;
        
        m_Utils.changeAttributeOfAccessors(attr, end);
    }
    
    // IZ 80035: conover
    // added this method so that MemberInfo in ide-integration could call it
    // similar to typeChanged method
    public void multiplicityChanged(IAttribute attr)
    {
        if (attr == null) 
            return;
        
        IClassifier c = attr.getFeaturingClassifier();
        m_Handler.multiplicityChange(attr, c);
    }
    
}
