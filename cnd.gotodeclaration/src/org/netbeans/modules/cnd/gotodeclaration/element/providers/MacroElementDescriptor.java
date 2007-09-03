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

import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;

import org.netbeans.modules.cnd.modelutil.CsmImageLoader;


/**
 * An ElementDescriptor for macros (CsmMacro)
 * @author Vladimir Kvashin
 */

/* package */ 
class MacroElementDescriptor extends BaseElementDescriptor implements ElementDescriptor {

    private final CsmMacro macro;
    private String displayName = null;
    private String contextName = null;
    private static Icon icon;
    
    MacroElementDescriptor(CsmMacro macro) {
	this.macro = macro;
	List<String> params = macro.getParameters();
	if( params == null || params.size() == 0 ) {
            displayName = macro.getName();
	}
	else {
	    StringBuilder sb = new StringBuilder(macro.getName());
	    sb.append('(');
	    for (int i = 0; i < params.size(); i++) {
		if( i > 0 ) {
		    sb.append(',');
		}
		sb.append(params.get(i));
	    }
	    sb.append(')');
	    displayName = sb.toString();
	}
	contextName = macro.getContainingFile().getName();
	if( icon == null ) {
            icon = new ImageIcon(CsmImageLoader.getImage(macro));
	}
    }
    
    protected CsmOffsetable getElement() {
	return macro;
    }

    protected String getContextNameImpl() {
	return contextName;
    }

    public String getDisplayName() {
	return displayName;
    }

    public Icon getIcon() {
	return icon;
    }

    public String getSortName() {
        return getDisplayName();
    }


}
