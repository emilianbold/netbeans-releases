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
import java.util.Map;
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
    
    private Map originalColors;
    private Map originalFonts;

    
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
