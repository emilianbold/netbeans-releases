/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.etl.ui.view;

import javax.swing.JSplitPane;

import org.netbeans.modules.etl.ui.ETLDataObject;


/**
 *
 * @author karthik
 */
public class ConfigureParametersPanel extends JSplitPane {

    private ConfigParamsTreeView configTreeView;

    /** Creates a new instance of EditDBModelPanel */
    public ConfigureParametersPanel(ETLDataObject mObj) {        
        configTreeView = new ConfigParamsTreeView(mObj, this);
        setOneTouchExpandable(true);
        setDividerLocation(200);
        setLeftComponent(configTreeView);
    }

    /**
     * Gets currently associated DBModelTreeView.
     * 
     * @return current ConfigParamsTreeView instance.
     */
    public ConfigParamsTreeView getConfigModelTreeView() {
        return configTreeView;
    }
}
