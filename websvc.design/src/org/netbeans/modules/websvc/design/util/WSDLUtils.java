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
