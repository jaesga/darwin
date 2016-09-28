package models.export;

import com.google.gson.JsonArray;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import models.factory.MongoFactory;
import models.utils.Mongo2gson;
import org.bson.types.ObjectId;
import play.vfs.VirtualFile;

import java.util.Iterator;
import java.util.Set;

public class MongoExporter extends EntityExporter<DBObject> {

    private static final String ID_KEY = "_id";

    @Override
    public VirtualFile export(String entity, Set<String> projection, DBObject query, ExportType type, boolean includeTimestamp) {
        DBCollection collection = MongoFactory.getDB().getCollection(entity);
        if (includeTimestamp) {
            projection.add(T_KEY);
        }
        DBCursor cursor = collection.find(query, buildProjection(projection));
        switch (type) {
            case CSV:
                return exportToCsv(cursor, projection);
            case JSON:
                return exportToJson(cursor);
            default:
                return null;
        }
    }

    private DBObject buildProjection(Set<String> keys) {
        if (keys !=  null && !keys.isEmpty()) {
            BasicDBObject projectionObject = new BasicDBObject();
            for (String key : keys) {
                if (key.equals(T_KEY)){
                    projectionObject.append(ID_KEY, 1);
                }else {
                    projectionObject.append(key, 1);
                }
            }

            if (!projectionObject.containsField(ID_KEY)){
                projectionObject.append(ID_KEY, 0);
            }
            return projectionObject;
        }
        return null;
    }

    private VirtualFile exportToCsv(DBCursor cursor, Set<String> projection) {
        VirtualFile csv = generateFile(ExportType.CSV);
        if (csv != null) {
            StringBuilder content = new StringBuilder();
            String header = buildCsvHeader(projection);
            content.append(header);
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                Iterator<String> it = projection.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    String value;
                    if (key.equals(T_KEY)) {
                        value = String.valueOf(((ObjectId) dbObject.get(ID_KEY)).getTimestamp());
                    } else {
                        value = dbObject.containsField(key) ? dbObject.get(key).toString() : "";
                    }
                    content.append(value);
                    if (it.hasNext()) {
                        content.append(CHAR_SEPARATOR);
                    }
                }
                content.append("\n");
            }
            csv.write(content.toString());
        }
        return csv;
    }

    private VirtualFile exportToJson(DBCursor cursor) {
        VirtualFile json = generateFile(ExportType.JSON);
        if (json != null) {
            JsonArray jsonArray = new JsonArray();
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                if (dbObject.containsField(ID_KEY)){
                    ObjectId id = (ObjectId) dbObject.removeField(ID_KEY);
                    dbObject.put(T_KEY, id.getTimestamp());
                }
                jsonArray.add(Mongo2gson.getAsJsonObject(dbObject));
            }
            json.write(jsonArray.toString());
        }
        return json;
    }

}
