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


