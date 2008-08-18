/*
 * GenericResourceCodeGeneratorTest.java
 * JUnit 4.x based test
 *
 * Created on May 30, 2007, 11:49 AM
 */

package org.netbeans.modules.websvc.rest.codegen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.support.ZipUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ayubskhan
 */
public class ClientStubsGeneratorTest extends TestBase {
    
    public ClientStubsGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpSrcDir();
        FileObject logDir = FileUtil.createFolder(new File(getWorkDir(), ".netbeans/var/log"));
    }
    
    public void testModelFromWadl() throws Exception {
        String appName = "WebApplication1";
        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".xml");
        ClientStubModel m = new ClientStubModel();
        String url = m.buildModel(is);
        assertEquals(url, "http://localhost:8080/"+appName+"/resources/");
        assertEquals(m.getResources().size(), 6);
    }
    
    public void testGenerateFromWadl() throws Exception {
        String appName = "WebApplication1";
        FileObject stubRoot = FileUtil.createFolder(getWorkDir());
        String folder = "rest";
        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".xml");
        ClientStubsGenerator cs = new ClientStubsGenerator(stubRoot, folder, is, true);
        cs.generate(null);
        
        FileObject restFolder = stubRoot.getFileObject(folder);
        File zipFile = new File(FileUtil.toFile(restFolder), appName.toLowerCase()+"_1.zip");
        if(zipFile.exists()) //clean
            zipFile.delete();
        FileObject appFolder = restFolder.getFileObject(appName.toLowerCase());
        String[] sources = {
            FileUtil.toFile(appFolder).getAbsolutePath()
        };
        String[] paths = {
            ""
        };
        ZipUtil zipUtil = new ZipUtil();
        zipUtil.zip(zipFile, sources, paths);
        
        File base = new File(FileUtil.toFile(restFolder), appName.toLowerCase()+".zip");
        FileUtil.copy(this.getClass().getResourceAsStream(appName.toLowerCase()+".zip"), new FileOutputStream(base));

        assertEquals(zipFile.length(), base.length());
    }
    
    //Tests dont work due to exception from Retouche
//    public void testModelFromProject() throws Exception {
//        String appName = "WebApplication2";
//        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".zip");
//        FileObject work = FileUtil.createFolder(getWorkDir());
//        ZipUtil zipUtil = new ZipUtil();
//        zipUtil.unzip(is, work, true);
//        Project p = FileOwnerQuery.getOwner(work.getFileObject(appName));
//        ClientStubModel m = new ClientStubModel();
//        m.buildModel(p);
//        assertEquals(m.getResources().size(), 6);
//    }
//    
//    public void testGenerateFromProject() throws Exception {
//        String appName = "WebApplication2";
//        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".zip");
//        FileObject work = FileUtil.createFolder(getWorkDir());
//        ZipUtil zipUtil = new ZipUtil();
//        zipUtil.unzip(is, work, true);
//        Project p = FileOwnerQuery.getOwner(work.getFileObject(appName));
//        FileObject stubRoot = FileUtil.createFolder(getWorkDir());
//        String folder = "rest";
//        ClientStubsGenerator cs = new ClientStubsGenerator(stubRoot, folder, p, false, true);
//        cs.generate(null);
//        
//        FileObject restFolder = stubRoot.getFileObject(folder);
//        File zipFile = new File(FileUtil.toFile(restFolder), appName.toLowerCase()+"_1.zip");
//        if(zipFile.exists()) //clean
//            zipFile.delete();
//        FileObject appFolder = restFolder.getFileObject(appName.toLowerCase());
//        String[] sources = {
//            FileUtil.toFile(appFolder).getAbsolutePath()
//        };
//        String[] paths = {
//            ""
//        };
//        zipUtil.zip(zipFile, sources, paths);
//        
//        File base = new File(FileUtil.toFile(restFolder), appName.toLowerCase()+".zip");
//        FileUtil.copy(this.getClass().getResourceAsStream(appName.toLowerCase()+".zip"), new FileOutputStream(base));
//
//        assertEquals(zipFile.length(), base.length());
//    }
}
