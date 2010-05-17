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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;

import org.netbeans.lib.collab.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IMCollabPrincipal extends CollabPrincipal {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private IMCollabSession session;
    private CollaborationPrincipal principal;

    /**
     *
     *
     */
    protected IMCollabPrincipal(IMCollabSession session, CollaborationPrincipal principal) {
        super(
            StringUtility.removeResource(principal.getUID()), 
        //principal.getName());
        parseName(principal.getName()), 
        //principal.getDisplayName());
        parseName(principal.getDisplayName())
        );
        this.session = session;
        this.principal = principal;
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	protected IMCollabPrincipal(String identifier)
    //	{
    //		super(identifier,identifier,identifier);
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected IMCollabPrincipal(String identifier, boolean parseIdentifier)
    //	{
    //		this(identifier,
    //			parseIdentifier ? parseName(identifier) : identifier,
    //			parseIdentifier ? parseName(identifier) : identifier);
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected IMCollabPrincipal(String identifier, String name, 
    //		String displayName)
    //	{
    //		super(identifier,name,displayName);
    //	}

    /**
     *
     *
     */

    /*pkg*/ static String parseName(String identifier) {
        String result = identifier;

        // I identifier contains a name after a trailing slash (/), that 
        // is the display name of the contact.  Otherwise, the display name 
        // is the string before the @ symbol.
        int index = identifier.indexOf("/");

        if (index != -1) {
            result = identifier.substring(index + 1);
        } else {
            index = identifier.indexOf("@");

            if (index != -1) {
                result = identifier.substring(0, index);
            }
        }

        return result;
    }

    /**
     *
     *
     */
    protected CollaborationPrincipal getPrincipal() {
        return principal;
    }

    /**
     *
     *
     */
    public void subscribe() throws CollabException {
        try {
            ((IMCollabSession) getCollabSession()).getPresenceService().subscribe(getIdentifier());
        } catch (CollaborationException e) {
            throw new CollabException(e, "Exception subscribing to principal \"" + this + "\""); // NOI18N
        }
    }

    /**
     *
     *
     */
    public void unsubscribe() throws CollabException {
        try {
            ((IMCollabSession) getCollabSession()).getPresenceService().unsubscribe(getIdentifier());
        } catch (CollaborationException e) {
            throw new CollabException(e, "Exception unsubscribing from principal \"" + // NOI18N
                this + "\""
            ); // NOI18N
        }
    }
}
