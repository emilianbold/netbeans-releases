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

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.perfan.ipc.IPCException;

public class IDBEInterface {

  private static final Logger log = DLightLogger.getLogger(IDBEInterface.class);
  private final DbeConnector connector;
  private static final long OPEN_EXPERIMENT_TIMEOUT = 60000;

  protected IDBEInterface(DbeConnector connector) {
    this.connector = connector;
  }

  public void disconnect() {
    synchronized (connector.lock) {
      connector.disconnect();
    }
  }

  public Object[] getTopFunctions(String mspec, String msort, int limit) {
    synchronized (connector.lock) {
      connector.send("getTopFunctions"); // NOI18N
      connector.send(mspec);
      connector.send(msort);
      connector.send(limit);
      return (Object[]) connector.recvObject();
    }
  }

  Object[] getCallees(String quilifiedName) {
    synchronized (connector.lock) {
      connector.send("getCallees"); // NOI18N
      connector.send(quilifiedName);
      return (Object[]) connector.recvObject();
    }
  }

  public Object[] getCallees(long objRef) {
    synchronized (connector.lock) {
      connector.send("getCalleesByRef"); // NOI18N
      connector.send(objRef);
      return (Object[]) connector.recvObject();
    }
  }

  Object[] getCallers(String quilifiedName) {
    synchronized (connector.lock) {
      connector.send("getCallers"); // NOI18N
      connector.send(quilifiedName);
      return (Object[]) connector.recvObject();
    }
  }

  public Object[] getCallers(long objRef) {
    synchronized (connector.lock) {
      connector.send("getCallersByRef"); // NOI18N
      connector.send(objRef);
      return (Object[]) connector.recvObject();
    }
  }

  public void waitForExperiment(String experimentDirectory) throws TimeoutException {
    int result = -1;

    synchronized (connector.lock) {
      long ts = System.currentTimeMillis();
      try {
        while (System.currentTimeMillis() - ts < OPEN_EXPERIMENT_TIMEOUT) {
          connector.send("openExperiment"); // NOI18N
          connector.send(experimentDirectory);
          result = connector.recvInt();
          if (result == 0) {
            break;
          }
          try {
            log.fine("Wait for experiment ...");
            Thread.sleep(3000);
          } catch (InterruptedException ex) {
          }
        }
      } catch (IPCException e) {
        log.severe("IPCException occured in waitForExperiment() " + e.toString());
      }

      if (result != 0) {
        throw new TimeoutException("Cannot open experiment " + experimentDirectory);
      }
    }

  }
}
