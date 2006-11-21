/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
