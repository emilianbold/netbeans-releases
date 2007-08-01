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
package apichanges;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.border.BorderSupport;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * Test for issue #103456 - BorderSupport.getSwingBorder method introduced
 * @author David Kaspar
 */
public class SwingBorderGetterTest extends NbTestCase {

    public SwingBorderGetterTest (String name) {
        super (name);
    }

    public void testGetter () {
        Scene scene = new Scene ();
        BevelBorder originalBorder = new BevelBorder (BevelBorder.RAISED);
        scene.setBorder (originalBorder);
        Border foundBorder = BorderSupport.getSwingBorder (scene.getBorder ());
        assertEquals (originalBorder, foundBorder);
    }

}
