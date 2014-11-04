/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.templates;

import java.awt.Component;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javax.swing.JComponent;
import static junit.framework.Assert.*;
import org.junit.Test;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public class HTMLTemplateTest {
    @TemplateRegistration(
        folder = "Test", iconBase = "org/netbeans/modules/templates/x.png",
        page = "org/netbeans/modules/templates/x.html"
    )
    static String myMethod() {
        return "init()";
    }
    
    @Test public void checkTheIterator() throws Exception {
        final String path = "Templates/Test/org-netbeans-modules-templates-HTMLTemplateTest-myMethod";
        FileObject fo = FileUtil.getConfigFile(path);
        assertNotNull(fo);
        
        DataObject obj = DataObject.find(fo);
        
        TemplateWizard.Iterator it = TemplateWizard.getIterator(obj);
        assertNotNull("Iterator found", it);
        
        WizardDescriptor.Panel<WizardDescriptor> p1 = it.current();
        assertNotNull("Panel found", p1);
        assertTrue("It is HTML wizard: " + p1, p1 instanceof HTMLPanel);
        
        Component cmp1 = p1.getComponent();
        assertNotNull("component initialized", cmp1);
        
        while (!p1.isValid()) {
            awaitFX();
        }
        assertTrue("error code set to 0", p1.isValid());
        
        assertSelectedIndex("Zero th panel is selected", cmp1, 0);
        
        assertSteps("There steps", cmp1, "One", "Two", "Three");
        
        assertCurrentStep((HTMLPanel)p1, "One");
    }
    
    private static void awaitFX() throws Exception {
        final CountDownLatch cdl = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        });
        cdl.await();
    }
    
    private static void assertSelectedIndex(String msg, Component c, int index) {
        Object selIndex = ((JComponent)c).getClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX);
        assertTrue(msg + selIndex, selIndex instanceof Number);
        assertEquals(msg, index, ((Number)selIndex).intValue());
    }

    private static void assertSteps(String msg, Component c, Object... arr) {
        Object obj = ((JComponent)c).getClientProperty(WizardDescriptor.PROP_CONTENT_DATA);
        assertTrue(msg + " it is array: " + obj, obj instanceof Object[]);
        Object[] real = (Object[]) obj;
        assertEquals(msg + " same size", arr.length, real.length);
        assertEquals(msg, Arrays.asList(arr), Arrays.asList(real));
    }
    
    private static void assertCurrentStep(HTMLPanel p, String name) throws Exception {
        Object value = p.evaluateProp("current");
        assertEquals("Current step is set properly", name, value);
    }
}
