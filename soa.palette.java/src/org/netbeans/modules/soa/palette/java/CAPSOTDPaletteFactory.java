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

package org.netbeans.modules.soa.palette.java;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author gpatil
 */
public class CAPSOTDPaletteFactory {
    private static PaletteController palette = null;    
    private static final String OTD_PALETTE_FOLDER = "CAPSEEPalette"; // NOI18N
    private static TopComponentChangeListener listener;

    public static PaletteController getPalette() {
        if (palette == null) {
            try {
                palette = PaletteFactory.createPalette(OTD_PALETTE_FOLDER, 
                        new OTDPaletteActions(),
                        new OTDPaletteFilter(),
                        new OTDPaletteDNDHandler());
                
                TopComponent.Registry registry = TopComponent.getRegistry();
                listener = new TopComponentChangeListener(palette);
                registry.addPropertyChangeListener(listener);
                
          } catch (IOException e) {
              Exceptions.printStackTrace(e);
          }
      }

      return palette;
    }
    
    public static class TopComponentChangeListener implements PropertyChangeListener{
        PaletteController pc = null;

        public TopComponentChangeListener(PaletteController pc1){
            this.pc = pc1;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED )) {
                TopComponent activeTc = TopComponent.getRegistry().getActivated();
                if( null != activeTc ) {
                    DataObject d = activeTc.getLookup().lookup(DataObject.class);
                    if(d != null)
                        associatePalette(d, pc);
                }
            }
        }

        void associatePalette(DataObject d, PaletteController pc) {
            if( isOTDJavaFile( d ) ) {
                pc.refresh();
            }
        }

        private boolean isOTDJavaFile(DataObject dobj){
            boolean ret = false;
            
            if ((dobj != null) && (dobj.getPrimaryFile() != null)){
                String ext = dobj.getPrimaryFile().getExt();
                ext = ext.toLowerCase();
                if ("java".equals(ext)){ //NOI18N
                    ret = true;
                }
            }
            return ret;
        }        
    }
}
