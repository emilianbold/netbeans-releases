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
package org.netbeans.modules.j2ee.sun.share.config.ui;

import org.openide.loaders.*;

/**
 * This interface provides a generic way to set Focus on a object represented in a ComponentPanel editor. It is
 * expected that the Open Support/OpenCookie/TopComponent for an specific ComponentPanel editor would implement this cookie and then delegate
 * to the ComponentPanel. Most ComponentPanels would then delegate to their child PanelView's. The PanelView's could then
 * delegate to individual panels that implement this interface or use some other mechanism to find the focusObject. The set focusObject's
 * supported by a particular PanelView or Panel is defined by that respective PanelView or Panel and should be documented by them.
 * The panelViewNameHint and panelNameHint can be used as hints to restrict the search or for disambiguation or to at least
 * put the focus on the appropriate panel even if the Object could not be resolved.
 * @author  bashby
 * @version 1.0
 */

public interface PanelFocusCookie  {
    /**
     * Request that the focus be set on the identified object using the name of the PanelView and/or panel as hints
     * to find the object.
     * @param String panelViewNameHint String used as a hint for the appropriate PanelView if there is more than one.
     * @param String panelNameHint String used as a hint for the appropiate panel in the PanelView
     * @param Object focusObject Object that can be used to identify the object that should have the focus.
     * @return boolean return true if the focus was able to be set on the identified Object
     */
    public boolean setFocusOn(String panelViewNameHint, String panelNameHint, Object focusObject);
}
