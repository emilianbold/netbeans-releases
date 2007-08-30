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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 * @deprecated Replace it with some common log api for J2EE servers.
 *             Consider this just a temporary solution until the api will be
 *             implemented.
 */
public class WLOutputManager {

    private final InputOutput io;

    private final WLTailer standardOutputTailer;

    private final WLTailer standardErrorTailer;

    private boolean finished;

    public WLOutputManager(Process process, String uri) {
        io = UISupport.getServerIO(uri);
        standardOutputTailer = new WLTailer(process.getInputStream(), io.getOut());
        standardErrorTailer = new WLTailer(process.getErrorStream(), io.getErr());
    }

    public final synchronized void start() {
        if (finished || io == null) {
            return;
        }

        try {
            // as described in the api we reset just ouptut
            io.getOut().reset();
        } catch (IOException ex) {
            Logger.getLogger(WLOutputManager.class.getName()).log(Level.INFO, null, ex);
        }

        io.select();
        standardOutputTailer.start();
        standardErrorTailer.start();
    }

    public final synchronized void finish() {
        finished = true;

        standardOutputTailer.finish();
        standardErrorTailer.finish();
    }

}
