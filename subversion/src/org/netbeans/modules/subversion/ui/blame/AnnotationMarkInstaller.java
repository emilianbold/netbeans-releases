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

package org.netbeans.modules.subversion.ui.blame;

import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;

/**
 * ErrorStripe SPI entry point registered at layer.
 *
 * @author Maros Sandor
 */
public final class AnnotationMarkInstaller implements MarkProviderCreator {
    
    private static final Object PROVIDER_KEY = new Object();

    public MarkProvider createMarkProvider(JTextComponent pane) {
        AnnotationMarkProvider amp = new AnnotationMarkProvider();
        pane.putClientProperty(PROVIDER_KEY, amp);
        return amp;
    }
    
    public static AnnotationMarkProvider getMarkProvider(JTextComponent pane) {
        return (AnnotationMarkProvider) pane.getClientProperty(PROVIDER_KEY);
    }
}
