/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
