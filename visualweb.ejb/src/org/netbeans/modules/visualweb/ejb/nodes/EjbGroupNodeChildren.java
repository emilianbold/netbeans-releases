/*
 * Class.java
 *
 * Created on May 3, 2004, 5:52 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelListener;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * The child nodes for EJB Group, which are grouping nodes:
 *   - Session Beans
 *   - Entity Beans
 *   - Message Driven Beans
 *
 * @author  cao
 */
public class EjbGroupNodeChildren extends Children.Keys implements EjbDataModelListener
{
    private EjbGroup ejbGroup;
    
    public EjbGroupNodeChildren(EjbGroup ejbGroup) 
    {
        this.ejbGroup = ejbGroup;
    }
    
    protected org.openide.nodes.Node[] createNodes( Object key ) 
    {
       // For each key/session bean, we'll create a session bean node
       if( key instanceof EjbInfo ) 
        {
            
            Node node = new SessionBeanNode( ejbGroup, (EjbInfo)key );
            return new Node[] {node};
        } 
        else
            return null;
    }
    
    protected void addNotify() 
    {
        // Set the keys for the children
        
        super.addNotify();
        if( ejbGroup.getSessionBeans() != null && !ejbGroup.getSessionBeans().isEmpty() )
            setKeys( ejbGroup.getSessionBeans() );
        else
            setKeys( Collections.EMPTY_SET );
        
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
        // Handled by EjbRootNodeChildren
    }
    
    public void groupChanged(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        if( modelEvent.getEjbGroup() == ejbGroup )
            setKeys( ejbGroup.getSessionBeans() );
    }
    
    public void groupDeleted(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        // Handled by EjbRootNodeChildren
    }
    
    public void groupsDeleted() 
    {
        // Handled by EjbRootNodeChildren
    }    
    
    
}
