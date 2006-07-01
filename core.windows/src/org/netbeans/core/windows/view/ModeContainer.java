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

