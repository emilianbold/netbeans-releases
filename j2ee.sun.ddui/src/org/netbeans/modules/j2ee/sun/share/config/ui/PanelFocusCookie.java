/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
