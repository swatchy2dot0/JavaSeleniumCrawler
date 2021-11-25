package de.swatchy2dot0.base;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AffiliateElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    private String title;

    private String category;

    private List<String> largeImageUrls;

    private List<BufferedImage> downloadedImages;

    private String imageStripRelativePath;

    private Timestamp createDate;

    private Timestamp pinDate;

    private boolean crawled;

    private String alternateId;

    private Double savingPercentage;

    private Double savingOldPrice;

    private Double savingNewPrice;

    private String asin;

    private String linkText;

    public AffiliateElement() {
        largeImageUrls = new ArrayList<>();
        downloadedImages = new ArrayList<>();
        createDate = new Timestamp(new Date().getTime());
        crawled = false;
    }

    public AffiliateElement(String url) {
        this();
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCrawled() {
        return crawled;
    }

    public void setCrawled(boolean crawled) {
        this.crawled = crawled;
    }

    public List<String> getLargeImageUrls() {
        return largeImageUrls;
    }

    public void setLargeImageUrls(List<String> largeImageUrls) {
        this.largeImageUrls = largeImageUrls;
    }

    public List<BufferedImage> getDownloadedImages() {
        return downloadedImages;
    }

    public void setDownloadedImages(List<BufferedImage> downloadedImages) {
        this.downloadedImages = downloadedImages;
    }

    public String getImageStripRelativePath() {
        return imageStripRelativePath;
    }

    public void setImageStripRelativePath(String imageStripRelativePath) {
        this.imageStripRelativePath = imageStripRelativePath;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getPinDate() {
        return pinDate;
    }

    public void setPinDate(Timestamp pinDate) {
        this.pinDate = pinDate;
    }

    public String getAlternateId() {
        return alternateId;
    }

    public void setAlternateId(String alternateId) {
        this.alternateId = alternateId;
    }

    public Double getSavingPercentage() {
        return savingPercentage;
    }

    public void setSavingPercentage(Double savingPercentage) {
        this.savingPercentage = savingPercentage;
    }

    public Double getSavingOldPrice() {
        return savingOldPrice;
    }

    public void setSavingOldPrice(Double savingOldPrice) {
        this.savingOldPrice = savingOldPrice;
    }

    public Double getSavingNewPrice() {
        return savingNewPrice;
    }

    public void setSavingNewPrice(Double savingNewPrice) {
        this.savingNewPrice = savingNewPrice;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AffiliateElement other = (AffiliateElement) obj;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (url == null) {
            return other.url == null;
        } else return url.equals(other.url);
    }

    public String getASIN() {
        return asin;
    }

    public void setASIN(String amazonId) {
        asin = amazonId;
    }

    public String getAffiliateLink() {
        return linkText;
    }

    public void setAffiliateLink(String linkText) {
        this.linkText = linkText;
    }
}