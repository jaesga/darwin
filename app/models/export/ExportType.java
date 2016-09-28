package models.export;

public enum  ExportType {

    CSV(EntityExporter.CSV_EXTENSION),
    JSON(EntityExporter.JSON_EXTENSION);

    private String extension;

    ExportType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
