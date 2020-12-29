package com.example.e_rationqueue;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class user {
    private  String cardNo;
    private String cardType;
    private String customerName;
    public user()
    {

    }
    public user(String cardNo,String cardType,String customerName)
    {
        this.cardNo=cardNo;
        this.cardType=cardType;
        this.customerName=customerName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCustomerName() {
        return customerName;
    }
}
