/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.TokenStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of APTDriver
 * This driver supports synchronized access with waiting when necessary to the
 * file's APT.
 * Wait if need to create and another process already creating.
 * @author Vladimir Voskresensky
 */
public class APTDriverImpl {
    /** map of active creators */
    private static Map/*<String, APTSyncCreator>*/ file2creator = new HashMap();
    /** static shared sync map */
    private static Map/*<String, APTFile>*/ file2apt = Collections.synchronizedMap(new HashMap());
    
    /** instance fields */
    
    /** Creates a new instance of APTCreator */
    private APTDriverImpl() {
    }
    
    public static APTFile findAPT(APTFileBuffer buffer, boolean withTokens) throws IOException {
        File file = buffer.getFile();
        String path = file.getAbsolutePath();
        APTFile apt = (APTFile) file2apt.get(path);
        if (apt == null) {
            APTSyncCreator creator = null;
            synchronized (file2creator) {
                creator = (APTSyncCreator) file2creator.get(path);
                if (creator == null) {
                    creator = new APTSyncCreator();
                    file2creator.put(path, creator);
                }
            }
            assert (creator != null);
            // use instance synchronized method to prevent
            // multiple apt creating for the same file
            apt = creator.findAPT(buffer, withTokens);
            synchronized (file2creator) {
                file2creator.remove(path);
            }
        }
        return apt;        
    }

    public static void invalidateAPT(APTFileBuffer buffer) {
        File file = buffer.getFile();
        String path = file.getAbsolutePath();
        file2apt.remove(path);
    }
    
    public static void invalidateAll() {
        file2apt.clear();
    }
    
    private static class APTSyncCreator {               
        public APTSyncCreator() {
        }
        
        /** synchronized on instance */
        public synchronized APTFile findAPT(APTFileBuffer buffer, boolean withTokens) throws IOException {
            File file = buffer.getFile();
            String path = file.getAbsolutePath();
            // quick exti: check if already was added by another creator
            // during wait
            APTFile apt = (APTFile) file2apt.get(path);
            if (apt == null) {
                // ok, create new apt
                
                // build token stream for file       
                InputStream stream = null;
                try {
                    stream = buffer.getInputStream();               
                    TokenStream ts = APTTokenStreamBuilder.buildTokenStream(path, stream);
                    // build apt from token stream
                    apt = APTBuilder.buildAPT(path, ts);
                    if (apt != null) {
                        if (APTTraceFlags.TEST_APT_SERIALIZATION) {
                            APTFile test = (APTFile) APTSerializeUtils.testAPTSerialization(buffer, apt);
                            if (test != null) {
                                apt = test;
                            } else {
                                System.err.println("error on serialization apt for file " + file.getAbsolutePath());
                            }
                        }
                        file2apt.put(path, apt);
                    }
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex) {
                            APTUtils.LOG.log(Level.SEVERE, "exception on closing stream", ex);
                        }
                    }
                }
            }
            return apt;
        }       
    }    
}
