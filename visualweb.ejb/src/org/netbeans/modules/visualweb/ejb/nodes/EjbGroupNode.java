/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * EjbGroupNode.java
 *
 * Created on May 3, 2004, 10:54 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.actions.ConfigureMethodsAction;
import org.netbeans.modules.visualweb.ejb.actions.DeleteEjbGroupAction;
import org.netbeans.modules.visualweb.ejb.actions.ExportEjbDataSourceAction;
import org.netbeans.modules.visualweb.ejb.actions.ModifyEjbGroupAction;
import org.netbeans.modules.visualweb.ejb.actions.RefreshEjbGroupAction;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet.Set;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * This node represents one EJB Group
 *
 * @author cao
 */
public class EjbGroupNode extends AbstractNode implements PropertyChangeListener, Node.Cookie {
    private EjbGroup ejbGroup;
    
    /** Creates a new instance of EjbGroupNode */
    public EjbGroupNode(EjbGroup ejbGroup) {
        super( new EjbGroupNodeChildren( ejbGroup ) );
        this.ejbGroup = ejbGroup;
        
        setName( ejbGroup.getName() );
        setDisplayName( ejbGroup.getName() );
        setShortDescription( ejbGroup.getName() );
    }
    
    // Create the popup menu for the EJB group node
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get( RefreshEjbGroupAction.class),
            SystemAction.get( ModifyEjbGroupAction.class),
            SystemAction.get( DeleteEjbGroupAction.class),
            SystemAction.get( ConfigureMethodsAction.class),
            null,
            SystemAction.get( ExportEjbDataSourceAction.class),
        };
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
    }
    
    public Image getIcon(int type){
        Image image1 = ImageUtilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/ejbSetFolder.png");
        Image image2 = ImageUtilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/archive_container_8x8.png");
        int x = image1.getWidth(null) - image2.getWidth(null);
        int y = image1.getHeight(null) - image2.getHeight(null);
        return ImageUtilities.mergeImages( image1, image2, x, y);
    }
    
    public Image getOpenedIcon(int type){
        Image image1 = ImageUtilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/ejbSetOpenFolder.png");
        Image image2 = ImageUtilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/archive_container_8x8.png");
        int x = image1.getWidth(null) - image2.getWidth(null);
        int y = image1.getHeight(null) - image2.getHeight(null);
        return ImageUtilities.mergeImages( image1, image2, x, y);
    }
    
    protected EjbGroupNodeChildren getEjbGroupNodeChildren() {
        return (EjbGroupNodeChildren)getChildren();
    }
    
    public EjbGroup getEjbGroup() {
        return this.ejbGroup;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("ejbGroup"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("ejbGroup");  // NOI18N
            ss.setDisplayName( NbBundle.getMessage(EjbGroupNode.class, "EJB_GROUP_INFORMATION") );
            ss.setShortDescription( NbBundle.getMessage(EjbGroupNode.class, "EJB_GROUP_INFORMATION") );
            sheet.put(ss);
        }
        
        // Name is read-writable.
        // Need to make sure name is not empty
        ss.put( new PropertySupport.ReadWrite( "name", // NOI18N
                String.class,
                NbBundle.getMessage(EjbGroupNode.class, "EJB_GROUP_NAME"),
                NbBundle.getMessage(EjbGroupNode.class, "EJB_GROUP_NAME") ) {
            public Object getValue() {
                return ejbGroup.getName();
            }
            
            public void setValue(Object val) {
                
                StringBuffer msg = new StringBuffer();
                boolean valid = true;
                if( ((String)val) == null || ((String)val).length() == 0 ) {
                    msg.append( NbBundle.getMessage(EjbGroupNode.class, "EMPTY_GROUP_NAME") );
                    valid = false;
                } else {
                    // Check uniqueness
                    if( EjbDataModel.getInstance().getEjbGroup( (String)val ) != null ) {
                        msg.append( NbBundle.getMessage(EjbGroupNode.class, "NAME_NOT_UNIQUE", "\'" + (String)val + "\'" ) );
                        valid = false;
                    }
                }
                
                if( !valid ) {
                    NotifyDescriptor d = new NotifyDescriptor.Message( msg.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify( d );
                } else {
                    if( !ejbGroup.getName().equals( (String)val ) ) {
                        ejbGroup.setName( (String)val );
                        EjbDataModel.getInstance().touchModifiedFlag();
                    }
                }
            }
            
        });
        
        // App Server type is read only
        ss.put( new PropertySupport.ReadOnly( "containerType", // NOI18N
                String.class,
                NbBundle.getMessage(EjbGroupNode.class, "APP_SERVER_TYPE"),
                NbBundle.getMessage(EjbGroupNode.class, "APP_SERVER_TYPE") ) {
            public Object getValue() {
                return ejbGroup.getAppServerVendor();
            }
        });
        
        // server host is read-writable
        // Need to make sure the server host is not empty and do not contain any spaces
        ss.put( new PropertySupport.ReadWrite( "serverHost", // NOI18N
                String.class,
                NbBundle.getMessage(EjbGroupNode.class, "APP_SERVER_HOST"),
                NbBundle.getMessage(EjbGroupNode.class, "APP_SERVER_HOST") ) {
            public Object getValue() {
                return ejbGroup.getServerHost();
            }
            
            public void setValue(Object val) {
                
                // Make sure the server host is valid
                StringBuffer msg = new StringBuffer();
                boolean valid = true;
                if( val == null || ((String)val).length() == 0 ) {
                    valid = false;
                    msg.append( NbBundle.getMessage(EjbGroupNode.class, "EMPTY_SERVER_HOST") );
                } else if( ((String)val).trim().indexOf( " " ) != -1 ) {
                    valid = false;
                    msg.append( NbBundle.getMessage(EjbGroupNode.class, "SPACES_IN_SERVER_HOST", "\'" + (String)val + "\'" ) );
                    
                }
                
                if( !valid ) {
                    NotifyDescriptor d = new NotifyDescriptor.Message( msg.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify( d );
                } else{
                    
                    // One more check, in case of the same client jar file is added in  more than
                    // one ejb group, warn the user to modify the other groups too
                    EjbGroup cloneGrp = (EjbGroup)ejbGroup.clone();
                    cloneGrp.setServerHost( (String)val );
                    
                    if( checkClientJarInfo( cloneGrp,  true ) ) {
                        if( !ejbGroup.getServerHost().equals( (String)val ) ) {
                            ejbGroup.setServerHost( (String)val );
                            EjbDataModel.getInstance().touchModifiedFlag();
                        }
                    }
                }
            }
        });
        
        // IIOP Port is read-writable.
        // Need to make sure the iiop is not empty and it is a number
        ss.put( new PropertySupport.ReadWrite( "iiopPort", // NOI18N
                String.class,
                NbBundle.getMessage(EjbGroupNode.class, "IIOP_PORT"),
                NbBundle.getMessage(EjbGroupNode.class, "IIOP_PORT") ) {
            public Object getValue() {
                return Integer.toString( ejbGroup.getIIOPPort() );
            }
            
            public void setValue(Object val) {
                
                // Make sure the server host is valid
                StringBuffer msg = new StringBuffer();
                boolean valid = true;
                if( val == null || ((String)val).length() == 0 ) {
                    valid = false;
                    msg.append( NbBundle.getMessage(EjbGroupNode.class, "EMPTY_IIOP_PORT" ) );
                } else { // Make it is a number
                    try {
                        int portNum = Integer.parseInt( (String)val );
                    } catch( NumberFormatException ex ) {
                        valid = false;
                        msg.append( NbBundle.getMessage(EjbGroupNode.class, "IIOP_PORT_NOT_NUMBER" ) );
                    }
                }
                
                if( !valid ) {
                    NotifyDescriptor d = new NotifyDescriptor.Message( msg.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify( d );
                } else{
                    
                    // One more check, in case of the same client jar file is added in  more than
                    // one ejb group, warn the user to modify the other groups too
                    EjbGroup cloneGrp = (EjbGroup)ejbGroup.clone();
                    cloneGrp.setIIOPPort( Integer.parseInt( (String)val ) );
                    
                    if( checkClientJarInfo( cloneGrp, false ) ) {
                        if( ejbGroup.getIIOPPort() != Integer.parseInt( (String)val ) ) {
                            ejbGroup.setIIOPPort( Integer.parseInt( (String)val ) );
                            EjbDataModel.getInstance().touchModifiedFlag();
                        }
                    }
                }
            }
        });
        
        // Client jars are read only
        ss.put( new PropertySupport.ReadOnly( "clientJarFileNames", // NOI18N
                String.class,
                NbBundle.getMessage(EjbGroupNode.class, "CLIENT_JARS"),
                NbBundle.getMessage(EjbGroupNode.class, "CLIENT_JARS") ) {
            public Object getValue() {
                return ejbGroup.getClientJarFilesAsOneStr();
            }
        });
        
        // The additional ear/jar for DDs. Read only
        ss.put( new PropertySupport.ReadOnly( "ddLocationFileName", // NOI18N
                String.class,
                NbBundle.getMessage(EjbGroupNode.class, "DD_JAR_FILE"),
                NbBundle.getMessage(EjbGroupNode.class, "DD_JAR_FILE") ) {
            public Object getValue() {
                
                if( ejbGroup.getDDLocationFile() == null )
                    return NbBundle.getMessage(EjbGroupNode.class, "NONE");
                else
                    return ejbGroup.getDDLocationFile();
            }
        });
        
        // Need to listen on the properties change
        // When the name gets changed, we need to change the node name to the new name
        ejbGroup.addPropertyChangeListener( (PropertyChangeListener)this );
        
        return sheet;
    }
    
    private boolean checkClientJarInfo( EjbGroup grp, boolean isServerHost ) {
        ArrayList grpNames = new ArrayList();
        
        for( Iterator iter = grp.getClientJarFileNames().iterator(); iter.hasNext(); ) {
            String jar = (String)iter.next();
            
            Collection grps = EjbDataModel.getInstance().findEjbGroupsForJar( jar);
            
            for( Iterator grpIter = grps.iterator(); grpIter.hasNext(); ) {
                EjbGroup existingGrpWithJar = (EjbGroup)grpIter.next();
                
                if( existingGrpWithJar != null &&
                        ( !existingGrpWithJar.getServerHost().equals( grp.getServerHost() ) ||
                        existingGrpWithJar.getIIOPPort() != grp.getIIOPPort() ) ) {
                    if( !grpNames.contains( existingGrpWithJar.getName() ) &&
                            !existingGrpWithJar.getName().equals( grp.getName() ) ) //Not itself
                        grpNames.add( existingGrpWithJar.getName() );
                }
            }
        }
        
        if( grpNames.size() != 0 ) {
            // The server host and/or RMI-IIOP port modification will cause EJB Set {0} to
            // contain incorrect information. Would like to preceed?
            StringBuffer nameStr = new StringBuffer();
            boolean first = true;
            for( Iterator iter = grpNames.iterator(); iter.hasNext(); ) {
                if( first )
                    first = false;
                else
                    nameStr.append( ", " );
                
                nameStr.append( (String)iter.next() );
                
            }
            String msg = null;
            
            if( isServerHost )
                msg = NbBundle.getMessage( EjbGroupNode.class, "MISMATH_HOST_INFO_JAR_Q", nameStr.toString() );
            else
                msg = NbBundle.getMessage( EjbGroupNode.class, "MISMATH_PORT_INFO_JAR_Q", nameStr.toString() );
            
            NotifyDescriptor confDialog = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);
            if( !(DialogDisplayer.getDefault().notify(confDialog) == NotifyDescriptor.OK_OPTION) )
                return false;
            else
                return true;
        } else
            return true;
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException{
        
        EjbDataModel.getInstance().removeEjbGroup( ejbGroup );
        super.destroy();
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        // Set the name to the new name the user just change to
        setName( (String)evt.getNewValue() );
    }
}
