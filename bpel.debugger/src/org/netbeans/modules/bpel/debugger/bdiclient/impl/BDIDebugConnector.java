/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.logging.Logger;
import org.netbeans.modules.bpel.debugger.api.DebugException;

import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELDebugger;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugListener;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.ObjectAdapter;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIClient;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIServer;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIService;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIServiceFactory;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;

/**
 * Connector implementation for the Alaska BPEL debugger client.
 * The connector opens and closes the network connection and handles
 * creation of the controller implementation.
 * 
 * @author Sun Microsystems
 * @author Sun Microsystems
 * @author Sun Microsystems
 */
public class BDIDebugConnector {

    private static Logger LOGGER = Logger.getLogger(BDIDebugConnector.class.getName());
    private static Hashtable allConnectors = new Hashtable ();
    
    private boolean mIsInitialized;
    private boolean mIsAttached;
    private RMIService mRmiService;
    private ObjectAdapter mObjectAdapter;
    private RMIServer mRmiServer;
    private Thread mServerThread;
    private DebugListener mListener;
    private BDIDebugger mBDIDebugger;
    private DebugListener mLocalListener;
    private String mHost;
    private int mPort;
    private Exception mException;
    
    private final BpelDebuggerImpl mDebugger;
    
    
    public BDIDebugConnector(BpelDebuggerImpl debugger) {
        mDebugger = debugger;
        initializeConnectivity();
        mLocalListener = new ClientDebuggerListernStub();
    }
    
    
    private void initializeConnectivity() {
        try {
            // Perform this initialization only once.
            // Though class names include the "RMI" name,
            // a simulated type of RMI is being used (i.e. not java.rmi).
            mRmiService = getRMIService();
            mRmiServer = mRmiService.createServer(0);
            mObjectAdapter = mRmiServer.createObjectAdapter("root");
            mRmiServer.setDefaultAdaptor(mObjectAdapter);
            mObjectAdapter.start();
            mServerThread = new Thread(mRmiServer);
            mServerThread.start();
            mIsInitialized = true;
        } catch (Exception e) {
            mException = e;
            StringWriter strWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(strWriter);
            e.printStackTrace(writer);
            writer.flush();
            strWriter.flush();
        }
    }
    
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    public Exception getException() {
        return mException;
    }
    
    public synchronized static BDIDebugConnector getDebugConnector (String host, int port) {
        return (BDIDebugConnector)allConnectors.get(host + ":" + port);
    }
    
    public void attach(final String host, final int port) {
        mHost = host;
        mPort = port;
        mBDIDebugger = new BDIDebugger(mDebugger);
        final BDIDebugConnector bdiconnector = this;
        
        try {
            RMIClient cli = mRmiService.createClient(host, port);
            cli.setObjectAdapter(mObjectAdapter);
            LOGGER.info("Trying to connect to " + host + ":" + port);
            mListener = (DebugListener) cli.importObject(DebugListener.class, "root", "debugListener");
            
            BPELDebugger proxyForDebugger = (BPELDebugger) mObjectAdapter.exportObject("foo", mBDIDebugger);
            mLocalListener.setDebugger(proxyForDebugger);
            mObjectAdapter.registerListerner(mLocalListener);
            mListener.setDebugger(proxyForDebugger);
            
            mIsAttached = true;
            synchronized (allConnectors) {
                allConnectors.put(host + ":" + port, bdiconnector);
            }
            
        } catch (Exception exc) {
            mIsAttached = false;
            mException = exc;
        }
    }
    
    public boolean isAttached() {
        return mIsAttached;
    }

    public void detach() {
        if (!mIsAttached) {
            return;
        }
        
        mIsAttached = false;         
            
        try {
            mListener.setDebugger(null);
        } catch (Exception e) {
            mDebugger.setException(new DebugException(e));
        }

        if (mRmiServer != null) {
            mRmiServer.closeClients();
        }

        if (mServerThread != null) {
            mServerThread.interrupt();
        }

        if (mRmiServer != null) {
            mRmiServer.destroy();
        }

        if (mRmiService != null) {
            mRmiService.destroy();
        }

        if (mObjectAdapter != null) {
            mObjectAdapter.destroy();
        }
        synchronized (allConnectors) {
            allConnectors.remove(mHost + ":" + mPort);
        }
        mServerThread = null;
        mRmiServer = null;
        mRmiService = null;
        mObjectAdapter = null;
        mBDIDebugger = null;
    }
    
    public BDIDebugger getBDIDebugger() {
        return mBDIDebugger;
    }

    private RMIService getRMIService() {
        try {
            RMIServiceFactory factory = (RMIServiceFactory) getClass()
                    .getClassLoader()
                    .loadClass("org.netbeans.modules.bpel.debuggerbdi.rmi.wp.impl.DefaultRMIServiceFactory")
                    .newInstance();
            return factory.createRMIService(getClass().getClassLoader());
        } catch (Exception e) {
            LOGGER.warning("Exception in getRMIService:\n" + e);
            return null;
        }
    }
    
    
    class ClientDebuggerListernStub implements DebugListener {
        private BPELDebugger bpDebugger = null;
        public void setDebugger(BPELDebugger debugger) {
            bpDebugger = debugger;
        }
        public void socketClosed(Object arg0) {
            if (bpDebugger != null) {
                bpDebugger.detach();
            }
        }
    }
}
