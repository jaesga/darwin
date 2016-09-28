package models.logger;


import java.util.Date;
import java.util.Map;

public abstract class Logger {

    protected String action;
    protected Date timestamp;
    protected Map<String, String> parameters;

    public abstract void log(String action, Map<String, String> parameters);
    protected abstract void save();
}
