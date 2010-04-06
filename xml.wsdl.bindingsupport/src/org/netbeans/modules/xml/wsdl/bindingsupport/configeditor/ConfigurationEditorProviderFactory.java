/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.configeditor;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author skini
 */
public class ConfigurationEditorProviderFactory {
    private static final Map<String, ExtensibilityElementConfigurationEditorProvider> map = new HashMap<String, ExtensibilityElementConfigurationEditorProvider>();
    private static final ConfigurationEditorProviderFactory factory = new ConfigurationEditorProviderFactory();
    
    enum State {

        CHANGE_REQUIRED,
        CHANGING,
        CHANGE_COMPLETED
    }
    private State hasChanged = State.CHANGE_REQUIRED;

    
    private ConfigurationEditorProviderFactory() {
        initialise();
    }
    
    public static ConfigurationEditorProviderFactory getDefault() {
        return factory;
    }
    

    private void checkUpdates() {
        if (hasChanged == State.CHANGE_REQUIRED) {
            lookupProviders();
        }
    }
    
    public ExtensibilityElementConfigurationEditorProvider getConfigurationProvider(String namespace) {
        checkUpdates();
        if (namespace != null) {
            return map.get(namespace);
        }
        return null;
    }
    
    
    
    private void initialise() {
        Result<ExtensibilityElementConfigurationEditorProvider> lookupResult = Lookup.getDefault().lookupResult(ExtensibilityElementConfigurationEditorProvider.class);
        lookupResult.addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                hasChanged = State.CHANGE_REQUIRED;
            }

        });
        lookupProviders();
    }
    
    private synchronized void lookupProviders() {
        if (hasChanged == State.CHANGE_COMPLETED) {
            return;
        }

        hasChanged = State.CHANGING;

        map.clear();


        for (ExtensibilityElementConfigurationEditorProvider provider : Lookup.getDefault().lookupAll(ExtensibilityElementConfigurationEditorProvider.class)) {
            map.put(provider.getNamespace(), provider);
        }

        if (hasChanged == State.CHANGING) {
            hasChanged = State.CHANGE_COMPLETED;
        }

    }
}
