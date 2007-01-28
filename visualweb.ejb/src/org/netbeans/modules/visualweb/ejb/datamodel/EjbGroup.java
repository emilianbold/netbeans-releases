/*
 * EjbGroup.java
 *
 * Created on April 28, 2004, 4:46 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;

/**
 * This class is to encapsulates the information Rave needs to know abount an EJB Group.
 * An EJB group is a group of EJBs deployed on the same server. They must shared the
 * following information:
 *  - Application server vendor name, for example, SunAppLicationServer, WebLogic, WebSphere
 *  - Server host name
 *  - RMI-IIOP port number
 *  - Client Jar file
 *
 * An EJB group also contains
 *  - All the session Beans in the group
 *  - All the entity Beans in the group
 *  - All the message Driven beans in the group
 *
 * @author cao
 */
public class EjbGroup implements java.lang.Cloneable
{
    private Vector propertyChangeListeners;

    private String name;
    private String appServerVendorName;
    private String hostName;
    private int iiopPort;
    // A collection of Strings
    private ArrayList clientJarFileNames;
    private String ddLocationFileName;
    private String clientWrapperBeanJar;
    private String designInfoJar;

    // EjbInfo objects in this ejb group
    // Keyed by component interface - (component interface, EjbInfo)
    private Map sessionBeans;
    private Map entityBeans;
    private Map mdbs;
    
    // All the classes found in this ejb group
    private ArrayList allClazz = new ArrayList();

    public EjbGroup(String name, String vendorName, String serverHostName, int iiopPortNum, ArrayList clientJarFileNames, String ddLocationFileName )
    {
        this.name = name;
        this.appServerVendorName = vendorName;
        this.hostName = serverHostName;
        this.iiopPort = iiopPortNum;
        this.clientJarFileNames = clientJarFileNames;
        this.ddLocationFileName = ddLocationFileName;
    }

    public EjbGroup(String name, String vendorName, String serverHostName, int iiopPortNum, ArrayList clientJarFileNames)
    {
        this( name, vendorName, serverHostName, iiopPortNum, clientJarFileNames, null );
    }

    public EjbGroup()
    {
    }

    /**
     * Find the ejb with the given remote interface
     */
    public EjbInfo getEjbInfo( String remote )
    {
        // Only search session beans for now
        return (EjbInfo)sessionBeans.get( remote );
    }

    public void setName( String newName )
    {
        // Note: if the old name is null, most likely it is
        // called by the ejb loader. We do not want to
        // notify any listeners for such case.

        if( this.name != null && !this.name.equals( newName ) )
        {
            // Notify whoever is interested in the change
            notifyPropertyChangeListeners( "name", name, newName );
        }

        this.name = newName;
    }

    public void setAppServerVendor( String vendor )
    {
        this.appServerVendorName = vendor;
    }

    public void setServerHost( String host )
    {
        this.hostName = host;
    }

    public void setIIOPPort( int port )
    {
        this.iiopPort = port;
    }


    public void setClientJarFiles( ArrayList fileNames )
    {
        this.clientJarFileNames = fileNames;
    }

    public void addClientJarFile( String jarFile )
    {
        if( this.clientJarFileNames == null )
            this.clientJarFileNames = new ArrayList();

        this.clientJarFileNames.add( jarFile );
    }

    public void setDDLocationFile( String fileName )
    {
        this.ddLocationFileName = fileName;
    }

    public void setSessionBeans( Collection beans )
    {
        if( beans == null )
        {
            sessionBeans = null;
            return;
        }

        // Wipe out what had before
        sessionBeans = new HashMap();

        for( Iterator iter = beans.iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            sessionBeans.put( ejbInfo.getCompInterfaceName(),  ejbInfo );
        }
    }

    public void addSessionBeans( Collection beans )
    {
        if( this.sessionBeans == null )
            this.sessionBeans = new HashMap();

        for( Iterator iter = beans.iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            sessionBeans.put( ejbInfo.getCompInterfaceName(),  ejbInfo );
        }
    }

    public void addSessionBean( EjbInfo ejbInfo )
    {
        if( sessionBeans == null )
            sessionBeans = new HashMap();

        sessionBeans.put( ejbInfo.getCompInterfaceName(), ejbInfo );
    }

    public void setEntityBeans( Collection beans )
    {
        if( beans == null )
        {
            entityBeans = null;
            return;
        }

        // Wipe out what had before
        entityBeans = new HashMap();

        for( Iterator iter = beans.iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            entityBeans.put( ejbInfo.getCompInterfaceName(),  ejbInfo );
        }
    }

    public void setMDBs( Collection beans )
    {
        if( beans == null )
        {
            mdbs = null;
            return;
        }

        // Wipe out what had before
        mdbs = new HashMap();

        for( Iterator iter = beans.iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            mdbs.put( ejbInfo.getCompInterfaceName(),  ejbInfo );
        }
    }

    public void setClientWrapperBeanJar( String jar )
    {
        this.clientWrapperBeanJar = jar;
    }
    
    public void setDesignInfoJar( String jar )
    {
        this.designInfoJar = jar;
    }
    
    public void setAllClazz( Set allClazz )
    {
        ArrayList classList = new ArrayList( allClazz );
        Collections.sort( classList );
        this.allClazz = classList;
    }

    public String getName() { return this.name; }
    public String getAppServerVendor() { return this.appServerVendorName; }
    public String getServerHost() { return this.hostName; }
    public int getIIOPPort() { return this.iiopPort; }
    public ArrayList getClientJarFiles() { return this.clientJarFileNames; }
    public String getDDLocationFile() { return this.ddLocationFileName; }
    public ArrayList getAllClazz() { return this.allClazz; }
    
    public boolean hasAnyMethodWithColletionReturn()
    {
        for( Iterator iter = getSessionBeans().iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            
            if( ejbInfo.hasAnyMethodWithCollectionReturn() )
                return true;
        }
        
        // Didn't find any
        return false;
    }
    
    public boolean hasAnyConfigurableMethod()
    {
        for( Iterator iter = getSessionBeans().iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            
            if( ejbInfo.hasAnyConfigurableMethod() )
                return true;
        }
        
        // Didn't find any
        return false;
    }

    public String getClientJarFilesAsOneStr()
    {
        if( this.clientJarFileNames == null || this.clientJarFileNames.isEmpty() )
            return null;
        else
        {
            StringBuffer str = new StringBuffer();
            boolean first = true;
            for( Iterator iter = this.clientJarFileNames.iterator(); iter.hasNext(); )
            {
                String jar = (String)iter.next();

                if( first )
                    first = false;
                else
                    str.append( ", " );

                str.append( jar );
            }

            return str.toString();
        }
    }

    public ArrayList getClientJarFileNames()
    {
        ArrayList justNames = new ArrayList();

        if( this.clientJarFileNames != null )
        {
            for( Iterator iter = this.clientJarFileNames.iterator(); iter.hasNext(); )
                justNames.add( org.netbeans.modules.visualweb.ejb.util.Util.getFileName( (String)iter.next() ) );
        }

        return justNames;
    }

    public Collection getSessionBeans() {
        if( this.sessionBeans != null )
        {
            ArrayList beans = new ArrayList( this.sessionBeans.values() );
            Collections.sort( beans );
            return beans;
        }
        else
            return null;
    }

    public Collection getEntityBeans() {
        if( this.entityBeans != null )
            return this.entityBeans.values();
        else
            return null;
    }
    public Collection getMDBs() {
        if( this.mdbs != null )
            return this.mdbs.values();
        else
            return null;
    }
    
    public String getDesignInfoJar() {
        return this.designInfoJar;
    }
    
    public String getClientWrapperBeanJar() {
        return this.clientWrapperBeanJar;
    }
    
    public boolean isSunAppServer()
    {
        if( this.getAppServerVendor().equals( EjbContainerVendor.SUN_APP_SERVER_8_1 ) ||
            this.getAppServerVendor().equals( EjbContainerVendor.SUN_APP_SERVER_8 ) ||
            this.getAppServerVendor().equals( EjbContainerVendor.SUN_APP_SERVER_7 ) )
            return true;
        else
            return false;
    }

    public boolean isWebLogicAppServer()
    {
        if( this.getAppServerVendor().equals( EjbContainerVendor.WEBLOGIC_8_1 ) )
            return true;
        else
            return false;
    }

    public boolean isWebsphereAppServer()
    {
        if( this.getAppServerVendor().equals( EjbContainerVendor.WEBSPHERE_5_1 ) )
            return true;
        else
            return false;
    }
    
    public void fixJarDir( String newDir )
    {
        // Client jars
        if( this.clientJarFileNames != null )
        {
            ArrayList newValues = new ArrayList();
            for( Iterator iter = this.clientJarFileNames.iterator(); iter.hasNext(); )
            {
                String oldVal = (String)iter.next();
                String newVal = new File( newDir, org.netbeans.modules.visualweb.ejb.util.Util.getFileName( oldVal ) ).getAbsolutePath();
                newValues.add( newVal );
            }
            
            this.clientJarFileNames = newValues;
        }
        
        // Client wrapper beans jar
        if( this.clientWrapperBeanJar != null )
        {
            File f = new File( newDir, org.netbeans.modules.visualweb.ejb.util.Util.getFileName(this.clientWrapperBeanJar ) );
            this.clientWrapperBeanJar = f.getAbsolutePath();
        }
        
        // DesignInfo jar
        if( this.designInfoJar != null )
        {
            File f = new File( newDir, org.netbeans.modules.visualweb.ejb.util.Util.getFileName(this.designInfoJar ) );
            this.designInfoJar = f.getAbsolutePath();
        }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        // NOI18N
        buf.append( "EJB Group Name: " + getName() + "\n" );
        buf.append( "ApplicationServerVendor: " + getAppServerVendor() + "\n" );
        buf.append( "Server host: " + getServerHost() + "\n" );
        buf.append( "IIOP port: " + getIIOPPort() + "\n" );
        buf.append( "Client Jar files: " + getClientJarFiles().toString() + "\n" );
        buf.append( "WrapperBeanJarFile: " + getClientWrapperBeanJar() + "\n" );
        buf.append( "DesignInfoJar: " + getDesignInfoJar() + "\n" );
        
        if( sessionBeans != null )
        {
            buf.append( "Num of Session beans: " + sessionBeans.size() + "\n" );
            buf.append( sessionBeans.toString() );
        }

        if( mdbs != null )
            buf.append( "Num of MBDs: " + mdbs.size() + "\n" );

        if( entityBeans != null )
            buf.append( "Num of Entity beans: " + entityBeans.size() + "\n" );

        return buf.toString();
    }

    public Object clone()
    {
        try
        {
            EjbGroup groupCopy = (EjbGroup)super.clone();
            
            // Clientjar files
            if( this.clientJarFileNames != null )
            {
                ArrayList fileNameCopy = new ArrayList();
                for( Iterator iter = this.clientJarFileNames.iterator(); iter.hasNext(); )
                {
                    String fileName = (String)iter.next();
                    fileNameCopy.add( fileName );
                }

                groupCopy.setClientJarFiles( fileNameCopy );
            }
            

            // Session Beans
            if( this.sessionBeans != null )
            {
                Collection sbCopy = new HashSet();
                for( Iterator iter = this.sessionBeans.keySet().iterator(); iter.hasNext(); )
                {
                    String remote = (String)iter.next();
                    sbCopy.add( ((EjbInfo)sessionBeans.get(remote)).clone() );
                }

                groupCopy.setSessionBeans( sbCopy );
            }

            // Entity Beans
            if( this.entityBeans != null )
            {
                Collection ebCopy = new ArrayList();
                for( Iterator iter = this.entityBeans.keySet().iterator(); iter.hasNext(); )
                {
                    ebCopy.add( ((EjbInfo)iter.next()).clone() );
                }

                groupCopy.setEntityBeans( ebCopy );
            }

            // MDBs
            if( this.mdbs != null )
            {
                Collection mbCopy = new ArrayList();
                for( Iterator iter = this.mdbs.keySet().iterator(); iter.hasNext(); )
                {
                    mbCopy.add( ((EjbInfo)iter.next()).clone() );
                }

                groupCopy.setMDBs( mbCopy );
            }

            return groupCopy;
        }
        catch( java.lang.CloneNotSupportedException e )
        {
            return null;
        }
    }

    public void addPropertyChangeListener( PropertyChangeListener listener )
    {
        if( propertyChangeListeners == null )
            propertyChangeListeners = new java.util.Vector();

        propertyChangeListeners.add( listener );
    }

    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        if( propertyChangeListeners == null )
            propertyChangeListeners.remove( listener );
    }

    private void notifyPropertyChangeListeners( String propName, Object oldVal, Object newVal )
    {
        if( propertyChangeListeners != null && !propertyChangeListeners.isEmpty() )
        {
            PropertyChangeEvent evt = new PropertyChangeEvent( this, propName, oldVal, newVal );

            for( int i = 0; i < propertyChangeListeners.size(); i ++ )
            {
                PropertyChangeListener l = (PropertyChangeListener)propertyChangeListeners.get( i );
                l.propertyChange( evt );
            }
        }
    }
}
