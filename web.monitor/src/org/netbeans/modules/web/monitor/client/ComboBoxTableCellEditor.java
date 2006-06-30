/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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


//***************** package ***********************************************

package org.netbeans.modules.web.monitor.client;


//***************** import ************************************************

import java.util.EventObject;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.IllegalComponentStateException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import javax.swing.table.TableCellEditor;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


//#########################################################################
/**
 *  This is a table cell editor with a combobox as the editing component.
 *  <br>
 *  This is the accessibility patch made due to an accessibility issue with 
 * ComboBoxes inside the JTable.
 *
 */
//#########################################################################

public class      ComboBoxTableCellEditor
       extends    DefaultCellEditor
       implements TableCellEditor
{

    /**
     *  The surrounding panel for the label and the combobox.
     */
    private JPanel editor;

    /**
     *  Listeners for the table added?
     */
    private boolean tableListenerAdded = false;

    /**
     * The table.
     */
    private JTable table;

    /**
     *  To request the focus for the combobox (with SwingUtilities.invokeLater())
     */
    private Runnable comboBoxFocusRequester;

    /**
     *  To popup the combobox (with SwingUtilities.invokeLater())
     */
    private Runnable comboBoxOpener;

    /**
     *  The current row.
     */
    private int currentRow = -1;

    /**
     *  The previously selected value in the editor.
     */
    private Object prevItem;

    /**
     *  The initial value of the editor.
     */
    private Object initialValue;

    /**
     *  React on action events on the combobox?
     */
    private boolean consumeComboBoxActionEvent = true;

    /**
     *  The event that causes the editing to start. We need it to know
     *  if we should open the popup automatically.
     */
    private EventObject startEditingEvent = null;


    /**********************************************************************
     *  Creates a new CellEditor.
     *********************************************************************/

    public ComboBoxTableCellEditor (Object [] values, 
        ListCellRenderer customRenderer)
    {

        super (new JComboBox ());

        setItems (values);
        this.editor = new JPanel (new BorderLayout ());
        setClickCountToStart (1);

        //show the combobox if the mouse clicks at the panel
        this.editor.addMouseListener (new MouseAdapter ()
            {
                public final void mousePressed (MouseEvent evt)
                {
                    eventEditorMousePressed ();
                }
            });

        JComboBox cb = getComboBox ();

        if (customRenderer != null)
            cb.setRenderer(customRenderer);

        cb.addActionListener (new ActionListener ()
            {
                public final void actionPerformed (ActionEvent evt)
                {
                    eventComboBoxActionPerformed ();
                }
            });

        this.comboBoxFocusRequester = new Runnable ()
            {
                public final void run ()
                {
                    getComboBox ().requestFocus ();
                }
            };
        this.comboBoxOpener = new Runnable ()
            {
                public final void run ()
                {
                    if  (startEditingEvent instanceof MouseEvent)
                    {
                        try
                        {
                            getComboBox ().setPopupVisible (true);
                        }
                        catch (IllegalComponentStateException ex)
                        {
                            //silently ignore - seems to be a bug in JTable
                        }
                    }
                }
            };

    }

    public ComboBoxTableCellEditor (Object [] values)
    {

        this (values, null);

    }

    public ComboBoxTableCellEditor ()
    {

        this (new Object [0], null);

    }


    public ComboBoxTableCellEditor (ListCellRenderer customRenderer)
    {

        this (new Object [0], customRenderer);

    }

    /**********************************************************************
     *  Returns the renderer component of the cell.
     *
     *  @interfaceMethod javax.swing.table.TableCellEditor
     *********************************************************************/

    public final Component getTableCellEditorComponent (JTable  table,
                                                        Object  value,
                                                        boolean selected,
                                                        int     row,
                                                        int     col)
    {

        //add a listener to the table
        if  ( ! this.tableListenerAdded)
        {
            this.tableListenerAdded = true;
            this.table = table;
            this.table.getSelectionModel ().addListSelectionListener (new ListSelectionListener ()
            {
                public final void valueChanged (ListSelectionEvent evt)
                {
                    eventTableSelectionChanged ();
                }
            });
        }
        this.currentRow = row;
        this.initialValue = value;

        return getEditorComponent (table, value, selected, row, col);

    }

    protected Component getEditorComponent (JTable  table,
                                            Object  value,
                                            boolean selected,
                                            int     row,
                                            int     col)
    {

        setSelectedItem (value);

        //new or old row?
        selected = table.isRowSelected (row);
        if  (selected)  //old row
        {
            SwingUtilities.invokeLater (this.comboBoxFocusRequester);
            SwingUtilities.invokeLater (this.comboBoxOpener);
            return getComboBox ();
        }

        //the user selected a new row
        this.editor.removeAll ();  //remove the combobox from the panel

        return this.editor;

    }


    /**********************************************************************
     *  Is the cell editable? If the mouse was pressed at a margin
     *  we don't want the cell to be editable.
     *
     *  @param  evt  The event-object.
     *
     *  @interfaceMethod javax.swing.table.TableCellEditor
     *********************************************************************/

	public boolean isCellEditable (EventObject evt)
	{

        this.startEditingEvent = evt;
        if  (evt instanceof MouseEvent  &&  evt.getSource () instanceof JTable)
        {
            MouseEvent me = (MouseEvent) evt;
            JTable table = (JTable) me.getSource ();
            Point pt = new Point (me.getX (), me.getY ());
            int row = table.rowAtPoint (pt);
            int col = table.columnAtPoint (pt);
            Rectangle rec = table.getCellRect (row, col, false);
            if  (me.getY () >= rec.y + rec.height  ||  me.getX () >= rec.x + rec.width)
            {
                return false;
            }
        }

        return super.isCellEditable (evt);

	}

    public Object getCellEditorValue ()
    {

        return prevItem ;

    }

    protected void setSelectedItem (Object item)
    {

        if  (getComboBox ().getSelectedItem () != item)
        {
            this.consumeComboBoxActionEvent = false;
            getComboBox ().setSelectedItem (item);
            this.consumeComboBoxActionEvent = true;
        }

    }

    public final void setItems (Object [] items)
    {

        JComboBox cb = getComboBox ();
        cb.removeAllItems ();
        final int n = (items != null  ?  items.length  :  0);
        for  (int i = 0; i < n; i++)
        {
            cb.addItem (items [i]);
        }

    }

    final void eventEditorMousePressed ()
    {

        this.editor.add (getComboBox ());
        this.editor.revalidate ();
        SwingUtilities.invokeLater (this.comboBoxFocusRequester);

    }

    protected void eventTableSelectionChanged ()
    {

        //stop editing if a new row is selected
        if  ( ! this.table.isRowSelected (this.currentRow))
        {
            stopCellEditing ();
        }

    }

    protected void eventComboBoxActionPerformed ()
    {

        Object item = getComboBox ().getSelectedItem ();
        if  (item != null) prevItem = item;
        if (consumeComboBoxActionEvent) stopCellEditing ();

    }

    public final JComboBox getComboBox ()
    {

        return (JComboBox) getComponent ();

    }


    public final Object getInitialValue ()
    {

        return this.initialValue;

    }


    public final int getCurrentRow ()
    {

        return this.currentRow;

    }


}
