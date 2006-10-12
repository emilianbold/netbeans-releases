/*
 * SchemaComponentTest.java
 *
 * Created on November 2, 2005, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import junit.framework.TestCase;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 */
public class SchemaComponentTest extends TestCase{
    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    public static final String EMPTY_XSD = "resources/Empty.xsd";
    
     Schema schema = null;
    /**
     * Creates a new instance of SchemaComponentTest
     */
    public SchemaComponentTest(String testcase) {
        super(testcase);
    }
    
   
    protected void setUp() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }
    
    public void testPosition(){
        //schema position
        this.assertEquals("<schema> position ", 40, schema.findPosition());
        System.out.println("schema position: " + schema.findPosition());
        
        //position of first global element
        Collection<GlobalElement> elements = schema.getElements();
        GlobalElement element  = elements.iterator().next();
        System.out.println("position of first element: " + element.findPosition());
        this.assertEquals("<purchaseorder> element position ", 276, element.findPosition());
        
         //position of referenced type PurchaseType
        NamedComponentReference<? extends GlobalType> ref = element.getType();
        GlobalType type = ref.get();
        System.out.println("Position of referenced type: " + type.getName() +  ": " + type.findPosition());
        assertEquals("referenced PurchaseType position ", 387, type.findPosition() );
        
        //position of sequence under PurchaseType
        GlobalComplexType gct = (GlobalComplexType)type;        
        ComplexTypeDefinition def = gct.getDefinition();
        System.out.println("Sequence under PurchaseType position: " + def.findPosition());
        assertEquals("sequence under PurchaseType position ", 430, def.findPosition() );
        
        Collection<GlobalSimpleType> simpleTypes = schema.getSimpleTypes();
        GlobalSimpleType simpleType = simpleTypes.iterator().next();
        System.out.println("Position of simple Type: " + simpleType.findPosition());
        assertEquals("simple type allNNI position ", 865, simpleType.findPosition());
    }
    
}
