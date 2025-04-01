package com.darren;

import com.darren.backend.Application;

public class Main {
    public static void main(String[] args) {
        // SpringBootServer.main(args); // This will not work. I suspect because `mvn clean spring-boot:run` performs some additional configuration from the parent in the `pom.xml`.
    }
}
