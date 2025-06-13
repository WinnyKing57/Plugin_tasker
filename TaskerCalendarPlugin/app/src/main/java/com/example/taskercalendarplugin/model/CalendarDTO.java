package com.example.taskercalendarplugin.model;

public class CalendarDTO {
    private long id;
    private String displayName;
    private String ownerAccount;

    public CalendarDTO(long id, String displayName, String ownerAccount) {
        this.id = id;
        this.displayName = displayName;
        this.ownerAccount = ownerAccount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    @Override
    public String toString() {
        return "CalendarDTO{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", ownerAccount='" + ownerAccount + '\'' +
                '}';
    }
}
