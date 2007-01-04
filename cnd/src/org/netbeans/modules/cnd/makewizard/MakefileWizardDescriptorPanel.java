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

package  org.netbeans.modules.cnd.makewizard;

import java.awt.Component;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MakefileWizardDescriptorPanel implements WizardDescriptor.Panel {

    /** Serial version number */
    static final long serialVersionUID = -7154324322016837684L;

    private Vector listvec;

    private MakefileWizardPanel panel = null;
    private String helpString = "Construct_make_create"; // NOI18N

    MakefileWizardDescriptorPanel(MakefileWizardPanel panel, String helpString) {
	this.panel = panel;
	this.helpString = helpString;
    }


    /** Get the component for this panel */
    public Component getComponent() {
	return panel;
    }


    /**
     *  Default help for those panels which do not currently have a help topic.
     */
    public HelpCtx getHelp() {
	return new HelpCtx(helpString);
    }


    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    public boolean isValid() { 
	return panel.isPanelValid(); 
    }

    public void addChangeListener(ChangeListener listener) {
	if (listvec == null) {
	    listvec = new Vector(1);
	}
	listvec.add(listener);
    }    
  

    public void removeChangeListener(ChangeListener listener) {
	if (listvec != null) {
	    listvec.remove(listener);
	}
    }


    public void readSettings(Object settings) {
    }


    public void storeSettings(Object settings) {
    }    

    public void putClientProperty(Object key, Object value) {
	panel.putClientProperty(key, value);
    }
}
