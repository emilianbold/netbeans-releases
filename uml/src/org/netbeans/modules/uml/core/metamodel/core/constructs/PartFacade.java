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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectorEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.Part;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Interface;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class PartFacade 
    extends org.netbeans.modules.uml.core.metamodel.core.constructs.Class 
    implements IPartFacade
{
    private IClassifier m_Owner = null;

    private IPart       m_Part    = new Part();
    private IInterface  m_Inf     = new Interface();
    private IActor      m_Act     = new Actor();
    private IUseCase    m_UseCase = new UseCase();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_Part.setNode(n);
        m_Inf.setNode(n);
        m_Act.setNode(n);
        m_UseCase.setNode(n);
    }
    
    public String getExpandedElementType()
    {
        String elemType = getElementType();
        String typeConstraint = getTypeConstraint();
        
        if(elemType != null)
        {
            if(typeConstraint != null && typeConstraint.length() > 0)
            {
				elemType += "_";
                elemType += typeConstraint;
            }
        }
        
        return elemType;
    }
    
    public IClassifier getFeaturingClassifier()
    {
        if(m_Owner != null)
        {
            return m_Owner;
        }
        else
        {
            return m_Part.getFeaturingClassifier();
        }
    }
    
    
    public void setFeaturingClassifier(IClassifier classifier)
    {
        if(classifier instanceof IPartFacade)
        {
            IPartFacade pFace = (IPartFacade)classifier;
            
            if(this.isSame(pFace))
            {
                m_Owner = classifier;
            }
            else
            {
                m_Owner = null;
                m_Part.setFeaturingClassifier(classifier);
            }
        }
        else
        {
            m_Owner = null;
            m_Part.setFeaturingClassifier(classifier);
        }
    }

    public void establishNodeAttributes( org.dom4j.Element node )
    {
        super.establishNodeAttributes(node);
        setFeaturingClassifier(this);  
    }
    
    public void establishNodePresence( Document doc , Node node )
    {
        buildNodePresence( "UML:PartFacade", doc, node );
    }
    
    public IVersionableElement performDuplication()
    {
        IVersionableElement pElem = super.performDuplication();
        return pElem;
    }
    
    public void performDependentElementCleanUp (IVersionableElement pElem)
    {
        super.performDependentElementCleanup(pElem);
    }
    
    
    //////////////////////// IPart delegate methods ///////////////////////////
    public boolean getIsSet() {
        return m_Part.getIsSet();
    }

    public boolean getIsStatic() {
        return m_Part.getIsStatic();
    }

    public boolean getIsVolatile() {
        return m_Part.getIsVolatile();
    }

    public boolean getIsWhole() {
        return m_Part.getIsWhole();
    }

    public IMultiplicity getMultiplicity() {
        return m_Part.getMultiplicity();
    }

    public IClassifier getType() {
        return m_Part.getType();
    }

    public IStructuralFeature getDefiningFeature() {
        return m_Part.getDefiningFeature();
    }

    public ETList<IConnectorEnd> getEnds() {
        return m_Part.getEnds();
    }

    public ETList<IStructuredClassifier> getRoleContexts() {
        return m_Part.getRoleContexts();
    }

    public int getClientChangeability() {
        return m_Part.getClientChangeability();
    }

    public int getInitialCardinality() {
        return m_Part.getInitialCardinality();
    }

    public int getOrdering() {
        return m_Part.getOrdering();
    }

    public int getPartKind() {
        return m_Part.getPartKind();
    }

    public String getTypeID() {
        return m_Part.getTypeID();
    }

    public String getTypeName() {
        return m_Part.getTypeName();
    }

    public void addEnd(IConnectorEnd end) {
        m_Part.addEnd(end);
    }

    public void addRoleContext(IStructuredClassifier classifier) {
        m_Part.addRoleContext(classifier);
    }

    public IFeature duplicateToClassifier(IClassifier classifier) {
        return m_Part.duplicateToClassifier(classifier);
    }

    public void moveToClassifier(IClassifier classifier) {
        m_Part.moveToClassifier(classifier);
    }

    public void removeEnd(IConnectorEnd end) {
        m_Part.removeEnd(end);
    }

    public void removeRoleContext(IStructuredClassifier classifier) {
        m_Part.removeRoleContext(classifier);
    }

    public void setClientChangeability(int par1) {
        m_Part.setClientChangeability(par1);
    }

    public void setDefiningFeature(IStructuralFeature feature) {
        m_Part.setDefiningFeature(feature);
    }

    public void setInitialCardinality(int par1) {
        m_Part.setInitialCardinality(par1);
    }

    public void setIsSet(boolean par1) {
        m_Part.setIsSet(par1);
    }

    public void setIsStatic(boolean par1) {
        m_Part.setIsStatic(par1);
    }

    public void setIsVolatile(boolean par1) {
        m_Part.setIsVolatile(par1);
    }

    public void setIsWhole(boolean par1) {
        m_Part.setIsWhole(par1);
    }

    public void setMultiplicity(IMultiplicity multiplicity) {
        m_Part.setMultiplicity(multiplicity);
    }

    public void setOrdering(int par1) {
        m_Part.setOrdering(par1);
    }

    public void setPartKind(int par1) {
        m_Part.setPartKind(par1);
    }

    public void setType(IClassifier classifier) {
        m_Part.setType(classifier);
    }

    public void setType2(String string) {
        m_Part.setType2(string);
    }

    public void setTypeName(String string) {
        m_Part.setTypeName(string);
    }


    ///////// IInterface delegate methods /////////
    public INamespace getProtocolStateMachine() {
        return m_Inf.getProtocolStateMachine();
    }

    public void setProtocolStateMachine(INamespace namespace) {
        m_Inf.setProtocolStateMachine(namespace);
    }


    ///////// IUseCase delegate methods /////////
    public IExtensionPoint createExtensionPoint() {
        return m_UseCase.createExtensionPoint();
    }

    public IUseCaseDetail createUseCaseDetail() {
        return m_UseCase.createUseCaseDetail();
    }

    public ETList<IUseCaseDetail> getDetails() {
        return m_UseCase.getDetails();
    }

    public ETList<IExtend> getExtendedBy() {
        return m_UseCase.getExtendedBy();
    }

    public ETList<IExtend> getExtends() {
        return m_UseCase.getExtends();
    }

    public ETList<IExtensionPoint> getExtensionPoints() {
        return m_UseCase.getExtensionPoints();
    }

    public ETList<IInclude> getIncludedBy() {
        return m_UseCase.getIncludedBy();
    }

    public ETList<IInclude> getIncludes() {
        return m_UseCase.getIncludes();
    }

    public void addExtend(IExtend extend) {
        m_UseCase.addExtend(extend);
    }

    public void addExtendedBy(IExtend extend) {
        m_UseCase.addExtendedBy(extend);
    }

    public void addExtensionPoint(IExtensionPoint point) {
        m_UseCase.addExtensionPoint(point);
    }

    public void addInclude(IInclude include) {
        m_UseCase.addInclude(include);
    }

    public void addIncludedBy(IInclude include) {
        m_UseCase.addIncludedBy(include);
    }

    public void addUseCaseDetail(IUseCaseDetail detail) {
        m_UseCase.addUseCaseDetail(detail);
    }

    public void removeExtend(IExtend extend) {
        m_UseCase.removeExtend(extend);
    }

    public void removeExtendedBy(IExtend extend) {
        m_UseCase.removeExtendedBy(extend);
    }

    public void removeExtensionPoint(IExtensionPoint point) {
        m_UseCase.removeExtensionPoint(point);
    }

    public void removeInclude(IInclude include) {
        m_UseCase.removeInclude(include);
    }

    public void removeIncludedBy(IInclude include) {
        m_UseCase.removeIncludedBy(include);
    }

    public void removeUseCaseDetail(IUseCaseDetail detail) {
        m_UseCase.removeUseCaseDetail(detail);
    }

    /**
     * Does this element have an expanded element type or is the expanded element type always the element type?
     */
    public boolean getHasExpandedElementType()
    {
            return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
     */
    public boolean onPreLowerModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
    {
            return m_Part.onPreLowerModified(mult, range, proposedValue);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
     */
    public void onLowerModified(IMultiplicity mult, IMultiplicityRange range) 
    {
            m_Part.onLowerModified(mult, range);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
     */
    public boolean onPreUpperModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
    {
            return m_Part.onPreUpperModified(mult, range, proposedValue);	
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
     */
    public void onUpperModified(IMultiplicity mult, IMultiplicityRange range) 
    {
            m_Part.onUpperModified(mult, range);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
     */
    public boolean onPreRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
    {
            return m_Part.onPreRangeAdded(mult, range);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
     */
    public void onRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
    {
            m_Part.onRangeAdded(mult, range);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
     */
    public boolean onPreRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
    {
            return m_Part.onPreRangeRemoved(mult, range);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
     */
    public void onRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
    {
            m_Part.onRangeRemoved(mult, range);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean)
     */
    public boolean onPreOrderModified(IMultiplicity mult, boolean proposedValue)
    {
            return m_Part.onPreOrderModified(mult, proposedValue);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
     */
    public void onOrderModified(IMultiplicity mult) 
    {
            m_Part.onOrderModified(mult);
    }	

    public void onCollectionTypeModified(IMultiplicity mult, IMultiplicityRange range)
    {
        m_Part.onCollectionTypeModified(mult, range);
    }
    
    public String getRangeAsString()
    {
        return getMultiplicity().getRangeAsString();
    }
}
