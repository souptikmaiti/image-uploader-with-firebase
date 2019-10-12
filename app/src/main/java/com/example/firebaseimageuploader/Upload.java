package com.example.firebaseimageuploader;

public class Upload {
    private String imageName;
    private String imageUrl;

    public Upload(){}   //empty constructor needed for Firebase database

    public Upload(String imageName, String imageUrl) {
        if(imageName.trim().equals("")) {imageName = "No Name";}

        this.imageName = imageName;
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
