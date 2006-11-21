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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.model.ObjectScene;

/**
 * @author David Kaspar
 */
public class CycleObjectSceneFocusProvider implements CycleFocusProvider {

    public boolean switchPreviousFocus (Widget widget) {
        Scene scene = widget.getScene ();
        return scene instanceof ObjectScene  &&  switchFocus ((ObjectScene) scene, false);
    }

    public boolean switchNextFocus (Widget widget) {
        Scene scene = widget.getScene ();
        return scene instanceof ObjectScene  &&  switchFocus ((ObjectScene) scene, true);
    }

    @SuppressWarnings ("unchecked")
    private boolean switchFocus (ObjectScene scene, boolean forwardDirection) {
        Object object = scene.getFocusedObject ();
        Comparable identityCode = scene.getIdentityCode (object);

        Object bestObject = null;
        Comparable bestIdentityCode = null;

        if (identityCode != null) {
            for (Object o : scene.getObjects ()) {
                Comparable ic = scene.getIdentityCode (o);
                if (forwardDirection) {
                    if (identityCode.compareTo (ic) < 0) {
                        if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) > 0) {
                            bestObject = o;
                            bestIdentityCode = ic;
                        }
                    }
                } else {
                    if (identityCode.compareTo (ic) > 0) {
                        if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) < 0) {
                            bestObject = o;
                            bestIdentityCode = ic;
                        }
                    }
                }
            }
        }

        if (bestIdentityCode == null) {
            for (Object o : scene.getObjects ()) {
                Comparable ic = scene.getIdentityCode (o);
                if (forwardDirection) {
                    if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) > 0) {
                        bestObject = o;
                        bestIdentityCode = ic;
                    }
                } else {
                    if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) < 0) {
                        bestObject = o;
                        bestIdentityCode = ic;
                    }
                }
            }
        }

        scene.setFocusedObject (bestObject);
        return true;
    }

}
