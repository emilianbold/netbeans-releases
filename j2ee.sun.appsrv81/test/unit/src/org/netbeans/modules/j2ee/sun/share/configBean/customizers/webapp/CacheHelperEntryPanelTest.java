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
 * CacheHelperEntryPanelTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 5:39 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.CacheHelper;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.WebProperty;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanInputDialog;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class CacheHelperEntryPanelTest extends TestCase {
    
    public void testCreate() {
        CacheHelperEntryPanel foo = 
            new CacheHelperEntryPanel();
        foo.requiredFieldsFilled();
    }
    
    public CacheHelperEntryPanelTest(java.lang.String testName) {
        super(testName);
    }
    
}
