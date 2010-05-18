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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.xam;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.ExtensionContainer;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.support.ActivityDescriptor;
import org.netbeans.modules.bpel.model.api.support.ActivityDescriptor.ActivityType;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ads
 */
public class ProcessTest extends TestCase {
    
    static final String D1 = "d1";
    
    static final String D2 = "d2";
    
    static final String NEW = "new";
    
    public ProcessTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {       
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ProcessTest.class);
        
        return suite;
    }

    
    /**
     * Test method for getActivity with preexisted Activity in xml file.
     * @throws Exception 
     */
    public void testGetActivity() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        ExtendableActivity activity = model.getProcess().getActivity();
                
        assert activity instanceof Sequence;
        
    }
    
    /**
     * Test method setActivity with preexisted Activity in xml file.
     * @throws Exception 
     */
    public void testSetActivity() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        setActivityTest( model );
    }
    
    /**
     * This method tests removeActivity method.
     * @throws Exception 
     */
    public void testRemoveActivity() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeActivityTest( model );
    }
    

    /**
     * This method test how setActivity will behave when it absent
     * initially in XML file.
     * @throws Exception 
     */
    public void testSetActivityAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeActivityTest( model );
        
        // first test - how model will behave after deletion 
        // activity via modle method.
        
        setActivityTest( model );
        
        // second  - we remove activity once again and load result 
        // document as new and once again test setting activity.
        model = removeActivityTest( model );
        
        assertNull( model.getProcess().getActivity() );
        
        setActivityTest( model );
    
    }
    
    /**
     * Test of getPartnerLinkContainer method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
     * @throws Exception 
     */
    public void testGetPartnerLinkContainer() throws Exception {  
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        assertNotNull( model.getProcess().getPartnerLinkContainer() );
    }

    /**
     * Test of setPartnerLinkContainer method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
     * @throws Exception 
     */
    public void testSetPartnerLinkContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        setPartnerLinkContainerTest(model);
    }
    
    public void testSetExtensionContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        setExtensionContainerTest( model );
    }

    public void testRemoveExtensionContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeExtensionContainerTest( model );
    }
    
    public void testSetExtensionContainerAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removeExtensionContainerTest( model );
        
        // first test - how model will behave after deletion 
        // ExtensionContainer via modle method.
        
        setExtensionContainerTest( model );
        
        // second  - we remove ExtensionContainer once again and load result 
        // document as new and once again test setting ExtensionContainer.
        model = removeExtensionContainerTest( model );
        
        assertNull( model.getProcess().getExtensionContainer() );
        
        setExtensionContainerTest( model );
    }
    
    public void testGetVariableContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        assertNotNull( model.getProcess().getVariableContainer() );
    }
    
    public void testSetVariableContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        setVariableContainerTest( model );
    }
    
    public void testRemoveVariableContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removeVariableContainerTest( model );
    }
    
    public void testSetVariableContainerAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removeVariableContainerTest( model );
        
        // first test - how model will behave after deletion 
        // PartnerLinkContainer via modle method.
        
        setVariableContainerTest( model );
        
        // second  - we remove PartnerLinkContainer once again and load result 
        // document as new and once again test setting PartnerLinkContainer.
        model = removeVariableContainerTest( model );
        
        assertNull( model.getProcess().getVariableContainer() );
        
        setVariableContainerTest( model );
    }
    
    public void testGetCorrelationSetContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        assertNotNull( model.getProcess().getCorrelationSetContainer() );
    }
    
    public void testGetDocumentation() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        assertTrue( model.getProcess().getDocumentations().length==2);
        assertTrue( model.getProcess().sizeOfDocumentations()==2);
        assertNotNull( model.getProcess().getDocumentation(0));
        assertNotNull( model.getProcess().getDocumentation(1));
    }
    
    public void testGetExtensionContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        assertNotNull( model.getProcess().getExtensionContainer() );
    }
    
    public void testGetImport() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        assertTrue( model.getProcess().getImports().length ==3 );
        assertTrue( model.getProcess().sizeOfImports() == 3) ;
        for ( int i =0 ; i<3 ; i++ ) {
            assertNotNull( model.getProcess().getImport(i ));
        }
    }
    
    public void testRemoveDocumentation() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removeDocumentationTest( model , 1 );
    }
    
    public void testAddDocumentation() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        addDocumentationTest( model );
    }
    
    public void testAddDocumentationAbsentInProcess() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        
        // remove both documenation from model.
        removeDocumentationTest( model , 0 );
        model = removeDocumentationTest( model , 0 );
        
        assertTrue( model.getProcess().sizeOfDocumentations() == 0 );
        
        addDocumentationTest( model );
    }
    
    public void testSetCorrelationSetContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        setCorrelationSetContainerTest( model );
    }
    
    public void testRemoveCorrelationSetContainer() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removeCorrelationSetContainerTest( model );
    }
    
    public void testSetCorrelationSetContainerAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeCorrelationSetContainerTest( model );
        
        // first test - how model will behave after deletion 
        // CorrelationSetContainer via modle method.
        
        setCorrelationSetContainerTest( model );
        
        // second  - we remove CorrelationSetContainer once again and load result 
        // document as new and once again test setting CorrelationSetContainer.
        model = removeCorrelationSetContainerTest( model );
        
        assertNull( model.getProcess().getCorrelationSetContainer() );
        
        setCorrelationSetContainerTest( model );
    }
    
    public void testGetFaultHandlers() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        assertNotNull( model.getProcess().getFaultHandlers() );
    }
    
    public void testSetFaultHandlers() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        setFaultHandlersTest( model );
    }
    
    public void testRemoveFaultHandlers() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeFaultHandlersTest( model );
    }
    
    public void testSetFaultHandlersAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeFaultHandlersTest( model );
        
        // first test - how model will behave after deletion 
        // FaultHandlers via modle method.
        
        setFaultHandlersTest( model );
        
        // second  - we remove FaultHandlers once again and load result 
        // document as new and once again test setting FaultHandlers.
        model = removeFaultHandlersTest( model );
        
        assertNull( model.getProcess().getFaultHandlers() );
        
        setFaultHandlersTest( model );
    }

    
    public void testGetEventHandlers() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        assertNotNull( model.getProcess().getEventHandlers() );
    }
    
    public void testSetEventHandlers() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        setEventHandlersTest( model );
    }
    
    public void testRemoveEventHandlers() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removeEventHandlersTest( model );
    }
    
    public void testSetEventHandlersAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        removeEventHandlersTest( model );
        
        // first test - how model will behave after deletion 
        // EventHandlers via modle method.
        
        setEventHandlersTest( model );
        
        // second  - we remove EventHandlers once again and load result 
        // document as new and once again test setting EventHandlers.
        model = removeEventHandlersTest( model );
        
        assertNull( model.getProcess().getEventHandlers() );
        
        setEventHandlersTest( model );
    }
//
//    /**
//     * Test of getAbstractProcess method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testGetAbstractProcess() {
//      // TODO : test attribute access
//    }
//
//
//    /**
//     * Test of setAbstractProcess method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testSetAbstractProcess() {
//      //       TODO : test attribute access
//    }
//
//    /**
//     * Test of getEnableInstanceCompensation method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testGetEnableInstanceCompensation() {
////     TODO : test attribute access
//    }
//
//
//    /**
//     * Test of setEnableInstanceCompensation method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testSetEnableInstanceCompensation() {
////     TODO : test attribute access
//    }
//
//    /**
//     * Test of getExpressionLanguage method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testGetExpressionLanguage() {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of setExpressionLanguage method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testSetExpressionLanguage() throws Exception {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of getQueryLanguage method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testGetQueryLanguage() {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of setQueryLanguage method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testSetQueryLanguage() throws Exception {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of getTargetNamespace method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testGetTargetNamespace() {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of setTargetNamespace method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testSetTargetNamespace() throws Exception {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of removeAbstractProcess method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testRemoveAbstractProcess() {
////         TODO : test attribute access
//    }
//
//    /**
//     * Test of removeQueryLanguage method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testRemoveQueryLanguage() {
////     TODO : test attribute access
//    }
//
//    /**
//     * Test of removeExpressionLanguage method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testRemoveExpressionLanguage() {
////     TODO : test attribute access
//    }
//
//    /**
//     * Test of removeEnableInstanceCompensation method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testRemoveEnableInstanceCompensation() {
////     TODO : test attribute access
//    }
    
    /**
     * Test of removePartnerLinkContainer method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
     * @throws Exception 
     */
    public void testRemovePartnerLinkContainer() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removePartnerLinkContainerTest( model );
    }
    
    public void testSetPartnerLinkContainerAbsentInProcess() throws Exception{
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        removePartnerLinkContainerTest( model );
        
        // first test - how model will behave after deletion 
        // PartnerLinkContainer via modle method.
        
        setPartnerLinkContainerTest( model );
        
        // second  - we remove PartnerLinkContainer once again and load result 
        // document as new and once again test setting PartnerLinkContainer.
        model = removePartnerLinkContainerTest( model );
        
        assertNull( model.getProcess().getPartnerLinkContainer() );
        
        setPartnerLinkContainerTest( model );
    }

//    /**
//     * Test of accept method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
//     */
//    public void testAccept() {
        // TODO : is this need to test ?
//    }

    /**
     * Test of getElementType method, of class org.netbeans.modules.soa.model.bpel.xdm.impl.ProcessImpl.
     * @throws Exception 
     */
    public void testGetElementType() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel();
        
        assertTrue( model.getProcess().getElementType().
                equals( Process.class ) );
    }
    
    private void setActivityTest( BpelModelImpl model ){
        Process process = model.getProcess();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();
        PartnerLinkContainer container = process.getPartnerLinkContainer();
        VariableContainer variableContainer = process.getVariableContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        EventHandlers eventHandlers = process.getEventHandlers();
        
        List<? extends BpelEntity> entities = process.getChildren();
        
        for( ActivityType type : ActivityType.values())
        {
            List<String> tagList = new LinkedList<String>();
            for (BpelEntity entity : entities) {
                tagList.add( Util.getTagName( entity ).intern() );
            }
            
            // when we call this method in test method that don't have
            // activity as child, we have less children then need in list
            if ( process.getActivity()== null ) {
                tagList.add( null );
            }
                    
            
            if ( type.equals( ActivityType.COMPENSATE )){
                continue;
            }
            ExtendableActivity activity = model.getBuilder().createActivity( 
                    new ActivityDescriptor( type ) );
            
            // HERE SET new activity.
            model.getProcess().setActivity( activity );     
            
            ExtendableActivity act = model.getProcess().getActivity();
            
            assertTrue( model.getProcess() == process );
            assertTrue( activity == act );
            
            assertTrue( container == process.getPartnerLinkContainer() );
            assertTrue( variableContainer == process.getVariableContainer());
            assertTrue( setContainer == process.getCorrelationSetContainer());
            assertTrue( faultHandlers == process.getFaultHandlers());
            assertTrue( eventHandlers == process.getEventHandlers());
            assertTrue( extensionContainer == process.getExtensionContainer() );
            
            assertTrue( process.getDocumentations().length == docs.length );
            int i = 0 ;
            for( Documentation doc : process.getDocumentations() ){
                assertTrue( doc == docs[i] );
                i++;
            }
            
            assertTrue( process.sizeOfImports() == imports.length );
            i=0;
            for( Import imp : process.getImports() ){
                assertTrue( imp == imports[i]);
                i++;
            }
            
            tagList.set( tagList.size()-1 , Util.getTagName( activity).intern() );
            
            checkOrderAndPresence(model, tagList );
        }
        
        Document doc = Util.flush( model );
                
        NodeList list = 
            doc.getElementsByTagName( "nb:insidesequence" );
        assertEquals( list.getLength() , 0 );

    }

    /**
     * This method checks that each tag in <code>tagList</code>
     * present as child tag in root of document.
     * They have correct order. They are single. 
     */
    private void checkOrderAndPresence(BpelModelImpl model, List<String> tagList ) {
        Document document = Util.flush( model );
        
        Set<String> foundTags = new HashSet<String>();
        
        Element root = document.getDocumentElement();
        NodeList list = root.getChildNodes();
        /* walk through all nodes in list and check 
         * that all of them in correct order and no other
         * elements appear. 
         */ 
        String lastTag = null;
        for( int i=0; i<list.getLength(); i++ ){
            Node node = list.item( i );
            String name = node.getNodeName();
            
            if ( tagList.size() == 0 ){
                if ( name!= null ) {
                    // if there some more bpel tags in XML then this incorrect
                    assertFalse( foundTags.contains( name.intern()));
                }
                continue;
            }                   

            if ( tagList.get(0).equals( name ) ){
                tagList.remove( 0 );
                // only one such tag could be present in XML 
                if ( !name.equals( lastTag) ){
                    assertFalse( foundTags.contains( name.intern()));
                    foundTags.add( name.intern() );
                }
                lastTag = name.intern();
            }               
        } 
        
        // if order was wrong this list will not null.
        if ( tagList.size()>0 ) {
            Util.debug( tagList.get(0)) ;
        }
        assertTrue( tagList.size() == 0 );
    }
    
    private BpelModelImpl removeActivityTest(BpelModelImpl model) throws Exception {
        Process process = model.getProcess();
        
        // initial suggestion about nonempty Activity in process.
        assert process.getActivity()!= null;
        
        String tagName = Util.getTagName( process.getActivity() );
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( tagList.size() - 1);
        
        process.removeActivity();
        
        assertNull( process.getActivity() );
        
        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList, 0 );
    }
    
    /**
     * Method returns list of tag names that correspond order
     * children in <code>process</code>.
     */
    private List<String> getChildrenTagList(Process process) {
        List<? extends BpelEntity> entities = process.getChildren();
        List<String> tagList = new LinkedList<String>();
        for (BpelEntity entity : entities) {
            tagList.add( Util.getTagName( entity ).intern() );
        }
        return tagList;
    }
    
    private void setPartnerLinkContainerTest(BpelModelImpl model) {
        Process process = model.getProcess();
        
        PartnerLinkContainer container = 
            model.getBuilder().createPartnerLinkContainer();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();
        VariableContainer variableContainer = process.getVariableContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        EventHandlers eventHandlers = process.getEventHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.getPartnerLinkContainer()== null ) {
            tagList.add( 6, Util.getTagName( container ) );
        }
            
        // HERE SET new  PartnerLinkContainer
        process.setPartnerLinkContainer( container );
        
        assertTrue( model.getProcess() == process );
                    
        assertTrue( extensionContainer == process.getExtensionContainer() );
        assertTrue( variableContainer == process.getVariableContainer());
        assertTrue( setContainer == process.getCorrelationSetContainer());
        assertTrue( faultHandlers == process.getFaultHandlers());
        assertTrue( eventHandlers == process.getEventHandlers());
        assertTrue( activity == process.getActivity() );
        
        assertTrue( process.getDocumentations().length == docs.length );
        int i = 0 ;
        for( Documentation doc : process.getDocumentations() ){
            assertTrue( doc == docs[i] );
            i++;
        }
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );
        
        Document doc = Util.flush( model );
        String tagName = Util.getTagName( container );
        
        NodeList list = 
            doc.getElementsByTagName( tagName.substring( 0, tagName.length() -1 ));
        assertEquals( list.getLength() , 0 );
    }
    
    private void setExtensionContainerTest(BpelModelImpl model) {
        Process process = model.getProcess();
        
        ExtensionContainer container = model.getBuilder().
            createExtensionContainer();
        
        Documentation[] docs = process.getDocumentations();
        Import[] imports = process.getImports();
        PartnerLinkContainer plnkContainer = process.getPartnerLinkContainer();
        VariableContainer variableContainer = process.getVariableContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        EventHandlers eventHandlers = process.getEventHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.getPartnerLinkContainer()== null ) {
            tagList.add( 2, Util.getTagName( container ) );
        }
            
        // HERE SET new  ExtensionContainer
        process.setExtensionContainer( container );
        
        assertTrue( model.getProcess() == process );
                    
        assertTrue( container == process.getExtensionContainer() );
        assertTrue( plnkContainer == process.getPartnerLinkContainer());
        assertTrue( variableContainer == process.getVariableContainer());
        assertTrue( setContainer == process.getCorrelationSetContainer());
        assertTrue( faultHandlers == process.getFaultHandlers());
        assertTrue( eventHandlers == process.getEventHandlers());
        assertTrue( activity == process.getActivity() );
        
        assertTrue( process.getDocumentations().length == docs.length );
        int i = 0 ;
        for( Documentation doc : process.getDocumentations() ){
            assertTrue( doc == docs[i] );
            i++;
        }
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );
        
        Document doc = Util.flush( model );
        String tagName = Util.getTagName( container );
        
        NodeList list = 
            doc.getElementsByTagName( tagName.substring( 0, tagName.length() -1 ));
        assertEquals( list.getLength() , 0 );
    }
    
    private void addDocumentationTest(BpelModelImpl model) {
        Process process = model.getProcess();
        
        Documentation doc = model.getBuilder().createDocumentation();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();
        PartnerLinkContainer plnkContainer = process.getPartnerLinkContainer();
        VariableContainer variableContainer = process.getVariableContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        EventHandlers eventHandlers = process.getEventHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.sizeOfDocumentations()== 0 ) {
            tagList.add( 0, Util.getTagName( doc ) );
        }
            
        // HERE SET add new Documentation  
        process.addDocumentation( doc );
        
        assertTrue( model.getProcess() == process );
        
        assertTrue( plnkContainer == process.getPartnerLinkContainer() );
        assertTrue( extensionContainer == process.getExtensionContainer() );
        assertTrue( variableContainer == process.getVariableContainer());
        assertTrue( setContainer == process.getCorrelationSetContainer());
        assertTrue( faultHandlers == process.getFaultHandlers());
        assertTrue( eventHandlers == process.getEventHandlers());
        assertTrue( activity == process.getActivity() );
        
        assertTrue( process.getDocumentations().length == docs.length +1);
        int i = 0 ;
        for( Documentation doc1 : docs) {
            assertTrue( doc1 == process.getDocumentation(i) );
            i++;
        }
        
        assertTrue( doc == process.getDocumentation( docs.length ));
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );
        
        
        Document domDoc = Util.flush( model );
        String tagName = Util.getTagName( doc );
        
        NodeList list = domDoc.getElementsByTagName( tagName );
        assertTrue(list.item( docs.length ).getChildNodes().getLength()==0);
        
        assertTrue( doc.getContent().length()==0);
    }

    
    private void setEventHandlersTest( BpelModelImpl model ) {
        Process process = model.getProcess();
        
        EventHandlers eventHandlers = 
            model.getBuilder().createEventHandlers();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();
        PartnerLinkContainer pContainer = process.getPartnerLinkContainer();
        VariableContainer variableContainer = process.getVariableContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.getEventHandlers()== null ) {
            tagList.add( 10, Util.getTagName( eventHandlers ) );
        }
            
        // HERE SET new  EventHandlers
        process.setEventHandlers( eventHandlers );
        
        assertTrue( model.getProcess() == process );
                        
        assertTrue( pContainer == process.getPartnerLinkContainer());
        assertTrue( variableContainer == process.getVariableContainer());
        assertTrue( faultHandlers == process.getFaultHandlers() );
        assertTrue( setContainer == process.getCorrelationSetContainer());
        assertTrue( activity == process.getActivity() );
        assert( extensionContainer == process.getExtensionContainer() );
        
        assertTrue( process.getDocumentations().length == docs.length );
        int i = 0 ;
        for( Documentation doc : process.getDocumentations() ){
            assertTrue( doc == docs[i] );
            i++;
        }
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );

        Document doc = Util.flush( model );     
        
        NodeList list = 
            doc.getElementsByTagName( "nb:insideeventHandlers" );
        assertEquals( list.getLength() , 0 );

    }
    
    private void setFaultHandlersTest(BpelModelImpl model) {
        Process process = model.getProcess();
        
        FaultHandlers faultHandlers = 
            model.getBuilder().createFaultHandlers();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();
        PartnerLinkContainer pContainer = process.getPartnerLinkContainer();
        VariableContainer variableContainer = process.getVariableContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        EventHandlers eventHandlers = process.getEventHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.getFaultHandlers()== null ) {
            tagList.add( 9, Util.getTagName( faultHandlers ) );
        }
            
        // HERE SET new  FaultHandlers
        process.setFaultHandlers( faultHandlers );
        
        assertTrue( model.getProcess() == process );
                        
        assertTrue( pContainer == process.getPartnerLinkContainer());
        assertTrue( variableContainer == process.getVariableContainer());
        assertTrue( setContainer == process.getCorrelationSetContainer());
        assertTrue( eventHandlers == process.getEventHandlers());
        assertTrue( activity == process.getActivity() );
        assertTrue( extensionContainer == process.getExtensionContainer() );
        
        assertTrue( process.getDocumentations().length == docs.length );
        int i = 0 ;
        for( Documentation doc : process.getDocumentations() ){
            assertTrue( doc == docs[i] );
            i++;
        }
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );

        Document doc = Util.flush( model );     
        
        NodeList list = 
            doc.getElementsByTagName( "nb:insidefaultHandler" );
        assertEquals( list.getLength() , 0 );
    }
    
    private void setCorrelationSetContainerTest(BpelModelImpl model) {
        Process process = model.getProcess();
        
        CorrelationSetContainer container = 
            model.getBuilder().createCorrelationSetContainer();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();
        PartnerLinkContainer pContainer = process.getPartnerLinkContainer();
        VariableContainer variableContainer = process.getVariableContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        EventHandlers eventHandlers = process.getEventHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.getVariableContainer()== null ) {
            tagList.add( 8, Util.getTagName( container ) );
        }
            
        // HERE SET new  CorrelationSetContainer
        process.setCorrelationSetContainer( container );
        
        assertTrue( model.getProcess() == process );
                        
        assertTrue( pContainer == process.getPartnerLinkContainer());
        assertTrue( variableContainer == process.getVariableContainer());
        assertTrue( faultHandlers == process.getFaultHandlers());
        assertTrue( eventHandlers == process.getEventHandlers());
        assertTrue( activity == process.getActivity() );
        assertTrue( extensionContainer == process.getExtensionContainer() );
        
        assertTrue( process.getDocumentations().length == docs.length );
        int i = 0 ;
        for( Documentation doc : process.getDocumentations() ){
            assertTrue( doc == docs[i] );
            i++;
        }
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );

        Document doc = Util.flush( model );
        String tagName = Util.getTagName( container );
        
        NodeList list = 
            doc.getElementsByTagName( tagName.substring( 0, tagName.length() -1 ));
        assertEquals( list.getLength() , 0 );
    }
    
    private void setVariableContainerTest(BpelModelImpl model) {
        Process process = model.getProcess();
        
        VariableContainer container = 
            model.getBuilder().createVariableContainer();
        
        Documentation[] docs = process.getDocumentations();
        ExtensionContainer extensionContainer = process.getExtensionContainer();
        Import[] imports = process.getImports();

        PartnerLinkContainer pContainer = process.getPartnerLinkContainer();
        CorrelationSetContainer setContainer = process.getCorrelationSetContainer();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        EventHandlers eventHandlers = process.getEventHandlers();
        ExtendableActivity activity = process.getActivity();

        List<String> tagList = getChildrenTagList(process);
        
        if ( process.getVariableContainer()== null ) {
            tagList.add( 7, Util.getTagName( container ) );
        }
            
        // HERE SET new  VariableContainer
        process.setVariableContainer( container );
        
        assertTrue( model.getProcess() == process );
           
        assertTrue( extensionContainer == process.getExtensionContainer() );
        assertTrue( pContainer == process.getPartnerLinkContainer());
        assertTrue( setContainer == process.getCorrelationSetContainer());
        assertTrue( faultHandlers == process.getFaultHandlers());
        assertTrue( eventHandlers == process.getEventHandlers());
        assertTrue( activity == process.getActivity() );
        
        
        assertTrue( process.getDocumentations().length == docs.length );
        int i = 0 ;
        for( Documentation doc : process.getDocumentations() ){
            assertTrue( doc == docs[i] );
            i++;
        }
        
        assertTrue( process.sizeOfImports() == imports.length );
        i=0;
        for( Import imp : process.getImports() ){
            assertTrue( imp == imports[i]);
            i++;
        }
        
        checkOrderAndPresence( model , tagList );

        Document doc = Util.flush( model );
        
        NodeList list = 
            doc.getElementsByTagName( "nb:insidevariables");
        assertEquals( list.getLength() , 0 );
    }

    
    private BpelModelImpl removePartnerLinkContainerTest(BpelModelImpl model)
            throws Exception 
    {
        Process process = model.getProcess();

        // initial suggestion about nonempty PartnerContainer in process.
        assert process.getPartnerLinkContainer() != null;

        String tagName = Util.getTagName(process.getPartnerLinkContainer());
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( 6 );

        process.removePartnerLinkContainer();

        assertNull(process.getPartnerLinkContainer());

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList, 0 );
    }
    
    
    private BpelModelImpl removeDocumentationTest(BpelModelImpl model , int i )
            throws Exception 
    {
        Process process = model.getProcess();
        String presentTag = D1;

        // initial suggestion about nonempty PartnerContainer in process.
        assert process.getDocumentation( i ) != null;
        if ( process.getDocumentation(i).getContent().equals( presentTag )){
            presentTag = D2;
        }

        String tagName = Util.getTagName(process.getDocumentation(i));
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( 0 );
        
        int size = process.sizeOfDocumentations();

        process.removeDocumentation( i );

        assertTrue(process.sizeOfDocumentations() == size -1 );
        assertTrue(process.getDocumentations().length == size -1  );
        
        if ( process.sizeOfDocumentations() >0 ){
            process.getDocumentation( 0 ).getContent().equals( presentTag );
        }

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList, 1 );
    }
    
    private BpelModelImpl removeVariableContainerTest( BpelModelImpl model)
            throws Exception 
    {
        Process process = model.getProcess();

        // initial suggestion about nonempty VariableContainer in process.
        assert process.getVariableContainer() != null;

        String tagName = Util.getTagName(process.getVariableContainer());
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( 7);

        process.removeVariableContainer();

        assertNull(process.getVariableContainer());

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList, 0 );
    }
    
    private BpelModelImpl removeExtensionContainerTest( BpelModelImpl model )
            throws Exception
    {
        Process process = model.getProcess();

        // initial suggestion about nonempty ExtensionContainer in process.
        assertNotNull(process.getVariableContainer());

        String tagName = Util.getTagName(process.getExtensionContainer());

        List<String> tagList = getChildrenTagList(process);
        tagList.remove(2);

        process.removeExtensionContainer();

        assertNull(process.getExtensionContainer());

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList, 0);
    }
    
    
    private BpelModelImpl removeEventHandlersTest( BpelModelImpl model)
            throws Exception 
    {
        Process process = model.getProcess();

        // initial suggestion about nonempty EventHandlers in process.
        assert process.getEventHandlers() != null;

        String tagName = Util.getTagName(process.getEventHandlers());
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( 10 );

        process.removeEventHandlers();

        assertNull(process.getEventHandlers());

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList , 0);
    }
    
    
    private BpelModelImpl removeFaultHandlersTest( BpelModelImpl model)
            throws Exception 
    {
        Process process = model.getProcess();

        // initial suggestion about nonempty FaultHandlers in process.
        assert process.getFaultHandlers() != null;

        String tagName = Util.getTagName(process.getFaultHandlers());
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( 9 );

        process.removeFaultHandlers();

        assertNull(process.getFaultHandlers());

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList , 0);
    }
    
    private BpelModelImpl removeCorrelationSetContainerTest( BpelModelImpl model)
            throws Exception 
    {
        Process process = model.getProcess();

        // initial suggestion about nonempty CorrelationSetContainer in process.
        assert process.getCorrelationSetContainer() != null;

        String tagName = Util.getTagName(process.getCorrelationSetContainer());
        
        List<String> tagList = getChildrenTagList(process);
        tagList.remove( 8 );

        process.removeCorrelationSetContainer();

        assertNull(process.getCorrelationSetContainer());

        return checkAbsenceOfTagAndLoadModel(model, tagName, tagList , 0);
    }

    /**
     * This method check for absence <code>tagName</code> in root children and
     * returns new model that built on XML without tagName.
     * Also it checks that order of elements in XML correspond to
     * order in <code>tagList</code>.
     * 
     */
    private BpelModelImpl checkAbsenceOfTagAndLoadModel(BpelModelImpl model, 
            String tagName, List<String> tagList, int countOfPresence ) 
            throws Exception 
    {
        int count = 0;
        StringBuilder builder = new StringBuilder();
        Document  document = Util.flush( model , builder );
        Element root = document.getDocumentElement();
        NodeList list = root.getChildNodes();
        for( int i=0 ; i<list.getLength(); i++ ){
            Node node = list.item( i );
            if ( tagName.equals( node.getNodeName() )){
                count++;
                assertTrue( count <=countOfPresence );
            }
        }
        
        checkOrderAndPresence( model , tagList );
        
        return Util.loadModel( builder.toString() );
    }

}
