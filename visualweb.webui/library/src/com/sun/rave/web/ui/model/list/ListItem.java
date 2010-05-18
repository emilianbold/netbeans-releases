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
package com.sun.rave.web.ui.model.list;

/**
 *
 * @author avk
 */
public class ListItem {

    Object valueObject;
    String label;
    String value;
    String description = null;
    boolean selected = false;
    boolean disabled = false;
    boolean title = false;

    public ListItem(String label) {
        this.label = label;
        this.valueObject = label;
    }

    public ListItem(Object realValue, String label) {
        this.label = label;
        this.valueObject = realValue;
    }

    public ListItem(Object realValue, String label, boolean disabled) {
        this.label = label;
        this.valueObject = realValue;
        this.disabled = disabled;
    }
    public ListItem(Object realValue, String label, String description,
            boolean disabled) {
        this.label = label;
        this.valueObject = realValue;
        this.description = description;
        this.disabled = disabled;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public Object getValueObject() {
        return valueObject;
    }
    
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    public void setTitle(boolean title) {
        this.title = title;
    }
    
    public boolean isTitle() {
        return title;
    }
   
}
