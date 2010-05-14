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

import com.sun.encoder.custom.appinfo.Delimiter;
import com.sun.encoder.custom.appinfo.DelimiterLevel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of the Children interface of the delimiter level node.
 *
 * @author Jun Xu
 */
public class DelimTreeLevelNodeChildren extends Children.Keys<Delimiter>
        implements DelimiterSetChangeNotificationSupport {
    
    private final DelimiterSetChangeNotifier mChangeNotifier = 
            new DelimiterSetChangeNotifier();
    private final DelimiterLevel mDelimLevel;
    
    /**
     * Creates a new instance of DelimTreeLevelNodeChildren
     * @param delimLevel - DelimiterLevel
     */
    public DelimTreeLevelNodeChildren(DelimiterLevel delimLevel) {
        mDelimLevel = delimLevel;
    }
    
    protected Node[] createNodes(Delimiter object) {
        DelimTreeDelimiterNode node = new DelimTreeDelimiterNode(
                object, Lookups.singleton(object));
        node.addDelimiterSetChangeListener(getDelimiterSetChangeListeners());
        return new Node[]{node};
    }

    @Override
    protected void removeNotify() {
        List<Delimiter> emptyDelimiterList = Collections.emptyList();
        setKeys(emptyDelimiterList);
    }

    @Override
    protected void addNotify() {
        List<Delimiter> keyList = new ArrayList<Delimiter>();
        for (int i = 0; i < mDelimLevel.sizeOfDelimiterArray(); i++) {
            keyList.add(mDelimLevel.getDelimiterArray(i));
        }
        setKeys(keyList);
    }

    @Override
    @Deprecated
    public boolean remove(final Node[] node) {
        addNotify();
        mChangeNotifier.fireDelimiterSetChangeEvent(mDelimLevel, "children",
                null, mDelimLevel.getDelimiterArray()); //NOI18N
        return true;
    }

    @Override
    @Deprecated
    public boolean add(Node[] node) {
        addNotify();
        mChangeNotifier.fireDelimiterSetChangeEvent(mDelimLevel, "children",
                null, mDelimLevel.getDelimiterArray()); //NOI18N
        return true;
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
