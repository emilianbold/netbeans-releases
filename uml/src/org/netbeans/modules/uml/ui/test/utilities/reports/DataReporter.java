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

package org.netbeans.modules.uml.ui.test.utilities.reports;


import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IMultiFlow;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.ICallConcurrencyKind;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IChangeableKind;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISaveAsGraphicKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectorEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import java.io.FileWriter;
import java.io.IOException;
import org.dom4j.Node;

/**
 * @author brettb
 *
 */
public class DataReporter
{
   private static final String PRINT_LINE80 = "================================================================================";
   private static final String PRINT_LINE80EL = "--------------------------------------------------------------------------------";

   /* updated 9/16/03 by Deb
    * This function prints basic information about the project. If you want XMI version information about the
    * project to be included in your report, use the ProjectReportXMI function instead.
    */
   public static void projectReport(IProject iProj, final String sFolder, final String sFileName)
   {
      try
      {
         FileWriter file = new FileWriter(sFolder + "\\" + sFileName);
         if (file != null)
         {
            //REQUIRED FOR ETV
            //Dim prod;ADProduct
            //Dim cpm;New CoreProductManager
            //Dim iproj;IProject
            //Set prod = cpm.CoreProduct
            //Set iproj = prod.ProjectManager.GetCurrentProject
            //Set gProd = prod
            //ahm.Product = gProd
            //ahc.Product = gProd

            reportHeader(file);

            printReportDetails(file, iProj);

            file.close();
         }
      }
      catch( IOException e )
      {
      }
   }

   /* updated 9/16/03 by Deb
    * This function prints both basic information about the project and XMI version information. If you don//t
    * want XMI version information in your report, use the ProjectReport function instead
    */
   public static void projectReportXMI(IProject iProj, final String sFolder, final String sFileName)
   {
      try
      {
         FileWriter file = new FileWriter(sFolder + "\\" + sFileName);
         if (file != null)
         {
            reportHeader(file);

            printProjectXMIVersionDetails(file, iProj);
            printReportDetails(file, iProj);

            file.close();
         }
      }
      catch( IOException e )
      {
      }
   }

   protected static void reportHeader(FileWriter file)
   {
      reportInformation(file, "Project");
   }

   protected static void reportInformation(FileWriter file, final String strInformation)
   {
      printLine(file, PRINT_LINE80);
      printLine(file, " BASELINE REPORT");
      printLine(file, PRINT_LINE80);
      printLine(file, " VERSION 6.0");
      printLine(file, strInformation + " Information");
      printLine(file, PRINT_LINE80);
   }

   protected static void reportComplete(FileWriter file)
   {
      printLine(file, PRINT_LINE80);
      printLine(file, " REPORT COMPLETE");
      printLine(file, PRINT_LINE80);
   }

   protected static void elementReport(IElement iEl, final String sElType, final String sFolder, String sFileName)
   {
      try
      {
         FileWriter file = new FileWriter(sFolder + "\\" + sFileName);
         if (file != null)
         {
            reportInformation(file, sElType);

            printAllElementDetails( file, 0, iEl);

            if (sElType.equals("Class")
               || sElType.equals("UseCase")
               || sElType.equals("Actor")
               || sElType.equals("Interface")
               || sElType.equals("DataType")
               || sElType.equals("AliasedType")
               || sElType.equals("DerivationClassifier"))
            {
               IClassifier iCl = (IClassifier)iEl;
               printAllClassifierDetails( file, 0, iCl);
            }

            reportComplete(file);
         }
      }
      catch( IOException e )
      {
      }
   }

   /* updated 9/16/03 by Deb
    * this routine prints basic information about the project
    */
   protected static void printReportDetails(FileWriter file, IProject iProj)
   {
      if ((iProj != null) && (file != null))
      {
         printLine(file, "Project Name: " + iProj.getName());
         printLine(file, "Project Alias: " + iProj.getAlias());
         printLine(file, "Project Visibility: "); //+ ahCld.Get_Visibility(iproj)
         printLine(file, "Project Documentation: " + iProj.getDocumentation());
         printLine(file, "Stereotypes: " + iProj.getAppliedStereotypesAsString(false));
         printTaggedValues( file, 0, iProj);
         printConstraints( file, 0, iProj);
         printLine(file, "Project Filename: " + iProj.getFileName());
         printLine(file, "Project Default Language: " + iProj.getDefaultLanguage());
         printLine(file, "Project Mode: " + iProj.getMode());

         //*** All Diagrams *************************************************
         printInfo(file, iProj, "Package");

         //*** Class Diagrams *************************************************
         printInfo(file, iProj, "Class");
         printInfo(file, iProj, "Node");
         printInfo(file, iProj, "Enumeration");
         printInfo(file, iProj, "DataType");
         printInfo(file, iProj, "AliasedType");
         printInfo(file, iProj, "Interface");
         //actor and use case details are called below
         //printInfo( file,  iproj, "Actor"
         //printInfo( file,  iproj, "UseCase"
         printInfo(file, iProj, "Artifact");
         printInfo(file, iProj, "PartFacade");

         printInfo(file, iProj, "Collaboration"); //DesignPattern
         printInfo(file, iProj, "AssociationClass");

         printInfo(file, iProj, "DerivationClassifier");

         //*** Sequence Diagrams Symbols ***************************************

         printInfo(file, iProj, "Interaction");
         printInfo(file, iProj, "Actor");
         printInfo(file, iProj, "Lifeline");
         printInfo(file, iProj, "Message");
         printInfo(file, iProj, "CombinedFragment");
         printInfo(file, iProj, "DestroyAction");

         //*** Activity Diagrams Symbols  **************************************
         printInfo(file, iProj, "InvocationNode");
         printInfo(file, iProj, "MultiFlow");
         printInfo(file, iProj, "JoinForkNode");
         printInfo(file, iProj, "ComplexActivityGroup");
         printInfo(file, iProj, "InitialNode");
         printInfo(file, iProj, "ActivityFinalNode");
         printInfo(file, iProj, "FlowFinalNode");
         printInfo(file, iProj, "ParameterUsageNode");
         printInfo(file, iProj, "DataStoreNode");
         printInfo(file, iProj, "SignalNode");
         printInfo(file, iProj, "ActivityPartition");
         printInfo(file, iProj, "DecisionMergeNode");

         //*** Use Case Diagrams  Symbols  ************************************
         printInfo(file, iProj, "UseCase");
         printInfo(file, iProj, "Include");
         printInfo(file, iProj, "Extend");

         //*** State Diagrams  Symbols  ************************************
         printInfo(file, iProj, "State");
         printInfo(file, iProj, "PseudoState");
         printInfo(file, iProj, "FinalState");
         printInfo(file, iProj, "Transition");
         printInfo(file, iProj, "InitialState");

         //*** Component Diagram Symbols **************************************
         printInfo(file, iProj, "Component");

         //*** Collaboration Diagram Symbols **************************************
         printInfo(file, iProj, "MessageConnector");

         //*** All Diagrams  **************************************************
         printInfo(file, iProj, "Comment");
         printInfo(file, iProj, "Graphic");

         //*** Relationships  *************************************************
         printInfo(file, iProj, "Association");
         printInfo(file, iProj, "Aggregation");
         printInfo(file, iProj, "Generalization");
         printInfo(file, iProj, "Implementation");

         printInfo(file, iProj, "Dependency");
         printInfo(file, iProj, "Realize");
         printInfo(file, iProj, "Usage");
         printInfo(file, iProj, "Permission");
         printInfo(file, iProj, "Abstraction");
         printInfo(file, iProj, "Derivation");

         reportComplete(file);
      }
   }

   /* updated 9/16/03 by Deb
    * this routine prints XMI version information and XMI.metamodel version information for the project.
    */
   protected static void printProjectXMIVersionDetails(FileWriter file, IProject iProj)
   {
      if (iProj != null)
      {
         Node iXNode = iProj.getNode();
         if (iXNode != null)
         {
            Node iXNode2 = null;
            try   // using ancestor in the XPath query can sometimes throw
            {
               iXNode2 = iXNode.selectSingleNode("ancestor::XMI/@xmi.version");
            }
            catch( Exception e )
            {
            }
            if (iXNode2 != null)
            {
               printLine(file, "XMI Version: " + iXNode2.getText());
            }
            Node iXNode3 = null;
            try   // using ancestor in the XPath query can sometimes throw
            {
               iXNode3 = iXNode.selectSingleNode("ancestor::XMI//XMI.metamodel/@xmi.version");
            }
            catch( Exception e )
            {
            }
            if (iXNode3 != null)
            {
               printLine(file, "XMI.metamodel Version: " + iXNode3.getText());
            }
         }
      }
   }

   protected static void printInfo(FileWriter file, INamespace ins, final String sName)
   {
      IElementLocator iloc = new ElementLocator();
      IElement iEl;
      ETList < IElement > eles;

      //find elements of a given type
      eles = iloc.findElementsByQuery(ins, "//UML:" + sName);

      final int iCount = eles.getCount();
      if (iCount > 0)
      {
         printLine(file, "================================================================================");
         printLine(file, "Total " + sName + " elements found: " + iCount);
         printLine(file, "================================================================================");
      }

      for (int i = 0; i < iCount; i++)
      {
         iEl = eles.item(i);
         printLine(file, sName + " " + String.valueOf(i + 1));

         printAllElementDetails(file, 0, iEl); //must be an IElement

         if (true)
         {
            if (sName.equals("Class")
               || sName.equals("UseCase")
               || sName.equals("Actor")
               || sName.equals("Interface")
               || sName.equals("DataType")
               || sName.equals("AliasedType")
               || sName.equals("DerivationClassifier"))
            {
               printAllClassifierDetails(file, 0, (IClassifier)iEl);
            }
            else if (sName.equals("Association") || sName.equals("Aggregation"))
            {
               printAssociationDetails(file, 0, (IAssociation)iEl);
            }
            else if (sName.equals("Generalization"))
            {
               printGenLinkDetails(file, 0, (IGeneralization)iEl);
            }
            else if (sName.equals("Implementation"))
            {
               printImplementationLinkDetails(file, 0, (IImplementation)iEl);
            }
            else if (sName.equals("Dependency") || sName.equals("Realize") || sName.equals("Usage") || sName.equals("Permission") || sName.equals("Abstraction"))
            {
               printDependencyDetails(file, 0, (IDependency)iEl);
            }

            else if (sName.equals("Comment"))
            {
               printCommentDetails(file, 0, (IComment)iEl);
            }

            else if (sName.equals("CombinedFragment"))
            {
               printCfragDetails(file, 0, (ICombinedFragment)iEl);
            }
            else if (sName.equals("Lifeline"))
            {
               printLifeLineDetails(file, 0, (ILifeline)iEl);
            }
            else if (sName.equals("UseCase"))
            {
               printUseCaseDetails(file, 0, (IUseCase)iEl);
            }
            else if (sName.equals("State"))
            {
               printStateDetails(file, 0, (IState)iEl);
            }
            else if (sName.equals("Message"))
            {
               printMessageDetails(file, 0, (IMessage)iEl);
            }
            else if (sName.equals("MessageConnector"))
            {
               printConnectorDetails(file, 0, (IMessageConnector)iEl);
            }
            else if (sName.equals("Artifact"))
            {
               printArtifactDetails(file, 0, (IArtifact)iEl);
            }
            else if (sName.equals("AssociationClass"))
            {
               printAssociationClassDetails(file, 0, (IAssociationClass)iEl);
            }
            else if (sName.equals("MultiFlow"))
            {
               printMultiFlowDetails(file, 0, (IMultiFlow)iEl);
            }
            else if (sName.equals("ActivityPartition"))
            {
               printPartitionDetails(file, 0, (IActivityPartition)iEl);
            }
            else if (sName.equals("Component"))
            {
               printComponentDetails(file, 0, (IComponent)iEl);
            }
            else if (sName.equals("Include"))
            {
               printIncludeDetails(file, 0, (IInclude)iEl);
            }
            else if (sName.equals("Extend"))
            {
               printExtendDetails(file, 0, (IExtend)iEl);
            }
            else if (sName.equals("Derivation"))
            {
               printDerivationDetails(file, 0, (IDerivation)iEl);
            }
         }

         printLine(file, "--------------------------------------------------------------------------------");
      }
   }

   protected static void printAllElementDetails( FileWriter file, int nindent, IElement iEl)
   {
      String sTemp = "";
      ETList < IElement > iels;
      IElement iEl2;
      ETList < IReference > irefs;
      IReference iref;
      ETList < IReference > irefs2;
      IReference iref2;
      IProxyDiagramManager proxMgr = ProxyDiagramManager.instance();
      ETList<IProxyDiagram > proxDiags;
      IProxyDiagram proxDiag;

      //All Named Elements------------------------------------------
      //On Error GoTo el  //if error, not a named element, so code below skipped

      INamedElement iNe = (iEl instanceof INamedElement) ? (INamedElement)iEl : null;
      if (iNe != null)
      {
         printLine(file, nindent, "Name: " + iNe.getName());
         //MsgBox ine.Name
         printLine(file, nindent, "Qual Name: " + iNe.getQualifiedName());
         printLine(file, nindent, "Alias: " + iNe.getAlias());

         printVisibility(file, nindent, iNe);

         //associated artifacts
         iels = iEl.getAssociatedArtifacts();
         if ((iels != null) && (iels.getCount() > 0))
         {
            final int iCount = iels.getCount();
            printLine(file, nindent, "Associated Artifact Count:  " + String.valueOf(iCount));
            for (int f = 0; f < iCount; f++)
            {
               IArtifact iart = (IArtifact)iels.item(f);
               if (iart != null)
               {
                  printLine(file, nindent + 2, "Associated Artifact  " + String.valueOf(f + 1));
                  printLine(file, nindent + 4, "File name: " + iart.getFileName());
               }
            }
         }

         //referring references (associated elements)
         irefs = iEl.getReferencingReferences();
         if ((irefs != null) && (irefs.getCount() > 0))
         {
            final int iCount = irefs.getCount();
            printLine(file, nindent, "Referencing Element Count:  " + String.valueOf(iCount));
            for (int k = 0; k < iCount; k++)
            {
               iref = irefs.item(k);
               if (iref != null)
               {
                  ETList < IElement > els2 = iref.getTargets();
                  if (els2 != null)
                  {
                     final int iEls2Count = els2.getCount();
                     for (int n = 0; n < iEls2Count; n++)
                     {
                        INamedElement ine4 = (INamedElement)els2.item(n);
                        if (ine4 != null)
                        {
                           printLine(file, nindent + 2, "Element " + String.valueOf(k + 1));
                           printLine(file, nindent + 4, "Name: " + ine4.getName());
                           printLine(file, nindent + 4, "Type: " + ine4.getElementType());
                        }
                     }
                  }
               }
            }
         }

         //referred references (associated elements)
         irefs2 = iEl.getReferredReferences();
         if ((irefs2 != null) && (irefs2.getCount() > 0))
         {
            final int iCount = irefs2.getCount();
            printLine(file, nindent, "Referred Element Count:  " + iCount);
            for (int j = 0; j < iCount; j++)
            {
               iref2 = irefs2.item(j);
               if (iref2 != null)
               {
                  ETList< IElement > els = iref2.getSources();
                  if (els != null)
                  {
                     final int iElsCount = els.getCount();
                     for (int l = 0; l < iElsCount; l++)
                     {
                        INamedElement ine3 = (INamedElement)els.item(l);
                        if (ine3 != null)
                        {
                           printLine(file, nindent + 2, "Element " + (j + 1) );
                           printLine(file, nindent + 4, "Name: " + ine3.getName());
                           printLine(file, nindent + 4, "Type: " + ine3.getElementType());
                        }
                     }
                  }
               }
            }
         }

         //associated diagrams
         proxDiags = proxMgr.getAssociatedDiagramsForElement(iEl);
         if ((proxDiags != null) && (proxDiags.getCount() > 0))
         {
            final int iCount = proxDiags.getCount();
            printLine(file, nindent, "Associated Diagram Count:  " + String.valueOf(iCount));
            for (int m = 0; m < iCount; m++)
            {
               proxDiag = proxDiags.item(m);
               if (proxDiag != null)
               {
                  printLine(file, nindent + 2, "Associated Diagram " + String.valueOf(m + 1));
                  printLine(file, nindent + 4, "Name: " + proxDiag.getName());
                  printLine(file, nindent + 4, "Type: " + proxDiag.getDiagramKindName());
               }
            }
         }

         //Dependencies
         ETList < IDependency > iClientdps = iNe.getClientDependencies();
         if ( (iClientdps != null) &&
              (iClientdps.getCount() > 0) )
         {
            printLine(file, nindent, "Client Dependencies: " + String.valueOf(iClientdps.getCount()));
         }
         ETList < IDependency > iSupplierdps = iNe.getSupplierDependencies();
         if ( (iSupplierdps != null) &&
              (iSupplierdps.getCount() > 0) )
         {
            printLine(file, nindent, "Supplier Dependencies: " + String.valueOf(iSupplierdps.getCount()));
         }

         //   If Not ine.ClientDependencies Is Nothing Then
         //      printLine( file,   nIndent, "CLIENT DEPENDENCIES"
         //      For i = 0; ine.ClientDependencies.Count - 1
         //            Set idp = iel.ClientDependencies.item(i)
         //            printLine( file,   nIndent + 1, "Dependency Supplier" + Str(i) + ": " + idp.Supplier.Name
         //            PrintDependencyDetails hFile, nIndent + 2, idp
         //      Next
         //   End If

         //   If Not ine.SupplierDependencies Is Nothing Then
         //      printLine( file,   nIndent, "SUPPLIER DEPENDENCIES"
         //      For i = 0; ine.SupplierDependencies.Count - 1
         //            Set idp = iel.SupplierDependencies.item(i)
         //            printLine( file,   nIndent + 1, "Dependency Client" + Str(i) + ": " + idp.Client.Name
         //            PrintDependencyDetails hFile, nIndent + 2, idp
         //      Next
         //   End If
      }

      //Any Element------------------------------------------

      final String strDocumentation = iEl.getDocumentation();
      if (strDocumentation.length() > 0)
      {
         printLine(file, nindent, "Documention: " + iEl.getDocumentation());
      }

      final String strStereotype = iEl.getAppliedStereotypesAsString(false);
      if (strStereotype.length() > 0)
      {
         printLine(file, nindent, "Stereotype: " + iEl.getAppliedStereotypesAsString(false));
      }

      printTaggedValues(file, nindent, iEl);
      printConstraints(file, nindent, iEl);
   }

   protected static void printTemplateParameterDetails( FileWriter file, int nindent, IClassifier iCl)
   {
      if (iCl != null)
      {
         ETList < IParameterableElement > paramElems = iCl.getTemplateParameters();
         if ((paramElems != null) && (paramElems.getCount() > 0))
         {
            final int iCount = paramElems.getCount();
            printLine(file, nindent, "Parameter Count: " + String.valueOf(iCount));
            for (int x = 0; x < iCount; x++)
            {
               IParameterableElement paramElem = (IParameterableElement)paramElems.item(x);
               if (paramElem != null)
               {
                  printLine(file, nindent, "Parameter " + String.valueOf(x + 1));
                  printAllElementDetails(file, nindent + 2, paramElem);
   
                  IParameterableElement defEl = paramElem.getDefaultElement();
                  if( defEl != null )
                  {
                     printLine(file, nindent + 2, "Default Element: " + defEl.getName());
                  }
   
                  final String strTypeConstraint = paramElem.getTypeConstraint();
                  if (strTypeConstraint.length() > 0)
                  {
                     printLine(file, nindent + 2, "Type Constraint: " + strTypeConstraint);
                  }
               }
            }
         }
      }
   }

   protected static void printAllClassifierDetails( FileWriter file, int nindent, IClassifier iEl)
   {
      printLine(file, nindent, "Leaf: " + reportBoolean(iEl.getIsLeaf()));
      printLine(file, nindent, "Transient: " + reportBoolean(iEl.getIsTransient()));
      printLine(file, nindent, "Abstract: " + reportBoolean(iEl.getIsAbstract()));

      IRedefinableElement iref;
      iref = iEl;
      printLine(file, nindent, "Final: " + reportBoolean(iref.getIsFinal()));

      //source file
      ISourceFileArtifact sfa;
      Object obj;

      ETList< IElement > artifacts = iEl.getSourceFiles();
      if (artifacts != null)
      {
         final int iCount = artifacts.getCount();
         for (int i = 0; i < iCount; i++)
         {
            sfa = (ISourceFileArtifact)artifacts.item(i);
            if (sfa != null)
            {
               printLine(file, nindent, "Source Filename: " + sfa.getShortName());
               printLine(file, nindent, "Source Path: " + sfa.getSourceFile());
            }
         }
      }

      ETList<IDependency> clientDependencies = iEl.getClientDependencies();
      if (clientDependencies != null)
      {
         final int iCount = clientDependencies.getCount();
         if (iCount > 0)
         {
            IDependency iDep;
            for (int i = 0; i < iCount; i++)
            {
               iDep = clientDependencies.item(i);
               if (iDep != null)
               {
                  INamedElement iNe;
                  iNe = iDep.getSupplier();
                  if (iNe != null)
                  {
                     printLine(file, nindent, "Dependency " + String.valueOf(i + 1) + ": " + iNe.getFullyQualifiedName(false));
                  }
               }
            }
         }
      }

      ETList<IAssociation> associations = iEl.getAssociations();
      if ( associations != null)
      {
         final int iCount = associations.getCount();
         if (iCount > 0)
         {
            IAssociation iassoc;
            for (int i = 0; i < iCount; i++)
            {
               iassoc = associations.item(i);
               //if( iassoc.Ends.item(0).getType().getFullyQualifiedName(false) = iel.getFullyQualifiedName(false) )
               {
                  if (iassoc != null)
                  {
                     ETList < IAssociationEnd > iAssocEnds;
                     iAssocEnds = iassoc.getEnds();
                     if (iAssocEnds != null)
                     {
                        IAssociationEnd iAssocEnd;
                        iAssocEnd = iAssocEnds.item(0);
                        if (iAssocEnd != null)
                        {
                           IClassifier iCl;
                           iCl = iAssocEnd.getType();
                           if (iCl != null)
                           {
                              if (iCl.getFullyQualifiedName(false).equals(iEl.getFullyQualifiedName(false)))
                              {
                                 IAssociationEnd iassocEnd2;
                                 iassocEnd2 = iAssocEnds.item(1);
                                 if (iassocEnd2 != null)
                                 {
                                    IClassifier icl2;
                                    icl2 = iassocEnd2.getType();
                                    if (icl2 != null)
                                    {
                                       //printLine( file,  nindent, "Association" + String.valueOf(i + 1) + " references: " + iassoc.Ends.item(1).getType().getFullyQualifiedName(false) );
                                       printLine(file, nindent, "Association " + String.valueOf(i + 1) + " references: " + icl2.getFullyQualifiedName(false));
                                    }
                                 }
                              }
                              else
                              {
                                 //printLine( file,  nindent, "Association" + String.valueOf(i + 1) + " referenced by: " + iassoc.Ends.item(0).getType().getFullyQualifiedName(false) );
                                 printLine(file, nindent, "Association " + String.valueOf(i + 1) + " referenced by: " + iCl.getFullyQualifiedName(false));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         ETList<IImplementation> implementations = iEl.getImplementations();
         if (implementations != null)
         {
            final int iImpsCount = implementations.getCount();
            if (iImpsCount > 0)
            {
               IImplementation iimp;
               for (int i = 0; i < iImpsCount; i++)
               {
                  iimp = implementations.item(i);
                  if (iimp != null)
                  {
                     INamedElement ine3;
                     ine3 = iimp.getSupplier();
                     if (ine3 != null)
                     {
                        printLine(file, nindent, "Implements " + String.valueOf(i + 1) + ": " + ine3.getFullyQualifiedName(false));
                     }
                  }
               }
            }
         }

         ETList<IGeneralization> generalizations = iEl.getGeneralizations();
         if (generalizations != null)
         {
            final int iGensCount = generalizations.getCount();
            if (iGensCount > 0)
            {
               IGeneralization iGen;
               for (int i = 0; i < iGensCount; i++)
               {
                  iGen = generalizations.item(i);
                  if (iGen != null)
                  {
                     IClassifier icl3;
                     icl3 = iGen.getGeneral();
                     if (icl3 != null)
                     {
                        printLine(file, nindent, "Extends " + String.valueOf(i + 1) + ": " + icl3.getFullyQualifiedName(false));
                     }
                  }
               }
            }
         }

         printAttributeDetail(file, nindent, iEl);
         printOperationDetail( file, nindent, iEl);
         printTemplateParameterDetails( file, nindent, iEl);
      }
   }

   protected static void printAttributeDetail(FileWriter file, int nindent, IClassifier iclR)
   {
      IAttribute iattrR;
      int i;
      int j;
      int R;
      String sTemp = "";

      ETList<IAttribute> attributes = iclR.getAttributes();
      final int iCount = attributes.getCount();
      if (iCount > 0)
      {
         printLine(file, nindent, "ATTRIBUTES");
         for (i = 0; i < iCount; i++)
         {
            iattrR = attributes.item(i);

            printLine(file, nindent + 1, "Attribute " + String.valueOf(i + 1));
            printAllElementDetails(file, nindent + 2, iattrR);
            printLine(file, nindent + 2, "TypeName: " + iattrR.getTypeName());
            printLine(file, nindent + 2, "Default: " + iattrR.getDefault2());

            IMultiplicity multiplicity = iattrR.getMultiplicity();
            ETList<IMultiplicityRange> ranges = multiplicity.getRanges();
            final int iRangeCount = ranges.getCount();
            if (iRangeCount > 0)
            {
               for (j = 0; j < iRangeCount; j++)
               {
                  IMultiplicityRange range = ranges.item(j);
                  printLine(
                     file,
                     nindent + 2,
                     "Attribute Multiplicity" + String.valueOf(j) + ": " + range.getLower() + ".." + range.getUpper());
               }
            }

            printLine(file, nindent + 2, "Final: " + reportBoolean(iattrR.getIsFinal()));
            printLine(file, nindent + 2, "Static: " + reportBoolean(iattrR.getIsStatic()));
            printLine(file, nindent + 2, "Transient: " + reportBoolean(iattrR.getIsTransient()));
            printLine(file, nindent + 2, "Volatile: " + reportBoolean(iattrR.getIsVolatile()));

            printClientChangeability( file, nindent + 2, iattrR );

            printLine(file, nindent + 2, "Derived: " + reportBoolean(iattrR.getIsDerived()));

            //printLine( file,  nIndent + 2, "Derived: " + reportBoolean(iattrR.getIsWithEvents) );
            //printLine( file,  nIndent + 2, "Derived: " + String.valueOf(iattrR.HeapBased) );

         }
      }

   }

   protected static void printOperationDetail(FileWriter file, int nindent, IClassifier iclR)
   {
      IOperation iopR;
      IClassifier iclExc;
      int i;
      int m;
      IParameter iParam;
      String sTemp = "";

      ETList< IOperation > operations = iclR.getOperations();
      final int iCount = operations.getCount();
      if (iCount > 0)
      {
         printLine(file, nindent, "OPERATIONS");
         for (i = 0; i<iCount; i++)
         {
            iopR = operations.item(i);
            printLine(file, nindent + 1, "Operation " + String.valueOf(i + 1));
            printAllElementDetails(file, nindent + 2, iopR);
            //Return Type
            printLine(file, nindent + 2, "Return Type: " + iopR.getReturnType2());
            IParameter returnType = iopR.getReturnType();
            if (returnType != null)
            {
               ETList< IMultiplicityRange > ranges = returnType.getMultiplicity().getRanges();
               if (ranges.getCount() > 0)
               {
                  IMultiplicityRange range = ranges.item(0);
                  printLine(
                     file,
                     nindent + 2,
                     "Return Type Multiplicity: "
                        + range.getLower()
                        + ".."
                        + range.getUpper());
               }
            }
            //Parameters
            ETList< IParameter > parameters = iopR.getParameters();
            final int iParamCnt = parameters.getCount();
            printLine(file, nindent + 2, "Param Count:  " + iParamCnt );
            if (iParamCnt > 0)
            {
               int k;
               for (k = 0; k < iParamCnt; k++)
               {
                  iParam = parameters.item(k);
                  printLine(file, nindent + 2, "Param " + String.valueOf(k) + " name: " + iParam.getName());
                  printLine(file, nindent + 2, "Param " + String.valueOf(k) + " type: " + iParam.getTypeName());
                  if (iParam.getDefault2().length() > 0)
                  {
                     printLine(file, nindent + 2, "Param " + String.valueOf(k) + " default value: " + iParam.getDefault2());
                  }

                  ETList< IMultiplicityRange > ranges = iParam.getMultiplicity().getRanges();
                  final int iRangeCnt = ranges.getCount();
                  if (iRangeCnt > 0)
                  {
                     for (m = 0; m < iRangeCnt; m++)
                     {
                        IMultiplicityRange range = ranges.item(m);
                        printLine(
                           file,
                           nindent,
                           "Parameter"
                              + String.valueOf(k)
                              + " Multiplicity"
                              + String.valueOf(m)
                              + ": "
                              + range.getLower()
                              + ".."
                              + range.getUpper());
                     }
                     //                      printLine( file,  nIndent + 2, "Parameter Multiplicity: " + iParam.getMultiplicity().getRanges().item(0).Lower + ".." + iParam.getMultiplicity().getRanges().item(0).Upper );
                  }
               }
            }

            //Raised Exceptions
            ETList<IClassifier> exceptions = iopR.getRaisedExceptions();
            if (exceptions != null)
            {
               final int iExceptionCnt = exceptions.getCount();
               if (iExceptionCnt > 0)
               {
                  printLine(file, nindent + 2, "RaisedExceptions Count: " + iExceptionCnt );
                  int R;
                  for (R = 0; R < iExceptionCnt; R++)
                  {
                     iclExc = exceptions.item(R);
                     printLine(file, nindent + 4, "Exception " + (R + 1));
                     printLine(file, nindent + 7, "Classifier: " + iclExc.getName());
                     printLine(file, nindent + 7, "Type: " + iclExc.getElementType());
                  }
               }
            }

            //Other
            printLine(file, nindent + 2, "Abstract: " + reportBoolean(iopR.getIsAbstract()));
            printLine(file, nindent + 2, "Final: " + reportBoolean(iopR.getIsFinal()));
            printLine(file, nindent + 2, "Static: " + reportBoolean(iopR.getIsStatic()));
            printLine(file, nindent + 2, "Native: " + reportBoolean(iopR.getIsNative()));
            printLine(file, nindent + 2, "StrictFTP: " + reportBoolean(iopR.getIsNative()));

            if (iopR.getIsSubroutine() == true)
            {
               printLine(file, nindent + 2, "Subroutine: " + reportBoolean(iopR.getIsSubroutine()));
            }

            if (iopR.getIsProperty() == true)
            {
               printLine(file, nindent + 2, "Property: " + reportBoolean(iopR.getIsProperty()));
            }

            if (iopR.getIsFriend() == true)
            {
               printLine(file, nindent + 2, "Friend: " + reportBoolean(iopR.getIsFriend()));
            }

            switch (iopR.getConcurrency())
            {
               case ICallConcurrencyKind.CCK_CONCURRENT :
                  sTemp = "Concurrent";
                  break;

               case ICallConcurrencyKind.CCK_GUARDED :
                  sTemp = "Guarded";
                  break;

               case ICallConcurrencyKind.CCK_SEQUENTIAL :
                  sTemp = "Sequential";
                  break;
            }
            printLine(file, nindent + 2, "Concurrency: " + sTemp);

            printLine(file, nindent + 2, "Query: " + reportBoolean(iopR.getIsQuery()));
         }
      }
   }

   /* ----------------- WE ARE HERE -------------------------- */

   protected static void printArtifactDetails( FileWriter file, int nindent, IArtifact iar)
   {
      printLine(file, nindent, "File name: " + iar.getFileName());
   }

   protected static void printDerivationDetails( FileWriter file, int nindent, IDerivation deriv)
   {
      if (deriv != null)
      {
         IElement iEl;
         iEl = deriv;
         if (iEl != null)
         {
            ETList < IUMLBinding > umlBinds;
            int x;
            umlBinds = deriv.getBindings();
            if (umlBinds != null)
            {
               final int iCount = umlBinds.getCount();
               if (iCount > 0)
               {
                  printLine(file, nindent, "Binding Count: " + iCount );
                  IUMLBinding umlBind;
                  for (x = 0; x<iCount; x++)
                  {
                     umlBind = umlBinds.item(x);
                     if (umlBind != null)
                     {
                        printLine(file, nindent + 2, "Binding " + String.valueOf(x + 1));
                        if (umlBind instanceof IElement )
                        {
                           printAllElementDetails(file, nindent + 4, umlBind);
                        }
                        IParameterableElement paramElem, paramElem2;
                        paramElem = umlBind.getFormal();
                        if (paramElem != null)
                        {
                           IElement iEl3;
                           iEl3 = paramElem;
                           if (iEl3 != null)
                           {
                              // TODO printLine(file, nindent + 4, "Formal: " + ahM.getNameFromElement(iEl3));
                           }
                        }
                        paramElem = umlBind.getActual();
                        if (paramElem != null)
                        {
                           IElement iEl4;
                           iEl4 = paramElem;
                           if (iEl4 != null)
                           {
                              // TODO printLine(file, nindent + 4, "Actual: " + ahM.getNameFromElement(iEl4));
                           }
                        }
                     }
                  }
               }
            }
            IClassifier iCl;
            iCl = deriv.getTemplate();
            if (iCl != null)
            {
               // TODO printLine(file, nindent, "Template: " + ahM.getNameFromClassifier(iCl));
            }
            IClassifier icl2;
            icl2 = deriv.getDerivedClassifier();
            if (icl2 != null)
            {
               // TODO printLine(file, nindent, "Derivation Classifier: " + ahM.getNameFromClassifier(icl2));
            }
         }
      }
   }

   //   *******************************************************************************************
   //    Activity Diagrams
   //   *******************************************************************************************
   protected static void printPartitionDetails( FileWriter file, int nindent, IActivityPartition partition)
   {
      if (partition != null)
      {
         ETList < IActivityNode > contents;
         contents = partition.getNodeContents();
         if (contents != null)
         {
            final int iCount = contents.getCount();
            if (iCount > 0)
            {
               printLine(file, nindent, "Contained Element Count:  " + iCount );
            }
            for ( int x = 0; x<iCount; x++)
            {
               INamedElement node;
               node = contents.item(x);
               printLine(file, nindent + 2, "Contained Element  " + String.valueOf(x + 1));
               if (node != null)
               {
                  printLine(file, nindent + 4, "Name: " + node.getName());
                  printLine(file, nindent + 4, "Type: " + node.getElementType());
               }
            }
         }
      }
   }

   //   *******************************************************************************************
   //    Component Diagrams
   //   *******************************************************************************************
   protected static void printComponentDetails( FileWriter file, int nindent, IComponent iComp)
   {
      if (iComp != null)
      {
         ETList < IPort > iports;
         iports = iComp.getExternalInterfaces();
         if (iports != null)
         {
            final int iCount = iports.getCount();
            if (iCount > 0)
            {
               printLine(file, nindent, "Port Count:  " + iCount );
            }
            for (int x = 0; x<iCount; x++)
            {
               IPort iport;
               iport = iports.item(x);
               if (iport != null)
               {
                  printLine(file, nindent + 2, "Port  " + String.valueOf(x + 1));
                  printLine(file, nindent + 4, "Name: " + iport.getName());
                  ETList< IInterface > ifaces;
                  ifaces = iport.getProvidedInterfaces();
                  if (ifaces != null)
                  {
                     final int iFacesCnt = ifaces.getCount();
                     int y;
                     printLine(file, nindent + 4, "Provided Interface Count:  " + iFacesCnt );
                     for (y = 0; y<iFacesCnt; y++)
                     {
                        IInterface iface = ifaces.item(y);
                        if (iface != null)
                        {
                           printLine(file, nindent + 6, "Interface  " + String.valueOf(y + 1));
                           printLine(file, nindent + 6, "Name: " + iface.getName());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   //   *******************************************************************************************
   //    Class Diagrams
   //   *******************************************************************************************
   protected static void printAssociationClassDetails( FileWriter file, int nindent, IAssociationClass iAssocClass)
   {
      if (iAssocClass != null)
      {
         printAssociationDetails( file, nindent, (IAssociation)iAssocClass);
      }
   }
   //   *******************************************************************************************
   //    Use case Diagrams:
   //   *******************************************************************************************

   protected static void printUseCaseDetails( FileWriter file, int nindent, IUseCase iuc)
   {
      IUseCaseDetail iucd;
      IUseCaseDetail iucd2;
      int i, j;
      String sTemp = "", sTemp2 = "";
      // printLine( file,  nIndent, "Constraints" );
      ETList <IUseCaseDetail> details = iuc.getDetails();
      final int iCount = details.getCount();
      if (iCount > 0)
      {
         printLine(file, nindent, "Use case details count: " + iCount );
         for (i = 0; i<iCount; i++)
         {
            iucd = details.item(i);
            printLine(file, nindent + 2, "Use case Detail Value" + String.valueOf(i + 1));
            printLine(file, nindent + 4, "name: " + iucd.getName());
            printLine(file, nindent + 4, "alias: " + iucd.getAlias());
            printVisibility( file, nindent + 4, iucd );
            printLine(file, nindent + 4, "Stereotype: " + iucd.getAppliedStereotypesAsString(false));
            printTaggedValues(file, nindent + 4, iucd);
            printLine(file, nindent + 4, "Documentation: " + iucd.getDocumentation());
            printConstraints(file, nindent + 4, iucd);
            printLine(file, nindent + 4, "Body: " + iucd.getBody());
            ETList <IUseCaseDetail> subDetails = iucd.getSubDetails();
            final int iSubCnt = subDetails.getCount();
            if (iSubCnt > 0)
            {
               printLine(file, nindent + 4, "Use case sub details count: " + iSubCnt );
               for (j = 0; j<iSubCnt; j++)
               {
                  iucd2 = subDetails.item(j);
                  printLine(file, nindent + 6, "Use case void Detail Value" + String.valueOf(j + 1));
                  {
                     printLine(file, nindent + 8, "name: " + iucd2.getName());
                     printLine(file, nindent + 8, "alias: " + iucd2.getAlias());
                     printVisibility(file, nindent + 8, iucd2 );
                     printLine(file, nindent + 8, "Stereotype: " + iucd2.getAppliedStereotypesAsString(false));
                     printTaggedValues( file, nindent + 8, iucd2);
                     printLine(file, nindent + 8, "Documentation: " + iucd2.getDocumentation());
                     printConstraints( file, nindent + 8, iucd2);
                     printLine(file, nindent + 8, "Body: " + iucd2.getBody());
                  }
               }
            }
         }
      }
   }

   protected static void printIncludeDetails( FileWriter file, int nindent, IInclude inc)
   {
      if (inc != null)
      {
         IUseCase uc1, uc2;
         uc1 = inc.getAddition();
         if (uc1 != null)
         {
            printLine(file, nindent, "Addition Name: " + uc1.getName());
         }
         uc2 = inc.getBase();
         if (uc2 != null)
         {
            printLine(file, nindent, "Base Name: " + uc2.getName());
         }
      }

   }

   protected static void printExtendDetails( FileWriter file, int nindent, IExtend ext)
   {
      if (ext != null)
      {
         IUseCase uc1, uc2;
         uc1 = ext.getBase();
         if (uc1 != null)
         {
            printLine(file, nindent, "Base Name: " + uc1.getName());
         }
         uc2 = ext.getExtension();
         if (uc2 != null)
         {
            printLine(file, nindent, "Extension Name: " + uc2.getName());
         }
      }

   }

   //   *******************************************************************************************
   //    State Diagrams
   //   *******************************************************************************************

   protected static void printStateDetails( FileWriter file, int nindent, IState iSt)
   {
      if (iSt != null)
      {
         printLine(file, nindent, "Is Composite: " + reportBoolean(iSt.getIsComposite()));
         printLine(file, nindent, "Is Orthogonal: " + reportBoolean(iSt.getIsOrthogonal()));
      }
   }

   //   *******************************************************************************************
   //    Sequence Diagrams
   //   *******************************************************************************************

   protected static void printLifeLineDetails( FileWriter file, int nindent, ILifeline iLife)
   {
      printLine(file, nindent, "Type: " + iLife.getElementType());
      if (iLife.getRepresentingClassifier() != null)
      {
         printLine(file, nindent, "RepresentingClassifier: " + iLife.getRepresentingClassifier().getName());
         if ( (iLife.getRepresentingClassifier().getElementType()).equalsIgnoreCase("CLASS") )
         {
            IClass iCl;
            iCl = (IClass)iLife.getRepresentingClassifier();
            if (iCl != null)
            {
               printLine(file, nindent + 2, "Active: " + reportBoolean(iCl.getIsActive()));
               ETList < IPart > prts;
               prts = iCl.getParts();
               if (prts != null)
               {
                  final int iCount = prts.getCount();
                  for (int j = 0; j<iCount; j++)
                  {
                     IPart prt = prts.item(j);
                     if (prt != null)
                     {
                        printLine(file, nindent + 2, "Part:  " + String.valueOf(j + 1));
                        printLine(file, nindent + 4, "Transient: " + reportBoolean(prt.getIsTransient()));
                        printLine(file, nindent + 4, "Static: " + reportBoolean(prt.getIsStatic()));
                     }
                  }
               }
            }
         }
      }
   }

   protected static void printMessageDetails( FileWriter file, int nindent, IMessage iMess)
   {
      int i, j, k;
      int msgKind;
      String sTemp = "";
      IOperation iop;
      IClassifier iCl;
      if (iMess != null)
      {
         msgKind = iMess.getKind();
         switch (msgKind)
         {
            case IMessageKind.MK_SYNCHRONOUS :
               sTemp = "synchronous";
               break;

            case IMessageKind.MK_CREATE :
               sTemp = "create";
               break;

            case IMessageKind.MK_RESULT :
               sTemp = "result";
               break;

            case IMessageKind.MK_ASYNCHRONOUS :
               sTemp = "asynchronous";
               break;

            case IMessageKind.MK_UNKNOWN :
               sTemp = "unknown";
               break;
         }
         printLine(file, nindent, "Message Type: " + sTemp);
         printLine(file, nindent, "Message Number: " + iMess.getAutoNumber());
         if (iMess.getOperationInvoked() != null)
         {
            iop = iMess.getOperationInvoked();
            printLine(file, nindent, "Operation Invoked name: " + iop.getName());

            String strReturnType = iop.getReturnType2();            
            if (strReturnType.length() > 0)
            {
               printLine(file, nindent, "Operation Invoked type: " + strReturnType );
            }
            IParameter returnType = iop.getReturnType();
            if (returnType != null)
            {
               ETList<IMultiplicityRange> ranges = returnType.getMultiplicity().getRanges();
               final int iRangeCnt = ranges.getCount();
               if (iRangeCnt > 0)
               {
                  for (j = 0; j<iRangeCnt; j++)
                  {
                     IMultiplicityRange range = ranges.item(j);
                     printLine(
                        file,
                        nindent,
                        "Operation Invoked Multiplicity"
                           + String.valueOf(j)
                           + ": "
                           + range.getLower()
                           + ".."
                           + range.getUpper());
                  }
               }
            }
            
            ETList<IParameter> parameters = iMess.getOperationInvoked().getParameters();
            if (parameters != null)
            {
               final int iParamCnt = parameters.getCount();
               printLine( file, nindent, "Operation Invoked Parameter Count:  " + iParamCnt );
               for (i = 0; i<iParamCnt; i++)
               {
                  printLine(file, nindent + 2, "Parameter  " + String.valueOf(i + 1));
                  IParameter parameter = parameters.item(i);
                  printLine(file, nindent + 4, " name: " + parameter.getName());
                  iCl = parameter.getType();
                  if (iCl != null)
                  {
                     printLine(file, nindent + 4, " type: " + iCl.getName());
                  }
                  
                  ETList<IMultiplicityRange> ranges = parameter.getMultiplicity().getRanges();
                  final int iRangeCnt = ranges.getCount(); 
                  if (iRangeCnt > 0)
                  {
                     for (k = 0; k<iRangeCnt; k++)
                     {
                        IMultiplicityRange range = ranges.item(k);
                        printLine(
                           file,
                           nindent + 4,
                           "multiplicity"
                              + String.valueOf(k)
                              + ": "
                              + range.getLower()
                              + ".."
                              + range.getUpper() );
                     }
                  }
               }
            }
         }

         if (iMess.getReceivingLifeline() != null)
         {
            printLine(file, nindent, "Receiving Lifeline: " + iMess.getReceivingLifeline().getName());
         }
         if (iMess.getSendingLifeline() != null)
         {
            printLine(file, nindent, "Sending Lifeline: " + iMess.getSendingLifeline().getName());
         }
         if (iMess.getReceivingClassifier() != null)
         {
            printLine(file, nindent, "Receiving Classifier: " + iMess.getReceivingClassifier().getName());
         }
         if (iMess.getSendingClassifier() != null)
         {
            printLine(file, nindent, "Sending Classifier: " + iMess.getSendingClassifier().getName());
         }
      }
   }

   protected static void printCfragDetails( FileWriter file, int nindent, ICombinedFragment comfrag)
   {
      String sTemp = "";

      switch (comfrag.getOperator())
      {
         case IInteractionOperator.IO_ALT :
            sTemp = "alt";
            break;

         case IInteractionOperator.IO_ASSERT :
            sTemp = "assert";
            break;

         case IInteractionOperator.IO_ELSE :
            sTemp = "else";
            break;

         case IInteractionOperator.IO_LOOP :
            sTemp = "loop";
            break;

         case IInteractionOperator.IO_NEG :
            sTemp = "neg";
            break;

         case IInteractionOperator.IO_OPT :
            sTemp = "opt";
            break;

         case IInteractionOperator.IO_PAR :
            sTemp = "par";
            break;

         case IInteractionOperator.IO_REGION :
            sTemp = "region";
            break;

         case IInteractionOperator.IO_SEQ :
            sTemp = "seq";
            break;

         case IInteractionOperator.IO_STRICT :
            sTemp = "strict";
            break;
      }
      printLine(file, nindent, "CombFrag Operator: " + sTemp);

      ETList<IInteractionOperand> operands = comfrag.getOperands();
      final int iCount = operands.getCount();
      if (iCount > 0)
      {
         int i;
         for (i = 0; i<iCount; i++)
         {
            printAllElementDetails(file, nindent + 2, operands.item(i));
         }
      }

   }

   //   *******************************************************************************************
   //    Collaboration Diagrams
   //   *******************************************************************************************
   protected static void printConnectorDetails( FileWriter file, int nindent, IConnector iCon)
   {
      ETList< IConnectorEnd > iConEnds;
      IConnectorEnd iConEnd;
      IAssociationEnd iDefEnd;

      int i;
      ETList< IElement > eles;
      IElement iEl;

      if (iCon != null)
      {
         iConEnds = iCon.getEnds();
         if (iConEnds != null)
         {
            final int iCount = iConEnds.getCount();
            for (i = 0; i<iCount; i++)
            {
               iConEnd = iConEnds.item(i);
               if (iConEnd != null)
               {
                  IPart prt;
                  prt = (IPart)iConEnd.getPart();
                  if (prt != null)
                  {
                     IClassifier iCl;
                     iCl = prt.getFeaturingClassifier();
                     if (iCl != null)
                     {
                        INamedElement iNe;
                        iNe = iCl;
                        if (iNe != null)
                        {
                           printLine(file, nindent, "End " + String.valueOf(i + 1) + " Classifier Name: " + iNe.getName());
                           printLine(file, nindent + 2, "Type: " + iNe.getElementType());
                        }
                     }
                  }
               }
            }
         }
      }

   }

   //   *******************************************************************************************
   //    Relationships
   //   *******************************************************************************************

   protected static void printImplementationLinkDetails( FileWriter file, int nindent, IImplementation iImpl)
   {

      IClassifier iCl, icl2;
      ETList < IElement > iels;
      IElement iEl;

      if (iImpl != null)
      {
         INamedElement iSupp, iClient;
         iSupp = iImpl.getSupplier();
         if (iSupp != null)
         {
            printLine(file, nindent, "Supplier: " + iSupp.getFullyQualifiedName(false));
         }
         iClient = iImpl.getClient();
         if (iClient != null)
         {
            printLine(file, nindent, "Client: " + iClient.getFullyQualifiedName(false));
         }
      }
   }

   protected static void printGenLinkDetails( FileWriter file, int nindent, IGeneralization iGen)
   {
      IClassifier iCl, icl2;
      ETList < IElement > iels;
      IElement iEl;

      if (iGen != null)
      {
         iCl = iGen.getGeneral();
         if (iCl != null)
         {
            printLine(file, nindent, "Super: " + iGen.getGeneral().getFullyQualifiedName(false));
         }
         icl2 = iGen.getSpecific();
         if (icl2 != null)
         {
            printLine(file, nindent, "Sub: " + iGen.getSpecific().getFullyQualifiedName(false));
         }

         iels = iGen.getAssociatedArtifacts();
         if (iels != null)
         {
            final int iCount = iels.getCount();
            if (iCount > 0)
            {
               printLine(file, nindent, "Associated Artifact Count:  " + iCount );
            }
            for (int f = 0; f<iCount; f++)
            {
               iEl = iels.item(f);
               if (iEl != null)
               {
                  IArtifact iart;
                  iart = (IArtifact)iEl;
                  if (iart != null)
                  {
                     printLine(file, nindent + 2, "Associated Artifact  " + String.valueOf(f + 1));
                     printLine(file, nindent + 4, "File name: " + iart.getFileName());
                  }
               }
            }
         }
      }
   }

   protected static void printAssociationDetails( FileWriter file, int nindent, IAssociation iassoc)
   {

      IClassifier iCl, icl2;
      IAggregation iagg;
      String sTemp = "", sTemp2 = "", sTemp3 = "", sTemp4 = "";
      int j;

      if (iassoc != null)
      {
         ETList<IAssociationEnd> ends = iassoc.getEnds();
         final int iEndCount = ends.getCount();
         
         printAllClassifierDetails( file, nindent, iassoc);
         printLine(file, nindent, "Association Type: " + iassoc.getElementType()); //+ str(i + 1)
         printLine(file, nindent, "Derived: " + reportBoolean(iassoc.getIsDerived()));
         printLine(file, nindent, "Reflexive: " + reportBoolean(iassoc.getIsReflexive()));

         if (iassoc.getElementType().equals("Aggregation"))
         {

            for (j = 0; j<iEndCount; j++)
            {
               IAssociationEnd iAssocEnd;
               iAssocEnd = ends.item(j);
               if (iAssocEnd != null)
               {
                  icl2 = iAssocEnd.getType();
                  if (icl2 != null)
                  {
                     //sTemp = iassoc.Ends.item(j).getType().getFullyQualifiedName(false)
                     sTemp = icl2.getFullyQualifiedName(false);
                     if (j == 0)
                     {
                        printLine(file, nindent, "Start Class: " + sTemp);
                     }
                     else if (j == (iEndCount - 1))
                     {
                        printLine(file, nindent, "End Class: " + sTemp);
                     }
                     else
                     { //not sure if Middle is possible
                        printLine(file, nindent, "Middle Class: " + sTemp);
                     }
                  }
               }
            }
            
            iagg = (IAggregation)iassoc;

            if (iagg != null)
            {
               printLine(file, nindent, "Composite: " + reportBoolean(iagg.getIsComposite()));

               //Aggregate End
               printLine(file, nindent + 2, "Aggregate End:");
               printAggregateEndDetails(file, nindent + 4, iagg);
               //Part End
               printLine(file, nindent + 2, "Part End:");
               printPartEndDetails(file, nindent + 4, iagg);
            }
         }
         else
         { //association
            for (j = 0; j<iEndCount; j++)
            {
               IAssociationEnd iassocEnd2;
               iassocEnd2 = ends.item(j);
               if (iassocEnd2 != null)
               {
                  icl2 = iassocEnd2.getType();
                  if (icl2 != null)
                  {
                     sTemp = icl2.getFullyQualifiedName(false);
                     if (j == 0)
                     {
                        printLine(file, nindent, "Start Class: " + sTemp);
                     }
                     else if (j == (iEndCount-1))
                     {
                        printLine(file, nindent, "End Class: " + sTemp);
                     }
                     else
                     { //not sure if Middle is possible
                        printLine(file, nindent, "Middle Class: " + sTemp);
                     }
                     printAssociationEndDetails(file, nindent + 4, iassocEnd2);
                  }
               }
            }
         }
      }
   }

   protected static void printAssociationEndDetails( FileWriter file, int nindent, IAssociationEnd iAssocEnd)
   {
      String sTemp = "", sTemp2 = "", sTemp3 = "";
      ETList<IAttribute> iattrs;

      printLine(file, nindent, "Name: " + iAssocEnd.getName());
      printLine(file, nindent, "Alias: " + iAssocEnd.getAlias());
      printVisibility( file, nindent, iAssocEnd );
      printLine(file, nindent, "Stereotype: " + iAssocEnd.getAppliedStereotypesAsString(false));
      printTaggedValues( file, nindent, iAssocEnd);
      printConstraints( file, nindent, iAssocEnd);
      printLine(file, nindent, "Final: " + reportBoolean(iAssocEnd.getIsFinal()));
      printLine(file, nindent, "Static: " + reportBoolean(iAssocEnd.getIsStatic()));
      printLine(file, nindent, "Transient: " + reportBoolean(iAssocEnd.getIsTransient()));
      printLine(file, nindent, "Volatile: " + reportBoolean(iAssocEnd.getIsVolatile()));
      printLine(file, nindent, "Type: " + iAssocEnd.getType().getElementType());
      printClientChangeability( file, nindent, iAssocEnd );
      sTemp3 = iAssocEnd.getMultiplicity().getRangeAsString( true );
      printLine(file, nindent, "Multiplicity: " + sTemp3);
      printLine(file, nindent, "Navigable: " + reportBoolean(iAssocEnd.getIsNavigable()));

      //Qualifiers
      iattrs = iAssocEnd.getQualifiers();
      if (iattrs != null)
      {
         printQualifierDetails( file, nindent, iattrs);
      }
   }

   protected static void printAggregateEndDetails( FileWriter file, int nindent, IAggregation iagg)
   {
      String sTemp = "", sTemp2 = "", sTemp3 = "";
      IClassifier iCl;
      ETList< IAttribute > iattrs;

      if (iagg != null)
      {
         IAssociationEnd aggregateEnd = iagg.getAggregateEnd();
         printLine(file, nindent, "Name: " + aggregateEnd.getName());
         printLine(file, nindent, "Alias: " + aggregateEnd.getAlias());
         printVisibility( file, nindent, aggregateEnd );
         printLine(file, nindent, "Stereotype: " + aggregateEnd.getAppliedStereotypesAsString(false));
         printTaggedValues( file, nindent, aggregateEnd);
         printConstraints( file, nindent, aggregateEnd);
         printLine(file, nindent, "Final: " + reportBoolean(aggregateEnd.getIsFinal()));
         printLine(file, nindent, "Static: " + reportBoolean(aggregateEnd.getIsStatic()));
         printLine(file, nindent, "Transient: " + reportBoolean(aggregateEnd.getIsTransient()));
         printLine(file, nindent, "Volatile: " + reportBoolean(aggregateEnd.getIsVolatile()));
         iCl = aggregateEnd.getType();
         if (iCl != null)
         {
            printLine(file, nindent, "Type: " + iCl.getElementType());
         }
         printClientChangeability( file, nindent, aggregateEnd );
         sTemp3 = aggregateEnd.getMultiplicity().getRangeAsString( true );
         printLine(file, nindent, "Multiplicity: " + sTemp3);
         printLine(file, nindent, "Navigable: " + reportBoolean(aggregateEnd.getIsNavigable()));
         //Qualifiers
         iattrs = aggregateEnd.getQualifiers();
         if (iattrs != null)
         {
            printQualifierDetails( file, nindent, iattrs);
         }
      }

   }

   protected static void printPartEndDetails( FileWriter file, int nindent, IAggregation iagg)
   {
      String sTemp = "", sTemp2 = "", sTemp3 = "";
      IClassifier iCl;
      ETList< IAttribute > iattrs;

      if (iagg != null)
      {
         IAssociationEnd partEnd = iagg.getPartEnd();
         printLine(file, nindent, "Name: " + partEnd.getName());
         printLine(file, nindent, "Alias: " + partEnd.getAlias());
         printVisibility( file, nindent, partEnd );
         printLine(file, nindent, "Stereotype: " + partEnd.getAppliedStereotypesAsString(false));
         printTaggedValues( file, nindent, partEnd);
         printConstraints( file, nindent, partEnd);
         printLine(file, nindent, "Final: " + reportBoolean(partEnd.getIsFinal()));
         printLine(file, nindent, "Static: " + reportBoolean(partEnd.getIsStatic()));
         printLine(file, nindent, "Transient: " + reportBoolean(partEnd.getIsTransient()));
         printLine(file, nindent, "Volatile: " + reportBoolean(partEnd.getIsVolatile()));
         iCl = partEnd.getType();
         if (iCl != null)
         {
            printLine(file, nindent, "Type: " + iCl.getElementType());
         }
         printClientChangeability( file, nindent, partEnd );
         sTemp3 = partEnd.getMultiplicity().getRangeAsString( true );
         printLine(file, nindent, "Multiplicity: " + sTemp3);
         printLine(file, nindent, "Navigable: " + reportBoolean(partEnd.getIsNavigable()));

         //Qualifiers
         iattrs = partEnd.getQualifiers();
         if (iattrs != null)
         {
            printQualifierDetails( file, nindent, iattrs);
         }
      }

   }

   protected static void printQualifierDetails( FileWriter file, int nindent, ETList < IAttribute > iattrs)
   {
      String sTemp = "";
      if (iattrs.getCount() > 0)
      {
         final int iCount = iattrs.getCount();
         printLine(file, nindent, "Qualifier Count:  " + iCount );
         for (int s = 0; s<iCount; s++)
         {
            IAttribute iAttr = iattrs.item(s);
            if (iAttr != null)
            {
               printLine(file, nindent + 2, "Qualifier " + String.valueOf(s + 1));
               printLine(file, nindent + 4, "Name: " + iAttr.getName());
               IClassifier iCl;
               iCl = iAttr.getType();
               if (iCl != null)
               {
                  printLine(file, nindent + 4, "Type: " + iCl.getName());
               }
               printVisibility( file, nindent + 4, iAttr );
               printLine(file, nindent + 4, "Documentation: " + iAttr.getDocumentation());
               printLine(file, nindent + 4, "Stereotype: " + iAttr.getAppliedStereotypesAsString(false));
               printTaggedValues(file, nindent + 4, iAttr);
               printConstraints(file, nindent + 4, iAttr);
            }
         }
      }

   }
   
   protected static void printDependencyDetails( FileWriter file, int nindent, IDependency idepend)
   {
      INamedElement iName1;
      INamedElement iName2;

      if (idepend != null)
      {
         iName1 = idepend.getClient();
         if (iName1 != null)
         {
            printLine(file, nindent, "Client Name: " + idepend.getClient().getName());
         }

         iName2 = idepend.getSupplier();
         if (iName2 != null)
         {
            printLine(file, nindent, "Supplier Name: " + idepend.getSupplier().getName());
         }
      }

   }

   protected static void printMultiFlowDetails( FileWriter file, int nindent, IMultiFlow multi)
   {
      if (multi != null)
      {
         IActivityNode anode1, anode2;
         anode1 = multi.getSource();
         if (anode1 != null)
         {
            printLine(file, nindent, "Source: " + anode1.getName());
            printLine(file, nindent, "Source Type: " + anode1.getElementType());
         }
         anode2 = multi.getTarget();
         if (anode2 != null)
         {
            printLine(file, nindent, "Target: " + anode2.getName());
            printLine(file, nindent, "Target Type: " + anode2.getElementType());
         }
      }
   }

   //   *******************************************************************************************
   //    All Elements
   //   *******************************************************************************************

   protected static void printCommentDetails( FileWriter file, int nindent, IComment iEl)
   {
      int i;

      printLine(file, nindent, "Body: " + iEl.getBody());
      ETList<INamedElement> annotatedElements = iEl.getAnnotatedElements();
      if ( (annotatedElements != null) &&
           (annotatedElements.getCount() > 0) )
      {
         final int iCount = annotatedElements.getCount();
         printLine(file, nindent, "Annotated Elements count:  " + iCount );
         for (i = 0; i<iCount; i++)
         {
            Object obj = annotatedElements.item(i);
            if( obj instanceof INamedElement )
            {
               INamedElement annotatedElement = (INamedElement)obj;
               printLine(file, nindent + 2, "Annotated Element  " + String.valueOf(i + 1));
               printLine(file, nindent + 4, " Name: " + annotatedElement.getName());
               printLine(file, nindent + 4, " Element Type: " + annotatedElement.getElementType());
            }
            else
            {
               ETSystem.out.println( "not a named element:  " + obj );
            }
         }
      }
   }

   protected static void printTaggedValues( FileWriter file, int nindent, IElement iEl) //need a sSpace var or helper );
   {

      ITaggedValue iTG;
      int i;
      String sSpaces;

      //  printLine( file,  nIndent, "Tagged Values" );
      final long lCount = iEl.getTaggedValueCount();
      if (iEl.getTaggedValueCount() > 0)
      {
         ETList<ITaggedValue> allTVs = iEl.getAllTaggedValues();
         printLine(file, nindent, "TaggedValues count:  " + lCount );
         for (i = 0; i < lCount; i++)
         {
            iTG = allTVs.item(i);

            if (iTG.isHidden() == false)
            {
               printLine(file, nindent + 2, "Tagged Value " + String.valueOf(i + 1));
            }
            else
            {
               printLine(file, nindent + 2, "Tagged Value " + String.valueOf(i + 1) + " (Hidden)");
            }
            printLine(file, nindent + 4, " name: " + iTG.getName());
            printLine(file, nindent + 4, " value: " + iTG.getDataValue());
         }

      }

      // TODO what is this from VB? -> DoEvents
   }

   protected static void printConstraints(FileWriter file, int nindent, IElement iEl) //need a sSpace var or helper );
   {

      IConstraint iCS;
      int i;
      // printLine( file,  nIndent, "Constraints" );
      ETList<IConstraint> constraints = iEl.getOwnedConstraints();
      final int iCount = constraints.getCount();
      if (iCount > 0)
      {
         printLine(file, nindent, "Constraint count:  " + iCount );
         for (i = 0; i<iCount; i++)
         {
            iCS = constraints.item(i);
            printLine(file, nindent + 2, "Constraint Value " + String.valueOf(i + 1));
            printLine(file, nindent + 4, "name: " + iCS.getName());
            printLine( file, nindent + 4, "express: " + iCS.getExpression());
         }
      }

   }
   
   protected static void printVisibility( FileWriter file, int nindent, INamedElement iNe )
   {
      String strVisibility = "";
      switch (iNe.getVisibility())
      {
         case IVisibilityKind.VK_PACKAGE :
         strVisibility = "package";
            break;
   
         case IVisibilityKind.VK_PRIVATE :
         strVisibility = "private";
            break;
   
         case IVisibilityKind.VK_PROTECTED :
         strVisibility = "protected";
            break;
   
         case IVisibilityKind.VK_PUBLIC :
         strVisibility = "public";
            break;
      }
      printLine( file, nindent, "Visibility: " + strVisibility );
   }
   
   protected static void printClientChangeability( FileWriter file, int nindent, IStructuralFeature feature )
   {
      printClientChangeability( file, nindent, feature.getClientChangeability() );
   }
   
   protected static void printClientChangeability( FileWriter file, int nindent, int changeability )
   {
      String strChangeability = "";
      switch ( changeability )
      {
         case IChangeableKind.CK_ADDONLY :
            strChangeability = "add only";
            break;

         case IChangeableKind.CK_REMOVEONLY :
            strChangeability = "removed only";
            break;

         case IChangeableKind.CK_RESTRICTED :
            strChangeability = "restricted";
            break;

         case IChangeableKind.CK_UNRESTRICTED :
            strChangeability = "unrestricted";
            break;
      }
      printLine( file, nindent, "ClientChangeability: " + strChangeability );
   }
   
   protected static void printLine(FileWriter file, int nindent, String str)
   {
      try
      {
         if (file != null)
         {
            for (int i = 0; i < nindent; i++)
            {
               file.write(" ");
            }
            printLine(file, str);
         }
      }
      catch( IOException e )
      {
      }
   }

   protected static void printLine(FileWriter file, String str)
   {
      try
      {
         if (file != null)
         {
            file.write(str);
            file.write("\r\n");
         }
      }
      catch( IOException e )
      {
      }
   }
   
   protected static String reportBoolean( boolean value )
   {
      return value ? "True" : "False";
   }

   //   =============================================================================================
   //     Diagram Report
   //
   //   =============================================================================================

   protected static void projectDiagramReport(IProduct prod, IProject iProj, String sDir, String sFileName, String bGraphic)
   {
      ProxyDiagramManager ipdm = ProxyDiagramManager.instance();
      ETList< IProxyDiagram > ipds;
      IDiagram iDiag;
      String sTemp = "";
      int iDiagLen;
      IDiagramCallback p = null;
      int hFile;
      boolean bWeOpen;
      // TODO IProxyDiagramCallback iDiagCall;

      try
      {
         FileWriter file = new FileWriter(sDir + "\\" + sFileName);
         if (file != null)
         {
            printLine(file, 0, PRINT_LINE80);
            printLine(file, 0, "Diagram Report for Project: " + iProj.getName());
   
            //ipds = ipdm.getDiagramsInProject(iproj.Name);
            ipds = ipdm.getDiagramsInProject(iProj);
   
            printLine(file, 0, "Diagram Count: " + String.valueOf(ipds.getCount()));
            printLine(file, 0, PRINT_LINE80);
            printLine(file, 0, "  ");
   
            final int iCount = ipds.getCount();
            for (int i = 0; i<iCount; i++)
            {
               IProxyDiagram ipd;
               ipd = ipds.item(i);
               if (ipd.isOpen() == true)
               {
                  bWeOpen = false;
                  iDiag = ipd.getDiagram();
               }
               else
               {
                  bWeOpen = true;
                  iDiag = prod.getDiagramManager().openDiagram2(ipd, true, p);
               }
   
               diagramReport( file, iDiag );
   
               if ( bGraphic.equalsIgnoreCase("TRUE") )
               {
                  iDiag.saveAsGraphic(sDir.trim() + "\\" + iDiag.getName().trim() + ".jpg ", ISaveAsGraphicKind.SAFK_JPG);
               }
   
               if (bWeOpen = true)
               {
                  if (iDiag.isDirty() == true)
                  {
                     iDiag.save();
                  }
                  // TODO prod.DiagramManager.closeDiagram3(ipd, iDiagCall);
               }
            }
   
            file.close();
         }
      }
      catch( IOException e )
      {
      }
   }

   //    CurrentDiagramReport

   //     prod - application product
   //     sDir - directory to store Diagram Report files
   //     sFileName - textfile name to write results, including extension
   //     sGraphicName - filename to write JPG capture of diagram, if empty string no JPG generated

   protected static void currentDiagramReport(IProduct prod, String sDir, String sFileName, String sGraphicName)
   {
      IDiagram iDiag;

      iDiag = prod.getDiagramManager().getCurrentDiagram();
      if (iDiag != null)
      {
         try
         {
            FileWriter file = new FileWriter(sDir + "\\" + sFileName);
            if (file != null)
            {
               diagramReport(file, iDiag);
   
               if (sGraphicName.length() > 0)
               {
                  iDiag.saveAsGraphic(sDir.trim() + "\\" + sGraphicName.trim(), ISaveAsGraphicKind.SAFK_JPG);
   
               }
   
               file.close();
            }
         }
         catch( IOException e )
         {
         }
      }
   }

   protected static void printMainDiagramDetails(FileWriter file, IDiagram iDiag)
   {

      String sTemp = "";

      if (iDiag != null)
      {
         printLine(file, 0, PRINT_LINE80EL);
         printLine(file, 0, " Diagram: " + iDiag.getQualifiedName());
         printLine(file, 0, PRINT_LINE80EL);

         // TODO: meteora
//         switch (iDiag.getLayoutStyle())
//         {
//            case ILayoutKind.LK_CIRCULAR_LAYOUT :
//               sTemp = "circular";
//               break;
//
//            case ILayoutKind.LK_GLOBAL_LAYOUT :
//               sTemp = "global";
//               break;
//
//            case ILayoutKind.LK_HIERARCHICAL_LAYOUT :
//               sTemp = "hierarchical";
//               break;
//
//            case ILayoutKind.LK_INCREMENTAL_LAYOUT :
//               sTemp = "incremental";
//               break;
//
//            case ILayoutKind.LK_NO_LAYOUT :
//               sTemp = "no layout";
//               break;
//
//            case ILayoutKind.LK_ORTHOGONAL_LAYOUT :
//               sTemp = "orthogonal";
//               break;
//
//            case ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT :
//               sTemp = "sequence";
//               break;
//
//            case ILayoutKind.LK_SYMMETRIC_LAYOUT :
//               sTemp = "symmetric";
//               break;
//
//            case ILayoutKind.LK_TREE_LAYOUT :
//               sTemp = "tree";
//               break;
//
//            case ILayoutKind.LK_UNKNOWN_LAYOUT :
//               sTemp = "unknown";
//               break;
//         }
//
//         printLine(file, 1, "Diagram Layout Style: " + sTemp);
//         printLine(file, 1, "Diagram Kind: " + iDiag.getDiagramKind2());
         printLine(file, 1, "Diagram Documentation: " + iDiag.getDocumentation());
         //associated diagrams
         ETList < IProxyDiagram > proxDiags = iDiag.getAssociatedDiagrams();
         if (proxDiags != null)
         {
            final int iCount = proxDiags.getCount();
            if (iCount > 0)
            {
               printLine(file, 1, "Associated Diagram Count:  " + iCount );
               int l;
               for (l = 0; l<iCount; l++)
               {
                  IProxyDiagram proxDiag;
                  proxDiag = proxDiags.item(l);
                  if (proxDiag != null)
                  {
                     printLine(file, 2, "Associated Diagram " + String.valueOf(l + 1));
                     printLine(file, 4, "Name: " + proxDiag.getName());
                     printLine(file, 4, "Type: " + proxDiag.getDiagramKindName());
                  }
               }
            }
         }

         //associated elements
         ETList< IElement > iEles;
         iEles = iDiag.getAssociatedElements();
         if (iEles != null)
         {
            final int iCount = iEles.getCount();
            if (iCount > 0)
            {
               printLine(file, 1, "Associated Element Count:" + iCount );
               int n;
               for (n = 0; n<iCount; n++)
               {
                  IElement iElem;
                  iElem = iEles.item(n);
                  if (iElem != null)
                  {
                     INamedElement iNe;
                     iNe = (INamedElement)iElem;
                     if (iNe != null)
                     {
                        printLine(file, 2, "Associated Element" + String.valueOf(n + 1));
                        printLine(file, 4, "Name: " + iNe.getName());
                        printLine(file, 4, "Type: " + iNe.getElementType());
                     }
                  }
               }
            }
         }
         printLine(file, 1, " ");
      }
   }

   //    DiagramReport
   //
   //     idiag - diagram to report on
   //     hFile - output file to write results

   protected static void diagramReport(FileWriter file, IDiagram iDiag)
   {
      // TODO AutoHelperReportDataSQD ahRSQD;
      if (iDiag != null)
      {

         printMainDiagramDetails(file, iDiag);

         if (iDiag.getDiagramKindAsString().equals("Sequence Diagram"))
         {
            // TODO ahRSQD.diagramReportSQD(iDiag);
         }
         else
         {
            //Presentation Elements
            IElement ee;
            INamedElement ne;

            ETList<IPresentationElement> allItems = iDiag.getAllItems(); 
            final int iCount = allItems.getCount();
            for (int t = 0; t<iCount; t++)
            {
               ee = allItems.item(t).getFirstSubject();
               if (ee != null)
               {
                  printLine(file, 1, "Element:" + String.valueOf(t));
                  //replaced with line below: printLine( file,  1, "Type:" + ee.ElementType );
                  printLine(file, 1, "Type:" + ee.getExpandedElementType());
               }

               ne = (INamedElement)ee;
               if (ne != null)
               {
                  printLine(file, 1, "Name:" + ne.getName());
               }
            }
            printLine(file, 1, "");
         }
      }
   }

   public static void diagramReport2(IDiagram iDiag, String sFolder, String sFileName)
   {
      try
      {
         FileWriter file = new FileWriter(sFolder + "\\" + sFileName);
         if (file != null)
         {
            diagramReport(file, iDiag);
   
            file.close();
         }
      }
      catch( IOException e )
      {
      }
   }

   //    This function reports on an interation under an operation.  ITs assumed there is only one interaction
   public static void interactionReport(IOperation iop, IProject iProj, String sFolder, String sFileName)
   {

      int i, j;
      ETList< IElement > eles;
      IInteraction iint;
      IElementLocator iEl = new ElementLocator();

      try
      {
         FileWriter file = new FileWriter(sFolder + "\\" + sFileName);
         if (file != null)
         {
            printLine(file, 0, PRINT_LINE80);
            printLine(file, 0, " BASELINE REPORT");
            printLine(file, 0, PRINT_LINE80);
            printLine(file, 0, " VERSION 6.0");
            printLine(file, 0, "Interaction Information");
            printLine(file, 0, PRINT_LINE80);
            eles = iEl.findElementsByQuery(iop, ".//UML:CombinedFragment");
   
            ETList<IElement> elements = iop.getElements();
            final int iCount = elements.getCount();
            for (i = 0; i<iCount; i++)
            {
               // TODO Debug.Print "element type:", iop.getElements().item(i).getElementType() if (iop.getElements().item(i).getElementType() = "Interaction")
               IElement element = elements.item(i);
               if( element instanceof IInteraction )
               {
                  iint = (IInteraction)element;
                  ETList<IMessage> messages = iint.getMessages();
                  final int iMessageCnt = messages.getCount();
                  printLine(file, 0, "Total Message Count: " + iMessageCnt );
                  ETList<ILifeline> lifelines = iint.getLifelines();
                  final int iLifelineCnt = lifelines.getCount();
                  printLine(file, 0, "Total Lifeline Count: " + iLifelineCnt );
                  final int iCFCnt = eles.getCount();
                  printLine(file, 0, "Total Comb Frag Count: " + iCFCnt );
                  printLine(file, 0, PRINT_LINE80);
                  for (j = 0; j<iMessageCnt; j++)
                  {
                     printLine(file, 0, "Message:" + String.valueOf(j + 1));
                     printMessageDetails(file, 1, messages.item(j));
                     printLine(file, 0, "");
                  }
                  printLine(file, 0, PRINT_LINE80);
                  for (j = 0; j<iLifelineCnt; j++)
                  {
                     printLine(file, 0, "Lifeline:" + String.valueOf(j + 1));
                     printLifeLineDetails(file, 1, lifelines.item(j));
                     printLine(file, 0, "");
                  }
                  printLine(file, 0, PRINT_LINE80);
   
                  for (j = 0; j<iCFCnt; j++)
                  {
                     printLine(file, 0, "Fragment:" + String.valueOf(j + 1));
                     ICombinedFragment icf;
                     icf = (ICombinedFragment)eles.item(j);
                     // TODO Debug.Print icf.getName()printCfragDetails(1, icf);
                     printLine(file, 0, "");
                  }
   
               }
            }
   
            file.close();
         }
      }
      catch( IOException e )
      {
      }
   }
}


