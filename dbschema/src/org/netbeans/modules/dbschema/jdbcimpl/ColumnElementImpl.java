/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import org.netbeans.modules.dbschema.*;

public class ColumnElementImpl extends DBMemberElementImpl implements ColumnElement.Impl {

    protected int _type;
    protected boolean _isNullable;
    protected Integer _length;
    protected Integer _precision;
    protected Integer _scale;
  
    /** Creates new ColumnElementImpl */
    public ColumnElementImpl() {
    }

	/** Creates new ColumnElementImpl with the specified name */
    public ColumnElementImpl (String name) {
        super(name);
	}

    /** Creates new ColumnElementImpl */
    public ColumnElementImpl(String name, String type, String isNullable, String size, String decimal) {
        super(name);
        
        _type = new Integer(type).intValue();
        int nullable = new Integer(isNullable).intValue();

/*
        if (isNullable.trim().equals("YES")) //NOI18N
            _isNullable = true;
        else
            _isNullable = false;
*/
        if (nullable == 0)
            //not allows null (0)
            _isNullable = false;
        else
            //allows null (1) or nobody knows (2)
            _isNullable = true;
        
        if (size != null)
            _length = new Integer(size);
        else
            _length = null;

        if (size != null)
            _precision = new Integer(size);
        else
            _precision = new Integer(0);

        if (decimal != null)
            _scale =  new Integer(decimal);
        else
            _scale = null;
    }
  
    /** Get the value type of the column.
     * @return the type
     */
    public int getType() {
        return _type;
    }
  
    /** Set the value type of the column.
     * @param type the type
     * @throws DBException if impossible
     */
    public void setType(int type) throws DBException {
        _type = type;
    }
  
    /** Returns whether the column is nullable.
     * @return a flag representing whether the column is nullable
     */
    public boolean isNullable() {
        return _isNullable;
    }
  
    /** Set whether the column is nullable.
     * @param flag flag representing whether the column is nullable
     * @throws DBException if impossible
     */
    public void setNullable(boolean isNullable) throws DBException {
        _isNullable = isNullable;
    }
  
    /** Get the length of the column - for character type fields only.
     * @return the length, <code>null</code> if it is not a character type
     * field or there is no length.
     */
    public Integer getLength() {
        return _length;
    }
  
    /** Set the length of the column - for character type fields only.
     * @param length the length for the column if it a character type
     * @throws DBException if impossible
     */
    public void setLength(Integer length) throws DBException {
        _length = length;
    }
  
    /** Get the precision of the column - for numeric type fields only.
     * @return the precision, <code>null</code> if it is not a numeric type
     * field or there is no precision.
     */
    public Integer getPrecision() {
        return _precision;
    }
  
    /** Set the precision of the column - for numeric type fields only.
     * @param precision the precision for the column if it a numeric type
     * @throws DBException if impossible
     */
    public void setPrecision(Integer precision) throws DBException {
        _precision = precision;
    }
  
    /** Get the scale of the column - for numeric type fields only.
     * @return the scale, <code>null</code> if it is not a numeric type
     * field or there is no scale.
     */
    public Integer getScale() {
        return _scale;
    }
  
    /** Set the scale of the column - for numeric type fields only.
     * @param scale the scale for the column if it a numeric type
     * @throws DBException if impossible
     */
    public void setScale(Integer scale) throws DBException {
        _scale = scale;
    }
}
