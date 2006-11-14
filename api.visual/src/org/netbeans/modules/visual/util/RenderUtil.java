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
package org.netbeans.modules.visual.util;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author David Kaspar
 */
public class RenderUtil {

    private static final String javaVersion = System.getProperty ("java.version");
    private static final boolean java6 = javaVersion.startsWith ("1.6");

    public static void drawRect (Graphics2D gr, Rectangle rect) {
        if (java6)
            gr.drawRect (rect.x, rect.y, rect.width - 1, rect.height - 1);
        else
            gr.draw (new Rectangle2D.Double (rect.x + 0.5, rect.y + 0.5, rect.width - 1.0, rect.height - 1.0));
    }

}
