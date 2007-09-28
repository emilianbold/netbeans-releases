/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
