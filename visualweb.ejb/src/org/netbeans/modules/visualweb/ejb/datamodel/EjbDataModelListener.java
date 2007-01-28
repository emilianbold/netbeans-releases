/*
 * EjbDataModelListener.java
 *
 * Created on May 5, 2004, 4:18 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

import java.util.EventListener;

/**
 * To listen on the changes happening in the data model
 *
 * @author  cao
 */
public interface EjbDataModelListener extends EventListener 
{
    public void groupAdded( EjbDataModelEvent modelEvent );
    public void groupDeleted( EjbDataModelEvent modelEvent );
    public void groupsDeleted();
    public void groupChanged( EjbDataModelEvent modelEvent );
}
