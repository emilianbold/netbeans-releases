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
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard.Convertor;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Jan Becicka
 */

public class ClipboardConvertor implements Convertor {

    public Transferable convert(Transferable t) {
        return NodeTransfer.createPaste(new RefactoringPaste(t));
    }
    
    private class RefactoringPasteType extends PasteType {
        Action move;
        RefactoringPasteType(Transferable orig, Node target) {
            Node[] nodes = NodeTransfer.nodes(orig, NodeTransfer.CLIPBOARD_CUT);
            InstanceContent ic = new InstanceContent();
            if (nodes!=null) {
                for (Node n:nodes) {
                    ic.add((n));
                }
            } else {
                return ;
            }
            Dictionary d = new Hashtable();
            d.put("target", target); //NOI18N
            PasteType[] types = target.getPasteTypes(orig);
            if (types.length>0)
                d.put("paste", types[0]); //NOI18N
            ic.add(d);
            Lookup l = new AbstractLookup(ic);
            this.move = RefactoringActionsFactory.moveAction().createContextAwareInstance(l);
        }
        
        public boolean canHandle() {
            if (move==null)
                return false;
            return move.isEnabled();
        }
        public Transferable paste() throws IOException {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (move!=null) {
                        move.actionPerformed(null);
                    }
                };
            });
            return null;
        }
    }

    private class RefactoringPaste implements NodeTransfer.Paste {
        
        private Transferable delegate;
        RefactoringPaste(Transferable t) {
            delegate = t;
        }
        
        public PasteType[] types (Node target) {
            RefactoringPasteType refactoringPaste = new RefactoringPasteType(delegate, target);
            if (refactoringPaste.canHandle())
                return new PasteType[] {refactoringPaste};
            return target.getPasteTypes(delegate);  
        }
    }

}
