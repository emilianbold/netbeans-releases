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
import java.util.Collection;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Test cases for Classifier.
 *
 */
public class ClassifierTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassifierTestCase.class);
    }
    
    private static IClass clazz = null;

    protected void setUp() throws Exception {
        super.setUp();
        clazz = createClass("FirstClass");
        clazz.addSourceFile("D:\\temp\\FirstClass.java");
        assertNotNull(clazz);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        project.removeElement(clazz);
        clazz.delete();
    }

    /**
     * Test whether this method finds the operation with the matching signature.
     * We are here using gloabalOper, which was already created through 
     * testCreateOperation() method.    
     */
    public void testFindMatchingOperation()
    {
        IOperation tempOper = null;

        IClass other = createClass("Other");
        IOperation created = other.createOperation("String", "operation1");
        other.addOperation(created);
        
        IOperation oper = clazz.createOperation("String", "operation1");
        clazz.addOperation(oper);

        assertNotNull(oper);
        tempOper = clazz.findMatchingOperation(created);
        
        //assertNotNull(tempOper);
        //assertEquals(tempOper.getName(),created.getName());   
    }
    
    public void testFindMatchingParentOperation()
    {
        IClassifier superC = createClass("BigChiefSittingBull");
        IOperation oper = superC.createOperation("float", "angst");
        superC.addOperation(oper);
        
        IGeneralization gen = factory.createGeneralization(null);
        clazz.addGeneralization(gen);
        superC.addSpecialization(gen);
        
        IOperation parOp = clazz.findMatchingParentOperation(oper, false);
        assertNotNull(parOp);
        assertEquals(oper.getXMIID(), parOp.getXMIID());
    }
    
    
    public void testGetDefaultTypeValue()
    {
        IAttribute a = clazz.createAttribute("long", "paddy");
        clazz.addAttribute(a);
        assertEquals("0L", a.getType().getDefaultTypeValue());
    }
    
    public void testGetRedefiningFeatures()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IAttribute attr = parent.createAttribute("int", "a");
        parent.addAttribute(attr);
        
        IAttribute cat = clazz.createAttribute("int", "a");
        clazz.addAttribute(cat);
        attr.addRedefiningElement(cat);
        cat.addRedefinedElement(attr);

        IOperation oper = parent.createOperation("int", "a");
        parent.addOperation(oper);
        
        IOperation red = clazz.createOperation("int", "a");
        clazz.addOperation(red);
        oper.addRedefiningElement(red);
        red.addRedefinedElement(oper);

        ETList<INamedCollection> coll = clazz.getRedefiningFeatures();
        assertEquals(1, coll.size());
        assertEquals(cat.getXMIID(),
            ((new ETArrayList<IFeature>((Collection)coll.get(0).getData())).get(0).getXMIID()));
        assertEquals(red.getXMIID(), 
            ((new ETArrayList<IFeature>((Collection)coll.get(0).getData())).get(1).getXMIID()));
    }
    
    public void testAddAttribute()
    {
        // Create an attribute
        IAttribute attr = clazz.createAttribute("int", "x");
        clazz.addAttribute(attr);
        assertNotNull(attr = clazz.getAttributeByName("x"));
        assertEquals("int", attr.getTypeName());
    }
    
    public void testGetAttributes()
    {
        testAddAttribute();
        ETList<IAttribute> atts = clazz.getAttributes();
        assertNotNull(atts);
        assertEquals(1, atts.size());
    }

    public void testCreateAttribute()
    {
        String attrName = "attribute1";
        
        // Create int attribute1
        IAttribute attr = clazz.createAttribute("int", attrName);
		clazz.addAttribute(attr);
        assertNotNull(attr);

        // Is the name what we want?
        assertEquals(attrName, attr.getName());

        assertEquals("int", attr.getTypeName());
    }

    public void testCreateAttribute2()
    {
        IClassifier minx = createClass("Jimmy");
        IAttribute attr = clazz.createAttribute2(minx, "bart");
        assertNotNull(attr);
        assertEquals("bart", attr.getName());
        
        clazz.addAttribute(attr);
        assertEquals(minx.getXMIID(), attr.getType().getXMIID());
    }
    
    public void testCreateAttribute3()
    {
        IAttribute attr = clazz.createAttribute3();
        assertNotNull(attr);
    }
    
    /**
     * Create a constructor and check whether its not null and its name is equal to
     * the class's name.
     */
    public void testCreateConstructor()
    {
        IOperation constructor = clazz.createConstructor();
        assertNotNull(constructor);
        assertEquals(clazz.getName(), constructor.getName());
    }

    /**
     * Create an operation and check whether its not null and its name is equal to the
     * name that was given as input.
     */
    public void testCreateOperation()
    {
        String operationName = "operation1";
        IOperation oper = clazz.createOperation("String", operationName);
        assertNotNull(oper);
        assertEquals(operationName, oper.getName());
    }

    public void testCreateOperation2()
    {
        IClass c = createClass("Movanic");
        IOperation oper = clazz.createOperation2(c, "zinger");
        assertNotNull(oper);
        assertEquals("zinger", oper.getName());
    }
    
    public void testCreateOperation3()
    {
        IOperation oper = clazz.createOperation3();
        assertNotNull(oper);
    }
    
    public void testAddOperation()
    {
        IOperation oper = clazz.createOperation("String", "wing");
        clazz.addOperation(oper);
        
        ETList<IOperation> ops = clazz.getOperations();
        assertNotNull(ops);
        assertEquals(2, ops.size());
        assertEquals(oper.getXMIID(), ops.get(1).getXMIID());
    }
    
    public void testGetOperations()
    {
        // Tested by testAddOperation
    }
    
    public void testGetOperationsByName()
    {
        IOperation oper = clazz.createOperation("String", "wing");
        clazz.addOperation(oper);

        oper = clazz.createOperation("Integer", "wing");
//        clazz.addOperation(oper);
        IParameter p = oper.createParameter("float", "z");
        oper.addParameter(p);
		clazz.addOperation(oper);

        oper = clazz.createOperation("String", "pine");
        clazz.addOperation(oper);
        
        ETList<IOperation> ops = clazz.getOperationsByName("wing");
        assertNotNull(ops);
		
        assertEquals(2, ops.size());
        
        ETList<IOperation> opss = clazz.getOperationsByName("pine");
        assertNotNull(opss);
        assertEquals(1, opss.size());
    }

    /**
     * Gets an attribute by its name and check whether its not null and its name is equal to the
     * name that was given as input.
     */
    public void testGetAttributeByName()
    {
        String attrName = "attribute2";
    	IAttribute attr = clazz.createAttribute("Integer",attrName);
    	assertNotNull(attr);
    	assertEquals(attrName, attr.getName());
        assertNull(clazz.getAttributeByName(attrName));
        
        clazz.addAttribute(attr);

        assertNotNull(clazz.getAttributeByName(attrName));
    }
    
    public void testGetAttributesByName()
    {
        IAttribute attr1 = clazz.createAttribute("int", "a");
        IAttribute attr3 = clazz.createAttribute("char", "b");
        clazz.addAttribute(attr1);
        clazz.addAttribute(attr3);
        
        ETList<IAttribute> attrs = clazz.getAttributesByName("a");
        assertNotNull(attrs);
        assertEquals(1, attrs.size());
    }
    
    public void testGetAttributesAndNavEndsByName()
    {
        IAttribute attr = clazz.createAttribute("Integer", "magellan");
        clazz.addAttribute(attr);

        IClass other = createClass("Other");
        IAssociation assoc =
            relFactory.createAssociation(clazz, other, project);
        project.addElement(assoc);
        
        IAssociationEnd end = other.getAssociationEnds().get(0);
        end.setName("magellan");

        INavigableEnd navEnd = end.makeNavigable();
        assertNotNull(navEnd);
        
        assertNotNull(clazz.getAttributesAndNavEndsByName("magellan"));
        assertEquals(2, clazz.getAttributesAndNavEndsByName("magellan").size());
    }
    
    public void testGetInboundNavigableEnds()
    {
        IClass other = createClass("Other");
        IAssociation assoc = relFactory.createAssociation(clazz, other, project);
        
        IAssociationEnd end = clazz.getAssociationEnds().get(0);
        end.setName("magellan");
        end.makeNavigable();
        
        ETList<INavigableEnd> inbound = clazz.getInboundNavigableEnds();
        assertNotNull(inbound);
        assertEquals(1, inbound.size());
    }
    
    public void testGetOutboundNavigableEnds()
    {
        IClass other = createClass("Other");
        IAssociation assoc =
            relFactory.createAssociation(clazz, other, project);
        other.getAssociationEnds().get(0).makeNavigable();
        
        ETList<INavigableEnd> outbound = clazz.getOutboundNavigableEnds();
        assertNotNull(outbound);
        assertEquals(1, outbound.size());
    }
    
    public void testGetNavigableEnds()
    {
        testGetInboundNavigableEnds();
        testGetOutboundNavigableEnds();
        
        ETList<INavigableEnd> ends = clazz.getNavigableEnds();
        assertNotNull(ends);
        assertEquals(1, ends.size());
    }
    
    /**
     * First set the isAbstract flag to true and check if the value set is
     * correct.
     */
    public void testSetIsAbstract()
    {
        clazz.setIsAbstract(true);
        assertTrue(clazz.getIsAbstract());
        
        clazz.setIsAbstract(false);
        assertFalse(clazz.getIsAbstract());
    }
    
    public void testGetIsAbstract()
    {
        // Tested by setIsAbstract
    }

    /**
     * First set the isLeaf flag to true and check if the value set is correct.
     */
    public void testSetIsLeaf()
    {
    	clazz.setIsLeaf(true);
        assertTrue(clazz.getIsLeaf());
        
        clazz.setIsLeaf(false);
        assertFalse(clazz.getIsLeaf());
    }
    
    public void testGetIsLeaf()
    {
        // Tested by testSetIsLeaf.
    }

    /**
     * First set the isTransient flag to true and check if the value set is correct.
     */
    public void testSetIsTransient()
    {
        clazz.setIsTransient(true);
        assertTrue(clazz.getIsTransient());

        clazz.setIsTransient(false);
        assertFalse(clazz.getIsTransient());
    }
    
    public void testGetIsTransient()
    {
        // Tested by testSetIsTransient
    }

    /**
     * Create an association and add it to the class, then get all the
     * association from the class and check whether the newly created
     * association is present or not    
     */
    public void testAddAssociationEnd()
    {
        String testName = "NewTestAssociation"; 
        IAssociationEnd end = factory.createAssociationEnd(clazz);
        end.setName(testName);
        project.addOwnedElement(end);
        clazz.addAssociationEnd(end);

        ETList<IAssociationEnd> ends = clazz.getAssociationEnds();
        boolean found = false;
        for (int i = 0; i < ends.size(); i++)
        {
            IAssociationEnd asso = ends.get(i);
            if (testName.equals(asso.getName()))
            {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
    
    public void testRemoveAssociationEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(clazz);
        end.setName("mobius");
        project.addOwnedElement(end);
        clazz.addAssociationEnd(end);
        
        clazz.removeAssociationEnd(end);
        assertEquals(0, clazz.getAssociationEnds().size());
    }
    
    public void testGetAssociations()
    {
        IClass other = createClass("Other");
        IClass third = createClass("Third");
        relFactory.createAssociation(clazz, other, project);
        relFactory.createAssociation(clazz, third, project);
        
        ETList<IAssociation> assocs = clazz.getAssociations();
        assertNotNull(assocs);
        assertEquals(2, assocs.size());
    }
    
    public void testGetAssociationEnds()
    {
        // Tested by testAddAssociationEnd()
    }
    
    /**
     * Create an Generalization and add it to the class.
     */
    public void testAddGeneralization()
    {
    	IGeneralization gen = factory.createGeneralization(clazz);
    	clazz.addGeneralization(gen);
        
        ETList<IGeneralization> gens = clazz.getGeneralizations();
        assertNotNull(gens);
        assertEquals(1, gens.size());
        
        // TODO: We really want two participating classes.
    }
    
    public void testRemoveGeneralization()
    {
        testAddGeneralization();
        clazz.removeGeneralization(clazz.getGeneralizations().get(0));
        assertEquals(0, clazz.getGeneralizations().size());
    }

    public void testGetGeneralizations()
    {
        // Tested by testAddGeneralization()
    }

    /**
     * Create a feature and add it to the class
     */
    public void testAddFeature()
    {
        IAttribute attr = factory.createAttribute(null);
        attr.setName("feature");
        clazz.addFeature(attr);
        
        ETList<IFeature> feat = clazz.getFeatures();
        assertNotNull(feat);
        assertEquals(2, feat.size());
        
        IFeature at = feat.get(0);
        assertEquals(attr.getXMIID(), at.getXMIID());
        assertEquals("feature", at.getName());
    }
    
    public void testInsertFeature()
    {
        testAddFeature();
        IFeature at = clazz.getFeatures().get(0);
        
        assertEquals(2, clazz.getFeatures().size());
        IAttribute attr = factory.createAttribute(null);
        attr.setName("movanic");
        clazz.insertFeature(at, attr);
        
        ETList<IFeature> feats = clazz.getFeatures();
        assertNotNull(feats);
        assertEquals(3, feats.size());
        assertEquals(attr.getXMIID(), feats.get(0).getXMIID());
    }
    
    public void testGetFeatures()
    {
        // Tested by testAddFeature and testInsertFeature
    }
    
    public void testRemoveFeature()
    {
        testAddFeature();
        IFeature at = clazz.getFeatures().get(0);
        assertNotNull(at);
        clazz.removeFeature(at);
        assertEquals(1, clazz.getFeatures().size());
    }

    /**
     * Create an Implementation and add it to the class    
     */
    public void testAddImplementation()
    {
    	IImplementation imp = factory.createImplementation(clazz);
        project.addElement(imp);
    	clazz.addImplementation(imp);
        
        ETList<IImplementation> imps = clazz.getImplementations();
        assertNotNull(imps);
        assertEquals(1, imps.size());
        
        IImplementation daemon = imps.get(0);
        assertEquals(imp.getXMIID(), daemon.getXMIID());
    }
    
    public void testRemoveImplementation()
    {
        testAddImplementation();
        IImplementation imp = clazz.getImplementations().get(0);
        clazz.removeImplementation(imp);
        assertTrue(clazz.getImplementations() == null ||
            clazz.getImplementations().size() == 0);
    }
    
    public void testGetImplementations()
    {
        // Tested by testAddImplementation and testRemoveImplementation
    }
    
    /**
     * Create an Increment and add it to the class    
     */
    public void testAddIncrement()
    {
    	IIncrement inc = factory.createIncrement(clazz);
        clazz.addIncrement(inc);
        
        ETList<IIncrement> increments = clazz.getIncrements();
        assertNotNull(increments);
        assertEquals(1, increments.size());
        assertEquals(inc.getXMIID(), increments.get(0).getXMIID());
    }
       
    
    /**
     * Create an Specialization and add it to the class.
     */
    public void testAddSpecialization()
    {
    	IGeneralization gen = factory.createGeneralization(clazz);
    	clazz.addSpecialization(gen);
        
        // Why we need to do this: adding a generalization to a class adds the 
        // IGeneralization as *scoped by the class*. Adding a specialization
        // merely adds the IGeneralization's XMI ID to the class' attributes.
        // Therefore, merely calling addSpecialization above hasn't added
        // 'gen' to the project (and since 'gen' isn't in the project, 
        // getSpecializations() won't find it).
        project.addElement(gen);
        
        ETList<IGeneralization> specs = clazz.getSpecializations();
        assertNotNull(specs);
        assertEquals(1, specs.size());
        assertEquals(gen.getXMIID(), specs.get(0).getXMIID());
    }
    
    public void testGetSpecializations()
    {
        // Tested by testAddSpecialization()
    }
    
    public void testRemoveSpecialization()
    {
        IGeneralization gen = factory.createGeneralization(clazz);
        clazz.addSpecialization(gen);
        project.addElement(gen);
        
        clazz.removeSpecialization(gen);
        ETList<IGeneralization> specs = clazz.getSpecializations();
        assertEquals(0, specs.size());
    }
    
    public void testSetDerivation()
    {
        IClassifier templation = createClass("TemplateC");
        IParameterableElement par = createClass("T");
        
        // We have to add at least one template parameter, or setDerivation()
        // ignores our poor 'templated' class.
        templation.addTemplateParameter(par);

        IDerivation der = relFactory.createDerivation(clazz, templation);
        assertNotNull(der);
        clazz.setDerivation(der);
        
        IDerivation gDer = clazz.getDerivation();
        assertNotNull(gDer);
        assertEquals(der.getXMIID(), gDer.getXMIID());
    }

    public void testGetDerivation()
    {
        // Tested by testSetDerivation
    }
    
    public void testAddTemplateParameter()
    {
        IParameterableElement par = createClass("T");
        clazz.addTemplateParameter(par);
        
        ETList<IParameterableElement> els = clazz.getTemplateParameters();
        assertNotNull(els);
        assertEquals(1, els.size());
        assertEquals(par.getXMIID(), els.get(0).getXMIID());
    }
    
    public void testGetIsTemplateParameter()
    {
        IParameterableElement par = createClass("T");
        clazz.addTemplateParameter(par);
        assertTrue(clazz.getIsTemplateParameter(par));
    }
    
    public void testRemoveTemplateParameter()
    {
        IParameterableElement par = createClass("T");
        clazz.addTemplateParameter(par);
        clazz.removeTemplateParameter(par);
        assertEquals(0, clazz.getTemplateParameters().size());
    }
    
    public void testGetTemplateParameters()
    {
        // Tested by testAddTemplateParameter() and testRemoveTemplateParameter()
    }
    
    public void testSetFeatures()
    {
        // setFeatures() is a stub.
    }

    public void testAddCollaboration()
    {
        ICollaborationOccurrence col =
            factory.createCollaborationOccurrence(null);
        assertNotNull(col);
        clazz.addCollaboration(col);
        
        ETList<ICollaborationOccurrence> cols = clazz.getCollaborations();
        assertNotNull(cols);
        assertEquals(1, cols.size());
        assertEquals(col.getXMIID(), cols.get(0).getXMIID());
    }
    
    public void testRemoveCollaboration()
    {
        ICollaborationOccurrence col =
            factory.createCollaborationOccurrence(null);
        assertNotNull(col);
        clazz.addCollaboration(col);
        
        clazz.removeCollaboration(col);
        assertEquals(0, clazz.getCollaborations().size());
    }
    
    public void testGetCollaborations()
    {
        // Tested by testAddCollaboration()
    }
    
    public void testSetRepresentation()
    {
        ICollaborationOccurrence col =
            factory.createCollaborationOccurrence(null);
        assertNotNull(col);
        clazz.setRepresentation(col);
        
        assertNotNull(clazz.getRepresentation());
        assertEquals(col.getXMIID(), clazz.getRepresentation().getXMIID());
    }
    
    public void testSetClassifierBehavior()
    {
        assertNull(clazz.getClassifierBehavior());
        
        // Dunno how to create a plain old Behavior
        IBehavior be = factory.createActivity(null);
        clazz.setClassifierBehavior(be);
        
        assertNotNull(clazz.getClassifierBehavior());
        assertEquals(be.getXMIID(), clazz.getClassifierBehavior().getXMIID());
    }
    
    public void testGetClassifierBehavior()
    {
        // Tested by testSetClassifierBehavior
    }
    
    public void testAddBehavior()
    {
        IBehavior be = factory.createActivity(null);
        clazz.addBehavior(be);
        
        ETList<IBehavior> beh = clazz.getBehaviors();
        assertNotNull(beh);
        assertEquals(1, beh.size());
        assertEquals(be.getXMIID(), beh.get(0).getXMIID());
    }
    
    public void testRemoveBehavior()
    {
        IBehavior be = factory.createActivity(null);
        clazz.addBehavior(be);
        assertEquals(1, clazz.getBehaviors().size());
        clazz.removeBehavior(be);
        assertTrue(clazz.getBehaviors() == null 
            || clazz.getBehaviors().size() == 0);
    }
    
    public void testGetBehaviors()
    {
        // Tested by testAddBehaviour
    }
    
    public void testGetRepresentation()
    {
        assertNull(clazz.getRepresentation());

        ICollaborationOccurrence occ = factory.createCollaborationOccurrence(null);
        clazz.setRepresentation(occ);
        assertNotNull(clazz.getRepresentation());
        
        assertEquals(occ.getXMIID(), clazz.getRepresentation().getXMIID());
    }
    
    public void testGetIncrements()
    {
        IIncrement inc = factory.createIncrement(null);
        assertEquals(0, clazz.getIncrements().size());
        
        clazz.addIncrement(inc);
        ETList<IIncrement> incs = clazz.getIncrements();
        assertNotNull(incs);
        assertEquals(1, incs.size());
        assertEquals(inc.getXMIID(), incs.get(0).getXMIID());
    }
    
    public void testRemoveIncrement()
    {
        IIncrement inc = factory.createIncrement(null);
        assertEquals(0, clazz.getIncrements().size());
        clazz.addIncrement(inc);
        clazz.removeIncrement(inc);
        
        assertEquals(0, clazz.getIncrements().size());
    }
    
    
    public void testTransform()
    {
//        try
//        {
//            IOperation created = clazz.createOperation("String", "operation1");
//            clazz.addOperation(created);
//
//            IAttribute attr = clazz.createAttribute("int", "x");
//            clazz.addAttribute(attr);
//            assertNotNull(attr = clazz.getAttributeByName("x"));
//            assertEquals("int", attr.getTypeName());
//
//            Thread.sleep(4000);
//            assertEquals(4, clazz.getOperations().size());
//            assertEquals(1, clazz.getAttributes().size());
//
//
//            //Transforming to Class to Interface
//            IClassifier cl = clazz.transform("Interface");
//
//            assertNotNull(cl);
//            assertTrue(cl instanceof IInterface);
//
//            //Checking properties of Interface after transformation
//            assertEquals(1, cl.getOperations().size());
//            assertEquals(1, clazz.getAttributes().size());
//            IAttribute iAttr = clazz.getAttributes().get(0);
//            assertTrue(iAttr.getIsFinal());
//            assertTrue(iAttr.getIsStatic());
//
//            IAttribute attrr;
//            IClassifier tempClazz = createClass("Class1");
//            tempClazz.addAttribute(attrr = tempClazz.createAttribute("char", "xx"));
//            assertEquals(3, tempClazz.getOperations().size());
//
//            IClassifier tempInterface = createInterface("Interface1");
//            tempInterface.addAttribute(attrr = tempInterface.createAttribute("char", "xx"));
//            assertEquals(0, tempInterface.getOperations().size());
//            assertEquals(1, tempInterface.getAttributes().size());
//        }
//        catch(InterruptedException e)
//        {
//            
//        }
    }
    
    public void testGetRedefiningAttributes2()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IAttribute attr = parent.createAttribute("int", "a");
        parent.addAttribute(attr);
        
        IAttribute cat = clazz.createAttribute("int", "a");
        clazz.addAttribute(cat);
        attr.addRedefiningElement(cat);
        cat.addRedefinedElement(attr);
        
        ETList<IAttribute> coll = clazz.getRedefiningAttributes2();
        assertEquals(1, coll.size());
        assertEquals(cat.getXMIID(), coll.get(0).getXMIID());
    }
    
    public void testGetRedefiningOperations2()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IOperation oper = parent.createOperation("int", "a");
        parent.addOperation(oper);
        
        IOperation red = clazz.createOperation("int", "a");
        clazz.addOperation(red);
        oper.addRedefiningElement(red);
        red.addRedefinedElement(oper);
        
        ETList<IOperation> coll = clazz.getRedefiningOperations2();
        assertEquals(1, coll.size());
        assertEquals(red.getXMIID(), coll.get(0).getXMIID());
    }

    public void testGetNonRedefiningFeatures()
    {
//        try{ 
//            IClassifier parent = createSuperclass(clazz, "Super");
//            IOperation oper = parent.createOperation("int", "a");
//            parent.addOperation(oper);
//
//            IOperation red = clazz.createOperation("int", "a");
//            clazz.addOperation(red);
//            oper.addRedefiningElement(red);
//            red.addRedefinedElement(oper);
//
//            IAttribute attr;
//            clazz.addAttribute(attr = clazz.createAttribute("char", "x"));
//
//            ETList<IFeature> feats = clazz.getNonRedefiningFeatures();
//
//            Thread.sleep(4000);
//            // Non-redef feats should be x, getX(), setX(int) and constructor		
//            assertEquals(4, feats.size());
//
//            assertEquals(attr.getXMIID(), feats.get(0).getXMIID());
//        }
//        catch(InterruptedException e)
//        {
//            
//        }
    }
    
    public void testGetNonRedefiningAttributes()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IAttribute attr = parent.createAttribute("int", "a");
        parent.addAttribute(attr);
        
        IAttribute cat = clazz.createAttribute("int", "a");
        clazz.addAttribute(cat);
        attr.addRedefiningElement(cat);
        cat.addRedefinedElement(attr);
        
        IAttribute nred;
        clazz.addAttribute(nred = clazz.createAttribute("char", "x"));
        ETList<IAttribute> attrs = clazz.getNonRedefiningAttributes();
        assertEquals(1, attrs.size());
        assertEquals(nred.getXMIID(), attrs.get(0).getXMIID());
    }
    
    public void testGetRedefiningOperations()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IOperation oper = parent.createOperation("int", "a");
        parent.addOperation(oper);
        
        IOperation red = clazz.createOperation("int", "a");
        clazz.addOperation(red);
        oper.addRedefiningElement(red);
        red.addRedefinedElement(oper);
        
        ETList<INamedCollection> coll = clazz.getRedefiningOperations();
        assertEquals(1, coll.size());
        assertEquals(red.getXMIID(), 
			((new ETArrayList<IFeature>((Collection)
                    coll.get(0).getData())).get(0).getXMIID()));
    }
    
    public void testGetNonRedefiningOperations()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IOperation oper = parent.createOperation("int", "a");
        parent.addOperation(oper);
        
        IOperation red = clazz.createOperation("int", "a");
        clazz.addOperation(red);
        oper.addRedefiningElement(red);
        red.addRedefinedElement(oper);
        
        IOperation nred;
        clazz.addOperation(nred = clazz.createOperation("float", "z"));

        ETList<IOperation> ops = clazz.getNonRedefiningOperations();
        assertEquals(2, ops.size());
        assertEquals(nred.getXMIID(), ops.get(1).getXMIID());
    }
    
    public void testGetRedefiningAttributes()
    {
        IClassifier parent = createSuperclass(clazz, "Super");
        IAttribute attr = parent.createAttribute("int", "a");
        parent.addAttribute(attr);
        
        IAttribute cat = clazz.createAttribute("int", "a");
        clazz.addAttribute(cat);
        attr.addRedefiningElement(cat);
        cat.addRedefinedElement(attr);
        
        ETList<INamedCollection> coll = clazz.getRedefiningAttributes();
        assertEquals(1, coll.size());
        assertEquals(cat.getXMIID(), 
            ((new ETArrayList<IFeature>((Collection)coll.get(0).getData())).get(0).getXMIID()));
    }
}
