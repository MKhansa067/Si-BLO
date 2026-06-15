package com.siblo.rent.dto;

import java.util.List;

public class BookingRequest {
    private Long courtId; private List<Long> slotIds; private String date;

    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }
    public List<Long> getSlotIds() { return slotIds; }
    public void setSlotIds(List<Long> slotIds) { this.slotIds = slotIds; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
