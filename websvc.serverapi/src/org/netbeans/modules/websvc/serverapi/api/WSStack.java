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

package org.netbeans.modules.websvc.serverapi.api;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.websvc.serverapi.WSStackAccessor;
import org.netbeans.modules.websvc.serverapi.spi.WSStackSPI;

/**
 *
 * @author mkuchtiak
 */
public final class WSStack {
    
    private WSStackSPI spi;
    private WSStackProvider stackProvider;
    
    // WS Stacks
    /** JAX-WS(Metro) stack */
    public static final String STACK_JAX_WS="stack_jax_ws"; //NOI18N
    /** JAX-RPC stack */
    public static final String STACK_JAX_RPC="stack_jax-rpc"; //NOI18N
    /** AXIS stack */
    public static final String STACK_AXIS2="stack_axis2"; //NOI18N

    
    // JAX-WS tools
    /** wsimport */
    public static final String TOOL_WSIMPORT ="tool_wsimport"; //NOI18N
    /** wsgen */
    public static final String TOOL_WSGEN ="tool_wsgen"; //NOI18N
    
    // JAX-RPC tools
    /** wscompile */
    public static final String TOOL_WSCOMPILE ="tool_wscompile"; //NOI18N
    
    // AXIS2 tools
    /** java2wsdl */
    public static final String TOOL_AXIS_JAVA2WSDL ="tool_axis_java2wsdl"; //NOI18N
    /** wsdl2java */
    public static final String TOOL_AXIS_WSDL2JAVA ="tool_axis_wsdl2java"; //NOI18N
    
    // KEYSTORE
    /** keystore */
    public static final String TOOL_KEYSTORE ="tool_keystore"; //NOI18N
    /** truststore */
    public static final String TOOL_TRUSTSTORE ="tool_truststore"; //NOI18N
    /** client keystore */
    public static final String TOOL_KEYSTORE_CLIENT ="tool_keystore_client"; //NOI18N
    /** client truststore */
    public static final String TOOL_TRUSTSTORE_CLIENT ="tool_truststore_client"; //NOI18N
    
    static  {
        WSStackAccessor.DEFAULT = new WSStackAccessor() {

            @Override
            public WSStack createWSStack(WSStackSPI spi, WSStackProvider stackProvider) {
                return new WSStack(spi, stackProvider);
            }
        };
    }
    
    private WSStack(WSStackSPI spi, WSStackProvider stackProvider) {
        if (spi == null)
            throw new IllegalArgumentException();
        this.spi = spi;
        this.stackProvider = stackProvider;
    }
    
    /**
     * WS Stack name, like WSStack.STACK_JAX_WS
     * @return WS stack name
     */
    public String getName() {
        return spi.getName();
    }
    
    /** 
     * WS stack version, like "2.1"
     * @return WS stack version
     */
    public String getVersion() {
        return spi.getVersion();
    }
    
    /** 
     * WS stack provider (SERVER, JDK or IDE).
     * @return WS stack provider
     */
    public WSStackProvider getWSStackProvider() {
        return stackProvider == null ? WSStackProvider.SERVER : stackProvider;
    }

    /** Returns the set of supported tools for particular WS stack.
     *  (for JAX-WS: wsgen, wsimport)  
     * 
     * @return set of supported tools
     */
    public Set<String> getSupportedTools() {
        return spi.getSupportedTools();
    }

    /** Returns the array of jars required for particular tool, that are bundled with the server.
     * 
     * @return File array of jar files
     */
    public File[] getToolClassPathEntries(String toolName) {
        return spi.getToolClassPathEntries(toolName);
    }

    /** Returns the URI patterns for web service.
     *  The pattern how WS URI and WSDL URI are computed for web service
     * @return implementation of WSUriDescriptor
     */
    public WSUriDescriptor getServiceUriDescriptor() {
        return spi.getServiceUriDescriptor();
    }

    /** Returns WS features supported by particular WS stack.
     *  (e.g. JSR_109, WSIT, ... )  
     * 
     * @return set of supported WS features
     */
    public Set<WSStackFeature> getServiceFeatures() {
        return spi.getServiceFeatures();
    }

}
