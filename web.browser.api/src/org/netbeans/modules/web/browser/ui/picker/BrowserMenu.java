/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.browser.ui.picker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Browser selection popup.
 * 
 * @author S. Aubrecht
 */
public class BrowserMenu implements ChangeListener {

    private final ProjectBrowserProvider browserProvider;
    private final Collection<WebBrowser> browsers;
    private final SelectionListFactory factory;
    private final SelectionModel selModel;
    private JPopupMenu popup;
    private WebBrowser selectedBrowser;
    private final ChangeSupport changeSupport = new ChangeSupport( this );

    public BrowserMenu( ProjectBrowserProvider provider ) {
        this( provider.getBrowsers(), provider.getActiveBrowser(), provider );
    }

    public BrowserMenu( Collection<WebBrowser> browsers, WebBrowser selectedBrowser ) {
        this( browsers, selectedBrowser, null );
    }

    private BrowserMenu( Collection<WebBrowser> browsers, WebBrowser selectedBrowser, ProjectBrowserProvider provider ) {
        this.browsers = browsers;
        this.browserProvider = provider;
        this.selectedBrowser = selectedBrowser;
        factory = SelectionListFactory.create();
        selModel = factory.getSelectionModel();
    }

    public void show( JComponent invoker, int x, int y ) {
        JPanel panel = new JPanel();
        panel.setOpaque( false );
        fillContent( panel );

        popup = new JPopupMenu();
        popup.add( panel );
        popup.show( invoker, x, y );
    }

    private void fillContent( JPanel contentPanel ) {
        contentPanel.setLayout( new GridBagLayout() );

        WebBrowser activeBrowser = selectedBrowser;
        ArrayList<ListItemImpl> defaultItems = new ArrayList<>( browsers.size() );
        ArrayList<ListItemImpl> mobileItems = new ArrayList<>( browsers.size() );
        ArrayList<ListItemImpl> phoneGapItems = new ArrayList<>( browsers.size() );
        ListItem selItem = null;
        for( WebBrowser browser : browsers ) {
            ListItemImpl item = new ListItemImpl( browser );
            if( null != activeBrowser && activeBrowser.getId().equals( browser.getId() ) ) {
                selItem = item;
            }
            if( BrowserFamilyId.PHONEGAP.equals( browser.getBrowserFamily() ) ) {
                phoneGapItems.add( item );
            } else if( browser.getBrowserFamily().isMobile() ) {
                mobileItems.add( item );
            } else {
                defaultItems.add( item );
            }
        }

        addSection( contentPanel, defaultItems, NbBundle.getMessage(BrowserMenu.class, "Header_BROWSER"), 0, null );
        addSection( contentPanel, mobileItems, NbBundle.getMessage(BrowserMenu.class, "Header_MOBILE"), 2, null );
        addSection( contentPanel, phoneGapItems, NbBundle.getMessage(BrowserMenu.class, "Header_PHONEGAP"), 4, createConfigureButton() );

        if( !defaultItems.isEmpty() && !mobileItems.isEmpty() ) {
            contentPanel.add( new JSeparator(JSeparator.VERTICAL),
                    new GridBagConstraints( 1, 1, 1, 2, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(10,10,10,10), 0, 0 ));
        }

        if( !mobileItems.isEmpty() && !phoneGapItems.isEmpty() ) {
            contentPanel.add( new JSeparator(JSeparator.VERTICAL),
                    new GridBagConstraints( 3, 1, 1, 2, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(10,10,10,10), 0, 0 ));
        }

        if( !phoneGapItems.isEmpty() ) {
            contentPanel.add( new JLabel( NbBundle.getMessage(BrowserMenu.class, "Hint_PhoneGap")),
                    new GridBagConstraints( 0, 3, 5, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,10,10,10), 0, 0 ));
        }

        if( null != selItem )
            selModel.setSelectedItem( selItem );
        selModel.addChangeListener( this );
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
        ListItem selItem = selModel.getSelectedItem();
        if( null == selItem )
            return;
        popup.setVisible( false );
        selectedBrowser = ((ListItemImpl)selItem).browser;
        changeSupport.fireChange();
    }

    private JComponent createList( List<ListItemImpl> items, boolean nbIntegrationOnly ) {
        if( items.isEmpty() )
            return null;

        ArrayList<ListItemImpl> filteredItems = new ArrayList<>( items.size() );
        for( ListItemImpl li : items ) {
            if( li.browser.hasNetBeansIntegration() == nbIntegrationOnly ) {
                filteredItems.add( li );
            }
        }
        if( filteredItems.isEmpty() )
            return null;

        final SelectionList list = factory.createSelectionList();
        list.setItems( filteredItems );

        return list.getComponent();
    }

    private void addSection( JPanel contentPanel, List<ListItemImpl> items, String header, int column, JComponent bottomPart ) {
        if( items.isEmpty() )
            return;

        contentPanel.add( createHeader( header ),
                new GridBagConstraints( column, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,10,5), 0, 0 ));

        JComponent list = createList( items, true );
        if( null != list ) {
            contentPanel.add( list,
                    new GridBagConstraints( column, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5), 0, 0 ));
        }

        JPanel panel = new JPanel( new BorderLayout( 0, 5 ) );
        panel.setOpaque( false );
        list = createList( items, false );
        if( null != list ) {
            panel.add( list, BorderLayout.CENTER );
        }
        if( null != bottomPart )
            panel.add( bottomPart, BorderLayout.SOUTH );

        if( null != bottomPart || null != list ) {
            contentPanel.add( panel,
                    new GridBagConstraints( column, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(15,5,5,5), 0, 0 ));
        }
    }

    private JComponent createHeader( String title ) {
        JLabel label = new JLabel( title );
        Font defaultFont = label.getFont();
        label.setFont( defaultFont.deriveFont( Font.BOLD ).deriveFont( defaultFont.getSize2D()+2.0f ));
        return label;
    }

    private JComponent createConfigureButton() {
        if( null == browserProvider || !browserProvider.hasCustomizer() )
            return null;
        JButton button = new JButton(NbBundle.getMessage(BrowserMenu.class, "Ctl_ConfigurePhoneGap"));
        button.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                browserProvider.customize();
            }
        });
        button.setBorder( new EmptyBorder(1, 1, 1, 1) );
        button.setMargin( new Insets(0, 0, 0, 0) );
        button.setForeground( Color.blue );

        button.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        button.setHorizontalAlignment( JLabel.LEFT );
        button.setFocusable( false );

        button.setBorderPainted( false );
        button.setFocusPainted( false );
        button.setRolloverEnabled( true );
        button.setContentAreaFilled( false );

        return button;
    }

    private static class ListItemImpl extends ListItem {
        private final WebBrowser browser;

        public ListItemImpl( WebBrowser browser ) {
            this.browser = browser;
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.image2Icon( browser.getIconImage() );
        }

        @Override
        public String getText() {
            return browser.getName();
        }
    }

    public void addChangeListener( ChangeListener listener ) {
        changeSupport.addChangeListener( listener );
    }

    public void removeChangeListener( ChangeListener listener ) {
        changeSupport.removeChangeListener( listener );
    }

    public WebBrowser getSelectedBrowser() {
        return selectedBrowser;
    }
}
