/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.execution.api.support;

import java.io.IOException;
import java.io.Reader;
import java.util.Random;
import org.netbeans.modules.dlight.util.Util;

import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

final class IOTabManagerImpl implements IOTabManager{
  private IOProvider provider = IOProvider.getDefault();

  IOTabManagerImpl() {
  }


  public InputOutput getIO(NativeTask task, boolean reuse) {
    InputOutput delegate = provider.getIO(task.toString(), task.getActions());
    return Util.getBoolean("dlight.io.wrapper", true) ? new IOWrapper(delegate) : delegate;
  }

  private static class IOWrapper implements InputOutput {

    InputOutput delegate;

    public IOWrapper(InputOutput delegate) {
      this.delegate = delegate;
    }

    public OutputWriter getOut() {
      return new OutputWriterWrapper(delegate.getOut());
    }

    public Reader getIn() {
      return delegate.getIn();
    }

    public OutputWriter getErr() {
      return delegate.getErr();
    }

    public void closeInputOutput() {
      delegate.closeInputOutput();
    }

    public boolean isClosed() {
      return delegate.isClosed();
    }

    public void setOutputVisible(boolean value) {
      delegate.setOutputVisible(value);
    }

    public void setErrVisible(boolean value) {
      delegate.setErrVisible(value);
    }

    public void setInputVisible(boolean value) {
      delegate.setInputVisible(value);
    }

    public void select() {
      delegate.select();
    }

    public boolean isErrSeparated() {
      return delegate.isErrSeparated();
    }

    public void setErrSeparated(boolean value) {
      delegate.setErrSeparated(value);
    }

    public boolean isFocusTaken() {
      return delegate.isFocusTaken();
    }

    public void setFocusTaken(boolean value) {
      delegate.setFocusTaken(value);
    }

    public Reader flushReader() {
      return delegate.flushReader();
    }
  }

  private static class OutputWriterWrapper extends OutputWriter {
    private OutputWriter delegate;
    private int count = 1;

    public OutputWriterWrapper(OutputWriter delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public void flush() {
      // No flush!
    }

    @Override
    public void write(String s, int off, int len) {
      if ("".equals(s)) {
        return;
      }
      
      if (--count == 0) {
        count = new Random().nextInt(5000) + 5000;
        delegate.write(s, off, len);
        delegate.println("... ");
      }
    }

    @Override
    public void println(String s, OutputListener l) throws IOException {
      delegate.println(s, l);
    }

    @Override
    public void reset() throws IOException {
      delegate.reset();
    }

  }
}
