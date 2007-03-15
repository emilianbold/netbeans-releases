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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLChangeExecutor  {
    
      
    /** Creates a new instance of WSDLChangeExecutor */
    public WSDLChangeExecutor() {
    }

      
     public boolean canChange(Referenceable target, AbstractRefactoring request){
        return (target instanceof WSDLComponent || target instanceof WSDLModel) && 
                (request instanceof RenameRefactoring ||
                 request instanceof SafeDeleteRefactoring);
    }

       
    public void doChange(Referenceable target, AbstractRefactoring request) throws IOException {
        if (target instanceof Nameable  && request instanceof RenameRefactoring) {
            SharedUtils.renameTarget((Nameable) target, ((RenameRefactoring)request).getNewName());
        } else if (target instanceof NamedReferenceable && request instanceof SafeDeleteRefactoring) {
            SharedUtils.deleteTarget((NamedReferenceable) target);
        } else if(target instanceof Model) {
            //just do nothing
        }
    }
        
}
