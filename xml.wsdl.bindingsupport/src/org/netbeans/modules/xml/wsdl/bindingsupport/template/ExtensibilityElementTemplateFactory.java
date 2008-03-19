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

package org.netbeans.modules.xml.wsdl.bindingsupport.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.xml.sax.SAXException;

public class ExtensibilityElementTemplateFactory {

    private Map<String, TemplateGroup> templateGroupMap;
    private Map<String, ExtensibilityElementTemplateProvider> providerMap;
    
    private Map<TemplateGroup, LocalizedTemplateGroup> localizedTemplateGroupMap;
    
    private static ExtensibilityElementTemplateFactory factory;
    enum State {
        CHANGE_REQUIRED,
        CHANGING,
        CHANGE_COMPLETED
    }
    private State hasChanged = State.CHANGE_REQUIRED;
    
    private ExtensibilityElementTemplateFactory() {
        initialise();
    }
    
    public static ExtensibilityElementTemplateFactory getDefault() {
        if (factory == null) {
            createFactory();
        }
        return factory;
    }
    
    static void createFactory() {
        synchronized (ExtensibilityElementTemplateFactory.class) {
            if (factory == null) {
                factory = new ExtensibilityElementTemplateFactory();
            }
        }
    }

    private void checkUpdates() {
        if (hasChanged == State.CHANGE_REQUIRED) {
            lookupProviders();
        }
    }

    private void initialise() {
        Result<ExtensibilityElementTemplateProvider> lookupResult = Lookup.getDefault().lookupResult(ExtensibilityElementTemplateProvider.class);
        lookupResult.addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                hasChanged = State.CHANGE_REQUIRED;
            }
        });
        templateGroupMap = new HashMap<String, TemplateGroup>();
        providerMap = new HashMap<String, ExtensibilityElementTemplateProvider>();
        localizedTemplateGroupMap = new HashMap<TemplateGroup, LocalizedTemplateGroup>();
        
        lookupProviders();
    }
    
    public TemplateGroup getExtensibilityElementTemplateGroup(String namespace) {
        checkUpdates();
        if (templateGroupMap == null || namespace == null) return null;
        return templateGroupMap.get(namespace);
    }
    
    public Collection<TemplateGroup> getExtensibilityElementTemplateGroups() {
        checkUpdates();
        if (templateGroupMap == null) return null;
        return templateGroupMap.values();
    }
    
    public String getLocalizedMessage(String namespace, String str, Object[] objs) {
        assert namespace != null : "namespace cannot be null";
        assert str != null : "message key cannot be null";
        checkUpdates();
        
        ExtensibilityElementTemplateProvider provider = providerMap.get(namespace);
        if (provider != null) {
            return provider.getLocalizedMessage(str, objs);
        }
        
        return null;
    }
    
    public LocalizedTemplateGroup getLocalizedTemplateGroup(TemplateGroup group) {
        checkUpdates();
        String namespace = group.getNamespace();
        ExtensibilityElementTemplateProvider provider = providerMap.get(namespace);
        
        assert provider != null : "ExtensibilityElementTemplateProvider cannot be null";
        LocalizedTemplateGroup localTemplateGroup = localizedTemplateGroupMap.get(group);
        if(localTemplateGroup == null) {
            localTemplateGroup = new LocalizedTemplateGroup(group, provider);
            localizedTemplateGroupMap.put(group, localTemplateGroup);
        }
        
        return localTemplateGroup;
        
    }
    
    private synchronized void lookupProviders() {
        if (hasChanged == State.CHANGE_COMPLETED) return;
        
        hasChanged = State.CHANGING;
        
        localizedTemplateGroupMap.clear();
        providerMap.clear();
        templateGroupMap.clear();

        
        for(ExtensibilityElementTemplateProvider provider : Lookup.getDefault().lookupAll(ExtensibilityElementTemplateProvider.class)) {
            InputStream stream = provider.getTemplateInputStream();
            if (stream != null) {
                TemplateGroup group;
                try {
                    group = TemplateGroup.read(stream);
                    templateGroupMap.put(group.getNamespace(), group);
                    providerMap.put(group.getNamespace(), provider);
                } catch (ParserConfigurationException e) {
                    ErrorManager.getDefault().notify(e);
                } catch (SAXException e) {
                    ErrorManager.getDefault().notify(e);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
        if (hasChanged == State.CHANGING) hasChanged = State.CHANGE_COMPLETED;
        
    }
    
}
