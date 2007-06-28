
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Util.java
 *
 * Created on March 13, 2007, 4:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.util;

import java.util.Collection;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
public class WSDLUtils {
    
    /** Creates a new instance of Util */
    private WSDLUtils() {
    }
    
    public static WSDLModel getWSDLModel(FileObject wsdlFile, boolean editable){
        ModelSource ms = Utilities.getModelSource(wsdlFile, editable);
        return WSDLModelFactory.getDefault().getModel(ms);
    }
    
    public static boolean isDocumentOriented(WSDLModel wsdlModel){
        Definitions definitions = wsdlModel.getDefinitions();
        Collection<Message> messages = definitions.getMessages();
        if(messages.size() > 0){
            Message firstMessage = messages.iterator().next();
            Collection<Part> parts = firstMessage.getParts();
            if(parts.size() > 0){
                Part firstPart = parts.iterator().next();
                if(firstPart.getType() != null){
                    return false;
                }
            }
        }
        return true;
    }
}
