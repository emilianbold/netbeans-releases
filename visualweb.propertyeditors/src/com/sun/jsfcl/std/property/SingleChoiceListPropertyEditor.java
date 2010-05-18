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
package com.sun.jsfcl.std.property;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;

/**
 * @author eric
 *
 * @deprecated
 */
public abstract class SingleChoiceListPropertyEditor extends AbstractPropertyEditor {
    protected final static Object CHOICE_NOT_FOUND_MARKER = new Object();
    protected final static int MAX_CHOICE_COUNT_FOR_TAGS = 8;

    protected List choices;
    protected Object valueChoice;

    public void attachToNewDesignProperty() {

        super.attachToNewDesignProperty();
        initializeValueChoice();
    }

    /**
     * Gets the property value as a string suitable for presentation
     * to a human to edit.
     *
     * @return The property value as a string suitable for presentation
     *       to a human to edit.
     * <p>   Returns "null" is the value can't be expressed as a string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *       be prepared to parse that string back in setAsText().
     */
    public String getAsText() {

        return getStringForChoice(getValueChoice());
    }

    protected Object getChoiceForString(String lookFor) {
        Object result;

        if (lookFor != null) {
            lookFor.trim();
        }
        result = getChoiceForStringImp(lookFor);
        if (result == CHOICE_NOT_FOUND_MARKER) {
            result = getChoiceForStringNotFound(lookFor);
        }
        return result;
    }

    /**
     * This method is inteded to be overriden in order to provide more specific
     * support by subclasses.  The base implementation is to return the string
     * passed in.
     * @param string
     * @return
     */
    protected Object getChoiceForStringImp(String lookFor) {

        if (lookFor == null) {
            lookFor = ""; //NOI18N
        }
        for (Iterator iterator = getChoices().iterator(); iterator.hasNext(); ) {
            Object object;
            String string;

            object = iterator.next();
            string = getStringForChoice(object);
            if (lookFor.equalsIgnoreCase(string)) {
                return object;
            }
        }
        return CHOICE_NOT_FOUND_MARKER;
    }

    protected Object getChoiceForStringNotFound(String lookFor) {

        return lookFor;
    }

    public List getChoices() {

        if (choices == null) {
            choices = getChoicesImp();
        }
        return choices;
    }

    protected abstract List getChoicesImp();

    /**
     * A PropertyEditor may chose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *      not supported.
     */
    public Component getCustomEditor() {

        return new SingleChoiceListPanel(this, getDesignProperty());
    }

    public String getJavaInitializationString() {

        return stringToJavaSourceString(getAsText());
    }

    /**
     * This method is inteded to be overriden in order to provide more specific
     * support by subclasses.  The base implementation is to return the result
     * of toString() on object.
     * @param string
     * @return
     */
    protected String getStringForChoice(Object object) {

        if (object == null) {
            return ""; //NOI18N
        }
        return object.toString();
    }

    public String[] getTags() {
        List choices = getChoices();
        if (choices.size() > MAX_CHOICE_COUNT_FOR_TAGS) {
            return null;
        }
        String[] result = new String[choices.size()];
        int i = 0;
        for (Iterator iterator = choices.iterator(); iterator.hasNext(); i++) {
            result[i] = getStringForChoice(iterator.next());
        }
        return result;
    }

    public Object getValueChoice() {

        return valueChoice;
    }

    protected Object getValueForChoice(Object object) {

        return getStringForChoice(object);
    }

    protected void initializeValueChoice() {

        valueChoice = getChoiceForString((String)getValue());
    }

    public boolean isPaintable() {

        // EATTODO: This CHEEZY as all get out, but its the only call I'm pretty sure is
        //   called first, before the others, and not as often as getTags in one pass when
        //  there is need of tags :(  I used to have it on getTags, but that gets called too
        //  many times, plus the fact that getTags turns around and immediately causes
        //  items to be rebuild again.
        // MUST FIND a better way to do this
        if (wantsAbilityToRefreshChoices()) {
            refreshChoices();
        }
        return super.isPaintable();
    }

    protected boolean isUnsetMarker(Object object) {

        return object == null || "".equals(object); //NOI18N
    }

    public void refreshChoices() {

        choices = null;
    }

    /**
     * Sets the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     *
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        Object object = getChoiceForString(text);
        setValueChoice(object);
    }

    public void setValueChoice(Object object) {

        valueChoice = object;
        setValue(getValueForChoice(object));
        if (isUnsetMarker(object)) {
            unsetProperty();
        }
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {

        return getChoices().size() > MAX_CHOICE_COUNT_FOR_TAGS;
    }

    /*
     * Provide a refresh choices in order to deal with the fact that NB seems
     * to be caching the property editors, and we need to make sure that
     * choices is always up to date.
     * Will be called when we think its a good time to refresh, keep in mind
     * may be called many times.
     * If you list of items is static, no need to answer true.
     * @author eric
     */
    protected boolean wantsAbilityToRefreshChoices() {

        return false;
    }

}
