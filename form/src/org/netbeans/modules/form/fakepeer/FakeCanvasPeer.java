/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.CanvasPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeCanvasPeer extends FakeComponentPeer implements CanvasPeer
{
    FakeCanvasPeer(Canvas target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    //
    //
    //

    private class Delegate extends Component
    {
        // noop
    }
}
