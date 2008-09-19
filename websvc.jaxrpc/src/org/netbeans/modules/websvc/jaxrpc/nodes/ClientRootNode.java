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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.filesystems.Repository;

import org.openide.nodes.CookieSet;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;

/** This is the root node for the graph of web services for which this module
 *  has been client-enabled (e.g. services this module is using.)
 *
 * @author Peter Williams
 */
public final class ClientRootNode extends AbstractNode {

    private static final Image WEB_SERVICES_BADGE = Utilities.loadImage( "org/netbeans/modules/websvc/jaxrpc/nodes/resources/webserviceclientgroup.png" ); // NOI18N
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;	
    private FileObject wsdlFolder;

    public ClientRootNode(FileObject wsdlFolder) throws DataObjectNotFoundException {
        super((wsdlFolder != null) ? new ClientViewChildren(wsdlFolder) : Children.LEAF, createLookup(wsdlFolder));
        this.wsdlFolder = wsdlFolder;
        initialize();
    }

    private void initialize() {
        // !PW add cookies to registered lookup -- see createLookup() below
        setName("WebServiceReferences"); // NOI18N
        setDisplayName(NbBundle.getBundle(ClientRootNode.class).getString("LBL_WebServiceReferences"));
    }

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }
        
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }
	
	public Action[] getActions(boolean context) {
		return new Action[] {
			org.netbeans.spi.project.ui.support.CommonProjectActions.newFileAction(),
			null,
			org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
			null,
			org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class ),
			null,
			org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class )
		};
	}

    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened, int type ) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = Utilities.mergeImages(image, WEB_SERVICES_BADGE, 7, 7 );
        return image;        
    }

    private static Lookup createLookup(FileObject wsdlFolder) {
        // !PW FIXME  -- from source -- Remove DataFolder when paste, find and refresh are reimplemented
		// !PW FIXME implement pathing and add PathFinder to lookup.
		if(wsdlFolder != null) {
	        DataFolder dataFolder = DataFolder.findFolder(wsdlFolder);        
		    return Lookups.fixed(new Object[]{ dataFolder /*, new PathFinder( group ) */ });
		} else {
		    return Lookups.fixed(new Object[]{ /* new PathFinder( group ) */ });
		}
    }
}
