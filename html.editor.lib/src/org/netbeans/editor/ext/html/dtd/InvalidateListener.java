/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html.dtd;

/** The DTD can change or can disappead. Every class holding reference
 * to the DTD should listen for invalidates and release their old invalid
 * DTDs.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public interface InvalidateListener {

    /** Called on all registered listeners when any DTD changes.
     * @param evt The InvalidateEvent containing the informations
     * about changed DTD.
     */
    public void dtdInvalidated( InvalidateEvent evt );

}
