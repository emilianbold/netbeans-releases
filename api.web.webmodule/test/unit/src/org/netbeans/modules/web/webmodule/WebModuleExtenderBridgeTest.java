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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.webmodule;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.test.MockChangeListener;

/**
 *
 * @author Andrei Badea
 */
@SuppressWarnings("deprecation")
public class WebModuleExtenderBridgeTest extends NbTestCase {

    private WebModule webModule;
    private ExtenderController controller;

    @Override
    protected void setUp() {
        webModule = WebModuleFactory.createWebModule(new SimpleWebModuleImpl());
        controller = ExtenderController.create();
    }

    public WebModuleExtenderBridgeTest(String testName) {
        super(testName);
    }

    public void testBasic() {

        class PanelImpl implements FrameworkConfigurationPanel {

            private ChangeSupport changeSupport = new ChangeSupport(this);
            private WizardDescriptor wizard;
            private boolean forcedInvalid;

            public void enableComponents(boolean enable) {
            }

            public void addChangeListener(ChangeListener l) {
                changeSupport.addChangeListener(l);
            }

            public Component getComponent() {
                return new JPanel();
            }

            public HelpCtx getHelp() {
                return new HelpCtx("help me");
            }

            public boolean isValid() {
                boolean valid = "foo".equals(wizard.getProperty("prop")) && !forcedInvalid;
                wizard.putProperty("WizardPanel_errorMessage", valid ? " " : "Not valid");
                return valid;
            }

            public void readSettings(Object settings) {
                wizard = (WizardDescriptor) settings;
            }

            public void removeChangeListener(ChangeListener l) {
                changeSupport.removeChangeListener(l);
            }

            public void storeSettings(Object settings) {
            }

            void forceInvalid(boolean value) {
                forcedInvalid = value;
                changeSupport.fireChange();
            }
        }

        class FrameworkImpl extends AbstractFrameworkImpl {

            private PanelImpl panel;
            private boolean extendCalled;

            public FrameworkImpl(PanelImpl panel) {
                this.panel = panel;
            }

            @Override
            public FrameworkConfigurationPanel getConfigurationPanel(WebModule wm) {
                return panel;
            }

            @Override
            public Set extend(WebModule wm) {
                extendCalled = true;
                return new HashSet();
            }
        }

        PanelImpl panel = new PanelImpl();
        FrameworkImpl framework = new FrameworkImpl(panel);
        WebModuleExtender extender = framework.createWebModuleExtender(webModule, controller);
        MockChangeListener listener = new MockChangeListener();

        extender.update();
        extender.addChangeListener(listener);
        assertFalse(extender.isValid());
        assertEquals("Not valid", controller.getErrorMessage());

        assertTrue(extender.getComponent() instanceof JPanel);

        controller.getProperties().setProperty("prop", "foo");
        extender.update();
        assertTrue(extender.isValid());
        assertNull(controller.getErrorMessage());

        panel.forceInvalid(true);
        listener.assertEventCount(1);
        assertFalse(extender.isValid());
        assertEquals("Not valid", controller.getErrorMessage());

        panel.forceInvalid(false);
        listener.assertEventCount(1);
        assertTrue(extender.isValid());

        controller.getProperties().setProperty("prop", null);
        extender.update();
        assertFalse(extender.isValid());

        assertFalse(framework.extendCalled);
        extender.extend(webModule);
        assertTrue(framework.extendCalled);
    }

    @SuppressWarnings("deprecation")
    private static abstract class AbstractFrameworkImpl extends WebFrameworkProvider {

        AbstractFrameworkImpl() {
            super("name", "description");
        }

        public File[] getConfigurationFiles(WebModule wm) {
            return new File[0];
        }

        public boolean isInWebModule(WebModule wm) {
            return false;
        }
    }
}
