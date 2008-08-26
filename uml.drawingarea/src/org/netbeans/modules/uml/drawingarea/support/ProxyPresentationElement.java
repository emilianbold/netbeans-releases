/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea.support;

import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 * @author treyspiva
 */
public class ProxyPresentationElement extends PresentationElement
{
    private IPresentationElement element = null;
    private String proxyType = "";
    private String xmiId = "";
    
    public ProxyPresentationElement(IPresentationElement element)
    {
        this(element, "");
    }

    public ProxyPresentationElement(IPresentationElement element, String type)
    {
        this.element = element;
        this.proxyType = type;
        this.xmiId = XMLManip.retrieveDCEID();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // IPresentationElement Methods
    
    public IPresentationElement transform(String elemName)
    {
        return element.transform(elemName);
    }

    public void setDisplayElementID(String id)
    {
        element.setDisplayElementID(id);
    }

    public void removeSubject(IElement elem)
    {
        element.removeSubject(elem);
    }

    public boolean isSubject(IElement elem)
    {
        return element.isSubject(elem);
    }

    public boolean isFirstSubject2(String elementXMIID)
    {
        return element.isFirstSubject2(elementXMIID);
    }

    public boolean isFirstSubject(IElement pElement)
    {
        return element.isFirstSubject(pElement);
    }

    public ETList<IElement> getSubjects()
    {
        return element.getSubjects();
    }

    public long getSubjectCount()
    {
        return element.getSubjectCount();
    }

    public String getFirstSubjectsType()
    {
        String retVal = proxyType;
        if((retVal == null) || (retVal.length() <= 0))
        {
            retVal = element.getFirstSubjectsType();
        }
        
        return retVal;
    }

    public IElement getFirstSubject()
    {
        return element.getFirstSubject();
    }

    public String getDisplayElementID()
    {
        return element.getDisplayElementID();
    }

    public IElement addSubject(IElement elem)
    {
        return element.addSubject(elem);
    }

    ////////////////////////////////////////////////////////////////////////////
    // IElement Methods
    
    public String topLevelId()
    {
        return element.topLevelId();
    }

    public void setOwner(IElement elem)
    {
        element.setOwner(elem);
    }

    public void setDocumentation(String doc)
    {
        element.setDocumentation(doc);
    }

    public Object retrieveAppliedStereotype(String name)
    {
        return element.retrieveAppliedStereotype(name);
    }

    public void removeTargetFlow(IFlow flow)
    {
        element.removeTargetFlow(flow);
    }

    public void removeTaggedValue(ITaggedValue tag)
    {
        element.removeTaggedValue(tag);
    }

    public void removeStereotypes()
    {
        element.removeStereotypes();
    }

    public void removeStereotype2(String name)
    {
        element.removeStereotype2(name);
    }

    public void removeStereotype(Object stereotype)
    {
        element.removeStereotype(stereotype);
    }

    public void removeSourceFlow(IFlow flow)
    {
        element.removeSourceFlow(flow);
    }

    public void removeSourceFile(String fileName)
    {
        element.removeSourceFile(fileName);
    }

    public void removeReferredReference(IReference ref)
    {
        element.removeReferredReference(ref);
    }

    public void removeReferencingReference(IReference ref)
    {
        element.removeReferencingReference(ref);
    }

    public void removePresentationElements()
    {
        element.removePresentationElements();
    }

    public void removePresentationElement(IPresentationElement elem)
    {
        element.removePresentationElement(elem);
    }

    public void removeOwnedConstraint(IConstraint constraint)
    {
        element.removeOwnedConstraint(constraint);
    }

    public IElement removeElement(IElement elem)
    {
        return element.removeElement(elem);
    }

    public boolean isPresent(IPresentationElement elem)
    {
        return element.isPresent(elem);
    }

    public boolean isOwnedElement(IElement elem)
    {
        return element.isOwnedElement(elem);
    }

    public boolean isOwnedElement(String id)
    {
        return element.isOwnedElement(id);
    }

    public boolean inSameProject(IElement elem)
    {
        return element.inSameProject(elem);
    }

    public boolean hasSourceFile(String fileName)
    {
        return element.hasSourceFile(fileName);
    }

    public String getTopLevelId()
    {
        return element.getTopLevelId();
    }

    public ETList<IFlow> getTargetFlows()
    {
        return element.getTargetFlows();
    }

    public long getTargetFlowCount()
    {
        return element.getTargetFlowCount();
    }

    public ETList<ITaggedValue> getTaggedValuesByName(String tagName)
    {
        return element.getTaggedValuesByName(tagName);
    }

    public String getTaggedValuesAsString()
    {
        return element.getTaggedValuesAsString();
    }

    public ETList<ITaggedValue> getTaggedValues()
    {
        return element.getTaggedValues();
    }

    public long getTaggedValueCount()
    {
        return element.getTaggedValueCount();
    }

    public ITaggedValue getTaggedValueByName(String tagName)
    {
        return element.getTaggedValueByName(tagName);
    }

    public ETList<IFlow> getSourceFlows()
    {
        return element.getSourceFlows();
    }

    public long getSourceFlowCount()
    {
        return element.getSourceFlowCount();
    }

    public ETList<IElement> getSourceFiles3(ILanguage language)
    {
        return element.getSourceFiles3(language);
    }

    public ETList<IElement> getSourceFiles2(String language)
    {
        return element.getSourceFiles2(language);
    }

    public ETList<IElement> getSourceFiles()
    {
        return element.getSourceFiles();
    }

    public IElement getSourceFile(String fileName)
    {
        return element.getSourceFile(fileName);
    }

    public ETList<IReference> getReferredReferences()
    {
        return element.getReferredReferences();
    }

    public long getReferredReferenceCount()
    {
        return element.getReferredReferenceCount();
    }

    public ETList<IReference> getReferencingReferences()
    {
        return element.getReferencingReferences();
    }

    public long getReferencingReferenceCount()
    {
        return element.getReferencingReferenceCount();
    }

    public IProject getProject()
    {
        return element.getProject();
    }

    public ETList<IPresentationElement> getPresentationElements()
    {
        return element.getPresentationElements();
    }

    public long getPresentationElementCount()
    {
        return element.getPresentationElementCount();
    }

    public IPresentationElement getPresentationElementById(String id)
    {
        return element.getPresentationElementById(id);
    }

    public String getPossibleCollectionTypesAsString()
    {
        return element.getPossibleCollectionTypesAsString();
    }

    public ETList<String> getPossibleCollectionTypes()
    {
        return element.getPossibleCollectionTypes();
    }

    public IPackage getOwningPackage()
    {
        return element.getOwningPackage();
    }

    public IElement getOwner()
    {
        return element.getOwner();
    }

    public ETList<IConstraint> getOwnedConstraints()
    {
        return element.getOwnedConstraints();
    }

    public int getNumAppliedStereotypes()
    {
        return element.getNumAppliedStereotypes();
    }

    public ETList<ILanguage> getLanguages()
    {
        return element.getLanguages();
    }

    public boolean getHasExpandedElementType()
    {
        return element.getHasExpandedElementType();
    }

    public String getExpandedElementType()
    {
        return element.getExpandedElementType();
    }

    public ETList<IElement> getElements()
    {
        return element.getElements();
    }

    public String getElementType()
    {
        return element.getElementType();
    }

    public long getElementCount()
    {
        return element.getElementCount();
    }

    public String getDocumentation()
    {
        return element.getDocumentation();
    }

    public String getConstraintsAsString()
    {
        return element.getConstraintsAsString();
    }

    public ETList<IElement> getAssociatedArtifacts()
    {
        return element.getAssociatedArtifacts();
    }

    public long getAssociatedArtifactCount()
    {
        return element.getAssociatedArtifactCount();
    }

    public String getAppliedStereotypesList()
    {
        return element.getAppliedStereotypesList();
    }

    public ETList<String> getAppliedStereotypesAsString()
    {
        return element.getAppliedStereotypesAsString();
    }

    public String getAppliedStereotypesAsString(boolean honorAliasing)
    {
        return element.getAppliedStereotypesAsString(honorAliasing);
    }

    public ETList<Object> getAppliedStereotypes()
    {
        return element.getAppliedStereotypes();
    }

    public ETList<ITaggedValue> getAllTaggedValues()
    {
        return element.getAllTaggedValues();
    }

    public void deleteReferenceRelations()
    {
        element.deleteReferenceRelations();
    }

    public void deleteFlowRelations()
    {
        element.deleteFlowRelations();
    }

    public IConstraint createConstraint(String name, String expr)
    {
        return element.createConstraint(name, expr);
    }

    public Object applyStereotype2(String name)
    {
        return element.applyStereotype2(name);
    }

    public void applyStereotype(Object stereotype)
    {
        element.applyStereotype(stereotype);
    }

    public void applyNewStereotypes(String name)
    {
        element.applyNewStereotypes(name);
    }

    public void addTargetFlow(IFlow flow)
    {
        element.addTargetFlow(flow);
    }

    public ITaggedValue addTaggedValue(String tagName, String value)
    {
        return element.addTaggedValue(tagName, value);
    }

    public void addSourceFlow(IFlow flow)
    {
        element.addSourceFlow(flow);
    }

    public void addSourceFileNotDuplicate(String fileName)
    {
        element.addSourceFileNotDuplicate(fileName);
    }

    public void addSourceFile(String fileName)
    {
        element.addSourceFile(fileName);
    }

    public IReference addReferredReference(IReference ref)
    {
        return element.addReferredReference(ref);
    }

    public IReference addReferencingReference(IReference ref)
    {
        return element.addReferencingReference(ref);
    }

    public IPresentationElement addPresentationElement(IPresentationElement elem)
    {
        return element.addPresentationElement(elem);
    }

    public void addOwnedConstraint(IConstraint constraint)
    {
        element.addOwnedConstraint(constraint);
    }

    public IElement addElement(IElement elem)
    {
        return element.addElement(elem);
    }

    ////////////////////////////////////////////////////////////////////////////
    // IVersionableElement Methods
    
    public boolean verifyInMemoryStatus()
    {
        return element.verifyInMemoryStatus();
    }

    public void setXMIID(String str)
    {
        element.setXMIID(str);
    }

    public void setVersionedFileName(String str)
    {
        element.setVersionedFileName(str);
    }

    public void setNode(Node n)
    {
        element.setNode(n);
    }

    public void setMarkForExtraction(boolean b)
    {
        element.setMarkForExtraction(b);
    }

    public void setLineNumber(int num)
    {
        element.setLineNumber(num);
    }

    public void setIsClone(boolean value)
    {
        element.setIsClone(value);
    }

    public void setDom4JNode(Node n)
    {
        element.setDom4JNode(n);
    }

    public void setDirty(boolean b)
    {
        element.setDirty(b);
    }

    public void setAggregator(IVersionableElement aggregator)
    {
        element.setAggregator(aggregator);
    }

    public boolean saveIfVersioned()
    {
        return element.saveIfVersioned();
    }

    public boolean safeDelete()
    {
        return element.safeDelete();
    }

    public void removeVersionInformation()
    {
        element.removeVersionInformation();
    }

    public void prepareNode(Node node)
    {
        element.prepareNode(node);
    }

    public boolean isVersioned()
    {
        return element.isVersioned();
    }

    public boolean isSame(IVersionableElement elem)
    {
        boolean same = false;
        String xmiid;
        if (elem != null && (xmiid = elem.getXMIID()) != null && xmiid.length() > 0)
        {
            if (xmiid.equals(getXMIID()))
            {
                same = true;
            }
        }
        return same;
    }

    public boolean isMarkForExtraction()
    {
        return element.isMarkForExtraction();
    }

    public boolean isDirty()
    {
        return element.isDirty();
    }

    public boolean isDeleted()
    {
        return element.isDeleted();
    }

    public boolean isClone()
    {
        return element.isClone();
    }

    public String getXMIID()
    {
        return xmiId;
    }

    public String getVersionedURI()
    {
        return element.getVersionedURI();
    }

    public String getVersionedFileName()
    {
        return element.getVersionedFileName();
    }

    public Node getNode()
    {
        return element.getNode();
    }

    public int getLineNumber()
    {
        return element.getLineNumber();
    }

    public Element getElementNode()
    {
        return element.getElementNode();
    }

    public Node getDOM4JNode()
    {
        return element.getDOM4JNode();
    }

    public IVersionableElement duplicate()
    {
        IVersionableElement dup =  element.duplicate();
        
        IVersionableElement retVal = dup;
        
        if(dup instanceof IPresentationElement)
        {
            retVal = new ProxyPresentationElement((IPresentationElement)dup,
                                                  proxyType);
        }
        
        return retVal;
    }

    public void delete()
    {
        super.delete();
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean retVal = false;
        
        if (obj instanceof IVersionableElement)
        {
            IVersionableElement vElement = (IVersionableElement) obj;
            retVal = isSame(vElement);
        }
        
        return retVal;
    }

    
}

