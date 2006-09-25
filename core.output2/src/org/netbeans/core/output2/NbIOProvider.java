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

package org.netbeans.core.output2;

import java.io.IOException;
import javax.swing.Action;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Supplies Output Window implementation through Lookup.
 * @author Jesse Glick, Tim Boudreau
 */
public final class NbIOProvider extends IOProvider {
    private static final PairMap namesToIos = new PairMap();

    private static final String STDOUT = NbBundle.getMessage(NbIOProvider.class,
        "LBL_STDOUT"); //NOI18N
    
    public OutputWriter getStdOut() {
        if (Controller.LOG) {
            Controller.log("NbIOProvider.getStdOut");
        }
        InputOutput stdout = getIO (STDOUT, false, null);
        NbWriter out = ((NbIO)stdout).writer();
        
        Controller.ensureViewInDefault ((NbIO) stdout, true);
        //ensure it is not closed
        if (out != null && out.isClosed()) {
            try {
                out.reset();
                out = (NbWriter) stdout.getOut();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
                stdout = getIO (STDOUT, true, null);
                out = (NbWriter) stdout.getOut();
            }
        } else {
            out = (NbWriter) stdout.getOut();
        }
        return out;
    }
    
    
    public InputOutput getIO(String name, boolean newIO) {
        return getIO (name, newIO, new Action[0]);
    }
    
    public InputOutput getIO(String name, Action[] toolbarActions) {
        return getIO (name, true, toolbarActions);
    }
    
    private InputOutput getIO(String name, boolean newIO, Action[] toolbarActions) {
        if (Controller.LOG) {
            Controller.log("GETIO: " + name + " new:" + newIO);
        }
        NbIO result = namesToIos.get(name);

        if (result == null || newIO) {
            result = new NbIO(name, toolbarActions);
            namesToIos.add (name, result);
            Controller.ensureViewInDefault (result, newIO);
        } else {
            // mkleint ignore actions if reuse of tabs.
//            result.setToolbarActions(toolbarActions);
        }
        return result;
    }
    
    
    static void dispose (NbIO io) {
        namesToIos.remove (io);
    }
    
    /**
     * Called when the output window is hidden.  Switches the caching of IOs
     * to weak references, so that only those still alive will be shown the 
     * next time the output window is opened.
     */
    static void setWeak(boolean value) {
        namesToIos.setWeak(value);
    }
}

