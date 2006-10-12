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

package org.netbeans.modules.j2ee.archive.wizard;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.openide.ErrorManager;

/**
 *
 * @author vbk
 */
public class EJBAnnotationDetector {
    
    private EJBAnnotationDetector() {
    }
    
    
    /**
     * This returns true only if the jar file contains 1 or more
     * javax/ejb/Stateless
     * javax/ejb/Stateful
     * javax/ejb/MessageDriven  annotations
     * Note: caller is expected to close the
     * @param jf the jar to be checked
     */
    public static  boolean containsSomeAnnotatedEJBs(JarFile jf) throws IOException {
        EJBClassFile classFile = new EJBClassFile();
        Enumeration<JarEntry> entriesEnum = jf.entries();
        boolean retVal = false;
        while(!retVal && entriesEnum.hasMoreElements()) {
            JarEntry je = entriesEnum.nextElement();
            if (je.getName().endsWith(".class")) {
                ReadableByteChannel channel = null;
                try {
                    channel = Channels.newChannel(jf.getInputStream(je));
                    retVal = classFile.containsAnnotation(channel, je.getSize());
                } finally {
                    if (null != channel) {
                        try {
                            channel.close();
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    ioe);
                        }
                    }
                }
                
            }
        }
        return retVal;
    }
}
