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
 *
 * Created on Jun 25, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.swing.drawingarea;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.ui.support.ADTransferable;

/**
 *
 * @author Trey Spiva
 */
public class ADDrawingAreaTransferHandler extends TransferHandler
{
	private int m_MoveAction = DnDConstants.ACTION_NONE;

	public ADDrawingAreaTransferHandler()
	{
	}

	/** 
	 * Overridden to check for the presence of a Describe data flavor.
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
	 */
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
	{
		boolean retVal = false;

		ETSystem.out.println("canImport");
		if (comp != null)
		{
			for (int index = 0; index < transferFlavors.length; index++)
			{
				if (transferFlavors[index].equals(ADTransferable.ADDataFlavor) == true)
				{
					retVal = true;
				} else if (transferFlavors[index].equals(DataFlavor.stringFlavor) == true)
				{
					retVal = true;
				}
			}
		} else
		{
			ETSystem.out.println("Component == null");
		}

		return retVal;
	}

	public boolean importData(JComponent comp, Transferable t)
	{
		boolean retVal = false;

		ETSystem.out.println("importData");
//		if (comp.getParent() instanceof JProjectTree)
//		{
//			JProjectTree tree = (JProjectTree) comp.getParent();
//			retVal = tree.fireEndDrag(t, m_MoveAction);
//		}

//		ADDrawingAreaControl drawingArea = (ADDrawingAreaControl) comp;
//		drawingArea.showNotImplementedMessage();
		try
		{
			String str = (String)t.getTransferData(DataFlavor.stringFlavor);
			ETSystem.out.println(str);
			retVal = true;
		} catch (UnsupportedFlavorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
	 */
	public int getSourceActions(JComponent c)
	{
		return COPY_OR_MOVE;
	}

}
