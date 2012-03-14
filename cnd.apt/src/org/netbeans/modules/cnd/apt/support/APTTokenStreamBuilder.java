/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.apt.support;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer;

/**
 * Creates token stream for input path
 * 
 * @author Vladimir Voskresensky
 */
public final class APTTokenStreamBuilder {

    private APTTokenStreamBuilder() {
    }   
    
//    public static TokenStream buildTokenStream(File file) throws FileNotFoundException {  
//        String path = file.getAbsolutePath();
//        // could be problems with closing this stream
//        InputStream stream = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);        
//        return buildTokenStream(path, stream);
//    }
    
    public static TokenStream buildTokenStream(String text, String lang) {
        char[] buf = new char[text.length()];
        text.getChars(0, text.length(), buf, 0);
        APTLexer lexer = new APTLexer(buf);
        lexer.init(text, 0, lang);
        return lexer;
    }  

    public static TokenStream buildTokenStream(char[] buf, String lang) {
        APTLexer lexer = new APTLexer(buf);
        lexer.init("", 0, lang); //NOI18N
        return lexer;
    }
    
    public static TokenStream buildLightTokenStream(CharSequence name, Reader in, String lang) {
        APTLexer lexer = new APTLexer(in);
        lexer.init(name.toString(), 0, lang);
        lexer.setOnlyPreproc(true);
        return lexer;
    }    

    public static TokenStream buildLightTokenStream(CharSequence name, char[] buf, String lang) {
        trackActivity(name, buf.length, true);
        APTLexer lexer = new APTLexer(buf);
        lexer.init(name.toString(), 0, lang);
        lexer.setOnlyPreproc(true);
        return lexer;
    }
    
    public static TokenStream buildTokenStream(CharSequence name, Reader in, String lang) {
        APTLexer lexer = new APTLexer(in);
        lexer.init(name.toString(), 0, lang);
        return lexer;
    }     

    public static TokenStream buildTokenStream(CharSequence name, char[] buf, String lang) {
        trackActivity(name, buf.length, false);
        APTLexer lexer = new APTLexer(buf);
        lexer.init(name.toString(), 0, lang);
        return lexer;
    }
    
    private static void traceActivity() {
        long totalReads = 0;
        long fileSizes = 0;
        int ligthNrReads = 0;
        int nrReads = 0;
        for (Map.Entry<CharSequence, Pair> entry : readFiles.entrySet()) {
            final Pair data = entry.getValue();
            final long readBytes = data.totalBytes();
            assert data.totalLightReads() < data.totalReads() : "strange params " + data + " " + entry.getKey();
//            System.err.printf("[%d|%d][%d|%d]%d\t:%s\n", data.totalReads(), data.getLength(), data.totalLightReads(), data.totalReads(), readBytes, entry.getKey());
            totalReads += readBytes;
            fileSizes += data.getLength();
            ligthNrReads += data.totalLightReads();
            nrReads += data.totalReads();
        }
        double ratio = fileSizes == 0 ? 0 : (1.0 * totalReads) / fileSizes;
        totalReads /= 1024;
        fileSizes /= 1024;
        System.err.printf("StreamBuilder has %d entries, ratio is %f (%d reads where %d Light) [read %dKb from files of total size %dKb]\n", readFiles.size(), ratio, nrReads, ligthNrReads, totalReads, fileSizes);
        readFiles.clear();
    }

    private static final class Pair {
        private final int length;
        private final AtomicInteger nrLightReads = new AtomicInteger(0);
        private final AtomicInteger nrReads = new AtomicInteger(0);

        public Pair(int length) {
            this.length = length;
        }

        public void add(int bytes, boolean light) {
            assert bytes == length;
            nrReads.incrementAndGet();
            if (light) {
                nrLightReads.incrementAndGet();
            }
        }

        public int totalBytes() {
            return nrReads.get() * length;
        }

        public int totalReads() {
            return nrReads.get();
        }

        public int totalLightReads() {
            return nrLightReads.get();
        }
        
        public int getLength() {
            return length;
        }

        @Override
        public String toString() {
            return "Pair{" + "length=" + length + ", nrLightReads=" + nrLightReads + ", nrReads=" + nrReads + '}'; // NOI18N
        }
    }
    
    private static final ConcurrentMap<CharSequence, Pair> readFiles = new ConcurrentHashMap<CharSequence, Pair>();
    private static void trackActivity(CharSequence name, int len, boolean light) {
        if (true) return;
//        if ("/home/vvoskres/NetBeansProjects/Quote_1/disk.h".contentEquals(name)) {
//            new Exception().printStackTrace(System.err);
//        }
        Pair size = readFiles.get(name);
        if (size == null) {
            size = new Pair(len);
            Pair prev = readFiles.putIfAbsent(name, size);
            if (prev != null) {
                size = prev;
            }
        }
        size.add(len, light);
    }
}
