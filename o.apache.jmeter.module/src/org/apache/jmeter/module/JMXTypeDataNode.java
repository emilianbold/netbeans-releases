package org.apache.jmeter.module;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

public class JMXTypeDataNode extends DataNode {
  private static final ResourceBundle bundle = ResourceBundle.getBundle("org/apache/jmeter/images/icon") ;
  
  private final static String NB_ENABLED = "nb.enabled";
  private final static String NB_RAMPUP = "nb.rampup";
  private final static String NB_PORT = "nb.port";
  private final static String NB_SERVER = "nb.server";
  private final static String NB_USERS = "nb.users";
  
  private class MapBasedProperty extends PropertySupport.ReadWrite {
    private Map vars = null;
    
    public MapBasedProperty(final Map vars, final String name, final Class clz, final String displayName, final String shortDescription) {
      super(name, clz, displayName, shortDescription);
      this.vars = vars;
    }
    
    public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      vars.put(getName(), value != null ? value.toString() : null);
    }
    
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
      Object result = vars.get(getName());
      Class typeClass = getValueType();
      Class intClass = Integer.class;
      
      if (getValueType().equals(Integer.class)) {
        result = Integer.parseInt(result.toString());
      }
      return result;
    }
  }
  
  public JMXTypeDataNode(JMXTypeDataObject obj) {
    super(obj, Children.LEAF);
    
    if (obj == null) {
      return;
    }
  }
  
  public Image getOpenedIcon(int i) {
    Image icon = null;
    icon = JMeterUtils.getImage("feather.gif").getImage();
    
    return icon != null ? icon : super.getOpenedIcon(i);
  }
  
  public Image getIcon(int i) {
    Image icon = null;
    icon = JMeterUtils.getImage("feather.gif").getImage();
    
    return icon != null ? icon : super.getIcon(i);
  }
  
  /**
   * Dont use preferred action! The preferred action is called at the creation of new node *TWICE*
   */
  public Action getPreferredAction() {
    return null;
  }
  
  protected Sheet createSheet() {
    Sheet retValue;
    
    Sheet.Set expert = null;
    try {
      JMeterIntegrationEngine engine = JMeterIntegrationEngine.getDefault();
      final String path = FileUtil.toFile(getDataObject().getPrimaryFile()).getCanonicalPath();
      TestPlan root = (TestPlan)engine.getRoot(path);
      final Map vars = root.getUserDefinedVariables();
      
      if (vars.containsKey(NB_ENABLED)) {
        expert = Sheet.createExpertSet();
        expert.put(new MapBasedProperty(vars, NB_SERVER, String.class, "Target server", "Sets the target server"));
        expert.put(new MapBasedProperty(vars, NB_PORT, Integer.class, "Target server port", "Sets the target server port"));
        expert.put(new MapBasedProperty(vars, NB_USERS, Integer.class, "Number of users", "The number of simulated users"));
        expert.put(new MapBasedProperty(vars, NB_RAMPUP, Integer.class, "Rampup time", "The interval between two simulated users (in seconds)"));
        
        expert.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            System.out.println("Property changed");
          }
        });
      }
      
    } catch (Exception e) {}
    
    retValue = super.createSheet();
    retValue.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("Property changed; the sheet");
      }
    });
    if (expert != null) {
      retValue.put(expert);
    }
    return retValue;
  }
  
  
}
