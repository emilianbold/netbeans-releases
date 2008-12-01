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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import javax.swing.Icon;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;

/**
 * Acommon base class for functions, variables, macros, etc. descriptors
 * Based on CsmOffsetable
 * 
 * @author Vladimir Kvashin
 */
public abstract class BaseElementDescriptor implements ElementDescriptor {

    private static final String contextNameFormat = ' ' + NbBundle.getMessage(BaseProvider.class, "CONTEXT_NAME_FORMAT");
    
    protected abstract CsmOffsetable getElement();
    protected abstract String getContextNameImpl();

    public String getContextName() {
	String contextName = getContextNameImpl();
	if( contextName != null && contextName.length() > 0 ) {
            contextName = String.format(contextNameFormat, contextName);
	}
	return contextName;
    }
    
    

    public String getAbsoluteFileName() {
	CsmOffsetable element = getElement();
	return (element == null) ? null : element.getContainingFile().getAbsolutePath().toString();
    }

    protected CsmProject getProject() {
        CsmOffsetable element = getElement();
	return (element == null) ? null : element.getContainingFile().getProject();
    }
    
    public Icon getProjectIcon() {
        return CsmImageLoader.getIcon(getProject());
    }

    public String getProjectName() {
	CsmProject project = getProject();
	return (project == null) ? "" : project.getName().toString();
    }

    public void open() {
	CsmUtilities.openSource(getElement());
    }    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return equals(this.getElement(), ((BaseElementDescriptor) obj).getElement());
    }
    
    private static boolean equals(CsmOffsetable element1, CsmOffsetable element2) {
        if (element1 == element2) {
            return true;
        } else if (element1 == null || element2 == null) {
            return false;
        } else {
            CsmFile file1 = element1.getContainingFile();
            CsmFile file2 = element2.getContainingFile();
            if( file1 == null || file2 == null ) {
                return element1 == element2;
            }
            else if( ! file1.getAbsolutePath().equals(file2.getAbsolutePath()) ) {
                return false;
            }
            else {
                return element1.getStartOffset() == element2.getStartOffset();
            }
        }
    }

    @Override
    public int hashCode() {
	CsmOffsetable element = getElement();
        if( element == null ) {
            return super.hashCode();
        }
        else {
            int hash = 5;
            String path = element.getContainingFile().getAbsolutePath().toString();
            hash = 19 * hash + ((path == null) ? 0 : path.hashCode());
            hash = 19 * hash + element.getStartOffset();
            return hash;
        }
    }
    
}
