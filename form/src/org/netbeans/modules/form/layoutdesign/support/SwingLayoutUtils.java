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

package org.netbeans.modules.form.layoutdesign.support;

import java.util.*;

/**
 * Utilities for swing layout support.
 *
 * @author Jan Stola
 */
public class SwingLayoutUtils {
    /** The default resizability of the component is not known. */
    public static final int STATUS_UNKNOWN = -1;
    /** The component is not resizable by default. */
    public static final int STATUS_NON_RESIZABLE = 0;
    /** The component is resizable by default. */
    public static final int STATUS_RESIZABLE = 1;
    
    /**
     * Contains class names of non-resizable components e.g.
     * components that are non-resizable unless one (or more) of
     * minimumSize, preferredSize or maximumSize properties is changed.
     */
    private static Set nonResizableComponents = new HashSet();
    static {
        nonResizableComponents.addAll(
            Arrays.asList(new String[] {
                "javax.swing.JLabel", // NOI18N
                "javax.swing.JButton", // NOI18N
                "javax.swing.JToggleButton", // NOI18N
                "javax.swing.JCheckBox", // NOI18N
                "javax.swing.JRadioButton", // NOI18N
                "javax.swing.JList", // NOI18N
                "javax.swing.JToolBar" // NOI18N
            })
        );
    }

    /**
     * Contains class names of resizable components e.g.
     * components that are resizable unless one (or more) of
     * minimumSize, preferredSize or maximumSize properties is changed.
     */
    private static Set resizableComponents = new HashSet();
    static {
        resizableComponents.addAll(
            Arrays.asList(new String[] {
                "javax.swing.JComboBox", // NOI18N
                "javax.swing.JTextField", // NOI18N
                "javax.swing.JTextArea", // NOI18N
                "javax.swing.JPanel", // NOI18N
                "javax.swing.JTabbedPane", // NOI18N
                "javax.swing.JScrollPane", // NOI18N
                "javax.swing.JSplitPane", // NOI18N
                "javax.swing.JFormattedTextField", // NOI18N
                "javax.swing.JPasswordField", // NOI18N
                "javax.swing.JSpinner", // NOI18N
                "javax.swing.JSeparator", // NOI18N
                "javax.swing.JTextPane", // NOI18N
                "javax.swing.JEditorPane", // NOI18N
                "javax.swing.JInternalFrame", // NOI18N
                "javax.swing.JLayeredPane", // NOI18N
                "javax.swing.JDesktopPane" // NOI18N
            })
        );
    }

    /**
     * Determines whether the given class represents component
     * that is resizable (by default) or not.
     *
     * @param componentClass <code>Class</code> object corresponding
     * to component we are interested in.
     * @return <code>STATUS_RESIZABLE</code>, <code>STATUS_NON_RESIZABLE</code>
     * or <code>STATUS_UNKNOWN</code>.
     */
    public static int getResizableStatus(Class componentClass) {
        String className = componentClass.getName();
        if (resizableComponents.contains(className)) return STATUS_RESIZABLE;
        if (nonResizableComponents.contains(className)) return STATUS_NON_RESIZABLE;
        return STATUS_UNKNOWN;
    }

}
