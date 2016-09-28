package models.export;

import play.Play;
import play.vfs.VirtualFile;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public abstract class EntityExporter<T> {

    protected static final String EXPORTS_ROOT = Play.configuration.getProperty("exports.root", "/exports");

    protected static final String CSV_EXTENSION = ".csv";
    protected static final String JSON_EXTENSION = ".json";

    public static final String CHAR_SEPARATOR = ",";
    public static final String CHAR_NEW_LINE = "\n";
    protected static final String T_KEY = "timestamp";

    public enum Type {

    }

    public VirtualFile export(String entity, Set<String> projection, T query, ExportType type) {
        return export(entity, projection, query, type, false);
    }

    public abstract VirtualFile export(String entity, Set<String> projection, T query, ExportType type, boolean includeTimestamp);

    protected VirtualFile generateFile(ExportType type) {
        VirtualFile file = null;
        if (checkExportsDirectoryExist()) {
            file = VirtualFile.fromRelativePath(EXPORTS_ROOT + "/" + UUID.randomUUID() + type.getExtension());
        }
        return file;
    }

    private boolean checkExportsDirectoryExist() {
        VirtualFile exportsDirectory = VirtualFile.fromRelativePath(EXPORTS_ROOT);
        if (!exportsDirectory.exists()) {
            try {
                return exportsDirectory.getRealFile().mkdirs();
            } catch (SecurityException e) {
                return false;
            }
        }
        return true;
    }

    protected String buildCsvHeader(Set<String> keys) {
        String header = "";
        if (keys != null) {
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                header += (it.hasNext()) ? key + CHAR_SEPARATOR : key + CHAR_NEW_LINE;
            }
        }
        return header;
    }
}
