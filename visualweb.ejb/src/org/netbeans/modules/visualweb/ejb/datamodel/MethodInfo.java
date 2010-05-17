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
 * MethodInfo.java
 *
 * Created on April 29, 2004, 1:08 AM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * To encapsulate the information for a method
 *
 * @author  cao
 */
public class MethodInfo implements java.lang.Cloneable, Comparable
{
    // Is it a business method or create method from home interface
    private boolean isBusinessMethod = true;
    
    // Name of the method
    private String name;
    
    // Some description
    private String description;
    
    // A list of MethodParams (name,type)
    private ArrayList parameters;
    
    // method return
    private MethodReturn returnType;
    
    // A list of exception class names (Strings)
    private ArrayList exceptions;
    
    // Full package data provider class name for this method
    private String dataprovider;
    
    public MethodInfo( String name, String description, ArrayList parameters, MethodReturn returnType, ArrayList exceptions ) {
        this.isBusinessMethod = true;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.returnType = returnType;
        this.exceptions = exceptions;
    }
    
    public MethodInfo( boolean buzMethod, String name, String description, ArrayList parameters, MethodReturn returnType, ArrayList exceptions ) {
        this.isBusinessMethod = buzMethod;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.returnType = returnType;
        this.exceptions = exceptions;
    }
    public MethodInfo()
    {
        this( null, null, null, null, null );
    }
    
    public void setName( String name )
    {
        this.name = name;
    }
    
    public void setDescription( String description )
    {
        this.description = description;
    }
    
    /**
     * @param parameters a list of MethodParam objects for the method parameters
     */
    public void setParameters( ArrayList parameters )
    {
        this.parameters = parameters;
    }
    
    public void addParameter( MethodParam parameter )
    {
        if( parameters == null )
            parameters = new ArrayList();
        
        parameters.add( parameter );
    }
    
    public boolean isParamNameUnique( String name )
    {
        for( int i = 0; i < parameters.size(); i ++ )
        {
            MethodParam p = (MethodParam)parameters.get(i);
            if( p.getName().equals( name ) )
                return false;
        }
        
        return true;
    }
    
    public boolean hasNoParameters()
    {
        if( parameters == null || parameters.isEmpty() )
            return true;
        else
            return false;
    }
    
    public void setReturnType( MethodReturn returnType )
    {
        this.returnType = returnType;
    }
   
    public void addException( String exception )
    {
        if( exceptions == null )
            exceptions = new ArrayList();
        
        exceptions.add( exception );
    }
    
    public void setExceptions( ArrayList exceptions )
    {
        this.exceptions = exceptions;
    }
    
    public void setIsBusinessMethod( boolean buz )
    {
        this.isBusinessMethod = buz; 
    }
    
    public void setDataProvider( String dataprovider )
    {
        this.dataprovider = dataprovider;
    }
    
    public boolean isMethodConfigurable()
    {
        if( !isBusinessMethod() )
            return false;
        
        // If the method has parameters and the return type is collection
        // then this method is considered as configurable because the user can change these things
        if( (getParameters() != null && !getParameters().isEmpty()) ||
            getReturnType().isCollection() )
            return true;
        else
            return false;
    }
    
    public boolean isBusinessMethod() { return this.isBusinessMethod; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    
    public ArrayList getParameters() { 
        if( parameters == null )
            return new ArrayList();
        else
            return this.parameters; 
    }
    
    public String getDataProvider() { return this.dataprovider; }
    public MethodReturn getReturnType() { return this.returnType; }
    public ArrayList getExceptions() { return this.exceptions; }
    
    public String getParametersAsOneStr()
    {
        StringBuffer buf = new StringBuffer();
        
        if( parameters != null && !parameters.isEmpty() )
        {
            boolean first = true;
            for( Iterator iter = parameters.iterator(); iter.hasNext(); )
            {
                MethodParam p = (MethodParam)iter.next();
                
                if( first )
                    first = false;
                else
                    buf.append( ", " );
                
                buf.append( p.toString() );
            }
        }
        
        return buf.toString();
    }
    
    public Class[] getParameterTypes()
    {
        if( parameters == null )
            return null;
        else
        {
            Class[] types = new Class[ parameters.size() ];
            for( int i = 0; i < parameters.size(); i ++ )
            {
                MethodParam p = (MethodParam)parameters.get( i );
                
                try {
                    Class type = Class.forName( p.getType() );
                    types[i] = type;
                } catch( Exception e ) {
                    types[i] = null;
                }
            }
            
            return types;
        }
    }
    
    public String getExceptionsAsOneStr()
    {
        StringBuffer buf = new StringBuffer();
        
        if( exceptions != null && !exceptions.isEmpty() )
        {
            boolean first = true;
            for( Iterator iter = exceptions.iterator(); iter.hasNext(); )
            {
                String exception = (String)iter.next();
                
                if( first )
                    first = false;
                else
                    buf.append( ", " );
                
                buf.append( exception );
            }
        }
        
        return buf.toString();
    }
    
    public Object clone()
    {
        try
        {
            MethodInfo methodCopy = (MethodInfo)super.clone();
            
            // Parameters
            if( this.parameters != null )
            {
                ArrayList pmCopy = new ArrayList();
                for( Iterator iter = this.parameters.iterator(); iter.hasNext(); )
                {
                    MethodParam p = (MethodParam)iter.next();
                    pmCopy.add( new MethodParam( p.getName(), p.getType() ) );
                }
                
                methodCopy.setParameters( pmCopy );
            }
            
            // Exceptions
            if( this.exceptions != null )
            {
                ArrayList exCopy = new ArrayList();
                for( Iterator iter = this.exceptions.iterator(); iter.hasNext(); )
                {
                    exCopy.add( new String( (String)iter.next() ) );
                }
                
                methodCopy.setExceptions( exCopy );
            }
            
            return methodCopy;
        }
        catch( java.lang.CloneNotSupportedException e )
        {
            return null;
        }
    }
    
    public String getSignature()
    {
        // NOI18N
        StringBuffer buf = new StringBuffer();
        buf.append( "public " );
        buf.append( getReturnType().getClassName() );
        buf.append( " " );
        buf.append( getName() );
        buf.append( "(" );
        
        if( parameters != null && parameters.size() != 0 )
        {
            for( int i = 0; i < parameters.size(); i ++ )
            {
                MethodParam p = (MethodParam)parameters.get(i);
                if( i != 0 )
                    buf.append( ", " );

                buf.append( p.getType() );
            }
        }
        
        buf.append( ") throws " );
        
        // Exception. There is always at least one Exception - RemoteException
        buf.append( getExceptionsAsOneStr() );
        
        buf.append( " \n" );
        
        return buf.toString();
    }
    
    public String toString()
    {
        // NOI18N
        StringBuffer buf = new StringBuffer();
        buf.append( "public " );
        buf.append( getReturnType().getClassName() );
        buf.append( " " );
        buf.append( getName() );
        buf.append( "(" );
        
        if( parameters != null && parameters.size() != 0 )
        {
            for( int i = 0; i < parameters.size(); i ++ )
            {
                MethodParam p = (MethodParam)parameters.get(i);
                if( i != 0 )
                    buf.append( ", " );

                buf.append( p.toString() );
            }
        }
        
        buf.append( ") throws " );
        
        // Exception. There is always at least one Exception - RemoteException
        buf.append( getExceptionsAsOneStr() );
        
        buf.append( " \n" );
        
        return buf.toString();
    }
    
    // Implementing Comparable   
    public int compareTo(Object o) {
        
        if( o== null || !(o instanceof MethodInfo) )
            return 0;
        
        String theOtherName1 = ((MethodInfo)o).getName();

        return this.getName().compareTo( theOtherName1 );
    }
    
}
