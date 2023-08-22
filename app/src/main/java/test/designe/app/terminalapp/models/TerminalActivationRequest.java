package test.designe.app.terminalapp.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class TerminalActivationRequest {

    Integer Id;

    String serialNumber;

    Integer transporterId;

    Boolean processed=false;

}
