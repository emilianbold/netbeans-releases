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
package org.netbeans.modules.visualweb.css2;

import javax.swing.JViewport;


/** Root box to use as the initial containing block. We can't
 * use the PageBox itself, since the PageBox corresponds to the
 * <body> tag, and may have padding and margins - so the absolute
 * x and y positions may not be 0,0 - and the initial containing
 * block (which is what absolutely positioned boxes will use, unless
 * they are descendants of other absolutely positioned boxes) should
 * be relative to 0,0.
 * @author Tor Norbye.
 */
public class ViewportBox extends CssBox {
    private JViewport viewport;

    public ViewportBox(JViewport viewport, int width, int height) {
        super(null, null, BoxType.NONE, true, false);
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.viewport = viewport;
    }

    protected void initialize() {
        effectiveTopMargin = 0;
        effectiveBottomMargin = 0;
    }

    protected void initializeInvariants() {
    }

    public int getAbsoluteX() {
        return x;
    }

    public int getAbsoluteY() {
        return y;
    }

    public JViewport getViewport() {
        return viewport;
    }
}
