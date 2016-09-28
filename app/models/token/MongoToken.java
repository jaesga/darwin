package models.token;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import models.Constants;
import models.factory.MongoFactory;
import models.utils.AuthUtils;
import models.utils.Mongo2gson;

import java.util.Date;

public class MongoToken extends Token {

    public MongoToken(String email, String token, TokenType type) {
        super(email, token, new Date(), type);
    }

    public MongoToken(String email, String token, TokenType type, JsonObject data) {
        super(email, token, new Date(), type, data);
    }

    public MongoToken(DBObject u, TokenType type) {
        this.email = u.containsField(Constants.Token.FIELD_EMAIL) ? u.get(Constants.Token.FIELD_EMAIL).toString() : "";
        this.token = u.containsField(Constants.Token.FIELD_TOKEN) ? u.get(Constants.Token.FIELD_TOKEN).toString() : "";
        this.generated = u.containsField(Constants.Token.FIELD_GENERATED) ? (Date)u.get(Constants.Token.FIELD_GENERATED) : new Date();
        this.data = u.containsField(Constants.Token.FIELD_DATA) ? Mongo2gson.getAsJsonObject((DBObject) u.get(Constants.Token.FIELD_DATA)) : null;
        this.type = type;
    }

    @Override
    public void save() {
        DB db = MongoFactory.getDB();
        DBCollection tokensCollection = db.getCollection(this.type.getType());
        BasicDBObject updatedValues = new BasicDBObject();
        updatedValues.append(Constants.Token.FIELD_EMAIL, this.email);
        updatedValues.append(Constants.Token.FIELD_TOKEN, this.token);
        updatedValues.append(Constants.Token.FIELD_GENERATED, this.generated);
        if (data != null) {
            updatedValues.append(Constants.Token.FIELD_DATA, JSON.parse(this.data.toString()));
        }
        tokensCollection.update(new BasicDBObject(Constants.Token.FIELD_EMAIL, this.email), new BasicDBObject().append("$set", updatedValues), true, false);
    }

    @Override
    public void remove() {
        DB db = MongoFactory.getDB();
        DBCollection tokens = db.getCollection(this.type.getType());
        BasicDBObject token = new BasicDBObject(Constants.Token.FIELD_TOKEN, this.token);
        tokens.remove(token);
    }

    @Override
    public void renew() {
        token = AuthUtils.generateToken(type.getLength());
        generated = new Date();
        save();
    }

    @Override
    public boolean isValid() {
        return type.getExpirationMillis() > new Date().getTime() - generated.getTime();
    }
}
