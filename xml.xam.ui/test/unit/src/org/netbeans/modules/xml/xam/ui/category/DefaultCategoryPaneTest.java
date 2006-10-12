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
