/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author marekfukala
 */
@ManagedBean(name="MBean")
@RequestScoped
public class MBean {

    protected String name;
    
    /** Creates a new instance of MBean */
    public MBean() {
    }

    public String getName() {
        return name;
    }

}
