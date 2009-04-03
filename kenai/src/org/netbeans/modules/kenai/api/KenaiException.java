/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.pojson.PojsonLoad;

/**
 *
 * @author Jan Becicka
 */
public class KenaiException extends IOException {
    private String errorResponse;
    private String status;
    private HashMap<String,String> errors;

    public KenaiException(String msg) {
        super(msg);
    }

    public KenaiException(Throwable cause) {
        super();
        initCause(cause);
    }

    public KenaiException(String message, Throwable cause, String errorResponse) {
        super(message);
        initCause(cause);
        this.errorResponse = errorResponse;
    }

    public KenaiException(String message, String errorResponse) {
        this(message);
        this.errorResponse = errorResponse;
    }
    
    public <T> T getKenaiError(Class<T> clazz) {
        PojsonLoad load = PojsonLoad.create();
        return load.load(errorResponse, clazz);
    }

    private void fillErrorData() {
        PojsonLoad load =PojsonLoad.create();
        try {
            final HashMap toCollections = (HashMap) load.toCollections(errorResponse);
            status = (String) toCollections.get("status");
            errors = (HashMap<String, String>) toCollections.get("errors");
        } catch (IOException ex) {
            Logger.getLogger(KenaiException.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * get error response as string
     * @return
     */
    public String getAsString() {
        return errorResponse;
    }

    /**
     * get status according to
     * <a href="http://kenai.com/projects/kenai/pages/API#Errors">spec</a>
     * @return
     */
    public String getStatus() {
        if (status==null)
            fillErrorData();
        return status;
    }

    /**
     * get errors according to
     * <a href="http://kenai.com/projects/kenai/pages/API#Errors">spec</a>
     * @return
     */
    public Map<String,String> getErrors() {
        if (errors==null)
            fillErrorData();
        return errors;
    }
}
