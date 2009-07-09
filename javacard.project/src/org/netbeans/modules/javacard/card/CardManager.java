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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.card;

import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.Card;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Anki R Nelaturu
 */
public class CardManager {

    public static CardManager getDefault() {
        //The class is now stateless, so no point in singletonizing it
        return new CardManager();
    }

    public Collection<Card> getServers() {
        Set<Card> result = new HashSet<Card>();
        for (FileObject fld : Utils.sfsFolderForRegisteredJavaPlatforms().getChildren()) {
            if (fld.getNameExt().endsWith(JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION)) {
                FileObject serversFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(fld.getName(), false);
                if (serversFolder != null) {
                    for (FileObject serverFo : serversFolder.getChildren()) {
                        try {
                            DataObject dob = DataObject.find(serverFo);
                            Card server = dob.getLookup().lookup(Card.class);
                            if (server != null && server.isValid()) {
                                result.add(server);
                            }
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean serverDisplayNameExists(String displayName) {
        for (Card server : getServers()) {
            if (server.getId().equals(displayName)) {
                return true;
            }
        }
        return false;
    }

    public boolean serverPortExists(String port) {
        return serverPortExists("", port);
    }

    public boolean serverPortExists(String displayName, String port) {
        for (Card server : getServers()) {
            if (displayName != null && displayName.equals(server.getDisplayName())) {
                continue;
            }
            try {
                Integer i = Integer.parseInt(port);
                if (server.getPortsInUse().contains(i)) {
                    return true;
                }
            } catch (NumberFormatException e) { //Corrupted project.properties
                //do nothing
            }
        }

        return false;
    }

    public boolean serverCardURLExists(String cardURL) {
        for (Card server : getServers()) {
            if (!server.isReferenceImplementation()) {
                if (((RealCard) server).getCardURL().equals(cardURL)) {
                    return true;
                }
            }
        }

        return false;
    }
}
