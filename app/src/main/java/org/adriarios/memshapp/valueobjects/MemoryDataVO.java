package org.adriarios.memshapp.valueobjects;

/**
 * Created by Adrian on 22/03/2015.
 */
public class MemoryDataVO {
    private int id;
    private String title;
    private String text;
    private String audioPath;
    private String videoPath;
    private String imagePath;
    private String date;
    private Double latitude;
    private Double longitude;
    private String memoryCode;

    public MemoryDataVO(int id,
                        String title,
                        String text,
                        String audioPath,
                        String videoPath,
                        String imagePath,
                        Double latitude,
                        Double longitude,
                        String date,
                        String memoryCode) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.audioPath = audioPath;
        this.videoPath = videoPath;
        this.imagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.memoryCode = memoryCode;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getMemoryCode() {
        return memoryCode;
    }

    public void setMemoryCode(String memoryCode) {
        this.memoryCode = memoryCode;
    }


}
