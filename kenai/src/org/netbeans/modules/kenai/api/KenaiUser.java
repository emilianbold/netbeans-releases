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

import org.netbeans.modules.kenai.UserData;

/**
 * Class representing user on Kenai
 * @author Jan Becicka
 */
public final class KenaiUser {

    UserData data;

    KenaiUser(UserData data) {
        this.data = data;
    }
    
    /**
     * getter for username
     * @return
     */
    public String getUserName() {
        return data.user_name;
    }
    
    /**
     * getter for first name
     * @return
     */
    public String getFirstName() {
        return data.first_name;
    }
    
    /**
     * getter for last name
     * @return
     */
    public String getLastName() {
        return data.last_name;
    }
    
    /**
     * getter for role
     * @return
     */
    public Role getRole() {
        if ("registered".equals(data.role)) {
            return Role.OBSERVER;
        }
        return Role.valueOf(data.role.toUpperCase());
    }
//    public Status getStatus() {
//        return Status.UNKNOWN;
//    }

    @Override
    public String toString() {
        return getUserName() + " (" + getRole() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KenaiUser other = (KenaiUser) obj;
        if (this.data != other.data && (this.data == null || !this.data.user_name.equals(other.data.user_name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return data.user_name.hashCode();
    }

//    public static enum Status {
//        ONLINE,
//        OFFLINE,
//        DND,
//        UNKNOWN
//    }
    
    /**
     * user role in projects
     */
    public static enum Role {
        ADMIN,
        OBSERVER,
        DEVELOPER
    }
}
