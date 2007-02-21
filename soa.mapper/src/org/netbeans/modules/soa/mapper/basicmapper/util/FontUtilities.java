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

package org.netbeans.modules.soa.mapper.basicmapper.util;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;


public class FontUtilities {

    public static final String DEFAULT_FONT_FACE = "Dialog";

    public static final String[] FONT_FACES =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    private static List m_fontFacesAsList = null;


    public static String getSafeFontFace(String fontFace)
    {
        if (m_fontFacesAsList == null)
        {
            m_fontFacesAsList = Arrays.asList(FONT_FACES);
        }
        if (fontFace == null || m_fontFacesAsList.indexOf(fontFace) < 0)
        {
            // Font face not found. Use default.
            fontFace = DEFAULT_FONT_FACE;
        }
        return fontFace;
    }
}
