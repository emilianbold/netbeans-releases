/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.antlr.TokenStream;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
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
    private static final ConcurrentHashMap<CharSequence, APTSyncCreator> file2creator = new ConcurrentHashMap<CharSequence, APTSyncCreator>();
    /** static shared sync map */
    private static Map<CharSequence, Reference<APTFile>> file2ref2apt = new ConcurrentHashMap<CharSequence, Reference<APTFile>>();
    private static Map<CharSequence, APTFile> file2apt = new ConcurrentHashMap<CharSequence, APTFile>();
    
    /** instance fields */
    
    /** Creates a new instance of APTCreator */
    private APTDriverImpl() {
    }
    
    public static APTFile findAPT(APTFileBuffer buffer, boolean withTokens, String lang) throws IOException {
        CharSequence path = buffer.getAbsolutePath();
        APTFile apt = _getAPTFile(path, withTokens);
        if (apt == null) {
            APTSyncCreator creator = file2creator.get(path);
            if (creator == null) {
                // no need to sync on ConcurrentHashMap due to putIfAbsent method
                creator = new APTSyncCreator();
                APTSyncCreator old = file2creator.putIfAbsent(path, creator);
                if (old != null) {
                    creator = old;
                }
            }
            assert (creator != null);
            // use instance synchronized method to prevent
            // multiple apt creating for the same file
            apt = creator.findAPT(buffer, withTokens, lang);
            file2creator.remove(path);
        }
        return apt;        
    }

    public static void invalidateAPT(APTFileBuffer buffer) {
        CharSequence path = buffer.getAbsolutePath();
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            file2ref2apt.remove(path);
        } else {
            file2apt.remove(path);
        }
    }
    
    public static void invalidateAll() {
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            file2ref2apt.clear();
        } else {
            file2apt.clear();
        }
    }
    
    private static class APTSyncCreator {  
        private APTFile fullAPT = null;
        private APTFile lightAPT = null;
        public APTSyncCreator() {
        }
        
        /** synchronized on instance */
        public synchronized APTFile findAPT(APTFileBuffer buffer, boolean withTokens, String lang) throws IOException {
            CharSequence path = buffer.getAbsolutePath();
            // quick exit: check if already was added by another creator
            // during wait
            if (withTokens && fullAPT != null) {
                return fullAPT;
            } else if (!withTokens && lightAPT != null) {
                return lightAPT;
            }
            APTFile apt = _getAPTFile(path, withTokens);
            if (apt == null) {
                // ok, create new apt
                
                // build token stream for file       
                Reader reader = null;
                try {
                    reader = buffer.getReader();
                    if (!withTokens) {
                        TokenStream ts = APTTokenStreamBuilder.buildLightTokenStream(path, reader, lang);
                        // build apt from light token stream
                        apt = APTBuilder.buildAPT(path, ts);
                        fullAPT = null;
                        if (apt != null) {
                            if (APTTraceFlags.TEST_APT_SERIALIZATION) {
                                APTFile test = (APTFile) APTSerializeUtils.testAPTSerialization(buffer, apt);
                                if (test != null) {
                                    apt = test;
                                } else {
                                    System.err.println("error on serialization apt for file " + path); // NOI18N
                                }
                            }
                            lightAPT = apt;
                            _putAPTFile(path, lightAPT, false);
                        }
                    } else {
                        TokenStream ts = APTTokenStreamBuilder.buildTokenStream(path, reader, lang);
                        // build apt from token stream
                        apt = APTBuilder.buildAPT(path, ts);
                        fullAPT = apt;
                        if (apt != null) {
                            if (APTTraceFlags.TEST_APT_SERIALIZATION) {
                                APTFile test = (APTFile) APTSerializeUtils.testAPTSerialization(buffer, apt);
                                if (test != null) {
                                    apt = test;
                                } else {
                                    System.err.println("error on serialization apt for file " + path); // NOI18N
                                }
                            }
                            _putAPTFile(path, fullAPT, true);
                            lightAPT = (APTFile) APTBuilder.buildAPTLight(apt);
                            _putAPTFile(path, lightAPT, false);
                        }
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            APTUtils.LOG.log(Level.SEVERE, "exception on closing stream\n{0}", new Object[] { ex }); // NOI18N
                        }
                    }
                }
            }
            return apt;
        }       
    } 
    
    private static APTFile _getAPTFile(CharSequence path, boolean withTokens) {
        if (withTokens) {
            // we do not cache full apt
            return null;
        }
        APTFile apt;
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            Reference<APTFile> aptRef = file2ref2apt.get(path);
            apt = aptRef == null ? null : aptRef.get();
        } else {
            apt = file2apt.get(path);
        }        
        return apt;
    }
    
    private static void _putAPTFile(CharSequence path, APTFile apt, boolean withTokens) {
        if (withTokens) {
            // we do not cache full apt
            return;
        }
        if (APTTraceFlags.APT_USE_SOFT_REFERENCE) {
            file2ref2apt.put(path, new SoftReference<APTFile>(apt));
        } else {
            file2apt.put(path, apt);
        }        
    }

    public static void close() {
        invalidateAll();
    }
}
