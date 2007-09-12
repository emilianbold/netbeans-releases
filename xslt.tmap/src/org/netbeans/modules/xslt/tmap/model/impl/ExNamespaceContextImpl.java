/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xslt.tmap.model.api.ExNamespaceContext;

/**
 *
 * @author ads
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ExNamespaceContextImpl implements ExNamespaceContext {
    static final String DEFAULT_NS = "ns";      // NOI18N
    private TMapComponentAbstract myComponent;

    public ExNamespaceContextImpl( TMapComponentAbstract component) {
        myComponent = component;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI( String prefix ) {

// TODO m
////        myElement.readLock();
////        try {
            return myComponent.lookupNamespaceURI(prefix);
////        }
////        finally {
////            myElement.readUnlock();
////        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix( String uri ) {
// TODO m
////        myElement.readLock();
////        try {
            String str = myComponent.getPeer().lookupPrefix(uri);
            if ( str!=null && str.length() == 0) {
                // it seems this is some buf in XDM when "" prefix is returned .
                return null;
            }
            return str;
////        }
////        finally {
////            myElement.readUnlock();
////        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator getPrefixes( String uri ) {
// TODO m        
////        myElement.readLock();
////        try {
            assert uri != null;

            List<String> list = new LinkedList<String>();

            TMapComponentAbstract component = myComponent;
            while (component != null) {
                fillPrefixes(component, uri, list);
                component = (TMapComponentAbstract) component.getParent();
            }

            return list.iterator();
////        }
////        finally {
////            myElement.readUnlock();
////        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.support.ExNamespaceContext#getPrefixes()
     */
    public Iterator<String> getPrefixes() {
// TODO m        
////        myElement.readLock();
////        try {
            List<String> list = new LinkedList<String>();

            TMapComponentAbstract component = myComponent;
            while (component != null) {
                fillPrefixes(component, null, list);
                component = (TMapComponentAbstract) component.getParent();
            }

            return list.iterator();
////        }
////        finally {
////            myElement.readUnlock();
////        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.support.ExNamespaceContext#addNamespace(java.lang.String)
     */
    public String addNamespace( String uri ) throws InvalidNamespaceException {
        try {
            new URI(uri);
        }
        catch (URISyntaxException e) {
            InvalidNamespaceException exc = new InvalidNamespaceException(e
                    .getMessage());
            throw exc;
        }
// TODO m        
////        myComponent.writeLock();
////        try {
            if (getPrefix(uri) != null) {
                return getPrefix(uri);
            }

            int i = findAppropriateIndex(null, uri);
            i++;
            String resultPrefix = DEFAULT_NS + i;

            TMapComponentAbstract component = getRoot();
            component.addPrefix(resultPrefix, uri);
            return resultPrefix;
////        }
////        finally {
////            myComponent.writeUnlock();
////        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.support.ExNamespaceContext#addNamespace(java.lang.String,
     *      java.lang.String)
     */
    public void addNamespace( String prefix, String uri )
            throws InvalidNamespaceException
    {
        try {
            new URI(uri);
        }
        catch (URISyntaxException e) {
            InvalidNamespaceException exc = new InvalidNamespaceException(e
                    .getMessage());
            throw exc;
        }
// TODO m        
////        myElement.writeLock();
////        try {
            if (!Utils.isValidNCName(prefix)) {
                throw new InvalidNamespaceException("Prefix : '" + prefix +// NOI18N
                        "' is not acceptable as prefix fot namespace."); // NOI18N
            }

            findAppropriateIndex(prefix, uri);

            getRoot().addPrefix(prefix, uri);
////        }
////        finally {
////            myElement.writeUnlock();
////        }
    }

    private void fillPrefixes( TMapComponentAbstract component, String uri,
            List<String> list )
    {
        Map<String, String> map = component.getPrefixes();
        for (Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (uri == null) {
                list.add(key);
            }
            if ((value != null) && (value.equals(uri))) {
                list.add(key);
            }

        }
    }

    private int findAppropriateIndex( String pref, String uri )
            throws InvalidNamespaceException
    {
        Iterator<String> iterator = getPrefixes();
        int i = 0;
        while (iterator.hasNext()) {
            String str = (String) iterator.next();
            if (pref != null) {
                if (pref.equals(str)) {
                    String foundUri = getNamespaceURI(pref);
                    if (foundUri.equals(uri)) {
                        return 0;
                    }
                    else {
                        throw new InvalidNamespaceException(
                                "Element's scope already have " + "prefix "// NOI18N
                                        + pref + " and it declared "// NOI18N
                                        + "with different namespace uri"); // NOI18N
                    }
                }
            }
            if (str.startsWith(DEFAULT_NS)) {
                String end = str.substring(2);
                try {
                    int ind = Integer.parseInt(end);
                    if (ind > i) {
                        i = ind;
                    }
                }
                catch (NumberFormatException e) {
                    // we don't care about it.
                }
            }

        }
        return i;
    }

    private TMapComponentAbstract getRoot() {
        TMapComponentAbstract component = myComponent;
        while (component.getParent() != null) {
            component = (TMapComponentAbstract) component.getParent();
        }
        return component;
    }

}
