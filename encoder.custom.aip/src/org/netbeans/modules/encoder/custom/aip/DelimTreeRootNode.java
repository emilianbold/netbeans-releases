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

import com.sun.encoder.custom.appinfo.DelimiterSet;
import java.util.ResourceBundle;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * The node that represents the root of the delimiter tree.
 *
 * @author Jun Xu
 */
public class DelimTreeRootNode extends AbstractNode
        implements DelimiterSetChangeNotificationSupport {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/Bundle");
    private final DelimiterSetChangeNotifier mChangeNotifier = 
            new DelimiterSetChangeNotifier();
    private final DelimiterSet mDelimSet;
    
    /** Creates a new instance of DelimTreeRootNode */
    public DelimTreeRootNode(DelimiterSet delimSet, Children children, Lookup lookup) {
        super(children, lookup);
        mDelimSet = delimSet;
    }

    @Override
    public String getName() {
        return "Level"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return _bundle.getString("delim_tree_root_node.lbl.level");
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
