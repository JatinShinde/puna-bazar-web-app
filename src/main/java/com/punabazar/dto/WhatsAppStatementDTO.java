package com.punabazar.dto;

public class WhatsAppStatementDTO {
    private Long customerId;
    private String customerName;
    private String mobileNumber;
    private String city;
    private String formattedMessage;
    private String whatsappUrl;

    public WhatsAppStatementDTO() {}

    public WhatsAppStatementDTO(Long customerId, String customerName, String mobileNumber, String city, String formattedMessage, String whatsappUrl) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.mobileNumber = mobileNumber;
        this.city = city;
        this.formattedMessage = formattedMessage;
        this.whatsappUrl = whatsappUrl;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getFormattedMessage() { return formattedMessage; }
    public void setFormattedMessage(String formattedMessage) { this.formattedMessage = formattedMessage; }

    public String getWhatsappUrl() { return whatsappUrl; }
    public void setWhatsappUrl(String whatsappUrl) { this.whatsappUrl = whatsappUrl; }
}
