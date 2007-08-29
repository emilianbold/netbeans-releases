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


package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

import org.netbeans.modules.uml.project.ui.cookies.DocumentationCookie;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.Util;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaRequestProcessor;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewPackageType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewElementType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewAttributeType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewOperationType;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;



/**
 *
 * @author Trey Spiva
 */
public class UMLModelElementNode extends UMLElementNode
    implements ITreeElement, INamedElementEventsSink, IElementLifeTimeEventsSink
{
    
    private String m_ExpandedElementType = "";
    private boolean m_bTranslateName = false;
    private DocumentationCookie mDocCookie = null;

    
    public UMLModelElementNode()
    {
        super();
        initCookies();
        initialize();
    }
    
    public UMLModelElementNode(Lookup lookup)
    {
        super(lookup);
        initialize();
    }
    
    public UMLModelElementNode(Children ch, Lookup lookup)
    {
        super(ch, lookup);
        initialize();
    }
    
    
    /**
     * @param item
     * @throws NullPointerException
     */
    public UMLModelElementNode(IProjectTreeItem item) throws NullPointerException
    {
        super(item);
        initialize();
    }
    
    /**
     * @param item
     * @throws NullPointerException
     */
    public UMLModelElementNode(Lookup lookup, IProjectTreeItem item)
        throws NullPointerException
    {
        super(lookup, item);
        initialize();
    }
    
    private void initialize()
    {
        DispatchHelper helper = new DispatchHelper();
        helper.registerForNamedElementEvents(this);
        helper.registerForLifeTimeEvents(this);
    }
    
    /**
     * Initializes the node with the default cookies.  <b>Note:</b>  Do not
     * call this method if a looup is added.  When a lookup is added then the
     * lookup is used to find the cookies.
     */
    protected void initCookies()
    {
        super.initCookies();
        
        Class[] cookies = {DocumentationCookie.class};
        getCookieSet().add(cookies, this);
    }
    
    public org.openide.nodes.Node.Cookie createCookie(Class klass)
    {
        org.openide.nodes.Node.Cookie retVal = super.createCookie(klass);
        
        if(DocumentationCookie.class.equals(klass) == true)
        {
            if(mDocCookie  == null)
            {
                mDocCookie = new ElementDocumentationCookie();
            }
            retVal = mDocCookie;
        }
        return retVal;
    }
    
    
    /**
     * Get the new types that can be created in this node. For example, a node
     * representing a class will permit attributes, operations, classes,
     * interfaces, and enumerations to be added.
     *
     * @return An array of new type operations that are allowed.
     */
    public NewType[] getNewTypes()
    {
        String elType = getElementType();
        NewType[] retVal = null;
        
        if (getModelElement() instanceof INamespace)
        {
            if (elType.equals(ELEMENT_TYPE_CLASS) ||
                elType.equals(ELEMENT_TYPE_INTERFACE) ||
                elType.equals(ELEMENT_TYPE_DATA_TYPE)||
                elType.equals(ELEMENT_TYPE_PART_FACADE))
            {
                return new NewType[]
                {
                    new NewDiagramType(this),
//                        new NewPackageType(this),
//                        new NewElementType(this),
                        new NewAttributeType(this),
                        new NewOperationType(this)
                };
            }
            
            else if (elType.equals(ELEMENT_TYPE_ACTIVITY) ||
                elType.equals(ELEMENT_TYPE_INTERACTION) ||
                elType.equals(ELEMENT_TYPE_STATE_MACHINE) ||
                elType.equals(ELEMENT_TYPE_PACKAGE) ||
                elType.equals(ELEMENT_TYPE_OPERATION) ||
//		elType.equals(ELEMENT_TYPE_PART_FACADE) ||
                elType.equals(ELEMENT_TYPE_ARTIFACT) ||
                elType.equals(ELEMENT_TYPE_NODE) ||
                elType.equals(ELEMENT_TYPE_ENUMERATION) ||
                elType.equals(ELEMENT_TYPE_DERIVATION_CLASSIFIER) ||
                elType.equals(ELEMENT_TYPE_COLLABORATION))
            {
                return new NewType[]
                {
                    new NewDiagramType(this),
                        new NewPackageType(this),
                        new NewElementType(this)
                };
            }
        } // if getModelElement() instanceof INamespace
        
        // The NewAction code does not check for null.  Therefore, we have
        // to create a new object just to keep them from throwing.
        if (retVal == null)
            retVal = new NewType[0];
        
        return retVal;
    }
    
    
    public boolean canRename()
    {
        String eleType = getElementType();
        
        // these element type nodes cannot be renamed
        if (eleType.equals(ELEMENT_TYPE_SOURCE_FILE_ARTIFACT) ||
            eleType.equals(ELEMENT_TYPE_DEPENDENCY) ||
            eleType.equals(ELEMENT_TYPE_REALIZATION) ||
            eleType.equals(ELEMENT_TYPE_USAGE) ||
            eleType.equals(ELEMENT_TYPE_PERMISSION) ||
            eleType.equals(ELEMENT_TYPE_ABSTRACTION) ||
            eleType.equals(ELEMENT_TYPE_GENERALIZATION) ||
            eleType.equals(ELEMENT_TYPE_ASSOCIATION) ||
            eleType.equals(ELEMENT_TYPE_AGGREGATION))
        {
            return false;
        }
        
        // all other element type nodes can be renamed
        return true;
    }
    
    
    public void setName(String val)
    {
        IElement element = getElement();
        
        if (element==null)
        {
            super.setName(val);
            return;
        }
        
        if (element instanceof ILifeline)
        {
            ILifeline lifeline = (ILifeline) element;
            String name = lifeline.getName();
            IClassifier classifier = lifeline.getRepresentingClassifier();
            
            String rcName = classifier == null 
                ? "" // NOI18N
                : classifier.getFullyQualifiedName(true);
            
            String oldName = name + " : " + rcName;
            
            if (oldName.equals(val))
                return;
            
            String newName = null;
            String newRCName = null;
            int index = val.indexOf(" : ");

            if (index > -1)
            {
                newName = val.substring(0, index).trim();
                newRCName = val.substring(index + 3, val.length()).trim();
            }
            
            else
                newName = val;

            lifeline.setName(newName);
            
            if (newRCName != null)
                lifeline.setRepresentingClassifier2(newRCName);

            else
                newRCName = rcName;
            
            lifeline.setDirty(true);
            String n = newName + " : " + newRCName;
            
            super.setName(n);
            
            if (getData() != null)
                getData().setItemText(n);
            
            setDisplayName(n);
        }

        else if (element instanceof INamedElement)
        {   
            String oldName = ((INamedElement)element).getName();
            
            if (oldName.equals(val))
                return;
            
			if (Util.hasNameCollision(getModelElement().getOwningPackage(), 
					val, getModelElement().getElementType(), (INamedElement)getModelElement()))
			{
				DialogDisplayer.getDefault().notify(
						new NotifyDescriptor.Message(NbBundle.getMessage(
						DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
				return;
			}
			
            JavaRequestProcessor p = new JavaRequestProcessor();
        
            if (p.isNewNameValid(((INamedElement)element), val))
            {
                ((INamedElement)element).setName(val);
                
                // if (the name wasn't allowed to be changed for the element,
                // then abort the name change for the node
                if (!((INamedElement)element).getName().equals(val))
                    return;

                // element name was changed successfully, 
                // continue with node name change
                element.setDirty(true);
                super.setName(val);
            
                if (getData() != null)
                    getData().setItemText(val);
        
                // setDisplayName(val);
            }
        }
        
        firePropertySetsChange(null, retreiveProperties());
    }
    
    
    public void setElement(IElement element)
    {
        //m_ExpandedElementType = "";
        if (element != null)
        {
            m_ExpandedElementType = element.getExpandedElementType();
            addElementCookie(element);
        }
        
        if (getData() != null)
            getData().setModelElement(element);
    }
    
    protected void addElementCookie(IElement element)
    {
        getCookieSet().add(element);
    }
    
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getElement()
    */
    public IElement getElement()
    {
        //System.out.print("Retrieving Element: ");
        IElement retVal = null;
        
        if (getData() != null)
        {
            retVal = getData().getModelElement();
        }
        
        //System.out.println(retVal.toString());
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getXMIID()
    */
    public String getXMIID()
    {
        String retVal = null;
        
        if (getData() != null)
        {
            retVal = getData().getModelElementXMIID();
        }
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getElementType()
    */
    public String getElementType()
    {
        String retVal = null;
        
        if (getData() != null)
        {
            retVal = getData().getModelElementMetaType();
        }
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement#getExpandedElementType()
    */
    public String getExpandedElementType()
    {
        return m_ExpandedElementType;
    }
    
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
    public boolean equals(Object obj)
    {
        boolean retVal = false;

        if (this.hashCode() == obj.hashCode())
            return true;
		
        if (obj instanceof ITreeElement)
        {
            IElement myElement = getData().getModelElement();
            ITreeElement objElement = (ITreeElement)obj;
            
            if ((myElement != null) && (objElement.getElement() != null))
            {
                retVal = myElement.isSame(objElement.getElement());
                if (retVal == false && getData().getModelElementMetaType()
                    .equals("DerivationClassifier"))
                {
                    retVal = getDisplayedName().equals(objElement.getDisplayedName());
                }
            }
            
            else if ((myElement == null) && (objElement.getElement() == null))
            {
                retVal = super.equals(obj);
                
                if (retVal == false)
                {
                    // getDisplayedName sometimes null for DiagramTopComponent's
                    // local node, and all DTC local nodes have an empty string
                    // name, so we must do a obj ref equality
                    // retVal = getDisplayedName().equals(objElement.getDisplayedName()); 

                    retVal = this == objElement;
                }
            }
            
        }
        
        else
        { 
            // Generic equals method. This can be used to test to ITreeItem(s).
            retVal = super.equals(obj);
        }
        
        return retVal;
    }
    
    /**
     * Retrieves the meta-data type name.
     *
     * @return The type name.
     */
    public String getType()
    {
        String retVal = null;
        
        if (getData() != null)
        {
            retVal = getData().getModelElementMetaType();
        }
        return retVal;
    }
    
    /**
     * Retrieves the XML nodes associated to the UML model element.
     *
     * @return The XML node.
     */
    public Node getXMLNode()
    {
        Node pVal = null;
        IElement pElement = getElement();
        if (pElement != null)
        {
            pVal = pElement.getNode();
        }
        return pVal;
    }
    
    /**
     * Determines if the ConfigStringTranslator should be used to translate the
     * name of the node.  The ConfigStringTranslator is used to translate
     * strings that start with PSK_* to a readable string.
     *
     * @return <b>true</b> if the name is to be translated.
     */
    public boolean getTranslateName()
    {
        return m_bTranslateName;
    }
    
    /**
     * Determines if the ConfigStringTranslator should be used to translate the
     * name of the node.  The ConfigStringTranslator is used to translate
     * strings that start with PSK_* to a readable string.
     *
     * @param val <b>true</b> if the name is to be translated.
     */
    public void setTranslateName(boolean val)
    {
        m_bTranslateName = val;
    }
    
    /**
     * Retrieves the user friendly name for the model element.
     *
     * @return The display namne.
     */
    public String getDisplayedName()
    {
        return getDisplayName();
    }
    
    /**
     * Retrieves the user friendly name for the model element.  The name is
     * translated if required.
     *
     * @return The display namne.
     * @see #getTranslateName()
     */
    public String getDisplayName()
    {
        String pVal = "";
        if (!getTranslateName())
        {
            pVal = super.getDisplayName();
        }
        else
        {
            // Try translating the name first
            IConfigStringTranslator pTranslator = new ConfigStringTranslator();
            if (pTranslator != null)
            {
                String displayName = super.getDisplayName();
                pVal = pTranslator.translateWord(displayName);
                if((pVal == null) || (pVal.length() <= 0))
                {
                    pVal = displayName;
                }
            }
        }
        return pVal;
    }
    
    ////////////////////////////////////////////////
    // Implementations of INamedElementEventsSink
    
    public void onVisibilityModified(INamedElement element, IResultCell cell)
    {
    }
    
    public void onPreVisibilityModified(
        INamedElement element, int proposedValue, IResultCell cell)
    {
    }
    
    public void onPreNameModified(
        INamedElement element, String proposedName, IResultCell cell)
    {
    }
    
    public void onPreNameCollision(
        INamedElement element, 
        String proposedName, 
        ETList<INamedElement> collidingElements, 
        IResultCell cell)
    {
    }
    
    public void onPreAliasNameModified(
        INamedElement element, 
        String proposedName, 
        IResultCell cell)
    {
    }
    
    public void onNameModified(INamedElement element, IResultCell cell)
    {
        if (element.isSame(this.getElement()))
        {
            if (!getName().equals(element.getName()))
                setName(element.getName());
        }
    }
    
    public void onNameCollision(
        INamedElement element, 
        ETList<INamedElement> collidingElements, 
        IResultCell cell)
    {
    }
    
    public void onAliasNameModified(INamedElement element, IResultCell cell)
    {
    }
    
    
    
    public class ElementDocumentationCookie implements DocumentationCookie
    {
        /**
         * Retreive the documentation from the node.
         */
        public String getDocumentation()
        {
            String retVal = "";
            
            IElement element = getModelElement();
            if (element != null)
            {
                retVal = element.getDocumentation();
            }
            return retVal;
        }
        
        /**
         * Sets the documentation for the node.
         */
        public void setDocumentation(String retVal)
        {
            IElement element = getModelElement();
            if (element != null)
            {
                element.setDocumentation(retVal);
            }
        }
    }
    
    
    // Implementation of Interface IElementLifeTimeEventsSink
    /////////////////////////////////////////////////////////
    
    public void onElementDeleted(
        IVersionableElement element, IResultCell cell) 
    {
        // cvc - 6317505
        // defensive null checks; getParentItem was returning null for sure
        // causing NPEs and InvocationTargetExceptions
        if (element != null && element.isSame(getModelElement()))
        {   
            ITreeItem parentItem = getParentItem();
            if (parentItem != null)
            {
                parentItem.removeChild(this);
            }
        }
   }
    
    public void onElementCreated(
        IVersionableElement element, IResultCell cell) {}

    public void onElementDuplicated(
        IVersionableElement element, IResultCell cell) {}

    public void onElementPreCreate(
        String ElementType, IResultCell cell) {}

    public void onElementPreDelete(
        IVersionableElement element, IResultCell cell) {}

    public void onElementPreDuplicated(
        IVersionableElement element, IResultCell cell) {}

    
}
