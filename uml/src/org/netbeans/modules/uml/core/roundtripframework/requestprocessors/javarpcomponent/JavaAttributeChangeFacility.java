/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
