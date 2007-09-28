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
