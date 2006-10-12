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
 * CustomizationTest.java
 * JUnit based test
 *
 * Created on February 3, 2006, 12:06 PM
 */

package org.netbeans.modules.websvc.customization.model;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.websvc.customization.model.impl.JAXWSQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Roderico Cruz
 */
public class CustomizationTest extends TestCase {
    private final static String TEST_WSDL = "resources/AddNumbers.wsdl";
    
    public CustomizationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    private WSDLModel getModel(){
        WSDLModel model = null;
       
        try{
             Document doc = this.getResourceAsDocument(TEST_WSDL);
             Lookup l = Lookups.fixed(new Object[] { doc });
             ModelSource source = new ModelSource(l, true);
             model = new WSDLModelImpl(source);
             model.sync();
        } catch(Exception e){
            System.out.println("Exception class: " + e.getClass().getName());
            System.out.println("Unable to load model: " + e.getMessage());
        }
        return model;
    }
    
    public void testWrite() throws Exception {
        WSDLModel model = getModel();
        model.startTransaction();
        Definitions d = model.getDefinitions();
        System.out.println("definitions: " + d);
        WSDLComponentFactory fact = d.getModel().getFactory();
        //set global customizations
        DefinitionsCustomization dc = (DefinitionsCustomization)
                fact.create(d, JAXWSQName.BINDINGS.getQName());
        EnableWrapperStyle ews = (EnableWrapperStyle) fact.create(dc, JAXWSQName.ENABLEWRAPPERSTYLE.getQName());
        ews.setEnabled(true);
        EnableAsyncMapping eam = (EnableAsyncMapping) fact.create(dc, JAXWSQName.ENABLEASYNCMAPPING.getQName());
        eam.setEnabled(false);
        dc.setEnableWrapperStyle(ews);
        dc.setEnableAsyncMapping(eam);
        d.addExtensibilityElement(dc);
        
        //set PortType customizations
        Collection<PortType> portTypes = d.getPortTypes();
        for(PortType p : portTypes){
            if(p.getName().equals("AddNumbersImpl")){
                PortTypeCustomization pc = (PortTypeCustomization)
                        fact.create(p, JAXWSQName.BINDINGS.getQName());
                JavaClass jc = (JavaClass) fact.create(pc, JAXWSQName.CLASS.getQName());
                jc.setName("MathUtil");
                pc.setJavaClass(jc);
                ews = (EnableWrapperStyle) fact.create(dc, JAXWSQName.ENABLEWRAPPERSTYLE.getQName());
                ews.setEnabled(true);
                pc.setEnableWrapperStyle(ews);
                eam = (EnableAsyncMapping) fact.create(dc, JAXWSQName.ENABLEASYNCMAPPING.getQName());
                eam.setEnabled(false);
                pc.setEnableAsyncMapping(eam);
                p.addExtensibilityElement(pc);
                
                //set PortTypeOperation customizations
                Collection<Operation> ops = p.getOperations();
                for(Operation op : ops){
                    if(op.getName().equals("addNumbers")){
                        PortTypeOperationCustomization ptoc = (PortTypeOperationCustomization)
                                fact.create(op, JAXWSQName.BINDINGS.getQName());
                        
                        //Customize method name
                        JavaMethod javaMethod = (JavaMethod) fact.create(ptoc, JAXWSQName.METHOD.getQName());
                        javaMethod.setName("add");
                        ptoc.setJavaMethod(javaMethod);
                        //Customize parameters using hard coded values
                        JavaParameter parm1 = (JavaParameter) fact.create(ptoc, 
                                JAXWSQName.PARAMETER.getQName());
                        parm1.setPart("wsdl:definitions/wsdl:message[@name='addNumbers']/wsdl:part[@name='parameters']");
                        parm1.setChildElementName("tns:number1");
                        parm1.setName("num1");
                        ptoc.addJavaParameter(parm1);
                        
                        JavaParameter parm2 = (JavaParameter) fact.create(ptoc,
                                JAXWSQName.PARAMETER.getQName());
                        parm2.setPart("wsdl:definitions/wsdl:message[@name='addNumbers']/wsdl:part[@name='parameters']");
                        parm2.setChildElementName("tns:number2");
                        parm2.setName("num2");
                        ptoc.addJavaParameter(parm2);
                        //end of hard coded values
                        
                        ews = (EnableWrapperStyle) fact.create(ptoc,
                                JAXWSQName.ENABLEWRAPPERSTYLE.getQName());
                        ews.setEnabled(true);
                        ptoc.setEnableWrapperStyle(ews);
                        eam = (EnableAsyncMapping) fact.create(ptoc,
                                JAXWSQName.ENABLEASYNCMAPPING.getQName());
                        ptoc.setEnableAsyncMapping(eam);
                        eam.setEnabled(false);
                        op.addExtensibilityElement(ptoc);
                        
                        //Customize the faults
                        Collection<Fault> faults = op.getFaults();
                        for(Fault fault : faults){
                            if(fault.getName().equals("AddNumbersException")){
                                PortTypeOperationFaultCustomization ptofc = (PortTypeOperationFaultCustomization)
                                        fact.create(fault, JAXWSQName.BINDINGS.getQName());
                                jc = (JavaClass) fact.create(ptofc, JAXWSQName.CLASS.getQName());
                                jc.setName("MathUtilException");
                                ptofc.setJavaClass(jc);
                                fault.addExtensibilityElement(ptofc);
                            }
                        }
                    }
                }
            }
        }
        
        //Customize the service
        Collection<Service> services = d.getServices();
        for(Service service : services){
            if(service.getName().equals("AddNumbersService")){
                ServiceCustomization sc = (ServiceCustomization) fact.create(service, JAXWSQName.BINDINGS.getQName());
                JavaClass jc = (JavaClass) fact.create(sc, JAXWSQName.CLASS.getQName());
                jc.setName("MathUtilService");
                sc.setJavaClass(jc);
                service.addExtensibilityElement(sc);
                
                //Customize the port
                Collection<Port> ports = service.getPorts();
                for(Port port : ports){
                    if(port.getName().equals("AddNumbersPort")){
                        PortCustomization poc = (PortCustomization) fact.create(port, JAXWSQName.BINDINGS.getQName());
                        JavaMethod jmethod = (JavaMethod) fact.create(poc, JAXWSQName.METHOD.getQName());
                        jmethod.setName("getMathUtil");
                        poc.setJavaMethod(jmethod);
                        port.addExtensibilityElement(poc);
                    }
                }
            }
        }
        model.endTransaction();
        
        File f = dumpToTempFile(((AbstractDocumentModel)model).getBaseDocument());
        System.out.println("file: " +f.getAbsolutePath());
        
        //now read it back
        
        List<DefinitionsCustomization> dces = d.getExtensibilityElements(DefinitionsCustomization.class);
        System.out.println("###number of DefinitionsCustomizations: " + dces.size());
        
        Collection<PortType> pts = d.getPortTypes();
        for(PortType portType : pts){
            if(portType.getName().equals("AddNumbersImpl")) {
                List<PortTypeCustomization> ptcs = portType.getExtensibilityElements(PortTypeCustomization.class);
                System.out.println("####number of PortTypeCustomizations: " + ptcs.size());
                for(PortTypeCustomization ptc : ptcs){
                    JavaClass jc = ptc.getJavaClass();
                    if(jc != null){
                        System.out.println("Class name:  "       + jc.getName());
                    }
                }
            }
        }
        
        Collection<Binding> bindings = d.getBindings();
        for(Binding binding : bindings){
            if(binding.getName().equals("AddNumbersImplBinding")){
                List<BindingCustomization> bcs = binding.getExtensibilityElements(BindingCustomization.class);
                System.out.println("####Number of BindingCustomizations: " + bcs.size());
            }
        }
    }
    
    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = CustomizationTest.class.getResourceAsStream(path);
        return loadDocument(in);
    }
    
    public static Document loadDocument(InputStream in) throws Exception {
	Document sd = new org.netbeans.editor.BaseDocument(
            org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
    
    public static void dumpToFile(Document doc, File f) throws Exception {
        if (! f.exists()) {
            f.createNewFile();
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static File dumpToTempFile(Document doc) throws Exception {
        File f = File.createTempFile("xsm", "xsd");
        dumpToFile(doc, f);
        return f;
    }
    
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
}
