package org.karnak.data.gateway;

import javax.persistence.*;

@Entity(name = "SOPClassUID")
@Table(name = "sop_class_uid")
public class SOPClassUID {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String ciod;
    private String uid;
    private String name;

    public SOPClassUID(){
    }

    public SOPClassUID(String ciod, String uid, String name){
        this.ciod = ciod;
        this.uid = uid;
        this.name = name;
    }

    public SOPClassUID(String ciod){
        this.ciod = ciod;
    }

    public String getCiod() {
        return ciod;
    }

    public void setCiod(String ciod) {
        this.ciod = ciod;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
