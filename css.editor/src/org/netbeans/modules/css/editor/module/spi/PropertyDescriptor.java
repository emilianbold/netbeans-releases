/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.spi;

import java.util.Collection;

/**
 * Described a CSS property.
 * 
 * Description of the grammar defining the property values:
 * (the grammar is *almost* the same as used for the property values definitions in the w3c.org specifications)
 * 
 * [ ]              denotes a group of elements
 * <ref>            represents a reference to another element named ref or -ref
 * *,+,?,{min,max}  multiplicity of elements or groups
 * e1 | e2          e1 or e2
 * e1 || e2         e1 or e2 or both
 * e1 e2            e1 followed by e2
 * !unit            represents a unit recognized by a CssPropertyValueAcceptor
 * 
 * Example: 
 * <uri> [ , <uri>]*            list of URIs separated by comma
 * [ right | left ] || center   
 * [ !length | !percentage ] !identifier
 * !identifier{1,4}
 * 
 * element name starting with at-sign (@) denotes an artificial property which can be referred 
 * by other elements but will not be exposed to the editor (completion, error checks etc..)
 * 
 * One may use Utilities.parsePropertyDefinitionFile(pathToTheProperiesFile) to obtain the list of 
 * PropertyDescriptor-s from a properties file.
 * 
 * @author mfukala@netbeans.org
 */
public class PropertyDescriptor {
    
    private String name, valueGrammar, initialValue, appliedTo;
    private boolean isInherited;
    private Collection<String> supportedMedias;
    private Collection<RenderingEngine> engines;

    public PropertyDescriptor(String name, String valueGrammar, String initialValue, String appliedTo, boolean isInherited, Collection<String> supportedMedias, Collection<RenderingEngine> engines) {
        this.name = name;
        this.valueGrammar = valueGrammar;
        this.initialValue = initialValue;
        this.appliedTo = appliedTo;
        this.isInherited = isInherited;
        this.supportedMedias = supportedMedias;
        this.engines = engines;
    }
    
    /**
     * @return The property name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return  Definition of the value in a form of semi-grammar.
     */
    public String getValueGrammar() {
        return valueGrammar;
    }
    
    /**
     * @return Initial value of the property.
     */
    public String getInitialValue() {
        return initialValue;
    }
    
    /**
     * @return  A text description of to what elements this property applies to.
     */
    public String getAppliedTo() {
        return appliedTo;
    }
    
    /**
     * @return true if the property is inherited in term of css inheritance.
     */
    public boolean isInherited() {
        return isInherited;
    }
    
//    public URL getHelpURL();
    
    /**
     * @return List of medias where this property can be used.
     */
    //TODO: media queries
    public Collection<String> getMedias() {
        return supportedMedias;
    }
    
    /**
     * @return a collection of RenderingEngine-s which suppors the property
     */
    public Collection<RenderingEngine> getRenderingEnginesWithPropertySupport() {
        return engines;
    }
    
}
