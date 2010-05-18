/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.rest.wadl.design.cookies;

import org.netbeans.modules.xml.xam.Component;
import org.openide.nodes.Node;

/**
 * ViewComponentCookie displays a xam component in a view, opening the
 * editor as necessary, and switching to the desired view, then
 * scrolling to or otherwise showing the component.
 *
 * @author Ajit Bhate
 * @author Todd Fast, todd.fast@sun.com
 * @author Nathan Fiedler
 */
public interface ViewComponentCookie extends Node.Cookie {

    /**
     * Indicates a type of view.
     */
    public enum View {
        /** Source "textual" view */
        SOURCE,
        /** Tree/column "structural" view */
        STRUCTURE,
        /** Schema/WSDL "design" view */
        DESIGN,
        /** Super-type "view" */
        SUPER,
        /** The currently showing view */
        CURRENT,
    }

    /**
     * Show the xam component in one view or another.
     *
     * @param  view        the view to be used.
     * @param  component   the xam component to show.
     * @param  parameters  the optional parameters for the viewer.
     */
    public void view(View view, Component component, Object... parameters);

    /**
     * Determines if referenced xam component can be view in given view.
     *
     * @param  view  the view to be used.
     * @return  true if referenced component can be viewed, false otherwise.
     */
    public boolean canView(View view, Component component);
}
