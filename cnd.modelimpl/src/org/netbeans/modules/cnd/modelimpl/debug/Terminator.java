/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.debug;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserQueue;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.openide.LifecycleManager;
import org.openide.util.RequestProcessor;


/**
 * For testing purposes, allow safe closing IDE at the time of parsing finish.
 *
 * @author Sergey Grinev
 */
public class Terminator implements Runnable {
    
    private ProjectBase project;
    private boolean timeout = false;
    
    private Terminator(ProjectBase project) {
        super();
        this.project = project;
    }
    
    private static Object lock = new Object();
    private static int inParse = 0;
    
    public static void create(ProjectBase project) {
        RequestProcessor.getDefault().post(new Terminator(project));
    }
    
    public void run() {
        synchronized (lock) {
            inParse++;
        }
        System.err.println("Parse started. " + inParse + " projects in list");
        if (TraceFlags.CLOSE_TIMEOUT > 0) {
            ActionListener terminator2 = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((ModelImpl)CsmModelAccessor.getModel()).shutdown();
                    synchronized (lock) {
                        timeout = true;
                    }
                }
            };
            new javax.swing.Timer(TraceFlags.CLOSE_TIMEOUT*1000, terminator2).start();
        }
        project.waitParse();
        synchronized (lock) {
            inParse--;
            System.err.println("Parse finished. " + inParse + " projects left");
            if (inParse == 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        terminate();
                    }
                });
            }
        }
    }
    
    private void terminate() {
        synchronized (lock) {
            long ptime = ParserQueue.instance().getStopWatchTime();
            System.err.println("disposing at " + ptime);
            String xmlOutput = System.getProperty("cnd.close.report.xml");
            if (xmlOutput != null) {
                BufferedWriter out;
                try {
                    out = new BufferedWriter(new FileWriter(xmlOutput, true));
                    String result = timeout ? "failed" : "passed";
                    out.write("<result>" + result + "</result>");
                    out.write("<parsetime>" + ptime + "</parsetime>");
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        LifecycleManager.getDefault().exit();
    }
}
