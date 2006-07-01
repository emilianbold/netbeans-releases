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

package org.netbeans.core.api.multiview;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.multiview.MultiViewCloneableTopComponent;
import org.netbeans.core.multiview.MultiViewTopComponent;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/** Factory class for handling multi views.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public final class MultiViews {

    /** Factory class, no instances. */
    private MultiViews() {
    }

    /**
     * For advanced manupulation with Multiview component, the handler can be requested
     * @return handle that one can use for manipulation with multiview component.
     */
    public static MultiViewHandler findMultiViewHandler(TopComponent tc) {
        if ( tc != null) {
            if (tc instanceof MultiViewTopComponent) {
                return new MultiViewHandler(((MultiViewTopComponent)tc).getMultiViewHandlerDelegate());
            }
            if (tc instanceof MultiViewCloneableTopComponent) {
                return new MultiViewHandler(((MultiViewCloneableTopComponent)tc).getMultiViewHandlerDelegate());
            }
        }
        return null;
    }
    
}
