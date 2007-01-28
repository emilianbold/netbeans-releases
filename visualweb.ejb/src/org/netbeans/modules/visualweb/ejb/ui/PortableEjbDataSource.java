/*
 * PortableEjbDataSource.java
 *
 * Created on August 31, 2004, 4:08 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;

/**
 * Encapsulates the EJB datasource for exporting or imparting
 *
 * @author  cao
 */
public class PortableEjbDataSource {
    
    // The data to be imported or exported
    private EjbGroup ejbGroup;
    
    // To indicate whether it is importable or exportable
    private boolean isPortable;
    
    public PortableEjbDataSource( EjbGroup ejbGroup )
    {
        this( ejbGroup, true );
    }
    
    public PortableEjbDataSource( EjbGroup ejbGroup, boolean selected )
    {
        this.ejbGroup = ejbGroup;
        this.isPortable = selected;
    }
    
    public String getName()
    {
        return ejbGroup.getName();
    }
    
    public boolean isPortable() { return this.isPortable; };
    
    public void setIsPortable( boolean portable )
    {
        this.isPortable = portable;
    }
    
    public EjbGroup getEjbGroup() { return this.ejbGroup; }
    
    public void setEjbGroup( EjbGroup ejbGroup )
    {
        this.ejbGroup = ejbGroup;
    }
    
}
