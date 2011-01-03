package net.glennward.filemanager.taglibrary;

/**
   * Represents the image file types
   * 
   * @author gjw
   * 
   */
  enum Image {
    GIF("gif"), JPG("jpg"), PNG("png");

    private String extension = null;

    /**
     * @param extension
     */
    Image(String extension) {
      this.extension = extension;
    }

    /**
     * @return
     */
    public String extension() {
      return extension;
    }
  }