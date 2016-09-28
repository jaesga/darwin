package models.changelog;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import models.Constants;
import models.factory.MongoFactory;
import models.utils.AuthUtils;

import java.util.HashMap;
import java.util.Map;

public class MongoChangeLogPoint extends ChangelogPoint {

    public MongoChangeLogPoint(String version, Map<String, Map<String, String>> message) {
        super(version, message);
    }

    public MongoChangeLogPoint(DBObject u) {
        this.id = u.containsField(Constants.ChangeLogPoint.FIELD_ID) ? u.get(Constants.ChangeLogPoint.FIELD_ID).toString() : AuthUtils.generateToken(Constants.ChangeLogPoint.ID_LENGTH);
        this.version = u.containsField(Constants.ChangeLogPoint.FIELD_VERSION) ? u.get(Constants.ChangeLogPoint.FIELD_VERSION).toString() : "";
        this.message = u.containsField(Constants.ChangeLogPoint.FIELD_MESSAGE) ? ((DBObject)u.get(Constants.ChangeLogPoint.FIELD_MESSAGE)).toMap() : new HashMap<String, Map<String, String>>();
    }

    @Override
    public void save() {
        DBCollection changelogCollection = MongoFactory.getDB().getCollection(Constants.ChangeLogPoint.COLLECTION_NAME);
        changelogCollection.update(new BasicDBObject(Constants.ChangeLogPoint.FIELD_ID, id), toDBObject(), true, false);
    }

    protected DBObject toDBObject() {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append(Constants.ChangeLogPoint.FIELD_ID, id);
        basicDBObject.append(Constants.ChangeLogPoint.FIELD_VERSION, version);
        basicDBObject.append(Constants.ChangeLogPoint.FIELD_MESSAGE, message);
        return basicDBObject;
    }

    @Override
    public void remove() {
        DBCollection changelogCollection = MongoFactory.getDB().getCollection(Constants.ChangeLogPoint.COLLECTION_NAME);
        changelogCollection.remove(new BasicDBObject(Constants.ChangeLogPoint.FIELD_ID, id));
    }
}
