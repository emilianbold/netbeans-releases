/**
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

package org.netbeans.modules.j2ee.persistence.unit;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 * Base class for persistence multiview editor tests.
 *
 * @author Erno Mononen
 */
public abstract class PersistenceEditorTestBase extends PUDataObjectTestBase {
    
    protected static final String PATH = "/persistence.xml";
    protected PUDataObject dataObject;
    protected FileObject ddFile;
    protected PersistenceToolBarMVElement mvElement;
    
    
    public PersistenceEditorTestBase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        initDataObject();
        
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ddFile.delete();
    }
    
    private void initDataObject() throws IOException{
        String persistenceFile = getDataDir().getAbsolutePath() + PATH;
        FileObject original = FileUtil.toFileObject(new File(persistenceFile));
        
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        this.ddFile = FileUtil.copyFile(original, workDirFO, "persistence_copy");
        this.dataObject = (PUDataObject) DataObject.find(ddFile);
        this.mvElement = new PersistenceToolBarMVElement(dataObject);
        
        Persistence persistence = dataObject.getPersistence();
        assertSame(2, persistence.getPersistenceUnit().length);
        assertEquals("em", persistence.getPersistenceUnit(0).getName());
        assertEquals("em2", persistence.getPersistenceUnit(1).getName());
    }
    
    /**
     * @return true if given FileObject contains given String, false
     * otherwise.
     */
    protected boolean fileObjectContains(FileObject fo, String str){
        return readFileObject(fo).indexOf(str) >= 0 ;
    }
    
    /**
     * Waits for the model synchronizer to update the data and
     * then checks whether data object's data cache contains
     * given string.
     * @return true if data object's data cache contains given str,
     * false otherwise.
     * @throws InterruptedException if waiting has been interrupted or if
     *    the wait cannot succeed due to possible deadlock collision
     */
    protected boolean dataCacheContains(String str) throws InterruptedException{
        RequestProcessor.Task updateTask = getUpdateTask();
        
        assertNotNull("Could not get updateTask", updateTask); //NOI18N
        
        updateTask.waitFinished(20000);
        if (dataObject.getDataCache().getStringData().indexOf(str) > -1){
            return true;
        }
        return false;
    }
    
    /**
     * Gets the task that takes care of updating the data from 
     * the data object's model synchronizer. Relies heavily on reflection and
     * will break down when there are changes in class hierarchy or field names. 
     * Needed for now since the current API doesn't give access to the task. 
     */
    private RequestProcessor.Task getUpdateTask(){
        RequestProcessor.Task updateTask = null;
        try {
            // get PUDataObject's model synchronizer (PUDataObject.ModelSynchronizer)
            Field puSynchronizerField = dataObject.getClass().getDeclaredField("modelSynchronizer");
            puSynchronizerField.setAccessible(true);
            XmlMultiViewDataSynchronizer puSynchronizer = 
                    (XmlMultiViewDataSynchronizer) puSynchronizerField.get(dataObject);
            // get the update task from the XmlMultiViewDataSynchronizer that is 
            // puSynchronizer's super class
            Field updateTaskField = puSynchronizer.getClass().getSuperclass().getDeclaredField("updateTask");
            updateTaskField.setAccessible(true);
            updateTask = (RequestProcessor.Task) updateTaskField.get(puSynchronizer);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return updateTask;
    }
    
    protected String readFileObject(FileObject fo){
        StringBuffer sb = new StringBuffer();
        int i;
        InputStream stream = null;
        try {
            stream = fo.getInputStream();
            while ((i = stream.read()) != -1) {
                sb.append((char) i);
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        } finally {
            if (stream != null){
                try {
                    stream.close();
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        }
        return sb.toString();
        
    }
    
    
    protected boolean containsUnit(PersistenceUnit persistenceUnit){
        for (int i = 0; i < dataObject.getPersistence().getPersistenceUnit().length; i++) {
            if (dataObject.getPersistence().getPersistenceUnit()[i].equals(persistenceUnit)){
                return true;
            }
        }
        return false;
    }
    
}
