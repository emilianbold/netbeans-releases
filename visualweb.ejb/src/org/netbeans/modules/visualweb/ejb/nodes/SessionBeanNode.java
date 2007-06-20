/*
 * SessionBeanNode.java
 *
 * Created on May 3, 2004, 6:20 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.Action;

import org.netbeans.modules.visualweb.ejb.actions.AddSessionBeanToPageAction;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.util.Util;
import org.openide.ErrorManager;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.impl.BasicBeanCreateInfo;

/**
 * This node is to represent one session bean
 * 
 * @author cao
 */
public class SessionBeanNode extends AbstractNode implements Node.Cookie {
    private EjbGroup ejbGroup;

    private EjbInfo ejbInfo;

    // private SessionBeanPaletteItem beanPaletteItem;

    /** Creates a new instance of SessionBeanNode */
    public SessionBeanNode(EjbGroup ejbGroup, EjbInfo ejbInfo) {
        super(new SessionBeanNodeChildren(ejbGroup, ejbInfo));

        this.ejbGroup = ejbGroup;
        this.ejbInfo = ejbInfo;

        // Set FeatureDescriptor stuff:
        setName(ejbInfo.getJNDIName());
        setDisplayName(ejbInfo.getJNDIName());
        setShortDescription(ejbInfo.getCompInterfaceName());
    }

    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/session_bean.png");
    }

    public Image getOpenedIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/session_bean.png");
    }

    // Create the popup menu for the session bean node
    public Action[] getActions(boolean context) {
        // todo I don't know what actions to popup from this node yet
        return new Action[] { SystemAction.get(AddSessionBeanToPageAction.class),
                SystemAction.get(PropertiesAction.class), };
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
    }

    protected SessionBeanNodeChildren getSessionBeanNodeChildren() {
        return (SessionBeanNodeChildren) getChildren();
    }

    public EjbInfo getEjbInfo() {
        return this.ejbInfo;
    }

    // Properties sheet
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("ejbInfo"); // NOI18N

        if (ss == null) {
            ss = new Set();
            ss.setName("ejbInfo"); // NOI18N
            ss.setDisplayName(NbBundle
                    .getMessage(SessionBeanNode.class, "SESSION_BEAN_INFORMATION"));
            ss.setShortDescription(NbBundle.getMessage(SessionBeanNode.class,
                    "SESSION_BEAN_INFORMATION"));
            sheet.put(ss);
        }

        // EJB type - stateful or stateless
        ss.put(new PropertySupport.ReadOnly("beanType", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "EJB_TYPE"), NbBundle
                        .getMessage(EjbGroupNode.class, "EJB_TYPE")) {
            public Object getValue() {
                switch (ejbInfo.getBeanType()) {
                case EjbInfo.STATEFUL_SESSION_BEAN:
                    return NbBundle.getMessage(EjbGroupNode.class, "STATEFUL_EJB");
                case EjbInfo.STATELESS_SESSION_BEAN:
                    return NbBundle.getMessage(EjbGroupNode.class, "STATELESS_EJB");
                default:
                    return NbBundle.getMessage(EjbGroupNode.class, "STATELESS_EJB");
                }
            }
        });

        // JNDI name
        ss.put(new PropertySupport.ReadOnly("jndiName", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "JNDI_NAME"), NbBundle
                        .getMessage(EjbGroupNode.class, "JNDI_NAME")) {
            public Object getValue() {
                return ejbInfo.getJNDIName();
            }
        });

        // ejb-ref-name
        ss.put(new PropertySupport.ReadOnly("webEjbRef", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "WEB_EJB_REF_NAME"), NbBundle
                        .getMessage(EjbGroupNode.class, "WEB_EJB_REF_NAME")) {
            public Object getValue() {
                return ejbInfo.getWebEjbRef();
            }
        });

        // Home interface
        ss.put(new PropertySupport.ReadOnly("homeInterface", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "HOME_INTERFACE"), NbBundle
                        .getMessage(EjbGroupNode.class, "HOME_INTERFACE")) {
            public Object getValue() {
                return ejbInfo.getHomeInterfaceName();
            }
        });

        // Remote interface
        ss.put(new PropertySupport.ReadOnly("remoteInterface", // NOI18N
                String.class, NbBundle.getMessage(EjbGroupNode.class, "REMOTE_INTERFACE"), NbBundle
                        .getMessage(EjbGroupNode.class, "REMOTE_INTERFACE")) {
            public Object getValue() {
                return ejbInfo.getCompInterfaceName();
            }
        });

        return sheet;
    }

    // Methods for Drag and Drop (not used for copy / paste at this point)

    public boolean canCopy() {
        return true;
    }

    public boolean canCut() {
        return true;
    }

    public Transferable clipboardCopy() {

        // // If the bean palette item is not initialized, lets create one
        // if( beanPaletteItem == null ) {
        // beanPaletteItem = new SessionBeanPaletteItem( ejbGroup, ejbInfo );
        // }
        if (ejbGroup == null || ejbInfo == null) {
            try {
                return super.clipboardCopy();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        // Add to, do not replace, the default node copy flavor:
        try {
            ExTransferable transferable = ExTransferable.create(super.clipboardCopy());
            // Now create the transferable
            transferable.put(
            // new
                    // ExTransferable.Single(PaletteItemTransferable.FLAVOR_PALETTE_ITEM)
                    // {
                    new ExTransferable.Single(FLAVOR_EJB_DISPLAY_ITEM) {
                        protected Object getData() {
                            // return new SessionBeanPaletteItem( ejbGroup,
                            // ejbInfo );
                            // return beanPaletteItem;
                            return new EjbBeanCreateInfo(ejbGroup, ejbInfo);
                        }
                    });
            return transferable;
        } catch (Exception ioe) {
            Util.getLogger().log(Level.SEVERE, "SessionBeanNode.clipboardCopy: Error", ioe);
            return null;
        }
    }

    private static final DataFlavor FLAVOR_EJB_DISPLAY_ITEM = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
            "Ejb Display Item"); // XXX Localize

    private static class EjbBeanCreateInfo extends BasicBeanCreateInfo {

        private final EjbGroup ejbGroup;

        private final EjbInfo ejbInfo;

        public EjbBeanCreateInfo(EjbGroup ejbGroup, EjbInfo ejbInfo) {
            this.ejbGroup = ejbGroup;
            this.ejbInfo = ejbInfo;
        }

        public String getBeanClassName() {
            // XXX This hack causes the jars to be added to the project if the
            // EJB node is even dragged onto the designer
            // without dropping
            try {
                EjbLibReferenceHelper.addEjbGroupToActiveProject(ejbGroup);
            } catch (Exception e) {
                Util.getLogger().log(Level.SEVERE, "Unable to add EJB Set to Project", e);
            }
            return ejbInfo.getBeanWrapperName();
        }

        public String getDisplayName() {
            return ejbInfo.getCompInterfaceName();
        }

    } // End of EjbBeanCreateInfo
}
