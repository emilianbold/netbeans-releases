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
    
}
