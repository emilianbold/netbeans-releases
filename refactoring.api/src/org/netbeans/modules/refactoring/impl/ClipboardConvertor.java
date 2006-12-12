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
package org.netbeans.modules.refactoring.impl;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.spi.impl.UIUtilities;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard.Convertor;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Jan Becicka
 */

public class ClipboardConvertor implements Convertor {
    
    public Transferable convert(Transferable t) {
        Node[] nodes = NodeTransfer.nodes(t, NodeTransfer.CLIPBOARD_CUT);
        
        if (nodes!=null && nodes.length>0) {
            //try to do move refactoring
            InstanceContent ic = new InstanceContent();
            for (Node n:nodes) {
                ic.add((n));
            }
            Hashtable d = new Hashtable();
            ic.add(d);
            Lookup l = new AbstractLookup(ic);
            Action move = RefactoringActionsFactory.moveAction().createContextAwareInstance(l);
            if (move.isEnabled()) {
                ExTransferable tr = ExTransferable.create(t);
                tr.put(NodeTransfer.createPaste(new RefactoringPaste(t, ic, move, d)));
                return tr;
            }
        }
        nodes = NodeTransfer.nodes(t, NodeTransfer.CLIPBOARD_COPY);
        if (nodes!=null && nodes.length>0) {
            //try to do copy refactoring
            InstanceContent ic = new InstanceContent();
            for (Node n:nodes) {
                ic.add((n));
            }
            Hashtable d = new Hashtable();
            ic.add(d);
            Lookup l = new AbstractLookup(ic);
            Action copy = RefactoringActionsFactory.copyAction().createContextAwareInstance(l);
            if (copy.isEnabled()) {
                ExTransferable tr = ExTransferable.create(t);
                tr.put(NodeTransfer.createPaste(new RefactoringPaste(t, ic, copy, d)));
                return tr;
            }
        }
        return t;
    }
    
    private class RefactoringPaste implements NodeTransfer.Paste {
        
        private Transferable delegate;
        private InstanceContent ic;
        private Action refactor;
        private Hashtable d;
        RefactoringPaste(Transferable t, InstanceContent ic, Action refactor, Hashtable d) {
            delegate = t;
            this.ic = ic;
            this.refactor = refactor;
            this.d=d;
        }
        
        public PasteType[] types(Node target) {
            RefactoringPasteType refactoringPaste = new RefactoringPasteType(delegate, target);
            if (refactoringPaste.canHandle())
                return new PasteType[] {refactoringPaste};
            return new PasteType[0];
        }
        
        private class RefactoringPasteType extends PasteType {
            RefactoringPasteType(Transferable orig, Node target) {
                d.clear();
                d.put("target", target); //NOI18N
                d.put("transferable", orig); //NOI18N
            }
            
            public boolean canHandle() {
                if (refactor==null)
                    return false;
                return refactor.isEnabled();
            }
            public Transferable paste() throws IOException {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (refactor!=null) {
                            refactor.actionPerformed(null);
                        }
                    };
                });
                return null;
            }
            public String getName() {
                return NbBundle.getMessage(UIUtilities.class,"Actions/Refactoring") + " " + refactor.getValue(Action.NAME);
            }
        }
    }
}
