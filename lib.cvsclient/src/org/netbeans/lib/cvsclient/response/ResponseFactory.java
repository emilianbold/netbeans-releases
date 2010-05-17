/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.response;

import java.util.*;
import java.text.MessageFormat;

/**
 * Create response objects appropriate for handling different types of response
 * @author  Robert Greig
 */
public class ResponseFactory {

    private final Map responseInstancesMap;
    private String previousResponse = null;

    public ResponseFactory() {
        responseInstancesMap = new HashMap();
        responseInstancesMap.put("E", new ErrorMessageResponse()); //NOI18N
        responseInstancesMap.put("M", new MessageResponse()); //NOI18N
        responseInstancesMap.put("Mbinary", new MessageBinaryResponse()); //NOI18N
        responseInstancesMap.put("MT", new MessageTaggedResponse()); //NOI18N
        responseInstancesMap.put("Updated", new UpdatedResponse()); //NOI18N
        responseInstancesMap.put("Update-existing", new UpdatedResponse()); //NOI18N
        responseInstancesMap.put("Created", new CreatedResponse()); //NOI18N
        responseInstancesMap.put("Rcs-diff", new RcsDiffResponse()); //NOI18N
        responseInstancesMap.put("Checked-in", new CheckedInResponse()); //NOI18N
        responseInstancesMap.put("New-entry", new NewEntryResponse()); //NOI18N
        responseInstancesMap.put("ok", new OKResponse()); //NOI18N
        responseInstancesMap.put("error", new ErrorResponse()); //NOI18N
        responseInstancesMap.put("Set-static-directory", new SetStaticDirectoryResponse()); //NOI18N
        responseInstancesMap.put("Clear-static-directory", new ClearStaticDirectoryResponse()); //NOI18N
        responseInstancesMap.put("Set-sticky", new SetStickyResponse()); //NOI18N
        responseInstancesMap.put("Clear-sticky", new ClearStickyResponse()); //NOI18N
        responseInstancesMap.put("Valid-requests", new ValidRequestsResponse()); //NOI18N
        responseInstancesMap.put("Merged", new MergedResponse()); //NOI18N
        responseInstancesMap.put("Notified", new NotifiedResponse()); //NOI18N
        responseInstancesMap.put("Removed", new RemovedResponse()); //NOI18N
        responseInstancesMap.put("Remove-entry", new RemoveEntryResponse()); //NOI18N
        responseInstancesMap.put("Copy-file", new CopyFileResponse()); //NOI18N
        responseInstancesMap.put("Mod-time", new ModTimeResponse()); //NOI18N
        responseInstancesMap.put("Template", new TemplateResponse()); //NOI18N
        responseInstancesMap.put("Module-expansion", new ModuleExpansionResponse()); //NOI18N
        responseInstancesMap.put("Wrapper-rcsOption", new WrapperSendResponse()); //NOI18N
        
    }
    
    public Response createResponse(String responseName) {
        Response response = (Response)responseInstancesMap.get(responseName);
        if (response != null) {
            previousResponse = responseName;
            return response;
        }
        if (previousResponse != null && previousResponse.equals("M")) { //NOI18N
            return new MessageResponse(responseName);
        }
        previousResponse = null;
        IllegalArgumentException2 ex = new IllegalArgumentException2("Unhandled response: " + //NOI18N
                                              responseName + "."); //NOI18N

        // assemble reasonable localized message

        String cvsServer = System.getenv("CVS_SERVER");  // NOI18N
        if (cvsServer == null) {
            cvsServer = "";  // NOI18N
        } else {
            cvsServer = "=" + cvsServer; // NOI18N
        }

        String cvsExe = System.getenv("CVS_EXE");  // NOI18N
        if (cvsExe == null) {
            cvsExe = "";  // NOI18N
        } else {
            cvsExe = "=" + cvsExe; // NOI18N
        }

        ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.lib.cvsclient.response.Bundle");  //NOI18N
        String msg = bundle.getString("BK0001");
        msg = MessageFormat.format(msg, new Object[] {responseName, cvsServer, cvsExe});
        ex.setLocalizedMessage(msg);
        throw ex;
    }

    private static class IllegalArgumentException2 extends IllegalArgumentException {

        private String localizedMessage;

        public IllegalArgumentException2(String s) {
            super(s);
        }

        public String getLocalizedMessage() {
            return localizedMessage;
        }

        private void setLocalizedMessage(String localizedMessage) {
            this.localizedMessage = localizedMessage;
        }


    }
}
