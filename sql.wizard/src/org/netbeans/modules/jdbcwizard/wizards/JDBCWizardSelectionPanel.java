/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;

import java.util.List;

/**
 * @author
 */
public class JDBCWizardSelectionPanel implements WizardDescriptor.Panel {

    /* Set <ChangeListeners> */
    protected final Set<ChangeListener> listeners = new HashSet<ChangeListener> (1);

    private String title;
    
    private JDBCWizardSelectionPanelUI comp;
    
    /** Creates new form JDBCWizardSelectionPanel */
    public JDBCWizardSelectionPanel(final String title) {
        this.title = title;
    }

    /**
     * 
     */
    public Component getComponent() {
        if (comp == null) {
            comp = new JDBCWizardSelectionPanelUI (this, title);
        }
        return comp;
    }
    /**
     * 
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
         return new HelpCtx(JDBCWizardSelectionPanel.class);
	  }

    /**
     * @param settings
     */
    public void readSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }
	
	}
    /**
     * @param settings
     */
    @SuppressWarnings("unchecked")
    public void storeSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }
		
		final Object selectedOption = wd.getValue();
        if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
        }

        if (comp == null) {
            getComponent ();
        }

		//populateDBModel();
        if(selectedOption.toString().equals("PREVIOUS_OPTION")){
        	//listModel = null;
        	if(comp.availableTablesList != null){
        		if(comp.listModel != null){
	        		comp.listModel.getSourceList().clear();
	        		comp.availableTablesScrollPane.setViewportView(comp.availableTablesList);
	        	}
         	}
        	if(comp.selectedTablesList != null){
        		if(comp.listModel != null){
	        		comp.listModel.getSourceList().clear();
	        		comp.selectedTablesScrollPane.setViewportView(comp.selectedTablesList);
        		}
        	}
        	return;
        }
        
		if(comp.listModel!= null){
			List selList = comp.listModel.getDestinationList();
        	List<DBTable> selTabList = new ArrayList<DBTable> ();
        	Iterator itr = selList.iterator();
        	while(itr.hasNext()){
        		DBTable tabObj = comp.populateDBTable ((String) itr.next());
        		// By default make the table selected
        		tabObj.setSelected(true);
        		selTabList.add(tabObj);
        	}
        	if (wd != null) {
        		wd.putProperty(JDBCWizardContext.SELECTEDTABLES, selTabList.toArray());
        		wd.putProperty(JDBCWizardContext.DBTYPE, comp.dbtype);
        		wd.putProperty(JDBCWizardContext.CONNECTION_INFO, comp.def);
	        }
        }
    }

    /**
     * @see JDBCWizardPanel#addChangeListener
     */
    public final void addChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    /**
     * @see JDBCWizardPanel#removeChangeListener
     */
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    /**
     * @see JDBCWizardPanel#fireChangeEvent
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (this.listeners) {
            it = new HashSet<ChangeListener>(this.listeners).iterator();
        }

        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    /**
     * @see JDBCWizardPanel#isValid
     */
    public boolean isValid() {
        boolean returnVal = false;
         if (comp.selTableLen > 0) {
			returnVal = true;
		}
        return returnVal;
    }

    /**
     * Extends ChangeEvent to convey information on an item being transferred to or from the source
     * of the event.
     */
    public static class TransferEvent extends ChangeEvent {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /** Indicates addition of an item to the source of the event */
        public static final int ADDED = 0;

        /** Indicates removal of an item from the source of the event */
        public static final int REMOVED = 1;

        private Object item;

        private int type;

        /**
         * Create a new TransferEvent instance with the given source, item and type.
         * 
         * @param source source of this transfer event
         * @param item transferred item
         * @param type transfer type, either ADDED or REMOVED
         * @see #ADDED
         * @see #REMOVED
         */
        public TransferEvent(final Object source, final Object item, final int type) {
            super(source);
            this.item = item;
            this.type = type;
        }

        /**
         * Gets item that was transferred.
         * 
         * @return transferred item
         */
        public Object getItem() {
            return this.item;
        }

        /**
         * Gets type of transfer event.
         * 
         * @return ADDED or REMOVED
         */
        public int getType() {
            return this.type;
        }
    }

}
