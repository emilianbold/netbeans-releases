/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
}
