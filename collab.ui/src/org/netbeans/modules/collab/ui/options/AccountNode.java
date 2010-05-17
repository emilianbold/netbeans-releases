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
package org.netbeans.modules.collab.ui.options;

import java.awt.Image;
import java.beans.*;
import java.io.IOException;
import java.util.*;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.actions.*;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class AccountNode extends BeanNode implements Node.Cookie, PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final Image CHECK_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/check_png.gif"
        ); // NOI18M
    private static final Image EMPTY_IMAGE = Utilities.loadImage("org/openide/resources/actions/empty.gif"); // NOI18M
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(SetDefaultAccountAction.class), 
            //			SystemAction.get(ToggleAutoLoginAccountAction.class),
            null, SystemAction.get(CutAction.class), SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class), null, SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class), null, SystemAction.get(PropertiesAction.class),
        };

    ////////////////////////////////////////////////////////////////////////////
    // Static initializer
    ////////////////////////////////////////////////////////////////////////////
    static {
        // Add our own bean info search path, since we want to provide BeanInfo
        // for the Account class, but it's in another package. Note, this code 
        // goes here because this class may be referenced before others in the 
        // module (including DefaultUserInterface).  If, however, other classes 
        // also want to provide alternate BeanInfo, then this code may need to 
        // move to the first class likely to be loaded.
        final String BEAN_INFO_PATH = "org.netbeans.modules.collab.ui.beaninfo"; // NOI18N

        boolean found = false;

        String[] paths = Introspector.getBeanInfoSearchPath();

        for (int i = 0; i < paths.length; i++) {
            if (paths[i].equals(BEAN_INFO_PATH)) {
                found = true;

                break;
            }
        }

        if (!found) {
            List pathList = new ArrayList(Arrays.asList(paths));
            pathList.add(BEAN_INFO_PATH);
            Introspector.setBeanInfoSearchPath((String[]) pathList.toArray(new String[pathList.size()]));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Account account;
    private boolean wasDefault;
    private Image mergedIcon;

    /**
     *
     *
     */
    public AccountNode(Account account) throws IntrospectionException {
        super(account);
        this.account = account;

        //		setName(account.getDisplayName());
        systemActions = DEFAULT_ACTIONS;

        //		setDefaultAction(DEFAULT_ACTIONS[0]);
        HiddenCollabSettings.getDefault().addPropertyChangeListener(this);
        getCookieSet().add(this);
    }

    /**
     *
     *
     */
    public boolean isDefault() {
        try {
            return getAccount() == CollabManager.getDefault().getUserInterface().getDefaultAccount();
        } catch (Exception e) {
            Debug.debugNotify(e);

            return false;
        }
    }

    /**
     *
     *
     */
    public boolean isAutoLogin() {
        try {
            return CollabManager.getDefault().getUserInterface().isAutoLoginAccount(getAccount());
        } catch (Exception e) {
            Debug.debugNotify(e);

            return false;
        }
    }

    /**
     *
     *
     */
    public Account getAccount() {
        return account;
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AccountNode.class);
    }

    /**
     *
     *
     */
    public boolean canCut() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canCopy() {
        return true;
    }

    /**
     *
     *
     */
    public boolean canDestroy() {
        return true;
    }

    /**
     *
     *
     */
    public boolean canRename() {
        return true;
    }

    /**
     *
     *
     */
    public void destroy() throws IOException {
        super.destroy();
        AccountManager.getDefault().removeAccount(getAccount());
    }

    /**
     *
     *
     */
    public Image getIcon(int type) {
        if ((mergedIcon != null) && (isDefault() == wasDefault)) {
            return mergedIcon;
        }

        wasDefault = isDefault();

        Image icon = super.getIcon(type);

        if (
            (icon != null) && isDefault() &&
                ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16))
        ) {
            mergedIcon = Utilities.mergeImages(icon, CHECK_IMAGE, 16, 0);
        } else {
            // Create a merged image that is 32x16 to later accomodate the 
            // default icon. Otherwise, the text of the node will be truncated
            // when we set the default icon.  For whatever reason, icons in 
            // the options seem to be only 24 pixels wide instead of 32 like
            // in the explorer.
            mergedIcon = Utilities.mergeImages(icon, EMPTY_IMAGE, 16, 0);
        }

        return mergedIcon;
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof HiddenCollabSettings) {
            if (event.getPropertyName().equals(HiddenCollabSettings.PROP_DEFAULT_ACCOUNT_ID)) {
                fireIconChange();
            }
        }
    }
}
