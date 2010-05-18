package org.netbeans.modules.wsdlextensions.email.editor;

import javax.swing.JComponent;

	

	/**
	 * General control interface for the various GUI "forms" (panels, dialogs, etc.)
	 * that comprise the Email implementation of the extensibility element
	 * configuration editor.
	 *
	 */	
	public interface Form {

	    /**
	     * Signal for the form to reread its data model into its view, in effect
	     * discarding uncommitted changes made thru the view.
	     */
	    void refresh();

	    /**
	     * Signal for the form to update its data model with uncommitted changes
	     * made thru its view.
	     */
	    void commit();

	    /**
	     * Populate the form's internal data model with the information provided.
	     * Since the supplied model type is opaque, an implementation may choose to
	     * disregard it if it cannot resolve the object's information to its own
	     * data model.
	     *
	     * @param model Some object tagged as a {@link FormModel} that is of some
	     * more meaningful type that an implementation can process.
	     */
	    void loadModel(FormModel model);

	    /**
	     * Returns the form's own data model.
	     *
	     * @return Form data model
	     */
	    FormModel getModel();

	    /**
	     * The Swing component that represents the form's visual representation.
	     *
	     * @return The form's view.
	     */
	    JComponent getComponent();

	    /** Tag interface */
	    static interface FormModel {
	    }
	    
}
