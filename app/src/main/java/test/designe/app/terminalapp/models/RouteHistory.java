package test.designe.app.terminalapp.models;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RouteHistory {
    
    RouteHistoryPrimaryKey primaryKey;
    Integer driverId;

    Timestamp toDateTime;
}
