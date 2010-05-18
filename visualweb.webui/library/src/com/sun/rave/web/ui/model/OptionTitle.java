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
package com.sun.rave.web.ui.model;

/**
 * Use the OptionsTitle class to add a Title to a list or
 * array of Options. The label text will be rendered marked by dashes.
 * <p>
 * Use this class instead of OptionTitle to indicate that no items have
 * been selected from this list. This is important in cases where a list
 * component like a DropDown or Listbox, may be bound to a null initial
 * component value.
 * </p><p>
 * When these components are bound to a null value, especialy a DropDown
 * where a value will always be submitted, it is not possible to
 * detect without a special Option that no change has actually occurred
 * and a user didn't explicitly select an option. This can
 * result in an incorrect value change event and model update.
 * </p><p>
 * When an OptionTitle is the first option in an array or
 * list of Option's assigned to the "items" property of a
 * DropDown, OptionTitle's value will be submitted when the form
 * is submitted, if a different option is not chosen by the user. When the
 * submitted value is decoded, this value will be identified and will treat the
 * submission of the DropDown as if it was not submitted.
 * </p><p>
 * This prevents a false update in the case where a user had not made
 * any change at all and the initial value of the DropDown does not
 * match the submitted value, such is the case when the initial value
 * of the component is null.
 * </p><p>
 * OptionTitle is also useful for a Listbox where once an
 * an item is selected it cannot be deselected. Using OptionTitle
 * gives the user an opportunity to select it, thereby deselecting a
 * previous selection. On form submit the list will appear as if nothing
 * had been selected.
 * </p><p>
 * NOTE: Calling setValue on this class will not change its
 * value. The label can be set.
 * </p>
 */
public class OptionTitle extends Option {
    
    public static final String NONESELECTED = "com_sun_rave_web_ui_NONESELECTED";
  
    public OptionTitle(String title) { 
        super(NONESELECTED, title); //NOI18N
    } 

    /*
    public String getLabel() {

	// FIXME: The dash "look" should be part of the Theme
	// ideally as a format String with one parameter.
	// 
        String label = super.getLabel();
        label = "&#8212; ".concat(label).concat(" &#8212;"); 
        return label; 
    }
     */

    /**
     * The value will not be changed, if this method is called.
     */
    public void setValue(String value) {
    }
}
