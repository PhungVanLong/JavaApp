package vn.edu.usth.stockdashboard.data.model;

public class CandleData {
    private long time;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    // Getters
    public long getTime() { return time; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public double getVolume() { return volume; }

    // Setters
    public void setTime(long time) { this.time = time; }
    public void setOpen(double open) { this.open = open; }
    public void setHigh(double high) { this.high = high; }
    public void setLow(double low) { this.low = low; }
    public void setClose(double close) { this.close = close; }
    public void setVolume(double volume) { this.volume = volume; }
}