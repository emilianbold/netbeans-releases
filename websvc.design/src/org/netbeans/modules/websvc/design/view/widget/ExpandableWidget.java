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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.widget;

/**
 * Implemented by those widgets that wish to expand and collapse under the
 * control of an <code>ExpanderWidget</code>. The implementation must make
 * the actual size change itself, when either of the collapse/expand
 * methods are invoked.
 *
 * @author Ajit Bhate
 * @author  Nathan Fiedler
 */
public interface ExpandableWidget {

    /**
     * Perform whatever steps are necessary to collapse this expandable
     * widget. This may remove child widgets, or animate the preferred
     * bounds of the widget, using the provided expander.
     *
     */
    void collapseWidget();

    /**
     * Perform whatever steps are necessary to expand this expandable
     * widget. This may add child widgets, or animate the preferred
     * bounds of the widget, using the provided expander.
     *
     */
    void expandWidget();

    /**
     * Returns the object that can be used as a hashtable key. This is
     * utilized in the ExpanderWidget for preserving the expanded state
     * of widgets in the event that they are recreated, as in the case
     * of an undo/redo operation.
     *
     * @return  hashtable key.
     */
    Object hashKey();
}
