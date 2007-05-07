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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.navigation;

import java.awt.Graphics;
import javax.swing.JToolBar;

/**
 * ToolBar that doesn't paint any border.
 * 
 * @author S. Aubrecht
 */
public class NoBorderToolBar extends JToolBar {
    
    /** Creates a new instance of NoBorderToolbar */
    public NoBorderToolBar() {
    }
    
    /** Creates a new instance of NoBorderToolbar 
     * @param layout
     */
    public NoBorderToolBar( int layout ) {
        super( layout );
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    }
}
