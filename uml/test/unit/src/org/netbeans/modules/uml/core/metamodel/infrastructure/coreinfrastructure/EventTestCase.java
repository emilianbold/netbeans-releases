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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for Event.
 */
public class EventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EventTestCase.class);
    }

    private IEvent event;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        // We currently have no concrete (XMI complete) class we can use, so we 
        // fake one.
        event = (IEvent)FactoryRetriever.instance().createType("Event", null);
//        {
//            public void establishNodePresence(Document doc, Node parent)
//            {
//                buildNodePresence("UML:Event",doc,parent);
//            }
//        };
//        event.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (event != null)
		{
			project.addElement(event);
		}
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
		if (event == null)
		{
			return;
		}
        project.removeElement(event);
        event.delete();
    }

    public void testAddArgument()
    {
		if (event == null)
		{
			return;
		}
        IValueSpecification val = factory.createExpression(null);
        event.addArgument(val);
        
        ETList<IValueSpecification> vals = event.getArguments();
        assertEquals(1, vals.size());
        assertEquals(val.getXMIID(), vals.get(0).getXMIID());
    }
    
    public void testRemoveArgument()
    {
		if (event == null)
		{
			return;
		}
        testAddArgument();
        event.removeArgument(event.getArguments().get(0));
        assertEquals(0, event.getArguments().size());
    }
    
    public void testGetArguments()
    {
        // Tested by testAddArgument and testRemoveArgument
    }
}
