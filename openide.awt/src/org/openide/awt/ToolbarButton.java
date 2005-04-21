/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.awt;

/**
 * An implementation of a toolbar button.
 * @deprecated This class was a workaround for JDK 1.2 era Windows Look and
 * feel issues.  All implementation code has been removed.  It is here only
 * for backward compatibility.
 */
public class ToolbarButton extends javax.swing.JButton {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6564434578524381134L;

    public ToolbarButton() {
    }

    public ToolbarButton(javax.swing.Icon icon) {
        super(icon);
    }

    public void processMouseEvent(java.awt.event.MouseEvent e) {
        super.processMouseEvent(e);
    }
}
