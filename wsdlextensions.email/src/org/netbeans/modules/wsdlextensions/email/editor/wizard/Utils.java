package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

	/**
	 * Yet another convenience utility class.
	 *
	 * @author Noel.Ang@sun.com
	 */
	
	final class Utils {


	    private static final Logger logger =
	            Logger.getLogger(Utils.class.getName());

	    private Utils() {
	    }

	    public static void dispatchToSwingThread(String name, Runnable runnable) {
	        if (runnable != null) {
	            if (name == null) {
	                name = "(no name given)";
	            }
	            try {
	                SwingUtilities.invokeAndWait(runnable);
	            } catch (InterruptedException e) {
	                logger.log(Level.SEVERE,
	                        "Wait for completion of Swing thread job " + name
	                                + " interrupted.",
	                        e);
	            } catch (InvocationTargetException e) {
	                logger.log(Level.SEVERE,
	                        "Execution of Swing thread job " + name
	                                + " interrupted.",
	                        e);
	            }
	        }
	    }

	    public static void equalizeSizes(JComponent[] components) {
	        if (components != null) {
	            int maxHeight = 0;
	            int maxWidth = 0;
	            for (JComponent component : components) {
	                maxHeight = Math.max(component.getHeight(), maxHeight);
	                maxWidth = Math.max(component.getWidth(), maxWidth);
	            }
	            Dimension dim = new Dimension(maxWidth, maxHeight);
	            for (JComponent component : components) {
	                component.setPreferredSize(dim);
	                component.setSize(dim);
	            }
	        }
	    }

	    public static String safeString(String value) {
	        if (value == null) {
	            value = "";
	        }
	        if (value.startsWith(" ") || value.endsWith(" ")) {
	            value = value.trim();
	        }
	        return value;
	    }

        public static boolean hasValue(String value){
            if(value != null && value.trim().length() > 0)
                return true;
            else
                return false;
        }
	
}
