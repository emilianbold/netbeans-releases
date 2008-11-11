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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.util.Iterator;
import org.dom4j.Attribute;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageDataType;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.ChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IAssociationEndTransformChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.NameManager;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class JavaAttributeChangeHandler extends JavaChangeHandler
        implements IJavaAttributeChangeHandler
{
    
    public JavaAttributeChangeHandler()
    {
        super();
    }
    
    public JavaAttributeChangeHandler(JavaChangeHandler copy)
    {
        super(copy);
    }
    
    public void handleRequest(IRequestValidator requestValidator )
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if (m_Utilities != null)
            {
                RequestDetails details = m_Utilities.getRequestDetails(
                        requestValidator.getRequest());
                if(details != null)
                {
                    int cType = details.changeKind;
                    int cDetail = details.requestDetailKind;
                    int eType = details.rtElementKind;
                    
                    if (cDetail != RequestDetailKind.RDT_TRANSFORM)
                    {
                        if (eType == RTElementKind.RCT_ATTRIBUTE ||
                                eType == RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE )
                        {
                            added(requestValidator, cType, cDetail);
                            deleted(requestValidator, cType, cDetail);
                            visibilityChange(requestValidator, cType, cDetail);
                            nameChange(requestValidator, cType, cDetail);
                            moved(requestValidator, cType, cDetail);
                            typeChange(requestValidator, cType, cDetail);
                            initialValueChange(requestValidator, cType, cDetail);
                            staticChange(requestValidator, cType, cDetail);
                            arraySpecifierChange(requestValidator, cType, cDetail);
                            multiplicityChange(requestValidator, cType, cDetail);
                            finalChange(requestValidator, cType, cDetail);
                        }
                        else if ( cDetail == RequestDetailKind.RDT_FEATURE_DUPLICATED )
                        {
                            duplicated(requestValidator, cType, cDetail);
                        }
                        else if ( eType == RTElementKind.RCT_RELATION )
                        {
                            IElement pReqElement = m_Utilities.getElement
                                    (requestValidator.getRequest(), false);
                            if ( pReqElement != null )
                            {
                                String elemType = pReqElement.getElementType();
                                
                                if ( elemType != null &&
                                        (elemType.equals("Association") ||
                                        elemType.equals("Aggregation") ||
                                        elemType.equals("Composition")) )
                                {
                                    added(requestValidator, cType, cDetail);
                                    deleted(requestValidator, cType, cDetail);
                                    visibilityChange(requestValidator, cType, cDetail);
                                    nameChange(requestValidator, cType, cDetail);
                                    moved(requestValidator, cType, cDetail);
                                    typeChange(requestValidator, cType, cDetail);
                                    initialValueChange(requestValidator, cType, cDetail);
                                    staticChange(requestValidator, cType, cDetail);
                                    arraySpecifierChange(requestValidator, cType, cDetail);
                                    multiplicityChange(requestValidator, cType, cDetail);
                                }
                                else
                                {
                                    if (requestValidator.getRequest() != null)
                                    {
                                        IRelationProxy relation = requestValidator.getRequest().getRelation();
                                        if ( relation != null )
                                            associationChanged(requestValidator, cType, cDetail);
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        // transform? This can only be an end transform. An attribute
                        // is either being added or deleted.
                        added(requestValidator, cType, cDetail);
                        deleted(requestValidator, cType, cDetail);
                    }
                }
            }
        }
    }
    
    /**
     *
     * If the attributes class is a class, make it conform.
     *
     * @param pAttribute[in]
     * @param pClass[in]
     *
     * @return
     *
     */
    public void transformToEnumeration(IAttribute pAttribute, IClassifier pClass)
    {
        if (pAttribute != null)
        {
            IClassifier localClass = pClass;
            if (localClass == null)
            {
                localClass = m_Utilities.getClassOfAttribute(pAttribute);
            }
            
            if (localClass != null)
            {
                boolean isFinal = pAttribute.getIsFinal();
                boolean isStatic = pAttribute.getIsStatic();
                int visibility = pAttribute.getVisibility();
                
                if((isFinal == true) && (isStatic == true) &&
                        (visibility == IVisibilityKind.VK_PUBLIC))
                {
                    //TODO transform to literal
                }
                else
                {
                    transformToClass(pAttribute, pClass);
                }
            }
        }
    }
    
    /**
     *
     * If the attributes class is a class, make it conform.
     *
     * @param pAttribute[in]
     * @param pClass[in]
     *
     * @return
     *
     */
    public void transformToClass(IAttribute pAttribute, IClassifier pClass)
    {
        if (pAttribute != null)
        {
            IClassifier localClass = pClass;
            if (localClass == null)
                localClass = m_Utilities.getClassOfAttribute(pAttribute);
            if (localClass != null)
            {
                pAttribute.setIsFinal(false);
                pAttribute.setIsStatic(false);
                pAttribute.setVisibility(IVisibilityKind.VK_PRIVATE);
                // Create accessors
                added(pAttribute, true, pClass);
            }
        }
    }
    
    protected void added(IRequestValidator requestValidator, int cType, int cDetail )
    {
        try
        {
            if ( requestValidator != null && requestValidator.getValid())
            {
                // First, determine if this is an attribute create
                if ( cType == ChangeKind.CT_CREATE )
                {
                    boolean valid = true;
                    IAttribute pAttribute = null;
                    IClassifier pClass = null;
                    ETPairT<IAttribute, IClassifier> operClass =
                            m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
                    if (operClass != null)
                    {
                        pAttribute = operClass.getParamOne();
                        pClass = operClass.getParamTwo();
                    }
                    
                    if ( pAttribute != null )
                    {
                        valid = addAttribute( requestValidator, cType, cDetail, pAttribute, pClass, valid);
                    }
                    if ( cDetail == RequestDetailKind.RDT_RELATION_CREATED && valid )
                    {
                        // Try to add the other end
                        INavigableEnd pNotThisEnd = pAttribute instanceof INavigableEnd? (INavigableEnd) pAttribute : null;
                        if (pNotThisEnd != null)
                        {
                            pClass = null;
                            pAttribute = null;
                            operClass = m_Utilities.getAttributeAndClass(
                                    requestValidator.getRequest(), pNotThisEnd, false);
                            if (operClass != null)
                            {
                                pAttribute = operClass.getParamOne();
                                pClass = operClass.getParamTwo();
                            }
                            if ( pAttribute != null )
                            {
                                valid = addAttribute( requestValidator, cType, cDetail, pAttribute, pClass, valid);
                            }
                        }
                    }
                    requestValidator.setValid(valid);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected boolean addAttribute(IRequestValidator requestValidator, int cType, int cDetail,
            IAttribute pAttribute, IClassifier pClass, boolean valid)
    {
        // If the attribute is still unnamed at this point, invalidate
        // the event so that no one receives it.
        
        boolean doTheAdd = true;
        
        if ( cDetail == RequestDetailKind.RDT_RELATION_CREATED )
            doTheAdd = false;
        
        if ( m_Utilities.isElementUnnamed( pAttribute ))
        {
            // If the attribute is really a navigable end, and the preference
            // says to name it, name it now and do a normal add.
            valid = addNavigableEndAttribute( requestValidator, pAttribute, pClass, valid );
        }
        else if ( doTheAdd )
        {
            added( pAttribute, valid, pClass );
            addDependency( requestValidator, pAttribute, pClass );
        }
        
        return valid;
    }
    
    public boolean addNavigableEndAttribute(IRequestValidator requestValidator, IAttribute pAttribute,
            IClassifier pClass, boolean valid )
    {
        try
        {
            INavigableEnd pEnd = pAttribute instanceof INavigableEnd? (INavigableEnd) pAttribute : null;
            if (pEnd != null)
            {
                // Using the LocalClass pattern means the user
                // does not have to get the class off the attribute, we will
                // do it here. This is a nice pattern to have in any doit function.
                
                IClassifier pLocalClass = null;
                if ( pClass != null )
                {
                    pLocalClass = pClass;
                }
                else
                {
                    pLocalClass = m_Utilities.getClassOfAttribute(pAttribute);
                }
                if ( pLocalClass != null )
                {
                    if ( m_Utilities.autoNameNavigableEndPreference() )
                    {
                        boolean success = nameNavigableEnd(pEnd);
                        if ( success )
                        {
                            addDependency( requestValidator, pAttribute, pLocalClass );
                            added( pAttribute, valid, pLocalClass);
                        }
                        
                        // TODO : might need to invalidate anyway because
                        // the name change will cause a request, and we
                        // dont want two going out.
                        valid = false;
                    }
                    else
                    {
                        valid = false;
                    }
                }
                else
                {
                    valid = false;
                }
            }
            else
            {
                valid = false;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return valid;
    }
    
    public boolean nameNavigableEnd(INavigableEnd pEnd)
    {
        boolean success = false;
        if (pEnd != null)
        {
            IClassifier pClass = pEnd.getParticipant();
            if (pClass != null)
            {
                if ( !m_Utilities.isElementUnnamed( pClass ) )
                {
                    // Use this class as the type, which will be part of the default name.
                    String typeName = pClass.getName();
                    String prefix = m_Utilities.attributePrefix();
                    String attrName = prefix + stripGenericBrackets(typeName);
                    
                    success = ensureUniqueRoleName(pEnd, attrName, attrName, 0);
                }
            }
        }
        return success;
    }
    
    private final static String stripGenericBrackets(String type)
    {
        // 102494, strip out <String, String>
        int begin = type.indexOf("<");
        if (begin > -1)
            return type.substring(0, begin).trim();
        return type;
//        return StringTokenizer2.replace(
//            StringTokenizer2.replace(type, "<", ""), ">", "");
    }
    
    protected boolean ensureUniqueRoleName( INavigableEnd pEnd, String origRoleName,
            String roleName, int count)
    {
        return NameManager.ensureUniqueRoleName( pEnd, origRoleName, roleName, count );
    }
    
    public void added( IAttribute pAttribute, boolean valid, IClassifier pClass)
    {
        IClassifier pLocalClass = pClass;
        if (pLocalClass == null)
            pLocalClass = getClassifier(pAttribute);
        if ( pLocalClass != null )
        {
            // If the class is an interface, the attribute
            // MUST BE static, final, and public, and have
            // and initial value.
            transformToInterface(pAttribute, pLocalClass);
            
            boolean isFinal = pAttribute.getIsFinal();
            boolean isStatic = pAttribute.getIsStatic();
            
            // Ok, we are saying the following:
            // If a user creates a static/final attribute, he
            // MOST LIKELY is using it as an enum, so no getters or setters.
            // If an attribute is final it by definition cannot have setters.
            
            boolean createGetter = true;
            boolean createSetter = true;
            if (isFinal)
            {
                // this is enforced by the language.
                createSetter = false;
                if ( isStatic )
                {
                    // This is not enforced, just a common default
                    createGetter = false;
                }
            }
            
            // Finally, set the default value if we have multiplicity and we haven't
            // already done so in the TransformToInterface call. Make sure that
            // we have set the multiplicity before the getters and setters are
            // created so that they create correctly.
            
            boolean force = false; // This makes sure that we don't erase something the user entered manually.
            setMultipleInitialValue( pAttribute, force );
            
            valid = true;
            
            //Jyothi: Test if the getters/setters already exists (Fix for Bug#6327840 )
            boolean getterExists = false;
            getterExists = m_Utilities.doesGetterExist(pAttribute, pClass);
            if (getterExists)
            {
                createGetter = false;
            }
            boolean setterExists = false;
            setterExists = m_Utilities.doesSetterExist(pAttribute, pClass);
            if (setterExists)
            {
                createSetter = false;
            }
            
            if ( createGetter )
            {
                m_Utilities.createReadAccessor( pAttribute, pLocalClass, force );
            }
            
            if ( valid && createSetter )
            {
                m_Utilities.createWriteAccessor( pAttribute, pLocalClass, force );
            }
        }
    }
    
    /**
     *
     * If the attribute has mulitplicity, we set the initial value to "new xxx[n]"
     *
     * @param pAttribute[in]
     * @param force[in] If set to true, the current initial value is cleared.
     *                  The default for this is true, but is set to false when
     *                  in Added, so that only a new VALID initializer is created.
     */
    public void setMultipleInitialValue(IAttribute pAttribute, boolean force)
    {
        if (pAttribute != null)
        {
            // Get the attributes multiplicity.
            IMultiplicity pMult = pAttribute.getMultiplicity();
            if ( pMult != null )
            {
                ETList<IMultiplicityRange> ranges = pMult.getRanges();
                if ( ranges != null )
                {
                    boolean invalid = false;
                    int count = ranges.size();
                    Iterator<IMultiplicityRange> iter = ranges.iterator();
                    if (iter != null)
                    {
                        String formatRanges = "";
                        while (iter.hasNext() && !invalid)
                        {
                            IMultiplicityRange pItem = iter.next();
                            if ( pItem != null )
                            {
                                // <sarcasm>
                                // Conveniently, MultiplicityRange returns the strings for
                                // the lower and upper bounds, but most folks are going
                                // to be interested in the integer values.
                                // </sarcasm>
                                
                                ETPairT<String, String> bothRanges = pItem.getRange();
                                String lowerRange = null;
                                String upperRange = null;
                                if (bothRanges != null)
                                {
                                    lowerRange = bothRanges.getParamOne();
                                    upperRange = bothRanges.getParamTwo();
                                }
                                invalid = true;
                                if (lowerRange != null && lowerRange.length() > 0
                                        && upperRange != null && upperRange.length() > 0)
                                {
                                    // Ok, if we cannot interpret EITHER bound as a natural
                                    // number, we will conclude that this attribute cannot be
                                    // initialized at this time (in other words, it is
                                    // created dynamically in code).
                                    
                                    //AZTEC: TODO: do it now
                                }
                                
                            }
                        }
                        
                        // If we have a valid format, set the initial value
                        
                        String langName = null;
                        if ( !invalid && count > 0 )
                        {
                            String initValue = "new ";
                            String attrType = pAttribute.getTypeName();
                            initValue += attrType;
                            initValue += formatRanges;
                            
                            ETPairT<String, String> pair = pAttribute.getDefault3();
                            String currentIVLang = null;
                            if (pair != null)
                                currentIVLang = pair.getParamOne();
                            
                            if ( currentIVLang == null || currentIVLang.length() == 0 )
                            {
                                langName = getLanguageName();
                            }
                            else
                            {
                                langName = currentIVLang;
                            }
                            
                            pAttribute.setDefault3(langName, initValue);
                        }
                        else if ( force && !invalid && count == 0 )
                        {
                            // we have been told to definitely removed the existing iv.
                            if ( langName == null || langName.length() == 0 )
                            {
                                langName = getLanguageName();
                            }
                            pAttribute.setDefault3(langName, pAttribute.getDefault2());
                        }
                    }
                }
            }
        }
    }
    
    protected IClassifier getClassifier(IAttribute pAttribute)
    {
        IClassifier retClass = null;
        try
        {
            if (pAttribute != null)
            {
                INavigableEnd pNavEnd = pAttribute instanceof INavigableEnd? (INavigableEnd) pAttribute : null;
                if (pNavEnd == null)
                {
                    retClass = pAttribute.getFeaturingClassifier();
                }
                else
                {
                    retClass = pNavEnd.getReferencingClassifier();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return retClass;
    }
    
    public void transformToInterface( IAttribute pAttribute, IClassifier pClass)
    {
        if ( pAttribute != null )
        {
            IClassifier pLocalClass = pClass;
            if ( pLocalClass == null )
            {
                pLocalClass = m_Utilities.getClassOfAttribute( pAttribute );
            }
            
            // If the class is an interface, the attribute
            // MUST BE static, final, and public, and have
            // and initial value.
            //
            // Note: We used to make this check by QI pLocalClass
            //       to see if it supported the IInterface, but that
            //       is not sufficient 'cause other types could support
            //       the IInterface interface but should not be treated
            //       as interfaces, such as the IPartFacade.
            
            String elementType = pLocalClass.getElementType();
            String constraint = null;
            if ( pClass instanceof IParameterableElement )
            {
                constraint = ((IParameterableElement)pClass).getTypeConstraint();
            }
            // Fix for #5085331
            boolean interfaceRole = "PartFacade".equals(elementType) && "Interface".equals( constraint );
            if ( elementType != null &&
                    ( elementType.equals("Interface") || interfaceRole ) )
            {
                boolean isFinal = true;
                boolean isStatic = true;
                int vis = IVisibilityKind.VK_PUBLIC;
                
                // We can just turn off round trip here, 'cause
                // a context is already going to go out with the pre-state
                // of the attribute, so the code gen listeners should always
                // be generating code from the after element, which we will
                // be modifying right here.
                turnRoundTripOn( false );
                
                pAttribute.setIsFinal( isFinal);
                pAttribute.setIsStatic( isStatic);
                pAttribute.setVisibility( vis);
                
                setDefaultInitialValue(pAttribute);
                
                // Delete all existing accessors
                
                ETList<IOperation> writeAccessors = null;
                ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                        m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
                if (writePair != null)
                    writeAccessors = writePair.getParamOne();
                
                ETList<IOperation> readAccessors =  null;
                ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                        m_Utilities.getReadAccessorsOfAttribute(pAttribute, null);
                if (readPair != null)
                    readAccessors = readPair.getParamOne();
                
                IJavaMethodChangeHandler handler = new JavaMethodChangeHandler(this);
                handler.deleteList( writeAccessors, true);
                handler.deleteList( readAccessors, true);
                
                
                turnRoundTripOn( true );
            }
        }
    }
    
    public void setDefaultInitialValue(IAttribute pAttribute)
    {
        if ( pAttribute != null )
        {
            IRequestProcessor pProc = getProcessor();
            if ( pProc != null )
            {
                ETPairT<String, String> pair = pAttribute.getDefault3();
                String currentIVLang = null;
                String currentIV = null;
                if (pair != null)
                {
                    currentIVLang = pair.getParamOne();
                    currentIV = pair.getParamTwo();
                }
                
                if ( currentIV == null || currentIV.length() == 0 )
                {
                    String langName = null;
                    String initValue = "null";
                    ILanguage pLang = pProc.getLanguage2();
                    if ( pLang != null )
                    {
                        langName = pLang.getName();
                        String attrType = pAttribute.getTypeName();
                        
                        boolean isPrimitive = pLang.isPrimitive(attrType);
                        if ( isPrimitive )
                        {
                            ILanguageDataType pDataType = pLang.getDataType(attrType);
                            if ( pDataType != null )
                            {
                                initValue = "";
                                initValue = pDataType.getDefaultValue();
                            }
                        }
                    }
                    
                    pAttribute.setDefault3( langName, initValue );
                }
            }
        }
    }
    
    protected void deleted(IRequestValidator requestValidator, int cType, int cDetail )
    {
        try
        {
            if ( requestValidator != null && requestValidator.getValid() &&
                    requestValidator.getRequest() != null)
            {
                // First, determine if this is an attribute create
                if ( cType == ChangeKind.CT_DELETE )
                {
                    IAttribute pAttribute = null;
                    IClassifier pClass = null;
                    ETPairT<IAttribute, IClassifier> operClass =
                            m_Utilities.getAttributeAndClass(requestValidator.getRequest(),true);
                    if (operClass != null)
                    {
                        pAttribute = operClass.getParamOne();
                        pClass = operClass.getParamTwo();
                    }
                    
                    if ( pAttribute != null )
                    {
                        boolean valid = true;
                        deleted( pAttribute, pClass);
                        
                        // if the request is really for an association deletion, we want to invalidate the
                        // request and build an attribute delete request
                        INavigableEnd pEnd = pAttribute instanceof INavigableEnd? (INavigableEnd) pAttribute : null;
                        if ( pEnd != null )
                        {
                            // DON'T DO THIS IF THE REQUEST IS A TRANSFORM
                            // This is because we cannot get the referencing
                            // classifier from a cloned navigable end.  So the
                            // transform request has stored it for us.
                            // This means that the listeners have to interpret
                            // the transform as a delete. I cannot give them a
                            // delete request.
                            
                            IChangeRequest req = requestValidator.getRequest();
                            IAssociationEndTransformChangeRequest pTransform = req instanceof IAssociationEndTransformChangeRequest? (IAssociationEndTransformChangeRequest) req : null;
                            if ( pTransform == null )
                            {
                                IChangeRequest pNewRequest = new ChangeRequest();
                                if ( pNewRequest != null )
                                {
                                    pNewRequest.setState( ChangeKind.CT_DELETE );
                                    pNewRequest.setRequestDetailType(RequestDetailKind.RDT_ELEMENT_DELETED );
                                    pNewRequest.setLanguage("Java");
                                    
                                    pNewRequest.setBefore(pAttribute);
                                    pNewRequest.setAfter(pAttribute);
                                    
                                    IEventPayload pPayload = requestValidator.getRequest().getPayload();
                                    pNewRequest.setPayload(pPayload);
                                    
                                    // Invalidate the current request and add this new one to it.
                                    
                                    valid = false;
                                    requestValidator.addRequest(pNewRequest);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    public void deleted(IAttribute pAttribute, IClassifier pClass)
    {
        // Get the read and write accessors of the attribute and delete them
        // if the user wishes.
        
        ETList<IOperation> writeAccessors = null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
        if (writePair != null)
            writeAccessors = writePair.getParamOne();
        
        ETList<IOperation> readAccessors =  null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                m_Utilities.getReadAccessorsOfAttribute(pAttribute, null);
        if (readPair != null)
            readAccessors = readPair.getParamOne();
        
        
        m_Utilities.breakReadAccessorsOfAttribute(pAttribute);
        m_Utilities.breakWriteAccessorsOfAttribute(pAttribute); 
        
        if ( ( readAccessors != null && readAccessors.size() > 0 )||
                ( writeAccessors != null && writeAccessors.size() > 0 ) )
        {
            boolean deleteAccessors = true;
            if (pClass != null && !pClass.isDeleted())
            {
                deleteAccessors = queryUserBeforeDelete(pAttribute);
            }
            
            if ( deleteAccessors )
            {
                final IJavaMethodChangeHandler handler = new JavaMethodChangeHandler(this);
                final ETList<IOperation> wAccessors = writeAccessors;
                final ETList<IOperation> rAccessors = readAccessors;
                
                // [TODO]
                // temporal workaround for #6309146: deletion of accessors needs to be delayed,
                // ideally it should be synchronized with EventHandler in the ideintegration module
                // but currently there is no such a mechanism
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(850);
                        }
                        catch (InterruptedException e)
                        {
                        }
                        handler.deleteList(wAccessors, true);
                        handler.deleteList(rAccessors, true);
                    }
                };
                RequestProcessor.getDefault().post(r);
            }
        }
    }
    
    protected boolean queryUserBeforeDelete(IAttribute pAttribute)
    {
        if (pAttribute.getOwner() instanceof IInterface)
            return false;
        
        boolean deleteAccessors = false;
        try
        {
            if (pAttribute != null)
            {
                String queryKey = null;
                String attrName = pAttribute.getName();
                INavigableEnd pEnd = pAttribute instanceof INavigableEnd? (INavigableEnd) pAttribute : null;
                if ( pEnd == null )
                {
                    queryKey = "DELETE";
                }
                else
                {
                    queryKey = "DELETE_END";
                }
                deleteAccessors = doQuery(queryKey, attrName);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return deleteAccessors;
    }
    
    protected void multiplicityChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( requestValidator != null && requestValidator.getValid())
        {
            if (cDetail == RequestDetailKind.RDT_RANGE_ADDED ||
                    cDetail == RequestDetailKind.RDT_RANGE_REMOVED ||
                    cDetail == RequestDetailKind.RDT_LOWER_MODIFIED ||
                    cDetail == RequestDetailKind.RDT_UPPER_MODIFIED ||
                    cDetail == RequestDetailKind.RDT_COLLECTION_TYPE_CHANGED)
            {
                IAttribute pAttribute = null;
                IClassifier pClass = null;
                ETPairT<IAttribute, IClassifier> operClass =
                        m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pAttribute = operClass.getParamOne();
                    pClass = operClass.getParamTwo();
                }
                multiplicityChange(pAttribute, pClass);
            }
        }
    }
    
    public void multiplicityChange(IAttribute pAttribute, IClassifier pClass)
    {
        IClassifier pLocalClass = pClass;
        if (pLocalClass == null)
            pLocalClass = getClassifier(pAttribute);
        if ( pLocalClass != null )
        {
            // Make sure that the initial value is correct
            setMultipleInitialValue(pAttribute, true);
            
            // We need to make sure that any parameters of the getters and setters
            // are appropriately tagged with the right number of ranges.
            ETList<IOperation> setters = null;
            ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                    m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
            if (writePair != null)
                setters = writePair.getParamOne();
            
            ETList<IOperation> getters =  null;
            ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                    m_Utilities.getReadAccessorsOfAttribute(pAttribute, null);
            if (readPair != null)
                getters = readPair.getParamOne();
            
            // Notice that we do this in all cases because the range might have
            // been deleted, so numberOfRanges is NOW 0, but wasn't previously.
            IMultiplicity pMult = pAttribute.getMultiplicity();
            
            ETList < IMultiplicityRange > ranges = pMult.getRanges();
            boolean canProcess = true;
            if((ranges != null) && (ranges.size() == 1))
            {
                IMultiplicityRange range = ranges.get(0);
                if(range != null)
                {
                    String lower = range.getLower();
                    String upper = range.getUpper();
                    
                    if((lower.equals("1") == true) && (upper.equals("1") == true))
                    {
                        canProcess = false;
                    }
                }
            }
            
            if(canProcess == true)
            {
                if (getters != null)
                {
                    Iterator<IOperation> getIter = getters.iterator();
                    if (getIter != null)
                    {
                        while (getIter.hasNext())
                        {
                            IOperation pItem = getIter.next();
                            if ( pItem != null )
                            {
                                IParameter pParm = pItem.getReturnType();
                                if ( pParm != null )
                                    setMultiplicity(pParm, pMult);
                            }
                        }
                    }
                }
                
                if (setters != null)
                {
                    Iterator<IOperation> setIter = setters.iterator();
                    if (setIter != null)
                    {
                        while (setIter.hasNext())
                        {
                            IOperation pItem = setIter.next();
                            if ( pItem != null )
                            {
                                ETList<IParameter> parms = pItem.getFormalParameters();
                                if ( parms != null )
                                {
                                    if (parms.size() == 1)
                                    {
                                        IParameter pParm = parms.get(0);
                                        if (pParm != null)
                                            setMultiplicity(pParm, pMult);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * Make the parameter's multiplicity correspond to the passed in multiplicity.
     * Most likely, the passed in multiplicity is from an attribute, and the
     * parameter is from an accessor of this attribute.
     *
     * @param pParm[in]
     * @param pMult[in]
     */
    protected void setMultiplicity(IParameter pParm, IMultiplicity pMult)
    {
        if (pMult != null && pParm != null)
        {
            IMultiplicity pParmMult = pParm.getMultiplicity();
            if ( pParmMult != null )
            {
                pParmMult.removeAllRanges();
                ETList < IMultiplicityRange > ranges = pMult.getRanges();
                for (int i=0; i<ranges.size(); i++)
                {
                    IMultiplicityRange pNewRange = pParmMult.createRange();
                    
                    pNewRange.setRange(((IMultiplicityRange)ranges.get(i)).getLower(),
                            ((IMultiplicityRange)ranges.get(i)).getUpper());
                    pNewRange.setCollectionType(ranges.get(i).getCollectionType(false));
                    pParmMult.addRange(pNewRange);
                }
            }
        }
    }
    
    protected void arraySpecifierChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        //C++ method is empty
    }
    
    protected void visibilityChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        //C++ method is empty.
    }
    
    protected void staticChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        //C++ method is empty.
    }
    
    protected void finalChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( cType == ChangeKind.CT_MODIFY  && cDetail == RequestDetailKind.RDT_FINAL_MODIFIED )
        {
            IAttribute pAttribute = null;
            
            ETPairT<IAttribute, IClassifier> operClass =
                    m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
            if (operClass != null)
            {
                pAttribute = operClass.getParamOne();
            }
            
            if (pAttribute == null)
                return;
                       
            ETList<IOperation> setters =  null;
            ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                    m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
            if (writePair != null)
                setters = writePair.getParamOne();
            for (IOperation op: setters)
            {
                op.delete();
            }
            if (!pAttribute.getIsFinal())
            {  
                m_Utilities.createWriteAccessor( pAttribute, getClassifier(pAttribute), false );
            }
        }
    }
    
    
    protected void initialValueChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        //C++ method is empty.
    }
    
    protected void typeChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( requestValidator != null && requestValidator.getValid())
        {
            // First, determine if this is an attribute create
            if ( cType == ChangeKind.CT_MODIFY  && cDetail == RequestDetailKind.RDT_TYPE_MODIFIED )
            {
                IAttribute pAttribute = null;
                IClassifier pClass = null;
                ETPairT<IAttribute, IClassifier> operClass =
                        m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pAttribute = operClass.getParamOne();
                    pClass = operClass.getParamTwo();
                }
                
                if (pAttribute != null && pClass != null)
                {
                    typeChange(pAttribute, pClass);
                    addDependency(requestValidator, pAttribute, pClass);
                }
            }
        }
    }
    
    public void typeChange(IAttribute pAttribute, IClassifier pClass)
    {
        IClassifier pLocalClass = pClass;
        if (pLocalClass == null)
            pLocalClass = getClassifier(pAttribute);
        
        if ( pLocalClass != null )
        {
            retypeReadAccessor(pAttribute, pLocalClass);
            retypeWriteAccessor(pAttribute, pLocalClass );
        }
    }
    
    protected void retypeReadAccessor( IAttribute pAttribute, IClassifier pClass )
    {
        ETList<IOperation> accessors =  null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                m_Utilities.getReadAccessorsOfAttribute(pAttribute, null);
        if (readPair != null)
            accessors = readPair.getParamOne();
        
        if (accessors != null)
        {
            String attrType = pAttribute.getTypeName();
            if (attrType != null && attrType.trim().length() > 0)
            {
                IJavaMethodChangeHandler handler = new JavaMethodChangeHandler(this);
                Iterator<IOperation> iter = accessors.iterator();
                if (iter != null && accessors.size() < 2)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Retype the operation, then perform the request processor
                            // stuff associated with renaming an operation.
                            m_Utilities.setOperationReturnType(pOp, attrType);
                            handler.typeChange(pOp);
                        }
                    }
                }
            }
        }
    }
    
    protected void retypeWriteAccessor( IAttribute pAttribute, IClassifier pClass )
    {
        ETList<IOperation> accessors = null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
        if (writePair != null)
            accessors = writePair.getParamOne();
        
        if (accessors != null)
        {
            String attrType = pAttribute.getTypeName();
            if (attrType != null && attrType.trim().length() > 0)
            {
                IJavaMethodChangeHandler handler = new JavaMethodChangeHandler(this);
                Iterator<IOperation> iter = accessors.iterator();
                if (iter != null && accessors.size() < 2)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            IParameter pParm = m_Utilities.getPositionParameter(pOp, 1);
                            
                            // Rename the operation, then perform the request processor
                            // stuff associated with renaming an operation.
                            if (pParm != null)
                            {
                                pParm.setTypeName(attrType);
                                handler.parameterChange(pParm);
                            }
                            
                        }
                    }
                }
            }
        }
    }
    
    protected void nameChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( requestValidator != null && requestValidator.getValid())
        {
            // First, determine if this is an attribute create
            if ( cType == ChangeKind.CT_MODIFY  && cDetail == RequestDetailKind.RDT_NAME_MODIFIED )
            {
                IAttribute pAttribute = null;
                IClassifier pClass = null;
                ETPairT<IAttribute, IClassifier> operClass =
                        m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pAttribute = operClass.getParamOne();
                    pClass = operClass.getParamTwo();
                }
                
                if (pAttribute != null && pClass != null)
                {
                    nameChange(pAttribute, pClass);
                }
            }
        }
    }
    
    public void nameChange(IAttribute pAttribute, IClassifier pClass)
    {
        IClassifier pLocalClass = pClass;
        if (pLocalClass == null)
            pLocalClass = getClassifier(pAttribute);
        
        if ( pLocalClass != null )
        {
            renameReadAccessor(pAttribute, pLocalClass);
            renameWriteAccessor(pAttribute, pLocalClass );
        }
    }
    
    protected void renameReadAccessor( IAttribute pAttribute, IClassifier pClass )
    {
        
        ETList<IOperation> accessors =  null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                m_Utilities.getReadAccessorsOfAttribute(pAttribute, null);
        if (readPair != null)
            accessors = readPair.getParamOne();
        
        if (accessors != null)
        {
            String attrName = pAttribute.getName();
            if (attrName != null && attrName.trim().length() > 0)
            {
                String opName = m_Utilities.readAccessorPrefix();
                String attrNameFix =
                        m_Utilities.capAttributeName(m_Utilities.removePrefixFromAttributeName(attrName));
                
                if (opName != null && attrNameFix != null)
                    opName += attrNameFix;
                
                IJavaMethodChangeHandler handler = new JavaMethodChangeHandler(this);
                Iterator<IOperation> iter = accessors.iterator();
                if (iter != null && accessors.size() < 2)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Rename the operation, then perform the request processor
                            // stuff associated with renaming an operation.
                            pOp.setName(opName);
                            handler.nameChange(pOp);
                        }
                    }
                }
            }
        }
    }
    
    protected void renameWriteAccessor( IAttribute pAttribute, IClassifier pClass )
    {
        ETList<IOperation> accessors = null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
        if (writePair != null)
            accessors = writePair.getParamOne();
        
        if (accessors != null)
        {
            String attrName = pAttribute.getName();
            if (attrName != null && attrName.trim().length() > 0)
            {
                String opName = m_Utilities.writeAccessorPrefix();
                String attrNameFix = m_Utilities.capAttributeName(m_Utilities.removePrefixFromAttributeName(attrName));
                
                if (opName != null && attrNameFix != null)
                    opName += attrNameFix;
                
                IJavaMethodChangeHandler handler = new JavaMethodChangeHandler(this);
                Iterator<IOperation> iter = accessors.iterator();
                if (iter != null && accessors.size() < 2)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Rename the operation, then perform the request processor
                            // stuff associated with renaming an operation.
                            pOp.setName(opName);
                            handler.nameChange(pOp);
                        }
                    }
                }
            }
        }
    }
    
    protected void moved(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( requestValidator != null && requestValidator.getValid())
        {
            // First, determine if this is an attribute moved
            if ( cType == ChangeKind.CT_MODIFY  && cDetail == RequestDetailKind.RDT_FEATURE_MOVED )
            {
                IAttribute pNewAttribute = null;
                IClassifier pNewClass = null;
                
                IAttribute pOldAttribute = null;
                IClassifier pOldClass = null;
                
                ETPairT<IAttribute, IClassifier> operClass =
                        m_Utilities.getAttributeAndClass(requestValidator.getRequest(),true);
                if (operClass != null)
                {
                    pOldAttribute = operClass.getParamOne();
                    pOldClass = operClass.getParamTwo();
                }
                
                operClass = null;
                operClass = m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pNewAttribute = operClass.getParamOne();
                    pNewClass = operClass.getParamTwo();
                }
                
                boolean valid = true;
                
                if ( pOldAttribute != null && pNewAttribute != null &&
                        pOldClass != null && pNewClass != null )
                {
                    moved(requestValidator, pOldAttribute, pOldClass, pNewClass);
                }
                
                requestValidator.setValid(valid);
            }
        }
    }
    
    public void moved(IRequestValidator requestValidator, IAttribute pAttribute,
            IClassifier pFromClass, IClassifier pToClass)
    {
        ETList<IOperation> setters =  null;
        ETList<IDependency> writeDeps = null;
        
        ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                m_Utilities.getWriteAccessorsOfAttribute(pAttribute, null);
        if (writePair != null)
        {
            setters = writePair.getParamOne();
            writeDeps = writePair.getParamTwo();
        }
        
        
        ETList<IOperation> getters =  null;
        ETList<IDependency> readDeps = null;
        ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                m_Utilities.getReadAccessorsOfAttribute(pAttribute, null);
        if (readPair != null)
        {
            getters = readPair.getParamOne();
            readDeps = readPair.getParamTwo();
        }
        
        
        // Move the dependency relationships over to the new class
        
        moveDependencies( pAttribute, pToClass, readDeps );
        moveDependencies( pAttribute, pToClass, writeDeps );
        
        // Now just move these operations to the pToClass as well.
        m_Utilities.moveToClass2( getters, pToClass );
        m_Utilities.moveToClass2( setters, pToClass );
        
        // If the new class is unnamed, we want to invalidate the move
        // event and just create a Delete event for the operation from the
        // old class.
        if ( m_Utilities.isElementUnnamed( pToClass ) )
        {
            IAttribute pOldAttr = null;
            IClassifier pTempClass = null;
            
            ETPairT<IAttribute, IClassifier> operClass =
                    m_Utilities.getAttributeAndClass(requestValidator.getRequest(),true);
            if (operClass != null)
            {
                pOldAttr = operClass.getParamOne();
                pTempClass = operClass.getParamTwo();
            }
            
            if ( pOldAttr != null )
            {
                IChangeRequest pNewRequest = m_Utilities.createChangeRequest(
                        null,
                        ChangeKind.CT_DELETE,
                        RequestDetailKind.RDT_ELEMENT_DELETED,
                        pOldAttr,
                        pOldAttr,
                        pFromClass);
                if ( pNewRequest != null )
                {
                    requestValidator.addRequest( pNewRequest );
                    requestValidator.setValid( false );
                }
            }
        }
    }
    
    /**
     *
     * Moves the passed in dependencies into the incoming Classifier
     *
     * @param pToClassifier[in]   The Classifier to own the relationships
     * @param deps[in]            The deps to move
     */
    protected void moveDependencies(IAttribute pAttr, IClassifier pToClassifier,
            ETList<IDependency> deps )
    {
        if( deps != null && pAttr != null)
        {
            int num = deps.size();
            
            String xmiID = pAttr.getXMIID();
            
            String query = "./@*[contains( ., \"" ;
            query += xmiID;
            query += "\")]";
            
            for( int x = 0; x < num; x++ )
            {
                IDependency dep = deps.get(x);
                if( dep != null)
                {
                    // Be sure to cleanse the supplier / client xml attributes to NOT
                    // include the URI information to the passed in attribute. This will
                    // do a complete replace of the attribute value, as we are assuming that
                    // the dependencies coming into this routine are internal relationships.
                    // Specificially, the Realization relationship should only have one client and
                    // one supplier.
                    //
                    // We needed to do this 'cause in the case where the user is dragging the
                    // passed in attribute from one version controlled element to another
                    // version controlled element, we found that the Realization relationship
                    // was referencing the Attribute via incorrect URI decorated XMI ids.
                    
                    Node depNode = dep.getNode();
                    if( depNode != null)
                    {
                        Node attrNode = XMLManip.selectSingleNode(depNode, query);
                        Attribute attr = (Attribute)attrNode;
                        if( attr != null)
                        {
                            attr.setValue(xmiID);
                        }
                    }
                    pToClassifier.addElement( dep );
                }
            }
        }
    }
    
    protected void duplicated(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( requestValidator != null && requestValidator.getValid())
        {
            // First, determine if this is an attribute moved
            if ( cType == ChangeKind.CT_MODIFY  && cDetail == RequestDetailKind.RDT_FEATURE_DUPLICATED )
            {
                IAttribute pNewAttribute = null;
                IClassifier pNewClass = null;
                
                IAttribute pOldAttribute = null;
                IClassifier pOldClass = null;
                
                ETPairT<IAttribute, IClassifier> operClass =
                        m_Utilities.getAttributeAndClass(requestValidator.getRequest(),true);
                if (operClass != null)
                {
                    pOldAttribute = operClass.getParamOne();
                    pOldClass = operClass.getParamTwo();
                }
                
                operClass = null;
                operClass = m_Utilities.getAttributeAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pNewAttribute = operClass.getParamOne();
                    pNewClass = operClass.getParamTwo();
                }
                
                boolean valid = true;
                
                if ( pOldAttribute != null && pNewAttribute != null &&
                        pOldClass != null && pNewClass != null )
                {
                    duplicated(pOldAttribute, pOldClass, pNewAttribute, pNewClass);
                }
                
                requestValidator.setValid(valid);
            }
        }
    }
    
    public void duplicated(IAttribute pFromAttribute, IClassifier pFromClass,
            IAttribute pToAttribute, IClassifier pToClass )
    {
        try
        {
            ETList<IOperation> setters =  null;
            ETPairT<ETList<IOperation>, ETList<IDependency>> writePair =
                    m_Utilities.getWriteAccessorsOfAttribute(pFromAttribute, null);
            if (writePair != null)
                setters = writePair.getParamOne();
            
            ETList<IOperation> getters =  null;
            ETPairT<ETList<IOperation>, ETList<IDependency>> readPair =
                    m_Utilities.getReadAccessorsOfAttribute(pFromAttribute, null);
            if (readPair != null)
                getters = readPair.getParamOne();
            
            // Core dupes too deep. We have to make sure to break realizations here.
            //m_Utilities.breakReadAccessorsOfAttribute(pToAttribute);
            //m_Utilities.breakWriteAccessorsOfAttribute(pToAttribute);
            removeDependencies(pToAttribute);
            
            if (getters != null)
            {
                //create a new set of getters and add them to the ToClass
                Iterator<IOperation> getIter = getters.iterator();
                if (getIter != null)
                {
                    while (getIter.hasNext())
                    {
                        IOperation pItem = getIter.next();
                        if ( pItem != null && pToClass.findMatchingOperation(pItem) == null )
                        {
                            IFeature pNewFeat = pItem.duplicateToClassifier(pToClass);
                            if ( pNewFeat != null )
                            {
                                IOperation pNewOp = pNewFeat instanceof IOperation? (IOperation) pNewFeat : null;
                                if ( pNewOp != null )
                                {
                                    // The dupe was too deep again. Make sure the new op
                                    // is disassociated from the old attr
                                    //m_Utilities.breakReadAccessorFromAttribute(pNewOp);
                                    //m_Utilities.breakWriteAccessorFromAttribute(pNewOp);
                                    removeDependencies(pNewOp);
                                    
                                    // set up the accessor relationship
                                    IDependency pDep = m_Utilities.createRealization
                                            (pToAttribute, pNewOp, pToClass);
                                }
                            }
                        }
                    }
                }
            }
            
            if (setters != null)
            {
                //create a new set of getters and add them to the ToClass
                Iterator<IOperation> setIter = setters.iterator();
                if (setIter != null)
                {
                    while (setIter.hasNext())
                    {
                        IOperation pItem = setIter.next();
                        if ( pItem != null && pToClass.findMatchingOperation(pItem) == null )
                        {
                            IFeature pNewFeat = pItem.duplicateToClassifier(pToClass);
                            if ( pNewFeat != null )
                            {
                                IOperation pNewOp = pNewFeat instanceof IOperation? (IOperation) pNewFeat : null;
                                if ( pNewOp != null )
                                {
                                    // The dupe was too deep again. Make sure the new op
                                    // is disassociated from the old attr
                                    //m_Utilities.breakReadAccessorFromAttribute(pNewOp);
                                    //m_Utilities.breakWriteAccessorFromAttribute(pNewOp);
                                    removeDependencies(pNewOp);
                                    
                                    // set up the accessor relationship
                                    IDependency pDep = m_Utilities.createRealization
                                            (pNewOp, pToAttribute, pToClass);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void removeDependencies(IParameterableElement pFeat)
    {
        if (pFeat != null)
        {
            ((org.dom4j.Element)pFeat.getNode()).setAttributeValue("clientDependency", "");
            ((org.dom4j.Element)pFeat.getNode()).setAttributeValue("supplierDependency", "");
        }
    }
    protected void associationChanged(IRequestValidator requestValidator, int cType, int cDetail )
    {
        try
        {
            if ( requestValidator != null && requestValidator.getValid())
            {
                // First, determine if this is an attribute moved
                if ( cType == ChangeKind.CT_MODIFY  && cDetail == RequestDetailKind.RDT_ASSOCIATION_END_MODIFIED )
                {
                    IChangeRequest request = requestValidator.getRequest();
                    IElement pReqElementBefore = request.getBefore();
                    IElement pReqElementAfter = request.getAfter();
                    IRelationProxy pRel = request.getRelation();
                    
                    IAssociationEnd pAssocBefore = pReqElementBefore instanceof IAssociationEnd? (IAssociationEnd) pReqElementBefore : null;
                    IAssociationEnd pAssocAfter = pReqElementAfter instanceof IAssociationEnd? (IAssociationEnd) pReqElementAfter : null;
                    
                    String relType = m_Utilities.getRelationType(pRel);
                    
                    if ( relType != null && relType.equals("AssociationEnd") )
                    {
                        IElement pFrom = pRel.getFrom();
                        
                        // if this NOT a nav end, but the other end is, we want to move the
                        // attribute.
                        
                        INavigableEnd pNavEnd = pAssocAfter instanceof INavigableEnd? (INavigableEnd) pAssocAfter : null;
                        if ( pNavEnd != null )
                        {
                            requestValidator.setValid( false );
                        }
                        else
                        {
                            IAssociationEnd pOtherEnd = pAssocAfter.getOtherEnd2();
                            INavigableEnd pOtherNavEnd = pOtherEnd instanceof INavigableEnd? (INavigableEnd) pOtherEnd : null;
                            if ( pOtherNavEnd == null )
                            {
                                requestValidator.setValid( false );
                            }
                            else
                            {
                                IAttribute pNavAttr = pOtherNavEnd instanceof IAttribute? (IAttribute) pOtherNavEnd : null;
                                if ( pNavAttr != null)
                                {
                                    IClassifier pOldClass = pAssocBefore.getParticipant();
                                    IClassifier pNewClass = pAssocAfter.getParticipant();
                                    
                                    moved( pNavAttr, pOldClass, pNewClass );
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void moved(IAttribute pAttribute, IClassifier pFromClass, IClassifier pToClass )
    {
        IRequestValidator req = new RequestValidator();
        moved(req, pAttribute, pFromClass, pToClass);
    }
    
    protected void addDependency(IRequestValidator request, IAttribute pAttr,
            IClassifier pDependentClass )
    {
        IClassifier pAttrType = m_Utilities.getType(pAttr);
        super.addDependency(request, pDependentClass, pAttrType);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaChangeHandler#buildQuery(java.lang.String)
     */
    protected IHandlerQuery buildQuery(String key)
    {
        boolean deleteDeepDefault = true;
        return "DELETE_END".equals(key)?
            (inBatch()?
                // we want a dialog with an "apply to all items" button
                new ConditionalHandlerQuery( key,
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_ASSOC_QUERY"),
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_TITLE"),
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_APPLYALL"),
                deleteDeepDefault,
                ErrorDialogIconKind.EDIK_ICONQUESTION,
                getSilent(),
                false ) // not a persistent query, since what kind it is is dependent on mode.
                :
                // normal query dialog
                new HandlerQuery( key,
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_ASSOC_QUERY"),
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_TITLE"),
                deleteDeepDefault,
                ErrorDialogIconKind.EDIK_ICONQUESTION,
                getSilent(),
                false ) // not a persistent query, since what kind it is is dependent on mode.
                )
                : ("DELETE".equals(key)?
                    (inBatch()?
                        // we want a dialog with an "apply to all items" button
                        new ConditionalHandlerQuery( key,
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_ATTR_QUERY"),
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_TITLE"),
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_APPLYALL"),
                deleteDeepDefault,
                ErrorDialogIconKind.EDIK_ICONQUESTION,
                getSilent(),
                false ) // not a persistent query, since what kind it is is dependent on mode.
                // normal query dialog
                :
                        new HandlerQuery( key,
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_ATTR_QUERY"),
                RPMessages.getString("IDS_JRT_DELETE_ACCESSORS_TITLE"),
                deleteDeepDefault,
                ErrorDialogIconKind.EDIK_ICONQUESTION,
                getSilent(),
                false ) // not a persistent query, since what kind it is is dependent on mode.
                )
                : null);
    }
}


