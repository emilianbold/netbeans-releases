/*
 * EjbRootNodeChildren.java
 *
 * Created on May 3, 2004, 11:04 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelListener;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The container for the EjbRootNode children
 *
 * @author cao
 */
public class EjbRootNodeChildren extends Children.Keys implements EjbDataModelListener
{
    public EjbRootNodeChildren() {
    }
    
    protected org.openide.nodes.Node[] createNodes( Object key ) 
    {
        if( key instanceof String ) 
        {
            EjbGroup ejbGrp = EjbDataModel.getInstance().getEjbGroup( (String)key );
            
            Node node = new EjbGroupNode( ejbGrp );
            return new Node[] {node};
        } 
        else
            return null;
    }
    
    protected void addNotify() 
    {
        // Set the keys for the children
        
        super.addNotify();
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
        
        // Listen on the changes in the EjbDataModel
        EjbDataModel.getInstance().addListener( this );
    }
    
    protected void removeNotify() 
    {
        setKeys( Collections.EMPTY_SET );
        super.removeNotify();
        
        // No need to listen on the data model any more
        EjbDataModel.getInstance().removeListener( this );
    }
    
    public void groupAdded(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
    public void groupChanged(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
    public void groupDeleted(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
    public void groupsDeleted() 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
}
