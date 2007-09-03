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

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcher;
import org.openide.util.NbBundle;

/**
 * 
 * @author Vladimir Kvashin
 */
public class MacroProvider extends BaseProvider implements ElementProvider {

    
    public String name() {
	return "C/C++ Macros"; // NOI18N
    }

    public String getDisplayName() {
	return NbBundle.getMessage(MacroProvider.class, "MACRO_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    protected void processProject(CsmProject project, List<ElementDescriptor> result, NameMatcher comparator) {
	if( TRACE ) System.err.printf("MacroProvider.processProject %s\n", project.getName());
        processFiles(project.getAllFiles(), result, comparator);
    }
    
    private void processFiles(Collection<CsmFile> files, List<ElementDescriptor> result, NameMatcher comparator) {
	for( CsmFile file : files ) {
            if( isCancelled() ) {
                return;
            }
	    for( CsmMacro macro : file.getMacros() ) {
                if( isCancelled() ) {
                    return;
                }
		if( comparator.matches(macro.getName()) ) {
		    result.add(new MacroElementDescriptor(macro));
		}
	    }
	}
    }

    
}
