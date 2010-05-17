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
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREArgument;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class ObjectInstanceInformation extends InstanceInformation
{
    private IREClass m_InstanceType;
    private IREClass m_InstantiatedType;
    private String   m_InstantiatedTypeName;
    
    public ObjectInstanceInformation(String name, String typeName, 
                                     IREClass type)
    {
        super(name, typeName);
        m_InstanceType = type;
    }
    
    public ObjectInstanceInformation()
    {
    }
    
    public boolean isPrimitive() 
    {
        return false;
    }

    /**
     * Retrieves the name of the instance type.  The type name 
     * will be the fully scoped name of the type.
     *
     * @return The fully scoped name of the instance type.
     */
    public String getInstanceTypeName() 
    {
        String typeName = null;
        
        // First try to get the fully scoped name from the 
        // IREClass.  If A IREClass was not found then call
        // CInstanceInformation::GetInstanceTypeName() to 
        // retrieve the name of the type.  The type name returned
        // by CInstanceInformation::GetInstanceTypeName() will be
        // the source name and thus will most likely be the short 
        // name.
        //
        // Fully Scoped Name: java.lang.String
        // Short Name       : String
        IREClass cl = getInstanceType();
        if (cl != null)
        {    
            String packageName = cl.getPackage();
            String className   = cl.getName();
            typeName = getFullName(packageName, className);
        }
        else
            typeName = m_InstantiatedTypeName;
        if (typeName == null || typeName.length() == 0)
            typeName = super.getInstanceTypeName();
        return typeName;
    }

    /**
     * Sets the type name used to instantiate the instance.;
     *
     * @param typeName [in] The type name.
     * @param pClassLoader [in] The class loader used to locate the type.
     */
    public void setInstantiatedType(String typeName, 
                                    IREClassLoader classLoader) 
    {
        if (typeName == null || typeName.length() == 0 || classLoader == null)
            return;
        
        m_InstantiatedType = null;
        
        // Set the data member m_InstantiatedType to the type that is 
        // specified by the typeName.  First look up the type defintion
        // using the class loader.
        m_InstantiatedType = findClass(classLoader, typeName);
        m_InstantiatedTypeName = typeName;
    }

    protected static String getFullName(String pack, String name)
    {
        StringBuffer fullname = new StringBuffer();
        if (pack != null)
            fullname.append(pack);
        if (name != null && name.length() > 0)
        {
            if (fullname.length() > 0)
                fullname.append("::");
            fullname.append(name);
        }
        return fullname.length() > 0? fullname.toString() : null;
    }
    
    /**
     * Retrieves the name of the type that was used to instantiate 
     * instance.
     *
     *
     * @return The type name.
     */
    public String getInstantiatedTypeName() 
    {
        String typeName = null;
        // First try to get the fully scoped name from the 
        // IREClass.  If A IREClass was not found then use
        // m_InstatiatedTypeName to retrieve the name of the type.
        // m_InstatiatedTypeName will be the source name and thus
        // will most likely be the short name.
        //
        // Fully Scoped Name: java.lang.String
        // Short Name       : String
        
        if (m_InstantiatedType != null)
        {
            String name        = m_InstantiatedType.getName();
            String packageName = m_InstantiatedType.getPackage();
            typeName = getFullName(packageName, name);
        }
        else
            typeName = m_InstantiatedTypeName;
        return typeName;
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
            IREClassLoader classLoader, boolean alwaysCreate)
    {
        MethodDeclaration decl = null;
        
        IREClass instanceType = getInstanceType();
        
        if(instanceType == null)
            instanceType = getInstantiatedType();
        
        //kris added
        if (instanceType == null) {
            instanceType = classLoader.loadClass(m_InstantiatedTypeName);
        }
        
        if (instanceType != null || isStatic())
        {
            decl = new MethodDeclaration();
            decl.setInstanceName(getInstanceName());
            
            OperationLocator opLocator = new OperationLocator(methodName, 
                                                              arguments);
            SourceElementLocator<MethodDeclaration> loc =
                        new SourceElementLocator<MethodDeclaration>();
            decl = loc.locate(opLocator, instanceType, classLoader, true,
                            true, decl);
            if ((decl == null || decl.getOperation() == null) && alwaysCreate)
                decl = new UnknownMethodDeclaration(methodName);
        }
        else if (alwaysCreate)
            decl = new UnknownMethodDeclaration(methodName);
        
        //check to see if a super class has been returned as the owner. If so,
        //set the owner to be the m_InstantiateType
        if (decl != null && m_InstantiatedType != null && decl.getOwner() != null &&
                (! decl.getOwner().getName().equals(m_InstantiatedType.getName())))
            decl.setOwner(m_InstantiatedType) ;
        
        if (decl != null && decl.getOperation() == null && !alwaysCreate)
            decl = null;

        return decl;
    }

    /**
     * Check if a instance is derived from a specified class.  The class hiearchy of the 
     * instance will be traced until the desiredType is located.
     *
     * @param desiredType [in] The type to find.
     * @param pLoader [in] The class loader to use when searching for type information.
     *
     * @return true if the desired type is found.
     */
    public boolean isDerivedFrom(String desiredType, IREClassLoader loader)
    {
        IREClass itype = getInstanceType();
        if (itype != null)
        {
            ClassLocator evaluator = new ClassLocator(desiredType);
            SourceElementLocator<IREClass> locator = 
                            new SourceElementLocator<IREClass>();
            
            IREClass ref = locator.locate(evaluator, itype, loader, true, 
                                            true, null);
            return ref != null;
        }
        return false;
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
                                                   IREClassLoader classLoader)
    {
        IREClass owner = getInstanceType();
        if (owner != null)
        {
            AttributeLocator attrLocator = new AttributeLocator(attrName);
            return new SourceElementLocator<InstanceInformation>()
                    .locate(attrLocator, owner, classLoader, true, false, 
                            null);
        }
        return null;
    }

    /**
     * Initializes the creation event data.
     *
     * @param pEvent [in] The event to initialize.
     */
    public void setCreationEventData(Node event)
    {
        if (event == null) return;
        
        super.setCreationEventData(event);
        Node descriptors = ensureElementExists(event, "TokenDescriptors", 
                                               "TokenDescriptors");
        if (descriptors != null)
            createDescriptor(descriptors, "IsPrimitive", "false");
        addClassifier(event);
    }
    
    protected void addClassifier(Node parentNode)
    {
        if (parentNode == null) return ;
        
        IREClass owner = getInstantiatedType();
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
    }

    /**
     * Initializes the IDestroyEvent before it is sent to the operation 
     * detail listeners.  InitializeDestroyEvent is virtual so decendents
     * can perform additional intialization.  This version will initialize
     * the properties InstanceName, InstanceTypeName, and IsPrimitive.  The
     * IsPrimitive will be set to true.
     *
     * @param pEvent [in] The event to initalize.
     */
    public void initializeDestroyEvent(Node event)
    {
        if (event == null) return ;
        
        super.setCreationEventData(event);
        Node desc = ensureElementExists(event, "TokenDescriptors",
                                        "TokenDescriptors");
        if (desc != null)
            createDescriptor(desc, "IsPrimitive", "false");
    }
    
    public EventExecutor.RefVariableDef getReferenceInfo()
    {
        EventExecutor.RefVariableDef ref = super.getReferenceInfo();
        ref.classDefinition = getInstanceType();
        return ref;
    }
    
    /**
     * Uses the class loader to find a class.  The "this" pointer is the 
     * class that is the owner of the operation that is being parsed.
     *
     * @param typeName [in] The name of the type to find.
     * @param pVal [out] The IREClass structure that represents the type.
     */
    protected IREClass findClass(IREClassLoader classLoader, String typeName)
    {
        if (typeName == null || typeName.length() == 0) return null;
        return findClass(classLoader, getInstanceOwner(), typeName);
    }
    
    /**
     * Uses the class loader to find a class.  The context class is used
     * to located the class.  The context is used to determine what 
     * dependencies to search.
     *
     * @param pContext [in] The "this" pointer.
     * @param typeName [in] The name of the type to find.
     * @param pVal [out] The IREClass structure that represents the type.
     */
    protected IREClass findClass(IREClassLoader classLoader, IREClass context,
                                 String typeName)
    {
        if (context == null || typeName == null || typeName.length() == 0)
            return null;
        
        return classLoader != null? classLoader.loadClass(typeName, context)
                                  : null;
    }

    /**
     * Sets the type of the instance.  The instance type is will be 
     * the reciever of all messages (method calls) made by the instance.
     *
     * @param *pVal [in] The instance type.
     */
    public void setInstanceType(IREClass val)
    {
        m_InstanceType = val;
    }

    /**
     * Retrieves the type of the instance.  The instance type is will be 
     * the reciever of all messages (method calls) made by the instance.
     *
     * @param pVal [out] The instance type, if the type is set then NULL is
     *                   returned.
     */
    public IREClass getInstanceType()
    {
        return m_InstanceType;
    }

    /**
     * Retrieves the type of the class that instantiated the instance.
     *
     * @param pVal [out] The instantiated type, if the type is set 
     *                   then NULL is returned.
     */
    public IREClass getInstantiatedType()
    {
        return m_InstantiatedType;
    }

    /**
     * Checks if the instance has been initialized.  If the instance 
     * information has not been initailized then the information is
     * not valid.
     *
     * @param *pVal [in] The value.
     */
    public boolean isValid()
    {
        String name = getInstanceName();
        IREClass type = getInstantiatedType();
        
        // If the name of the instance and the type of the instance
        // has been set then the CObjectInstanceInformation is valid.  Otherwise
        // The CObjectInstanceInformation is not valid.
        return name != null && name.length() > 0 && type != null;
    }
}
