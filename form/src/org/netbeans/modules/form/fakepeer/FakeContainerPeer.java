/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ContainerPeer;

/**
 *
 * @author Tran Duc Trung
 */

abstract class FakeContainerPeer extends FakeComponentPeer implements ContainerPeer
{
    private Insets _insets;

    FakeContainerPeer(Container target) {
        super(target);
    }

    public Insets getInsets() {
        return insets();
    }

    public void beginValidate() {
    }

    public void endValidate() {
    }

    public Insets insets() {
        if(_insets == null)
            _insets = new Insets(0, 0, 0, 0);
        return _insets;
    }
}
