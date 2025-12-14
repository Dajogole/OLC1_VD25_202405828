package utils;

public class SourceLocation {
    private final String file;
    private final Position position;

    public SourceLocation(String file, Position position) {
        this.file = file;
        this.position = position;
    }

    public String getFile() {
        return file;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return file + ":" + position;
    }
}