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

package org.netbeans.modules.cnd.classview.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Alexander Simon
 */
public class MoreDeclarations extends AbstractAction implements Presenter.Popup {
    private static final String PROP_DECLARATION = "prop_declaration"; // NOI18N
    private Collection<? extends CsmOffsetableDeclaration> arr;
    public MoreDeclarations(Collection<? extends CsmOffsetableDeclaration> arr) {
        this.arr = arr;
    }
    public JMenuItem getPopupPresenter() {
        JMenu result = new JMenu();
        List<ItemWrapper> list = new ArrayList<ItemWrapper>();
        for (CsmOffsetableDeclaration decl : arr) {
            list.add(new ItemWrapper(decl));
        }
        Collections.sort(list);
        result.setText(I18n.getMessage("LBL_MoreDeclarations")); //NOI18N
        if (list.size() < 36) {
            for (ItemWrapper i : list) {
                result.add(createItem(i.decl));
            }
        } else {
            int n = (int)Math.ceil(Math.sqrt((double)list.size()));
            Iterator<ItemWrapper> i = list.iterator();
            while(i.hasNext()){
                JMenu current = new JMenu();
                CsmOffsetableDeclaration first = null;
                CsmOffsetableDeclaration last = null;
                for (int j = 0; j < n && i.hasNext(); j++){
                    CsmOffsetableDeclaration decl = i.next().decl;
                    if (j == 0) {
                        first = decl;
                    } else {
                        last = decl;
                    }
                    current.add(createItem(decl));
                }
                if (first != null && last != null){
                    current.setText(first.getContainingFile().getName()+" ... "+last.getContainingFile().getName()); // NOI18N
                    result.add(current);
                } else {
                    result.add(createItem(first));
                }
            }
        }
        return result;
    }

    private JMenuItem createItem(final CsmOffsetableDeclaration decl) {
        JMenuItem item = new JMenuItem();
        CsmFile file = decl.getContainingFile();
        item.setText(file.getName());
        item.putClientProperty(PROP_DECLARATION, decl);
        item.addActionListener(this);
        return item;
    }

    public void actionPerformed(ActionEvent ae) {
        JMenuItem item = (JMenuItem) ae.getSource();
        CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) item.getClientProperty(PROP_DECLARATION);
        GoToDeclarationAction action = new GoToDeclarationAction(decl);
        action.actionPerformed(null);
    }
    
    private class ItemWrapper implements Comparable<ItemWrapper>{
        private String name;
        private CsmOffsetableDeclaration decl;
        private ItemWrapper(CsmOffsetableDeclaration decl){
            this.decl = decl;
            name = decl.getContainingFile().getName();
        }
        public int compareTo(MoreDeclarations.ItemWrapper o) {
            return name.compareTo(o.name);
        }
    }
}
