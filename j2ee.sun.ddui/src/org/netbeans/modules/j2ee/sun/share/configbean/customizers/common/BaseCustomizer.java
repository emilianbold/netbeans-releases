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
/*
 * BaseCustomizer.java
 *
 * Created on October 8, 2003, 11:13 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ResourceBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.openide.ErrorManager;

import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.share.configbean.Base;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.ErrorMessageDB;
import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;


/** Base customizer class is abstract due to not implementing Customizer.setObject()
 *  and getHelpId().  These methods should be implemented by the derived class.
 *
 *  Derived classes of this class should use the following design pattern:
 *
 *  They should have an initComponents() method (typically created and locked by
 *  the NetBeans form editor) that is called from the constructor.
 *
 *  They should also have a user defined method 'void initUserComponents()' that
 *  is called in the constructor <b>after</b> initComponents().  The body of this
 *  method should call <code>addTitlePanel(String)</code>, then perform any required
 *  additions or modifications of the main panel content and then call
 *  <code>addErrorPanel()</code> as the last line to enable the title and error
 *  message displays, respectively.  See WebAppRootCustomizer for an example.
 *
 * @author Peter Williams
 * @version %I%, %G%
 */
public abstract class BaseCustomizer extends JPanel implements Customizer, 
	HelpCtx.Provider, CustomizerErrorPanel.ErrorClient {
			
	/** Reference to the resource bundle in customizers/common
	 */
	protected static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	/** Path for help button image resource
	 */
	private static final String errorGlyphPath = 
		"org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/errorGlyph.gif"; // NOI18N
	
	/** We only want to load this once.  It's only used here, but in case someone
	 *  decides to use it elsewhere, we'll make it public.
	 */
//	public static final ImageIcon panelErrorIcon = 
//		new ImageIcon(Utils.getResourceURL(errorGlyphPath, BaseCustomizer.class));
	public static ImageIcon panelErrorIcon;

	static {
		// This is diagnostic test code to try to get more information about a
		// suspicious intermittant exception
		try {
			panelErrorIcon = new ImageIcon(Utils.getResourceURL(errorGlyphPath, BaseCustomizer.class));
		} catch(NullPointerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
			panelErrorIcon = null;
		}
	}
	
	/** Error/warning message icons.
	 */
	private static final String errorIconPath = 
		"org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/errorIcon.png"; // NOI18N
	private static final String warningIconPath = 
		"org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/warningIcon.png"; // NOI18N
	
	public static ImageIcon errorMessageIcon;
	public static ImageIcon warningMessageIcon;

	static {
		// Diagnostic test code to try to get more information about a suspicious intermittant exception
        // (This is circa NB 4.1, so it may not be necessary anymore).
		try {
			errorMessageIcon = new ImageIcon(Utils.getResourceURL(errorIconPath, InputDialog.class));
		} catch(NullPointerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
			errorMessageIcon = null;
		}
		try {
			warningMessageIcon = new ImageIcon(Utils.getResourceURL(warningIconPath, InputDialog.class));
		} catch(NullPointerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
			warningMessageIcon = null;
		}
	}
    
    
	/** !PW Foreground color for error message text when in the NetBeans IDE.
	 *  See http://ui.netbeans.org/docs/inline_errors/index.html, about
	 *  halfway down, for specification. (Was light blue: [89, 79, 191])
	 */
    private static final Object colorMonitor = new Object();
    private static Color errorTextForegroundColor = null;
    private static Color warningTextForegroundColor = null;
    
    public static Color getErrorForegroundColor() {
        Color result = null;
        
        synchronized(colorMonitor) {
            if(errorTextForegroundColor == null) {
                errorTextForegroundColor = UIManager.getColor("nb.errorForeground");
                if(errorTextForegroundColor == null) {
                    errorTextForegroundColor = new Color(89, 79, 191); // See http://ui.netbeans.org/docs/inline_errors/index.html
                }
            }
            
            result = errorTextForegroundColor;
        }
        
        return result;
    }
	
    public static Color getWarningForegroundColor() {
        Color result = null;
        
        synchronized(colorMonitor) {
            if(warningTextForegroundColor == null) {
                warningTextForegroundColor = UIManager.getColor("Label.foreground");
                if(warningTextForegroundColor == null) {
                    warningTextForegroundColor = new Color(0,0,0); // black
                }
            }
            
            result = warningTextForegroundColor;
        }
        
        return result;
    }
    
	/** -----------------------------------------------------------------------
	 *  State variables managed by BaseCustomizer
	 */
	/** The bean being editing, referenced by base class.  The design pattern
	 *  I've used for the webapp and common customizers also maintains a reference
	 *  to the appropriate derived bean class in the derived customizer class.
	 */
	private Base theBaseBean;
	
	/** The title panel */
	private CustomizerTitlePanel titlePanel;
	
	/** The error panel */
	private CustomizerErrorPanel errorPanel;
	
	
	/** BaseCustomizer constructor */
    public BaseCustomizer() {
		titlePanel = new CustomizerTitlePanel();
		errorPanel = new CustomizerErrorPanel(this);
    }

	/** Adds the title panel (also contains help button).  This method should
	 *  be called in the derived customizer's initUserComponents().  It can be
	 *  called at any time from this method.  (See class description for the
	 *  design pattern that defines 'initUserComponents()'.
	 *
	 * @param title The title to display in the panel, e.g "Sun Web Appplication".
	 */
	protected void addTitlePanel(String title) {
		titlePanel.setCustomizerTitle(title);
		add(titlePanel, titlePanel.getConstraints(), 0);
	}
	
	/** Retrieves a reference to the title panel
	 *
	 * @return a reference to the title panel.
	 */
	public CustomizerTitlePanel getTitlePanel() {
		return titlePanel;
	}
	
	/** Adds the error panel.  This method should be called in the derived 
	 *  customizer's initUserComponents().  It must be called <b>at the end</b>
	 *  of this method, because this panel must be the last panel added to the
	 *  customizer (as it goes at the bottom.)  (See class description for the
	 *  design pattern that defines 'initUserComponents()'.
	 */
	protected void addErrorPanel() {
		add(errorPanel, errorPanel.getConstraints());
	}
	
	/** Retrieves a reference to the error panel
	 *
	 * @return a reference to the error panel.
	 */
	public CustomizerErrorPanel getErrorPanel() {
		return errorPanel;
	}

	
	/** ----------------------------------------------------------------------- 
	 * Implementation of Customizer interface
	 */
	public void setObject(Object bean) {
		if(theBaseBean != bean) {
			if(theBaseBean != null) {
				// Remove any listeners added in addListeners().
				removeListeners();
			}

			if(setBean(bean)) {
				assert (theBaseBean != null) : 
					"Derived class failed to call super.setBean() in their implementation"; // NOI18N
					
				// Initialize the customizer fields with the data from the new bean.
				initFields();
				
				// Add any listeners required (includes message db and control listeners.)
				addListeners();
				
				// Perform validation on bean to refresh visible validation.
				validateBean();
			}
		}
	}
	
	
	/** Initialization method for any bean references maintained by this or
	 *  derived classes.  If derived classes want to maintain a local reference
	 *  to the current bean, presumably typecast to the correct type, or want
	 *  to protect against the wrong type being passed in, override this method
	 *  but make sure 'super.setBean(bean)' is called before doing anything.
	 *
	 *  @param bean This is the bean to be edited and should either be an instance
	 *    of a DConfigBean (e.g. EjbRef) or null (for nothing to edit.)
	 *  @return true if the bean was non-null and the correct type.  Derived classes
	 *    should return null if the object is not the type they expect.
	 */
	protected boolean setBean(Object bean) {
		boolean result = false;
		
		if(bean instanceof Base) {
			theBaseBean = (Base) bean;
			result = true;
		} else {
			theBaseBean = null;
		}
		
		return result;
	}
    
    // Currently used by getTitlePanel -- may change.
    Base getBean() {
        return theBaseBean;
    }

	/** Initialization method called when the bean referenced by the customizer
	 *  changes.  Derived classes should implement this method and provide
	 *  field initialization and enabling/disabling based on the new bean the
	 *  customizer is now editing.  This method is not called if the bean is
	 *  changed to itself.
	 */
	protected abstract void initFields();

	
	/** -----------------------------------------------------------------------
	 *  Validation
	 */
	protected PropertyChangeListener validationListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if(ErrorMessageDB.VALIDATION_STATE_CHANGED.equals(evt.getPropertyName())) {
				validationStateChanged((Boolean) evt.getNewValue());
			} else if(ErrorMessageDB.PARTITION_STATE_CHANGED.equals(evt.getPropertyName())) {
				partitionStateChanged((ErrorMessageDB.PartitionState) evt.getOldValue(), 
					(ErrorMessageDB.PartitionState) evt.getNewValue());
			}
		}
	};
	
	
	/** Adds listener to the message database for this bean.  If derived classes
	 *  override this method, ensure they call this version via super.addListeners().
	 */
	protected void addListeners() {
		ErrorMessageDB.getMessageDB(theBaseBean).addPropertyChangeListener(validationListener);
	}
	
	
	/** Removes listener to the message database for this bean.  If derived classes
	 *  override this method, ensure they call this version via super.removeListeners().
	 */
	protected void removeListeners() {
		ErrorMessageDB.getMessageDB(theBaseBean).removePropertyChangeListener(validationListener);
	}
	
		
	/** Method called by validation database listener when the global validation
	 *  state of the edited bean changes.  True means the bean is valid (and it's
	 *  database is empty).  False means there is at least one error on some partition
	 *  associated with this bean.
	 *
	 *  @param newState New valid state of the edited bean (true = valid).
	 */
	public void validationStateChanged(Boolean newState) {
	}		

	
	/** Method called by validation database listener when any partition corresponding
	 *  to the edited bean changes.  Both old and new states of the partition are
	 *  provided so that the user can determine if just messages changed or if
	 *  the actual validation state of the partition changed.  Override this method
	 *  if you want to display errors from anything but the global partition, such
	 *  as if this customizer has distinct viewing tabs.  See WebAppRootCustomizer
	 *  for an example.
	 *
	 *  @param oldState The former state of this partition.
	 *  @param newState The new state of this partition.
	 */
	public void partitionStateChanged(ErrorMessageDB.PartitionState oldState, 
		ErrorMessageDB.PartitionState newState) {
		showErrors();
	}
	
	
	/** Validates all fields in the bean.
	 *
	 *  @return true if the bean is valid, false otherwise.
	 */
	public boolean validateBean() {
		return theBaseBean.validateFields(false);
    }

	
	/** Validates an individual field (independently of any other errors).
	 *
	 *  @param fieldId the field id of the field to be validated.
	 *  @return true if the bean is valid, false otherwise.	 
	 */
    public boolean validateField(String fieldId) {
		return theBaseBean.validateField(fieldId);
    }
	
	
	/** Short cut so derived classes don't have to get the error panel first.
	 */
	public void showErrors() {
		errorPanel.showErrors(theBaseBean);
	}
	
	
	/** Returns the help ID for this customizer.  If the customizer has multiple
	 *  tabs, the help ID will be for the current active tab (and subtab, etc.)
	 *
	 * @return String representing the current active help ID for this customizer 
	 */
	public abstract String getHelpId();
	
	
	/** ----------------------------------------------------------------------- 
	 *  Implementation of HelpCtx.Provider interface
	 */
	public HelpCtx getHelpCtx() {
		return new HelpCtx(getHelpId());
	}
    
	/** ----------------------------------------------------------------------- 
	 *  Helper method to determine the index of a component within a container
     *  (!PW Why isn't this in java.awt.Container?  Did I miss it?)
     *
     * @param container The container to search.
     * @param target The component to search for.
     * @return integer index of the component within the container or -1 if not found.
	 */
	public static int getComponentIndex(Container container, Component target) {
        int result = -1;
        
        Component [] components = container.getComponents();
        if(components != null) {
            for(int i = 0; i < components.length; i++) {
                if(components[i] == target) {
                    result = i;
                    break;
                }
            }
        }
        
        return result;
	}
	
	/** ----------------------------------------------------------------------- 
	 *  Implementation of CustomizerErrorPanel.ErrorClient interface
	 */
	/** Returns the foreground color to use for the error text.  This is defined
	 *  by NetBeans UI spec as RGB: (89, 79, 191)
	 *
	 * @return Color object representing the desired foreground color.
	 */
	public Color getErrorMessageForegroundColor() {
		return BaseCustomizer.getErrorForegroundColor();
	}
	
	
	/** Gets the current panel descriptor.  Derived classes with subpanels should
	 *  override this method to ensure it returns the partition for the current
	 *  selected panel.
	 *
	 *  @return Global partition object by default.
	 */	
	public ValidationError.Partition getPartition() {
		return ValidationError.PARTITION_GLOBAL;
	}
} 
