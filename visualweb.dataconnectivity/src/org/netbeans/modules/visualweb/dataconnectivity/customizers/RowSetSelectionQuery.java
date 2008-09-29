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
/*
 * RowSetSelectionQuery.java
 *
 * Created on June 9, 2005, 9:40 AM
 */

package org.netbeans.modules.visualweb.dataconnectivity.customizers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * This presents either a short form of the sql text or the full sql text.
 *
 * @author  jfbrown
 */
public class RowSetSelectionQuery extends javax.swing.JPanel {

    private static final ImageIcon closedIcon = new ImageIcon( ImageUtilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/sqlClosed.gif") ) ;
    private static final ImageIcon openIcon = new ImageIcon( ImageUtilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/sqlOpened.gif") ) ;
    private static final ImageIcon emptyIcon = new ImageIcon( ImageUtilities.loadImage("org/openide/resources/actions/empty.gif") ) ;
    boolean expanded = false ;
    private static final Color bgColor = (new javax.swing.JLabel() ).getBackground() ;

    SpecialButton expandButton = new SpecialButton() ;
    boolean noExpand = false ; // is expandable (e.g., is only one line)

    /** Creates new form RowSetSelectionQuery */
    public RowSetSelectionQuery( String sql ) {
        initComponents();

        String bStuff = " SQL" ; //NOI18N
        this.getAccessibleContext().setAccessibleName(bStuff) ;
        this.getAccessibleContext().setAccessibleDescription(bStuff);
        shortSql.getAccessibleContext().setAccessibleName(bStuff) ;
        shortSql.getAccessibleContext().setAccessibleDescription(bStuff);
        longSql.getAccessibleContext().setAccessibleName(bStuff) ;
        longSql.getAccessibleContext().setAccessibleDescription(bStuff);

        this.setBackground(bgColor) ;

        /*
        JPanel lac = new JPanel() ;
        lac.addMouseListener( new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent me) {
                if ( noExpand ) return ;
                setExpanded( ! expanded ) ;
            }

        }) ;
        lac.add( expandButton ) ;
        */

        expandButton.setToolTipText(NbBundle.getMessage(RowSetSelectionQuery.class,"ExpandSql_tooltip" ) ) ;

        expandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if ( noExpand ) return ;
                setExpanded( ! expanded ) ;
            }
        });

        longSql.setText(sql) ;
        String[] lines = sql.split("\n") ;
        int maxWidth = 25 ;
        for ( int i = 0 ; i < lines.length ; i++ ) {
            if ( maxWidth < lines[i].length() ) maxWidth = lines[i].length() ;
        }
        int width = maxWidth ;
        if ( lines.length <= 2 ) {
            shortSql.setText( sql ) ;
            expandButton.setIcon(emptyIcon) ;
            noExpand = true ;
            // setExpanded( true ) ;

        } else {
            // long query
            String lowerSql = sql.toLowerCase() ;
            StringBuffer dsql = new StringBuffer(100) ;
            int pos = -1 ;
            dsql.append(lines[0]).append(" ...\n") ; // NOI18N
            width = lines[0].length() ;
            for ( int i = 1 ; i < lines.length ; i++ ) {
                String nn = lines[i].toLowerCase() ;
                pos = nn.indexOf("from") ;  // NOI18N
                if ( pos >= 0 ) {
                    if ( pos < 5  ) {
                        dsql.append(lines[i]) ;
                        dsql.append(" ...") ;  // NOI18N
                        int line2 = lines[i].length()+4 ;
                        if ( line2 > width) {
                            width=line2 ;
                        }
                        break ;
                    }
                }
            }
            if ( pos < 0 ) dsql.append(lines[1]) ;
            shortSql.setText( dsql.toString() ) ;
            setExpanded( false ) ;
        }
        if ( width > 60 ) width = 60 ;
        this.width_short = width ;
        shortSql.setColumns(width) ; 
        if ( maxWidth > 70 ) maxWidth = 70 ;
        this.width_long = maxWidth ;
        longSql.setColumns( maxWidth ) ;
        
        // add the expand button (or a blank filler of the same size).
        GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridheight = 2;
        gbc.gridx = 0 ;  gbc.gridy = 0 ;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        if ( ! noExpand ) {
            add(expandButton, gbc);
        } else {
            add( new EmptyPanel(), gbc) ;
        }        
    }
    private int width_short = 30 ;
    private int width_long = 30 ;
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        shortSql = new javax.swing.JTextArea();
        longSql = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        shortSql.setBackground(bgColor);
        shortSql.setColumns(25);
        shortSql.setEditable(false);
        shortSql.setTabSize(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(shortSql, gridBagConstraints);

        longSql.setBackground(bgColor);
        longSql.setColumns(25);
        longSql.setEditable(false);
        longSql.setTabSize(4);
        longSql.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(longSql, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
        
    public synchronized void setExpanded( boolean newExpanded ) {
        expandButton.setText(null) ;
        if ( newExpanded ) {
            shortSql.setVisible(false) ;
            longSql.setColumns(this.width_long) ;
            longSql.setVisible(true) ;
            expandButton.setIcon( openIcon ) ;
            expanded = true ;
        } else {
            shortSql.setVisible(true) ;
            longSql.setColumns(this.width_short) ;
            longSql.setVisible(false) ;
            expandButton.setIcon( closedIcon ) ;
            expanded = false ;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea longSql;
    private javax.swing.JTextArea shortSql;
    // End of variables declaration//GEN-END:variables
    
    public class SpecialButton extends JButton {
        
        private final int gSize = 16 ;
        private final Dimension dim = new Dimension(gSize,gSize) ;
         
        public boolean isOpaque() {
            return false  ;
        }
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g ;
            ImageIcon im = (ImageIcon)this.getIcon() ;
            g2.drawImage(im.getImage(),0,0,this.getWidth(), this.getHeight(),
                    bgColor, null);
        }
        public Dimension getPreferredSize() {
            return dim ;
        }
        public Dimension getMinimumSize() {
            return dim ;
        }
        public Dimension getMaximumSize() {
            return dim ;
        }
        public Insets getInsets() {
            return new Insets(0,0,0,0) ;
        }
    }
    public class EmptyPanel extends JPanel {
        private final int gSize = 16 ;
        private final Dimension dim = new Dimension(gSize,gSize) ;
        public EmptyPanel() {
            super() ;
            String bStuff = " " ; //NOI18N
            this.getAccessibleContext().setAccessibleName(bStuff) ;
            this.getAccessibleContext().setAccessibleDescription(bStuff);
        }
        public boolean isOpaque() {
            return false  ;
        }
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g ;
            ImageIcon im = emptyIcon ;
            g2.drawImage(im.getImage(),0,0,this.getWidth(), this.getHeight(),
                    bgColor, null);
        }
        public Dimension getPreferredSize() {
            return dim ;
        }
        public Dimension getMinimumSize() {
            return dim ;
        }
        public Dimension getMaximumSize() {
            return dim ;
        }
        public Insets getInsets() {
            return new Insets(0,0,0,0) ;
        }        
    }
    
    
}
