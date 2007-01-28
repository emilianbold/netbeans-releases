/*
 * WebsphereDeploymentDescriptorParser.java
 *
 * Created on August 3, 2004, 4:24 PM
 */

package org.netbeans.modules.visualweb.ejb.load;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
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
}

