/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.search.api;

import javax.swing.Icon;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.25
 */
public interface SearchElement {

    /**
     * Returns name of element.
     * @return name of element
     */
    String getName();

    /**
     * Returns tool tip of element.
     * @return tool tip of element
     */
    String getToolTip();

    /**
     * Returns icon of element.
     * @return icon of element
     */
    Icon getIcon();

    /**
     * Returns parent of element.
     * @return parent of element
     */
    SearchElement getParent();

    /**
     * Goes to the source of element.
     */
    void gotoSource();

    /**
     * Goes to the visual of element.
     */
    void gotoVisual();

    /**
     * Returns true if element is deleted.
     * @return true if element is deleted
     */
    boolean isDeleted();

    /**
     * Highlights element.
     */
    void highlight();

    /**
     * Unhighlights element.
     */
    void unhighlight();

    // --------------------------------------------
    public class Adapter implements SearchElement {

        public Adapter(String name, String toolTip, Icon icon, SearchElement parent) {
            myName = name;
            myToolTip = toolTip;
            myIcon = icon;
            myParent = parent;
        }

        public String getName() {
            return myName;
        }

        public String getToolTip() {
            return myToolTip;
        }

        public Icon getIcon() {
            return myIcon;
        }

        public SearchElement getParent() {
            return myParent;
        }

        public boolean isDeleted() {
            return false;
        }

        public void gotoSource() {
        }

        public void gotoVisual() {
        }

        public void highlight() {
        }

        public void unhighlight() {
        }

        @Override
        public String toString() {
            return getName();
        }

        private Icon myIcon;
        private String myName;
        private String myToolTip;
        private SearchElement myParent;
    }
}
