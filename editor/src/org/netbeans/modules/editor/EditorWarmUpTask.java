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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.Enumeration;
import javax.swing.JEditorPane;
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
     * Number of times when a fresh editor pane gets created.
     */
    private static final int EDITOR_PANE_CREATION_COUNT = 20;
    
    /**
     * Number of lines that an artificial document
     * for view hierarchy code optimization will have.
     */
    private static final int ARTIFICIAL_DOCUMENT_LINE_COUNT = 5000;

    /**
     * Number of insertions in which the line count defined above
     * will be inserted.
     */
    private static final int INSERTION_FRAGMENT_COUNT = 10;
    
    /**
     * Number of repeats when all the data are removed from the document
     * and the new content inserted.
     */
    private static final int INSERTION_COUNT = 3;
    
    /**
     * Number of times a long document is assigned to the editor pane
     * which causes the view hierarchy for it to be (re)built.
     */
    private static final int VIEW_HIERARCHY_CREATION_COUNT = 3;
    

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
            System.out.println("storage initialized:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
        // Parsing of sampledir, that is mounted by default.
        // The autoupdate of that filesystem cannot start as it starts only after 
        // mount action.
        //sampleDirParsing();
        if (debug){
            System.out.println("sample dir parsed:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
        
        // Initialization of editor settings initializers and PrintOptions.
        EditorModule.init();        

        // Init of JavaKit and JavaOptions
        BaseKit javaKit = BaseKit.getKit(JavaKit.class);
        
        //creating actions instances
        javaKit.getActions();
        
        Document emptyDoc = javaKit.createDefaultDocument();
        BaseKit plainKit = BaseKit.getKit(PlainKit.class);

        if (debug) {
            startTime = System.currentTimeMillis();
        }

/* Disabled until thorough performance analysis will be finished
        // Try to force hotspot to compile the view hierarchy for larger files
        // Create document with many lines and init the view hierarchy
        
        // Switch the kits in the pane
        JEditorPane pane = null;
        
        for (int i = 0; i < EDITOR_PANE_CREATION_COUNT; i++) {
            pane = new JEditorPane();
            pane.setEditorKit(plainKit);
            pane.setEditorKit(javaKit);
            EditorUI editorUI = Utilities.getEditorUI(pane);
            if (editorUI != null) {
                editorUI.getExtComponent();
            }
        }

        Document longDoc = pane.getDocument();
        // Fill the document - number of lines is more important than number of columns in a line
        // Do one big insert instead of many small inserts
        StringBuffer sb = new StringBuffer();
        for (int i = ARTIFICIAL_DOCUMENT_LINE_COUNT / INSERTION_FRAGMENT_COUNT; i > 0; i--) {
            // Append empty line comment
            sb.append("//\n"); // NOI18N
        }
        String insertionFragment = sb.toString();
        
        try {
            
            // Remove and insert data into the document several times
            for (int i = 0; i < INSERTION_COUNT; i++) {
                longDoc.remove(0, longDoc.getLength());
                for (int j = INSERTION_FRAGMENT_COUNT; j > 0; j--) {
                    longDoc.insertString(0, insertionFragment, null);
                }
            }
            
            // Switch between empty doc and long doc three times
            // to force view hierarchy code compilation
            for (int i = 0; i < VIEW_HIERARCHY_CREATION_COUNT; i++) {
                pane.setDocument(emptyDoc);
                
                // Set long doc - causes view hierarchy to be rebuilt
                pane.setDocument(longDoc);
            }

            // Force switch the line views from estimated spans to exact measurements
            AbstractDocument doc = (AbstractDocument)pane.getDocument();
            doc.readLock();
            try {
                View rootView = Utilities.getDocumentView(pane);
                LockView lockView = LockView.get(rootView);
                lockView.lock();
                try {
                    int viewCount = rootView.getViewCount();
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
        } catch (BadLocationException e) {
        }

        // Candidates Annotations.getLineAnnotations()
            
        if (debug) {
            System.out.println("View hierarchy initialized:"+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
        }
 */
        
    }
}
