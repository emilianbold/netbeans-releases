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


package org.netbeans.modules.uml.ui.addins.diagramcreator;

import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.applicationmanager.IDiagramCallback;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;

/**
 * @author sumitabhk
 *
 */
public class DiagramHandler implements IDiagramCallback
{
   // The diagCreator that needs to be called back
   private DiagCreatorAddIn m_rawDiagCreatorAddIn = null;

   // parameters to be passed into CDiagCreatorAddIn::AddElementsToDiagram()
   private ETList < IElement > m_cpElements = null;
   private IElement m_cpElement = null;
   private IProjectTreeControl m_cpProjectTree = null;
   private IProjectTreeModel projectTreeModel = null;
   private IOperation m_cpOperationToRE = null;

   /// When true the GUI functions have been called
   private boolean m_bUsingGUI = false;
   protected ETSmartWaitCursor waitCursor = null;
   /**
    * 
    */
   public DiagramHandler()
   {
      super();
   }

   /*
    * 
    * @author Kevinm
    *
    * Run Fit in window later so we can get the graph window up,
    */
   protected class FitInWindowThread implements Runnable
   {
      IDiagram pDiagram;

      public FitInWindowThread(IDiagram diagram)
      {
         pDiagram = diagram;
      }

      public void run()
      {
         if (m_cpProjectTree != null)
         {
            // This refresh will be ignored, but will tell the gui blocker to refresh when it
            // destructs
            try
            {
               m_cpProjectTree.refresh(false);
            }
            catch (Exception e)
            {
            }

         }
         if (pDiagram != null)
         {
            pDiagram.setAllowRedraw(true);
            pDiagram.fitInWindow();
            pDiagram.setPopulating(false);
            if (waitCursor != null)
            {
               waitCursor.stop();
            }
         }
      }
   }
   
   protected void preProcessDiagramReturned(IDiagram pDiagram)
   {
      // Turn off the window painting process, and bounds updates for performance.
      if (pDiagram != null)
      {
         waitCursor = new ETSmartWaitCursor();
         pDiagram.setAllowRedraw(false);
         pDiagram.setAutoUpdateBounds(false);
         pDiagram.setPopulating(true);
      }

   }

   protected void postProcessDiagramReturned(IDiagram pDiagram)
   {
      if (pDiagram != null)
      {
      	final IDiagram diagram = pDiagram;
         pDiagram.setAutoUpdateBounds(true);
         pDiagram.sizeToContents(false);

         // Bounds have changed double check that we still fit in the window.
	SwingUtilities.invokeLater(new FitInWindowThread(pDiagram));
      }
   }

   protected void processDiagramReturned(IDiagram pDiagram)
   {
      ETSmartWaitCursor wait = new ETSmartWaitCursor();
      try
      {
         // Operations may need to be reverse engineered
         if (m_cpOperationToRE != null)
         {
            IInteraction cpInteraction = m_rawDiagCreatorAddIn.continueREOperation(m_cpOperationToRE, pDiagram);

            // The interaction is now the element to be used for determining
            // the elements to be added to the diagram.
            m_cpElement = cpInteraction;
         }
         wait.restore();

         // Fix W6733:  Moved the Question for adding owned elements here
         // If m_bUsingGUI is false, CDiagCreatorAddIn::AddElementsToDiagram() will add
         // the owned elements, if necessary, without asking the user.
         int nBehavior = DiagCreatorAddIn.CRB_GET_ALL;
         if (m_bUsingGUI && m_cpElement != null && m_cpElements != null)
         {
            // Don't waste time with the owned elements when creating an SQD
            if (pDiagram.getDiagramKind() != IDiagramKind.DK_SEQUENCE_DIAGRAM)
            {
               int numSelected = m_cpElements.size();
               if (numSelected == 1)
               {
                  m_rawDiagCreatorAddIn.guiAddOwnedElements(pDiagram, m_cpElement, m_cpElements);

                  // Make sure we don't ask for owned elements again
                  nBehavior = DiagCreatorAddIn.CRB_NONE;
               }
            }
         }
         wait.restore();

         // Create a blocker so future refreshes of the project tree are delayed
         {
            m_rawDiagCreatorAddIn.addElementsToDiagram(pDiagram, m_cpElements, m_cpElement, nBehavior);
         }
         wait.restore();
         
         //Fixed 110811. 
         // Expand the project tree to the created item
         if (projectTreeModel != null && m_cpElements != null)
         {
             m_rawDiagCreatorAddIn.expandProjectTree(m_cpElements, projectTreeModel);
         }
//         if (m_cpProjectTree != null)
//         {
//            if (m_cpElement != null)
//            {
//               m_rawDiagCreatorAddIn.expandProjectTree(m_cpElement, m_cpProjectTree);
//            }
//            m_rawDiagCreatorAddIn.expandProjectTree(pDiagram, m_cpProjectTree);
//         }
         wait.restore();

         // After this the "this" pointer is invalid.  This object was created via
         // new CComObject by the controller who manages the memory.
         m_rawDiagCreatorAddIn.removeDiagramCallback(this);
         postProcessDiagramReturned(pDiagram);
         
         //Fixed issue 96121, 96119, 96118
         //Automatically save diagrams generated from CDFS, Dependency Diagram and RE Operation.
         //Need to call save() in a "invokeLater" thread to make sure the diagram is saved after
         //all other threads are done modifyfing the diagram."
         if ( pDiagram != null )
         {
             final IDiagram diagram = pDiagram;
             SwingUtilities.invokeLater(new Runnable()
             {
                 public void run()
                 {
                     diagram.save();
                 }
             }
             );
         }
      }
      finally
      {
         wait.stop();
      }
   }

   class DiagramProcessor implements Runnable
   {
      IDiagram pDiagram;
      public DiagramProcessor(IDiagram returnedDiagram)
      {
         super();
         pDiagram = returnedDiagram;
      }

      public void run()
      {
         processDiagramReturned(pDiagram);
      }
   }

   public void returnedDiagram(IDiagram pDiagram)
   {
      if (m_rawDiagCreatorAddIn != null)
      { 
         this.preProcessDiagramReturned(pDiagram);

         SwingUtilities.invokeLater(new DiagramProcessor(pDiagram));
      }
   }

   /**
    * @param b
    */
   public void setUsingGUI(boolean newVal)
   {
      m_bUsingGUI = newVal;
   }

   /**
    * @param element
    */
   public void setElement(IElement element)
   {
      m_cpElement = element;
   }

   public IElement getElement()
   {
      return m_cpElement;
   }

   public void setElements(ETList < IElement > elements)
   {
      m_cpElements = elements;
   }

   /**
    * @param operation
    */
   public void setOperationToRE(IOperation operation)
   {
      m_cpOperationToRE = operation;
   }

   /**
    * @param control
    */
   public void setProjectTree(IProjectTreeControl control)
   {
      m_cpProjectTree = control;
   }
   
   public void setProjectTreeModel(IProjectTreeModel treeModel)
   {
      projectTreeModel = treeModel;
   }

   /**
    * @param in
    */
   public void setDiagCreatorAddIn(DiagCreatorAddIn addin)
   {
      m_rawDiagCreatorAddIn = addin;
   }

}
