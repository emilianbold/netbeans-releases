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


/**
 * Test cases for Feature.
 */
public class FeatureTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FeatureTestCase.class);
    }

    private IFeature    feat;
    private IClassifier clazz;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        clazz = createClass("Calliope");
        IOperation op = clazz.createOperation("int", "moo");
        clazz.addOperation(op);
        
        feat = op;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clazz.delete();
    }

    public void testSetFeaturingClassifier()
    {
        IClassifier featC = createClass("Feature");
        feat.setFeaturingClassifier(featC);
        assertEquals(featC.getXMIID(), 
            feat.getFeaturingClassifier().getXMIID());
    }
    
    public void testGetFeaturingClassifier()
    {
        // Tested by setFeaturingClassifier.
    }
    
    public void testSetIsStatic()
    {
        assertFalse(feat.getIsStatic());
        feat.setIsStatic(true);
        assertTrue(feat.getIsStatic());
        feat.setIsStatic(false);
        assertFalse(feat.getIsStatic());
    }
    
    public void testGetIsStatic()
    {
        // Tested by setIsStatic.
    }
    
    public void testGetQualifiedName2()
    {
        assertEquals("Calliope::moo", feat.getQualifiedName2());
    }
    
    public void testMoveToClassifier()
    {
        IClassifier other = createClass("Other");
        feat.moveToClassifier(other);
        
        assertEquals(0, clazz.getOwnedElementsByName("moo").size());
        assertEquals(1, other.getOwnedElementsByName("moo").size());
        assertEquals(feat.getXMIID(), 
            other.getOwnedElementsByName("moo").get(0).getXMIID());
        other.delete();
    }
    
    public void testDuplicateToClassifier()
    {
        IClassifier other = createClass("Other");
        feat.duplicateToClassifier(other);
        assertEquals(1, clazz.getOwnedElementsByName("moo").size());
        assertEquals(1, other.getOwnedElementsByName("moo").size());
        other.delete();
    }
    
    public void testDuplicateToClassifier2()
    {
        // ?? Why have this function at all?
        testDuplicateToClassifier();
    }
}
