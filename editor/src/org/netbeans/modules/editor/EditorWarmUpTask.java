/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.View;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.view.spi.EstimatedSpanView;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.modules.editor.java.JCStorage;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.plain.PlainKit;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * "Warm-up" task for editor. Executed after IDE startup, it should
 * pre-initialize some suitable parts of the module to improve first time usage
 * experience - which might suffer from long response time due to class loading
 * and various initialization.
 * See {@link org.netbeans.core.AfterStartWarmUp} for details about how the task is run.
 *
 * @author  Tomas Pavek, Martin Roskanin
 */

public class EditorWarmUpTask implements Runnable{
    
    /**
     * Number of lines that an artificial document
     * for view hierarchy code optimization will have.
     * <br>
     * The default threshold for hotspot method compilation
     * is 1500 invocations.
     */
    private static final int ARTIFICIAL_DOCUMENT_LINE_COUNT = 1700;

    /**
     * Number of times a long document is assigned to the editor pane
     * which causes the view hierarchy for it to be (re)built.
     */
    private static final int VIEW_HIERARCHY_CREATION_COUNT = 1;
    
    /**
     * Width of an artificial frame used to hold the editor pane.
     */
    private static final int FRAME_WIDTH = 600;
    
    /**
     * Height of an artificial frame used to hold the editor pane.
     */
    private static final int FRAME_HEIGHT = 400;
    
    /**
     * Number of scrolls to be simulated.
     */
    private static final int SCROLL_COUNT = 30;
    

    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.warmup"); // NOI18N
    
    
    private void sampleDirParsing(){
        File userdir = new File(System.getProperty("netbeans.user", ""),"sampledir"); //NOI18N
        String fsName = userdir.getAbsolutePath().replace('\\','/');
        JCStorage.getStorage().parseFSOnBackground(Repository.getDefault().findFileSystem(fsName));
    }
    
    public void run() {
        long startTime = System.currentTimeMillis();
        
        // initializing code completion database. Reading *.jcs files and creating memory map of available 
        // completin classes
        JCStorage.getStorage();
        if (debug){
            System.out.println("Storage initialized:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
        // Parsing of sampledir, that is mounted by default.
        // The autoupdate of that filesystem cannot start as it starts only after 
        // mount action.
        //sampleDirParsing();
        if (debug){
            System.out.println("Sample dir parsed:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
        // Initialization of editor settings initializers and PrintOptions.
        EditorModule.init();        

        // Init of JavaKit and JavaOptions
        BaseKit javaKit = BaseKit.getKit(JavaKit.class);
        BaseKit plainKit = BaseKit.getKit(PlainKit.class);
        
        //creating actions instances
        javaKit.getActions();
        

        // Start of a code block that tries to force hotspot to compile
        // the view hierarchy and related classes for faster performance
        if (debug) {
            startTime = System.currentTimeMillis();
        }

        // Work with artificial frame that will host an editor pane
        JFrame frame = new JFrame();
        JEditorPane pane = new JEditorPane();
        JComponent extComponent = null;
        pane.setEditorKit(javaKit);

        // Obtain extended component (with editor's toolbar and scrollpane)
        EditorUI editorUI = Utilities.getEditorUI(pane);
        if (editorUI != null) {
            extComponent = editorUI.getExtComponent();
        }

        if (extComponent == null) {
            extComponent = new JScrollPane(pane);
        }

        frame.getContentPane().add(extComponent);

        // Have two documents - one empty and another one filled with many lines
        Document emptyDoc = javaKit.createDefaultDocument();
        Document longDoc = pane.getDocument();
        
        try {
            
            // Fill the document with data.
            // Number of lines is more important here than number of columns in a line
            // Do one big insert instead of many small inserts
            StringBuffer sb = new StringBuffer();
            for (int i = ARTIFICIAL_DOCUMENT_LINE_COUNT; i > 0; i--) {
                sb.append("int ident = 1; // comment\n"); // NOI18N
            }
            longDoc.insertString(0, sb.toString(), null);

            // Switch between empty doc and long several times
            // to force view hierarchy creation
            for (int i = 0; i < VIEW_HIERARCHY_CREATION_COUNT; i++) {
                pane.setDocument(emptyDoc);
                
                // Set long doc - causes view hierarchy to be rebuilt
                pane.setDocument(longDoc);
            }

            // Do view-related operations
            AbstractDocument doc = (AbstractDocument)pane.getDocument();
            doc.readLock();
            try {
                View rootView = Utilities.getDocumentView(pane);
                LockView lockView = LockView.get(rootView);
                lockView.lock();
                try {
                    int viewCount = rootView.getViewCount();

                    // Force switch the line views from estimated spans to exact measurements
                    for (int j = 0; j < viewCount; j++) {
                        View v = rootView.getView(j);
                        if (v instanceof EstimatedSpanView) {
                            ((EstimatedSpanView)v).setEstimatedSpan(false);
                        }
                    }

                    // Get child allocation for each line
                    for (int j = 0; j < viewCount; j++) {
                        Rectangle alloc = new Rectangle(0, 0,
                            (int)rootView.getPreferredSpan(View.X_AXIS),
                            (int)rootView.getPreferredSpan(View.Y_AXIS)
                        );
                        rootView.getChildAllocation(j, alloc);
                    }

                    // Test modelToView and viewToModel
                    float rootViewYSpan = rootView.getPreferredSpan(View.Y_AXIS);
                    float maybeLineSpan = rootViewYSpan / viewCount;
                    Point point = new Point();
                    point.x = 5; // likely somewhere inside the first char on the line
                    for (int j = 0; j < viewCount; j++) {
                        pane.modelToView(rootView.getView(j).getStartOffset());

                        point.y = (int)(j * maybeLineSpan);
                        int pos = pane.viewToModel(point);
                    }

                } finally {
                    lockView.unlock();
                }
            } finally {
                doc.readUnlock();
            }

            // Pack the frame and then set target size and validate
            frame.pack();
            frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
            frame.validate();

            // Create buffered image for painting simulation
            BufferedImage bImage = new BufferedImage(
                FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics bGraphics = bImage.getGraphics();
            bGraphics.setClip(0, 0, FRAME_WIDTH, FRAME_HEIGHT);

            frame.paint(bGraphics);

            // Scroll through the document and do the paints into buffered image
            if (pane.getParent() instanceof JViewport) {
                JViewport viewport = (JViewport)pane.getParent();
                for (int i = 0; i < SCROLL_COUNT; i++) {
                    int y = (i * pane.getHeight()) / SCROLL_COUNT;
                    viewport.setViewPosition(new Point(0,y));
                    // cliping area should be retained in the graphics
                    frame.paint(bGraphics);
                }
            }

        } catch (BadLocationException e) {
        }

        // Candidates Annotations.getLineAnnotations()
            
        if (debug) {
            System.out.println("View hierarchy initialized:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
    }
}
