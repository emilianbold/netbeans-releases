/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe.caret;

import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;

/**
 *
 * @author Jan Lahoda
 */
public class CaretMarkProviderCreator implements MarkProviderCreator {
    
    /**For test only!*/
    public static boolean switchOff = false;
    
    private static final boolean ENABLE = true;//Boolean.getBoolean("org.netbeans.modules.editor.errorstripe.caret");
    
    /** Creates a new instance of AnnotationMarkProviderCreator */
    public CaretMarkProviderCreator() {
    }

    public MarkProvider createMarkProvider(JTextComponent document) {
        if (ENABLE && !switchOff)
            return new CaretMarkProvider(document);
        else
            return null;
    }
    
}
