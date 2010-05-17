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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.EventExecutor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.CreationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.DestroyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDestroyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
abstract public class InstanceInformation implements Cloneable
{
    private String   m_InstanceName;
    private boolean m_IsStatic;
    private boolean m_IsInstantiated;
    
    private IREClass m_pInstanceType;
    private IREClass m_pInstanceOwner;
    
    private String   m_TypeName;
    private boolean m_HasBeenReferenced;
    
    public InstanceInformation()
    {
    }

    public InstanceInformation(String name, String typeName)
    {
        m_InstanceName = name;
        m_TypeName     = typeName;
    }

    abstract public boolean isPrimitive();

    abstract public boolean isValid();

    abstract public String getInstantiatedTypeName();

    abstract public boolean isDerivedFrom(String desiredType, 
                                            IREClassLoader pLoader);
    
    /**
     * Sets the type name used to instantiate the instance.;
     *
     * @param typeName [in] The type name.
     * @param pClassLoader [in] The class loader used to locate the type.
     */
    abstract public void setInstantiatedType(String typeName, 
                                             IREClassLoader pClassLoader);
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        // Note: we're shallow-copying the IREClass references, so any 
        //       modifications to those IREClasses will have eeeeevil 
        //       consequences. We might need to make IREClass itself Cloneable.
        return super.clone();
    }
    
    /**
     * Sets the class that owns the instance of the variable.
     *
     * @param pOwner [out] The owner of the instance.
     */
    public void setInstanceOwner(IREClass pOwner)
    {
        m_pInstanceOwner = pOwner;
    }

    /**
     * Set if the instance is instatiated or not.  If the variable
     * is not instantiated then it can be considered referenced.
     *
     * @param value [in] The value.
     */
    public void setIsInstantiated(boolean value)
    {
        m_IsInstantiated = value;
    }

    /**
     * Sets if the instance is a static variable or
     * an instance variable.
     *
     * @param value [in] The value.
     */
    public void setIsStatic(boolean value)
    {
        m_IsStatic = value;
    }

    /**
     * Sets the name of the instance.
     *
     * @param &value [in] The name.
     */
    public void setInstanceName(String value)
    {
        //kris - in response to is 78375. When an array expression contains an integer 
        // constant index, this will filter off the []. The needs to be revisited 
        // and determined if this is this action is even appropriate.
        if (value.indexOf("[") != -1) value="" ;
        m_InstanceName = value;
    }

    /**
     * Determines if the instance has been referenced already.
     *
     * @param value [in] True if referenced.
     */
    public void setHasBeenReferenced(boolean value)
    {
        m_HasBeenReferenced = value;
    }

    /**
     * Retrieves the class that owns the instance of the variable.
     *
     * @param pOwner [out] The owner of the instance.
     */
    public IREClass getInstanceOwner()
    {
        return m_pInstanceOwner;
    }

    /**
     * Retrieves the name of the type that was used to instantiate 
     * instance.
     *
     *
     * @return The type name.
     */
    public String getInstanceOwnerName()
    {
        if (m_pInstanceOwner != null)
        {    
            String name = m_pInstanceOwner.getName();
            String pack = m_pInstanceOwner.getPackage();
            if (pack != null && pack.length() > 0)
                name = pack + "::" + name;
            return name;
        }
        return null;
    }

    /**
     * Checks if the instance is instatiated or not.  If the variable
     * is not instantiated then it can be considered referenced.
     *
     * @return true if instantiated, false otherwise.
     */
    public boolean isInstantiated()
    {
        return m_IsInstantiated;
    }

    /**
     * Checks if the instance is a static variable or
     * an instance variable.
     *
     * @return true if static, false otherwise.
     */
    public boolean isStatic()
    {
        return m_IsStatic;
    }

    /**
     * Retrieves the name of the instance.
     *
     * @param The instance name.
     */
    public String getInstanceName()
    {
        return m_InstanceName;
    }

    /**
     * Determines if the instance has been referenced already.
     *
     * @return True if referenced.
     */
    public boolean hasBeenReferenced()
    {
        return m_HasBeenReferenced;
    }

    /**
     * Retrieves the name of the instance type.  The type name 
     * will be the fully scoped name of the type.
     *
     * @param value The fully scoped name of the instance type.
     */
    public String getInstanceTypeName()
    {
        return m_TypeName;
    }

    public void setInstanceTypeName(String value)
    {
        m_TypeName = value;
    }

    /**
     * Searches the instance owner for a method that matches the specified 
     * signature.  If the instance owner does not have a matching method
     * the super classes of the owner will be searched.
     *
     * @param methodName [in] The method to find.
     * @param pClassLoader [in] The class loader to use.
     *
     * @return The method declaration.
     */
    public MethodDeclaration getMethodDeclaration(String methodName, 
					ETList< ETPairT<InstanceInformation, String> > arguments, 
					IREClassLoader pClassLoader, boolean alwaysCreate)
    {
        // Likewise doth the C++ code.
        return null;
    }

    /**
     * Searches for an attribute declaration.  The attributes owner and its super 
     * classes will be search until the attribute decaration is found.
     *
     * @param attrName [in] The name of a attribute.
     * @param pClassLoader [in] The class loader used to retrieve the type information.
     *
     * @return The instance information, NULL if the instance information is not foune.
     */
    public InstanceInformation getInstanceDeclaration(String attrName, 
                                                   IREClassLoader pClassLoader)
    {
        IREClass owner = getInstanceOwner();
        AttributeLocator attrLocator = new AttributeLocator(attrName);
        SourceElementLocator<InstanceInformation> locator = 
                new SourceElementLocator<InstanceInformation>();
        return locator.locate(attrLocator, owner, pClassLoader, true, false,
                                null);
    }

    /**
     * Searches for an attribute declaration.  The attributes owner and its super 
     * classes will be search until the attribute decaration is found.  Since, 
     * the instance is a member of a class the ReferencedVariable event will be
     * sent if pDispatcher is not NULL.
     *
     * @param attrName [in] The name of a attribute.
     * @param pClassLoader [in] The class loader used to retrieve the type information.
     *
     * @return The instance information, NULL if the instance information is not foune.
     */
    public static InstanceInformation getInstanceDeclaration(String attrName, 
                                                  IREClass pOwner, 
                                                  IREClassLoader pClassLoader)
    {
        return new SourceElementLocator<InstanceInformation>()
                .locate(new AttributeLocator(attrName), pOwner, pClassLoader,
                        true, false, null);
    }

    /**
     * Sends an object creation event to all listeners on an event dispatcher.
     * Before sending the object creation event the XML event data will be created.
     * The event data will be the child of the parent node.
     *
     * @param pParentNode [in] The parent node of the event data.
     * @param pDispatcher [in] The event to use.
     */
    public void sendCreationEvent(Node pParentNode, 
                                  long lineNumber, 
                                  IUMLParserEventDispatcher pDispatcher,
                                  MethodDeclaration       declaration)
    {
        Node data = generateCreationXMI(pParentNode, lineNumber, declaration);
        if (data != null)
        {    
            ICreationEvent ce = new CreationEvent();
            ce.setEventData(data);
            pDispatcher.fireCreateAction(ce, null);
        }
        setIsInstantiated(true);
    }

    public void sendReference(Node pParentNode)
    {
        if (!hasBeenReferenced())
        {
            EventExecutor.RefVariableDef data = 
                            new EventExecutor.RefVariableDef();
            data.variableName = getInstanceName();
            data.variableType = getInstanceTypeName();
            data.variableType = this.m_TypeName ;
            data.declaringClassifier = getInstanceOwnerName();
            
            Stack<EventExecutor.RefVariableDef> refStack = 
                new Stack<EventExecutor.RefVariableDef>();
            refStack.push( data );
            EventExecutor.sendVariableReference(refStack, pParentNode);
        }
        setHasBeenReferenced(true);
    }
    
    public EventExecutor.RefVariableDef getReferenceInfo()
    {
        EventExecutor.RefVariableDef data = new EventExecutor.RefVariableDef();

        data.variableName = getInstanceName();
        data.variableType = getInstanceTypeName();
        data.declaringClassifier = getInstanceOwnerName();
        
        setHasBeenReferenced(true);
        return data;
    }

    /**
     * Sends the OnDestroyAction event to all operation detail listeners.
     *
     * @param pDispatcher [in] The dispatcher used to send the event.
     */
    public void sendDestroy(Node parentNode, 
                            IUMLParserEventDispatcher dispatcher)
    {
        if (dispatcher == null) 
            return;
        Node data = generateDestroyXMI(parentNode);
        if (data != null)
        {
            IDestroyEvent event = new DestroyEvent();
            event.setEventData(data);
            dispatcher.fireDestroyAction(event, null);
        }
    }
    
    /**
     * Initializes the IDestroyEvent before it is sent to the operation 
     * detail listeners.  InitializeDestroyEvent is virtual so decendents
     * can perform additional intialization.  This version will initialize
     * the properties InstanceName, InstanceTypeName, and IsPrimitive.  The
     * IsPrimitive will be set to false.
     *
     * @param pEvent [in] The event to initalize.
     */
    protected void initializeDestroyEvent(Node event)
    {
        if (event == null)
            return ;
        ensureElementExists(event, "TokenDescriptors", "TokenDescriptors");
    }
    
    /**
     * Makes sure that the node with the passed in name is present
     * under curNode. If it isn't, one is created.  XMLManip has a
     * method that does the exact same thing.  The only problem is that
     * XMLMainp wants to create a node with a namespace.  In this
     * case we do not want the namespace.
     * 
     * [Aztec]: As far as I can see, the code in XMLManip is identical, and the
     *          preceding comment is not valid, at least for the Java port.
     *
     * @param curNode [in] The node to append to.
     * @param name    [in] Name of the node to check for existence for. 
     * @parma query   [in] The query string to used to check for existence.
     * @param node    [out] the node representing the element
     */
    protected Node ensureElementExists(Node curNode, String name, String query)
    {
        return XMLManip.ensureNodeExists(curNode, name, query);
    }

    /**
     * Create the XML data that represents the creation instance information data.
     *
     * @param pParentNode [in] The owner of the XML data.
     * @param pVal [out] The XML data.
     */
    public Node generateCreationXMI(Node pParentNode, long lineNumber, MethodDeclaration declaration)
    {
        Node createNode = createNode(pParentNode, "UML:CreateObjectAction");
        if (createNode != null)
        {
            XMLManip.setAttributeValue(createNode, "line", 
                    String.valueOf(lineNumber));
            XMLManip.setAttributeValue(createNode, "classifier", 
                    getInstanceTypeName());
            
            Node outputPinNode = createNode(createNode, "UML:OutputPin");
            if (outputPinNode != null)
            {
                String instanceName = getInstanceName();
                if(instanceName.equals("<RESULT>"))
                   instanceName = "";

                XMLManip.setAttributeValue(outputPinNode, "value",instanceName);
                XMLManip.setAttributeValue(outputPinNode, "type", "Instance");
            }
            if(declaration != null)
            {
               declaration.addOperation(createNode);
               Node pDeclaringNode = 
               	    XMLManip.createElement((Element) createNode, 
                            "DeclaringClass");
               if(pDeclaringNode != null)
               {
                  declaration.addOwner(pDeclaringNode, this);
               }
            }
            
            setCreationEventData(createNode);
        }
        return createNode;
    }

    /**
     * Create the XML data that represents the destroy instance information data.
     *
     * @param pParentNode [in] The owner of the XML data.
     * @param pVal [out] The XML data.
     */
    public Node generateDestroyXMI(Node pParentNode)
    {
        Node destroyNode = createNode(pParentNode, "UML:DestroyObjectAction");
        if (destroyNode != null)
        {
            XMLManip.setAttributeValue(destroyNode, "classifier", 
                    getInstanceTypeName());
            Node inputPinNode = createNode(destroyNode, "UML:InputPin");
            if (inputPinNode != null)
            {    
                XMLManip.setAttributeValue(inputPinNode, "value", 
                        getInstanceName());
                XMLManip.setAttributeValue(inputPinNode, "type", "Instance");
            }
            initializeDestroyEvent(destroyNode);
        }
        return destroyNode;
    }

    /**
     * Creates a Input pin that represent the instance information.
     *
     * @param pParent [in] The parent of the input pin node.
     * @param pVal [out] The input pin that was created.  If pVal is NULL
     * then the pin will not be returned.
     */
    public Node getInputPinInformation(Node parent)
    {
        if (parent == null) return null;
        
        Node inputPinNode = createNode(parent, "UML:InputPin");
        if (inputPinNode != null)
        {
            String instanceName = getInstanceName();
            XMLManip.setAttributeValue(inputPinNode, "value", instanceName);
            XMLManip.setAttributeValue(inputPinNode, "name", instanceName);
            XMLManip.setAttributeValue(inputPinNode, "type", "Instance");
        }
        return inputPinNode;
    }

    /**
     * Creates an Output pin that represent the instance information.
     *
     * @param pParent [in] The parent of the output pin node.
     * @param pVal [out] The output pin that was created.  If pVal is NULL
     * then the pin will not be returned.
     */
    public Node getOutputPinInformation(Node parent)
    {
        if (parent == null) return null;
        
        Node outputPinNode = createNode(parent, "UML:OutputPin");
        if (outputPinNode != null)
        {
            String name = getInstanceName();
            if (name != null && name.length() > 0)
            {    
                XMLManip.setAttributeValue(outputPinNode, "value", name);
                XMLManip.setAttributeValue(outputPinNode, "type", "Instance");
            }
            else
            {    
                name = getInstanceTypeName();
                XMLManip.setAttributeValue(outputPinNode, "value", name);
                XMLManip.setAttributeValue(outputPinNode, "type", "Type");
            }
        }
        return outputPinNode;
    }

    /**
     * Creates a new Token Descriptor tag for the specified XML DOM node.  The 
     * assumption is that the specified not is the node that must contain the 
     * TokenDescriptor node.  If the TokenDescriptor must reside under the 
     * TokenDescriptors tag then use one of the CreateTokenDescriptor methods.
     * 
     * @param pNode [in] The node to recieve the token descriptor.
     * @param type [in] The name of the token descriptor.
     * @param line [in] The token's line number.
     * @param col [in] The token's column number.
     * @param position [in] The token's file position number.
     * @param value [in] The token's value.
     * @param lenght [in] The token's value length.
     * @see #CreateTokenDescriptor(IXMLDOMNode* pNode, CComBSTR type, long line, long col, long pos, CComBSTR value, long length)
     */
    public void createDescriptor(Node pNode, String type, String value)
    {
        if (pNode == null) return;
        Node desc = XMLManip.createElement((Element) pNode, "TDescriptor");
        if (desc != null)
        {
            XMLManip.setAttributeValue(desc, "type", type);
            XMLManip.setAttributeValue(desc, "value", value);
        }
    }

    /**
     * Create a new XML node and added to the document.  CreateElement will throw
     * _com_error exceptions will an invalid HRESULT is received.
     * @param pOwner [in] The node that will own the new node.
     * @param nodeName [in] The name of the new XML node.
     * @param pNewNode [out] The new XML node. 
     */
    public Node createNode(Node owner, String nodeName)
    {
        if (owner == null || nodeName == null) return null;
        
        Document doc = owner.getDocument();
        // If we failed to retrieve the DOM document test if the 
        // specified node is the DOM document.
        if (doc == null && owner instanceof Document)
            doc = (Document) owner;
        
        if (doc != null)
        {
            Node newNode = XMLManip.createElement(doc, nodeName);
            newNode.detach();
            ((Element) owner).add(newNode);
            
            return newNode;
        }
        return null;
    }
    
    /**
     * Initializes the creation event data.
     *
     * @param pEvent [in] The event to initialize.
     */
    protected void setCreationEventData(Node event)
    {
        if (event == null) return ;
        
        Node descs = ensureElementExists(event, "TokenDescriptors", 
                            "TokenDescriptors");
        if (descs != null)
            createDescriptor(descs, "InstantiatedTypeName",
                    getInstantiatedTypeName());
    }
}
