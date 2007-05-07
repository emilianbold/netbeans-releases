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

package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac, Martin Matula
 */
public class WSITSafeDeleteRefactoringPlugin extends WSITRefactoringPlugin<SafeDeleteRefactoring> {
    public WSITSafeDeleteRefactoringPlugin(SafeDeleteRefactoring refactoring) {
        super(refactoring);
    }

    protected RefactoringElementImplementation createMethodRE(String methodName, WSDLModel model) {
        return new MethodRE(methodName, model);
    }

    protected RefactoringElementImplementation createClassRE(WSDLModel model) {
        return new ClassRE(model);
    }
    
    private static class ClassRE extends AbstractRefactoringElement {
        private BackupFacility.Handle id;

        public ClassRE(WSDLModel model) {
            super(model);
        }
        
        public void performChange() {
            FileObject parentFile = getParentFile();
            try {
                id = BackupFacility.getDefault().backup(parentFile);
                parentFile.delete();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        public void undoChange() {
            try {
                id.restore();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt()};
            return MessageFormat.format(NbBundle.getMessage(ClassRE.class, "TXT_WsitXmlClassSafeDelete"), args);
        }
    }
    
   /**
     * Rename refactoring element for wsit-*.xml
     */
    private static class MethodRE extends AbstractRefactoringElement {
        private final String methodName;
        
        public MethodRE(String methodName, WSDLModel model) {
            super(model);
            this.methodName = methodName;
        }
        
        public void performChange() {
            Definitions d = model.getDefinitions();
            Binding b = (Binding) d.getBindings().toArray()[0];
            Collection<Message> messages = d.getMessages();
            Collection<BindingOperation> bOperations = b.getBindingOperations();
            PortType portType = (PortType) d.getPortTypes().toArray()[0];
            Collection<Operation> operations = portType.getOperations();
            model.startTransaction();

            for (BindingOperation bOperation : bOperations) {
                if (methodName.equals(bOperation.getName())) {
                    b.removeBindingOperation(bOperation);
                }
            }
            
            for (Operation o : operations) {
                if (methodName.equals(o.getName())) {
                    portType.removeOperation(o);
                }
            }

            for (Message m : messages) {
                if (methodName.equals(m.getName()) || (methodName + "Response").equals(m.getName())) {
                    d.removeMessage(m);
                }
            }

            model.endTransaction();
        }

        public void undoChange() { 
            // [TODO] implement me
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {methodName, getParentFile().getNameExt()};
            return MessageFormat.format(NbBundle.getMessage(MethodRE.class, "TXT_WsitXmlMethodSafeDelete"), args);
        }
    }
    
}
