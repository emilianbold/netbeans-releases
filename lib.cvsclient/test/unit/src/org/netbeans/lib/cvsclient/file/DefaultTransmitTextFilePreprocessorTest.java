/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.lib.cvsclient.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Ondrej Vrabec
 */
public class DefaultTransmitTextFilePreprocessorTest extends NbTestCase {
    private String prevEndings;
    private static final int CHUNK_SIZE = DefaultTransmitTextFilePreprocessor.CHUNK_SIZE;

    public DefaultTransmitTextFilePreprocessorTest (String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        prevEndings = System.getProperty("line.separator");
        System.setProperty("line.separator", "\r\n");
    }

    @Override
    protected void tearDown () throws Exception {
        System.setProperty("line.separator", prevEndings);
        super.tearDown();
    }
    
    @Test
    public void testMultiChunks () throws IOException {
        File file = new File(getWorkDir(), "input");
        
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < CHUNK_SIZE + 1; ++i) {
            if (i == CHUNK_SIZE - 1) {
                out.write('\r');
            } else {
                out.write('a');
            }
        }
        out.close();
        
        File converted = new DefaultTransmitTextFilePreprocessor().getPreprocessedTextFile(file);
        FileInputStream in = new FileInputStream(converted);
        byte[] arr = new byte[2 * CHUNK_SIZE];
        int read = in.read(arr);
        assertEquals(CHUNK_SIZE + 1, read);
        for (int i = 0; i < read; ++i) {
            if (i == CHUNK_SIZE - 1) {
                assertEquals('\r', arr[i]);
            } else {
                assertEquals('a', arr[i]);
            }
        }
    }
    
    @Test
    public void testPartialSepAtEnd () throws IOException {
        File file = new File(getWorkDir(), "input");
        
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < CHUNK_SIZE; ++i) {
            if (i == CHUNK_SIZE - 1) {
                out.write('\r');
            } else {
                out.write('a');
            }
        }
        out.close();
        
        File converted = new DefaultTransmitTextFilePreprocessor().getPreprocessedTextFile(file);
        FileInputStream in = new FileInputStream(converted);
        byte[] arr = new byte[CHUNK_SIZE];
        int read = in.read(arr);
        assertEquals(CHUNK_SIZE, read);
        for (int i = 0; i < read; ++i) {
            if (i == CHUNK_SIZE - 1) {
                assertEquals('\r', arr[i]);
            } else {
                assertEquals('a', arr[i]);
            }
        }
    }
    
    @Test
    public void testPartialSepAtStart () throws IOException {
        File file = new File(getWorkDir(), "input");
        
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < CHUNK_SIZE; ++i) {
            if (i == 0) {
                out.write('\r');
            } else {
                out.write('a');
            }
        }
        out.close();
        
        File converted = new DefaultTransmitTextFilePreprocessor().getPreprocessedTextFile(file);
        FileInputStream in = new FileInputStream(converted);
        byte[] arr = new byte[CHUNK_SIZE];
        int read = in.read(arr);
        assertEquals(CHUNK_SIZE, read);
        for (int i = 0; i < read; ++i) {
            if (i == 0) {
                assertEquals('\r', arr[i]);
            } else {
                assertEquals('a', arr[i]);
            }
        }
    }
    
    @Test
    public void testLineEndingsConversion () throws IOException {
        File file = new File(getWorkDir(), "input");
        
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < CHUNK_SIZE + 2; ++i) {
            switch (i) {
                case 0:
                case CHUNK_SIZE - 1:
                    out.write('\r');
                    break;
                case 1:
                case CHUNK_SIZE:
                    out.write('\n');
                    break;
                default:
                    out.write('a');
                    break;
            }
        }
        out.close();
        
        File converted = new DefaultTransmitTextFilePreprocessor().getPreprocessedTextFile(file);
        FileInputStream in = new FileInputStream(converted);
        byte[] arr = new byte[CHUNK_SIZE];
        in.read(arr);
        assertEquals('\n', arr[0]);
        assertEquals('a', arr[1]);
        assertEquals('a', arr[CHUNK_SIZE - 3]);
        assertEquals('\n', arr[CHUNK_SIZE - 2]);
        assertEquals('a', arr[CHUNK_SIZE - 1]);
    }
    
}
