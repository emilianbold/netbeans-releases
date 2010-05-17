/*
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
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.lib.collab;

/**
 * Interface for listening to the events generated for user and
 * gateway registration
 *

 *
 * @since version 0.1
 *
 */
public interface RegistrationListener {

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String FIRST = "first";

    public static final String LAST = "last";

    public static final String NAME = "name";

    public static final String EMAIL = "email";

    public static final String REGISTERED = "registered";
    
    public static final String NICK = "nick";
    
    public static final String ADDRESS = "address";
    
    public static final String CITY = "city";
    
    public static final String STATE = "state";
    
    public static final String ZIP = "zip";
    
    public static final String PHONE = "phone";
    
    public static final String URL = "url";
    
    public static final String DATE = "date";
        
    public static final String MISC = "misc";
    
    public static final String TEXT = "text";
        
    public static final String INSTRUCTIONS = "instructions";
    
    /**
     * Service unavailable error condition.
     */
    public static final String SERVICE_UNAVAILABLE = "service_unavailable";
    
    /**
     * Not authorized error condition.
     */
    public static final String NOT_AUTHORIZED = "not_authorized";
    
    /**
     * Not registered error condition.
     */
    public static final String NOT_REGISTERED = "not_registered";
    
    /**
     * Already registered error condition.
     */
    public static final String ALREADY_REGISTERED = "already_registered";
    
    /**
     * Missing data error condition.
     */
    public static final String MISSING_DATA = "missing_data";
    
    /**
     * Unknown error condition.
     */
    public static final String UNKNOWN_ERROR_CONDITION = "unknown_error_condition";
    
    /**
     * fill the registration fields and its values in the Map.
     * @param fields - the registration fields required for registering a new user
     * @return boolean - true if registration fields were filled successfully, otherwise false
     */
    public boolean fillRegistrationInformation(java.util.Map fields, String server);
    
    /*
     * call back to notify when the registration failed 
     * @param errorCondition - Error conditions as defined in RegistrationListener
     * @param errorText - human readable description of the error
     */
    public void registrationFailed(String errorCondition, String errorText, String server);
    
    /*
     * call back to notify of the success of the registration request    
     */
    public void registered(String server);
    
    /*
     * call back to notify of the success of the unregistration operation    
     */
    public void unregistered(String server);
    
    
     /*
     * call back to notify when the registration failed 
     * @param errorCondition - Error conditions as defined in RegistrationListener
     * @param errorText - human readable description of the error
     */
    public void unregistrationFailed(String errorCondition, String errorText, String server);
        
    
       /*
        *
        * call back to notify that password has been change successfully
        */
    public void registrationUpdated(String server);
    
    
           /*
        *
        * call back to notify that password change has failed
        */
    public void registrationUpdateFailed(String errorCondition, String errorText, String server);
    
    /*
     * call back to notify the redirect URL for registration
     */
    public void  redirected(java.net.URL url, String server);
    
        
    
}
