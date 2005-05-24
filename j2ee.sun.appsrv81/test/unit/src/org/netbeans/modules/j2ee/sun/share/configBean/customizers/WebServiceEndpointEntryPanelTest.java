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
 * WebServiceEndpointEntryPanelTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanInputDialog;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class WebServiceEndpointEntryPanelTest extends TestCase {
    
    public void testCreate() {
        WebServiceEndpointEntryPanel foo =
            new WebServiceEndpointEntryPanel();
    }
    
    public WebServiceEndpointEntryPanelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(WebServiceEndpointEntryPanelTest.class);
        return suite;
    }
    
    
    
}
