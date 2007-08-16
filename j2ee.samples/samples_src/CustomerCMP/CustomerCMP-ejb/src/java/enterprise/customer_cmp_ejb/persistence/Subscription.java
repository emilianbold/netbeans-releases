/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package enterprise.customer_cmp_ejb.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.*;


@Entity
@NamedQuery(name="findAllSubscriptions", query="select s from Subscription s")

public  class Subscription implements java.io.Serializable{

    private String title;
    private String type;
    private Collection<Customer> customers;
    
    public Subscription(){
        
    }
    
    @Id
    public String getTitle(){ //primary key
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }
    
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type=type;
    }

    //access methods for cmr fields
    @ManyToMany(mappedBy="subscriptions")
    public Collection<Customer> getCustomers(){
        return customers;
    }
    public void setCustomers(Collection<Customer> customers){
        this.customers=customers;
    }

    public Subscription (
            String title,
            String type) {

        if (type.equals(SubscriptionType.MAGAZINE)) {
            _create(title,SubscriptionType.MAGAZINE);
        } 
        else if (type.equals(SubscriptionType.JOURNAL)) {
            _create(title,SubscriptionType.JOURNAL);
        } 
        else if (type.equals(SubscriptionType.NEWS_PAPER)) {
            _create(title,SubscriptionType.NEWS_PAPER);
        }
        else
            _create(title,SubscriptionType.OTHER);

    }
    

    private String _create (
            String title,
            String type)  {
        

        setTitle(title);
        setType(type);
        return title;
    }
    
}


    
