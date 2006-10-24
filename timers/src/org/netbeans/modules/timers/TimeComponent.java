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
package org.netbeans.modules.timers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Lahoda
 */
public class TimeComponent extends TopComponent {

    private static final String PREFERRED_ID = "timers"; //NOI18N
            static final String ICON_PATH = "org/netbeans/modules/timers/resources/timer.png"; //NOI18N
    private static TimeComponent INSTANCE;
    
    /**
     * Creates a new instance of TimeComponent
     */
    public TimeComponent() {
        setName ("timers"); //NOI18N
        setDisplayName (NbBundle.getMessage ( TimeComponent.class, "LBL_TimeComponent" )); //NOI18N
        setIcon(Utilities.loadImage(ICON_PATH));
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(new TimeComponentPanel(), gridBagConstraints);
    }

    public @Override String preferredID () {
        return PREFERRED_ID;
    }
    

    public @Override int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized TimeComponent getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new TimeComponent();
        }
        return INSTANCE;
    }
    
    public static synchronized TimeComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find TimeComponent component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TimeComponent) {
            return (TimeComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
}
