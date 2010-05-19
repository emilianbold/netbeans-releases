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

package org.netbeans.modules.compapp.casaeditor.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.nodes.LookAndFeelNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author jsandusky
 */
public class LookAndFeelEditor extends PropertyEditorSupport {
    
    private final static String EMPTY = Constants.EMPTY_STRING;
    
    private String mPropertyName;
    private boolean mIsChangesApplied;
    
    private LinkedHashMap<String, Color> originalColors;
    private LinkedHashMap<String, Font> originalFonts;

    
    public enum Option { None, Declared, Other };
    
    
    public LookAndFeelEditor(String propertyName) {
        mPropertyName = propertyName;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public String getAsText() {
        return NbBundle.getMessage(getClass(), "LBL_EditLookAndFeel");      // NOI18N
    }
    
    public void setAsText(String s) {
    }
    
    public boolean isPaintable() {
        return true;
    }
    
    public void paintValue(Graphics g, Rectangle rectangle) {
        String paintableString = getPaintableString();
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(
                paintableString,
                rectangle.x,
                rectangle.y + (rectangle.height-metrics.getHeight()) / 2 +
                metrics.getAscent());
    }
    
    protected String getPaintableString() {
        Object value = getAsText();
        return value == null ?
            NbBundle.getMessage(StringEditor.class, "LBL_Null") :        // NOI18N
            getAsText();
    }

    public Component getCustomEditor() {
        
        mIsChangesApplied = false;
        initialUIState();
        
        final PropertySheet propertySheetPanel = new PropertySheet();
        propertySheetPanel.setNodes(new Node[] { new LookAndFeelNode() });
        
        final DialogDescriptor descriptor = new DialogDescriptor(
                propertySheetPanel,
                mPropertyName,
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    mIsChangesApplied = true;
                }
            }
        });
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(500, 700));
        
        dlg.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                if (mIsChangesApplied) {
                    applyUIChanges();
                } else {
                    revertUIChanges();
                }
            }
        });
        
        return dlg;
    }
    
    private void initialUIState() {
        // TODO remember what the original UI properties were
        originalColors = new LinkedHashMap<String, Color>();
        originalFonts = new LinkedHashMap<String, Font>();
        
        originalColors.putAll(CasaFactory.getCasaCustomizer().getColorsMapReference());
        originalFonts.putAll(CasaFactory.getCasaCustomizer().getFontsMapReference());
    }
    
    private void revertUIChanges() {
        // TODO revert to the original UI properties
        CasaFactory.getCasaCustomizer().getColorsMapReference().putAll(originalColors);
        CasaFactory.getCasaCustomizer().getFontsMapReference().putAll(originalFonts);
        CasaFactory.getCasaCustomizerRegistor().propagateChange();
    }
    
    private void applyUIChanges() {
        // TODO persist changes to all UI properties
        CasaFactory.getCasaCustomizer().savePreferences();
    }
}
