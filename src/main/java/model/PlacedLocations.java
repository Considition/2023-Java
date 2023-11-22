package model;

import lombok.Data;

@Data
public class PlacedLocations {
    private double longitud;
    private double latitud;
    private String locationType = "";
    private int freestyle9100Count = -1;
    private int freestyle3100Count = -1;

    public int getFreestyle9100Count() {
        return freestyle9100Count;
    }

    public void setFreestyle9100Count(int freestyle9100Count) {
        if (freestyle9100Count >= 0 && freestyle9100Count <= 5) {
            this.freestyle9100Count = freestyle9100Count;
        } else {
            throw new IllegalArgumentException("Freestyle9100Count must be between 0 and 5.");
        }
    }

    public int getFreestyle3100Count() {
        return freestyle3100Count;
    }

    public void setFreestyle3100Count(int freestyle3100Count) {
        if (freestyle3100Count >= 0 && freestyle3100Count <= 2) {
            this.freestyle3100Count = freestyle3100Count;
        } else {
            throw new IllegalArgumentException("Freestyle3100Count must be between 0 and 5.");
        }
    }
}
