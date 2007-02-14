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

package org.netbeans.modules.editor.options;

import java.util.Collection;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author vita
 */
public class BaseOptionsDeadlock92449Test extends NbTestCase {
    
    /** Creates a new instance of BaseOptionsTest */
    public BaseOptionsDeadlock92449Test(String name) {
        super(name);
    }
    
    public void testDeadlock92449() {
        // Initialize the whole module system, it should load java module besides of other things
        Collection modules = Lookup.getDefault().lookupAll(ModuleInfo.class);

        // Check that the modules have been loaded properly
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/x-java/Settings.settings");
        assertNotNull("Can't find Settings.settings for text/x-java", f);

        final String mimeType = "text/x-java";
        final MimePath mimePath = MimePath.parse(mimeType);

        final BaseOptions [] baseOptions = new BaseOptions[1];
        final Runnable runnableA = new Runnable() {
            public void run() {
                baseOptions[0] = (BaseOptions) MimeLookup.getLookup(mimePath).lookup(BaseOptions.class);
                baseOptions[0].getAbbrevMap();
            }
        };

        final EditorKit [] editorKit = new EditorKit[1];
        final Runnable runnableB = new Runnable() {
            public void run() {
                editorKit[0] = (EditorKit) MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
                editorKit[0].getActions();
            }
        };

        final Boolean [] stop = new Boolean[] { Boolean.FALSE };
        final Runnable loadGenerator = new Runnable() {
            public void run() {
                for( ; ; ) {
                    if (stop[0].booleanValue()) {
                        break;
                    }
                    
                    int [] array = new int [1024000];
                    for(int j = 0; j < array.length; j++) {
                        array[j] = j;
                    }
                }
            }
        };
        
        Task loadGeneratorTask = RequestProcessor.getDefault().post(loadGenerator);
        Task taskA = RequestProcessor.getDefault().post(runnableA);
        Task taskB = RequestProcessor.getDefault().post(runnableB);
        
        for(int i = 0; i < 50; i++) {
            if (taskA.isFinished() && taskB.isFinished()) {
                break;
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        stop[0] = Boolean.TRUE;
 
        assertTrue("TaskA - lookup BaseOptions, has not finished. Possible deadlock", taskA.isFinished());
        assertTrue("TaskB - lookup EditorKit, has not finished. Possible deadlock", taskB.isFinished());
        
        assertNotNull("Can't find BaseOptions for " + mimeType, baseOptions[0]);
        assertEquals("Wrong mime type on BaseOptions", mimeType, baseOptions[0].getContentType());

        assertNotNull("Can't find EditorKit for " + mimeType, editorKit[0]);
        assertEquals("Wrong mime type on EditorKit", mimeType, editorKit[0].getContentType());
    }
}
