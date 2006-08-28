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
 * LoaderPoolTest.java
 *
 * Tests adding and removing of DataLoaders and some other related stuff.
 * Should run in fresh instance of IDE.
 *
 * Created on May 23, 2001, 1:42 PM
 */

package DataLoaderTests.LoaderPoolTest;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.Lookup;

import org.netbeans.core.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.*;
import java.util.jar.Attributes;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import junit.framework.*;
import org.netbeans.junit.*;

public class LoaderPoolTest extends NbTestCase 
                            implements DataLoader.RecognizedFiles 
{
    
    /** Creates new LoaderPoolTest */
    public LoaderPoolTest(java.lang.String testName) {
        super(testName);
    }
    
    boolean successful = true;
    
    LoaderPoolNode LPN = null;
    Repository Rep = null;
    DataLoaderPool DLP = null;
    DataLoader javadl = null; //JavaDataLoader
    DataLoader textdl = null; //TXTDataLoader
    LoaderPoolTest.ChL changel = null; //change listener for changes over DataLoderPool
    Enumeration en = null;
    
    int noOfChanges = 0; //count of changes over DataLoaderPool
    
    /**This methods write an output to log stream*/
    public void writeLog(String text) {
        log(text);
        System.out.println(text);
        if (text.equals(FAILED)) successful = false;
    }
    
    /**This methods write an output to reference stream*/
    public void writeRef(String text) {
        ref(text);
        System.out.println(text);
        if (text.equals(FAILED)) successful = false;
    }
    
    /**This methods write an output to reference stream and asserts success of the test fragment*/
    public void writeRef(String text, String inf) {
        ref(text);
        System.out.println(text);
        if (inf.equals(FAILED)) successful = false;
        assertTrue(text,successful);
    }
    
    /**If enabled, prints exception to the output and to the ref stream*/
    void printException(Exception e) {
        if(PRINT_EXCEPTIONS){
            e.printStackTrace();
            e.printStackTrace(getRef());
        }
    }
    

    /**overrides parent definition of this methot,
     *so this new works in this way - returns work filed
     *that should have been set by user of this utility class
     */
    public String getWorkDirPath() {
        if (work == null) fail("Working directory not set!");
        //always return what a someone else has set
        return work;
    }        
    
    /**
     *Performs initializing before own tests starts
     */
    void prepare() {
        try{
            //initializing ide
//            TopManager.getDefault();
            
            //when not in XTest harness -> woring directory will be under actual userdir
            if (Manager.getWorkDirPath()==null) System.setProperty("nbjunit.workdir",System.getProperty("netbeans.user"));
            //clearWorkDir();
            noOfChanges = 0;
            Rep = (Repository) Lookup.getDefault().lookup(Repository.class);
            
            //mounting filesystem
            String str = null;
            java.net.URL url = new LoaderPoolTest("x").getClass().getResource("LoaderPoolTest.class");
            if (url.getProtocol().equals("nbfs")) {
                //this allows tests to be executed inside running ide (ide mode)
             //   str = (FileUtil.toFile(org.openide.execution.NbfsURLConnection.decodeURL(url))).getAbsolutePath();
                fail("nbfs is not handled");
            }
            else str = url.getPath(); //else test executed in code mode
            str = str.substring(0,str.indexOf(new LoaderPoolTest("x").getClass().getPackage().getName().replace('.','/')));
            java.io.File ff = new java.io.File(str);
            org.openide.filesystems.LocalFileSystem lfs = new org.openide.filesystems.LocalFileSystem();
            lfs.setRootDirectory(ff);
            Rep.addFileSystem(lfs);            
            
            DLP = DataLoaderPool.getDefault ();
            //following doesn't work in nongui mode ;-(
            // XXX Places are deprecated at all but the repository node (RepositoryNodeFactory), 
            // The rest is not useful.
//            LPN = (LoaderPoolNode) ((Places)Lookup.getDefault().lookup(Places.class)).nodes().loaderPool();
//            changel = new LoaderPoolTest.ChL(this);
//            DLP.addChangeListener( changel );
        }catch(Exception e){
            e.printStackTrace();
            e.printStackTrace(getRef());
            assertTrue("Initialization of test failed! ->" + e,false);
        }
    }
    
    /**
     *Performs clean up
     */
    public  void clean() {
        //getRef().flush();
        //getRef().close();
        
        DLP.removeChangeListener( changel );
        noOfChanges = 0;
        Rep = null;
        DLP = null;
        LPN = null;
        changel = null;
    }
    
    /**
     *Performs waiting of current thread for time in millis
     *@param millist integer number - time in millis to wait
     */
    void dummyWait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            printException(ex);
        }
    }
    
    /**
     *Listener for changes over LoaderPool
     */
    class ChL implements ChangeListener{
        LoaderPoolTest parent = null;
        public ChL(LoaderPoolTest lp){
            super();
            parent=lp;
        }
        public void stateChanged(ChangeEvent e) {
            writeRef("\nSome change over DataLoaderPool has happend, added or removed DataLoader!");
            parent.noOfChanges ++;
        }
    }
    
    /**
     *Gets the specified DataLoader
     *@param DLDisplayNameSubstr substring of the DataLoader's DisplayName
     *@returns DataLoader or null
     */
    public DataLoader getDataLoader(String DLDisplayNameSubstr) {
        if ( DLDisplayNameSubstr != null ) {
            en = DLP .allLoaders();
            while ( en.hasMoreElements() ) {
                DataLoader dl = (DataLoader) en.nextElement();
                if ( dl.getDisplayName().indexOf(DLDisplayNameSubstr) != -1 ) return dl;
            }
        }
        writeRef("\n" + DLDisplayNameSubstr + " loader not found in the LoaderPool!");
        return null;
    }
    
    /**
     *Goes through all registered DataLoaders, firstProducerOf, ProducerOf,
     *gets and sets PrefferedLoader for java source file
     */
    void exploreDataLoaderPool() throws Exception {
        
        writeRef("\nListing all registred DataLoaders ...");
        en = DLP .allLoaders();
        while ( en.hasMoreElements() ) {
            DataLoader dl = (DataLoader) en.nextElement();
            String str = dl.toString();
            System.out.println( dl.getDisplayName() + " / " + str.substring(0,str.indexOf('@')) );
            if ( dl.getDisplayName().indexOf("Java Source") != -1 ) javadl = dl;
            if ( dl.getDisplayName().indexOf("Textual") != -1 ) textdl = dl;
        }
        writeRef(PASSED);
        
        writeRef("\nGetting firstProducerOf ...");
        DataLoader[] dla = DLP.toArray();
        for ( int i = 0 ; i < dla.length ; i ++ ) {
            String str = (DLP.firstProducerOf(dla[i].getRepresentationClass()) ).toString();
            System.out.println(str.substring(0,str.indexOf('@')));
        }
        writeRef(PASSED);
        
        writeRef("\nGetting ProducerOf ...");
        for ( int i = 0 ; i < dla.length ; i ++ ) {
            Enumeration e = DLP.producersOf(dla[i].getRepresentationClass());
            while (e.hasMoreElements()) {
                String str = e.nextElement().toString();
                System.out.println(str.substring(0,str.indexOf('@')));
            }
            System.out.println("*");
        }
        writeRef(PASSED);
        
        writeRef("\nGetting prefered DataLoader for java source file ...");
        //some java file
        String name = "DataLoaderTests/DataObjectTest/data/ClassObject.java";
        
        
        FileObject fo = toFileObject(name);
        writeRef(fo.toString());
        if ( DataLoaderPool.getPreferredLoader( fo ) == null ) writeRef("\nnull");
        else {
            //writeRef("Check this, should be null.");
            String str = DataLoaderPool.getPreferredLoader( fo ) .toString();
            writeRef(str.substring(0,str.indexOf('@')));
        }
        writeRef(PASSED);
        
        writeRef("\nSetting prefered DataLoader for java source file ...");
        DataLoaderPool.setPreferredLoader( fo , javadl );
         writeRef(PASSED);
        
        writeRef("\nGetting prefered DataLoader for java source file ...");
        writeRef(fo.toString());
        //temp. disabled
        //String str = DataLoaderPool.getPreferredLoader( fo ) .toString();
        //writeRef(str.substring(0,str.indexOf('@')));
        writeRef(PASSED);
    }
    
   static LocalFileSystem lfs ;  
  
    private static FileObject toFileObject(String fileName ) throws Exception {
        if (lfs == null ) {
            lfs = new LocalFileSystem();
            String xtestData = System.getProperty("xtest.data");
            if (! xtestData.endsWith(File.separator) ) {
                xtestData = xtestData + "/";
            }
            lfs.setRootDirectory(new File (xtestData));
        }
  /*      System.out.println("filename:" + fileName );
        fileName = xtestData + fileName.replace('/',File.separatorChar);
        System.out.println("fileName:" + fileName);
        File file = new File(fileName);
        if (file.exists() == false ) {
            throw new FileNotFoundException(fileName);
        }*/
        FileObject fo = lfs.findResource(fileName);
//        FileObject fo = org.openide.filesystems.FileUtil.toFileObject(new File(fileName));        
        if (fo == null) {
            throw new FileNotFoundException ("fo:" + fileName);
        }
        return fo;
    }
    /**
     *@param file is path to the file in the repository
     *@param dl is DataLoader
     */
    void createDataObject(String fileName, DataLoader dl) throws Exception {
        writeRef("\nCreating DataObject for file " + fileName.substring(fileName.lastIndexOf('/')+1) + " ...");
        
        dl.findDataObject (toFileObject(fileName), this );
    }
    
    /**
     *For specified file tests if there is an exception thrown when creating
     *DataObject using passed DataLoader (assuming that the DataObject exists
     *the exception will be DataObjectExistsException).
     *@param file is path to the file in the repository
     *@param dl is DataLoader
     */
    void notCreateDataObject(String file, DataLoader dl) throws Exception {
        writeRef("\nnotCreating DataObject for file " + file.substring(file.lastIndexOf('/')+1) + " ...");
        FileObject fo = toFileObject(file);
        
        try {
           dl.findDataObject (fo, this );
            writeRef("NotCreating DataObject failed!",FAILED);
        } catch (Exception ex) {
            printException(ex);
            writeRef(PASSED);
        }
    }
    
    /**
     *Tests if for specified file exists DataObject
     *@param file path to the file in the repository
     *@return true if exists
     */
    boolean existDataObject(String fileName) throws Exception {
        writeRef("\nDataObject for file " + fileName.substring(fileName.lastIndexOf('/')+1) + " should exist ...");
        FileObject fo = toFileObject(fileName);
        try {
            DataObject.find(fo);
            writeRef(PASSED);
            return true;
        } catch (Exception ex) {
            printException(ex);
            writeRef("DataObject should exist but doesn't!",FAILED);
            return false;
        }
    }
    
    /**
     *Tests if for specified file does not exist DataObject
     *@param file path to the file in the repository
     *@return true if does not exist
     */
/*    boolean notExistDataObject(String file){
        writeRef("\nDataObject for file " + file.substring(file.lastIndexOf('/')+1) + " should not exist ...");
        FileObject fo = FileUtil.toFileObject(new File(file));
        try {
            DataObject.find(fo);
            writeRef("DataObject shouldn't exit but does!",FAILED);
            return false;
        } catch (Exception ex) {
            printException(ex);
            writeRef(PASSED);
            return true;
        }
    }*/
    
    
    /**
     *For passed file finds DataObject and verify if it was created with the desired DataLoader
     *@param file path to the file in the repository
     *@param loaderDisplayName DisplayName of desired loader
     *@return true if the two DisplayNames are identical
     */
    boolean recognizedAs(String fileName, String loaderDisplayName) throws IOException {
        writeRef("\nFile " + fileName.substring(fileName.lastIndexOf('/')+1) + " should be recognized as " + loaderDisplayName + " ...");
        File file = new File(fileName);
        if (file.exists() == false) {
            throw new FileNotFoundException (fileName);
        }
        FileObject fo = FileUtil.toFileObject(file);
        try {
            boolean status = DataObject.find(fo).getLoader().getDisplayName().indexOf(loaderDisplayName) != -1;
            //System.out.println(DataObject.find(fo).getLoader().getDisplayName());
            writeRef(PASSED);
            return status;
        } catch (Exception ex) {
            printException(ex);
            writeRef("File should be recognized as "+ loaderDisplayName +" but isn't!",FAILED);
            return false;
        }
    }
    
    /**
     *For passed file finds DataObject and verify if it was not created with the desired DataLoader
     *@param file path to the file in the repository
     *@param loaderDisplayName DisplayName of desired loader
     *@return true if the two DisplayNames differ
     */
    boolean notRecognizedAs(String file, String loaderName){
        writeRef("\nFile " + file.substring(file.lastIndexOf('/')+1) + " should not be recognized as " + loaderName + " ...");
        FileObject fo = FileUtil.toFileObject(new File(file));
        try {
            boolean status = DataObject.find(fo).getLoader().getDisplayName().indexOf(loaderName) == -1;
            //System.out.println(DataObject.find(fo).getLoader().getDisplayName());
            writeRef(PASSED);
            return status;
        } catch (Exception ex) {
            printException(ex);
            writeRef("File shouldn't be recognized as "+ loaderName +" but is!",FAILED);
            return false;
        }
    }
    
    /**
     *Removes DataLoader from DataLoaderPool
     *@param dl DataLoader
     */
    void removeDataLoader( DataLoader dl ){
        writeRef("\nRemoving DataLoader ...");
        writeRef(dl.getRepresentationClass().toString());
        LPN.remove( dl );
        writeRef(PASSED);
    }
    
    /**
     *Adds DataLoader(s) to DataLoaderPool
     *@param module name of the module containing loder(s) (name of the jar without extension)
     */
    void addDataLoader( String module ){
        writeRef("\nAdding DataLoaders from LoadersSection from the module manifest ...");
        try{
            org.netbeans.ModuleManager mm = org.netbeans.core.NbTopManager.get().getModuleSystem().getManager();
            org.netbeans.Module m = mm.get(module);
            
            System.out.println("Got the Module: " + m.toString());
            
            HashSet mysections = new HashSet(25); // Set<ManifestSection>
            Iterator it = m.getManifest().getEntries().entrySet().iterator(); // Iterator<Map.Entry<String,Attributes>>
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                org.netbeans.core.startup.ManifestSection section = org.netbeans.core.startup.ManifestSection.create((String)entry.getKey(), (Attributes)entry.getValue(), m);
                if (section != null) {
                    mysections.add(section);
                }
            }
            
            System.out.println("Got all sections from manifest: " + mysections.toString());
            
            it = mysections.iterator();
            while (it.hasNext()) {
                org.netbeans.core.startup.ManifestSection sect = (org.netbeans.core.startup.ManifestSection)it.next();
                if (sect instanceof org.netbeans.core.startup.ManifestSection.LoaderSection) {
                    System.out.println("Got the LoaderSection: " + ((org.netbeans.core.startup.ManifestSection.LoaderSection)sect).toString() );
                    LPN.add((org.netbeans.core.startup.ManifestSection.LoaderSection)sect);
                }
            }
            
            System.out.println("Loader should be added.");
            
            writeRef(PASSED);
        }catch(Exception ex){
            printException(ex);
            writeRef("Adding of DataLoader failed!",FAILED);
        }
    }
    
    /**
     *Prints extensions that specified loader will recognize
     *@param ufl UniFileLoader
     */
    void printExtensions(UniFileLoader ufl){
        if ( ufl != null ) {
            writeRef("\nGetting registered file extension within this Loader ...");
            en = ufl .getExtensions() .extensions();
            while ( en.hasMoreElements() ) writeRef(en.nextElement().toString());
            writeRef(PASSED);
        }
    }
    
    /**
     *Prints extensions that specified loader will recognize
     *@param ufl UniFileLoader
     */
    boolean refPrintExtensions(DataLoader dl){
        if ( dl != null ) {
            writeRef("\nGetting registered file extension within this Loader ...");
            try {
                //java.lang.reflect.Method[] ms = dl.getClass().getMethods();
                //for(int i = 0 ; i < ms.length ; i ++) System.out.println(ms[i]);
                java.lang.reflect.Method mm = dl.getClass().getMethod("getExtensions", new Class[0]);
                Object obj =  mm.invoke(dl,new Class[0]);
                en = ( (ExtensionList) obj).extensions();
                while ( en.hasMoreElements() ) writeRef(en.nextElement().toString());
                writeRef(PASSED);
                return true;
            }catch (Exception ex){
                printException(ex);
                writeRef("Printing extensions failed!",FAILED);
            }
        } return false;
    }
    
    /**
     *Two ways should return the same
     */
    void compareTest(){
        // See above XXX.
//        writeRef("\nComparing instances of NbLoaderPool ...");
//        //dlp should be NbLoaderPool
//        LoaderPoolNode.NbLoaderPool nblp = (LoaderPoolNode.NbLoaderPool) DLP;
////        LoaderPoolNode lpn = (LoaderPoolNode) ((Places)Lookup.getDefault().lookup(Places.class)).nodes().loaderPool();
//        LoaderPoolNode.NbLoaderPool nblp_temp = lpn.getNbLoaderPool();
//        //nblp and nblp_temp should be the same
//        if ( ! nblp.equals(nblp_temp) ) writeRef("Got two different instances of DataLoaderPool!",FAILED);
//        else writeRef(PASSED);
    }
    
    /**
     *Will consume specified amount of memory.
     *Method: create String[amount]
     *@param amount dimension of String array
     */
    void eatMemory(int amount){
        writeRef("\nConsuming memory ...");
        String arr[] = new String[amount];
        for (int i = 0 ; i < amount ; i ++)  arr[i] = "Something realy stupid here ;-) ...";
        writeRef(" Done.");
    }
    
    /**
     *Tries to invoke garbage collection.
     *Method: 3xgc, sleep 5s, 1xgc, sleep 5s
     */
    void sweepMemory(){
        writeRef("\nSweeping memory ...");
        for (int i = 0 ; i < 3 ; i ++) System.gc();
        try {
            Thread.currentThread().sleep(5000);
        }catch(Exception ex) {}
        System.gc();
        try {
            Thread.currentThread().sleep(5000);
        }catch(Exception ex) {}
        writeRef(" Done.");
    }
    
    
    
    /**ONLY FOR TESTING PURPOSES
     *REAL TESTS ARE IN SEPARATED CLASSES
     */
    public static void main(String args[]) {
        
        LoaderPoolTest lpt = new LoaderPoolTest("x");
        
        try{
            
            lpt.prepare();
            
            lpt.writeRef(new LoaderPoolTest("x").getClass().getName());
            
            //removeDataLoader(getDataLoader("Text"));
            lpt.addDataLoader("org.netbeans.modules.text");
            
            lpt.clean();
            
            System.out.println("\n" + lpt.successful );
            
        }catch(Throwable ee){
            ee.printStackTrace(lpt.getRef());
            ee.printStackTrace();
            lpt.writeRef(lpt.FAILED);
        }
        
        lpt.assertTrue(lpt.successful);
        
    }
    
    public void markRecognized(FileObject fo) {
    }
    
    //if you want print exceptions into log file, put here true.
    public  final boolean PRINT_EXCEPTIONS = true;
    
    public  final String PASSED = "passed.";
    public  final String FAILED = "failed.";
    //workdir, this hould be set by an user of this class
    //user of this class shouldn't use their own ref and log
    public static String work = null;
}
