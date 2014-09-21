package hu.bme.aut.smeyelframework.timing;

/**
 * Created on 2014.09.21..
 *
 * @author Ákos Pap
 */
public class ElapsedTimeLogItem implements LogItem {

    public static final String KEY_NAME = "measurementname";
    public static final String KEY_ELAPSED_TIME = "result";

    String measurementname;
    double result;

    public ElapsedTimeLogItem(String measurementName, double elapsedTime) {
        this.measurementname = measurementName;
        this.result = elapsedTime;
    }
}
