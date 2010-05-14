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
package org.netbeans.modules.sun.manager.jbi.nodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jqian
 */
public abstract class JBIArtifactValidator {
    
    private static ComponentValidator serviceEngineValidator;
    private static ComponentValidator bindingComponentValidator;
    private static SharedLibraryValidator sharedLibraryValidator;
    private static ServiceAssemblyValidator serviceAssemblyValidator;
        
    public static JBIArtifactValidator getServiceEngineValidator(String name) {
        if (serviceEngineValidator == null) {
            serviceEngineValidator = new ServiceEngineValidator();
        }
        serviceEngineValidator.setComponentName(name);
        
        return serviceEngineValidator;
    }
    
    public static JBIArtifactValidator getBindingComponentValidator(String name) {
        if (bindingComponentValidator == null) {
            bindingComponentValidator = new BindingComponentValidator();
        }
        bindingComponentValidator.setComponentName(name);
        
        return bindingComponentValidator;
    }
    
    public static JBIArtifactValidator getSharedLibraryValidator() {
        if (sharedLibraryValidator == null) {
            sharedLibraryValidator = new SharedLibraryValidator();
        }
        
        return sharedLibraryValidator;
    }
    
    public static JBIArtifactValidator getServiceAssemblyValidator() {
        if (serviceAssemblyValidator == null) {
            serviceAssemblyValidator = new ServiceAssemblyValidator();
        }
        
        return serviceAssemblyValidator;
    }

    public boolean validate(File zipFile) {
        boolean isRightType = false;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }

        if (docBuilder != null) {
            JarFile jf = null;
            try {
                jf = new JarFile(zipFile);
                JarEntry je = (JarEntry) jf.getEntry("META-INF/jbi.xml"); // NOI18N
                if (je != null) {
                    InputStream is = jf.getInputStream(je);
                    Document doc = docBuilder.parse(is);
                    isRightType = validate(doc); // very basic type checking
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (jf != null) {
                    try {
                        jf.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return isRightType;
    }

    protected abstract boolean validate(Document jbiDoc);
    
    
    //==========================================================================

    private static class ComponentValidator extends JBIArtifactValidator {

        private String componentType;
        private String componentName;
        
        ComponentValidator(String componentType) {
            this.componentType = componentType;
        }
        
        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        protected boolean validate(Document jbiDoc) {
            NodeList ns = jbiDoc.getElementsByTagName("component"); // NOI18N
            if (ns.getLength() > 0) {
                Element e = (Element) ns.item(0);
                String type = e.getAttribute("type"); // NOI18N
                if (type != null && type.equals(componentType)) {
                    if (componentName == null) {
                        return true;
                    } else {
                        String name = null;
                        NodeList ids = e.getElementsByTagName("identification"); // NOI18N
                        if (ids.getLength() > 0) {
                            Element id = (Element) ids.item(0);
                            NodeList names = id.getElementsByTagName("name"); // NOI18N
                            if (names.getLength() > 0) {
                                Element n = (Element) names.item(0);
                                name = n.getFirstChild().getNodeValue();
                            }
                        }
                        if (componentName.equals(name)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
    
    private static class ServiceEngineValidator extends ComponentValidator {
        ServiceEngineValidator() {
            super("service-engine"); // NOI18N
        }
    }

    private static class BindingComponentValidator extends ComponentValidator {
        BindingComponentValidator() {
            super("binding-component"); // NOI18N
        }
    }

    private static class SharedLibraryValidator extends JBIArtifactValidator {
        
        protected boolean validate(Document jbiDoc) {
            NodeList ns = jbiDoc.getElementsByTagName("shared-library"); // NOI18N
            return ns.getLength() > 0;
        }
    }
    
    private static class ServiceAssemblyValidator extends JBIArtifactValidator {
                
        protected boolean validate(Document jbiDoc) {
            NodeList ns = jbiDoc.getElementsByTagName("service-assembly"); // NOI18N
            return ns.getLength() == 1;
        }
    }
}