/*
 *                 Sun Public License Notice
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
