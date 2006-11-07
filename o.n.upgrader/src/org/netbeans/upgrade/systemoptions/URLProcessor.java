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

package org.netbeans.upgrade.systemoptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author Milos Kleint
 */
class URLProcessor extends PropertyProcessor {    
    URLProcessor() {
        super("java.net.URL");//NOI18N
    }
    
    void processPropertyImpl(String propertyName, Object value) {
        StringBuffer sb = new StringBuffer();       
        if ("mainProjectURL".equals(propertyName)) {//NOI18N
            List l = ((SerParser.ObjectWrapper)value).data;
            try {
                URL url = createURL(l);
                addProperty(propertyName, url.toExternalForm());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }  else {
            throw new IllegalStateException();
        }
    }
    
    public static URL createURL(List l) throws MalformedURLException {
            String protocol = null;
            String host = null;
            int port = -1;
            String file = null;
            String authority = null;
            String ref = null;
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object elem = (Object) it.next();
                if (elem instanceof SerParser.NameValue) {
                    SerParser.NameValue nv = (SerParser.NameValue)elem;
                    if (nv.value != null && nv.name != null) {
                        if (nv.name.name.equals("port")) {//NOI18N
                            port = ((Integer)nv.value).intValue();//NOI18N
                        }
                        else if (nv.name.name.equals("file")) {//NOI18N
                            file = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("authority")) {//NOI18N
                            authority = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("host")) {//NOI18N
                            host = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("protocol")) {//NOI18N
                            protocol = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("ref")) {//NOI18N
                            ref = nv.value.toString();//NOI18N
                        }
                    }
                }
            }
            return new URL(protocol, host, port, file);
        
    }
}
