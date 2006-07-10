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

package javax.swing;

import org.netbeans.performance.Benchmark;
import java.awt.*;
import javax.swing.border.*;

/**
 * The Benchmark measuring the difference between creating new JLabel
 * (a typical CellRenderer component) and setting up existing JLabel
 * for usage as a cell renderer in a JList.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class ListCellTest extends Benchmark {

    public ListCellTest(String name) {
        super( name );
    }

    private static JList list = new JList();


    /**
     * Create <i>count</i> new JLabels and give them a name;
     */
    public void testCreateNew() throws Exception {
        int count = getIterationCount();
	doIt( count, new CreatingListCellRenderer() );
    }

    public void testSetupShared() throws Exception {
        int count = getIterationCount();
	doIt( count, new SharingListCellRenderer() );
    }

    private void doIt(int count, ListCellRenderer source) {
    	Component[] arr = new Component[count]; 
        while( --count >= 0 ) {
	    arr[count] = source.getListCellRendererComponent( list,
		"Hello" + count, count, count < 1, count < 1); 
        }
    }


    public static void main( String[] args ) {
	simpleRun( ListCellTest.class );
    }
    
    private static class SharingListCellRenderer implements ListCellRenderer {
	private static JLabel stamp;
	private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
	    
	    if( stamp == null ) stamp = new JLabel();
	    
	    if( isSelected ) {
		stamp.setBackground(list.getSelectionBackground());
		stamp.setForeground(list.getSelectionForeground());
	    } else {
		stamp.setBackground(list.getBackground());
	        stamp.setForeground(list.getForeground());
	    }
	    
	    stamp.setText(value.toString());
	    stamp.setFont(list.getFont());
	    stamp.setBorder((cellHasFocus) ? 
		    UIManager.getBorder("List.focusCellHighlightBorder") : 
		    noFocusBorder );
		    
	    return stamp;
	}
    }
    
    private static class CreatingListCellRenderer implements ListCellRenderer {
	private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
	    
	    JLabel stamp = new JLabel();
	    
	    if( isSelected ) {
		stamp.setBackground(list.getSelectionBackground());
		stamp.setForeground(list.getSelectionForeground());
	    } else {
		stamp.setBackground(list.getBackground());
	        stamp.setForeground(list.getForeground());
	    }
	    
	    stamp.setText(value.toString());
	    stamp.setFont(list.getFont());
	    stamp.setBorder((cellHasFocus) ? 
		    UIManager.getBorder("List.focusCellHighlightBorder") : 
		    noFocusBorder );
		    
	    return stamp;
	}
    }
}
