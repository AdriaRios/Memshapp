package org.adriarios.memshapp.valueobjects;

/**
 * Created by Adrian on 17/05/2015.
 */
public class MemoryDataOnLineVO extends MemoryDataVO {
    private String memoryOwner;
    private String memoryCode;
    public MemoryDataOnLineVO(int id, String title, String text, String audioPath,
                              String videoPath, String imagePath, Double latitude,
                              Double longitude, String date, String memoryOwner, String memoryCode) {
        super(id, title, text, audioPath, videoPath, imagePath, latitude, longitude, date);

        this.memoryOwner = memoryOwner;
        this.memoryCode = memoryCode;

    }

    public String getMemoryOwner() {
        return memoryOwner;
    }

    public void setMemoryOwner(String memoryOwner) {
        this.memoryOwner = memoryOwner;
    }

    public String getMemoryCode() {
        return memoryCode;
    }

    public void setMemoryCode(String memoryCode) {
        this.memoryCode = memoryCode;
    }
}
