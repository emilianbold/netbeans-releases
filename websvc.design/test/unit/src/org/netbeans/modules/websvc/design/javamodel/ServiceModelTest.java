/*
 * JavaGeneratorTest.java
 *
 * Created on March 6, 2007, 4:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.javamodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebParam.Mode;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;

/**
 *
 * @author mkuchtiak
 */
public class ServiceModelTest extends NbTestCase {
private static ServiceModelTest DEFAULT_LOOKUP = null;

    private static final String NAME="AddNumbers";
    private static final String SERVICE_NAME="AddNumbersService";
    private static final String PORT_NAME="AddNumbersPort";
    private static final int NUMBER_OF_METHODS =3;
    private static final String[] OP_NAMES={"add","echo-operation","send"};
    private static final String[] OP_RETURN_TYPES={"int","java.lang.String","void"};
    private static final boolean[] OP_ONE_WAY={false,false,true};
    private static final String[][] PARAM_NAMES={
        {"x","y"},
        {},
        {"message"}
    };
    private static final String[][] PARAM_TYPES={
        {"int","int"},
        {},
        {"java.lang.String"}
    };
    private static final Object[][] PARAM_MODES={
        {Mode.IN,Mode.IN},
        {},
        {Mode.IN}
    };
    
    private static final String NAME_1="AddNumbers";
    private static final String SERVICE_NAME_1="AddNumbers";
    private static final String PORT_NAME_1="AddNumbersPort";
    private static final String TARGET_NAMESPACE_1="http://www.netbeans.org/tests/AddNumbersTest";
    private static final int NUMBER_OF_METHODS_1 =4;
    private static final String[] OP_NAMES_1={"add","echo-operation","send","hello"};
    private static final String[] OP_RETURN_TYPES_1={"int","java.lang.String","void","java.lang.String"};
    private static final boolean[] OP_ONE_WAY_1={false,false,true,false};
    private static final String[][] PARAM_NAMES_1={
        {"x","y"},
        {},
        {"message","arg1"},
        {"arg0"}
    };
    private static final String[][] PARAM_TYPES_1={
        {"int","int"},
        {},
        {"java.lang.String","java.lang.String"},
        {"add.foo.Foo"}
    };
    private static final Object[][] PARAM_MODES_1={
        {Mode.IN,Mode.IN},
        {},
        {Mode.IN,Mode.IN},
        {Mode.IN}
    };
    
    private static final String[][] EXPECTED_EVENTS = {
        {"propertyChanged","serviceName","AddNumbersService","AddNumbers"},
        {"propertyChanged","targetNamespace",null,"http://www.netbeans.org/tests/AddNumbersTest"},
        {"operationChanged","send"},
        {"operationAdded","hello"}      
    };
    
    private List<String[]> events;

    private FileObject dataDir;
    
    public ServiceModelTest(String testName) {
        super(testName);
    }
 
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        // workaround for JavaSource class
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        dataDir = FileUtil.toFileObject(getDataDir());
            
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return ClassPathSupport.createClassPath(new FileObject[] {dataDir});
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    //if (type == ClassPath.BOOT)
                    //    return createClassPath(System.getProperty("sun.boot.class.path"));
                    return null;
            }
        };
        
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {loader, cpp});
        
        events=new ArrayList<String[]>();
    }
    
    protected void tearDown() throws Exception {
    }
    
    /** generates java artifacts for selected schema element
     */
    public void testServiceModel () throws IOException {
        FileObject sourceFileObject = dataDir.getFileObject("add/AddNumbers.java");
        assertNotNull(sourceFileObject);
        
        try {
            // compare model values
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);
            assertEquals(NAME, model.getName());
            assertEquals(SERVICE_NAME, model.getServiceName());
            assertEquals(PORT_NAME, model.getPortName());
            assertEquals(NUMBER_OF_METHODS,model.getOperations().size());
            List<MethodModel> operations = model.getOperations();
            int i=0;
            for (MethodModel op:operations) {
                assertEquals(OP_NAMES[i], op.getOperationName());
                assertEquals(OP_RETURN_TYPES[i], op.getReturnType());
                assertEquals(OP_ONE_WAY[i], op.isOneWay());
                List<ParamModel> params = op.getParams();
                int j=0;
                    for (ParamModel param:params) {
                        assertEquals(PARAM_NAMES[i][j], param.getName());
                        assertEquals(PARAM_TYPES[i][j], param.getParamType());
                        assertEquals(PARAM_MODES[i][j], param.getMode());
                        j++;
                    }
                i++;
            }
            // testing merge
            FileObject sourceFileObject_1 = dataDir.getFileObject("add/AddNumbers_1.java");
            assertNotNull(sourceFileObject_1);
            ServiceModel model_1 = ServiceModel.getServiceModel(sourceFileObject_1);
            model.addServiceChangeListener(new ServiceChangeListener() {
                public void propertyChanged(String propertyName, String oldValue, String newValue) {
                    System.out.println("propertyChanged "+propertyName+":"+oldValue+":"+newValue);
                    events.add(new String[]{"propertyChanged",propertyName,oldValue,newValue});
                }

                public void operationAdded(MethodModel method) {
                    System.out.println("operationAdded "+method.getOperationName());
                    events.add(new String[]{"operationAdded",method.getOperationName()});
                }

                public void operationRemoved(MethodModel method) {
                    System.out.println("operationRemoved "+method.getOperationName());
                    events.add(new String[]{"operationRemoved",method.getOperationName()});
                }

                public void operationChanged(MethodModel method) {
                    System.out.println("operationChanged "+method.getOperationName());
                     events.add(new String[]{"operationChanged",method.getOperationName()});
                }
            });
            model.mergeModel(model_1);
            i=0;
            for (String[] event:events) {
                int j=0;
                for (String eventPart:event) {
                    assertEquals(EXPECTED_EVENTS[i][j], eventPart);
                    j++;
                }
                i++;
            }
            
            // compare again model values
            assertEquals(NAME_1, model.getName());
            assertEquals(SERVICE_NAME_1, model.getServiceName());
            assertEquals(PORT_NAME_1, model.getPortName());
            assertEquals(NUMBER_OF_METHODS_1,model.getOperations().size());
            operations = model.getOperations();
            i=0;
            for (MethodModel op:operations) {
                assertEquals(OP_NAMES_1[i], op.getOperationName());
                assertEquals(OP_RETURN_TYPES_1[i], op.getReturnType());
                assertEquals(OP_ONE_WAY_1[i], op.isOneWay());
                List<ParamModel> params = op.getParams();
                int j=0;
                    for (ParamModel param:params) {
                        assertEquals(PARAM_NAMES_1[i][j], param.getName());
                        assertEquals(PARAM_TYPES_1[i][j], param.getParamType());
                        assertEquals(PARAM_MODES_1[i][j], param.getMode());
                        j++;
                    }
                i++;
            }
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
