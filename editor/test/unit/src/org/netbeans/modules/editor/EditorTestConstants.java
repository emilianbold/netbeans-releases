/*
 *                          Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the LaTeX module.
 * The Initial Developer of the Original Code is Jan Lahoda.
 * Portions created by Jan Lahoda_ are Copyright (C) 2002-2004.
 * All Rights Reserved.
 *
 * Contributor(s): Jan Lahoda.
 */
package org.netbeans.modules.editor;

import java.net.URL;


/**
 * Various constants.
 *
 * @author Miloslav Metelka
 */
public final class EditorTestConstants {
    
    public static final String EDITOR_LAYER = "org/netbeans/modules/editor/resources/layer.xml";
    public static final URL EDITOR_LAYER_URL
            = EditorTestConstants.class.getClassLoader().getResource(
            "org/netbeans/modules/editor/resources/layer.xml");
    
    private EditorTestConstants() {
    }
    
}
