package test.designe.app.terminalapp.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Terminal {


    Integer id;
    Boolean isActive;

    Integer activationRequestID;

    Integer transporterId;
}
