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

package org.netbeans.core.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;



/**
 * Delegate to be implemented by the MultiViewTopComponent
 */
public interface MultiViewElementCallbackDelegate {
    
    public void requestActive();
    
    public void requestVisible();
    
    public Action[] createDefaultActions();
    
    public void updateTitle(String title);
    
    public boolean isSelectedElement();
    
    public TopComponent getTopComponent();
    
}


