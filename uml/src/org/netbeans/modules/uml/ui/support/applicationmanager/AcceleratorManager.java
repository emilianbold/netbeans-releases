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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.common.generics.ETPairT;

/**
 * @author sumitabhk
 *
 *
 */
public class AcceleratorManager implements IAcceleratorManager
{
	protected ETList<ETPairT<JComponent, ETList<String> > > m_AcceleratorTable = new ETArrayList<ETPairT<JComponent, ETList<String> > >();
	protected ETList<ETPairT<JComponent, ETList<String> > > m_NoFocusAcceleratorTable = new ETArrayList<ETPairT<JComponent, ETList<String> > >();
	protected ETList<ETPairT<IAcceleratorListener, ETList<String> > >m_AcceleratorListenerTable = new ETArrayList<ETPairT<IAcceleratorListener, ETList<String> > >();

	/**
	 * 
	 */
	public AcceleratorManager()
	{
		super();
	}

	public void register(JComponent hwnd, final IAcceleratorListener listener, final String accelerator, boolean bNoFocus)
	{
		int nCondition = JComponent.WHEN_FOCUSED;
		if (bNoFocus)
		{
			nCondition = JComponent.WHEN_IN_FOCUSED_WINDOW;
		}
		
		ActionListener action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				listener.onAcceleratorInvoke(accelerator);
			}
		};
		
		KeyStroke keyStroke = KeyStroke.getKeyStroke(accelerator);
                
                //added this line to handle tab/shift-tab 
		hwnd.setFocusTraversalKeysEnabled(false);
		hwnd.registerKeyboardAction(action, keyStroke, nCondition);
	}
	
	public void register(JComponent hwnd, IAcceleratorListener listener, ETList<String> accelerators, boolean bNoFocus )
	{
		if (hwnd != null && listener != null && accelerators != null)
		for (int i = 0; i < accelerators.size(); i++)
		{
			this.register(hwnd, listener, accelerators.get(i), bNoFocus);
		}
	}

        public void register(JComponent hwnd, final IAcceleratorListener listener, final int keyCode, int modifierMask, boolean bNoFocus)
	{
		int nCondition = JComponent.WHEN_FOCUSED;
		if (bNoFocus)
		{
			nCondition = JComponent.WHEN_IN_FOCUSED_WINDOW;
		}
		
		AbstractAction action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				listener.onCreateNewNodeByKeyboard();
			}
		};
		

                //added this line to handle tab/shift-tab 
		hwnd.setFocusTraversalKeysEnabled(false);
                
                KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifierMask, true);
		hwnd.registerKeyboardAction(action, keyStroke, nCondition);
	}
        
	public void revoke(JComponent hwnd)
	{
	}

	public void registerListener(IAcceleratorListener listener, ETList<String> pAccelerators)
	{
		if(listener != null && pAccelerators != null)
		{
			ETPairT<IAcceleratorListener, ETList<String> > newVal = new ETPairT<IAcceleratorListener, ETList<String> >();
			newVal.setParamOne(listener);
			newVal.setParamTwo(pAccelerators);
			m_AcceleratorListenerTable.add(newVal);
		}
	}

	public void revokeListener(IAcceleratorListener listener)
	{
		if(listener != null)
		{
			for(int i = 0; i < m_AcceleratorListenerTable.size(); i++)
			{
				ETPairT<IAcceleratorListener, ETList<String> > val = m_AcceleratorListenerTable.get(i);
				IAcceleratorListener pAcceleratorListener = val.getParamOne();
				if(pAcceleratorListener.equals(listener))
				{
					m_AcceleratorListenerTable.remove(i);
					break;
				}
			}
		}
	}

	public boolean translateAccelerators(String keyCode)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorManager#setHandled(boolean)
	 */
	public void setHandled(boolean newVal) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorManager#getHandled()
	 */
	public boolean getHandled() {
		// TODO Auto-generated method stub
		return false;
	}
}


