/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view;


import org.openide.windows.TopComponent;

import java.awt.*;


/**
 * Class which represents access to GUI of mode.
 *
 * @author  Peter Zavadsky
 */
public interface ModeContainer {

    public ModeView getModeView();
    
    public Component getComponent();
    
    public void addTopComponent(TopComponent tc);
    
    public void removeTopComponent(TopComponent tc);
    
    public void setSelectedTopComponent(TopComponent tc);
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected);
    
    public TopComponent getSelectedTopComponent();
    
    public void setActive(boolean active);
    
    public boolean isActive();
    
    public void focusSelectedTopComponent();
    
    public TopComponent[] getTopComponents();
    
    public void updateName(TopComponent tc);
    
    public void updateToolTip(TopComponent tc);
    
    public void updateIcon(TopComponent tc);
    
    public void requestAttention(TopComponent tc);

    public void cancelRequestAttention(TopComponent tc);
}

