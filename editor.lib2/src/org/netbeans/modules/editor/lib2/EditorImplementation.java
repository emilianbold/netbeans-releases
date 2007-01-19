/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.lib2;

import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.EditorImplementationProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** This is provider of implementation. This package (org.netbeans.editor) 
 * represent editor core which can be used independently on the rest of NetBeans.
 * However this core needs access to higher level functionality like access
 * to localized bundles, access to settings storage, etc. which can be implemented
 * differently by the applications which uses this editor core. For this purpose
 * was created this abstract class and it can be extended with any other methods which
 * are more and more often required by core editor. Example implementation
 * of this provider can be found in org.netbeans.modules.editor package
 * 
 * @author David Konecny
 * @since 10/2001
 */

public final class EditorImplementation {

    private static final Logger LOG = Logger.getLogger(EditorImplementation.class.getName());
    private static final EditorImplementationProvider DEFAULT = new DefaultImplementationProvider();
    
    private static EditorImplementation instance = null;
    
    private static EditorImplementationProvider externalProvider = null;
    private Lookup.Result<EditorImplementationProvider> result = null;
    
    /** Returns currently registered provider */
    public static synchronized EditorImplementation getDefault() {
        if (instance == null) {
            instance = new EditorImplementation();
        }
        return instance;
    }

    /**
     * <p><b>IMPORTANT:</b> This method is here only for supporting the backwards
     * compatibility of the {@link org.netbeans.editor.DialogSupport} class.
     * 
     */
    public void setExternalProvider(EditorImplementationProvider provider) {
        this.externalProvider = provider;
    }
    
    /** Returns ResourceBundle for the given class.*/
    public ResourceBundle getResourceBundle(String localizer) {
        return getProvider().getResourceBundle(localizer);
    }

    /** This is temporary method which allows core editor to access
     * glyph gutter action. These actions are then used when user clicks
     * on glyph gutter. In next version this should be removed and redesigned
     * as suggested in issue #16762 */
    public Action[] getGlyphGutterActions(JTextComponent target) {
        return getProvider().getGlyphGutterActions(target);
    }

    /** Activates the given component or one of its ancestors.
     * @return whether the component or one of its ancestors was succesfuly activated
     * */
    public boolean activateComponent(JTextComponent c) {
        return getProvider().activateComponent(c);
    }

    private EditorImplementation() {
        result = Lookup.getDefault().lookup(
            new Lookup.Template<EditorImplementationProvider>(EditorImplementationProvider.class));
    }

    private EditorImplementationProvider getProvider() {
        if (externalProvider != null) {
            return externalProvider;
        } else {
            Collection<? extends EditorImplementationProvider> providers = result.allInstances();
            if (providers.isEmpty()) {
                LOG.warning("Can't find any EditorImplementationProvider; using default.");
                return DEFAULT;
            } else {
                return providers.iterator().next();
            }
        }
    }
    
    private static final class DefaultImplementationProvider implements EditorImplementationProvider {
        private static final Action [] NOACTIONS = new Action[0];
        
        public ResourceBundle getResourceBundle(String localizer) {
            return NbBundle.getBundle(localizer);
        }

        public Action[] getGlyphGutterActions(JTextComponent target) {
            return NOACTIONS;
        }

        public boolean activateComponent(JTextComponent c) {
            return false;
        }
    } // End of DefaultImplementationProvider class
}
