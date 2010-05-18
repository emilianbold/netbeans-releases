/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.core;

import org.netbeans.modules.xml.catalog.XmlGlobalCatalog;

public class BPELCatalog extends XmlGlobalCatalog {

    public BPELCatalog() {
        super("LBL_BPEL_Catalog", "DSC_BPEL_Catalog", "resources/bpel_catalog.gif"); // NOI18N

        register("http://schemas.xmlsoap.org/ws/2003/03/business-process/", "bpel4ws_1_1.xsd"); // NOI18N
        register("http://schemas.xmlsoap.org/ws/2003/05/partner-link/", "bpel4ws_1_1_plinkType.xsd"); // NOI18N
        register("http://docs.oasis-open.org/wsbpel/2.0/process/executable", "wsbpel_2_0.xsd"); // NOI18N
        register("http://docs.oasis-open.org/wsbpel/2.0/plnktype", "ws-bpel_plnktype.xsd"); // NOI18N
        register("http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/ErrorHandling", "ErrorHandling.xsd"); // NOI18N
        register("http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor", "editor.xsd"); // NOI18N
        register("http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Trace", "trace.xsd"); // NOI18N
        register("http://docs.oasis-open.org/wsbpel/2.0/serviceref", "http://docs.oasis-open.org/wsbpel/2.0/OS/serviceref/ws-bpel_serviceref.xsd", "ws-bpel_serviceref.xsd"); // NOI18N
        register("http://schemas.xmlsoap.org/ws/2004/08/addressing", "ws-addressing.xsd"); // NOI18N
        register("http://www.w3.org/2005/08/addressing", "http://www.w3.org/2006/03/addressing/ws-addr.xsd", "ws-addr.xsd"); // NOI18N
    }

    private void register(String systemId, String source) {
        register(systemId, systemId, source);
    }

    private void register(String systemId, String location, String source) {
        registerEntry(SCHEMA + systemId, systemId, location, "nbres:/org/netbeans/modules/bpel/model/api/resources/" + source); // NOI18N
    }
}
