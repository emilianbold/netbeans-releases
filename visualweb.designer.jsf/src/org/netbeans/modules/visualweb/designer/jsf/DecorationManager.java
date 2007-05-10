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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.designer.jsf;


import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.modules.visualweb.spi.designer.DecorationProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;


/**
 * Manages the <code>DecorationProvider</code>s, and provides
 * decoration services for <code>CssBox</code>.
 *
 * @author Peter Zavadsky
 */
public class DecorationManager {

    /** Name of <code>decorationProviders</code> property. */
    public static final String PROP_DECORATION_PROVIDERS = "decorationProviders"; // NOI18N

    private static final DecorationManager instance = new DecorationManager();


    private final Lookup.Result<DecorationProvider> lookupResult;

    private final LookupListener decorationProvidersListener = new DecorationProvidersListener(this);

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);


    /** Creates a new instance of DecorationManager */
    private DecorationManager() {
        lookupResult = Lookup.getDefault().lookup(new Lookup.Template<DecorationProvider>(DecorationProvider.class));
        lookupResult.addLookupListener(
                (LookupListener)WeakListeners.create(LookupListener.class, decorationProvidersListener, lookupResult));
    }

    public static DecorationManager getDefault() {
        return instance;
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    public Decoration getDecoration(Element element) {
        DecorationProvider[] decorationProviders = getDecorationProviders();
//        if (decorationProviders.length == 0) {
//            return null;
//        } else {
//            return decorationProviders[0].getDecoration(element);
//        }
        for (DecorationProvider decorationProvider : decorationProviders) {
            Decoration decoration = decorationProvider.getDecoration(element);
            if (decoration != null) {
                return decoration;
            }
        }
        return null;
    }

    private void fireDecorationProvidersPropertyChange() {
        support.firePropertyChange(PROP_DECORATION_PROVIDERS, null, getDecorationProviders());
    }

    public DecorationProvider[] getDecorationProviders() {
        Collection<? extends DecorationProvider> decorationProviders = lookupResult.allInstances();
        return decorationProviders.toArray(new DecorationProvider[decorationProviders.size()]);
    }


    private static class DecorationProvidersListener implements LookupListener {
        private final DecorationManager decorationManager;

        public DecorationProvidersListener(DecorationManager decorationManager) {
            this.decorationManager = decorationManager;
        }

        public void resultChanged(LookupEvent lookupEvent) {
            decorationManager.fireDecorationProvidersPropertyChange();
        }
    } // End of DecorationProvidersListener.
}
