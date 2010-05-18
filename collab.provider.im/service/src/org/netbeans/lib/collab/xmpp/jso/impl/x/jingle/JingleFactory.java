/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
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

package org.netbeans.lib.collab.xmpp.jso.impl.x.jingle;

import net.outer_planes.jso.AbstractElementFactory;
import org.jabberstudio.jso.NSI;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleContent;

/**
 *
 * @author jerry
 */
public class JingleFactory extends AbstractElementFactory{

    /** Creates a new instance of JingleFactory */
    public JingleFactory() {
        super(NSI.STRICT_COMPARATOR);
    }

    protected void registerSupportedElements() throws IllegalArgumentException {
        putSupportedElement(new JingleImpl(null));
        //putSupportedElement(new JingleContentImpl(null));
        putSupportedElement(new JingleAudioImpl(null));
        putSupportedElement(new JingleUDPImpl(null));
        //putSupportedElement(JingleImpl.NAME, new JingleContentImpl(null));
        //putSupportedElement(JingleImpl.NAME, new JingleContentImpl(null));
        //putSupportedElement(new JingleImpl(null, JingleContent.NAME));
       // putSupportedElement(new NSI("content", JingleImpl.NAME.getNamespaceURI()), new JingleContentImpl(null));
        //putSupportedElement(new JingleAudioImpl(null));
    }
    
    
}
