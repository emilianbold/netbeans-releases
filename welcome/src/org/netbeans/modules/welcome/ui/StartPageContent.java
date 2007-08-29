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

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;

/**
 *
 * @author S. Aubrecht
 */
public class StartPageContent extends JPanel implements Constants {

    public StartPageContent() {
        super( new BorderLayout() );
        
        add( new TopBar(), BorderLayout.NORTH );
        add( new Tabs( BundleSupport.getLabel( "WelcomeTab" ), new WelcomeTab(), //NOI18N
                       BundleSupport.getLabel( "MyNetBeansTab"), new MyNetBeansTab()), //NOI18N
                       BorderLayout.CENTER  );
        
        setBackground( Utils.getColor( COLOR_SCREEN_BACKGROUND ) );
        setMinimumSize( new Dimension(START_PAGE_MIN_WIDTH,100) );
    }
}
