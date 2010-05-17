/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * EjbDataModel.java
 *
 * Created on May 3, 2004, 11:38 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;
import java.util.*;

/**
 * The data model for all the ejbs loaded in the rave
 *
 * @author dongmei cao
 */
public class EjbDataModel 
{
    private static EjbDataModel instance = new EjbDataModel();
    
    // A flag to indicate whether the data model has been modified
    // during the session. 
    private boolean modified = false;
    
    private Set ejbGroups = new HashSet();
    
    // The EjbDataModelListeners
    private Set listeners;
    
    public static EjbDataModel getInstance()
    {
        return instance;
    }
    
    private EjbDataModel() 
    {
    }
    
    public EjbGroup findEjbGroupForJar( String jar )
    {
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            
            for( Iterator jarIter = grp.getClientJarFiles().iterator(); jarIter.hasNext(); )
            {
                String jarFileName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( (String)jarIter.next() );

                if( jarFileName.equals( jar ) )
                    return grp;
            }
        }
        
        return null;
    }
    
    public Collection findEjbGroupsForJar( String jar )
    {
        Collection grps = new ArrayList();
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            
            for( Iterator jarIter = grp.getClientJarFiles().iterator(); jarIter.hasNext(); )
            {
                String jarFileName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( (String)jarIter.next() );

                if( jarFileName.equals( jar ) )
                    grps.add( grp );
            }
        }
        
        return grps;
    }
    
    /**
     * Finds the EJB Group containing the given client jars
     *
     * @param jars a list of jar names (not path)
     */
    public EjbGroup findEjbGroupForJars( ArrayList jars )
    {
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            
            if( grp.getClientJarFiles().size() == jars.size() )
            {
                // Lets first assume we found it
                boolean foundIt = true;
            
                for( Iterator jarIter = grp.getClientJarFiles().iterator(); jarIter.hasNext(); )
                {
                    String jarFileName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( (String)jarIter.next() );

                    if( !jars.contains( jarFileName ) )
                    {
                        foundIt = false;
                        break;
                    }
                }
                
                if( foundIt )
                    return grp;
            }
        }
        
        return null;
    }
    
    /**
     * Finds the EJB Group containing the given client jars
     *
     * @param jars a list of jar names (not path)
     */
    public EjbGroup findEjbGroupForClientWrapperJar( String wrapperJarName )
    {
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            
            String jarFileName = org.netbeans.modules.visualweb.ejb.util.Util.getFileName( grp.getClientWrapperBeanJar() );
            
            if( jarFileName.equals( wrapperJarName ) )
                return grp;
        }
        
        return null;
    }
    
    /**
     * Find the EJB with the given remote interface name
     */
    public EjbInfo findEjbInfo( String remote )
    {
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            EjbInfo ejb = grp.getEjbInfo( remote );
            
            if( ejb != null )
                return ejb;            
        }
        
        return null;
    }
    
    /**
     * Find the EJB group the EJB with the given remote interface
     */
    public EjbGroup findEjbGroup( String remote )
    {
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            EjbInfo ejb = grp.getEjbInfo( remote );
            
            if( ejb != null )
                return grp;    
        }
        
        return null;
    }
    
    public EjbGroup getEjbGroup( String name )
    {
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            if( grp.getName().equals( name ) )
                return grp;
        }
        
        return null;
    }
    
    public Set getEjbGroups()
    {
        return this.ejbGroups;
    }
    
    public Collection getEjbGroupNames()
    {
        ArrayList names = new ArrayList();
        
        for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup grp = (EjbGroup)iter.next();
            names.add( grp.getName() );
        }
        
        // Sort the names; then return
        Collections.sort( names );
        return names;
    }
    
    /**
     * Modify the ejb group 
     */
    public void modifyEjbGroup( EjbGroup oldValue, EjbGroup newValue )
    {
        EjbGroup group = getEjbGroup( oldValue.getName() );
        group.setName( newValue.getName() );
        group.setAppServerVendor( newValue.getAppServerVendor() );
        group.setClientJarFiles( newValue.getClientJarFiles() );
        group.setIIOPPort( newValue.getIIOPPort() );
        group.setServerHost( newValue.getServerHost() );
        group.setClientWrapperBeanJar( newValue.getClientWrapperBeanJar() );
        group.setDesignInfoJar( newValue.getDesignInfoJar() );
        
        // Notify the listeners about this new group
        if( listeners != null && !listeners.isEmpty() )
        {
            EjbDataModelEvent event = new EjbDataModelEvent( newValue );
            
            for( Iterator iter = listeners.iterator(); iter.hasNext(); )
            {
                EjbDataModelListener listener = (EjbDataModelListener)iter.next();
                listener.groupChanged( event );
            }
        }
        
        touchModifiedFlag();
    }
    
    public void refreshEjbGroup( EjbGroup group )
    {
        // Notify the listeners aobut the possible changes
        if( listeners != null && !listeners.isEmpty() )
        {
            EjbDataModelEvent event = new EjbDataModelEvent( group );
            
            for( Iterator iter = listeners.iterator(); iter.hasNext(); )
            {
                EjbDataModelListener listener = (EjbDataModelListener)iter.next();
                listener.groupChanged( event );
            }
        }
        
        touchModifiedFlag();
    }
    
    public void addEjbGroups( Collection groups )
    {
        if( ejbGroups == null )
            ejbGroups = new HashSet( groups );
        else
        {
            for( Iterator iter = groups.iterator(); iter.hasNext(); )
            {
                addEjbGroup( (EjbGroup)iter.next());
            }
        }
        
        touchModifiedFlag();
    }
    
    // Add a new EjbGroup
    public void addEjbGroup( EjbGroup newGroup )
    {
        if( ejbGroups == null )
            ejbGroups = new HashSet();
        
        // Make sure the name is unique
        // Fix it if not
        checkNameUniqueness( newGroup );

        ejbGroups.add( newGroup );
        
        // Notify the listeners about this new group
        if( listeners != null && !listeners.isEmpty() )
        {
            EjbDataModelEvent event = new EjbDataModelEvent( newGroup );
            
            for( Iterator iter = listeners.iterator(); iter.hasNext(); )
            {
                EjbDataModelListener listener = (EjbDataModelListener)iter.next();
                listener.groupAdded( event );
            }
        }
        
        touchModifiedFlag();
    }
    
    public void removeEjbGroup( String name )
    {
        EjbGroup grp = getEjbGroup( name );
        removeEjbGroup( grp );
    }
    
    public void removeEjbGroup( EjbGroup group )
    {
        ejbGroups.remove( group );
        
        // Notify the listeners about this remove
        if( listeners != null && !listeners.isEmpty() )
        {
            EjbDataModelEvent event = new EjbDataModelEvent( group );
            
            for( Iterator iter = listeners.iterator(); iter.hasNext(); )
            {
                EjbDataModelListener listener = (EjbDataModelListener)iter.next();
                listener.groupDeleted( event );
            }
        }
        
        touchModifiedFlag();
    }
    
    public void removeEjbGroups( Collection groups )
    {
        ejbGroups.removeAll( groups );
        
        // Notify the listeners about this remove
        if( listeners != null && !listeners.isEmpty() )
        {
            for( Iterator iter = listeners.iterator(); iter.hasNext(); )
            {
                EjbDataModelListener listener = (EjbDataModelListener)iter.next();
                listener.groupsDeleted();
            }
        }
        
        touchModifiedFlag();
    }
    
    public void addListener( EjbDataModelListener listener )
    {
        if( !(listener instanceof EjbDataModelListener) )
            throw new java.lang.IllegalArgumentException( "Incorrect listener" ); 
        
        if( listeners == null )
            listeners = new HashSet();
        
        listeners.add( listener );
    }
    
    public void removeListener( EjbDataModelListener listener )
    {
        listeners.remove( listener );
    }
    
    private void checkNameUniqueness( EjbGroup ejbGroup )
    {
        Collection existingNames = getEjbGroupNames();
        String name = ejbGroup.getName();
        
        if( !existingNames.contains( name ) )
            return;
        
        // Name not unique. Then get a unique one
        ejbGroup.setName( getAUniqueName( name ) );
    }
    
    /**
     * Append a number after the given name till the name is unique
     *
     * @param baseName The name the number will be appended on 
     */
    public String getAUniqueName( String baseName )
    {
        Collection existingNames = getEjbGroupNames();
        
        int count = 1;
        String newName = baseName + count;
        while( existingNames.contains( newName ) ) {
            count ++;
            newName = baseName + count;
        }
        
        return newName;
    }
    
    public void resetModifiedFlag()
    {
        this.modified = false;
    }
    
    public void touchModifiedFlag()
    {
        this.modified = true;
    }
    
    public boolean isModified()
    {
        return this.modified;
    }
}
