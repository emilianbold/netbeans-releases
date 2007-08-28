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

/*
 * File       : OperationRE.java
 * Created on : Dec 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import java.util.HashMap;
import java.util.Stack;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Multiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IParameterDirectionKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.modelanalysis.ClassifierUtilities;
import org.netbeans.modules.uml.core.reverseengineering.reframework.CreationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDestroyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IInitializeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPostProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREArgument;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREBinaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClause;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionJumpHandlerEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREMethodDetailData;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREMultiplicityRange;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRaisedException;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReturnEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REMultiplicityRange;
import org.netbeans.modules.uml.core.reverseengineering.reframework.UMLParserUtilities;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 * @author Aztec
 */
public class OperationRE implements IOperationRE
{
   private Stack < Integer > m_parsingStates = new Stack < Integer > ();

   private IUMLParsingIntegrator m_cpIntegrator;

   private IInteraction m_cpInteraction;

   /// lifeline representing "this" only access via GetThisLifeline()
   private ILifeline m_cpThisLifeline;

   /// The lifeline associated with the previous result
   private ILifeline m_cpResultLifeline;

   /// The name of the classifier that was the result of the previous action
   String m_bsResultClassifier;

   /// When true process element events, set to false when inside begin/end initialize/test
   boolean m_bProcessElementEvents = true;

   /// The interaction fragments we are currently processing
   private Stack < IInteractionFragment > m_stackInteractionFragments = new Stack < IInteractionFragment > ();

   /// The combined fragments we are currently processing
   private Stack < ICombinedFragment > m_stackCombinedFragments = new Stack < ICombinedFragment > ();

   //    Stack<ILifeline> StackLifelines = new  Stack<ILifeline>();
   /// The combined fragments we are currently processing
   HashMap < String, Stack < ILifeline >> m_mapLifelines = new HashMap < String, Stack < ILifeline >> ();

   HashMap < String, ILifeline > m_mapStaticLifelines = new HashMap < String, ILifeline > ();

   /// Use to find different elements, only access via GetElementLocator()
   private IElementLocator m_cpElementLocator;

   private String SPECIAL_THIS = "<THIS>";
   private String SPECIAL_SUPER = "<SUPER>";
   private String SPECIAL_RESULT = "<RESULT>";

   protected int getParsingState()
   {
      int nParsingState = IParsingState.OPS_UNKNOWN;

      if (m_parsingStates.size() > 0)
      {
         nParsingState = m_parsingStates.peek().intValue();
      }
      return nParsingState;
   }

   /**
    * Use the ElementLocator to find the lifeline under the member interaction
    */
   public ILifeline findLifelineInInteraction(String bstrLifelineName)
   {
      ILifeline ppLifeline = null;
      if (bstrLifelineName != null && bstrLifelineName.length() > 0)
      {
         INamespace cpNamespace = (m_cpInteraction instanceof INamespace) ? (INamespace)m_cpInteraction : null;
         if (cpNamespace != null)
         {
            ETList < INamedElement > cpElements = getElementLocator().findByName(cpNamespace, bstrLifelineName);
            if (cpElements != null)
            {
               int lCnt = cpElements.size();
               for (int lIndx = 0; lIndx < lCnt; lIndx++)
               {
                  INamedElement cpElement = cpElements.get(lIndx);
                  ILifeline cpLifeline = (cpElement instanceof ILifeline) ? (ILifeline)cpElement : null;
                  if (cpLifeline != null)
                  {
                     ppLifeline = cpLifeline;
                     break;
                  }
               }
            }
         }
      }
      return ppLifeline;
   }

   protected IElementLocator getElementLocator()
   {
      if (m_cpElementLocator == null)
         m_cpElementLocator = new ElementLocator();
      return m_cpElementLocator;
   }

   protected void insertMessage(IElement pFromElement, IElement pToElement, IOperation pOperation, int kind, int lLineNumber)
   {
      // Input parameters are checked by the corresponding InsertMessage() calls.

      IInteractionFragment cpCurrentIF = null;
      if (m_stackInteractionFragments.size() > 0)
      {
         //           if( kind == IMessageKind.MK_CREATE && IParsingState.OPS_TEST != getParsingState())
         //           {
         //              // Fix W6641:  Temporarily pop the top interaction fragment off the stack
         //              // so we can get the one under it.
         //                IInteractionFragment cpTempIF 
         //                    = m_stackInteractionFragments.peek();
         //
         //                m_stackInteractionFragments.pop();
         //                if( m_stackInteractionFragments.size() > 0)
         //                {
         //                    cpCurrentIF = m_stackInteractionFragments.peek();
         //                }
         //                m_stackInteractionFragments.push( cpTempIF );
         //           }
         //           else
         {
            cpCurrentIF = m_stackInteractionFragments.peek();
         }
      }
      IMessage cpMessage = null;

      ILifeline cpLifeline = (pFromElement instanceof ILifeline) ? (ILifeline)pFromElement : null;
      if (cpLifeline != null)
      {
         cpMessage = cpLifeline.insertMessage(null, cpCurrentIF, pToElement, cpCurrentIF, pOperation, kind);

         if (IMessageKind.MK_SYNCHRONOUS == kind)
         {
            insertMessage(pToElement, cpLifeline, pOperation, IMessageKind.MK_RESULT, lLineNumber);
         }
      }
      else
      {
         IInteraction cpInteraction = (pFromElement instanceof IInteraction) ? (IInteraction)pFromElement : null;
         if (cpInteraction != null)
         {
            cpMessage = cpInteraction.insertMessage(null, pToElement, cpCurrentIF, pOperation, kind);
         }
      }

      if (cpMessage != null)
      {
         cpMessage.setLineNumber(lLineNumber);
      }
   }

   protected IClassifier getClassifier(IREClass pREClass)
   {
      IClassifier cpClassifier = null;
      if (pREClass != null)
      {
         String packageName = pREClass.getPackage();
         String classifierName = pREClass.getName();

         // the C++ version doesn't add package here, but
         // I don't want to take the risk of changing this completely
         // a week from code complete
         if(packageName != null && packageName.length()>0)
            cpClassifier = getClassifier(packageName + "::" + classifierName);
         else
            cpClassifier = getClassifier(classifierName);
         
         if (cpClassifier == null && m_cpIntegrator != null)
         {
            // Did not find the classifier, so "build" it and search again
            m_cpIntegrator.addClassToProject(pREClass);
            cpClassifier = getClassifier(classifierName);
         }
      }
      return cpClassifier;
   }

   protected IClassifier getClassifier(String fullyScopedName)
   {
      IClassifier cpClassifier = null;
      if (fullyScopedName != null)
      {
         ETList < IElement > cpElements = getElementLocator().findScopedElements(m_cpInteraction, fullyScopedName);
         if (cpElements != null)
         {
            int lCnt = cpElements.size();
            IElement cpElement = null;
            for (int lIndx = 0; lIndx < lCnt; lIndx++)
            {
               cpElement = cpElements.get(lIndx);

               cpClassifier = (cpElement instanceof IClassifier) ? (IClassifier)cpElement : null;
               if (cpClassifier != null)
                  break;
            }
         }
      }
      return cpClassifier;
   }

   protected IClassifier createClassifier(String fullyScopedName)
   {
      IClassifier retVal = null;

      IElement cpElement = getElementLocator().resolveScopedElement(m_cpInteraction, fullyScopedName);
      if (cpElement != null)
      {
         retVal = (cpElement instanceof IClassifier) ? (IClassifier)cpElement : null;
      }
      return retVal;
   }

   protected IOperation getConstructorOperation(ICreationEvent pEvent)
   {

      IOperation ppOperation = null;
      IClassifier cpClassifier = null;
      IREClass cpREClass = pEvent.getDeclaringClass();
      if (cpREClass != null)
      {
         cpClassifier = getClassifier(cpREClass);
         if (cpClassifier != null)
         {
            IREOperation cpREOperation = pEvent.getConstructor();
            if (cpREOperation != null)
            {
               ppOperation = UMLParserUtilities.findMatchingOperation(cpClassifier, cpREOperation);
            }
         }
      }
      // Find the constructor via the model, if necessary
      if (ppOperation == null)
      {
         String bsInstantiatedTypeName = pEvent.getInstantiatedTypeName();
         cpClassifier = getClassifier(bsInstantiatedTypeName);
         if (cpClassifier != null)
         {
            ETList < IOperation > cpOperations = cpClassifier.getOperations();
            if (cpOperations != null)
            {
               int lCnt = cpOperations.size();

               for (int lIndx = 0; lIndx < lCnt; lIndx++)
               {
                  IOperation cpOperation = cpOperations.get(0);
                  if (cpOperation != null)
                  {
                     boolean vbIsConstructor = cpOperation.getIsConstructor();
                     if (vbIsConstructor)
                     {
                        ppOperation = cpOperation;
                        break;
                     }
                  }
               }
            }
         }
      }

      /*IOperation cpOperation = null;
      IClassifier cpClassifier = getClassifier(classifierName);
      if( cpClassifier != null)
      {
          ETList< IOperation > cpOperations 
                  = cpClassifier.getOperations();
          if( cpOperations != null )
          {
              int lCnt = cpOperations.size();
      
              for(int lIndx = 0 ; lIndx < lCnt ; lIndx++)
              {
                  cpOperation = cpOperations.get(lIndx);
      
                  if( cpOperation != null && cpOperation.getIsConstructor())
                      break;                 
              }
          }
      }*/
      return ppOperation;
   }

   protected ILifeline getThisLifeline()
   {
      if (m_cpThisLifeline == null)
      {
         String strThis = REIntegrationMessages.getString("IDS_THIS");
         m_cpThisLifeline = createLifeline(strThis);
      }

      if (m_cpThisLifeline != null)
      {
         // Determine "this"'s representing classifier
         IClassifier cpRepresentingClassifier = OwnerRetriever.getOwnerByType(m_cpInteraction, IClassifier.class);
         if (cpRepresentingClassifier != null)
         {
            m_cpThisLifeline.setRepresentingClassifier(cpRepresentingClassifier);
         }
      }
      return m_cpThisLifeline;
   }

   protected ETPairT < ILifeline, Boolean > getLifeline(String lifelineName)
   {
      boolean lifelineCreated = false;
      if (m_cpInteraction == null)
         return null;

      ILifeline retVal = null;

      String bstrLifelineName = lifelineName;
      if (SPECIAL_RESULT.equals(bstrLifelineName))
      {
         bstrLifelineName = "";
      }

      if (lifelineName != null && lifelineName.length() > 0)
      {
         if (SPECIAL_THIS.equals(lifelineName) || SPECIAL_SUPER.equals(lifelineName))
         {
            retVal = getThisLifeline();
         }
         else if ((retVal = getLifelineFromMap(lifelineName)) == null)
         {
            INamespace cpNamespace = (m_cpInteraction instanceof INamespace) ? (INamespace)m_cpInteraction : null;
            if (cpNamespace != null)
            {
               ETList < INamedElement > cpElements = getElementLocator().findByName(cpNamespace, lifelineName);

               if (cpElements != null)
               {
                  int lCnt = cpElements.size();

                  for (int lIndx = 0; lIndx < lCnt; lIndx++)
                  {
                     INamedElement cpElement = cpElements.get(lIndx);
                     retVal = (cpElement instanceof ILifeline) ? (ILifeline)cpElement : null;
                     if (retVal != null)
                        break;
                  }
               }
            }

            if (retVal == null)
            {
               retVal = createLifeline(lifelineName);
               lifelineCreated = true;
            }
         }
      }
      else if (m_cpResultLifeline != null)
      {
         retVal = m_cpResultLifeline;
      }
      else
      {
         retVal = createLifeline("");
         if (retVal != null)
         {
            attachRepresentingClassifier(m_bsResultClassifier, retVal);
            lifelineCreated = true;
         }
      }
      return new ETPairT < ILifeline, Boolean > (retVal, new Boolean(lifelineCreated));
   }

   protected ETPairT < ILifeline, Boolean > getStaticLifeline(String classifierName)
   {
      ILifeline retVal = null;
      boolean created = false;

      if (classifierName != null)
      {
         if ((retVal = getStaticLifelineFromMap(classifierName)) == null)
         {
            retVal = createStaticLifeline(classifierName);
            created = true;
         }
      }
      return new ETPairT < ILifeline, Boolean > (retVal, new Boolean(created));
   }

   protected ILifeline getLifelineFromMap(String lifelineName)
   {
      if (m_mapLifelines != null)
         return StackGetLifeline(lifelineName);

      return null;
   }

   protected ILifeline getStaticLifelineFromMap(String classifierName)
   {
      ILifeline retVal = null;
      if (classifierName != null)
      {
         retVal = m_mapStaticLifelines.get(classifierName);
      }
      return retVal;
   }

   protected ILifeline createLifeline(String lifelineName)
   {
      if (m_cpInteraction == null)
         return null;

      // Create the lifeline
      ILifeline cpLifeline = new TypedFactoryRetriever < ILifeline > ().createType("Lifeline");
      if (cpLifeline != null)
      {
         m_cpInteraction.addLifeline(cpLifeline);
         cpLifeline.setName(lifelineName);

         addLifeline(lifelineName, cpLifeline);
      }
      return cpLifeline;
   }

   protected ILifeline createStaticLifeline(String classifierName)
   {
      if (m_cpInteraction == null)
         return null;

      // Create the lifeline
      ILifeline cpLifeline = new TypedFactoryRetriever < ILifeline > ().createType("Lifeline");
      if (cpLifeline != null)
      {
         m_cpInteraction.addLifeline(cpLifeline);

         m_mapStaticLifelines.put(classifierName, cpLifeline);
      }
      return cpLifeline;
   }

   protected boolean attachRepresentingClassifier(IREClass pREClass, ILifeline pLifeline)
   {
      if (pLifeline == null)
         return false;
      boolean bAttached = false;

      // Determine the lifeline's representing classifier
      // Determine the lifeline's representing classifier
      IClassifier cpRepresentingClassifier = getClassifier(pREClass);
      if (cpRepresentingClassifier != null)
      {
         pLifeline.setRepresentingClassifier(cpRepresentingClassifier);
         bAttached = true;
      }
      return bAttached;
   }

   protected boolean attachRepresentingClassifier(String className, ILifeline pLifeline)
   {
      if (pLifeline == null)
         return false;
      boolean bAttached = false;

      // Determine the lifeline's representing classifier
      IClassifier cpRepresentingClassifier = getClassifier(className);
      if (cpRepresentingClassifier == null)
      {
         cpRepresentingClassifier = createClassifier(className);
      }

      if (cpRepresentingClassifier != null)
      {
         pLifeline.setRepresentingClassifier(cpRepresentingClassifier);
         bAttached = true;
      }
      return bAttached;
   }

   protected ICombinedFragment createCombinedFragment(int interOper)
   {
      ICombinedFragment cpCombinedFragment = new TypedFactoryRetriever < ICombinedFragment > ().createType("CombinedFragment");
      if (cpCombinedFragment != null)
      {
         cpCombinedFragment.setOperator(interOper);

         m_cpInteraction.addFragment(cpCombinedFragment);

         // Add the comgined fragment to the current interaction operand, if it exists
         IInteractionOperand cpOperand = getCurrentInteractionOperand();
         if (cpOperand != null)
         {
            cpOperand.addFragment(cpCombinedFragment);
         }
         m_stackCombinedFragments.push(cpCombinedFragment);
      }

      return cpCombinedFragment;
   }

   protected IInteractionOperand createInteractionOperand()
   {
      ICombinedFragment cpCombinedFragment = null;
      IInteractionOperand cpInteractionOperand = null;
      if (m_stackCombinedFragments.size() > 0)
      {
         cpCombinedFragment = m_stackCombinedFragments.peek();
      }
      if (cpCombinedFragment != null)
      {
         cpInteractionOperand = cpCombinedFragment.createOperand();
         if (cpInteractionOperand != null)
         {
            pushInteractionFragment(cpInteractionOperand);
         }
      }
      return cpInteractionOperand;
   }

   protected void pushInteractionFragment(IInteractionFragment interactionFragment)
   {
      if (interactionFragment == null)
         throw new IllegalArgumentException();
      m_stackInteractionFragments.push(interactionFragment);
   }

   protected void addLineNumberToCombinedFragment(IREMethodDetailData data)
   {
      if (m_stackCombinedFragments.size() > 0)
      {
         ICombinedFragment cpCombinedFragment = m_stackCombinedFragments.peek();
         if (cpCombinedFragment != null)
         {
            long lLineNumber = data.getLine();

            if (lLineNumber > 0)
            {
               cpCombinedFragment.setLineNumber((int)lLineNumber);
            }
         }
      }
   }

   protected IInteractionOperand getInteractionOperand(IInteractionFragment pInteractionFragment, int lIndex)
   {
      if (pInteractionFragment == null || lIndex < 0)
         return null;
      IInteractionOperand retVal = null;
      ICombinedFragment cpCombinedFragment = (pInteractionFragment instanceof ICombinedFragment) ? (ICombinedFragment)pInteractionFragment : null;
      if (cpCombinedFragment == null)
      {
         return null;
      }

      ETList < IInteractionOperand > cpInteractionOperands = cpCombinedFragment.getOperands();
      if (cpInteractionOperands != null)
      {
         int lCnt = cpInteractionOperands.size();

         if (lIndex < lCnt)
         {
            retVal = cpInteractionOperands.get(lIndex);
         }
      }
      return retVal;
   }

   protected void createInteractionOperandGuard(String guard, int lLineNumber)
   {
      if (guard != null)
      {
         IInteractionOperand cpOperand = getCurrentInteractionOperand();
         if (cpOperand != null)
         {
            IInteractionConstraint cpInteractionConstraint = cpOperand.createGuard();
            if (cpInteractionConstraint != null)
            {
               cpInteractionConstraint.setExpression(guard);
            }

            cpOperand.setLineNumber(lLineNumber);
         }
      }
   }

   protected IInteractionOperand getCurrentInteractionOperand()
   {
      IInteractionOperand cpOperand = (m_stackInteractionFragments.size() > 0) ? (IInteractionOperand)m_stackInteractionFragments.peek() : null;
      return cpOperand;
   }

   protected IInteractionConstraint createInteractionConstraint(String guard)
   {
      IInteractionConstraint cpInteractionConstraint = new TypedFactoryRetriever < IInteractionConstraint > ().createType("InteractionConstraint");
      if (cpInteractionConstraint != null)
      {
         cpInteractionConstraint.setExpression(guard);
      }

      return cpInteractionConstraint;
   }

   protected IOperation getOperation(IMethodEvent pEvent, ILifeline pLifeline)
   {
      if (pEvent == null)
         return null;
      IOperation retVal = null;
      IClassifier cpClassifier = null;

      String methodName = pEvent.getMethodName();
      if (methodName != null && methodName.length() > 0)
      {         
         IREClass cpREClass = pEvent.getREClass();
         if (cpREClass != null)
         {
//             String classifierName = pEvent.getFullQNameOfOwner();
             String classifierName = pEvent.getDeclaringClassName();
            cpClassifier = getClassifier(classifierName);
         }
         else
         {
            // When the operation is being called via the "super"
            // use the parent classifier to find the operation
            String instanceName = pEvent.getInstanceName();
            if (SPECIAL_RESULT.equals(instanceName))
            {
               // UPDATE:  for now we are ignoring the <RESULT> instance name
               instanceName = "";
            }

            if (SPECIAL_SUPER.equals(instanceName))
            {
               cpClassifier = getSuperOfRepresentingClassifier(pLifeline);
               if (cpClassifier == null)
               {
                  if (pLifeline != null)
                  {
                     cpClassifier = pLifeline.getRepresentingClassifier();
                  }
                  else
                  {
                     // Get the classifier from the model
                     // This classifier should already exist,
                     // so don't create it if it does not

                     String classifierName = pEvent.getDeclaringClassName();
                     cpClassifier = getClassifier(classifierName);
                  }
               }
            }
            
            if(cpClassifier == null)
            {
               if(pLifeline != null)
               {
                  cpClassifier = pLifeline.getRepresentingClassifier();
               }
               else
               {
                  String classifierName = pEvent.getDeclaringClassName();
                  cpClassifier = getClassifier(classifierName);
               }
            }
         }
         if (cpClassifier != null)
         {
            IREOperation cpREOperation = pEvent.getOperation();
            if (cpREOperation != null)
            {
               retVal = UMLParserUtilities.findMatchingOperation(cpClassifier, cpREOperation);
            }

            // Find the 1st operation that has the same name
            if (retVal == null)
               retVal = findOperationOfSameName(cpClassifier, methodName);

            // If we did not find the operation on the classifier ...
            if (retVal == null) {
                IDataType cpDataType = (cpClassifier instanceof IDataType) ? (IDataType)cpClassifier : null;
                
                //kris richards - CreateOperation pref deleted. Set to true.
                // ... and the classifier is an IDataType or the preference is to create new operations,
                // create a new method
                
                // Create the Operation
                retVal = cpClassifier.createOperation("", methodName);
                if (retVal != null) {
                    // Add the operation to the classifier before processing the parameter,
                    // because the parameters need to be resolved via the containing project.
                    cpClassifier.addOperation(retVal);
                    // Fill in the parameters for the new operation
                    if (cpREOperation != null)
                        copyREOperationParameters(cpREOperation, retVal);
                    else
                        copyREArgumentsAsParameters(pEvent, retVal);
                }
                
            }
         }
      }
      if (retVal == null)
      {
         // Inform the user that we were not able to create an operation
         String classifierName = null;
         if (cpClassifier != null)
            classifierName = cpClassifier.getName();
         else
            classifierName = REIntegrationMessages.getString("IDS_UNKNOWN");

         String format = REIntegrationMessages.getString("IDS_W_NO_OPERATION");
         String message = REIntegrationMessages.getString(format, new Object[] { classifierName, methodName });

         new UMLMessagingHelper(REIntegrationMessages.getString("IDS_MESSAGINGFACILITY")).sendWarningMessage(message);
      }
      return retVal;
   }

   /**
    * Retrieves the one & only super class from the lifeline's representing classifier
    */
   private IClassifier getSuperOfRepresentingClassifier(ILifeline pLifeline)
   {
      IClassifier ppClassifier = null;
      IClassifier cpThisClassifier = pLifeline.getRepresentingClassifier();
      if (cpThisClassifier != null)
      {
         ETList < IGeneralization > cpGeneralizations = cpThisClassifier.getGeneralizations();
         if (cpGeneralizations != null)
         {
            int lCnt = cpGeneralizations.size();
            if (lCnt > 0)
            {
               IGeneralization cpGeneralization = cpGeneralizations.get(0);
               if (cpGeneralization != null)
               {
                  return cpGeneralization.getGeneral();
               }
            }
         }
      }
      return ppClassifier;
   }

   /**
    * Retreives the 1st operation in the input classifier's hierarchy that has the same name
    */
   protected IOperation findOperationOfSameName(IClassifier pClassifier, String methodName)
   {
      IOperation ppOperation = null;
      if (pClassifier != null && methodName != null)
      {
         ClassifierUtilities cpUtils = new ClassifierUtilities();
         if (cpUtils != null)
         {
            ETList < IOperation > cpOperations = cpUtils.collectAllOperations(pClassifier);
            if (cpOperations != null)
            {
               int lCnt = cpOperations.size();
               for (int lIndx = 0; lIndx < lCnt; lIndx++)
               {
                  IOperation cpOperation = cpOperations.get(lIndx);
                  if (cpOperation != null)
                  {
                     String bsName = cpOperation.getName();
                     if (methodName.equals(bsName))
                     {
                        ppOperation = cpOperation;
                        break;
                     }
                  }
               }
            }
         }
      }
      return ppOperation;
   }

   /**
    * Transfer the parameters from the Parsed event's operation to the input operation
    */
   private void copyREOperationParameters(IREOperation pREOperation, IOperation pOperation)
   {

      if ((pREOperation != null) && (pOperation != null))
      {
         ETList < IREParameter > cpREParameters = pREOperation.getParameters();
         if (cpREParameters != null)
         {
            int lCnt = cpREParameters.size();
            for (int lIndx = 0; lIndx < lCnt; lIndx++)
            {
               IREParameter cpREParameter = cpREParameters.get(lIndx);
               if (cpREParameter != null)
               {
                  String type = cpREParameter.getType();
                  String name = cpREParameter.getName();

                  int kind = cpREParameter.getKind();
                  IParameter cpParameter = null;
                  {
                     if (IParameterDirectionKind.PDK_RESULT == kind)
                     {
                        // If a result parameter already exists, handle it here
                        cpParameter = pOperation.getReturnType();
                        if (cpParameter != null)
                        {
                           cpParameter.setType2(type);
                           cpParameter.setName(name);
                        }
                     }
                     else
                     {
                        cpParameter = pOperation.createParameter(type, name);
                        if (cpParameter != null)
                        {
                           cpParameter.setDirection(kind);
                           pOperation.addParameter(cpParameter);
                        }
                     }
                     
                     copyParameterMultiplicity(cpParameter, cpREParameter) ;
                  }
               }
            }
         }
      }
   }

   private void copyParameterMultiplicity(IParameter parameter, IREParameter reParameter) {
       ETList<IREMultiplicityRange> mults = reParameter.getMultiplicity() ;
       
       IMultiplicity m = parameter.getMultiplicity() ;
       
       for (IREMultiplicityRange mult: mults) {
           IMultiplicityRange range = m.createRange() ;
           range.setLower(mult.getLower()) ;
           range.setUpper(mult.getUpper()) ;
           
           m.addRange( range );
       }
       
   }
   
   /**
    * Transfer the arguments from the Parsed event to the input operation
    */
   private void copyREArgumentsAsParameters(IMethodEvent pEvent, IOperation pOperation)
   {
      ETList < IREArgument > cpREArguments = pEvent.getArguments();
      if (cpREArguments != null)
      {
         int lCnt = cpREArguments.size();
         for (int lIndx = 0; lIndx < lCnt; lIndx++)
         {
            IREArgument cpREArgument = cpREArguments.get(lIndx);
            if (cpREArgument != null)
            {
               String type = cpREArgument.getType();
               String name = cpREArgument.getName();
               IParameter cpParameter = pOperation.createParameter(type, name);
               if (cpParameter != null)
               {
                  // UPDATE: We are currently not supporting argument values.  When
                  //         Message get the capability to store the argument values
                  //         we have to make the parser report the argument values.
                  // CComBSTR bsValue;
                  // _VH( cpREArgument->get_Value( &bsValue ));
                  // _VH( cpParameter->put_Default2( bsValue ));

                  pOperation.addParameter(cpParameter);
               }
            }
         }
      }
   }

   /**************************************************************************
    ************************** Implemented Methods **************************/

   /**
    * The interaction used to add the parsed information
    */
   public IInteraction getInteraction()
   {
      return m_cpInteraction;
   }

   /**
    * The lifeline used to represent self (this)
    */
   public ILifeline getSelfLifeline()
   {
      return getThisLifeline();
   }

   /**
    * The parent IUMLParsingIntegrator
    */
   public IUMLParsingIntegrator getUMLParsingIntegrator()
   {
      return m_cpIntegrator;
   }

   /**
    * The interaction used to add the parsed information
    */
   public void setInteraction(IInteraction pInteraction)
   {
      if (pInteraction == null)
         return;

      m_cpInteraction = pInteraction;
      m_cpThisLifeline = null;
      m_cpResultLifeline = getThisLifeline();

      pushInteractionFragment(m_cpInteraction);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IOperationRE#setSelfLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
    */
   public void setSelfLifeline(ILifeline pLifeline)
   {
      m_cpThisLifeline = pLifeline;
   }

   /**
    * The parent IUMLParsingIntegrator
    */
   public void setUMLParsingIntegrator(IUMLParsingIntegrator pIntegrator)
   {
      m_cpIntegrator = pIntegrator;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginClause(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeginClause(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      createInteractionOperand();
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onBeginConditional(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      // Create a combined fragment
      createCombinedFragment(BaseElement.IO_ALT);
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a critical section.
    *
    * @param cell [in] The event result cell.
    */
   public void onBeginCriticalSection(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         return;

      // Create a combined fragment
      createCombinedFragment(BaseElement.IO_REGION);
      createInteractionOperand();
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onBeginExceptionJumpHandler(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         return;

      // pop the current interaction operand, and its parent combined fragment.  The
      // parents interaction is the body of the combined fragment.
      if (m_stackInteractionFragments.size() > 0)
         m_stackInteractionFragments.pop();

      // Create an interaction operand
      createInteractionOperand();
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onBeginExceptionProcessing(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         return;

      // Create a combined fragment
      createCombinedFragment(BaseElement.IO_ASSERT);
      createInteractionOperand();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginInitialize(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeginInitialize(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      m_parsingStates.push(new Integer(IParsingState.OPS_INITIALIZE));
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onBeginLoop(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      // Create a combined fragment
      createCombinedFragment(BaseElement.IO_LOOP);
      createInteractionOperand();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginPostProcessing(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeginPostProcessing(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();
      m_bProcessElementEvents = false;
      m_parsingStates.push(new Integer(IParsingState.OPS_POST_PROCESSING));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginRaisedException(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeginRaisedException(IResultCell cell)
   {
      if (cell == null)
         return;

      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBeginTest(org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeginTest(IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      m_parsingStates.push(new Integer(IParsingState.OPS_TEST));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onBinaryOperator(org.netbeans.modules.uml.core.reverseengineering.reframework.IREBinaryOperator, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBinaryOperator(IREBinaryOperator event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onConditional(org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onConditional(IREConditional event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onCreateAction(org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCreateAction(ICreationEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      if (m_bProcessElementEvents)
      {
         // Determine the name of the lifeline
         String instanceName = event.getInstanceName();
         if (SPECIAL_RESULT.equals(instanceName))
         {
            // UPDATE:  for now we are ignoring the <RESULT> instance name
            instanceName = "";
         }

         // Create the new lifeline
         ILifeline lifeline = createLifeline(instanceName);
         if (lifeline != null)
         {
            m_cpResultLifeline = lifeline;

            // Determine the lifeline's representing classifier
            IREClass rec = event.getREClass();
            if (!attachRepresentingClassifier(rec, lifeline))
            {
               String instanceTypeName = event.getInstanceTypeName();
               attachRepresentingClassifier(instanceTypeName, lifeline);
            }

            // Determine the proper create operation
            String instantiatedTypeName = event.getInstantiatedTypeName();
            IOperation operation = getConstructorOperation(event);

            // Determine the line number to be associated with the message
            int lineNumber = event.getLine();

            insertMessage(getThisLifeline(), lifeline, operation, BaseElement.MK_CREATE, lineNumber);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onCriticalSection(org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCriticalSection(IRECriticalSection event, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onDestroyAction(org.netbeans.modules.uml.core.reverseengineering.reframework.IDestroyEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDestroyAction(IDestroyEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      if (m_bProcessElementEvents)
      {
         // Determine the referenced of the lifeline
         String instanceName = event.getInstanceName();
         if (SPECIAL_RESULT.equals(instanceName))
         {
            // UPDATE:  for now we are ignoring the <RESULT> instance name
            instanceName = "";
         }
         ILifeline lifeline = getLifeline(instanceName).getParamOne();
         if (lifeline != null)
            lifeline.createDestructor();

         removeLifeline(instanceName);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndClause(org.netbeans.modules.uml.core.reverseengineering.reframework.IREClause, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onEndClause(IREClause pEvent, IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      // UPDATE when issue W5711 is partially resolved
      boolean isDeterminate = false;
      if (pEvent != null)
         isDeterminate = pEvent.getIsDeterminate();

      if (isDeterminate)
      {
         int lineNumber = pEvent.getLine();
         String guard = REIntegrationMessages.getString("IDS_ELSE");
         createInteractionOperandGuard(guard, lineNumber);
      }

      // pop the interaction operand
      if (m_stackInteractionFragments.size() > 0)
         m_stackInteractionFragments.pop();
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onEndConditional(IREConditional event, IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpIntegrator == null)
         throw new NullPointerException();

      // pop the combined fragment
      if (m_stackCombinedFragments.size() > 0)
         m_stackCombinedFragments.pop();
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a critical section.
    *
    * @param cell [in] The event result cell.
    */
   public void onEndCriticalSection(IRECriticalSection event, IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpIntegrator == null)
         throw new NullPointerException();

      // pop the current interaction operand, and its parent combined fragment
      if (m_stackInteractionFragments.size() > 0)
         m_stackInteractionFragments.pop();
      if (m_stackCombinedFragments.size() > 0)
      {
         addLineNumberToCombinedFragment(event);
         m_stackCombinedFragments.pop();
      }
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onEndExceptionJumpHandler(IREExceptionJumpHandlerEvent event, IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpIntegrator == null)
         throw new NullPointerException();

      int lineNumber = event.getLine();
      boolean isDefault = event.getIsDefault();
      if (!isDefault)
      {
         String guard = event.getStringRepresentation();
         createInteractionOperandGuard(guard, lineNumber);
      }
      else
      {
         String guard = REIntegrationMessages.getString("IDS_FINALLY");
         createInteractionOperandGuard(guard, lineNumber);
      }
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onEndExceptionProcessing(IREExceptionProcessingEvent event, IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         return;

      // pop the current interaction operand, and its parent combined fragment
      if (m_stackInteractionFragments.size() > 0)
         m_stackInteractionFragments.pop();
      if (m_stackCombinedFragments.size() > 0)
      {
         addLineNumberToCombinedFragment(event);
         m_stackCombinedFragments.pop();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndInitialize(org.netbeans.modules.uml.core.reverseengineering.reframework.IInitializeEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onEndInitialize(IInitializeEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;

      if (m_cpInteraction == null)
         throw new NullPointerException();

      m_bProcessElementEvents = true;
      if (m_parsingStates.pop().intValue() != IParsingState.OPS_INITIALIZE)
         throw new IllegalStateException();
   }

   /**
    * Notifies the listeners that the parser is about to start processing an 
    * initialization section of a combined fragment.
    *
    * @param cell [in] The event result cell.
    */
   public void onEndLoop(IRELoop event, IResultCell cell)
   {
      if (cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      // pop the current interaction operand, and its parent combined fragment
      if (m_stackInteractionFragments.size() > 0)
         m_stackInteractionFragments.pop();
      if (m_stackCombinedFragments.size() > 0)
      {
         addLineNumberToCombinedFragment(event);
         m_stackCombinedFragments.pop();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndPostProcessing(org.netbeans.modules.uml.core.reverseengineering.reframework.IPostProcessingEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onEndPostProcessing(IPostProcessingEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;

      if (m_cpInteraction == null)
         throw new NullPointerException();

      m_bProcessElementEvents = true;
      if (m_parsingStates.pop().intValue() != IParsingState.OPS_POST_PROCESSING)
         throw new IllegalStateException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndRaisedException(org.netbeans.modules.uml.core.reverseengineering.reframework.IRaisedException, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onEndRaisedException(IRaisedException event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;

      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onEndTest(org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onEndTest(ITestEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;

      if (m_cpInteraction == null)
         throw new NullPointerException();

      int lineNumber = event.getLine();
      String guard = event.getStringRepresentation();
      createInteractionOperandGuard(guard, lineNumber);

      m_bProcessElementEvents = true;
      if (m_parsingStates.pop().intValue() != IParsingState.OPS_TEST)
         throw new IllegalStateException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onJumpEvent(org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onJumpEvent(IJumpEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;

      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onLoop(org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onLoop(IRELoop event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      int lineNumber = event.getLine();
      String guard = event.getStringRepresentation();
      createInteractionOperandGuard(guard, lineNumber);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onMethodCall(org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onMethodCall(IMethodEvent e, IResultCell cell)
   {
      if (e == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      if (m_bProcessElementEvents)
      {
         // Determine the referenced of the lifeline
         String instanceName = e.getInstanceName();
         if (SPECIAL_RESULT.equals(instanceName))
         {
            // UPDATE:  for now we are ignoring the <RESULT> instance name
            instanceName = "";
         }

         String classifierName = e.getDeclaringClassName();

         ILifeline lifeline = null;
         boolean lifelineCreated = false;
         if (instanceName == null || instanceName.length() == 0)
         {
            ETPairT < ILifeline, Boolean > pair = getStaticLifeline(classifierName);
            lifeline = pair.getParamOne();
            lifelineCreated = pair.getParamTwo().booleanValue();
         }
         if (lifeline == null)
         {
            ETPairT < ILifeline, Boolean > pair = getLifeline(instanceName);
            lifeline = pair.getParamOne();
            lifelineCreated = pair.getParamTwo().booleanValue();
         }

         if (lifeline != null)
         {
            if (lifelineCreated)
            {
               IREClass c = e.getREClass();
               // Determine the lifeline's representing classifier
               if (!attachRepresentingClassifier(c, lifeline))
               {
                  String name = e.getDeclaringClassName();
                  attachRepresentingClassifier(name, lifeline);
               }
            }

            // Determine the message kind
            String result = e.getResult();

            m_cpResultLifeline = null;

            // Retrieve the operation
            IOperation op = getOperation(e, lifeline);

            // Determine the line number to be associated with the message
            int lineNumber = e.getLine();

            insertMessage(getThisLifeline(), lifeline, op, BaseElement.MK_SYNCHRONOUS, lineNumber);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onReferencedVariable(org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onReferencedVariable(IReferenceEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();

      if (m_bProcessElementEvents)
      {
         // Determine the name of the lifeline
         String fullName = event.getFullName();
         String classifierName = event.getDeclaringClassifier();

         // Create a lifeline for the reference
         ILifeline lifeline = null;

         if (!(fullName == null ? classifierName == null : fullName.equals(classifierName)))
         {
            if ((lifeline = getLifelineFromMap(fullName)) == null)
               lifeline = createLifeline(fullName);
         }
         else if (classifierName != null)
         {
            // When the reference name is blank, we are processing a static reference
            lifeline = getStaticLifeline(classifierName).getParamOne();
         }

         if (lifeline != null)
         {
            m_cpResultLifeline = lifeline;

            IREClass rec = event.getREClass();

            if (!attachRepresentingClassifier(rec, lifeline))
            {
               // Determine the lifeline's representing classifier
               String type = event.getType();
               attachRepresentingClassifier(type, lifeline);
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onReturnAction(org.netbeans.modules.uml.core.reverseengineering.reframework.IReturnEvent, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onReturnAction(IReturnEvent event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink#onUnaryOperator(org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onUnaryOperator(IREUnaryOperator event, IResultCell cell)
   {
      if (event == null || cell == null)
         return;
      if (m_cpInteraction == null)
         throw new NullPointerException();
   }

   private void addLifeline(String lifelineName, ILifeline pLifeline)
   {

      if (lifelineName != null && m_mapLifelines != null)
      {
         Stack < ILifeline > stackLifelines = m_mapLifelines.get(lifelineName);
         if (stackLifelines != null)
            stackLifelines.push(pLifeline);
         else
         {
            Stack < ILifeline > StackLifelines = new Stack < ILifeline > ();
            StackLifelines.push(pLifeline);
            m_mapLifelines.put(lifelineName, StackLifelines);
         }

      }
   }

   /**
    * Retrieves a lifeline from the proper stack
    */
   private ILifeline StackGetLifeline(String bstrLifelineName)
   {
      ILifeline ppLifeline = null;
      if ((bstrLifelineName != null) && (bstrLifelineName.length() > 0))
      {
         Stack < ILifeline > stackLifelines = m_mapLifelines.get(bstrLifelineName);
         if ((stackLifelines != null) && (stackLifelines.size() > 0))
         {
            ILifeline cpLifeline = stackLifelines.peek();
            if (cpLifeline != null)
               ppLifeline = cpLifeline;
         }
      }
      return ppLifeline;
   }

   public void removeLifeline(String lifelineName)
   {
      if (m_mapLifelines != null)
      {
         if (m_mapLifelines.containsKey(lifelineName))
            m_mapLifelines.remove(lifelineName);
      }
   }

}
