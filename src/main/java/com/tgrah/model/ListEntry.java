package com.tgrah.model;

public class ListEntry {

    private String name;
    private String secret;
    private Long totp;

    public ListEntry() {
    }

    public ListEntry(String name, String secret, Long totp) {
        this.name = name;
        this.secret = secret;
        this.totp = totp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getTotp() {
        return totp;
    }

    public void setTotp(Long totp) {
        this.totp = totp;
    }
}
