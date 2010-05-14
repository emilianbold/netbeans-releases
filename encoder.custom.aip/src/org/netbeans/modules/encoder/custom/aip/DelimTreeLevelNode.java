/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.custom.aip;

import com.sun.encoder.custom.appinfo.DelimiterLevel;
import java.awt.Image;
import java.util.ResourceBundle;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.ImageUtilities;

/**
 * The node that represents the delimiter level in the delimiter tree.
 *
 * @author Jun Xu
 */
public class DelimTreeLevelNode extends AbstractNode 
        implements DelimiterSetChangeNotificationSupport {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/Bundle");
    private final DelimiterSetChangeNotifier mChangeNotifier = 
            new DelimiterSetChangeNotifier();
    private final DelimiterLevel mDelimLevel;
    
    /**
     * Creates a new instance of DelimTreeLevelNode (with empty children).
     * @param delimLevel - DelimiterLevel
     * @param children - Children
     * @param lookup - Lookup
     */
    public DelimTreeLevelNode(DelimiterLevel delimLevel, Children children, Lookup lookup) {
        super(children, lookup);
        mDelimLevel = delimLevel;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public Image getIcon(int i) {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/encoder/custom/aip/delimLevelIcon.PNG");  //NOI18N
    }

    @Override
    public Image getOpenedIcon(int i) {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/encoder/custom/aip/delimLevelOpenIcon.PNG");  //NOI18N
    }

    @Override
    public String getName() {
        return "Level";  //NOI18N
    }

    @Override
    public String getDisplayName() {
        return _bundle.getString("delim_tree_level_node.lbl.level");
    }

    public void addDelimiterSetChangeListener(DelimiterSetChangeListener listener) {
        mChangeNotifier.addDelimiterSetChangeListener(listener);
    }

    public DelimiterSetChangeListener[] getDelimiterSetChangeListeners() {
        return mChangeNotifier.getDelimiterSetChangeListeners();
    }

    public void removeDelimiterSetChangeListener(DelimiterSetChangeListener listener) {
        mChangeNotifier.removeDelimiterSetChangeListener(listener);
    }

    public void addDelimiterSetChangeListener(DelimiterSetChangeListener[] listeners) {
        mChangeNotifier.addDelimiterSetChangeListener(listeners);
    }

    public void removeDelimiterSetChangeListener(DelimiterSetChangeListener[] listeners) {
        mChangeNotifier.removeDelimiterSetChangeListener(listeners);
    }
}
