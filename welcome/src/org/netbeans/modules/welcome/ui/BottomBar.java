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

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Logo;

/**
 *
 * @author S. Aubrecht
 */
class BottomBar extends JPanel {

    public BottomBar() {
        setLayout( new GridBagLayout() );
        setOpaque( false );
        
        add( Logo.createSunLogo(), new GridBagConstraints(0,0,1,1,0.0,0.0,
                GridBagConstraints.WEST,GridBagConstraints.VERTICAL,new Insets(0,12,6,5),0,0 ) );
        
        add( new JLabel(), new GridBagConstraints(1,0,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0 ) );
        
        add( new ShowNextTime(), new GridBagConstraints(2,0,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.VERTICAL,new Insets(0,0,6,0),0,0 ) );
        
            add( new JLabel(), new GridBagConstraints(3,0,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0 ) );
        add( Logo.createJavaLogo(), new GridBagConstraints(4,0,1,1,0.0,0.0,
                GridBagConstraints.EAST,GridBagConstraints.VERTICAL,new Insets(0,5,6,12),0,0 ) );
    }

}
