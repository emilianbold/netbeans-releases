/*
 * TestBean.java
 *
 * Created on April 19, 2005, 1:40 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.test;

/**
 *
 * @author petr
 */
public class TestBean {
    
    /** Creates a new instance of TestBean */
    public TestBean() {
    }

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Holds value of property id.
     */
    private Integer id;

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Integer getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(Integer id) {

        this.id = id;
    }
    
}
