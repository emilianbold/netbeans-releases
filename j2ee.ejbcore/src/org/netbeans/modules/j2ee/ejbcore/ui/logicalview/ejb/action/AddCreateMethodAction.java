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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;
import org.openide.util.HelpCtx;

/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddCreateMethodAction extends AbstractAddMethodAction {
    
    public AddCreateMethodAction() {
        super(new AddCreateMethodStrategy());
    }
    
    public AddCreateMethodAction(String name) {
        super(new AddCreateMethodStrategy(name));
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(AddBusinessMethodAction.class);
    }
    
}
