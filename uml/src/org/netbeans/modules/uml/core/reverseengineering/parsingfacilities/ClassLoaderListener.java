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

import org.netbeans.modules.uml.core.reverseengineering.reframework.FileInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IAttributeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IFileInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IOperationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREAttribute;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClassElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRERealization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class ClassLoaderListener 
    implements IClassLoaderListener, IUMLParserEventsSink, 
                IUMLParserAtomicEventsSink
{
    private IFileInformation m_Information;
    private ETList<IREAttribute> m_TopLevelAttributes;
    private ETList<IREOperation> m_TopLevelOperations;    

    ////////////////////////////////////////////////////////////////////////
    // All empty methods are also empty in C++ source.
    
    /** 
     * Returns an IFileInformation object.  This object has a list of all
     * dependencies, packages, and classes found in the parsed file.
     * 
     * @param pVal[out] the IFileInformation object
     */
    synchronized public IFileInformation getFileInformation()
    {
        if (m_Information == null)
            m_Information = new FileInformation();
        return m_Information;
    }
    
    /** 
     * Returns true if @a pEvent refers to a top level operation
     * (i.e. an operation declared outside of a  class).
     * 
     * @param pEvent[in] the IOperationEvent object
     * @param bIsTopLevel[out] specifies whether or not @a refers to a top-
     *                         level operation.
     */
    protected boolean isTopLevelOperation(IOperationEvent event)
    {
        if (event == null) return false;

        IREOperation operation = event.getREOperation();
        return isTopLevelClassElement(operation);
    }
    
    /** 
     * Returns true if @a pEvent refers to a top level attribute
     * (i.e. an attribute declared outside of a  class).
     * 
     * @param pEvent[in] the IAttributeEvent object
     * @param bIsTopLevel[out] specifies whether or not @a refers to a top-
     *                         level attribute.
     */
    protected boolean isTopLevelAttribute(IAttributeEvent event)
    {
        if (event == null) return false;
        
        IREAttribute attr = event.getREAttribute();
        return isTopLevelClassElement(attr);
    }
    
    /** 
     * Determines if @a pElement is a top-level class element.  Top-level
     * means that the class element does not belong to a class.
     * 
     * @param pElement[in] the element to test
     * @param bIsTopLevel[out] the result of the test
     * 
     * @return HRESULT
     */
    protected boolean isTopLevelClassElement(IREClassElement element)
    {
        if (element == null) return false;
        return element.getOwner() == null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IClassLoaderListener#getTopLevelAttributes()
     */
    synchronized public ETList<IREAttribute> getTopLevelAttributes()
    {
        if (m_TopLevelAttributes == null)
            m_TopLevelAttributes = new ETArrayList<IREAttribute>();
        return m_TopLevelAttributes;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IClassLoaderListener#getTopLevelOperations()
     */
    synchronized public ETList<IREOperation> getTopLevelOperations()
    {
        if (m_TopLevelOperations == null)
            m_TopLevelOperations = new ETArrayList<IREOperation>();
        return m_TopLevelOperations;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onPackageFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPackageFound(IPackageEvent data, IResultCell cell)
    {
        if (data == null || cell == null) return;
        
        getFileInformation().addPackage(data);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onDependencyFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDependencyFound(IDependencyEvent data, IResultCell cell)
    {
        if (data == null || cell == null) return;
        
        getFileInformation().addDependency(data);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onClassFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onClassFound(IClassEvent data, IResultCell cell)
    {
        if (data == null || cell == null) return;
        
        IREClass reclass = data.getREClass();
        getFileInformation().addClass(reclass);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onBeginParseFile(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onBeginParseFile(String fileName, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onEndParseFile(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEndParseFile(String fileName, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink#onError(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onError(IErrorEvent data, IResultCell cell)
    {
        if (data == null || cell == null) return;
        
        getFileInformation().addError(data);
    }

    /** 
     * Called when an Attribute has been found in source code
     * 
     * @param data[in] the attribute event for the found attribute
     * @param cell[out] 
     */
    public void onAttributeFound(IAttributeEvent data, IResultCell cell)
    {
        if (isTopLevelAttribute(data))
        {
            IREAttribute attr = data.getREAttribute();
            if (attr != null)
                getTopLevelAttributes().add(attr);
        }
    }

    /** 
     * Called when an Operation has been found in source code
     * 
     * @param data[in] the attribute event for the found attribute 
     * @param cell[out] 
     */
    public void onOperationFound(IOperationEvent data, IResultCell cell)
    {
        if (isTopLevelOperation(data))
        {
            IREOperation op = data.getREOperation();
            if (op != null)
                getTopLevelOperations().add(op);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserAtomicEventsSink#onGeneralizationFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onGeneralizationFound(IREGeneralization data, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserAtomicEventsSink#onImplementationFound(org.netbeans.modules.uml.core.reverseengineering.reframework.IRERealization, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onImplementationFound(IRERealization data, IResultCell cell)
    {
    }
}
