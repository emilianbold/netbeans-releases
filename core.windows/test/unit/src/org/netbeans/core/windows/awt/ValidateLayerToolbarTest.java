/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.awt;

import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.*;
import org.openide.loaders.*;

/** Checks the consistence of Toolbar folder.
 *
 * @author Jaroslav Tulach
 */
public class ValidateLayerToolbarTest extends ValidateLayerMenuTest {
    
    /** Creates a new instance of SFSTest */
    public ValidateLayerToolbarTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ValidateLayerToolbarTest.class);
        
        return suite;
    }
    
    //
    // override in subclasses
    //
    
    protected String rootName () {
        return "Toolbars";
    }
    
    /** Allowes to skip filest that are know to be broken */
    protected boolean skipFile (FileObject fo) {
        return false;
    }
    
    protected boolean correctInstance (Object obj) {
        if (obj instanceof javax.swing.Action) {
            return true;
        }
        if (obj instanceof org.openide.util.actions.Presenter.Toolbar) {
            return true;
        }
        if (obj instanceof javax.swing.JToolBar.Separator) {
            return true;
        }
        if (obj instanceof org.openide.awt.ToolbarPool.Configuration) {
            // definition of configuration
            return true;
        }
        if (obj instanceof java.awt.Component) {
            // definition of configuration
            return true;
        }
        
        return false;
    }

    //
    // Inherits test from superclass
    //
    
}

