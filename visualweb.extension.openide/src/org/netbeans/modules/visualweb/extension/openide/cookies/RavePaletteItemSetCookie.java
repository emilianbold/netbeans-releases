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

package org.netbeans.modules.visualweb.extension.openide.cookies;

// Copied from openide/src/org/openide/cookies, it was a wrong place for this.
import org.openide.nodes.Node;

/** Cookie used to communicate that a node has the capability
 * to export a PaletteItem to the drag clipboard (without actually
 * constructing the transferable).  Used to quickly check whether
 * a set of nodes represent a potential drop operation into the
 * designer when dnd is not actually in effect.
 *
 * @todo This cookie should have a second method which actually
 *   returns the palette item set, instead of clients having to
 *   using the dnd stuff to locate the set.  I didn't do that
 *   yet because this requires PaletteItemSet to move from toolbox
 *   into openide (or using Object as a return type with a required
 *   cast) so we'll revisit this after TP.
 *
 * @author Tor Norbye
 */
public interface RavePaletteItemSetCookie extends Node.Cookie {
    /**
     * Report whether any palette items are available, without actually
     * creating them.
     * @return true if there are palette items in the set, false
     *   if the set is empty.
     */
    public boolean hasPaletteItems();

    /**
     * If hasPaletteItems is true, this method may provide an array
     * of class names for beans included in the palette items.
     * These may be used during drag & drop operations to decide
     * if the palette items can be dropped on top of other components.
     * These are the same classes that if the PaletteItems are
     * BeanPaletteItems, getBeanClassName() would return.
     */
    public String[] getClassNames();
}
