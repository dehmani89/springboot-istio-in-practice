package com.dental.oms.order.web;

public class DownstreamException extends RuntimeException {

    private final String service;
    private final int status;

    public DownstreamException(String service, int status) {
        super(service + " returned HTTP " + status);
        this.service = service;
        this.status = status;
    }

    public String getService() { return service; }
    public int getStatus() { return status; }
}
