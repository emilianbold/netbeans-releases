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

package org.netbeans.modules.cnd.navigation.includeview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.CDataLoader;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.loaders.ExtensionList;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class IncludeNode extends AbstractCsmNode {
    private Image icon;
    private CsmFile object;
    
    /** Creates a new instance of IncludeNode */
    public IncludeNode(CsmFile element, IncludedModel model, IncludedChildren parent) {
        this(element, new IncludedChildren(element,model,parent), false);
    }
    
    public IncludeNode(CsmFile element, Children children, boolean recursion) {
        super(children);
        if (recursion) {
            setName(element.getName()+" "+getString("CTL_Recuesion")); // NOI18N
        } else {
            setName(element.getName());
        }
        object = element;
    }
    
    private static final String CSrcIcon = "org/netbeans/modules/cnd/loaders/CSrcIcon.gif"; // NOI18N
    private static final String HDataIcon = "org/netbeans/modules/cnd/loaders/HDataIcon.gif";  // NOI18N
    private static final String CppSrcIcon = "org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"; // NOI18N
    
    public CsmObject getCsmObject() {
        return object;
    }
    
    @Override
    public Image getIcon(int param) {
        String path = object.getAbsolutePath();
        int i = path.lastIndexOf('.');
        if (i > 0) {
            String iconPath = HDataIcon;
            String suffix = path.substring(i+1);
            if (getCppSourceSuffixes().contains(suffix)){
                iconPath = CppSrcIcon;
            } else if (getCSourceSuffixes().contains(suffix)){
                iconPath = CSrcIcon;
            }
            Image aIcon = Utilities.loadImage(iconPath);
            if (aIcon != null) {
                return aIcon;
            }
        }
        return super.getIcon(param);
    }
    
    public int compareTo(Object o) {
        if( o instanceof IncludeNode ) {
            return getDisplayName().compareTo(((IncludeNode) o).getDisplayName());
        }
        return 0;
    }
    
    @Override
    public Action getPreferredAction() {
        if (object.isValid()) {
            Node parent = getParentNode();
            if (parent instanceof IncludeNode){
                CsmFile find = ((IncludeNode)parent).object;
                for (final CsmInclude inc : object.getIncludes()){
                    if (find.equals(inc.getIncludeFile())) {
                        if (CsmKindUtilities.isOffsetable(inc)){
                            return new AbstractAction(){
                                public void actionPerformed(ActionEvent e) {
                                    CsmUtilities.openSource((CsmOffsetable)inc);
                                }
                            };
                        }
                        break;
                    } else if (object.equals(inc.getIncludeFile())){
                        if (CsmKindUtilities.isOffsetable(inc)){
                            return new AbstractAction(){
                                public void actionPerformed(ActionEvent e) {
                                    CsmUtilities.openSource((CsmOffsetable)inc);
                                }
                            };
                        }
                    }
                }
            }
            return new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    CsmUtilities.openSource(object);
                }
            };
        }
        return super.getPreferredAction();
    }
    
    private static Set<String> cSuffixes;
    private static Set<String> getCSourceSuffixes() {
        if (cSuffixes == null){
            cSuffixes = new HashSet<String>();
            addSuffices(cSuffixes, CDataLoader.getInstance().getExtensions());
        }
        return cSuffixes;
    }
    
    private static Set<String> cppSuffixes;
    private static Set<String> getCppSourceSuffixes() {
        if (cppSuffixes == null){
            cppSuffixes = new HashSet<String>();
            addSuffices(cppSuffixes, CCDataLoader.getInstance().getExtensions());
        }
        return cppSuffixes;
    }
    
    private static void addSuffices(Set<String> suffixes, ExtensionList list) {
        for (Enumeration e = list.extensions(); e != null &&  e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            suffixes.add(ex);
        }
    }
    private String getString(String key) {
        return NbBundle.getMessage(IncludeNode.class, key);
    }

    @Override
    public String getShortDescription() {
        if (object.isValid()) {
            return object.getAbsolutePath();
        }
        return super.getShortDescription();
    }
}