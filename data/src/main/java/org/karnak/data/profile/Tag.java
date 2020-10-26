package org.karnak.data.profile;

import org.hibernate.annotations.DiscriminatorOptions;

import javax.persistence.*;

/*https://stackoverflow.com/questions/14810287/hibernate-inheritance-and-relationship-mapping-generics/14919535*/
/*https://docs.jboss.org/hibernate/orm/5.3/javadocs/org/hibernate/annotations/DiscriminatorOptions.html*/
@Entity(name = "Tag")
@Table(name = "tag")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tag_type")
@DiscriminatorOptions(force=true)
public abstract class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "profile_element_id", nullable = false)
    private ProfileElement profileElement;

    String tagValue;

    public Tag() {
    }

    public Tag(String tagValue, ProfileElement profileElement) {
        this.tagValue = tagValue;
        this.profileElement = profileElement;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }
}
