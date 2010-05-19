/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

public class TabToNextComponentAction extends DesignViewAction {

    private static final long serialVersionUID = 1L;
    private boolean forward;

    public TabToNextComponentAction(DesignView view, boolean forward) {
        super(view);
        this.forward = forward;
    }

    public void actionPerformed(ActionEvent e) {
        
        DesignView view = getDesignView();
        if (view.getCopyPasteHandler().isActive()) {
            view.getCopyPasteHandler().tabNextPlaceholder(forward);
        } else {
            
            Pattern next = view.getSelectionModel().getSelectedPattern();
            

            
            while (true){
                next = getNextPattern(next);
                if(next == null){
                    break;
                }
                if(next.isSelectable()){
                    break;
                }
            }
            
            if (next != null) {
                view.getSelectionModel().setSelectedPattern(next);
                view.scrollSelectedToView();
            }
        }
    }

    private Pattern getNextPattern(Pattern current) {
        DesignView view = getDesignView();
    
       
        if (current == null){
            return null;
        }
        DiagramView current_view = current.getView();
        
        Pattern next = getNextInView(current);
        if (next != null){
            return next;
        }
        
        DiagramView next_view = getNextView(current_view);
        
        if (next_view == null){
            return null;
        }
        return getFirstInView(next_view);
        
    }
    private Pattern getNextInView(Pattern pattern){
        DiagramView current_view = pattern.getView();
        Iterator<Pattern> it = current_view.getPatterns();
        
        Pattern prev = null;
        while(it.hasNext()){
            Pattern p = it.next();
            if (p == pattern){
                if (forward){
                    return it.hasNext() ? it.next() : null;
                } else {
                    return prev;
                }
            
            }
            prev = p; 
        }
        return null;
    }
    private Pattern getFirstInView(DiagramView view){
        Iterator<Pattern> it = view.getPatterns();
        if (forward){
            return it.hasNext() ? it.next() : null;
        } else {
            Pattern last = null;
            while (it.hasNext()){
                last = it.next();
            }
            return last;
        }
    }
    private DiagramView getNextView(DiagramView view){
        DesignView designView = getDesignView();
        if (view == designView.getConsumersView()){
            return forward ? designView.getProcessView() : designView.getProvidersView();
        } else if(view == designView.getProcessView()) {
            return forward ? designView.getProvidersView() : designView.getConsumersView();
        } else if(view == designView.getProvidersView()) {
            return forward ? designView.getConsumersView() : designView.getProcessView();
        }
        return null;
    }
}
