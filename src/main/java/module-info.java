module com.clp.trabalho {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.scripting;
    requires com.fazecast.jSerialComm;

    opens com.clp.trabalho to javafx.fxml;
    exports com.clp.trabalho;
}