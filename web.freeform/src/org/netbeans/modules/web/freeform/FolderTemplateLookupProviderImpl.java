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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.freeform;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Special lookup provider to provide 'Folder' template as the last item in New
 * context menu of Web Freeform project
 * 
 * @author Milan Kubec
 */
public class FolderTemplateLookupProviderImpl implements LookupProvider {

    public FolderTemplateLookupProviderImpl() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        
        Lookup retVal = Lookup.EMPTY;
        
        assert baseContext.lookup(Project.class) != null;
        AuxiliaryConfiguration aux = baseContext.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        
        if (isWebFFProject(aux)) {
            retVal = Lookups.fixed(new Object[] {
                new PrivilegedTemplatesImpl(),
            });
        }
        
        return retVal;
        
    }
    
    private boolean isWebFFProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true) != null // NOI18N
               || aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true) != null; // NOI18N
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Other/Folder",
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
}
