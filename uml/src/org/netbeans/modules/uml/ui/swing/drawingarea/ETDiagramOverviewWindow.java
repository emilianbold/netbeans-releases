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

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.overview.TSEOverviewComponent;
import com.tomsawyer.editor.overview.TSEOverviewWindow;

import java.awt.Frame;

/**
 * @author KevinM
 *
 */
public class ETDiagramOverviewWindow extends TSEOverviewWindow implements IETSecondaryWindow
{

   /**
    * @param arg0
    * @param arg1
    * @param arg2
    */
   public ETDiagramOverviewWindow(Frame arg0, String arg1, TSEGraphWindow arg2)
   {
      super(arg0, arg1, arg2);
      // TODO Auto-generated constructor stub
   }

   /**
    * @param arg0
    * @param arg1
    * @param arg2
    */
   public ETDiagramOverviewWindow(Frame arg0, String arg1, TSEOverviewComponent arg2)
   {
      super(arg0, arg1, arg2);
      // TODO Auto-generated constructor stub
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IETSecondaryWindow#setGraphWindow(com.tomsawyer.editor.TSEGraphWindow)
    */
   public void setGraphWindow(TSEGraphWindow graphWindow)
   {
      if (this.getOverviewComponent() != null)
			this.getOverviewComponent().setGraphWindow(graphWindow);

   }
   
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IETSecondaryWindow#getGraphWindow()
    */
	public TSEGraphWindow getGraphWindow()
	{
		return this.getOverviewComponent() != null ? this.getOverviewComponent().getGraphWindow() : null;
	}
	
	public void setVisible(boolean show)
	{
		super.setVisible(show);
		if (show == false)
		{
			ETSystem.out.println("ETDiagramOverview is being hiden");
		}
		else
		{
			ETSystem.out.println("ETDiagramOverview is being shown");
		}
	}
	
}
