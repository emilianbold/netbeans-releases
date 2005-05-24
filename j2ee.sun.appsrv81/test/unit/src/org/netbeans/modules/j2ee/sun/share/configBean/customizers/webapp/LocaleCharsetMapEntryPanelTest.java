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
 * LocaleCharsetMapEntryPanelTest.java
 * JUnit based test
 *
 * Created on March 15, 2004, 5:46 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.SortedMap;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import java.util.Locale;
import java.nio.charset.Charset;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanInputDialog;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class LocaleCharsetMapEntryPanelTest extends TestCase {
    
    public void testCreate() {
        LocaleCharsetMapEntryPanel foo = new LocaleCharsetMapEntryPanel();
    }
    
    public LocaleCharsetMapEntryPanelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LocaleCharsetMapEntryPanelTest.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
