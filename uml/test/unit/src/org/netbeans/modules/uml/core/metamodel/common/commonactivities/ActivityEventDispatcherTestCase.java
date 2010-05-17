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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 * Test cases for ActivityEventDispatcher.
 */
public class ActivityEventDispatcherTestCase extends AbstractUMLTestCase
    implements IActivityEdgeEventsSink
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityEventDispatcherTestCase.class);
    }

    private IActivityEventDispatcher dispatcher;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        dispatcher = new ActivityEventDispatcher();
        dispatcher.registerForActivityEdgeEvents(this);
    }
    
    public void testRevokeActivityEdgeSink()
    {
        dispatcher.revokeActivityEdgeSink(this);
        IActivityEdge edge = new ControlFlow();
        dispatcher.fireGuardModified(edge, null);
        assertFalse(onGuardModified);
    }

    public void testRegisterForActivityEdgeEvents()
    {
        // Tested by other event fire methods.
    }

    public void testFireGuardModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.fireGuardModified(edge, null);
        assertTrue(onGuardModified);
    }

    public void testFirePreGuardModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.firePreGuardModified(edge, "xyzzy", null);
        assertTrue(onPreGuardModified);
    }

    public void testFirePreWeightModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.firePreWeightModified(edge, "xyzzy", null);
        assertTrue(onPreWeightModified);
    }

    public void testFireWeightModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.fireWeightModified(edge, null);
        assertTrue(onWeightModified);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onPreWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreWeightModified(IActivityEdge pEdge, String newValue, IResultCell cell)
    {
        onPreWeightModified = true;
        assertNotNull(pEdge);
        assertNotNull(newValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWeightModified(IActivityEdge pEdge, IResultCell cell)
    {
        onWeightModified = true;
        assertNotNull(pEdge);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onPreGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreGuardModified(IActivityEdge pEdge, String newValue, IResultCell cell)
    {
        onPreGuardModified = true;
        assertNotNull(pEdge);
        assertNotNull(newValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onGuardModified(IActivityEdge pEdge, IResultCell cell)
    {
        onGuardModified = true;
        assertNotNull(pEdge);
        assertNotNull(cell);
    }
    private static boolean onGuardModified = false;
    private static boolean onPreGuardModified = false;
    private static boolean onPreWeightModified = false;
    private static boolean onWeightModified = false;
}
