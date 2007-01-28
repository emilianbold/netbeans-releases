/*
 * EjbDataModelEvent.java
 *
 * Created on May 5, 2004, 4:18 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

/**
 * The event generated when the data model is changed
 *
 * @author  cao
 */
public class EjbDataModelEvent
{
    private EjbGroup ejbGroup;
    
    public EjbDataModelEvent(EjbGroup ejbGroup) 
    {
        this.ejbGroup = ejbGroup;
    }
    
    public EjbGroup getEjbGroup()
    {
        return this.ejbGroup;
    }
    
}
