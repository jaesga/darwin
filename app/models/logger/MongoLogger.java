package models.logger;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import models.Constants;
import models.factory.MongoFactory;

import java.util.Date;
import java.util.Map;

public class MongoLogger extends Logger {

    public void log(String action, Map<String, String> parameters) {
        this.action = action;
        this.timestamp = new Date();
        this.parameters = parameters;
        save();
    }

    protected BasicDBObject beforeSave(BasicDBObject params) {
        return params;
    }

    @Override
    protected void save() {
        DB db = MongoFactory.getDB();
        DBCollection logCollection = db.getCollection(getCollectionName());
        BasicDBObject log = new BasicDBObject();
        log.append(Constants.Logger.FIELD_ACTION, action);
        log.append(Constants.Logger.FIELD_TIMESTAMP, timestamp);
        log.append(Constants.Logger.FIELD_PARAMETERS, parameters);
        log = beforeSave(log);
        logCollection.insert(log);
    }

    protected String getCollectionName() {
        return Constants.Logger.COLLECTION_NAME;
    }

}
