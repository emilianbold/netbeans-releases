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

package org.netbeans.modules.cnd.modelutil;

import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 * container of paint components
 * manager of icons
 * @author  Vladimir Voskresensky
 */
public abstract class NbCsmPaintComponent extends CsmPaintComponent {


    public final static class NbNamespacePaintComponent extends CsmPaintComponent.NamespacePaintComponent{

        public NbNamespacePaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.NAMESPACE_DEFINITION, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbEnumPaintComponent extends CsmPaintComponent.EnumPaintComponent{
        
        public NbEnumPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.ENUM, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbEnumeratorPaintComponent extends CsmPaintComponent.EnumeratorPaintComponent {
        
        public NbEnumeratorPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.ENUM, CsmUtilities.ENUMERATOR);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbMacroPaintComponent extends CsmPaintComponent.MacroPaintComponent{
        
        public NbMacroPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.MACRO, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }

    public final static class NbClassPaintComponent extends CsmPaintComponent.ClassPaintComponent{
        
        public NbClassPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.CLASS, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }

    public final static class NbTypedefPaintComponent extends CsmPaintComponent.TypedefPaintComponent{
        
        public NbTypedefPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.TYPEDEF, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbStructPaintComponent extends CsmPaintComponent.StructPaintComponent{
        
        public NbStructPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.STRUCT, 0);
            setIcon(newIcon);
            return newIcon;            
        }
        
    }

    public final static class NbUnionPaintComponent extends CsmPaintComponent.UnionPaintComponent{
        
        public NbUnionPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.UNION, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbGlobalVariablePaintComponent extends CsmPaintComponent.GlobalVariablePaintComponent {
        
        public NbGlobalVariablePaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.GLOBAL);
            return newIcon;             
        }
    }
    
    public final static class NbLocalVariablePaintComponent extends CsmPaintComponent.LocalVariablePaintComponent {
        
        public NbLocalVariablePaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.LOCAL);
            return newIcon;             
        }
    }
    
    public final static class NbFileLocalVariablePaintComponent extends CsmPaintComponent.FileLocalVariablePaintComponent {
        
        public NbFileLocalVariablePaintComponent(){
            super();
        }
        
        protected Icon getIcon() {
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.FILE_LOCAL);
            return newIcon;             
        }
    }    
    
    public final static class NbFieldPaintComponent extends CsmPaintComponent.FieldPaintComponent{

        public NbFieldPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.MEMBER);
            return newIcon;            
        }
    
    }    
    
    public final static class NbGlobalFunctionPaintComponent extends CsmPaintComponent.GlobalFunctionPaintComponent{
        
        public NbGlobalFunctionPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION, getModifiers()|CsmUtilities.GLOBAL);
            return newIcon;             
        }
    }
    
    public final static class NbMethodPaintComponent extends CsmPaintComponent.MethodPaintComponent{
        
        public NbMethodPaintComponent(){
            super();
        }
        
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION, getModifiers()|CsmUtilities.MEMBER);
            return newIcon;            
        }
    }
    
    public final static class NbConstructorPaintComponent extends CsmPaintComponent.ConstructorPaintComponent{
        
        public NbConstructorPaintComponent(){
            super();
        }

        
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION, getModifiers()|CsmUtilities.CONSTRUCTOR);
            return newIcon;            
        }
        
    }    
    
    public final static class NbStringPaintComponent extends CsmPaintComponent.StringPaintComponent {
        
        public NbStringPaintComponent(){
            super();
        }
    }    
}
