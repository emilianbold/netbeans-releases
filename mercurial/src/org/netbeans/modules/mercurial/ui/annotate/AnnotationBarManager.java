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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.annotate;

import org.netbeans.editor.SideBarFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;


/**
 * @author Maros Sandor
 */
public class AnnotationBarManager implements SideBarFactory {

    private static final Object BAR_KEY = new Object();

    /**
     * Creates initially hidden annotations sidebar.
     * It's called once by target lifetime.
     */
    public JComponent createSideBar(JTextComponent target) {
        final AnnotationBar ab = new AnnotationBar(target);
        target.putClientProperty(BAR_KEY, ab);
        return ab;
    }

    /**
     * Shows annotations sidebar.
     */
    public static AnnotationBar showAnnotationBar(JTextComponent target) {
        AnnotationBar ab = (AnnotationBar) target.getClientProperty(BAR_KEY);
        assert ab != null: "#58828 reappeared!"; // NOI18N
        ab.annotate();
        return ab;
    }

    /**
     * Shows annotations sidebar.
     */
    public static void hideAnnotationBar(JTextComponent target) {
        if (target == null) return;
        AnnotationBar ab = (AnnotationBar) target.getClientProperty(BAR_KEY);
        assert ab != null: "#58828 reappeared!"; // NOI18N
        ab.hideBar();
    }

    /**
     * Tests wheteher given editor shows annotations.
     */
    public static boolean annotationBarVisible(JTextComponent target) {
        if (target == null) return false;
        AnnotationBar ab = (AnnotationBar) target.getClientProperty(BAR_KEY);
        if (ab == null) {
            return false;
        }
        return ab.getPreferredSize().width > 0;
    }
}

