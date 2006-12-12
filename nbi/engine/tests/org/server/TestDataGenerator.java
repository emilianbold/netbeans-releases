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
 *
 * $Id$
 */
package org.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.*;
import org.netbeans.installer.utils.StreamUtils;

/**
 *
 * @author Danila_Dugurov
 */
public class TestDataGenerator {
  
  public static final int K_BYTE = 1024;
  public static final int M_BYTE = 1024 * K_BYTE;
  
  File dirToGenerate;
  
  public static final String[] testFiles = new String[] {
    "smallest.data",
    "small.data",
    "smallaverage.data",
    "bigaverage.data",
    "big.data",
    "bigest.data"};
  
  public static final int[] testFileSizes = new int[] {
    K_BYTE - 435,
    M_BYTE - 237 * K_BYTE - 1,
    10 * M_BYTE - 139 * K_BYTE - 23,
    50 * M_BYTE - K_BYTE - 758,
    100 * M_BYTE - 3,
    200 * M_BYTE
  };
  
  public static final byte MAGIC_BYTE = (byte) 201;
  
  public static final byte[] buffer = new byte[4096];
  
  static {
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = MAGIC_BYTE;
    }
  }
  
  public TestDataGenerator(String dirToGenerate) {
    this.dirToGenerate = new File(dirToGenerate);
    this.dirToGenerate.mkdirs();
  }
  
  public void generateTestData() throws IOException {
    for (int index = 0; index < testFiles.length; index++) {
      final File file = new File(dirToGenerate, testFiles[index]);
      if (file.exists()) continue;
      fillWithMagicBytes(file, index);
    }
  }
  
  private void fillWithMagicBytes(File testFile, int index) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(testFile));
    int alreadyWritten = 0;
    while (alreadyWritten < testFileSizes[index]) {
      int writeCount = alreadyWritten + buffer.length <= testFileSizes[index] ? buffer.length : testFileSizes[index] - alreadyWritten;
      out.write(buffer, 0, writeCount);
      alreadyWritten += writeCount;
    }
    out.flush();
    out.close();
  }
  
  public void deleteTestData() {
    for (int index = 0; index < testFiles.length; index++) {
      final File file = new File(dirToGenerate, testFiles[index]);
      file.delete();
    }
  }
}
