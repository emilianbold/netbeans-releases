/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.nodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.openide.ErrorManager;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.jdbcimpl.*;

/** Normal implementation of children for source element nodes.
* <P>
* Ordering and filtering of the children can be customized
* using {@link SourceElementFilter}.
* {@link FilterCookie} is implemented to provide a means
* for user customization of the filter.
* <p>The child list listens to changes in the source element, as well as the filter, and
* automatically updates itself as appropriate.
* <p>A child factory can be used to cause the children list to create
* non-{@link DefaultFactory default} child nodes, if desired, both at the time of the creation
* of the children list, and when new children are added.
* <p>The children list may be unattached to any source element temporarily,
* in which case it will have no children (except possibly an error indicator).
*
* @author Dafe Simonek, Jan Jancura
*/
public class SchemaRootChildren extends Children.Keys {

  /** The key describing state of source element */
  static final Object                   NOT_KEY = new Object();
  /** The key describing state of source element */
  static final Object                   ERROR_KEY = new Object();
  
  /** The element whose subelements are represented. */
  protected SchemaElement               element;
  /** Factory for obtaining class nodes. */
  protected DBElementNodeFactory        factory;
  /** Weak listener to the element and filter changes */
  private PropertyChangeListener        wPropL;
  /** Listener to the element and filter changes. This reference must
  * be kept to prevent the listener from finalizing when we are alive */
  private DBElementListener             propL;
  /** Flag saying whether we have our nodes initialized */
  private boolean                       nodesInited = false;


  private boolean parseStatus = false;
  private Object parseLock = new Object();
  
  private org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject obj;
  
  /** Create a children list.
  * @param factory a factory for creating children
  * @param obj database schema data object
  */
  public SchemaRootChildren(final DBElementNodeFactory factory, org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject obj) {
    super();
    this.factory = factory;
    this.obj = obj;
  }
  
    // Children implementation ..............................................................

    /* Overrides initNodes to run the preparation task of the
    * source element, call refreshKeys and start to
    * listen to the changes in the element too. */
    protected void addNotify () {
        SchemaElement el = getElement();

        if (el != null) {
            // listen to the source element property changes
            if (wPropL == null) {
                propL = new DBElementListener();
                wPropL = WeakListeners.propertyChange(propL, el);
            }
            el.addPropertyChangeListener (wPropL);
        }
        
        refreshKeys ();
    }

    protected void removeNotify () {
        setKeys (java.util.Collections.EMPTY_SET);
        nodesInited = false;
    }

    /* Create nodes for given key.
    * The node is created using node factory.
    */
    protected Node[] createNodes (final Object key) {
    if (key instanceof SchemaElement)
        return new Node[] { factory.createSchemaNode((SchemaElement) key) };
    if (NOT_KEY.equals(key))
        return new Node[] { factory.createWaitNode() };
        
    // never should get here
    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
            new Exception ("DbSchema: Error node created for object " + key + 
                " (class " + ((key == null) ? "null" : key.getClass().getName()) + ")"));
    return new Node[] { factory.createErrorNode() };
    }


    // main public methods ..................................................................

    /** Get the currently attached source element.
     * @return the element, or <code>null</code> if unattached
     */
    public SchemaElement getElement () {
        if (element == null && !parseStatus) {
            refreshKeys2();
          
            RequestProcessor.getDefault().post(new Runnable() {
                public void run () {
                    synchronized (parseLock) {
                        if (!parseStatus) {
                            nodesInited = true;
                            setElement(obj.getSchema());
                            parseStatus = true;
                        }
                    }
                }
            }, 0);
        }

        return element;
    }

    /** Set a new source element to get information about children from.
    * @param element the new element, or <code>null</code> to detach
    */
    public void setElement (final SchemaElement element) {
        if (this.element != null)
            this.element.removePropertyChangeListener(wPropL);

        this.element = element;
        if (this.element != null) {
            if (wPropL == null) {
                propL = new DBElementListener();
                wPropL = WeakListeners.propertyChange(propL, this.element);
            }
            else {
                // #55249 - need to recreate the listener with the right element
                wPropL = WeakListeners.propertyChange(propL, this.element);
            }
            
            this.element.addPropertyChangeListener(wPropL);
        }
        
        // change element nodes according to the new element
        if (nodesInited)
            refreshKeys ();
    }

    // other methods ..........................................................................

    /** Refreshes the keys according to the current state of the element and
    * filter etc.
    * (This method is also called when the change of properties occurs either
    * in the filter or in the element)
    * PENDING - (for Hanz - should be implemented better, change only the
    * keys which belong to the changed property).
    * @param evt the event describing changed property (or null to signalize
    * that all keys should be refreshed)
    */
    public void refreshKeys () {
        int status;

        SchemaElement el = getElement();

        if (parseStatus)
            status = (el == null) ? SchemaElement.STATUS_ERROR : el.getStatus();
        else
            status = (el == null) ? SchemaElement.STATUS_NOT : el.getStatus();
            
        switch (status) {
            case SchemaElement.STATUS_NOT:
                setKeys(new Object[] { NOT_KEY });
                break;
            case SchemaElement.STATUS_ERROR:
                setKeys(new Object[] { ERROR_KEY });
                break;
            case SchemaElement.STATUS_PARTIAL:
            case SchemaElement.STATUS_OK:
                refreshAllKeys();
                break;
        }
    }
  
    private void refreshKeys2() {
        setKeys(new Object[] {NOT_KEY});
    }
  
    /** Updates all the keys (elements) according to the current
    * filter and ordering */
    private void refreshAllKeys () {
        int[] order = SchemaElementFilter.DEFAULT_ORDER;

        final LinkedList keys = new LinkedList();
        // build ordered and filtered keys for the subelements
        for (int i = 0; i < order.length; i++) 
            addKeysOfType(keys, order[i]);

        // set new keys
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // #55249 - first reset to empty set, so the old keys are eliminated. 
                // Due to the way equals() is implemented on schema elements, this needs 
                // to be done: two elements are considered equal if their names are equal,
                // even if the sets of subelements are not equal
                setKeys2(Collections.EMPTY_SET);
                setKeys2(keys);
            }
        });
    }

    /** Filters and adds the keys of specified type to the given
    * key collection.
    */
    private void addKeysOfType (Collection keys, final int elementType) {
        SchemaElement schemaElement = (SchemaElement) getElement();
        if (elementType != 0)
            keys.add (schemaElement);
    }

    private void setKeys2(Collection c) {
      setKeys(c);
    }

    // innerclasses ...........................................................................

    /** The listener for listening to the property changes in the filter.
    */
    private final class DBElementListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            boolean refresh = DBElementProperties.PROP_SCHEMA.equals(evt.getPropertyName());
            if (!refresh && DBElementProperties.PROP_STATUS.equals(evt.getPropertyName())) {
                Integer val = (Integer) evt.getNewValue();
                refresh = ((val == null) || (val.intValue() != SchemaElement.STATUS_NOT));
            }
            
            if (refresh)
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        refreshKeys();
                    }
                });
        }
    } // end of ElementListener inner class
}
