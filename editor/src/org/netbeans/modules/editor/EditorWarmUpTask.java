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

import java.io.File;
import java.util.Enumeration;
import javax.swing.JEditorPane;
import org.netbeans.editor.BaseKit;
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
        
        javaKit.createDefaultDocument();
        BaseKit.getKit(PlainKit.class);
    }
}
