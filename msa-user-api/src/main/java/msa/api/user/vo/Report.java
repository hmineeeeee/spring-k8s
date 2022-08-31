package msa.api.user.vo;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class Member implements Serializable {

    @Id
    @GeneratedValue
    Long id;
    String name;
    Long amount;
    Date date;
    public Member() {}
    public Member(String name, Long amount, Date date) {
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public static Member join(@NonNull String name, Long amount, Date date) {
        return new Member(name, amount, date);
    }
}
