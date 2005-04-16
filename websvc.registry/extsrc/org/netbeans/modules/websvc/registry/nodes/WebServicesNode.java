/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.nodes;

// FIXME CUT-PASTE FROM RAVE import com.sun.rave.palette.PaletteItemDataFlavor;
// FIXME CUT-PASTE FROM RAVE import com.sun.rave.project.model.Project;
// FIXME CUT-PASTE FROM RAVE import com.sun.rave.toolbox.PaletteItemButton;


import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.ExTransferable.Single;

// FIXME CUT-PASTE FROM RAVE import org.netbeans.modules.websvc.registry.actions.AddToFormAction;
import org.netbeans.modules.websvc.registry.actions.DeleteWebServiceAction;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
// FIXME CUT-PASTE FROM RAVE import org.openide.cookies.PaletteItemSetCookie;



/**
 * Represents a deployed web service. This is a dynamic leaf node and
 * none may be shown initially, unless we decide to persist the nodes
 * as links between Rave sessions
 *
 * @author octav, Winston Prakash
 */
public class WebServicesNode extends AbstractNode implements WebServicesCookie { // FIXME CUT-PASTE FROM RAVE implements PaletteItemSetCookie  {
    
    private WebServiceData websvcData;
    /** a handle to the current project */
// FIXME CUT-PASTE FROM RAVE    private static Project project = null;
    
    public WebServicesNode() {
        this(null);
    }
    
    public WebServiceData getWebServiceData() {
        return websvcData;
    }
    
    public WebServicesNode(WebServiceData wsData) {
        super(new WebServicesNodeChildren(wsData));
        websvcData = wsData;
        setName(wsData.getDisplayName());
        setShortDescription(wsData.getWSDescription());
		getCookieSet().add(this);
    }
    
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservice.png");
    }
    
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/webservice.png");
    }
    
    // Create the popup menu:
//    protected SystemAction[] createActions() {
//        return new SystemAction[] {
//            SystemAction.get(AddToFormAction.class),
//            SystemAction.get(DeleteWebServiceAction.class)
//        };
//    }
	
	public Action[] getActions(boolean context) {
        return new SystemAction[] {
// FIXME CUT-PASTE FROM RAVE            SystemAction.get(AddToFormAction.class),
            SystemAction.get(DeleteWebServiceAction.class)
        };
	}
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     * Create a property sheet for the individual W/S node
     * @return property sheet for the data source nodes
     */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "WS_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "WS_INFO"));
            sheet.put(ss);
        }
        
        try {
            Reflection p;
            
            p = new Reflection(websvcData, String.class, "getName", null); // NOI18N
            p.setName("name"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "WS_NAME"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "WS_NAME"));
            ss.put(p);
            
            p = new Reflection(websvcData, Integer.class, "getWebServicePort", null); // NOI18N
            p.setName("port"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "WS_PORT"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "WS_PORT"));
            ss.put(p);
            
            p = new Reflection(websvcData, String.class, "getURL", null); // NOI18N
            p.setName("URL"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "WS_URL"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "WS_URL"));
            ss.put(p);
            
            p = new Reflection(websvcData, String.class, "getWebServiceAddress", null); // NOI18N
            p.setName("Web Service Address"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WebServicesNode.class, "WS_ADDRESS"));
            p.setShortDescription(NbBundle.getMessage(WebServicesNode.class, "WS_ADDRESS"));
            ss.put(p);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        }
        
        return sheet;
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException{
        WebServiceListModel wsListModel = WebServiceListModel.getInstance();
        wsListModel.removeWebService(websvcData.getId());
        /**
         * Also remove the jar file for this webservice.
         *
         * NOTE- this code will handle the design where there is one jar file
         * per webservice.  If the design is changed so that a multiple-service
         * WSDL causes only one jar file to be shared by multiple services, this
         * code will need to be changed.
         * - David Botterill 3/22/2004
         */
        if(null != websvcData.getProxyJarFileName()) {
            String jarFileName = websvcData.getProxyJarFileName();
			// !PW 49707 proxyJarFileName can be (always is?) an absolute path.
			File wsJarFile = new File(jarFileName);
			if(wsJarFile.getParentFile() == null || !wsJarFile.exists()) {
				wsJarFile = new File(new File(System.getProperty("netbeans.user"), "websvc/"), wsJarFile.getName());
			}
            wsJarFile.delete();
        }
        
        super.destroy();
    }
    
    // Handle Drag and Drop (not used for copy / paste at this point)
    public boolean canCopy() {
        return true;
    }
    
    public boolean canCut() {
        return true;
    }
    
    public Transferable clipboardCopy() {
        // Add to, do not replace, the default node copy flavor:
        try {
            ExTransferable et = ExTransferable.create(super.clipboardCopy());
/* FIXME CUT-PASTE FROM RAVE
			DataFlavor supportedFlavor = new PaletteItemDataFlavor();
            et.put(new Single(supportedFlavor) {
                protected Object getData() {
                    return new WebServicePaletteItem(websvcData);
                }
            });

            // For an explanation of why this hack is necessary, see
            //  {@link com.sun.rave.toolbox.PaletteItemButton.mostRecentTransferable}.
            PaletteItemButton.mostRecentTransferable = et;
 */

            return et;
        } catch (IOException ioe) {
            System.err.println("WSLeaf.clipboardCopy: Error");
            ioe.printStackTrace();
            return null;
        }
    }
    
    
    // Permit user to customize whole node at once (instead of per-property):
    /*
    public boolean hasCustomizer() {
        return true;
    }
    public Component getCustomizer() {
        return new MyCustomizingPanel(this);
    }
     */
    
    // -- implements PaletteItemSetCookie
/* FIXME CUT-PASTE FROM RAVE
	public boolean hasPaletteItems() {
        return true;
    }
    
    // -- implements PaletteItemSetCookie
    public String[] getClassNames() {
        return new String[0];
    }
*/

    public Cookie getCookie (Class type) {
/* FIXME CUT-PASTE FROM RAVE
        if (type == PaletteItemSetCookie.class) {
            // Don't know why this wasn't automatic - I implement
            // Node.Cookie. This is automatic for data objects - not for
            // nodes I guess?
            return this;
        } else {
            return super.getCookie(type);
        }
 */
		return super.getCookie(type);
    }
}


