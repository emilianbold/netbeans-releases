/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author marekfukala
 */
@ManagedBean(name="MBean")
@RequestScoped
public class MBean {

    private String name;

    private String id;
    
    /** Creates a new instance of MBean */
    public MBean() {
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void performAction() {
        
    }

}
