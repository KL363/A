package org.example.agentback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryTable {
    private int id;
    private String title;
    private String date;
}
