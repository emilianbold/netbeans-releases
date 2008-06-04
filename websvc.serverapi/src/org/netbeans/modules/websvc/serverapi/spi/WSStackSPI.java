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

package org.netbeans.modules.websvc.serverapi.spi;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.websvc.serverapi.api.WSUriDescriptor;
import org.netbeans.modules.websvc.serverapi.api.WSStackFeature;


/**
 *
 * @author mkuchtiak
 */
public interface WSStackSPI {
    /**
     * WS Stack name, like WSStack.STACK_JAX_WS
     * @return WS stack name
     */
    String getName();
    
    /** 
     * WS stack version, like "2.1".
     * @return WS stack version
     */
    String getVersion();

    /** Returns the set of supported tools for particular WS stack.
     *  (for JAX-WS: wsgen, wsimport)  
     * 
     * @return set of supported tools
     */
    Set<String> getSupportedTools();
    
    /** Returns the array of jars required for particular tool, that are bundled with the server.
     *
     * @return File array of jar files
     */   
    File[] getToolClassPathEntries(String toolName);
    
    /** Returns the URI patterns for web service.
     *  The pattern how WS URI and WSDL URI are computed for web service
     * @return implementation of WSUriDescriptor
     */
    WSUriDescriptor getServiceUriDescriptor();
    
    /** Returns WS features supported by particular WS stack.
     *  (e.g. JSR_109, WSIT, ... )  
     * 
     * @return set of supported WS features
     */
    public Set<WSStackFeature> getServiceFeatures();
}
