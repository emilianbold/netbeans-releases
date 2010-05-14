/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.edm.editor.graph.components;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JSplitPane;

/**
 *
 * @author karthikeyan s
 */
public class MashupTopPanel extends JSplitPane {
    
    /** Creates a new instance of MashupTopPanel */
    public MashupTopPanel() {
        this.setOneTouchExpandable(true);
        this.setOrientation(VERTICAL_SPLIT);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int divLocation = d.height * 3 / 5;
        this.setDividerLocation(divLocation);        
    }
}