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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.pojson.PojsonLoad;

/**
 * Exception representing error connecting to kenai or internal kenai server
 * error
 * @author Jan Becicka
 */
public class KenaiException extends IOException {

    private String errorResponse = "";
    private String status;
    private HashMap<String, String> errors;

    /**
     * Constructs an {@code KenaiException} with the specified detail message.
     *
     * @param msg
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     */
    public KenaiException(String msg) {
        super(msg);
    }

    /**
     * Constructs an {@code KenaiException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     */
    public KenaiException(Throwable cause) {
        super();
        initCause(cause);
    }

    /**
     * Constructs an {@code KenaiException} with the specified detail message,
     * cause and errorResponse.
     *
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     *
     * @param errorResponse
     *         String response from server (which is saved for later
     *         retrieval by the {@link #getAsString()} method).  (A null value
     *         is permitted, and indicates that the errorResponse is nonexistent
     *         or unknown.)
     *
     */
    public KenaiException(String message, Throwable cause, String errorResponse) {
        super(message);
        initCause(cause);
        this.errorResponse = errorResponse;
    }

    /**
     * Constructs an {@code KenaiException} with the specified detail message
     * and errorResponse.
     *
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     *
     * @param errorResponse
     *         String response from server (which is saved for later
     *         retrieval by the {@link #getAsString()} method).  (A null value
     *         is permitted, and indicates that the errorResponse is nonexistent
     *         or unknown.)
     *
     */
    public KenaiException(String message, String errorResponse) {
        this(message);
        this.errorResponse = errorResponse;
    }

    public <T> T getKenaiError(Class<T> clazz) {
        PojsonLoad load = PojsonLoad.create();
        return load.load(errorResponse==null?"":errorResponse, clazz);
    }

    private void fillErrorData() {
        PojsonLoad load = PojsonLoad.create();
        try {
            if (errorResponse==null) {
                status = "unknown"; //NOI18N
                errors = new HashMap<String, String>();
            } else {
                final HashMap toCollections = (HashMap) load.toCollections(errorResponse);
                if (toCollections == null) {
                    status = "unknown"; //NOI18N
                    errors = new HashMap<String, String>();
                    errors.put("generic", errorResponse); //NOI18N
                } else {
                    status = (String) toCollections.get("status");
                    errors = (HashMap<String, String>) toCollections.get("errors");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(KenaiException.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException e) {
            status = "unknown"; //NOI18N
            errors = new HashMap<String, String>();
            errors.put("generic", errorResponse); //NOI18N
        }
    }

    /**
     * get error response as string
     * @return returns string representation of server response
     */
    public String getAsString() {
        return errorResponse;
    }

    /**
     * getAsString() method mapped to key, value map
     * @return
     */
    public Map<String, String> getAsMap() {
        if (errorResponse!=null) {
            PojsonLoad load = PojsonLoad.create();
            try {
                return (HashMap<String, String>) load.toCollections(errorResponse);
            } catch (IOException ex) {
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    /**
     * get status according to
     * <a href="http://kenai.com/projects/kenai/pages/API#Errors">spec</a>
     * @return status
     */
    public String getStatus() {
        if (errorResponse==null)
            return null;
        if (status == null) {
            fillErrorData();
        }
        return status;
    }

    /**
     * get errors according to
     * <a href="http://kenai.com/projects/kenai/pages/API#Errors">spec</a>
     * @return keay-value map of errors
     */
    public Map<String, String> getErrors() {
        if (errorResponse==null)
            return Collections.emptyMap();
        if (errors == null) {
            fillErrorData();
        }
        return errors;
    }

    @Override
    public String toString() {
        return errorResponse==null?super.toString(): super.toString() + ". Server response=" + errorResponse; //NOI18N
    }
}
