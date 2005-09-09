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
import javax.swing.Action;
import org.openide.util.HelpCtx;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddBusinessMethodAction extends AbstractAddMethodAction {
    
    public AddBusinessMethodAction() {
        super(new AddBusinessMethodStrategy());
    }
    
    public AddBusinessMethodAction(String name) {
        super(new AddBusinessMethodStrategy(name));
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
