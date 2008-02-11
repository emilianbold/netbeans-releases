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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import java.util.Hashtable;
import java.util.logging.Logger;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELDebugger;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugListener;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.ObjectAdapter;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIClient;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIServer;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIService;
import org.netbeans.modules.bpel.debuggerbdi.rmi.wp.RMIServiceFactory;


/**
 * Connector implementation for the Alaska BPEL debugger client.
 * The connector opens and closes the network connection and handles
 * creation of the controller implementation.
 * 
 * @author Sun Microsystems
 */
public class BDIDebugConnector {

    private static Logger LOGGER = Logger.getLogger(
            BDIDebugConnector.class.getName());
    
    private static Hashtable<String, BDIDebugConnector> allConnectors = 
            new Hashtable<String, BDIDebugConnector>();
    
    public synchronized static BDIDebugConnector getDebugConnector (
            final String host, 
            final int port) {
        return allConnectors.get(host + ":" + port);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
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
        mLocalListener = new ClientDebuggerListenerStub();
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
        }
    }
    
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    public Exception getException() {
        return mException;
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
        } catch (Exception e) {
            mDebugger.setException(new DebugException(e));
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
            e.printStackTrace();
            LOGGER.warning("Exception in getRMIService:\n" + e);
            return null;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    class ClientDebuggerListenerStub implements DebugListener {
        private BPELDebugger bpDebugger = null;
        
        public void setDebugger(BPELDebugger debugger) {
            bpDebugger = debugger;
        }
        
        public void socketClosed(Object arg0) {
            if (bpDebugger != null) {
                // Supposedly any kind of exception can be thrown from this 
                // invocation. Since this operation is kind of a clean-up one
                // we should merely catch them (to avoid showing it to the
                // user) and log.
                try {
                    bpDebugger.detach();
                } catch (Exception e) {
                    LOGGER.warning("Exception in " +
                            "ClientDebuggerListernStub.socketClosed:\n" + e);
                }
            }
        }
    }
}
