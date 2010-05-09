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

package org.netbeans.modules.xml.xpath.ext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.ErrorManager;

/**
 * Represents a node test on name.
 * 
 * @author Enrico Lelina
 * @version 
 */
public class StepNodeNameTest implements StepNodeTest {

    public static String ASTERISK = "*";

    /** The node name. */
    private QName mNodeName;
    
    /**
     * Constructor.
     * @param nodeName the node name
     */
    public StepNodeNameTest(QName nodeName) {
        super();
        mNodeName = nodeName;
    }

    public StepNodeNameTest(NamespaceContext nsContext, SchemaComponent sComp,
            SchemaModelsStack sms) {
        super();
        assert (sComp instanceof Named);
        String componentName = ((Named)sComp).getName();
        QName sCompQName = null;
        //
        if (XPathUtils.isPrefixRequired(sComp)) {
            //
            String nsPrefix = null;
            String namespaceURI = null;
            if (sms == null) {
                namespaceURI = sComp.getModel().getEffectiveNamespace(sComp);
            } else {
                namespaceURI = SchemaModelsStack.getEffectiveNamespace(sComp, sms);
            }
            assert namespaceURI != null;
            //
            if (nsContext != null) {
                nsPrefix = nsContext.getPrefix(namespaceURI);
                //
                if (nsPrefix == null) {
                    if (nsContext instanceof ExNamespaceContext) {
                        try {
                            nsPrefix = ((ExNamespaceContext) nsContext).
                                    addNamespace(namespaceURI);
                        } catch (InvalidNamespaceException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }
                //
                // Log a warning if prefix still not accessible 
                if (nsPrefix == null) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, 
                            "A prefix has to be declared for the namespace " +
                            "\"" + namespaceURI + "\""); // NOI18N
                }
            }
            //
            if (nsPrefix == null || nsPrefix.length() == 0) {
                sCompQName = new QName(componentName);
            } else {
                sCompQName = new QName(namespaceURI, componentName, nsPrefix);
            }
        } else {
            sCompQName = new QName(componentName);
        }
        mNodeName = sCompQName;
    }
    
    public StepNodeNameTest(NamespaceContext nsContext, XPathPseudoComp pseudo,
            SchemaModelsStack sms) {
        super();
        String componentName = pseudo.getName();
        QName sCompQName = null;
        //
        String namespaceURI = pseudo.getNamespace();
        boolean prefixRequired = namespaceURI != null && namespaceURI.length() != 0;
        //
        if (prefixRequired) {
            //
            String nsPrefix = null;
            //
            if (nsContext != null) {
                nsPrefix = nsContext.getPrefix(namespaceURI);
                //
                if (nsPrefix == null) {
                    if (nsContext instanceof ExNamespaceContext) {
                        try {
                            nsPrefix = ((ExNamespaceContext) nsContext).
                                    addNamespace(namespaceURI);
                        } catch (InvalidNamespaceException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }
                //
                // Log a warning if prefix still not accessible 
                if (nsPrefix == null) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, 
                            "A prefix has to be declared for the namespace " +
                            "\"" + namespaceURI + "\""); // NOI18N
                }
            }
            //
            if (nsPrefix == null || nsPrefix.length() == 0) {
                sCompQName = new QName(componentName);
            } else {
                sCompQName = new QName(namespaceURI, componentName, nsPrefix);
            }
        } else {
            sCompQName = new QName(componentName);
        }
        mNodeName = sCompQName;
    }
    
    public StepNodeNameTest(NamespaceContext nsContext, SchemaCompHolder sCompHolder,
            SchemaModelsStack sms) {
        super();
        String componentName = sCompHolder.getName();
        QName sCompQName = null;
        //
        String namespaceURI = sCompHolder.getNamespace(sms);
        //
        if (sCompHolder.isPrefixRequired()) {
            //
            String nsPrefix = null;
            //
            if (nsContext != null) {
                nsPrefix = nsContext.getPrefix(namespaceURI);
                //
                if (nsPrefix == null) {
                    if (nsContext instanceof ExNamespaceContext) {
                        try {
                            nsPrefix = ((ExNamespaceContext) nsContext).
                                    addNamespace(namespaceURI);
                        } catch (InvalidNamespaceException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }
                //
                // Log a warning if prefix still not accessible 
                if (nsPrefix == null) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, 
                            "A prefix has to be declared for the namespace " +
                            "\"" + namespaceURI + "\""); // NOI18N
                }
            }
            //
            if (nsPrefix == null || nsPrefix.length() == 0) {
                sCompQName = new QName(componentName);
            } else {
                sCompQName = new QName(namespaceURI, componentName, nsPrefix);
            }
        } else {
            sCompQName = new QName(componentName);
        }
        mNodeName = sCompQName;
    }
    
    /**
     * Gets the node name.
     * @return the node name
     */
    public QName getNodeName() {
        return mNodeName;
    }
    
    public boolean isWildcard() {
        String lName = mNodeName.getLocalPart();
        return ASTERISK.equals(lName); // NOI18N
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StepNodeNameTest) {
            QName nodeName2 = ((StepNodeNameTest)obj).getNodeName();
            return XPathUtils.equalsIgnorNsUri(nodeName2, mNodeName);
        }
        return false;
    }

    @Override
    public String toString() {
        return getExpressionString();
    }

    public String getExpressionString() {
        return XPathUtils.qNameObjectToString(mNodeName);
    }
}
