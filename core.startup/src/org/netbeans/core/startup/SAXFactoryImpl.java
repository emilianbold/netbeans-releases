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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A special SAXParserFactory that delegates to other factories till it finds
 * one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class SAXFactoryImpl extends SAXParserFactory {

    private static Class<? extends SAXParserFactory> getFirst() {
        try {
            String name = System.getProperty("nb.backup." + SAXParserFactory_PROP); // NOI18N
            return name == null ? null : Class.forName(name).asSubclass(SAXParserFactory.class);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private Map<String,Boolean> features = new LinkedHashMap<String,Boolean>();
    
    /** The default property name according to the JAXP spec */
    private static final String SAXParserFactory_PROP =
        "javax.xml.parsers.SAXParserFactory"; // NOI18N

    public static void install() {
            System.getProperties().put(SAXParserFactory_PROP,
                                   SAXFactoryImpl.class.getName());
    }

    static {
        if (getFirst() == null) {
            ClassLoader orig = Thread.currentThread().getContextClassLoader();
            // Not app class loader. only ext and bootstrap
            try {
               Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader().getParent());
               System.setProperty("nb.backup." + SAXParserFactory_PROP,SAXParserFactory.newInstance().getClass().getName()); // NOI18N
            } finally {
               Thread.currentThread().setContextClassLoader(orig);            
            }
        }
        DOMFactoryImpl.install();
        SAXFactoryImpl.install();
    }
    
    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return features.get(name);
    }

    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        SAXParser parser = tryCreate();
        return parser;
    }

    public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        features.put(name, value);
        tryCreate();
    }

    private SAXParser tryCreate() throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        for (Iterator<Class<? extends SAXParserFactory>> it = 
            new LazyIterator(getFirst(), SAXParserFactory.class, SAXFactoryImpl.class); 
            it.hasNext(); 
        ) {
            try {
                SAXParser parser = tryCreate(it.next());
                return parser;
            } catch (ParserConfigurationException e) {
                if (!it.hasNext()) throw e;
            } catch (SAXNotRecognizedException e) {
                if (!it.hasNext()) throw e;
            } catch (SAXNotSupportedException e) {
                if (!it.hasNext()) throw e;
            } catch (SAXException e) {
                if (!it.hasNext()) throw new ParserConfigurationException();
            }
        }
        throw new IllegalStateException("Can't get here!"); // NOI18N
    }

    private SAXParser tryCreate(Class<? extends SAXParserFactory> delClass) throws ParserConfigurationException, SAXException {
        Exception ex = null;
        try {
            SAXParserFactory delegate = delClass.newInstance();
            delegate.setValidating(isValidating());
            delegate.setNamespaceAware(isNamespaceAware());
            for (Map.Entry<String,Boolean> entry : features.entrySet()) {
                delegate.setFeature(entry.getKey(), entry.getValue());
            }
            return delegate.newSAXParser();
        } catch (InstantiationException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        }
        throw (ParserConfigurationException) new ParserConfigurationException("Broken factory").initCause(ex); // NOI18N
    }
}
