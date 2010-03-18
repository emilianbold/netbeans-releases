/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.Image;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.codegen.Property;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public abstract class CheckNode extends DefaultMutableTreeNode {

    private static String ICON_BASE = "org/netbeans/modules/php/editor/resources/";     //NOI18N
    private static String ICON_EXTENSION = ".png";  //NOI18N
    public final static int SINGLE_SELECTION = 0;
    public final static int DIG_IN_SELECTION = 4;
    protected int selectionMode;
    protected boolean isSelected;

    public CheckNode() {
        this(null);
    }

    public CheckNode(Object userObject) {
        this(userObject, true, false);
    }

    public CheckNode(Object userObject, boolean allowsChildren, boolean isSelected) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
        setSelectionMode(DIG_IN_SELECTION);
    }

    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;

        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration en = children.elements();
            while (en.hasMoreElements()) {
                CheckNode node = (CheckNode) en.nextElement();
                node.setSelected(isSelected);
            }
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public abstract Image getIcon();

    public static class CGSClassNode extends CheckNode {

        public CGSClassNode(String className) {
            super(className, true, false);
        }

        @Override
        public Image getIcon() {
            return ImageUtilities.loadImage(ICON_BASE + "class" + ICON_EXTENSION); //NOI18N
        }
    }

    public static class CGSPropertyNode extends CheckNode {

        private final Property property;

        public CGSPropertyNode(Property property) {
            super(property.getName(), false, property.isSelected());
            this.property = property;
        }

        @Override
        public void setSelected(boolean isSelected) {
            super.setSelected(isSelected);
            property.setSelected(isSelected);
        }

        @Override
        public Image getIcon() {
            final int modifier = property.getModifier();
            final boolean isPublic = BodyDeclaration.Modifier.isPublic(modifier);
            final boolean isProtected = isPublic ? false : BodyDeclaration.Modifier.isProtected(modifier);
            final boolean isStatic = BodyDeclaration.Modifier.isStatic(modifier);
            String name = "fieldPrivate";           //NOI18N
            if (property.getKind().equals(PhpElementKind.METHOD)) {
                StringBuilder sb = new StringBuilder();
                sb.append(isStatic? "methodStatic" : "method");//NOI18N
                sb.append(isPublic ? "Public" : "Protected");//NOI18N
                name = sb.toString();
            } else {
                name = "fieldPrivate";
                if (isPublic) {
                    name = "fieldPublic";               //NOI18N
                } else if (isProtected) {
                    name = "fieldProtected";               //NOI18N
                }
            }
            return ImageUtilities.loadImage(ICON_BASE + name + ICON_EXTENSION);
        }
    }
}
