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

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;

import org.netbeans.editor.SideBarFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Binds annotation action and editor sidebars support.
 *
 * <p>It's registered in module layer into
 * <tt>Editors/text/base/SideBar</tt> registry.
 *
 * @author Petr Kuzel
 */
public final class AnnotationBarManager implements SideBarFactory {

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
        return ab.getPreferredSize().width > 0;
    }
}
