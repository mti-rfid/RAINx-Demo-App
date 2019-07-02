package org.rainrfid.handset.bean;

public class EpcBean {
    private String pc;
    private String epc;
    private double rssi;
    private int count;

    public String getPc() {
        return pc;
    }

    public void setPc(String pc) {
        this.pc = pc;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getRssi() {
        return String.format("%.2f", rssi);
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
