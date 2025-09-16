package org.example.agentback.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdWithUUID {
    private Integer id;
    private String uuid;
}
