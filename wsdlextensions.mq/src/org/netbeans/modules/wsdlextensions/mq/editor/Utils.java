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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Yet another convenience utility class.
 *
 * @author Noel.Ang@sun.com
 */
final class Utils {
    private static final Logger logger =
            Logger.getLogger(Utils.class.getName());

    private Utils() {
    }

    public static void dispatchToSwingThread(String name, Runnable runnable) {
        if (runnable != null) {
            if (name == null) {
                name = "(no name given)";
            }
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE,
                        "Wait for completion of Swing thread job " + name
                                + " interrupted.",
                        e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE,
                        "Execution of Swing thread job " + name
                                + " interrupted.",
                        e);
            }
        }
    }

    public static void equalizeSizes(JComponent[] components) {
        if (components != null) {
            int maxHeight = 0;
            int maxWidth = 0;
            for (JComponent component : components) {
                maxHeight = Math.max(component.getHeight(), maxHeight);
                maxWidth = Math.max(component.getWidth(), maxWidth);
            }
            Dimension dim = new Dimension(maxWidth, maxHeight);
            for (JComponent component : components) {
                component.setPreferredSize(dim);
                component.setSize(dim);
            }
        }
    }

    public static String safeString(String value) {
        if (value == null) {
            value = "";
        }
        return value.trim();
    }
}
