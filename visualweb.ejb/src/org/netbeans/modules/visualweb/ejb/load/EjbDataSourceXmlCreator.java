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
 * EjbDataSourceXmlCreator.java
 *
 * Created on May 2, 2004, 4:17 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import java.util.*;


/**
 * This class saves the  EJBDataSource into xml file
 *
 * @author cao
 */
public class EjbDataSourceXmlCreator {
    
    public static final String XML_BEGIN_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    public static final String XML_BEGIN_2 = "<ejb-data-source xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
    public static final String XML_END = "</ejb-data-source>";
    
    private static final int INDENT_SPACES = 4;
    
    private Collection ejbGroups;
    
    // Where the xml will output/write to
    private java.io.Writer out; 
    
    public EjbDataSourceXmlCreator(EjbDataModel ejbData, java.io.Writer out) {
        this.ejbGroups = ejbData.getEjbGroups();
        this.out = out;
    }
    
    public EjbDataSourceXmlCreator( Collection ejbGroups, java.io.Writer out )
    {
        this.ejbGroups = ejbGroups;
        this.out = out;
    }
    
    public void toXml() throws java.io.IOException
    {
        out.write( XML_BEGIN_1 );
        out.write( XML_BEGIN_2 );
        
        if( this.ejbGroups == null || this.ejbGroups.isEmpty() )
        {
            out.write( XML_END );
            return;
        }
        
        for( Iterator iter = this.ejbGroups.iterator(); iter.hasNext(); )
        {
            EjbGroup ejbGrp = (EjbGroup)iter.next();
            
            out.write( openTag( XmlTagConstants.EJB_GROUP_TAG, true, 1 ) );
       
            out.write( openTag( XmlTagConstants.GROUP_NAME_TAG, false,  2 ) );
            out.write( ejbGrp.getName() );
            out.write( closeTag( XmlTagConstants.GROUP_NAME_TAG,  0 ) );

            out.write( openTag( XmlTagConstants.CONTAINER_VENDOR_TAG, false, 2 ) );
            out.write( ejbGrp.getAppServerVendor() );
            out.write( closeTag( XmlTagConstants.CONTAINER_VENDOR_TAG, 0 ) );

            out.write( openTag( XmlTagConstants.SERVER_HOST_TAG, false, 2 ) );
            out.write( ejbGrp.getServerHost() );
            out.write( closeTag( XmlTagConstants.SERVER_HOST_TAG, 0 ) );

            out.write( openTag( XmlTagConstants.IIOP_PORT_TAG, false, 2 ) );
            out.write( Integer.toString(ejbGrp.getIIOPPort()) );
            out.write( closeTag( XmlTagConstants.IIOP_PORT_TAG, 0 ) );

            for( Iterator jarIter = ejbGrp.getClientJarFiles().iterator(); jarIter.hasNext(); )
            {
                String clientJar = (String)jarIter.next();
                out.write( openTag( XmlTagConstants.CLIENT_JAR_TAG, false, 2 ) );
                out.write( clientJar );
                out.write( closeTag( XmlTagConstants.CLIENT_JAR_TAG, 0 ) );
            }
            
            // Client wrapper jar 
            out.write( openTag( XmlTagConstants.BEAN_WRAPPER_JAR_TAG, false, 2 ) );
            out.write( ejbGrp.getClientWrapperBeanJar() );
            out.write( closeTag( XmlTagConstants.BEAN_WRAPPER_JAR_TAG, 0 ) );

            // DesignInfo jar - new in Thresher
            if( ejbGrp.getDesignInfoJar() != null )
            {
                out.write( openTag( XmlTagConstants.DESIGN_TIME_TAG, false, 2 ) );
                out.write( ejbGrp.getDesignInfoJar() );
                out.write( closeTag( XmlTagConstants.DESIGN_TIME_TAG, 0 ) );
            }
            
            if( ejbGrp.getDDLocationFile() != null )
            {
                out.write( openTag( XmlTagConstants.ADDITIONAL_DD_JAR_TAG, false, 2 ) );
                out.write( ejbGrp.getDDLocationFile() );
                out.write( closeTag( XmlTagConstants.ADDITIONAL_DD_JAR_TAG, 0 ) );
            }

            // All the enterprise beans
            out.write( openTag( XmlTagConstants.ENTERPRISE_BEANS_TAG, true, 2 ) );

            // Session beans;
            if( ejbGrp.getSessionBeans() != null && !ejbGrp.getSessionBeans().isEmpty() )
                writeOutSessionBeans( ejbGrp.getSessionBeans() );
            
            // entity beans;
            
            // message driven beans

            out.write( closeTag( XmlTagConstants.ENTERPRISE_BEANS_TAG, 2 ) );
            
            // end of this group
            out.write( closeTag( XmlTagConstants.EJB_GROUP_TAG,  1 ) );
        }
        
        out.write( XML_END );
    }
    
    private void writeOutSessionBeans( Collection sessionBeans ) throws java.io.IOException
    {
        for( Iterator iter = sessionBeans.iterator(); iter.hasNext(); )
        {
            EjbInfo info = (EjbInfo)iter.next();
            
            String sessionTag = XmlTagConstants.STATELESS_SESSION_TAG;
            if( info.getBeanType() == EjbInfo.STATEFUL_SESSION_BEAN )
                sessionTag = XmlTagConstants.STATEFUL_SESSION_TAG;
            
            out.write( openTag( sessionTag, true, 3 ) );
            
            out.write( openTag( XmlTagConstants.JNDI_NAME_TAG, false, 4 ) );
            out.write( info.getJNDIName() );
            out.write( closeTag( XmlTagConstants.JNDI_NAME_TAG,  0 ) );
            
            out.write( openTag( XmlTagConstants.EJB_NAME_TAG, false,  4 ) );
            out.write( info.getEjbName() );
            out.write( closeTag( XmlTagConstants.EJB_NAME_TAG, 0 ) );
            
            out.write( openTag( XmlTagConstants.HOME_TAG_TAG, false, 4 ) );
            out.write( info.getHomeInterfaceName() );
            out.write( closeTag( XmlTagConstants.HOME_TAG_TAG, 0 ) );
            
            out.write( openTag( XmlTagConstants.REMOTE_TAG, false, 4 ) );
            out.write( info.getCompInterfaceName() );
            out.write( closeTag( XmlTagConstants.REMOTE_TAG, 0 ) );
            
            out.write( openTag( XmlTagConstants.WEB_EJB_REF_TAG, false, 4 ) );
            out.write( info.getWebEjbRef() );
            out.write( closeTag( XmlTagConstants.WEB_EJB_REF_TAG, 0 ) );
            
            out.write( openTag( XmlTagConstants.WRAPPER_BEAN_TAG, false, 4 ) );
            out.write( info.getBeanWrapperName() );
            out.write( closeTag( XmlTagConstants.WRAPPER_BEAN_TAG, 0 ) );
            
            out.write( openTag( XmlTagConstants.WRAPPER_BEAN_INFO_TAG, false, 4 ) );
            out.write( info.getBeanInfoWrapperName() );
            out.write( closeTag( XmlTagConstants.WRAPPER_BEAN_INFO_TAG, 0 ) );
            
            if( info.getMethods() != null && !info.getMethods().isEmpty() )
                writeOutMethods( info.getMethods() );;
            
            out.write( closeTag( sessionTag, 3 ) );
        }
    }
    
    private void writeOutMethods( Collection methodInfos ) throws java.io.IOException
    {
        for( Iterator iter = methodInfos.iterator(); iter.hasNext(); )
        {
            MethodInfo mInfo = (MethodInfo)iter.next();
            
            String methodTag = XmlTagConstants.METHOD_TAG;
            if( !mInfo.isBusinessMethod() )
                methodTag = XmlTagConstants.CREATE_METHOD_TAG;
                
            out.write( openTag( methodTag, true,  4 ) );
            
            out.write( openTag( XmlTagConstants.METHOD_NAME_TAG, false, 5 ) );
            out.write( mInfo.getName() );
            out.write( closeTag( XmlTagConstants.METHOD_NAME_TAG, 0 ) );
            
            // Return-type
            Map returnTypeAttrMap = new HashMap();
            
            // RETURN_IS_COLLECTION_ATTR and  RETURN_ELEM_TYPE_ATTR are new in Thresher. 
            // In Thresher, because of the data provider support, we need to remember whether
            // the turn type is a collection. If it is, then the element class name needs to be remembered too
            if( mInfo.getReturnType().isCollection() )
            {
                returnTypeAttrMap.put( XmlTagConstants.RETURN_IS_COLLECTION_ATTR, "true" );
                
                if( mInfo.getReturnType().getElemClassName() != null )
                    returnTypeAttrMap.put( XmlTagConstants.RETURN_ELEM_TYPE_ATTR,  mInfo.getReturnType().getElemClassName() );
            }
            else
                returnTypeAttrMap.put( XmlTagConstants.RETURN_IS_COLLECTION_ATTR, "false" );
            
            out.write( openTag( XmlTagConstants.RETURN_TYPE_TAG, returnTypeAttrMap, false, 5 ) );
            out.write( mInfo.getReturnType().getClassName() );
            out.write( closeTag( XmlTagConstants.RETURN_TYPE_TAG, 0 ) );
            
            // Parameters
            // PARAM_NAME_ATTR is new in Thresher
            // In Thresher, we allow the user to enter/modify the parameter method names
            ArrayList parameters = mInfo.getParameters();
            if( parameters != null && !parameters.isEmpty() )
            {               
                for( int i = 0; i < parameters.size(); i ++ )
                {
                    MethodParam p = (MethodParam)parameters.get(i);
                    
                    Map nameAttr = new HashMap();
                    nameAttr.put( XmlTagConstants.PARAM_NAME_ATTR,  p.getName() );
                    
                    out.write( openTag( XmlTagConstants.PARAMETER_TAG, nameAttr, false, 5 ) );
                    out.write( p.getType() );
                    out.write( closeTag( XmlTagConstants.PARAMETER_TAG, 0 ) );
                }
            }
            
            // Exceptions
            ArrayList exceptions = mInfo.getExceptions();
            if( exceptions != null && !exceptions.isEmpty() )
            {
                for( int i = 0; i < exceptions.size(); i ++ )
                {
                    out.write( openTag( XmlTagConstants.EXCEPTION_TAG, false, 5 ) );
                    out.write( (String)exceptions.get(i) );
                    out.write( closeTag( XmlTagConstants.EXCEPTION_TAG, 0 ) );
                }
            }
            
            // Data privider is new in Thresher
            if( mInfo.getDataProvider() != null ) 
            {
                out.write( openTag( XmlTagConstants.DATAPROVIDER_TAG, false, 5 ) );
                out.write( mInfo.getDataProvider() );
                out.write( closeTag( XmlTagConstants.DATAPROVIDER_TAG, 0 ) );
            }
                        
            out.write( closeTag( methodTag, 4 ) );
        }
    }
    
     private String openTag( String tagName, boolean newLine, int indentLevel )
     {
         return openTag( tagName, null, newLine, indentLevel );
     }
    
     private String openTag( String tagName, Map attributes, boolean newLine, int indentLevel )
     {
        StringBuffer buf = new StringBuffer();
        
        // Indent INDENT_SPACES for each level
        for( int i = 0; i < indentLevel*INDENT_SPACES; i ++ )
            buf.append( " " );
        
        buf.append( "<" );
        buf.append( tagName );
        
        // Attributes
        if( attributes != null )
        {
            for( Iterator iter = attributes.entrySet().iterator(); iter.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)iter.next();

                buf.append( " " );
                buf.append( (String)entry.getKey() );
                buf.append( "=\"" );
                buf.append( (String)entry.getValue() );
                buf.append( "\"" );
            }
        }
        
        buf.append( ">" );
        
        if( newLine )
            buf.append( "\n" );
        
        return buf.toString();
    }
    private String closeTag( String tagName, int indentLevel )
    {
        StringBuffer buf = new StringBuffer();
        
        for( int i = 0; i < indentLevel*INDENT_SPACES; i ++ )
            buf.append( " " );
        
        buf.append( "</" );
        buf.append( tagName );
        buf.append( ">" );
        buf.append( "\n" );
        
        return buf.toString();
    }
    
}
