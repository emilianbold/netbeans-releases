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

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;



/**
 *
 * @author Trey Spiva
 */
public class ADDiagramClassEngine extends ADCoreEngine
{
   public ADDiagramClassEngine()
   {
   }

   /** (non-Javadoc)
    * Allows engines to override the tool that gets created.
    *
    * @param sButtonID The button string in the presentation types file.  A
    *                  lookup is performed to figure out the TS init string.
    * @return Set the <code>true</code> to tell the drawing area that we've 
    *         handled the event
    * 
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#enterModeFromButton(java.lang.String)
    */
   public boolean enterModeFromButton(String sButtonID)
   {
      boolean retVal = false;
      
      retVal = super.enterModeFromButton(sButtonID);
      if(retVal == false)
      {
         // To the cls specific stuff.
      }
      
      return retVal;
   }

   /** 
    * Register for the accelerators that are specific to the class diagram.
    * 
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#registerAccelerators()
    */
	public void registerAccelerators()
	{
		ETList<String> accelsToRegister = new ETArrayList<String>();

		// Add the normal accelerators
		addNormalAccelerators(accelsToRegister, true);

		// Add the nodes and edges specific to the activity diagram
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_ATTRIBUTE);
      
		// Unique to the class diagram 
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_CLASS);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_GENERALIZATION);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_COMPOSITION  );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_ASSOCIATION  );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_AGGREGATION  );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_IMPLEMENTATION  );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_ABSTRACTION  );

		// Toggle orthogonality
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY );

		registerAcceleratorsByType(accelsToRegister);
	}

}
