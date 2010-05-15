/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.widgets;

/**
 * This interface represents an ability to collapse and expand a widget. It is implemented by a VMDNodeWidget.
 *
 * @author David Kaspar
 */
public interface EDMMinimizeAbility {

    /**
     * Collapses the widget.
     */
    public void collapseWidget ();

    /**
     * Expands the widget.
     */
    public void expandWidget ();

}
