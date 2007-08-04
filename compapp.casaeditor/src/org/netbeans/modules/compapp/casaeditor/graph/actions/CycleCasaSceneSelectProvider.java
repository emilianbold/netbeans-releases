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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.graph.CasaPinWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;

/**
 *
 * @author rdara
 */


public class CycleCasaSceneSelectProvider implements CycleFocusProvider {

    public boolean switchPreviousFocus (Widget widget) {
        Scene scene = widget.getScene ();
        return scene instanceof CasaModelGraphScene  &&  switchFocus ((CasaModelGraphScene) scene, false);
    }

    public boolean switchNextFocus (Widget widget) {
        Scene scene = widget.getScene ();
        return scene instanceof CasaModelGraphScene  &&  switchFocus ((CasaModelGraphScene) scene, true);
    }

    @SuppressWarnings ("unchecked")
    private boolean switchFocus (CasaModelGraphScene scene, boolean forwardDirection) {
        Object object = scene.getFocusedObject ();
        Comparable identityCode = scene.getIdentityCode (object);

        Object bestObject = null;
        Comparable bestIdentityCode = null;

        if (identityCode != null) {
            for (Object o : scene.getObjects ()) {
                if(scene.getIdentityCode (o) != null) {
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
        }

        
        if (bestIdentityCode == null) {
            for (Object o : scene.getObjects ()) {
                if(scene.getIdentityCode (o) != null) {
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
        }
        if(bestObject != null ) {
            scene.setFocusedObject (bestObject);
            Set selectedObjects = new HashSet();
            selectedObjects.add(bestObject);
            scene.setSelectedObjects(selectedObjects);
            Widget w = scene.findWidget(bestObject);
            scene.getView().scrollRectToVisible(w.convertLocalToScene(w.getBounds()));
            return true;
        } else {
            return false;
        }
        //return true;
    }
}
