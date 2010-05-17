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

package org.netbeans.modules.xml.xam.ui.category;

import javax.swing.Icon;
import javax.swing.JLabel;
import junit.framework.TestCase;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.Lookup;

/**
 * Tests DefaultCategoryPane class.
 *
 * @author Nathan Fiedler
 */
public class DefaultCategoryPaneTest extends TestCase {

    public DefaultCategoryPaneTest(String testName) {
        super(testName);
    }

    public void testAddSetGet() {
        DefaultCategoryPane pane = new DefaultCategoryPane();
        Category c1 = new TestCategory();
        Category c2 = new TestCategory();
        Category c3 = new TestCategory();
        pane.addCategory(c1);
        pane.addCategory(c2);
        pane.addCategory(c3);
        pane.setCategory(c1);
        assertTrue(pane.getCategory() == c1);
        pane.setCategory(c3);
        assertTrue(pane.getCategory() == c3);
        pane.setCategory(c2);
        assertTrue(pane.getCategory() == c2);
    }

    /**
     * Dummy category for the purpose of testing.
     */
    private static class TestCategory extends AbstractCategory {

        public void showComponent(Component component) {
        }

        public String getTitle() {
            return "";
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return "";
        }

        public java.awt.Component getComponent() {
            return new JLabel();
        }

        public void componentShown() {
        }

        public void componentHidden() {
        }
    }
}
