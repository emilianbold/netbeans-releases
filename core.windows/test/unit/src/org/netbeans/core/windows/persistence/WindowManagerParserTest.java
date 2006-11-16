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

package org.netbeans.core.windows.persistence;

import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/** Functionality tests for saving and loading WindowManager configuration data
 *
 * @author Marek Slama
 */
public class WindowManagerParserTest extends NbTestCase {
    private FileObject origRootModuleFolder;
    private FileObject origRootLocalFolder;
    
    public WindowManagerParserTest() {
        super("");
    }
    
    public WindowManagerParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(WindowManagerParserTest.class);
        return suite;
    }
    
    protected void setUp () throws Exception {
        origRootModuleFolder = PersistenceManager.getDefault().getRootModuleFolder();
        origRootLocalFolder = PersistenceManager.getDefault().getRootLocalFolder();
    }
    
    protected void tearDown () throws Exception {
        PersistenceManager.getDefault().setRootModuleFolder(origRootModuleFolder);
        PersistenceManager.getDefault().setRootLocalFolder(origRootLocalFolder);
    }
    
    ////////////////////////////////
    //Testing VALID data
    ////////////////////////////////
    /** Test of loaded data
     */
    public void testLoadWM01 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testLoadWM01 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager01");

        WindowManagerConfig wmCfg = null;
        try {
            wmCfg = wmParser.load();
        } catch (IOException exc) {
            Logger.global.log(Level.WARNING, null, exc);
            fail("Unexpected exception during parsing");
        }
        
        //Check loaded data
        assertNotNull("Could not load data.",wmCfg);
        
        assertEquals("Joined bounds x",100,wmCfg.xJoined);
        assertEquals("Joined bounds y",100,wmCfg.yJoined);
        assertEquals("Joined bounds width",800,wmCfg.widthJoined);
        assertEquals("Joined bounds height",600,wmCfg.heightJoined);
        
        assertEquals("Joined bounds relative width",-1.0,wmCfg.relativeWidthJoined,0.0);
        assertEquals("Joined bounds relative height",-1.0,wmCfg.relativeHeightJoined,0.0);
        
        assertEquals("Joined maximizeIfWidthBelow",-1,wmCfg.maximizeIfWidthBelowJoined);
        assertEquals("Joined maximizeIfHeightBelow",-1,wmCfg.maximizeIfHeightBelowJoined);
        
        assertFalse("Joined centered horizontaly",wmCfg.centeredHorizontallyJoined);
        assertFalse("Joined centered verticaly",wmCfg.centeredVerticallyJoined);
        
        assertEquals("Joined main window frame state",Frame.NORMAL,wmCfg.mainWindowFrameStateJoined);
        assertEquals("Separated main window frame state",Frame.NORMAL,wmCfg.mainWindowFrameStateSeparated);
        
        assertEquals("Incorrect editor area state",Constants.EDITOR_AREA_JOINED,wmCfg.editorAreaState);
        
        assertNotNull("Editor area relative bounds are null",wmCfg.editorAreaRelativeBounds);
        
        assertEquals("Editor area relative bounds x",0,wmCfg.editorAreaRelativeBounds.x);
        assertEquals("Editor area relative bounds y",0,wmCfg.editorAreaRelativeBounds.y);
        assertEquals("Editor area relative bounds width",100,wmCfg.editorAreaRelativeBounds.width);
        assertEquals("Editor area relative bounds height",60,wmCfg.editorAreaRelativeBounds.height);
        
        assertNull("Editor area bounds are not null",wmCfg.editorAreaBounds);
        
        assertEquals("Editor area window frame state",Frame.NORMAL,wmCfg.editorAreaFrameState);
        
        assertNotNull("Editor area constraints are null",wmCfg.editorAreaConstraints);
        assertEquals("Editor area constraints array has incorrect size",2,wmCfg.editorAreaConstraints.length);
        SplitConstraint item;
        item = wmCfg.editorAreaConstraints[0];
        assertEquals("Editor area constraint 0 - orientation",Constants.VERTICAL,item.orientation);
        assertEquals("Editor area constraint 0 - index",0,item.index);
        assertEquals("Editor area constraint 0 - weight",0.7,item.splitWeight,0.0);
        
        item = wmCfg.editorAreaConstraints[1];
        assertEquals("Editor area constraint 1 - orientation",Constants.HORIZONTAL,item.orientation);
        assertEquals("Editor area constraint 1 - index",1,item.index);
        assertEquals("Editor area constraint 1 - weight",0.5,item.splitWeight,0.0);
        
        assertNotNull("Screen size is null",wmCfg.screenSize);
        assertEquals("Screen width",1280,wmCfg.screenSize.width);
        assertEquals("Screen height",1024,wmCfg.screenSize.height);
        
        assertEquals("Active mode","editor",wmCfg.activeModeName);
        assertEquals("Editor maximized mode","editor",wmCfg.editorMaximizedModeName);
        assertEquals("View maximized mode","explorer",wmCfg.viewMaximizedModeName);
        assertEquals("Toolbar","Standard",wmCfg.toolbarConfiguration);
        
        assertNotNull("Modes are null",wmCfg.modes);
        
        assertNotNull("Groups are null",wmCfg.groups);
        
        System.out.println("WindowManagerParserTest.testLoadWM01 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadWM02 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testLoadWM02 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager02");
        
        WindowManagerConfig wmCfg = null;
        try {
            wmCfg = wmParser.load();
        } catch (IOException exc) {
            Logger.global.log(Level.WARNING, null, exc);
            fail("Unexpected exception during parsing");
        }
        
        //Check loaded data
        assertNotNull("Could not load data.",wmCfg);
        
        assertEquals("Joined bounds x",-1,wmCfg.xJoined);
        assertEquals("Joined bounds y",-1,wmCfg.yJoined);
        assertEquals("Joined bounds width",-1,wmCfg.widthJoined);
        assertEquals("Joined bounds height",-1,wmCfg.heightJoined);
        
        assertEquals("Joined bounds relative x",-1.0,wmCfg.relativeXJoined,0.0);
        assertEquals("Joined bounds relative y",-1.0,wmCfg.relativeYJoined,0.0);
        assertEquals("Joined bounds relative width",85.0,wmCfg.relativeWidthJoined,0.0);
        assertEquals("Joined bounds relative height",85.0,wmCfg.relativeHeightJoined,0.0);
        
        assertEquals("Joined maximizeIfWidthBelow",1004,wmCfg.maximizeIfWidthBelowJoined);
        assertEquals("Joined maximizeIfHeightBelow",728,wmCfg.maximizeIfHeightBelowJoined);
        
        assertTrue("Joined centered horizontaly",wmCfg.centeredHorizontallyJoined);
        assertTrue("Joined centered verticaly",wmCfg.centeredVerticallyJoined);
        
        assertEquals("Joined main window frame state",
            Frame.MAXIMIZED_BOTH,wmCfg.mainWindowFrameStateJoined);
        assertEquals("Separated main window frame state",
            Frame.MAXIMIZED_BOTH,wmCfg.mainWindowFrameStateSeparated);
        
        assertEquals("Incorrect editor area state",Constants.EDITOR_AREA_SEPARATED,wmCfg.editorAreaState);
        
        assertNull("Editor area relative bounds are not null",wmCfg.editorAreaRelativeBounds);
        
        assertNotNull("Editor area bounds are null",wmCfg.editorAreaBounds);
        
        assertEquals("Editor area bounds x",50,wmCfg.editorAreaBounds.x);
        assertEquals("Editor area bounds y",60,wmCfg.editorAreaBounds.y);
        assertEquals("Editor area bounds width",600,wmCfg.editorAreaBounds.width);
        assertEquals("Editor area bounds height",400,wmCfg.editorAreaBounds.height);
        
        assertEquals("Editor area window frame state",
            Frame.MAXIMIZED_BOTH,wmCfg.editorAreaFrameState);
        
        assertNotNull("Editor area constraints are null",wmCfg.editorAreaConstraints);
        assertEquals("Editor area constraints array has incorrect size",2,wmCfg.editorAreaConstraints.length);
        SplitConstraint item;
        item = wmCfg.editorAreaConstraints[0];
        assertEquals("Editor area constraint 0 - orientation",Constants.VERTICAL,item.orientation);
        assertEquals("Editor area constraint 0 - index",1,item.index);
        assertEquals("Editor area constraint 0 - weight",0.6,item.splitWeight,0.0);
        
        item = wmCfg.editorAreaConstraints[1];
        assertEquals("Editor area constraint 1 - orientation",Constants.HORIZONTAL,item.orientation);
        assertEquals("Editor area constraint 1 - index",0,item.index);
        assertEquals("Editor area constraint 1 - weight",0.75,item.splitWeight,0.0);
        
        assertNotNull("Screen size is null",wmCfg.screenSize);
        assertEquals("Screen width",640,wmCfg.screenSize.width);
        assertEquals("Screen height",480,wmCfg.screenSize.height);
        
        assertEquals("Active mode","editor",wmCfg.activeModeName);
        assertEquals("View maximized mode","explorer",wmCfg.viewMaximizedModeName);
        assertNotNull("Editor maximized mode is null",wmCfg.editorMaximizedModeName);
        assertEquals("Editor maximized mode is not empty","",wmCfg.editorMaximizedModeName);
        assertEquals("Toolbar","Standard",wmCfg.toolbarConfiguration);
        
        assertNotNull("Modes are null",wmCfg.modes);
        
        assertNotNull("Groups are null",wmCfg.groups);
        
        System.out.println("WindowManagerParserTest.testLoadWM02 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadWM03 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testLoadWM03 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager03");
        
        WindowManagerConfig wmCfg = null;
        try {
            wmCfg = wmParser.load();
        } catch (IOException exc) {
            Logger.global.log(Level.WARNING, null, exc);
            fail("Unexpected exception during parsing");
        }
        
        //Check loaded data
        assertNotNull("Could not load data.",wmCfg);
        
        assertEquals("Editor maximized mode","editor",wmCfg.editorMaximizedModeName);
        assertNotNull("View maximized mode is null",wmCfg.viewMaximizedModeName);
        assertEquals("View maximized mode is not empty","",wmCfg.viewMaximizedModeName);
        
        assertNotNull("Modes are null",wmCfg.modes);
        
        assertNotNull("Groups are null",wmCfg.groups);
        
        System.out.println("WindowManagerParserTest.testLoadWM03 FINISH");
    }
    
    /** Test of loaded data - no maximized mode
     */
    public void testLoadWM04 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testLoadWM04 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager04");
        
        WindowManagerConfig wmCfg = null;
        try {
            wmCfg = wmParser.load();
        } catch (IOException exc) {
            Logger.global.log(Level.WARNING, null, exc);
            fail("Unexpected exception during parsing");
        }
        
        //Check loaded data
        assertNotNull("Could not load data.",wmCfg);
        
        assertNotNull("View maximized mode is null",wmCfg.viewMaximizedModeName);
        assertEquals("View maximized mode is not empty","",wmCfg.viewMaximizedModeName);
        
        assertNotNull("Editor maximized mode is null",wmCfg.editorMaximizedModeName);
        assertEquals("Editor maximized mode is not empty","",wmCfg.editorMaximizedModeName);
        
        assertNotNull("Modes are null",wmCfg.modes);
        
        assertNotNull("Groups are null",wmCfg.groups);
        
        System.out.println("WindowManagerParserTest.testLoadWM04 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveWM01 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testSaveWM01 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager01");
        
        WindowManagerConfig wmCfg1 = wmParser.load();
        
        wmParser.save(wmCfg1);
        
        WindowManagerConfig wmCfg2 = wmParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",wmCfg1.equals(wmCfg2));
        
        System.out.println("WindowManagerParserTest.testSaveWM01 FINISH");
    }
    
    public void testSaveWM02 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testSaveWM02 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager02");
        
        WindowManagerConfig wmCfg1 = wmParser.load();
        
        wmParser.save(wmCfg1);
        
        WindowManagerConfig wmCfg2 = wmParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",wmCfg1.equals(wmCfg2));
        
        System.out.println("WindowManagerParserTest.testSaveWM02 FINISH");
    }
    
    public void testSaveWM03 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testSaveWM03 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager03");
        
        WindowManagerConfig wmCfg1 = wmParser.load();
        
        wmParser.save(wmCfg1);
        
        WindowManagerConfig wmCfg2 = wmParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",wmCfg1.equals(wmCfg2));
        
        System.out.println("WindowManagerParserTest.testSaveWM03 FINISH");
    }
    
    public void testSaveWM04 () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testSaveWM04 START");
        
        WindowManagerParser wmParser = createWMParser("data/valid/Windows","windowmanager04");
        
        WindowManagerConfig wmCfg1 = wmParser.load();
        
        wmParser.save(wmCfg1);
        
        WindowManagerConfig wmCfg2 = wmParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",wmCfg1.equals(wmCfg2));
        
        System.out.println("WindowManagerParserTest.testSaveWM04 FINISH");
    }
    
    ////////////////////////////////
    //Testing INVALID data
    ////////////////////////////////
    /** Test of missing file
     */
    public void testLoadWM01Invalid () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testLoadWM01Invalid START");
        
        WindowManagerParser wmParser = createWMParser("data/invalid/Windows","windowmanager01");
        
        try {
            wmParser.load();
        } catch (FileNotFoundException exc) {
            //Missing file detected
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("WindowManagerParserTest.testLoadWM01Invalid FINISH");
            return;
        }
        
        fail("Missing file was not detected.");
    }
    
    /** Test of empty file
     */
    public void testLoadWM02Invalid () throws Exception {
        System.out.println("");
        System.out.println("WindowManagerParserTest.testLoadWM02Invalid START");
        
        WindowManagerParser wmParser = createWMParser("data/invalid/Windows","windowmanager02");
        
        try {
            wmParser.load();
        } catch (IOException exc) {
            //Empty file detected
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("WindowManagerParserTest.testLoadWM02Invalid FINISH");
            return;
        }
        
        fail("Empty file was not detected.");
    }
    
    private WindowManagerParser createWMParser (String path, String name) {
        URL url;
        url = WindowManagerParserTest.class.getResource(path);
        assertNotNull("url not found.",url);
        
        FileObject [] foArray = URLMapper.findFileObjects(url);
        assertNotNull("Test parent folder not found. Array is null.",foArray);
        assertTrue("Test parent folder not found. Array is empty.",foArray.length > 0);
        
        FileObject parentFolder = foArray[0];
        assertNotNull("Test parent folder not found. ParentFolder is null.",parentFolder);
        
        PersistenceManager.getDefault().setRootModuleFolder(parentFolder);
        PersistenceManager.getDefault().setRootLocalFolder(parentFolder);
        
        WindowManagerParser wmParser = new WindowManagerParser(PersistenceManager.getDefault(),name);
        
        return wmParser;
    }
    
}
