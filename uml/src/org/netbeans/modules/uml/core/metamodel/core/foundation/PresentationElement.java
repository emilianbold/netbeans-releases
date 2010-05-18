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
package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.common.generics.ETPairT;
import java.util.Iterator;
import org.dom4j.Document;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.dom4j.Node;

/**
 * PresentationElementImpl is the implementation of the UML 
 * PresentationElement meta type.
 *
 * A presentation element is a textual or graphical presentation of one or more model elements.
 * In the metamodel, a PresentationElement is an Element which presents a set of NamedElements
 * to a reader. It is the base for all metaclasses used for presentation. All other metaclasses with
 * this purpose are either direct or indirect subclasses of PresentationElement.
 * PresentationElement is an abstract metaclass. The subclasses of this class are proper to a
 * graphic editor tool and are not specified here. It is a stub for their future definition.
 */
public class PresentationElement extends Element implements IPresentationElement
{
    private String overrideTypeName = null;
    
    /**
     * 
     */
    public PresentationElement()
    {
        super();
    }

    /**
     * Retrieves the metatype of the the firstSubject.  
     * @return The model elements metatype
     */
    public String getFirstSubjectsType()
    {
        String retVal = null;
        
        IElement subject = getFirstSubject();
        if(subject != null)
        {
            retVal = subject.getElementType();
        }
        else
        {
            retVal = "";
        }
        
        return retVal;
    }
                
    /**
     *
     * Retrieves all the model elements that this presentation element represents.
     *
     * @return subjects[out] The elements
     *
     */
    public ETList<IElement> getSubjects()
    {
        IElement dummy = null;
        return retrieveElementCollectionWithAttrIDs(dummy, "subject", IElement.class);

    }

    /**
     *
     * Returns the first element on the subjects collection.
     *
     * @param subject[out] the first element in the list
     *
     * @return HRESULTs
     *
     */
    public IElement getFirstSubject()
    {

        IElement retVal = null;

        String firstElemetnID = getFirstSubjectID();
        if ((firstElemetnID != null) && (firstElemetnID.length() > 0))
        {
            Node foundNode = UMLXMLManip.findElementByID(getNode(), firstElemetnID);

            if (foundNode != null)
            {
                TypedFactoryRetriever<IElement> ret = new TypedFactoryRetriever<IElement>();
                retVal = ret.createTypeAndFill(foundNode);
            }
            else
            {
                ElementLocator loc = new ElementLocator();
                retVal = loc.findElementByID(firstElemetnID);

                if (retVal != null)
                {
                    Node assocNode = retVal.getNode();

                    Node owner = XMLManip.ensureNodeExists(assocNode, "UML:Element.presentation", "UML:Element.presentation");
                    if (owner instanceof org.dom4j.Element)
                    {
                        org.dom4j.Element ownerElement = (org.dom4j.Element) owner;
                        org.dom4j.Element myElement = ownerElement.elementByID(getXMIID());
                        if (myElement != null)
                        {
                            setNode(myElement);
                        }
                        else
                        {
                            Node myNode = getNode();
                            myNode.detach();
                            ownerElement.add(myNode);
                        }
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Determines whether or not the passed in element is the first subject of 
     * this PresentationElement.
     */
    public String getFirstSubjectID()
    {
        String retVal = null;

        String ids = XMLManip.getAttributeValue(getNode(), "subject");
        if ((ids != null) && (ids.length() > 0))
        {
            int index = ids.indexOf(' ');
            if (index < 0)
            {
                index = ids.length();
            }
            String firstID = ids.substring(0, index);

            if ((firstID != null) && (firstID.length() > 0))
            {
                retVal = URILocator.retrieveRawID(firstID);
            }
        }

        return retVal;
    }

    /**
     * Adds the passed-in NamedElement to the collection of elements
     * that this PresentationElement is associated with.
     *
     * @param element[in] The NamedElement to add
     *
     * @return HREUSLTs
     */
    public IElement addSubject(IElement elem)
    {

        // See ElementImpl::AddPresentationElement for an explanation
        // of why we are pushing this event context...
        EventContextManager man = new EventContextManager();
        String name = EventDispatchNameKeeper.modifiedName();
//		IEventDispatcher disp = null;
//		IEventContext context = man.getNoEffectContext(this, name, "PresentationAdded", disp);

        ETPairT<IEventContext, IEventDispatcher> contextInfo = man.getNoEffectContext(this,
                name,
                "PresentationAdded");

        IEventDispatcher disp = contextInfo.getParamTwo();
        IEventContext context = contextInfo.getParamOne();

        EventState state = new EventState(disp, context);

        try
        {
            final IElement element = elem;
            addChildAndConnect(
                    true, "subject", "subject", element,
                    new IBackPointer<IPresentationElement>()
                    {

                        public void execute(IPresentationElement obj)
                        {
                            element.addPresentationElement(obj);
                        }
                    });
        }
        finally
        {
            state.existState();
        }
        return elem;
    }

    /**
     * Removes the NamedElement from this PresentationElement with the matching
     * id.
     *
     * @param elementID[in] The id of the NamedElement that should be removed
     *
     * @return HRESULTs
     */
    public void removeSubject(IElement elem)
    {
        if (elem != null)
        {
            boolean found = false;
            found = isSubject(elem);
            if (found)
            {
                // See ElementImpl::AddPresentationElement for an explanation
                // of why we are pushing this event context...
                EventContextManager man = new EventContextManager();
                String name = EventDispatchNameKeeper.modifiedName();
//				IEventDispatcher disp = null;
//				IEventContext context = man.getNoEffectContext(this, name, "PresentationRemoved", disp);

                ETPairT<IEventContext, IEventDispatcher> contextInfo = man.getNoEffectContext(this,
                        name,
                        "PresentationRemoved");

                IEventDispatcher disp = contextInfo.getParamTwo();
                IEventContext context = contextInfo.getParamOne();

                EventState state = new EventState(disp, context);
                try
                {
                    removeElementByID(elem, "subject");
                    elem.removePresentationElement(this);
                }
                finally
                {
                    state.existState();
                }
            }
        }
    }

    /**
     * Determines whether or not the passed-in NamedElement is associated with this
     * PresentatinElement.
     *
     * @param element[in] The element to check against
     * @param isSubject[out] - true if present, else
     *                       - false if not
     *
     * @return HRESULTs
     */
    public boolean isSubject(IElement elem)
    {
        ETList<IElement> elems = getSubjects();
        if (elems != null)
        {
            Iterator<IElement> iter = elems.iterator();

            while (iter.hasNext())
            {
                IElement element = iter.next();
                if (element != null && elem.isSame(elem))
                    return true;
            }
        }
        return false;
    }

    public boolean isFirstSubject2(String elementXMIID)
    {
        if (elementXMIID == null)
            return false;

        boolean isFirstSubject = false;

        IElement firstSubject = getFirstSubject();
        if (firstSubject != null)
        {
            String firstSubjectXMIID = firstSubject.getXMIID();
            if (firstSubjectXMIID.length() > 0 && firstSubjectXMIID.compareTo(elementXMIID) == 0)
                isFirstSubject = true;
        }

        return isFirstSubject;
    }

    /**
     *
     * Retrieves the unique ID of the actual element used to show this presentation element to the user.
     *
     * @param displayID[out] The id
     *
     * @return 
     *
     */
    public String getDisplayElementID()
    {
        return XMLManip.getAttributeValue(m_Node, "displayID");
    }

    /**
     *
     * Sets the unique id of the actual element used to show this presentation element to the user.
     *
     * @param displayID[in] The id
     *
     * @return 
     *
     */
    public void setDisplayElementID(String id)
    {
        setAttributeValue("displayID", id);
    }

    /**
     * Transforms this presentation element into another, such as an AssocationEdge into an AggregationEdge.
     * This should be overridden by any base class that's interested.
     */
    public IPresentationElement transform(String elemName)
    {
        return null;
    }

    /** 
     * Returns the number of subjects owned by this presentation element.
     * 
     * @param pVal[out] The number of subjects owned by this presentation element
     *
     * Historical Note: This operation used to call XMLManip::QueryCount to determine
     *                  how many subjects it owned.  But, after a presentation element
     *                  was deleted, its XML still showed that it had subjects even though
     *                  the subject's XMI ids were invalid.  That threw off the count.  This
     *                  operation was returning the fact that it had a subject, but trying
     *                  to retrieve the subject would fail.  The current approach might be
     *                  slower but it is accurate.
     */
    public long getSubjectCount()
    {
        ETList<IElement> elems = getSubjects();
        // TODO: EMBT's code returns elems.size() + 1. Why?
        return elems != null ? elems.size() : 0;
    }

    /**
     *
     * Sets the "removeOnSave" attribute, dependent on preferences. If set to 
     * "T", this presentation element is removed from the data file upon save.
     *
     * @param node[in] The node representing this PresentationElement
     *
     * @return HRESULT
     *
     */
    public void establishNodeAttributes(org.dom4j.Element ele)
    {
        super.establishNodeAttributes(ele);
        XMLManip.setAttributeValue(ele, "removeOnSave", "T");

        IProject project = getProject();
        if (project != null)
        {
            project.addRemoveOnSave(this);
        }
    }

    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:PresentationElement", doc, node);
    }

    /**
     *
     * Duplicates this element. 
     *
     * @see ElementImpl::PerformDuplication()
     *
     */
    public IVersionableElement performDuplication()
    {
        IVersionableElement dup = super.performDuplication();
        IPresentationElement pEle = (IPresentationElement) dup;
        ETList<IElement> subjects = pEle.getSubjects();
        if (subjects != null)
        {
            Iterator<IElement> iter = subjects.iterator();

            while (iter.hasNext())
            {
                IElement elem = iter.next();
                elem.addPresentationElement(pEle);
            }
        }
        return dup;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement#isFirstSubject(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean isFirstSubject(IElement pElement)
    {
        IElement firstSubject = this.getFirstSubject();
        return pElement != null && firstSubject != null && firstSubject.isSame(pElement);
    }
}

