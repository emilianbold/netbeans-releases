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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public class SimpleAction implements ISimpleAction
{
	int m_kind = 0;

	/**
	 *
	 */
	public SimpleAction()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleAction#getKind()
	 */
	public int getKind()
	{
		// TODO Auto-generated method stub
		return m_kind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleAction#setKind(int)
	 */
	public void setKind(int value)
	{
		m_kind = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#getDescription()
	 */
	public String getDescription()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#execute()
	 */
	public void execute( IDrawingAreaControl control )
   {
      if( null == control )   throw new IllegalArgumentException();
      
      switch( m_kind )
      {
         case DiagramAreaEnumerations.SAK_DELETE_SELECTED:
            control.deleteSelected(false);
            control.setIsDirty(true);
            break;
            
         default:
            ETSystem.out.println( "WARNING:  need to implement a SimpleAction:  " + m_kind );
            break;
      }
	}

}
