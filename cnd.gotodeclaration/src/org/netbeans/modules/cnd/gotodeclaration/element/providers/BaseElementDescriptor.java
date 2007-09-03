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
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.util.EmptyIcon;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Acommon base class for functions, variables, macros, etc. descriptors
 * Based on CsmOffsetable
 * 
 * @author Vladimir Kvashin
 */
public abstract class BaseElementDescriptor implements ElementDescriptor {

    private static Icon projectIcon = new ImageIcon(Utilities.loadImage(CsmImageLoader.PROJECT));
    private static Icon emptyIcon = new EmptyIcon(16);
    
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
	return (element == null) ? null : element.getContainingFile().getAbsolutePath();
    }

    protected CsmProject getProject() {
        CsmOffsetable element = getElement();
	return (element == null) ? null : element.getContainingFile().getProject();
    }
    
    public Icon getProjectIcon() {
	CsmProject project = getProject();
	return project.isArtificial() ? emptyIcon : projectIcon;
    }

    public String getProjectName() {
	CsmProject project = getProject();
	return (project == null) ? "" : project.getName();
    }

    public void open() {
	CsmUtilities.openSource(getElement());
    }    
}
