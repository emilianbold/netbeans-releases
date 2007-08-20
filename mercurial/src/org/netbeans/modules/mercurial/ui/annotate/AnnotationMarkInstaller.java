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

package org.netbeans.modules.mercurial.ui.annotate;

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
