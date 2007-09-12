/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.bpel.design.decoration;


public class Decoration {

    private DimmDescriptor dimmed;
    
    private GlowDescriptor glow;
    
    private StrokeDescriptor stroke;
    
    private TextstyleDescriptor textstyle;
    
    private ComponentsDescriptor components;
    
    private StripeDescriptor stripe;
    
    
    public Decoration() {}
    
    
    public Decoration(Descriptor descriptor) {
        this(new Descriptor[]{descriptor});
    }
    
    
    public Decoration(Descriptor[] descriptors){
        for (Descriptor d : descriptors){
            if (d instanceof DimmDescriptor) {
                dimmed = (DimmDescriptor) d;
            } else if (d instanceof GlowDescriptor){
                glow = (GlowDescriptor) d;
            } else if (d instanceof StrokeDescriptor){
                stroke = (StrokeDescriptor) d;
            } else if (d instanceof TextstyleDescriptor){
                textstyle = (TextstyleDescriptor) d;
            } else if (d instanceof ComponentsDescriptor){
                components = (ComponentsDescriptor) d;
            } else if (d instanceof StripeDescriptor) {
                stripe = (StripeDescriptor) d;
            }
        }
    }
    
    
    
    public GlowDescriptor getGlow() {
        return glow;
    }
    
    
    public boolean hasGlow() {
        return (glow != null);
    }
    
    
    public boolean hasDimmed() {
        return (dimmed != null);
    }
    
    
    public DimmDescriptor getDimmed() {
        return dimmed;
    }
    
    
    
    public StrokeDescriptor getStroke() {
        return stroke;
    }
    
    
    public StripeDescriptor getStripe() {
        return stripe;
    }
    
    
    public boolean hasStroke() {
        return (stroke != null);
    }
    
    
    public boolean hasStripe() {
        return (stripe != null);
    }
    
    
    public TextstyleDescriptor getTextstyle(){
        return textstyle;
    }
    
    
    public boolean hasTextstyle(){
        return (textstyle != null);
    }
    
    
    public boolean  hasComponents(){
        return (components != null);
    }
    
    public ComponentsDescriptor getComponents(){
        return components;
    }
    
    
    
    
    public Decoration combineWith(Decoration d) {
        if (d == null){
            return this;
        }
        
        if (d.hasGlow()){
            glow = d.getGlow();
        }
        
        if (d.hasStroke()){
            stroke = d.getStroke();
        }
        
        if (d.hasDimmed()){
            dimmed = d.getDimmed();
        }
        
        if (d.hasTextstyle()){
            textstyle = d.getTextstyle();
        }
        
        if (d.hasComponents()){
            if (components == null){
                components = new ComponentsDescriptor();
            }
            components.addAll(d.getComponents());
        }
        
        if (d.hasStripe()) {
            stripe = StripeDescriptor.merge(stripe, d.getStripe());
        }
        
        return this;
    }
    
    
}
