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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

/**
 * Implemented by those widgets that wish to expand and collapse under the
 * control of an <code>ExpanderWidget</code>. The implementation must make
 * the actual size change itself, when either of the collapse/expand
 * methods are invoked.
 *
 * @author  Nathan Fiedler
 */
public interface ExpandableWidget {

    /**
     * Perform whatever steps are necessary to collapse this expandable
     * widget. This may remove child widgets, or animate the preferred
     * bounds of the widget, using the provided expander.
     *
     * @param  expander  provides methods for animating size change.
     */
    void collapseWidget(ExpanderWidget expander);

    /**
     * Perform whatever steps are necessary to expand this expandable
     * widget. This may add child widgets, or animate the preferred
     * bounds of the widget, using the provided expander.
     *
     * @param  expander  provides methods for animating size change.
     */
    void expandWidget(ExpanderWidget expander);

    /**
     * Returns the object that can be used as a hashtable key. This is
     * utilized in the ExpanderWidget for preserving the expanded state
     * of widgets in the event that they are recreated, as in the case
     * of an undo/redo operation.
     *
     * @return  hashtable key.
     */
    Object hashKey();
    
    
    /**
     * Utility method to programmatically expand the widget.
     * This should be called when a widget needs to be expanded.
     *
     */
    void expandWidget();

    /**
     * Utility method to programmatically collapse the widget.
     * This should be called when a widget needs to be collapsed.
     *
     */
    void collapseWidget();
}
