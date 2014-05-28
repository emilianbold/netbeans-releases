/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.editor.document.implspi.DocumentServiceFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Maintains a cache of {@link DocumentServiceFactory}s for individual implementation
 * classes.
 * @author sdedic
 */
public class DocumentServices {
    private static volatile DocumentServices INSTANCE;
    
    private Map<Class, Lookup.Result<DocumentServiceFactory>> factoryMap = new HashMap<>(5);
    
    public static DocumentServices getInstance() {
        if (INSTANCE == null) {
            synchronized (DocumentServices.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DocumentServices();
                }
            }
        }
        return INSTANCE;
    }
    
    public void initDocumentServices(Document doc) {
        doInitDocumentServices(doc, doc.getClass());
    }
    
    //@GuardedBy(this)
    private Lookup.Result<DocumentServiceFactory> initDocumentFactories(Class c) {
        List<Lookup> lkps = new ArrayList<Lookup>(5);
        do {
            String cn = c.getCanonicalName();
            if (cn != null) {
                lkps.add(Lookups.forPath("Editors/Documents/" + cn)); // NOI18N
            }
            c = c.getSuperclass();
        } while (c != null && c != java.lang.Object.class);
        Lookup[] arr = lkps.toArray(new Lookup[lkps.size()]);
        return new ProxyLookup(arr).lookupResult(DocumentServiceFactory.class);
    }
    
    private Lookup doInitDocumentServices(Document doc, Class c) {
        boolean stub = c != doc.getClass();
        Object k = stub ? STUB_KEY : DocumentServices.class;
        Lookup res;

        res = (Lookup)doc.getProperty(k);
        if (res != null) {
            return res;
        }
        Lookup.Result<DocumentServiceFactory> factories;
        
        synchronized (this) {
            factories = factoryMap.get(c);
            if (factories == null) {
                factories = initDocumentFactories(c);
            }
        }
        Collection<? extends DocumentServiceFactory> col = factories.allInstances();
        Collection<Lookup> lkps = new ArrayList<Lookup>(col.size());
        for (DocumentServiceFactory f : col) {
            try {
                Lookup l = f.forDocument(doc);
                if (l == null) {
                    continue;
                }
                lkps.add(l);
            } catch (Exception ex) {
            }
        }
        res = new ProxyLookup(lkps.toArray(new Lookup[lkps.size()]));
        doc.putProperty(k, res);
        
        return res;
    }
    
    private static final Object STUB_KEY = DocumentServices.class.getName() + ".stub"; // NOI18N
    
    public Lookup getStubLookup(Document doc) {
        return doInitDocumentServices(doc, Document.class);
    }
    
    public Lookup getLookup(Document doc) {
        return doInitDocumentServices(doc, doc.getClass());
    }
}
