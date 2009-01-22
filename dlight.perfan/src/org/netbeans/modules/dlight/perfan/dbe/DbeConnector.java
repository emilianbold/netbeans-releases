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
package org.netbeans.modules.dlight.perfan.dbe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.dlight.perfan.ipc.IPCException;
import org.netbeans.modules.dlight.util.ExecUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.util.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.netbeans.modules.nativeexecution.api.NativeTaskListener;
import org.openide.util.Exceptions;

public class DbeConnector implements NativeTaskListener {

  public final Object lock = new Object();
  private static final char[] hex = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };
  protected Reader processOutput,  processError;
  protected OutputStream processInput;
//  private static final int L_PROGRESS = 0;
  private static final int L_INTEGER = 1;
  private static final int L_BOOLEAN = 2;
  private static final int L_LONG = 3;
  private static final int L_STRING = 4;
  private static final int L_DOUBLE = 5;
  private static final int L_ARRAY = 6;
  private static final int L_OBJECT = 7;
  private static final int L_CHAR = 8;
  private int iVal;
  private boolean bVal;
  private long lVal;
  private String sVal;
  private Object aVal;
  private DecimalFormat format;
  private NativeTask idbeTask;
  private final ExecutionEnvironment execEnv;
  private final String experimentDirectory;
  private volatile IDBEInterface idbe;
  private String idbeCmd;
  private ConnectorListener listener;


  public DbeConnector(final ExecutionEnvironment execEnv, String experimentDirectory) {
    format = new DecimalFormat();
    format.setDecimalFormatSymbols(
            new DecimalFormatSymbols(Locale.getDefault()));
    this.execEnv = execEnv;
    this.experimentDirectory = experimentDirectory;
    idbeCmd = ExecUtil.getFullPath("perfan/" + HostInfo.getPlatformPath(execEnv) + "/prod/bin/idbe");
    idbe = null;
  }

  public boolean connect(ConnectorListener listener) throws IOException {
    if (idbe != null) {
      return true;
    }

    this.listener = listener;
    
    idbeTask = new NativeTask(execEnv, idbeCmd, null);
    idbeTask.addListener(this);
    idbeTask.submit();

    try {
      processOutput = new InputStreamReader(idbeTask.getInputStream());
      processError = new BufferedReader(Channels.newReader(Channels.newChannel(idbeTask.getErrorStream()), "UTF-8"));
      processInput = idbeTask.getOutputStream();

      idbe = new IDBEInterface(this);

      try {
        idbe.waitForExperiment(experimentDirectory);
        listener.connected(idbe);
      } catch (TimeoutException ex) {
        Exceptions.printStackTrace(ex);
      }

    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }

    return true;
  }

  public void disconnect() {
    idbeTask.cancel();
    idbeTask.removeListener(this);
    idbeTask = null;
    idbe = null;
  }

  private int readByte() {
    int val = 0;
    try {
      for (int i = 0; i < 2; i++) {
        final int c = processOutput.read();
        switch (c) {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            val = val * 16 + c - '0';
            break;
          case 'a':
          case 'b':
          case 'c':
          case 'd':
          case 'e':
          case 'f':
            val = val * 16 + c - 'a' + 10;
            break;
        }
      }
    } catch (IOException ioe) {
      throw new IPCException(ioe);
    }
    return val;
  }

  private int readIntegerValue() {
    // read 4 bytes ...
    int val = readByte();
    for (int i = 0; i < 3; i++) {
      val = val * 256 + readByte();
    }
    return val;
  }

  private long readLVal() {
    long val = readByte();
    for (int i = 0; i < 7; i++) {
      val = val * 256 + readByte();
    }
    return val;
  }

  private boolean readBVal() {
    final int val = readByte();
    return val != 0;
  }

  private char readCVal() {
    final int val = readByte();
    return (char) val;
  }

  private double readDVal() {
    final String s = readSVal();
    final double DValue;
    try {
      DValue = format.parse(s).doubleValue();
    } catch (ParseException ex) {
      ex.printStackTrace();
      return 0.0;
    }
    return DValue;
  }

  private String readSVal() {
    StringBuilder sb = new StringBuilder();

    int remainingLength = readIntegerValue();

    if (remainingLength == -1) {
      return null;
    }

    try {
      for (int i = 0; i < remainingLength; i++) {
        sb.append((char)processOutput.read());
      }
    } catch (IOException ioe) {
      throw new IPCException(ioe);
    }

    return sb.toString();
  }

  private Object readAVal() {
    boolean twoD = false;
    int type = readByte();
    if (type == L_ARRAY) {
      twoD = true;
      type = readByte();
    }
    final int len = readIntegerValue();
    if (len == -1) {
      return null;
    }
    switch (type) {
      case L_INTEGER:
        if (twoD) {
          final int[][] array = new int[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (int[]) readAVal();
          }
          return array;
        } else {
          final int[] array = new int[len];
          for (int i = 0; i < len; i++) {
            array[i] = readIntegerValue();
          }
          return array;
        }
      //break;
      case L_LONG:
        if (twoD) {
          final long[][] array = new long[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (long[]) readAVal();
          }
          return array;
        } else {
          final long[] array = new long[len];
          for (int i = 0; i < len; i++) {
            array[i] = readLVal();
          }
          return array;
        }
      //break;
      case L_DOUBLE:
        if (twoD) {
          final double[][] array = new double[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (double[]) readAVal();
          }
          return array;
        } else {
          final double[] array = new double[len];
          for (int i = 0; i < len; i++) {
            array[i] = readDVal();
          }
          return array;
        }
      //break;
      case L_BOOLEAN:
        if (twoD) {
          final boolean[][] array = new boolean[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (boolean[]) readAVal();
          }
          return array;
        } else {
          final boolean[] array = new boolean[len];
          for (int i = 0; i < len; i++) {
            array[i] = readBVal();
          }
          return array;
        }
      //break;
      case L_CHAR:
        if (twoD) {
          final char[][] array = new char[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (char[]) readAVal();
          }
          return array;
        } else {
          final char[] array = new char[len];
          for (int i = 0; i < len; i++) {
            array[i] = readCVal();
          }
          return array;
        }
      //break;
      case L_STRING:
        if (twoD) {
          final String[][] array = new String[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (String[]) readAVal();
          }
          return array;
        } else {
          final String[] array = new String[len];
          for (int i = 0; i < len; i++) {
            array[i] = readSVal();
          }
          return array;
        }
      //break;
      case L_OBJECT:
        if (twoD) {
          final Object[][] array = new Object[len][];
          for (int i = 0; i < len; i++) {
            array[i] = (Object[]) readAVal();
          }
          return array;
        } else {
          final Object[] array = new Object[len];
          for (int i = 0; i < len; i++) {
            array[i] = readAVal();
          }
          return array;
        }
      //break;
    }
    return null;
  }

//  private void readProgress() {
//    //STUB
////    parent_Analyzer.setProgress( readIVal(), readSVal() );
//  }
//  private void readResult1() {
//    for (;;) {
//      try {
//        int c = processOutput.read();
//        System.out.println(c);
//        if (c == -1) {
//          return;
//        }
//      } catch (IOException ex) {
//        Exceptions.printStackTrace(ex);
//      }
//    }
//  }
  private void readResult() {
    for (;;) {
      final int tVal = readByte();
      switch (tVal) {
//        case L_PROGRESS:
//          readProgress();
//          continue;
        case L_INTEGER:
          iVal = readIntegerValue();
//          System.out.println("L_INTEGER " + iVal + " recieved!");
          break;
        case L_LONG:
          lVal = readLVal();
//          System.out.println("L_LONG " + lVal + " recieved!");
          break;
        case L_BOOLEAN:
          bVal = readBVal();
//          System.out.println("L_BOOLEAN " + bVal + " recieved!");
          break;
        case L_STRING:
          sVal = readSVal();
//          System.out.println("L_STRING " + sVal + " recieved!");
          break;
        case L_ARRAY:
          aVal = readAVal();
//          System.out.println("L_ARRAY " + aVal + " recieved!");
          break;
        default:
//          Gizmo.err.log("IPC error in readResult(): Unknown code " + tVal); // NOI18N
//          System.out.println("UNKNOWN!!! " + tVal + " recieved!");
          break;
      }
      return;
    }
  }

  public int recvInt() {
    readResult();
    return iVal;
  }

  public String recvString() {
    readResult();
    return sVal;
  }

  public long recvLong() {
    readResult();
    return lVal;
  }

  public boolean recvBoolean() {
    readResult();
    return bVal;
  }

  public Object recvObject() {
    readResult();
    return aVal;
  }

  private void sendByte(final int b) {
    synchronized (lock) {
      try {
        processInput.write(hex[(b >> 4) & 0xf]);
        processInput.write(hex[b & 0xf]);
        processInput.flush();
      } catch (IOException ioe) {
        throw new IPCException(ioe);
      }
    }
  }

  private void sendVal(final String s) {
    synchronized (lock) {
      if (s == null) {
        sendVal(-1);
        return;
      }
      byte[] sb = s.getBytes();
      sendVal(sb.length);
      try {
        processInput.write(sb, 0, sb.length);
        processInput.flush();
      } catch (IOException ioe) {
        throw new IPCException(ioe);
      }
    }
  }

  private void sendVal(final int i) {
    synchronized (lock) {
      try {
        for (int j = 28; j >= 0; j = j - 4) {
          processInput.write(hex[(i >> j) & 0xf]);
        }
        processInput.flush();
      } catch (IOException ioe) {
        throw new IPCException(ioe);
      }
    }
  }

  private void sendVal(final long l) {
    synchronized (lock) {
      try {
        for (int j = 60; j >= 0; j = j - 4) {
          processInput.write(hex[(int) ((l >> j) & 0xf)]);
        }
        processInput.flush();
      } catch (IOException ioe) {
        throw new IPCException(ioe);
      }
    }
  }

  private void sendVal(final boolean b) {
    sendByte(b ? 1 : 0);
  }

  private void sendVal(final char c) {
    sendByte((int) c);
  }

  private void sendVal(final double d) {
    sendVal(Double.toString(d));
  }

  private void sendVal(final Object object) {
    synchronized (lock) {
      if (object == null) {
        sendByte(L_INTEGER);
        sendVal(-1);
        return;
      }

      if (object instanceof double[]) {
        sendByte(L_DOUBLE);
        final double[] array = (double[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      } else if (object instanceof int[]) {
        sendByte(L_INTEGER);
        final int[] array = (int[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      } else if (object instanceof long[]) {
        sendByte(L_LONG);
        final long[] array = (long[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      } else if (object instanceof char[]) {
        sendByte(L_CHAR);
        final char[] array = (char[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      } else if (object instanceof boolean[]) {
        sendByte(L_BOOLEAN);
        final boolean[] array = (boolean[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      } else if (object instanceof String[]) {
        sendByte(L_STRING);
        final String[] array = (String[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      } else if (object instanceof Object[]) {
        sendByte(L_OBJECT);
        final Object[] array = (Object[]) object;
        sendVal(array.length);
        for (int i = 0; i < array.length; i++) {
          sendVal(array[i]);
        }
      }
    }
  }

  public void send(final int i) {
    synchronized (lock) {
      sendByte(L_INTEGER);
      sendVal(i);
    }
  }

  public void send(final long l) {
    synchronized (lock) {
      sendByte(L_LONG);
      sendVal(l);
    }
  }

  public void send(final boolean b) {
    synchronized (lock) {
      sendByte(L_BOOLEAN);
      sendVal(b);
    }
  }

  public void send(final String s) {
    synchronized (lock) {
      sendByte(L_STRING);
      sendVal(s);
    }
  }

  public void send(final Object object) {
    synchronized (lock) {
      sendByte(L_ARRAY);
      sendVal(object);
    }
  }

  private void reconnect() {
    idbe = null;
    try {
      connect(listener);
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  public void taskStarted(NativeTask task) {
  }

  public void taskFinished(NativeTask task, Integer result) {
    System.out.println("XXXXXXXXXX Idbe finished!");
    reconnect();
  }

  public void taskCancelled(NativeTask task, CancellationException cex) {
    System.out.println("XXXXXXXXXX Idbe cancelled!");
    idbe = null;
  }

  public void taskError(NativeTask task, Throwable t) {
    System.out.println("XXXXXXXXXX Idbe Error - Need restarting!");
    reconnect();
  }
}
