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
 * ManagedObjectTest.java
 * JUnit based test
 *
 * Created on April 16, 2004, 4:34 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.text.MessageFormat;
import javax.management.*;
import javax.swing.AbstractAction;
import junit.framework.*;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.execution.NbClassPath;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.ide.editors.BooleanEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor;
import org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.*;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;

/**
 *
 * @author vkraemer
 */
public class ManagedObjectTest extends TestCase {
    
    public void testCreateClasspathArray() {
        String [] retVal = ManagedObject.createClasspathArray("/a/b/c:/x/y/z");
        assertNotNull(retVal);
        assertEquals(2, retVal.length);
        assertEquals("/a/b/c",retVal[0]);
        assertEquals("/x/y/z",retVal[1]);
    }
    
    public void testToPumpCoverage() {
        Set s = ManagedObject.moduleTypes;
        Map foo = ManagedObject.getIconBases();
        foo = ManagedObject.getToolTips();
        foo = ManagedObject.getHelpIDs();
        foo = ManagedObject.getPropertiesHelpIDs();
        foo = ManagedObject.standardOpsStart;
        foo = ManagedObject.standardOpsStop;
        foo = ManagedObject.standardOpsResources;
        Object bar = ManagedObject.getObjectValue("123",Integer.class);
        Class t = ManagedObject.getSupportedType("int");
    }
    
    public ManagedObjectTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ManagedObjectTest.class);
        return suite;
    }
    
    
}
