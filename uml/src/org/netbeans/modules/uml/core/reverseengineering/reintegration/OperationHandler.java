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

package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.OpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.UMLParserUtilities;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.roundtripframework.RoundTripModeRestorer;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class OperationHandler
{
    public OperationHandler(ETList<IElement> operations)
    {
        setOperations(operations);
    }
    
    public OperationHandler(IElement operation)
    {
        ETList<IElement> ops = new ETArrayList<IElement>();
        ops.add(operation);
        setOperations(ops);
    }
    
    /**
     * Set the operation to process, also "clears" the state information
     */
    public void setOperations(ETList<IElement> operations)
    {
        m_OperationElements = operations;
        
        // Reset our other member variables
        m_Loader = null;
        m_Options = null;
    }

    public boolean guiGetUserInput()
    {
        return false;
    }

    /**
     * Process all the operations
     */
    public boolean processOperations(IUMLParsingIntegrator integrator)
    {
        RoundTripModeRestorer restorer = new RoundTripModeRestorer(RTMode.RTM_OFF);
        try
        {
            boolean allOpsProcessed = false;
            boolean bCancel = false;
            // Set the options to their defaults when none have already been set.
            if (m_Options == null)
            {
                m_Options = new OpParserOptions();
                bCancel = m_Options.initialize(m_OperationElements, true, true, true);
                m_Loader = m_Options.getClassLoader();
            }
            
            if (!bCancel)
            {  
               int count = m_OperationElements.size();
               for (int i = 0; i < count; ++i)
               {
                   UMLParserRegistrar reg = new UMLParserRegistrar();
                   IElement el = m_OperationElements.get(i);
                   if (!(el instanceof IOperation)) continue;
                
                   IOperation op = (IOperation) el;
                   IInteraction inter = getOperationsInteraction(op);
                   if (inter == null) continue;
                
                   IOperationRE opre = reg.getOperationRE();
                   if (opre != null)
                   {
                       opre.setUMLParsingIntegrator(integrator);
                       opre.setInteraction(inter);
                   }
                
                   if (processOperation(op))
                       addInteractionMessages(op, opre);
                
                   // Clear the interaction so that another will be created
                   // for the next operation being processed
                   m_Interaction = null;
                   // 114831, remove listener after operation is processed
                   reg.revokeOperationDetailsSink();
               }
            }
            
            return allOpsProcessed;
            
        } catch (Exception e) {
            //e.printStackTrace() ;
            
        } finally {
            restorer.restoreOriginalMode();
        }
        
        return false ;
    }
    
    protected boolean processOperation(IOperation op)
    {
        boolean opProcessed = false;
        String classifierQualifiedName = getClassifierQualifiedName(op);
        ETList<IElement> els = op.getSourceFiles();
        if (els != null)
        {
            // Loop through all the operations' source files
            opProcessed = true;
            
            for (int i = 0, count = els.size(); i < count; ++i)
            {
                IElement el = els.get(i);
                if (el == null) continue;
                
                String filename = getFileName(el);
                if (!parseOperation(op, filename, classifierQualifiedName))
                    opProcessed = false;
            }
        }
        return opProcessed;
    }
    
    /**
     * Add the messages at the begining and end that represent the operation
     */
    protected void addInteractionMessages(IOperation op, IOperationRE opre) 
    {
        // Fix W6744:  Add the messages at the begining and end that represent the operation
        ILifeline selfLifeline = opre.getSelfLifeline();
        if (selfLifeline != null)
        {
            IInteraction inter = getOperationsInteraction(op);
            if (inter != null)
            {
                ETList<IMessage> messages = inter.getMessages();
                if (messages != null)
                {
                    IMessage first = messages.size() > 0? 
                                                      messages.get(0) : null;
                    IMessage sendMessage = inter.insertMessage(first, 
                            selfLifeline, inter, op, 
                            BaseElement.MK_SYNCHRONOUS);
                    IMessage resultMessage = selfLifeline.createMessage(
                        inter, inter, inter, op, BaseElement.MK_RESULT);
                    if (sendMessage != null && resultMessage != null)
                        resultMessage.setSendingMessage(sendMessage);
                }
            }
        }
    }
    
    /**
     * Convert the operation to its meta data
     *
     * @param pElement[in] An ISourceFileArtifact associated with the pOperation
     * @param pOperation[in] The operation to convert
     */
    protected boolean parseOperation(IOperation op, String filename,
                                     String classifierQualifiedName)
    {
        boolean opParsed = false;
        IUMLParser parser = UMLParserRegistrar.getUMLParser();
        
        if (parser != null)
        {
            IREClass c = m_Loader.loadClassFromFile(filename, classifierQualifiedName);
            if (c != null)
            {
                IREOperation reop = UMLParserUtilities.findMatchingREOperation(c, op);
                if (reop != null)
                {
                    parser.processOperationFromFile(filename, reop, m_Options);
                    opParsed = true;
                }
            }
        }
        return opParsed;
    }
    
    /**
     * Retrieve the name of the input operation's classifier
     */
    protected String getClassifierQualifiedName(IOperation op)
    {
        IClassifier cl = op.getFeaturingClassifier();
        return cl != null? cl.getFullyQualifiedName(false) : null;
    }
    
    /**
     * Retrieve the file name from the source file artifact represented by the input IElement
     */
    protected String getFileName(IElement element)
    {
        if (element instanceof ISourceFileArtifact)
            return ((ISourceFileArtifact) element).getSourceFile();
        return null;
    }
    
    /**
     * Retrieve's the operation's interaction
     */
    protected IInteraction getOperationsInteraction(IOperation op)
    {
        if (m_Interaction == null)
        {
            if (op != null)
            {
                // Fix W6494:  Always create a new interaction so we don't get multiple lifelines
                m_Interaction = new TypedFactoryRetriever<IInteraction>().createType("Interaction");
                
                if (m_Interaction != null)
                {
                    m_Interaction.setOwner(op);
                    
                    // Give the interaction the same name as its owning operation
                    m_Interaction.setName(op.getName());
                }
            }
        }
        
        return m_Interaction;
    }
    
    private ETList<IElement> m_OperationElements;

    /// Parser information variables, cleared in SetOperations()
    private IREClassLoader m_Loader;
    private IOpParserOptions m_Options;

    /// The intaction the current operation is associated with, Access only via GetOperationsInteraction()
    private IInteraction m_Interaction;
}
