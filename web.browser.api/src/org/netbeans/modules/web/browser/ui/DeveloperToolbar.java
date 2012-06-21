/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.browser.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.web.browser.api.ResizeOption;
import org.netbeans.modules.web.browser.spi.Zoomable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Toolbar with web-developer tools.
 * 
 * @author S. Aubrecht
 */
public class DeveloperToolbar {

    private final JPanel panel;
    private Lookup context;

    private DeveloperToolbar() {
        panel = new JPanel( new BorderLayout( 5, 0 ) );
    }

    public static DeveloperToolbar create() {
        return new DeveloperToolbar();
    }

    public Component getComponent() {
        return panel;
    }

    public void intialize( Lookup context ) {
        this.context = context;
        JToolBar bar = context.lookup( JToolBar.class );
        if( null == bar ) {
            bar = new JToolBar();
        }
        bar.setFloatable( false );
        bar.setFocusable( false );
        panel.add( bar, BorderLayout.WEST );

        ButtonGroup group = new ButtonGroup();

        List<ResizeOption> options = ResizeOption.loadAll();
        for( ResizeOption ro : options ) {
            if( !ro.isShowInToolbar() )
                continue;
            AbstractButton button = BrowserResizeButton.create( ro, context );
            group.add( button );
            bar.add( button );
        }

        AbstractButton button = BrowserResizeButton.create( NbBundle.getMessage(DeveloperToolbar.class, "Lbl_AUTO"), context );
        button.setSelected( true );
        group.add( button );
        bar.add( button );

        bar.addSeparator();

        //ZOOM combo box
        DefaultComboBoxModel zoomModel = new DefaultComboBoxModel();
        zoomModel.addElement( "200%" ); //NOI18N
        zoomModel.addElement( "150%" ); //NOI18N
        zoomModel.addElement( "100%" ); //NOI18N
        zoomModel.addElement( "75%" ); //NOI18N
        zoomModel.addElement( "50%" ); //NOI18N
        final JComboBox comboZoom = new JComboBox(zoomModel);
        comboZoom.setEditable( true );
        if( comboZoom.getEditor().getEditorComponent() instanceof JTextField )
            ((JTextField)comboZoom.getEditor().getEditorComponent()).setColumns( 4 );
        comboZoom.setSelectedItem( "100%" ); //NOI18N
        comboZoom.addItemListener( new ItemListener() {

            @Override
            public void itemStateChanged( ItemEvent e ) {
                if( e.getStateChange() == ItemEvent.DESELECTED )
                    return;
                String newZoom = zoom( comboZoom.getSelectedItem().toString() );
                comboZoom.setSelectedItem( newZoom );
            }
        });
        comboZoom.setEnabled( null != getLookup().lookup( Zoomable.class ) );
        bar.add( comboZoom );
    }

    private Lookup getLookup() {
        return null == context ? Lookup.EMPTY : context;
    }

    private String zoom( String zoomFactor ) {
        if( zoomFactor.trim().isEmpty() )
            return null;

        Zoomable zoomable = getLookup().lookup( Zoomable.class );
        if( null == zoomable )
            return null;
        try {
            zoomFactor = zoomFactor.replaceAll( "\\%", ""); //NOI18N
            zoomFactor = zoomFactor.trim();
            double zoom = Double.parseDouble( zoomFactor );
            zoom = Math.abs( zoom )/100;
            zoomable.zoom( zoom );
            return (int)(100*zoom) + "%"; //NOI18N
        } catch( NumberFormatException nfe ) {
            //ignore
        }
        return null;
    }
}
