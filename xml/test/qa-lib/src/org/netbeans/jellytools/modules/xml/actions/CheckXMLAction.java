/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.xml.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** CheckXMLAction class 
 * @author <a href="mailto:mschovanek@netbeans.org">Martin Schovanek</a> */
public class CheckXMLAction extends ActionNoBlock {

    private static final String popup = 
    Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.actions.Bundle", "NAME_Check_XML");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F9, KeyEvent.ALT_MASK);

    /** creates new CheckXMLAction instance */    
    public CheckXMLAction() {
        super(null, popup, "org.netbeans.modules.xml.tools.actions.CheckAction", shortcut);
    }
}