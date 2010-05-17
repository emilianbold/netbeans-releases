/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * WebsphereDeploymentDescriptorParser.java
 *
 * Created on August 3, 2004, 4:24 PM
 */

package org.netbeans.modules.visualweb.ejb.load;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * To parse the websphere specific deployment descriptor - ibm-ejb-jar-bnd.xmi
 * @author  cao
 */
public class WebsphereDeploymentDescriptorParser extends DefaultHandler {
    
    private static final String EJB_BINDINGS_TAG = "ejbBindings";
    private static final String ENTERPRISE_BEAN_TAG = "enterpriseBean";
    private static final String JNDI_NAME_ATTR = "jndiName";
    private static final String HREF_ATTR = "href";
    
    private String xmlFileName;
    
    private String jndiName;
    private String beanId;
    
    private Map nameMapping = new HashMap();
    

    public WebsphereDeploymentDescriptorParser( String vendorXml ) {
        this.xmlFileName = vendorXml;
    }
    
    /**
     * @return a map of (bean id, jndi name ) pairs
     */
    public Map parse() throws EjbLoadException
    {
         try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware( true );
            factory.setValidating( false );
            SAXParser parser = factory.newSAXParser();
            parser.parse( xmlFileName, this );

            return nameMapping;
        }
        catch( java.io.IOException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the vendor EJB deployment descriptor. Cannot read file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.WebsphereDeploymentDescriptorParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();
            
            // Throw up as SYSTEM_ERROR. Should never happen
            throw new EjbLoadException( e.getMessage() );
        }
        catch( ParserConfigurationException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the vendor EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.WebsphereDeploymentDescriptorParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();
            
            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_VENDOR_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
        catch( SAXException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the vendor EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.WebsphereDeploymentDescriptorParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();
            
            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_VENDOR_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
    }
    
      public void startElement( String uri, String localName, String qName,
                              Attributes attributes ) throws SAXException 
    { 
        if( qName.equalsIgnoreCase( EJB_BINDINGS_TAG ) )
        {
            jndiName = attributes.getValue( JNDI_NAME_ATTR );
        }
        
        if( qName.equalsIgnoreCase( ENTERPRISE_BEAN_TAG ) )
        {
            String href = attributes.getValue( HREF_ATTR );
            
            // The id will be something like "META-INF/ejb-jar.xml#Greeter"
            // We only need the one after the #
            
            beanId = href.substring( href.indexOf( '#' ) + 1 );
        }
    }
    
      public void endElement( String uri, String localName, String qName )
                    throws SAXException 
      {
          if( qName.equalsIgnoreCase( EJB_BINDINGS_TAG ) ) 
          {
              nameMapping.put( beanId, jndiName );
              beanId = null;
              jndiName = null;
          }
      }
    
    public void characters(char buf [], int offset, int len)
        throws SAXException 
    { 
    }
    
    public InputSource resolveEntity( String publicId, String systemId )
    {
        // Ignore any external entities
        return new InputSource(new StringReader(""));
    }
}

