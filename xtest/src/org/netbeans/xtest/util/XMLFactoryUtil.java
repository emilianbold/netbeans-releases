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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * XMLFactoryUtil.java
 *
 * Created on October 12, 2001, 5:08 PM
 */

package org.netbeans.xtest.util;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;

/**
 *
 * @author  mb115822
 */
public class XMLFactoryUtil {
    
    private static final String[] names = new String[] {};
        //{"javax.xml.parsers.DocumentBuilderFactory",
         //"javax.xml.parsers.SAXParserFactory", 
         //"org.apache.xerces.xni.parser.XMLParserConfiguration",
         //"org.xml.sax.driver",
         //"javax.xml.transform.TransformerFactory"};
    private static final String[] values = new String[] {};
        //{"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl",
         //"org.apache.xerces.jaxp.SAXParserFactoryImpl",
         //"org.apache.xerces.parsers.StandardParserConfiguration",
         //"org.apache.xerces.parsers.SAXParser",
         //"org.apache.xalan.processor.TransformerFactoryImpl"};
    
    private static String[] setNewProperties() {
        
        String oldValues[]=new String[names.length];
        for (int i=0; i<names.length; i++) 
            oldValues[i]=System.setProperty(names[i], values[i]);
         
        return oldValues;
         
    }
    
    private static void setOriginalProperties(String[] oldValues) {
        for (int i=0; i<names.length; i++)
            if (oldValues[i]==null)
                System.getProperties().remove(names[i]);
            else
                System.setProperty(names[i], oldValues[i]);
        
    }

    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        String[] oldProperties = setNewProperties();
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } finally {
            setOriginalProperties(oldProperties);
        }
    }
        
    public static Transformer newTransformer() throws TransformerConfigurationException {
        String[] oldProperties = setNewProperties();
        try {
            return  (TransformerFactory.newInstance()).newTransformer();
        } finally {
            setOriginalProperties(oldProperties);
        }
    }

    public static Transformer newTransformer(StreamSource xsltSource) throws TransformerConfigurationException {
        String[] oldProperties = setNewProperties();
        try {
            return  (TransformerFactory.newInstance()).newTransformer(xsltSource);
        } finally {
            setOriginalProperties(oldProperties);
        }
    }
    
}
