package org.netbeans.modules.e2e.wsdl.wsdl2java;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import junit.framework.*;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Lukas Waldmann
 */
public class WSDL2JavaImplTest extends TestCase
{
    final String outputDir = "C:\\WSDL";
    final String wsdlFile  = "C:\\WSDL\\QuizService.wsdl";
    final String pack      = "test";
            
    Set<String> generatedFiles;
    HashMap<String,Object> report=new HashMap<String,Object>();
    
    public WSDL2JavaImplTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        WSDL2Java.Configuration conf=new WSDL2Java.Configuration();
        conf.setOutputDirectory(outputDir);
        conf.setWSDLFileName(wsdlFile);
        conf.setPackageName(pack);
        org.netbeans.modules.e2e.wsdl.wsdl2java.WSDL2JavaImpl instance = new WSDL2JavaImpl(conf);
        
        boolean result = instance.generate();
    }

    protected void tearDown() throws Exception
    {
    }

    /**
     * Test of generate method, of class org.netbeans.modules.e2e.wsdl.wsdl2java.WSDL2JavaImpl.
     */
    public void testGenerate()
    {
        System.out.println("generate");
            
        for (String fName : generatedFiles)
        {            
            HashMap<String,Object> expectedClasses=(HashMap<String,Object>)report.remove(fName);
            assertNotNull("File "+fName+ " was not expected to be generated",expectedClasses);
            
            File file = new File(outputDir+File.separator+fName);
            ClassExplorer explorer=new ClassExplorer(FileUtil.toFileObject(file));
            List<TypeElement> generatedTypes=explorer.getClasses();
            assertTrue("Generated classes for file "+fName+" doesn't match expected",explorer.compareClasses(expectedClasses.keySet(),generatedTypes));
            for (TypeElement type : generatedTypes)
            {
                HashMap<String,Object> expectedMethods=(HashMap<String,Object>)expectedClasses.remove(type.toString());
                Set<String> methods=new HashSet<String>();
                HashMap<String,String> retTypes=new HashMap<String,String>();
                for (String metret : expectedMethods.keySet())
                {
                    int pos=metret.indexOf('|');
                    String method=metret.substring(0,pos-1);
                    methods.add(method);
                    retTypes.put(method,metret.substring(pos+1));
                }
                List<ExecutableElement> generatedMethods=explorer.getMethods(type);
                assertTrue("Generated methods for class "+type.toString()+" doesn't match expected",explorer.compareMethods(methods,generatedMethods));
                for (ExecutableElement method: generatedMethods)
                {
                    //Check return type
                    assertEquals("Return type of method "+method.toString()+" doesn't match expected",retTypes.get(method.toString()),explorer.getReturnType(method));
                    
                    Set<String> expectedParameters=(Set<String>)expectedMethods.remove(method.toString()+'|'+retTypes.get(method.toString()));
                    assertNotNull("Method "+method.toString()+ " was not expected to be generated",expectedParameters);
                    
                    List<TypeMirror> generatedParameters=explorer.getParameters(method);
                    assertTrue("Generated classes for file "+fName+" doesn't match expected",explorer.compareParameters(expectedParameters,generatedParameters));                    
                }
                if (!expectedMethods.isEmpty())
                {
                    fail("Following methods " +expectedMethods.keySet()+" of class "+type.toString()+" were not generated for file "+fName);
                }
            }
            if (!expectedClasses.isEmpty())
            {
                fail("Following classes "+expectedClasses.keySet()+" were not generated for file "+fName);
            }
        }
        if (!report.isEmpty())
        {
            fail("Following files were not generated: "+report.keySet());
        }
    }
}
