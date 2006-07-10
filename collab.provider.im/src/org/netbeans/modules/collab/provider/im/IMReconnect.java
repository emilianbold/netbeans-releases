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
package org.netbeans.modules.collab.provider.im;

import java.util.Iterator;
import java.util.Vector;


import org.netbeans.modules.collab.core.Debug;
import com.sun.collablet.Account;
import com.sun.collablet.UserInterface;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.provider.im.ui.ServerStatusPanel;

/**
 *
 * @author Alexandr Scherbatiy
 */
public class IMReconnect {

    private boolean launch = false;
    private boolean doReconnect = true;
    private boolean showStatusDialog = true;
    private volatile boolean idle = false;
    private Vector sessionList = new Vector();
    
    private ServerStatusPanel statusDialog;
    
    
    private synchronized boolean getLaunch() {
        return launch;
    }
    
    private synchronized void setLaunch(boolean newLaunch) {
        launch = newLaunch;
    }
    
    private synchronized boolean getIdle() {
        return idle;
    }
    
    private synchronized void setIdle(boolean newIdle) {
        idle = newIdle;
    }
    
    
    public synchronized boolean  getDoReconnect() {
        return doReconnect ;
    }
    
    public synchronized void setDoReconnect(boolean newDoReconnect) {
        doReconnect = newDoReconnect;
    }
    
    public synchronized void setShowStatusDialog(boolean newShowStatusDialog) {
        showStatusDialog = newShowStatusDialog;
    }
    
    
    synchronized void  startReconnect(final CollabSession session) {
        if (doReconnect){
            addElem(session);
        }
    }
    
    synchronized void  addElem(CollabSession session) {
        if (!sessionList.contains(session)) {
            sessionList.addElement(session);
            showStatusDialog(session.getAccount().getServer());
        }
    }
    
    synchronized void  removeElem(CollabSession session) {
        sessionList.remove(session);
    }
    
    
    private synchronized void showStatusDialog(String title) {
        if (showStatusDialog && statusDialog == null) {
            statusDialog =  new ServerStatusPanel(this, title);
            statusDialog.startAction();
        } else {
            startAction();
        }
    }
    
    public synchronized void startAction() {
        setIdle(false);
        
        if (!getLaunch()) {
            attemptReconnect();
        }
    }
    
    public synchronized void stopAction() {
        setIdle(true);
    }
    
    public synchronized void okAction() {
        if (getIdle()){
            endAction();
        } else if (!getDoReconnect()) {
            setIdle(true);
            endAction();
        }
    }
    
    public synchronized void cancelAction() {
        if (getIdle()) {
            endAction();
        }
    }
    
    
    public synchronized void endAction() {
        if ((statusDialog!=null) && (statusDialog.isAlive())) {
            statusDialog.endAction();
        }
        statusDialog = null;
        
        setIdle(false);
        sessionList = new Vector();
        
    }
    
    synchronized void attemptReconnect() {
        setLaunch(true);
        
        new Thread( new Runnable() {
            public void run() {
                int N = 10;
                for (int i=0; i < N; i++) {
                    try { Thread.sleep(1000); } catch(Exception e) {};

                    Iterator iter = ((Vector) sessionList.clone()).iterator();
                    while (iter.hasNext()) {
                        CollabSession session = (CollabSession) iter.next();
                        Account account = session.getAccount();
                        try {
                            CollabManager collabManager = CollabManager.getDefault();
                            CollabSession newSession = collabManager.createSession(account, account.getPassword());
                            collabManager.getUserInterface().changeUI(UserInterface.SHOW_COLLAB_SESSION_PANEL);
                            removeElem(session);
                        } catch(Exception e) {
                            //out("[do not establish] " + account.getUserName() + ": "+ e.getMessage());
                        }
                    }
                    
                    if (sessionList.size() == 0) {
                        break;
                    }
                    
                    if (getIdle()) {
                        setLaunch(false);
                        return;
                    }
                }
                
                endAction();
                setLaunch(false);
            }
        }).start();
    }
}
