/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.actions;

import java.beans.*;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.*;

/**
*
* @author Jan Jancura
*/
public class MainProjectManager implements ProjectActionPerformer {
    
    private static MainProjectManager mainProjectManager = new MainProjectManager ();
    
    public static MainProjectManager getDefault () {
        return mainProjectManager;
    }
    
    private Action a;
    private Project mainProject;
    private PropertyChangeSupport pcs;


    private MainProjectManager () {
        pcs = new PropertyChangeSupport (this);
        a = MainProjectSensitiveActions.mainProjectSensitiveAction (
            this, null, null
        );
        a.isEnabled ();
    }

    public Project getMainProject () {
        return mainProject;
    }

    public void perform (Project p) {
    }

    public boolean enable (Project p) {
        if (mainProject == p) return true;
        Project o = mainProject;
        mainProject = p;
        pcs.firePropertyChange ("mainProject", o, mainProject);
        return true;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
}
