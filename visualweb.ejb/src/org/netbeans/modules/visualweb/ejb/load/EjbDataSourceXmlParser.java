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
 * EjbDataSourceXmlParser.java
 *
 * Created on May 2, 2004, 2:45 AM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodReturn;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.ErrorManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author  cao
 */
public class EjbDataSourceXmlParser extends DefaultHandler
{
    private String xmlFileName;

    private EjbGroup curEjbGroup;
    private MethodInfo curMethodInfo;
    private MethodParam curMethodParam;
    private EjbInfo curEjbInfo;
    private String currentTag;
    private String data;

    private Collection allEjbGroups;
    
    private static String bundledPeRmiIiopPort;
    private static File sampleEjbDir;
    static {
        bundledPeRmiIiopPort = getBundledPeRmiIiopPort();
        sampleEjbDir = InstalledFileLocator.getDefault().locate("samples/ejb", null, false ); // NOI18N
    }
    
    /** Creates a new instance of EjbDataSourceXmlParser */
    public EjbDataSourceXmlParser(String xmlFile)
    {
        xmlFileName = xmlFile;
        allEjbGroups = new HashSet();
    }

    // Return a Collection of EjbGroups parsed from the given xml file
    public Collection parse() throws EjbLoadException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware( true );
            factory.setValidating( false );
            SAXParser parser = factory.newSAXParser();
            File file = new File(xmlFileName);
            parser.parse( file, this );
        }
        catch( java.io.IOException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the standard deployment descriptor file. Cannot read file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();

            // Throw up as SYSTEM_ERROR
            throw new EjbLoadException( e.getMessage() );
        }
        catch( ParserConfigurationException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the ejb data source file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();

            // Throw up as SYSTEM_ERROR
            throw new EjbLoadException( e.getMessage() );
        }
        catch( SAXException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the ejb data source file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();

            // Throw up as SYSTEM_ERROR
            throw new EjbLoadException( e.getMessage() );
        }

        return allEjbGroups;
    }

    public void startElement( String uri, String localName, String qName,
                              Attributes attributes ) throws SAXException
    {
        currentTag = qName;

        if( currentTag.equalsIgnoreCase( XmlTagConstants.EJB_GROUP_TAG ) )
            curEjbGroup = new EjbGroup();
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.STATELESS_SESSION_TAG ) )
        {
            curEjbInfo = new EjbInfo();
            curEjbInfo.setBeanType( EjbInfo.STATELESS_SESSION_BEAN );
        }
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.STATEFUL_SESSION_TAG ) )
        {
            curEjbInfo = new EjbInfo();
            curEjbInfo.setBeanType( EjbInfo.STATEFUL_SESSION_BEAN );
        }
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.METHOD_TAG ) )
        {
            curMethodInfo = new MethodInfo();
            curMethodInfo.setIsBusinessMethod( true );
        }
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.CREATE_METHOD_TAG ) )
        {
            curMethodInfo = new MethodInfo();
            curMethodInfo.setIsBusinessMethod( false );
        }
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.RETURN_TYPE_TAG ) )
        {
            // Get the element class type 
            String elemType = (String)attributes.getValue( XmlTagConstants.RETURN_ELEM_TYPE_ATTR );
            String isReturnCol = (String)attributes.getValue( XmlTagConstants.RETURN_IS_COLLECTION_ATTR );
            
            MethodReturn mr = new MethodReturn();
            
            if( isReturnCol != null )
                mr.setIsCollection( new Boolean(isReturnCol).booleanValue() );
            
            mr.setElemClassName( elemType );
            
            curMethodInfo.setReturnType( mr );
        }
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.PARAMETER_TAG ) )
        {
            curMethodParam = new MethodParam();
            
            // Parameter name is from the "name" attribute
            String paramName = (String) attributes.getValue( XmlTagConstants.PARAM_NAME_ATTR );
            
            if( paramName == null )
            {
                // Not specified. Default to arg#
                int numParams = 0;
                if( curMethodInfo.getParameters() != null )
                    numParams = curMethodInfo.getParameters().size() - 1;
                
                paramName = "arg" + numParams; // NOI18N
            }
            
            curMethodParam.setName( paramName );
        }
    }

    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if( currentTag != null )
        {
            // Set the data to the ejb group
            setData();
        }
        
        if( qName.equals( XmlTagConstants.STATELESS_SESSION_TAG ) ||
            qName.equals( XmlTagConstants.STATEFUL_SESSION_TAG ) )
        {
            curEjbGroup.addSessionBean( curEjbInfo );
        }
        else if( qName.equalsIgnoreCase( XmlTagConstants.METHOD_TAG ) ||
                 qName.equalsIgnoreCase( XmlTagConstants.CREATE_METHOD_TAG ) )
        {
            curEjbInfo.addMethod( curMethodInfo );
        }
        else if( qName.equalsIgnoreCase( XmlTagConstants.PARAMETER_TAG )  )
        {
            curMethodInfo.addParameter( curMethodParam );
        }
        else if ( qName.equalsIgnoreCase( XmlTagConstants.EJB_GROUP_TAG ) )
        {
            // End of this ejb group
            allEjbGroups.add( curEjbGroup );
            curEjbGroup = null;
        }

        currentTag = null;
        data = null;
    }

    public void characters(char buf [], int offset, int len)
        throws SAXException
    {
        if( data == null )
            data = new String(buf, offset, len);
        else
            data = data + new String(buf, offset, len);
    }

    private void setData()
    {
        // Trim the data first
        if( data != null )
            data = data.trim();
        
        if( currentTag.equalsIgnoreCase( XmlTagConstants.GROUP_NAME_TAG ) )
            curEjbGroup.setName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.CONTAINER_VENDOR_TAG ) )
            curEjbGroup.setAppServerVendor( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.SERVER_HOST_TAG ) )
            curEjbGroup.setServerHost( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.IIOP_PORT_TAG ) )
            curEjbGroup.setIIOPPort( resolveIiopPort( data ) );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.CONTAINER_VENDOR_TAG ) )
            curEjbGroup.setAppServerVendor( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.CLIENT_JAR_TAG ) )
            curEjbGroup.addClientJarFile( resolveJarFileName(data) );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.BEAN_WRAPPER_JAR_TAG ) )
            curEjbGroup.setClientWrapperBeanJar( resolveJarFileName(data) );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.DESIGN_TIME_TAG ) )
            curEjbGroup.setDesignInfoJar( resolveJarFileName(data) );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.ADDITIONAL_DD_JAR_TAG ) )
            curEjbGroup.setDDLocationFile( resolveJarFileName(data) );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.JNDI_NAME_TAG ) )
            curEjbInfo.setJNDIName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.EJB_NAME_TAG ) )
            curEjbInfo.setEjbName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.HOME_TAG_TAG ) )
            curEjbInfo.setHomeInterfaceName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.REMOTE_TAG ) )
            curEjbInfo.setCompInterfaceName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.WEB_EJB_REF_TAG ) )
            curEjbInfo.setWebEjbRef( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.WRAPPER_BEAN_TAG ) )
            curEjbInfo.setBeanWrapperName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.WRAPPER_BEAN_INFO_TAG ) )
            curEjbInfo.setBeanInfoWrapperName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.METHOD_NAME_TAG ) )
            curMethodInfo.setName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.RETURN_TYPE_TAG ) )
            curMethodInfo.getReturnType().setClassName( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.PARAMETER_TAG ) )
            curMethodParam.setType( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.EXCEPTION_TAG ) )
            curMethodInfo.addException( data );
        else if( currentTag.equalsIgnoreCase( XmlTagConstants.DATAPROVIDER_TAG ) )
            curMethodInfo.setDataProvider( data );
    }
    
    private int resolveIiopPort( String port ) 
    {
        int iiopPort = 23700;
        
        if( port.indexOf( "{bundle_pe_iiop_port}" ) != -1 )
        {
            String bundleiiopPort = getBundledPeRmiIiopPort();
            
            if( bundleiiopPort != null )
                return Integer.parseInt( bundleiiopPort );
        }
        else
            iiopPort = Integer.parseInt( port );
        
        return iiopPort;
    }
    
    private static String getBundledPeRmiIiopPort() {
        /**
         * Get the installer properties to find out what port PE was installed on.
         */
        File installPropertiesFile = InstalledFileLocator.getDefault().locate(
                "config/com-sun-rave-install.properties", null, true);
        
        try {
            /**
             * If we couldn't find the installer properties, don't try to get
             * the port from them.
             */
            if( installPropertiesFile != null || installPropertiesFile.exists() ) {
                Properties installProps = new Properties();
                installProps.load(new FileInputStream(installPropertiesFile));
                return installProps.getProperty("iiop1Port");
            }

            return null;
        } catch( Exception e ) {
            // No install.properties found. It is OK. Just return null so that default IIOP port will be used
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbDataSourceXmlParser" ).log( ErrorManager.INFORMATIONAL, 
                      "Error accessing com-sun-rave-install.properties" );
            return null;
        }
    }

    private String resolveJarFileName( String fileName )
    {
        // If there is a {tools.home} in the file name, resolove it to
        // the real directory location
        if( fileName.indexOf( "{samples/ejb}" ) != -1 ) // NOI18N
        {
            fileName = sampleEjbDir.getAbsolutePath() + fileName.substring(  fileName.indexOf('}') + 1 );
        }

        // Use the OS file separator
        fileName = fileName.replace( '/', File.separatorChar );
        fileName = fileName.replace( '\\', File.separatorChar );

        return fileName;
    }
}
