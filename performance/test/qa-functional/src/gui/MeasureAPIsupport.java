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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.action.*;
import gui.menu.*;
import gui.window.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureAPIsupport  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        //actions
        //suite.addTest(new CreateProject("testCreateJavaApplicationProject", "Create Java Application project"));
        
        //dialogs
        suite.addTest(new NetBeansPlatformManager("measureTime", "NetBeans Platform Manager open"));
        suite.addTest(new ProjectPropertiesWindow_NBproject("measureTime", "NB Project Properties window open"));

        //menus
        suite.addTest(new ProjectsViewPopupMenu("testNBProjectNodePopupMenuProjects", "NB Project node popup in Projects View"));
        
        
        return suite;
    }
    
}
