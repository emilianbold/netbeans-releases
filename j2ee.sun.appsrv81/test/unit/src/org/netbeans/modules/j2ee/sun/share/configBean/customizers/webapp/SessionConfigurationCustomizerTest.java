/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SessionConfigurationCustomizerTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 3:24 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.beans.Customizer;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.enterprise.deploy.spi.DConfigBean;
import junit.framework.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.SessionConfig;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.SessionManager;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.ManagerProperties;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.StoreProperties;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.SessionProperties;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.CookieProperties;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.WebProperty;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;

/**
 *
 * @author vkraemer
 */
public class SessionConfigurationCustomizerTest extends TestCase {
    
    public void testCreate() {
	///	SessionConfigurationCustomizer foo = 
	///		new SessionConfigurationCustomizer();
    }
    
    public SessionConfigurationCustomizerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SessionConfigurationCustomizerTest.class);
        return suite;
    }
}
