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
