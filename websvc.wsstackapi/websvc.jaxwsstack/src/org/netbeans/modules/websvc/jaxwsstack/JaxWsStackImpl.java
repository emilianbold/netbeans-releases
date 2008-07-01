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

package org.netbeans.modules.websvc.jaxwsstack;

import java.io.File;
import java.net.URL;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsStackImpl implements WSStackImplementation<JaxWs> {
    
    private File file;
    
    JaxWsStackImpl(File file) {
        this.file = file;
    }
    
    public JaxWs get() {
        return new JaxWs(file);
    }

    public WSStackVersion getVersion() {
        return WSStackVersion.valueOf(1, 2, 3, 0);
    }

    public WSTool getWSTool(final Tool toolId) {
        if (toolId == JaxWs.Tool.WSGEN || toolId == JaxWs.Tool.WSIMPORT) {
            return WSStackFactory.createWSTool ( new WSToolImplementation() {

                public String getName() {
                    return toolId.getName();
                }

                public URL[] getLibraries() {
                    return new URL[] {};
                }
                
            });
        }
        return null;
    }

    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.WSIT || feature == JaxWs.Feature.JSR109) {
            return true;
        }
        return false;
    }

}
