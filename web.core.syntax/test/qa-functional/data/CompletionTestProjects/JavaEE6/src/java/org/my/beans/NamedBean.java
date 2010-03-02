/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.my.beans;

import javax.inject.Named;

/**
 *
 * @author jindra
 */
@Named
public class NamedBean {

    private int number;
    private String text;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
