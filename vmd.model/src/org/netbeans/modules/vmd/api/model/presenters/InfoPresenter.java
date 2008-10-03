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
package org.netbeans.modules.vmd.api.model.presenters;

import org.netbeans.modules.vmd.api.model.*;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.*;

/**
 * The info presenter is used for resolving display name, icon and editable name.
 * The values are resolved by Resolver and cached by the presenter.
 * When a document/component is changed according to the specified DesignEventFilter, the cache is cleared.
 *
 * @author David Kaspar
 */
public final class InfoPresenter extends DynamicPresenter {

    /**
     * The name type.
     */
    public enum NameType {
        PRIMARY, SECONDARY, TERTIARY
    }

    /**
     * The icon type.
     */
    public enum IconType {
        COLOR_16x16, COLOR_32x32, COLOR_48x48, COLOR_64x64
    }

    /**
     * Creates an info presenter for a specified resolver.
     * @param resolver the resolver of the info presenter
     * @return the info presenter
     */
    public static InfoPresenter create (Resolver resolver) {
        return new InfoPresenter (resolver);
    }

    private Resolver resolver;

    private String cachedPrimary;
    private String cachedSecondary;
    private String cachedTertiary;

    private Image cached16;
    private Image cached32;
    private Image cached48;
    private Image cached64;

    private InfoPresenter (Resolver resolver) {
        this.resolver = resolver;
    }

    protected void notifyAttached (DesignComponent component) {
    }

    protected void notifyDetached (DesignComponent component) {
    }

    protected DesignEventFilter getEventFilter () {
        return resolver.getEventFilter (getComponent ());
    }

    protected void designChanged (DesignEvent event) {
        cachedPrimary = cachedSecondary = cachedTertiary = null;
        cached16 = cached32 = cached48 = cached64 = null;
        firePresenterChanged ();
    }

    protected void presenterChanged (PresenterEvent event) {
    }

    /**
     * Returns a display name for a specific component and name type.
     * @param nameType the name type
     * @return the display name
     */
    public String getDisplayName (NameType nameType) {
        switch (nameType) {
            case PRIMARY:
                if (cachedPrimary == null)
                    cachedPrimary = resolver.getDisplayName (getComponent (), nameType);
                return cachedPrimary;
            case SECONDARY:
                if (cachedSecondary == null)
                    cachedSecondary = resolver.getDisplayName (getComponent (), nameType);
                return cachedSecondary;
            case TERTIARY:
                if (cachedTertiary == null)
                    cachedTertiary = resolver.getDisplayName (getComponent (), nameType);
                return cachedTertiary;
            default:
                throw new IllegalStateException ();
        }
    }

    /**
     * Returns whether a name is editable.
     * @return true, if editable
     */
    public boolean isEditable () {
        return resolver.isEditable (getComponent ());
    }

    /**
     * Returns an initial name usually used in an in-place editor.
     * @return the initial name; if null, then the in-place editor is not allowed
     */
    public String getEditableName () {
        return resolver.getEditableName (getComponent ());
    }

    /**
     * Sets a new name usually entered by an in-place editor.
     * @param enteredName the edited name
     */
    public void setEditableName (String enteredName) {
        resolver.setEditableName (getComponent (), enteredName);
    }

    /**
     * Returns an icon.
     * @param iconType the icon type
     * @return the icon
     */
    public Image getIcon (IconType iconType) {
        switch (iconType) {
            case COLOR_16x16:
                if (cached16 == null)
                    cached16 = resolver.getIcon (getComponent (), iconType);
                return cached16;
            case COLOR_32x32:
                if (cached32 == null)
                    cached32 = resolver.getIcon (getComponent (), iconType);
                return cached32;
            case COLOR_48x48:
                if (cached48 == null)
                    cached48 = resolver.getIcon (getComponent (), iconType);
                return cached48;
            case COLOR_64x64:
                if (cached64 == null)
                    cached64 = resolver.getIcon (getComponent (), iconType);
                return cached64;
            default:
                throw new IllegalStateException ();
        }
    }

    public static InfoPresenter createStatic (final String displayName, final String typeName, String iconResource) {
        return createStatic (displayName, typeName, null, iconResource);
    }

    public static InfoPresenter createStatic (final String displayName, final String typeName, final String toolTip, String iconResource) {
        return createStatic (displayName, typeName, toolTip, ImageUtilities.loadImage (iconResource));
    }

    public static InfoPresenter createStatic (final String displayName, final String typeName, final String toolTip, final Image icon) {
        return new InfoPresenter (new Resolver() {
            public DesignEventFilter getEventFilter (DesignComponent component) {
                return null;
            }

            public String getDisplayName (DesignComponent component, NameType nameType) {
                switch (nameType) {
                    case PRIMARY:
                        return displayName;
                    case SECONDARY:
                        return typeName;
                    case TERTIARY:
                        return toolTip;
                    default:
                        throw Debug.illegalState ();
                }
            }

            public boolean isEditable (DesignComponent component) {
                return false;
            }

            public String getEditableName (DesignComponent component) {
                throw new IllegalStateException ();
            }

            public void setEditableName (DesignComponent component, String enteredName) {
                throw new IllegalStateException ();
            }

            public Image getIcon (DesignComponent component, IconType iconType) {
                return IconType.COLOR_16x16.equals (iconType) ? icon : null;
            }
        });
    }

    public static String getHtmlDisplayName (DesignComponent component) {
        InfoPresenter presenter = component.getPresenter (InfoPresenter.class);
        if (presenter == null) {
            Debug.warning ("Missing InfoPresenter for: ", component); // NOI18N
            return null;
        }
        String primary = presenter.getDisplayName (InfoPresenter.NameType.PRIMARY);
        String secondary = presenter.getDisplayName (InfoPresenter.NameType.SECONDARY);
        return secondary != null ? primary + " <font color=\"#808080\">[" + secondary + "]" : primary; // NOI18N
    }
    
    public static String getDisplayName (DesignComponent component) {
        InfoPresenter presenter = component.getPresenter (InfoPresenter.class);
        if (presenter == null) {
            Debug.warning ("Missing InfoPresenter for: ", component); // NOI18N
            return null;
        }
        String primary = presenter.getDisplayName (InfoPresenter.NameType.PRIMARY);
        String secondary = presenter.getDisplayName (InfoPresenter.NameType.SECONDARY);
        return secondary != null ? primary +" [" + secondary + "]" : primary; // NOI18N
    }

    public static String getToolTip (DesignComponent component) {
        InfoPresenter presenter = component.getPresenter (InfoPresenter.class);
        if (presenter == null) {
            Debug.warning ("Missing InfoPresenter for: ", component); // NOI18N
            return null;
        }
        return getToolTip (presenter);
    }

    public static String getToolTip (InfoPresenter presenter) {
        String text = presenter.getDisplayName (NameType.TERTIARY);
        if (text != null)
            return text;
        return presenter.getDisplayName (NameType.SECONDARY);
    }

    public interface Resolver {

        /**
         * Returns an event filter for a component.
         * @param component the component
         * @return the event filter used by a info presenter where the resolver is attached
         */
        DesignEventFilter getEventFilter (DesignComponent component);

        /**
         * Returns a display name for a specific component and name type.
         * @param component the component
         * @param nameType the name type
         * @return the display name
         */
        String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType);

        /**
         * Returns whether a name is editable.
         * @return true, if editable
         */
        boolean isEditable (DesignComponent component);

        /**
         * Returns an initial name usually used in an in-place editor.
         * @param component the component
         * @return the initial name; if null, then the in-place editor is not allowed
         */
        String getEditableName (DesignComponent component);

        /**
         * Sets a new name usually entered by an in-place editor.
         * @param component the component
         * @param enteredName the edited name
         */
        void setEditableName (DesignComponent component, String enteredName);

        /**
         * Returns an icon.
         * @param component the component
         * @param iconType the icon type
         * @return the icon
         */
        Image getIcon (DesignComponent component, IconType iconType);

    }

}
