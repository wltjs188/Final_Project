package com.example.ds.final_project.db.DTO;

public class SizeDressOriginal extends Size {
    float total;
    float shoulder;
    float breast;
    float sleeve;

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getShoulder() {
        return shoulder;
    }

    public void setShoulder(float shoulder) {
        this.shoulder = shoulder;
    }

    public float getBreast() {
        return breast;
    }

    public void setBreast(float breast) {
        this.breast = breast;
    }

    public float getSleeve() {
        return sleeve;
    }

    public void setSleeve(float sleeve) {
        this.sleeve = sleeve;
    }

    public String toString() {
        String str="";
        String s1="총기장: "+total +"\n";
        String s2="어깨너비: "+shoulder+"\n";
        String s3="가슴단면: "+breast+"\n";
        String s4="소매기장: "+sleeve+"\n";
        str=s1+s2+s3+s4;
        return str;
    }
}
