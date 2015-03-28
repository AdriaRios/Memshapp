package org.adriarios.memshapp.valueobjects;

/**
 * Created by Adrian on 22/03/2015.
 */
public class MemoryDataVO {
    private String title;
    private String text;
    private String audioPath;
    private String videoPath;
    private String imagePath;
    private Double latitude;
    private Double longitude;

    public MemoryDataVO(String title,
                        String text,
                        String audioPath,
                        String videoPath,
                        String imagePath,
                        Double latitude,
                        Double longitude) {
        this.title = title;
        this.text = text;
        this.audioPath = audioPath;
        this.videoPath = videoPath;
        this.imagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;

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


}
