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
 * DataObjectTest.java
 *
 * Here are implemented separated tests methos, testing manipulating operations with DataObejcts.
 * Other classes in this packages use them in the logic chain to create meaningfull testcases.
 *
 * Created on June 13, 2001, 1:19 PM
 */

package DataLoaderTests.DataObjectTest;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataNode;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.loaders.DataObjectNotFoundException;


public class DataObjectTest extends NbTestCase {
    
    DataFolder resources = null;
    /**
     * teporary data folder 
     */
    DataFolder temp = null;
    
    /** Creates new DataObjectTest */
    public DataObjectTest(java.lang.String testName) {
        super(testName);
    }
    
    boolean successful = true;
    
    
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
   //     if(PRINT_EXCEPTIONS) {
            e.printStackTrace();
            e.printStackTrace(getRef());
     //   }
    }
    
    String exceptionToString(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        pw.close();
        return writer.toString();
    }

    /**overrides parent definition of this methot,
     *so this new works in this way - returns work filed
     *that should have been set by user of this utility class
     */
    public String getWorkDirPath()  {
        if (work == null) fail("Working directory not set!");
        //always return what a someone else has set
        return work;
    }            
    
    /**
     *Performs initializing before own tests starts
     */
    void prepare() throws Exception {
            //initializing ide
            
            //when not in XTest harness -> woring directory will be under actual userdir
            successful = true;

            String xtestData = System.getProperty("xtest.data");
            File dataDir = new File (xtestData,"DataObjectTest");
            assertTrue(dataDir.exists());
            if (dataDir.exists() == false ) {
                throw new FileNotFoundException(dataDir.getPath());
            }
            FileObject fo = FileUtil.toFileObject(dataDir);
            assertNotNull(fo);
            resources = DataFolder.findFolder(fo);
            System.err.println("resources:" + resources);
            
            temp = DataFolder.findFolder(FileUtil.toFileObject(new File (System.getProperty("xtest.tmpdir"))));
            assertNotNull("No temporary folder found.",temp);
    }
    
    /**
     *Performs clean up
     */
    void clean() {
        //Put some cleaning stuff here ...
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
    
    DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        try{
            dob = DataObject.find(fo);
        }catch(Exception ex){
            printException(ex);
            writeRef("DataObject.find has failed!",FAILED);
            return null;
        }
        DataLoader loader = dob.getLoader();
        
        return getDataObject(fo,loader.getDisplayName());
    }
    
    DataObject getDataObject(FileObject fo, String moduleName) {
        writeRef("\nGetting DataObject started ...");
        DataLoader loader = null;
        try {
            loader = new DataLoaderTests.LoaderPoolTest.LoaderPoolTest("x").getDataLoader(moduleName);
        }catch(Exception ex){
            //simple do nothing
        }
        DataObject do1 = null;
        try{
            do1 = DataObject.find(fo);
        }catch(Exception ex){
            printException(ex);
            writeRef("DataObject.find has failed",FAILED);
            return null;
        }
        
        
        DataLoader loader2 = do1.getLoader();
        if ((loader!=null) && (loader2!=null)) {
            if (! loader.equals(loader2)) {
                writeRef("DLs do not equals!",FAILED);
                return null;
            } else {
                writeRef(PASSED);
                return do1;
            }
        } else {
            if (loader2 != null) {writeRef(PASSED);return do1;}
        }
        writeRef("Both DLs null!?!",FAILED);
        return null;
    }
    
    boolean containsFO(DataObject dob, FileObject fob){
        writeRef("\nDoes files contain FO ...");
        java.util.Iterator it = dob.files().iterator();
        while (it.hasNext()) {
            if (fob.equals(it.next())) {
                writeRef(PASSED);
                return true;
            }
        }
        writeRef("Files() doesn't contain the FO!",FAILED);
        return false;
    }
    
    boolean isInFolder(DataObject dob, DataFolder df){
        writeRef("\nIs this DO in that DF ...");
        if (dob.getFolder().equals(df)) {
            writeRef(PASSED);
            return true;
        } else{
            writeRef("This DO isn't in the DF!",FAILED);
            return false;
        }
    }
    
    boolean testHelpCtx(DataObject dob){
        writeRef("\nAsking for HelpCtx ...");
        HelpCtx hc = dob.getHelpCtx();
        assertTrue(dob.isValid());
        System.out.println(dob.getClass());
         
        if ( ( (dob instanceof DataFolder) && hc == null ) ||
             ( (hc.getHelpID()==null)&&(hc.getHelp()!=null) )  ||
             ( (hc.getHelpID()!=null)&&(hc.getHelp()==null) ) ) {
            writeRef(PASSED);
            return true;
        } else{
            writeRef("HelpCtx error!",FAILED);
            return false;
        }
    }
    
    void getName(DataObject dob){
        writeRef("\nGetting name ...");
        writeRef("\n" + dob.getName());
        writeRef(PASSED);
    }
    
    void testDelegate(DataObject dob){
        //very simple test this test tests DO not Nodes ;-)
        writeRef("\nTesting DataObject's node delegate ...");
        DataNode dn = (DataNode) dob.getNodeDelegate();
        
        DataObjectTest.PChL l = new DataObjectTest.PChL();
        
        dob.addPropertyChangeListener(l);
        
        writeRef("\nDisplayName: " + dn.getDisplayName());
        writeRef("Name: " + dn.getName());
        writeRef("ShortDescription: " + dn.getShortDescription());
        writeRef("ShowFileExtensions: " + dn.getShowFileExtensions());
        writeRef("Preferred: " + dn.isPreferred());
        //writeRef("ParentNode name: " + dn.getParentNode().getName());
        writeRef("Expert: " + dn.isExpert());
        writeRef("Hidden: " + dn.isHidden());
        writeRef("Leaf: " + dn.isLeaf());
        
        String dsn = dn.getDisplayName();
        String n = dn.getName();
        String sd = dn.getShortDescription();
        boolean sfe = dn.getShowFileExtensions();
        boolean p = dn.isPreferred();
        
        //new settings
        dn.setDisplayName("Oleee");
        dn.setName("Hmmm",true);
        dn.setShortDescription("A short description.");
        dn.setShowFileExtensions(true);
        dn.setPreferred(true);
        
        writeRef("\nnew DisplayName: " + dn.getDisplayName());
        writeRef("new Name: " + dn.getName());
        writeRef("new ShortDescription: " + dn.getShortDescription());
        writeRef("new ShowFileExtensions: " + dn.getShowFileExtensions());
        writeRef("new Preferred: " + dn.isPreferred());
        
        dummyWait(1000);
        
        dob.removePropertyChangeListener(l);
        
        //restoring old settings
        dn.setDisplayName(dsn);
        dn.setName(n,true);
        dn.setShortDescription(sd);
        dn.setShowFileExtensions(sfe);
        dn.setPreferred(p);
        
        writeRef(PASSED);
    }
    
    class PChL implements java.beans.PropertyChangeListener{
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            writeRef("\nFrom DO's property listener: " + propertyChangeEvent.getPropertyName() +
            "(" + propertyChangeEvent.getOldValue() + " -> " +
            propertyChangeEvent.getNewValue() + ")");
        }
    }
    
    class VChL implements java.beans.VetoableChangeListener{
        public void vetoableChange(java.beans.PropertyChangeEvent propertyChangeEvent) throws java.beans.PropertyVetoException {
            if ( DataObject.PROP_VALID.equals(propertyChangeEvent.getPropertyName()) &&
            (propertyChangeEvent.getNewValue()== Boolean.FALSE) ) {
                writeRef("\nGoing to veto this change ...");
                throw new java.beans.PropertyVetoException("This change is not allowed ;-)", propertyChangeEvent);
            } else {
                writeRef("\nNot vetoing this change.");
            }
        }
    }
    
    boolean inModifiedContainer(DataObject dob){
        writeRef("\nShould be in modified container ...");
        java.util.Iterator it = dob.getRegistry().getModifiedSet().iterator();
        while (it.hasNext()) {
            if (dob.equals(it.next())) {
                writeRef(PASSED);
                return true;
            }
        }
        writeRef("This DO isn't in modified container!",FAILED);
        return false;
    }
    
    boolean notInModifiedContainer(DataObject dob){
        writeRef("\nShould not be in modified container ...");
        java.util.Iterator it = dob.getRegistry().getModifiedSet().iterator();
        while (it.hasNext()) {
            if (dob.equals(it.next())) {
                writeRef("This DO is in modified container!",FAILED);
                return false;
            }
        }
        writeRef(PASSED);
        return true;
    }
    
    class ChL implements javax.swing.event.ChangeListener{
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            writeRef("\nSome change over set of modified DOs!");
            writeRef("Registry: " + e.getSource().toString());
        }
    }
    
    public  boolean checkModifyAbility(DataObject dob) throws Exception {
        writeRef("\nChecking modify marking facility ...");
        javax.swing.event.ChangeListener l = new DataObjectTest.ChL();
        dob.getRegistry().addChangeListener(l);
        performDelete(dob);
        dummyWait(1000);
        if(!dob.isModified()){
            if(notInModifiedContainer(dob)){
                writeRef("\nMarking as modified ...");
                dob.setModified(true);
                dummyWait(1000);
                if(inModifiedContainer(dob)){
                    writeRef(PASSED);
                    writeRef("\nMarking as not modified ...");
                    dob.setModified(false);
                } else {
                    writeRef("Now should be in modified container but isn't!",FAILED);
                    return false;
                }
            } else {
                writeRef("Modified but not in the modified registry!",FAILED);
                return false;
            }
        } else {
            writeRef("I have thought that it shouldn't be in modified registry.!",FAILED);
            return false;
        }
        dummyWait(1000);
        if(!notInModifiedContainer(dob)){
            writeRef("Shouldn't be in modified registry!!",FAILED);
            return false;
        } else writeRef(PASSED);
        dummyWait(1000);
        dob.getRegistry().removeChangeListener(l);
        writeRef(PASSED);
        return true;
    }
    
    public  void checkValidity(DataObject dob) throws Exception {
        writeRef("\nTesting validity ...");
        if (!dob.isValid()) {
            writeRef("DO have to be valid for this test!!",FAILED);
            return;
        }
        DataObject newDO = null;
        if (dob.isCopyAllowed()) {
            newDO = dob.copy(temp);
        } else {
            writeRef("\nCopy not allowed!");
            return;
        }
        if (!newDO.isValid()) {
            writeRef("Newly created working DO have to be valid for this test!",FAILED);
            return;
        }
        DataObjectTest.VChL v = new DataObjectTest.VChL();
        newDO.addVetoableChangeListener(v);
        writeRef("\nChecking vetoableChangeListener ...");
        try{
            newDO.setValid(false);
            writeRef("This change should have been vetoed!",FAILED);
        }catch(Exception ex){
            writeRef(ex.getMessage());
            writeRef(PASSED);
        }
        dummyWait(2000);
        newDO.removeVetoableChangeListener(v);
        if (!newDO.isValid()) {
            writeRef("setValid(false) should have been vetoed, but isn't, now is newDO invalid, cannot continue!",FAILED);
            return;
        }
        if (newDO.isDeleteAllowed()){
            try{
                newDO.delete();
            }catch(Exception ex){
                printException(ex);
                writeRef("Deleting of copied object failed!",FAILED);
                return;
            }
        } else {
            writeRef("\nDelete not allowed!");
            return;
        }
        if (newDO.isValid()) {
            writeRef("newDO should not be valid at the end of this test!",FAILED);
            return;
        }
        writeRef(PASSED);
    }
    
    public  boolean performCopy(DataObject dob) throws Exception {
        writeRef("\nNow copying ...");
        DataObject newDO = null;
        if (dob.isCopyAllowed()) {
                newDO = dob.copy(temp);
        } else {
            writeRef("\nCopy not allowed!");
            return false;
        }
        if (! newDO.getName().equals(dob.getName())) {
            writeRef("Old and new name differ!",FAILED);
            return false;
        }
        if (newDO.isDeleteAllowed()){
                newDO.delete();
        } else {
            writeRef("\nDelete not allowed!");
            return false;
        }
        writeRef(PASSED);
        return true;
    }
    
    public  boolean performMove(DataObject dob) throws Exception {
        writeRef("\nNow moving ...");
        if (dob.isMoveAllowed()) {
           
              for (int i = 0 ; i < 10 ; i++) {  
                  try {
                    dob.move(temp);
                    break;
                  } catch (IOException ioe) {
                      // on windows the file is locked 
                      Thread.currentThread().sleep(500);
                      if (i == 9) {
                          throw new Exception(ioe);
                      }
                  }
              }

//            }catch(Exception ex){
//                printException(ex);
//                writeRef("Moving failed!",FAILED);
//                return false;
//            }
        } else {
            writeRef("\nMove not allowed!");
            return false;
        }
        if (dob.isMoveAllowed()){
            try{
                dob.move(resources);
            }catch(Exception ex){
                String str = exceptionToString(ex);
                writeRef("Moving back failed! + str" ,FAILED);
                return false;
            }
        } else {
            writeRef("\nMove not allowed!");
            return false;
        }
        writeRef(PASSED);
        return true;
    }
    
    public  boolean performRename(DataObject dob){
        writeRef("\nNow renaming ...");
        final String newName = "NewName";
        String oldName = dob.getName();
        if (dob.isRenameAllowed()) {
            try{
                dob.rename(newName);
            }catch(Exception ex){
                printException(ex);
                writeRef("Renaming failed!",FAILED);
                return false;
            }
        } else {
            writeRef("\nRename not allowed!");
            return false;
        }
        if (! newName.equals(dob.getName())) {
            writeRef("New name not set!",FAILED);
            return false;
        }
        if (dob.isRenameAllowed()){
            try{
                dob.rename(oldName);
            }catch(Exception ex){
                printException(ex);
                writeRef("Renaming back failed!",FAILED);
                return false;
            }
        } else {
            writeRef("\nRename not allowed!");
            return false;
        }
        writeRef(PASSED);
        return true;
    }
    
    public  boolean performDelete(DataObject dob) throws Exception {
        writeRef("\nNow deleting ...");
        String oldName = dob.getName();
        DataObject backup = null;
        if (dob.isCopyAllowed()) {
            backup = dob.copy(temp);
        } else {
            writeRef("\nCopy not allowed!");
            return false;
        }
        if (dob.isDeleteAllowed()) {
            dob.delete();
        } else {
            writeRef("\nDelete not allowed!");
            return false;
        }
        if (backup.isMoveAllowed()) {
            backup.move(resources);
        } else {
            writeRef("\nMove not allowed!");
            return false;
        }
        writeRef(PASSED);
        return true;
    }
    
    
    boolean checkManipulationOperations(DataObject dob) throws Exception {
        writeRef("\nChecking manipulating operations for " + dob.getName() + " ...");
        return performCopy(dob) && performMove(dob) && performRename(dob) && performDelete(dob);
    }
    
    public  boolean checkTemplate(DataObject dob){
        writeRef("\nNow checking template ...");
        if(!dob.isTemplate()){
            try{
                dob.setTemplate(true);
            }catch(Exception ex){
                printException(ex);
                writeRef("Setting template failed!",FAILED);
                return false;
            }
            if(!dob.isTemplate()) {
                writeRef("DO should be template but isn't!",FAILED);
                return false;
            }
            try{
                dob.setTemplate(false);
            }catch(Exception ex){
                printException(ex);
                writeRef("Unsetting template failed!",FAILED);
                return false;
            }
        } else writeRef("\nIs a template.");
        writeRef(PASSED);
        return true;
    }
    
    public  DataShadow shadowMe(DataObject dob) throws Exception{
        return shadowMe(dob,null);
    }
    
    public  DataShadow shadowMe(DataObject dob, String name) throws Exception {
        writeRef("\nCreating shadow from " + dob.getName() + " ...");
        if(dob.isShadowAllowed()){
//            try{
                DataShadow ds = null;
                ds = DataShadow.create(temp, name, dob);
                writeRef(PASSED);
                return ds;
//            }catch(Exception ex){
//                printException(ex);
//                writeRef("Creating of shadow failed!",FAILED);
//                return null;
//            }
        } else {
            writeRef("\nThis DO cannot be shadowed!");
            writeRef(PASSED);
            return null;
        }
    }
    
    
     public static void showFilesRecursively(File f, PrintStream ps) {
         ps.println(f);
         if (f.isDirectory()) {
             File files [] = f.listFiles();
             for (int i = 0 ; i < files.length ; i++) {
                showFilesRecursively(files[i],ps);
             }
         }
     }
     public static DataObject findResource(String name) throws Exception {
        String xtestData = System.getProperty("xtest.data");
        if (! xtestData.endsWith(File.separator) ) {
            xtestData = xtestData + "/";
        }
        String fileName = xtestData + name.substring(1).replace('/',File.separatorChar);
        System.out.println("fileName:" + fileName);
        if (new File(fileName).exists() == false ) {
            throw new FileNotFoundException(fileName);
        }
        FileObject fo = org.openide.filesystems.FileUtil.toFileObject(new File(fileName));
        if (fo == null) {
            throw new NullPointerException("No resource found for " + fileName);
        }
        try {
            return DataObject.find(fo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException("No resource found for " + fileName);
        }
    }
     
    /**ONLY FOR TESTING PURPOSES
     *REAL TESTS ARE IN SEPARATED CLASSES
     */
  
    //if you want print exceptions into log file, put here true.
    public  final boolean PRINT_EXCEPTIONS = true;
    
    public  final String PASSED = "passed.\n";
    public  final String FAILED = "failed.\n";
    //workdir, this hould be set by an user of this class
    //user of this class shouldn't use their own ref and log
    public static String work = null;
}
