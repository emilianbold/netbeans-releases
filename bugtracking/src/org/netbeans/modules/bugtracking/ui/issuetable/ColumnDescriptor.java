
package org.netbeans.modules.bugtracking.ui.issuetable;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport.ReadOnly;

/**
 *
 * @author Tomas Stupka
 */
/**
 * Describes a particular column in the queries table
 */
public class ColumnDescriptor<T> extends ReadOnly<T> {
    private int width;
    public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
        this(name, type, displayName, shortDescription, -1); // -1 means default
    }
    public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription, int width) {
        super(name, type, displayName, shortDescription);
        this.width = width;
    }
    @Override
    public T getValue() throws IllegalAccessException, InvocationTargetException {
        return null;
    }
    public int getWidth() {
        return width;
    }
}

