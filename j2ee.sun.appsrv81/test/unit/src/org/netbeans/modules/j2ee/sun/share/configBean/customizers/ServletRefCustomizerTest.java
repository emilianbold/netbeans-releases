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
 * ServletRefCustomizerTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 3:58 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.beans.Customizer;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.j2ee.sun.common.dd.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.share.configbean.ServletRef;
import org.netbeans.modules.j2ee.sun.share.configbean.ServletVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.ErrorMessageDB;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerErrorPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class ServletRefCustomizerTest extends TestCase {
    
    public void testCreate() {
        ServletRefCustomizer foo = 
            new ServletRefCustomizer();
    }
    
    public ServletRefCustomizerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ServletRefCustomizerTest.class);
        return suite;
    }
    
    
}
