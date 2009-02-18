/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.notifications.spi;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.notifications.spi.Notification.Priority;

/**
 *
 * @author Jan Becicka
 */
public class NotificationTest {

    public NotificationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of add method, of class Notification.
     */
    @Test
    public void testAdd() {
        Notifications.getDefault().clear();
        Notification norm = new NormNotification();
        norm.add();
        assertEquals(Notifications.getDefault().top(),norm);

        Notification low = new LowNotification();
        low.add();
        assertEquals(Notifications.getDefault().top(),norm);

        Notification high = new HighNotification();
        high.add();
        assertEquals(Notifications.getDefault().top(),high);
    }

    /**
     * Test of remove method, of class Notification.
     */
    @Test
    public void testRemove() {
        assertEquals(Notifications.getDefault().top().getTitle(), "High");
        Notifications.getDefault().top().remove();
        assertEquals(Notifications.getDefault().top().getTitle(), "Norm");
    }


    public static class HighNotification extends Notification {

        @Override
        public String getLinkTitle() {
            return "high";
        }

        @Override
        public String getTitle() {
            return "High";
        }

        @Override
        public String getDescription() {
            return "high";
        }

        @Override
        public void showDetails() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Priority getPriority() {
            return Priority.HIGH;
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon();
        }
    }

    public static class LowNotification extends Notification {

        @Override
        public String getLinkTitle() {
            return "low";
        }

        @Override
        public String getTitle() {
            return "Low";
        }

        @Override
        public String getDescription() {
            return "low";
        }

        @Override
        public void showDetails() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Priority getPriority() {
            return Priority.LOW;
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon();
        }
    }

    public static class NormNotification extends Notification {

        @Override
        public String getLinkTitle() {
            return "norm";
        }

        @Override
        public String getTitle() {
            return "Norm";
        }

        @Override
        public String getDescription() {
            return "norm";
        }

        @Override
        public void showDetails() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Priority getPriority() {
            return Priority.NORMAL;
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon();
        }
    }
}