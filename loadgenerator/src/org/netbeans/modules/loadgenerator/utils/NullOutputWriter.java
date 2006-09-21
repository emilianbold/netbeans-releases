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

package org.netbeans.modules.loadgenerator.utils;

import java.io.IOException;
import java.io.Writer;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * A NULL OutputWriter - effectively dumps all data
 * @author Jaroslav Bachorik
 */
public class NullOutputWriter extends OutputWriter {
  private final static Writer writer = new NullWriter();
  
  /** Creates a new instance of NullOutputWriter */
  public NullOutputWriter() {
    super(writer);
  }

  public void println(String string, OutputListener outputListener) throws IOException {
  }

  public void reset() throws IOException {
  }
  
}
