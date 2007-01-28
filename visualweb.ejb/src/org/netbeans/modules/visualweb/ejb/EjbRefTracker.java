/*
 * EjbRefTracker.java
 *
 * Created on March 23, 2005, 11:24 PM
 */

package org.netbeans.modules.visualweb.ejb;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbContainerVendor;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
//This was a hacker. Can not use it anymore
//import org.netbeans.modules.j2ee.deployment.devmodules.spi.RequestedEjbResource;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author  cao
 */
public class EjbRefTracker {
    private static final String EJB_DATASOURCE_ELEMENT_NAME = "ejb-data-sources" ; // NOI18N
    private static final String EJB_DATASOURCE_ELEMENT_NAMESPACE = "http://creator.sun.com/project/ejb-datasfdsources" ; // NOI18N
    
    /*public static RequestedEjbResource[] getRequestedEjbResources( Project project ) 
    {
        AuxiliaryConfiguration projectAux = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class) ;
        if ( projectAux == null ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.ERROR, "no AuxiliaryConfiguration.class implementation for Project "+ project ) ; // NOI18N
        } else {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.INFORMATIONAL, "we have an AuxiliaryConfiguration") ; // NOI18N
        }
        Element ejbdatasourceElement = projectAux.getConfigurationFragment(EJB_DATASOURCE_ELEMENT_NAME, EJB_DATASOURCE_ELEMENT_NAMESPACE, false) ;
        
        if ( ejbdatasourceElement != null ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.INFORMATIONAL, "Restoring ejb datasoruce for " + project ) ;
            
            String tagName = ejbdatasourceElement.getTagName();
            
            ArrayList ejbResources = new ArrayList();
            NodeList groupNodeList = ejbdatasourceElement.getElementsByTagName( "ejb-group"); 
            for( int i = 0; i < groupNodeList.getLength(); i ++ )
            {
                Element grpElement = (Element)groupNodeList.item( i );
                
                Element groupNameElement = (Element)grpElement.getElementsByTagName( "group-name" ).item( 0 );
                
                Element containerElement = (Element)grpElement.getElementsByTagName( "container" ).item( 0 );
                String appServer = containerElement.getFirstChild().getNodeValue();
                
                Element serverHostElement = (Element)grpElement.getElementsByTagName( "server-host" ).item( 0 );
                String hostName = serverHostElement.getFirstChild().getNodeValue();
                
                Element iiopElement = (Element)grpElement.getElementsByTagName( "iiop-port" ).item( 0 );
                String iiop = iiopElement.getFirstChild().getNodeValue();
                
                Element ejbsElement = (Element)grpElement.getElementsByTagName( "enterprise-beans" ).item( 0 );
                // TODO stateful-session too
                NodeList ejbNodeList = ejbsElement.getElementsByTagName( "stateless-session" ); 
                for( int ei = 0; ei < ejbNodeList.getLength(); ei ++ ) 
                {
                    Element ejbElement = (Element)ejbNodeList.item( ei );
                    
                    Element homeElement = (Element)ejbElement.getElementsByTagName( "home").item( 0 );
                    String home = homeElement.getFirstChild().getNodeValue();
                    
                    Element remoteElement = (Element)ejbElement.getElementsByTagName( "remote").item( 0 );
                    String remote = remoteElement.getFirstChild().getNodeValue();
                    
                    Element ejbRefElement = (Element)ejbElement.getElementsByTagName( "web-ejb-ref").item( 0 ); 
                    String refName = ejbRefElement.getFirstChild().getNodeValue();
                    
                    // The global JNDI name for this EJB
                    //  - corbaname:iiop:<hostname>:<port>#<jndiname> for Sun Application server, weblogic
                    //  - corbaname:iiop:<hostname>:<port>/NameServiceServerRoot#<jndiname> for websphere 5.1
                    Element jndiNameElement = (Element)ejbElement.getElementsByTagName( "jndi-name" ).item( 0 );
                    String jndiName = jndiNameElement.getFirstChild().getNodeValue();
                    
                    String globalJndiName = "corbaname:iiop:" + hostName + ":" + iiop + "#" + jndiName;
                    if( appServer.equals( EjbContainerVendor.WEBSPHERE_5_1 ) )
                        globalJndiName = "corbaname:iiop:" + hostName + ":" + iiop + "/NameServiceServerRoot#" + jndiName;
                    
                    // TODO should use global JDNI name 
                    ejbResources.add(  new RequestedEjbResource( refName, jndiName, "Session", home, remote ) );
                } 
            }
            
            return (RequestedEjbResource[])ejbResources.toArray( new RequestedEjbResource[0] );
        }
        else
            return new RequestedEjbResource[0];
    }*/
    
    public static void AddEjbDataSourcesToProject( Project project, EjbGroup ejbGroup )
    {
        // Get the AuxiliaryConfiguration
        AuxiliaryConfiguration projectAux = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class) ;
        if ( projectAux == null ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.ERROR, "no AuxiliaryConfiguration.class implementation for Project "+ project ) ; // NOI18N
        } else {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.INFORMATIONAL, "we have an AuxiliaryConfiguration") ; // NOI18N
        }
        
        try { 
            Element ejbdatasourceElement = projectAux.getConfigurationFragment(EJB_DATASOURCE_ELEMENT_NAME, EJB_DATASOURCE_ELEMENT_NAMESPACE, false) ;
            
            Document doc = null;
            // No such thing yet, create one now
            if( ejbdatasourceElement == null )
            {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = docFactory.newDocumentBuilder();
                doc = db.newDocument();
                ejbdatasourceElement = doc.createElementNS(EJB_DATASOURCE_ELEMENT_NAMESPACE, EJB_DATASOURCE_ELEMENT_NAME) ;
                doc.appendChild( ejbdatasourceElement );
            }
            else
            {
                doc = ejbdatasourceElement.getOwnerDocument();
                
                NodeList groupNodeList = ejbdatasourceElement.getElementsByTagName( "ejb-group"); 
                for( int i = 0; i < groupNodeList.getLength(); i ++ )
                {
                    Element grpElement = (Element)groupNodeList.item( i );

                    Element groupName = (Element)grpElement.getElementsByTagName( "group-name" ).item( 0 );
                    if( groupName.getFirstChild().getNodeValue().equals( ejbGroup.getName() ) ) 
                        ejbdatasourceElement.removeChild( grpElement );
                } 
            }
            
            Element group = doc.createElement( "ejb-group" );
            ejbdatasourceElement.appendChild( group ); 
            
            // Child node of ejb-group: group-name
            Element groupName = doc.createElement( "group-name" );
            groupName.appendChild( doc.createTextNode( ejbGroup.getName() ) );
            group.appendChild( groupName );
            
            // Child node of ejb-group: container
            Element container = doc.createElement( "container" );
            container.appendChild( doc.createTextNode( ejbGroup.getAppServerVendor() ) );
            group.appendChild( container );
            
            // Child node of ejb-group: server-host
            Element serverHost = doc.createElement( "server-host" );
            serverHost.appendChild( doc.createTextNode( ejbGroup.getServerHost() ) );
            group.appendChild( serverHost );
            
            // Child node of ejb-group: iiop-port
            Element iiop = doc.createElement( "iiop-port" );
            iiop.appendChild( doc.createTextNode( Integer.toString( ejbGroup.getIIOPPort() ) ) );
            group.appendChild( iiop );
            
            // Child node of ejb-group: enterprise-beans
            Element ejbs = doc.createElement( "enterprise-beans" );
            group.appendChild( ejbs );
            
            // Child node of enterprise-beans: one per ejb
            for( Iterator iter = ejbGroup.getSessionBeans().iterator(); iter.hasNext(); ) {
                EjbInfo ejbInfo = (EjbInfo) iter.next();
                String tagName = "stateless-session";
                if( !ejbInfo.isStatelessSessionBean() )
                    tagName = "stateful-session";
                
                Element ejb = doc.createElement( tagName );
                ejbs.appendChild( ejb );
                
                // Child node of stateless-session or stateful-session: jndi-name
                Element jndiName = doc.createElement( "jndi-name" );
                jndiName.appendChild( doc.createTextNode( ejbInfo.getJNDIName() ) );
                ejb.appendChild( jndiName );
                
                // Child node of stateless-session or stateful-session: ejb-name
                Element ejbName = doc.createElement( "ejb-name" );
                ejbName.appendChild( doc.createTextNode( ejbInfo.getEjbName() ) );
                ejb.appendChild( ejbName );
                
                // Child node of stateless-session or stateful-session: home
                Element home = doc.createElement( "home" );
                home.appendChild( doc.createTextNode( ejbInfo.getHomeInterfaceName() ) );
                ejb.appendChild( home );
                
                // Child node of stateless-session or stateful-session: remote
                Element remote = doc.createElement( "remote" );
                remote.appendChild( doc.createTextNode( ejbInfo.getCompInterfaceName() ) );
                ejb.appendChild( remote );
                
                // Child node of stateless-session or stateful-session: web-ejb-ref
                Element webRef = doc.createElement( "web-ejb-ref" );
                webRef.appendChild( doc.createTextNode( ejbInfo.getWebEjbRef() ) );
                ejb.appendChild( webRef );
            }
            
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.INFORMATIONAL, "new proj element for hard datasources created.") ;
            
            //ejbdatasourceElement.setAttribute( "value", "EJB-DATASOURCES GO HERE" ) ; // NOI18N
            projectAux.putConfigurationFragment( ejbdatasourceElement, false );
        }
        catch ( javax.xml.parsers.ParserConfigurationException pce ) {
            // seriously screwed up.
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.ERROR, "could not create the document element for saving HC DS") ; // NOI18N
        }
    }
    
}
