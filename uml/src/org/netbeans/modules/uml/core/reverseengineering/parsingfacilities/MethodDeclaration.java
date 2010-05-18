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

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.EventExecutor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.MethodEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ScopeKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class MethodDeclaration
{
    private IREOperation m_Operation;
    private IREClass     m_Owner;
    private String       m_InstanceName;
    
    /**
     * Sends a method call event to all listeners.
     *
     * @param pDispatcher [in] The dispatcher used to send the event
     */
    public void sendMethodCallEvent(Node parentNode, int lineNum, 
                        InstanceInformation instance, 
                        ETList<ETPairT<InstanceInformation, String>> arguments, 
                        IUMLParserEventDispatcher dispatcher)
    {
        if (dispatcher == null) return;
        
        IMethodEvent event = new MethodEvent();
        Node eventData = generateXML(parentNode, lineNum, instance, arguments);
        if (eventData != null)
        {
            event.setEventData(eventData);
            dispatcher.fireMethodCall(event, null);
        }
    }

    /**
     * Generates the XMI fragment that represent the method call.
     *
     * @param pParentNode [in] The parent node of the result.
     * @param pVal [out] The result.
     */
    public Node generateXML(Node parentNode, int lineNumber, 
                        InstanceInformation instance, 
                        ETList<ETPairT<InstanceInformation, String>> arguments)
    {
        if (parentNode == null) return null;
        
        Node callNode = createNode(parentNode, "UML:CallBehaviorAction");
        if (callNode != null)
        {
            if (m_Operation != null)
            {
                XMLManip.setAttributeValue(callNode, "name", 
                                            m_Operation.getName());
                String instanceName = getInstanceName();
                
                if (instanceName == null || instanceName.length() == 0)
                    if ( ! (m_Operation.getOwnerScope() == ScopeKind.SK_CLASSIFIER))
                        instanceName = instance.getInstanceName();
                
                XMLManip.setAttributeValue(callNode, "instance", instanceName);
                XMLManip.setAttributeValue(callNode, "line", 
                                           String.valueOf(lineNumber));
                
                addArguments(callNode, arguments);
                addOperation(callNode);
            }
            addOwner(callNode, instance);
        }
        return callNode;
    }

    protected void addOperation(Node parentNode)
    {
        if (parentNode == null) return ;
        
        IREOperation op = getOperation();
        if (op != null)
        {
            Node operNode = op.getEventData();
            if (operNode != null)
            {
                Node clone = (Node) operNode.clone();
                ((Element) parentNode).add(clone);
            }
        }
    }
    
    protected void addOwner(Node parentNode, InstanceInformation instance)
    {
        if (parentNode == null) return ;
        
        IREClass owner = getOwner();
        if (owner != null)
        {
            Node node = owner.getEventData();
            if (node != null)
            {
                Node clone = (Node) node.clone();
                if (clone != null)
                    ((Element) parentNode).add(clone);
            }
        }

        String typeName = instance.getInstanceTypeName();
        if (typeName != null && typeName.length() > 0)
            EventExecutor.createTokenDescriptor(parentNode, typeName,
                        "DeclaringType", -1, -1, -1, -1);
    }
    
    /**
     * Adds the XML to represent the calls argumnts.
     *
     * @param pParentNode [in] The parent node.
     * @param arguments [in] The method call arguments .
     */
    protected void addArguments(Node parentNode, 
                      ETList<ETPairT<InstanceInformation, String>> arguments)
    {
        if (parentNode == null) return;
        
        IREOperation op = getOperation();
        if (op != null)
        {
            ETList<IREParameter> pars = op.getParameters();
            if (pars != null)
            {
                Node argumentNode = createNode(parentNode, 
                                            "UML:PrimitiveAction.argument");
                if (argumentNode != null)
                {
                    for (int i = 0, count = pars.size(), arg = 0, 
                            argc = arguments.size(); i < count; ++i)
                    {
                        IREParameter curPar = pars.get(i);
                        if (curPar == null) continue;
                        
                        int kind = curPar.getKind();
                        if (kind != IREParameter.PDK_RESULT && arg == argc)
                        {    
                            // I do not wish to report the error.
                            // AZTEC TODO
                            // SendErrorMessage(IDS_MISMATCH_ARGUMENTS, RE_E_MISMATCH_ARGUMENTS);
                        }
                        
                        if (arg < argc)
                        {
                            switch (kind)
                            {
                                case IREParameter.PDK_IN:
                                    createInputPin(argumentNode, curPar, 
                                           arguments.get(arg).getParamOne(),arguments.get(arg).getParamTwo());
                                    arg++;
                                    break;
                                case IREParameter.PDK_INOUT:
                                    createInputPin(argumentNode, curPar, 
                                        arguments.get(arg).getParamOne(),arguments.get(arg).getParamTwo());
                                    createOutputPin(argumentNode, curPar, 
                                        arguments.get(arg).getParamOne(),arguments.get(arg).getParamTwo());
                                    arg++;
                                    break;
                                case IREParameter.PDK_OUT:
                                    createOutputPin(argumentNode, curPar, 
                                        arguments.get(arg).getParamOne(),arguments.get(arg).getParamTwo());
                                    arg++;
                                    break;
                                case IREParameter.PDK_RESULT:
                                    createResultOutputPin(parentNode, curPar);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    // AZTEC: TODO: Translate this.
//    HRESULT CMethodDeclaration::SendErrorMessage(int msgID, int hrID)
//    {
//        HRESULT hr = S_OK;
//        
//        try
//        {
//            CComPtr < IREOperation > pOperation;
//            GetOperation(&pOperation);
//
//            if(pOperation != NULL)
//               {
//                CComBSTR name;
//                _VH(pOperation->get_Name(&name));
//                if(name.Length() > 0)
//                   {
//                    COM_ERROR_REPL(msgID, W2T(name), __uuidof(JavaUMLParserProcessor));
//                    hr = hrID;
//                }
//            }
//        }
//        catch(_com_error& e)
//        {
//            hr = COMErrorManager::ReportError(e);
//        }
//        
//        return hr;
//        
//    }
    
    /**
     * Creates an input pin that represents the parameter information.
     *
     * @param pParent [in] The owner of the input pin data.
     * @param pParam [in] The parameter.
     * @param ref [in] The instance information.
     * @param value parameter value
     * @param manip [out] The XMLManip used to manipulate the XML.
     */
    protected void createInputPin(Node parent, IREParameter par, 
                                  InstanceInformation ref,String value)
    {
        if (parent == null || ref == null) return ;
        
        Node inputPin = createNode(parent, "UML:InputPin");
        if (inputPin != null)
        {
            String instanceName = ref.getInstanceName();
            
            if (instanceName != null && instanceName.length() > 0)
            {
                XMLManip.setAttributeValue(inputPin, "value", instanceName);
                XMLManip.setAttributeValue(inputPin, "kind", "instance");
            }
            else
            {
                XMLManip.setAttributeValue(inputPin, "value", 
                                            ref.getInstanceTypeName());
                XMLManip.setAttributeValue(inputPin, "kind", "Type");
            }
            if(value!=null)
            {
                XMLManip.setAttributeValue(inputPin, "argumentValue", value);
            }
        }
    }
    
    /**
     * Creates an output pin that represents the parameter information.
     *
     * @param pParent [in] The owner of the input pin data.
     * @param pParam [in] The parameter.
     * @param ref [in] The instance information.
     * @param value argument value
     */
    protected void createOutputPin(Node parent, IREParameter par, 
                                   InstanceInformation ref,String value)
    {
        if (parent == null || par == null) return ;
        
        Node outputPin = createNode(parent, "UML:OutputPin");
        if (outputPin != null)
        {
            XMLManip.setAttributeValue(outputPin, "name", par.getName());
            
            if (ref != null)
            {
                String instanceName = ref.getInstanceName();
                if (instanceName != null && instanceName.length() > 0)
                {
                    XMLManip.setAttributeValue(outputPin, "value", 
                                                instanceName);
                    XMLManip.setAttributeValue(outputPin, "kind", "instance");
                }
                else
                {    
                    instanceName = ref.getInstanceTypeName();
                    XMLManip.setAttributeValue(outputPin, "value", 
                                                instanceName);
                    XMLManip.setAttributeValue(outputPin, "kind", "CallResult");
                }
                if(value!=null)
                {
                    XMLManip.setAttributeValue(outputPin, "argumentValue", value);
                }
            }
            else
            {    
                XMLManip.setAttributeValue(outputPin, "kind", "type");
                XMLManip.setAttributeValue(outputPin, "value", par.getType());
            }
        }
    }
    
    /**
     * Creates an result output pin that represents the parameter information.
     *
     * @param pParent [in] The owner of the input pin data.
     * @param pParam [in] The parameter.
     * @param manip [out] The XMLManip used to manipulate the XML.
     */
    protected void createResultOutputPin(Node parent, IREParameter par)
    {
        if (parent == null || par == null) return ;
        
        Node node = createNode(parent, "UML:PrimitiveAction.result");
        if (node != null)
            createOutputPin(node, par, null,null);
    }
    
    /**
     * Creates a CPrimitiveInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then a CPrimitiveInstanceInformation
     * will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    protected InstanceInformation createPrimitiveInstance(
            String name, String instanceType, IREClass instanceOwner)
    {
        if (instanceType != null && instanceType.length() > 0)
        {
            PrimitiveInstanceInformation info = 
                    new PrimitiveInstanceInformation(name, instanceType);
            info.setPrimitiveType(instanceType);
            info.setInstanceOwner(instanceOwner);
            
            return info;
        }
        return null;
    }
    
    /**
     * Creates a CObjectInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then an instance information 
     * object will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    protected InstanceInformation createObjectInstance(
            String name, IREClass type, IREClass instanceOwner)
    {
        if (type != null)
        {
            String packageName = type.getPackage();
            String className   = type.getName();
            
            String typeName = packageName != null && packageName.length() > 0
                    ? packageName + "::" + className : className;
            ObjectInstanceInformation info =
                    new ObjectInstanceInformation(name, typeName, type);
            info.setInstanceOwner(instanceOwner);
            return info;
        }
        
        return null;
    }
    
    /**
     * Create a new XML node and added to the document.  CreateElement will throw
     * _com_error exceptions will an invalid HRESULT is received.
     * @param pOwner [in] The node that will own the new node.
     * @param nodeName [in] The name of the new XML node.
     * @param pNewNode [out] The new XML node. 
     */
    protected Node createNode(Node owner, String name)
    {
        return XMLManip.createElement((Element) owner, name);
    }

    /**
     * Sets the operation that is being called by the method call.
     *
     * @param pOperation [in] The operation.
     */
    public void setOperation(IREOperation operation)
    {
        m_Operation = operation;
    }

    /**
     * Retrieves the operation that is being called by the method call.
     *
     * @param pVal [out] The operation.
     */
    public IREOperation getOperation()
    {
        return m_Operation;
    }

    /**
     * Sets the operations owner.
     *
     * @param pOwner [int] The operation's owner.
     */
    public void setOwner(IREClass owner)
    {
        m_Owner = owner;
    }

    /**
     * Retrieves the operations owner.
     *
     * @param pVal [out] The operation's owner.
     */
    public IREClass getOwner()
    {
        return m_Owner;
    }

    public void setInstanceName(String name)
    {
        m_InstanceName = name;
    }

    public String getInstanceName()
    {
        return m_InstanceName;
    }

    /**
     * Retrieves the instance information that represents the operations return type.
     *
     * @param pClassLoader [in] The classloader used to find the definition 
     *                          of the return type.
     *
     * @return 
     */
    public InstanceInformation getReturnInstance(IREClassLoader classLoader)
    {
        if (m_Operation != null)
        {
            boolean isPrimitive = m_Operation.getIsPrimitive();
            String typename = m_Operation.getType();
            IREClass clazz  = m_Operation.getOwner();
            
            if (isPrimitive)
                return createPrimitiveInstance("<RESULT>", typename, clazz);
            else if (typename != null && typename.length() > 0)
            {
                IREClass opret = classLoader.loadClass(typename, clazz);
                if (opret != null)
                    return createObjectInstance("<RESULT>", opret, clazz);
            }
        }
        return null;
    }
    
    public boolean equals(Object o)
    {
       if(o == null)
          return m_Operation==null;
          
       if(o instanceof IREOperation)
          return m_Operation==o;
          
       return super.equals(o);
    }
}
