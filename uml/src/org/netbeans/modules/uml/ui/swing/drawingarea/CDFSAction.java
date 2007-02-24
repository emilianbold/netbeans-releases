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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.SimpleElementsAction;

/**
 * @author KevinM
 *
 */
public class CDFSAction extends SimpleElementsAction
{
	protected boolean wasPopulating;
   /**
    *
    */
   public CDFSAction(IDrawingAreaControl control)
   {
      super();
		setKind(ISimpleElementsAction.SEAK.DELAYED_CDFS);
		IDiagram diagram = control != null ? control.getDiagram() : null;
		boolean populating = false;
		if (diagram != null)
		{
			wasPopulating = diagram.getPopulating();
			diagram.setPopulating(true);
		}		
		else
			wasPopulating = false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.drawingarea.IExecutableAction#execute(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
    */
   public void execute(IDrawingAreaControl control)
   {
		IDiagram diagram = control != null ? control.getDiagram() : null;
		if (diagram != null)
		{
			diagram.setPopulating(true);
		}
		
      super.execute(control);
      
		if (diagram != null)
		{
			diagram.setPopulating(wasPopulating);
		}
   }

}
