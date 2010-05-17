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
 * EjbJarXmlParser.java
 *
 * Created on April 28, 2004, 11:04 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is used to parse the standard deployment descriptor - ejb-jar.xml.
 * The class only extracts out the session beans. It can be enhanced to extract
 * MDBs and entity beans later on as needed.
 */
public class StdDeploymentDescriptorParser extends DefaultHandler
{
    private static final String HOME_TAG = "home";
    private static final String REMOTE_TAG = "remote";
    private static final String EJB_NAME_TAG = "ejb-name";
    private static final String SESSION_TAG = "session";
    private static final String EJB_SESSION_TYPE_TAG = "session-type";
    private static final String BEAN_ID_ATTR = "id";

    private String xmlFileName;

    private String beanId;
    private String homeName;
    private String remoteName;
    private String ejbName;
    private String sessionType;

    private String currentTag;
    private String data;

    private Collection sessionBeans = new ArrayList();

    // To remember to skipped non-package EJBs
    private Set skippedEjbs = new HashSet();

    /** Creates a new instance of EjbJarXmlParser */
    public StdDeploymentDescriptorParser(String ejbJarXml) {
        this.xmlFileName = ejbJarXml;
    }

    public Collection parse() throws EjbLoadException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware( true );
            factory.setValidating( false );
            SAXParser parser = factory.newSAXParser();
            parser.parse( xmlFileName, this );

            return sessionBeans;
        }
        catch( java.io.IOException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the standard EJB deployment descriptor. Cannot read file " + xmlFileName;
            // TODO - EjbJarXmlParser is not a valid class name
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbJarXmlParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            // Throw up as SYSTEM_ERROR. Should never happen
            throw new EjbLoadException( e.getMessage() );
        }
        catch( ParserConfigurationException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the standard EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbJarXmlParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_STD_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
        catch( SAXException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the standard EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbJarXmlParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_STD_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
    }

    public void startElement( String uri, String localName, String qName,
                              Attributes attributes ) throws SAXException
    {
        currentTag = qName;

        if( qName.equalsIgnoreCase( SESSION_TAG ) )
            beanId = attributes.getValue( BEAN_ID_ATTR );
    }

    public void endElement( String uri, String localName, String qName )
        throws SAXException {

        // Set data
        if( currentTag != null )
            setData();

        if( qName.equals( SESSION_TAG ))
        {
            if( homeName != null && remoteName != null && ejbName != null && sessionType != null )
            {
                EjbInfo ejbInfo = new EjbInfo();

                ejbInfo.setHomeInterfaceName( homeName );
                ejbInfo.setCompInterfaceName( remoteName );
                ejbInfo.setEjbName( ejbName );
                ejbInfo.setBeanId( beanId );

                if( sessionType.equalsIgnoreCase( "Stateless" ) )
                    ejbInfo.setBeanType( EjbInfo.STATELESS_SESSION_BEAN );
                else
                    ejbInfo.setBeanType( EjbInfo.STATEFUL_SESSION_BEAN );

                // If either the home or the remote interface of the
                // ejb does not have a package, then the ejb will be
                // skipped with a log message
                if( hasPackage( ejbInfo ) )
                    sessionBeans.add( ejbInfo );
                else
                {
                    // EJB has been skipped because there is no package defined for its home or/and remote interface: {0}
                    ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load").log( ErrorManager.WARNING, "EJB has been skipped because there is no package defined for its home or/and remote interface: " + ejbInfo.getCompInterfaceName() );
                    skippedEjbs.add( ejbInfo.getCompInterfaceName() );
                }

                homeName = null;
                remoteName = null;
                ejbName = null;
            }
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
        if( data != null )
            data = data.trim();

        if( currentTag.equalsIgnoreCase( HOME_TAG ) )
            homeName = data;
        else if( currentTag.equalsIgnoreCase( REMOTE_TAG ) )
            remoteName = data;
        else if( currentTag.equalsIgnoreCase( EJB_NAME_TAG ) )
            ejbName = data;
        else if( currentTag.equalsIgnoreCase( EJB_SESSION_TYPE_TAG ) )
            sessionType = data;
    }

    public InputSource resolveEntity( String publicId, String systemId )
    {
        // Ignore any external entities
        return new InputSource(new StringReader(""));
    }

    public Collection getSkippedEjbs()
    {
        return this.skippedEjbs;
    }

    private boolean hasPackage( EjbInfo ejbInfo )
    {
        // return false if either the home or the remote interface has no
        // package defined
        if( ejbInfo.getCompInterfaceName().indexOf('.') == -1 ||
            ejbInfo.getHomeInterfaceName().indexOf('.') == -1 )
            return false;
        else
            return true;
    }
}
