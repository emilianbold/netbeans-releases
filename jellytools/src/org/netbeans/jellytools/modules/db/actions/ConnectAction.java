/*
 *                Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.db.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.Operator;


/** Used to call "Connect ..." popup menu item.
 * @see org.netbeans.jellytools.actions.Action
 * @author Martin.Schovanek@sun.com */
public class ConnectAction extends ActionNoBlock {
    
    /** creates new "Connect ..." action */
    public ConnectAction() {
        super(null, Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "Connect"));
        setComparator(new Operator.DefaultStringComparator(true, true));
    }
}
