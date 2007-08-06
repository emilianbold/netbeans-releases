/*
 * EntityRESTServicesCodeGeneratorTest.java
 * JUnit 4.x based test
 *
 * Created on May 17, 2007, 8:31 PM
 */

package org.netbeans.modules.websvc.rest.codegen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.framework.Assert;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nam
 */
public class EntityRESTServicesCodeGeneratorTest extends TestBase {
    public EntityRESTServicesCodeGeneratorTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testGenerate() throws Exception {
        getEntities();
        List<JavaSource> entities = new ArrayList<JavaSource>();
        for (FileObject child : entityClassDirFO.getChildren()) {
            if (! child.isData() || ! "java".equals(child.getExt())) {
                continue;
            }
            FileObject entityFO =  entityClassDirFO.getFileObject(child.getNameExt());
            JavaSource js = JavaSource.create(ClasspathInfo.create(
                    defaultCPP.getBootClassPath(), defaultCPP.getCompileClassPath(), defaultCPP.getSourceClassPath()), 
                    Collections.singleton(entityFO));
            assertNotNull("can't get javasource for "+entityFO.getPath(), js);
            if (js != null && JavaSourceHelper.isEntity(js)) {
                entities.add(js);
            }
        }
        /*EntityRESTServicesCodeGenerator gen = new EntityRESTServicesCodeGenerator(
                entities, entityClassDirFO,  PACKAGE_ACME, "AcmePU");*/
        
        //FIXME faile on generic java template processing on include of default-license.text
        //gen.generate();
    }

    public void testSingularize() {
        //assertEquals("Product", Util.singularize("Products"));
        assertEquals("Product", Util.singularize("Product"));
        //assertEquals("Products", Util.pluralize("Products"));
        assertEquals("Products", Util.pluralize("Product"));
        //assertEquals("Address", Util.singularize("Addresses"));
        //assertEquals("Address", Util.singularize("Address"));
        assertEquals("Addresses", Util.pluralize("Address"));
        //assertEquals("Addresses", Util.pluralize("Addresses"));
    }
}
