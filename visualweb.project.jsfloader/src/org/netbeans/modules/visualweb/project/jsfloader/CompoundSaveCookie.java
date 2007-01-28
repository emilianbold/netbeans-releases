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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.visualweb.project.jsfloader;


import java.io.IOException;

import org.openide.cookies.SaveCookie;


/**
 * Compound save cookie. It provides save cookie for jsp and java pair.
 *
 * @author  Peter Zavadsky
 */
class CompoundSaveCookie implements SaveCookie {

    private final SaveCookie firstSaveCookie;

    private final SaveCookie secondSaveCookie;


    /** Creates a new instance of CompoundSaveCookie */
    public CompoundSaveCookie(SaveCookie firstSaveCookie, SaveCookie secondSaveCookie) {
        this.firstSaveCookie = firstSaveCookie;
        this.secondSaveCookie = secondSaveCookie;
    }

    /**
     * Return true if one of my composed save cookies is cookie. We could have
     * overriden equals, but since equals has no way to be symmetric, we added a
     * new operation to differentiate.
     */
    boolean containsCookie(SaveCookie cookie) {
        if (cookie == null)
            return false;
        return firstSaveCookie == cookie || secondSaveCookie == cookie;
    }

    public void save() throws IOException {
        if(firstSaveCookie != null) {
            try {
                firstSaveCookie.save();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw ioe;
            }
        }
        if(secondSaveCookie != null) {
            try {
                secondSaveCookie.save();
            } catch(IOException ioe) {
                ioe.printStackTrace();
                throw ioe;
            }
        }
    }
}
