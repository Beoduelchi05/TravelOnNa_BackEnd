package com.travelonna.demo.domain.log.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikesId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer log;
    private Integer user;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikesId likesId = (LikesId) o;
        return Objects.equals(log, likesId.log) &&
                Objects.equals(user, likesId.user);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(log, user);
    }
} 