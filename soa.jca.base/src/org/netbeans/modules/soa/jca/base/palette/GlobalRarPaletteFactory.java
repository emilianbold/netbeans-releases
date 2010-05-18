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
package org.netbeans.modules.soa.jca.base.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * palette factory class
 *
 * @author echou
 */
public final class GlobalRarPaletteFactory {

    private GlobalRarPaletteFactory() {}

    /**
    * Creates a new soa palette.
    * @return a new soa palette
    */
    public static PaletteController getPalette() {
        if (ourPalette == null) {
            try {
                ourPalette = PaletteFactory.createPalette(
                    RAR_PALETTE_FOLDER,
                    new GlobalRarPaletteActions(),
                    new GlobalRarPaletteFilter(),
                    new GlobalRarPaletteDragAndDropHandler());

                TopComponent.Registry registry = TopComponent.getRegistry();
                registry.addPropertyChangeListener(new TopComponentPropertyChangeListener());

          } catch (IOException e) {
              e.printStackTrace();
          }
      }

      return ourPalette;
    }

    static class TopComponentPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if(TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {
                TopComponent.Registry registry = TopComponent.getRegistry();
                TopComponent tc = registry.getActivated();
                if(tc != null) {
                    Lookup lkup = tc.getLookup();
                    if(lkup != null) {
                        DataObject dObj = lkup.lookup(DataObject.class);

                        if(dObj != null) {
                            String ext = dObj.getPrimaryFile().getExt();
                            if(ext != null) {
                                ext = ext.toLowerCase();
                                if(ext.equals("java") ) { // NOI18N
                                    ourPalette.refresh();
                                }
                            }
                        }
                    }

                }
            }
        }

    }

    private static final String RAR_PALETTE_FOLDER = "CAPSEEPalette"; // NOI18N
    private static PaletteController ourPalette;

}
