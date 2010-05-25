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

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.PropertySupport;

/**
 * A tree table view for showing the delimiter list.
 *
 * @author Jun Xu
 */
public class DelimiterTreeTableView extends TreeTableView {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/Bundle");
    private static final int COL_TYPE               = 0;
    private static final int COL_PRECEDENCE         = COL_TYPE + 1;
    private static final int COL_OPTIONAL_MODE      = COL_PRECEDENCE + 1;
    private static final int COL_TERMINATOR_MODE    = COL_OPTIONAL_MODE + 1;
    private static final int COL_BYTES              = COL_TERMINATOR_MODE + 1;
    private static final int COL_OFFSET             = COL_BYTES + 1;
    private static final int COL_LENGTH             = COL_OFFSET + 1;
    private static final int COL_DETACHED           = COL_LENGTH + 1;
    private static final int COL_BEGIN_BYTES        = COL_DETACHED + 1;
    private static final int COL_BEGIN_OFFSET       = COL_BEGIN_BYTES + 1;
    private static final int COL_BEGIN_LENGTH       = COL_BEGIN_OFFSET + 1;
    private static final int COL_BEGIN_DETACHED     = COL_BEGIN_LENGTH + 1;
    private static final int COL_SKIP_LEADING       = COL_BEGIN_DETACHED + 1;
    private static final int COL_COLLAPSE           = COL_SKIP_LEADING + 1;
    
    /** Creates a new instance of DelimiterTreeTableView */
    public DelimiterTreeTableView() {
        super();
        PropertySupport.ReadWrite[] props = new PropertySupport.ReadWrite[COL_COLLAPSE + 1];
        props[COL_TYPE] = new SimplePropertySupport<String>(
                "kind",
                String.class,
                _bundle.getString("delim_tree_tab.lbl.type"),
                _bundle.getString("delim_tree_tab.lbl.type_short")); //NOI18N
        props[COL_PRECEDENCE] = new SimplePropertySupport<Short>(
                "precedence",
                short.class,
                _bundle.getString("delim_tree_tab.lbl.precedence"),
                _bundle.getString("delim_tree_tab.lbl.precedence_short")); //NOI18N
        props[COL_OPTIONAL_MODE] = new SimplePropertySupport<String>(
                "optionMode",
                String.class,
                _bundle.getString("delim_tree_tab.lbl.opt_mode"),
                _bundle.getString("delim_tree_tab.lbl.opt_mode_short")); //NOI18N
        props[COL_TERMINATOR_MODE] = new SimplePropertySupport<String>(
                "termMode",
                String.class,
                _bundle.getString("delim_tree_tab.lbl.term_mode"),
                _bundle.getString("delim_tree_tab.lbl.term_mode_short")); //NOI18N
        props[COL_BYTES] = new SimplePropertySupport<String>(
                "bytes",
                String.class,
                _bundle.getString("delim_tree_tab.lbl.delim_bytes"),
                _bundle.getString("delim_tree_tab.lbl.delim_bytes_short")); //NOI18N
        props[COL_OFFSET] = new SimplePropertySupport<Integer>(
                "offset",
                int.class,
                _bundle.getString("delim_tree_tab.lbl.offset"),
                _bundle.getString("delim_tree_tab.lbl.offset_short")); //NOI18N
        props[COL_LENGTH] = new SimplePropertySupport<Short>(
                "length",
                short.class,
                _bundle.getString("delim_tree_tab.lbl.length"),
                _bundle.getString("delim_tree_tab.lbl.length_short")); //NOI18N
        props[COL_DETACHED] = new SimplePropertySupport<Boolean>(
                "detached",
                boolean.class,
                _bundle.getString("delim_tree_tab.lbl.detached"),
                _bundle.getString("delim_tree_tab.lbl.detached_short")); //NOI18N
        props[COL_BEGIN_BYTES] = new SimplePropertySupport<String>(
                "beginBytes",
                String.class,
                _bundle.getString("delim_tree_tab.lbl.begin_delim_bytes"),
                _bundle.getString("delim_tree_tab.lbl.begin_delim_bytes_short")); //NOI18N
        props[COL_BEGIN_OFFSET] = new SimplePropertySupport<Integer>(
                "beginOffset",
                int.class,
                _bundle.getString("delim_tree_tab.lbl.begin_offset"),
                _bundle.getString("delim_tree_tab.lbl.begin_offset_short")); //NOI18N
        props[COL_BEGIN_LENGTH] = new SimplePropertySupport<Short>(
                "beginLength",
                short.class,
                _bundle.getString("delim_tree_tab.lbl.begin_length"),
                _bundle.getString("delim_tree_tab.lbl.begin_length_short")); //NOI18N
        props[COL_BEGIN_DETACHED] = new SimplePropertySupport<Boolean>(
                "beginDetached", boolean.class,
                _bundle.getString("delim_tree_tab.lbl.begin_detached"),
                _bundle.getString("delim_tree_tab.lbl.begin_detached_short")); //NOI18N
        props[COL_SKIP_LEADING] = new SimplePropertySupport<Boolean>(
                "skipLeading",
                boolean.class,
                _bundle.getString("delim_tree_tab.lbl.skip_leading"),
                _bundle.getString("delim_tree_tab.lbl.skip_leading_short")); //NOI18N
        props[COL_COLLAPSE] = new SimplePropertySupport<Boolean>(
                "collapse",
                boolean.class,
                _bundle.getString("delim_tree_tab.lbl.collapse"),
                _bundle.getString("delim_tree_tab.lbl.collapse_short")); //NOI18N
        setProperties(props);

        double ratio = getFont().getSize2D() / 12;
        
        setTableColumnPreferredWidth(COL_TYPE,              (int) (ratio * 65));
        setTableColumnPreferredWidth(COL_PRECEDENCE,        (int) (ratio * 68));
        setTableColumnPreferredWidth(COL_OPTIONAL_MODE,     (int) (ratio * 60));
        setTableColumnPreferredWidth(COL_TERMINATOR_MODE,   (int) (ratio * 65));
        setTableColumnPreferredWidth(COL_BYTES,             (int) (ratio * 42));
        setTableColumnPreferredWidth(COL_OFFSET,            (int) (ratio * 42));
        setTableColumnPreferredWidth(COL_LENGTH,            (int) (ratio * 42));
        setTableColumnPreferredWidth(COL_DETACHED,          (int) (ratio * 60));
        setTableColumnPreferredWidth(COL_BEGIN_BYTES,       (int) (ratio * 60));
        setTableColumnPreferredWidth(COL_BEGIN_OFFSET,      (int) (ratio * 60));
        setTableColumnPreferredWidth(COL_BEGIN_LENGTH,      (int) (ratio * 65));
        setTableColumnPreferredWidth(COL_BEGIN_DETACHED,    (int) (ratio * 78));
        setTableColumnPreferredWidth(COL_SKIP_LEADING,      (int) (ratio * 35));
        setTableColumnPreferredWidth(COL_COLLAPSE,          (int) (ratio * 50));
    }

    private static class SimplePropertySupport<T>
            extends PropertySupport.ReadWrite<T> {
        
        private T mValue;
        
        public SimplePropertySupport(String name, Class<T> clazz,
                String shortDesc, String displayName) {
            super(name, clazz, displayName, shortDesc);
        }
        
        public T getValue()
            throws IllegalAccessException, InvocationTargetException {
            return mValue;
        }

        public void setValue(T object)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
            mValue = object;
        }
    }
}
