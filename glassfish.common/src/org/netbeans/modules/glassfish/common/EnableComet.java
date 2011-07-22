/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;

/**
 *
 * @author vkraemer
 */
public class EnableComet implements Runnable {

    private final GlassfishModule support;

    public EnableComet(GlassfishModule support) {
        this.support = support;
    }

    public void run() {
        GetPropertyCommand gpc = new GetPropertyCommand("*.comet-support-enabled"); // NOI18N
        Future<OperationState> result = support.execute(gpc);
        //((GlassfishModule) si.getBasicNode().getLookup().lookup(GlassfishModule.class)).execute(gpc);
        try {
            if (result.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> retVal = gpc.getData();
                String newValue = support.getInstanceProperties().get(GlassfishModule.COMET_FLAG);
                if (null == newValue || newValue.trim().length() < 1) {
                    newValue = "false"; // NOI18N
                }
                for (Entry<String, String> entry : retVal.entrySet()) {
                    String key = entry.getKey();
                    // do not update the admin listener....
                    if (null != key && !key.contains("admin-listener")) { // NOI18N
                        SetPropertyCommand spc = support.getCommandFactory().getSetPropertyCommand(key, newValue);
                        Future<OperationState> results = support.execute(spc);
                        //((GlassfishModule) si.getBasicNode().getLookup().lookup(GlassfishModule.class)).execute(gpc);
                        results.get(10, TimeUnit.SECONDS);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        }
    }
}
