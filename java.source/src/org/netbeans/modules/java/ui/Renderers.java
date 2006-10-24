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

package org.netbeans.modules.java.ui;

import java.awt.Component;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.HtmlRenderer.Renderer;
import org.openide.filesystems.FileSystem.HtmlStatus;
import org.netbeans.api.java.source.UiUtils;

/** Contains implementation of various CellRenderers.
 *
 * @author Petr Hrebejk
 */
public final class Renderers {
    
    /** Creates a new instance of Renderers */
    private Renderers() {
    }
    
    public static TreeCellRenderer declarationTreeRenderer() {
        return new DeclarationTreeRenderer();
    }
    
    public static ListCellRenderer declarationListRenderer() {
        return new DeclarationTreeRenderer();
    }
        
    // Innerclasses ------------------------------------------------------------
    
    private static class DeclarationTreeRenderer implements TreeCellRenderer, ListCellRenderer {
        
        Renderer renderer;
        
        /** Creates a new instance of ClassesRenderer */
        private DeclarationTreeRenderer() {
            this.renderer = HtmlRenderer.createRenderer();        
        }
        
        // ListCellRenderer implementation -------------------------------------

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if ( value instanceof DefaultMutableTreeNode ) {
                value = ((DefaultMutableTreeNode)value).getUserObject();
            }
            
            String name;
            String toolTip;
            Icon icon;
            if (value instanceof TypeElement) {
                TypeElement te = (TypeElement) value;
                name = getDisplayName(te);
                toolTip = getToolTipText(te);            
                icon = UiUtils.getElementIcon( te.getKind(), te.getModifiers() );
            }
            else {
                name = "??";
                toolTip = name = (value == null ? "NULL" : value.toString());
                icon = null;
            }
            
            JLabel comp = (JLabel)renderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            comp.setText( name );            
            comp.setToolTipText(toolTip);            
            if (icon != null) {                
                comp.setIcon(icon);
            }
//            if ( index % 2 > 0 ) {
//                comp.setBackground( list.getBackground().darker() ); // Too dark
//                comp.setOpaque( true );
//            }
            return comp;
            
        }
        
        // TreeCellRenderer implementation -------------------------------------
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {        
            
            if ( value instanceof DefaultMutableTreeNode ) {
                value = ((DefaultMutableTreeNode)value).getUserObject();
            }
            
            String name;
            String toolTip;
            Icon icon;
            if (value instanceof TypeElement) {
                TypeElement te = (TypeElement) value;
                name = getDisplayName(te);
                toolTip = getToolTipText(te);            
                icon = UiUtils.getElementIcon( te.getKind(), te.getModifiers() );
            }
            else {
                name = "???";
                toolTip = name = (value == null ? "NULL" : value.toString());
                icon = null;
            }
            
            JLabel comp = (JLabel)renderer.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
            comp.setText( name );
            comp.setToolTipText(toolTip);
            if (icon != null) {
                comp.setIcon(icon);
            }
            return comp;
        }
        
        // Private methods -----------------------------------------------------

        private static String getDisplayName( TypeElement te ) {
            boolean deprecated = false;//XXX: removing isDeprecated from SourceUtils, use Elements.isDeprecated instead
            String simpleName = te.getSimpleName().toString();
            String qualifiedName = te.getQualifiedName().toString();
            int lastIndex = qualifiedName.length() - simpleName.length();
            lastIndex = lastIndex == 0 ? lastIndex : lastIndex - 1;
            return "<html><b>" + (deprecated ? "<s>" : "" ) + simpleName + (deprecated ? "</s></b>" : "</b>" ) + "<font color=\"#707070\"> (" + qualifiedName.substring( 0, lastIndex ) + ")</font></html>";            
        }

        private static String getToolTipText( TypeElement value ) {
            return value.getQualifiedName().toString();
        }       

               
    }
            
}
