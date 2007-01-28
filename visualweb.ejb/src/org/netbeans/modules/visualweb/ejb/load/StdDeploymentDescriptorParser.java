/*
 * EjbJarXmlParser.java
 *
 * Created on April 28, 2004, 11:04 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import java.util.*;
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
            ErrorManager.getDefault().getInstance( "com.sun.rave.ejb.load.EjbJarXmlParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            // Throw up as SYSTEM_ERROR. Should never happen
            throw new EjbLoadException( e.getMessage() );
        }
        catch( ParserConfigurationException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the standard EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "com.sun.rave.ejb.load.EjbJarXmlParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_STD_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
        catch( SAXException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the standard EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "com.sun.rave.ejb.load.EjbJarXmlParser").log( ErrorManager.WARNING, logMsg );
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
        try
        {
            // sunAppServer 7 ejb-jar.xml
            if (systemId.equals("http://java.sun.com/j2ee/dtds/ejb-jar_1_1.dtd")) // NOI18N
            {
                return new InputSource( new java.io.CharArrayReader( new char[0] ) );
            }
            // weblogic 8.1 ejb-jar.xml
            else if( systemId.equals( "http://java.sun.com/dtd/ejb-jar_2_0.dtd" ) ) // NOI18N
            {
                return new InputSource( new java.io.CharArrayReader( new char[0] ) );
            }
            else
                return null;
            }
            catch( Exception e )
            {
                // Ignore the exception
                return null;
            }
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
