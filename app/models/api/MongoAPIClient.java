package models.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import models.Constants;
import models.factory.MongoFactory;
import models.utils.AuthUtils;


public class MongoAPIClient extends APIClient {


    public MongoAPIClient(String name, String email) {
        super(name, email);
    }

    public MongoAPIClient(DBObject u) {
        this.clientId = u.containsField(Constants.APIClient.FIELD_ID) ? u.get(Constants.APIClient.FIELD_ID).toString() : "";
        this.secret = u.containsField(Constants.APIClient.FIELD_SECRET) ? u.get(Constants.APIClient.FIELD_SECRET).toString() : "";
        this.name = u.containsField(Constants.APIClient.FIELD_NAME) ? u.get(Constants.APIClient.FIELD_NAME).toString() : "";
        this.email = u.containsField(Constants.APIClient.FIELD_EMAIL) ? u.get(Constants.APIClient.FIELD_EMAIL).toString() : "";
    }

    @Override
    public void renewSecret() {
        this.secret = AuthUtils.generateToken(CLIENT_SECRET_LENGTH);
        save();
    }

    @Override
    public boolean isExistingAPIClient() {
        DB db = MongoFactory.getDB();
        String collectionName = getCollectionName();
        DBCollection clients = db.getCollection(collectionName);
        BasicDBObject apiClient = new BasicDBObject();
        apiClient.append(Constants.APIClient.FIELD_NAME, name);
        apiClient.append(Constants.APIClient.FIELD_EMAIL, email);
        return clients.count(apiClient) > 0;
    }

    private BasicDBObject beforeSave(BasicDBObject updatedValues) {
        return updatedValues;
    }

    @Override
    public void save() {
        DB db = MongoFactory.getDB();
        String collectionName = getCollectionName();
        DBCollection clients = db.getCollection(collectionName);
        BasicDBObject updatedValues = new BasicDBObject();
        updatedValues.append(Constants.APIClient.FIELD_ID, this.clientId);
        updatedValues.append(Constants.APIClient.FIELD_NAME, this.name);
        updatedValues.append(Constants.APIClient.FIELD_SECRET, this.secret);
        updatedValues.append(Constants.APIClient.FIELD_EMAIL, this.email);
        updatedValues = beforeSave(updatedValues);
        clients.update(new BasicDBObject(Constants.APIClient.FIELD_ID, clientId), new BasicDBObject().append("$set", updatedValues), true, false);
    }

    @Override
    public void remove() {
        DB db = MongoFactory.getDB();
        String collectionName = getCollectionName();
        DBCollection apiClients = db.getCollection(collectionName);
        BasicDBObject apiClient = new BasicDBObject(Constants.APIClient.FIELD_ID, clientId);
        apiClients.remove(apiClient);
    }

    public String getCollectionName() {
        return Constants.APIClient.COLLECTION_NAME;
    }

}