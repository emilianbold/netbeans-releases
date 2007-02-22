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

package org.netbeans.modules.xml.xam.ui.highlight;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A default implementation of HighlightManager.
 *
 * @author Nathan Fiedler
 */
public class DefaultHighlightManager extends HighlightManager {

    /**
     * Creates a new instance of DefaultHighlightManager.
     */
    public DefaultHighlightManager() {
    }

    protected void hideHighlights(HighlightGroup group) {
        hideOrShow(group, false);
        group.setShowing(false);
    }

    /**
     * Shows or hides the given group of highlights.
     *
     * @param  group  highlight group to show or hide.
     * @param  show   true to show, false to hide.
     */
    private void hideOrShow(HighlightGroup group, boolean show) {
        Map<Highlighted, List<Highlight>> map = findListeners(group);
        Set<Map.Entry<Highlighted, List<Highlight>>> entries = map.entrySet();
        Iterator<Map.Entry<Highlighted, List<Highlight>>> iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry<Highlighted, List<Highlight>> entry = iter.next();
            Highlighted listener = entry.getKey();
            List<Highlight> lights = entry.getValue();
            for (Highlight light : lights) {
                if (show) {
                    listener.highlightAdded(light);
                } else {
                    listener.highlightRemoved(light);
                }
            }
        }
    }

    protected void showHighlights(HighlightGroup group) {
        group.setShowing(true);
        hideOrShow(group, true);
    }

}
