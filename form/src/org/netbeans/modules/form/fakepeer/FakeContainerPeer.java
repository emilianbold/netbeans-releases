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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.fakepeer;

import java.awt.*;

/**
 *
 * @author Tran Duc Trung
 */
abstract class FakeContainerPeer extends FakeComponentPeer
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

    // JDK 1.4
    public void beginLayout() {
    }

    // JDK 1.4
    public void endLayout() {
    }

    // JDK 1.4
    public boolean isPaintPending() {
        return false;
    }

    // JDK 1.5
    public void cancelPendingPaint(int x, int y, int w, int h) {
    }

    // JDK 1.5
    public void restack() {
    }

    // JDK 1.5
    public boolean isRestackSupported() {
        return false;
    }

    // deprecated
    public Insets insets() {
        if (_insets == null)
            _insets = new Insets(0, 0, 0, 0);
        return _insets;
    }
}
