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

package org.netbeans.core.startup;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;

/**
 * A special DocumentBuilderFactory that delegates to other factories till
 * it finds one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class DOMFactoryImpl extends DocumentBuilderFactory {

    private static Class<? extends DocumentBuilderFactory> getFirst() {
        try {
            String name = System.getProperty("nb.backup." + Factory_PROP); // NOI18N
            return name == null ? null : Class.forName(
                name, true, ClassLoader.getSystemClassLoader()
            ).asSubclass(DocumentBuilderFactory.class);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private Map<String,Object> attributes = new LinkedHashMap<String,Object>();
    private Map<String,Boolean> features = new LinkedHashMap<String,Boolean>();
    
    /** The default property name according to the JAXP spec */
    private static final String Factory_PROP =
        "javax.xml.parsers.DocumentBuilderFactory"; // NOI18N

    public static void install() {
        System.getProperties().put(Factory_PROP,
                                   DOMFactoryImpl.class.getName());
    }

    static {
        if (getFirst() == null) {
            ClassLoader orig = Thread.currentThread().getContextClassLoader();
            // Not app class loader. only ext and bootstrap
            try {
               Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
               System.setProperty("nb.backup." + Factory_PROP, DocumentBuilderFactory.newInstance().getClass().getName()); // NOI18N
            } finally {
               Thread.currentThread().setContextClassLoader(orig);            
            }
        }
        
        DOMFactoryImpl.install();
        SAXFactoryImpl.install();
    }
    
    public Object getAttribute(String name) throws IllegalArgumentException {
        return attributes.get(name);
    }
    
    public boolean getFeature (String name) {
        return Boolean.TRUE.equals(features.get(name));
    }
    
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
        Boolean old = features.put(name, value);
        try {
            tryCreate();
        } catch (IllegalArgumentException e) {
            features.put(name, old);
            throw e;
        } catch (ParserConfigurationException e) {
            features.put(name, old);
            throw e;
        }
    }

    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        try {
            return tryCreate();
        } catch (IllegalArgumentException e) {
            throw (ParserConfigurationException) new ParserConfigurationException(e.toString()).initCause(e);
        }
    }

    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        Object old = attributes.put(name, value);
        try {
            tryCreate();
        } catch (IllegalArgumentException x) {
            attributes.put(name, old);
            throw x;
        } catch (ParserConfigurationException e) {
            attributes.put(name, old);
            throw (IllegalArgumentException) new IllegalArgumentException(e.toString()).initCause(e);
        }
    }
    
    private DocumentBuilder tryCreate() throws ParserConfigurationException, IllegalArgumentException {
        for (
            Iterator<Class<? extends DocumentBuilderFactory>> it 
                = new LazyIterator<DocumentBuilderFactory>(getFirst(), DocumentBuilderFactory.class, DOMFactoryImpl.class); 
            it.hasNext(); 
        ) {
            try {
                DocumentBuilder builder = tryCreate(it.next());
                return builder;
            } catch (ClassCastException e) {
                if (!it.hasNext()) throw e;
            } catch (ParserConfigurationException e) {
                if (!it.hasNext()) throw e;
            } catch (IllegalArgumentException e) {
                if (!it.hasNext()) throw e;
            }
        }
        throw new IllegalStateException("Can't get here!"); // NOI18N
    }

    private DocumentBuilder tryCreate(Class<? extends DocumentBuilderFactory> delClass) throws ParserConfigurationException, IllegalArgumentException {
        Exception ex = null;
        try {
            DocumentBuilderFactory delegate = delClass.newInstance();
            delegate.setNamespaceAware(isNamespaceAware());
            delegate.setValidating(isValidating());
            delegate.setIgnoringElementContentWhitespace(isIgnoringElementContentWhitespace());
            delegate.setExpandEntityReferences(isExpandEntityReferences());
            delegate.setIgnoringComments(isIgnoringComments());
            delegate.setCoalescing(isCoalescing());

            for (Map.Entry<String,Object> entry : attributes.entrySet()) {
                if (entry.getValue() != null) {
                    delegate.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String,Boolean> entry : features.entrySet()) {
                if (entry.getValue() != null) {
                    delegate.setFeature(entry.getKey(), entry.getValue());
                }
            }
            return delegate.newDocumentBuilder();
        } catch (InstantiationException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        }
        throw (ParserConfigurationException) new ParserConfigurationException("Broken factory").initCause(ex); // NOI18N
    }    
}
