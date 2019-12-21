package com.example.ds.final_project.db.DTO;

public class SizeSkirt extends Size {
    float total;
    float waist;
    float tail;

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getWaist() {
        return waist;
    }

    public void setWaist(float waist) {
        this.waist = waist;
    }

    public float getTail() {
        return tail;
    }

    public void setTail(float tail) {
        this.tail = tail;
    }

    @Override
    public String toString() {
        String str="";
        str+="총장: "+getTotal();
        str+="\n허리단면: "+getWaist();
        str+="\n밑단단면: "+getTail();
        return str;
    }
}
