/*
 * EjbContainerVendor.java
 *
 * Created on April 28, 2004, 4:50 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

import org.openide.util.NbBundle;

/**
 * This class lists all the EJB container vendors Rave can support 
 *
 * @author  cao
 */
public class EjbContainerVendor
{
    // Supported application servers
    public static final String SUN_APP_SERVER_8_1 = NbBundle.getMessage( EjbContainerVendor.class, "SUN_APP_SERVER_8_1" );
    public static final String SUN_APP_SERVER_8 = NbBundle.getMessage( EjbContainerVendor.class, "SUN_APP_SERVER_8" );
    public static final String SUN_APP_SERVER_7 = NbBundle.getMessage( EjbContainerVendor.class, "SUN_APP_SERVER_7" );
    public static final String WEBLOGIC_8_1 = NbBundle.getMessage( EjbContainerVendor.class, "BEA_WEBLOGIC_8_1" );
    public static final String WEBSPHERE_5_1 = NbBundle.getMessage( EjbContainerVendor.class, "IBM_WEBSPHERE_5_1" );
    
    // Default IIOP Port for different application servers
    public static final int SUN_APP_SERVER_PORT = 3700;
    public static final int WEBLOGIC_PORT = 7001;
    public static final int WEBSPHERE_PORT = 2809;
    public static final int UNKNOWN_PORT = 0;
    
    // EJB deployment descriptors
    public static final String STANDARD_DEPLOYMENT_DESCRIPTOR = "ejb-jar.xml";
    public static final String SUN_DEPLOYMENT_DESCRIPTOR = "sun-ejb-jar.xml";
    public static final String WEBLOGIC_DEPLOYMENT_DESCRIPTOR = "weblogic-ejb-jar.xml";
    public static final String WEBSPHERE_DEPLOYMENT_DESCRIPTOR = "ibm-ejb-jar-bnd.xmi";
    
    public static String[] getContainerTypeNames()
    {
        String[] containerTypes = { SUN_APP_SERVER_8_1, SUN_APP_SERVER_8, SUN_APP_SERVER_7, WEBLOGIC_8_1, WEBSPHERE_5_1 };
        return containerTypes;
    }
    
    public static int getDefaultPort( String server )
    {
        if( server.equals( SUN_APP_SERVER_8_1 ) || server.equals( SUN_APP_SERVER_8 ) || server.equals( SUN_APP_SERVER_7 ) )
            return SUN_APP_SERVER_PORT;
        else if( server.equals( WEBLOGIC_8_1 ) )
            return WEBLOGIC_PORT;
        else if( server.equals( WEBSPHERE_5_1 ) )
            return WEBSPHERE_PORT;
        else
            return UNKNOWN_PORT;
    }
    
    public static String getVendorDDFileName( String server )
    {
        if( server.equals( SUN_APP_SERVER_8_1 ) || server.equals( SUN_APP_SERVER_8 ) || server.equals( SUN_APP_SERVER_7 ) )
            return SUN_DEPLOYMENT_DESCRIPTOR;
        else if( server.equals( WEBLOGIC_8_1 ) )
            return WEBLOGIC_DEPLOYMENT_DESCRIPTOR;
        else if( server.equals( WEBSPHERE_5_1 ) )
            return WEBSPHERE_DEPLOYMENT_DESCRIPTOR;
        else
            throw new java.lang.IllegalArgumentException( server );
    }
}
