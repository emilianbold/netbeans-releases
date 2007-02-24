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

package org.netbeans.modules.uml.reporting.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public abstract class WizardPanelBase extends JPanel implements WizardDescriptor.Panel {

    public WizardPanelBase() {
        super();
    }


    public WizardPanelBase(String name) {
		this();
		setName(name);
    }
  
    
    /**
     *
     *
     */
    public synchronized Component getComponent() {
        return this;
    }
    
    
    /**
     *
     *
     */
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    
    /**
     *
     *
     */
    public boolean isValid() {
		if (valid)
		{
			
		}
        return valid;
    }
    
    
    /**
     *
     *
     */
    public void setValid(boolean value) {
        this.valid = value;
        fireStateChanged();
    }
    
    
	/**
	 *
	 *
	 */
	public void addChangeListener(ChangeListener listener)
	{
		changeListeners.add(listener);
	}


	/** 
	 *
	 *
	 */
	public void removeChangeListener(ChangeListener listener)
	{
		changeListeners.remove(listener);
	}


	/** 
	 *
	 *
	 */
	public void fireStateChanged()
	{
		ChangeEvent event=new ChangeEvent(this);
		for (Iterator i=changeListeners.iterator(); i.hasNext(); )
		{
			try
			{
				((ChangeListener)i.next()).stateChanged(event);
			}
			catch (Exception e)
			{
				ErrorManager.getDefault().notify(e);
			}
		}
	}


    
    /**
	 *
	 *
	 */
	public abstract void readSettings(Object settings);


	/**
	 *
	 *
	 */
	public abstract void storeSettings(Object settings);


    
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    
    private String name;
    private boolean valid = false;
	private Set changeListeners=new HashSet();
}
