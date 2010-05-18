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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 */
public class BehavioralFeatureTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(BehavioralFeatureTestCase.class);
    }
    
    private IBehavioralFeature feat;
    private IClass clazz;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        clazz = createClass("Mohave");
        IOperation oper = clazz.createOperation("int", "desert");
        clazz.addOperation(oper);
        feat = oper;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        project.removeOwnedElement(clazz);
        clazz.delete();
    }
    
    private IBehavior createBehavior(String name) {
        IBehavior behavior = (IBehavior)FactoryRetriever.instance().createType("Procedure", null);
        //behavior.prepareNode(DocumentFactory.getInstance().createElement(""));
        behavior.setName(name);
        
        return behavior;
    }
    
    public void testIsFormalSignatureSame()
    {
		IClass clazb = createClass("aztec");
        IOperation walnut = clazb.createOperation("char", "desert");
        clazb.addOperation(walnut);
        
        testSetFormalParameters();
        
        IBehavioralFeature temp = feat;
        feat = walnut;
        testSetFormalParameters();
        
        assertTrue(feat.isFormalSignatureSame(temp));
        
        clazz.removeOwnedElement(walnut);
        walnut.delete();
        
        feat = temp;
    }
    
    public void testIsSignatureSame()
    {
        feat.removeAllParameters();
        
		IClass clazb = createClass("aztec");
        IOperation walnut = clazb.createOperation("char", "desert");
        clazb.addOperation(walnut);
        walnut.removeAllParameters();

        testSetFormalParameters();

        IBehavioralFeature temp = feat;
        feat = walnut;
        testSetFormalParameters();
        
        assertTrue(feat.isSignatureSame(temp));
        
        clazz.removeOwnedElement(walnut);
        walnut.delete();
        
        feat = temp;
    }

    public void testSetConcurrency()
    {
        feat.setConcurrency(BaseElement.CCK_GUARDED);
        assertEquals(BaseElement.CCK_GUARDED, feat.getConcurrency());
        feat.setConcurrency(BaseElement.CCK_SEQUENTIAL);
        assertEquals(BaseElement.CCK_SEQUENTIAL, feat.getConcurrency());
    }
    
    public void testGetConcurrency()
    {
        // Tested by setConcurrency.
    }
    
    public void testSetFormalParameters()
    {
        IParameter par1 = feat.createParameter("float", "x");
        IParameter par2 = feat.createParameter("char", "zodiac");
        ETList<IParameter> pars = new ETArrayList<IParameter>();
        pars.add(par1);
        pars.add(par2);
        
        feat.setFormalParameters(pars);
        
        ETList<IParameter> fpars = feat.getFormalParameters();
        assertNotNull(fpars);
        assertEquals(pars.size(), fpars.size());
        assertEquals(par1.getXMIID(), fpars.get(0).getXMIID());
        assertEquals(par2.getXMIID(), fpars.get(1).getXMIID());
    }
    
    public void testGetFormalParameters()
    {
        // Tested by setFormalParameters.
    }
    
    public void testRemoveAllParameters()
    {
        testSetFormalParameters();
        feat.removeAllParameters();
        ETList<IParameter> pars = feat.getParameters();
        assertEquals(0, pars.size());
    }
    
    public void testAddHandledSignal()
    {
        ISignal sig = factory.createSignal(project);
        project.addElement(sig);
        feat.addHandledSignal(sig);
        
        ETList<ISignal> sigs = feat.getHandledSignals();
        assertNotNull(sigs);
        assertEquals(1, sigs.size());
        assertEquals(sig.getXMIID(), sigs.get(0).getXMIID());
    }
    
    public void testRemoveHandledSignal()
    {
        testAddHandledSignal();
        feat.removeHandledSignal(feat.getHandledSignals().get(0));
        assertEquals(0, feat.getHandledSignals().size());
    }
    
    public void testGetHandledSignals()
    {
        // Tested by testAddHandledSignal
    }
    
    public void testSetIsAbstract()
    {
        feat.setIsAbstract(true);
        assertTrue(feat.getIsAbstract());
        feat.setIsAbstract(false);
        assertFalse(feat.getIsAbstract());
    }
    
    public void testGetIsAbstract()
    {
        // Tested by setIsAbstract.
    }
    
    public void testSetIsNative()
    {
        feat.setIsNative(true);
        assertTrue(feat.getIsNative());
        feat.setIsNative(false);
        assertFalse(feat.getIsNative());
    }
    
    public void testGetIsNative()
    {
        // Tested by setIsNative.
    }
    
    public void testSetIsStrictFP()
    {
        feat.setIsStrictFP(true);
        assertTrue(feat.getIsStrictFP());
        feat.setIsStrictFP(false);
        assertFalse(feat.getIsStrictFP());
    }
    
    public void testGetIsStrictFP()
    {
        // Tested by setIsStrictFP.
    }
    
    public void testAddMethod()
    {
        IBehavior meth = createBehavior("xyzzy");
        project.addElement(meth);
        feat.addMethod(meth);
        
        ETList<IBehavior> meths = feat.getMethods();
        assertNotNull(meths);
        assertEquals(1, meths.size());
        assertEquals(meth.getXMIID(), meths.get(0).getXMIID());
    }
    
    public void testRemoveMethod()
    {
        testAddMethod();
        feat.removeMethod(feat.getMethods().get(0));
        assertEquals(0, feat.getMethods().size());
    }
    
    public void testGetMethods()
    {
        // Tested by testAddMethod()
    }
    
    public void testCreateParameter()
    {
        IParameter par;
        assertNotNull(par = feat.createParameter("int", "zappo"));
        assertEquals("zappo", par.getName());
        
        feat.addParameter(par);
        assertEquals("int", par.getTypeName());
    }
    
    public void testAddParameter()
    {
        IParameter par = feat.createParameter("int", "zappo");
        feat.addParameter(par);
        
        ETList<IParameter> pars = feat.getParameters();
        assertEquals(2, pars.size());
        assertEquals(par.getXMIID(), pars.get(1).getXMIID());
    }
    
    public void testInsertParameter()
    {
        // Strip the return type.
        feat.removeAllParameters();
        
        testSetFormalParameters();
        
        IParameter par = feat.createParameter("double", "igor");
        feat.insertParameter(feat.getParameters().get(0), par);
        assertEquals(3, feat.getParameters().size());
        assertEquals(par.getXMIID(), feat.getParameters().get(0).getXMIID());
        
        par = feat.createParameter("byte", "probono");
        feat.insertParameter(null, par);
        assertEquals(4, feat.getParameters().size());
        assertEquals(par.getXMIID(), feat.getParameters().get(3).getXMIID());
        
        par = feat.createParameter("boolean", "yellowstone");
        feat.insertParameter(feat.getParameters().get(2), par);
        assertEquals(5, feat.getParameters().size());
        assertEquals(par.getXMIID(), feat.getParameters().get(2).getXMIID());
    }
    
    public void testRemoveParameter()
    {
        feat.removeAllParameters();
        testSetFormalParameters();
        
        feat.removeParameter(feat.getParameters().get(0));
        assertEquals(1, feat.getParameters().size());
    }
    
    public void testCreateParameter2()
    {
        IClass tc = createClass("Indianapolis");
        IParameter par = feat.createParameter2(tc, "indy");
        assertNotNull(par);
        assertEquals("indy", par.getName());
        
        feat.addParameter(par);
        assertEquals("Indianapolis", par.getTypeName());
        assertEquals(tc.getXMIID(), par.getType().getXMIID());
    }
    
    public void testCreateParameter3()
    {
        IParameter par = feat.createParameter3();
        assertNotNull(par);
    }
    
    public void testSetParameters()
    {
        IParameter par0 = feat.createParameter("int", null);
        IParameter par1 = feat.createParameter("float", "x");
        IParameter par2 = feat.createParameter("char", "zodiac");
        ETList<IParameter> pars = new ETArrayList<IParameter>();
        pars.add(par0);
        pars.add(par1);
        pars.add(par2);
        
        feat.setParameters(pars);
        
        ETList<IParameter> fpars = feat.getParameters();
        assertNotNull(fpars);
        assertEquals(pars.size(), fpars.size());
        assertEquals(par0.getTypeName(), fpars.get(0).getTypeName());
        assertEquals(par1.getXMIID(), fpars.get(1).getXMIID());
        assertEquals(par2.getXMIID(), fpars.get(2).getXMIID());
    }
    
    public void testGetParameters()
    {
        // Tested by setParameters.
    }
    
    public void testAddRaisedSignal()
    {
        ISignal sig = factory.createSignal(null);
        project.addElement(sig);
        feat.addRaisedSignal(sig);
        
        assertEquals(1, feat.getRaisedSignals().size());
        assertEquals(sig.getXMIID(), feat.getRaisedSignals().get(0).getXMIID());
    }
    
    public void testRemoveRaisedSignal()
    {
        testAddRaisedSignal();
        feat.removeRaisedSignal(feat.getRaisedSignals().get(0));
        assertEquals(0, feat.getRaisedSignals().size());
    }
    
    public void testGetRaisedSignals()
    {
        // Tested by preceding methods.
    }
    
    public void testSetRepresentation()
    {
        IBehavior beh = createBehavior("zack");
        project.addElement(beh);
        feat.setRepresentation(beh);
        assertNotNull(feat.getRepresentation());
        assertEquals(beh.getXMIID(), feat.getRepresentation().getXMIID());
    }
    
    public void testGetRepresentation()
    {
        // Tested by setRepresentation.
    }
    
    public void testCreateReturnType()
    {
        IParameter par = feat.createReturnType();
        assertNotNull(par);
    }
    
    public void testSetReturnType2()
    {
        createClass("Chilblain");
        feat.setReturnType2("Chilblain");
        assertEquals("Chilblain", feat.getReturnType2());
    }
    
    public void testGetReturnType2()
    {
        // Tested by setReturnType2.
    }
    
    public void testSetReturnType()
    {
        IParameter par = feat.createReturnType();
        
        IClass c = createClass("Lake");
        par.setType(c);
        feat.setReturnType(par);
        
        assertNotNull(feat.getReturnType());
        assertEquals(c.getXMIID(), par.getType().getXMIID());
    }
    
    public void testGetReturnType()
    {
        // Tested by setReturnType.
    }
}
