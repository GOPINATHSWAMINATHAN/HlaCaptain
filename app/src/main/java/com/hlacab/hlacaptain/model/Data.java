package com.hlacab.hlacaptain.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by gopinath on 18/02/18.
 */

public class Data {
    @SerializedName("resultCode")
    @Expose
    String resultCode;

    @SerializedName("resultMessage")
    @Expose
    String resultMessage;
    @SerializedName("referenceNumber")
    @Expose
    String referenceNumber;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }


    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getName() {
        return resultCode;
    }

    public void setName(String name) {
        this.resultCode = name;
    }

}
