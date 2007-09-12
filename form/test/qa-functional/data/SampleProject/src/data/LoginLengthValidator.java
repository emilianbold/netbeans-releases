/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package data;

import org.jdesktop.beansbinding.Validator;

/**
 * Very very very simple example of binding validator
 *
 * @author Jiri Vagner
 */
public class LoginLengthValidator extends Validator{
    private int MIN_LENGTH = 4;

    public Validator.Result validate(Object arg) {

        if (((String) arg).length() < MIN_LENGTH) {
            return new Validator.Result(null, "Password is too short. Min length is " + String.valueOf(MIN_LENGTH));  // NOI18N
        } else {
            return null;
        }
    }
}
