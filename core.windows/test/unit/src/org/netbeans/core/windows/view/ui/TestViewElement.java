/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.*;
import org.netbeans.core.windows.view.ui.*;
       
import java.awt.Component;
import java.awt.Dimension;

/**
 * A dummy ViewElement implementation for MultiSplitPane testing.
 *
 * @author Stanislav Aubrecht
 */
class TestViewElement extends ViewElement {
    
    SplitTestComponent myComponent;
    int orientation;
    
    /** Creates a new instance of TestViewElement */
    public TestViewElement( int orientation, double resizeWeight ) {
        super( null, resizeWeight );
        this.myComponent = new SplitTestComponent();
        this.orientation = orientation;
    }
    
    public Component getComponent() {
        return myComponent;
    }
    
    /**
     * lets the visual components adjust to the current state.
     * @returns true if a change was performed.
     */
    public boolean updateAWTHierarchy(Dimension availableSpace) {
        return true;
    }
    
    int getSizeInSplit() {
        return orientation == JSplitPane.HORIZONTAL_SPLIT ? getComponent().getWidth() : getComponent().getHeight();
    }
    
    int getNonSplitSize() {
        return orientation != JSplitPane.HORIZONTAL_SPLIT ? getComponent().getWidth() : getComponent().getHeight();
    }

    void setMinSize( int minSize ) {
        myComponent.setMinimumSize( new Dimension( minSize, minSize ) );
    }
    
    private static class SplitTestComponent extends JComponent {
        public SplitTestComponent() {
            setMinimumSize( new Dimension( 0, 0 ) );
        }
    }
}
