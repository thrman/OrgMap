package com.coocpu.orgmap;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：组织架构数据源
 *
 * @author coo_fee.
 * @Time 2019/9/12.
 */
public class MorgDataBean implements Parcelable {

    private List<MorgDataBean> childs;
    private int cid;
    /*公司id*/
    private String cuuid;
    private String dn;
    private String img;
    private int isenable;
    private int isvalid;
    private String note;
    private String orgcode;
    /*公司名称*/
    private String orgname;
    private String orgplace;
    private String parentUuid;
    private int type;
    private String userid;

    /**
     * 额外增加参数  所在的列列
     */
    private float currentRow;
    /**
     * 新增参数  所在行数
     */
    private int currentLine;
    /**
     * 新增参数 是否选中
     */
    private boolean selected;


    public float getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(float currentRow) {
        this.currentRow = currentRow;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public MorgDataBean() {
    }

    public MorgDataBean(List<MorgDataBean> childs, int cid, String cuuid, String dn, String img,
                        int isenable, int isvalid, String note, String orgcode, String orgname,
                        String orgplace, String parentUuid, int type, String userid, float currentRow,
                        int currentLine, boolean selected) {
        this.childs = childs;
        this.cid = cid;
        this.cuuid = cuuid;
        this.dn = dn;
        this.img = img;
        this.isenable = isenable;
        this.isvalid = isvalid;
        this.note = note;
        this.orgcode = orgcode;
        this.orgname = orgname;
        this.orgplace = orgplace;
        this.parentUuid = parentUuid;
        this.type = type;
        this.userid = userid;
        this.currentRow = currentRow;
        this.currentLine = currentLine;
        this.selected = selected;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getCuuid() {
        return cuuid;
    }

    public void setCuuid(String cuuid) {
        this.cuuid = cuuid;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getIsenable() {
        return isenable;
    }

    public void setIsenable(int isenable) {
        this.isenable = isenable;
    }

    public int getIsvalid() {
        return isvalid;
    }

    public void setIsvalid(int isvalid) {
        this.isvalid = isvalid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOrgcode() {
        return orgcode;
    }

    public void setOrgcode(String orgcode) {
        this.orgcode = orgcode;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public String getOrgplace() {
        return orgplace;
    }

    public void setOrgplace(String orgplace) {
        this.orgplace = orgplace;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<MorgDataBean> getChilds() {
        return childs;
    }

    public void setChilds(List<MorgDataBean> childs) {
        this.childs = childs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.childs);
        dest.writeInt(this.cid);
        dest.writeString(this.cuuid);
        dest.writeString(this.dn);
        dest.writeString(this.img);
        dest.writeInt(this.isenable);
        dest.writeInt(this.isvalid);
        dest.writeString(this.note);
        dest.writeString(this.orgcode);
        dest.writeString(this.orgname);
        dest.writeString(this.orgplace);
        dest.writeString(this.parentUuid);
        dest.writeInt(this.type);
        dest.writeString(this.userid);
        dest.writeFloat(this.currentRow);
        dest.writeInt(this.currentLine);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    protected MorgDataBean(Parcel in) {
        this.childs = new ArrayList<MorgDataBean>();
        in.readList(this.childs, MorgDataBean.class.getClassLoader());
        this.cid = in.readInt();
        this.cuuid = in.readString();
        this.dn = in.readString();
        this.img = in.readString();
        this.isenable = in.readInt();
        this.isvalid = in.readInt();
        this.note = in.readString();
        this.orgcode = in.readString();
        this.orgname = in.readString();
        this.orgplace = in.readString();
        this.parentUuid = in.readString();
        this.type = in.readInt();
        this.userid = in.readString();
        this.currentRow = in.readFloat();
        this.currentLine = in.readInt();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<MorgDataBean> CREATOR = new Creator<MorgDataBean>() {
        @Override
        public MorgDataBean createFromParcel(Parcel source) {
            return new MorgDataBean(source);
        }

        @Override
        public MorgDataBean[] newArray(int size) {
            return new MorgDataBean[size];
        }
    };
}
