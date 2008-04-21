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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.jconsole;

import java.io.PrintWriter;

import org.netbeans.api.xml.cookies.*;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.IOProvider;

/**
 * Create a new pane in the IDE output tab panel.
 *
 * To output a message, use the message method.
 *
 */
public class OutputConsole implements CookieObserver
{
    /** output tab */
    private InputOutput io;

    /** writer to that tab */
    private OutputWriter ow = null;

    private void initInputOutput (String name)
    {
        if (ow != null)
            return;

        // reuse the existing tab with same name if possible
        io = IOProvider.getDefault().getIO(name, false);
        io.select();
        io.setFocusTaken (false);
        ow = io.getOut();
            try {
                // clear the output pane
                ow.reset();
            } catch (java.io.IOException ex) {
                //bad luck
            }
    }

    /** Creates a new instance of OutputConsole */
    public OutputConsole (String name)
    {
        initInputOutput(name);
    }

    // get the embedded PrintWriter interface
    public PrintWriter getPrintWriter()
    {
        if (ow != null)
            return ow;
        else
            return null;
    }

    public void receive (CookieMessage msg)
    {
        if (ow != null)
            ow.println(msg.getMessage());
    }

    public synchronized void message(String message)
    {
        ow.println(message);
    }

    /**
     * Try to move InputOutput to front. Suitable for last message.
     */
    public final void moveToFront()
    {
        boolean wasFocusTaken = io.isFocusTaken();
        io.select();
        io.setFocusTaken(true);
        ow.write("\r");// NOI18N
        io.setFocusTaken(wasFocusTaken);
    }
    
    public void close() {
        io.closeInputOutput();
    }

}