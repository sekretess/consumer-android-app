package io.sekretess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OneTimeKeyBundleDto {
    @JsonProperty("OPK")
    private String[] opk;
    @JsonProperty("OPQK")
    private String[] opqk;

    public OneTimeKeyBundleDto() {
    }

    public OneTimeKeyBundleDto(String[] opk, String[] opqk) {
        this.opk = opk;
        this.opqk = opqk;
    }

    public String[] getOpk() {
        return opk;
    }

    public void setOpk(String[] opk) {
        this.opk = opk;
    }

    public String[] getOpqk() {
        return opqk;
    }

    public void setOpqk(String[] opqk) {
        this.opqk = opqk;
    }
}
