package co.edu.unal.gpsandroid.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PointInterest {

    private String type;
    private Geometry geometry;

    class Geometry{
        private String type;
        private List<Double> coordinates;
    }

    private FProperties properties;

    class FProperties{
        private String type;
        private String extrude;
        private String iso_3166_1;
        private String underground;



    }
}
