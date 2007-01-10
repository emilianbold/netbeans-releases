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

package org.netbeans.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/** Global password protected sites Authenticator for IDE
 *
 * @author Jiri Rechtacek
 */

class NbAuthenticator extends java.net.Authenticator {
    NbAuthenticator () {
        Preferences proxySettingsNode = NbPreferences.root ().node ("/org/netbeans/core");
        assert proxySettingsNode != null;
    }

    protected java.net.PasswordAuthentication getPasswordAuthentication() {
        Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.FINER, "Authenticator.getPasswordAuthentication() with prompt " + this.getRequestingPrompt());
        
        if (ProxySettings.useAuthentication ()) {
            Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.FINER, "Username set to " + ProxySettings.getAuthenticationUsername () + " while request " + this.getRequestingURL ());
            return new java.net.PasswordAuthentication (ProxySettings.getAuthenticationUsername (), ProxySettings.getAuthenticationPassword ());
        } else {
            Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.WARNING, "No authentication set while requesting " + this.getRequestingURL ());
            return null;
        }
        
    }
}
