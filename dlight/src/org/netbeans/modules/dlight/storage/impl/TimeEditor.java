/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.storage.impl;

import org.netbeans.modules.dlight.storage.api.Time;
import java.beans.PropertyEditorSupport;
import java.text.NumberFormat;

/**
 * Property editor for {@link Time} value type. Does not actually support
 * editing, but represents nanosecond times in human-readable format.
 *
 * @author Alexey Vladykin
 */
public class TimeEditor extends PropertyEditorSupport {

    private final NumberFormat format;

    /**
     * Creates new instance.
     */
    public TimeEditor() {
        format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(3);
    }

    @Override
    public String getAsText() {
        return format.format(((Time)getValue()).getNanos() / 1e9);
    }

    @Override
    public void setAsText(String text) {
        throw new UnsupportedOperationException();
    }

}
