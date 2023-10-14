package task1;

import lombok.Builder;

@Builder
public class Geo {
    private Double lat;
    private Double lng;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}