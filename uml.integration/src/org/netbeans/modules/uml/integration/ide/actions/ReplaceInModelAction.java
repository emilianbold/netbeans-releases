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

package org.netbeans.modules.uml.integration.ide.actions;

import org.netbeans.modules.uml.integration.finddialog.FindControllerDialog;
import org.netbeans.modules.uml.project.UMLProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class ReplaceInModelAction extends CallableSystemAction 
	implements PropertyChangeListener {
    
	/**
     */
    protected void initialize() {
        super.initialize();
		OpenProjects.getDefault().addPropertyChangeListener(this);
    }

	/**
     * This method is called if we are listening for changes on the set
     * of open projecst and some project(s) is opened/closed.
     */
    public void propertyChange(PropertyChangeEvent e) {
        synchronized (getLock()) {
			updateState();          
        }  
    }

    /**
     */
    public boolean isEnabled() {
		boolean enabled = false;
		
        synchronized (getLock()) {
			Project[] projects = OpenProjects.getDefault().getOpenProjects();
			for (Project p: projects)
			{
				if (p instanceof UMLProject)
					enabled=true;
			}
			return enabled;
        }
    }
    
    /**
     */
    private synchronized void updateState() {

        boolean enabled = false;
		Project[] projects = OpenProjects.getDefault().getOpenProjects();
		for (Project p: projects)
		{
			if (p instanceof UMLProject)
				enabled=true;
		}
		
		final boolean e = enabled;
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                setEnabled(e);
            }
        });
    }
	
	public void performAction() {
		Thread r = new Thread() {
            public void run() {
                FindControllerDialog fc = new FindControllerDialog();
                fc.showReplaceDialog();
            }
        };
        r.start();
	}
	
	public String getName() {
		return NbBundle.getMessage(ReplaceInModelAction.class, "CTL_ReplaceInModelAction");
	}
	
	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}
	
	protected boolean asynchronous() {
		return false;
	}
	
}
