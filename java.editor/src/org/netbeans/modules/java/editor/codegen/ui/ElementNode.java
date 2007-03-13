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
package org.netbeans.modules.java.editor.codegen.ui;


import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode.Description;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/** Node representing an Element
 *
 * @author Petr Hrebejk, Jan Lahoda, Dusan Balek
 */
public class ElementNode extends AbstractNode {
    
    private Description description;
           
    /** Creates a new instance of TreeNode */
    public ElementNode(Description description) {
        super(description.subs == null ? Children.LEAF: new ElementChilren(description.subs), Lookups.singleton(description));
        this.description = description;
        description.node = this;
        setDisplayName(description.name); 
    }
        
    @Override
    public Image getIcon(int type) {
        if (description.elementHandle == null)
            return super.getIcon(type);
        return Utilities.icon2Image(UiUtils.getElementIcon(description.elementHandle.getKind(), description.modifiers));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
                   
    @Override
    public java.lang.String getDisplayName() {
        return description.name;
    }
            
    @Override
    public String getHtmlDisplayName() {
        return description.htmlHeader;
    }
    
    private static final class ElementChilren extends Children.Keys<Description> {
            
        public ElementChilren(List<Description> descriptions) {
            setKeys(descriptions);            
        }
        
        protected Node[] createNodes(Description key) {
            return new Node[] {new ElementNode(key)};
        }
    }
                       
    /** Stores all interesting data about given element.
     */    
    public static class Description {
        
        private ElementNode node;
        
        private String name;
        private ElementHandle<? extends Element> elementHandle;
        private Set<Modifier> modifiers;        
        private List<Description> subs; 
        private String htmlHeader;
        private boolean isSelected;
        private boolean isSelectable;
        
        public static Description create(List<Description> subs) {
            return new Description("<root>", null, null, subs, null, false, false); // NOI18N
        }
        
        public static Description create(Element element, List<Description> subs, boolean isSelectable, boolean isSelected ) {
            String htmlHeader = null;
            switch (element.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    htmlHeader = createHtmlHeader((TypeElement)element);
                    break;
                case ENUM_CONSTANT:
                case FIELD:
                    htmlHeader = createHtmlHeader((VariableElement)element);
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    htmlHeader = createHtmlHeader((ExecutableElement)element);
                    break;                    
            }
            return new Description(element.getSimpleName().toString(), 
                                   ElementHandle.create(element), 
                                   element.getModifiers(), 
                                   subs, 
                                   htmlHeader,
                                   isSelectable,
                                   isSelected);
        }

        private Description(String name, ElementHandle<? extends Element> elementHandle,
                Set<Modifier> modifiers, List<Description> subs, String htmlHeader,
                boolean isSelectable, boolean isSelected ) {
            this.name = name;
            this.elementHandle = elementHandle;
            this.modifiers = modifiers;
            this.subs = subs;
            this.htmlHeader = htmlHeader;
            this.isSelectable = isSelectable;
            this.isSelected = isSelected;
        }
        
        public boolean isSelectable() {
            return isSelectable;
        }
        
        public boolean isSelected() {
            return isSelected;
        }
        
        public List<Description> getSubs() {
            return subs;
        }
        
        public void setSelected( boolean selected ) {
            this.isSelected = selected;
            if ( node != null ) {       // notity the node
                node.fireDisplayNameChange(null, null);
            }
        }
        
        public ElementHandle<? extends Element> getElementHandle() {
            return elementHandle;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Description))
                return false;
            Description d = (Description)o;
            if (!this.name.equals(d.name))
                return false;
            if (this.elementHandle != d.elementHandle) {
                if (this.elementHandle == null || d.elementHandle == null)
                    return false;
                if (this.elementHandle.getKind() != d.elementHandle.getKind())
                    return false;
                if (!this.elementHandle.signatureEquals(d.elementHandle))
                    return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.elementHandle != null ? this.elementHandle.getKind().hashCode() : 0);
            return hash;
        }
        
        public static Description deepCopy( Description d ) {
         
            List<Description> subsCopy;
                    
            if ( d.subs == null ) {
                subsCopy = null;
            }
            else {            
                subsCopy = new ArrayList<Description>( d.subs.size() );
                for( Description s : d.subs ) {
                    subsCopy.add( deepCopy(s) );
                }
            }
            
            return new Description( d.name, d.elementHandle, d.modifiers, subsCopy, 
                                    d.htmlHeader, d.isSelectable, d.isSelected );
            
        }
        
        private static String createHtmlHeader(ExecutableElement e) {
            StringBuilder sb = new StringBuilder();
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                sb.append(e.getEnclosingElement().getSimpleName());
            } else {
                sb.append(e.getSimpleName());
            }
            sb.append("("); // NOI18N
            for(Iterator<? extends VariableElement> it = e.getParameters().iterator(); it.hasNext(); ) {
                VariableElement param = it.next();
                sb.append(print(param.asType()));
                sb.append(" "); // NOI18N
                sb.append(param.getSimpleName());
                if (it.hasNext()) {
                    sb.append(", "); // NOI18N
                }
            }
            sb.append(")"); // NOI18N
            if ( e.getKind() != ElementKind.CONSTRUCTOR ) {
                TypeMirror rt = e.getReturnType();
                if ( rt.getKind() != TypeKind.VOID ) {
                    sb.append(" : "); // NOI18N
                    sb.append(print(e.getReturnType()));
                }
            }
            return sb.toString();
        }
        
        private static String createHtmlHeader(VariableElement e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getSimpleName());
            if ( e.getKind() != ElementKind.ENUM_CONSTANT ) {
                sb.append( " : " ); // NOI18N
                sb.append(print(e.asType()));
            }
            return sb.toString();
        }
        
        private static String createHtmlHeader(TypeElement e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getSimpleName());
            List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
            if (typeParams != null && !typeParams.isEmpty()) {
                sb.append("&lt;"); // NOI18N
                for(Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                    TypeParameterElement tp = it.next();
                    sb.append(tp.getSimpleName());
                    try {
                        List<? extends TypeMirror> bounds = tp.getBounds();
                        if (!bounds.isEmpty()) {
                            sb.append(printBounds(bounds));
                        }
                    }
                    catch (NullPointerException npe) {
                    }                    
                    if (it.hasNext()) {
                        sb.append(", "); // NOI18N
                    }
                }
                sb.append("&gt;"); // NOI18N
            }
            return sb.toString();
        }
        
        private static String printBounds(List<? extends TypeMirror> bounds) {
            if (bounds.size() == 1 && "java.lang.Object".equals(bounds.get(0).toString())) // NOI18N
                return "";
            StringBuilder sb = new StringBuilder();
            sb.append(" extends "); // NOI18N
            for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
                TypeMirror bound = it.next();
                sb.append(print(bound));
                if (it.hasNext()) {
                    sb.append(" & "); // NOI18N
                }
            }
            return sb.toString();
        }
        
        private static String print( TypeMirror tm ) {
            StringBuilder sb;
            switch (tm.getKind()) {
                case DECLARED:
                    DeclaredType dt = (DeclaredType)tm;
                    sb = new StringBuilder(dt.asElement().getSimpleName().toString());
                    List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                    if (!typeArgs.isEmpty()) {
                        sb.append("&lt;"); // NOI18N
                        for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                            TypeMirror ta = it.next();
                            sb.append(print(ta));
                            if (it.hasNext()) {
                                sb.append(", "); // NOI18N
                            }
                        }
                        sb.append("&gt;"); // NOI18N
                    }                    
                    return sb.toString();
                case TYPEVAR:
                    TypeVariable tv = (TypeVariable)tm;
                    sb = new StringBuilder(tv.asElement().getSimpleName().toString());
                    return sb.toString();
                case ARRAY:
                    ArrayType at = (ArrayType)tm;
                    sb = new StringBuilder(print(at.getComponentType()));
                    sb.append("[]"); // NOI18N
                    return sb.toString();
                case WILDCARD:
                    WildcardType wt = (WildcardType)tm;
                    sb = new StringBuilder("?"); // NOI18N
                    if (wt.getExtendsBound() != null) {
                        sb.append(" extends "); // NOI18N
                        sb.append(print(wt.getExtendsBound()));
                    }
                    if (wt.getSuperBound() != null) {
                        sb.append(" super "); // NOI18N
                        sb.append(print(wt.getSuperBound()));
                    }
                    return sb.toString();
                default:
                    return tm.toString();
            }
        }
            
    }
}
