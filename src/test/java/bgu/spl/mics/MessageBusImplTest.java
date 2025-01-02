package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;

public class MessageBusImplTest {
    private MessageBusImpl messageBus;
    //private MicroService testMicroService;
    private MicroService microService1;
    private MicroService microService2;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("MicroService1") {
            @Override
            protected void initialize() {}
        };
        microService2 = new MicroService("MicroService2") {
            @Override
            protected void initialize() {}
        };
        // testMicroService = new MicroService("TestService") {
        //     @Override
        //     protected void initialize() {
        //         // No-op for testing
        //     }
        // };
        messageBus.register(microService1);
        messageBus.register(microService2);
    }

    






}