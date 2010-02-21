/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.exceptions;

import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.lib.uihandler.NBBugzillaAccessor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsSettings {
    
    private static final String userProp = "UserName";       // NOI18N
    private static final String passwdProp = "Passwd";
    private static final String passwdKey = "exceptionreporter"; // NOI18N
    private static final String guestProp = "Guest";
    private static final String rememberProp = "RememberPasswd";
    private final NBBugzillaAccessor nba;


    /** Creates a new instance of ExceptionsSettings */
    public ExceptionsSettings() {
        nba = Lookup.getDefault().lookup(NBBugzillaAccessor.class);
    }

    private Preferences prefs() {
        return NbPreferences.forModule(ExceptionsSettings.class);
    }

    public String getUserName() {
        if (nba != null){
            String username = nba.getNBUsername();
            if (username != null){
                return username;
            }
        }
        return prefs().get(userProp, "");
    }

    public void setUserName(String userName) {
        if (nba != null){
            nba.saveNBUsername(userName);
        }else{
            prefs().put(userProp, userName);
        }
    }
        
    public char[] getPasswd() {
        if (nba != null){
            char[] passwd = nba.getNBPassword();
            if (passwd != null){
                return passwd;
            }
        }
        String old = prefs().get(passwdProp, null);
        if (old != null) {
            setPasswd(old.toCharArray());
            prefs().remove(passwdProp);
        }
        char[] passwd =  Keyring.read(passwdKey);
        if (passwd != null){
            return passwd;
        }
        return new char[0];
    }

    public void setPasswd(char[] passwd) {
        if (nba != null) {
            nba.saveNBPassword(passwd);
        } else {
            Keyring.save(passwdKey, passwd,
                    NbBundle.getMessage(ExceptionsSettings.class, "ExceptionsSettings.password.description"));
        }
    }
    
    public boolean isGuest() {
        return prefs().getBoolean(guestProp, false);
    }

    public void setGuest(Boolean guest){
        prefs().putBoolean(guestProp, guest);
    }

    public boolean rememberPasswd(){
        return prefs().getBoolean(rememberProp, true);
    }

    public void setRememberPasswd(boolean remember){
        prefs().putBoolean(rememberProp, remember);
    }
    
}
