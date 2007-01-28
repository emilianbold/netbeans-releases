/*
 * SunXmlParser.java
 *
 * Created on April 28, 2004, 11:04 PM
 */

package org.netbeans.modules.visualweb.ejb.load;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is used to parse the Sun specific deployment
 * descriptor - sun-ejb-jar.xml.
 */
public class SunDeploymentDescriptorParser extends DefaultHandler{

    private static final String EJB_TAG = "ejb";
    private static final String EJB_NAME_TAG = "ejb-name";
    private static final String JNDI_NAME_TAG = "jndi-name";

    private String xmlFileName;

    private String jndiName;
    private String ejbName;

    private String currentTag;
    private String data;

    private Map nameMapping = new HashMap();

    /** Creates a new instance of VendorXmlParser */
    public SunDeploymentDescriptorParser(String vendorXml) {
        this.xmlFileName = vendorXml;
    }

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
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.SunDeploymentDescriptorParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            // Throw up as SYSTEM_ERROR. Should never happen
            throw new EjbLoadException( e.getMessage() );
        }
        catch( ParserConfigurationException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the vendor EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.SunDeploymentDescriptorParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_VENDOR_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
        catch( SAXException e )
        {
            // Log error
            String logMsg = "Error occured when trying to parse the vendor EJB deployment descriptor file " + xmlFileName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.SunDeploymentDescriptorParser").log( ErrorManager.WARNING, logMsg );
            e.printStackTrace();

            String errMsg = NbBundle.getMessage( StdDeploymentDescriptorParser.class, "CANNOT_PARSE_VENDOR_DD" );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
    }

      public void startElement( String uri, String localName, String qName,
                              Attributes attributes ) throws SAXException
    {
        currentTag = qName;
    }

      public void endElement( String uri, String localName, String qName )
                    throws SAXException
      {
          if( currentTag != null )
              setData();
         
          if( ejbName != null && jndiName != null ) {
              nameMapping.put( ejbName, jndiName );
              ejbName = null;
              jndiName = null;
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

        if( currentTag.equalsIgnoreCase( JNDI_NAME_TAG ) )
            jndiName = data;
        else if( currentTag.equalsIgnoreCase( EJB_NAME_TAG ) )
            ejbName = data;
    }

    public InputSource resolveEntity( String publicId, String systemId )
    {
        try {
            // PE 8.1 sun-ejb-jar.xml
            if( systemId.equals("http://www.sun.com/software/appserver/dtds/sun-ejb-jar_2_1-1.dtd") ) // NOI18N
            {
                return new InputSource( new java.io.CharArrayReader( new char[0] ) ); // NOI18N
            }
            // PE 8.0 sun-ejb-jar.xml
            else if( systemId.equals("http://www.sun.com/software/appserver/dtds/sun-ejb-jar_2_1-0.dtd") ) // NOI18N
            {
                return new InputSource( new java.io.CharArrayReader( new char[0] ) );
            }
            // SunAppServer 7 sun-ejb-jar.xml
            else if( systemId.equals("http://www.sun.com/software/sunone/appserver/dtds/sun-ejb-jar_2_0-0.dtd")) // NOI18N
            {
                return new InputSource( new java.io.CharArrayReader( new char[0] ) );
            }
            else
                return null;
        }
        catch( Exception e ) {
            // Ignore the exception
            return null;
        }
    }
}
