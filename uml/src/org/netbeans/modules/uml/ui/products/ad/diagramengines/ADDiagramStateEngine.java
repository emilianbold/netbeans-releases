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


package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
//import com.tomsawyer.layout.property.*;
//import com.tomsawyer.layout.glt.property.*;
/**
 * 
 * @author Trey Spiva
 */
public class ADDiagramStateEngine extends ADCoreEngine
{

   /**
    * Called after a new diagram is initialized.  The sequence diagram creates a new
    * IInteraction and then places the diagram under that IElement.
    */
   public void initializeNewDiagram()
   {
      if (getDrawingArea() != null)
      {
         getStateDiagramStateMachine();
         // Fixed 96474 - NPE
         // No need to set dirty state at this point.
         // The diagram will be saved after it is generated.
         // getDrawingArea().setIsDirty(true);
      }
      
   }

   /**
    *
    * Uses the presentation reference to determine the submachine associated with this diagram
    *
    * @param pState[out] The parent IState of the diagram, if it's null then a state is 
    * created and the diagram is reparented.
    *
    * @return HRESULT
    */
   private IStateMachine getStateDiagramStateMachine()
   {
      IStateMachine retObj = null;
      IDrawingAreaControl control = getDrawingArea();
      if (control != null)
      {
         IDiagram pDiagram = control.getDiagram();

         // See if the namespace is already and activity.  If it is then don't create one.
         INamespace pNamespace = null;
         String name = null;
         if (pDiagram != null)
         {
            pNamespace = pDiagram.getNamespace();
            name = pDiagram.getName();
         }

         IStateMachine pStateForStateDiagram = null;
         if (pNamespace != null && pNamespace instanceof IStateMachine)
         {
            pStateForStateDiagram = (IStateMachine) pNamespace;
         }

         // If we did not find a state machine,
         // create a state machine, and create the associated presentation reference
         if (pDiagram != null && pStateForStateDiagram == null)
         {
            // Create the state machine for the state diagram.
            TypedFactoryRetriever < IStateMachine > factory = new TypedFactoryRetriever < IStateMachine > ();
            pStateForStateDiagram = factory.createType("StateMachine");

            if (pStateForStateDiagram != null)
            {
               // Give the activity the same name as the diagram
               pStateForStateDiagram.setName(name);

               // Associate the activity with the diagram's current namespace
               pStateForStateDiagram.setNamespace(pNamespace);

               // Move the activity diagram under the activity
               control.setNamespace(pStateForStateDiagram);
            }
         }

         if (pStateForStateDiagram != null)
         {
            retObj = pStateForStateDiagram;
         }
      }
      return retObj;
   }

   /**
    * Returns the the namespace to use when elements are created on the diagram.  
    * Usually this is the same as the namespace of the diagram, but for the state diagram
    * we grab the first region off the state.
    *
    * @param pNamespace [out,retval] The namespace that should be used when creating new elements
    */
   public INamespace getNamespaceForCreatedElements()
   {
      INamespace space = null;

      IDrawingAreaControl control = getDrawingArea();
      if (control != null)
      {
         INamespace diaNamespace = control.getNamespace();
         if (diaNamespace != null && diaNamespace instanceof IStateMachine)
         {
            IStateMachine pStateMachine = (IStateMachine) diaNamespace;
            ETList < IRegion > pRegions = pStateMachine.getRegions();
            int count = 0;
            if (pRegions != null)
            {
               count = pRegions.size();
            }

            if (count > 0)
            {
               IRegion pRegion = pRegions.get(0);
               space = pRegion;
            }
            else
            {
               // Create a region
               Object obj = DrawingFactory.retrieveMetaType("Region");
               if (obj != null && obj instanceof IRegion)
               {
                  IRegion pCreatedRegion = (IRegion) obj;
                  pStateMachine.addRegion(pCreatedRegion);
                  pStateMachine.addOwnedElement(pCreatedRegion);
                  space = pCreatedRegion;
               }
            }
         }
      }

      return space;
   }

   /**
    * Called after a new diagram is initialized to setup our default layout settings
    */
   public void setupLayoutSettings(boolean bNewDiagram)
   {
      if (bNewDiagram)
      {
         IDrawingAreaControl control = getDrawingArea();
         if (control != null)
         {
            control.setLayoutStyleSilently(ILayoutKind.LK_HIERARCHICAL_LAYOUT);
            /*
            TSBooleanLayoutProperty property = new TSBooleanLayoutProperty(TSTailorProperties.HIERARCHICAL_ORTHOGONAL_ROUTING);
            property.setCurrentValue(true);

            control.getGraphWindow().getGraph().setTailorProperty(property);
             */
         }
      }
   }

   public void registerAccelerators()
   {
      ETList < String > accelsToRegister = new ETArrayList < String > ();

      // Add the normal accelerators, minus the layout stuff
      addNormalAccelerators(accelsToRegister, false);

      // Unique to the state diagram 
      accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_SIMPLESTATE);
      accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_STATETRANSITION);

      // Toggle orthogonality
      accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY);

      registerAcceleratorsByType(accelsToRegister);
   }
}
