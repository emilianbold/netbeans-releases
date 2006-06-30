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
