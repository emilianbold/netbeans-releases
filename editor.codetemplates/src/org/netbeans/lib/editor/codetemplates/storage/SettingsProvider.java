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

package org.netbeans.lib.editor.codetemplates.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 *  @author Vita Stejskal
 */
public final class SettingsProvider implements MimeDataProvider {

    private static final Logger LOG = Logger.getLogger(SettingsProvider.class.getName());
    
    public SettingsProvider () {
    }
    
    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    public Lookup getLookup(MimePath mimePath) {
        return new MyLookup(mimePath, null);
    }
    
    private static final class MyLookup extends AbstractLookup implements PropertyChangeListener {
        
        private final MimePath mimePath;
        
        private final InstanceContent ic;
        private Object codeTemplateSettings = null;
        private CodeTemplateSettingsImpl ctsi;
        
        public MyLookup(MimePath mimePath, String profile) {
            this(mimePath, profile, new InstanceContent());
        }
        
        private MyLookup(MimePath mimePath, String profile, InstanceContent ic) {
            super(ic);

            this.mimePath = mimePath;
            this.ic = ic;
            
            // Start listening
            this.ctsi = CodeTemplateSettingsImpl.get(mimePath);
            this.ctsi.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ctsi));
        }

        protected @Override void initialize() {
            synchronized (this) {
                codeTemplateSettings = this.ctsi.createInstanceForLookup();
                ic.set(Arrays.asList(new Object [] { codeTemplateSettings }), null);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                boolean ctsChanged = false;

                // Determine what has changed
                if (this.ctsi == evt.getSource() || 
                    CodeTemplateSettingsImpl.PROP_EXPANSION_KEY.equals(evt.getPropertyName())
                ) {
                    ctsChanged = true;
                }
                
                // Update lookup contents
                boolean updateContents = false;
                
                if (ctsChanged  && codeTemplateSettings != null) {
                    codeTemplateSettings = this.ctsi.createInstanceForLookup();
                    updateContents = true;
                }
                
                if (updateContents) {
                    ic.set(Arrays.asList(new Object [] { codeTemplateSettings }), null);
                }
            }
        }

    } // End of MyLookup class
}
